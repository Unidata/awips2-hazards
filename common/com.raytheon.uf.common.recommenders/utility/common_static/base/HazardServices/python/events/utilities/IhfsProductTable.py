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
# This python class is used to query for Observed and Forecast SHEF Data
# from the IHFS (Hydro) database data and Tabulate the result data.
# 
# The primary interface function for this script:
#             composeIhfsProductTable(inputDictList, forecastStartTime, forecastDateTime)
#
# This class is used to generate Tablular data for Hazard Services Issued Products.
# Input: List of tuples Elements containing the following elements:
#        (<LID> : <OBS PE>, <OBS TS>, [<OBS DUR>, <OBS EXTREMUM>, ] <FCST PE>, <FCST TS> [, <FCST DUR>, <FCST EXTREMUM>] tuples
# Where:
# inputDictList ::
# <LID>          : River Point Id
# <OBS PE>       : Observed PE (Physical Element) query parameter for LID
# <OBS TS>       : Observed TS (Type Source) query parameter for LID
# <OBS DUR>      : Observed DUR (Duration) query parameter for LID [Optional]
# <OBS EXTREMUM> : Observed EXTREMUM query parameter for LID [Optional]
# <FCST PE>      : Forecast PE (Physical Element) query parameter for LID
# <FCST TS>      : Forecast TS (Type Source) query parameter for LID 
# <FCST DUR>     : Forecast DUR (Duration) query parameter for LID [Optional]
# <FCST EXTREMUM>: Forecast EXTREMUM query parameter for LID [Optional]
#
# forecastStartTime ::
# Date Time in 'YYYY-mm-dd HH:MM:SS' for the start of the forecast data query
#    This MUST be for the Same Time Zone as the Database (Assumed to be GMT)
#
# forecastTime ::
# Date Time in 'HH:MM:SS' for time that is used to determine which forecast
# Time record is used for the Forecast Day Column
#    This MUST be for the Same Time Zone as the Database (Assumed to be GMT)
#    Output table display time will be for the Report Time Zone
#
# This class uses the "Current" Date time from the Cave: Hazard Services Current Time
#    
#    USE EXAMPLE:
# ihfsProductTable = IhfsProductTable()
# myInputSettingsDict =  { ihfsProductTableIHFS_DISPLAY_BF : True,
#                          ihfsProductTable.IHFS_GROUP_BY_RIVER_NAME : True,
#                          ihfsProductTable.IHFS_SORT_BY_RIVER_MILE : True,
#                          ihfsProductTable.IHFS_USE_LID_VALUE : False,
#                          ihfsProductTable.IHFS_DATABASE_TIME_ZONE : 'GMT',
#                          ihfsProductTable.IHFS_REPORT_TIME_ZONE : 'CST6CDT',
#                          ihfsProductTable.IHFS_OBSERVED_LOOKBACK_HOURS : 24,
#                          ihfsProductTable.IHFS_FORECAST_LOOKAHEAD_DAYS : 4,
#                          ihfsProductTable.USE_LATESTOBSVALUE_TABLE : True
#                        }
# inputDictList = []
#        inputDict = {}
#        inputDict[ihfsProductTable.IHFS_LID] = 'DCTN1'
#        inputDict[ihfsProductTable.IHFS_OBS_PE] = 'HG'
#        inputDict[ihfsProductTable.IHFS_OBS_TS] = 'RG' 
#        inputDict[ihfsProductTable.IHFS_OBS_DUR] = None
#        inputDict[ihfsProductTable.IHFS_OBS_EXTREMUM] = None
#        inputDict[ihfsProductTable.IHFS_FCST_PE] = 'HG'
#        inputDict[ihfsProductTable.IHFS_FCST_TS] = 'FF' 
#        inputDict[ihfsProductTable.IHFS_FCST_DUR] = None
#        inputDict[ihfsProductTable.IHFS_FCST_EXTREMUM] = None
#        inputDictList.append(inputDict)
#  <The rest of this list is omitted for brevity>
#
# forecastStartTime = '2011-02-08 04:00:00'
# forecastTime = 8
# composedTableHeaderList, composedTableRowList = ihfsProductTable.queryAndBuildIhfsProductTable \
#                                    ( inputDictList,forecastStartTime, forecastTime)
# for tableHeader in composedTableHeaderList:
#     print tableHeader
# for dataRow in composedTableRowList:
#     print dataRow
#
# OUTPUT::
#                    BF  FLD  OBSERVED            FORECAST  3 AM CST6CDT
# LOCATION          STG  STG    STG    DAY TIME    TUE   WED   THU
# Elkhorn River
#   Waterloo         17   17    4.2    FRI  8 PM                  
# Missouri River
#   Omaha            27   29   14.5    THU  8 PM                  
#   Decatur          35   35   22.5    FRI  8 PM  34.4  13.1  13.3
#   Nebraska City    18   18   12.0    FRI  8 PM                  
# Platte River
#   Louisville        9    9    4.0    FRI  8 PM   8.5  10.2  11.3
#   Ashland          20   20   16.4    FRI  8 PM  20.8  20.3  19.3

# See comments in the below functions for additional query details. 
#
#     SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    Aug 14 2015     9988          Chris.Cody     Initial version
#    Sep 12, 2016    19147         Robert.Blum    Removed use of pytz and mktime().
#
#

import JUtil
import datetime
import time
import dateutil.tz
from operator import itemgetter
import TimeUtils
import numpy

from com.raytheon.uf.common.time import SimulatedTime
from IhfsQuery import *


