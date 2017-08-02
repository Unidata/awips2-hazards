
'''
    Description: Hydro Product Generator that all Hydro Product
                 specific generators should inherit from.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 26, 2015    4936    Chris.Cody  Implement scripts for Flash Flood Watch Products (FFA,FAA,FLA)
    Jan 31, 2015    4937    Robert.Blum General cleanup and bug fixes.
    Feb 20, 2015    4937    Robert.Blum Added required data for groupSummary to section dicitonary
    Mar 17, 2015    6963    Robert.Blum Rounded impact height to a precision of 2.
    Apr 30, 2015    7579    Robert.Blum Changes for multiple hazards per section.
    May 05, 2015    7141    Robert.Blum Changes for floodPointTable.
    May 26, 2015    7634    Chris.Cody  Changes for Forecast Bullet Generation
    Jun 25, 2015    8565    Chris.Cody  Impacts Error issuing multiple products from the HID
    Jun 25, 2015    8313    Benjamin.Phillippe Fixed issued event loading when time is changed
    Jul 23, 2015    9643    Robert.Blum All rounding is now done in one common place so that it can 
                                        be easily overridden by sites.
    Jan 18, 2016   12942    Roger.Ferrel Change Forecast in _prepareRiverForecastPointData
                                         to 6 hour window.
    Feb 22, 2016   15017    Robert.Blum  Using Crest stage/time from HID/Graphical Editor.
    Mar 02, 2016   11898    Robert.Blum  Completely reworked ForecastStageText based on RiverPro's templates.
    Mar 15, 2016   11892    Robert.Blum  Fixed Hydrologic cause mapping to match WarnGen.
    May 04, 2016   15584    Kevin.Bisanz Updates to parse physicalElement from impact.
    May 19, 2016   16545    Robert.Blum  Added previous category for obs and forecast.
    May 24, 2016   15584    Kevin.Bisanz Renamed RiverForecastUtils.getFlowUnits(..)
                                         to getStageFlowUnits(..).
    Jun 03, 2016   17532    Robert.Blum  Correctly populating groupMaxForecastFloodCatName.
    Jun 21, 2016    9620    Robert.Blum  Removed unused code.
    Jun 23, 2016   19537    Chris.Golden Changed to use UTC when converting epoch time to datetime.
    Aug 16, 2016   15017    Robert.blum  Removed fields that are now set by the recommender.
    Oct 07, 2016   21777    Robert.Blum  Removed logger setup as the base generator handles it.

'''
from com.raytheon.uf.common.hazards.hydro import RiverForecastManager
from com.raytheon.uf.common.hazards.hydro import RiverForecastPoint
from com.raytheon.uf.common.hazards.hydro import RiverForecastGroup
from RiverForecastUtils import RiverForecastUtils
from RiverForecastUtils import CATEGORY
from RiverForecastUtils import CATEGORY_NAME
from RiverForecastUtils import TIME
from HydroProductParts import HydroProductParts
from com.raytheon.uf.common.time import SimulatedTime
from HazardConstants import MISSING_VALUE
import datetime
import Legacy_Base_Generator
import HazardConstants
from abc import *

