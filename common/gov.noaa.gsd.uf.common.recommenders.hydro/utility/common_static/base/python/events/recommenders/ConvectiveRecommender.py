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

import TimeUtils
from PHI_GridRecommender import Recommender as PHIGridRecommender
#from MapsDatabaseAccessor import MapsDatabaseAccessor
from ProbUtils import ProbUtils

from HazardConstants import *
import HazardDataAccess

from com.raytheon.uf.common.time import SimulatedTime
from edu.wisc.ssec.cimss.common.dataplugin.convectprob import ConvectProbRecord
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
PROBABILITY_FILTER = 20 # filter our any objects less than this.
SOURCEPATH_ARCHIVE = '/awips2/edex/data/hdf5/convectprob'
SOURCEPATH_REALTIME = '/realtime-a2/hdf5/probsevere'
    

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
        self._probUtils = ProbUtils()
    
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
        return metaDict

    def defineDialog(self, eventSet):
        """
        @return: A dialog definition to solicit user input before running tool
        """        
#===============================================================================
#         dialogDict = {"title": "PHI Cell ID Recommender"}
#         
#         choiceFieldDict = {}
#         choiceFieldDict["fieldType"] = "RadioButtons"
#         choiceFieldDict["fieldName"] = "forecastType"
#         choiceFieldDict["label"] = "Type:"
#         choiceFieldDict["choices"] = ["Watch", "Warning", "Advisory", "ALL"]
#         
#         fieldDicts = []
#         dialogDict["fields"] = fieldDicts
# 
#         valueDict = {"forecastType":"Warning"}
#         dialogDict["valueDict"] = valueDict
#         
#         return dialogDict
#===============================================================================
        return None

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
        
        sessionAttributes = eventSet.getAttributes()
        if sessionAttributes:
            sessionMap = JUtil.pyDictToJavaMap(sessionAttributes)
            
        currentTime = datetime.datetime.utcfromtimestamp(long(sessionAttributes["currentTime"])/1000)
        latestCurrentEventTime = self.getLatestTimestampOfCurrentEvents(eventSet)
        
        currentEvents = self.getCurrentEvents(eventSet)        
        recommendedEventsDict = self.getRecommendedEventsDict(currentTime, latestCurrentEventTime)
        recommendedEventsDict = self.filterForUserOwned(currentEvents, recommendedEventsDict)
                
        mergedEventSet = self.mergeHazardEvents(currentEvents, recommendedEventsDict, currentTime)
        
        #for evt in mergedEventSet:
        #    print 'ME:', evt.getHazardAttributes().get('removeEvent')
        #    #  Tracy -- Not sure about this -- does probSeverAttrs really have an OBJECT_ID entry?
        #    #   It thought it only had wdir and wspd
        #    print evt.getHazardAttributes().get('probSeverAttrs').get(OBJECT_ID) 
        #    print evt.getCreationTime(), evt.getStartTime(), evt.getEndTime()
        
