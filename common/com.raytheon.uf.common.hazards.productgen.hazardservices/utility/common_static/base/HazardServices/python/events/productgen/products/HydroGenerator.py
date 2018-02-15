
'''
    Description: Hydro Product Generator that all Hydro Product
                 specific generators should inherit from.
'''
import JUtil
from com.raytheon.uf.common.hazards.hydro import RiverForecastManager
from HydroProductParts import HydroProductParts
from com.raytheon.uf.common.time import SimulatedTime
from HazardConstants import MISSING_VALUE, MISSING_VALUE_STR
from GeneralConstants import MILLIS_PER_HOUR
import datetime
import Legacy_Base_Generator
from abc import *

class Product(Legacy_Base_Generator.Product):
    def __init__(self):
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
        if self._riverForecastManager is None:
            self._riverForecastManager = RiverForecastManager()
        
        hazardEventDict['pointImpacts'] = self.preparePointImpacts(hazardEvent)
        hazardEventDict['crestsSelectedForecastPointsComboBox'] = hazardEvent.get('crestsSelectedForecastPointsComboBox')

        primaryPE = hazardEvent.get('primaryPE')
        if not primaryPE:
            # Must have a PE to do the below queries
            return

        # Spec values
        forecastTypeSource = self._riverForecastManager.getTopRankedTypeSource(pointID, primaryPE, 0, 'Z')
        rankedForecastTypeSources = self._riverForecastManager.getRankedTypeSources(pointID, primaryPE, 0, 'Z')
        jResult = self._riverForecastManager.getPhysicalElementValue(
                pointID, primaryPE, 0, forecastTypeSource, 'Z', '4|1200|1', millis)
        result = JUtil.javaObjToPyVal(jResult)
        hazardEventDict['specValue'] = result.get('value')
        hazardEventDict['specTime'] = result.get('validtime')

        # Next 3 days values for FloodPointTable
        baseTime = self._tpc.getFormattedTime(millis, '%H%M')
        # Build timeArgs  e.g. '1|1200|1'
        timeArgs = []
        for i in range(3):
            timeArgs.append(str(i+1)+'|'+baseTime+'|3')

        day1Val = MISSING_VALUE
        day2Val = MISSING_VALUE
        day3Val = MISSING_VALUE
        day0ValTime_ms = MISSING_VALUE

        # If data is not available at one type source, attempt to find data using the next lowest ranked type source
        for typeSource in rankedForecastTypeSources:
            day1JResult = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[0], millis)
            day2JResult = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[1], millis)
            day3JResult = self._riverForecastManager.getPhysicalElementValue(pointID, primaryPE, 0, forecastTypeSource, 'Z', timeArgs[2], millis)

            day1Result = JUtil.javaObjToPyVal(day1JResult)
            day2Result = JUtil.javaObjToPyVal(day2JResult)
            day3Result = JUtil.javaObjToPyVal(day3JResult)

            day1Val = day1Result.get('value')
            day2Val = day2Result.get('value')
            day3Val = day3Result.get('value')

            # Subtract hours to get the current day/time to compensate for the
            # formatter adding N days.
            if day1Val != MISSING_VALUE:
                day0ValTime_ms = day1Result.get('validtime')
                day0ValTime_ms -= 24 * MILLIS_PER_HOUR
                break
            elif day2Val != MISSING_VALUE:
                day0ValTime_ms = day2Result.get('validtime')
                day0ValTime_ms -= 48 * MILLIS_PER_HOUR
                break
            elif day3Val != MISSING_VALUE:
                day0ValTime_ms = day3Result.get('validtime')
                day0ValTime_ms -= 72 * MILLIS_PER_HOUR
                break

        hazardEventDict['day1'] = day1Val
        hazardEventDict['day2'] = day2Val
        hazardEventDict['day3'] = day3Val
        hazardEventDict['day0ValTime_ms'] = day0ValTime_ms

        time = self._tpc.getFormattedTime(millis, '%H%M')

        jResult = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HT", 0, "FF", 'X', '0|' + time + '|1', millis)
        result = JUtil.javaObjToPyVal(jResult)
        hazardEventDict['HT0FFXNext'] = result.get('value')
        hazardEventDict['HT0FFXNextTime'] = result.get('validtime')

        jResult = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HP", 0, "FF", 'X', '0|' + time + '|1', millis)
        result = JUtil.javaObjToPyVal(jResult)
        hazardEventDict['HP0FFXNext'] = result.get('value')
        hazardEventDict['HP0FFXNextTime'] = result.get('validtime')

        jResult = self._riverForecastManager.getPhysicalElementValue(
                pointID, "HG", 0, "FF", 'X', '0|' + time + '|1', millis)
        result = JUtil.javaObjToPyVal(jResult)
        hazardEventDict['HG0FFXNext'] = result.get('value')
        hazardEventDict['HG0FFXNextTime'] = result.get('validtime')

        jResult = self._riverForecastManager.getPhysicalElementValue(
                pointID, "QR", 0, "FF", 'X', '0|' + time + '|1', millis)
        result = JUtil.javaObjToPyVal(jResult)
        hazardEventDict['QR0FFXNext'] = result.get('value')
        hazardEventDict['QR0FFXNextTime'] = result.get('validtime')

    def preparePointImpacts(self, hazardEvent):
        # Pull out the list of chosen impact text fields
        impacts = []
        validVals = hazardEvent.get('impactCheckBoxes')
        
        if validVals is None:
            return None
        for key in validVals:
            if key.startswith('impactCheckBox_'):
                stageFlow, physicalElement, impactValue = self.parseImpactKey(key)
                if stageFlow is not None and physicalElement is not None and impactValue is not None:
                    textFieldName = 'impactTextField_'+impactValue
                    textFieldValue = hazardEvent.get(textFieldName)
                    #Ignore impacts lacking a text field value
                    if textFieldValue is not None:
                        impacts.append((stageFlow, physicalElement, textFieldValue))
        return impacts

    def parseImpactKey(self, key):
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
