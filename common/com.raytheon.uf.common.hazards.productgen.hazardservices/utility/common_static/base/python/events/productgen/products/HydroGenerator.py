
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
import logging, UFStatusHandler
import datetime
import Legacy_Base_Generator
from abc import *

class Product(Legacy_Base_Generator.Product):

    def __init__(self):
        self._riverForecastUtils = None
        self._riverForecastManager = None
        self._hydroProductParts = HydroProductParts()
        super(Product, self).__init__()

    def _initialize(self):
        super(Product, self)._initialize()
        self.logger = logging.getLogger('HydroGenerator')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'com.raytheon.uf.common.hazards.productgen', 'HydroGenerator', level=logging.INFO))
        self.logger.setLevel(logging.INFO)  

    #### Utility methods

    def _prepareRiverForecastPointData(self, pointID, hazardEventDict, hazardEvent):
        '''
            Adds all data related to RiverForecastPoints to the hazardEventDict dictionary.
        '''
        
        millis = SimulatedTime.getSystemTime().getMillis()
        currentTime = datetime.datetime.fromtimestamp(millis / 1000)
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
        hazardEventDict['groupMaxForecastFloodCatName'] = self._riverForecastUtils.getMaximumForecastCatName(riverForecastPoint)

        hazardEventDict['proximity'] = riverForecastPoint.getProximity()
        hazardEventDict['riverPointName'] = riverForecastPoint.getName()
        # Observed and Flood Stages
        observedCurrentIndex = riverForecastPoint.getObservedCurrentIndex()
        shefObserved = self._riverForecastManager.getSHEFObserved(riverForecastPoint, observedCurrentIndex)
        if shefObserved is not None:
            observedStage = shefObserved.getValue()
            hazardEventDict['observedStage'] = observedStage
        else:
            hazardEventDict['observedStage'] = MISSING_VALUE
        category = self._riverForecastUtils.getObservedLevel(riverForecastPoint, CATEGORY)
        categoryName = self._riverForecastUtils.getObservedLevel(riverForecastPoint, CATEGORY_NAME)
        observedTime_ms = self._riverForecastUtils.getObservedLevel(riverForecastPoint, TIME)
        hazardEventDict['observedCategory'] = category
        hazardEventDict['observedCategoryName'] = categoryName
        hazardEventDict['observedTime_ms'] = observedTime_ms
        
        max24HourIndex = riverForecastPoint.getObservedMax24Index()
        stageCodePair = riverForecastPoint.getMaximum24HourObservedStage()
        tempStageString = str (stageCodePair.getFirst() )
        
        max24HourObservedStage = float( tempStageString )
        max24HourObservedShefQualityCode = stageCodePair.getSecond()
        hazardEventDict['max24HourObservedStage'] = max24HourObservedStage
        primaryPE = riverForecastPoint.getPrimaryPE()
        hazardEventDict['primaryPE'] = primaryPE
        hazardEventDict['stageFlowName'] = self._riverForecastUtils.getStageFlowName(primaryPE)
        hazardEventDict['floodStage'] = riverForecastPoint.getFloodStage()
        # Maximum Forecast Stage
        maxForecastIndex = riverForecastPoint.getMaximumForecastIndex()
        maxShefForecast = self._riverForecastManager.getSHEFForecast(riverForecastPoint, maxForecastIndex)
        if maxShefForecast is not None:
            maximumForecastStage = maxShefForecast.getValue()
            maximumForecastTime_ms = maxShefForecast.getValidTime()
        else:
            maximumForecastStage = MISSING_VALUE
            maximumForecastTime_ms = MISSING_VALUE

        # Need to save this off to be set in the ProductInformation later.
        
        self._maxFcstCategory = riverForecastPoint.getMaximumForecastCategory()
        hazardEventDict['maxFcstCategory'] = self._maxFcstCategory
        
        maxFcstCategoryName = self._riverForecastUtils.getMaximumForecastLevel(riverForecastPoint, primaryPE, CATEGORY_NAME)
        hazardEventDict['maxFcstCategoryName'] = maxFcstCategoryName
        # Rise
        hazardEventDict['forecastRiseAboveFloodStageTime_ms'] = riverForecastPoint.getForecastRiseAboveTime()
        # Crest
        hazardEventDict['forecastCrestStage'] = riverForecastPoint.getForecastCrestValue()
        hazardEventDict['forecastCrestTime_ms'] = riverForecastPoint.getForecastCrestTime()
        # Fall
        hazardEventDict['forecastFallBelowFloodStageTime_ms'] = riverForecastPoint.getForecastFallBelowTime()

        hazardEventDict['stageFlowUnits'] = self._riverForecastUtils.getFlowUnits(primaryPE)
        # Trend
        trendPhrase = self._riverForecastUtils.getStageTrend(riverForecastPoint)
        hazardEventDict['stageTrend'] = trendPhrase

        hazardEventDict['pointImpacts'] = self._preparePointImpacts(hazardEvent)
        hazardEventDict['impactCompUnits'] = self._riverForecastUtils.getFlowUnits(primaryPE)
        hazardEventDict['crestsSelectedForecastPointsComboBox'] = hazardEvent.get('crestsSelectedForecastPointsComboBox')

        if maximumForecastStage and maximumForecastStage != MISSING_VALUE:
            hazardEventDict['maximumForecastStage'] = maximumForecastStage            

        if maximumForecastTime_ms and maximumForecastTime_ms != MISSING_VALUE:
            hazardEventDict['maximumForecastTime_ms'] = maximumForecastTime_ms
            
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
            timeArgs.append(str(i+1)+'|'+baseTime+'|1')  

        hazardEventDict['day1'] = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[0], False, 
                                                millis)
        hazardEventDict['day2'] = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[1], False, 
                                                millis)
        hazardEventDict['day3'] = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[2], False, 
                                                millis) 

    def _preparePointImpacts(self, hazardEvent):
        # Pull out the list of chosen impact text fields
        impacts = []
        validVals = hazardEvent.get('impactCheckBoxes')
        
        if validVals is None:
            return None

        for key in validVals:
            if key.startswith('impactCheckBox_'):
                height, impactValue = self._parseImpactKey(key)
                value = hazardEvent.get(key)
                textFieldName = 'impactTextField_'+impactValue
                impacts.append((height, hazardEvent.get(textFieldName)))
        return impacts

    def _parseImpactKey(self, key):
        parts = key.rsplit('_')
        if len(parts) > 1:
            impactValue = parts[1]
            height = impactValue.rsplit('-')[0]
            # Round height to 2 decimal point - result is a string
            height = format(float(height), '.2f')
        else:
            impactValue = ''
            height = ''
        return height, impactValue

    def hydrologicCauseMapping(self, hydrologicCause):
        mapping = {
            'dam': 'DM',
            'siteImminent': 'DM',
            'siteFailed': 'DM',
            'levee': 'DM',
            'floodgate': 'DR',
            'glacier': 'GO',
            'icejam': 'IJ',
            'snowMelt': 'RS',
            'volcano': 'SM',
            'volcanoLahar': 'SM',
            'default': 'ER'
            }
        if mapping.has_key(hydrologicCause):
            return mapping[hydrologicCause]
        else:
            return mapping['default']

