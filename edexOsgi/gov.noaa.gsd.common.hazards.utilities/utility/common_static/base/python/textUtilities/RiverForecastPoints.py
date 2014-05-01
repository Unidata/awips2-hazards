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
'''
from gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender import RiverProFloodRecommender
from sets import Set

import math
import JUtil

class RiverForecastPoints:
    
    MISSING_VALUE = -9999
    MISSING_SHEF_QUALITY_CODE = 'Z'

    RIVERSTAT_PRIMARY_PE_FIELD_POSITION = 1
    RIVERSTAT_LATITUDE_FIELD_POSITION = 15
    RIVERSTAT_LONGITUDE_FIELD_POSITION = 16
    RIVERSTAT_ZERO_DATUM_FIELD_POSITION = 26
    
    FLOOD_CATEGORY_VALUE_DICT = {-1: 'UNKNOWN',
                                  0: 'NONFLOOD',
                                  1: 'MINOR',
                                  2: 'MODERATE',
                                  3: 'MAJOR',
                                  4: 'RECORD'}
    
    TREND_VALUE_DESCRIPTION_DICT = {  'RISE': 'RISING',
                                      'UNCHANGED': 'STEADY',
                                      'FALL': 'FALLING',
                                      'MISSING': 'UNKNOWN'}
    
    def __init__(self, floodDataAccessObject=None):
        '''
        This class uses the same data structures
        used by the RiverProRiverFloodRecommender class. Each
        time a river forecast point product is created, an
        object of this class should be instantiated.  It will build all
        of the data structures needed by the methods in this class.
        
        @param floodDataAccessObject:  Data access object for river data. 
                                       If not provided, then the default
                                       data access object will be used. 
                                       This parameter is mainly available for 
                                       testing. It allows a test dao to be
                                       injected without the need for a live
                                       hydro database.
        '''
        if floodDataAccessObject is not None:
            self.riverProFloodRecommender = RiverProFloodRecommender(floodDataAccessObject)
        else:
            self.riverProFloodRecommender = RiverProFloodRecommender()
            
    ###############################################################
    #
    # Independent Template Variables
    #
    ###############################################################
    def getGrpList(self):
        '''
        Emulates the functionality of the <GrpList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: The list of available groups
        '''
        return self.getGroupList()

    def getGroupList(self):
        '''
        Emulates the functionality of the <GrpList> template variable.
        
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

    def getRiverList(self):
        '''
        Emulates the functionality of the <RiverList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: The list of rivers in the recommendation
        '''
        return self.getListOfRivers()

    def getListOfRivers(self):
        '''
        Emulates the functionality of the <RiverList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: The list of rivers in the recommendation
        '''
        forecastPointList = JUtil.javaObjToPyVal(self.riverProFloodRecommender.getForecastPointList())
        riverSet = Set()
        
        for forecastPoint in forecastPointList:
            if forecastPoint.isIncludedInRecommendation():
                riverSet.add(forecastPoint.getStream())
        
        riverList = list(riverSet)
        riverList.sort()
    
        return '...'.join(riverList)
    
    def getGrpsFPList(self):
        '''
        Emulates the functionality of the <GrpsFPList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: A string describing the river groups and the forecast points
                 on them
        '''
        return self.getListOfRiverPointsPerGroup()

    def getListOfRiverPointsPerGroup(self):
        '''
        Emulates the functionality of the <GrpsFPList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: A string describing the river groups and the forecast points
                 on them
        '''
        riverGroupList = JUtil.javaObjToPyVal(self.riverProFloodRecommender.getRiverGroupList())
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
    def getGrpId(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpId> template variable.
 
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_grp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The identifier of the group the river point belongs
                 to
        '''
        return self.getGroupIdentifier(forecastPointID)

    def getGroupIdentifier(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpId> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_grp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The identifier of the group the river point belongs
                 to
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getGroupId()

    def getGrpIdName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpIdName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_grp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the group the river point belongs
                 to
        '''
        return self.getGroupName(forecastPointID)

    def getGroupName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpIdName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_grp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the group the river point belongs
                 to
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getName()

    def getGrpFPList(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpFPList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A list of forecast point names in the group
        '''
        return self.getGroupForecastPointList(forecastPointID)

    def getGroupForecastPointList(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpFPList> template variable.
        
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
        
    def getGrpMaxCurCat(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxCurCat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Maximum flood category for all forecast points in the group.
        '''
        return self.getGroupMaximumObservedFloodCategory(forecastPointID)

    def getGroupMaximumObservedFloodCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxCurCat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Value of the maximum flood category for all forecast 
                 points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getMaxCurrentObservedCategory()

    def getGrpMaxCurCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxCurCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum flood category for all forecast 
                 points in the group.
        '''
        return self.getGroupMaximumObservedFloodCategoryName(forecastPointID)

    def getGroupMaximumObservedFloodCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxCurCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum flood category for all forecast 
                 points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return self.FLOOD_CATEGORY_VALUE_DICT.get(group.getMaxCurrentObservedCategory())

    def getGrpMaxFcstCat(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxFcstCat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Maximum forecast flood category for all forecast points 
                 in the group.
        '''
        return self.getGroupMaximumForecastFloodCategory(forecastPointID)

    def getGroupMaximumForecastFloodCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxFcstCat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Maximum forecast flood category value for all forecast points 
                 in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getMaxForecastCategory()

    def getGrpMaxFcstCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxFcstCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum forecast flood category name for all 
                 forecast points in the group.
        '''
        return self.getGroupMaximumForecastFloodCategoryName(forecastPointID)

    def getGroupMaximumForecastFloodCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpMaxCurCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum flood category for all forecast 
                 points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return self.FLOOD_CATEGORY_VALUE_DICT.get(group.getMaxForecastCategory())
    
    def getGrpOMFCat(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category value for all 
                 forecast points in the group.
        '''
        return self.getGroupMaximumObservedForecastFloodCategory(forecastPointID)

    def getGroupMaximumObservedForecastFloodCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category value for all 
                 forecast points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return group.getMaxOMFCategory()
    
    def getGrpOMFCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category name for all 
                 forecast points in the group.
        '''
        return self.getGroupMaximumObservedForecastFloodCategoryName(forecastPointID)

    def getGroupMaximumObservedForecastFloodCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category name for all 
                 forecast points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return self.FLOOD_CATEGORY_VALUE_DICT.get(group.getMaxForecastCategory())

    def getGrpOMFCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category name for all 
                 forecast points in the group.
        '''
        return self.getGroupMaximumObservedForecastFloodCategoryName(forecastPointID)

    def getGroupMaximumObservedForecastFloodCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpOMFCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: Name of the maximum observed/forecast flood category name for all 
                 forecast points in the group.
        '''
        group = self.getRiverGroup(forecastPointID)
        return self.FLOOD_CATEGORY_VALUE_DICT.get(group.getMaxForecastCategory())
    
    def getGrpFcstFound(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpFcstFound> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: True  - a forecast-based flood category was found for the group
                 False - a forecast-based flood category was not found for the group
        '''
        return self.getGroupForecastFound(forecastPointID)

    def getGroupForecastFound(self, forecastPointID):
        '''
        Emulates the functionality of the <GrpFcstFound> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_stagegrp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: True  - a forecast-based flood category was found for the group
                 False - a forecast-based flood category was not found for the group
        '''
        maxForecastFloodCategory = self.getGroupMaximumForecastFloodCategory(forecastPointID)
        
        if maxForecastFloodCategory != self.MISSING_VALUE:
            return True
        else:
            return False

    def getNumGrps(self):
        '''
        Emulates the functionality of the <NumGrps> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_misc_variable_value()
        
        @return: The number of available groups.
        '''
        return self.getNumberOfGroups()

    def getNumberOfGroups(self):
        '''
        Emulates the functionality of the <NumGrps> template variable.
        
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
    def getId(self, forecastPointID):
        '''
        Emulates the functionality of the <Id> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The identifier of the river point
        '''
        return self.getRiverPointIdentifier(forecastPointID)

    def getRiverPointIdentifier(self, forecastPointID):
        '''
        Emulates the functionality of the <Id> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The identifier of the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getId()
    

    def getIdName(self, forecastPointID):
        '''
        Emulates the functionality of the <IdName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the river point
        '''
        return self.getRiverPointName(forecastPointID)

    def getRiverPointName(self, forecastPointID):
        '''
        Emulates the functionality of the <IdName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getName()
    
    def getCounty(self, forecastPointID):
        '''
        Emulates the functionality of the <County> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point county
        '''
        return self.getRiverPointCounty(forecastPointID)

    def getRiverPointCounty(self, forecastPointID):
        '''
        Emulates the functionality of the <County> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point county
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getCounty()

    def getStateId(self, forecastPointID):
        '''
        Emulates the functionality of the <StateId> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point state identifier
        '''
        return self.getRiverPointStateIdentifier(forecastPointID)

    def getRiverPointStateIdentifier(self, forecastPointID):
        '''
        Emulates the functionality of the <StateId> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point state identifier
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getState()
    
    def getStateName(self, forecastPointID):
        '''
        Emulates the functionality of the <StateName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point state name
        '''
        return self.getRiverPointStateName(forecastPointID)

    def getRiverPointStateName(self, forecastPointID):
        '''
        Emulates the functionality of the <StateName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river point state identifier
        '''
        floodDAO = self.riverProFloodRecommender.getFloodDAO()
        stateAbbreviation = self.getRiverPointStateIdentifier(forecastPointID)
        stateName = floodDAO.getStateNameForAbbreviation(stateAbbreviation)
        return stateName
    
    def getRiver(self, forecastPointID):
        '''
        Emulates the functionality of the <River> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the river the forecast point is located on
        '''
        return self.getRiverName(forecastPointID)

    def getRiverName(self, forecastPointID):
        '''
        Emulates the functionality of the <River> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The name of the river the forecast point is located on
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        riverName = riverForecastPoint.getStream()
        return riverName
    
    def getReach(self, forecastPointID):
        '''
        Emulates the functionality of the <Reach> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river reach associated with the forecast point.
        '''
        return self.getRiverReachName(forecastPointID)

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

    def getProximity(self, forecastPointID):
        '''
        Emulates the functionality of the <Proximity> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river reach associated with the forecast point.
        '''
        return self.getRiverPointProximity(forecastPointID)

    def getRiverPointProximity(self, forecastPointID):
        '''
        Emulates the functionality of the <Reach> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The river reach associated with the forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getProximity()
 
    def getLocCntyList(self, forecastPointID):
        '''
        Emulates the functionality of the <LocCntyList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A list of counties associated with the forecast point.
        '''
        return self.getRiverPointCounties(forecastPointID)

    def getRiverPointCounties(self, forecastPointID):
        '''
        Emulates the functionality of the <LocCntyList> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                    load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A list of counties associated with the forecast point.
        '''
        #
        # TODO. Determine if logic must be implemented for this method
        # or if county information can be retrieved from another
        # part of the Product Generation Framework.
        #
        pass
    
    def getLocGeoArea(self, forecastPointID):
        '''
        Emulates the functionality of the <LocGeoArea> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A description of the areas near the river point 
                 affected by the flooding.
        '''
        return self.getRiverPointGeoArea(forecastPointID)

    def getRiverPointGeoArea(self, forecastPointID):
        '''
        Emulates the functionality of the <LocGeoArea> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: A description of the areas near the river point 
                 affected by the flooding.
        '''
        #
        # TODO. Access this information from the IHFS database, perhaps 
        # through the flood data access object.
        #
        pass
    
    def getFldStg(self, forecastPointID):
        '''
        Emulates the functionality of the <FldStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood stage associated with the river point
        '''
        return self.getFloodStage(forecastPointID)

    def getFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <FldStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood stage associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getFloodStage()
    
    def getBankStg(self, forecastPointID):
        '''
        Emulates the functionality of the <BankStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The bank-full stage associated with the river point
        '''
        return self.getBankFullStage(forecastPointID)

    def getBankFullStage(self, forecastPointID):
        '''
        Emulates the functionality of the <BankStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The bank-full stage associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getBankFull()
    
    def getWStag(self, forecastPointID):
        '''
        Emulates the functionality of the <WStag> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The warning (or action) stage associated with the river point
        '''
        return self.getWarningStage(forecastPointID)

    def getWarningStage(self, forecastPointID):
        '''
        Emulates the functionality of the <WStag> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The warning (or action) stage associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getActionStage()

    def getFldFlow(self, forecastPointID):
        '''
        Emulates the functionality of the <FldFlow> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood flow associated with the river point
        '''
        return self.getFloodFlow(forecastPointID)

    def getFloodFlow(self, forecastPointID):
        '''
        Emulates the functionality of the <FldFlow> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood flow associated with the river point
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getFloodFlow()
    
    def getZDatum(self, forecastPointID):
        '''
        Emulates the functionality of the <ZDatum> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The zero datum (base elevation for flood values)
                 associated with the river point
        '''
        return self.getZeroDatum(forecastPointID)

    def getZeroDatum(self, forecastPointID):
        '''
        Emulates the functionality of the <ZDatum> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The zero datum (base elevation for flood values)
                 associated with the river point
        '''
        floodDAO = self.riverProFloodRecommender.getFloodDAO()
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        zeroDatum = riverStatRecord[self.RIVERSTAT_ZERO_DATUM_FIELD_POSITION]
        return zeroDatum

    def getStgFlowName(self, forecastPointID):
        '''
        Emulates the functionality of the <StgFlowName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: 'stage' or 'flow' based on the riverstat primary pe value
        '''
        return self.getStageFlowName(forecastPointID)

    def getStageFlowName(self, forecastPointID):
        '''
        Emulates the functionality of the <StgFlowName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: 'stage' or 'flow' based on the riverstat primary pe value
        '''
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        primaryPE = riverStatRecord[self.RIVERSTAT_PRIMARY_PE_FIELD_POSITION]
        
        if primaryPE[0] == 'Q':
            return 'flow'
        else:
            return 'stage'

    def getStgFlowUnits(self, forecastPointID):
        '''
        Emulates the functionality of the <StgFlowUnits> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: 'cfs' or 'ft' based on the riverstat primary pe value.
                 Flow is measured in cubic feet per second (cfs) and 
                 stage is measured in feet (ft).
        '''
        return self.getStageFlowUnits(forecastPointID)

    def getStageFlowUnits(self, forecastPointID):
        '''
        Emulates the functionality of the <StgFlowUnits> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: 'cfs' or 'feet' based on the riverstat primary pe value.
                 Flow is measured in cubic feet per second (cfs) and 
                 stage is measured in feet.
        '''
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        primaryPE = riverStatRecord[self.RIVERSTAT_PRIMARY_PE_FIELD_POSITION]
        
        if primaryPE[0] == 'Q':
            return 'cfs'
        else:
            return 'feet'

    def getLocLat(self, forecastPointID):
        '''
        Emulates the functionality of the <LocLat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The latitude of the river point
        '''
        return self.getLocationLatitude(forecastPointID)

    def getLocationLatitude(self, forecastPointID):
        '''
        Emulates the functionality of the <LocLat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The latitude of the river point
        '''
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        return riverStatRecord[self.RIVERSTAT_LATITUDE_FIELD_POSITION]
    
    def getLocLon(self, forecastPointID):
        '''
        Emulates the functionality of the <LocLon> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The longitude of the river point
        '''
        return self.getLocationLongitude(forecastPointID)

    def getLocationLongitude(self, forecastPointID):
        '''
        Emulates the functionality of the <LocLon> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_locinfo_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The longitude of the river point
        '''
        riverStatRecord = self.getRiverStatRecord(forecastPointID)
        return riverStatRecord[self.RIVERSTAT_LONGITUDE_FIELD_POSITION]
    
        

    ###############################################################
    #
    # Forecast Point Reference Template Variables
    #
    ###############################################################
    def getMinCatVal(self, forecastPointID):
        '''
        Emulates the functionality of the <MinCatVal> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variable_value.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The minor flood stage
        '''
        return self.getMinorFloodStage(forecastPointID)

    def getMinorFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MinCatVal> template variable.
        
        @param forecastPointID: The river forecast point identifier.
        @return: The minor flood stage 
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getMinorFloodCategory()
    

    def getModCatVal(self, forecastPointID):
        '''
        Emulates the functionality of the <ModCatVal> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The moderate flood stage
        '''
        return self.getModerateFloodStage(forecastPointID)

    def getModerateFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ModCatVal> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The moderate flood stage
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getModerateFloodCategory()
    
    def getMajCatVal(self, forecastPointID):
        '''
        Emulates the functionality of the <MajCatVal> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The major flood stage
        '''
        return self.getMajorFloodStage(forecastPointID)

    def getMajorFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MajCatVal> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The major flood stage
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getMajorFloodCategory()
    
    def getRecCatVal(self, forecastPointID):
        '''
        Emulates the functionality of the <RecCatVal> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The record flood stage
        '''
        return self.getRecordFloodStage(forecastPointID)

    def getRecordFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <RecCatVal> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_fp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The record flood stage
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getRecordFloodCategory()
    
    def getImpactStg(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpactStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_misc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The impact stage associated with the current observed/forecast
                 flood stage.
        '''
        return self.getImpactStage(self, forecastPointID)
    
    def getImpactStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpactStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_misc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The impact stage associated with the current observed/forecast
                 flood stage.
        '''
        #
        # TODO: code will have to be written to support the
        # retrieval of impact information. The RiverFloodRecommender
        # does not handle this.
        #
        pass
        
    def getImpactDescr(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpactDescr> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_misc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The impact statement associated with the current 
                 observed/forecast flood stage.
        '''
        return self.getImpactDescription(self, forecastPointID)
    
    def getImpactDescription(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpactDescr> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_misc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The impact statement associated with the current 
                 observed/forecast flood stage.
        '''
        #
        # TODO: code will have to be written to support the
        # retrieval of impact information. The RiverFloodRecommender
        # does not handle this.
        #
        pass
    
    def getHistCrestDate(self, forecastPointID):
        '''
        Emulates the functionality of the <HistCrestDate> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The date associated with the flood-of-record for this
                 forecast point.
        '''
        self.getHistoricalCrestDate(self, forecastPointID)

    def getHistoricalCrestDate(self, forecastPointID):
        '''
        Emulates the functionality of the <HistCrestDate> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The date associated with the flood-of-record for this
                 forecast point.
        '''
        #
        # TODO: code will have to be written to support the
        # retrieval of impact information. The RiverFloodRecommender
        # does not handle this.
        #
        pass

    def getHistCrestStg(self, forecastPointID):
        '''
        Emulates the functionality of the <HistCrestStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The stage associated with the flood-of-record for this
                 forecast point.
        '''
        self.getHistoricalCrestStage(self, forecastPointID)

    def getHistoricalCrestStage(self, forecastPointID):
        '''
        Emulates the functionality of the <HistCrestStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The stage associated with the flood-of-record for this
                 forecast point.
        '''
        #
        # TODO: code will have to be written to support the
        # retrieval of impact information. The RiverFloodRecommender
        # does not handle this.
        #
        pass


    def getImpCompUnits(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpCompUnits> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The unit (ft or cfs) associated with the stage or flow
        '''
        return self.getStageFlowUnits(forecastPointID)

              

    ###############################################################
    #
    # Forecast Point Previous Template Variables
    #
    ###############################################################
    
    #
    # TODO: Need to decide how to handle these in Hazazard Services
    

    ###############################################################
    #
    # Location Physical Element Template Variables
    #
    ###############################################################
    def getPEVal(self, forecastPointID):
        '''
        Emulates the functionality of the <PEVal> template variable.
        
        @param forecastPointID: The river forecast point identifier.
        @return: The value for the specified physical element and
                 forecast point. 
        '''
        return self.getPhysicalElementValue(forecastPointID)
    
    def getPhysicalElementValue(self, forecastPointID):    
        '''
        Emulates the functionality of the <PEVal> template variable.

        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_var.c
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The value for the specified physical element and
                 forecast point. 
        '''
        pass
    
    def getPETime(self, forecastPointID):
        '''
        Emulates the functionality of the <PETime> template variable.
        
        @param forecastPointID: The river forecast point identifier.
        @return: Time of the value for the specified physical element and 
                 forecast point.
        '''
        return self.getPhysicalElementTime(forecastPointID)
    
    def getPhysicalElementTime(self, forecastPointID):    
        '''
        Emulates the functionality of the <PETime> template variable.

        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_var.c
                   
        @param forecastPointID: The river forecast point identifier.
        @return: Time of the value for the specified physical element and
                 forecast point. 
        '''
        pass

    
        
    
    
        
    ###############################################################
    #
    # Forecast Point Stage Template Variables
    #
    ###############################################################
    def getObsStg(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsStg> template variable.
        
        @param forecastPointIDparam: The river forecast point identifier.
        @return: The current observed river stage. 
        '''
        return self.getObservedStage(forecastPointID)
    
    def getObservedStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsStg> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The current observed river stage.
        '''
        value = self.MISSING_VALUE
        shefQualCode = RiverForecastPoints.MISSING_SHEF_QUALITY_CODE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        stageIndex = riverForecastPoint.getObservedCurrentIndex()
        
        if stageIndex != RiverForecastPoints.MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(stageIndex)
            value = observation.getValue()
            shefQualCode = observation.getShefQualCode()

        return value, shefQualCode
    
    def getObsCat(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCat> template variable.
        
        @param forecastPointID: The river forecast point identifier.
        @return: The flood category value of the current stage observation.
        '''
        return self.getObservedCategory(forecastPointID)
    
    def getObservedCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCat> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The flood category value of the current stage observation.
                   
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getCurrentObservationCategory()
       
    def getObsCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCatName> template variable.
        
        @param forecastPointID: The river forecast point identifier.
        @return: The category name of the current stage observation.
        '''
        return self.getObservedCategoryName(forecastPointID)
    
    def getObservedCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCatName> template variable.
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The river forecast point identifier.
        @return: The category name of the current stage observation.                    
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        category = riverForecastPoint.getCurrentObservationCategory()
        categoryName = RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        return categoryName
    
    def getObsTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsTime> template variable.
        
        @param forecastPointID: The river forecast point identifier.
        @return: The time of the current stage observation in milliseconds. 
        '''
        return self.getObservedTime(forecastPointID)
    
    def getObservedTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsTime> Template variable.
                
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
                   
        @param forecastPointID: The forecast point identifier
        @return: The time of the current stage observation in milliseconds.            
        '''
        observedTime = self.MISSING_VALUE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        stageIndex = riverForecastPoint.getObservedCurrentIndex()
        
        if stageIndex != RiverForecastPoints.MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(stageIndex)
            observedTime = observation.getValidTime()

        return observedTime
    
    def getMaxFcstStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstStg> template variable.
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast stage for this river forecast point.
        '''
        return self.getMaximumForecastStage(forecastPointID)
    
    def getMaximumForecastStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast stage for this river forecast point.
        '''
        maximumForecastStage = self.MISSING_VALUE
        shefQualCode = RiverForecastPoints.MISSING_SHEF_QUALITY_CODE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
        
        if maximumForecastIndex != RiverForecastPoints.MISSING_VALUE:
            forecastHydrograph = riverForecastPoint.getForecastHydrograph().getShefHydroDataList()
            forecast = forecastHydrograph.get(maximumForecastIndex)
            maximumForecastStage = forecast.getValue()
            shefQualCode = forecast.getShefQualCode()

        return maximumForecastStage, shefQualCode
    
    def getMaxFcstCat(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstCat> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood category for this river forecast point.
        '''
        return self.getMaximumForecastCategory(forecastPointID)
    
    def getMaximumForecastCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstCat> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood category for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getMaximumForecastCategory()
        
    def getMaxFcstCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstCatName> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood category name for this river forecast point.
        '''
        return self.getMaximumForecastCatName(forecastPointID)
    
    def getMaximumForecastCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstCatName> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood category name for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        forecastPointEventMap = self.riverProFloodRecommender.getForecastPointEventMap()
        category = riverForecastPoint.getMaximumForecastCategory()
        categoryName = RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(category, 'UNKNOWN')
        return categoryName
    
    def getMaxFcstTime(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast flood time for this river forecast point.
        '''
        return self.getMaximumForecastTime(forecastPointID)
    
    def getMaximumForecastTime(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast time for this river forecast point.
        '''
        maximumForecastTime = self.MISSING_VALUE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
        
        if maximumForecastIndex != RiverForecastPoints.MISSING_VALUE:
            forecastHydrograph = riverForecastPoint.getForecastHydrograph().getShefHydroDataList()
            maximumForecast = forecastHydrograph.get(maximumForecastIndex)
            maximumForecastTime = maximumForecast.getValidTime()

        return maximumForecastTime
    
    def getOMFVal(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFVal> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast value for this river forecast point.
        '''
        
        return self.getMaximumObservedForecastValue(forecastPointID)
    
    def getMaximumObservedForecastValue(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFVal> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast value for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumObservedForecastValue = riverForecastPoint.getMaximumObservedForecastValue()
        return maximumObservedForecastValue
    
    def getOMFCat(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFCat> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast category for this river forecast point.
        '''
        return self.getMaximumObservedForecastCategory(forecastPointID)
    
    def getMaximumObservedForecastCategory(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFCat> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast category for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumObservedForecastCategory = riverForecastPoint.getMaximumObservedForecastCategory()
        return maximumObservedForecastCategory
    
    def getOMFCatName(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFCatName> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast category name for this river forecast point.
        '''
        return self.getMaximumObservedForecastCategoryName(forecastPointID)
    
    def getMaximumObservedForecastCategoryName(self, forecastPointID):
        '''
        Emulates the functionality of the <OMFCatName> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum observed/forecast category name for this river forecast point.
        '''
        maximumObservedForecastCategory = self.getMaximumObservedForecastCategory(forecastPointID)
        categoryName = RiverForecastPoints.FLOOD_CATEGORY_VALUE_DICT.get(maximumObservedForecastCategory, 'UNKNOWN')
        return categoryName
    
    def getStgTrend(self, forecastPointID):
        '''
        Emulates the functionality of the <StgTrend> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The stage trend for this river forecast point.
        '''
        return self.getStageTrend(forecastPointID)
    
    def getStageTrend(self, forecastPointID):
        '''
        Emulates the functionality of the <StgTrend> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_xfp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The stage trend for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        trend = riverForecastPoint.getTrend()
        trendPhrase = self.TREND_VALUE_DESCRIPTION_DICT.get(str(trend), 'UNKNOWN')
        return trendPhrase
    
    def getObsCrestStg(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCrestStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed crest stage for this river forecast point.
        '''
        return self.getObservedCrestStage(forecastPointID)

        

    def getObservedCrestStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCrestStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed crest stage for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getObservedCrestValue()
            
    def getObsCrestTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCrestTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed crest time for this river forecast point.
        '''
        return self.getObservedCrestTime(forecastPointID)

    def getObservedCrestTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsCrestTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed crest time for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getObservedCrestTime()
        
    def getFcstCrestStg(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstCrestStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest stage for this river forecast point.
        '''
        return self.getForecastCrestStage(forecastPointID)
    
    def getForecastCrestStage(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstCrestStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest stage for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getForecastCrestValue()
    
    def getFcstCrestTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstCrestTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest time for this river forecast point.
        '''
        return self.getForecastCrestTime(forecastPointID)
    
    def getForecastCrestTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstCrestTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest time for this river forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getForecastCrestTime()
    
    def getObsRiseFSTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsRiseFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed rise above flood stage time for this river 
                  forecast point.
        '''
        return self.getObservedRiseAboveFloodStageTime(forecastPointID)
    
    def getObservedRiseAboveFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsRiseFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed rise above flood stage time for this river 
                  forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getObservedRiseAboveTime() 
        
    def getObsFallFSTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFallFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The observed fall below flood stage time for this river
                  forecast point. 
        '''
        return self.getObservedFallBelowFloodStageTime(forecastPointID)

    def getObservedFallBelowFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFallFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The observed fall below flood stage time for this river
                 forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getObservedFallBelowTime() 
       
    def getFcstRiseFSTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstRiseFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The forecast rise above flood stage time for this river
                 forecast point.s
        '''
        return self.getForecastRiseAboveFloodStageTime(forecastPointID)
    
    def getForecastRiseAboveFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstRiseFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The forecast rise above flood stage time for this river
                 forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getForecastRiseAboveTime() 
    
    def getFcstFallFSTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFallFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The forecast fall below flood stage time for this river
                 forecast point.
        '''
        return self.getForecastFallBelowFloodStageTime(forecastPointID)
    
    def getForecastFallBelowFloodStageTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFallFSTime> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The forecast fall below flood stage time for this river
                 forecast point.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getForecastFallBelowTime() 
    
    def getObsFSDeparture(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFSDeparture> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The departure of the observed stage from flood stage.
        '''
        return self.getObservedDepatureFromFloodStage(forecastPointID)
    
    def getObservedDepatureFromFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFSDeparture> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The departure of the observed stage from flood stage.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getObservedFloodStageDeparture()
    
    def getFcstFSDeparture(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFSDeparture> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The departure of the forecast stage from flood stage.
        '''
        return self.getForecastDepartureFromFloodStage(forecastPointID)
    
    def getForecastDepartureFromFloodStage(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFSDeparture> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The departure of the forecast stage from flood stage.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForcastPoint.getForecastFloodStageDeparture()
    
    def getObsFSDepatureA(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFSDepartureA> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the observed departure from flood stage.
        '''
        return self.getAbsoluteObservedFloodStageDeparture(forecastPointID)
    
    def getAbsoluteObservedFloodStageDeparture(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsFSDepartureA> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the observed departure from flood stage.
        '''
        return math.fabs(self.getObservedDepatureFromFloodStage(forecastPointID))
    
    def getFcstFSDepatureA(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFSDepartureA> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the forecast departure from flood stage.
        '''
        return self.getAbsoluteForecastFloodStageDeparture(forecastPointID)
    
    def getAbsoluteForecastFloodStageDeparture(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstFSDepartureA> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The absolute value of the forecast departure from flood stage.
        '''
        return math.fabs(self.getForecastDepartureFromFloodStage(forecastPointID))
    
    def getMaxObsStg24(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxObsStg24> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The maximum observed stage for the last 24 hours.
        '''
        return self.getMaximum24HourObservedStage(forecastPointID)
    
    def getMaximum24HourObservedStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxObsStg24> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The maximum observed stage for the last 24 hours.
        '''
        maxObservation = self.MISSING_VALUE
        shefQualCode = self.MISSING_SHEF_QUALITY_CODE
        shef
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        index = riverForecastPoint.getObservedMax24Index()
        
        if index != self.MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(stageIndex)
            maxObservation = observation.getValue()
            shefQualCode = observation.getShefQualCode()
        
        return maxObservation, shefQualCode

    def getMaxObsStg06(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxObsStg06> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The maximum observed stage for the last 6 hours.
        '''
        return self.getMaximum6HourObservedStage(forecastPointID)
    
    def getMaximum6HourObservedStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxObsStg06> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The maximum observed stage for the last 6 hours.
        '''
        maxObservation = self.MISSING_VALUE
        shefQualCode = self.MISSING_SHEF_QUALITY_CODE
        shef
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        index = riverForecastPoint.getObservedMax06Index()
        
        if index != self.MISSING_VALUE:
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
        pass
    
    def getSpecFcstStgTime(self, forecastPointID):
        pass
    
    def getNumObsStg(self, forecastPointID):
        '''
        Emulates the functionality of the <NumObsStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The number of observed stage values.
        '''
        return self.getNumberOfObservations(forecastPointID)
    
    def getNumberOfObservations(self, forecastPointID):
        '''
        Emulates the functionality of the <NumObsStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The number of observed stage values.
        '''
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        return riverForecastPoint.getNumObsH();
    
    def getNumFcstStg(self, forecastPointID):
        '''
        Emulates the functionality of the <NumFcstStg> template variable.

        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The number of forecast stage values.
        '''
        return self.getNumberOfForecasts(forecastPointID)
     
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
        forecastPointEventMap = self.riverProFloodRecommender.getForecastPointEventMap()
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
        floodDAO = self.riverProFloodRecommender.getFloodDAO()
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
        return JUtil.javaObjToPyVal(self.riverProFloodRecommender.getRiverGroupList())

