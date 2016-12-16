'''
Ingests convective cell identification and attributes from automated source

November 2015 :: starting with ingesting ProbSevere data from 
http://cimss.ssec.wisc.edu/severe_conv/probsev.html

Assumes data feed from CIMSS via LDM into /awips2/edex/data/manual

convectprob data plugin for edex should be part of baseline and should ingest
to datastore

'''
import sys
import datetime
import EventFactory
import EventSetFactory
import GeometryFactory
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
from collections import defaultdict
from shapely.wkt import loads
from shapely.geometry import Polygon, Point

import TimeUtils, LogUtils
from ProbUtils import ProbUtils

from HazardConstants import *
import HazardDataAccess

from com.raytheon.uf.common.time import SimulatedTime
from edu.wisc.ssec.cimss.common.dataplugin.convectprob import ConvectProbRecord
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
DEFAULT_DURATION_IN_SECS = 2700 # 45 minutes
#DEFAULT_DURATION_IN_SECS = 120
PROBABILITY_FILTER = 8 # filter our any objects less than this.
SOURCEPATH_ARCHIVE = '/awips2/edex/data/hdf5/convectprob'
SOURCEPATH_REALTIME = '/realtime-a2/hdf5/probsevere'
    
AUTOMATION_LEVELS = ['userOwned','attributesOnly','attributesAndMechanics','automated']

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
        metaDict['background'] = True
        metaDict['includeDataLayerTimes'] = True
        return metaDict

    def defineDialog(self, eventSet):
        """
        @return: A dialog definition to solicit user input before running tool
        """        
        return None

    def initialize(self):
        self.probUtils = ProbUtils()
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
                         str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
        self.initialize()
        sessionAttributes = eventSet.getAttributes()
        if sessionAttributes:
            sessionMap = JUtil.pyDictToJavaMap(sessionAttributes)
            
        st = time.time()

        currentEvents = self.getCurrentEvents(eventSet)        

        LogUtils.logMessage('Finnished ', 'getCurrentEvent',' Took Seconds', time.time()-st)
        st = time.time()
            
        self.currentTime = datetime.datetime.utcfromtimestamp(long(sessionAttributes["currentTime"])/1000)
        
        dlt = sessionAttributes.get("dataLayerTimes", [])
        
        self.dataLayerTime = sorted(dlt)[-1] if len(dlt) else self.currentTime
        
        latestCurrentEventTime = self.getLatestTimestampOfCurrentEvents(eventSet, currentEvents)

        LogUtils.logMessage('Finnished ', 'getLatestTimestampOfCurrentEvents',' Took Seconds', time.time()-st)
        st = time.time()
        
        recommendedEventsDict = self.getRecommendedEventsDict(self.currentTime, latestCurrentEventTime)

        LogUtils.logMessage('Finnished ', 'getRecommendedEventsDict',' Took Seconds', time.time()-st)
        st = time.time()
        recommendedEventsDict = self.filterForUserOwned(currentEvents, recommendedEventsDict)
        LogUtils.logMessage('Finnished ', 'filterForUserOwne',' Took Seconds', time.time()-st)
        st = time.time()
        mergedEventSet = self.mergeHazardEventsNew(currentEvents, recommendedEventsDict)

        LogUtils.logMessage('Finnished ', 'mergeHazardEvent',' Took Seconds', time.time()-st)
        
        
        if len(mergedEventSet.events) > 0:
            st = time.time()
            swathRec = SwathRecommender()
            mergedEventSet = swathRec.execute(mergedEventSet, None, None)
            LogUtils.logMessage('Finnished ', 'swathRec.execute',' Took Seconds', time.time()-st)
        
        # Ensure that any resulting events are saved to the database.
        mergedEventSet.addAttribute("saveToDatabase", True)
        
        return mergedEventSet
    

    def getRecommendedEventsDict(self, currentTime, latestDatetime):
        hdfFilesList = self.getLatestProbSevereDataHDFFileList(currentTime)
        #eventsDict = self.eventSetFromHDFFile(hdfFilesList, currentTime, latestDatetime)
        eventsDict = self.latestEventSetFromHDFFile(hdfFilesList, currentTime)
        return eventsDict

    def uvToSpdDir(self, motionEasts, motionSouths):
        if motionEasts is None or motionSouths is None:
            wdir = self.defaultWindDir()
            wspd = self.defaultWindSpeed() #kts
        else:
            u = float(motionEasts)
            v = -1.*float(motionSouths)
            wspd = int(round(math.sqrt(u**2 + v**2) * 1.94384)) # to knots
            wdir = int(round(math.degrees(math.atan2(-u, -v))))
            if wdir < 0:
                wdir += 360
                
        return {'wdir':wdir, 'wspd':wspd}


    def latestEventSetFromHDFFile(self, hdfFilenameList, currentTime):
        ### Should be a single file with latest timestamp
        hFile = None
        try:
            hFile = h5py.File(hdfFilenameList[0],'r')
        except:
            print 'Convective Recommender Warning: Unable to open', hdfFilenameList[0], ' in h5py. Skipping...'
            return
        
        valuesDict = {self.parseGroupName(group.name):group for group in hFile.values()}
        latestGroupDT = min(valuesDict.keys(), key=lambda date : abs(currentTime-date))
        latestGroup = valuesDict.get(latestGroupDT)

        returnDict = {}
        for i in range(latestGroup.values()[0].len()):
            row = {k:v[i] for k,v in latestGroup.iteritems()}

            thisPoly = row.get('polygons')
            if thisPoly is None:
                continue
            elif not loads(thisPoly).centroid.within(self.domainPolygon):
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
            vectorDict = self.uvToSpdDir(row.get('motionEasts'),row.get('motionSouths'))
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
            #===================================================================
            # fileList = sorted(glob.glob(os.path.join(SOURCEPATH,'*.h5')), 
            #               reverse=True)
            #===================================================================
            fileList = sorted(glob.glob(os.path.join(SOURCEPATH_REALTIME,'*.h5')), reverse=True) + \
                        sorted(glob.glob(os.path.join(SOURCEPATH_ARCHIVE,'*.h5')), reverse=True)
        except:
            print 'Convective Recommender Warning: Could not obtain list of convectprob*.h5 files at:', os.path.join(SOURCEPATH,'*.h5')
            print 'Returning:', fileList
            return fileList
        
        
        
        
        if latestDatetime:
            ### Use filename to make datetime and return ONLY the latest
            regex = "convectprob-%Y-%m-%d-%H.h5"
            if fileList:
                if fileList[0].startswith('probsevere'):
                    regex = "probsevere-%Y-%m-%d-%H.h5"
                
            fileDict = {datetime.datetime.strptime(os.path.basename(x),regex):x for x in fileList}
            
            # see https://bytes.com/topic/python/answers/765154-find-nearest-time-datetime-list
            returnFileList = [fileDict.get(min(fileDict.keys(), key=lambda date : abs(latestDatetime-date)))]
            
            ### Use filename to make datetime
            #returnFileList = [x for x in fileList if 
            #                  datetime.datetime.strptime(os.path.basename(x),
            #                  "convectprob-%Y-%m-%d-%H.h5")
            #                  > latestDatetime
            #                  ]
            
            ### Use file's modification time to make datetime
            #returnFileList = [x for x in fileList if 
            #                  datetime.datetime.utcfromtimestamp(os.path.getmtime(x))
            #                  > latestDatetime
            #                  ]
            
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

        #=======================================================================
        # for event in eventSet:
        #     eventCreationTime = event.getCreationTime()
        #     if eventCreationTime > latestDatetime:
        #        latestDatetime =  eventCreationTime
        #=======================================================================
               
        return latestDatetime
    
    def getCurrentEvents(self, eventSet):
        siteID = eventSet.getAttributes().get('siteID')        
        mode = eventSet.getAttributes().get('hazardMode', 'PRACTICE').upper()
        # Get current events from Session Manager (could include pending / potential)
        currentEvents = [event for event in eventSet]
        eventIDs = [event.getEventID() for event in eventSet]

        # Add in those from the Database
        databaseEvents = HazardDataAccess.getHazardEventsBySite(siteID, mode) 
        for event in databaseEvents:
            if event.getEventID() not in eventIDs:
                currentEvents.append(event)
                eventIDs.append(event.getEventID())
        return currentEvents

    def makeHazardEvent(self, ID, values):
        
        if values.get('belowThreshold'):
            print '\tBelow Threshold, returning None'
            return None
        
        sys.stdout.flush()
        probSevereTime = values.get('startTime', self.dataLayerTime)
        #dataLayerTimeMS = int(currentTime.strftime('%s'))*1000
        hazardEvent = EventFactory.createEvent()
        hazardEvent.setCreationTime(probSevereTime)
        self.setEventTimes(hazardEvent, values)
        
        hazardEvent.setHazardStatus("pending")
        hazardEvent.setHazardMode("O")        
        
        hazardEvent.setPhenomenon("Prob_Severe")
        
        polygon = loads(values.pop('polygons'))
        hazardEvent.setGeometry(polygon)
        hazardEvent.set('convectiveObjectDir', values.get('wdir'))
        hazardEvent.set('convectiveObjectSpdKts', values.get('wspd'))
        hazardEvent.set('probSeverAttrs',values)
        hazardEvent.set('objectID', ID)
        
        hazardEvent.set('automationLevel', 'automated')
        
        graphProbs = self.probUtils.getGraphProbs(hazardEvent, int(probSevereTime.strftime('%s'))*1000)
        hazardEvent.set('convectiveProbTrendGraph', graphProbs)
        
        return hazardEvent

    def setEventTimes(self, event, values):
        psStartTime = values.get('startTime', self.dataLayerTime)
        event.set('probSevereStartTime', TimeUtils.datetimeToEpochTimeMillis(psStartTime))
        psEndTime = psStartTime + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
        event.set('probSevereEndTime', TimeUtils.datetimeToEpochTimeMillis(psEndTime)) 
        
        #  Set the start / end times of the new event
        #     (Kind of klunky due to the methods we have for rounding)
        #  We set the event start time to the probSevereStartTime and then round it
        #  Similarly for the end time. 
        event.setStartTime(psStartTime)
        startTime = TimeUtils.roundDatetime(event.getStartTime()) 
        event.setStartTime(startTime)
        
        endTime = startTime + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
        event.setEndTime(endTime)
        endTime = TimeUtils.roundDatetime(event.getEndTime())                
        event.setEndTime(endTime)
        
