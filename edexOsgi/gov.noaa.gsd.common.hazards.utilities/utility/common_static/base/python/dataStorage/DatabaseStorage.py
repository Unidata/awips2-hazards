"""
 Contains methods for accessing Hazard Services
 datasets including VTEC information. This class
 needs to be revisited as several methods have
 been overcome by events.
 
 @since: February 2012
 @author: GSD Hazard Services Team
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 Sept 25, 2013   1298    blawrenc     Made fixes to prevent
                                      vtecTestRecords_local.json
                                      from being orphaned and 
                                      removed from localization.
 Feb 27, 2014    2826    dgilling     Remove code that handles VTEC records.
"""

import json, os, cPickle, time, fcntl, random, stat
import Logger as LogStream
from PathManager import PathManager
       
class DatabaseStorage:

    def __init__(self):
        # Build the path to the database files.
        # These files live in the same directory as this module.
        self._localizationPath = 'gfe/userPython/'
        self._dbPath = self.getUserPath("HazardServices")
        self._lastSeqNumFilePath = self._dbPath + ".lastSeqNum.json"
        
    def getUserPath(self, category):
        pathMgr = PathManager()
        lf = pathMgr.getLocalizationFile(str(self._localizationPath), 'CAVE_STATIC', 'USER')
        fullpath = lf.getPath()
        return fullpath + "/"
        
    def getData(self, criteria):        
        info = json.loads(criteria)
        dataType = info.get("dataType")

        if dataType == "alerts":
            return self.getAlertValues()

        elif dataType == 'events':
            return self.getEvents(dataType, info)
        
        elif dataType == "colorTable":
            format = info.get("format", "json")
            return self.getColorTable(format)

    def putData(self, criteria):
        info = json.loads(criteria)
        dataType = info.get('dataType')
        if dataType == "events":            
            self.writeEventDatabase(info.get("eventDicts"))
        if dataType in ["vtecRecords", 'testVtecRecords']:            
            self.writeVtecDatabase(dataType, info.get("vtecDicts"))

    def deleteData(self, criteria):
        info = json.loads(criteria)
        dataType = info.get('dataType')
        if dataType == "events":            
            self.deleteEventDatabase(info.get("eventDicts"))
        if dataType == "vtecRecords":            
            self.deleteVtecDatabase(info.get("vtecDicts"))
        
    def lockEventDatabase(self):
        self._eventsLockFD, self._eventsLockTime = self._lockDB(self._eventsFileName, self._eventsLockName, initialData={})
        
    def unlockEventDatabase(self):
        self._unlockDB(self._eventsFileName, self._eventsLockName, self._eventsLockFD, self._eventsLockTime)

