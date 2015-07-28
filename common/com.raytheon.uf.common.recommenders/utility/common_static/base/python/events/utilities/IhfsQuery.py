# #
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
#
#

import JUtil
from collections import OrderedDict
from java.util import HashMap, LinkedHashMap, ArrayList

from com.raytheon.uf.common.hazards.ihfs.data import AbstractTableData
from com.raytheon.uf.common.hazards.ihfs.helper import IhfsQueryHelper
from com.raytheon.uf.common.hazards.ihfs.helper import QueryPredicateHelper


class IhfsQuery(object):
    
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
    
    def __init__(self):
        pass

    def getHydroQueryType(self, tableName):
        queryType = 'OTHER'
        for value in self.IHFS_FORECAST_TABLE_DICT.itervalues():
            if value == tableName:
                queryType = 'FORECAST'
                break
        
        if queryType == 'OTHER':
            for value in self.IHFS_TABLE_DICT.itervalues():
                if value == tableName:
                    queryType = 'OBSERVED'
                    break
        return queryType
    

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
    
                
    # buildAndExecuteShefQuery(self, pe, ts, dur, extremum, startTime, endTime, basisTime=None)
    # Where:
    #    pe        : Physical Element (Also used to determine which SHEF table to query)
    #    ts        : Type Source (Also used to determine which SHEF table to query)
    #    dur       : Duration
    #    extremum  : Extremum 
    #    startTime : Starting Date Time  (Observed: OPSTIME, Forecast: VALIDTIME)
    #    endTime   : Ending Date Time  (Observed: OPSTIME, Forecast: VALIDTIME)
    #    basisTime : Basis Time (Valid only for Forecast data queries)
    # addresses existing, and known needs for querying the IHFS database for SHEF data. 
    # The queried SHEF tables are:
    # Observed: 
    #     Agricultural, Evaporation, Fishcount, Ground, Height, Ice, Lake, Moisture, Gatedam, 
    #     Discharge, Radiation, Snow, Temperature, Wind, Power, Waterquality, Weather, and Yunique.
    # Forecast: 
    #     Fcstheight, Fcstprecip, Fcstdischarge, and Fcsttemperature.
    #
    #
    # The parameters are parsed into an SQL statement using an equals ('=') for all parameters, 
    # except for startTime and endTime which use greater than or equal to ('>=') and less than ('<') respectively.
    # 
    # The returned data is parsed from a Java List of Java data objects into a Python List of
    # Label:Value Dictionary elements.
    # Basis time will also use an equals ('=') operator.
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
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '>=', timeStart)
            queryHelperPredicateList.add(queryHelperPredicate)
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '<', timeStart)
            queryHelperPredicateList.add(queryHelperPredicate)
        if queryType == 'FORECAST':
            columnName = 'VALIDTIME'
            tableAndColumnName = ihfsTableName + '.' + columnName
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '>=', timeStart)
            queryHelperPredicateList.add(queryHelperPredicate)
            queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '<', timeStart)
            queryHelperPredicateList.add(queryHelperPredicate)

            if basisTime is not None:
                columnName = 'BASISTIME'
                tableAndColumnName = ihfsTableName + '.' + columnName
                queryHelperPredicate = ihfsQueryHelper.createQueryPredicateHelper('AND', tableAndColumnName, '=', basisTime)
                queryHelperPredicateList.add(queryHelperPredicate)

        if queryType == 'OTHER':
            return []
                
        javaReturnList = ihfsQueryHelper.buildAndExecuteSingleTableQuery(ihfsTableName, None, queryHelperPredicateList)
        
        pyReturnList = translateIhfsJavaToPy(javaReturnList)

        return pyReturnList
            
    # The function:
    #    buildAndExecuteIhfsQuery(self, tableName, selectTableColumnList, queryPredicateTupleList)
    # is used to compile and execute more complex and flexible queries.
    # Where:
    #    tableName      : Name of IHFS Table (all caps)
    #    selectTableColumnList    : List of IHFS Table.Column strings (all caps) to include in the query output.
    #                    A  Python value of None will cause all fields from the queried table to be included
    #    queryPredicateTupleList : A list of Query Predicate Tuples (conjunction, Table.Column, operator, value)
    #    conjunction     : 'AND' or 'OR' (this is ignored for the first tuple in the List)
    #    Table.Column    : <Table Name>.<Column Name> for the predicate
    #    operator        : A Valid Operator [ =, <>, <, <=, >, >=, IN (   ), ]
    #    value           : Value for the predicate. This can be a literal value '2015-07-11 00:00:00' 
    #                    (a valid date time string)
    #                    Or it can reference another <Table Name>.<Column Name>.
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
    #    selectColumnList = None   #A None for this parameter includes all columns in the result.
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
    def buildAndExecuteIhfsQuery(self, tableName, selectTableColumnList, queryPredicateTupleList):
        
        ihfsQueryHelper = IhfsQueryHelper.getInstance()

        tableName = tableName.upper()
        javaSelectColumnList = None
        if selectTableColumnList is not None:
            javaSelectColumnList = ArrayList()
            for selectTableColumn in selectTableColumnList:
                selectTableColumn = selectTableColumn.upper()
                if selectTableColumn.startsWith(tableName):
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
             
        javaReturnList = ihfsQueryHelper.buildAndExecuteSingleTableQuery(tableName, javaSelectColumnList, queryHelperPredicateList)
        
        if javaReturnList is not None:
            pyReturnList = self.translateIhfsJavaToPy(javaReturnList)
        else :
            pyReturnList = []
            
        return pyReturnList
