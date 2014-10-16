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
import JUtil

import Logger as LogStream


def getInstance(siteID4, operationalMode=True, testHarness=False, automatedTest=False):
    """Returns the necessary VTECTableIO implementation.
        
    Keyword arguments:
    siteID4 -- 4-character site identifier.
    operationalMode -- Whether we're operating against the operational or practice table.
    testHarness -- Whether we're executing within the test framework or not.
    """    
    if not testHarness and not automatedTest:
        return _EdexVTECTableIO(siteID4, operationalMode)
    else:
        if testHarness:
            return _MockVTECTableIO()
        else:
            return _TestEdexVTECTableIO(siteID4, operationalMode)
            
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


class _EdexVTECTableIO(VTECTableIO):
    """A VTECTableIO implementation that makes thrift requests to retrieve VTEC data from EDEX."""

    def __init__(self, siteID, operationalMode):
        self.siteID = siteID
        self.operationalMode = bool(operationalMode)
        self.hazardsConflictDict = None
        self.queryPracticeWarningTable = False
        
    def getVtecRecords(self, reqInfo={}):
        import JUtil        
        from com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests import VtecInteroperabilityActiveTableRequest
        from com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests import VtecInteroperabilityWarningRequest
        from com.raytheon.uf.common.dataplugin.events.hazards.requests import GetHazardsConflictDictRequest
        from com.raytheon.uf.common.serialization.comm import RequestRouter     
        
        phensig = None
        if 'hazardEvents' in reqInfo:
            if self.hazardsConflictDict is None:
                request = GetHazardsConflictDictRequest()
                response = RequestRouter.route(request)
                self.hazardsConflictDict = JUtil.javaObjToPyVal(response)
            
            hazardEvents = reqInfo['hazardEvents']
            phensig = hazardEvents[0].getPhenomenon() + '.' + hazardEvents[0].getSignificance()
            if phensig not in self.hazardsConflictDict and self.operationalMode == False:
                self.queryPracticeWarningTable = True
            else:
                self.queryPracticeWarningTable = False

        if self.queryPracticeWarningTable:
            request = VtecInteroperabilityWarningRequest()
            # TODO This problem occurs because PGF gets called twice.
            # Fix the workflow so product generation gets called only once
            if phensig == None:
                phensig = 'FF.W'
            request.setPhensig(phensig)
        else:
            request = VtecInteroperabilityActiveTableRequest()
            
        request.setSiteID(self.siteID)
        request.setPractice(self.operationalMode == False)
        response = RequestRouter.route(request)
        
        if not response.isSuccess():
            # Notify a client that an error has occurred and halt product generation.
            raise Exception(response.getExceptionText())
        
        vtecMapList = []
        
        #return response.getActiveTable()
        i = 0
        while i < response.getResults().size():
            jVtecMap = response.getResults().get(i)
            vtecMap = JUtil.javaMapToPyDict(jVtecMap)
            if vtecMap is not None:
                vtecMapList.append(vtecMap)
            i += 1
                
        return vtecMapList
    
    def putVtecRecords(self, vtecRecords, reqInfo={}):
        # product transmission/interoperability handles updating active table
        # in this implementation. No need for client to send updated records
        # back to EDEX. 
        pass
    
    def clearVtecTable(self):
        if not self.operationalMode:
            from com.raytheon.uf.common.activetable.request import ClearPracticeVTECTableRequest
            from com.raytheon.uf.viz.core.requests import ThriftClient
            from com.raytheon.uf.viz.core import VizApp
            request = ClearPracticeVTECTableRequest(self.siteID, VizApp.getWsId())
            ThriftClient.sendRequest(request)