#         startTime = TimeUtils.roundDatetime(currentTime) 
#         endTime = startTime + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
#         event.setStartTime(startTime)
#         event.setEndTime(endTime)


    def updateEventGeometry(self, event, recommendedDict):
        try:
            newShape = loads(recommendedDict.get('polygons'))
            event.setGeometry(newShape)
        except:
            print 'ConvectiveRecommender: WHAT\'S WRONG WITH THIS POLYGON?', currID, type(recommended.get('polygons')), recommended.get('polygons')
            sys.stdout.flush()
        


    def updateUserOwned(self, event, recommended, dataLayerTimeMS):
        pass


    def updateAttributesAndMechanics(self, event, recommended, dataLayerTime):
        self.updateAttributesOnly(event, recommended, dataLayerTime)
        self.updateEventGeometry(event, recommended)


    def updateAttributesOnly(self, event, recommended, probSevereTime):
        manualAttrs = event.get('manualAttributes', [])
        ##################################################################
        ##  Need to handle special cases in computing updatedAttrs
        ##################################################################
        updatedAttrs = {k:v for k,v in recommended.iteritems() if k not in manualAttrs}
        probSevereAttrs = event.get('probSeverAttrs')
        
        #print '\n=================='
        #print 'CR - ManualAttrs:', manualAttrs
        #print 'CR - UpdatedAttrs:', updatedAttrs
        #print 'CR - probSevereAttrs1:', probSevereAttrs
        
        for k,v in updatedAttrs.iteritems():
                probSevereAttrs[k] = v

        #print 'CR - probSevereAttrs2:', probSevereAttrs
        #print '*******'
        
        event.set('probSeverAttrs',probSevereAttrs)
        
        
        ### Special Cases
        if 'convectiveObjectDir' not in manualAttrs:
            event.set('convectiveObjectDir', recommended.get('wdir'))
            
        if 'convectiveObjectSpdKts' not in manualAttrs:
            event.set('convectiveObjectSpdKts', recommended.get('wspd'))
            
        if 'convectiveProbTrendGraph' not in manualAttrs:
            graphProbs = self.probUtils.getGraphProbs(event, probSevereTime)
            event.set('convectiveProbTrendGraph', graphProbs)
            

    def updateAutomated(self, event, recommended, probSevereTime):
        self.updateEventGeometry(event, recommended)
            
        event.set('convectiveObjectDir', recommended.get('wdir'))
        event.set('convectiveObjectSpdKts', recommended.get('wspd'))
        event.set('probSeverAttrs',recommended)
        graphProbs = self.probUtils.getGraphProbs(event, probSevereTime)
        event.set('convectiveProbTrendGraph', graphProbs)
        
    def updateCurrentEvents(self, intersectionDict, mergedEvents):
        dataLayerTimeMS = int(self.dataLayerTime )
        
        for ID, vals in intersectionDict.iteritems():
            currentEvent = vals['currentEvent']

            if currentEvent.get('editableObject'):
                print 'Event:',  currentEvent.get('objectID'), 'is being actively edited.  NO UPDATE IN CONVECTIVE RECOMMENDER!!'
                mergedEvents.add(currentEvent)  
                continue
            
            recommendedAttrs = vals['recommendedAttrs']
            automationLevel = currentEvent.get('automationLevel')  ### TODO: determine default value
            
            if automationLevel == 'attributesOnly':
                self.updateAttributesOnly(currentEvent, recommendedAttrs, dataLayerTimeMS)
            
            if automationLevel == 'attributesAndMechanics':
                self.updateAttributesAndMechanics(currentEvent, recommendedAttrs, dataLayerTimeMS)
            
            if automationLevel == 'automated':
                self.updateAutomated(currentEvent, recommendedAttrs, dataLayerTimeMS)
            
            if automationLevel == 'userOwned':
                self.updateUserOwned(currentEvent, recommendedAttrs, dataLayerTimeMS)
                
            mergedEvents.add(currentEvent)  
            
            

    
    def mergeHazardEventsNew(self, currentEventsList, recommendedEventsDict):
        intersectionDict = {}
        recommendedObjectIDsList = sorted(recommendedEventsDict.keys())
        currentOnlyList = []
        mergedEvents = EventSet(None)

        currentTimeMS = int(self.currentTime.strftime('%s'))*1000
        mergedEvents.addAttribute('currentTime', currentTimeMS)
        mergedEvents.addAttribute('trigger', 'autoUpdate')
        
        
        ### First, find the overlap between currentEvents and recommended events
        for currentEvent in currentEventsList:
            currentEventObjectID = currentEvent.get('objectID')
            if currentEventObjectID in recommendedObjectIDsList:
                ### If current event has match in rec event, add to dict for later processing
                intersectionDict[currentEventObjectID] = {'currentEvent': currentEvent, 'recommendedAttrs': recommendedEventsDict[currentEventObjectID]}
                
                ### Remove ID from rec list so remaining list is "newOnly"
                recommendedObjectIDsList.remove(currentEventObjectID)
            else:
                currentOnlyList.append(currentEvent)
                

        
        ### Update the current events with the attributes of the recommended events
        #self.updateCurrentEvents(intersectionDict, mergedEvents, currentTime)
        self.updateCurrentEvents(intersectionDict, mergedEvents)
        
        
        ### Loop through remaining/unmatched recommendedEvents
        ### if recommended geometry overlaps an existing *manual* geometry
        ### ignore it. 
        for recID in recommendedObjectIDsList:
            recommendedValues = recommendedEventsDict[recID]
            ### Get recommended geometry
            recGeom = loads(recommendedValues.get('polygons'))
            

            if len(currentOnlyList) == 0:
                recommendedEvent = self.makeHazardEvent(recID, recommendedValues)
                if recommendedEvent:
                    mergedEvents.add(recommendedEvent)
                
            else:
                ### The only events left in this list should be
                ###  1) those that are full manual
                ###  2) formerly automated at some level but no longer have
                ###     a corresponding ProbSevere ID and should be "removed".
                for event in currentOnlyList:
                    if 'userOwned' not in event.get('automationLevel'):
                        event.setStatus('ELAPSED')
                        ### userOwned events get precedent over automated
                    else:
                        ### Add the userOwned geometry to the EventSet
                        evtGeom = event.getGeometry().asShapely()
                        ### if the geometries DO NOT intersect, add recommended
                        if not evtGeom.intersects(recGeom):
                            recommendedEvent = self.makeHazardEvent(recID, recommendedValues)
                            if recommendedEvent:
                                mergedEvents.add(recommendedEvent)
    
                    mergedEvents.add(event)
                    
        return mergedEvents
        
            
    def filterForUserOwned(self, currentEvents, recommendedEventsDict):
        newDict = {}
        # Check the object ID match or polygon overlap of each recommended event values 
        #    with any "userOwned" currentEvent
        #  If there is an overlap, throw it out. 
        for ID, recommendedEventValues in recommendedEventsDict.iteritems():
            overlap = False
            for event in currentEvents:
                if event.get('convectiveUserOwned'):
                    if event.get(OBJECT_ID) == ID:
                        overlap = True
                        break
                    polygon = loads(recommendedEventValues.get('polygons'))
                    if GeometryFactory.createPolygon(polygon).overlaps(event.getFlattenedGeometry()):
                        overlap = True
                        break
            if not overlap:
                newDict[ID] = recommendedEventValues
        self.flush()
        return newDict

                                
    def flush(self):
        import os
        os.sys.__stdout__.flush()
        
        
    #########################################
    ### OVERRIDES
    
    def defaultWindSpeed(self):
        return 32
    
    def defaultWindDir(self):
        return 270


