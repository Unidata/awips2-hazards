"""VTECTableIO is a module that provides a number of customized implementations for access to the VTEC active table.
 
 @author: dgilling
 
"""

#
# Software History:
#  Date         Ticket#    Engineer    Description
#  ------------ ---------- ----------- --------------------------
#  Feb 24, 2014  #2826     dgilling     Initial Creation.
#
#


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


def getInstance(operationalMode=True, testHarness=False):
    """Returns the necessary VTECTableIO implementation.
        
    Keyword arguments:
    operationalMode -- Whether we're operating against the operational or practice table.
    testHarness -- Whether we're executing within the test framework or not.
    """    
    if not testHarness:
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
        lf = pathMgr.getLocalizationFile("hazardServices", 'CAVE_STATIC', 'USER')
        basepath = lf.getPath()
        
        self.__vtecRecordsLocPath = os.path.join("hazardServices", recordsFileName)
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
        
    def _readVtecDatabase(self):
        import PathManager
        pathMgr = PathManager.PathManager()
        vtecLF = pathMgr.getLocalizationFile(self.__vtecRecordsLocPath, 'CAVE_STATIC', 'USER')
        fd = None
        vtecRecords = []
        try:
            fd = vtecLF.getFile("r")
            vtecRecords = json.load(fd)
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
