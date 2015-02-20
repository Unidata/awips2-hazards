
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

    def _prepareRiverForecastPointData(self, pointID, segment, event):
        '''
            Adds all data related to RiverForecastPoints to the segment dictionary.
        '''
        millis = SimulatedTime.getSystemTime().getMillis()
        currentTime = datetime.datetime.fromtimestamp(millis / 1000)
        if self._rfp is None:
            self._rfp = RiverForecastPoints(currentTime)

        segment['pointID'] = pointID
        segment['riverName_GroupName'] = self._rfp.getGroupName(pointID)
        segment['riverName_RiverName'] = self._rfp.getRiverName(pointID)
        segment['groupForecastPointList'] = self._rfp.getGroupForecastPointList(pointID)
        segment['groupMaxForecastFloodCatName'] = self._rfp.getGroupMaximumForecastFloodCategoryName(pointID)

        segment['proximity'] = self._rfp.getRiverPointProximity(pointID)
        segment['riverPointName'] = self._rfp.getRiverPointName(pointID)
        # Observed and Flood Stages
        observedStage, shefQualityCode = self._rfp.getObservedStage(pointID)
        segment['observedStage'] = observedStage
        segment['observedCategory'] = self._rfp.getObservedCategory(pointID)
        segment['observedCategoryName'] = self._rfp.getObservedCategoryName(pointID)
        segment['observedTime_ms'] = self._rfp.getObservedTime(pointID)
        max24HourObservedStage, shefQualityCode = self._rfp.getMaximum24HourObservedStage(pointID)
        segment['max24HourObservedStage'] = max24HourObservedStage
        segment['stageFlowName'] = self._rfp.getStageFlowName(pointID)
        segment['floodStage'] = self._rfp.getFloodStage(pointID)
        # Maximum Forecast Stage
        primaryPE = self._rfp.getPrimaryPhysicalElement(pointID)
        segment['primaryPE'] = primaryPE
        maximumForecastStage = self._rfp.getMaximumForecastLevel(pointID, primaryPE)
        segment['maximumForecastStage'] = maximumForecastStage
        segment['maximumForecastTime_ms'] = self._rfp.getMaximumForecastTime(pointID)
        # Need to save this off to be set in the ProductInformation later.
        self._maxFcstCategory = self._rfp.getMaximumForecastCategory(pointID)
        segment['maxFcstCategory'] = self._maxFcstCategory
        segment['maxFcstCategoryName'] = self._rfp.getMaximumForecastCatName(pointID)
        # Rise
        segment['forecastRiseAboveFloodStageTime_ms'] = self._rfp.getForecastRiseAboveFloodStageTime(pointID)
        # Crest
        segment['forecastCrestStage'] = self._rfp.getForecastCrestStage(pointID)
        segment['forecastCrestTime_ms'] = self._rfp.getForecastCrestTime(pointID)
        # Fall
        segment['forecastFallBelowFloodStageTime_ms'] = self._rfp.getForecastFallBelowFloodStageTime(pointID)

        segment['stageFlowUnits'] = self._rfp.getStageFlowUnits(pointID)
        # Trend
        segment['stageTrend'] = self._rfp.getStageTrend(pointID)

        segment['pointImpacts'] = self._preparePointImpacts(event)
        segment['impactCompUnits'] = self._rfp.getImpactCompUnits(pointID)
        segment['crestsSelectedForecastPointsComboBox'] = event.get('crestsSelectedForecastPointsComboBox')

        # Spec values
        forecastTypeSource = self._rfp.getForecastTopRankedTypeSource(pointID, primaryPE, 0, 'Z')
        segment['specValue'] = self._rfp.getPhysicalElementValue(
                pointID, primaryPE, 0, forecastTypeSource, 'Z', '4|1200|1', timeFlag=False, currentTime_ms=millis)
        segment['specTime'] = self._rfp.getPhysicalElementValue(
                pointID, primaryPE, 0, forecastTypeSource, 'Z', '4|1200|1', timeFlag=True, currentTime_ms=millis)

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
