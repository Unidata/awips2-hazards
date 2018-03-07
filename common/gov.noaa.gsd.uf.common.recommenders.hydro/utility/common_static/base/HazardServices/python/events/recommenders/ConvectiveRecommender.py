'''
Ingests convective cell identification and attributes from automated source

November 2015 :: starting with ingesting ProbSevere data from 
http://cimss.ssec.wisc.edu/severe_conv/probsev.html

Assumes data feed from CIMSS via LDM into /awips2/edex/data/manual

probsevere data plugin for edex should be part of baseline and should ingest
to datastore

'''
import sys
import datetime
import EventFactory
import EventSetFactory
import GeometryFactory
import AdvancedGeometry
import RecommenderTemplate
import numpy
import JUtil
import time
import re
from EventSet import EventSet

import h5py
import numpy as np
import glob, os, time, datetime
import pprint
from collections import defaultdict, deque
from shapely.wkt import loads
from shapely.geometry import Polygon, Point

import TimeUtils, LogUtils
from ProbUtils import ProbUtils

from HazardConstants import *
import HazardDataAccess

from com.raytheon.uf.common.time import SimulatedTime
from edu.wisc.ssec.cimss.common.dataplugin.probsevere import ProbSevereRecord
from SwathRecommender import Recommender as SwathRecommender 
#
# The size of the buffer for default flood polygons.
DEFAULT_POLYGON_BUFFER = 0.05

#
# Keys to values in the attributes dictionary produced
# by the flood recommender.
OBJECT_ID = "objectids"
MISSING_VALUE = -9999
MILLIS_PER_SECOND = 1000

### FIXME
DEFAULT_DURATION_IN_SECS = 3600 # 60 minutes


sysTime=SimulatedTime.getSystemTime().getMillis()/1000
print "SYSTIME:", sysTime
if sysTime < 1478186880:
    SOURCEPATH='/awips2/edex/data/hdf5/probsevere'
else:
    SOURCEPATH = '/realtime-a2/hdf5/probsevere'

AUTOMATION_LEVELS = ['userOwned','attributesOnly','attributesAndGeometry','automated']

