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
    Sep 08, 2016   17514     Robert.Blum         Changed logic so that any hazard with a pre-existing
                                                 pointID is automatically included in the output, disregarding
                                                 the filter selection.
    Oct 12, 2016   22069     Ben.Phillippe       Removed usage of riverinundation table due to geometry conflicts with RiverPro
    Aug 15, 2017   22757     Chris.Golden        Added code to display recommender results, changed logic appropriately
                                                 as per Robert Blum's changes under this same ticket in 18-Hazard_Services.
    Feb 23, 2018   28387     Chris.Golden        Changed to only process FL.* and HY.* events.

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
from HazardEventLockUtils import HazardEventLockUtils
from TextProductCommon import TextProductCommon
from HazardConstants import *
import HazardDataAccess

from RiverForecastUtils import RiverForecastUtils
from RiverForecastUtils import CATEGORY
from RiverForecastUtils import CATEGORY_NAME
from RiverForecastUtils import TIME

from RiverForecastPointHandler import pyRiverForecastPointToJavaRiverForecastPoint, javaRiverForecastPointToPyRiverForecastPoint
JUtil.registerPythonToJava(pyRiverForecastPointToJavaRiverForecastPoint)
JUtil.registerJavaToPython(javaRiverForecastPointToPyRiverForecastPoint)

from com.raytheon.uf.common.hazards.hydro import RiverStationInfo
from gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender import RiverProFloodRecommender
from com.raytheon.uf.common.time import SimulatedTime

from com.raytheon.uf.common.hazards.hydro import RiverForecastManager

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

SIGNIFICANCES = [("Warning", "W"), ("Watch", "A"), ("Advisory", "Y"), ("Statement", "S")]

# Map used to compare flood category values
FLOOD_CATEGORY_MAP = {
                      "U": (0, ""),
                      "N": (0, ""),
                      "0": (0, ""),
                      "1": (1, "minor"),
                      "2": (2, "moderate"),
                      "3": (3, "major"),
                      }

