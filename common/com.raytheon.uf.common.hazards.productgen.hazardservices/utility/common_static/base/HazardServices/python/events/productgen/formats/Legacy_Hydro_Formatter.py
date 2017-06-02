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
    Apr 16, 2015    7579    Robert.Blum Updates for amended Product Editor.
    Apr 27, 2015    7579    Robert.Blum Fix error when stageFlowUnits are None.
    May 05, 2015    7141    Robert.Blum Passing required data to the TableText module and
                                        adding back in floodPointTable since it no longer
                                        re-instantiates RiverForecastPoints.
    May 12, 2015    7729    Robert.Blum Changed floodCategoryBullet to use severity from 
                                        the HID and not the database also minor change to
                                        put impacts on the product Editor even if no impacts
                                        were chosen on the HID.
    May 07, 2015    6979    Robert.Blum EditableEntries are passed in for reuse.
    May 13, 2015    7729    Robert.Blum Updated floodCategoryBullet to account for None and
                                        Unknown flood severities.
    May 14, 2015    7376    Robert.Blum Changed to look for only None and not
                                        empty string.
    Jun 02, 2015    7138    Robert.Blum Limited flood point table for RVS based on 
                                        staging dialog input.
    Jul 22, 2015    9640    Robert.Blum Changes for displaying flood table based on checkbox in 
                                        the HID. Also retrieving saved user edited tables from 
                                        productText table. Plus moved all rounding to one common
                                        place so that it can be easily overridden by sites.
    Jul 27, 2015    9637    Robert.Blum Adjustment of && due to adding polygonText product part 
                                        for FL.* hazards.
    Sep 09, 2015    10263   Robert Blum No Forecast bullet if there is no forecast stage.
    Oct 07, 2015    11858   Robert.Blum Removed Group Summary when forecastMaxCategory is
                                        unknown or nonFlood. Also made it so the product part is
                                        not required.
    Nov 10, 2015    12942   Robert.Blum Added logic to flood category bullet for when there is
                                        observed but no forecast category.
    Jan 11, 2016    11888   Roger.Ferrel Added check to prevent duplicate entries in group summary.
    Jan 11, 2016    13008   Roger.Ferrel Remove reference to volcano in typeOfFloodingMapping's SM.
    Jan 14, 2016     9643   Roger.Ferrel Display MSG in createDataDictForFloodPointTable for
                                         missing observedStage.
    Feb 23, 2016    11901   Robert.Blum  Passing issueTime to ForecastStageText module.
    Mar 02, 2016    11898   Robert.Blum  Completely reworked ForecastStageText based on RiverPro's templates.
    Mar 21, 2016    15640   Robert.Blum  Fixed custom edits not getting put in final product.
    May 04, 2016    15584   Kevin.Bisanz Updates to handle stage or flow based river point.
    May 06, 2016    17523   mduff        Fixed the forecast stage bullet to read correctly if no observed data are available.
    May 17, 2016    15010   mduff        Round FL.* products to 1 decimal place.
    May 24, 2016    15584   Kevin.Bisanz Do not convert flow based values
                                         to stage; instead use values which
                                         have been based on primary PE.
    May 26, 2016    16043   Robert.Blum  Rounding values in the flood point table.
