'''
    Description: Hydro Formatter that contains common hydro functions.
                 Product Specific formatters can inherit from this class.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
'''
import datetime
import collections
import types, re, sys
import Legacy_Base_Formatter
from abc import *

# Need to import this for the Missing_Value_Constant
import RiverForecastPoints

class Format(Legacy_Base_Formatter.Format):

    def initialize(self) :
        self.MISSING_VALUE = RiverForecastPoints.Missing_Value_Constant
        super(Format, self).initialize()

    @abstractmethod
    def _processProductParts(self, eventSet):
        '''
        Must be overridden by the Product Fromatter
        '''
        pass

    @abstractmethod
    def execute(self, eventSet, dialogInputMap):
        '''
        Must be overridden by the Product Formatter
        '''
        pass

    ######################################################
    #  Hydro Product Part Methods 
    #  Note that these product part methods are specific 
    #  to hydro products. These methods are shared between all 
    #  hydro product formatters. If a change does not apply for 
    #  all products the method must be overridden in that specific formatter.
    ######################################################

    ################# Product Level

    def _floodPointTable(self, productDict):
        '''
                                        FLD OBSERVED    24 HR FORECASTS (7 AM)
        LOCATION                        STG STG DAY/TIME CHG  THU FRI    SAT SUN
        TOMBIGBEE RIVER BASIN
            LOWER BEVILL L&D            122 110.0 6 AM 7.1 117.0 122.0 126.0 124.0
            LOWER GAINESVILLE L&D       101  78.6 6 AM 2.3  83.0  92.0  98.5  96.0
            LOWER DEMOPOLIS L&D          68  43.6 7 AM 1.4  44.0  45.0  52.0  68.0
        '''
        # TODO Determine if product or segment level
        return '| floodPointTable Placeholder |'

    ################# Segment Level

    ###################### Section Level

    def _observedStageBullet(self, segmentDict):
        if segmentDict['observedCategory'] < 0:
            bulletContent = 'There is no current observed data.'
        else:
            stageFlowName = segmentDict['stageFlowName']
            observedStage = segmentDict['observedStage']
            stageFlowUnits = segmentDict['stageFlowUnits']
            observedTime = self._getFormattedTime(segmentDict['observedTime_ms'], timeZones=self.timezones)

            bulletContent = 'At '+observedTime+ 'the '+stageFlowName+' was '+`observedStage`+' '+stageFlowUnits+'.'
        return bulletContent

    def _floodStageBullet(self, segmentDict):
        floodStage = segmentDict['floodStage']
        if floodStage != self.MISSING_VALUE:
            bulletContent = 'Flood stage is '+`floodStage`+' '+segmentDict['stageFlowUnits']+'.'
        else:
            bulletContent = ''
        return bulletContent

    def _otherStageBullet(self, segmentDict):
        bulletContent = '* |* Default otherStageBullet *|'
        return bulletContent

    def _floodCategoryBullet(self, segmentDict):
        observedCategory = segmentDict['observedCategory']
        observedCategoryName = segmentDict['observedCategoryName']
        maxFcstCategory = segmentDict['maxFcstCategory']
        maxFcstCategoryName = segmentDict['maxFcstCategoryName']
        if observedCategory <= 0 and maxFcstCategory > 0:
            bulletContent = maxFcstCategoryName + ' flooding is forecast.'
        elif observedCategory > 0 and maxFcstCategory > 0:
            bulletContent = observedCategoryName + ' flooding is occurring and '+maxFcstCategoryName+' flooding is forecast.'
        else:
            action = segmentDict['hazards'][0]['act']
            if action == 'ROU' or (observedCategory == 0 and maxFcstCategory < 0):
                bulletContent = 'No flooding is currently forecast.'
            else:
                bulletContent = '|* Default floodCategoryBullet *|'
        return bulletContent

    def _recentActivityBullet(self, segmentDict):
        bulletContent = '* '
        if segmentDict['observedCategory'] > 0:
            maxStage = segmentDict['max24HourObservedStage']
            observedTime = self._getFormattedTime(segmentDict['observedTime_ms'], timeZones=self.timezones)
            bulletContent = '* Recent Activity...The maximum river stage in the 24 hours ending at '+observedTime+' was '+`maxStage`+' feet. '
        else:
            bulletContent = '* |* Default recentActivityBullet *|'
        return bulletContent

    def _forecastStageBullet(self, segmentDict):
        action = segmentDict['hazards'][0]['act']
        riverDescription = self._getRiverDescription(segmentDict)
        forecastCrest = '30 feet'

        maximumForecastStage = segmentDict['maximumForecastStage']
        maximumForecastTime_ms = segmentDict['maximumForecastTime_ms']
        maximumForecastTime_str = self._getFormattedTime(maximumForecastTime_ms, emptyValue='at time unknown', timeZones=self.timezones)
        observedStage = segmentDict['observedStage']
        floodStage = segmentDict['floodStage']
        stageFlowUnits = segmentDict['stageFlowUnits']
        forecastCrestTime_ms = segmentDict['forecastCrestTime_ms']
        forecastCrestTime_str = self._getFormattedTime(forecastCrestTime_ms, timeZones=self.timezones)
        forecastFallBelowFloodStageTime_ms = segmentDict['forecastFallBelowFloodStageTime_ms']
        forecastFallBelowFloodStageTime_str = self._getFormattedTime(forecastFallBelowFloodStageTime_ms, timeZones=self.timezones)
        forecastRiseAboveFloodStageTime_ms = segmentDict['forecastRiseAboveFloodStageTime_ms']
        forecastRiseAboveFloodStageTime_str = self._getFormattedTime(forecastRiseAboveFloodStageTime_ms, timeZones=self.timezones)

        bulletContent = ''
        # Create bullet content
        if maximumForecastStage == self.MISSING_VALUE :
            bulletContent = '|* Forecast is missing, insert forecast bullet here. *|'
        elif action != 'ROU':
            if observedStage == self.MISSING_VALUE:
                if maximumForecastStage >= floodStage:
                    bulletContent = riverDescription + ' is forecast to have a maximum value of '+`maximumForecastStage`+' '+stageFlowUnits+\
                      ' by '+maximumForecastTime_str+'. '
                elif maximumForecastStage < floodStage:
                    bulletContent = riverDescription + ' is forecast below flood stage with a maximum value of '+`maximumForecastStage`+' '+stageFlowUnits+\
                      ' by '+maximumForecastTime_str+'. '
            elif observedStage < floodStage:
                if maximumForecastStage == floodStage:
                    bulletContent = riverDescription + ' is expected to rise to near flood stage by '+ maximumForecastTime_str
                elif forecastCrest > floodStage and forecastFallBelowFloodStageTime_ms == self.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by '+ forecastRiseAboveFloodStageTime_str + \
                        ' and continue to rise to near ' + `forecastCrest` + ' '+stageFlowUnits + ' by '+ forecastCrestTime_str+'. '
                elif maximumForecastStage > floodStage and forecastCrest == self.MISSING_VALUE and +\
                    forecastFallBelowFloodStageTime_ms == self.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by '+forecastRiseAboveFloodStageTime_str +\
                       ' and continue to rise to near '+`maximumForecastStage`+' '+stageFlowUnits+' by '+\
                       maximumForecastTime_str+'. Additional rises are possible thereafter.'
                elif forecastCrest > floodStage and forecastFallBelowFloodStageTime_ms != self.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by '+forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near '+ `forecastCrest`+' '+stageFlowUnits+' by '+forecastCrestTime_str + \
                       '. The river will fall below flood stage by '+forecastFallBelowFloodStageTime_str+'. '
            else:
                if maximumForecastStage > observedStage and forecastCrest == self.MISSING_VALUE and \
                     forecastFallBelowFloodStageTime_ms == self.MISSING_VALUE:
                     bulletContent = riverDescription + ' will continue rising to near '+ `maximumForecastStage`+' '+stageFlowUnits + \
                     ' by '+ maximumForecastTime_str + '. Additional rises may be possible thereafter. '
                elif forecastCrest > observedStage and forecastFallBelowFloodStageTime_ms == self.MISSING_VALUE:
                        bulletContent = riverDescription + ' will continue rising to near '+`forecastCrest`+' '+stageFlowUnits+' by '+\
                        forecastCrestTime_str+ ' then begin falling.'
                elif forecastCrest > observedStage and forecastFallBelowFloodStageTime_ms != self.MISSING_VALUE and \
                    forecastCrest > observedStage:
                    bulletContent = riverDescription + ' will continue rising to near '+`forecastCrest`+' '+stageFlowUnits+' by '+\
                       forecastFallBelowFloodStageTime_ms+'. ' 
                elif maximumForecastStage <= observedStage and stageTrend == 'falling' and \
                    forecastFallBelowFloodStageTime_ms == self.MISSING_VALUE:
                    # TODO Need SpecFcstStg and SpecFcstStgTime
                    bulletContent = ''
                elif maximumForecastStage <= observedStage and stageTrend == 'steady' and \
                    forecastFallBelowFloodStageTime_ms == self.MISSING_VALUE:
                    bulletContent = riverDescription + ' will remain near '+`maximumForecastStage`+' '+stageFlowUnits+'. '
                elif maximumForecastStage <= observedStage and forecastFallBelowFloodStageTime_ms != self.MISSING_VALUE:
                    bulletContent = riverDescription + ' will continue to fall to below flood stage by '+forecastFallBelowFloodStageTime_str+'.'
        elif action in ['ROU']:
            if maximumForecastStage != self.MISSING_VALUE:
                bulletContent = riverDescription + ' will rise to near '+`maximumForecastStage`+' '+stageFlowUnits+\
                      ' by '+maximumForecastTime_str+'. '
        return '* Forecast...' + bulletContent

    def _getRiverDescription(self, segmentDict):
        '''
        To use the actual river name:
        
        return riverDescription = segmentDict['riverName_RiverName']
        '''
        return 'The river'

    def _pointImpactsBullet(self, segmentDict):
        impactStrings = []
        impactsList = segmentDict.get('pointImpacts', None)
        if impactsList:
            for height, textField in impactsList:
                impactString = '* Impact...At ' + height + ' feet...'+textField
                impactStrings.append(impactString)
            if impactStrings:
                impactBulletsString = '\n'.join(impactStrings)
                impactBulletsString += '\n'
        else:
            impactBulletsString = ''

        return impactBulletsString

    def _floodHistoryBullet(self, segmentDict):
        crestString = ''
        crestContents = segmentDict.get('crestsSelectedForecastPointsComboBox', None)
        units = segmentDict.get('impactCompUnits', '')
        if crestContents is not None:
            crest,crestDate = crestContents.split(' ')
            # <ImpCompUnits>
            crestString = "This crest compares to a previous crest of " + crest + " " + units + " on " + crestDate +"."
        else:
            crestString = 'Flood History...No available flood history available.'
        return crestString

    def _floodPointHeader(self):
        # TODO This productPart needs to be completed
        return 'Flood point header'

    def _floodPointHeadline(self):
        # TODO This productPart needs to be completed
        return 'Flood point headline'

    ###################### Utility methods

    def getAreaPhrase(self, segmentDict):
        if segmentDict.get('geoType') == 'area':
            immediateCause = segmentDict.get('immediateCause')
            ugcList = []
            for area in segmentDict['impactedAreas']:
                ugcList.append(area['ugc'])
            ugcPhrase = self._tpc.getAreaPhrase(ugcList)

            if immediateCause in ['DM', 'DR', 'GO', 'IJ','RS', 'SM']:
                hydrologicCause = segmentDict.get('hydrologicCause')
                riverName = None
                if immediateCause == 'DM' and hydrologicCause in ['dam', 'siteImminent', 'siteFailed']:
                    damOrLeveeName = self._tpc.getProductStrings(hazardEvent, metaData, 'damOrLeveeName')
                    if damOrLeveeName:
                        damInfo = self._damInfo().get(damOrLeveeName)
                        if damInfo:
                            riverName = damInfo.get('riverName')
                    if not riverName or not damOrLeveeName:
                        return ugcPhrase
                    else:
                        return 'The '+riverName+' below '+damOrLeveeName+ ' in ' + ugcPhrase
            return ugcPhrase
        else:
            #  <River> <Proximity> <IdName> 
            riverName = segmentDict['riverName_GroupName']
            proximity = segmentDict['proximity']
            # TODO Occasionally proximity comes back as None.
            # What should we make the default in that case?
            if proximity is None:
                proximity = 'near'
            riverPointName = segmentDict['riverPointName']
            return  'the '+riverName + ' '+ proximity + ' ' + riverPointName