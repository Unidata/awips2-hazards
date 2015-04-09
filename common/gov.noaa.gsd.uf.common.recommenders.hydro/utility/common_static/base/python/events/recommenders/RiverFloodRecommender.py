'''
Description: Python wrapper for the Java version of the river flood recommender.
Enables the flood recommender to be run from the python
recommender framework

    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Dec      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents
    Feb      2013  2161      Chris.Golden        Removed megawidget side effects script
                                                 from this file and placed it in its own
                                                 RiverFloodRecommenderSideEffects.py file,
                                                 which is localized.
    Apr 1, 2014  3581        bkowal     Updated to use common hazards hydro
    Jun 17, 2014    3982     Chris.Golden        Changed megawidget 'side effects'
                                                 to 'interdependencies', and
                                                 changed to use simpler way of
                                                 getting and setting values for
                                                 single-state megawidgets' mutable
                                                 properties.
    Aug 15, 2014    4243     Chris.Golden        Changed megawidget interdependency script
                                                 entry point function to be within the
                                                 recommender script.
    
@since: November 2012
@author: GSD Hazard Services Team
'''
import datetime
import EventFactory
import GeometryFactory
import RecommenderTemplate
import numpy
import JUtil
import time
import re
from EventSet import EventSet

from MapsDatabaseAccessor import MapsDatabaseAccessor

from HazardConstants import *
import HazardDataAccess

from com.raytheon.uf.common.hazards.hydro import RiverProDataManager
from gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender import RiverProFloodRecommender
from gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender import FloodRecommenderConstants
from com.raytheon.uf.common.hazards.hydro import FloodDAO
from RiverForecastPoints import RiverForecastPoints
from com.raytheon.uf.common.time import SimulatedTime

#
# The size of the buffer for default flood polygons.
DEFAULT_POLYGON_BUFFER = 0.05