#######################
####  HELPER METHODs
        
    def deleteVtecDatabase(self, vtecDicts):
        self.deleteLocalData(self._dbPath+"vtecRecords", ["oid","key", "etn"], vtecDicts, dataFormat="list")    
                             
    def reset(self, criteria):
        info = json.loads(criteria)
        dataType = info.get('dataType')
        if dataType == "events":
            self.resetEventDatabase()
            self.resetVtecRecords()
        else:            
            try: 
                cmd = "rm " + self._dbPath + dataType + "_local.json"
                os.system(cmd)
            except:
                pass
     
    def resetEventDatabase(self):
        eventDicts = []  
        self.writeEventDatabase(eventDicts)
             
    def getAlertValues(self):
        result = self.getLocalData(self._dbPath+"alerts", "Not used")
        return json.dumps(result)
    
    def getEvents(self, dataType, info):
        self.lockEventDatabase()
        # events is a dictionary of eventDicts with "eventID" as the key in the top level dictionary
        events = self.readEventDatabase()              
        if info.get("lock") != "True":
            self.unlockEventDatabase()
        # Leave database locked if user has requested read with lock
        ###Basic filter - filters for visibleTypes and visibleSites
        filter = info.get("filter")
        
        if filter is not None:
            self.filterEvents(events, "type", filter.get("visibleTypes"))
            self.filterEvents(events, "siteID", filter.get("visibleSites"))
            self.filterEvents(events, "state", filter.get("visibleStates"))
        
        return json.dumps(events)
    
    def filterEvents(self, events, field, values):
            toRemove = []
            if values is not None:
                for entry in events:
                    if events[entry].get(field) not in values:
                        toRemove.append(entry)
                for i in toRemove:
                    events.pop(i)                     

    def _lockDB(self, fileName, lockName, initialData=[]):

        if fileName is None:
            raise Exception, "lockAT without filename"

        t1 = time.time()
        lockFD = open(lockName, 'a+')
        try:
            os.chmod(lockName, 0664)
        except:
            pass

        opened = 1
        while opened:
           try:
              fcntl.lockf(lockFD.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
              fcntl.lockf(lockFD.fileno(), fcntl.LOCK_EX)
              opened = 0
           except IOError:
              time.sleep(random.random())
              opened = 1

        t2 = time.time()
        lockTime = t2

        # if file doesn't exist or zero length, create it (first time)
        if not os.path.exists(fileName) or \
          os.stat(fileName)[stat.ST_SIZE] == 0:
            LogStream.logDebug("creating new active table")
            pickleData = cPickle.dumps(initialData)
            fd = open(fileName, 'w+')
            try:
                os.chmod(fileName, 0664)
            except:
                pass
            fd.write(pickleData)
            fd.close()
        return lockFD, lockTime

    def _unlockDB(self, fileName, lockName, lockFD, lockTime):
        if fileName is None:
            raise Exception, "unlockAT without filename"

        fcntl.lockf(lockFD.fileno(), fcntl.LOCK_UN)
        t2 = time.time()
        lockFD.close()    
        
    def newSeqNum(self):
        try:             #Check if last sequence number file exists, then open and edit
            seqNumFile = open(self._lastSeqNumFilePath, 'r')
            seqNum = int(seqNumFile.read())
            seqNum += 1
            seqNumFile = open(self._lastSeqNumFilePath, 'w')
            seqNumFile.write(str(seqNum))
            seqNumFile.close()
            return str(seqNum)
        except:        #If sequence number file does not exist, create it
            # This is currently set to one more than the max eventID in events.json
            FIRST_AVAILABLE_EVENT_ID = '17'
            seqNumFile = open(self._lastSeqNumFilePath, 'w')
            seqNumFile.write(FIRST_AVAILABLE_EVENT_ID)
            seqNumFile.close()
            return FIRST_AVAILABLE_EVENT_ID
        
    def getLayerColorTable(self, model):
        colorTable_dir = self.readOnlyDbPath + "SnowAmtColorTable.xml"
        return open(colorTable_dir, "r")

    def getLocalData(self, filename, id=None, dataFormat="dictionary"): 
        """ 
        In the following methods:
        
        If dataFormat == "dictionary": "dataDicts" is a dictionary of dictionaries 
        and id is one key field e.g. settings with the key being settingsID OR events with the key being eventID
        
        If dataFormat == "list": "dataDicts" is a list of dictionaries and "id" is a list of fields
        which uniquely identify and entry e.g. a list of vtecRecord dictionaries with
        ["oid", "key", "etn"] uniquely identifying a vtecRecords.

        This method will first look for a local version of a json file e.g. settings_local.json,
        otherwise, will use the base version e.g. events.json
        """
        # Use local version if there 
        self.flush()
        dataDicts = self.readJsonFile(filename + "_local")
        
        if dataDicts is None:
           dataDicts = self.readJsonFile(filename)
        if dataDicts is not None:
            return dataDicts
        if dataFormat == "dictionary": return {}
        else: return []

    def putLocalData(self, filename, id, dataDicts, dataFormat="dictionary", replaceFile=False):
        """
            Read current dataDicts.  
            Write dataDicts to local file, replacing duplicates and adding ones keying off of id.
        """ 
        currentDicts = self.getLocalData(filename, id, dataFormat) # add a new parameter : dataFormat
        if dataFormat == "dictionary":
            for key in dataDicts:
                currentDicts[key] = dataDicts.get(key)
        if dataFormat == "list":
            for dict in dataDicts:
                # Replace matching record if it exists
                match = self.findMatch(dict, currentDicts, id)

                if match is not None:
                    currentDicts[match] = dict
                # Otherwise, add it
                else:
                    currentDicts.append(dict)
        self.writeJsonFile(filename + "_local", id, currentDicts, replaceFile)  
              
    def deleteLocalData(self, filename, id, dataDicts, dataFormat="dictionary"):
        """
        Read current dataDicts.  
        Write dataDicts to local file, removing those to be deleted.
        """
        currentDicts = self.getLocalData(filename, id)
        if dataFormat == "dictionary":
            for key in dataDicts:
                if currentDicts.get(key) == key:
                    currentDicts.pop(key)
        if dataFormat == "list":
            for dict in dataDicts:
                match = self.findMatch(dict, currentDicts, id)
                if match is not None:
                    currentDicts.pop(match)
        self.writeJsonFile(filename + "_local", id, currentDicts)   
                
    def findMatch(self, matchDict, dictList, ids):
        """
        Return the index of the dictionary in dictList that matches the given matchDict
        with respect to the list of fields given in ids. 
        Return None if no match.
        """
        if not ids: return None
        for i in range(len(dictList)): # get length of dictList
            dict = dictList[i] 
            found = True
            for id in ids:
                if not dict.get(id) == matchDict.get(id):
                    found = False
                    break
            if not found:
                continue
            return i
        return None
    
           
    def readJsonFile(self, filename):
        try:
            fd = open(filename+".json", 'rb')                
            data = json.loads(fd.read())
            fd.close()
        except:
            data = None
        return data
    
    def writeJsonFile(self, filename, id, dataDicts, replaceFile=False):
        if replaceFile:
            mode = "w"
        else:
            mode = "w+"

        pathMgr = PathManager()
        vtecFile = pathMgr.getLocalizationFile(str(filename + ".json"), 'CAVE_STATIC', 'USER')            
        fd = vtecFile.getFile(mode)
        fd.write(json.dumps(dataDicts, sort_keys=True, indent=4))
        fd.close()
        vtecFile.save()
          
    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()

