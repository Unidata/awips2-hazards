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
#DEFAULT_DURATION_IN_SECS = 2700 # 45 minutes
DEFAULT_DURATION_IN_SECS = 120
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

        self._probSevereKeys = ['ObjectType', 
                                'Probability', 
                                'MUCAPE', 
                                'EffectiveShear',
                                'MESH',
                                'SatGrowthPredictor1',
                                'SatGrowthPredictor2',
                                'LatLons',
                                'ObjectID',
                                'MeanMotionEast',
                                'MeanMotionSouth'
                                ]
        
        #self._probSvrFile = open('/home/kevin.manross/SSEC_AWIPS_CONVECTPROB_20151012_200017.ascii','r')
        self._probSvr='''
Valid: 20151012_200017 
RAD:5:3007 J/kg:26.8 kts:1954Z 0.15 in.:N/A:N/A:25.72,-77.96,25.72,-77.95,25.73,-77.94,25.74,-77.94,25.75,-77.93,25.76,-77.92,25.76,-77.91,25.76,-77.90,25.75,-77.89,25.75,-77.88,25.75,-77.87,25.74,-77.87,25.73,-77.87,25.72,-77.87,25.71,-77.87,25.70,-77.87,25.70,-77.88,25.70,-77.89,25.70,-77.90,25.71,-77.91,25.70,-77.92,25.70,-77.93,25.71,-77.94,25.71,-77.95,25.71,-77.96,25.72,-77.96:26286:4.12000:-1.18300 
RAD:6:603 J/kg:38.6 kts:1958Z 0.08 in.:1945Z 1.59 %/min (strong):1945Z 0.04 /min (moderate):37.97,-89.04,37.98,-89.03,37.99,-89.03,38.00,-89.02,38.01,-89.01,38.01,-89.00,38.01,-88.99,38.01,-88.98,38.02,-88.97,38.02,-88.96,38.02,-88.95,38.02,-88.94,38.01,-88.94,38.00,-88.94,37.99,-88.94,37.98,-88.93,37.97,-88.94,37.96,-88.95,37.95,-88.96,37.94,-88.97,37.94,-88.98,37.93,-88.99,37.93,-89.00,37.93,-89.01,37.94,-89.02,37.95,-89.03,37.96,-89.04,37.97,-89.04:26290:3.77521:7.83281 
RAD:4:662 J/kg:38.8 kts:2000Z 0.00 in.:N/A:N/A:37.52,-89.40,37.53,-89.39,37.54,-89.38,37.54,-89.37,37.55,-89.36,37.55,-89.35,37.55,-89.34,37.54,-89.34,37.53,-89.34,37.52,-89.34,37.51,-89.34,37.50,-89.34,37.49,-89.34,37.48,-89.34,37.47,-89.35,37.47,-89.36,37.47,-89.37,37.47,-89.38,37.47,-89.39,37.48,-89.40,37.49,-89.40,37.50,-89.40,37.51,-89.40,37.52,-89.40:26291:2.49717:7.77522
'''.strip()

        
    
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
            
        #forecastType = dialogInputMap.get("forecastType")
        
        
        currentTime = datetime.datetime.fromtimestamp(long(sessionAttributes["currentTime"])/1000)
        latestCurrentTime = self.getLatestTimestampOfCurrentEvents(eventSet)

        
        ### Is this needed?  Tracy says there are instances where events are
        ### not included in the passed in eventSet, but are available in the database
        currentEvents = self.getCurrentEvents(eventSet)
        
        recommendedEventSet = self.getRecommendedEvents(currentTime, latestCurrentTime)
                
        mergedEventSet = self.mergeHazardEvents(currentEvents, recommendedEventSet)
        
