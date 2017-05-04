# 
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
#
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
#
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
#
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.

#
# This python class is used to query for IHFS (Hydro) database data.
# The functions supported are used for different types of IHFS data 
# See comments in the below functions for additional query details. 
#
#
#     SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    Jul 28, 2015    8839          Chris.Cody     Initial version
#    Aug 14, 2015    9988          Chris.Cody     Refine queries and add Aggregate query
#
#

import JUtil
from collections import OrderedDict
from java.util import HashMap, LinkedHashMap, ArrayList

from com.raytheon.uf.common.hazards.ihfs.data import AbstractTableData
from com.raytheon.uf.common.hazards.ihfs.helper import IhfsQueryHelper
from com.raytheon.uf.common.hazards.ihfs.helper import QueryPredicateHelper


class IhfsQuery(object):
    
    #
    # IHFS Observed Table Names supported by this Python class
    #
    IHFS_TABLE_DICT = {
                       # pressure, procvalue, rawpc, and rawpp are handlers
                       'A' : 'Agricultural',
                       'E' : 'Evaporation',
                       'F' : 'Fishcount',
                       'G' : 'Ground',
                       'H' : 'Height',
                       'I' : 'Ice',
                       'L' : 'Lake',
                       'M' : 'Moisture',
                       'N' : 'Gatedam',
                       #
                       'P' : 'Precip', # This is not actually an IHFS Table
                       'Q' : 'Discharge',
                       'R' : 'Radiation',
                       'S' : 'Snow',
                       'T' : 'Temperature',
                       'U' : 'Wind',
                       'V' : 'Power',
                       'W' : 'Waterquality',
                       'X' : 'Weather',
                       'Y' : 'Yunique' }
                       
    #
    # IHFS Forecast Table Names supported by this Python class
    #
    IHFS_FORECAST_TABLE_DICT = {
                       'H' : 'Fcstheight',
                       'P' : 'Fcstprecip',
                       'Q' : 'Fcstdischarge',
                       'T' : 'Fcsttemperature' }


    IHFS_PROCVALUE = 'PROCVALUE'                  # 'procvalue'
    IHFS_SHEF_PROCOBS = 'SHEF_PROCOBS'            # 'shef_procobs' This is not an IFHS table
    IHFS_CONTINCENCY_VALUE = 'CONTINGENCYVALUE'   # 'Contingencyvalue'
    IHFS_PRESSURE = 'PRESSURE'                    # 'Pressure' 
    IHFS_RAWPP = 'RAWPP'                          # 'Rawpp'
    IHFS_RAWPC = 'RAWPC'                          # 'Rawpc'
    IHFS_FCSTOTHER = 'FCSTOTHER'                  # 'Fcstother'
    IHFS_INVALID = 'INVALID'
    IHFS_OBSERVED_TYPE = 'OBSERVED'
    IHFS_FORECAST_TYPE = 'FORECAST'
    IHFS_OTHER_TYPE = 'OTHER'
    
    def __init__(self):
        pass

    #
    # getHydroQueryType(tableName)
    #
    # Get the Type of Hydro (IHFS) Query for the given Table Name.
    #
    # @param tableName IHFS Table Name
    # @return "FORECAST" or "OBSERVED" based on tableName input
    #
    def getHydroQueryType(self, tableName):
        queryType = self.IHFS_OTHER_TYPE
        
        for value in self.IHFS_FORECAST_TABLE_DICT.itervalues():
            if value == tableName:
                queryType = self.IHFS_FORECAST_TYPE
                break
        
        if queryType == self.IHFS_OTHER_TYPE:
            for value in self.IHFS_TABLE_DICT.itervalues():
                if value == tableName:
                    queryType = self.IHFS_OBSERVED_TYPE
                    break
        return queryType
    

    #
    # getIhfsShefTableName(pe, ts)
    #
    # Get the IHFS (Hydro) Table name for the given pe, ts combination
    #
    # @param pe  Physical Element
    # @param ts  Type Source
    #
    def getIhfsShefTableName(self, pe, ts):
        
        tableName = self.IHFS_PROCVALUE
        processAsObserved = False;
        matchFound = False;
        
        if pe is not None:
            pe = pe.upper()
        else:
            return
        
        if ts is not None:
            ts = ts.upper()
        else:
            return
    
        if ts.startswith('P') == True:

            procObs = AppsDefaults.getInstance().getToken(self.IHFS_SHEF_PROCOBS);
            if procObs is not None:
                procObs = procObs.upper()
                if procObs == 'ON':
                    processAsObserved = True
                else:
                    return tableName
            else:
                return tableName

        if ts.startswith('C') == True:
            return self.IHFS_CONTINCENCY_VALUE

        #if observed data or processed data being treated as observed 
        if ts.startswith('R') == True or processAsObserved == True or ts.startswith("XX") == True:
            for key, value in self.IHFS_TABLE_DICT.iteritems():
                if pe.startswith(key) == True:
                    if pe.startswith('P') == True:
                        if pe =='PA' or pe == 'PD' or pe == 'PE' or pe == 'PL':
                            tableName = self.IHFS_PRESSURE
                        else:
                            if pe == 'PP':
                                tableName = self.IHFS_RAWPP
                            else:
                                tableName = self.IHFS_RAWPC
                        matchFound = True
                        break
                    else:
                        tableName = self.IHFS_TABLE_DICT.get(pe[:1])
                        matchFound = True;
                        break
        else:
            if ts.startswith('F') == True:
                # Forecast data
                tableName = self.IHFS_FORECAST_TABLE_DICT.get(pe[:1])
                if tableName is None:
                    tableName = self.IHFS_FCSTOTHER
                matchFound = True
            else: 
                # "Invalid type-source specified
                tableName = self.IHFS.INVALID
                matchFount = False

        if matchFound == False:
            tableName = self.IHFS.INVALID

        return tableName;


    #
    # translateIhfsJavaToPy(javaDataList)
    # 
    # Translate a (Java) List of Java Objects into a python list of dictionary of label:value objects
    #
    # @param javaDataList (Java) List of Java Objects (queried from IHFS)
    # @retyrn Python list of dictionary label:value objects where label is a TABLE.COLUMN name reference.
    #  
    def translateIhfsJavaToPy(self, javaDataList):
        pyReturnList = None        

        pyReturnList = None
        if javaDataList is not None:
            pyReturnList = []
            for javaObject in javaDataList:
                javaMap = javaObject.getColumnValueMap()
                pyDict = JUtil.javaObjToPyVal(javaMap)
                pyReturnList.append(pyDict)
                
        return pyReturnList
    
    #            
    # buildAndExecuteShefQuery(pe, ts, dur, extremum, startTime, endTime, basisTime=None)
    #
    # Primary External Interface function.
    # Query for IHFS (Hydro) Shef (Observed and Forecast) data for the given parameters.
    # Return a Python list of dictionary label:value objects.
    #
    # The queried SHEF tables are:
    # Observed: 
    #     Agricultural, Evaporation, Fishcount, Ground, Height, Ice, Lake, Moisture, Gatedam, 
    #     Discharge, Radiation, Snow, Temperature, Wind, Power, Waterquality, Weather, and Yunique.
    # Forecast: 
    #     Fcstheight, Fcstprecip, Fcstdischarge, and Fcsttemperature.
    #
    # The parameters are parsed into an SQL statement using an equals ('=') for all parameters, 
    # except for startTime and endTime which use greater than or equal to ('>=') and less than ('<') respectively.
    # 
    # The returned data is parsed from a Java List of Java data objects into a Python List of
    # Label:Value Dictionary elements.
    # Basis time will also use an equals ('=') operator.
    #
    # @param pe  Physical Element (Also used to determine which SHEF table to query)
    # @param ts  Type Source (Also used to determine which SHEF table to query)
    # @param dur  Duration (integer duration)
    # @param extremum  Extremum 
    # @param startTime  Starting Date Time  (Observed: OPSTIME, Forecast: VALIDTIME)
    #                    String in the form "YYYY-mm-DD HH:MM:SS" 
    # @param endTime  Ending Date Time  (Observed: OPSTIME, Forecast: VALIDTIME)
    #                    String in the form "YYYY-mm-DD HH:MM:SS" 
    # @param basisTime  Basis Time (Valid only for Forecast data queries)
    #                    String in the form "YYYY-mm-DD HH:MM:SS" 
    # @return Python list of dictionary label:value objects for the query
    #
    def buildAndExecuteShefQuery(self, pe, ts, dur, extremum, startTime, endTime, basisTime=None):
        
        ihfsTableName = self.getIhfsShefTableName(pe, ts)
         
        ihfsQueryHelper = IhfsQueryHelper.getInstance()

        queryHelperPredicateList = ArrayList();
        if pe is not None:
            columnName = 'PE'
            tableAndColumnName = ihfsTableName + '.' + columnName
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '=', pe)
            queryHelperPredicateList.add(queryHelperPredicate)

        if ts is not None:
            columnName = 'TS'
            tableAndColumnName = ihfsTableName + '.' + columnName
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '=', ts)
            queryHelperPredicateList.add(queryHelperPredicate)

        if dur is not None:
            columnName = 'DUR'
            durStr = str(dur)
            tableAndColumnName = ihfsTableName + '.' + columnName
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '=', durStr)
            queryHelperPredicateList.add(queryHelperPredicate)

        if extremum is not None:
            columnName = 'EXTREMUM'
            tableAndColumnName = ihfsTableName + '.' + columnName
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '=', extremum)
            queryHelperPredicateList.add(queryHelperPredicate)
        
        queryType = self.getHydroQueryType(ihfsTableName)
        if queryType == 'OBSERVED':
            columnName = 'OBSTIME'
            tableAndColumnName = ihfsTableName + '.' + columnName
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '>=', startTime)
            queryHelperPredicateList.add(queryHelperPredicate)
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '<', endTime)
            queryHelperPredicateList.add(queryHelperPredicate)
        if queryType == 'FORECAST':
            columnName = 'VALIDTIME'
            tableAndColumnName = ihfsTableName + '.' + columnName
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '>=', startTime)
            queryHelperPredicateList.add(queryHelperPredicate)
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '<', endTime)
            queryHelperPredicateList.add(queryHelperPredicate)

            if basisTime is not None:
                columnName = 'BASISTIME'
                tableAndColumnName = ihfsTableName + '.' + columnName
                queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '=', basisTime)
                queryHelperPredicateList.add(queryHelperPredicate)

        if queryType == 'OTHER':
            return []
                
        javaReturnList = ihfsQueryHelper.buildAndExecuteSingleTableQuery(ihfsTableName, None, queryHelperPredicateList, None, None)
        
        pyReturnList = translateIhfsJavaToPy(javaReturnList)

        return pyReturnList

    # 
    # buildAndExecuteIhfsQuery(tableName, selectTableColumnList, queryPredicateTupleList)
    #
    # This function is used to compile and execute more complex and flexible queries for IHFS Data.
    # 
    # For example: to create the query string:
    #    SELECT
    #    FPINFO.LID, FPINFO.NAME, FPINFO.COUNTY, ...., FPINFO.MAJOR_FLOW 
    #    FROM
    #    FPINFO, COUNTIES
    #    WHERE
    #    FPINFO.COUNTY = COUNTIES.COUNTY
    #    AND COUNTIES.WFO = 'OAX'
    # The following python script code  can be used:
    #    selectColumnList = None   
    #             A None for this parameter includes all columns in the result.
    #    fpInfoTableName = 'FPINFO'
    #    countiesTableName = 'COUNTIES'
    #    queryPredicateTupleList = []
    #    queryPredicateTupleList.append( ("AND", fpInfoTableName +'.COUNTY', '=', countiesTableName +'.COUNTY'))
    #    queryPredicateTupleList.append( ("AND", countiesTableName +'.WFO', '=', 'OAX'))
    #    
    #    ihfsQuery = IhfsQuery()
    #    pyIhfsDataList = ihfsQuery.buildAndExecuteIhfsQuery(fpInfoTableName, selectColumnList, queryPredicateTupleList)
    #    pyIhfsDataList is a Python List [] of Dictionary { } objects of Label:Value pairs for each queries column
    #
    # @param tableName Name of IHFS Table (all caps)
    # @param selectTableColumnList List of IHFS Table.Column strings (all caps) to include in the query output
    #                              A  Python value of None will cause all fields from the queried table to be included
    # @param queryPredicateTupleList  A list of Query Predicate Tuples (conjunction, Table.Column, operator, value)
    #                     conjunction     : 'AND' or 'OR' (this is ignored for the first tuple in the List)
    #                     Table.Column    : <Table Name>.<Column Name> for the predicate
    #                     operator        : A Valid Operator [ =, <>, <, <=, >, >=, IN (   ), ]
    #                     value           : Value for the predicate. This can be a literal value '2015-07-11 00:00:00' 
    #                                       (i.e. a valid date time string)
    #                                       Or it can reference another <Table Name>.<Column Name>.
    # @param orderByList  A List of "Order By" directives (e.g. "FPINFO.LID DESC") [Optional]
    # @param groupByList  A List of Group By directives (List of <Table>.<ColumnName> strings [Optional]
    # @return Python list of dictionary label:value objects for the query
    #
    def buildAndExecuteIhfsQuery(self, tableName, selectTableColumnList, queryPredicateTupleList, orderByList=None, groupByList=None):
        
        ihfsQueryHelper = IhfsQueryHelper.getInstance()
        tableName = tableName.upper()
        javaSelectColumnList = None
        if selectTableColumnList is not None:
            javaSelectColumnList = ArrayList()
            for selectTableColumn in selectTableColumnList:
                selectTableColumn = selectTableColumn.upper()
                if selectTableColumn.startswith(tableName):
                    javaSelectColumnList.add(selectTableColumn)
                else:
                    javaSelectColumnList.add(tableName + "." + selectTableColumn)
    
        queryHelperPredicateList = ArrayList();
        for conjunction, tableColumnName, operator, predicateValue in queryPredicateTupleList:
            if isinstance(predicateValue, list):
                javaQueryPredicate = ihfsQueryHelper.createQueryPredicateInHelper(conjunction, tableColumnName, operator, javaList)
            else:
                javaQueryPredicate = ihfsQueryHelper.createQueryPredicateHelper(conjunction, tableColumnName, operator, predicateValue)
            queryHelperPredicateList.add(javaQueryPredicate)
        
        javaOrderByList = None
        if orderByList is not None:
            javaOrderByList = ArrayList()
            for orderBy in orderByList:
                javaOrderByList.add(orderBy)
                
        javaGroupByList = None
        if groupByList is not None:
            javaGroupByList = ArrayList()
            for groupBy in groupByList:
                javaGroupByList.add(groupBy)
                
        javaReturnList = ihfsQueryHelper.buildAndExecuteSingleTableQuery(tableName, javaSelectColumnList, queryHelperPredicateList, javaOrderByList, groupByList)
        
        if javaReturnList is not None:
            pyReturnList = self.translateIhfsJavaToPy(javaReturnList)
        else:
            pyReturnList = []
            
        return pyReturnList

    # 
    # buildAndExecuteIhfsAggregateQuery(tableName, aggregateFunction, aggregateColumn, queryPredicateTupleList)
    #
    # This function is used to compile and execute more complex and flexible queries for IHFS Data.
    # 
    # For example: to create the query string:
    #    SELECT MAX(OBSTIME) 
    #    FROM
    #    LATESTOBSVALUE
    #    WHERE
    #        PE = 'HG'
    # The following python script code  can be used:
    #    tableName = 'LATESTOBSVALUE'
    #    aggregateFunction = 'MAX'
    #    aggregateColumn = tableName + 'OBSTIME'
    #    queryPredicateTupleList = []
    #    queryPredicateTupleList.append( ("AND", tableName +'.PE', '=', 'HG'))
    #    
    #    ihfsQuery = IhfsQuery()
    #    pyIhfsData = ihfsQuery.buildAndExecuteIhfsAggregateQuery(tableName, aggregateFunction, aggregateColumn, queryPredicateTupleList)
    #    pyIhfsDataList is a single value (it may be an Integer, Long, Double, Float or String depending on the column tyoe)
    #
    # @param tableName Name of IHFS Table (all caps)
    # @param aggregateFunction  Aggregate function ('MIN', 'MAX', 'COUNT', 'SUM'
    # @param aggregateColumn <Table>.<Column> for the Aggregate function
    #                              A  Python value of None will cause all fields from the queried table to be included
    # @param queryPredicateTupleList  A list of Query Predicate Tuples (conjunction, Table.Column, operator, value)
    #                     conjunction     : 'AND' or 'OR' (this is ignored for the first tuple in the List)
    #                     Table.Column    : <Table Name>.<Column Name> for the predicate
    #                     operator        : A Valid Operator [ =, <>, <, <=, >, >=, IN (   ), ]
    #                     value           : Value for the predicate. This can be a literal value '2015-07-11 00:00:00' 
    #                                       (i.e. a valid date time string)
    #                                       Or it can reference another <Table Name>.<Column Name>.
    # @return Aggregate Function Value
    #
    def buildAndExecuteIhfsAggregateQuery(self, tableName, aggregateFunction, aggregateColumn, queryPredicateTupleList):
        ihfsQueryHelper = IhfsQueryHelper.getInstance()

        tableName = tableName.upper()
        javaSelectColumn = None
        
        if aggregateFunction is None:
            return -1
        else:
            aggregateFunction = aggregateFunction.upper()
            if aggregateFunction != "MIN" and aggregateFunction != "MAX" and \
                aggregateFunction != "COUNT":
                return -1
            
        if aggregateColumn is not None:
                aggregateColumn = aggregateColumn.upper()
                if aggregateColumn.startswith(tableName):
                    javaSelectColumn = aggregateColumn
                else:
                    javaSelectColumn = tableName + "." + aggregateColumn
    
        queryHelperPredicateList = ArrayList();
        for conjunction, tableColumnName, operator, predicateValue in queryPredicateTupleList:
            if isinstance(predicateValue, list):
                javaQueryPredicate = ihfsQueryHelper.createQueryPredicateInHelper(conjunction, tableColumnName, operator, javaList)
            else:
                javaQueryPredicate = ihfsQueryHelper.createQueryPredicateHelper(conjunction, tableColumnName, operator, predicateValue)
            queryHelperPredicateList.add(javaQueryPredicate)
             
        javaReturn = ihfsQueryHelper.buildAndExecuteAggregateQuery(tableName, aggregateFunction, aggregateColumn, queryHelperPredicateList)
        pyValue = JUtil.javaObjToPyVal(javaReturn)
        
        return pyValue

        
    