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
    May 14, 2015    7974     Robert.Blum         Added hours label to cutoff time.
    May 18, 2015    6562     Chris.Cody          Restructure River Forecast Points/Recommender
    May 26, 2015    7634     Chris.Cody          Changes for Forecast Bullet Generation
    Jul 29, 2015    9306     Chris.Cody          Add processing for HazardSatus.ELAPSED
    Aug 11, 2015    9448     Robert.Blum         Changed to look at both observed and forecast
                                                 data when computing Warning/Watch/Advisory.
    Aug 18, 2015    9650     Robert.Blum and     Changes to allow the recommender to correctly issue
                                 Chris.Golden    new statuses for events. Also changed mergedHazardEvents to
                                                 return a tuple, so that it can also return a list of event
                                                 identifiers for events to be deleted. 
    Sep 10, 2015   10195     Chris.Cody          Modify getFloodPolygonForRiverPointHazard to take a string
                                                 representation of a list or a list of strings of point coords
    Nov 12, 2015   11870     Robert.Blum         Updated mergeHazardEvents to change ending events back to 
                                                 issued if the endTime changed. Also to set events to ending if 
                                                 there is no recommended hazard with the same point id.
    Dec 11, 2015   13312     Michael Duff        Fix to not recommend downgrading existing issued hazards.
    Jan 07, 2016   13143     Robert.Blum         Filtered out ELAPSED hazards from the currentEvents.
    Feb 09, 2016    9650     Roger.Ferrel        When flood level fall below alert level mark any issued Watch as ending.
    Feb 25, 2016   15225     Robert.Blum         Filtered out ENDED hazards from the currentEvents.
    Mar 23, 2016   16048     Kevin.Bisanz        Don't remove non-issued events if recommender was run against a
                                                 specific point ID.
    May 04, 2016   15584     Kevin.Bisanz        Added _getFloodValues(..) and refactored isWatch(..),
                                                 isWarning(..),  and isAdvisory(..).
    May 06, 2016   18217     mduff               Recommended hazard events potential rather than pending.
    May 06, 2016   18202     Robert.Blum         Changes for operational mode.
    May 06, 2016   15225     Robert.Blum         Added overridable method for FL.W to FL.Y behavior.
    May 09, 2016   18241     Roger.Ferrel        Changed mergeHazardEvents sets replaceBy when changing a Watch to Warning/Advisory.
    May 09, 2016   18219     Roger.Ferrel        Only allow one PENDING warning for a given point.
    May 06, 2016   17512     Robert.Blum         Test Matrix fixes for rows 15-17 - ending a FL.A.
    May 09, 2016   17514     Robert.Blum         Added Falling Limb scenario logic.
    May 10, 2016   18240     Chris.Golden        Changed selectedPointID to be provided via event set attribute, so that
                                                 running the recommender from a gage allows the recommender's dialog to
                                                 pop up and be used.
    May 16, 2016   18219     Kevin.Bisanz        Fix 18219 bug: wrong event set to ENDING.
    May 18, 2016   17342     Ben.Phillippe       Added location constraint to polygon query
    Jun 09, 2016   17516     mduff               Moved getCurrentEvents to HazardDataAccess.
    Jun 23, 2016   19537     Chris.Golden        Removed spatial info, since it was unused.
    Jun 30, 2016   17514     Kevin.Bisanz        Change Falling Limb scenario logic.
    Aug 09, 2016   20382     Ben.Phillippe       Impacts CurObs and MaxFcst values updated 
    Aug 22, 2016   15017     Robert.Blum         Updating additional attributes when recommender runs.
    Aug 26, 2016   21435     Sara.Stewart        Crests MaxFcst and CurObs values added
    Sep 02, 2016   21612     Mark.Fegan          Adjust Observed and Forecast Flood Severity for FL.W per NWSI 10-1703.
    Sep 08, 2016   17514    Robert.Blum          Changed logic so that any hazard with a pre-existing
                                                 pointID is automatically included in the output, disregarding
                                                 the filter selection.

