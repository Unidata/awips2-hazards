'''
    Description: Creates tables from row and column definitions.
    
    Currently supports rows per River Forecast Points with columns of values 
    from RiverForecastPoints module.  
    
    It could be expanded to support other kinds of tables as HazardS Services
    expands to more hazard types.
    
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 2015       4375    Tracy Hansen      Initial creation
    @author Tracy.L.Hansen@noaa.gov
'''

import os
from TextProductCommon import TextProductCommon

MISSING_VALUE = -9999

class Column:
    def __init__(self, variable, variableTime=None, width=None, align='^', labelLine1='', labelAlign1='<', labelLine2='', labelAlign2='<'):
        self.variable = variable
        self.variableTime = variableTime
        self.width = width
        self.align = align
        self.labelLine1 = labelLine1
        self.labelLine2 = labelLine2
        self.labelAlign1 = labelAlign1
        self.labelAlign2 = labelAlign2

class FloodPointTable:
    '''
    &&
                        Fld             Observed             Forecast (7 pm)  
    Location            Stg       Stg    Day    Time       Thu   Fri   Sat  
    
    Sulphur River
       Cooper           16        15.7   Wed    7 pm      15.7  15.6  15.5
    
    '''
    def __init__(self, hazardEvents, columns, currentTime_ms, timeZones, rfp):
        self.hazardEvents = hazardEvents
        self.columns = columns
        self.timeZones = timeZones
        self.rfp = rfp
        self.tpc = TextProductCommon()
        self.locationLabelWidth = 16
        self.currentTime_ms = currentTime_ms
    
    def makeTable(self):
        table = '\n&&\n'
        table += self.makeHeadings()
        # Group the hazard events into rivers
        riverGroups = {}
        for hazardEvent in self.hazardEvents:
            streamName = hazardEvent.get('streamName')
            riverGroups.setdefault(streamName, []).append(hazardEvent)
        for streamName in riverGroups:
            table += self.makeRiverRows(streamName, riverGroups.get(streamName))
        return table
            
    def makeHeadings(self): 
        columnHeading1 = self.format('___________', self.locationLabelWidth, align='<')
        columnHeading2 = self.format('Location', self.locationLabelWidth, align='<')
        for column in self.columns:
            columnHeading1 += self.format(column.labelLine1, column.width, column.labelAlign1)
            if column.variable == 'forecastStage_next3days':
                for hazardEvent in self.hazardEvents:
                    pointID = hazardEvent.get('pointID')
                observedTime_ms = self.rfp.getObservedTime(pointID)
                day1Label = self.tpc.getFormattedTime(observedTime_ms + 24*3600*1000, '%a', timeZones=self.timeZones)
                day2Label = self.tpc.getFormattedTime(observedTime_ms + 48*3600*1000, '%a', timeZones=self.timeZones)
                day3Label = self.tpc.getFormattedTime(observedTime_ms + 72*3600*1000, '%a', timeZones=self.timeZones) 
                columnHeading2+= day1Label + '  '+day2Label+ '  '+day3Label
            else:               
                columnHeading2 += self.format(column.labelLine2, column.width, column.labelAlign2)
        return columnHeading1 + columnHeading2 + '\n'
        return self.format(columnHeadings)
           
    def makeRiverRows(self, streamName, hazardEvents): 
        rows = streamName + '\n'           
        for hazardEvent in self.hazardEvents:
            pointID = hazardEvent.get('pointID')
            pointLabel = self.rfp.getRiverPointName(pointID)
            primaryPE = self.rfp.getPrimaryPhysicalElement(pointID)
            row = self.format('   '+pointLabel, self.locationLabelWidth, align='<')
            for column in self.columns:
                row += self.getColumnValue(pointID, primaryPE, column)
            rows += row + '\n'
        return rows
    
    def getColumnValue(self, pointID, primaryPE, column):  
        colValue = ''  
        if column.variable == 'floodStage':
            colValue = self.rfp.getFloodStage(pointID)
        elif column.variable == 'observedStage':
            observedTime = self.rfp.getObservedTime(pointID)
            timeStr = self.formatTime(observedTime, '%a   %I %p', timeZones=self.timeZones)
            observedStage, shefCode = self.rfp.getObservedStage(pointID)
            colValue = `observedStage` + ' ' + timeStr
        elif column.variable == 'forecastStage_next3days':
            fcstTypeSource = self.rfp.getForecastTopRankedTypeSource(pointID)
            observedTime = self.rfp.getObservedTime(pointID)
            baseTime = self.tpc.getFormattedTime(observedTime, '%H%M')
            # Build timeArgs  e.g. '1|1200|1'
            timeArgs = []
            for i in range(3):
                timeArgs.append(str(i+1)+'|'+baseTime+'|1')  
            # TODO Fix getPhysicalElementValue              
            day1 = self.rfp.getPhysicalElementValue(pointID, primaryPE, 0, fcstTypeSource, 'Z', timeArgs[0], timeFlag=False, 
                                                    currentTime_ms=self.currentTime_ms)
            day2 = self.rfp.getPhysicalElementValue(pointID, primaryPE, 0, fcstTypeSource, 'Z', timeArgs[1], timeFlag=False, 
                                                    currentTime_ms=self.currentTime_ms)
            day3 = self.rfp.getPhysicalElementValue(pointID, primaryPE, 0, fcstTypeSource, 'Z', timeArgs[2], timeFlag=False, 
                                                    currentTime_ms=self.currentTime_ms)           
            day1 = '25.0'
            day2 = '26.7'
            day3 = '27.0'
            colValue = self.format(day1, 6) + self.format(day2, 6) + self.format(day3, 6)
        return self.format(colValue, column.width, column.align) 
      
    def format(self, value, width=None, align='<'): 
        if value == MISSING_VALUE:
            value = ' '
        value = str(value) 
        if width is None:
            width = len(value)
        formatStr = '{:'+align+str(width)+'}'
        return formatStr.format(value)   
    
    def formatTime(self, time_ms, format='%a   %I %p', timeZones=[]): 
        timeStr = self.tpc.getFormattedTime(time_ms, '%a   %I %p', timeZones=timeZones) 
        timeStr = timeStr.replace("AM", 'am')
        timeStr = timeStr.replace("PM", 'pm')
        timeStr = timeStr.replace(' 0', ' ')
        return timeStr

    
    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

