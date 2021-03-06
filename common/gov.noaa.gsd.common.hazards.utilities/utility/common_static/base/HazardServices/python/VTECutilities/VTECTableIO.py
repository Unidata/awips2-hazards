"""VTECTableIO is a module that provides a number of customized implementations for access to the VTEC active table.
 
 @author: dgilling
 
"""

#
# Software History:
#  Date         Ticket#    Engineer    Description
#  ------------ ---------- ----------- --------------------------
#  Feb 24, 2014  #2826     dgilling     Initial Creation.
#  Dec 08, 2014  #2826     dgilling     Remove _TestEdexVTECTableIO, reinstate
#                                       _JsonVTECTableIO.
#  Dec 11, 2014  #4933     dgilling     _JsonVTECTableIO now encodes using utf-8
#                                        removing unicode.
#  Dec 15, 2014   3846,4375 Tracy.L.Hansen added ability to handle sets in as_str
#  Dec 15, 2014  #2826     dgilling     Fix EDEX-based implementation.
#  Mar 24, 2015  #6110     Robert.Blum  Fix so the hazardServices sub-directory is auto
#                                       created if it doesnt already exist.
#  Nov 17, 2015   3473     Chris.Golden Changed path hazardServices to HazardServices.
#  Apr 05, 2016  16577   Ben.Phillippe  Storage of Hazard Event VTEC records
#  May 06, 2016  18202   Robert.Blum    Changes for operational mode.
#  Jun 24, 2016  20037   Robert.Blum    Adding deleteVtecRecords so records can be purged.


import abc
import cPickle as pickle
import errno
import fcntl
import json
import os
import pwd
import random
import stat
import tempfile
import time

import Logger as LogStream


def getInstance(siteID4, operationalMode=True, testHarness=False):
    """Returns the necessary VTECTableIO implementation.
        
    Keyword arguments:
    siteID4 -- 4-character site identifier.
    operationalMode -- Whether we're operating against the operational or practice table.
    testHarness -- Whether we're executing within the test framework or not.
    """
    ## TODO: Remove this environment variable check once interoperability code is complete. ##
    # NOTE: set this variable to true to use interoperability for Hazard Services.
    useInteropImpl = False
    if "HAZARD_SERVICES_USE_INTEROP" in os.environ and str(os.environ["HAZARD_SERVICES_USE_INTEROP"]).lower() == "true":
        useInteropImpl = True
    
    if not testHarness:
        if useInteropImpl:
            LogStream.logEvent("Interoperability is enabled for the Hazard Services and active table.")
            return _EdexVTECTableIO(siteID4, operationalMode)
        else:
            LogStream.logEvent("Interoperability is disabled for the Hazard Services and active table.")
            return _JsonVTECTableIO(operationalMode)
    else:
        return _MockVTECTableIO()
            
class VTECTableIO(object):
    """An interface that defines access to the VTEC active table."""
    
    __metaclass__ = abc.ABCMeta
    
    def __init__(self):
        pass
    
    @abc.abstractmethod
    def getVtecRecords(self, reqInfo={}):
        """Reads the active table and returns the list of records.
        
        Keyword arguments:
        reqInfo -- Constraints on the data retrieval.
        """
        raise NotImplementedError
    
    @abc.abstractmethod
    def putVtecRecords(self, vtecRecords, reqInfo={}):
        """Persists the given VTEC records to permanent storage.
        
        Keyword arguments:
        vtecRecords -- The VTEC records to persist.
        reqInfo -- Constraints and parameters for the data storage operation.
        """
        raise NotImplementedError

    @abc.abstractmethod
    def deleteVtecRecords(self, vtecRecords, reqInfo={}):
        """Deletes the given VTEC records from permanent storage.
        
        Keyword arguments:
        vtecRecords -- The VTEC records to delete.
        reqInfo -- Constraints and parameters for the data storage operation.
        """
        raise NotImplementedError
    
    @abc.abstractmethod
    def clearVtecTable(self):
        """Empties the active table."""
        raise NotImplementedError


