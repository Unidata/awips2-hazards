package gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.SessionHazardEvent;
import com.raytheon.uf.common.hazards.hydro.HazardSettings;
import com.raytheon.uf.common.hazards.hydro.HydrographForecast;
import com.raytheon.uf.common.hazards.hydro.HydrographObserved;
import com.raytheon.uf.common.hazards.hydro.RecommenderData;
import com.raytheon.uf.common.hazards.hydro.RecommenderManager;
import com.raytheon.uf.common.hazards.hydro.RiverForecastGroup;
import com.raytheon.uf.common.hazards.hydro.RiverForecastManager;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroFloodCategories;
import com.raytheon.uf.common.hazards.hydro.RiverStationInfo;
import com.raytheon.uf.common.hazards.hydro.SHEFForecast;
import com.raytheon.uf.common.hazards.hydro.SHEFObserved;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * This emulates the RiverPro flood product recommender. This is designed to run
 * in the Tool Framework. Unlike RiverPro, this recommender does not recommend
 * products. Instead, it recommends hazard events. The goal is for this
 * recommender to know as little as possible about products.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2012               Bryon.Lawrence    Initial creation
 * Jun 20, 2013   1277     Chris.Golden      Fixed bug that could cause
 *                                           exception due to Float being found
 *                                           where Integer was expected.
 * Apr 1, 2014  3581       bkowal       Updated to use common hazards hydro
 * Dec 12, 2014 4124       Kevin.Manross     Change logic to return list of 
 *                                           potential river points without
 *                                           setting Phen/Sig which is now
 *                                           handled in python (RiverForecastPoints.py)
 * Feb 18, 2014 3961       Kevin.Manross     Modify for single point workflow
 * Feb 24, 2015 2331       Kevin.Manross     Add code to do nothing if insufficient
 *                                           info available.
 * Apr 09, 2015 7271       Kevin.Manross     Ensured generated FL.Y hazard events
 *                                           have riseAbove/crest/fallBelow values.
 * May 08, 2015 6562       Chris.Cody        Restructure River Forecast Points/Recommender
 * Aug 18, 2015 9650       Robert.Blum       Removed null check since we want the latest
 *                                           recommenderData.
 * Aug 20, 2015 9387       Robert.Blum       Fixed UFN where the endTime would get
 *                                           incorrectly set after it was correctly set.
 * Jan 14, 2016 12935      Robert.Blum       Setting Rise/Crest/Fall values to Missing
 *                                           instead of 0 or UFN.
 * Jan 15, 2016 9387       Robert.Blum       Fixed bug where negative intervals where
 *                                           being saved as the events duration befure
 *                                           ufn was selected.
 * May 10, 2016 18240      Kevin.Bisanz      Always create hazard if point selected when
 *                                           RFR is invoked.
 * May 27, 2016 19137      Roger.Ferrel      In {@link #buildRiseCrestFallAttributes(RiverForecastPoint, Map, IHazardEvent)}
 *                                           when missing Fall Below Time use Forecast Fall Below Action Stage Time.
 * Jun 23, 2016 19537      Chris.Golden      Removed spatial info parameter from method.
 * Jul 25, 2016 19537      Chris.Golden      Moved constant definition to HazardConstants.
 * Aug 09, 2016 20382      Ben.Phillippe     Impacts CurObs and MaxFcst values updated
 * Aug 16, 2016 15017      Robert.Blum       Added observed crest, forecast crest, and max forecast attributes
 *                                           to the hazard event.
 * Aug 26, 2016 21435      Sara.Stewart      Added Crests MaxFcst and CurObs attributes
 * Oct 13, 2016 22519      mduff             Use the HSA passed into the recommender, not the local site's HSA.
 *                                           The system could be in service backup mode.
 * Feb 28, 2017 22588      Kevin.Bisanz      Change value of INCLUDE_NONFLOOD_POINTS to match python.
 * Mar 15, 2017 30224      Robert.Blum       Added hazard attribute for group name.
 * May 05, 2017 33737      bkowal            Set the crest time to missing when not available.
 * May 08, 2018 15561      Chris.Golden      Changed BaseHazardEvent to SessionHazardEvent.
 * Jun 06, 2018 15561      Chris.Golden      Added practice flag for hazard event construction.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public class RiverProFloodRecommender {

    RecommenderData recommenderData = null;

    /**
     * Flag indicating whether or not to include non-flood points in the
     * recommendation.
     */
    private static final String INCLUDE_NONFLOOD_POINTS = "includePointsBelowAdvisory";

    /**
     * Represents the first character of a river flow PE.
     */
    private static final char RIVER_FLOW_CHARACTER = 'Q';

    private final boolean practice;

    private RiverForecastManager riverForecastManager = null;

    /**
     * Default constructor
     * 
     * @param practice
     *            Practice mode flag.
     */
    public RiverProFloodRecommender(boolean practice) {
        this.practice = practice;
        this.riverForecastManager = new RiverForecastManager();
    }

    /**
     * Runs the Tool.
     * 
     * @param sessionAttributeMap
     *            A map of session information
     * @param dialogInputMap
     *            A map of user-input dialog information
     * @return A list of hazard events representing river flood recommendations
     */
    public EventSet<IHazardEvent> getRecommendation(
            Map<String, Object> sessionAttributeMap,
            Map<String, Object> dialogInputMap) {

        return createHazards(dialogInputMap, sessionAttributeMap);

    }

    private RecommenderData getRecommenderData(String siteId) {
        RecommenderManager recommenderManager = RecommenderManager
                .getInstance();
        long currentSystemTime = SimulatedTime.getSystemTime().getMillis();
        this.recommenderData = recommenderManager
                .getRiverFloodRecommenderData(currentSystemTime, siteId);
        return (this.recommenderData);
    }

    /**
     * Builds a set of the recommended river flood hazards.
     * 
     * @param dialogInputMap
     *            A map containing information from the tool dialog.
     * @param sessionAttributeMap
     * @return A set of recommended potential river flood hazards
     */
    private EventSet<IHazardEvent> createHazards(
            Map<String, Object> dialogInputMap,
            Map<String, Object> sessionAttributeMap) {

        boolean includeNonFloodPoints = Boolean.TRUE
                .equals(dialogInputMap.get(INCLUDE_NONFLOOD_POINTS));
        String siteId = (String) sessionAttributeMap
                .get(HazardConstants.LOCALIZED_SITE_ID);
        EventSet<IHazardEvent> potentialHazardEventSet = getPotentialRiverHazards(
                includeNonFloodPoints, dialogInputMap, siteId);

        return potentialHazardEventSet;
    }

    /**
     * Builds a set of hazard recommendations based on the output from the river
     * flood recommender.
     * 
     * @param includeNonFloodPoints
     *            true - include nonFloodPoints in a recommendation if at least
     *            one point in the group is flooding. false - include only flood
     *            points.
     * @param riverForecastPoint
     *            - particular RiverForecastPoint on which attributes will be
     *            set.
     */
    private IHazardEvent setRiverHazard(boolean includeNonFloodPoints,
            RiverForecastPoint riverForecastPoint) {

        IHazardEvent riverHazard = new SessionHazardEvent(practice);
        Map<String, Serializable> hazardAttributes = new HashMap<>();

        if (riverForecastPoint.isIncludedInRecommendation()
                || includeNonFloodPoints) {

            riverHazard.setEventID("");
            riverHazard.setStatus(HazardStatus.POTENTIAL);

            hazardAttributes.put(HazardConstants.POINTID,
                    riverForecastPoint.getLid());
            hazardAttributes.put(HazardConstants.STREAM_NAME,
                    riverForecastPoint.getStream());

            hazardAttributes.put(HazardConstants.FLOOD_STAGE,
                    riverForecastPoint.getFloodStage());
            hazardAttributes.put(HazardConstants.ACTION_STAGE,
                    riverForecastPoint.getActionStage());

            double currentStage = riverForecastPoint.getCurrentObservation()
                    .getValue();

            double impactsMaxFcst = riverForecastPoint
                    .getMaximumForecastValue();
            hazardAttributes.put(HazardConstants.IMPACTS_CUR_OBS, currentStage);
            hazardAttributes.put(HazardConstants.IMPACTS_MAX_FCST,
                    impactsMaxFcst);
            hazardAttributes.put(HazardConstants.CURRENT_STAGE, currentStage);

            long currentStageTime = riverForecastPoint.getCurrentObservation()
                    .getObsTime();
            hazardAttributes.put(HazardConstants.CURRENT_STAGE_TIME,
                    currentStageTime);

            if (riverForecastPoint.isIncludedInRecommendation()) {
                buildFloodAttributes(riverForecastPoint, hazardAttributes);

            } else {
                buildNonFloodAttributes(riverForecastPoint, hazardAttributes,
                        riverHazard);
            }

            buildRiseCrestFallAttributes(riverForecastPoint, hazardAttributes,
                    riverHazard);

            riverHazard.setCreationTime(TimeUtil.newCalendar().getTime());

            List<Double> pointCoords = Lists.newArrayList();
            Coordinate pointLocation = riverForecastPoint.getLocation();
            pointCoords.add(pointLocation.x);
            pointCoords.add(pointLocation.y);

            Map<String, Serializable> forecastPointAttributes = Maps
                    .newHashMap();
            forecastPointAttributes.put(HazardConstants.POINT_TYPE,
                    (Serializable) pointCoords);
            forecastPointAttributes.put(HazardConstants.RIVER_POINT_ID,
                    riverForecastPoint.getLid());
            forecastPointAttributes.put(HazardConstants.RIVER_POINT_NAME,
                    riverForecastPoint.getName());
            hazardAttributes.put(HazardConstants.FORECAST_POINT,
                    (Serializable) forecastPointAttributes);

            riverHazard.setHazardAttributes(hazardAttributes);
        }

        return riverHazard;
    }

    /**
     * Builds a set of hazard recommendations based on the output from the river
     * flood recommender.
     * 
     * @param includeNonFloodPoints
     *            true - include nonFloodPoints in a recommendation if at least
     *            one point in the group is flooding. false - include only flood
     *            points.
     * @param dialogInputMap
     *            - used for single point selection workflow.
     * @param siteId
     *            - The siteID/HSA to recommend for
     * @return A set of recommended hazards.
     */
    private EventSet<IHazardEvent> getPotentialRiverHazards(
            boolean includeNonFloodPoints, Map<String, Object> dialogInputMap,
            String siteId) {
        EventSet<IHazardEvent> potentialRiverEventSet = new EventSet<>();

        String pointID = (String) dialogInputMap
                .get(HazardConstants.SELECTED_POINT_ID);

        RecommenderData recommenderData = getRecommenderData(siteId);

        if (pointID != null) {
            RiverForecastPoint riverForecastPoint = getRiverForecastPoint(
                    pointID, recommenderData);
            if (riverForecastPoint != null) {
                String groupId = riverForecastPoint.getGroupId();
                RiverForecastGroup riverForecastGroup = getRiverForecastGroup(
                        groupId, recommenderData);
                // If a specific point is provided, create a hazard for it.
                if (riverForecastGroup.isIncludedInRecommendation() == true
                        || riverForecastGroup.isPointIdInGroup(pointID)) {
                    IHazardEvent riverHazard = setRiverHazard(
                            includeNonFloodPoints, riverForecastPoint);
                    riverHazard.setStatus(HazardStatus.PENDING);
                    potentialRiverEventSet.add(riverHazard);
                }
            }
            return (potentialRiverEventSet);
        }

        List<RiverForecastGroup> riverGroupList = null;
        riverGroupList = recommenderData.getRiverForecastGroupList();
        for (RiverForecastGroup riverForecastGroup : riverGroupList) {
            if (riverForecastGroup.isIncludedInRecommendation()) {
                List<RiverForecastPoint> riverForecastPointList = riverForecastGroup
                        .getForecastPointList();
                if (riverForecastPointList != null) {
                    for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
                        IHazardEvent riverHazard = setRiverHazard(
                                includeNonFloodPoints, riverForecastPoint);
                        potentialRiverEventSet.add(riverHazard);
                    }
                }
            }
        }

        return potentialRiverEventSet;
    }

    /**
     * private void buildFloodAttributes(RiverForecastPoint riverForecastPoint,
     * Creates the attributes defining a river flood watch or a river flood
     * warning.
     * 
     * @param riverForecastPoint
     *            The river forecast point to build the watch or warning
     *            attributes for
     * @param hazardAttributes
     *            The attributes of the hazard
     * @return
     */
    private void buildFloodAttributes(RiverForecastPoint riverForecastPoint,
            Map<String, Serializable> hazardAttributes) {

        hazardAttributes.put(HazardConstants.POINTID,
                riverForecastPoint.getLid());
        hazardAttributes.put(HazardConstants.STREAM_NAME,
                riverForecastPoint.getStream());

        String groupId = riverForecastPoint.getGroupId();
        RiverForecastGroup riverForecastGroup = getRiverForecastGroup(groupId,
                recommenderData);
        hazardAttributes.put(HazardConstants.GROUP_NAME,
                riverForecastGroup.getGroupName());

        // Default to excessive rainfall.
        hazardAttributes.put(HazardConstants.IMMEDIATE_CAUSE,
                FloodRecommenderConstants.ImmediateCause.EXCESSIVE_RAINFALL
                        .getValue());

        // Define flood record
        int floodCategory = Math.min(
                riverForecastPoint.getMaximumObservedForecastCategory(),
                HydroFloodCategories.MAJOR_FLOOD_CATEGORY.getRank());

        String recordStatus = retrieveFloodRecord(
                this.riverForecastManager.getHazardSettings(),
                riverForecastPoint);
        hazardAttributes.put(HazardConstants.FLOOD_RECORD, recordStatus);

        /*
         * Need to translate the flood category to a string value.
         */
        if (floodCategory == HydroFloodCategories.NULL_CATEGORY.getRank()) {
            hazardAttributes.put(HazardConstants.FLOOD_SEVERITY_CATEGORY,
                    FloodRecommenderConstants.FloodSeverity.NONE.getValue());
        } else {
            hazardAttributes.put(HazardConstants.FLOOD_SEVERITY_CATEGORY,
                    Integer.toString(floodCategory));
        }
    }

    private void buildRiseCrestFallAttributes(
            RiverForecastPoint riverForecastPoint,
            Map<String, Serializable> hazardAttributes,
            IHazardEvent riverHazardEvent) {

        /*
         * Retrieve the reach information for this forecast point
         */

        long riseAbove = riverForecastPoint.getRiseAboveTime();

        if ((riseAbove != RiverHydroConstants.MISSING_VALUE)
                && (riseAbove > 0)) {
            hazardAttributes.put(HazardConstants.RISE_ABOVE, riseAbove);
            riverHazardEvent.setStartTime(new Date(riseAbove));
        } else {
            hazardAttributes.put(HazardConstants.RISE_ABOVE,
                    RiverHydroConstants.MISSING_VALUE);
            riverHazardEvent.setStartTime(RiverForecastManager.getSystemTime());
        }

        Date maxObservedForecastCrestDate = riverForecastPoint
                .getMaximumObservedForecastTime();

        double forecastCrest = riverForecastPoint.getForecastCrestValue();
        long forecastCrestTime = riverForecastPoint.getForecastCrestTime();
        hazardAttributes.put(HazardConstants.CREST_STAGE_FORECAST,
                forecastCrest);
        hazardAttributes.put(HazardConstants.CREST_TIME_FORECAST,
                forecastCrestTime);

        double crestsMaxFcst = riverForecastPoint.getMaximumForecastValue();
        double crestsCurrentStage = riverForecastPoint.getCurrentObservation()
                .getValue();

        hazardAttributes.put(HazardConstants.CRESTS_MAX_FCST, crestsMaxFcst);

        hazardAttributes.put(HazardConstants.CRESTS_CUR_OBS,
                crestsCurrentStage);

        double observedCrest = riverForecastPoint.getObservedCrestValue();
        long observedCrestTime = riverForecastPoint.getObservedCrestTime();
        hazardAttributes.put(HazardConstants.CREST_STAGE_OBSERVED,
                observedCrest);
        hazardAttributes.put(HazardConstants.CREST_TIME_OBSERVED,
                observedCrestTime);

        hazardAttributes.put(HazardConstants.MAX_FORECAST_STAGE,
                riverForecastPoint.getMaximumForecastValue());
        hazardAttributes.put(HazardConstants.MAX_FORECAST_TIME,
                riverForecastPoint.getMaximumForecastTime());

        if (maxObservedForecastCrestDate != null) {
            hazardAttributes.put(HazardConstants.CREST,
                    maxObservedForecastCrestDate.getTime());
            hazardAttributes.put(HazardConstants.CREST_STAGE,
                    riverForecastPoint.getMaximumObservedForecastValue());
        } else {
            hazardAttributes.put(HazardConstants.CREST,
                    RiverHydroConstants.MISSING_VALUE);
            hazardAttributes.put(HazardConstants.CREST_STAGE, 0);
        }

        long fallBelow = riverForecastPoint.getFallBelowTime();

        if ((fallBelow != RiverHydroConstants.MISSING_VALUE)
                && (fallBelow > 0)) {
            hazardAttributes.put(HazardConstants.FALL_BELOW, fallBelow);

            /*
             * Need to consider the shift ahead hours here...
             */
            long virtualEndTime = this.riverForecastManager
                    .getVirtualFallBelowTime(riverForecastPoint, true);
            riverHazardEvent.setEndTime(new Date(virtualEndTime));
        } else {
            long ffbasTime = riverForecastPoint
                    .getForecastFallBelowActionStageTime();

            hazardAttributes.put(HazardConstants.FALL_BELOW,
                    RiverHydroConstants.MISSING_VALUE);

            if (ffbasTime > 0) {
                hazardAttributes.put(
                        HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                        false);
                Calendar ffbasCal = Calendar.getInstance();
                ffbasCal.setTimeInMillis(ffbasTime);
                riverHazardEvent.setEndTime(ffbasCal.getTime());
            } else {
                hazardAttributes.put(
                        HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                        true);
                Calendar ufnCal = Calendar.getInstance();
                ufnCal.setTimeInMillis(
                        HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
                riverHazardEvent.setEndTime(ufnCal.getTime());
            }

            long latestTime = 0L;
            HydrographForecast hydrographForecast = riverForecastPoint
                    .getHydrographForecast();
            if (hydrographForecast != null) {
                List<SHEFForecast> shefForecastList = hydrographForecast
                        .getShefHydroDataList();
                if (shefForecastList != null) {
                    for (SHEFForecast fcst : shefForecastList) {
                        if (fcst.getValidTime() > latestTime) {
                            latestTime = fcst.getValidTime();
                        }
                    }
                    long interval = latestTime
                            - riverHazardEvent.getStartTime().getTime();

                    // Only save a valid interval
                    if (interval > 0) {
                        hazardAttributes.put(
                                HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE,
                                interval);
                    }
                }
            }
        }

        // Default to excessive rainfall.
        hazardAttributes.put(HazardConstants.IMMEDIATE_CAUSE,
                FloodRecommenderConstants.ImmediateCause.EXCESSIVE_RAINFALL
                        .getValue());

        // Define flood record
        int floodCategory = Math.min(
                riverForecastPoint.getMaximumObservedForecastCategory(),
                HydroFloodCategories.MAJOR_FLOOD_CATEGORY.getRank());

        String recordStatus = retrieveFloodRecord(
                this.riverForecastManager.getHazardSettings(),
                riverForecastPoint);
        hazardAttributes.put(HazardConstants.FLOOD_RECORD, recordStatus);

        /*
         * Need to translate the flood category to a string value.
         */
        if (floodCategory == HydroFloodCategories.NULL_CATEGORY.getRank()) {
            hazardAttributes.put(HazardConstants.FLOOD_SEVERITY_CATEGORY,
                    FloodRecommenderConstants.FloodSeverity.NONE.getValue());
        } else {
            hazardAttributes.put(HazardConstants.FLOOD_SEVERITY_CATEGORY,
                    Integer.toString(floodCategory));
        }
    }

    /**
     * Creates the attributes defining a river statement.
     * 
     * @param riverForecastPoint
     *            The river forecast point to build the statement attributes for
     * @param hazardAttributes
     *            The attributes of the hazard
     * @param pointHazardEvent
     *            The hazard event representing the river point
     * @return
     */
    private void buildNonFloodAttributes(RiverForecastPoint riverForecastPoint,
            Map<String, Serializable> hazardAttributes,
            IHazardEvent pointHazardEvent) {
        /*
         * Retrieve the reach information for this forecast point
         */

        hazardAttributes.put(HazardConstants.POINTID,
                riverForecastPoint.getLid());
        hazardAttributes.put(HazardConstants.STREAM_NAME,
                riverForecastPoint.getStream());

        String groupId = riverForecastPoint.getGroupId();
        RiverForecastGroup riverForecastGroup = getRiverForecastGroup(groupId,
                recommenderData);
        hazardAttributes.put(HazardConstants.GROUP_NAME,
                riverForecastGroup.getGroupName());

        hazardAttributes.put(HazardConstants.FLOOD_STAGE,
                riverForecastPoint.getFloodStage());
        hazardAttributes.put(HazardConstants.ACTION_STAGE,
                riverForecastPoint.getActionStage());

        hazardAttributes.put(HazardConstants.RISE_ABOVE,
                RiverHydroConstants.MISSING_VALUE);

        hazardAttributes.put(HazardConstants.CREST,
                RiverHydroConstants.MISSING_VALUE);

        hazardAttributes.put(HazardConstants.FALL_BELOW,
                RiverHydroConstants.MISSING_VALUE);

        pointHazardEvent.setStartTime(RiverForecastManager.getSystemTime());

        Calendar cal = TimeUtil.newCalendar(pointHazardEvent.getStartTime());
        cal.add(Calendar.HOUR, this.riverForecastManager.getHazardSettings()
                .getFlwExpirationHours());
        pointHazardEvent.setEndTime(cal.getTime());

        /*
         * Default to unknown cause.
         */
        hazardAttributes.put(HazardConstants.IMMEDIATE_CAUSE,
                FloodRecommenderConstants.ImmediateCause.UNKNOWN.getValue());

        hazardAttributes.put(HazardConstants.FLOOD_RECORD,
                FloodRecommenderConstants.FloodRecordStatus.AREAL_POINT_FLASH_FLOOD
                        .getValue());

        hazardAttributes.put(HazardConstants.FLOOD_SEVERITY_CATEGORY,
                FloodRecommenderConstants.FloodSeverity.NONE.getValue());
    }

    /**
     * Returns the flood record based on the maximum observed/forecast value and
     * the record value. Possible return values include:
     * 
     * 
     * @param settings
     *            Settings which control the operation of the flood recommender.
     * @param forecastPoint
     *            The river forecast point
     * @return "NO": A record flood is not expected. "NR": Near record or record
     *         flood expected "UU": Flood without a period of record to compare
     *         "OO": For areal flood warnings, areal flash flood products, and
     *         and flood advisories (point and areal)
     */
    public String retrieveFloodRecord(HazardSettings settings,
            RiverForecastPoint forecastPoint) {

        String pe = forecastPoint.getPhysicalElement();
        double crestValue = forecastPoint.getMaximumObservedForecastValue();
        double recordValue = forecastPoint
                .getFloodCategory()[HydroFloodCategories.RECORD_FLOOD_CATEGORY
                        .getRank()];
        double threshold;
        String recordString = null;

        if (crestValue != RiverHydroConstants.MISSING_VALUE_DOUBLE
                && recordValue != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
            if (pe.charAt(0) == RIVER_FLOW_CHARACTER) {
                threshold = settings.getVtecRecordFlowOffset();
            } else {
                threshold = settings.getVtecRecordStageOffset();
            }

            if (crestValue >= (recordValue - threshold)) {
                recordString = FloodRecommenderConstants.FloodRecordStatus.NEAR_RECORD_FLOOD_OR_RECORD_FLOOD_EXPECTED
                        .getValue();
            } else {
                recordString = FloodRecommenderConstants.FloodRecordStatus.RECORD_FLOOD_NOT_EXPECTED
                        .getValue();
            }
        } else {
            recordString = FloodRecommenderConstants.FloodRecordStatus.NO_PERIOD_OF_RECORD
                    .getValue();
        }

        return recordString;
    }

    /**
     * Retrieve a deep queried River Forecast Group.
     * 
     * This data should be returned from the Cached Recommender Data object.
     * 
     * @param groupID
     * @param recommenderData
     * @return River Forecast Group (deep query)
     */
    public RiverForecastGroup getRiverForecastGroup(String groupID) {
        return getRiverForecastGroup(groupID, recommenderData);
    }

    /**
     * Retrieve a deep queried River Forecast Group.
     * 
     * This data should be returned from the Cached Recommender Data object.
     * 
     * @param groupID
     * @param recommenderData
     * @return River Forecast Group (deep query)
     */
    private RiverForecastGroup getRiverForecastGroup(String groupID,
            RecommenderData recommenderData) {

        RiverForecastGroup riverForecastGroup = null;
        if (groupID != null) {
            if (recommenderData != null) {
                Map<String, RiverForecastGroup> riverForecastGroupMap = recommenderData
                        .getRiverForecastGroupMap();
                if (riverForecastGroupMap != null) {
                    riverForecastGroup = riverForecastGroupMap.get(groupID);
                }
            }
            if (riverForecastGroup == null) {
                // Unable to find Group Id in cached Recommender Data. Querying
                // separately.
                riverForecastGroup = riverForecastManager
                        .getRiverForecastGroup(groupID, true);
            }
        }
        return (riverForecastGroup);
    }

    /**
     * Retrieve a deep queried River Forecast Point.
     * 
     * This data should be returned from the Cached Recommender Data object.
     * 
     * @param pointID
     * @return River Forecast Point (deep query)
     */
    public RiverForecastPoint getRiverForecastPoint(String pointID) {
        return getRiverForecastPoint(pointID, recommenderData);
    }

    /**
     * Retrieve a deep queried River Forecast Point.
     * 
     * This data should be returned from the Cached Recommender Data object.
     * 
     * @param pointID
     * @param recommenderData
     * @return River Forecast Point (deep query)
     */
    private RiverForecastPoint getRiverForecastPoint(String pointID,
            RecommenderData recommenderData) {

        RiverForecastPoint riverForecastPoint = null;
        if (pointID != null) {
            if (recommenderData != null) {
                Map<String, RiverForecastPoint> riverForecastPointMap = recommenderData
                        .getRiverForecastPointMap();
                if (riverForecastPointMap != null) {
                    riverForecastPoint = riverForecastPointMap.get(pointID);
                }
            }
            if (riverForecastPoint == null) {
                // Unable to find Point Id in cached Recommender Data. Querying
                // separately.
                riverForecastPoint = riverForecastManager
                        .getRiverForecastPoint(pointID, true);
            }
        }
        return (riverForecastPoint);
    }

    /**
     * Retrieve a Map of LID (Gauge Id) to a string containing Lat/Lon coords
     * for area Flood Inundation.
     * 
     * @return A Map with the gauge 'lid' as the key and a string of latitude
     *         and longitude values as the value.
     */
    public Map<String, String> getAreaInundationCoordinates() {

        return (this.riverForecastManager.getAreaInundationCoordinates());
    }

    /**
     * Retrieve RiverStationInfo for a LID (Gauge Id)
     * 
     * @return River Station Info Object
     */
    public RiverStationInfo getRiverStationInfo(String pointID) {

        return (this.riverForecastManager
                .getRiverForecastPointRiverStationInfo(pointID));
    }

    /**
     * Get an indexed SHEF Observed object for a River Forecast Point.
     * 
     * This is for use by Python scripts. Python does not handle lists of
     * complex objects elegantly. This method retrieves a River Forecast Point
     * from the Recommender Data Cache. Then it locates a SHEF Observed object
     * from the River Forecast Point object's HydrographObserved list of SHEF
     * Observed data objects.
     * 
     * @param pointID
     *            River Forecast Point LID
     * @index Index of the SHEF Observed object in the HydrographObserved lise
     * @return Selected SHEF Observed object
     */
    public SHEFObserved getSHEFObserved(String pointID, int index) {
        Map<String, RiverForecastPoint> riverForecastPointMap = recommenderData
                .getRiverForecastPointMap();
        RiverForecastPoint riverForecastPoint = riverForecastPointMap
                .get(pointID);
        if (riverForecastPoint != null) {
            HydrographObserved hydrographObserved = riverForecastPoint
                    .getHydrographObserved();
            if (hydrographObserved != null) {
                List<SHEFObserved> shefObservedList = hydrographObserved
                        .getShefHydroDataList();
                if ((shefObservedList != null)
                        && (shefObservedList.size() > index)) {
                    SHEFObserved shefObserved = shefObservedList.get(index);
                    return (shefObserved);
                }
            }
        }
        return (null);
    }

    /**
     * Get an indexed SHEF Forecast object for a River Forecast Point.
     * 
     * This is for use by Python scripts. Python does not handle lists of
     * complex objects elegantly. This method retrieves a River Forecast Point
     * from the Recommender Data Cache. Then it locates a SHEF Forecast object
     * from the River Forecast Point object's HydrographForecast list of SHEF
     * Forecast data objects.
     * 
     * @param pointID
     *            River Forecast Point LID
     * @index Index of the SHEF Forecast object in the HydrographForecast lise
     * @return Selected SHEF Forecast object
     */
    public SHEFForecast getSHEFForecast(String pointID, int index) {

        Map<String, RiverForecastPoint> riverForecastPointMap = recommenderData
                .getRiverForecastPointMap();
        RiverForecastPoint riverForecastPoint = riverForecastPointMap
                .get(pointID);
        if (riverForecastPoint != null) {
            HydrographForecast hydrographForecast = riverForecastPoint
                    .getHydrographForecast();
            if (hydrographForecast != null) {
                List<SHEFForecast> shefForecastList = hydrographForecast
                        .getShefHydroDataList();
                if ((shefForecastList != null)
                        && (shefForecastList.size() > index)) {
                    SHEFForecast shefForecast = shefForecastList.get(index);
                    return (shefForecast);
                }
            }
        }
        return (null);
    }
}