package gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender;

import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.HydroEvent.HydroEventReason;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverForecastPoint.HydroGraphTrend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecaction;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
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
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public class RiverProFloodRecommender {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverProFloodRecommender.class);

    /**
     * Threshold confidence level (percent) which delineates an watch from a
     * warning.
     */
    private static final int WARNING_THRESHOLD = 80;

    /**
     * List of river forecast points.
     */
    private List<RiverForecastPoint> forecastPointList = null;

    /**
     * List of river forecast groups.
     */
    private List<RiverForecastGroup> riverGroupList = null;

    /**
     * List of county forecast groups.
     */
    private List<CountyForecastGroup> countyForecastGroupList = null;

    /**
     * List of hydro events.
     */
    private List<HydroEvent> hydroEventList = null;

    /**
     * Contains configurable settings for this recommender.
     */
    private HazardSettings hazardSettings = null;

    /**
     * Map of hydro events to forecast points.
     */
    private Map<String, HydroEvent> forecastPointEventMap;

    /**
     * Data access object. Isolates the recommender from the implementation
     * details of data retrieval. Helps testing.
     */
    private final IFloodRecommenderDAO floodDAO;

    /**
     * Default constructor
     */
    public RiverProFloodRecommender() {
        this.floodDAO = FloodRecommenderDAO.getInstance();
    }

    /**
     * This constructor allows a flood recommender DAO to be injected for
     * testing.
     * 
     * @param floodDAO
     *            Data access object
     */
    public RiverProFloodRecommender(IFloodRecommenderDAO floodDAO) {
        this.floodDAO = floodDAO;
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
    public List<IHazardEvent> getRecommendation(
            Map<String, Object> sessionAttributeMap,
            Map<String, Object> dialogInputMap,
            Map<String, Object> spatialInputMap) {

        initialize();

        /*
         * Retrieve the static meta data associated with the forecast points,
         * forecast groups and county groups.
         */
        loadForecastPoints();
        loadRiverGroups();
        loadCountyGroups();

        /*
         * Load the river and previous VTEC information.
         */
        loadFreshData();

        /*
         * The core of the recommender logic is here.
         */
        recommendRiverFloodHazards();

        return createHazards(dialogInputMap);
    }

    /**
     * 
     * Retrieves the river forecast points and their associated meta data for
     * this office.
     */
    private void loadForecastPoints() {
        forecastPointList = floodDAO.getForecastPointInfo(hazardSettings);
    }

    /**
     * Loads the river groups and their associated forecast points.
     */
    private void loadRiverGroups() {
        riverGroupList = floodDAO.getForecastGroupInfo(forecastPointList);
    }

    /**
     * Loads the counties and their associated forecast points.
     */
    private void loadCountyGroups() {
        countyForecastGroupList = floodDAO.getForecastCountyGroups(
                hazardSettings, forecastPointList);
    }

    /**
     * Loads updated observed and forecast data, if necessary.
     */
    private void loadFreshData() {
        loadPreviousEventInfo();
        loadTimeSeries();
        loadGroupMaxObservedForecastInfo();
        loadCountyMaxObservedForecastInfo();
    }

    /**
     * Reads previous event information.
     * 
     * This may not be needed, based on what we decide the role of recommenders
     * to be. At the moment, this recommender does not need to know about
     * previous events.
     */
    private void loadPreviousEventInfo() {
        Map<String, Object> eventDict = null;

        for (RiverForecastPoint forecastPoint : forecastPointList) {
            HydroEvent hazardEvent = new HydroEvent(forecastPoint,
                    hazardSettings, eventDict, floodDAO);
            hydroEventList.add(hazardEvent);
        }

    }

    /**
     * Loads the observed and forecast data for each river forecast point.
     */
    private void loadTimeSeries() {
        for (RiverForecastPoint forecastPoint : forecastPointList) {
            forecastPoint.loadTimeSeries();
        }
    }

    /**
     * Determines the maximum observed/forecast category and valid time for each
     * river group.
     */
    private void loadGroupMaxObservedForecastInfo() {
        for (RiverForecastGroup forecastGroup : riverGroupList) {
            forecastGroup.computeGroupMofo();
        }
    }

    /**
     * Determines the maximum observed/forecast category and valid time for each
     * county.
     */
    private void loadCountyMaxObservedForecastInfo() {
        for (CountyForecastGroup countyGroup : countyForecastGroupList) {
            countyGroup.computeCountyMofo();
        }
    }

    /**
     * Recommends river flood hazards based on all of the river forecast point
     * information loaded above.
     */
    private void recommendRiverFloodHazards() {
        boolean active = false;
        boolean shiftFlag = true;

        /*
         * Process each forecast point, using the previous event info already
         * extracted. There should be an event object for each river forecast
         * point.
         */
        for (HydroEvent hydroEvent : hydroEventList) {
            RiverForecastPoint forecastPoint = hydroEvent.getForecastPoint();

            if (hydroEvent.isEventFound()) {
                active = HydroEvent.checkIfEventActive(hydroEvent, floodDAO);
            }

            /*
             * The active state is saved at this point since this is the state
             * used in the below recommendations. we don't want to recompute the
             * active state when displaying the recomm info in the gui since the
             * system clock change may alter the active state, but the recomms
             * are not recomputed, so then they would not correspond. the active
             * field in this structure is the ongoing value of this, while the
             * prev_active is the field associated with the recomm info based on
             * the previous events...
             */
            hydroEvent.setEventActive(active);

            /*
             * define the times based on the pass thru times. have special check
             * in case the original rise-above used for the original begin time
             * has passed out of the data retrieval window, and a rise is noted
             * again for the same event - for this case we need to ensure that
             * once a (previous) begin time is missing, then the begin from then
             * on should always be missing. shift the ending time as requested
             * via token, to match the ending time actually determined later.
             */
            Date beginTime = forecastPoint.getRiseAboveTime();

            if (hydroEvent.getPreviousFLW().getVtecInfo().getBegintime() == null) {
                beginTime = null;
            }

            Date endTime = forecastPoint.getVirtualFallBelowTime(shiftFlag);

            /*
             * If active event, determine recommended follow-up.
             */
            if (active) {
                /*
                 * recommend cancel if the maximum of the current obs and max
                 * fcst is below minor flood. This check is used instead of
                 * checking if the end time is before the current time, to avoid
                 * the possible case of all data being below flood level due to
                 * some data situation; anyway if the max level is below flood,
                 * then it is certain that the endtime is below flood level.
                 */
                if (forecastPoint.getMaximumObservedForecastCategory() == RiverForecastPoint.HydroFloodCategories.NO_FLOOD_CATEGORY
                        .getRank()) {
                    hydroEvent.setRecommendedAction(HydroEvent.CANCEL_ACTION);

                    /*
                     * In general, if the proposed begin or endtime is not the
                     * same as the previous event's end time, then the action
                     * code is EXTended. There are some exceptions. By rule, the
                     * begin time cannot be changed once the begin time is
                     * reached.
                     */

                } else {
                    Date previousBeginTime = hydroEvent.getPreviousFLW()
                            .getVtecInfo().getBegintime();

                    if ((beginTime != null && previousBeginTime != null)
                            && (beginTime != previousBeginTime && previousBeginTime
                                    .getTime() > floodDAO.getSystemTime()
                                    .getTime())) {
                        hydroEvent
                                .setRecommendedAction(HydroEvent.EXTEND_IN_TIME_ACTION);
                    } else if (endTime != hydroEvent.getPreviousFLW()
                            .getVtecInfo().getEndtime()) {
                        hydroEvent
                                .setRecommendedAction(HydroEvent.EXTEND_IN_TIME_ACTION);
                    } else {
                        hydroEvent
                                .setRecommendedAction(HydroEvent.CONTINUE_ACTION);
                    }
                }

                /*
                 * now set the recommended product reason/index; if categorical
                 * rise occurred, then recommend FLW product. if there was a
                 * rise, then we know the omf > NONFLOOD, so the action for this
                 * active event is either EXT or CON.
                 */

                if (forecastPoint.getRiseOrFall() == HydroGraphTrend.RISE) {

                    hydroEvent
                            .setRecommendationReason(HydroEventReason.FLW_INCREASED_FLOODING);
                    hydroEvent.setRecommendationIndex(HydroEvent.FLW);
                }

                /*
                 * if no rise occurred for an active event, then either flooding
                 * is continuing or has ended
                 */

                else {
                    if (forecastPoint.getMaximumObservedForecastCategory() > RiverForecastPoint.HydroFloodCategories.NO_FLOOD_CATEGORY
                            .getRank()
                            || forecastPoint
                                    .getMaximumObservedForecastCategory() == RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                                    .getRank()) {
                        hydroEvent
                                .setRecommendationReason(HydroEventReason.FLS_CONTINUED_FLOODING);
                        hydroEvent.setRecommendationIndex(HydroEvent.FLS);
                    } else {
                        hydroEvent
                                .setRecommendationReason(HydroEventReason.FLS_ENDED_FLOODING);
                        hydroEvent.setRecommendationIndex(HydroEvent.FLS);
                    }

                }
            } else {
                /* if the forecast point is not an active event... */
                /* if flooding is occurring then recommend FLW NEW. */
                if (forecastPoint.getMaximumObservedForecastCategory() > RiverForecastPoint.HydroFloodCategories.NO_FLOOD_CATEGORY
                        .getRank()) {
                    hydroEvent
                            .setRecommendationReason(HydroEventReason.FLW_NEW_FLOODING);
                    hydroEvent.setRecommendationIndex(HydroEvent.FLW);
                    hydroEvent.setRecommendedAction(HydroEvent.NEW_ACTION);
                }

                /*
                 * if no flooding, recommend an EXP if event recently ended.
                 * otherwise recommend an RVS
                 */
                else if (forecastPoint.getMaximumObservedForecastCategory() == RiverForecastPoint.HydroFloodCategories.NO_FLOOD_CATEGORY
                        .getRank()) {
                    Date previousEndTime = hydroEvent.getPreviousFLW()
                            .getVtecInfo().getEndtime();
                    Date systemTime = floodDAO.getSystemTime();
                    Vtecaction vtecaction = hydroEvent.getPreviousFLW()
                            .getVtecInfo().getVtecaction();

                    if ((previousEndTime != null)
                            && (previousEndTime.getTime() > (systemTime
                                    .getTime() - HydroEvent.END_TIME_WITHIN))
                            && (!vtecaction.getAction().equals("CAN"))
                            && (!vtecaction.getAction().equals("EXP"))) {
                        hydroEvent
                                .setRecommendationReason(HydroEventReason.FLS_EXPIRED_FLOODING);
                        hydroEvent.setRecommendationIndex(HydroEvent.FLS);
                        hydroEvent
                                .setRecommendedAction(HydroEvent.EXPIRE_ACTION);
                    } else {
                        hydroEvent
                                .setRecommendationReason(HydroEventReason.RVS_NO_FLOODING);
                        hydroEvent.setRecommendationIndex(HydroEvent.RVS);
                        hydroEvent.setRecommendedAction(HydroEvent.NO_ACTION);
                    }
                } else {
                    hydroEvent
                            .setRecommendationReason(HydroEventReason.RVS_NO_DATA);
                    hydroEvent.setRecommendationIndex(HydroEvent.RVS);
                    hydroEvent.setRecommendedAction(HydroEvent.NO_ACTION);
                }
            }
        }

        /*
         * Loop on on all forecast points and find the most severe recommended
         * product.
         */
        int most_severe_product = HydroEvent.OTHER_PROD;

        for (HydroEvent hydroEvent : hydroEventList) {
            if (hydroEvent.getRecommendationIndex() > most_severe_product) {
                most_severe_product = hydroEvent.getRecommendationIndex();
            }
        }

        forecastPointEventMap = new HashMap<String, HydroEvent>();

        for (HydroEvent hydroEvent : hydroEventList) {
            RiverForecastPoint forecastPoint = hydroEvent.getForecastPoint();

            forecastPointEventMap.put(forecastPoint.getId(), hydroEvent);

            if ((hydroEvent.getRecommendationIndex() == most_severe_product)
                    || (most_severe_product <= HydroEvent.RVS)) {
                forecastPoint.setIncludedInRecommendation(true);
            } else {
                forecastPoint.setIncludedInRecommendation(false);
            }
        }

        /*
         * make another pass on the forecast points by group, to check if a
         * special group option is set when the product is an FLS or FLW. if so,
         * then if at least one point in group is included, include all points
         * with data in the group that do not have an event recommended. the
         * latter check is needed to ensure that actions associated with an FLW
         * are not placed in an FLS product.
         */

        for (RiverForecastGroup riverGroup : riverGroupList) {
            if ((riverGroup.isRecommendAllPointsInGroup())
                    && (most_severe_product > HydroEvent.RVS)) {
                boolean point_included = false;

                List<RiverForecastPoint> forecastPoints = riverGroup
                        .getForecastPointList();

                for (RiverForecastPoint forecastPoint : forecastPoints) {
                    if (forecastPoint.isIncludedInRecommendation()) {
                        point_included = true;
                        break;
                    }
                }

                if (point_included) {
                    for (int i = 0; i < forecastPoints.size(); ++i) {
                        HydroEvent hydroEvent = hydroEventList.get(i);

                        if (hydroEvent.getRecommendedAction().equals(
                                HydroEvent.NO_ACTION)
                                && hydroEvent.getRecommendationReason() != HydroEventReason.RVS_NO_DATA) {
                            forecastPoints.get(i).setIncludedInRecommendation(
                                    true);

                            if (most_severe_product == HydroEvent.FLS) {
                                hydroEvent
                                        .setRecommendationReason(HydroEventReason.FLS_GROUP_IN_FLS);
                            } else {
                                hydroEvent
                                        .setRecommendationReason(HydroEventReason.FLW_GROUP_IN_FLW);
                            }

                            hydroEvent
                                    .setRecommendationIndex(most_severe_product);
                            hydroEvent
                                    .setRecommendedAction(HydroEvent.ROUTINE_ACTION);
                        }
                    }
                }
            }
        }

        /* filter out any points that do not have data for non-FLS/FLW products */
        if (most_severe_product <= HydroEvent.RVS) {
            for (RiverForecastPoint riverForecastPoint : forecastPointList) {
                if (riverForecastPoint.getMaximumObservedForecastCategory() == RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                        .getRank()) {
                    riverForecastPoint.setIncludedInRecommendation(false);
                }
            }
        }

        /*
         * Determine which groups (i.e. portions thereof) are included based
         * upon whether any forecast points in the group are included.
         */
        for (RiverForecastGroup riverGroup : riverGroupList) {
            riverGroup.setIncludedInRecommendation(false);

            for (RiverForecastPoint riverForecastPoint : riverGroup
                    .getForecastPointList()) {
                if (riverForecastPoint.isIncludedInRecommendation()) {
                    riverGroup.setIncludedInRecommendation(true);
                    break;
                }
            }
        }

        /*
         * log info on the recomms. log the recs themselves, then the supporting
         * info, including the previous info and the current info.
         */
        for (RiverForecastGroup riverGroup : riverGroupList) {
            for (RiverForecastPoint riverForecastPoint : riverGroup
                    .getForecastPointList()) {
                HydroEvent hydroEvent = forecastPointEventMap
                        .get(riverForecastPoint.getId());
                String output = String.format("%s (%s): %s %s",
                        riverForecastPoint.getId(), riverGroup.getId(),
                        hydroEvent.getRecommendedAction(),
                        hydroEvent.getRecommendationReason());
                statusHandler.info(output);

                /* log previous info */
                if (hydroEvent.getPreviousFLW().isEventFound()) {
                    output = String.format("  previous: %d %s %s-%s",
                            hydroEvent.getPreviousFLW().isEventActive(),
                            hydroEvent.getPreviousFLW().getVtecInfo()
                                    .getVtecaction().getAction(), hydroEvent
                                    .getPreviousFLW().getVtecInfo()
                                    .getBegintime(), hydroEvent
                                    .getPreviousFLW().getVtecInfo()
                                    .getEndtime());
                    statusHandler.info(output);
                }

                /* log current info */
                output = String
                        .format("  current: %s %s %s-%s\n", riverForecastPoint
                                .getMaximumObservedForecastCategory(),
                                riverForecastPoint.getRiseOrFall(),
                                riverForecastPoint.getRiseAboveTime(),
                                riverForecastPoint.getFallBelowTime());
                statusHandler.info(output);
            }
        }
    }

    /**
     * Builds a JSON string containing a list of the recommended river flood
     * hazards.
     * 
     * @param
     * @return
     */
    private List<IHazardEvent> createHazards(Map<String, Object> dialogInputMap) {
        boolean isWarning = false;
        /*
         * Check the runData for a forecast confidence percentage.
         */
        Number percentageNumber = (Number) dialogInputMap
                .get("forecastConfidencePercentage");

        if (percentageNumber != null) {
            int percentage = percentageNumber.intValue();

            if (percentage >= WARNING_THRESHOLD) {
                isWarning = true;
            }
        }

        List<IHazardEvent> listOfPotentialEventDicts = getFloodDictList(isWarning);

        return listOfPotentialEventDicts;
    }

    /**
     * Builds a list of hazard recommendations based on the output from the
     * river flood recommender.
     * 
     * @param isWarning
     *            true - create FL.W hazards, false - create FL.A hazards
     * @return A list of recommended hazards.
     */
    public List<IHazardEvent> getFloodDictList(boolean isWarning) {
        List<IHazardEvent> listOfPotentialEventDicts = new ArrayList<IHazardEvent>();

        for (RiverForecastGroup riverGroup : riverGroupList) {
            for (RiverForecastPoint riverForecastPoint : riverGroup
                    .getForecastPointList()) {
                if (riverForecastPoint.isIncludedInRecommendation()) {
                    /*
                     * Retrieve the reach information for this forecast point
                     */
                    Map<String, Serializable> hazardAttributes = new HashMap<String, Serializable>();

                    IHazardEvent pointEventDict = new BaseHazardEvent();
                    pointEventDict.setEventID("");
                    pointEventDict.setState(HazardState.POTENTIAL);
                    pointEventDict.setPhenomenon("FL");

                    if (isWarning) {

                        pointEventDict.setSignificance("W");
                    } else {
                        pointEventDict.setSignificance("A");

                    }

                    listOfPotentialEventDicts.add(pointEventDict);

                    hazardAttributes.put("pointID", riverForecastPoint.getId());

                    hazardAttributes.put("floodStage",
                            riverForecastPoint.getFloodStage());
                    hazardAttributes.put("actionStage",
                            riverForecastPoint.getActionStage());

                    double currentStage = riverForecastPoint
                            .getCurrentObservation().getValue();

                    hazardAttributes.put("currentStage", currentStage);

                    long currentStageTime = riverForecastPoint
                            .getCurrentObservation().getValidTime();
                    hazardAttributes.put("currentStageTime", currentStageTime);

                    Date riseAbove = riverForecastPoint.getRiseAboveTime();

                    if ((riseAbove != null) && (riseAbove.getTime() > 0)) {
                        if (isWarning) {
                            hazardAttributes.put("riseAbove",
                                    riseAbove.getTime());
                        } else {
                            hazardAttributes.put("riseAbove", 0);
                        }
                        pointEventDict.setStartTime(riseAbove);
                    } else {
                        hazardAttributes.put("riseAbove", 0);
                        pointEventDict.setStartTime(floodDAO.getSystemTime());
                    }

                    Date maxObservedForecastCrestDate = riverForecastPoint
                            .getMaximumObservedForecastTime();

                    if (maxObservedForecastCrestDate != null && isWarning) {
                        hazardAttributes.put("crest",
                                maxObservedForecastCrestDate.getTime());
                        hazardAttributes.put("crestStage", riverForecastPoint
                                .getMaximumObservedForecastValue());
                    } else {
                        hazardAttributes.put("crest", 0);
                        hazardAttributes.put("crestStage", 0);
                    }

                    Date fallBelow = riverForecastPoint.getFallBelowTime();

                    if (fallBelow != null) {
                        if (isWarning) {
                            hazardAttributes.put("fallBelow",
                                    fallBelow.getTime());
                        } else {
                            hazardAttributes.put("fallBelow", 0);
                        }

                        /*
                         * Need to consider the shift ahead hours here...
                         */
                        Date virtualEndTime = riverForecastPoint
                                .getVirtualFallBelowTime(true);
                        pointEventDict.setEndTime(virtualEndTime);
                    } else {
                        hazardAttributes.put("fallBelow", 0);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(pointEventDict.getStartTime());
                        cal.add(Calendar.HOUR,
                                hazardSettings.getFlwExpirationHours());
                        pointEventDict.setEndTime(cal.getTime());
                    }

                    // Default to excessive rainfall.
                    hazardAttributes.put("immediateCause", "ER");

                    // Define flood record
                    int floodCategory = riverForecastPoint
                            .getMaximumObservedForecastCategory();

                    if (floodCategory > RiverForecastPoint.HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                            .getRank()) {
                        floodCategory = RiverForecastPoint.HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                                .getRank();
                    }

                    if (isWarning) {
                        /*
                         * This needs work...
                         */
                        String recordStatus = retrieveFloodRecord(
                                hazardSettings, riverForecastPoint);
                        hazardAttributes.put("floodRecord", recordStatus);
                    } else {
                        hazardAttributes.put("floodRecord", "OO");
                    }

                    /*
                     * Need to translate the flood category to a string value.
                     */
                    if (isWarning) {
                        if (floodCategory == RiverForecastPoint.HydroFloodCategories.NULL_CATEGORY
                                .getRank()) {
                            hazardAttributes.put("floodSeverity", "N");
                        } else {
                            hazardAttributes.put("floodSeverity",
                                    Integer.toString(floodCategory));
                        }
                    } else {
                        hazardAttributes.put("floodSeverity", "0");
                    }

                    hazardAttributes.put("creationTime", Calendar.getInstance()
                            .getTime().getTime());
                    pointEventDict.setIssueTime(Calendar.getInstance()
                            .getTime());

                    // Point shape specific information
                    // Circle pointObject = new Circle("",
                    // riverForecastPoint.getId(),
                    // riverForecastPoint.getName(), "true", "true",
                    // "true", "", riverForecastPoint.getLocation());
                    // pointEventDict.addShape(pointObject);
                    List<Double> pointCoords = Lists.newArrayList();
                    Coordinate pointLocation = riverForecastPoint.getLocation();
                    pointCoords.add(pointLocation.x);
                    pointCoords.add(pointLocation.y);

                    Map<String, Serializable> forecastPointAttributes = Maps
                            .newHashMap();
                    forecastPointAttributes.put("point",
                            (Serializable) pointCoords);
                    forecastPointAttributes.put("id",
                            riverForecastPoint.getId());
                    forecastPointAttributes.put("name",
                            riverForecastPoint.getName());
                    hazardAttributes.put("forecastPoint",
                            (Serializable) forecastPointAttributes);

                    pointEventDict.setHazardAttributes(hazardAttributes);
                }
            }
        }

        return listOfPotentialEventDicts;
    }

    /**
     * Initializes the River Flood Recommender.
     */
    private void initialize() {
        hydroEventList = Lists.newArrayList();

        /*
         * Retrieve control information that pertains to all of the recommended
         * hydrologic hazards.
         */
        hazardSettings = floodDAO.retrieveRecommenderSettings();
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
            if (pe.charAt(0) == 'Q') {
                threshold = settings.getVtecRecordFlowOffset();
            } else {
                threshold = settings.getVtecRecordStageOffset();
            }

            if (crestValue >= (recordValue - threshold)) {
                recordString = "NR";
            } else {
                recordString = "NO";
            }
        } else {
            recordString = "UU";
        }

        return recordString;
    }

}