#        for evt in mergedEventSet:
#            print 'ME:', evt.getHazardAttributes().get('removeEvent'), evt.getHazardAttributes().get('probSeverAttrs').get(OBJECT_ID)
        
        return mergedEventSet
    

    def getRecommendedEvents(self, currentTime, latestDatetime):
        hdfFilesList = self._getLatestProbSevereDataHDFFileList(latestDatetime)
        eventSet = self._eventSetFromHDFFile(hdfFilesList, currentTime, latestDatetime)
        
        return eventSet


    def _eventSetFromHDFFile(self, hdfFilenameList, currentTime, latestDatetime=None):

        eventsList = []
        
        for hdfFilename in hdfFilenameList:
            hFile = None
            try:
                hFile = h5py.File(hdfFilename, 'r')
            except:
                print 'Unable to open', latestFile, ' in h5py. Skipping...'
                
            if hFile:
                for group in hFile.values():
                    startTime = self._parseGroupName(group.name)
                    endTime = startTime + datetime.timedelta(seconds=DEFAULT_DURATION_IN_SECS)
                    
                    
                    ### entry is too old, skip to next one
                    if startTime < latestDatetime or startTime > currentTime or endTime < currentTime:
                        continue
                    
        
                    
                    for i in range(group.values()[0].len()):
                        row = {k:v[i] for k,v in group.iteritems()}
                        
                        ### Dumb filter. Need to make dynamic
                        if row.get('probabilities') < PROBABILITY_FILTER:
                            continue
                        
                        
                        hazardEvent = EventFactory.createEvent()
                        hazardEvent.setCreationTime(currentTime)
                        hazardEvent.setStartTime(startTime)
                        hazardEvent.setEndTime(endTime)
                        
                        #hazardEvent.setHazardStatus("PENDING")
                        hazardEvent.setHazardMode("O")
                        hazardEvent.setPhenomenon("Prob_Severe")
                        
                        
                        polygon = loads(row.pop('polygons'))
                        hazardEvent.setGeometry(polygon)
                        hazardEvent.setHazardAttributes({'probSeverAttrs':row})
                        hazardEvent.set('objectID', row.get('objectids'))
                        hazardEvent.set('removeEvent',False)
                        eventsList.append(hazardEvent)
                
                hFile.close()

        eventSet = EventSetFactory.createEventSet(eventsList)
        return eventSet
                           
                    
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
    
    def mergeHazardEvents(self, currentEvents, recommendedEventSet):        
        mergedEvents = EventSet(None)
        recEventObjectIDs = list(set([c.get('probSeverAttrs').get(OBJECT_ID) for c in recommendedEventSet]))
        currEventObjectIDs = list(set([c.get('probSeverAttrs').get(OBJECT_ID) for c in currentEvents]))
        
        
        for recommendedEvent in recommendedEventSet:
            recommendedEvent.set('removeEvent',False)
            
            recProbSeverAttrs = recommendedEvent.get('probSeverAttrs')
            
            if recProbSeverAttrs.get(OBJECT_ID) not in currEventObjectIDs:
                mergedEvents.add(recommendedEvent)
                
            else:
            
                for currentEvent in currentEvents:
                    curProbSeverAttrs = currentEvent.get('probSeverAttrs')
                    if curProbSeverAttrs.get(OBJECT_ID) == recProbSeverAttrs.get(OBJECT_ID):
                        # If ended, then simply add the new recommended one
                        if currentEvent.getStatus() == 'ELAPSED' or currentEvent.getStatus() == 'ENDED':
                            continue 
                        
                        ### Needed? Will there be any other hazardType than "Prob_Severe"?
                        elif currentEvent.getHazardType() != recommendedEvent.getHazardType():
                            # Handle transitions to new hazard type
                            currentEvent.setStatus('ending')
                            mergedEvents.add(currentEvent)
                            recommendedEvent.setStatus('pending')
                            mergedEvents.add(recommendedEvent)
                        else:
                            currentEvent.setGeometry(recommendedEvent.getGeometry())
                            currentEvent.set('probSeverAttrs',recProbSeverAttrs)
                            mergedEvents.add(currentEvent)
                
        ### Needed if currentEvent is no longer, but not issued
        for c in currentEvents:
            if c.get('probSeverAttrs').get(OBJECT_ID) not in recEventObjectIDs and c.getStatus() != 'ISSUED':
                c.set('removeEvent',True)
                mergedEvents.add(c)
                
        return mergedEvents
                            

                
    def flush(self):
        import os
        os.sys.__stdout__.flush()