@since: November 2012
@author: GSD Hazard Services Team
'''
import sys
import datetime
import EventFactory
import GeometryFactory
import RecommenderTemplate
import numpy
import JUtil
import time
import re
from EventSet import EventSet
from Bridge import Bridge

from HazardConstants import *
import HazardDataAccess
from RiverForecastUtils import RiverForecastUtils

from com.raytheon.uf.common.hazards.hydro import RiverForecastPoint
from com.raytheon.uf.common.hazards.hydro import RiverStationInfo
from gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender import RiverProFloodRecommender
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

# values related to adjusting FL.W severity
NULL_CATEGORY = "N"
NO_FLOOD_CATEGORY = "0"

class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        self._riverProFloodRecommender = None
        self._riverForecastUtils = RiverForecastUtils()
        
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
        selectedPointID = None
        if SELECTED_POINT_ID in eventSet.getAttributes():
            selectedPointID = eventSet.getAttribute(SELECTED_POINT_ID)

        dialogDict = {"title": "Flood Recommender"}
        
        # Only add the filter widgets when running for all points
        if selectedPointID is None:
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
        warningThreshCutOff["label"] = "Watch/Warning Cutoff Time (hours)"
        warningThreshCutOff["values"] = self._getWarningThreshold()
        warningThreshCutOff["minValue"] = 1
        warningThreshCutOff["maxValue"] = 72

        if selectedPointID is None:
            fieldDicts = [choiceFieldDict, includeNonFloodPointDict, warningThreshCutOff]
            valueDict = {"forecastType":"Warning",
                         "includePointsBelowAdvisory":False,
                         "warningThreshold": warningThreshCutOff["values"] }
        else:
            fieldDicts = [includeNonFloodPointDict, warningThreshCutOff]
            valueDict = {"includePointsBelowAdvisory":True,
                         "warningThreshold": warningThreshCutOff["values"] }

        dialogDict["fields"] = fieldDicts
        dialogDict["valueDict"] = valueDict
        return dialogDict

    def execute(self, eventSet, dialogInputMap, visualFeatures):
        """
        Runs the River Flood Recommender tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param visualFeatures: Visual features as defined by the
                               defineSpatialInfo() method and
                               modified by the user to provide
                               spatial input; ignored.
        
        @return: A list of potential events. 
        """
        
        self._setHazardPolygonDict(eventSet.getAttribute("localizedSiteID"))

        if self._riverProFloodRecommender is None:
            self._riverProFloodRecommender = RiverProFloodRecommender()
        
        self._warningThreshold = dialogInputMap.get("warningThreshold")
        sessionAttributes = eventSet.getAttributes()
        
        sessionMap = JUtil.pyDictToJavaMap(sessionAttributes)
        inputMap = JUtil.pyDictToJavaMap({"includeNonFloodPoints": True})
        selectedPointID = None
        if SELECTED_POINT_ID in sessionAttributes:
            selectedPointID = sessionAttributes.get(SELECTED_POINT_ID)
            
        selectedPointIdList = []
        if selectedPointID is not None:
            dialogInputMap['forecastType'] = "ALL"
            inputMap.put(SELECTED_POINT_ID, selectedPointID)
            selectedPointIdList.append(selectedPointID)
        
        javaEventList = self._riverProFloodRecommender.getRecommendation(
                        sessionMap, inputMap)

        recommendedEventSet = EventSet(javaEventList)  
        currentEvents = HazardDataAccess.getCurrentEvents(eventSet)        
        toBeDeleted, mergedEventSet = self.mergeHazardEvents(currentEvents, recommendedEventSet, selectedPointIdList)
        filteredEventSet = self.filterHazards(mergedEventSet, currentEvents, dialogInputMap)
        self.addFloodPolygons(filteredEventSet)
        filteredEventSet.addAttribute('deleteEventIdentifiers', toBeDeleted)
        return filteredEventSet
    
    def toString(self):
        return "RiverFloodRecommender"

    def _setHazardPolygonDict(self, siteID):
        if self._riverProFloodRecommender is None:
            self._riverProFloodRecommender = RiverProFloodRecommender()
        lowresInundationAreas = JUtil.javaMapToPyDict(self._riverProFloodRecommender.getAreaInundationCoordinates())

        self.hazardPolygonDict = {}
        for k,v in lowresInundationAreas.iteritems():
            if lowresInundationAreas[k]:
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
    
    def mergeHazardEvents(self, currentEvents, recommendedEventSet, selectedPointIdList):        
        mergedEvents = EventSet(None)
        deleteEventIdentifiers = set()
        
        # Remove non-issued FL.* hazards from the currentEvents
        # that do not match a pointID in the recommendedEvents
        # but only if the recommender was not run against a specific point ID.
        for currentEvent in currentEvents:
            foundCurrentPointID = False
            for recommendedEvent in recommendedEventSet:
                if currentEvent.get(POINT_ID) == recommendedEvent.get(POINT_ID):
                    foundCurrentPointID = True
                    break
            if not foundCurrentPointID and len(selectedPointIdList) == 0:
                if currentEvent.getPhenomenon() == "FL":
                    if currentEvent.getStatus() in ['POTENTIAL', 'PENDING']:
                        # Not in recommended events and pending - remove it
                        deleteEventIdentifiers.add(currentEvent.getEventID())
                    else:
                        # Not in recommended events and issued - set to ending
                        currentEvent.setStatus('ending')
                        mergedEvents.add(currentEvent)

        for recommendedEvent in recommendedEventSet:
            # If flood stage is missing you cannot create a hazard, skip it
            pointID = recommendedEvent.get(POINT_ID)
            riverForecastPoint = self._riverProFloodRecommender.getRiverForecastPoint(pointID)
            if riverForecastPoint.getFloodStage() == MISSING_VALUE:
                continue
            
            self.setHazardType(recommendedEvent)
            
            self.adjustFloodSeverity(recommendedEvent)
            
            # Look for an event that already exists
            found = False
            pendingCurrentEvent = None
            haveWarning = False
            for currentEvent in currentEvents:
                if currentEvent.get(POINT_ID) == recommendedEvent.get(POINT_ID):
                    # If ended, then simply add the new recommended one
                    if currentEvent.getStatus() == 'ELAPSED' or currentEvent.getStatus() == 'ENDED':
                        continue 
                    elif currentEvent.getStatus() in ['POTENTIAL', 'PENDING'] \
                     and currentEvent.getHazardType() == recommendedEvent.getHazardType() \
                     and currentEvent.getSignificance() in ['W', 'Y']:
                        # Already have a pending event for point do not add a second one.
                        deleteEventIdentifiers.add(recommendedEvent.getEventID())
                        pendingCurrentEvent = None
                        haveWarning = True
                        found = True
                        continue
                    elif currentEvent.getHazardType() != recommendedEvent.getHazardType():
                        # Handle transitions to new hazard type
                        if currentEvent.getStatus() in ['POTENTIAL', 'PENDING']:
                            # Never issued - delete it
                            deleteEventIdentifiers.add(currentEvent.getEventID())
                        elif currentEvent.getStatus() == 'ENDING':
                            # Issued - set it to ending
                            currentEvent.setStatus('ending')
                            mergedEvents.add(currentEvent)
                        elif currentEvent.getStatus() == 'ISSUED':
                            if currentEvent.getSignificance() == 'W':
                                if recommendedEvent.getHazardType() == 'FL.Y':
                                    if self.recommendFallingLimbAdvisoryForWarning(riverForecastPoint) == False:
                                        deleteEventIdentifiers.add(recommendedEvent.getEventID())
                                    else:
                                        mergedEvents.add(recommendedEvent)
                                    if self.endWarningIfAdvisoryRecommended():
                                        currentEvent.setStatus('ending')
                                        mergedEvents.add(currentEvent)
                                elif recommendedEvent.getHazardType() == 'HY.S':
                                    currentEvent.setStatus('ending')
                                    mergedEvents.add(currentEvent)
                                continue
                            elif currentEvent.getSignificance() == 'A':
                                # Watch should be ended if a HY.S is recommended or
                                # it is being replaced with a Warning/Advisory.
                                if recommendedEvent.getSignificance() in ['W', 'Y'] or recommendedEvent.getHazardType() == 'HY.S':
                                    if haveWarning == False:
                                        pendingCurrentEvent = currentEvent
                                else:
                                    deleteEventIdentifiers.add(recommendedEvent.getEventID())
                                continue
                            elif currentEvent.getSignificance() == 'Y':
                                # Advisory can be upgraded to Watch or Warning
                                currentEvent.setStatus('ending')  
                                mergedEvents.add(recommendedEvent)
                                mergedEvents.add(currentEvent)
                                continue   
                            
                        else:
                            # PROPOSED - leave as is?
                            # Will result in 2 pending hazards for same point ID
                            mergedEvents.add(currentEvent)
                        mergedEvents.add(recommendedEvent)
                    else:
                        #  Update current event with recommended rise / crest /fall                        
                        for attribute in ['riseAbove', 'crest', 'fallBelow', 'currentStage', 'currentStageTime',
                                          'crestStage', 'floodRecord', 'floodSeverityObserved', 'floodSeverityForecast'
                                          ,'impactsCurObsField','impactsMaxFcstField', 'crestsMaxFcstField', 
                                          'crestsCurObsField', 'obsCrestStage', 'forecastCrestStage', 'observedCrestTime', 
                                          'forecastCrestTime', 'maxForecastStage', 'maxForecastTime']:
                            currentEvent.set(attribute, recommendedEvent.get(attribute, MISSING_VALUE))
                        currentEndTime = currentEvent.getEndTime()
                        newEndTime = recommendedEvent.getEndTime()
                        if currentEndTime < newEndTime and currentEvent.getStatus() == 'ENDING':
                            # endTime changed - move back to issued
                            currentEvent.setStatus('issued')
                        currentEvent.setEndTime(newEndTime)
                        mergedEvents.add(currentEvent)
                    found = True
            if pendingCurrentEvent and haveWarning == False:
                bridge = Bridge()
                cHeadline = bridge.getHazardTypes(hazardType=pendingCurrentEvent.getHazardType()).get('headline')
                rHeadline = bridge.getHazardTypes(hazardType=recommendedEvent.getHazardType()).get('headline')

                recommendedEvent.set('replaces', cHeadline)
                pendingCurrentEvent.setStatus('ending')
                pendingCurrentEvent.set('replacedBy', rHeadline)
                mergedEvents.add(pendingCurrentEvent)
            if not found:
                mergedEvents.add(recommendedEvent)
                
        return list(deleteEventIdentifiers), mergedEvents
                            
    def setHazardType(self, hazardEvent):        
        pointID = hazardEvent.get(POINT_ID)

        riverForecastPoint = self._riverProFloodRecommender.getRiverForecastPoint(pointID)
        riverStationInfo = self._riverProFloodRecommender.getRiverStationInfo(pointID)
        riverMile = riverStationInfo.getMile()
        hazardEvent.set("riverMile", riverMile)
        
        currentStageTime = MISSING_VALUE
        currentShefObserved = riverForecastPoint.getCurrentObservation()
        if currentShefObserved is not None:
            currentStageTime = currentShefObserved.getObsTime()
        if currentStageTime == MISSING_VALUE:
            currentStageTime = SimulatedTime.getSystemTime().getMillis()
        
        hazEvtStart = currentStageTime/1000
        warningTimeThresh = self.getWarningTimeThreshold(hazEvtStart)
        hazardEvent.setPhenomenon("FL")
        
        if self.isWarning(riverForecastPoint, warningTimeThresh):
            hazardEvent.setSignificance("W")
        elif self.isWatch(riverForecastPoint, warningTimeThresh):
            hazardEvent.setSignificance("A")
        elif self.isAdvisory(riverForecastPoint):
            hazardEvent.setSignificance("Y")
        else:
            hazardEvent.setPhenomenon("HY")
            hazardEvent.setSignificance("S")

    def adjustFloodSeverity(self, hazardEvent):
        ''' Changes the observed/forecast flood severity for FL.W from "0" to "N" to comply with NWSI 10-1703 '''
        recommendedHazardType = hazardEvent.getHazardType()
        observedFloodSeverity = hazardEvent.get('floodSeverityObserved')
        if (recommendedHazardType == 'FL.W') and (observedFloodSeverity == NO_FLOOD_CATEGORY):
            hazardEvent.set('floodSeverityObserved',NULL_CATEGORY)
        forecastFloodSeverity = hazardEvent.get('floodSeverityForecast')
        if (recommendedHazardType == 'FL.W') and (forecastFloodSeverity == NO_FLOOD_CATEGORY):
            hazardEvent.set('floodSeverityForecast',NULL_CATEGORY)

    def filterHazards(self, mergedHazardEvents, currentEvents, dMap):
        filterType = dMap.get('forecastType')
        includeNonFloodPts = dMap.get("includePointsBelowAdvisory")

        returnEventSet = EventSet(None)

        if filterType == 'ALL':
            if includeNonFloodPts:
                return mergedHazardEvents
            else:
                for hazardEvent in mergedHazardEvents:
                    phenSig = hazardEvent.getHazardType()
                    if phenSig != 'HY.S':
                      returnEventSet.add(hazardEvent)  
                return returnEventSet

        # Create a list of all the pre-existing pointIds
        pointIds = []
        for currentEvent in currentEvents:
            pointId = currentEvent.get(POINT_ID)
            if pointId and pointId not in pointIds:
                pointIds.append(pointId)

        for hazardEvent in mergedHazardEvents:
            if hazardEvent.get(POINT_ID) in pointIds:
                # There is a pre-existing hazard with this pointId
                # automatically include any other hazards with the same pointId
                returnEventSet.add(hazardEvent)
            else:
                phenSig = hazardEvent.getHazardType()

                if phenSig == 'FL.W' and filterType == 'Warning':
                    returnEventSet.add(hazardEvent)
                elif phenSig == 'FL.A' and filterType == 'Watch':
                    returnEventSet.add(hazardEvent)
                elif phenSig == 'FL.Y' and filterType == 'Advisory':
                    returnEventSet.add(hazardEvent)
                elif phenSig == 'HY.S' and includeNonFloodPts:
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
        pointList = forecastPointDict.get(POINT)
        if isinstance(pointList, str):
            pointStrLen = len(pointList)
            pointSubString = pointList[1:pointStrLen -1]
            pointStringList = pointSubString.split(', ')
            pointStringVal0 = pointStringList[0]
            pointStringVal1 = pointStringList[1]
            pointList = [ pointStringVal0, pointStringVal1 ]
        
        coords = [float(pointList[0]), float(pointList[1])]
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

    def _getFloodValues(self, riverForecastPoint):
        '''
        @param riverForecastPoint A river forecast point
        @return: A tuple containing
            maxForecastStageFlow Maximum forecast stage or flow value
            observedStageFlow Observed stage or flow value
            floodStageFlow Flood stage or flow value
            actionStageFlow Action stage or flow value
        '''
        primaryPE = riverForecastPoint.getPrimaryPE()
        floodStageFlow = None
        if self._riverForecastUtils.isPrimaryPeStage(primaryPE):
            floodStageFlow = riverForecastPoint.getFloodStage()
            actionStageFlow = riverForecastPoint.getActionStage()
        else:
            floodStageFlow = riverForecastPoint.getFloodFlow()
            actionStageFlow = riverForecastPoint.getActionFlow()

        observedStageFlow = riverForecastPoint.getObservedCurrentValue()
        maxForecastStageFlow = riverForecastPoint.getMaximumForecastValue()
        return maxForecastStageFlow, observedStageFlow, floodStageFlow, actionStageFlow

    def isWatch(self, riverForecastPoint, warningThreshEpoch):
        (maxForecastStageFlow, observedStageFlow, floodStageFlow, actionStageFlow) = self._getFloodValues(riverForecastPoint)

        aboveFloodStageTime = riverForecastPoint.getForecastRiseAboveTime()
        if aboveFloodStageTime != MISSING_VALUE:
            aboveFloodStageTime = aboveFloodStageTime / 1000

        if (maxForecastStageFlow >= floodStageFlow) and (aboveFloodStageTime > warningThreshEpoch) and \
           (observedStageFlow < floodStageFlow):
            return True
        else:
            return False

    def isWarning(self, riverForecastPoint, warningThreshEpoch):
        (maxForecastStageFlow, observedStageFlow, floodStageFlow, actionStageFlow) = self._getFloodValues(riverForecastPoint)

        aboveFloodStageTime = riverForecastPoint.getForecastRiseAboveTime()
        if aboveFloodStageTime != MISSING_VALUE:
            aboveFloodStageTime = aboveFloodStageTime / 1000

        if ((maxForecastStageFlow >= floodStageFlow) and (aboveFloodStageTime <= warningThreshEpoch)) or \
            (observedStageFlow >= floodStageFlow):
            return True
        else:
            return False

    def isAdvisory(self, riverForecastPoint):
        (maxForecastStageFlow, observedStageFlow, floodStageFlow, actionStageFlow) = self._getFloodValues(riverForecastPoint)

        referenceValue=max(observedStageFlow, maxForecastStageFlow)
        if (referenceValue >= actionStageFlow and referenceValue < floodStageFlow):
            return True
        else:
            return False

    def endWarningIfAdvisoryRecommended(self):
        """
        Method for focal points to override.  When a FL.Y is recommended for a
        gage with an active FL.W, this method determines if the FL.W is ended.
        @return True to set the gage's active FL.W hazard to "ENDING".
                False to keep the gage's FL.W hazard active (Issued).
        """
        return True

    def recommendFallingLimbAdvisoryForWarning(self, riverForecastPoint, advisoryThreshHrs=5):
        """
        Detects a Falling Limb scenario for when a FL.W hazard exists and a
        FL.Y may be recommended. This method contains the logic that determines
        whether or not the FL.Y hazard should be recommended.
        @param riverForecastPoint: forecast point for this gage.
        @param advisoryThreshHrs: number of hours from currentTime to compare
               the fallBelowActionStageTime against.
        @return: True if the Advisory should be recommended, otherwise False.
        """
        (maxForecastStageFlow, observedStageFlow, floodStageFlow, actionStageFlow) = self._getFloodValues(riverForecastPoint)
        fallBelowActionStageTime = riverForecastPoint.getForecastFallBelowActionStageTime()

        if fallBelowActionStageTime != MISSING_VALUE:
            fallBelowActionStageTime = fallBelowActionStageTime / 1000
        else:
            # No fall below action stage time.
            # This can happen in several scenarios:
            # 1) No forecast data is available
            # 2) All forecast data is above action stage
            # 3) All forecast data is below action stage (forecast was never
            # above, so never fell below).

            # If maxForecast is below do not recommend
            # the advisory.
            if maxForecastStageFlow < actionStageFlow:
                return False
            else:
                return True

        # Determine the Advisory threshhold time
        currentTime = SimulatedTime.getSystemTime().getMillis()/1000
        advisoryThreshEpoch = currentTime + datetime.timedelta(hours=advisoryThreshHrs).total_seconds()

        # Next determine if it is a falling limb scenario
        # We already know observedStageFlow and maxForecastStageFlow
        # are below flood stage because if they were above flood stage a
        # warning or watch would be recommended.
        if observedStageFlow >= actionStageFlow:
            if maxForecastStageFlow >= actionStageFlow:
                # Check if the fall below action stage time is within the threshhold time
                if fallBelowActionStageTime < advisoryThreshEpoch:
                    # The forecast falls below action stage within the
                    # threshhold.  Do not recommend an advisory because it
                    # would only exist for a short while.
                    return False
                else:
                    # The forecast stays above action stage past the threshhold
                    # time.  Do recommend an advistory.
                    return True
                
        # Default to recommending the advisory
        return True