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
    Feb 2015       6599    Robert.Blum       Changed to new style class
    Apr 2015       7271    Chris.Golden      Added reference to new missing time value constant.
    May 2015       7141    Robert.Blum       Simplied module to only format the data that is passed in.
    
    @author Tracy.L.Hansen@noaa.gov
'''

import os
from TextProductCommon import TextProductCommon

class Column(object):
    def __init__(self, variable, width=None, align='^', labelLine1='', labelAlign1='<', labelLine2='', labelAlign2='<'):
        self.variable = variable
        self.width = width
        self.align = align
        self.labelLine1 = labelLine1
        self.labelLine2 = labelLine2
        self.labelAlign1 = labelAlign1
        self.labelAlign2 = labelAlign2

class Table(object):
    '''
    Generic Table - Can be expanded upon in the future if other tables are needed.
    '''
    def __init__(self, columns, dataDict):
        '''
        @param columns -- list of column objects for this table
        @param dataDict - dictionary that contains the data for this table
        '''
        self.columns = columns
        self.dataDict = dataDict

    def makeTable(self):
        '''
        Constructs a table with the passed in columns and dataDict.

        @return: returns a string containing the table
        '''
        tableText = ''
        tableText += self.makeHeadings()
        tableText += self.populateTable()
        return tableText

    def makeHeadings(self):
        '''
        Creates the column headers. Each column header can contain 2 lines.
        '''
        columnHeading1 = ''
        columnHeading2 = ''
        for column in self.columns:
            columnHeading1 += self.format(column.labelLine1, column.width, column.labelAlign1)
            columnHeading2 += self.format(column.labelLine2, column.width, column.labelAlign2)
        columnHeadings =  columnHeading1 + '\n' + columnHeading2 + '\n\n'
        return self.format(columnHeadings)

    def populateTable(self):
        '''
        Populates the table with the supplied data.
        '''
        text = ''
        for key in self.dataDict:
            text += key + '\n'
            # Each entry in the dictionary is a list of sub-dictionaries.
            text += self.makeRows(self.dataDict.get(key)) + '\n'
        return text

    def makeRows(self, dataList):
        '''
        Creates each row of the table by pulling each column variable out of each dictionary
        in the list.
        '''
        rows = ''
        for data in dataList:
            row = ''
            for column in self.columns:
                row += self.format(data.get(column.variable), column.width, column.align) 
            rows += row + '\n'
        return rows

    def format(self, value, width=None, align='<'): 
        value = str(value) 
        if width is None:
            width = len(value)
        formatStr = '{:'+align+str(width)+'}'
        return formatStr.format(value)

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()
