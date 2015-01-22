'''
    Description: Hydro Product Generator that all Hydro Product
                 specific generators should inherit from.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
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
        if self._rfp is None:
            millis = SimulatedTime.getSystemTime().getMillis()
            currentTime = datetime.datetime.fromtimestamp(millis / 1000)
            self._rfp = RiverForecastPoints(currentTime)

        segment['pointID'] = pointID
        segment['riverName_GroupName'] = self._rfp.getGroupName(pointID)
        segment['riverName_RiverName'] = self._rfp.getRiverName(pointID)

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
        segment['maxFcstCategory'] = self._rfp.getMaximumForecastCategory(pointID)
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

    def hydrologicCauseMapping(self, hydrologicCause, key):
        mapping = {
            'dam':          {'immediateCause': 'DM', 'typeOfFlooding':'A dam failure in...'},
            'siteImminent': {'immediateCause': 'DM', 'typeOfFlooding':'A dam break in...'},
            'siteFailed':   {'immediateCause': 'DM', 'typeOfFlooding':'A dam break in...'},
            'levee':        {'immediateCause': 'DM', 'typeOfFlooding':'A levee failure in...'},
            'floodgate':    {'immediateCause': 'DR', 'typeOfFlooding':'A dam floodgate release in...'},
            'glacier':      {'immediateCause': 'GO', 'typeOfFlooding':'A glacier-dammed lake outburst in...'},
            'icejam':       {'immediateCause': 'IJ', 'typeOfFlooding':'An ice jam in...'},
            'snowMelt':     {'immediateCause': 'RS', 'typeOfFlooding':'Extremely rapid snowmelt in...'},
            'volcano':      {'immediateCause': 'SM', 'typeOfFlooding':'Extremely rapid snowmelt caused by volcanic eruption in...'},
            'volcanoLahar': {'immediateCause': 'SM', 'typeOfFlooding':'Volcanic induced debris flow in...'},
            'default':      {'immediateCause': 'ER', 'typeOfFlooding':'Excessive rain in...'}
            }
        if mapping.has_key(hydrologicCause):
            return mapping[hydrologicCause][key]
        else:
            return mapping['default'][key]

    def immediateCauseMapping(self, immediateCause):
        mapping = {
            'SM' : 'snowmelt',
            'RS' : 'rain and snowmelt',
            'IJ' : 'an ice jam',
            'DM' : 'a levee failure',
            'DR' : 'a dam floodgate release',
            'GO' : 'a glacier-dammed lake outburst'
            }
        if mapping.has_key(immediateCause):
                return mapping.get(immediateCause)
        return ''