class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        """
        We'll see if anything needs to be initialized
        """
        
        description="""
            first line is always, "Valid: yyyymmdd_HHMMSS" (in UTC). Each line after than gives information for a unique storm.
            
            A colon separates each piece of info. 
            
            Column1: Type of object (string). Right now, it is always "RAD". It could potentially be "SAT" or "LTG" in the future.
            Column2: The probability. Integer from 0 - 100.
            Column3: String for MUCAPE info. May sometimes be "N/A"
            Column4: String for effective shear info. May sometimes be "N/A"
            Column5: String for MESH info, giving time and size. May sometimes be "N/A"
            Column6: String for satellite growth predictor1. Can be "N/A"
            Column7: String for satellite growth predictor2. Can be "N/A"
            Column8: Comma separated list of lats and lons. "Lat1, Lon1,...,Lat(n),Lon(n),Lat1,Lon1. Lat1 and Lon1 pair are at the end of the list, too, in order to close the polygon
            Column9: ObjectID (long)
            Column10: mean motion east, in m/s (float)
            Column11: mean motion south, in m/s (float)
        """
    
    def defineScriptMetadata(self):
        """
        @return: A dictionary containing information about this
                 tool
        """
        metaDict = {}
        metaDict["toolName"] = "ConvectiveRecommender"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Ingests convective cell identification and attributes from automated source"
        metaDict["eventState"] = "Pending"
        metaDict['includeEventTypes'] = [ "Prob_Severe", "Prob_Tornado" ]
        metaDict['includeDataLayerTimes'] = True
        metaDict["getDialogInfoNeeded"] = False
        metaDict["getSpatialInfoNeeded"] = False
        return metaDict

    def defineDialog(self, eventSet):
        """
        @return: A dialog definition to solicit user input before running tool
        """        
        return None

    def initialize(self, practice):
        self.probUtils = ProbUtils(practice=practice)
        lats = self.probUtils.lats
        ulLat = lats[0]
        lrLat = lats[-1]
        lons = self.probUtils.lons
        ulLon = lons[0]
        lrLon = lons[-1]
        self.domainPolygon = Polygon([(ulLon, ulLat), (lrLon, ulLat), (lrLon, lrLat), (ulLon, lrLat), (ulLon, ulLat)])
        self.lowThreshold = self.probUtils.lowThreshold
    

    def execute(self, eventSet, dialogInputMap, visualFeatures):
        """
        Runs the River Flood Recommender tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param visualFeatures:   A list of visual features provided
                                 by the defineSpatialInput() method;
                                 ignored for this recommender.
        
        @return: A list of potential events. 
        """

        import sys
        sys.stderr.write("Running convective recommender.\n    trigger:    " +
                         str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
                         str(eventSet.getAttribute("eventType")) + "\n    origin:     " + 
                         str(eventSet.getAttribute("origin")) + "\n    hazard ID:  " +
                         str(eventSet.getAttribute("eventIdentifiers")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")


        ### Make sure we are getting the latest PHIConfig info each time we run.
        caveMode = eventSet.getAttributes().get('hazardMode','PRACTICE').upper()
        practice = (False if caveMode == 'OPERATIONAL' else True)
        self.initialize(practice)

        sessionAttributes = eventSet.getAttributes()
        if sessionAttributes:
            sessionMap = JUtil.pyDictToJavaMap(sessionAttributes)
            
        st = time.time()

        currentEvents = self.getCurrentEvents(eventSet)        

        LogUtils.logMessage('Finnished ', 'getCurrentEvent',' Took Seconds', time.time()-st)
        st = time.time()
            
        self.currentTime = sessionAttributes["currentTime"]
        
        dlt = sessionAttributes.get("dataLayerTimes", [])
        
        self.dataLayerTime = sorted(dlt)[-1] if len(dlt) else self.currentTime
        self.latestDLTDT = datetime.datetime.fromtimestamp(self.dataLayerTime/1000)
        print '\n\n\t[CR] DATALAYERTIME:', self.latestDLTDT
        
        latestCurrentEventTime = self.getLatestTimestampOfCurrentEvents(eventSet, currentEvents)

        LogUtils.logMessage('Finnished ', 'getLatestTimestampOfCurrentEvents',' Took Seconds', time.time()-st)
        st = time.time()
        
        recommendedEventsDict = self.getRecommendedEventsDict(self.currentTime, latestCurrentEventTime)

        LogUtils.logMessage('Finnished ', 'getRecommendedEventsDict',' Took Seconds', time.time()-st)
        
        
        st = time.time()
        identifiersOfEventsToSaveToHistory, identifiersOfEventsToSaveToDatabase, mergedEventSet = self.mergeHazardEventsNew2(currentEvents, recommendedEventsDict)

        LogUtils.logMessage('Finnished ', 'mergeHazardEvent',' Took Seconds', time.time()-st)
        
        
        if len(mergedEventSet.events) > 0:
            st = time.time()
            swathRec = SwathRecommender()
            swathRec.execute(mergedEventSet, None, None)
            LogUtils.logMessage('Finnished ', 'swathRec.execute',' Took Seconds', time.time()-st)
        
        for e in mergedEventSet:
            print '[CR-1] )))) ', e.get('objectID'), e.getStatus()

        ### Ensure that any resulting events are saved to the history list or the database
        ### (the latter as latest versions of those events).  If one or both categories have
        ### no identifiers, set them to nothing in case the Swath Recommender set that
        ### attribute, as this recommender knows which ones should be saved in which category.
        if (identifiersOfEventsToSaveToHistory):
            mergedEventSet.addAttribute(SAVE_TO_HISTORY_KEY, identifiersOfEventsToSaveToHistory)
        else:
            mergedEventSet.addAttribute(SAVE_TO_HISTORY_KEY, None)

        mergedEventSet.addAttribute(SAVE_TO_DATABASE_KEY, True)
        mergedEventSet.addAttribute(TREAT_AS_ISSUANCE_KEY, True)
        mergedEventSet.addAttribute(KEEP_SAVED_TO_DATABASE_LOCKED_KEY, False)
        return mergedEventSet
    

    def getRecommendedEventsDict(self, currentTime, latestDatetime):
        hdfFilesList = self.getLatestProbSevereDataHDFFileList(latestDatetime=None)
        eventsDict = self.latestEventSetFromHDFFile(hdfFilesList, currentTime)
        return eventsDict

    def uvToSpdDir(self, eastMotions, southMotions):
        if eastMotions is None or southMotions is None:
            wdir = self.defaultWindDir()
            wspd = self.defaultWindSpeed() #kts
        else:
            ### Note: traditionally u, v calculations have v > 0 = Northward
            ### For some reason, ProbSevere has "southMotions" where
            ### > 0 = Southward.  Multiplying by -1. here to correct this.
            wspd, wdir = self.probUtils.UVToMagDir(float(eastMotions), -1.*float(southMotions))
            wspd = self.probUtils.convertMsecToKts(wspd)
                
        return {'wdir':int(round(wdir)), 'wspd':int(round(wspd))}



    def dumpHDFContents(self, hFile, latestN=1):
        contents = defaultdict(dict)
        def saveItems(name, obj):
            if isinstance(obj, h5py.Dataset):
                group, ds = name.split('/')
                groupDT = self.parseGroupName(group)
                contents[groupDT][ds] = obj.value.tolist()
                
        hFile.visititems(saveItems)
        
        ### Do we need to sort?
        
        return dict(contents)
                
    def makeValid(self, polyString):
        poly = loads(polyString)
        #newPoly = loads(polyString)
        tolerance = 0.01
        valid = False
        while not valid:
           newPoly = poly.simplify(tolerance)
           valid = newPoly.is_valid
           tolerance+=0.01
           if tolerance > 1:
               break
           
        return str(newPoly)
        
        

    def latestEventSetFromHDFFile(self, hdfFilenameList, currentTime):
        ### Should be a single file with latest timestamp
        hFile = None
        valuesDict = {}
        for hdfFilename in hdfFilenameList[:2]: # should grab at most 2, but should still pass if len(hdfFilenameList) == 1
            try:
                hFile = h5py.File(hdfFilename,'r')
            except:
                print 'Convective Recommender Warning: Unable to open', hdfFilename, ' in h5py. Skipping...'
                if len(hdfFilenameList) == 1:
                    return

            valuesDict.update(self.dumpHDFContents(hFile))
            hFile.close()
        
        ### BUG ALERT :: Rounding to zero in datetime comparison
        groupDTList = [t for t in sorted(valuesDict.keys()) if t.replace(second=0) <= self.latestDLTDT.replace(second=0)]
        
        ### No ProbSevere objects older than Latest Data Layer
        if len(groupDTList) == 0:
            return {}

        latestGroupDT = max(groupDTList)# if len(groupDTList) else self.latestDLTDT
        latestGroup = valuesDict.get(latestGroupDT)

        returnDict = {}
        for i in range(len(latestGroup.values()[0])):
            row = {k:v[i] for k,v in latestGroup.iteritems()}

            thisPolyString = row.get('polygons')
            if thisPolyString is None:
                continue
            elif not loads(thisPolyString).centroid.within(self.domainPolygon):
                continue
            elif not loads(thisPolyString).is_valid:
                print 'CR: Invalid polygon, Attempting to validate...', row.get('objectids')
                validPoly = self.makeValid(thisPolyString)
                if loads(validPoly).is_valid:
                    row['polygons'] = validPoly
                else:
                    print '\tSTILL INVALID, SKIPPING'
                    continue
            
           
            if row.get('probabilities') < self.lowThreshold:
                row['belowThreshold'] = True
            else:
                row['belowThreshold'] = False
            
            ### Current CONVECTPROB feed has objectids like "653830; Flash Rate 0 fl/min"
            objIds = row.get('objectids')
            if ';' in objIds:
                row['objectids'] =  objIds.split(';')[0]
            
            
            row['startTime'] = latestGroupDT
            vectorDict = self.uvToSpdDir(row.get('eastMotions'),row.get('southMotions'))
            row['wdir'] = vectorDict.get('wdir')
            row['wspd'] = vectorDict.get('wspd')
            
            ### Needed for now - refactor
            returnDict[row['objectids']] = row

        return returnDict
        
                           
                    
    def parseGroupName(self, rootName):
        name,dtString = rootName.split('::')
        dt = datetime.datetime.strptime(dtString,"%Y-%m-%d_%H:%M:%S.0")
        return dt

    
    def getLatestProbSevereDataHDFFileList(self, latestDatetime=None):
        fileList = None
        try:
            fileList = sorted(glob.glob(os.path.join(SOURCEPATH,'*.h5')), reverse=True)
        except:
            print 'Convective Recommender Warning: Could not obtain list of probsevere*.h5 files at:', os.path.join(SOURCEPATH,'*.h5')
            print 'Returning:', fileList
            return fileList
        
        
        if latestDatetime:
            ### Use filename to make datetime and return ONLY the latest
            regex = "probsevere-%Y-%m-%d-%H.h5"
                
            fileDict = {datetime.datetime.strptime(os.path.basename(x),regex):x for x in fileList}
            
            # see https://bytes.com/topic/python/answers/765154-find-nearest-time-datetime-list
            returnFileList = [fileDict.get(min(fileDict.keys(), key=lambda date : abs(latestDatetime-date)))]
            
            return returnFileList
        else:
            return fileList
                
    
    def toString(self):
        return "ConvectiveRecommender"
    

    def getLatestTimestampOfCurrentEvents(self, eventSet, currentEvents):
        ### Initialize latestDatetime
        latestDatetime = datetime.datetime.min

        for event in currentEvents:
            eventCreationTime = event.getCreationTime()
            if eventCreationTime > latestDatetime:
               latestDatetime =  eventCreationTime

        return latestDatetime

    
    def getCurrentEvents(self, eventSet):
        siteID = eventSet.getAttributes().get('siteID')        

        caveMode = eventSet.getAttributes().get('hazardMode','PRACTICE').upper()
        practice = True
        if caveMode == 'OPERATIONAL':
            practice = False
        # Get current events from Session Manager (could include pending / potential)
        currentEvents = [event for event in eventSet]
        eventIDs = [event.getEventID() for event in eventSet]

        # Add in those from the Database
        databaseEvents = HazardDataAccess.getHazardEventsBySite(siteID, practice) 
        for event in databaseEvents:
            if event.getEventID() not in eventIDs:
                currentEvents.append(event)
                eventIDs.append(event.getEventID())
        return currentEvents


    def makeHazardEvent(self, ID, values):
        
        if values.get('belowThreshold'):
            print '\t', ID, 'Below Threshold', self.lowThreshold, 'returning None'
            return None

        try:        
            polygon = loads(values.pop('polygons'))
        except:
            print 'POLYPOLYPOLY  Unable to load polygons. Returning None.  POLYPOLYPOLY'
            return None
        
        sys.stdout.flush()
        probSevereTime = values.get('startTime', self.dataLayerTime)
        hazardEvent = EventFactory.createEvent()
        hazardEvent.setCreationTime(probSevereTime)
        self.setEventTimes(hazardEvent, values)
        
        hazardEvent.setHazardStatus("pending")
        hazardEvent.setHazardMode("O")        
        
        hazardEvent.setPhenomenon("Prob_Severe")
        
        hazardEvent.setGeometry(polygon)
        hazardEvent.set('convectiveObjectDir', values.get('wdir'))
        hazardEvent.set('convectiveObjectSpdKts', values.get('wspd'))
        hazardEvent.set('probSeverAttrs',values)
        hazardEvent.set('objectID', ID)
        hazardEvent.set('visibleGeometry', 'highResolutionGeometryIsVisible')
        hazardEvent.setStatus('ISSUED')
        hazardEvent.set('statusForHiddenField', 'ISSUED')
        
        hazardEvent.set('manuallyCreated', False)
        hazardEvent.set('geometryAutomated', True)
        hazardEvent.set('motionAutomated', True)
        hazardEvent.set('durationAutomated', True)
        hazardEvent.set('probTrendAutomated', True)
        
        graphProbs = self.probUtils.getGraphProbs(hazardEvent, int(probSevereTime.strftime('%s'))*1000)
        hazardEvent.set('convectiveProbTrendGraph', graphProbs)
        
        return hazardEvent


    def setEventTimes(self, event, values=None):
        
        if event.getEndTime() is None or event.get('durationAutomated'):
            durSecs = DEFAULT_DURATION_IN_SECS
        else:
            durSecs = self.probUtils.getDurationSecs(event)
        
        if values is not None:
            psStartTime = values.get('startTime', self.dataLayerTime)
            event.set('probSevereStartTime', TimeUtils.datetimeToEpochTimeMillis(psStartTime))
            psEndTime = psStartTime + datetime.timedelta(seconds=durSecs)
            event.set('probSevereEndTime', TimeUtils.datetimeToEpochTimeMillis(psEndTime)) 
        
        ### Per request from Greg
        event.setStartTime(self.latestDLTDT)
        endTime = event.getStartTime() + datetime.timedelta(seconds=durSecs)
        event.setEndTime(endTime)
        

    def storeLastEvent(self, event):
        checkList = ['convectiveObjectSpdKtsUnc', 'convectiveObjectDirUnc', 'convectiveProbTrendGraph',
                            'convectiveObjectDir', 'convectiveObjectSpdKts']
        
        for c in checkList:
            event.set('convRecPast'+c, event.get(c))


    def storeNextGeometry(self, event, recommendedDict):
        geomList = deque(event.get('probSevereGeomList', []), maxlen=5)
        
        st = long(TimeUtils.datetimeToEpochTimeMillis(self.latestDLTDT))
        geom = AdvancedGeometry.createShapelyWrapper(loads(recommendedDict.get('polygons')), 0)
        
        ### Only want to store if new startTime 
        times = sorted([t[1] for t in geomList])
        if len(times) == 0:
            geomList.append((geom, st))
        elif st > times[-1]:
            geomList.append((geom, st))
        
        event.set('probSevereGeomList', list(geomList))
        
        
    def updateEvent(self, event, recommended, dataLayerTimeMS):
        
        # Store last update
        self.storeLastEvent(event)
        self.storeNextGeometry(event, recommended)
        
        if event.get('motionAutomated', False):
            pastProbSeverePolys = event.get('probSevereGeomList', [])
            if len(pastProbSeverePolys) > 1:
                self.probUtils.updateMotionVector(event, pastProbSeverePolys)
            else:
                event.set('convectiveObjectDir', recommended.get('wdir'))
                event.set('convectiveObjectSpdKts', recommended.get('wspd'))
                
            ### Requested by Greg (HWT 2018) to restrict uncertainty for automated events
            event.set('convectiveObjectDirUnc', 12)
            event.set('convectiveObjectSpdKtsUnc', 4)

            
        if event.get('probTrendAutomated', False):
            ### BUG ALERT: do we want DataLayerTime or ProbSevereTime?
            graphProbs = self.probUtils.getGraphProbs(event, dataLayerTimeMS)
            event.set('convectiveProbTrendGraph', graphProbs)
            
        self.updateEventGeometry(event, recommended)
            
        probSevereAttrs = event.get('probSeverAttrs')
        for k,v in recommended.iteritems():
            probSevereAttrs[k] = v
            

        event.set('probSeverAttrs',probSevereAttrs)
        
        ### BUG ALERT: Do we want to set the 
        self.setEventTimes(event)
        #event.setStartTime(self.latestDLTDT)
        #endTime = event.getStartTime() + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
        #event.setEndTime(endTime)


    def updateEventGeometry(self, event, recommendedDict):
        
        if event.get('geometryAutomated', False):
            try:
                newShape = loads(recommendedDict.get('polygons'))
                event.setGeometry(newShape)
            except:
                print 'ConvectiveRecommender: WHAT\'S WRONG WITH THIS POLYGON?', currID, type(recommended.get('polygons')), recommended.get('polygons')
                sys.stdout.flush()
        ### We want to get the nearest location of forecast (relocated inner) poly nearest the given time and use that
        else:
            polyGeom = None
            timeMin = sys.maxint
            recommendedStartTimeDT = recommendedDict.get('startTime')
            recommendedEpochMillis = TimeUtils.datetimeToEpochTimeMillis(recommendedStartTimeDT)
            latestDLTMillis = TimeUtils.datetimeToEpochTimeMillis(self.latestDLTDT)
            features = event.getVisualFeatures()
            for feature in features:
                featureIdentifier = feature.get('identifier')
                if featureIdentifier.startswith("swathRec_relocated_"):
                    geomDict = feature.get('geometry')
                    if geomDict is None:
                        continue
                    #"geometry": {(polySt_ms, polyEt_ms): relocatedShape}
                    polySt_ms = int(geomDict.keys()[0][0])
                    polyShape = geomDict.values()[0]
                        
                    timeDiff = np.abs(recommendedEpochMillis-polySt_ms)
                    
                    if timeDiff < timeMin:
                        timeMin = timeDiff
                        polyGeom = polyShape

            if polyGeom is not None:
                event.setGeometry(polyGeom)
                    

      
    ### Update the current events, and return a list of identifiers of
    ### events that are to be saved to the database.  
    def updateCurrentEvents(self, intersectionDict, mergedEvents):
        dataLayerTimeMS = int(self.dataLayerTime )
        
        identifiersOfEventsToSaveToDatabase = []
        
        for ID, vals in intersectionDict.iteritems():
            currentEvent = vals['currentEvent']

            #print '\n!!!!!!!  ID[1]: ', ID, '>>>>', currentEvent.getStatus()
            if currentEvent.getStatus() == 'ELAPSED':
                continue
            
            recommendedAttrs = vals['recommendedAttrs']
            
            self.updateEvent(currentEvent, recommendedAttrs, dataLayerTimeMS)
            
            
            identifiersOfEventsToSaveToDatabase.append(currentEvent.getEventID())
            mergedEvents.add(currentEvent)
            
        return identifiersOfEventsToSaveToDatabase
            
    
    def doubleCheckEnded(self, currentEventsList, recommendedEventsDict):
        recoverAutomatedList = []
        for evt in currentEventsList:
            if evt.getStatus() in ['ENDED', 'ENDING', 'ELAPSED']:
                objectID = evt.get('objectID')
                if objectID in recommendedEventsDict.keys():
                   recoverAutomatedList.append(int(objectID))
        return recoverAutomatedList
    

    # Create an event set of new hazard events to be merged, together with
    # existing events that are to be elapsed. Return a tuple of three elements,
    # the first being a list of identifiers of events that are to be saved to
    # history, the second being a list of identifiers of events that are to be
    # saved to the database as latest versions, and the third being the event
    # set itself.
    def mergeHazardEventsNew2(self, currentEventsList, recommendedEventsDict):
        intersectionDict = {}
        recommendedObjectIDsList = sorted(recommendedEventsDict.keys())
        unmatchedEvents = []
        manualEventGeomsList = []
        mergedEvents = EventSet(None)

        currentTimeMS = self.currentTime
        mergedEvents.addAttribute('currentTime', currentTimeMS)
        mergedEvents.addAttribute('trigger', 'autoUpdate')
        
        identifiersOfEventsToSaveToDatabase = []
        
        for currentEvent in currentEventsList:
            
            currentEventObjectID = currentEvent.get('objectID')
            ### If current event has match in rec event, add to dict for later processing
            ### should avoid 'userOwned' since they are filtered out with previous if statement

            rawRecommendedID = re.findall('\d+', str(currentEventObjectID))[0]

            
            #if currentEvent.get('automationLevel') == 'userOwned':
            if not currentEvent.get('geometryAutomated') and not currentEvent.get('motionAutomated') and not currentEvent.get('probTrendAutomated'):
                evtGeom = currentEvent.getGeometry().asShapely()
                manualEventGeomsList.append({'ID':currentEvent.get('objectID'), 'hazType':currentEvent.getHazardType(), 'geom':evtGeom})
                continue
            
            ### Set below threshold to Proposed to hide only for fully automated events
            if currentEvent.get('probSeverAttrs').get('probabilities') < self.lowThreshold and currentEvent.get('geometryAutomated') and currentEvent.get('motionAutomated') and currentEvent.get('probTrendAutomated'):
                currentEvent.setStatus('PROPOSED')
                sys.stderr.write('\n\t' + currentEvent.get('objectID') + ' is below threshold ['+ str(self.lowThreshold) + '].Skipping...\n')
                sys.stderr.flush()
                mergedEvents.add(currentEvent)
                continue
            else:
                if currentEvent.getStatus() == 'PROPOSED':
                    sys.stderr.write('\n\t' + currentEvent.get('objectID') + ' is above threshold again ['+ str(self.lowThreshold) + ']. Resurrecting...\n')
                    sys.stderr.flush()
                    currentEvent.setStatus('ISSUED')
            

            #if currentEventObjectID in recommendedObjectIDsList:
            ### Account for prepended 'M' to automated events that are level 3 or 2 automation.
            if str(currentEventObjectID).endswith(tuple([str(z) for z in recommendedObjectIDsList])):
                intersectionDict[currentEventObjectID] = {'currentEvent': currentEvent, 'recommendedAttrs': recommendedEventsDict[rawRecommendedID]}
                
                ### Remove ID from rec list so remaining list is "newOnly"
                recommendedObjectIDsList.remove(rawRecommendedID)
                mergedEvents.add(currentEvent)
                
            else:
                if currentEvent.getStatus() != 'ELAPSED':
                    currentEvent.setStatus('ELAPSED')
                    currentEvent.set('statusForHiddenField', 'ELAPSED')
                    mergedEvents.add(currentEvent)
                    identifiersOfEventsToSaveToDatabase.append(currentEvent.getEventID())

        ### Update the current events with the attributes of the recommended events.
        ### This returns a list of identifiers of events that are to be saved to the
        ### database (not history list).  
        #identifiersOfEventsToSaveToDatabase = self.updateCurrentEvents(intersectionDict, mergedEvents, currentTime)
        identifiersOfEventsToSaveToDatabase.extend(self.updateCurrentEvents(intersectionDict, mergedEvents))
        
        # Create a list of hazard event identifiers that are to be saved
        # to the history list.
        identifiersOfEventsToSaveToHistory = []
        
        recoverAutomatedList = self.doubleCheckEnded(currentEventsList, recommendedEventsDict)
        makeNewObjectsIDList = recommendedObjectIDsList+recoverAutomatedList
        
        ### Loop through remaining/unmatched recommendedEvents
        ### if recommended geometry overlaps an existing *manual* geometry
        ### ignore it. 
        for recID in recommendedObjectIDsList:
            recommendedValues = recommendedEventsDict[recID]
            recommendedEvent = None

            if len(manualEventGeomsList) == 0:
                
                ### If an event is created, add it to the event set and add
                ### it to the list of events to be saved to history.
                recommendedEvent = self.makeHazardEvent(recID, recommendedValues)
                
            else:
                ### Get recommended geometry
                recGeom = loads(recommendedValues.get('polygons'))
                makeNew = True
                for eventDict in manualEventGeomsList:
                    ### if recommended geometry intersects with Prob_Severe geometry, don't make new Haz Evt
                    hazType = eventDict.get('hazType', None)
                    evtGeom = eventDict.get('geom')
                    if hazType == 'Prob_Severe' and evtGeom.intersects(recGeom):
                        makeNew = False
                if makeNew:
                    recommendedEvent = self.makeHazardEvent(recID, recommendedValues)

            if recommendedEvent:
                mergedEvents.add(recommendedEvent)
                identifiersOfEventsToSaveToHistory.append(recommendedEvent.getEventID())

        #for e in mergedEvents:
        #    print '[CR-2] %%%%%:', e.get('objectID'), '(', e.getEventID(), ')', e.getStatus()
                    
        return identifiersOfEventsToSaveToHistory, identifiersOfEventsToSaveToDatabase, mergedEvents
    
        
    def isEditableSelected(self, event):
        selected = event.get('selected', False)
        if selected == 0: selected = False  
        return selected and event.get('activate', False), selected        

                                
    def flush(self):
        import os
        os.sys.__stdout__.flush()
        
        
    #########################################
    ### OVERRIDES
    
    def defaultWindSpeed(self):
        return 32
    
    def defaultWindDir(self):
        return 270