class _MockVTECTableIO(VTECTableIO):
    """A VTECTableIO implementation used solely for unit testing."""

    def __init__(self):
        userName = pwd.getpwuid(os.getuid())[0]
        self.__vtecRecordsFilename = os.path.join(tempfile.gettempdir(), userName, "vtecRecords.db")
        try:
            os.makedirs(os.path.dirname(self.__vtecRecordsFilename))
        except OSError as e:
            if e.errno != errno.EEXIST:
                raise e
        
    def getVtecRecords(self, reqInfo={}):
        vtecRecords = []
        try:
            with open(self.__vtecRecordsFilename, "r+b") as vtecTable:
                vtecRecords = pickle.load(vtecTable)
        except IOError:
            vtecRecords = []
        
        return vtecRecords
    
    def putVtecRecords(self, vtecRecords, reqInfo={}):
        with open(self.__vtecRecordsFilename, "w+b") as vtecTable:
            pickle.dump(vtecRecords, vtecTable)

    def deleteVtecRecords(self, vtecRecords, reqInfo={}):
        self.putVtecRecords([])
    
    def clearVtecTable(self):
        self.putVtecRecords([])

class _JsonVTECTableIO(VTECTableIO):
    """A VTECTableIO implementation that is backed by a JSON file."""

    def __init__(self, operationalMode):
        operationalMode = bool(operationalMode)
        recordsFileName = "vtecRecords.json" if operationalMode else "testVtecRecords.json"
        lockFileName = "vtecRecords.lock" if operationalMode else "testVtecRecords.lock"
        
        import PathManager
        pathMgr = PathManager.PathManager()
        lf = pathMgr.getLocalizationFile("HazardServices", 'CAVE_STATIC', 'USER')
        basepath = lf.getPath()

        # Verify the basepath exists, if it doesnt create it.
        if os.path.isdir(basepath) == False:
            os.makedirs(basepath)

        self.__vtecRecordsLocPath = os.path.join("HazardServices", recordsFileName)
        self.__vtecRecordsFilename = os.path.join(basepath, recordsFileName)
        self.__vtecRecordsLockname = os.path.join(basepath, lockFileName)
        self.__vtecRecordsLockFD = None

    def getVtecRecords(self, reqInfo={}):
        try:
            self._lockVtecDatabase()
            dictTable = self._readVtecDatabase()
        finally:
            if reqInfo.get("lock") != "True" and self.__vtecRecordsLockFD:
                self._unlockVtecDatabase()
        return dictTable
    
    def putVtecRecords(self, vtecRecords, reqInfo={}):
        import PathManager
        pathMgr = PathManager.PathManager()
        vtecLF = pathMgr.getLocalizationFile(self.__vtecRecordsLocPath, 'CAVE_STATIC', 'USER')
        fd = None
        try:
            fd = vtecLF.getFile("w")
            json.dump(vtecRecords, fd)
        finally:
            if fd:
                fd.close()
            vtecLF.save()
            if reqInfo.get("lock") != "True" and self.__vtecRecordsLockFD:
                self._unlockVtecDatabase()
    
    def clearVtecTable(self):
        self.putVtecRecords([])

    def deleteVtecRecords(self, vtecRecords, reqInfo={}):
        self.putVtecRecords([])
        
    def _readVtecDatabase(self):
        import PathManager
        pathMgr = PathManager.PathManager()
        vtecLF = pathMgr.getLocalizationFile(self.__vtecRecordsLocPath, 'CAVE_STATIC', 'USER')
        fd = None
        vtecRecords = []
        try:
            fd = vtecLF.getFile("r")
            vtecRecords = json.load(fd)
            vtecRecords = self.as_str(vtecRecords)
        except:
            vtecRecords = []
        finally:
            if fd:
                fd.close()
        return vtecRecords
    
    def _lockVtecDatabase(self):
        assert self.__vtecRecordsLockname is not None, "lockAT without filename"
        assert self.__vtecRecordsLockFD is None, "Attempted to double lock VTEC table file."

        self.__vtecRecordsLockFD = open(self.__vtecRecordsLockname, 'a+')
        try:
            os.chmod(self.__vtecRecordsLockname, 0664)
        except:
            pass

        locked = False
        while not locked:
            try:
                fcntl.lockf(self.__vtecRecordsLockFD.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
                fcntl.lockf(self.__vtecRecordsLockFD.fileno(), fcntl.LOCK_EX)
                locked = True
            except IOError:
                time.sleep(random.SystemRandom().random())
                
        # if file doesn't exist or zero length, create it (first time)
        if not os.path.isfile(self.__vtecRecordsFilename) or \
          os.stat(self.__vtecRecordsFilename)[stat.ST_SIZE] == 0:
            LogStream.logDebug("Creating new active table.")
            with open(self.__vtecRecordsFilename, "w+") as fd:
                json.dump([], fd)
            try:
                os.chmod(self.__vtecRecordsFilename, 0664)
            except:
                pass
        
    def _unlockVtecDatabase(self):
        assert self.__vtecRecordsLockFD is not None, "Attempted to unlock VTEC table file without previously locking."
        fcntl.lockf(self.__vtecRecordsLockFD.fileno(), fcntl.LOCK_UN)
        self.__vtecRecordsLockFD.close()
        self.__vtecRecordsLockFD = None

    @staticmethod
    def as_str(obj):
        if isinstance(obj, dict):
            return {_JsonVTECTableIO.as_str(key):_JsonVTECTableIO.as_str(value) for key,value in obj.items()}
        elif isinstance(obj, list):  
            return [_JsonVTECTableIO.as_str(value) for value in obj]
        elif isinstance(obj, set):  
            return {_JsonVTECTableIO.as_str(value) for value in obj}
        elif isinstance(obj, unicode):
            return obj.encode('utf-8')
        else:
            return obj

class _EdexVTECTableIO(VTECTableIO):
    """A VTECTableIO implementation that makes thrift requests to retrieve VTEC data from EDEX."""

    def __init__(self, siteID, operationalMode):
        self.siteID = siteID
        self.operationalMode = bool(operationalMode)
        
    def getVtecRecords(self, reqInfo={}):
        import JUtil
        from com.raytheon.uf.common.dataplugin.events.hazards.request import GetHazardActiveTableRequest
        from com.raytheon.uf.common.serialization.comm import RequestRouter     
 
        request = VtecInteroperabilityActiveTableRequest()
        request.setSiteID(self.siteID)
        request.setPractice(not self.operationalMode)
         
        response = RequestRouter.route(request)
        if not response.isSuccess():
            # Notify a client that an error has occurred and halt product generation.
            raise Exception(response.getExceptionText())
          
        vtecMapList = []
        for i in range(response.getResults().size()):
            jVtecMap = response.getResults().get(i)
            vtecMap = JUtil.javaMapToPyDict(jVtecMap)
            if vtecMap is not None:
                vtecMapList.append(vtecMap)
        return vtecMapList
    
    def putVtecRecords(self, vtecRecords, reqInfo={}):
        import JUtil
        from com.raytheon.uf.common.dataplugin.events.hazards.request import StoreHazardEventVtecRequest
        from com.raytheon.uf.common.serialization.comm import RequestRouter
        request = StoreHazardEventVtecRequest()
        request.setPractice(self.operationalMode == False)
        request.setVtecList(JUtil.pyValToJavaObj(vtecRecords))
        response = RequestRouter.route(request)

    def deleteVtecRecords(self, vtecRecords, reqInfo={}):
        import JUtil
        from com.raytheon.uf.common.dataplugin.events.hazards.request import DeleteHazardEventVtecsRequest
        from com.raytheon.uf.common.serialization.comm import RequestRouter
        javaRecords = JUtil.pyValToJavaObj(vtecRecords)
        request = DeleteHazardEventVtecsRequest(javaRecords, self.operationalMode == False)
        response = RequestRouter.route(request)