# Used for CAVE Automated Tests            
class _TestEdexVTECTableIO(_EdexVTECTableIO):
    """A VTECTableIO implementation that makes thrift requests to retrieve VTEC data from EDEX."""

    def __init__(self, siteID, operationalMode):
        super(_TestEdexVTECTableIO, self).__init__(siteID, operationalMode)
    
    #override
    def putVtecRecords(self, vtecRecords, reqInfo={}):
        from java.util import Date as JDate
        from java.util import ArrayList as JArrayList
        from com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.test import TestVtecInteroperabilityRelation
        relationList = JArrayList()
        for vtecRecord in vtecRecords:                
            record = self._buildVtecRecord(vtecRecord)
            
            relation = TestVtecInteroperabilityRelation()
            relation.setRecord(record)
            relation.setEventID(str(vtecRecord['eventID']))
            relation.setHazardType(vtecRecord['key'])
            
            relationList.add(relation)
            
        from com.raytheon.uf.common.serialization.comm import RequestRouter
        from com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.test import TestPracticeVtecStorageRequest
        request = TestPracticeVtecStorageRequest()
        request.setRelations(relationList)
        response = RequestRouter.route(request)
    
    def _buildVtecRecord(self, vtecRecord):
        from com.raytheon.uf.common.activetable import PracticeActiveTableRecord

        endTime = self._convertPyDateToGMTJCalendar(vtecRecord['endTime'])
        issueTime = self._convertPyDateToGMTJCalendar(vtecRecord['issueTime'])
        startTime = self._convertPyDateToGMTJCalendar(vtecRecord['startTime'])
        purgeTime = self._makePurgeTime()
        
        floodRecordStatus = None
        floodSeverity = None
        immediateCause = None
        if 'hvtec' in vtecRecord.keys() and vtecRecord['hvtec']:
            floodRecordStatus = vtecRecord['hvtec']['floodRecord']
            floodSeverity = vtecRecord['hvtec']['floodSeverity']
            immediateCause = vtecRecord['hvtec']['immediateCause']
        
        if not floodRecordStatus: 
            floodRecordStatus = '00'

        if not floodSeverity:
            floodSeverity = '0'        
            
        record = PracticeActiveTableRecord()
        record.setAct(vtecRecord['act'])
        record.setCountyheader(str(vtecRecord['id']))
        record.setEndTime(endTime)
        record.setEtn(self._padETN(str(vtecRecord['etn'])))
        record.setFloodRecordStatus(str(floodRecordStatus))
        record.setFloodSeverity(str(floodSeverity))
        record.setImmediateCause(immediateCause)
        record.setIssueTime(issueTime)
        record.setOfficeid(vtecRecord['officeid'])
        record.setOverviewText(vtecRecord['hdln'])
        record.setPhen(vtecRecord['phen'])
        record.setPhensig(vtecRecord['phensig'])
        #record.setPil(vtecRecord['phen'] + vtecRecord['sig'])
        record.setPurgeTime(purgeTime)
        record.setSeg(vtecRecord['seg'])
        if 'hvtecstr' in vtecRecord.keys():
            record.setSegText(vtecRecord['hvtecstr'])
        record.setSig(vtecRecord['sig'])
        record.setStartTime(startTime)
        record.setUfn(vtecRecord['ufn'])
        record.setUgcZone(str(vtecRecord['id']))
        record.setVtecstr(vtecRecord['vtecstr'])
        record.setXxxid(vtecRecord['officeid'][1:])
        
        return record
        
    # utility functions
    def _makePurgeTime(self):
        from java.util import Calendar as JCalendar
        
        jCalendar = JCalendar.getInstance()
        jCalendar.add(JCalendar.YEAR, 1)
        
        return jCalendar
    
    def _padETN(self, etnStr):
        desiredETNLength = 4
        currentETNLength = len(etnStr)
        requiredETNLength = desiredETNLength - currentETNLength
        if requiredETNLength > 0:
            prefix = ''
            count = 0
            while count < requiredETNLength:
                prefix = prefix + '0'
                count += 1
            return prefix + etnStr
        else:
            return etnStr
    
    def _convertPyDateToGMTJCalendar(self, pySeconds):
        from java.util import Calendar as JCalendar
        
        timeStruct = time.gmtime(pySeconds)
        yyyy = timeStruct.tm_year
        MM = timeStruct.tm_mon - 1
        dd = timeStruct.tm_mday
        hh = timeStruct.tm_hour
        mm = timeStruct.tm_min
        ss = timeStruct.tm_sec
        jCalendar = JCalendar.getInstance()
        jCalendar.clear()
        jCalendar.set(yyyy, MM, dd, hh, mm, ss)
        
        return jCalendar