class IhfsProductTable(object):

    #Constants, Formats, Table Names, Column Names, etc.
    # Input Setting: Boolean: Display a Bank Full (BF) Column
    IHFS_DISPLAY_BF = 'DISPLAY_BF' 
    # Input Setting: Boolean: Group Items by River Name
    IHFS_GROUP_BY_RIVER_NAME = 'GROUP_BY_RIVER_NAME'
    # Input Setting: Boolean: Sort Items by RIVERSTAT.MILE (ascending value)
    IHFS_SORT_BY_RIVER_MILE = 'SORT_BY_RIVER_NAME'
    # Input Setting: Boolean: Display LID Value (True) or Stream (False)
    IHFS_USE_LID_VALUE = 'USE_LID_VALUE'
    # Input Setting: String: Database time zone (Default: GMT)
    IHFS_DATABASE_TIME_ZONE = 'DATABASE_TIME_ZONE'
    # Input Setting: String: Report time zone (Default: CST6CDT)
    IHFS_REPORT_TIME_ZONE = 'REPORT_TIME_ZONE'
    # Input Setting: Integer: Number of HOURS for Observed Lookback (Most recent value is used)
    IHFS_OBSERVED_LOOKBACK_HOURS = 'OBSERVED_LOOKBACK_HOURS'
    # Input Setting: Integer: Number of DAYS to Display Forecast Columns
    IHFS_FORECAST_LOOKAHEAD_DAYS = 'FORECAST_LOOKAHEAD_DAYS'
    # Input Setting: Boolean: Query Observed Value from LATESTOBSVALUE Table (True) or by PE TS (False)
    USE_LATESTOBSVALUE_TABLE = 'USE_LATESTOBSVALUE_TABLE' 
    # Examples of Valid TimeZone Settings: EST5EDT, CST6CDT, MST7MDT, PST8PDT, GMT, UTC, Zulu,
    #                    EST, EDT, CST, CDT, MST, MDT, PST, PDT

    DefaultInputSettingsDict =  { IHFS_DISPLAY_BF : True,
                                  IHFS_GROUP_BY_RIVER_NAME : True,
                                  IHFS_SORT_BY_RIVER_MILE : True,
                                  IHFS_USE_LID_VALUE : False,
                                  IHFS_DATABASE_TIME_ZONE : 'GMT',
                                  IHFS_REPORT_TIME_ZONE : 'CST6CDT',
                                  IHFS_OBSERVED_LOOKBACK_HOURS : 48,
                                  IHFS_FORECAST_LOOKAHEAD_DAYS : 3,
                                  USE_LATESTOBSVALUE_TABLE : True
                                  }

    MILS_PER_HOUR = 3600000
    SECS_PER_DAY = 86400
    DATABASE_DATE_FORMAT = '{:4}-{:02}-{:02} {:02}:{:02}:{:02}'
    
    DAY_OF_WEEK = { 0 : 'MON',
                    1 : 'TUE',
                    2 : 'WED',
                    3 : 'THU',
                    4 : 'FRI',
                    5 : 'SAT',
                    6 : 'SUN' }

    NEXT_DAY_NAME_DICT = { 'MON' : 'TUE', 'TUE' : 'WED', 'WED' : 'THU',
                           'THU' : 'FRI', 'FRI' : 'SAT', 'SAT' : 'SUN',
                           'SUN' : 'MON' }
    
    FPINFO_TABLE_NAME = 'FPINFO'
    FPINFO_SELECT_COLUMN_LIST = [ 'FPINFO.LID', 'FPINFO.NAME', 'FPINFO.STREAM', 'FPINFO.BF', \
                                  'FPINFO.MINOR_STAGE', 'FPINFO.MODERATE_STAGE', 'FPINFO.MAJOR_STAGE', \
                                  'FPINFO.MINOR_FLOW', 'FPINFO.MODERATE_FLOW', 'FPINFO.MAJOR_FLOW' ]
    IHFS_LID = 'LID'
    IHFS_STREAM = 'STREAM'
    
    RIVERSTAT_TABLE_NAME = 'RIVERSTAT'
    RIVERSTAT_SELECT_COLUMN_LIST = [ 'RIVERSTAT.LID', 'RIVERSTAT.MILE' ]
    IHFS_MILE = 'MILE'
    
    IHFS_LATESTOBS_TABLE_NAME = 'LATESTOBSVALUE'
    IHFS_PE = 'PE'
    IHFS_TS = 'TS' 
    IHFS_DUR= 'DUR'
    IHFS_EXTREMUM = 'EXTREMUM'
    
    IHFS_OBSTIME = 'OBSTIME'
    IHFS_VALUE = 'VALUE'
    IHFS_OBSERVED_SELECT_COLUMN_LIST = [ IHFS_LID, IHFS_PE, IHFS_TS, IHFS_OBSTIME, IHFS_VALUE ]
    IHFS_BASISTIME = 'BASISTIME' 
    IHFS_VALIDTIME = 'VALIDTIME' 
    IHFS_FORECAST_SELECT_COLUMN_LIST = [ IHFS_LID, IHFS_PE, IHFS_TS, IHFS_VALIDTIME, IHFS_BASISTIME, IHFS_VALUE ]

    IHFS_OBS_PE = 'OBS_PE'
    IHFS_OBS_TS = 'OBS_TS' 
    IHFS_OBS_DUR = 'OBS_DUR'
    IHFS_OBS_EXTREMUM = 'OBS_EXTREMUM'
    IHFS_FCST_PE = 'FCST_PE'
    IHFS_FCST_TS = 'FCST_TS' 
    IHFS_FCST_DUR = 'FCST_DUR'
    IHFS_FCST_EXTREMUM = 'FCST_EXTREMUM'
    PLACEHOLDER = 'PLACEHOLDER'
    MIN_TIMESTAMP_WITH_MSEC_VAL = 10000000000

    #
    # init()
    #
    # Default initializer
    #    
    def __init__(self):
        self.observedTableName = self.IHFS_LATESTOBS_TABLE_NAME
        self._currentTime = SimulatedTime.getSystemTime().getMillis()

        self._forecastBasisTimeDict = { }
        self._ihfsQuery = IhfsQuery()

        self._inputSettingsDict = self.DefaultInputSettingsDict
        
        
    #
    # getInputSettings()
    # (External interface function).
    #
    # Get current IhfsProductTable Input Settings
    #
    # @return current Input Settings Dictionary
    #
    def getInputSettings(self):
        return self._inputSettingsDict
    
    #
    # setInputSettings(newInputSettingsDict)
    # (External interface function).
    #
    # Set IhfsProductTable Input Settings or reset to settings to default
    #
    # @param newInputSettingsDict Settings Dictionary. Missing values will not be set
    #                        None will reset to default settings
    #
    def setInputSettings(self, newInputSettingsDict):
        if newInputSettingsDict is not None:
            tempSetting = newInputSettingsDict.get(self.IHFS_DISPLAY_BF, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.IHFS_DISPLAY_BF] = tempSetting
            tempSetting = newInputSettingsDict.get(self.IHFS_GROUP_BY_RIVER_NAME, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.IHFS_GROUP_BY_RIVER_NAME] = tempSetting
            tempSetting = newInputSettingsDict.get(self.IHFS_SORT_BY_RIVER_MILE, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.IHFS_SORT_BY_RIVER_MILE] = tempSetting
            tempSetting = newInputSettingsDict.get(self.IHFS_USE_LID_VALUE, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.IHFS_USE_LID_VALUE] = tempSetting

            tempSetting = newInputSettingsDict.get(self.DATABASE_TIME_ZONE, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.DATABASE_TIME_ZONE] = tempSetting
            tempSetting = newInputSettingsDict.get(self.REPORT_TIME_ZONE, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.REPORT_TIME_ZONE] = tempSetting

            tempSetting = newInputSettingsDict.get(self.IHFS_OBSERVED_LOOKBACK_HOURS, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.IHFS_OBSERVED_LOOKBACK_HOURS] = tempSetting
            tempSetting = newInputSettingsDict.get(self.IHFS_FORECAST_LOOKAHEAD_DAYS, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.IHFS_FORECAST_LOOKAHEAD_DAYS] = tempSetting

            tempSetting = newInputSettingsDict.get(self.USE_LATESTOBSVALUE_TABLE, None)
            if tempSetting is not None:
                self._inputSettingsDict[self.USE_LATESTOBSVALUE_TABLE] = tempSetting
        else:
            self._inputSettingsDict = self.DefaultInputSettingsDict

    #
    # getObservedTableName(obsPe, obsTs)
    #
    # Get the IHFS SHEF Observed table Name.
    #
    # If Input Setting USE_LATESTOBSVALUE_TABLE is True (default); the
    # LATESTOBSVALUE will be queried. If this setting is False; then the 
    # Input Dictionary values for IHFS_OBS_PE ('OBS_PE') and IHFS_OBS_TS ('OBS_TS')
    # for the listed Input LID will be used.
    # Table values will be drawn from IhfsQuery.getIhfsShefTableName
    #
    # @param obsPe  Observed PE (Physical Element) value
    # @param obsTs  Observed TS (Type Source) value
    # return SHEF Observed table to query for Most Recent VALUE
    #
    def getObservedTableName(self, obsPe, obsTs):
        if self._inputSettingsDict.get(self.USE_LATESTOBSVALUE_TABLE, True) == True:
            return self.IHFS_LATESTOBS_TABLE_NAME
        else:
            return self._ihfsQuery.getObservedTableName(obsPe, obsTs)
        
    #
    # composeIhfsProductTable(inputDictList, forecastStartTime, forecastTime)
    # PRIMARY INTERFACE FUNCTION:
    #
    # Query for and Tabulate IHFS (Hydro) SHEF (Observed and Forecast) Data.
    # 
    # @param inputDictList A list of Dictionaries containing:
    #        (<LID> : <OBS PE>, <OBS TS>, [<OBS DUR>, <OBS_EXTREMUM>, ] <FCST PE>, <FCST TS> [, <FCST DUR>, <FCST EXTREMUM>] tuples
    # @param forecastStartTime Start Time for Forecast Data 
    #                (in the form 'YYYY-mm-dd HH:MM:SS')
    # @param forecastTime Time used for daily Forecast Data 
    #                (in the form 'HH:MM:SS'
    # @return composedTableHeaderList  A list of Table Header Strings
    #         composedTableRowList   A List of Table Data Row Strings
    #         (Returned Strings are space separated and do not have linefeed characters.)
    #
    def composeIhfsProductTable(self, inputDictList, forecastStartTime, forecastTime):

        if inputDictList is None or len(inputDictList) == 0:
            return list(), list()

        observedLookbackDate = self.computeLookbackDate()

        forecastNumDays = self._inputSettingsDict.get(self.IHFS_FORECAST_LOOKAHEAD_DAYS)

        lidList = self.extractLidList(inputDictList)
        
        lidToLidDataDict = self.retrieveIhfsPointIdNameData(lidList)        

        lidToLatestObsDict = self.retrieveIhfsLatestObservedData(inputDictList, observedLookbackDate)
        
        lidToForecastListDict = self.retrieveIhfsForecastData(inputDictList, forecastStartTime, forecastNumDays)

        composedTableHeaderList = self.composeTableHeader(forecastStartTime, forecastTime, forecastNumDays)
        composedTableRowList = self.formatTableData(lidToLidDataDict, lidToLatestObsDict, lidToForecastListDict, forecastStartTime, forecastTime, forecastNumDays)
            
        return composedTableHeaderList, composedTableRowList
    
    #
    # setTimeZones(databaseTimeZone, reportTimeZone)
    # (External interface function).
    #
    # Set Database and Report Time Zone Values.
    #
    # @param databaseTimeZone Time Zone of the database data
    # @param reportTimeZone Time Zone of the report data
    # 
    def setTimeZones(self, databaseTimeZone, reportTimeZone):
        self._inputSettingsDict[self.DATABASE_TIME_ZONE] = databaseTimeZone
        self._inputSettingsDict[self.REPORT_TIME_ZONE] = reportTimeZone        

    
    #
    # extractLidList(inputDictList)
    #
    # Extract the LID (River Point ID) values from list of Dictionaries containing:
    #        (<LID> : <OBS PE>, <OBS TS>, [<OBS DUR>, <OBS_EXTREMUM>, ] <FCST PE>, <FCST TS> [, <FCST DUR>, <FCST EXTREMUM>] tuples
    # 
    # @param inputDictList List of Tuples
    #        (<LID> : <OBS PE>, <OBS TS>, [<OBS DUR>, <OBS_EXTREMUM>, ] <FCST PE>, <FCST TS> [, <FCST DUR>, <FCST EXTREMUM>] tuples
    # @return List of LID values
    #
    def extractLidList(self, inputDictList):
        lidList = []
        if inputDictList is not None and len(inputDictList) > 0:
            for inputDict  in inputDictList:
                lid = inputDict.get(self.IHFS_LID, None)
                if lid is not None:
                    lidList.append(lid)
        return lidList
    
    #
    # retrieveIhfsPointIdNameData(lidList)
    #
    # Query IHFS Database (through IhfsQuery) for Point ID (LID) Data
    #        Data returned is a Dictionary of Point ID (LID) : Dictionary {
    #             'LID'            : Point Id (LID)
    #             'NAME'           : Name
    #             'STREAM'         : Stream
    #             'MINOR_STAGE'    : Minor Stage (Numeric Double) 
    #             'MODERATE_STAGE' : Moderate Stage (Numeric Double)
    #             'MAJOR_STAGE'    : Major Stage (Numeric Double)
    #             'MINOR_FLOW'     : Minor Flow (Numeric Double)
    #             'MODERATE_FLOW'  : Moderate Flow (Numeric Double)
    #             'MAJOR_FLOW'     : Major Flow (Numeric Double)
    # Sub Query RIVERSTAT for River Mile (RIVERSTAT.MILE) data for later tabulation.
    #
    # @param lidList List of LID values
    # @return a Dictionary of {LID : {Dictionary LABEL:VALUE} }
    #   
    def retrieveIhfsPointIdNameData(self, lidList): 
        
        lidToLidDataDict = { }
        if lidList is None or len(lidList) == 0:
            return lidNameDataDict
        
        selectTableColumnList = self.FPINFO_SELECT_COLUMN_LIST

        inLidStringList = ', '.join(lidList)
        queryPredicateTupleList = []
        
        queryPredicateTupleList.append( ('AND', self.FPINFO_TABLE_NAME + '.' + self.IHFS_LID, 'IN', inLidStringList) )
        
        resultDictList = self._ihfsQuery.buildAndExecuteIhfsQuery(self.FPINFO_TABLE_NAME, selectTableColumnList, queryPredicateTupleList)
        if resultDictList is not None:
            if len(resultDictList) > 0:
                for returnDict in resultDictList:
                    lid = returnDict.get(self.IHFS_LID, None)
                    if lid is not None:
                        lidToLidDataDict[lid] = returnDict
                     
        #Set River Mile Data in LID DATA DICT
        lidToRiverStatDataDict = self.retrieveIhfsPointIdRiverMileData(lidList)
        for lid in lidList:
            lidDataDict = lidToLidDataDict.get(lid, None)
            riverStatDataDict = lidToRiverStatDataDict.get(lid, None)
            if lidDataDict is not None and riverStatDataDict is not None:
                riverMile = riverStatDataDict.get(self.IHFS_MILE, -9999)
                lidDataDict[self.IHFS_MILE] = riverMile
                
        return lidToLidDataDict

    #
    # retrieveIhfsPointIdRiverMileData(lidList)
    #
    # Query IHFS Database (through IhfsQuery) for RIVERSTAT.MILE Data
    #        Data returned is a Dictionary of Point ID (LID) : Dictionary {
    #             'LID'            : Point Id (LID)
    #             'MILE'           : River Mile of LID
    #
    # @param lidList List of LID values
    # @return a Dictionary of {LID : {Dictionary LABEL:VALUE} }
    #   
    def retrieveIhfsPointIdRiverMileData(self, lidList): 
        
        lidToRiverMileDataDict = { }
        if lidList is None or len(lidList) == 0:
            return lidToRiverMileDataDict
        
        selectTableColumnList = self.RIVERSTAT_SELECT_COLUMN_LIST

        inLidStringList = ', '.join(lidList)
        queryPredicateTupleList = []
        
        queryPredicateTupleList.append( ('AND', self.RIVERSTAT_TABLE_NAME + '.' + self.IHFS_LID, 'IN', inLidStringList) )
        
        resultDictList = self._ihfsQuery.buildAndExecuteIhfsQuery(self.RIVERSTAT_TABLE_NAME, selectTableColumnList, queryPredicateTupleList)
        if resultDictList is not None:
            if len(resultDictList) > 0:
                for returnDict in resultDictList:
                    lid = returnDict.get(self.IHFS_LID, None)
                    if lid is not None:
                        lidToRiverMileDataDict[lid] = returnDict
                        
        return lidToRiverMileDataDict


    #
    # retrieveIhfsLatestObservedData(inputDictList, observedLookbackDate)
    # 
    # This function queries for OHFS Observed data values.
    # If Input Setting USE_LATESTOBSVALUE_TABLE is true; the LATESTOBSVALUE table
    # will be queried. Otherwise, the queried table will be determined by the
    # OBS PE and OBS TS values for the LID.
    #
    # Query OBSERVED values from the <Observed Data Table>.
    #        Data returned is a Dictionary of Point ID (LID) : Dictionary {
    #             'LID'            : Point Id (LID)
    #             'PE'             : Physical Element (PE)
    #             'TS'             : Type Source (TS)
    #             'OBSTIME'        : Observed Time (Timestamp) 
    #             'VALUE'          : Observed Value (Numeric Double)
    #             'DUR'            : Observed Duration
    #             'EXTREMUM'       : Observed Extremum
    #
    # @param inputDictList A List of Dictionaries
    #        (<LID> : <OBS PE>, <OBS TS>, [<OBS DUR>, <OBS_EXTREMUM>, ] <FCST PE>, <FCST TS> [, <FCST DUR>, <FCST EXTREMUM>] tuples
    # @param observedLookbackDate Earliest Date Time (YYYY-mm-dd HH:MM:SS) for latest Observation  
    # @return {Dictionary LID : {Dictionary LABEL : VALUE } }
    #
    def retrieveIhfsLatestObservedData(self, inputDictList, observedLookbackDate):
        
        if inputDictList is None or len(inputDictList) == 0:
            return []
        
        observedDictDict = { }

        for inputDict in inputDictList:
            lid = inputDict.get(self.IHFS_LID, None)
            obsPe = inputDict.get(self.IHFS_OBS_PE, None)
            obsTs = inputDict.get(self.IHFS_OBS_TS, None)
            obsDur = inputDict.get(self.IHFS_OBS_DUR, None)
            obsExtremum = inputDict.get(self.IHFS_OBS_EXTREMUM, None)

            observedTableName = self.getObservedTableName(obsPe, obsTs)
            observedSelectTableColumnList = self.getObservedTableColumnList(observedTableName)
            
            queryPredicateTupleList = []
            
            queryPredicateTupleList.append( ('AND', observedTableName + '.' + self.IHFS_LID, '=', lid) )
            queryPredicateTupleList.append( ('AND', observedTableName + '.' + self.IHFS_PE, '=', obsPe) )
            queryPredicateTupleList.append( ('AND', observedTableName + '.' + self.IHFS_TS, '=', obsTs) )
            if obsDur is not None:
                queryPredicateTupleList.append( ('AND', observedTableName + '.' + self.IHFS_DUR, '=', obsDur) )
            if obsExtremum is not None:
                queryPredicateTupleList.append( ('AND', observedTableName + '.' + self.IHFS_EXTREMUM, '=', obsExtremum) )
                
            queryPredicateTupleList.append( ('AND', observedTableName + '.' + self.IHFS_OBSTIME, '>=', observedLookbackDate) )

            pyReturnList = self._ihfsQuery.buildAndExecuteIhfsQuery(observedTableName, observedSelectTableColumnList,
                                                                             queryPredicateTupleList)
            
            if pyReturnList is not None and len(pyReturnList) > 0:
                if len(pyReturnList) == 1:
                    latestObsDict = pyReturnList[0]
                    observedDictDict[lid] = latestObsDict
                else:
                    observedDictDict[lid] = self.getLatestObservedValueDict(pyReturnList)
            else:
                observedDictDict[lid] = { }
                
        return observedDictDict

    #
    # getLatestObservedValueDict(latestObsListDict)
    #
    # Retrieve the most current Observed Value.
    #
    # @param latestObsDictList a List [ Dictionary { Label:Value } ]
    #        Dictionary Keys: LID, PE, TS, OBSTIME, VALUE, DUR, EXTREMUM
    # @return Dictionary Containing the Largest Value
    #
    def getLatestObservedValueDict(self, latestObsDictList):
        if latestObsDictList is None:
            return {}
        
        latestObsTimestamp = -1
        latestObsDict = { }
        for obsDict in latestObsDictList:
            tempTimestamp = obsDict.get(self.IHFS_OBSTIME, 0)
            if tempTimestamp > latestObsTimestamp:
                latestObsTimestamp = tempTimestamp
                lateObsDict = obsDict
        return latestObsDict

    #
    # computeLookbackDate()
    #
    # Compute the Observed look back date from the 
    # Timestamp of the current time and the set look back hours.
    # 
    # The number of look back hours is set in the Input Settings: IHFS_OBSERVED_LOOKBACK_HOURS
    # This is used for querying for Observed Data.
    #
    # @return Observed Look Back Date in the form "YYYY-mm-dd HH:MM:SS"
    #
    def computeLookbackDate(self):
        
        observedLookbackTime = self._inputSettingsDict.get(self.IHFS_OBSERVED_LOOKBACK_HOURS, 48)
            
        lookbackDateInMills = self._currentTime - (observedLookbackTime  * self.MILS_PER_HOUR)
        
        lookbackDateString = self.convertTimeStampToDateString(lookbackDateInMills)
        return lookbackDateString


    #                 
    # retrieveIhfsForecastData(inputDictList, forecastStartTime, forecastNumDays)
    # 
    # Query rows from the appropriate Forecast table.
    #        Data returned is a Dictionary of Point ID (LID) : Dictionary {
    #             'LID'            : Point Id (LID)
    #             'PE'             : Physical Element (PE)
    #             'TS'             : Type Source (TS)
    #             'VALIDTIME'      : Forecast Valid Time (Timestamp) 
    #             'BASISTIME'      : Forecast Basis Time (Timestamp) 
    #             'VALUE'          : Forecast Value (Numeric Double)
    #             'DUR'            : Forecast Duration
    #             'EXTREMUM'       : Forecast Extremum
    # Forecast Data Table is determined from FCST PE and FCST TS values
    #
    # @param inputDictList   : A List [ Dictionary {  Label:Value } ]
    #               Dictionary Keys: LID, OBS_PE, OBS_TS, OBS_DUR, OBS_EXTREMUM,
    #                        FCST_PE, FCST_TS, FCST_DUR, FCST_EXTREMUM
    # @param forecastStartTime  Forecast Start Date in the form "YYYY-mm-dd HH:MM:SS"
    # @param forecastNumDays Number of Days to include in forecast
    # @return a List [ Dictionary { Label:Value} ] for the requested Forecast data
    #
    def retrieveIhfsForecastData(self, inputDictList, forecastStartTime, forecastNumDays):
        if inputDictList is None or len(inputDictList) == 0:
            return []

        forecastEndTime = self.computeForecastEndTime(forecastStartTime , forecastNumDays)

        forecastDictList = { }
        
        for inputDict in inputDictList:
            lid = inputDict.get(self.IHFS_LID, None)
            forPe = inputDict.get(self.IHFS_FCST_PE, None)
            forTs = inputDict.get(self.IHFS_FCST_TS, None)
            forDur = inputDict.get(self.IHFS_FCST_DUR, None)
            forExtremum = inputDict.get(self.IHFS_FCST_EXTREMUM, None)
            forecastTableName = self._ihfsQuery.getIhfsShefTableName(forPe, forTs).upper()
            
            forecastBasisTime = self.retrieveIhfsForecastBasisTime(forecastTableName)

            forecastSelectTableColumnList = self.getForecastTableColumnList(forecastTableName)
            queryPredicateTupleList = []
            queryPredicateTupleList.append( ('AND', forecastTableName + '.' + self.IHFS_LID, '=', lid) )
            queryPredicateTupleList.append( ('AND', forecastTableName + '.' + self.IHFS_PE, '=', forPe) )
            queryPredicateTupleList.append( ('AND', forecastTableName + '.' + self.IHFS_TS, '=', forTs) )
            if forDur is not None:
                queryPredicateTupleList.append( ('AND', forecastTableName + '.' + self.IHFS_DUR, '=', forDur) )
            if forExtremum is not None:
                queryPredicateTupleList.append( ('AND', forecastTableName + '.' + self.IHFS_EXTREMUM, '=', forExtremum) )
                
            queryPredicateTupleList.append( ('AND', forecastTableName + '.' + self.IHFS_VALIDTIME, '>=', forecastStartTime) )
            queryPredicateTupleList.append( ('AND', forecastTableName + '.' + self.IHFS_VALIDTIME, '<', forecastEndTime) )
            queryPredicateTupleList.append( ('AND', forecastTableName + '.'  + self.IHFS_BASISTIME, '=', forecastBasisTime) )
        
            pyReturnList = self._ihfsQuery.buildAndExecuteIhfsQuery(forecastTableName, forecastSelectTableColumnList,
                                                                             queryPredicateTupleList)
            
            if pyReturnList is not None and  len(pyReturnList) > 0:
                forecastDictList[lid] = pyReturnList
            else:
                forecastDictList[lid] = [ ]
            
        return forecastDictList

    #                 
    # retrieveIhfsForecastBasisTime(forecastTableName)
    # 
    # Query for MAX(BASISTIME) from the given Forecast table.
    # This function caches the Basis Time, since it will be the same
    # for a given Table.
    #
    # @param forecastTableName Name of Forecast Table to query 
    # @return Maximum Forecast Basis Time value
    #
    def retrieveIhfsForecastBasisTime(self, forecastTableName):
        
        databaseTimeZoneString = self._inputSettingsDict[self.IHFS_DATABASE_TIME_ZONE]
        databaseTimeZone = dateutil.tz.gettz(databaseTimeZoneString)
        
        forecastBasisTimeString = self._forecastBasisTimeDict.get(forecastTableName, None)
        if forecastBasisTimeString == None:
            aggregateFunction = 'MAX'
            aggregateTableColumn = '' + forecastTableName + '.' + self.IHFS_BASISTIME
            queryPredicateTupleList = []
            forecastBasisTimeInMils = self._ihfsQuery.buildAndExecuteIhfsAggregateQuery(forecastTableName, aggregateFunction,
                                                                             aggregateTableColumn, queryPredicateTupleList)
            forecastBasisTimeInSecs = forecastBasisTimeInMils // 1000
            forecastBasisTime = datetime.datetime.fromtimestamp(forecastBasisTimeInSecs, databaseTimeZone) 
            forecastBasisTimeString = self.DATABASE_DATE_FORMAT.format(
                                               forecastBasisTime.year, forecastBasisTime.month, forecastBasisTime.day, 
                                               forecastBasisTime.hour, forecastBasisTime.minute, forecastBasisTime.second)

            self._forecastBasisTimeDict[forecastTableName] = forecastBasisTimeString
            
        return forecastBasisTimeString
                 
    #
    # computeForecastEndTime(forecastStartTime, forecastNumDays)
    #
    # Compute the End Date Time from a Forecast Start Time an a number of days
    #
    # @param forecastStartTime  Forecast Start Date in the form "YYYY-mm-dd HH:MM:SS"
    # @param forecastNumDays Number of Days to include in forecast
    # @return  Forecast End Date in the form "YYYY-mm-dd HH:MM:SS"
    # 
    def computeForecastEndTime(self, forecastStartTime, forecastNumDays):
        databaseTimeZoneString = self._inputSettingsDict[self.IHFS_DATABASE_TIME_ZONE]
        databaseTimeZone = dateutil.tz.gettz(databaseTimeZoneString)
        forecastStartTimestampInSecs = self.convertDateStringToTimestamp(forecastStartTime)
        forecastEndTimestampInSecs = forecastStartTimestampInSecs + (forecastNumDays * self.SECS_PER_DAY)
        forecastEndTime = datetime.datetime.fromtimestamp(forecastEndTimestampInSecs, databaseTimeZone) 
        
        forecastEndTimeString = self.DATABASE_DATE_FORMAT.format(forecastEndTime.year, forecastEndTime.month, forecastEndTime.day, 
                                                       forecastEndTime.hour, forecastEndTime.minute, forecastEndTime.second)
        
        return forecastEndTimeString
    
    #
    # convertDateStringToTimestamp(dateTimeString)
    # 
    # Convert a (database) date time string (YYYY-mm-dd HH:MM:SS) to a 
    # Python timestamp IN SECONDS.
    #
    # @param dateTimeString Input Date Time String (YYYY-mm-dd HH:MM:SS)
    # @return Python timestamp in SECONDS
    # 
    def convertDateStringToTimestamp(self, dateTimeString):
        return long( time.mktime( datetime.datetime.strptime(dateTimeString, "%Y-%m-%d %H:%M:%S").timetuple() ) )

    #
    # convertTimeStampToDateString(timestampInMils, timeZoneString=None)
    # 
    # Convert a timestamp (in Seconds OR in Milliseconds) to a (database) date time string (YYYY-mm-dd HH:MM:SS) 
    #
    # @param timestampInMilsOrSecs Timestamp in Seconds or Milliseconds
    # @param timeZoneString  Time Zone String identifier
    # @return dateTimeString Input Date Time String (YYYY-mm-dd HH:MM:SS)
    # 
    def convertTimeStampToDateString(self, timestampInMilsOrSecs, timeZoneString=None):
        if timestampInMilsOrSecs == None:
            return ''

        if timeZoneString == None:
            timeZoneString = self._inputSettingsDict.get(self.IHFS_DATABASE_TIME_ZONE)
            
        if timestampInMilsOrSecs > self.MIN_TIMESTAMP_WITH_MSEC_VAL:
            #Timestamp is in milliseconds. Convert to Seconds
            timestampInSecs = timestampInMilsOrSecs // 1000
        else:
            timestampInSecs = timestampInMilsOrSecs
        
        
        timeZone = dateutil.tz.gettz(timeZoneString)
        theDate = datetime.datetime.fromtimestamp(timestampInSecs, timeZone) 
        return self.DATABASE_DATE_FORMAT.format(theDate.year, theDate.month, theDate.day, 
                                                theDate.hour, theDate.minute, theDate.second)

    #
    # getObservedTableColumnList(observedTableName)
    #
    # Prepend the given Table Name (observedTableName) to the
    # needed Observed Query Column Table names.
    #
    # @param observedTableName Observed Table Name
    # @return List of <TABLE NAME>.<COLUMN NAME> values
    #
    def getObservedTableColumnList(self, observedTableName):
        observedColumnNameList = [ ]
            
        for colName in self.IHFS_OBSERVED_SELECT_COLUMN_LIST:
            observedColumnNameList.append(observedTableName + '.' + colName)
        return observedColumnNameList
    
    #
    # getForecastTableColumnList(forecastTableName)
    #
    # Prepend the given Table Name (forecastTableName) to the
    # needed Forecast Query Column Table names
    #
    # @param forecastTableName Forecast Table Name
    # @return List of <TABLE NAME>.<COLUMN NAME> values
    #
    def getForecastTableColumnList(self, forecastTableName):
        forecastColumnNameList = []
        if forecastTableName is not None:
            for colName in self.IHFS_FORECAST_SELECT_COLUMN_LIST:
                forecastColumnNameList.append(forecastTableName + '.' + colName)
        return forecastColumnNameList
    
    #
    # composeTableHeader(forecastStartTime, numDays)
    #
    # Build the Output Table Column Header as a List of Strings
    #
    # The generated columns will have a BF (Bank Full) column if
    # the Input Setting of IHFS_DISPLAY_BF is True.
    #
    # @param forecastStartTime  Forecast Start Date in the form 'YYYY-mm-dd HH:MM:SS'
    # @param forecastHour  Hour used to extract daily forecast data
    # @param forecastNumDays Number of Days to include in forecast
    #
    def composeTableHeader(self, forecastStartTime, forecastHour, forecastNumDays):
        
        forecastHourString = self.getForecastHourString(forecastHour)
        
        displayBF = self._inputSettingsDict.get(self.IHFS_DISPLAY_BF, True)
        if displayBF == True:
            topHeaderString = '                   BF  FLD  OBSERVED            FORECAST ' + forecastHourString
            botHeaderString = 'LOCATION          STG  STG    STG    DAY TIME    '
        else:
            topHeaderString = '                  FLD  OBSERVED            FORECAST ' + forecastHourString
            botHeaderString = 'LOCATION          STG    STG    DAY TIME    '

        currentDayName = self.getForecastDayName(forecastStartTime)
        botHeaderString = botHeaderString + currentDayName
        for x in range(0, (forecastNumDays - 1)):
            currentDayName = self.getNextDayName(currentDayName)
            botHeaderString = botHeaderString + "   " + currentDayName     
        headerList = [ topHeaderString, botHeaderString ]

        return headerList
    
    #
    # formatTableData(lidToLidDataDict, lidToLatestObsDict, lidToForecastListDict)
    #
    # Build the Output Table Data as a List of Strings
    # The generated columns will have a BF (Bank Full) column if
    # the Input Setting of IHFS_DISPLAY_BF is True.
    #
    # @param lidToLidDataDict A dictionary of LID to LID Values
    # @param lidToLatestObsDict A dictionary of LID to Latest Observed Values
    # @param lidToForecastListDict A dictionary to a List of dictionary objects containing
    #                            SHEF Forecast data Label:Value pairs
    # @param forecastStartTime  Forecast Start Date in the form "YYYY-mm-dd HH:MM:SS"
    # @param forecastHour    Hour of the day for Forecast time
    # @param forecastNumDays Number of days set of Forecast data to generate
    # @return A list of strings containing the tabulated data
    #  
    def formatTableData(self, lidToLidDataDict, lidToLatestObsDict, lidToForecastListDict, forecastStartTime, forecastHour, forecastNumDays):
        
        rowDataList = []
        if lidToLidDataDict is not None:
            riverNameList, rowOrderListDict = self.generateRowOrderList(lidToLidDataDict)
        
            for riverName in riverNameList:
                if riverName != self.PLACEHOLDER:
                    rowDataList.append(riverName)
                lidDataDictList = rowOrderListDict.get(riverName, None)
                if lidDataDictList is not None:
                    for lidDataDict in lidDataDictList:
                        lid = lidDataDict.get(self.IHFS_LID, None)
                        if lid is not None:
                            latestObsDict = None
                            forecastDictList = None
                            if lidToLatestObsDict is not None:
                                latestObsDict = lidToLatestObsDict.get(lid, None)
                            if lidToForecastListDict is not None:
                                forecastDictList = lidToForecastListDict.get(lid, None)
                            rowData = self.composeTableRow(lidDataDict, latestObsDict, forecastDictList, forecastStartTime, forecastHour, forecastNumDays)
                            rowDataList.append(rowData)
        return rowDataList
    

    #
    # generateRowOrderList(lidDataDict)
    #
    # Generate an Ordering for the Output Data rows.
    # The ordering is controlled by the input Settings:
    # IHFS_GROUP_BY_RIVER_NAME, and IHFS_SORT_BY_RIVER_MILE.
    # When both are True:
    #    Rows will be organized (alphabetically) by River Name
    #    with LID rows listed as sub points and ordered by RIVERSTAT.MILE values (ascending) 
    # When only IHFS_GROUP_BY_RIVER_NAME is True
    #    Rows will be organized (alphabetically) by River Name
    #    No ordering will be applied to the sub LID rows listed as sub points 
    # When only IHFS_GROUP_BY_RIVER_NAME is True
    #    No River Name designation lines will be written 
    #    LID rows are ordered by RIVERSTAT.MILE values (ascending) 
    # When both are False:
    #    No ordering is imposed.
    # 
    # @param lidDataDict A dictionary of LID : Dictionary of LID Data
    # @return riverNameList, riverToLidDataDict
    #
    def generateRowOrderList(self, lidDataDict):

        riverToLidDataDict = { }

        if self._inputSettingsDict.get(self.IHFS_GROUP_BY_RIVER_NAME, False) == True:
            for lid in lidDataDict:
                lidData = lidDataDict.get(lid, None)
                if lidData is not None:
                    riverName = lidData.get(self.IHFS_STREAM, None)
                    if riverName is not None:
                        unorderedLidDataList = riverToLidDataDict.get(riverName, None)
                        if unorderedLidDataList == None:
                            unorderedLidDataList = []
                            riverToLidDataDict[riverName] = unorderedLidDataList
                        if not lidData in unorderedLidDataList:    
                            unorderedLidDataList.append(lidData)                            
            
            if self._inputSettingsDict.get(self.IHFS_SORT_BY_RIVER_MILE, False) == True:
                for riverName in riverToLidDataDict:
                    unorderedLidDataList = riverToLidDataDict.get(riverName, None)
                    if unorderedLidDataList is not None:
                         orderedLidDataList = sorted(unorderedLidDataList, key=itemgetter(self.IHFS_STREAM))
                         riverToLidDataDict[riverName] = orderedLidDataList
        else:
            unorderedLidDataList = []
            for lid in lidDataDict:
                lidData = lidDataDict.get(lid, None)
                if lidData is not None:
                    unorderedLidDataList.append(lidData)
            if self._inputSettingsDict.get(self.IHFS_GROUP_BY_RIVER_NAME, False) == True:
                    orderedLidDataList = sorted(unorderedLidDataList, key=itemgetter(self.IHFS_STREAM))
                    riverToLidDataDict[self.PLACEHOLDER] = orderedLidDataList
            else:
                riverToLidDataDict[self.PLACEHOLDER] = unorderedLidDataList

        #Sort River Name (STREAM) Alphabetically
        riverNameList = []
        for riverName in riverToLidDataDict:
            riverNameList.append(riverName)
        riverNameList.sort()
        
        return riverNameList, riverToLidDataDict
        
    
    #
    # composeTableRow(lidDataDict, observedDict, forecastDictList, forecastStartTime, forecastHour, forecastNumDays):
    #
    # Build the Output Table Data as a List of Strings
    # The Row "name" is controlled by the Input Setting: IHFS_USE_LID_VALUE
    #    When False (default) the FPINFO.NAME will be used.
    #    When True the FPINFO.LID value will be used.
    # If the Input Setting: IHFS_DISPLAY_BF is True, then a BF (Bank Full) value will be
    # included in the data row.
    #
    # @param lidDataDict  Dictionary of LID value to LID Data Values
    # @param observedDict  A dictionary of LID to Latest Observed Values
    # @param forecastDictList  A dictionary of LID to a List of dictionary objects containing
    #                            SHEF Forecast data Label:Value pairs
    # @param forecastStartTime  Forecast Start Date in the form "YYYY-mm-dd HH:MM:SS" (In Database Time Zone)
    # @param forecastHour   Hour for Daily Forecast Data (In Database Time Zone)
    # @param forecastNumDays  Number of days set of Forecast data to generate
    #
    def composeTableRow(self, lidDataDict, observedDict, forecastDictList, forecastStartTime, forecastHour, forecastNumDays):
        stream = ''
        # LID Name
        name = ''
        identifier = ''
        # Bank Full 
        bf = 0.0
        # Flood Stage
        fldStg = 0.0
        # Observed Stage
        obsStg = 0.0
        # Observed Day And Time
        obsDate = 0
        formattedObsDate = ''
        
        displayBF = self._inputSettingsDict.get(self.IHFS_DISPLAY_BF, True)
        useLidValue = self._inputSettingsDict.get(self.IHFS_USE_LID_VALUE, False)
        
        # FPINFO  portion of data row        
        if lidDataDict is not None:
            lid = lidDataDict.get(self.IHFS_LID, None)
            name = lidDataDict.get('NAME', None)
            if useLidValue == False:
                identifier = name
            else:
                identifier = lid
                
            bf = lidDataDict.get('BF', 0.0)
            fldStg = lidDataDict.get('MINOR_STAGE', 0.0)
            stream = lidDataDict.get('STREAM', None)
        # Observed portion of data row        
        observedPart = ''
        obsStg = 0.0
        obsDate =  0
        formattedObsDate = ''
        if observedDict is not None:
            obsStg = observedDict.get('VALUE', 0.0)
            obsDate = observedDict.get('OBSTIME', 0)
            formattedObsDate = self.formatObsDate(obsDate)
        
        if displayBF == True: 
            partOneFormatString = "  {:<16s} {:>2.0f}   {:>2.0f}   {:>4.1f}    {:<9s}"    
            observedPart = partOneFormatString.format(identifier, bf, fldStg, obsStg, formattedObsDate)
        else:
            partOneFormatString = "  {:<16s} {:>2.0f}   {:>4.1f}    {:<9s}"    
            observedPart = partOneFormatString.format(identifier, fldStg, obsStg, formattedObsDate)
            
        #Forecast portion of data row
        forecastPart = ''
        if forecastDictList is not None and len(forecastDictList) > 0:
            forecastColumDef = "  {:>4.1f}"
            for x in range(0, forecastNumDays):
                #Get Forecast
                curForecastValue = self.findForecastValue(forecastStartTime, forecastHour, forecastNumDays, x, forecastDictList)
                if curForecastValue is not None:
                    forecastPart = forecastPart + forecastColumDef.format(curForecastValue)
                else:
                    forecastPart = forecastPart + '      '
    
        return observedPart + forecastPart
    
    #
    # findForecastValue(forecastStartTime, forecastHour, forecastNumDays. curDay, forecastDictList)
    #
    # Search the List of Forecast Value Dictionary objects for the correct value to use.
    #
    # @param forecastStartTime  Forecast Start Date in the form "YYYY-mm-dd HH:MM:SS"
    #                    DATABASE TIME ZONE VALUE
    # @param forecastHour Hour of day for forecast
    #                    DATABASE TIME ZONE VALUE 
    # @param forecastNumDays  Number of days set of Forecast data to generate
    # @param curDay Current Day Number 
    # @param forecastDictList  List of Forecast Label:Value dictionary objects
    # @return forecast Value to use for the Given Day AS A NUMBER (float)
    #
    def findForecastValue(self, forecastStartTime, forecastHour, forecastNumDays, curDay, forecastDictList):
        
        if forecastDictList is None or len(forecastDictList) == 0:
            return None
        
        databaseTimeZoneString = self._inputSettingsDict[self.IHFS_DATABASE_TIME_ZONE]
        databaseTimeZone = dateutil.tz.gettz(databaseTimeZoneString)
        
        forecastStartDateTime = datetime.datetime.strptime(forecastStartTime, "%Y-%m-%d %H:%M:%S")
        intHour = int (forecastHour)
        forecastReportHourDateTime = forecastStartDateTime.replace( hour=intHour, minute=0, second= 0)
        forecastTimestampInSecs = long( time.mktime( forecastReportHourDateTime.timetuple() ) )
        
        orderedForecastDictList = sorted(forecastDictList, key=itemgetter(self.IHFS_VALIDTIME))

        curForecastTimestampInSecs = forecastTimestampInSecs + ( (curDay + 1) * self.SECS_PER_DAY)
        currentForecastDateString = self.convertTimeStampToDateString(curForecastTimestampInSecs)
        currentForecastDateString = currentForecastDateString[0:10]

        curDayForecastDictList = []
        for forecastDict in forecastDictList:
            validTimeInMils = forecastDict.get(self.IHFS_VALIDTIME, None)
            validTimeString = self.convertTimeStampToDateString(validTimeInMils)
            if validTimeString.startswith(currentForecastDateString):
                curDayForecastDictList.append(forecastDict)
            
        closestValue = None
        closestDiff = 99999999
        if len(curDayForecastDictList) > 0:
            for forecastDict in curDayForecastDictList:
                validTimeTimestampInSecs = forecastDict.get(self.IHFS_VALIDTIME, None) // 1000
                value = forecastDict.get(self.IHFS_VALUE, None)
                diff = curForecastTimestampInSecs - validTimeTimestampInSecs
                diff = abs(diff)
                if diff < closestDiff:
                     closestDiff = diff
                     closestValue = value
                 
        return closestValue
    
    #
    # getForecastDayName(forecastStartTime)
    #
    # Get the Day of the week for the forecast day.
    #
    # @param forecastStartTime  Date Time for Forecast Start Time (YYYY-mm-dd HH:MM:SS)
    # @return The day of the week for the given Start Time (3 letter string)
    # 
    def getForecastDayName(self, forecastStartTime):
        #Extract the name of the day of the week from the forecastStartTime
        numberWeekDay = datetime.datetime.strptime(forecastStartTime, "%Y-%m-%d %H:%M:%S").weekday()
        return self.getDayOfWeek(numberWeekDay)
        
    #
    # getNextDayName(currentDayName)
    #
    # Get the Next day of the week (3 letter string)
    # for the given day of the week
    #
    # @param currentDayName Current Day of the week (MON, TUE, etc.)
    # @return Next day of the week (3 letter string)
    # 
    def getNextDayName(self, currentDayName):
        return self.NEXT_DAY_NAME_DICT[currentDayName]

    #
    # getForecastHourString(forecastHour)
    #
    # Get the Forecast Hour for the generated Data Table.
    #
    # @param forecastHour  Hour of the Daily Forecast (In Database Time Zone)
    # @return the Forecast Hour String 'HH AP TZ' (In Report Time Zone)
    # 
    def getForecastHourString(self, forecastHour):

        databaseTimeZoneString = self._inputSettingsDict[self.IHFS_DATABASE_TIME_ZONE]
        databaseTimeZone = dateutil.tz.gettz(databaseTimeZoneString)
        reportTimeZoneString = self._inputSettingsDict[self.IHFS_REPORT_TIME_ZONE]
        reportTimeZone = dateutil.tz.gettz(reportTimeZoneString)

        intHour = int( forecastHour)
        dateTimeGmtNow = datetime.datetime.now(databaseTimeZone)
        dateTimeGmtNow = dateTimeGmtNow.replace( hour=intHour, minute=0, second=0)
        
        dateTimeReport = dateTimeGmtNow.astimezone(reportTimeZone)
        
        hour = dateTimeReport.hour
        hour, amPm = self.get12hourAmPmFrom24Hour(hour)

        forecastDateFormat = '{:>2} {:<2s} {:>3s}'
        forecastDateString = forecastDateFormat.format(hour, amPm,  reportTimeZoneString)    
        return forecastDateString
        
    #
    # formatObsDate(obsDateTime)
    #
    # Parse a formatted Observed Date for the 
    # display "DAY TIME" column.
    # It is in the format WWW DD AP
    #     WWW: String abbreviation for day of the week
    #     HH:  12 hour, hour of day
    #     AP:  AM or PM
    #
    # @param obsDateTime Observed Date Time (in seconds or milliseconds)
    # @return formatted Observed Day Column Data 
    # 
    def formatObsDate(self, obsDateTime):
        if obsDateTime > self.MIN_TIMESTAMP_WITH_MSEC_VAL:
            #Timestamp is in milliseconds. Convert to Seconds
            obsDateTime = obsDateTime // 1000

        databaseTimeZoneString = self._inputSettingsDict[self.IHFS_DATABASE_TIME_ZONE]
        databaseTimeZone = dateutil.tz.gettz(databaseTimeZoneString)
        
        observedDate =  datetime.datetime.fromtimestamp(obsDateTime, databaseTimeZone)
        numberWeekDay = observedDate.weekday()
        dayOfWeek = self.getDayOfWeek(numberWeekDay)
        dayOfMonth = observedDate.day
        
        hour = observedDate.hour
        hour, amPm = self.get12hourAmPmFrom24Hour(hour)
                
        observedDateFormat = '{:<3s} {:>2} {:<2s}'
        observedDate = observedDateFormat.format(dayOfWeek, hour, amPm)
            
        return observedDate
        
        
    #
    # def getAmPm(hour)
    #
    # Get (12 hour) hour and AM or PM from (24 hour) hour
    #
    # @param (24 hour) hour value
    # @return (12 hour) hour, 'AM' or 'PM' 
     
    def get12hourAmPmFrom24Hour(self, hour):
        
        intHour = int(hour)
        amPm = 'AM'
        if intHour >= 12:
            amPm = 'PM'
            intHour = intHour - 12
        else :
            if intHour == 0:
                intHour = 12
        return intHour, amPm     
        
        
    #
    # formatObsDate(obsDateTime)
    #
    # Get the String day of the week (MON, TUE, etc.)
    # given a Numeric day of the week from datetime
    #
    # @param  dayOfWeek Numeric 0-6 day of the week
    # @return string day of the week
    # 
    def getDayOfWeek(self, dayOfWeek):
        return( self.DAY_OF_WEEK[dayOfWeek] )
    
    
