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

'''
 
 @since: April 2015
 @author: Raytheon Hazard Services Team
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 May 08, 2015  6562      Chris.Cody  Initial Creation: Restructure River Forecast Points/Recommender
                                     Legacy functions and constants imported from RiverForecastPoints.py
 May 21, 2015  8112      Chris.Cody  Python reads 0 values from Java methods as None. The return type is lost.
 Jun 25, 2015  8313      Benjamin.Phillippe Fixed situation with missing stage date 
'''

from com.raytheon.uf.common.time import SimulatedTime
from com.raytheon.uf.common.hazards.hydro import RiverForecastPoint
from com.raytheon.uf.common.hazards.hydro import RiverForecastGroup
from com.raytheon.uf.common.hazards.hydro import RiverHydroConstants

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

MISSING_STAGE_DATE = MISSING_VALUE, datetime.datetime.fromtimestamp(0)
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
        
    TREND_VALUE_DESCRIPTION_DICT = {  'RISE': 'Rising',
                                          'UNCHANGED': 'Steady',
                                          'FALL': 'Falling',
                                          'MISSING': 'Unknown'}
            
    def __init__(self):
        pass
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
                                            
        @param forecastPointID: The river forecast point identifier.
        @return: The stage, date associated with the flood-of-record for this
                                   forecast point.
        '''
        
        if filters is None :
            return [], []
    
        listTuple, maxIndex, diffIndex, recentIndex = self.getIndices(impactDataList, filters, primaryPE, riverForecastPoint, 'Impacts')
    
        # Index values need to be compared to None rather than submitted to a
        # pure boolean test, otherwise index value of 0 (first value in list)
        # will fail to get picked up.
        searchType = filters['Search Type']
        allBelow = False        
        if searchType.find("All Below") >= 0 :        
            allBelow = True
            
        characterizations = []
        descriptions = []
        
        for index, val in enumerate(listTuple) :
            stageOrFlow, impact = val
            
            impactData = impact.split("||")        
            if len(impactData) != 2 :        
                continue
            if allBelow :        
                characterizations.append(("%.2f"%stageOrFlow)+impactData[0])        
                descriptions.append(impactData[1])
    
        if searchType.find("Highest") >= 0 :
            if maxIndex != None :
                impactData = listTuple[maxIndex][1].split("||")
                stageOrFlow = listTuple[maxIndex][0]
                characterizations.append(("%.2f"%stageOrFlow)+impactData[0])
                descriptions.append(impactData[1])
        elif searchType.find("Closest") >= 0 :
            if diffIndex != None :
                impactData = listTuple[diffIndex][1].split("||")
                stageOrFlow = listTuple[diffIndex][0]
                characterizations.append(("%.2f"%stageOrFlow)+impactData[0])
                descriptions.append(impactData[1])
    
        return characterizations, descriptions
        
    
    def getIndices(self, plist, filters, primaryPhysicalElement, riverForecastPoint, impactsOrCrests):
    
        currTime = self.getCurrentTimeMS()
        
        if len(plist)==2:
            if isinstance(plist[0],bool) and isinstance(plist[1],list):
                plist = plist[1]
    
        listTuple = []
        for pair in plist :
            first = JUtil.javaObjToPyVal(pair.getFirst())
            if not isinstance(first, float) :
                continue
            second = JUtil.javaObjToPyVal(pair.getSecond())
            
            if not isinstance(second, str) and impactsOrCrests == 'Impacts':
                continue
            elif not isinstance(second, datetime.datetime) and impactsOrCrests == 'Crests':
                continue
            pytuple = ( first, second )
            listTuple.append(pytuple)
    
    
        referenceType = filters['Reference Type']
        depthBelowFloodStage = filters.get('Depth Below Flood Stage', float(0.0) )
        flowWindowLower = filters.get('Flow Window Lower', float(0.0) )
        flowWindowUpper = filters.get('Flow Window Upper', float(0.0) )
        stageWindowLower = filters.get('Stage Window Lower', float(0.0) )
        stageWindowUpper = filters.get('Stage Window Upper', float(0.0) )
        
        ### yearLookBack found only in Crests.  Expect 'None' for impacts
        yearLookBack = filters.get('Year Lookback')
        
        searchType = filters['Search Type']
        if primaryPhysicalElement.startswith(PE_Q) :
            flowStageWindow = filters['Flow Stage Window']
        else :
            flowStageWindow = 0
    
        if impactsOrCrests == 'Impacts':
            floodStage = float(riverForecastPoint.getFloodStage())
        elif impactsOrCrests == 'Crests':
            floodStage = float(riverForecastPoint.getFloodFlow())
        else:
            floodStage = None
    
        ### Note: currentDate is only used with 'Crests'
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
            else:
                referenceValue = maxObs
                currentDate = riverForecastPoint.getObservedCurrentTime()
        if referenceValue is None:
            referenceValue = float(0.0)
                
        ### curDateDate through minDateDate used with Crests only
        minDateDate = None
        if impactsOrCrests == 'Crests':
            curDateDate = datetime.datetime.fromtimestamp(currentDate / 1000)
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
        
        maxIndex = None
        diffIndex = None
        ### recentDate, recentIndex are for Crests only
        recentDate = minDateDate
        recentIndex = None
            
        if lowerBound > floodValueStage :
            lowerBound = floodValueStage
                
        ### For Impacts only    
        if searchType.find("All Below") >= 0 :        
            lowerBound = 0
            
            
        for index, val in enumerate(listTuple) :
            stageOrFlow, impactOrDate = val
            
            if stageOrFlow<lowerBound or stageOrFlow>upperBound :
                
                if impactsOrCrests == 'Impacts':
                    continue
                else: #impactsOrCrests == 'Crests'
                    if impactOrDate<minDateDate:
                        continue
                    
            if stageOrFlow > maximumValue :
                maximumValue = stageOrFlow
                maxIndex = index
                
            diffVal = math.fabs(referenceValue - stageOrFlow )
            if diffVal < differenceValue :
                differenceValue = diffVal
                diffIndex = index
               
            if impactsOrCrests == 'Crests':
                if impactOrDate > recentDate :
                    recentDate = impactOrDate
                    recentIndex = index
                   
        return listTuple, maxIndex, diffIndex, recentIndex
        
        
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
        plist = [ ]

        if primaryPE.startswith(PE_Q) :
            jlist = riverForecastPoint.getFlowCrestHistory()
            plist = JUtilHandler.javaCollectionToPyCollection(jlist)
        elif primaryPE.startswith(PE_H) :
            jlist = riverForecastPoint.getStageCrestHistory()
            plist = JUtilHandler.javaCollectionToPyCollection(jlist)
    
        listTuple, maxIndex, diffIndex, recentIndex = self.getIndices(plist, filters, primaryPE, riverForecastPoint, 'Crests')
    
        # Index values need to be compared to None rather than submitted to a
        # pure boolean test, otherwise index value of 0 (first value in list)
        # will fail to get picked up.
        
        stageDate = MISSING_STAGE_DATE
        searchType = filters['Search Type']
        if searchType.find("Highest") >= 0 :
            if maxIndex != None :
                stageDate =listTuple[maxIndex]
        elif searchType.find("Closest") >= 0 :
            if diffIndex != None :
                stageDate = listTuple[diffIndex]
        elif recentIndex != None : # most recent
            stageDate = listTuple[recentIndex]
        else :
            stageDate = MISSING_STAGE_DATE
    
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
        
    
    def getFlowUnits(self, primaryPE):
        '''
        Emulates the functionality of the <ImpCompUnits> template variable.
        e.g. feet
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The unit (ft or cfs) associated with the stage or flow
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


    '''
    Retrieve a RiverForecastPoint from a RiverForecastGroup.
    The RiverForecastGroup must be from a Deep query
    '''
    def getRiverForecastPointFromRiverForecastGroup(self, pointID, riverForecastGroup):
        if pointID is not None and riverForecastGroup is not None:
            riverForecastPointList = riverForecastGroup.getForecastPointList()
            if riverForecastPointList is not None:
                for riverForecastPoint in riverForecastPointList:
                    riverForecastPointID = riverForecastPoint.getLid()
                    if pointID == riverForecastPointID:
                        return riverForecastPoint
        return None

