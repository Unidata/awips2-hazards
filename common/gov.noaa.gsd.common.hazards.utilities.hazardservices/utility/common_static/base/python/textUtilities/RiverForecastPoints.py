'''
 Contains methods which access river forecast point-specific
 information from the IHFS data. For each template variable available
 in the legacy RiverPro product content control files, there are two 
 methods, one with a name which reflects the template variable it is
 replacing and one with a more descriptive name. For example, the 
 two methods which replace the "obsStg" template variable are 
 getObsStg and getObservedStage. Having a method name which 
 matches the template variable name should help in template
 conversions, but focal points are encouraged to use the more
 readable version.   
 
 For detailed descriptions of these template variables please see the 
 RiverProAppendices document on the WHFS Support Page: 
 
 https://ocwws.weather.gov/intranet/whfs/
 
 @since: January 2014
 @author: GSD Hazard Services Team
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 Jan 25, 2014  2394      blawrenc     Initial Coding
 May 1, 2014   3581      bkowal       Updated to use common hazards hydro
 Jan 12, 2015  4937      Robert.Blum  Moved constant outside of the RFP class
                                      so that the class does not have to be instantiated
                                      to access the constant.
 Apr 07, 2015  7271      Chris.Golden Added reference to new missing time value constant.
'''
from com.raytheon.uf.common.hazards.hydro import RiverProDataManager
from sets import Set

import math
import JUtil
import JUtilHandler
import sys
import datetime
import time

CATEGORY = 'CATEGORY'
TIME = 'TIME'
CATEGORY_NAME = 'CATEGORYNAME'
VALUE = 'VALUE'

MAJOR = 'MAJOR'
MINOR = 'MINOR'
MODERATE = 'MODERATE'
RECORD = 'RECORD'
PE_H = 'H'
PE_Q = 'Q'

from HazardConstants import MISSING_VALUE

