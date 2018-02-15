# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
#
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
#
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
#
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
# #


import JUtil

from RiverForecastPointHandler import pyRiverForecastPointToJavaRiverForecastPoint, javaRiverForecastPointToPyRiverForecastPoint
JUtil.registerPythonToJava(pyRiverForecastPointToJavaRiverForecastPoint)
JUtil.registerJavaToPython(javaRiverForecastPointToPyRiverForecastPoint)

from RiverForecastGroupHandler import pyRiverForecastGroupToJavaRiverForecastGroup, javaRiverForecastGroupToPyRiverForecastGroup
JUtil.registerPythonToJava(pyRiverForecastGroupToJavaRiverForecastGroup)
JUtil.registerJavaToPython(javaRiverForecastGroupToPyRiverForecastGroup)

from HydrographHandler import pyHydrographToJavaHydrograph, javaHydrographToPyHydrograph
JUtil.registerPythonToJava(pyHydrographToJavaHydrograph)
JUtil.registerJavaToPython(javaHydrographToPyHydrograph)

from com.raytheon.uf.common.time import SimulatedTime
from com.raytheon.uf.common.hazards.hydro import RiverHydroConstants
from com.raytheon.uf.common.hazards.hydro import RiverForecastManager

from sets import Set

import math
import JUtil
import JUtilHandler
import sys
import datetime
import time

from HazardConstants import MISSING_VALUE

CATEGORY = 'CATEGORY'
TIME = 'TIME'
CATEGORY_NAME = 'CATEGORYNAME'
VALUE = 'VALUE'

MAJOR = 'MAJOR'
MINOR = 'MINOR'
MODERATE = 'MODERATE'
RECORD = 'RECORD'

MISSING_STAGE_DATE = MISSING_VALUE, datetime.datetime.utcfromtimestamp(0)
MISSING_SHEF_QUALITY_CODE = RiverHydroConstants.MISSING_SHEF_QUALITY_CODE
PE_H = 'H'
PE_Q = 'Q'

