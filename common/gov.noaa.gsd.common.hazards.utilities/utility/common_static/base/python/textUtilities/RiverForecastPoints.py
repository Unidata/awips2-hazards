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
'''
from com.raytheon.uf.common.hazards.hydro import RiverProDataManager
from sets import Set

import math
import JUtil

class RiverForecastPoints(object):
    
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
        Emulates the functionality of the <GrpMaxCurCatName> template variable.
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
        
        if maxForecastFloodCategory != self.MISSING_VALUE:
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
        if primaryPE[0] == 'Q':
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
        if primaryPE[0] == 'Q':
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
    
    def getImpactStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpactStg> template variable.
        e.g. 27
        
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
    
    def getImpactDescription(self, forecastPointID):
        '''
        Emulates the functionality of the <ImpactDescr> template variable.
        e.g. Widespread flooding envelopes the reach from just north of the airport
        downstream to the southwest of Mapleton.  A levee protects the town of Mapleton.
        
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

    def getHistoricalCrestDate(self, forecastPointID):
        '''
        Emulates the functionality of the <HistCrestDate> template variable.
        e.g. "May 31 1959"
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The date associated with the flood-of-record for this
                 forecast point.
        '''
        #
        # TODO: NEEDED for floodHistoryBullet
        
        # code will have to be written to support the
        # retrieval of impact information. The RiverFloodRecommender
        # does not handle this.
        #
        pass

    def getHistoricalCrestStage(self, forecastPointID):
        '''
        Emulates the functionality of the <HistCrestStg> template variable.
        e.g. 44.4
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT/
                   load_variableValue.c - load_pcc_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return: The stage associated with the flood-of-record for this
                 forecast point.
        '''
        #
        # TODO: NEEDED for floodHistoryBullet
        
        # code will have to be written to support the
        # retrieval of impact information. The RiverFloodRecommender
        # does not handle this.
        #
        pass
    
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
                                typeSource, extremum, timeArg, derivationInstruction='', timeFlag=False):    
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
                                typeSource, extremum, timeArg, derivationInstruction, timeFlag)
        
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
        
    def getPrimaryPhysicalElement(self, forecastPointID):
        return self.riverProDataManager.getFloodDAO().getPrimaryPE(forecastPointID)
        
    ###############################################################
    #
    # Forecast Point Stage Template Variables
    #
    ###############################################################
    
    def getObservedStage(self, forecastPointID):
        '''
        Emulates the functionality of the <ObsStg> template variable.
        e.g. 35
        
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
        observedTime = self.MISSING_VALUE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        stageIndex = riverForecastPoint.getObservedCurrentIndex()
        
        if stageIndex != RiverForecastPoints.MISSING_VALUE:
            observedHydrograph = riverForecastPoint.getObservedHydrograph().getShefHydroDataList()
            observation = observedHydrograph.get(stageIndex)
            observedTime = observation.getValidTime()

        return observedTime
   
    def getMaximumForecastLevel(self, forecastPointID, primaryPE):
        '''
        Emulates the functionality of the <MaxFcstStg> template variable.
        e.g. 35
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ofp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The maximum forecast stage for this river forecast point.
        '''
        maximumForecastLevel = self.MISSING_VALUE
        shefQualCode = RiverForecastPoints.MISSING_SHEF_QUALITY_CODE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
        if primaryPE[0] == 'h' or primaryPE[0] == 'H' :
            if maximumForecastIndex != RiverForecastPoints.MISSING_VALUE:
                forecastHydrograph = riverForecastPoint.getForecastHydrograph().getShefHydroDataList()
                forecast = forecastHydrograph.get(maximumForecastIndex)
                maximumForecastStage = forecast.getValue()
                shefQualCode = forecast.getShefQualCode()
        elif primaryPE[0] == 'q' or primaryPE[0] == 'Q':
            pass
        # TODO, maybe we don't need to return the shefQualCode, rather
        # we could return a more pertinent maximumForecastStage
        return maximumForecastLevel, shefQualCode
    
    def getMaximumForecastStage(self, forecastPointID):
        '''
        Emulates the functionality of the <MaxFcstStg> template variable.
        e.g. 35
        
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
        maximumForecastTime = self.MISSING_VALUE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        maximumForecastIndex = riverForecastPoint.getMaximumForecastIndex()
        
        if maximumForecastIndex != RiverForecastPoints.MISSING_VALUE:
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
        return riverForecastPoint.getForecastCrestValue()

    def getForecastCrestTime(self, forecastPointID):
        '''
        Emulates the functionality of the <FcstCrestTime> template variable.
        e.g. "1/22 12:00"
        
        Reference: AWIPS2_baseline/nativeLib/rary.ohd.whfs/src/RPFEngine/TEXT
                   load_variable_value.c -  load_stage_ffp_variable_value()
        
        @param forecastPointID: The river forecast point identifier.
        @return:  The forecast crest time for this river forecast point  in milliseconds
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
        maxObservation = self.MISSING_VALUE
        shefQualCode = self.MISSING_SHEF_QUALITY_CODE
        riverForecastPoint = self.getRiverForecastPoint(forecastPointID)
        index = riverForecastPoint.getObservedMax24Index()
        
        if index != self.MISSING_VALUE:
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
    
    
    def _convertToMS(self, t):
        if t:
            return t.getTime() 
        else:
            return t

