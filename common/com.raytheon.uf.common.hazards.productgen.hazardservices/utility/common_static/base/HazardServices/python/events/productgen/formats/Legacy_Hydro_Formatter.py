'''
    Description: Hydro Formatter that contains common hydro functions.
                 Product Specific formatters can inherit from this class.
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
import HazardConstants

class Format(Legacy_Base_Formatter.Format):
    # String to be displayed in the flood point table if
    # the value is missing in the Hydro database.
    FLOOD_POINT_TABLE_MISSING_VALUE_STRING = 'MSG'

    def initialize(self, editableEntries) :
        self._riverForecastUtils = RiverForecastUtils()
        super(Format, self).initialize(editableEntries)

    @abstractmethod
    def execute(self, productDict, editableEntries):
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

    def groupSummary(self, productDict, productPart):
        '''
        For the North Branch Potomac River...including KITZMILLER...CUMBERLAND...Record 
            flooding is forecast.
        '''
        summaryStmt = ''
        summaryStmts = []
        for segment in productDict.get('segments', []):
            for section in segment.get('sections', []):
                for hazard in section.get('hazardEvents', []):
                    groupName = hazard.get('groupName', '')
                    groupList = hazard.get('groupForecastPointList', '')
                    groupList = groupList.replace(',',', ')
                    maxCatName = hazard.get('groupMaxForecastFloodCatName', '')
                    forecastCategoryName = hazard.get('floodCategoryForecastName')
                    if forecastCategoryName:
                        # The forecast category name has been set in the HID, use it.
                        maxCatName = forecastCategoryName
                    if maxCatName in ['Unknown', 'NonFlood']:
                        continue
                    groupSummary = 'For the ' + groupName + '...including ' + groupList + '...' + maxCatName + ' flooding is forecast.'
                    if not operator.contains(summaryStmts, groupSummary) :
                        summaryStmts.append(groupSummary)
        if summaryStmts:
            summaryStmt = '\n'.join(summaryStmts)
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(summaryStmt)
        return self.getFormattedText(productPart, endText='\n\n')
    ################# Segment Level

    ###################### Section Level

    def observedStageBullet(self, sectionDict, productPart):
        # There will only be one hazard per section for point hazards
        hazard = sectionDict.get('hazardEvents')[0]
        if hazard.get('floodCategoryObserved') < 0:
            bulletContent = 'There is no current observed data.'
        else:
            observedStageFlow = hazard.get('observedStage')
            (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, observedStageFlow)
            observedTime = self._getFormattedTime(hazard.get('observedTime_ms'), timeZones=self.timezones)
            bulletContent = 'At {0} the {1} was {2}.'.format(observedTime, stageFlowName, combinedValuesUnits)
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        return self.getFormattedText(productPart, startText='* ', endText='\n')

    def floodStageBullet(self, sectionDict, productPart):
        # There will only be one hazard per section for point hazards
        hazard = sectionDict.get('hazardEvents')[0]
        primaryPE = hazard.get('primaryPE')
        if self._riverForecastUtils.isPrimaryPeStage(primaryPE) :
            floodStageFlow = hazard.get('floodStage')
        else:
            floodStageFlow = hazard.get('floodFlow')

        if floodStageFlow != HazardConstants.MISSING_VALUE:
            (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, floodStageFlow)
            bulletContent = 'Flood {0} is {1}.'.format(stageFlowName, combinedValuesUnits)
        else:
            bulletContent = ''
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        return self.getFormattedText(productPart, startText='* ', endText='\n')

    def otherStageBullet(self, sectionDict, productPart):
        # TODO This productPart needs to be completed
        bulletContent = '|* Default otherStageBullet *|'
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        return self.getFormattedText(productPart, startText='* ', endText='\n')

    def floodCategoryBulletWorker(self, category, categoryName, action):
        '''
        Constructs a part of the flood category bullet based on the inputs.
        @param category Flood category (e.g. -1,0,1,2,3,4)
        @param categoryName Flood category name (e.g. Minor, Moderate, Major, Record)
        @param action Word describing the time frame of the data (e.g. "occurring"
               for observed data or "forecast" for forecast data)
        @return String for the flood category bullet or None if no flooding.
        '''
        bulletContent = None

        if category is not None:
            categoryInt = int(category)

            if categoryInt > 0 and categoryName:     # Minor/Moderate/Major/Record flooding
                bulletContent = categoryName + ' flooding is ' + action
            elif categoryInt == 0:  # No flooding
                bulletContent = None
            elif categoryInt == -1:   # Unknown flooding
                bulletContent = 'Flooding is ' + action
            else:
                raise Exception('Unhandled flood category:{0} categoryName:{1}'.format(category, categoryName))

        return bulletContent

    def floodCategoryBullet(self, sectionDict, productPart):
        # There will only be one hazard per section for point hazards
        hazard = sectionDict.get('hazardEvents')[0]
        phen= hazard.get('phen')
        sig = hazard.get('sig')
        phenSig = phen + '.' + sig

        observedCategory = hazard.get('floodCategoryObserved')
        observedCategoryName = hazard.get('floodCategoryObservedName')
        forecastCategory = hazard.get('floodCategoryForecast')
        forecastCategoryName = hazard.get('floodCategoryForecastName')
        if phenSig in ['HY.S']:
            observedCategory = '0'
            observedCategoryName = ''
            forecastCategory = '0'
            forecastCategoryName = ''
        elif phenSig in ['FL.A']:
            # Nothing observed during a watch, or else it would not be a watch
            observedCategory = '0'
            observedCategoryName = ''

        observedText = self.floodCategoryBulletWorker(observedCategory, observedCategoryName, 'occurring')
        forecastText = self.floodCategoryBulletWorker(forecastCategory, forecastCategoryName, 'forecast')

        if phenSig in ['FL.A']:
            # FL.A uses "<category> is possible"
            # FL.W uses "<category> is forecast"
            forecastText = self.floodCategoryBulletWorker(forecastCategory, forecastCategoryName, 'possible')

        if observedText and forecastText :
            bulletContent = '{0} and {1}.'.format(observedText, forecastText)
        elif observedText:
            bulletContent = '{0}.'.format(observedText)
        elif forecastText:
            bulletContent = '{0}.'.format(forecastText)
        else:
            bulletContent = 'No flooding is currently forecast.'

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        return self.getFormattedText(productPart, startText='* ', endText='\n')


    def recentActivityBullet(self, sectionDict, productPart):
        bulletContent = ''
        # There will only be one hazard per section for point hazards
        hazard = sectionDict.get('hazardEvents')[0]
        if hazard.get('floodCategoryObserved') > 0:
            maxStageFlow = hazard.get('max24HourObservedStage')
            stageFlowUnits =  hazard.get('stageFlowUnits') 

            observedTime = self._getFormattedTime(hazard.get('observedTime_ms'), timeZones=self.timezones).rstrip()

            (stageFlowName, stageFlowValue, stageFlowUnits, combinedValuesUnits) = self._stageFlowValuesUnits(hazard, maxStageFlow)
            bulletContent = 'Recent Activity...The maximum river {0} in the 24 hours ending at {1} was {2}.'\
                        .format(stageFlowName, observedTime, combinedValuesUnits)
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        return self.getFormattedText(productPart, startText='* ', endText='\n')
    
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

    def forecastStageBullet(self, sectionDict, productPart):
        # There will only be one hazard per section for point hazards
        hazard = sectionDict.get('hazardEvents')[0]
        bulletContent = ForecastStageText().getForecastStageText(hazard, self.timezones, self.productDict.get('issueTime'), \
                                                                 sectionDict.get('vtecRecord').get('act'), sectionDict.get('vtecRecord').get('pil'))
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        return self.getFormattedText(productPart, startText='* Forecast...', endText='\n')

    def _getRiverDescription(self, hazardDict):
        '''
        To use the actual river name:
        
        return riverDescription = hazardDict.get('streamName')
        '''
        return 'The river'

    def pointImpactsBullet(self, sectionDict, productPart):
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

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        if bulletContent:
            # Add the bullets
            impactStrings = bulletContent.split('\n')
            bulletContent = '' 
            for string in impactStrings:
                bulletContent += '* ' + string + '\n'
        return bulletContent

    def floodHistoryBullet(self, sectionDict, productPart):
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

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletContent)
        return self.getFormattedText(productPart, startText='* ', endText='\n')

    def floodPointTable(self, dictionary, productPart):
        text = ''
        HazardEventDicts = self.getDataForFloodPointTable(dictionary)
        if HazardEventDicts:
            columns = []
            # Create the columns
            columns.append(Column('location', width=16, align='<', labelLine1='', labelAlign1='<',  labelLine2='Location', labelAlign2='<'))
            columns.append(Column('floodStage', width=6, align='<', labelLine1='Fld', labelAlign1='<', labelLine2='Stg', labelAlign2='<'))
            columns.append(Column('observedStage', width=16, align='<',labelLine1='Observed', labelAlign1='<', labelLine2='Stg   Day/Time', labelAlign2='<'))

            hazardEventDict = HazardEventDicts[0]
            day0ValTime_ms = hazardEventDict.get('day0ValTime_ms', HazardConstants.MISSING_VALUE)
            if day0ValTime_ms != HazardConstants.MISSING_VALUE:
                day1Label = self._tpc.getFormattedTime(day0ValTime_ms + 24*3600*1000, '%a', timeZones=self.timezones)
                day2Label = self._tpc.getFormattedTime(day0ValTime_ms + 48*3600*1000, '%a', timeZones=self.timezones)
                day3Label = self._tpc.getFormattedTime(day0ValTime_ms + 72*3600*1000, '%a', timeZones=self.timezones)
                timeStr = self.formatTime(day0ValTime_ms, format='%I %p', timeZones=self.timezones)
            else:
                day1Label = 'd1 '
                day2Label = 'd2 '
                day3Label = 'd3 '
                timeStr = 'unknown time'
            headerLabel = day1Label + '   '+day2Label+ '   '+day3Label
            columns.append(Column('forecastStage_next3days', width=18, align='<', labelLine1='Forecasts (' + timeStr + ")", labelAlign1='<', labelLine2=headerLabel, labelAlign2='<'))
 
            # Got the column headings now make the required data structure for the table
            tableValuesDict = self.createDataDictForFloodPointTable(HazardEventDicts)

            # Got the columns and data now format the table.
            floodPointTable = Table(columns, tableValuesDict)
            text = floodPointTable.makeTable()

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(text.rstrip())
        endText = ''
        if text:
            endText = '\n\n'
        return self.getFormattedText(productPart, startText='\n&&\n\n\n', endText=endText)

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
                if observedTime != HazardConstants.MISSING_VALUE:
                    timeStr = self.formatTime(observedTime, format='%a %I %p', timeZones=self.timezones)
                else:
                    timeStr = 'unknown'
                # Round - result is a string
                os = hazard.get('observedStage')
                if os != HazardConstants.MISSING_VALUE :
                    observedStage = self._tpc.roundFloat(os, returnString=True)
                else:
                    observedStage = self.FLOOD_POINT_TABLE_MISSING_VALUE_STRING
                floodStage = self._tpc.roundFloat(hazard.get('floodStage'), returnString=True)
                day1 = hazard.get('day1', HazardConstants.MISSING_VALUE)
                if day1 != HazardConstants.MISSING_VALUE:
                    day1 = self._tpc.roundFloat(day1)
                day2 = hazard.get('day2', HazardConstants.MISSING_VALUE)
                if day2 != HazardConstants.MISSING_VALUE:
                    day2 = self._tpc.roundFloat(day2)
                day3 = hazard.get('day3', HazardConstants.MISSING_VALUE)
                if day3 != HazardConstants.MISSING_VALUE:
                    day3= self._tpc.roundFloat(day3)
                next3DaysValue = self.format(day1, width=6) + self.format(day2, width=6) + self.format(day3, width=6)

                # Add all the column values to the dictionary
                valueDictionary['location'] = hazard.get('riverPointName')
                valueDictionary['floodStage'] = self.format(floodStage, width=6)
                valueDictionary['observedStage'] = self.format(observedStage, width=6) + self.format(timeStr, width=10)
                valueDictionary['forecastStage_next3days'] = next3DaysValue

                # Add the dictionary to the list
                listOfValueDicts.append(valueDictionary)
            tableValuesDict[streamName] = listOfValueDicts
        return tableValuesDict 

    def format(self, value, width=None, align='<'):
        value = str(value)
        if value == HazardConstants.MISSING_VALUE_STR:
            value = self.FLOOD_POINT_TABLE_MISSING_VALUE_STRING
        if width is None:
            width = len(value)
        formatStr = '{:'+align+str(width)+'}'
        return formatStr.format(value)

    def formatTime(self, time_ms, format='%a    %I %p', timeZones=[]): 
        timeStr = self._tpc.getFormattedTime(time_ms, format, timeZones=timeZones)
        timeStr = timeStr.replace("AM", 'am')
        timeStr = timeStr.replace("PM", 'pm')
        timeStr = timeStr.replace(' 0', ' ')
        return timeStr

    def typeOfFloodingMapping(self, immediateCuase):
        mapping = {
            'DM' : 'A levee failure',
            'DR' : 'A dam floodgate release',
            'GO' : 'A glacier-dammed lake outburst',
            'IJ' : 'An ice jam',
            'RS' : '', #Blanked out to match GFE products
            'SM' : 'Snowmelt'
            }
        if mapping.has_key(immediateCuase):
            return mapping[immediateCuase]
        else:
            return ''