class RiverForecastUtils(object):

    FLOOD_CATEGORY_VALUE_DICT = {-1: 'Unknown',
                                      0: 'NonFlood',
                                      1: 'Minor',
                                      2: 'Moderate',
                                      3: 'Major',
                                      4: 'Record'}

    TREND_VALUE_DESCRIPTION_DICT = {  'RISE': 'rising',
                                          'UNCHANGED': 'steady',
                                          'FALL': 'falling',
                                          'MISSING': 'unknown'}

    # Search Type Options for both both impacts and crest
    CLOSEST_IN_STAGE_FLOW_WINDOW = "Closest in Stage/Flow Window"
    HIGHEST_IN_STAGE_FLOW_WINDOW = "Highest in Stage/Flow Window"

    # Search Type Options for crest only
    CLOSEST_IN_STAGE_FLOW_YEAR_WINDOW = "Closest in Stage/Flow, Year Window"
    RECENT_IN_STAGE_FLOW_YEAR_WINDOW = "Recent in Stage/Flow, Year Window"
    RECENT_IN_STAGE_FLOW_WINDOW = "Recent in Stage/Flow Window"

    def __init__(self):
        self._riverForecastManager = RiverForecastManager()
     ###############################################################
     #
     # Forecast Group Template Variables
     #
     ###############################################################

    def getGroupForecastPointNameList(self, riverForecastGroup=None,
                                      riverForecastPointList=None):

        forecastPointNameList = []
        if riverForecastGroup is not None:
            riverForecastPointList = riverForecastGroup.getForecastPointList()

        if riverForecastPointList is not None:
            forecastPointList = JUtil.javaObjToPyVal(riverForecastPointList)

        forecastPointNameList = []
        for forecastPoint in forecastPointList:
            forecastPointNameList.append(forecastPoint.getName())

        return ','.join(forecastPointNameList)

    def getFloodCategoryName(self, categoryValue):
        return self.FLOOD_CATEGORY_VALUE_DICT.get(categoryValue)



    def getFloodLevel(self, primaryPE,  riverForecastPoint):
        # getFloodStage and getFloodFlow
        # use the category to get the different values
        # TODO, use getFloodStage for reference

        if primaryPE[0] == PE_H :
            # get flood stage
            return riverForecastPoint.getFloodStage()
        else :
            # get flood flow
            return riverForecastPoint.getFloodFlow()


    def getStageFlowName(self, primaryPE):
        if primaryPE[0] == PE_Q:
            return 'flow'
        else:
            return 'stage'

    def isPrimaryPeStage(self, primaryPE):
        if primaryPE[0] == PE_H:
           return True
        else:
           return False

    def isPrimaryPeFlow(self, primaryPE):
        if primaryPE[0] == PE_Q:
           return True
        else:
           return False

    ###############################################################
    #
    # Forecast Point Reference Template Variables
    #
    ###############################################################

    def encodeStageDate(self, stageDateTuple) :
        retstr = "%.2f"%stageDateTuple[0]+" "
        retstr += "%2.2d"%(stageDateTuple[1].month)+"/"
        retstr += "%2.2d"%(stageDateTuple[1].day)+"/"
        retstr += "%4.4d"%(stageDateTuple[1].year)
        return retstr


    def getImpacts(self, impactDataList, primaryPE, riverForecastPoint, filters=None):
        '''
        @param impactDataList Impacts for this point.
        @param primaryPE Primary physical element for the river forecast point.
        @param riverForecastPoint: The river forecast point.
        @return: The characterization (value,date,trend), physicalElement, description
            associated with the flood-of-record for this forecast point.
        '''

        if filters is None :
            return [], [], []

        listTuple = self.createTupleList(impactDataList, 'Impacts')

        characterizations = []
        physicalElements = []
        descriptions = []

        charIdx = 0;    # characterization index into impactData
        peIdx = 1;      # physical element index into impactData
        descIdx = 2;    # description index into impactData

        for index, val in enumerate(listTuple) :
            stageOrFlow, impact = val

            impactData = impact.split("||")
            if len(impactData) != 3 :
                continue

            characterizations.append(("%.2f" % stageOrFlow) + impactData[charIdx])
            physicalElements.append(impactData[peIdx])
            descriptions.append(impactData[descIdx])

        return characterizations, physicalElements, descriptions


    def createTupleList(self, plist, impactsOrCrests):
        if len(plist)==2:
            if isinstance(plist[0],bool) and isinstance(plist[1],list):
                plist = plist[1]

        listTuple = []
        for pair in plist :
            first = JUtil.javaObjToPyVal(pair.getFirst())
            # Stage is a float value, flow is an int value
            if not isinstance(first, float) and not isinstance(first, int):
                continue
            second = JUtil.javaObjToPyVal(pair.getSecond())

            if not isinstance(second, str) and impactsOrCrests == 'Impacts':
                continue
            elif not isinstance(second, datetime.datetime) and impactsOrCrests == 'Crests':
                continue
            pytuple = ( first, second )
            listTuple.append(pytuple)
        return listTuple

    def getHistoricalCrest(self, riverForecastPoint, primaryPE, filters=None) :
        '''
        Emulates the functionality of the <HistCrestStg>, <HistCrestDate> template variable.
        e.g. 44.4

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                                         load_variableValue.c - load_pcc_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return: The stage, date associated with the flood-of-record for this
                                   forecast point.
        '''
        if filters is None :
            return None, None

        crestHistoryList = [ ]
        if primaryPE.startswith(PE_Q) :
            crestHistoryList = riverForecastPoint.getFlowCrestHistory()
        elif primaryPE.startswith(PE_H) :
            crestHistoryList = riverForecastPoint.getStageCrestHistory()

        listTuple, index = self.getHistoricalCrestIndex(crestHistoryList, filters, primaryPE, riverForecastPoint)

        # Index value need to be compared to None rather than submitted to a
        # pure boolean test, otherwise index value of 0 (first value in list)
        # will fail to get picked up.
        stageDate = MISSING_STAGE_DATE
        if index != None:
            stageDate = listTuple[index]

        # Sort by descending crest value
        n = len(listTuple)-1
        t = 0
        while t<n :
            tt = t+1
            while tt<=n :
                if listTuple[t][0]<listTuple[tt][0] :
                    lt = listTuple[t]
                    listTuple[t] = listTuple[tt]
                    listTuple[tt] = lt
                tt += 1
            t += 1

        choiceList = []
        for oneTuple in listTuple :
            choiceList.append(self.encodeStageDate(oneTuple))
        return ( self.encodeStageDate( stageDate ), choiceList )

    def getHistoricalCrestIndex(self, plist, filters, primaryPhysicalElement, riverForecastPoint):
        currTime = self.getCurrentTimeMS()
        listTuple = self.createTupleList(plist, "Crests")

        referenceType = filters['Reference Type']
        depthBelowFloodStage = filters.get('Depth Below Flood Stage', 0.0)
        flowWindowLower = filters.get('Flow Window Lower', 0.0)
        flowWindowUpper = filters.get('Flow Window Upper', 0.0)
        stageWindowLower = filters.get('Stage Window Lower', 0.0)
        stageWindowUpper = filters.get('Stage Window Upper', 0.0)
        yearLookBack = filters.get('Year Lookback')

        searchType = filters['Search Type']
        if primaryPhysicalElement.startswith(PE_Q) :
            flowStageWindow = filters['Flow Stage Window']
        else :
            flowStageWindow = 0

        if primaryPhysicalElement.startswith(PE_H) :
            floodStage = float(riverForecastPoint.getFloodStage())
        else:
            floodStage = float(riverForecastPoint.getFloodFlow())

        currentDate = 0
        if referenceType == 'Max Forecast' :
            referenceValue = riverForecastPoint.getMaximumForecastValue()
            currentDate = riverForecastPoint.getMaximumForecastTime()
        elif referenceType == 'Current Observed' :
            referenceValue = riverForecastPoint.getObservedCurrentValue()
            currentDate = riverForecastPoint.getObservedCurrentTime()
        else :
            maxFcst = riverForecastPoint.getMaximumForecastValue()
            if maxFcst is None:
                maxFcst = float(0.0)
            maxObs = riverForecastPoint.getObservedCurrentValue()
            if maxObs is None:
                maxObs = float(0.0)
            if maxFcst > maxObs :
                referenceValue = maxFcst
                currentDate = riverForecastPoint.getMaximumForecastTime()
            else:
                referenceValue = maxObs
                currentDate = riverForecastPoint.getObservedCurrentTime()
        if referenceValue is None:
            referenceValue = float(0.0)

        minDateDate = None
        curDateDate = datetime.datetime.utcfromtimestamp(currentDate / 1000)
        minYear = curDateDate.year+yearLookBack
        if searchType.find("Year Window")<0 or minYear<datetime.MINYEAR :
            minYear = datetime.MINYEAR
        minDateDate = datetime.datetime(minYear, curDateDate.month, curDateDate.day)

        # Flow offsets are all in percent, stage offsets are all in feet.
        if primaryPhysicalElement.startswith('Q') :
            flowWindowLower = referenceValue * float(flowWindowLower) * .01
            flowWindowUpper = referenceValue * float(flowWindowUpper) * .01
            flowStageWindow = floodStage * float(flowStageWindow) * .01
            lowerBound = referenceValue - math.fabs(flowWindowLower)
            upperBound = referenceValue + math.fabs(flowWindowUpper)
            floodValueStage = floodStage - math.fabs(flowStageWindow)
        else :
            if stageWindowLower is None:
                stageWindowLower = float(0.0)
            if stageWindowUpper is None:
                stageWindowUpper = float(0.0)
            if depthBelowFloodStage is None:
                depthBelowFloodStage = float(0.0)
            lowerBound = referenceValue - math.fabs(stageWindowLower)
            upperBound = referenceValue + math.fabs(stageWindowUpper)
            floodValueStage = floodStage - math.fabs(depthBelowFloodStage)

        maximumValue = MISSING_VALUE
        differenceValue = math.fabs(MISSING_VALUE) # start with a huge value
        recentDate = minDateDate
        returnIndex = None

        for index, val in enumerate(listTuple) :
            stageOrFlow, impactOrDate = val

            # Check if below max depth below flood stage value
            if stageOrFlow < floodValueStage :
                continue

            # Check if outside stage/flow window
            if stageOrFlow<lowerBound or stageOrFlow>upperBound :
                continue

            # Check if outside the year lookback window
            if searchType == self.CLOSEST_IN_STAGE_FLOW_YEAR_WINDOW or \
                searchType == self.RECENT_IN_STAGE_FLOW_YEAR_WINDOW:
                if impactOrDate<minDateDate:
                        continue

            if searchType == self.HIGHEST_IN_STAGE_FLOW_WINDOW:
                if stageOrFlow > maximumValue :
                    maximumValue = stageOrFlow
                    returnIndex = index
            elif searchType == self.CLOSEST_IN_STAGE_FLOW_WINDOW or \
                searchType == self.CLOSEST_IN_STAGE_FLOW_YEAR_WINDOW:
                diffVal = math.fabs(referenceValue - stageOrFlow )
                if diffVal < differenceValue :
                    differenceValue = diffVal
                    returnIndex = index

            elif searchType == self.RECENT_IN_STAGE_FLOW_WINDOW or \
                searchType == self.RECENT_IN_STAGE_FLOW_YEAR_WINDOW:
                if impactOrDate > recentDate :
                    recentDate = impactOrDate
                    returnIndex = index

        return listTuple, returnIndex

    ###############################################################
    #
    # Forecast Point Stage Template Variables
    #
    ###############################################################

    def getObservedLevel(self, riverForecastPoint, type=VALUE):
        '''
        Emulates the functionality of the <ObsStg>,<ObsCat>,<ObsTime>,<ObsCatName> template variable.
        e.g. 35

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return: The current observed river stage.
        '''
        if type == TIME :
            observedTime = riverForecastPoint.getObservedCurrentTime()
            return observedTime
        elif type == CATEGORY :
            return riverForecastPoint.getCurrentObservationCategory()
        elif type == CATEGORY_NAME :
            category = riverForecastPoint.getCurrentObservationCategory()
            return self.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        else :
            value = riverForecastPoint.getObservedCurrentValue()

        return value

    def getObservedCategoryName(self, riverForecastPoint):
        '''
        Emulates the functionality of the <ObsCatName> template variable.
        e.g. Minor

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return: The category name of the current stage observation.
        '''
        category = riverForecastPoint.getCurrentObservationCategory()
        categoryName = self.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        return categoryName

    def getMaximumForecastLevel(self, riverForecastPoint, primaryPE, type=VALUE):
        '''
        Emulates the functionality of the <MaxFcstStg>, <MaxFcstCat>, <MaxFcstCatName>, <MaxFcstTime> template variable.
        e.g. 35

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast stage for this river forecast point.
        '''
        if type == CATEGORY :
            return riverForecastPoint.getMaximumForecastCategory()
        elif type == CATEGORY_NAME :
            category = riverForecastPoint.getMaximumForecastCategory()
            return self.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        elif type == TIME :
            maximumForecastTime = riverForecastPoint.getMaximumForecastTime()
            return maximumForecastTime
        else :
            maximumForecastLevel = MISSING_VALUE
            if primaryPE.startswith(PE_H) :
                maximumForecastLevel = riverForecastPoint.getMaximumForecastValue()
            elif primaryPE.startswith(PE_Q):
                pass
            return maximumForecastLevel

    def getMaximumForecastCatName(self, riverForecastPoint):
        '''
        Emulates the functionality of the <MaxFcstCatName> template variable.
        e.g. Minor

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood category name for this river forecast point.
        '''
        category = riverForecastPoint.getMaximumForecastCategory()
        categoryName = self.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        return categoryName

    def getMaximumForecastTime(self, riverForecastPoint):
        '''
        Emulates the functionality of the <MaxFcstTime> template variable.
        e.g. "1/22 12:00"

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast time for this river forecast point in milliseconds.
        '''
        maximumForecastTime = MISSING_VALUE
        maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()

        if maximumForecastIndex != MISSING_VALUE:
            pointID = riverForecastPoint.getLid()
            shefForecast= riverProFlood.getSHEFForecast(pointID, maximumForecastIndex)
            if shefForecast is not None:
                maximumForecastTime = shefForecast.getValidTime()

        return maximumForecastTime

    def getMaximumObservedForecastCategoryName(self, riverForecastPoint):
        '''
        Emulates the functionality of the <OMFCatName> template variable.
        e.g. Minor

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast category name for this river forecast point.
        '''
        maximumObservedForecastCategory = riverForecastPoint.getMaximumObservedForecastCategory()
        categoryName = self.FLOOD_CATEGORY_VALUE_DICT.get(maximumObservedForecastCategory, 'UNKNOWN')
        return categoryName

    def getStageTrend(self, riverForecastPoint):
        '''
        Emulates the functionality of the <StgTrend> template variable.
        e.g. Rising

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return:  The stage trend for this river forecast point.
        '''
        trend = riverForecastPoint.getTrend()
        trendPhrase = self.TREND_VALUE_DESCRIPTION_DICT.get(str(trend), 'UNKNOWN')
        return trendPhrase

    def getForecastCrest(self, riverForecastPoint):
        primaryPE = riverForecastPoint.getPrimaryPhysicalElement()

        crestTimeOrValue = MISSING_VALUE
        if primaryPE[0] == 'H' or primaryPE[0] == 'h':
            crestTimeOrValue = riverForecastPoint.getForecastCrestValue()
        elif primaryPE[0] == 'F' or primaryPE[0] == 'f' :
            crestTimeOrValue = riverForecastPoint.getForecastCrestTime()

        if crestTimeOrValue == crestTimeOrValue:
            crestTimeOrValue = None

        return crestTimeOrValue

    def getForecastCrestStage(self, riverForecastPoint):
        '''
        Emulates the functionality of the <FcstCrestStg> template variable.
        e.g. 35

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest stage for this river forecast point.
        '''
        returnVal = riverForecastPoint.getForecastCrestValue()
        if returnVal == MISSING_VALUE:
            returnVal = None
        return returnVal

    def getAbsoluteObservedFloodStageDeparture(self, riverForecastPoint):
        '''
        Emulates the functionality of the <ObsFSDepartureA> template variable.
        e.g. 7

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the observed departure from flood stage.
        '''
        return math.fabs(riverForecastPoint.getObservedDepatureFromFloodStage())

    def getAbsoluteForecastFloodStageDeparture(self, riverForecastPoint):
        '''
        Emulates the functionality of the <FcstFSDepartureA> template variable.
        e.g. 7

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the forecast departure from flood stage.
        '''
        return math.fabs(riverForecastPoint.getForecastDepartureFromFloodStage())

    def getStageFlowUnits(self, primaryPE):
        '''
        Emulates the functionality of the <ImpCompUnits> template variable.
        e.g. feet

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()

        @param primaryPE: The physical element
        @return: The unit (feet or cfs) associated with the stage or flow
        '''
        if primaryPE[0] == PE_Q:
            return 'cfs'
        else:
            return 'feet'

    def getCurrentTimeMS(self):
        currenTime = SimulatedTime.getSystemTime().getMillis()
        return currenTime

    def _convertToMS(self, t):
         if t:
             return t.getTime()
         else:
             return t

    def getRiverForecastPointFromRiverForecastGroup(self, pointID, riverForecastGroup):
        '''
        Retrieve a RiverForecastPoint from a RiverForecastGroup.
        The RiverForecastGroup must be from a Deep query
        '''
        if pointID is not None and riverForecastGroup is not None:
            riverForecastPointList = riverForecastGroup.getForecastPointList()
            if riverForecastPointList is not None:
                for riverForecastPoint in riverForecastPointList:
                    riverForecastPointID = riverForecastPoint.getLid()
                    if pointID == riverForecastPointID:
                        return JUtil.javaObjToPyVal(riverForecastPoint)
        return None

    def getAreaInundationCoordinates(self):
        '''
        Retrieve a Map of LID (Gauge Id) to a string containing Lat/Lon coords
        for area Flood Inundation.
        
        @return: A Map with the gauge 'lid' as the key and a string of latitude and longitude values as the value.
        '''
        return self._riverForecastManager.getAreaInundationCoordinates()
    
    def getAreaInundationCoordinatesForLid(self, lid):
        '''
        Retrieve Lat/Lon Flood Inundation for a specific River Forecast Point LID
        
        @param lid: River forecast point identifier
        @return: A string of latitude and longitude values.
        '''
        return self._riverForecastManager.getAreaInundationCoordinates(lid)
    
    def getHydrologicServiceAreaIdForGage(self, lid):
        '''
        Retrieve the Hydrologic Service Area Id for a specific River Forecast Point LID
        
        @param lid: River forecast point identifier
        
        @return: The retrieved Hydrologic Service Area (HSA) ID
        '''
        return self._riverForecastManager.getHydrologicServiceAreaIdForGage(lid)
    
    def getAllRiverForecastGroupList(self, isSubDataNeeded):
        '''
        Retrieve a List of ALL RiverForecastGroup data. This is a variable depth query.
        
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        
        @return: List containing all RiverForecastGroup Data
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getAllRiverForecastGroupList(isSubDataNeeded))
    
    def getRiverForecastGroup(self, groupId, isSubDataNeeded):
        '''
        Retrieve a River Forecast Group for a given River Forecast Group Identifier
        value. This is a variable depth query.
        
        @param groupId: RiverForecastGroup Group Identifier
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        
        @return: Retrieved RiverForecastGroup object
        '''
        
        return JUtil.javaObjToPyVal(self._riverForecastManager.getRiverForecastGroup(groupId, isSubDataNeeded))
    
    def getRiverForecastGroupForRiverForecastPoint(self, lid, isSubDataNeeded):
        '''
        Retrieve a River Forecast Group parent for a RiverForecastPoint LID value. This is a Variable Depth query.
        
        @param lid: RiverForecastPoint Identifier
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        
        @return: Retrieved RiverForecastGroup object
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getRiverForecastGroupForRiverForecastPoint(lid, isSubDataNeeded))
    
    def getHydrologicServiceAreaIdList(self):
        '''
        Retrieve a List of ALL Hydrologic Service Area Id values. This is a SHALLOW QUERY.
        
        @return: List containing all Hydrologic Service Area IDs.
        '''
        return self._riverForecastManager.getHydrologicServiceAreaIdList()
    
    def getHsaRiverForecastPointList(self, hsaId, isSubDataNeeded):
        '''
        Retrieve a List of RiverForecastPoint object for a given Hydrologic Service
        Area (HSA) value.
        
        @param hsaId: Hydrological Service Area Identifier
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        @return: List containing RiverForecastPoint data objects
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getHsaRiverForecastPointList(hsaId, isSubDataNeeded))
    
    def getHsaRiverForecastGroupList(self, hsaId, isSubDataNeeded):
        '''
        Get a List of RiverForecastPoint object for a given Hydrologic Service
        Area (HSA) value. This is a Variable Depth query.
        
        @param hsaId: Hydrological Service Area Identifier
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        @return: List containing RiverForecastGroup data objects
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getHsaRiverForecastGroupList(hsaId, isSubDataNeeded))
    
    def getGroupRiverForecastPointList(self, groupId, isSubDataNeeded):
        '''
        Get a List of RiverForecastPoint objects for a given River Forecast Group
        Id value. This is a Variable Depth query.
        
        @param groupId: RiverForecastGroup Group Identifier
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        @return List containing RiverForecastPoint data objects 
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getGroupRiverForecastPointList(groupId, isSubDataNeeded))
    
    def getRiverForecastPointList(self, lidList, isSubDataNeeded):
        '''
        Get a List of RiverForecastPoint objects for a given list of LID values.
        This is a Variable Depth query.
        
        @param lidList: List of RiverForecastPoint Identifiers
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        @return: List containing RiverForecastPoint data objects 
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getRiverForecastPointList(lidList, isSubDataNeeded))
    
    def getRiverForecastPoint(self, lid, isSubDataNeeded):
        '''
        Get a single RiverForecastPoint object. This is a Variable Depth query.
        
        @param lid: RiverForecastPoint Identifier
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        @return: The retrieved RiverForecastPoint
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getRiverForecastPoint(lid, isSubDataNeeded))
    
    def getRiverForecastGroupRiverForecastPointList(self, groupId, isSubDataNeeded):
        '''
        Retrieve a list of RiverForecastPoint objects for a given, parent River
        Forecast Group Identifier.
        
        @param groupdId: RiverForecastGroup Group Identifier
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        @return: List of retrieved RiverForecastPoint objects
        '''
        return JUtil.javaObjToPyVal(self._riverForecastManager.getRiverForecastGroupRiverForecastPointList(groupId, isSubDataNeeded))
    
    def getRiverForecastPointCurrentObservation(self, riverForecastPoint, currentSystemTime):
        '''
        Compute RiverForecastPoint Observation data values.
        
        @param riverForecastPoint: RiverForecastPoint to compute values for.
        @param currentSystemTime: Set System time for observation.
        @return: The populated riverForecastPoint
        '''
        self._riverForecastManager.getRiverForecastPointCurrentObservation(riverForecastPoint.toJavaObj(), currentSystemTime)
        return riverForecastPoint
    
    def getHydrographObservedData(self, riverForecastPoint, currentSystemTime):
        '''
        Retrieve and set Hydrograph Observed data for a RiverForecastPoint.
        
        @param riverForecastPoint: RiverForecastPoint to compute values for.
        @param currentSystemTime: Set System time for observation.
        @return: The populated riverForecastPoint
        '''
        self._riverForecastManager.getHydrographObservedData(riverForecastPoint.toJavaObj(), currentSystemTime)
        return riverForecastPoint

    def getRiverForecastPointHydrographForecast(self, riverForecastPoint, currentSystemTime):
        '''
        Retrieve and set Hydrograph Forecast data for a RiverForecastPoint.
        
        @param riverForecastPoint: RiverForecastPoint to fill and compute data
        @param currentSystemTime: Current System observation time
        @return: The populated riverForecastPoint
        '''
        self._riverForecastManager.getRiverForecastPointHydrographForecast(riverForecastPoint.toJavaObj(), currentSystemTime)
        return riverForecastPoint

    def getRiverForecastPointRiverStationInfo(self, lid):
        '''
        Retrieve RiverStationInfo for the specified RiverForecastPoint Identifier
        
        @param lid: RiverForecastPoint Identifier
        @return: The retrieved RiverStationInfo
        '''
        return self._riverForecastManager.getRiverForecastPointRiverStationInfo(lid)
    
    def getRiverForecastPointRiverStationInfoList(self, lidList):
        '''
        Get a List of RiverStationInfo objects for a given list of RiverForecastPoint LID values.
        
        @param lidList: RiverForecastPoint Identifier list of values
        @return: List of RiverStationInfo objects
        '''
        return self._riverForecastManager.getRiverForecastPointRiverStationInfoList(lidList)
    
    def getRiverForecastPointRiverStatus(self, lid, physicalElement):
        '''
        Get a List of RiverStatus objects for a given RiverForecastPoint LID and Physical Element value.
        
        @param lid: RiverForecastPoint Identifier value
        @param physicalElement: PhysicalElement value
        @return: List of RiverStatus objects 
        '''
        return self._riverForecastManager.getRiverForecastPointRiverStatus(lid, physicalElement)
    
    def getRiverForecastPointCountyStateListForLids(self, lidList):
        '''
        Get a List of CountyStateData objects for a given list of RiverForecastPoint LID values.
        
        @param lidList: RiverForecastPoint Identifier list of values
        @return List of CountyStateData objects
        '''
        return self._riverForecastManager.getRiverForecastPointCountyStateList(lidList)
    
    def getRiverForecastPointCountyStateListForLid(self, lid):
        '''
        Get a CountyStateData object for a given RiverForecastPoint LID value.
        
        @param lid: RiverForecastPoint Identifier
        @return: List of CountyStateData objects
        '''
        return self._riverForecastManager.getRiverForecastPointCountyStateList(lid)
    
    def getImpactsDataList(self, lid, month, day):
        '''
        Get a list of Impact value, Impact detail pairs for a River Forecast Point and given month and day.
        
        @param lid: RiverForecastPoint Identifier
        @param month: Numeric month
        @param day: Numeric day of month
        @return: List of Impact Pairs (Value,Detail)
        '''
        return self._riverForecastManager.getImpactsDataList(lid, month, day)
    
    def getFloodStatementDataList(self, lid, month, day):
        '''
        Get a List of Flood Statement FloodStmtData objects for a River Forecast Point and given month and day.
        
        @param lid: RiverForecastPoint Identifier
        @param month: Numeric month
        @param day: Numeric day of month
        @return: List of FloodStmtData objects
        '''
        return self._riverForecastManager.getFloodStatementDataList(lid, month, day)
    
    def getRiverForecastPointRiverZoneInfo(self, lid):
        '''
        Retrieve a RiverForecastZoneInfo object for the given LID (Point Id)
        
        @param lid: River Forecast Point Id (LID)
        @return: List of RiverPointZoneInfo objects
        '''
        return self._riverForecastManager.getRiverForecastPointRiverZoneInfo(lid)
    
    def getRiverForecastCrestHistory(self, lid, physicalElement, crestTypeList=None):
        '''
        Get a List of Crest History objects for a given LID and Physical Element
        pair. Only Record (R) crest history values are queried.
        
        @param lid: River Forecast Point Identifier
        @param physicalElement: Physical Element value
        @param crestTypeList: List of valid (prelim) values [optional]
        @return: List of CrestHistory objects
        '''
        if crestTypeList is None:
            return self._riverForecastManager.getRiverForecastCrestHistory(lid, physicalElement)
        else:
            return self._riverForecastManager.getRiverForecastCrestHistory(lid, physicalElement, crestTypeList)
        
    def getLidToCountyDataMap(self, lidList):
        '''
        Query for a Map of Lid (Point ID) to CountyStateData objects.
        
        @param lidList: List of RiverForecastPoint Identifier values
        @return: A Map of all input Lid values to their corresponding CountyStateData objects
        '''
        return self._riverForecastManager.getLidToCountyDataMap(lidList)
    
    def getCountyForecastGroup(self, state, county, isSubDataNeeded):
        '''
        Get a River Forecast County with its List of RiverForecastPoint objects
        and computed data for a given State Abbreviation and County Name values.
        
        @param state: 2 letter State abbreviation
        @param county: Name of county (First letter capitalized)
        @param isSubDataNeeded: Flag indicating whether this is a Deep query (true) or a Shallow query (false)
        @return: the retrieved CountyForecastGroup object
        '''
        return self._riverForecastManager.getCountyForecastGroup(state, county, isSubDataNeeded)
    
    def getTopRankedTypeSource(self, lid, physicalElement, duration, extremum):
        '''
        Retrieve the top ranked type source for the given parameters.
        
        @param lid: River Forecast Point Id (Point ID)
        @param physicalElement: River Forecast Physical Element
        @param duration: Duration of measurement
        @param extremum: e.g. Z, X
        @return: Type Source for given parameters or None 
        '''
        return self._riverForecastManager.getTopRankedTypeSource(lid, physicalElement, duration, extremum)
    
    def getForecastTimeSeries(self, lid, physicalElement, fcstValidBeginTime, fcstValidEndTime, basisBeginTime, useLatestForecast):
        '''
        Retrieve Hydrograph Forecast data for the given parameters.
        
        @param lid: RiverForecastPoint Identifier
        @param physicalElement: Physical Element identifier
        @param fcstValidBeginTime: The lower bound of time window to retrieve Time Series data for (in millis)
        @param fcstValidEndTime: The upper bound of the time window to retrieve Time Series data for (in millis)
        @param basisBeginTime: Forecast Basis Begin Time (in millis)
        @param useLatestForecast: Flag to use Only the Latest Forecast value when true
        @return: A full HydrographForecast object with SHEF Forecast Time Series data
        '''
        hydrograph = self._riverForecastManager.getForecastTimeSeries(lid, physicalElement, fcstValidBeginTime, fcstValidEndTime, basisBeginTime, useLatestForecast)
        return JUtil.javaObjToPyVal(hydrograph)
    
    def getPrecipTimeSeries(self, lid, physicalElement, typeSource, obsBeginTime, obsEndTime):
        '''
        Retrieve Hydrograph Precip data for the given parameters.
        
        @param lid: RiverForecastPoint Identifier
        @param physicalElement: Physical Element identifier
        @param typeSource: The SHEF Type Source code
        @param obsBeginTime: The lower bound of time window to retrieve observations for
        @param obsEndTime: The upper bound of the time window to retrieve observations for. (Current System Time)
        @return: A full HydrographForecast object with SHEF Forecast Time Series data
        '''
        hydrograph = self._riverForecastManager.getPrecipTimeSeries(lid, physicalElement, typeSource, obsBeginTime, obsEndTime)
        return JUtil.javaObjToPyVal(hydrograph)
    
    def getObservedTimeSeries(self, lid, physicalElement, typeSource, startTime, endTime):
        '''
        Retrieve Hydrograph Observed data for the given parameters.
        
        @param lid: RiverForecastPoint Identifier
        @param physicalElement: Physical Element identifier
        @param typeSource: The SHEF Type Source code
        @param startTime: The lower bound of time window to retrieve observations for
        @param endTime: The upper bound of the time window to retrieve observations for. (Current System Time)
        @return: A full HydrographForecast object with SHEF Forecast Time Series data
        '''
        hydrograph = self._riverForecastManager.getObservedTimeSeries(lid, physicalElement, typeSource, startTime, endTime)
        return JUtil.javaObjToPyVal(hydrograph)
    
    def getPhysicalElementValue(self, lid, physicalElement, duration, typeSource, extremum, timeArg, currentTime_ms):
        '''
        Retrieves the given physical element value for a river forecast point.
        
        @param lid: RiverForecastPoint Identifier
        @param physicalElement: Physical Element identifier
        @param duration: Duration of measurement
        @param typeSource: The SHEF Type Source code
        @param extremum: e.g. Z, X
        @param timeArg: The time specification dayOffset|hhmm|interval e.g. 0|1200|1
        @param currentTime_ms: current time in milliseconds
        @return: Dictionary containing the retrieved physical element value and associated validtime
        '''
        return self._riverForecastManager.getPhysicalElementValue(lid, physicalElement, duration, typeSource, extremum, timeArg, currentTime_ms)