'''
import datetime
import collections
import types, re, sys, operator
import Legacy_Base_Formatter
from RiverForecastUtils import RiverForecastUtils
from abc import *
from ForecastStageText import ForecastStageText
from TableText import Table
from TableText import Column

from HazardConstants import MISSING_VALUE

class Format(Legacy_Base_Formatter.Format):
    # String to be displayed in the flood point table if
    # the value is missing in the Hydro database.
    FLOOD_POINT_TABLE_MISSING_VALUE_STRING = 'MSG'

    def initialize(self, editableEntries) :
        self.MISSING_VALUE = MISSING_VALUE
        self._riverForecastUtils = RiverForecastUtils()
        super(Format, self).initialize(editableEntries)

    @abstractmethod
    def execute(self, productDict, editableEntries, overrideProductText):
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
        # Get saved value from productText table if available
        summaryStmt = self._getVal('groupSummary', productDict)
        if summaryStmt is None:
            summaryStmt = ''
            summaryStmts = []
            for segment in productDict.get('segments', []):
                for section in segment.get('sections', []):
                    for hazard in section.get('hazardEvents', []):
                        groupName = hazard.get('riverName_GroupName', '')
                        groupList = hazard.get('groupForecastPointList', '')
                        groupList = groupList.replace(',',', ')
                        maxCatName = hazard.get('groupMaxForecastFloodCatName', '')
                        if maxCatName in ['Unknown', 'NonFlood']:
                            continue
                        groupSummary = 'For the ' + groupName + '...including ' + groupList + '...' + maxCatName + ' flooding is forecast.'
                        if not operator.contains(summaryStmts, groupSummary) :
                            summaryStmts.append(groupSummary)
            if summaryStmts:
                summaryStmt = '\n'.join(summaryStmts)
        self._setVal('groupSummary', summaryStmt, productDict, 'Group Summary', required=False)
        return self._getFormattedText(summaryStmt, endText='\n\n')

    ################# Segment Level

    ###################### Section Level

    def _observedStageBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('observedStageBullet', sectionDict)
        if bulletContent is None:
            # There will only be one hazard per section for point hazards
            hazard = sectionDict.get('hazardEvents')[0]
            if hazard.get('observedCategory') < 0:
                bulletContent = 'There is no current observed data.'
            else:
                observedStageFlow = hazard.get('observedStage')
                (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, observedStageFlow)
                observedTime = self._getFormattedTime(hazard.get('observedTime_ms'), timeZones=self.timezones)
                bulletContent = 'At {0} the {1} was {2}.'.format(observedTime, stageFlowName, combinedValuesUnits)
        self._setVal('observedStageBullet', bulletContent, sectionDict, 'Observed Stage Bullet')
        return self._getFormattedText(bulletContent, startText='* ', endText='\n')

    def _floodStageBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('floodStageBullet', sectionDict)
        if bulletContent is None:
            # There will only be one hazard per section for point hazards
            hazard = sectionDict.get('hazardEvents')[0]
            primaryPE = hazard.get('primaryPE')
            if self._riverForecastUtils.isPrimaryPeStage(primaryPE) :
                floodStageFlow = hazard.get('floodStage')
            else:
                floodStageFlow = hazard.get('floodFlow')

            if floodStageFlow != self.MISSING_VALUE:
                (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, floodStageFlow)
                bulletContent = 'Flood {0} is {1}.'.format(stageFlowName, combinedValuesUnits)
                
            else:
                bulletContent = ''
        self._setVal('floodStageBullet', bulletContent, sectionDict, 'Flood Stage Bullet')
        return self._getFormattedText(bulletContent, startText='* ', endText='\n')

    def _otherStageBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('otherStageBullet', sectionDict)
        if bulletContent is None:
            # TODO This productPart needs to be completed
            bulletContent = '|* Default otherStageBullet *|'
        self._setVal('otherStageBullet', bulletContent, sectionDict, 'Other Stage Bullet', required=False)
        return self._getFormattedText(bulletContent, startText='* ', endText='\n')

    def _floodCategoryBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('floodCategoryBullet', sectionDict)
        if bulletContent is None:
            # There will only be one hazard per section for point hazards
            hazard = sectionDict.get('hazardEvents')[0]
            observedCategory = hazard.get('floodSeverity')
            observedCategoryName = hazard.get('floodSeverityName')
            maxFcstCategory = hazard.get('maxFcstCategory')
            maxFcstCategoryName = hazard.get('maxFcstCategoryName')
            if observedCategory in ['N', 'U']:
                observedCategoryInt = 0
            else:
                observedCategoryInt = int(observedCategory)

            if hazard.get('observedCategory') < 0 and maxFcstCategory > 0:
                bulletContent = maxFcstCategoryName + ' flooding is forecast.'
            elif observedCategoryInt <= 0 and maxFcstCategory > 0:
                bulletContent = maxFcstCategoryName + ' flooding is forecast.'
            elif observedCategoryInt > 0 and maxFcstCategory > 0:
                bulletContent = observedCategoryName + ' flooding is occurring and '+ maxFcstCategoryName+' flooding is forecast.'
            elif observedCategoryInt > 0:
                bulletContent = observedCategoryName + ' flooding is occurring.'
            else:
                if observedCategory is 'U':
                    bulletContent = 'Flooding is forecast.'
                else:
                    bulletContent = 'No flooding is currently forecast.'

        self._setVal('floodCategoryBullet', bulletContent, sectionDict, 'Flood Category Bullet')
        return self._getFormattedText(bulletContent, startText='* ', endText='\n')

    def _recentActivityBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('recentActivityBullet', sectionDict)
        if bulletContent is None:
            bulletContent = ''
            # There will only be one hazard per section for point hazards
            hazard = sectionDict.get('hazardEvents')[0]
            if hazard.get('observedCategory') > 0:

                maxStageFlow = hazard.get('max24HourObservedStage')
                stageFlowUnits =  hazard.get('stageFlowUnits') 

                observedTime = self._getFormattedTime(hazard.get('observedTime_ms'), timeZones=self.timezones).rstrip()

                (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, maxStageFlow)
                bulletContent = 'Recent Activity...The maximum river {0} in the 24 hours ending at {1} was {2}.'\
                            .format(stageFlowName, observedTime, combinedValuesUnits)
        self._setVal('recentActivityBullet', bulletContent, sectionDict, 'Recent Activity Bullet', required=False)
        return self._getFormattedText(bulletContent, startText='* ', endText='\n')
    
    def _stageFlowValuesUnits(self, hazard, stageFlowValue, physicalElement=None):
        '''
        @param hazard
        @param stageFlowValue A stage or flow value corresponding with the physicalElement.
        @param physicalElement Physical element to use.  Or use the hazard's primaryPE if this is None.
        @return Multiple values:
            stageFlowName Name of value (based on the PE)
            stageFlowValue Stage or flow value (based on the PE)
            stageFlowUnits Stage or flow units (based on the PE)
            combinedValuesUnits A combined value/unit string
        '''
        if physicalElement is None:
            physicalElement = hazard.get('primaryPE')

        stageFlowName = self._riverForecastUtils.getStageFlowName(physicalElement)

        stageFlowValue = self._tpc.roundFloat(stageFlowValue, returnString=True)

        stageFlowUnits = self._riverForecastUtils.getStageFlowUnits(physicalElement)

        combinedValuesUnits = "{0} {1}".format(stageFlowValue, stageFlowUnits)

        return stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits

    def _forecastStageBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('forecastStageBullet', sectionDict)
        if bulletContent is None:
            # There will only be one hazard per section for point hazards
            hazard = sectionDict.get('hazardEvents')[0]
            bulletContent = ForecastStageText().getForecastStageText(hazard, self.timezones, self.productDict.get('issueTime'), \
                                                                     sectionDict.get('vtecRecord').get('act'), sectionDict.get('vtecRecord').get('pil'))
        self._setVal('forecastStageBullet', bulletContent, sectionDict, 'Forecast Stage Bullet', required=False)
        return self._getFormattedText(bulletContent, startText='* Forecast...', endText='\n')

    def _getRiverDescription(self, hazardDict):
        '''
        To use the actual river name:
        
        return riverDescription = hazardDict.get('riverName_RiverName')
        '''
        return 'The river'

    def _pointImpactsBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('pointImpactsBullet', sectionDict)
        if bulletContent is None:
            # There will only be one hazard per section for point hazards
            hazard = sectionDict.get('hazardEvents')[0]
            impactStrings = []
            impactsList = hazard.get('pointImpacts', None)
            if impactsList:
                for value, physicalElement, textField in impactsList:
                    (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, value, physicalElement)
                    impactString = 'Impact...At ' + stageFlowValue + ' ' + stageFlowUnits +'...'+textField
                    impactStrings.append(impactString)
                if impactStrings:
                    bulletContent = '\n'.join(impactStrings)
            else:
                bulletContent = ''

        self._setVal('pointImpactsBullet', bulletContent, sectionDict, 'Impacts Bullet', required=False)
        if bulletContent:
            # Add the bullets
            impactStrings = bulletContent.split('\n')
            bulletContent = '' 
            for string in impactStrings:
                bulletContent += '* ' + string + '\n'
        return bulletContent

    def _floodHistoryBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletContent = self._getVal('floodHistoryBullet', sectionDict)
        if bulletContent is None:
            # There will only be one hazard per section for point hazards
            hazard = sectionDict.get('hazardEvents')[0]
            primaryPE = hazard.get('primaryPE')
            crestContents = hazard.get('crestsSelectedForecastPointsComboBox', None)
            if crestContents is not None:
                crest,crestDate = crestContents.split(' ')
                (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, crest)
                bulletContent = "Flood History...This crest compares to a previous crest of " + stageFlowValue + " " + stageFlowUnits + " on " + crestDate +"."
            else:
                bulletContent = 'Flood History...No available flood history available.'
        self._setVal('floodHistoryBullet', bulletContent, sectionDict, 'Flood History Bullet', required=False)
        return self._getFormattedText(bulletContent, startText='* ', endText='\n')

    def _floodPointHeader(self, sectionDict):
        # Get saved value from productText table if available
        header = self._getVal('floodPointHeader', sectionDict)
        if header is None:
            # TODO This productPart needs to be completed
            header = 'Flood point header'
        self._setVal('floodPointHeader', header, sectionDict, 'Flood Point Header')
        return self._getFormattedText(header, endText='\n')

    def _floodPointHeadline(self, segmentDict):
        # Get saved value from productText table if available
        headline = self._getVal('floodPointHeadline', sectionDict)
        if headline is None:
            # TODO This productPart needs to be completed
            headline = 'Flood point headline'
        self._setVal('floodPointHeadline', headline, sectionDict, 'Flood Point Headline')
        return self._getFormattedText(headline, endText='\n')

    def _floodPointTable(self, dictionary):
        text = ''
        HazardEventDicts = self.getDataForFloodPointTable(dictionary)
        if HazardEventDicts:
            # Get saved value from productText table if available
            text = self._getVal('floodPointTable', dictionary)
            if text is None:
                columns = []
                # Create the columns
                columns.append(Column('location', width=16, align='<', labelLine1='___________', labelAlign1='<',  labelLine2='Location', labelAlign2='<'))
                columns.append(Column('floodStage', width=6, align='<', labelLine1='Fld', labelAlign1='<', labelLine2='Stg', labelAlign2='<'))
                columns.append(Column('observedStage', width=20, align='<',labelLine1='Observed', labelAlign1='^', labelLine2='Stg    Day  Time', labelAlign2='<'))

                hazardEventDict = HazardEventDicts[0]
                observedTime_ms = hazardEventDict.get('observedTime_ms')
                day1Label = self._tpc.getFormattedTime(observedTime_ms + 24*3600*1000, '%a', timeZones=self.timezones)
                day2Label = self._tpc.getFormattedTime(observedTime_ms + 48*3600*1000, '%a', timeZones=self.timezones)
                day3Label = self._tpc.getFormattedTime(observedTime_ms + 72*3600*1000, '%a', timeZones=self.timezones) 
                headerLabel = day1Label + '   '+day2Label+ '   '+day3Label

                timeStr = self.formatTime(observedTime_ms, format='%I %p', timeZones=self.timezones)
                columns.append(Column('forecastStage_next3days', width=20, align='<', labelLine1='Forecast ' + timeStr, labelAlign1='<', labelLine2=headerLabel, labelAlign2='<'))
     
                # Got the column headings now make the required data structure for the table
                tableValuesDict = self.createDataDictForFloodPointTable(HazardEventDicts)
    
                # Got the columns and data now format the table.
                floodPointTable = Table(columns, tableValuesDict)
                text = floodPointTable.makeTable()
        self._setVal('floodPointTable', text, dictionary, 'Flood Point Table', required=False)

        endText = ''
        if text:
            endText = '\n\n'
        return self._getFormattedText(text.rstrip(), startText='\n&&\n\n', endText=endText)

    ###################### Utility methods

    def getDataForFloodPointTable(self, dictionary):
        HazardEventDicts = []

        # Check if flood table was checked on staging dialog - RVS
        if self._productID == 'RVS' and 'include' not in dictionary.get('floodPointTable', []):
            return HazardEventDicts

        if dictionary.get('segments', None):
            # Product Level Table
            for segment in dictionary.get('segments'):
                for section in segment.get('sections', []):
                    for hazard in section.get('hazardEvents', []):
                        if 'selectFloodPointTable' in hazard.get('include', []):
                            HazardEventDicts.append(hazard)
        elif dictionary.get('sections', None):
            # Segment Level Table
            for section in dictionary.get('sections'):
                for hazard in section.get('hazardEvents', []):
                    if 'selectFloodPointTable' in hazard.get('include', []):
                        HazardEventDicts.append(hazard)
        else:
            # RVS or Section Level Table
            for hazard in dictionary.get('hazardEvents', []):
                if 'selectFloodPointTable' in hazard.get('include', []) or self._productID == 'RVS':
                    HazardEventDicts.append(hazard)
        return HazardEventDicts

    def createDataDictForFloodPointTable(self, HazardEventDicts):
        # Group the hazards based on streamName
        riverGroups = {}
        for hazardEvent in HazardEventDicts:
            streamName = hazardEvent.get('streamName')
            riverGroups.setdefault(streamName, []).append(hazardEvent)

        # Dictionary that will hold all the column values for all the rows in the table.
        tableValuesDict = {}
        for streamName in riverGroups:
            # Could have multiple rows for the same streamName
            listOfValueDicts = []
            for hazard in riverGroups.get(streamName):
                # Dictionary that will hold the column values for this one row in the table.
                valueDictionary = {}
                # Info needed to create column values
                observedTime = hazard.get('observedTime_ms')
                timeStr = self.formatTime(observedTime, '%a    %I %p', timeZones=self.timezones)
                # Round - result is a string
                os = hazard.get('observedStage')
                if os != self.MISSING_VALUE :
                    observedStage = self._tpc.roundFloat(os, returnString=True)
                else:
                    observedStage = self.FLOOD_POINT_TABLE_MISSING_VALUE_STRING
                floodStage = self._tpc.roundFloat(hazard.get('floodStage'), returnString=True)
                day1 = self._tpc.roundFloat(hazard.get('day1'))
                day2 = self._tpc.roundFloat(hazard.get('day2'))
                day3 = self._tpc.roundFloat(hazard.get('day3'))
                next3DaysValue = self.format(day1, 6) + self.format(day2, 6) + self.format(day3, 6)

                # Add all the column values to the dictionary
                valueDictionary['location'] = hazard.get('riverPointName')
                valueDictionary['floodStage'] = floodStage
                valueDictionary['observedStage'] = observedStage + '   ' + timeStr
                valueDictionary['forecastStage_next3days'] = next3DaysValue

                # Add the dictionary to the list
                listOfValueDicts.append(valueDictionary)
            tableValuesDict[streamName] = listOfValueDicts
        return tableValuesDict 

    def format(self, value, width=None, align='<'):
        value = str(value)
        if value == str(self.MISSING_VALUE):
            value = self.FLOOD_POINT_TABLE_MISSING_VALUE_STRING
        if width is None:
            width = len(value)
        formatStr = '{:'+align+str(width)+'}'
        return formatStr.format(value)


    def formatTime(self, time_ms, format='%a    %I %p', timeZones=[]): 
        timeStr = self._tpc.getFormattedTime(time_ms, format, timeZones=timeZones)
        timeStr = timeStr.replace("AM", 'am')
        timeStr = timeStr.replace("PM", 'pm')
        timeStr = timeStr.replace(' 0', '  ')
        return timeStr

    def typeOfFloodingMapping(self, immediateCuase):
        mapping = {
            'DM' : 'A levee failure',
            'DR' : 'A dam floodgate release',
            'GO' : 'A glacier-dammed lake outburst',
            'IJ' : 'An ice jam',
            'RS' : 'Extremely rapid snowmelt',
            'SM' : 'Snowmelt'
            }
        if mapping.has_key(immediateCuase):
            return mapping[immediateCuase]
        else:
            return ''