#
# Keys to values in the attributes dictionary produced
# by the flood recommender.
POINT_ID = "pointID"
SELECTED_POINT_ID = "selectedPointID"
POINT = "point"
FORECAST_POINT = "forecastPoint"
MISSING_VALUE = -9999
     
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        pass
        
    def defineScriptMetadata(self):
        """
        @return: A dictionary containing information about this
                 tool
        """
        metaDict = {}
        metaDict["toolName"] = "RiverFloodRecommender"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Builds recommendations based on river gauge points and hydro data."
        metaDict["eventState"] = "Potential"
        return metaDict

    def defineDialog(self, eventSet):
        """
        @return: A dialog definition to solicit user input before running tool
        """        
        dialogDict = {"title": "Flood Recommender"}
        
        choiceFieldDict = {}
        choiceFieldDict["fieldType"] = "RadioButtons"
        choiceFieldDict["fieldName"] = "forecastType"
        choiceFieldDict["label"] = "Type:"
        choiceFieldDict["choices"] = ["Watch", "Warning", "Advisory", "ALL"]
        
        includeNonFloodPointDict = {}
        includeNonFloodPointDict["fieldType"] = "CheckBox"
        includeNonFloodPointDict["fieldName"] = "includePointsBelowAdvisory"
        includeNonFloodPointDict["label"] = "Include points below advisory"
        includeNonFloodPointDict["enable"] = True 

        warningThreshCutOff = {}
        warningThreshCutOff["fieldType"] = "IntegerSpinner"
        warningThreshCutOff["fieldName"] = "warningThreshold"
        warningThreshCutOff["label"] = "Watch/Warning Cutoff Time"
        warningThreshCutOff["values"] = self._getWarningThreshold()
        warningThreshCutOff["minValue"] = 1
        warningThreshCutOff["maxValue"] = 72
        
        fieldDicts = [choiceFieldDict, includeNonFloodPointDict, warningThreshCutOff]
        dialogDict["fields"] = fieldDicts

        valueDict = {"forecastType":"Warning","includePointsBelowAdvisory":True,
                     "warningThreshold": warningThreshCutOff["values"] }
        dialogDict["valueDict"] = valueDict
        
        return dialogDict

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
        
        self._setHazardPolygonDict()
        
        millis = SimulatedTime.getSystemTime().getMillis()
        currentTime = datetime.datetime.fromtimestamp(millis / 1000)
        self._rfp = RiverForecastPoints(currentTime)
        self._riverProDataManager = RiverProDataManager()
        self._riverProFloodRecommender = RiverProFloodRecommender(self._riverProDataManager)
        
        self._warningThreshold = dialogInputMap.get("warningThreshold")
        sessionAttributes = eventSet.getAttributes()
        
        sessionMap = JUtil.pyDictToJavaMap(sessionAttributes)
        inputMap = JUtil.pyDictToJavaMap({"includeNonFloodPoints": True})
        selectedPointID = None
        try:
            selectedPointID = dialogInputMap.get(SELECTED_POINT_ID)
        except:
            pass
            
        if selectedPointID is not None:
              inputMap.put(SELECTED_POINT_ID, selectedPointID)
                    
        spatialMap = JUtil.pyDictToJavaMap(spatialInputMap)
        
        javaEventList = self._riverProFloodRecommender.getRecommendation(
                        sessionMap, inputMap, spatialMap)

        recommendedEventSet = EventSet(javaEventList)  
        currentEvents = self.getCurrentEvents(eventSet, sessionAttributes)        
        mergedEventSet = self.mergeHazardEvents(currentEvents, recommendedEventSet)
        filteredEventSet = self.filterHazards(mergedEventSet, dialogInputMap)
        self.addFloodPolygons(filteredEventSet)
                  
        return filteredEventSet
    
    def toString(self):
        return "RiverFloodRecommender"

    def _setHazardPolygonDict(self):
        hiresInundationAreas = {}
        mapsAccessor = MapsDatabaseAccessor()
        try:
            hiresInundationAreas = mapsAccessor.getPolygonDict(HIRES_RIVERINUNDATION_TABLE)
        except:
            print "Could not connect to HIRES inundation table:", HIRES_RIVERINUNDATION_TABLE

        floodDAO = FloodDAO.getInstance()
        lowresInundationAreas = JUtil.javaMapToPyDict(floodDAO.getAreaInundationCoordinates())

        self.hazardPolygonDict = {}
        for k,v in lowresInundationAreas.iteritems():
            if lowresInundationAreas[k]:
                if k in hiresInundationAreas:
                    self.hazardPolygonDict[k] = hiresInundationAreas[k]
                else:
                    self.hazardPolygonDict[k] = self._parseLowresAreas(k, v)

    def _parseLowresAreas(self, lid, area):
        """
        @param area: A string of lat lons as whole integers
        
        Eg. '4008 9575 4005 9575 4001 9557 4000 9539 4003 9538'
        
        @return Nested lists of lat lon pairs
        
        Eg. [[40.08 95.75] [40.05 95.75] [40.01 95.57] [40.00 95.39] [40.03 95.38]]
        """
        
        area = area.replace('||','')
        area = re.sub(' +', ' ',area)
        validPt = re.search('^\d+ \d+', area)
        if area and validPt:
            pointsStringList = [int(k)/100.0 for k in area.split(' ') if len(k)]
            lats = pointsStringList[0::2]
            lons = [-1.0*k for k in pointsStringList[1::2]]
            pointPairs = [list(y) for y in zip(lons, lats)]
            pointPairs.append(pointPairs[0])
            return pointPairs
        else:
            return None
                
    def getWarningTimeThreshold(self, startEpoch):
        hrs = self._warningThreshold
        if not hrs:
            hrs = self._getWarningThreshold()
        return startEpoch + datetime.timedelta(hours=hrs).total_seconds()
    
    def getCurrentEvents(self, eventSet, sessionAttributes):
        siteID = eventSet.getAttributes().get('siteID')        
        mode = sessionAttributes.get('hazardMode', 'PRACTICE').upper()
        # Get current events from Session Manager (could include pending / potential)
        currentEvents = []
        for event in eventSet:
            currentEvents.append(event)
        # Add in those from the Database
        databaseEvents = HazardDataAccess.getHazardEventsBySite(siteID, mode) 
        eventIDs = [event.getEventID() for event in currentEvents]
        for event in databaseEvents:
            if event.getEventID() not in eventIDs:
                currentEvents.append(event)
                eventIDs.append(event.getEventID())
        return currentEvents
    
    def mergeHazardEvents(self, currentEvents, recommendedEventSet):        
        mergedEvents = EventSet(None)
        for recommendedEvent in recommendedEventSet:
            self.setHazardType(recommendedEvent)
            # Look for an event that already exists
            found = False
            for currentEvent in currentEvents:
                if currentEvent.get(POINT_ID) == recommendedEvent.get(POINT_ID):
                    # If ended, then simply add the new recommended one
                    if currentEvent.getStatus() == 'ENDED':
                        continue 
                    elif currentEvent.getHazardType() != recommendedEvent.getHazardType():
                        # Handle transitions to new hazard type
                        currentEvent.setStatus('ending')
                        mergedEvents.add(currentEvent)
                        recommendedEvent.setStatus('pending')
                        mergedEvents.add(recommendedEvent)
                    else:
                        #  Update current event with recommended rise / crest /fall                        
                        for attribute in ['riseAbove', 'crest', 'fallBelow']:
                            currentEvent.set('attribute', recommendedEvent.get(attribute, MISSING_VALUE))
                        mergedEvents.add(currentEvent)
                    found = True
            if not found:
                mergedEvents.add(recommendedEvent) 
        return mergedEvents
                            
    def setHazardType(self, hazardEvent):        
        pointID = hazardEvent.get(POINT_ID)
        
        riverMile = self._rfp.getRiverMile(pointID)
        hazardEvent.set("riverMile", riverMile)
        
        hazEvtStart = hazardEvent.get("currentStageTime")/1000
        warningTimeThresh = self.getWarningTimeThreshold(hazEvtStart)
        hazardEvent.setPhenomenon("FL")
        
        if self.isWarning(pointID, warningTimeThresh):
            hazardEvent.setSignificance("W")
        elif self.isWatch(pointID, warningTimeThresh):
            hazardEvent.setSignificance("A")
        elif self.isAdvisory(pointID):
            hazardEvent.setSignificance("Y")
        else:
            hazardEvent.setPhenomenon("HY")
            hazardEvent.setSignificance("S")                        

    def filterHazards(self,hazardEvents, dMap):
        filterType = dMap.get('forecastType')
        includeNonFloodPts = dMap.get("includePointsBelowAdvisory")
        selectedPointID = dMap.get(SELECTED_POINT_ID)

        returnEventSet = EventSet(None)

        if filterType == 'ALL':
            if includeNonFloodPts:
                return hazardEvents
            else:
                for hazardEvent in hazardEvents:
                    phenSig = hazardEvent.getHazardType()
                    if phenSig != 'HY.S':
                      returnEventSet.add(hazardEvent)  
                return returnEventSet
            
        
        for hazardEvent in hazardEvents:
            phenSig = hazardEvent.getHazardType()
            
            if phenSig == 'FL.W' and filterType == 'Warning':
                returnEventSet.add(hazardEvent)
            
            elif phenSig == 'FL.A' and filterType == 'Watch':
                returnEventSet.add(hazardEvent)
            
            elif phenSig == 'FL.Y' and filterType == 'Advisory':
                returnEventSet.add(hazardEvent)
                
            elif phenSig == 'HY.S' and includeNonFloodPts:
                returnEventSet.add(hazardEvent)
                    
            elif selectedPointID:
                returnEventSet.add(hazardEvent)
                
            else:
                pass
        
        return returnEventSet

    def addFloodPolygons(self, hazardEvents):
        """
        Inserts flood polygons for each river forecast point
        flood hazard, if they are available.
        """
        if hazardEvents is not None:
            for hazardEvent in hazardEvents:
                self.getFloodPolygonForRiverPointHazard(hazardEvent)                

    def getFloodPolygonForRiverPointHazard(self, hazardEvent):
        """
        Returns a user-defined flood hazard polygon for 
        a river forecast point flood hazard. The base version
        of this tool does nothing. It is up to the implementer
        to override this method.
        
        @param  hazardEvent: A hazard event corresponding to 
                           a river forecast point flood 
                           hazard
        """
        id = hazardEvent.get(POINT_ID)
        
        # Always create the point representing the 
        # The river forecast point location.
        # Then check to determine if there 
        # is an additional inundation map available.
        forecastPointDict = hazardEvent.get(FORECAST_POINT)
        point = forecastPointDict.get(POINT)
        coords = [float(point[0]), float(point[1])]
        pointGeometry = GeometryFactory.createPoint(coords)
        geometryList = [pointGeometry]
                
        if id in self.hazardPolygonDict:
            hazardPolygon = self.hazardPolygonDict[id]
            geometry = GeometryFactory.createPolygon(hazardPolygon)
            geometryList.append(geometry)      
            
        geometryCollection = GeometryFactory.createCollection(geometryList)
        hazardEvent.setGeometry(geometryCollection)
                
    def flush(self):
        import os
        os.sys.__stdout__.flush()
        
        
    #####################
    #  OVERRIDES
    #####################
         
    def _getWarningThreshold(self):
        return 24 

    def isWatch(self, pointID, warningThreshEpoch):
        maxFcstStage = self._rfp.getMaximumForecastStage(pointID)
        floodStage = self._rfp.getFloodStage(pointID)
        fcstRiseAboveFloodStageTime = self._rfp.getForecastRiseAboveFloodStageTime(pointID)
        if fcstRiseAboveFloodStageTime:
            fcstRiseAboveFloodStageTime = fcstRiseAboveFloodStageTime / 1000
        
        if (maxFcstStage >= floodStage) and (fcstRiseAboveFloodStageTime > warningThreshEpoch):
            return True
        else:
            return False
        
    def isWarning(self, pointID, warningThreshEpoch):
        maxFcstStage = self._rfp.getMaximumForecastStage(pointID)
        floodStage = self._rfp.getFloodStage(pointID)
        fcstRiseAboveFloodStageTime = self._rfp.getForecastRiseAboveFloodStageTime(pointID)
        if fcstRiseAboveFloodStageTime:
            fcstRiseAboveFloodStageTime = fcstRiseAboveFloodStageTime / 1000
        
        if (maxFcstStage >= floodStage) and (fcstRiseAboveFloodStageTime <= warningThreshEpoch):
            return True
        else:
            return False
            
    def isAdvisory(self, pointID):
        actionStage = self._rfp.getRiverForecastPoint(pointID).getActionStage()
        maxFcstStage = self._rfp.getMaximumForecastStage(pointID)
        obsStage = self._rfp.getObservedStage(pointID)[0]
        
        if (obsStage >= actionStage) or (maxFcstStage >= actionStage):
            return True
        else:
            return False    