class Product(Legacy_Base_Generator.Product):
    def __init__(self):
        self._riverForecastUtils = None
        self._riverForecastManager = None
        self._hydroProductParts = HydroProductParts()
        super(Product, self).__init__()

    def _initialize(self):
        super(Product, self)._initialize()

    #### Utility methods

    def _prepareRiverForecastPointData(self, pointID, hazardEventDict, hazardEvent):
        '''
            Adds all data related to RiverForecastPoints to the hazardEventDict dictionary.
        '''
        millis = SimulatedTime.getSystemTime().getMillis()
        if self._riverForecastUtils is None:
            self._riverForecastUtils = RiverForecastUtils()
        if self._riverForecastManager is None:
            self._riverForecastManager = RiverForecastManager()
        
        riverForecastGroup = self._riverForecastManager.getRiverForecastGroupForRiverForecastPoint(pointID, True)
        riverForecastPoint = self._riverForecastUtils.getRiverForecastPointFromRiverForecastGroup( pointID, riverForecastGroup)
        hazardEventDict['pointID'] = pointID
        hazardEventDict['riverName_GroupName'] = riverForecastGroup.getGroupName()
        hazardEventDict['riverName_RiverName'] = riverForecastPoint.getStream()
        hazardEventDict['groupForecastPointList'] = self._riverForecastUtils.getGroupForecastPointNameList(riverForecastGroup, None)
        groupMaxFcstCat = riverForecastGroup.getMaxForecastCategory()
        hazardEventDict['groupMaxForecastFloodCatName'] = self._riverForecastUtils.getFloodCategoryName(groupMaxFcstCat)

        hazardEventDict['proximity'] = riverForecastPoint.getProximity()
        hazardEventDict['riverPointName'] = riverForecastPoint.getName()

        # Observed Data

        # Rise
        hazardEventDict['obsRiseAboveFSTime_ms'] = riverForecastPoint.getObservedRiseAboveTime()
        # Fall
        hazardEventDict['obsFallBelowFSTime_ms'] = riverForecastPoint.getObservedFallBelowTime()
        # Observed Stage
        observedCurrentIndex = riverForecastPoint.getObservedCurrentIndex()
        shefObserved = self._riverForecastManager.getSHEFObserved(riverForecastPoint, observedCurrentIndex)
        if shefObserved is not None:
            observedStage = shefObserved.getValue() # Or flow
            hazardEventDict['observedStage'] = observedStage
        else:
            hazardEventDict['observedStage'] = MISSING_VALUE
        # Need to save this off to be set in the ProductInformation later.
        observedCategory = self._riverForecastUtils.getObservedLevel(riverForecastPoint, CATEGORY)
        categoryName = self._riverForecastUtils.getObservedLevel(riverForecastPoint, CATEGORY_NAME)
        observedTime_ms = self._riverForecastUtils.getObservedLevel(riverForecastPoint, TIME)
        hazardEventDict['observedCategory'] = observedCategory
        hazardEventDict['observedCategoryName'] = categoryName
        hazardEventDict['previousObservedCategory'] = hazardEvent.get("previousObservedCategory", None)
        hazardEventDict['previousObservedCategoryName'] = hazardEvent.get("previousObservedCategoryName", None)
        
        hazardEventDict['observedTime_ms'] = observedTime_ms
        
        max24HourIndex = riverForecastPoint.getObservedMax24Index()
        stageCodePair = riverForecastPoint.getMaximum24HourObservedStage()
        tempStageString = str (stageCodePair.getFirst() )
        
        max24HourObservedStage = float( tempStageString )
        max24HourObservedShefQualityCode = stageCodePair.getSecond()
        hazardEventDict['max24HourObservedStage'] = max24HourObservedStage  # Or flow
        primaryPE = riverForecastPoint.getPrimaryPE()
        hazardEventDict['primaryPE'] = primaryPE
        hazardEventDict['stageFlowName'] = self._riverForecastUtils.getStageFlowName(primaryPE)
        hazardEventDict['floodStage'] = riverForecastPoint.getFloodStage()
        hazardEventDict['floodFlow'] = riverForecastPoint.getFloodFlow()

        # Forecast Data

        # Need to save this off to be set in the ProductInformation later.
        maxFcstCategory = riverForecastPoint.getMaximumForecastCategory()
        hazardEventDict['previousForecastCategory'] = hazardEvent.get("previousForecastCategory", None)
        hazardEventDict['previousForecastCategoryName'] = hazardEvent.get("previousForecastCategoryName", None)
        hazardEventDict['maxFcstCategory'] = maxFcstCategory
        
        maxFcstCategoryName = self._riverForecastUtils.getMaximumForecastLevel(riverForecastPoint, primaryPE, CATEGORY_NAME)
        hazardEventDict['maxFcstCategoryName'] = maxFcstCategoryName
        # Rise
        hazardEventDict['forecastRiseAboveFloodStageTime_ms'] = riverForecastPoint.getForecastRiseAboveTime()
        # Fall
        hazardEventDict['forecastFallBelowFloodStageTime_ms'] = riverForecastPoint.getForecastFallBelowTime()

        hazardEventDict['stageFlowUnits'] = self._riverForecastUtils.getStageFlowUnits(primaryPE)
        # Trend
        trendPhrase = self._riverForecastUtils.getStageTrend(riverForecastPoint)
        hazardEventDict['stageTrend'] = trendPhrase

        hazardEventDict['pointImpacts'] = self._preparePointImpacts(hazardEvent)
        hazardEventDict['impactCompUnits'] = self._riverForecastUtils.getStageFlowUnits(primaryPE)
        hazardEventDict['crestsSelectedForecastPointsComboBox'] = hazardEvent.get('crestsSelectedForecastPointsComboBox')

        # Spec values
        forecastTypeSource = self._riverForecastManager.getTopRankedTypeSource(pointID, primaryPE, 0, 'Z')
        hazardEventDict['specValue'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, primaryPE, 0, forecastTypeSource, 'Z', '4|1200|1', False, millis)
        
        hazardEventDict['specTime'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, primaryPE, 0, forecastTypeSource, 'Z', '4|1200|1', True, millis)

        # Next 3 days values for FloodPointTable
        baseTime = self._tpc.getFormattedTime(observedTime_ms, '%H%M')
        # Build timeArgs  e.g. '1|1200|1'
        timeArgs = []
        for i in range(3):
            timeArgs.append(str(i+1)+'|'+baseTime+'|3')

        hazardEventDict['day1'] = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[0], False, 
                                                millis)
        hazardEventDict['day2'] = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[1], False, 
                                                millis)
        hazardEventDict['day3'] = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[2], False, 
                                                millis) 

        time = self._tpc.getFormattedTime(millis, '%H%M')
        hazardEventDict['HT0FFXNext'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HT", 0, "FF", 'X', '0|' + time + '|1', False, millis)
        hazardEventDict['HT0FFXNextTime'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HT", 0, "FF", 'X', '0|' + time + '|1', True, millis)

        hazardEventDict['HP0FFXNext'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HP", 0, "FF", 'X', '0|' + time + '|1', False, millis)
        hazardEventDict['HP0FFXNextTime'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HP", 0, "FF", 'X', '0|' + time + '|1', True, millis)

        hazardEventDict['HG0FFXNext'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HG", 0, "FF", 'X', '0|' + time + '|1', False, millis)
        hazardEventDict['HG0FFXNextTime'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HG", 0, "FF", 'X', '0|' + time + '|1', True, millis)

        hazardEventDict['QR0FFXNext'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "QR", 0, "FF", 'X', '0|' + time + '|1', False, millis)
        hazardEventDict['QR0FFXNextTime'] = self._riverForecastManager.getPhysicalElementValue(
                pointID, "QR", 0, "FF", 'X', '0|' + time + '|1', True, millis)
        
    def _preparePointImpacts(self, hazardEvent):
        # Pull out the list of chosen impact text fields
        impacts = []
        validVals = hazardEvent.get('impactCheckBoxes')
        
        if validVals is None:
            return None

        for key in validVals:
            if key.startswith('impactCheckBox_'):
                stageFlow, physicalElement, impactValue = self._parseImpactKey(key)
                if stageFlow is not None and physicalElement is not None and impactValue is not None:
                    textFieldName = 'impactTextField_'+impactValue
                    textFieldValue = hazardEvent.get(textFieldName)
                    #Ignore impacts lacking a text field value
                    if textFieldValue is not None:
                        impacts.append((stageFlow, physicalElement, textFieldValue))
        return impacts

    def _parseImpactKey(self, key):
        peIdx = 1
        valueIdx = 2

        parts = key.rsplit('_')
        if len(parts) > 1:
            impactValue = parts[valueIdx]
            physicalElement = parts[peIdx]
            stageFlow = impactValue.rsplit('-')[0]
            # Round stageFlow - result is a string
            stageFlow = self._tpc.roundFloat(stageFlow, returnString=True)
        else:
            impactValue = ''
            physicalElement = ''
            stageFlow = ''
        return stageFlow, physicalElement, impactValue

    def hydrologicCauseMapping(self, hydrologicCause):
        mapping = {
            'dam': 'DM',
            'siteImminent': 'DM',
            'siteFailed': 'DM',
            'levee': 'DM',
            'floodgate': 'DR',
            'glacier': 'GO',
            'icejam': 'IJ',
            'snowMelt': 'SM',
            'volcano': 'SM',
            'volcanoLahar': 'SM',
            'rain': 'RS',
            'default': 'ER'
            }
        if mapping.has_key(hydrologicCause):
            return mapping[hydrologicCause]
        else:
            return mapping['default']

