'''
    Description: Hydro Formatter that contains common hydro functions.
                 Product Specific formatters can inherit from this class.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 31, 2015    4937    Robert.Blum General cleanup
'''
import datetime
import collections
import types, re, sys
import Legacy_Base_Formatter
from abc import *
from ForecastStageText import ForecastStageText

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
        return '\n| floodPointTable Placeholder |\n'

    ################# Segment Level

    ###################### Section Level

    def _observedStageBullet(self, segmentDict):
        if segmentDict.get('observedCategory') < 0:
            bulletContent = 'There is no current observed data.'
        else:
            stageFlowName = segmentDict.get('stageFlowName')
            observedStage = segmentDict.get('observedStage')
            stageFlowUnits = segmentDict.get('stageFlowUnits')
            observedTime = self._getFormattedTime(segmentDict.get('observedTime_ms'), timeZones=self.timezones)

            bulletContent = 'At '+observedTime+ 'the '+stageFlowName+' was '+`observedStage`+' '+stageFlowUnits+'.'
        return '* ' + bulletContent + '\n'

    def _floodStageBullet(self, segmentDict):
        floodStage = segmentDict.get('floodStage')
        if floodStage != self.MISSING_VALUE:
            bulletContent = 'Flood stage is '+`floodStage`+' '+segmentDict.get('stageFlowUnits')+'.'
        else:
            bulletContent = ''
        return '* ' + bulletContent + '\n'

    def _otherStageBullet(self, segmentDict):
        # TODO This productPart needs to be completed
        bulletContent = '|* Default otherStageBullet *|'
        return '* ' + bulletContent + '\n'

    def _floodCategoryBullet(self, segmentDict):
        observedCategory = segmentDict.get('observedCategory')
        observedCategoryName = segmentDict.get('observedCategoryName')
        maxFcstCategory = segmentDict.get('maxFcstCategory')
        maxFcstCategoryName = segmentDict.get('maxFcstCategoryName')
        if observedCategory <= 0 and maxFcstCategory > 0:
            bulletContent = maxFcstCategoryName + ' flooding is forecast.'
        elif observedCategory > 0 and maxFcstCategory > 0:
            bulletContent = observedCategoryName + ' flooding is occurring and '+maxFcstCategoryName+' flooding is forecast.'
        else:
            action = segmentDict.get('vtecRecord').get('act')
            if action == 'ROU' or (observedCategory == 0 and maxFcstCategory < 0):
                bulletContent = 'No flooding is currently forecast.'
            else:
                bulletContent = '|* Default floodCategoryBullet *|'
        return '* ' + bulletContent + '\n'

    def _recentActivityBullet(self, segmentDict):
        bulletContent = '* '
        if segmentDict.get('observedCategory') > 0:
            maxStage = segmentDict.get('max24HourObservedStage')
            observedTime = self._getFormattedTime(segmentDict.get('observedTime_ms'), timeZones=self.timezones)
            bulletContent = '* Recent Activity...The maximum river stage in the 24 hours ending at '+observedTime+' was '+`maxStage`+' feet. '
        else:
            bulletContent = '* |* Default recentActivityBullet *|'
        return bulletContent + '\n'

    def _forecastStageBullet(self, segmentDict):
        bulletContent = ForecastStageText().getForecastStageText(segmentDict, self.timezones)
        return '* Forecast...' + bulletContent

    def _getRiverDescription(self, segmentDict):
        '''
        To use the actual river name:
        
        return riverDescription = segmentDict.get('riverName_RiverName')
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
        return crestString + '\n'

    def _floodPointHeader(self, segmentDict):
        # TODO This productPart needs to be completed
        return 'Flood point header' + '\n'

    def _floodPointHeadline(self, segmentDict):
        # TODO This productPart needs to be completed
        return 'Flood point headline' + '\n'

    ###################### Utility methods

    def getAreaPhrase(self, segmentDict):
        if segmentDict.get('geoType') == 'area':
            immediateCause = segmentDict.get('immediateCause')
            areaPhrase  = self.getAreaPhraseBullet(segmentDict)
            ugcList = []
            for area in segmentDict['impactedAreas']:
                ugcList.append(area['ugc'])
            ugcPhrase = self._tpc.getAreaPhrase(ugcList)

            if immediateCause in ['DM', 'DR', 'GO', 'IJ','RS', 'SM']:
                hydrologicCause = segmentDict.get('hydrologicCause')
                riverName = None
                if immediateCause == 'DM' and hydrologicCause in ['dam', 'siteImminent', 'siteFailed']:
                    damOrLeveeName = segmentDict.get('damOrLeveeName', '')
                    if damOrLeveeName:
                        damInfo = self._damInfo().get(damOrLeveeName)
                        if damInfo:
                            riverName = damInfo.get('riverName')
                    if not riverName or not damOrLeveeName:
                        return areaPhrase
                    else:
                        return 'The '+riverName+' below '+damOrLeveeName+ ' in...\n' + areaPhrase
                else:
                    typeOfFlooding = self.typeOfFloodingMapping(immediateCause)
                    return typeOfFlooding + ' in...\n' + areaPhrase
            return areaPhrase
        else:
            #  <River> <Proximity> <IdName> 
            riverName = segmentDict.get('riverName_GroupName')
            proximity = segmentDict.get('proximity')
            # TODO Occasionally proximity comes back as None.
            # What should we make the default in that case?
            if proximity is None:
                proximity = 'near'
            riverPointName = segmentDict.get('riverPointName')
            return  'the '+riverName + ' '+ proximity + ' ' + riverPointName

    def typeOfFloodingMapping(self, immediateCuase):
        mapping = {
            'DM' : 'A levee failure',
            'DR' : 'A dam floodgate release',
            'GO' : 'A glacier-dammed lake outburst',
            'IJ' : 'An ice jam',
            'RS' : 'Extremely rapid snowmelt',
            'SM' : 'Extremely rapid snowmelt caused by volcanic eruption'
            }
        if mapping.has_key(immediateCuase):
            return mapping[immediateCuase]
        else:
            return ''

    def immediateCauseMapping(self, immediateCauseCode):
        immediateCauseDict = {"ER":"excessive rain",
                              "SM":"snowmelt",
                              "RS":"rain and snowmelt", 
                              "DM":"a dam or levee failure",
                              "DR":"a dam floodgate release",
                              "GO":"a glacier-dammed lake outburst",
                              "IJ":"an ice jam", 
                              "IC":"rain and/or snow melt and/or ice jam",
                              "FS":"upstream flooding plus storm surge", 
                              "FT":"upstream flooding plus tidal effects",
                              "ET":"elevated upstream flow plus tidal effects",
                              "WT":"wind and/or tidal effects",
                              "OT":"other effects",
                              "MC":"multiple causes",
                              "UU":"Unknown" }
        immediateCauseText = immediateCauseDict[immediateCauseCode]
        return immediateCauseText