class RiverForecastPoints(object):
    
    MISSING_SHEF_QUALITY_CODE = 'Z'

    RIVERSTAT_PRIMARY_PE_FIELD_POSITION = 1
    RIVERSTAT_RIVERMILE_FIELD_POSITION = 11
    RIVERSTAT_LATITUDE_FIELD_POSITION = 15
    RIVERSTAT_LONGITUDE_FIELD_POSITION = 16
    RIVERSTAT_ZERO_DATUM_FIELD_POSITION = 26
    
    FLOOD_CATEGORY_VALUE_DICT = {-1: 'Unknown',
                                  0: 'NonFlood',
                                  1: 'Minor',
                                  2: 'Moderate',
                                  3: 'Major',
                                  4: 'Record'}
    
    TREND_VALUE_DESCRIPTION_DICT = {  'RISE': 'Rising',
                                      'UNCHANGED': 'Steady',
                                      'FALL': 'Falling',
                                      'MISSING': 'Unknown'}
        
    def __init__(self, currentTime, floodDataAccessObject=None):
        '''
        This class uses the same data structures
        used by the RiverProRiverFloodRecommender class. Each
        time a river forecast point product is created, an
        object of this class should be instantiated.  It will build all
        of the data structures needed by the methods in this class.
        
        @param currentTime:            Date object representing the current time.
        @param floodDataAccessObject:  Data access object for river data. 
                                       If not provided, then the default
                                       data access object will be used. 
                                       This parameter is mainly available for 
                                       testing. It allows a test dao to be
                                       injected without the need for a live
                                       hydro database.
        '''
        self.currenTime = currentTime
        if floodDataAccessObject is not None:
            self.riverProDataManager = RiverProDataManager(floodDataAccessObject)
        else:
            self.riverProDataManager = RiverProDataManager()
            
    ###############################################################
    #
    # Independent Template Variables
    #
    ###############################################################
    def getGroupList(self):
        '''
        Emulates the functionality of the <GrpList> template variable.
        e.g. ELKHORN RIVER...PLATTE RIVER
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: The list of available groups
        '''
        groupList = self.getRiverGroupList()
        groupListString = ''
        
        for group in groupList:
            if group.isIncludedInRecommendation():
                groupListString += group.getName()
                groupListString += '...'
    
        return groupListString.rstrip('...')

    def getListOfRivers(self):
        '''
        Emulates the functionality of the <RiverList> template variable.
        e.g. ELKHORN RIVER...PLATTE RIVER
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: The list of rivers in the recommendation
        '''
        forecastPointList = JUtil.javaObjToPyVal(self.riverProDataManager.getForecastPointList())
        riverSet = Set()
        
        for forecastPoint in forecastPointList:
            if forecastPoint.isIncludedInRecommendation():
                riverSet.add(forecastPoint.getStream())
        
        riverList = list(riverSet)
        riverList.sort()
    
        return riverList

    def getListOfRiverPointsPerGroup(self):
        '''
        Emulates the functionality of the <GrpsFPList> template variable.
        e.g. ELKHORN RIVER AT NELIGH...NORFOLK...PILGER...WEST POINT...HOOPER...WINSLOW...
             WATERLOO...PLATTE RIVER AT DUNCAN...NORTH BEND...LESHARA...ASHLAND...LOUISVILLE
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: A string describing the river groups and the forecast points
                 on them
        '''
        riverGroupList = JUtil.javaObjToPyVal(self.riverProDataManager.getRiverGroupList())
        includedRiverGroups = {}
        
        for riverGroup in riverGroupList:
            if riverGroup.isIncludedInRecommendation():
                
                forecastPointList = JUtil.javaObjToPyVal(riverGroup.getForecastPointList())
                includedForecastPointList = []
                
                for forecastPoint in forecastPointList:
                    
                    if forecastPoint.isIncludedInRecommendation():
                        includedForecastPointList.append(forecastPoint.getName())
                        
                includedRiverGroups[riverGroup.getName()] = includedForecastPointList
        
        
        riverString = ''
        
        for river in sorted(includedRiverGroups.keys()):
            riverString += river
            riverString += ' At '
            riverString += '...'.join(includedRiverGroups[river])

        return riverString.rstrip('...')

    ###############################################################
    #
    # Forecast Group Template Variables
    #
    ###############################################################
    def getGroupIdentifier(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpId> template variable.
        e.g. MPLRIV
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_grp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The identifier of the group the river point belongs
                 to
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getGroupId()

    def getGroupName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpIdName> template variable.
        e.g. MAPLE RIVER
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_grp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the group the river point belongs
                 to
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getName()

    def getGroupForecastPointList(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpFPList> template variable.
        e.g. MAPLETON
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A list of forecast point names in the group
        '''
        group = self.getRiverGroup(forecastPointID)
        forecastPointList = JUtil.javaObjToPyVal(group.getForecastPointList())
       
        forecastPointNameList = [] 
        for forecastPoint in forecastPointList:
            forecastPointNameList.append(forecastPoint.getName())
        
        return ','.join(forecastPointNameList)

    def getGroupMaximumObservedFloodCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxCurCat> template variable.
        e.g. 1
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Value of the maximum flood category for all forecast 
                 points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getMaxCurrentObservedCategory()

    def getGroupMaximumObservedFloodCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxCurCatName> template variable.
        e.g. M
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum flood category for all forecast 
                 points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return self.FLOOD_CATEGORY_VALUE_DICT.get(group.getMaxCurrentObservedCategory())

    def getGroupMaximumForecastFloodCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxFcstCat> template variable.
        e.g. 4
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Maximum forecast flood category value for all forecast points 
                 in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getMaxForecastCategory()

    def getGroupMaximumForecastFloodCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxFcstCatName> template variable.
        e.g. RECORD
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum flood category for all forecast 
                 points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return self.FLOOD_CATEGORY_VALUE_DICT.get(group.getMaxForecastCategory())

    def getGroupMaximumObservedForecastFloodCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCat> template variable.
        e.g. 4
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category value for all 
                 forecast points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getMaxOMFCategory()

    def getGroupMaximumObservedForecastFloodCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCatName> template variable.
        e.g. RECORD
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category name for all 
                 forecast points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return self.FLOOD_CATEGORY_VALUE_DICT.get(group.getMaxForecastCategory())

    def getGroupForecastFound(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpFcstFound> template variable.
        e.g. 1
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: True  - a forecast-based flood category was found for the group
                 False - a forecast-based flood category was not found for the group
        '''
        maxForecastFloodCategory = self.getGroupMaximumForecastFloodCategory(forecastPointID)
        
        if maxForecastFloodCategory != MISSING_VALUE:
            return True
        else:
            return False

    def getNumberOfGroups(self):
        '''
        Emulates the functionality of the <NumGrps> template variable.
        e.g. 1
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: The number of available groups.
        '''
        groupList = self.getRiverGroupList()
        return len(groupList)
    
    ###############################################################
    #
    # Location Reference Template Variables
    #
    ###############################################################

    def getRiverPointIdentifier(self, forecastPointID):
        '''
        Emulates the functionality of the <Id> template variable.
        e.g. DCTN1
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The identifier of the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getId()

    def getRiverPointName(self, forecastPointID):
        '''
        Emulates the functionality of the <IdName> template variable.
        e.g. Decatur 
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getName()

    def getRiverPointCounty(self, forecastPointID):
        '''
        Emulates the functionality of the <County> template variable.
        e.g. Monona
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point county
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getCounty()

    def getRiverPointStateIdentifier(self, forecastPointID):
        '''
        Emulates the functionality of the <StateId> template variable.
        e.g. IA
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point state identifier
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getState()

    def getRiverPointStateName(self, forecastPointID):
        '''
        Emulates the functionality of the <StateName> template variable.
        e.g. Iowa
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point state identifier
        '''
        floodDAO = self.riverProDataManager.getFloodDAO()
        stateAbbreviation = self.getRiverPointStateIdentifier(forecastPointID)
        stateName = floodDAO.getStateNameForAbbreviation(stateAbbreviation)
        return stateName

    def getRiverName(self, forecastPointID):
        '''
        Emulates the functionality of the <River> template variable.
        e.g. Missouri River
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the river the forecast point is located on
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        riverName = riverForecastPoint.getStream()
        return riverName

    def getRiverReachName(self, forecastPointID):
        '''
        Emulates the functionality of the <Reach> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river reach associated with the forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getReach()

    def getRiverPointProximity(self, forecastPointID):
        '''
        Emulates the functionality of the <Reach> template variable.
        e.g. at
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river reach associated with the forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getProximity()

    def getRiverPointCounties(self, forecastPointID):
        '''
        Emulates the functionality of the <LocCntyList> template variable.
        e.g. Monona...Burt and Thurston Counties 
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                    load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A list of counties associated with the forecast point.
        '''
        #
        # TODO - Not Needed immediately -- using Area Dictionary
        # Determine if logic must be implemented for this method
        # or if county information can be retrieved from another
        # part of the Product Generation Framework.
        #
        return '** River Point Counties **'

    def getRiverPointGeoArea(self, forecastPointID):
        '''
        Emulates the functionality of the <LocGeoArea> template variable.
        e.g. 4228 9640 4221 9603 4203 9594 4180 9624 4180 9595
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A description of the areas near the river point 
                 affected by the flooding.
        '''
        #
        # TODO. 
        # Access this information from the IHFS database, perhaps 
        # through the flood data access object.
        #
        pass


    #@@@# Eventually may want to deprecate this in favor of getFloodLevels()
    def getFloodLevel(self, forecastPointID, category=MINOR):
        '''
        Emulates the functionality of the <FldStg>, <FldFlw> template variable.
        e.g. 35
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood stage associated with the river point
        '''
        # getFloodStage and getFloodFlow
        # use the category to get the different values
        # TODO, use getFloodStage for reference
        
        primaryPE = self.getPrimaryPhysicalElement(forecastPointID)
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        if primaryPE[0] == PE_H :
            # get flood stage
            return riverForecastPoint.getFloodStage()
        else :
            # get flood flow
            return riverForecastPoint.getFloodFlow()

    #@@@# Eventually may want to deprecate this in favor of getFloodLevel()
    def getFloodLevels(self, forecastPointID, category=MINOR):
        '''
        Emulates the functionality of the <FldStg>, <FldFlw> template variable.
        e.g. 35
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood stage associated with the river point
        '''
        # getFloodStage and getFloodFlow
        # use the category to get the different values
        # TODO, use getFloodStage for reference
        
        primaryPE = self.getPrimaryPhysicalElement(forecastPointID)
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        if primaryPE[0] == PE_H :
            # get flood stage
            return ( riverForecastPoint.getFloodStage(), MISSING_VALUE )
        else :
            # get flood flow
            return ( MISSING_VALUE, riverForecastPoint.getFloodFlow() )

    def getFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <FldStg> template variable.
        e.g. 35
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood stage associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getFloodStage()

    def getBankFullStage(self, forecastPointID):
        '''
        Emulates the functionality of the <BankStg> template variable.
        e.g. 35
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The bank-full stage associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getBankFull()
    
    def getWarningStage(self, forecastPointID):
        '''
        Emulates the functionality of the <WStag> template variable.
        e.g. 35
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The warning (or action) stage associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getActionStage()

    def getFloodFlow(self, forecastPointID):
        '''
        Emulates the functionality of the <FldFlow> template variable.
        e.g. M
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood flow associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getFloodFlow()

    def getZeroDatum(self, forecastPointID):
        '''
        Emulates the functionality of the <ZDatum> template variable.
        e.g. 1010
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The zero datum (base elevation for flood values)
                 associated with the river point
        '''
        floodDAO = self.riverProDataManager.getFloodDAO()
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        zeroDatum = riverStatRecord[self.RIVERSTAT_ZERO_DATUM_FIELD_POSITION]
        return zeroDatum

    def getStageFlowName(self, forecastPointID):
        '''
        Emulates the functionality of the <StgFlowName> template variable.
        e.g. stage
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: 'stage' or 'flow' based on the riverstat primary pe value
        '''
        primaryPE = self.getPrimaryPhysicalElement(forecastPointID)
        if primaryPE[0] == PE_Q:
            return 'flow'
        else:
            return 'stage'

    def getStageFlowUnits(self, forecastPointID):
        '''
        Emulates the functionality of the <StgFlowUnits> template variable.
        e.g. feet
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: 'cfs' or 'feet' based on the riverstat primary pe value.
                 Flow is measured in cubic feet per second (cfs) and 
                 stage is measured in feet.
        '''
        primaryPE = self.getPrimaryPhysicalElement(forecastPointID)
        if primaryPE[0] == PE_Q:
            return 'cfs'
        else:
            return 'feet'

    def getLocationLatitude(self, forecastPointID):
        '''
        Emulates the functionality of the <LocLat> template variable.
        e.g. 42
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The latitude of the river point
        '''
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        return riverStatRecord[self.RIVERSTAT_LATITUDE_FIELD_POSITION]

    def getLocationLongitude(self, forecastPointID):
        '''
        Emulates the functionality of the <LocLon> template variable.
        e.g. 96.2
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The longitude of the river point
        '''
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        return riverStatRecord[self.RIVERSTAT_LONGITUDE_FIELD_POSITION]

    def getRiverMile(self, forecastPointID):
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        return riverStatRecord[self.RIVERSTAT_RIVERMILE_FIELD_POSITION]
        

    ###############################################################
    #
    # Forecast Point Reference Template Variables
    #
    ###############################################################

    def getMinorFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MinCatVal> template variable.
        e.g. 35
                
        @param forecastPointID: The river forecast point identifier.
        @return: The minor flood stage 
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getMinorFloodCategory()

    def getModerateFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ModCatVal> template variable.
        e.g. 38
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The moderate flood stage
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getModerateFloodCategory()
    
    def getMajorFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MajCatVal> template variable.
        e.g. 41
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The major flood stage
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getMajorFloodCategory()

    def getRecordFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <RecCatVal> template variable.
        e.g. 44.4
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The record flood stage
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getRecordFloodCategory()
    
    ###############################################################
    #
    # Forecast Point Reference Template Variables
    #
    ###############################################################
    
    def encodeStageDate(self, stageDateTuple ) :
        retstr = "%.2f"%stageDateTuple[0]+" "
        retstr += "%2.2d"%(stageDateTuple[1].month)+"/"
        retstr += "%2.2d"%(stageDateTuple[1].day)+"/"
        retstr += "%4.4d"%(stageDateTuple[1].year)
        return retstr

    def getImpacts(self, forecastPointID, filters=None):
        '''
                                            
        @param forecastPointID: The river forecast point identifier.
        @return: The stage, date associated with the flood-of-record for this
                                   forecast point.
        '''
        
        if filters is None :
            return [], []

        _PE = self.getPrimaryPhysicalElement(forecastPointID)
        currTime = self.getCurrentTime()

        # query for descriptions here
        jlist = self.riverProDataManager.getFloodDAO().getImpactValues(forecastPointID, currTime.month,currTime.day)
        plist = JUtilHandler.javaCollectionToPyCollection(jlist)
        
        listTuple, maxIndex, diffIndex, recentIndex = self.getIndices(plist, filters, _PE, forecastPointID, 'Impacts')


        # Index values need to be compared to None rather than submitted to a
        # pure boolean test, otherwise index value of 0 (first value in list)
        # will fail to get picked up.
        searchType = filters['Search Type']
        allBelow = False        
        if searchType.find("All Below") >= 0 :        
            allBelow = True
            
        characterizations = []
        descriptions = []
        
        for index, val in enumerate(listTuple) :
            stageOrFlow, impact = val
            
            impactData = impact.split("||")        
            if len(impactData) != 2 :        
                continue
            if allBelow :        
                characterizations.append(("%.2f"%stageOrFlow)+impactData[0])        
                descriptions.append(impactData[1])

        if searchType.find("Highest") >= 0 :
            if maxIndex != None :
                impactData = listTuple[maxIndex][1].split("||")
                stageOrFlow = listTuple[maxIndex][0]
                characterizations.append(("%.2f"%stageOrFlow)+impactData[0])
                descriptions.append(impactData[1])
        elif searchType.find("Closest") >= 0 :
            if diffIndex != None :
                impactData = listTuple[diffIndex][1].split("||")
                stageOrFlow = listTuple[diffIndex][0]
                characterizations.append(("%.2f"%stageOrFlow)+impactData[0])
                descriptions.append(impactData[1])

        return characterizations, descriptions
        


    def getIndices(self, plist, filters, _PE, forecastPointID, impactsOrCrests):
        
        currTime = self.getCurrentTime()
        
        if len(plist)==2:
            if isinstance(plist[0],bool) and isinstance(plist[1],list):
                plist = plist[1]

        listTuple = []
        for pair in plist :
            first = JUtil.javaObjToPyVal(pair.getFirst())
            if not isinstance(first, float) :
                continue
            second = JUtil.javaObjToPyVal(pair.getSecond())
            
            if not isinstance(second, str) and impactsOrCrests == 'Impacts':
                continue
            elif not isinstance(second, datetime.datetime) and impactsOrCrests == 'Crests':
                continue
            pytuple = ( first, second )
            listTuple.append(pytuple)


        referenceType = filters['Reference Type']
        depthBelowFloodStage = float(filters['Depth Below Flood Stage'])
        flowWindowLower = filters['Flow Window Lower']
        flowWindowUpper = filters['Flow Window Upper']
        stageWindowLower = filters['Stage Window Lower']
        stageWindowUpper = filters['Stage Window Upper']
        
        ### yearLookBack found only in Crests.  Expect 'None' for impacts
        yearLookBack = filters.get('Year Lookback')
        
        searchType = filters['Search Type']
        if _PE.startswith(PE_Q) :
            flowStageWindow = filters['Flow Stage Window']
        else :
            flowStageWindow = 0

        if impactsOrCrests == 'Impacts':
            floodStage = float(self.getFloodStage(forecastPointID))
        elif impactsOrCrests == 'Crests':
            floodStage = float(self.getFloodLevel(forecastPointID))
        else:
            floodStage = None

        ### Note: currentDate is only used with 'Crests'
        if referenceType == 'Max Forecast' :
            referenceValue = self.getMaximumForecastLevel(forecastPointID)
            currentDate = self.getMaximumForecastLevel(forecastPointID,TIME)
        elif referenceType == 'Current Observed' :
            referenceValue = self.getObservedLevel(forecastPointID)
            currentDate = self.getObservedLevel(forecastPointID,TIME)
        else :
            maxFcst = self.getMaximumForecastLevel(forecastPointID)
            maxObs = self.getObservedLevel(forecastPointID)
            if maxFcst > maxObs :
                referenceValue = maxFcst
                currentDate = self.getMaximumForecastLevel(forecastPointID,TIME)
            else:
                referenceValue = maxObs
                currentDate = self.getObservedLevel(forecastPointID,TIME)
                
                
        ### curDateDate through minDateDate used with Crests only
        minDateDate = None
        if impactsOrCrests == 'Crests':
            curDateDate = datetime.datetime.fromtimestamp(currentDate / 1000)
            minYear = curDateDate.year+yearLookBack
            if searchType.find("Year Window")<0 or minYear<datetime.MINYEAR :
                minYear = datetime.MINYEAR
            minDateDate = datetime.datetime(minYear, curDateDate.month, curDateDate.day)


        # Flow offsets are all in percent, stage offsets are all in feet.
        if _PE.startswith(PE_Q) :
            flowWindowLower = referenceValue * float(flowWindowLower) * .01
            flowWindowUpper = referenceValue * float(flowWindowUpper) * .01
            flowStageWindow = floodStage * float(flowStageWindow) * .01
            lowerBound = referenceValue - math.fabs(flowWindowLower)
            upperBound = referenceValue + math.fabs(flowWindowUpper)
            floodValueStage = floodStage - math.fabs(flowStageWindow)
        else :
            lowerBound = referenceValue - math.fabs(stageWindowLower)
            upperBound = referenceValue + math.fabs(stageWindowUpper)
            floodValueStage = floodStage - math.fabs(depthBelowFloodStage)

        maximumValue = MISSING_VALUE
        differenceValue = math.fabs(MISSING_VALUE) # start with a huge value

        maxIndex = None
        diffIndex = None
        ### recentDate, recentIndex are for Crests only
        recentDate = minDateDate
        recentIndex = None
        
        if lowerBound > floodValueStage :
            lowerBound = floodValueStage
            
        ### For Impacts only    
        if searchType.find("All Below") >= 0 :        
            lowerBound = 0
        
        
        for index, val in enumerate(listTuple) :
            stageOrFlow, impactOrDate = val
            
            
            if stageOrFlow<lowerBound or stageOrFlow>upperBound :
                if impactsOrCrests == 'Impacts':
                    continue
                else: #impactsOrCrests == 'Crests'
                   if impactOrDate<minDateDate:
                       continue
                
            if stageOrFlow > maximumValue :
                maximumValue = stageOrFlow
                maxIndex = index
            
            diffVal = math.fabs(referenceValue - stageOrFlow )
            if diffVal < differenceValue :
               differenceValue = diffVal
               diffIndex = index
           
            if impactsOrCrests == 'Crests':
               if impactOrDate > recentDate :
                   recentDate = impactOrDate
                   recentIndex = index
               
               
        return listTuple, maxIndex, diffIndex, recentIndex


    
    def getHistoricalCrest(self, forecastPointID, filters=None) :
        '''
        Emulates the functionality of the <HistCrestStg>, <HistCrestDate> template variable.
        e.g. 44.4
                      
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                                         load_variableValue.c - load_pcc_variable_value()
                      
        @param forecastPointID: The river forecast point identifier.
        @return: The stage, date associated with the flood-of-record for this
                                   forecast point.
        '''
        if filters is None :
            return None, None

        _PE = self.getPrimaryPhysicalElement(forecastPointID)

        plist = [ ]
        #if 'Date' in filters :
        if _PE.startswith(PE_Q) :
            jlist = self.riverProDataManager.getFloodDAO().getFlowCrestHistory(forecastPointID)
            plist = JUtilHandler.javaCollectionToPyCollection(jlist)
        elif _PE.startswith(PE_H) :
            jlist = self.riverProDataManager.getFloodDAO().getStageCrestHistory(forecastPointID)
            plist = JUtilHandler.javaCollectionToPyCollection(jlist)

        listTuple, maxIndex, diffIndex, recentIndex = self.getIndices(plist, filters, _PE, forecastPointID, 'Crests')

        # Index values need to be compared to None rather than submitted to a
        # pure boolean test, otherwise index value of 0 (first value in list)
        # will fail to get picked up.
        stageDate = MISSING_VALUE, datetime.datetime.fromtimestamp(0)
        searchType = filters['Search Type']
        if searchType.find("Highest") >= 0 :
            if maxIndex != None :
                stageDate =listTuple[maxIndex]
        elif searchType.find("Closest") >= 0 :
            if diffIndex != None :
                stageDate = listTuple[diffIndex]
        elif recentIndex != None : # most recent
            stageDate = listTuple[recentIndex]

        # Sort by descending crest value
        n = len(listTuple)-1
        t = 0
        while t<n :
            tt = t+1
            while tt<=n :
                if listTuple[t][0]<listTuple[tt][0] :
                    lt = listTuple[t]
                    listTuple[t] = listTuple[tt]
                    listTuple[tt] = lt
                tt += 1
            t += 1

        choiceList = []
        for oneTuple in listTuple :
            choiceList.append(self.encodeStageDate(oneTuple))
        return ( self.encodeStageDate( stageDate ), choiceList )

            
    def getImpactCompUnits(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpCompUnits> template variable.
        e.g. feet
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The unit (ft or cfs) associated with the stage or flow
        '''
        return self.getStageFlowUnits(forecastPointID)

    def getForecastTopRankedTypeSource(self, forecastPointID, primaryPE, duration, extremum):
        floodDAO = self.riverProDataManager.getFloodDAO()
        return floodDAO.getForecastTopRankedTypeSource(forecastPointID, primaryPE, duration, extremum)
    
    ###############################################################
    #
    # Forecast Point Previous Template Variables
    #
    ###############################################################
    
    #
    # TODO: Need to decide how to handle these in Hazard Services
    #
    # NEEDED  getPrevCat(hazardEvent)  - getPreviousCategory
    
    #         getPrevCatName(hazardEvent)
    #         getPrevObsCat(hazardEvent)
    #         getPrevObsCatName(hazardEvent)
    
    # NEEDED  getFcstCat(hazardEvent)  - getForecastCategory
    
    #         getPrevFcstCatName(hazardEvent)
        
    ###############################################################
    #
    # Location Physical Element Template Variables
    #
    ###############################################################
    
    def getPhysicalElementValue(self, forecastPointID, physicalElement, duration,
                                typeSource, extremum, timeArg, derivationInstruction='', timeFlag=False, currentTime_ms=0):    
        '''
        Emulates the functionality of the <PEVal> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_var.c
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The value for the specified physical element and
                 forecast point. 
        '''
                
        data = self.riverProDataManager.getFloodDAO().getPhysicalElement(forecastPointID, physicalElement, duration,
                                typeSource, extremum, timeArg, derivationInstruction, timeFlag, currentTime_ms)
        
        if not timeFlag :
            data = float(data)
        return data
    
    
    def getPhysicalElementTime(self, forecastPointID):    
        '''
        Emulates the functionality of the <PETime> template variable.
        e.g. 20:32:00
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_var.c
                   
        @param forecastPointID: The river forecast point identifier.
        @return: Time of the value for the specified physical element and
                 forecast point. 
        '''
        # TODO 
        pass    
        
    ###############################################################
    #
    # Forecast Point Stage Template Variables
    #
    ###############################################################
    
    def getObservedLevel(self, forecastPointID, type=VALUE): 
        '''
        Emulates the functionality of the <ObsStg>,<ObsCat>,<ObsTime>,<ObsCatName> template variable.
        e.g. 35

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()

        @param forecastPointID: The river forecast point identifier.
        @return: The current observed river stage.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)

        if type == TIME :
            observedTime = MISSING_VALUE
            stageIndex = riverForecastPoint.getObservedCurrentIndex()
            if stageIndex != MISSING_VALUE:
                observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
                observation = observedHydrograph.get(stageIndex)
                observedTime = observation.getValidTime()
            return observedTime
        elif type == CATEGORY :        
            return riverForecastPoint.getCurrentObservationCategory()
        elif type == CATEGORY_NAME :
            category = riverForecastPoint.getCurrentObservationCategory()
            return RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        else :
            value = MISSING_VALUE
            stageIndex = riverForecastPoint.getObservedCurrentIndex()
        
            if stageIndex != MISSING_VALUE:
                observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
                observation = observedHydrograph.get(stageIndex)
                value = observation.getValue()

        return value
   
    def getObservedStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsStg> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The current observed river stage.
        '''
        value = MISSING_VALUE
        shefQualCode = RiverForecastPoints.MISSING_SHEF_QUALITY_CODE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        stageIndex = riverForecastPoint.getObservedCurrentIndex()
        
        if stageIndex != MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(stageIndex)
            value = observation.getValue()
            shefQualCode = observation.getShefQualCode()

        return value, shefQualCode
    
    def getObservedCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCat> template variable.
        e.g. 1
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The flood category value of the current stage observation.
                   
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getCurrentObservationCategory()
    
    def getObservedCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCatName> template variable.
        e.g. Minor 
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The category name of the current stage observation.                    
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        category = riverForecastPoint.getCurrentObservationCategory()
        categoryName = RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        return categoryName
    
    def getObservedTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsTime> Template variable.
        e.g. "1/22 12:00"
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The forecast point identifier
        @return: The time of the current stage observation in milliseconds.            
        '''
        observedTime = MISSING_VALUE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        stageIndex = riverForecastPoint.getObservedCurrentIndex()
        
        if stageIndex != MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(stageIndex)
            observedTime = observation.getValidTime()

        return observedTime
   

    def getMaximumForecastLevel(self, forecastPointID, type=VALUE):
        '''
        Emulates the functionality of the <MaxFcstStg>, <MaxFcstCat>, <MaxFcstCatName>, <MaxFcstTime> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast stage for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        primaryPE = self.getPrimaryPhysicalElement(forecastPointID)
        
        if type == CATEGORY :
            return riverForecastPoint.getMaximumForecastCategory()
        elif type == CATEGORY_NAME :
            forecastPointEventMap = self.riverProDataManager.getForecastPointEventMap()
            category = riverForecastPoint.getMaximumForecastCategory()
            return RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        elif type == TIME :
            maximumForecastTime = MISSING_VALUE
            riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
            maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
            
            if maximumForecastIndex != MISSING_VALUE:
                forecastHydrograph = riverForecastPoint.getForecastHydrograph().getShefHydroDataList()
                maximumForecast = forecastHydrograph.get(maximumForecastIndex)
                maximumForecastTime = maximumForecast.getValidTime()
    
            return maximumForecastTime
        else :
            maximumForecastLevel = MISSING_VALUE
            maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
            if primaryPE.startswith(PE_H) :
                if maximumForecastIndex != MISSING_VALUE:
                    forecastHydrograph = riverForecastPoint.getForecastHydrograph().getShefHydroDataList()
                    forecast = forecastHydrograph.get(maximumForecastIndex)
                    maximumForecastLevel = forecast.getValue()
            elif primaryPE.startswith(PE_Q):
                pass
            return maximumForecastLevel
  
    #@@@# Eventually replace calls to this by some form of call to getMaximumForecastLevel(),
    # and then deprecate getMaximumForecastStage().
    def getMaximumForecastStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstStg> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast stage for this river forecast point.
        '''
        maximumForecastStage = MISSING_VALUE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
        
        if maximumForecastIndex != MISSING_VALUE:
            forecastHydrograph = riverForecastPoint.getForecastHydrograph().getShefHydroDataList()
            forecast = forecastHydrograph.get(maximumForecastIndex)
            maximumForecastStage = forecast.getValue()

        return maximumForecastStage
    
        
    def getMaximumForecastCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstCat> template variable.
        e.g. 1
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood category for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getMaximumForecastCategory()
    
    def getMaximumForecastCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstCatName> template variable.
        e.g. Minor
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood category name for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        forecastPointEventMap = self.riverProDataManager.getForecastPointEventMap()
        category = riverForecastPoint.getMaximumForecastCategory()
        categoryName = RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        return categoryName
  
    def getMaximumForecastTime(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstTime> template variable.
        e.g. "1/22 12:00"
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast time for this river forecast point in milliseconds.
        '''
        maximumForecastTime = MISSING_VALUE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
        
        if maximumForecastIndex != MISSING_VALUE:
            forecastHydrograph = riverForecastPoint.getForecastHydrograph().getShefHydroDataList()
            maximumForecast = forecastHydrograph.get(maximumForecastIndex)
            maximumForecastTime = maximumForecast.getValidTime()

        return maximumForecastTime
    
    def getMaximumObservedForecastValue(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFVal> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast value for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumObservedForecastValue = riverForecastPoint.getMaximumObservedForecastValue()
        return maximumObservedForecastValue
    
    def getMaximumObservedForecastCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFCat> template variable.
        e.g. 1
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast category for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumObservedForecastCategory = riverForecastPoint.getMaximumObservedForecastCategory()
        return maximumObservedForecastCategory
    
    def getMaximumObservedForecastCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFCatName> template variable.
        e.g. Minor
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast category name for this river forecast point.
        '''
        maximumObservedForecastCategory = self.getMaximumObservedForecastCategory(forecastPointID)
        categoryName = RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(maximumObservedForecastCategory, 'UNKNOWN')
        return categoryName
    
    def getStageTrend(self, forecastPointID):
        '''
        Emulates the functionality of the <StgTrend> template variable.
        e.g. Rising
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The stage trend for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        trend = riverForecastPoint.getTrend()
        trendPhrase = self.TREND_VALUE_DESCRIPTION_DICT.get(str(trend), 'UNKNOWN')
        return trendPhrase
    
    def getObservedCrestStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCrestStg> template variable.
        e.g. Rising
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed crest stage for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getObservedCrestValue()
            
    def getObservedCrestTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCrestTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed crest time for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return self._convertToMS(riverForecastPoint.getObservedCrestTime())

    def getForecastTrend(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCrestTime> template variable.
        e.g. EXPECTED TO RISE ABOVE FLOOD STAGE OF  21.0 FT TONIGHT THEN FORECAST 
             TO RISE TO NEAR  28.0 FT WEDNESDAY AFTERNOON

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed crest time for this river forecast point.
        
        '''
        #
        # TODO -- NEEDED
        #
        # The <FcstTrend> template variable generates a detailed phrase describing the overall 
        # characteristics of the forecast stage or discharge time series.  
        # This variable uses a sophisticated algorithm to determine the river forecast trend 
        # characteristics and to allow local configuration of the precise phrasing used to describe the trend.  
        # Appendix E of the RiverPro Reference Manual is dedicated to this template variable.
        #
        pass
        
   
    def getForecastCrest(self, forecastPointID):
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        primaryPE = self.getPrimaryPhysicalElement(forecastPointID)
        if primaryPE[0] == 'H' or primaryPE[0] == 'h':
            return getForecastCrestStage(forecastPointID)
        elif primaryPE[0] == 'F' or primaryPE[0] == 'F' :
            return getForecastCrestTime(forecastPointID)
        return None
    
    def getForecastCrestStage(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstCrestStg> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest stage for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        returnVal = riverForecastPoint.getForecastCrestValue()
        if returnVal == MISSING_VALUE:
            returnVal = None
        return returnVal

    def getForecastCrestTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstCrestTime> template variable.
        e.g. "1/22 12:00"
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest time for this river forecast point in milliseconds
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return self._convertToMS(riverForecastPoint.getForecastCrestTime())
    
    def getObservedRiseAboveFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsRiseFSTime> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed rise above flood stage time for this river 
                  forecast point in milliseconds
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return self._convertToMS(riverForecastPoint.getObservedRiseAboveTime())

    def getObservedFallBelowFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFallFSTime> template variable.
        e.g. "1/22 12:00"
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The observed fall below flood stage time for this river
                 forecast point in milliseconds
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return self._convertToMS(riverForecastPoint.getObservedFallBelowTime())
    
    def getForecastRiseAboveFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstRiseFSTime> template variable.
        e.g. "1/22 12:00"
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The forecast rise above flood stage time for this river
                 forecast point in milliseconds
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return self._convertToMS(riverForecastPoint.getForecastRiseAboveTime())

    def getForecastFallBelowFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFallFSTime> template variable.
        e.g. "1/22 12:00"
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The forecast fall below flood stage time for this river
                 forecast point in milliseconds
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return self._convertToMS(riverForecastPoint.getForecastFallBelowTime()) 
    
    def getObservedDepatureFromFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFSDeparture> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The departure of the observed stage from flood stage.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getObservedFloodStageDeparture()
    
    def getForecastDepartureFromFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFSDeparture> template variable.
        e.g. 7
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The departure of the forecast stage from flood stage.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForcastPoint.getForecastFloodStageDeparture()
    
    def getAbsoluteObservedFloodStageDeparture(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFSDepartureA> template variable.
        e.g. 7
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the observed departure from flood stage.
        '''
        return math.fabs(self.getObservedDepatureFromFloodStage(forecastPointID))
        
    def getAbsoluteForecastFloodStageDeparture(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFSDepartureA> template variable.
        e.g. 7
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the forecast departure from flood stage.
        '''
        return math.fabs(self.getForecastDepartureFromFloodStage(forecastPointID))
        
    def getMaximum24HourObservedStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxObsStg24> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The maximum observed stage for the last 24 hours.
        '''
        maxObservation = MISSING_VALUE
        shefQualCode = self.MISSING_SHEF_QUALITY_CODE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        index = riverForecastPoint.getObservedMax24Index()
        
        if index != MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(index)
            maxObservation = observation.getValue()
            shefQualCode = observation.getShefQualCode()
        
        return maxObservation, shefQualCode
    
    def getMaximum6HourObservedStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxObsStg06> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The maximum observed stage for the last 6 hours.
        '''
        maxObservation = MISSING_VALUE
        shefQualCode = self.MISSING_SHEF_QUALITY_CODE
        shef
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        index = riverForecastPoint.getObservedMax06Index()
        
        if index != MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(stageIndex)
            maxObservation = observation.getValue()
            shefQualCode = observation.getShefQualCode()
        
        return maxObservation, shefQualCode

    
    def getSpecObsStg(self, forecastPointID):
        pass
    
    def getSpecObsStgTime(self, forecastPointID):
        pass
    
    def getSpecFcstStg(self, forecastPointID):
        # Use physical element
        pass
    
    def getSpecFcstStgTime(self, forecastPointID):
        # Use physical element
        pass
    
    def getNumberOfObservations(self, forecastPointID):
        '''
        Emulates the functionality of the <NumObsStg> template variable.
        e.g. 23
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The number of observed stage values.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getNumObsH();
     
    def getNumberOfForecasts(self, forecastPointID):
        '''
        Emulates the functionality of the <NumFcstStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The number of forecast stage values.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getNumFcstH();

    def getRiverForecastPoint(self, forecastPointID):
        '''
        Retrieves the river forecast point data structure for a given
        forecast point id.
        
        @param forecastPointID: The forecast point identifier
        @return: The forecast point data structure for a given forecast
                 point id. 
        '''
        forecastPointEventMap = self.riverProDataManager.getForecastPointEventMap()
        hydroEvent = forecastPointEventMap.get(forecastPointID)
        return hydroEvent.getForecastPoint()
    
    def getRiverStatRecord(self, forecastPointID):
        '''
        Retrieves the record from the riverstat table in the IHFS DB
        for the given forecast point id. The riverstat table contains
        static metadata defining a river point.
        
        @param forecastPointID: The forecast point identifier
        @return: The riverstat record corresponding to the forecast point.
        '''
        floodDAO = self.riverProDataManager.getFloodDAO()
        riverStatRecord = JUtil.javaObjToPyVal(floodDAO.getRiverStationInfo(forecastPointID))
        return riverStatRecord[0]
    
    def getRiverGroup(self, forecastPointID):
        '''
        Retrieves the river group which contains the 
        given forecast point.
        
        @param forecastPointID: The forecast point identifier
        @return: The river group containing the forecast point.
        '''
        groupId = self.getGroupIdentifier(forecastPointID)
        groupList = self.getRiverGroupList()
        
        for group in groupList:
            if groupId == group.getId():
                break

        return group
    
    def getRiverGroupList(self):
        '''
        @return: The list of available river groups.
        '''
        return JUtil.javaObjToPyVal(self.riverProDataManager.getRiverGroupList())

    def getCurrentTime(self, day=0, hour=0, minutes=0):
        return self.currenTime + datetime.timedelta(days=day, hours=hour, minutes=minutes)
 
    # Put back to the old name from __getPrimaryPE, because there is a need for outside
    # clients to use this.   #@@@#
    def getPrimaryPhysicalElement(self, forecastPointID):
        return self.riverProDataManager.getFloodDAO().getPrimaryPE(forecastPointID)
        
    def _convertToMS(self, t):
        if t:
            return t.getTime() 
        else:
            return t

