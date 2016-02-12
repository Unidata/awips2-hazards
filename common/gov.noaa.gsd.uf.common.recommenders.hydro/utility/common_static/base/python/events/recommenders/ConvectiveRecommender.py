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

from PHIGridRecommender import Recommender as PHIGridRecommender



#from MapsDatabaseAccessor import MapsDatabaseAccessor

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
PROBABILITY_FILTER = 40 # filter our any objects less than this.
SOURCEPATH = '/awips2/edex/data/hdf5/convectprob'

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

    #===========================================================================
    # def defineSpatialInfo(self):
    #     '''
    #     @summary: Determines spatial information needed by the recommender.
    #     @return: Unknown
    #     @todo: fix comments, further figure out spatial info
    #     '''
    #     resultDict = {"outputType":"spatialInfo",
    #                   "label":"Drag To Hazard Location", "returnType":"Point"}
    #     return resultDict
    #===========================================================================

    
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
        metaDict["eventState"] = "Potential"
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
#         fieldDicts = [choiceFieldDict]
#         dialogDict["fields"] = fieldDicts
# 
#         valueDict = {"forecastType":"Warning"}
#         dialogDict["valueDict"] = valueDict
#         
#         return dialogDict
#===============================================================================
        return None

    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        """
        Runs the River Flood Recommender tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param spatialInputMap:   A map of information retrieved
                                  from the user's interaction with the
                                  spatial display.
        
        @return: A list of potential events. 
        """
        
        sessionAttributes = eventSet.getAttributes()
        if sessionAttributes:
            sessionMap = JUtil.pyDictToJavaMap(sessionAttributes)
        if spatialInputMap:
            spatialMap = JUtil.pyDictToJavaMap(spatialInputMap)
            
        currentTime = datetime.datetime.fromtimestamp(long(sessionAttributes["currentTime"])/1000)
        latestCurrentTime = self.getLatestTimestampOfCurrentEvents(eventSet)
        
        ### Is this needed?  Tracy says there are instances where events are
        ### not included in the passed in eventSet, but are available in the database
        currentEvents = self.getCurrentEvents(eventSet)
        
        recommendedEventList = self.getRecommendedEvents(currentTime, latestCurrentTime)
                
        mergedEventSet = self.mergeHazardEvents(currentEvents, recommendedEventList, currentTime)
        
        #for evt in mergedEventSet:
        #    print 'ME:', evt.getHazardAttributes().get('removeEvent'), evt.getHazardAttributes().get('probSeverAttrs').get(OBJECT_ID), evt.getCreationTime(), evt.getStartTime(), evt.getEndTime()
        
#        ### REMOVE ME!! This is to automate the PHIGRidRecommender following
#        ### execution of the Convective recommender until we find a better 
#        ### way to implement this  
#        pgr = PHIGridRecommender()
#        pgr.execute(mergedEventSet, None, None)
        
        return mergedEventSet
    

    def getRecommendedEvents(self, currentTime, latestDatetime):
        hdfFilesList = self._getLatestProbSevereDataHDFFileList(latestDatetime)
        eventList = self._eventSetFromHDFFile(hdfFilesList, currentTime, latestDatetime)
        
        return eventList

    def _uvToSpdDir(self, motionEasts, motionSouths):
        if motionEasts is None or motionSouths is None:
            wdir = 270
            wspd = 32 #kts
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
                print 'Unable to open', latestFile, ' in h5py. Skipping...'
                
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
                        
        hFile.close()

        latestSet = []
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
            fileList = sorted(glob.glob(os.path.join(SOURCEPATH,'*.h5')), 
                          reverse=True)
        except:
            print 'Could not obtain list of convectprob*.h5 files at:', os.path.join(SOURCEPATH,'*.h5')
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
                              datetime.datetime.fromtimestamp(os.path.getmtime(x))
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
            startTime = values.pop('startTime')
            hazardEvent.setStartTime(startTime)
            endTime = startTime + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
            hazardEvent.setEndTime(endTime)
            
            hazardEvent.setHazardStatus("potential")
            hazardEvent.setHazardMode("O")
            hazardEvent.setPhenomenon("Prob_Severe")
            
            polygon = loads(values.pop('polygons'))
            hazardEvent.setGeometry(polygon)
            hazardEvent.set('probSeverAttrs',values)
            hazardEvent.set('objectID', ID)
            hazardEvent.set('removeEvent',False)
            
            return hazardEvent
        
    
    def mergeHazardEvents(self, currentEvents, recommendedEventList, currentTime):        
        mergedEvents = EventSet(None)
        
        ### if no recommended events, return current events
        if len(recommendedEventList) == 0:
            return currentEvents
        
        recEventObjectIDs = sorted(recommendedEventList.keys())
        currEventObjectIDs = list(set([c.get('objectID') for c in currentEvents]))
        
        for ID, recommendedEvent in recommendedEventList.iteritems():

            
            if ID not in currEventObjectIDs:
                recommendedEvent = self.makeHazardEvent(ID, recommendedEvent, currentTime)
                mergedEvents.add(recommendedEvent)
                
            else:
            
                for currentEvent in currentEvents:
                    
                    if currentEvent.get('objectID') not in recEventObjectIDs and currentEvent.getStatus() != 'ISSUED':
                        ### See CommonMetaData where manually drawn events get objectID starting with 'M'
                        if not currentEvent.get('objectID').startswith('M'): 
                            currentEvent.set('removeEvent',True)
                            mergedEvents.add(currentEvent)
                   
                    elif currentEvent.get('objectID') == ID:
                        # If ended, then simply add the new recommended one
                        if currentEvent.getStatus() == 'ELAPSED' or currentEvent.getStatus() == 'ENDED':
                            continue 
                        
                        ### Needed? Will there be any other hazardType than "Prob_Severe"?
#                        elif currentEvent.getHazardType() != recommendedEvent.getHazardType():
#                            print '\t[', fi_filename, getframeinfo(currentframe()).lineno,']'
#                            # Handle transitions to new hazard type
#                            currentEvent.setStatus('ending')
#                            mergedEvents.add(currentEvent)
#                            #recommendedEvent.setStatus('pending')
#                            mergedEvents.add(recommendedEvent)
                        else:
                            #currentEvent.setCreationTime(recommendedEvent.getCreationTime())
                            startTime = recommendedEvent.pop('startTime')
                            endTime = startTime + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
                            currentEvent.setStartTime(startTime)
                            currentEvent.setEndTime(endTime)
                            currentEvent.setGeometry(recommendedEvent.pop('polygons'))
                            currentEvent.set('probSeverAttrs',recommendedEvent)
                            mergedEvents.add(currentEvent)
                
                
        return mergedEvents
                            

                
    def flush(self):
        import os
        os.sys.__stdout__.flush()

