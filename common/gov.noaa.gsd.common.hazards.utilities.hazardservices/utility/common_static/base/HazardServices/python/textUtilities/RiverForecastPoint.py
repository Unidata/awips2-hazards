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
# #


'''
    A Python Wrapper of RiverForecastPoint.java. This allows focal points the ability
    to see the available methods on the Java object. Note that only getter methods 
    have been implemented at this time, since this is strictly used for accessing
    data.
'''

import JUtil
from com.raytheon.uf.common.hazards.hydro import RiverHydroConstants
HydroGraphTrend = RiverHydroConstants.HydroGraphTrend

from HydrographHandler import pyHydrographToJavaHydrograph, javaHydrographToPyHydrograph
JUtil.registerPythonToJava(pyHydrographToJavaHydrograph)
JUtil.registerJavaToPython(javaHydrographToPyHydrograph)

class RiverForecastPoint(JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        self.jobj = wrappedObject

    def __eq__(self, other):
        return self.jobj.equals(other.jobj)

    def __ne__(self, other):
        return not self.__eq__(other)

    def __str__(self):
        string = 'RiverForecastPoint: ' + self.jobj.toString()
        return string

    def toJavaObj(self):
        return self.jobj

##########################################################
#
#    Getter methods from FpInfo.java
#
##########################################################

    def getLid(self):
        '''
        Gets the identifier of this forecast point.
        '''
        return self.jobj.getLid()

    def getGroupId(self):
        '''
        Gets the group id of this forecast point.
        '''
        return self.jobj.getGroupId()

    def isIncludedInRecommendation(self):
        '''
        Gets whether or not this forecast point should be included in a
        recommendation
        '''
        return self.jobj.isIncludedInRecommendation()

    def getName(self):
        '''
        Gets the name of this forecast point.
        '''
        return self.jobj.getName()

    def getFloodStage(self):
        '''
        Gets the forecast points flood stage.
        '''
        return self.jobj.getFloodStage()

    def getActionStage(self):
        '''
        Gets the forecast points action stage.
        '''
        return self.jobj.getActionStage()

    def getLocation(self):
        '''
        Gets a Coordinate representing this station's location.
        '''
        return self.jobj.getLocation()

    def getPhysicalElement(self):
        '''
        Gets the primary physical element
        '''
        return self.jobj.getPhysicalElement()

    def getFloodCategory(self):
        '''
        Gets the flood category reached by the data associated with this point.
        '''
        return JUtil.javaObjToPyVal(self.jobj.getFloodCategory())

    def getProximity(self):
        '''
        Gets the proximity from the descrip table.
        '''
        return self.jobj.getProximity()

    def getReach(self):
        '''
        Gets the river reach associated with this point.
        '''
        return self.jobj.getReach()

    def getActionFlow(self):
        '''
        Gets the action flow value.
        '''
        return self.jobj.getActionFlow()

    def getCounty(self):
        '''
        Gets the county this point is in.
        '''
        return self.jobj.getCounty()

    def getState(self):
        '''
        Gets the state this point resides in.
        '''
        return self.jobj.getState()

    def getStream(self):
        '''
        Gets the name of the stream this forecast point is on.
        '''
        return self.jobj.getStream()

    def getBankFull(self):
        '''
        Gets the bankfull stage.
        '''
        return self.jobj.getBankFull()

    def getHsa(self):
        '''
        Gets the Hydrologic Service Area this point resides in.
        '''
        return self.jobj.getHsa()

    def getPrimaryBackup(self):
        '''
        Gets the primary backup office for this point.
        '''
        return self.jobj.getPrimaryBackup()

    def getRecommendationType(self):
        '''
        Gets the recommendation type associated with this point.
        '''
        return self.jobj.getRecommendationType()

    def getSecondaryBackup(self):
        '''
        Gets the secondary backup office for this point.
        '''
        return self.jobj.getSecondaryBackup()

    def getMinorFloodCategory(self):
        '''
        Gets the minor flood category.
        '''
        return self.jobj.getMinorFloodCategory()

    def getModerateFloodCategory(self):
        '''
        Gets the moderate flood category.
        '''
        return self.jobj.getModerateFloodCategory()

    def getMajorFloodCategory(self):
        '''
        Gets the major flood category.
        '''
        return self.jobj.getMajorFloodCategory()

    def getRecordFloodCategory(self):
        '''
        Gets the record flood category.
        '''
        return self.jobj.getRecordFloodCategory()

    def getFloodFlow(self):
        '''
        Gets the floodFlow.
        '''
        return self.jobj.getFloodFlow()

    def getMinorFlow(self):
        '''
        Gets the minor flow for this point..
        '''
        return self.jobj.getMinorFlow()

    def getModerateFlow(self):
        '''
        Gets the moderate flow for this point.
        '''
        return self.jobj.getModerateFlow()

    def getMajorFlow(self):
        '''
        Gets the major flow for this point.
        '''
        return self.jobj.getMajorFlow()

    def getMinorStage(self):
        '''
        Gets the minor stage for this point.
        '''
        return self.jobj.getMinorStage()

    def getModerateStage(self):
        '''
        Gets the moderate stage for this point.
        '''
        return self.jobj.getModerateStage()

    def getMajorStage(self):
        '''
        Gets the major stage for this point.
        '''
        return self.jobj.getMajorStage()

    def getUseLatestForecast(self):
        '''
        Gets whether or not the latest forecast is used.
        '''
        return self.jobj.getUseLatestForecast()

    def getOrdinal(self):
        '''
        Gets the ordinal.
        '''
        return self.jobj.getOrdinal()

    def getChangeThreshold(self):
        '''
        Gets the change threshold.
        '''
        return self.jobj.getChangeThreshold()

    def getBackHrs(self):
        '''
        Gets the back hours for this point.
        '''
        return self.jobj.getBackHrs()

    def getForwardHrs(self):
        '''
        Gets the forward hours for this point.
        '''
        return self.jobj.getForwardHrs()

    def getAdjustEndHrs(self):
        '''
        Gets the adjusted end hours for this point.
        '''
        return self.jobj.getAdjustEndHrs()

    def getLatitude(self):
        '''
        Gets the latitude of this point.
        '''
        return self.jobj.getLatitude()

    def getLongitude(self):
        '''
        Gets the longitude of this point.
        '''
        return self.jobj.getLongitude()

##########################################################
#
#    Getter methods from RiverForecastPoint.java
#
##########################################################

    def getPreviousCurObsValue(self):
        '''
        Gets the observed value of the previous recommended event.
        '''
        return self.jobj.getPreviousCurObsValue()

    def getPreviousCurrentObsTime(self):
        '''
        Gets the time of the previous observed value.
        '''
        return self.jobj.getPreviousCurrentObsTime()

    def getPreviousProductAvailable(self):
        '''
        Gets whether there is there a previous product available.
        '''
        return self.jobj.getPreviousProductAvailable()

    def getPreviousMaxFcstValue(self):
        '''
        Gets the previous maximum forecast value.
        '''
        return self.jobj.getPreviousMaxFcstValue()

    def getPreviousMaxFcstTime(self):
        '''
        Gets the previous maximum forecast time.
        '''
        return self.jobj.getPreviousMaxFcstTime()

    def getPreviousMaxFcstCTime(self):
        '''
        Gets the previous maximum forecast crest time.
        '''
        return self.jobj.getPreviousMaxFcstCTime()

    def getPreviousMaxObservedForecastCategory(self):
        '''
        Gets the previous event maximum observed forecast category.
        '''
        return self.jobj.getPreviousMaxObservedForecastCategory()

    def getCurrentObservation(self):
        '''
        Gets the current observation, which is a SHEFObserved Java object.
        '''
        return self.jobj.getCurrentObservation()

    def getCurrentObservationCategory(self):
        '''
        Gets the current observation flood category.
        '''
        return self.jobj.getCurrentObservationCategory()

    def getMaximumForecastCategory(self):
        '''
        Gets the maximum forecast flood category.
        '''
        return self.jobj.getMaximumForecastCategory()

    def getMaximumSHEFForecast(self):
        '''
        Gets the maximum SHEF forecast, which is a SHEFForecast Java object.
        '''
        return self.jobj.getMaximumSHEFForecast()

    def getMaximumSHEFObserved(self):
        '''
        Gets the maximum SHEF Observed, which is a SHEFObserved Java object.
        '''
        return self.jobj.getMaximumSHEFObserved()

    def getRiseAboveTime(self):
        '''
        Gets the rise above flood stage time.
        '''
        return self.jobj.getRiseAboveTime()

    def getMaximumObservedForecastCategory(self):
        '''
        Gets the maximum observed forecast flood category.
        '''
        return self.jobj.getMaximumObservedForecastCategory()

    def getRiseOrFall(self):
        '''
        Gets the trend of the hydrograph trend associated with this forecast point.
        '''
        trend = self.jobj.getRiseOrFall()
        if trend:
            return trend.toString()
        return trend

    def getFallBelowTime(self):
        '''
        Gets the fall below flood stage time.
        '''
        return self.jobj.getFallBelowTime()

    def getObservedCrestTime(self):
        '''
        Gets the observed crest time.
        '''
        return self.jobj.getObservedCrestTime()

    def getForecastCrestTime(self):
        '''
        Gets the forecast crest time.
        '''
        return self.jobj.getForecastCrestTime()

    def getMaximumObservedForecastValue(self):
        '''
        Gets the maximum observed forecast value.
        '''
        return self.jobj.getMaximumObservedForecastValue()

    def getMaximumObservedForecastTime(self):
        '''
        Gets the maximum observed forecast time.
        '''
        return self.jobj.getMaximumObservedForecastTime()

    def getObsCutoffTime(self):
        '''
        Gets the observed data cutoff time.
        '''
        return self.jobj.getObsCutoffTime()

    def getObsLoadTime(self):
        '''
        Gets the observed data load time.
        '''
        return self.jobj.getObsLoadTime()

    def getObservedRiseOrFall(self):
        '''
        Gets the the observed hydrograph trend.
        '''
        trend = self.jobj.getObservedRiseOrFall()
        if trend:
            return trend.toString()
        return trend

    def getForecastRiseOrFall(self):
        '''
        Gets the forecast hydrograph trend.
        '''
        trend = self.jobj.getForecastRiseOrFall()
        if trend:
            return trend.toString()
        return trend

    def getObservedFloodStageDeparture(self):
        '''
        Gets the observed flood stage departure.
        '''
        return self.jobj.getObservedFloodStageDeparture()

    def getObservedMax24Index(self):
        '''
        Gets the index of the maximum observed value in the passed 24 hours.
        '''
        return self.jobj.getObservedMax24Index()

    def getMaximum24HourObservedStage(self):
        '''
        Gets the maximum observed stage for the last 24 hours and Shef Quality Code pair.
        '''
        return self.jobj.getMaximum24HourObservedStage()

    def getObservedMax06Index(self):
        '''
        Gets the index of the maximum 6 hour observation.
        '''
        return self.jobj.getObservedMax06Index()

    def getMaximum6HourObservedStage(self):
        '''
        Gets the maximum observed stage for the last 6 hours and Shef Quality Code pair.
        '''
        return self.jobj.getMaximum6HourObservedStage()

    def getObservedCrestValue(self):
        '''
        Gets the observed crest value.
        '''
        return self.jobj.getObservedCrestValue()

    def getForecastFloodStageDeparture(self):
        '''
        Gets the forecast flood stage departure
        '''
        return self.jobj.getForecastFloodStageDeparture()

    def getForecastXfCrestIndex(self):
        '''
        Gets the forecast maximum crest index (if there is more than one
        forecast timeseries).
        '''
        return self.jobj.getForecastXfCrestIndex()

    def getForecastCrestValue(self):
        '''
        Gets the forecast crest value.
        '''
        return self.jobj.getForecastCrestValue()

    def getFallBelowTypeSource(self):
        '''
        Gets the flood Fall Below Type Source.
        '''
        return self.jobj.getFallBelowTypeSource()

    def getRiseAboveTypeSource(self):
        '''
        Gets the flood Rise Above Type Source.
        '''
        return self.jobj.getRiseAboveTypeSource()

    def getCrestTypeSource(self):
        '''
        Gets the flood Crest Type Source.
        '''
        return self.jobj.getCrestTypeSource()

    def getTrend(self):
        '''
        Gets the hydrograph trend.
        '''
        trend = self.jobj.getTrend()
        if trend:
            return trend.toString()
        return trend

    def getObservedCurrentIndex(self):
        '''
        Gets the Index of the Current SHEF Observed.
        '''
        return self.jobj.getObservedCurrentIndex()

    def getObservedCurrentStage(self):
        '''
        Gets the current observed river stage and Shef Quality Code pair.
        '''
        return self.jobj.getObservedCurrentStage()

    def getObservedCurrentTime(self):
        '''
        Gets the time of the current observed Shef value.
        '''
        return self.jobj.getObservedCurrentTime()

    def getObservedCurrentValue(self):
        '''
        Gets the current observed Shef value.
        '''
        return self.jobj.getObservedCurrentValue()

    def getMaximumForecastIndex(self):
        '''
        Gets the Index of the maximum SHEF Forecast.
        '''
        return self.jobj.getMaximumForecastIndex()

    def getMaximumForecastStage(self):
        '''
        Gets the maximum forecast stage for the last 24 hours and Shef Quality Code pair
        '''
        return self.jobj.getMaximumForecastStage()

    def getMaximumForecastTime(self):
        '''
        Gets the time of the maximum forecast Shef value.
        '''
        return self.jobj.getMaximumForecastTime()

    def getMaximumForecastValue(self):
        '''
        Gets the maximum forecast Shef value.
        '''
        return self.jobj.getMaximumForecastValue()

    def getObservedRiseAboveTime(self):
        '''
        Gets the observed rise above time.
        '''
        return self.jobj.getObservedRiseAboveTime()

    def getObservedFallBelowTime(self):
        '''
        Gets the observed fall below time.
        '''
        return self.jobj.getObservedFallBelowTime()

    def getForecastRiseAboveTime(self):
        '''
        Gets the forecast rise above time.
        '''
        return self.jobj.getForecastRiseAboveTime()

    def getForecastFallBelowTime(self):
        '''
        Gets the forecast fall below time.
        '''
        return self.jobj.getForecastFallBelowTime()

    def getHydrographForecast(self):
        '''
        Gets the Forecast Hydrograph data, which is a HydrographForecast Java object.
        '''
        return JUtil.javaObjToPyVal(self.jobj.getHydrographForecast())

    def getHydrographObserved(self):
        '''
        Gets the Observed Hydrograph data, which is a HydrographObserved Java object.
        '''
        return JUtil.javaObjToPyVal(self.jobj.getHydrographObserved())

    def getFullTimeSeriesLoadedTime(self):
        '''
        Gets the load time for the next possible pass into this function.
        '''
        return self.jobj.getFullTimeSeriesLoadedTime()

    def getObservedMaximumIndex(self):
        '''
        Gets the maximum observation data index.
        '''
        return self.jobj.getObservedMaximumIndex()

    def getStageCrestHistoryList(self):
        '''
        Gets the Stage Crest history List. This returns CrestHistory
        objects.
        '''
        return self.jobj.getStageCrestHistoryList()

    def getStageCrestHistory(self):
        '''
        Get the Stage Crest history List. This returns a List of Pair
        objects of the Q column and Date of the record.
        '''
        return self.jobj.getStageCrestHistory()

    def getFlowCrestHistoryList(self):
        '''
        Get the Flow Crest history List. This returns CrestHistory 
        objects.
        '''
        return self.jobj.getFlowCrestHistoryList()

    def getFlowCrestHistory(self):
        '''
        Get the Flow Crest history List. This returns a List of Pair
        objects of the Q column and Date of the record.
        '''
        return self.jobj.getFlowCrestHistory()


    def getForecastFallBelowActionStageTime(self):
        '''
        Gets the forecast fall below action stage time..
        '''
        return self.jobj.getForecastFallBelowActionStageTime()