# For Focal Points to work more on this logic
#
# def generateTestCases(issueTime):
#     columns = []
#     columns.append(Column('floodStage', width=6, align='<', labelLine1='Fld', labelAlign1='<', labelLine2='Stg', labelAlign2='<'))
#     columns.append(Column('observedStage', issueTime, width=20, align='<',labelLine1='Observed', labelAlign1='^', labelLine2='Stg    Day    Time', labelAlign2='<'))
#     columns.append(Column('forecastStage_next3days', issueTime, width=20, labelLine1='Forecast', labelAlign1='^'))
#     hazardEvent = {
#         'pointID': 'DCTN1',
#         'streamName': 'Missouri River',
#         }
#     testCase1 = (hazardEvents, columns)
#     return [testCase1]
#     
# class RiverForecastPoints:
#     def getRiverPointName(self, pointID):
#         return 'Dodge'
#     def getFloodStage(self, pointID):
#         return '35.0'
#     def getObservedStage(self, pointID):
#         return '25.0'
#     def getObservedTime(self, pointID):
#         return 'Wed  7pm'
#     def getPhysicalElement(self, pointID, primaryPE, duration, fcstTypeSource, extremum, timeArg, timeFlag=False):
#         return '28.0'
                            
# testCases = generateTestCases()
# for hazardEvents, columns in testCases:
#     FloodPointTable(hazardEvents, columns, RiverForecastPoints()).makeTable()