#        ### REMOVE ME!! This is to automate the PHI_GridRecommender following
#        ### execution of the Convective recommender until we find a better 
#        ### way to implement this  
#        pgr = PHIGridRecommender()
#        pgr.execute(mergedEventSet, None, None)
        
        return mergedEventSet
    

    def getRecommendedEventsDict(self, currentTime, latestDatetime):
        hdfFilesList = self._getLatestProbSevereDataHDFFileList(latestDatetime)
        eventsDict = self._eventSetFromHDFFile(hdfFilesList, currentTime, latestDatetime)
        return eventsDict

    def _uvToSpdDir(self, motionEasts, motionSouths):
        if motionEasts is None or motionSouths is None:
            wdir = self._defaultWindDir()
            wspd = self._defaultWindSpeed() #kts
        else:
            u = float(motionEasts)
            v = -1.*float(motionSouths)
            wspd = int(round(math.sqrt(u**2 + v**2) * 1.94384)) # to knots
            wdir = int(round(math.degrees(math.atan2(-u, -v))))
            if wdir < 0:
                wdir += 360
                
        return {'wdir':wdir, 'wspd':wspd}


    def _eventSetFromHDFFile(self, hdfFilenameList, currentTime, latestDatetime=None):

        eventsList = []
        eventsPerTimestamp = {}

        for hdfFilename in hdfFilenameList:
            hFile = None
            try:
                hFile = h5py.File(hdfFilename, 'r')
            except:
                print 'Convective Recommender Warning: Unable to open', latestFile, ' in h5py. Skipping...'
                
            if hFile:
                for group in hFile.values():
                    startTime = self._parseGroupName(group.name)
                    ##endTime = startTime + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
                    #endTime = None
                    
                    ### entry is too old, skip to next one
                    if startTime > currentTime:
                        continue
                    
                    eventsPerTimestamp[startTime] = {}

                    for i in range(group.values()[0].len()):
                        row = {k:v[i] for k,v in group.iteritems()}

                        ### Dumb filter. Need to make dynamic
                        if row.get('probabilities') < PROBABILITY_FILTER:
                            continue
                        
                        
                        row['startTime'] = startTime
                        vectorDict = self._uvToSpdDir(row.get('motionEasts'),row.get('motionSouths'))
                        row['wdir'] = vectorDict.get('wdir')
                        row['wspd'] = vectorDict.get('wspd')
                        
                        eventsPerTimestamp[startTime].update({row['objectids']:row})
            
            if hFile:
                hFile.close()

        latestSet = {}
        if len(eventsPerTimestamp):
            latestTimeStamp = sorted(eventsPerTimestamp.keys())[-1]
            latestSet = eventsPerTimestamp[latestTimeStamp]
            
        return latestSet
                           
                    
    def _parseGroupName(self, rootName):
        name,dtString = rootName.split('::')
        dt = datetime.datetime.strptime(dtString,"%Y-%m-%d_%H:%M:%S.0")
        return dt

    
    def _getLatestProbSevereDataHDFFileList(self, latestDatetime=None):
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
            
            ### Use filename to make datetime
            #returnFileList = [x for x in fileList if 
            #                  datetime.datetime.strptime(os.path.basename(x),
            #                  "convectprob-%Y-%m-%d-%H.h5")
            #                  > latestDatetime
            #                  ]
            
            ### Use file's modification time to make datetime
            returnFileList = [x for x in fileList if 
                              datetime.datetime.utcfromtimestamp(os.path.getmtime(x))
                              > latestDatetime
                              ]
            
            return returnFileList
        else:
            return fileList
                
    
    def toString(self):
        return "ConvectiveRecommender"
    
    def getLatestTimestampOfCurrentEvents(self, eventSet):
        ### Initialize latestDatetime
        latestDatetime = datetime.datetime.min
        siteID = eventSet.getAttributes().get('siteID')
        mode = eventSet.getAttributes().get('hazardMode', 'PRACTICE').upper()
        
        databaseEvents = HazardDataAccess.getHazardEventsBySite(siteID, mode)
        for event in databaseEvents:
            eventCreationTime = event.getCreationTime()
            if eventCreationTime > latestDatetime:
               latestDatetime =  eventCreationTime

        for event in eventSet:
            eventCreationTime = event.getCreationTime()
            if eventCreationTime > latestDatetime:
               latestDatetime =  eventCreationTime
               
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

    def makeHazardEvent(self, ID, values, currentTime):        
        hazardEvent = EventFactory.createEvent()
        hazardEvent.setCreationTime(currentTime)
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
        
        return hazardEvent

    def setEventTimes(self, event, values):
        psStartTime = values.pop('startTime')
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
            
    def mergeHazardEvents(self, currentEventsList, recommendedEventsDict, currentTime):    
        ### if no recommended events
        if len(recommendedEventsDict) == 0:
            #print '[1] No NEW records, returning no events
            return [] # We only want to return events we have changed
        
        mergedEvents = EventSet(None)
        
        # Ensure that any resulting events are saved to the database.
        mergedEvents.addAttribute("saveToDatabase", True)
        
        ### recommended events but no current events
        if len(currentEventsList) == 0:
            #print '[2] No CURRENT records, making and returning NEW events...'
            for ID, recommendedValues in recommendedEventsDict.iteritems():
                recommendedEvent = self.makeHazardEvent(ID, recommendedValues, currentTime)
                mergedEvents.add(recommendedEvent)
            return mergedEvents

        # Do not change UserOwned currentEvents
        #   NOTE: This is redundant since we've already filtered out
        #      the recommended events that would coincide with 
        #      User Owned current events.
        #  However, filtering them out from the merge will save a bit of time.
        nonUserOwnedCurrentEvents = EventSet(None)
        for c in currentEventsList:
            if not c.get('convectiveUserOwned'): 
                nonUserOwnedCurrentEvents.add(c)
                            
        recEventObjectIDs = sorted(recommendedEventsDict.keys())
        
        ### Manual Events are filtered out, so now only need to worry about merging/comparing automated events
        nonUserOwnedObjectIDs = list(set([c.get('objectID') for c in nonUserOwnedCurrentEvents]))
                
        ### Using set difference, obtain new recommended objects and add
        newRecs = list(set(recEventObjectIDs).difference(set(nonUserOwnedObjectIDs)))
        for newRec in newRecs:
            #print '[3] Adding NEW event', newRec, ' to mergedEvents'            
            recommendedEvent = self.makeHazardEvent(newRec, recommendedEventsDict[newRec], currentTime)
            
            recGeom = recommendedEvent.getGeometry()
            intersects = False
                
            #===================================================================
            # #########  FIXME ###############
            # #
            # # Keep getting error at intersects call:
            # # <class 'shapely.geos.PredicateError'>: Failed to evaluate <_FuncPtr object at 0x7f88208aeae0>
            # # at /awips2/python/lib/python2.7/site-packages/shapely/geos.errcheck_predicate(geos.py:500)
            # # at /awips2/python/lib/python2.7/site-packages/shapely/predicates.__call__(predicates.py:11)
            # # at /awips2/python/lib/python2.7/site-packages/shapely/geometry/base.intersects(base.py:614)
            # # but cannot repeat in python interpreter (see intersectionTest.py)
            # ################################
            #
            # ### Logic to ignore any automated events that spatially overlap a manually drawn event
            # for manEvt in manualCurrentEvents:
            #     manGeom = manEvt.getGeometry()
            #     print 'MAN:', type(manGeom)
            #     print 'AUTO:', type(recGeom)
            #     #if recGeom.intersects(manGeom):
            #     if manGeom.intersects(recGeom):
            #         intersects = True
            #===================================================================

            if not intersects:
                mergedEvents.add(recommendedEvent)
                            
        ### Using set difference, obtain expired (recommended event ID no longer present) IDs and set hazardEvent to 'ended'
        expiredRecs = list(set(nonUserOwnedObjectIDs).difference(set(recEventObjectIDs)))
        for expiredRec in expiredRecs:
            #print '[4] Found event', expiredRec, ' to potentially be removed...'
            for currentEvent in nonUserOwnedCurrentEvents:
                if currentEvent.get('objectID') == expiredRec:
                    if currentEvent.getStatus() != 'ISSUED' and currentEvent.get('objectID'):
                        currentEvent.set('vtecCodes', ['CAN'])
                        currentEvent.setStatus('ENDED')
                        #print '\t[4.5] Setting current event', currentEvent.get('objectID'), ' to status ', currentEvent.getStatus()
                        mergedEvents.add(currentEvent)
        
        ### Using set intersection, update current events with recommended event attributes
        updates = list(set(recEventObjectIDs).intersection(set(nonUserOwnedObjectIDs)))
        for currentEvent in nonUserOwnedCurrentEvents:
            currID = currentEvent.get('objectID')
            #print '[5] Potentially updating event', currID, ' (status', currentEvent.getStatus(), ')'
            if currID not in expiredRecs and currID in recommendedEventsDict:
                #print '\t[5.5] Yep, now updating ', currID
                recommendedEventValues = recommendedEventsDict[currID]
                self.setEventTimes(currentEvent, recommendedEventValues)
                try:
                    currentEvent.setGeometry(recommendedEventValues.pop('polygons'))
                except:
                    print 'ConvectiveRecommender: WHAT\'S WRONG WITH THIS POLYGON?', currID, type(recommendedEventValues.get('polygons')) 
                    
                currentEvent.set('convectiveObjectDir', recommendedEventValues.get('wdir'))
                currentEvent.set('convectiveObjectSpdKts', recommendedEventValues.get('wspd'))
                currentEvent.set('probSeverAttrs',recommendedEventValues)
                mergedEvents.add(currentEvent)
                            
#         print 'Returning...'
#         for evt in mergedEvents:
#             print evt.get('objectID'), evt.getStatus()
#         print '=== Done ==='        
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
                    if GeometryFactory.createPolygon(polygon).overlaps(event.getGeometry()):
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
    
    def _defaultWindSpeed(self):
        return 32
    
    def _defaultWindDir(self):
        return 270

    #########################################
                


