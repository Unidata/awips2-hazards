'''
    Description: Hydro Formatter that contains common hydro functions.
                 Product Specific formatters can inherit from this class.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 31, 2015    4937    Robert.Blum General cleanup
    Feb 20, 2015    4937    Robert.Blum Added groupSummary productPart method
    Mar 17, 2015    6963    Robert.Blum Rounded rfp stage values to a precision of 2.
    Apr 09, 2015    7271    Chris.Golden Changed to use MISSING_VALUE constant.
'''
import datetime
import collections
import types, re, sys
import Legacy_Base_Formatter
from abc import *
from ForecastStageText import ForecastStageText
from TableText import FloodPointTable
from TableText import Column
from com.raytheon.uf.common.time import SimulatedTime

from HazardConstants import MISSING_VALUE

class Format(Legacy_Base_Formatter.Format):

    def initialize(self) :
        self.MISSING_VALUE = MISSING_VALUE
        super(Format, self).initialize()

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
    

    def _groupSummary(self, productDict):
        '''
        For the North Branch Potomac River...including KITZMILLER...CUMBERLAND...Record 
        flooding is forecast.
        '''
        summaryStmt = ''
        for segment in productDict.get('segments', None):
            sections = segment.get('sections', None)
            if sections:
                groupName = sections[0].get('riverName_GroupName', None)
                groupList = sections[0].get('groupForecastPointList', None)
                groupList = groupList.replace(',',', ')
                maxCatName = sections[0].get('groupMaxForecastFloodCatName', None)
                groupSummary = 'For the ' + groupName + '...including ' + groupList + '...' + maxCatName + ' flooding is forecast.'
                summaryStmt += groupSummary + '\n'
        return summaryStmt + '\n'
    
    ################# Segment Level

    ###################### Section Level

    def _observedStageBullet(self, segmentDict):
        if segmentDict.get('observedCategory') < 0:
            bulletContent = 'There is no current observed data.'
        else:
            stageFlowName = segmentDict.get('stageFlowName')
            observedStage = segmentDict.get('observedStage')
            # Round to 2 decimal point - result is a string
            observedStage = format(observedStage, '.2f')
            stageFlowUnits = segmentDict.get('stageFlowUnits')
            observedTime = self._getFormattedTime(segmentDict.get('observedTime_ms'), timeZones=self.timezones)

            bulletContent = 'At '+observedTime+ 'the '+stageFlowName+' was '+ observedStage +' '+stageFlowUnits+'.'
        return '* ' + bulletContent + '\n'

    def _floodStageBullet(self, segmentDict):
        floodStage = segmentDict.get('floodStage')
        if floodStage != self.MISSING_VALUE:
            # Round to 2 decimal point - result is a string
            floodStage = format(floodStage, '.2f')
            bulletContent = 'Flood stage is '+ floodStage +' '+segmentDict.get('stageFlowUnits')+'.'
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
            # Round to 2 decimal point - result is a string
            maxStage = format(maxStage, '.2f')
            observedTime = self._getFormattedTime(segmentDict.get('observedTime_ms'), timeZones=self.timezones).rstrip()
            bulletContent = '* Recent Activity...The maximum river stage in the 24 hours ending at '+observedTime+' was '+ maxStage +' feet. '
        else:
            bulletContent = '* |* Default recentActivityBullet *|'
        return bulletContent + '\n'

    def _forecastStageBullet(self, segmentDict):
        bulletContent = ForecastStageText().getForecastStageText(segmentDict, self.timezones)
        return '* Forecast...' + bulletContent + '\n'

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
            # Round to 2 decimal point - result is a string
            crest = format(float(crest), '.2f')
            crestString = "* Flood History...This crest compares to a previous crest of " + crest + " " + units + " on " + crestDate +"."
        else:
            crestString = '* Flood History...No available flood history available.'
        return crestString + '\n'

    def _floodPointHeader(self, segmentDict):
        # TODO This productPart needs to be completed
        return 'Flood point header' + '\n'

    def _floodPointHeadline(self, segmentDict):
        # TODO This productPart needs to be completed
        return 'Flood point headline' + '\n'

    def _floodPointTable(self, dataDictionary):
#         floodPointDataList = None
#         if dataDictionary.get('floodPointTable', None):
#             # Dictionary has floodPointTable so this is a RVS
#             floodPointDataList = dataDictionary.get('floodPointTable', None)
#         elif dataDictionary.get('segments', None):
#             # Non RVS Product Level Table
#             floodPointDataList = []
#             for segment in dataDictionary.get('segments'):
#                 for section in segment.get('sections', []):
#                     floodPointDataList.append(section)
#         elif dataDictionary.get('sections', None):
#             # Non RVS Segment Level Table
#             floodPointDataList = []
#             for section in dataDictionary.get('sections'):
#                 floodPointDataList.append(section)
#         else:
#             # Non RVS Section Level Table
#             floodPointDataList = [dataDictionary]
# 
#         millis = SimulatedTime.getSystemTime().getMillis() 
#         currentTime = datetime.datetime.fromtimestamp(millis / 1000)
#         rfp = RiverForecastPoints.RiverForecastPoints(currentTime)   
# 
#         columns = []
#         columns.append(Column('floodStage', width=6, align='<', labelLine1='Fld', labelAlign1='<', labelLine2='Stg', labelAlign2='<'))
#         columns.append(Column('observedStage', self._issueTime, width=20, align='<',labelLine1='Observed', labelAlign1='^', labelLine2='Stg    Day    Time', labelAlign2='<'))
#         columns.append(Column('forecastStage_next3days', self._issueTime, width=20, labelLine1='Forecast', labelAlign1='^'))
# 
#         floodPointTableText = ''
#         if (floodPointDataList is not None):
#             floodPointTable = FloodPointTable(floodPointDataList, columns, millis, self.timezones, rfp)
#             floodPointTableText = floodPointTable.makeTable()
# 
#         return(floodPointTableText)

        return( "|* floodPointTable *|")

    ###################### Utility methods

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
