
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
'''
from RiverForecastPoints import RiverForecastPoints
from HydroProductParts import HydroProductParts
from com.raytheon.uf.common.time import SimulatedTime
import logging, UFStatusHandler
import datetime
import Legacy_Base_Generator
from abc import *

class Product(Legacy_Base_Generator.Product):

    def __init__(self):
        self._rfp = None
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
        if self._rfp is None:
            self._rfp = RiverForecastPoints(currentTime)

        hazardEventDict['pointID'] = pointID
        hazardEventDict['riverName_GroupName'] = self._rfp.getGroupName(pointID)
        hazardEventDict['riverName_RiverName'] = self._rfp.getRiverName(pointID)
        hazardEventDict['groupForecastPointList'] = self._rfp.getGroupForecastPointList(pointID)
        hazardEventDict['groupMaxForecastFloodCatName'] = self._rfp.getGroupMaximumForecastFloodCategoryName(pointID)

        hazardEventDict['proximity'] = self._rfp.getRiverPointProximity(pointID)
        hazardEventDict['riverPointName'] = self._rfp.getRiverPointName(pointID)
        # Observed and Flood Stages
        observedStage, shefQualityCode = self._rfp.getObservedStage(pointID)
        hazardEventDict['observedStage'] = observedStage
        hazardEventDict['observedCategory'] = self._rfp.getObservedCategory(pointID)
        hazardEventDict['observedCategoryName'] = self._rfp.getObservedCategoryName(pointID)
        observedTime_ms = self._rfp.getObservedTime(pointID)
        hazardEventDict['observedTime_ms'] = observedTime_ms
        max24HourObservedStage, shefQualityCode = self._rfp.getMaximum24HourObservedStage(pointID)
        hazardEventDict['max24HourObservedStage'] = max24HourObservedStage
        hazardEventDict['stageFlowName'] = self._rfp.getStageFlowName(pointID)
        hazardEventDict['floodStage'] = self._rfp.getFloodStage(pointID)
        # Maximum Forecast Stage
        primaryPE = self._rfp.getPrimaryPhysicalElement(pointID)
        hazardEventDict['primaryPE'] = primaryPE
        maximumForecastStage = self._rfp.getMaximumForecastLevel(pointID, primaryPE)
        hazardEventDict['maximumForecastStage'] = maximumForecastStage
        hazardEventDict['maximumForecastTime_ms'] = self._rfp.getMaximumForecastTime(pointID)
        # Need to save this off to be set in the ProductInformation later.
        self._maxFcstCategory = self._rfp.getMaximumForecastCategory(pointID)
        hazardEventDict['maxFcstCategory'] = self._maxFcstCategory
        hazardEventDict['maxFcstCategoryName'] = self._rfp.getMaximumForecastCatName(pointID)
        # Rise
        hazardEventDict['forecastRiseAboveFloodStageTime_ms'] = self._rfp.getForecastRiseAboveFloodStageTime(pointID)
        # Crest
        hazardEventDict['forecastCrestStage'] = self._rfp.getForecastCrestStage(pointID)
        hazardEventDict['forecastCrestTime_ms'] = self._rfp.getForecastCrestTime(pointID)
        # Fall
        hazardEventDict['forecastFallBelowFloodStageTime_ms'] = self._rfp.getForecastFallBelowFloodStageTime(pointID)

        hazardEventDict['stageFlowUnits'] = self._rfp.getStageFlowUnits(pointID)
        # Trend
        hazardEventDict['stageTrend'] = self._rfp.getStageTrend(pointID)

        hazardEventDict['pointImpacts'] = self._preparePointImpacts(hazardEvent)
        hazardEventDict['impactCompUnits'] = self._rfp.getImpactCompUnits(pointID)
        hazardEventDict['crestsSelectedForecastPointsComboBox'] = hazardEvent.get('crestsSelectedForecastPointsComboBox')

        # Spec values
        forecastTypeSource = self._rfp.getForecastTopRankedTypeSource(pointID, primaryPE, 0, 'Z')
        hazardEventDict['specValue'] = self._rfp.getPhysicalElementValue(
                pointID, primaryPE, 0, forecastTypeSource, 'Z', '4|1200|1', timeFlag=False, currentTime_ms=millis)
        hazardEventDict['specTime'] = self._rfp.getPhysicalElementValue(
                pointID, primaryPE, 0, forecastTypeSource, 'Z', '4|1200|1', timeFlag=True, currentTime_ms=millis)

        # Next 3 days values for FloodPointTable
        baseTime = self._tpc.getFormattedTime(observedTime_ms, '%H%M')
        # Build timeArgs  e.g. '1|1200|1'
        timeArgs = []
        for i in range(3):
            timeArgs.append(str(i+1)+'|'+baseTime+'|1')  
        # TODO Fix getPhysicalElementValue              
        hazardEventDict['day1'] = self._rfp.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[0], timeFlag=False, 
                                                currentTime_ms=millis)
        hazardEventDict['day2'] = self._rfp.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[1], timeFlag=False, 
                                                currentTime_ms=millis)
        hazardEventDict['day3'] = self._rfp.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[2], timeFlag=False, 
                                                currentTime_ms=millis) 

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
