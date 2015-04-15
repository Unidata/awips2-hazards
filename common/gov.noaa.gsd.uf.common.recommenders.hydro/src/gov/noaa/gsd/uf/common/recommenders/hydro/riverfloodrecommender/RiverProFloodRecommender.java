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
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.hydro.HazardSettings;
import com.raytheon.uf.common.hazards.hydro.RiverForecastGroup;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint;
import com.raytheon.uf.common.hazards.hydro.RiverProDataManager;
import com.raytheon.uf.common.hazards.hydro.SHEFObservation;
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
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public class RiverProFloodRecommender {
    /**
     * Threshold confidence level (percent) which delineates an watch from a
     * warning.
     */
    private static final int WARNING_THRESHOLD = 80;

    /**
     * Probabilistic threshold for watch/warning basis.
     */
    private static final String FORCAST_CONFIDENCE_PERCENTAGE = "forecastConfidencePercentage";

    /**
     * Flag indicating whether or not to include non-flood points in the
     * recommendation.
     */
    private static final String INCLUDE_NONFLOOD_POINTS = "includeNonFloodPoints";

    /**
     * Key value for in dialogInputMap for single point workflow
     * 
     */
    private static final String SELECTED_POINT_ID = "selectedPointID";

    /**
     * Represents the first character of a river flow PE.
     */
    private static final char RIVER_FLOW_CHARACTER = 'Q';

    private final RiverProDataManager riverProDataManager;

    /**
     * Default constructor
     */
    public RiverProFloodRecommender(RiverProDataManager riverProDataManager) {
        this.riverProDataManager = riverProDataManager;
    }

    /**
     * Runs the Tool.
     * 
     * @param sessionAttributeMap
     *            A map of session information
     * @param dialogInputMap
     *            A map of user-input dialog information
     * @param spatialInputMap
     *            A map of user-input spatial information
     * @return A list of hazard events representing river flood recommendations
     */
    public EventSet<IHazardEvent> getRecommendation(
            Map<String, Object> sessionAttributeMap,
            Map<String, Object> dialogInputMap,
            Map<String, Object> spatialInputMap) {

        return createHazards(dialogInputMap);

    }

    /**
     * Builds a set of the recommended river flood hazards.
     * 
     * @param dialogInputMap
     *            A map containing information from the tool dialog.
     * @return A set of recommended potential river flood hazards
     */
    private EventSet<IHazardEvent> createHazards(
            Map<String, Object> dialogInputMap) {

        boolean includeNonFloodPoints = Boolean.TRUE.equals(dialogInputMap
                .get(INCLUDE_NONFLOOD_POINTS));

        EventSet<IHazardEvent> potentialHazardEventSet = getPotentialRiverHazards(
                includeNonFloodPoints, dialogInputMap);

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

        IHazardEvent riverHazard = new BaseHazardEvent();
        Map<String, Serializable> hazardAttributes = new HashMap<String, Serializable>();

        if (riverForecastPoint.isIncludedInRecommendation()
                || includeNonFloodPoints) {

            riverHazard.setEventID("");
            riverHazard.setStatus(HazardStatus.POTENTIAL);

            hazardAttributes.put(HazardConstants.POINTID,
                    riverForecastPoint.getId());
            hazardAttributes.put(HazardConstants.STREAM_NAME,
                    riverForecastPoint.getStream());

            hazardAttributes.put(HazardConstants.FLOOD_STAGE,
                    riverForecastPoint.getFloodStage());
            hazardAttributes.put(HazardConstants.ACTION_STAGE,
                    riverForecastPoint.getActionStage());

            double currentStage = riverForecastPoint.getCurrentObservation()
                    .getValue();

            hazardAttributes.put(HazardConstants.CURRENT_STAGE, currentStage);

            long currentStageTime = riverForecastPoint.getCurrentObservation()
                    .getValidTime();
            hazardAttributes.put(HazardConstants.CURRENT_STAGE_TIME,
                    currentStageTime);

            buildRiseCrestFallAttributes(riverForecastPoint, hazardAttributes,
                    riverHazard);

            if (riverForecastPoint.isIncludedInRecommendation()) {
                buildFloodAttributes(riverForecastPoint, hazardAttributes);

            } else {
                buildNonFloodAttributes(riverForecastPoint, hazardAttributes);
            }

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
                    riverForecastPoint.getId());
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
     * @return A set of recommended hazards.
     */
    public EventSet<IHazardEvent> getPotentialRiverHazards(
            boolean includeNonFloodPoints, Map<String, Object> dialogInputMap) {
        EventSet<IHazardEvent> potentialRiverEventSet = new EventSet<IHazardEvent>();

        String pointID = (String) dialogInputMap.get(SELECTED_POINT_ID);

        for (RiverForecastGroup riverGroup : this.riverProDataManager
                .getRiverGroupList()) {
            if (riverGroup.isIncludedInRecommendation()) {
                for (RiverForecastPoint riverForecastPoint : riverGroup
                        .getForecastPointList()) {

                    if (pointID != null) {
                        if (riverForecastPoint.getId().equals(pointID)) {
                            IHazardEvent riverHazard = setRiverHazard(
                                    includeNonFloodPoints, riverForecastPoint);
                            riverHazard.setStatus(HazardStatus.PENDING);
                            potentialRiverEventSet.add(riverHazard);
                            return potentialRiverEventSet;
                        }
                    } else {
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
     * @param riverHazardEvent
     *            The hazard event representing the river point
     * @return
     */
    private void buildFloodAttributes(RiverForecastPoint riverForecastPoint,
            Map<String, Serializable> hazardAttributes) {

        // Default to excessive rainfall.
        hazardAttributes.put(HazardConstants.IMMEDIATE_CAUSE,
                FloodRecommenderConstants.ImmediateCause.EXCESSIVE_RAINFALL
                        .getValue());

        // Define flood record
        int floodCategory = Math.min(riverForecastPoint
                .getMaximumObservedForecastCategory(),
                RiverForecastPoint.HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                        .getRank());

        String recordStatus = retrieveFloodRecord(
                this.riverProDataManager.getHazardSettings(),
                riverForecastPoint);
        hazardAttributes.put(HazardConstants.FLOOD_RECORD, recordStatus);

        /*
         * Need to translate the flood category to a string value.
         */
        if (floodCategory == RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                .getRank()) {
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

        Date riseAbove = riverForecastPoint.getRiseAboveTime();

        if ((riseAbove != null) && (riseAbove.getTime() > 0)) {
            hazardAttributes.put(HazardConstants.RISE_ABOVE,
                    riseAbove.getTime());
            riverHazardEvent.setStartTime(riseAbove);
        } else {
            hazardAttributes.put(HazardConstants.RISE_ABOVE, 0);
            riverHazardEvent.setStartTime(this.riverProDataManager
                    .getFloodDAO().getSystemTime());
        }

        Date maxObservedForecastCrestDate = riverForecastPoint
                .getMaximumObservedForecastTime();

        if (maxObservedForecastCrestDate != null) {
            hazardAttributes.put(HazardConstants.CREST,
                    maxObservedForecastCrestDate.getTime());
            hazardAttributes.put(HazardConstants.CREST_STAGE,
                    riverForecastPoint.getMaximumObservedForecastValue());
        } else {
            hazardAttributes.put(HazardConstants.CREST, 0);
            hazardAttributes.put(HazardConstants.CREST_STAGE, 0);
        }

        Date fallBelow = riverForecastPoint.getFallBelowTime();

        if (fallBelow != null) {
            hazardAttributes.put(HazardConstants.FALL_BELOW,
                    fallBelow.getTime());

            /*
             * Need to consider the shift ahead hours here...
             */
            Date virtualEndTime = riverForecastPoint
                    .getVirtualFallBelowTime(true);
            riverHazardEvent.setEndTime(virtualEndTime);
        } else {

            hazardAttributes.put(HazardConstants.FALL_BELOW,
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
            hazardAttributes.put(
                    HazardConstants.FALL_BELOW_UNTIL_FURTHER_NOTICE, true);
            Calendar ufnCal = Calendar.getInstance();
            ufnCal.setTimeInMillis(HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
            riverHazardEvent.setEndTime(ufnCal.getTime());
            hazardAttributes.put(
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                    true);

            long latestTime = 0L;
            if (riverForecastPoint.getForecastHydrograph() != null) {
                if (riverForecastPoint.getForecastHydrograph()
                        .getShefHydroDataList() != null) {
                    for (SHEFObservation fcst : riverForecastPoint
                            .getForecastHydrograph().getShefHydroDataList()) {
                        if (fcst.getValidTime() > latestTime) {
                            latestTime = fcst.getValidTime();
                        }
                    }

                    long interval = latestTime
                            - riverHazardEvent.getStartTime().getTime();

                    hazardAttributes
                            .put(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE,
                                    interval);

                    if (riverForecastPoint.getMaximumObservedForecastTime() != null) {
                        interval = latestTime
                                - riverForecastPoint
                                        .getMaximumObservedForecastTime()
                                        .getTime();
                    }

                    hazardAttributes.put("hiddenFallBelowLastInterval",
                            interval);
                }
            }
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
            Map<String, Serializable> hazardAttributes) {
        /*
         * Default to unknown cause.
         */
        hazardAttributes.put(HazardConstants.IMMEDIATE_CAUSE,
                FloodRecommenderConstants.ImmediateCause.UNKNOWN.getValue());

        hazardAttributes
                .put(HazardConstants.FLOOD_RECORD,
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
        double recordValue = forecastPoint.getFloodCategory()[RiverForecastPoint.HydroFloodCategories.RECORD_FLOOD_CATEGORY
                .getRank()];
        double threshold;
        String recordString = null;

        if (crestValue != RiverForecastPoint.MISSINGVAL
                && recordValue != RiverForecastPoint.MISSINGVAL) {
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
}