class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        self._riverProFloodRecommender = None
        self._riverForecastUtils = RiverForecastUtils()
        self._riverForecastManager = RiverForecastManager()
        self.hazardEventLockUtils = None
        self.tpc = TextProductCommon()
        
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
        metaDict["getSpatialInfoNeeded"] = False
        metaDict['includeEventTypes'] = [ "FL.A", "FL.W", "FL.Y", "HY.O", "HY.S" ]
        return metaDict

    def defineDialog(self, eventSet):
        """
        @return: A dialog definition to solicit user input before running tool
        """
        selectedPointId = None
        self.siteId = eventSet.getAttribute('localizedSiteID')
        if SELECTED_POINT_ID in eventSet.getAttributes():
            selectedPointId = eventSet.getAttribute(SELECTED_POINT_ID)

        dialogDict = {"title": "Flood Recommender"}
        
        # Only add the filter widgets when running for all points
        if selectedPointId is None:
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

        if selectedPointId is None:
            fieldDicts = [choiceFieldDict, includeNonFloodPointDict, warningThreshCutOff]
            valueDict = {"forecastType":"Warning",
                         "includePointsBelowAdvisory":False,
                         "warningThreshold": warningThreshCutOff["values"] }
            self.addForecastPointSelectionDict(fieldDicts, valueDict)
        else:
            fieldDicts = [includeNonFloodPointDict, warningThreshCutOff]
            valueDict = {"includePointsBelowAdvisory":True,
                         "warningThreshold": warningThreshCutOff["values"] }

        dialogDict["fields"] = fieldDicts
        dialogDict["valueDict"] = valueDict
        return dialogDict

    def addForecastPointSelectionDict(self, fieldDicts, valueDict):
        '''
        # Create hierarchical list of rivers and forecast points to choose from
        '''
        choices = self.riverChoices()
        if not choices:
            return
        forecastPointSelectionDict = {
                'fieldType': 'HierarchicalChoicesTree',
                'fieldName': 'forecastPointSelections',
                'label': 'Forecast Point Selections',
                'lines': 8,
                'expandHorizontally': True,
                'expandVertically': True,
                'choices': choices,
                }
        fieldDicts.append(forecastPointSelectionDict)
        selectionValues = []
        for riverDict in choices:
            values = {
                      'displayString': riverDict['displayString'],
                      'identifier': riverDict['displayString'],
                      'children': riverDict['children']
                      }
            selectionValues.append(values)
        valueDict['forecastPointSelections'] = selectionValues

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

        sessionAttributes = eventSet.getAttributes()
        sessionMap = JUtil.pyDictToJavaMap(sessionAttributes)

        caveMode = sessionAttributes.get('runMode','PRACTICE').upper()
        self.practice = True
        if caveMode == 'OPERATIONAL':
            self.practice = False

        if self.hazardEventLockUtils is None:
            self.hazardEventLockUtils = HazardEventLockUtils(self.practice)
        
        self.siteId = eventSet.getAttribute('localizedSiteID')
        self._setHazardPolygonDict(self.siteId)

        if self._riverProFloodRecommender is None:
            self._riverProFloodRecommender = RiverProFloodRecommender(self.practice)
        
        self._warningThreshold = dialogInputMap.get("warningThreshold")

        selectedPointId = self._getSelectedPoint(sessionAttributes)
        selectedPointIdList = []
        if selectedPointId is not None:
            dialogInputMap['forecastType'] = "ALL"
            selectedPointIdList.append(selectedPointId)
            
        inputMap = JUtil.pyDictToJavaMap({
                                          "includePointsBelowAdvisory": True,
                                          "siteID": self.siteId,
                                          "forecastType": dialogInputMap.get("forecastType", None),
                                          SELECTED_POINT_ID: selectedPointId
                                          })
        javaEventList = self._riverProFloodRecommender.getRecommendation(sessionMap, inputMap)

        recommendedEventSet = EventSet(javaEventList)  
        forecastPointSelections = dialogInputMap.get("forecastPointSelections")
        if forecastPointSelections:
            recommendedEventSet, selectedPointIds = self.trimEventSet(recommendedEventSet, forecastPointSelections)
            selectedPointIdList = selectedPointIds
        validEvents = self.filterEvents(eventSet)

        lockedPointIds = self.getLockedPointIds(validEvents)
        toBeDeleted, mergedEventSet = self.mergeHazardEvents(validEvents, recommendedEventSet, selectedPointIdList, lockedPointIds, dialogInputMap.get('includePointsBelowAdvisory'))
        filteredEventSet, lockedEvents = self.filterHazards(mergedEventSet, validEvents, dialogInputMap, lockedPointIds)
        self.addFloodPolygons(filteredEventSet)
        self.addAditionalRiverPointData(filteredEventSet)
        toBeDeletedIdentifiers = [toBeDeletedEvent.getEventID() for toBeDeletedEvent in toBeDeleted]
        filteredEventSet.addAttribute(DELETE_EVENT_IDENTIFIERS_KEY, toBeDeletedIdentifiers)

        # If no events are being added, modified, or deleted by the recommender,
        # notify the user of this; otherwise, give the user a list of the various
        # statuses of the changed events, other details as appropriate, etc. 
        if len(filteredEventSet.getEvents()) == 0 and len(toBeDeleted) == 0 and len(lockedEvents) == 0:
            filteredEventSet.addAttribute(RESULTS_MESSAGE_KEY, "Recommender completed. No recommended hazards.")
        else:
            critResultString, normResultString, notRecommendedResultString = self.createResultsOutput(filteredEventSet, toBeDeleted, toBeDeletedIdentifiers, lockedEvents)
            fields = []
            if critResultString:
                critWidget = {
                              "fieldType": "Text",
                              "fieldName": "critTextResults",
                              "label": "Critical Results:",
                              "editable": False,
                              "expandHorizontally": True,
                              "expandVertically": True,
                              "values": critResultString,
                              "lines": 4,
                              "visibleChars": 40,
                              }
                fields.append(critWidget)
            if normResultString:
                widget = {
                          "fieldType": "Text",
                          "fieldName": "textResults",
                          "label": "Results:",
                          "editable": False,
                          "expandHorizontally": True,
                          "expandVertically": True,
                          "values": normResultString,
                          "lines": 15,
                          "visibleChars": 40,
                          }
                fields.append(widget)
            if notRecommendedResultString:
                notRecommendedWidget = {
                                        "fieldType": "Text",
                                        "fieldName": "filteredResults",
                                        "label": "Not Recommended (due to locks):",
                                        "editable": False,
                                        "expandHorizontally": True,
                                        "expandVertically": True,
                                        "values": notRecommendedResultString,
                                        "lines": 5,
                                        }
                fields.append(notRecommendedWidget)
            filteredEventSet.addAttribute(RESULTS_DIALOG_KEY, {
                                                               "title": "Flood Recommender",
                                                               "minInitialWidth": 450,
                                                               "fields": fields
                                                               })

        return filteredEventSet

    def _getSelectedPoint(self, sessionAttributes):
        if SELECTED_POINT_ID in sessionAttributes:
            return sessionAttributes.get(SELECTED_POINT_ID)
        return None
    
    def toString(self):
        return "RiverFloodRecommender"

    def getLockedPointIds(self, currentEvents):
        
        lockedHazardIds = self.hazardEventLockUtils.getLockedEvents()
        
        # Get the pointIDs from the locked current events. Then don't recommend anything
        # new for those pointIDs.
        lockedPointIds = []
        for currentEvent in currentEvents:
            if currentEvent.getEventID() in lockedHazardIds:

                # Event is locked, get the pointID
                pointId = currentEvent.get("pointID", None)
                if pointId:
                    lockedPointIds.append(pointId)
        return lockedPointIds

    def _setHazardPolygonDict(self, siteID):
        if self._riverProFloodRecommender is None:
            self._riverProFloodRecommender = RiverProFloodRecommender(self.practice)
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
    
    def mergeHazardEvents(self, currentEvents, recommendedEventSet, selectedPointIdList, lockedPointIdList, includePointsBelowAdvisory):        

        # Remove non-issued events that are to still in the recommendations.
        if len(selectedPointIdList) == 0:
            currentEvents, mergedEvents, deleteEvents = self.removePendingOrPotentialEvents(currentEvents, recommendedEventSet, lockedPointIdList)
        else:
            mergedEvents = EventSet(None)
            deleteEvents = set()

        for recommendedEvent in recommendedEventSet:
            if recommendedEvent.get(POINT_ID) is None:
                continue

            pointID = recommendedEvent.get(POINT_ID)
            if pointID not in selectedPointIdList:
                continue

            # If flood stage is missing you cannot create a hazard, skip it
            riverForecastPoint = self._riverProFloodRecommender.getRiverForecastPoint(pointID)
            riverForecastPoint = JUtil.javaObjToPyVal(riverForecastPoint)
            if riverForecastPoint.getFloodStage() == MISSING_VALUE:
                continue
            
            self.setHazardType(recommendedEvent, riverForecastPoint)
            
            self.adjustFloodSeverity(recommendedEvent)
            
            # Look for an event that already exists
            found = False
            for currentEvent in currentEvents:
                if currentEvent.get(POINT_ID) == recommendedEvent.get(POINT_ID):
                    found = True
                    if currentEvent.getHazardType() != recommendedEvent.getHazardType():
                        if currentEvent.getStatus() in ['POTENTIAL', 'PENDING']:
                            # Never issued - delete it
                            deleteEvents.add(currentEvent)
                            mergedEvents.add(recommendedEvent)
                        elif currentEvent.getStatus() == 'ISSUED':
                            if currentEvent.getSignificance() == 'W':
                                if recommendedEvent.getHazardType() == 'FL.Y':
                                    if self.recommendFallingLimbAdvisoryForWarning(riverForecastPoint):
                                        mergedEvents.add(recommendedEvent)
                                    if self.endWarningIfAdvisoryRecommended():
                                        currentEvent.setStatus('ending')
                                        mergedEvents.add(currentEvent)
                                elif recommendedEvent.getHazardType() == 'HY.S':
                                    # Add the HY.S if needed.
                                    if includePointsBelowAdvisory:
                                        mergedEvents.add(recommendedEvent)
                                    currentEvent.setStatus('ending')
                                    mergedEvents.add(currentEvent)
                            elif currentEvent.getSignificance() in ['A', 'Y']:
                                # Watch/Advisory should be ended if any other hazard type is recommended.
                                self.addReplacementAttributes(recommendedEvent, currentEvent)
                                mergedEvents.add(recommendedEvent)
                                currentEvent.setStatus('ending')
                                mergedEvents.add(currentEvent)
                                continue
                            else:
                                # None Ended/Elapsed HY.S - Add the recommendedEvent
                                mergedEvents.add(recommendedEvent)
                        else:
                            # currentEvent is proposed
                            mergedEvents.add(recommendedEvent)
                    else:
                        # Update the current event
                        currentEvent = self.updateEventFromRecommendedEvent(currentEvent, recommendedEvent)
                        mergedEvents.add(currentEvent)
            if not found:
                mergedEvents.add(recommendedEvent)
        return list(deleteEvents), mergedEvents

    def removePendingOrPotentialEvents(self, currentEvents, recommendedEventSet, lockedPointIdList):
                    
        # Remove non-issued FL.* hazards from the currentEvents
        # that do not match a pointID in the recommendedEvents
        # but only if the recommender was not run against a specific point ID.
        newCurrentEvents = EventSet(None)
        mergedEvents = EventSet(None)
        deleteEvents = set()
        for currentEvent in currentEvents:

            changedEvent = False
            foundCurrentPointId = False
            for recommendedEvent in recommendedEventSet:
                if currentEvent.get(POINT_ID) == recommendedEvent.get(POINT_ID):
                    foundCurrentPointId = True
                    break

            if not foundCurrentPointId:

                # Cannot removed locked hazards
                if currentEvent.get("pointID", None) not in lockedPointIdList:
                    if currentEvent.getPhenomenon() == "FL":
                        if currentEvent.getStatus() in ['POTENTIAL', 'PENDING']:
                            # Not in recommended events and pending - remove it
                            deleteEvents.add(currentEvent)
                        else:
                            # Not in recommended events and issued - set to ending
                            currentEvent.setStatus('ending')
                            mergedEvents.add(currentEvent)
                        changedEvent = True

            if changedEvent is False:
                newCurrentEvents.add(currentEvent)

        return newCurrentEvents, mergedEvents, deleteEvents

    def addReplacementAttributes(self, recommendedEvent, currentEvent):
        '''
            Populates the "replaces" and "replacedBy" attributes of the events
            that are replacing each other.

            @param recommendedEvent: The Event being recommended.
            @param currentEvent: The current event with the same pointID as the
                                 recommended event.
        '''
        recHazardType = recommendedEvent.getHazardType()
        curHazardType = currentEvent.getHazardType() 
        
        # Verify that one Hazard Type actually replaces the other.
        if recHazardType in self.bridge.getHazardTypes(hazardType=curHazardType).get('replacedBy'):
            cHeadline = self.bridge.getHazardTypes(hazardType=curHazardType).get('headline')
            rHeadline = self.bridge.getHazardTypes(hazardType=recHazardType).get('headline')
            recommendedEvent.set('replaces', cHeadline)
            currentEvent.set('replacedBy', rHeadline)
                                    
    def setHazardType(self, hazardEvent, riverForecastPoint):
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

    def addAditionalRiverPointData(self, hazardEvents):
        '''
            Updates the Hazard Events with data from the RiverForecastPoints.
        '''
        if hazardEvents is not None:
            for hazardEvent in hazardEvents:
                pointID = hazardEvent.get(POINT_ID)
                riverForecastPoint = self._riverProFloodRecommender.getRiverForecastPoint(pointID)
                riverForecastPoint = JUtil.javaObjToPyVal(riverForecastPoint)

                primaryPE = riverForecastPoint.getPhysicalElement()

                riverForecastGroup = self._riverProFloodRecommender.getRiverForecastGroup(riverForecastPoint.getGroupId())
                hazardEvent.set('groupForecastPointList', self._riverForecastUtils.getGroupForecastPointNameList(riverForecastGroup, None))
                groupMaxFcstCat = riverForecastGroup.getMaxForecastCategory()
                hazardEvent.set('groupMaxForecastFloodCatName', self._riverForecastUtils.getFloodCategoryName(groupMaxFcstCat))

                riverStationInfo = self._riverProFloodRecommender.getRiverStationInfo(pointID)
                riverMile = riverStationInfo.getMile()
                hazardEvent.set("riverMile", riverMile)
                hazardEvent.set('primaryPE', primaryPE)
                hazardEvent.set('proximity', riverForecastPoint.getProximity())
                hazardEvent.set('riverPointName', riverForecastPoint.getName())
                hazardEvent.set('floodStage', riverForecastPoint.getFloodStage())
                hazardEvent.set('floodFlow', riverForecastPoint.getFloodFlow())
                hazardEvent.set('obsRiseAboveFSTime_ms', riverForecastPoint.getObservedRiseAboveTime())
                hazardEvent.set('obsFallBelowFSTime_ms', riverForecastPoint.getObservedFallBelowTime())
                hazardEvent.set('forecastRiseAboveFSTime_ms', riverForecastPoint.getForecastRiseAboveTime())
                hazardEvent.set('forecastFallBelowFSTime_ms', riverForecastPoint.getForecastFallBelowTime())

                hazardEvent.set('observedCategory', self._riverForecastUtils.getObservedLevel(riverForecastPoint, CATEGORY))
                hazardEvent.set('observedCategoryName', self._riverForecastUtils.getObservedLevel(riverForecastPoint, CATEGORY_NAME))
                hazardEvent.set('maxFcstCategory', riverForecastPoint.getMaximumForecastCategory())
                hazardEvent.set('maxFcstCategoryName', self._riverForecastUtils.getMaximumForecastLevel(riverForecastPoint, primaryPE, CATEGORY_NAME))
                hazardEvent.set('observedTime_ms', self._riverForecastUtils.getObservedLevel(riverForecastPoint, TIME))
                hazardEvent.set('stageFlowUnits', self._riverForecastUtils.getStageFlowUnits(primaryPE))
                hazardEvent.set('stageTrend', self._riverForecastUtils.getStageTrend(riverForecastPoint))
                hazardEvent.set('stageFlowName', self._riverForecastUtils.getStageFlowName(primaryPE))
                hazardEvent.set('impactCompUnits', self._riverForecastUtils.getStageFlowUnits(primaryPE))

                stageCodePair = riverForecastPoint.getMaximum24HourObservedStage()
                tempStageString = str (stageCodePair.getFirst() )
                hazardEvent.set('max24HourObservedStage', float(tempStageString))

                observedCurrentIndex = riverForecastPoint.getObservedCurrentIndex()
                shefObserved = self._riverForecastManager.getSHEFObserved(riverForecastPoint.toJavaObj(), observedCurrentIndex)
                hazardEvent.set('observedStage', MISSING_VALUE)
                if shefObserved is not None:
                    observedStage = shefObserved.getValue() # Or flow
                    hazardEvent.set('observedStage', observedStage)

    def adjustFloodSeverity(self, hazardEvent):
        ''' Changes the observed/forecast flood severity for FL.W from "0" to "N" to comply with NWSI 10-1703 '''
        recommendedHazardType = hazardEvent.getHazardType()
        observedFloodSeverity = hazardEvent.get('floodSeverityObserved')
        if (recommendedHazardType == 'FL.W') and (observedFloodSeverity == NO_FLOOD_CATEGORY):
            hazardEvent.set('floodSeverityObserved',NULL_CATEGORY)
        forecastFloodSeverity = hazardEvent.get('floodSeverityForecast')
        if (recommendedHazardType == 'FL.W') and (forecastFloodSeverity == NO_FLOOD_CATEGORY):
            hazardEvent.set('floodSeverityForecast',NULL_CATEGORY)

    def filterHazards(self, mergedHazardEvents, currentEvents, dMap, lockedPointIds):
        filterType = dMap.get('forecastType')
        includeNonFloodPts = dMap.get("includePointsBelowAdvisory")

        returnEventSet = EventSet(None)

        if filterType == 'ALL':
            if includeNonFloodPts:
                return self.filterLockedPointIds(mergedHazardEvents, lockedPointIds)
            else:
                for hazardEvent in mergedHazardEvents:
                    phenSig = hazardEvent.getHazardType()
                    if phenSig != 'HY.S':
                      returnEventSet.add(hazardEvent)  
                return self.filterLockedPointIds(returnEventSet, lockedPointIds)

        for hazardEvent in mergedHazardEvents:
            phenSig = hazardEvent.getHazardType()
            if phenSig == 'HY.S' and includeNonFloodPts:
                returnEventSet.add(hazardEvent)
                continue
            elif phenSig == 'HY.S':
                continue
            
            if phenSig == 'FL.W' and filterType == 'Warning':
                returnEventSet.add(hazardEvent)
            elif phenSig == 'FL.A' and filterType == 'Watch':
                returnEventSet.add(hazardEvent)
            elif phenSig == 'FL.Y' and filterType == 'Advisory':
                returnEventSet.add(hazardEvent)
            else:
                pass

        return self.filterLockedPointIds(returnEventSet, lockedPointIds)

    def filterLockedPointIds(self, eventSet, lockedPointIds):
        '''
        Remove any hazard from the eventSet that has its' pointID in the 
        list of locked point IDs, and return the resulting event set, as
        well as a list of the events that were found to have their point
        IDs locked.
        '''
        filteredEventSet = EventSet(None)
        lockedEvents = []
        for event in eventSet:
            pointID = event.get("pointID", "")
            if pointID not in lockedPointIds:
                filteredEventSet.add(event);
            else:
                lockedEvents.append(event)
        return filteredEventSet, lockedEvents

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
        primaryPE = riverForecastPoint.getPhysicalElement()
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

    def updateEventFromRecommendedEvent(self, currentEvent, recommendedEvent):
        '''
            Updates a pre-existing event with data from a recommended event.
            This method assumes that the two events are for the same pointID and
            all the data from the recommended event applies to the current event.
        '''
        # Attributes to be updated
        attributes = ['riseAbove', 'crest', 'fallBelow', 'currentStage', 'currentStageTime',
                      'crestStage', 'floodRecord', 'floodSeverityObserved', 'floodSeverityForecast',
                      'impactsCurObsField','impactsMaxFcstField', 'crestsMaxFcstField', 
                      'crestsCurObsField', 'obsCrestStage', 'forecastCrestStage', 'observedCrestTime', 
                      'forecastCrestTime', 'maxForecastStage', 'maxForecastTime']

        for attribute in attributes:
            # Save off the previous severities so we can notify the user when they change
            # via the results dialog
            if attribute in ['floodSeverityObserved', 'floodSeverityForecast']:
                prevKey = "prevF" + attribute[1:]
                currentEvent.set(prevKey, currentEvent.get(attribute))
            currentEvent.set(attribute, recommendedEvent.get(attribute, MISSING_VALUE))
        currentEndTime = currentEvent.getEndTime()
        newEndTime = recommendedEvent.getEndTime()
        if currentEndTime < newEndTime and currentEvent.getStatus() == 'ENDING':
            # endTime changed - move back to issued
            currentEvent.setStatus('issued')
        currentEvent.setEndTime(newEndTime)
        return currentEvent

    def createResultsOutput(self, eventSet, deletedEvents, deletedIdentifiers, lockedEvents):
        '''
            Returns three formatted strings to be displayed in the results dialog,
            the first for critical updates, the second for normal updates, and the
            third for events that were not updated due to their being locked.
        '''
        # Return Strings
        critReturnString = ""
        normReturnString = ""

        eventResultsList = []
        for event in eventSet:
            eventResultsList.append(self.getHazardEventResultsOutput(event, False))
        for event in deletedEvents:
            eventResultsList.append(self.getHazardEventResultsOutput(event, True))

        for label, sig in SIGNIFICANCES:
            # Get all the hazard tuples for this sig
            eventTuples = []
            for eventResults in eventResultsList:
                if eventResults[0].getSignificance() == sig:
                    eventTuples.append(eventResults)
            if eventTuples:
                critResult, result = self.createResultsOutputForSig(eventTuples, deletedIdentifiers)
                if critResult:
                    critReturnString += critResult
                if result:
                    header = label + " Hazards:\n"
                    normReturnString += header + result
            
        lockedReturnString = ""
        lockedStringsList = []        
        for event in lockedEvents:
            status = event.getStatus()
            phensig = event.getPhensig()
            pointID = event.get("pointID", "")
            if status in ["POTENTIAL", "ENDING"]:
                resultText = "{} - {} {}".format(pointID, status.lower(), phensig)
            else: # ISSUED
                resultText = "{} - updated {}".format(pointID, phensig)
            lockedStringsList.append(resultText)
        if lockedStringsList:
            lockedStringsList.sort()
            lockedReturnString = "\n".join(lockedStringsList)

        return critReturnString, normReturnString, lockedReturnString

    def createResultsOutputForSig(self, eventTuples, deletedIdentifiers):
        '''
            Returns 2 formatted strings for a specific Hazard Significance.
            One for critical updates and another containing the remaining updates.
        '''
        output = ""
        critOutput = ""
        if eventTuples:
            # Sort the hazard tuples by status
            sortedTuples = self.sortTuplesByStatus(eventTuples, deletedIdentifiers)
            for tupleList, statusLabel in sortedTuples:
                if tupleList:
                    # Add status header
                    output += "   " + statusLabel + ":\n"
                    for eventTuple in tupleList:
                        hazardID = eventTuple[2]
                        critString = eventTuple[1]
                        output += "      " + hazardID
                        if critString:
                            critOutput += hazardID + critString
                    output += "\n"
        return critOutput, output

    def sortTuplesByStatus(self, eventTuples, deletedIdentifiers):
        potentialHazards = []
        pendingHazards = []
        issuedHazards = []
        endingHazards = []
        deletedHazards = []

        for eventTuple in eventTuples:
            status = eventTuple[0].getStatus()
            if status == "POTENTIAL":
                potentialHazards.append(eventTuple)
            elif status == "PENDING":
                pendingHazards.append(eventTuple)
            elif status == "ISSUED":
                issuedHazards.append(eventTuple)
            elif status == "ENDING":
                endingHazards.append(eventTuple)
            elif eventTuple[0].getEventID() in deletedIdentifiers:
                deletedHazards.append(eventTuple)

        returnList = [(potentialHazards, "Potential"), (pendingHazards, "Pending"), (issuedHazards, "Issued - Update as needed"),\
                (endingHazards, "Ending"), (deletedHazards, "Deleted")]

        # Remove empty lists
        returnList = [statusTuple for statusTuple in returnList if statusTuple[0]]

        # Sort on the Hazard Event Label
        for statusTuple in returnList:
            statusTuple[0].sort(key=lambda tup: tup[2])
        return returnList

    def getHazardEventResultsOutput(self, event, deleted):
        '''
            Returns a tuple that contains text strings that can be used in the recommenders
            results dialog. Currently there are 2 types of results displayed critical and 
            normal that is just a hazard id consisting of pointID, eventID, and hazard type.
            Note not all hazard will have critical results that need to be reported.
        '''
        # For all events display the pointID, eventID, and hazardType
        pointID = event.get("pointID", "")
        locationName = event.get("name", "")
        riverName = event.get("streamName", "")
        issueTime = event.get("issueTime", "")
        if issueTime:
            issueTime = self.tpc.getFormattedTime(issueTime, format='%H:%MZ %d-%b-%y', stripLeading=False)

        labelFields = [pointID, locationName, riverName, issueTime]

        # Remove any Nones or empty strings
        filter(None, labelFields)

        # Construct the output string
        hazardID = "-".join(labelFields) + "\n"

        # Display additional information for issued hazards that were updated.
        # For example whether or not the flood category increased.
        criticalString = ""
        if deleted:
            status = "DELETED"
        else:
            status = event.getStatus()
            if status == "ISSUED":
                prevObsCat = event.get("prevFloodSeverityObserved", "N")
                prevfcstCat = event.get("prevFloodSeverityForecast", "N")
                obsCat = event.get("floodSeverityObserved", "N")
                fcstCat = event.get("floodSeverityForecast", "N")
    
                if self.compareFloodCategories(prevObsCat, obsCat) == -1:
                    # obs category increased
                    criticalString += "   *Observed flood category increased to " + FLOOD_CATEGORY_MAP.get(obsCat)[1] + "\n"
                if self.compareFloodCategories(prevfcstCat, fcstCat) == -1:
                    # fcst category increased
                    criticalString += "   *Forecast flood category increased to " + FLOOD_CATEGORY_MAP.get(fcstCat)[1] + "\n"

        # return the tuple output
        return (event, criticalString, hazardID)

    def filterEvents(self, eventSet):
        '''
            This method should filter out Elapsed and Ended events 
            and events not used by this recommender.
        '''
        # Filter out ELAPSED and ENDED hazards
        events = []
        for event in eventSet:
            if event.getStatus() not in ['ELAPSED', 'ENDED']:
                if event.getPhenomenon() in [ "FL", "HY" ]:
                    events.append(event)
        
        return events

    def compareFloodCategories(self, cat1, cat2):
        value1 = FLOOD_CATEGORY_MAP.get(cat1)[0]
        value2 = FLOOD_CATEGORY_MAP.get(cat2)[0]
        if value1 == value2:
            return 0
        elif value1 < value2:
            return -1
        else:
            return 1

    def riverChoices(self):
        #  OVERRIDE THIS METHOD to include a hierarchical list of rivers and
        #    forecast points to choose from
        riverMetadata = self._riverForecastManager.getRiverMetadata(self.siteId)
        dataList = []
        for group in riverMetadata:
            returnDict = {}
            gages = []
            for forecastPoint in group.getRiverGages():
                gages.append(forecastPoint.getLid())
            
            returnDict['displayString'] = group.getGroup()
            returnDict['children'] = gages
            dataList.append(returnDict)
            
        return dataList

    def trimEventSet(self, recommendedEventSet, forecastPointSelections):
        '''
         Example forecastPointSelections:
         [
         {'displayString': 'Sabine River', 'identifier': 'Sabine River', 'children': ['BRVT2', 'BWRT2', 'BKLL1']},
         {'displayString': 'Calcasieu River', 'identifier': 'Calcasieu River', 'children': ['LKCL1', 'OKDL1', 'KDRL1']}
         ]
        '''
        # Create list of gages
        selectedGages = []
        for choiceDict in forecastPointSelections:
            for fcstPoint in choiceDict['children']:
                selectedGages.append(fcstPoint)
        
        selectedEventSet = EventSet(None)
        for event in recommendedEventSet:
            pointID = event.get('pointID')
            if pointID in selectedGages:
                selectedEventSet.add(event)
        
        return selectedEventSet, selectedGages
    
