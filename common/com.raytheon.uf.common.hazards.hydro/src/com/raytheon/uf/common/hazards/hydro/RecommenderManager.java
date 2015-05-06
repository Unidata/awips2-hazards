/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.hazards.hydro;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecaction;
import com.raytheon.uf.common.hazards.hydro.HydroEvent.HydroEventReason;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroFloodCategories;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroGraphTrend;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * This class is responsible for managing queries and queried River Flood (River
 * Forecast) data. This class is used to manage and access a RecommenderData
 * object that is contained within the memory-persistent RecommenderDataCache
 * object.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * </pre>
 * 
 * @author Chris.Cody
 */

public class RecommenderManager {

    /**
     * The River Forcast Manager data accessor object.
     */
    private RiverForecastManager riverForecastManager;

    /**
     * Singleton instance of this River Flood Recommender Manager
     */
    private static RecommenderManager recommenderManagerInstance = null;

    /**
     * Private RecommenderManager constructor. There needs to be only 1 instance
     * in memory for this to be useful for caching.
     * 
     */
    private RecommenderManager() {
        this.riverForecastManager = new RiverForecastManager();
    }

    /**
     * Retrieve the RecommenderManager singleton instance.
     * 
     * @return
     */
    public synchronized static RecommenderManager getInstance() {
        if (recommenderManagerInstance == null) {
            recommenderManagerInstance = new RecommenderManager();
        }
        return (recommenderManagerInstance);
    }

    /**
     * Query and Cache Recommender data for the River Flood Recommender.
     * 
     * It also processes the queried data and recommends river flood hazards
     * based on all of the river forecast point information.
     * 
     * @param currentSystemTime
     *            Current system time for Observation and Forecast queries. This
     *            is also used for a set time for the RecommenderDataCache.
     * 
     * @return RecommenderData data object containing queried and computed River
     *         Flood Recommender data.
     */
    public RecommenderData getRiverFloodRecommenderData(long cacheSystemTime) {

        RecommenderData recommenderData = null;
        RecommenderDataCache recommenderDataCache = RecommenderDataCache
                .getInstance();

        if (recommenderDataCache.isCacheValid(cacheSystemTime) == true) {
            recommenderData = recommenderDataCache
                    .getCachedData(cacheSystemTime);
        } else {
            recommenderDataCache.purgeCachedData();
        }

        if (recommenderData == null) {
            recommenderData = queryAndComputeRecommenderData(cacheSystemTime);
            if (recommenderData != null) {
                recommenderDataCache.putCachedData(cacheSystemTime,
                        recommenderData);
            }
        }

        return (recommenderData);
    }

    /**
     * This method will query and compute all necessary data for the River Flood
     * Recommender.
     * 
     * It does not perform the final recommendation processing.
     * 
     * @param currentSystemTime
     *            Current system time for Observation and Forecast queries. This
     *            is also used for a set time for the RecommenderDataCache.
     * 
     * @return RecommenderData data object containing queried and computed River
     *         Flood Recommender data.
     */
    public RecommenderData queryAndComputeRecommenderData(long currentSystemTime) {

        RecommenderData recommenderData = buildRecommenderRiverForecastData(currentSystemTime);

        processHydroEventRecommendationData(currentSystemTime, recommenderData);

        return (recommenderData);
    }

    /**
     * This method consolidates the queried and computed Recommender Data.
     * 
     * It does not cache the Recommender data.
     * 
     * @param currentSystemTime
     *            Current system time for Observation and Forecast queries. This
     *            is also used for a set time for the RecommenderDataCache.
     * @return RecommenderData data object containing queried and computed River
     *         Flood Recommender data.
     */
    protected RecommenderData buildRecommenderRiverForecastData(
            long cacheSystemTime) {

        if (cacheSystemTime == 0) {
            cacheSystemTime = TimeUtil.currentTimeMillis();
        }
        String hsaId = this.riverForecastManager.getHydrologicServiceAreaId();

        List<RiverForecastGroup> recAllRiverForecastGroupList = queryAllHsaRiverForecastData(hsaId);

        List<RiverForecastPoint> recAllRiverForecastPointList = buildAllRiverForecastList(recAllRiverForecastGroupList);

        Map<String, Object> previousEventMap = Maps.newHashMap();
        List<HydroEvent> recAllHydroEventList = constructHydroEventList(hsaId,
                recAllRiverForecastPointList, previousEventMap, cacheSystemTime);

        // Now all data has been assembled. Process and set recommendations
        RecommenderData recommenderData = buildRecommenderData(hsaId,
                cacheSystemTime, recAllRiverForecastGroupList,
                recAllRiverForecastPointList, recAllHydroEventList,
                previousEventMap);

        return (recommenderData);

    }

    /**
     * Query all RiverForecastGroup objects within the currently configured
     * Hydrologic Service Area (HSA).
     * 
     * @return a DEEP QUERY with COMPUTED values for all RiverForecastGroup
     *         objects and their lists RiverForecastPoint objects (Also DEEP
     *         QUERY with COMPUTED values).
     */
    private List<RiverForecastGroup> queryAllHsaRiverForecastData(String hsaId) {

        List<RiverForecastGroup> recAllRiverForecastGroupList = riverForecastManager
                .getHsaRiverForecastGroupList(hsaId, true);

        return (recAllRiverForecastGroupList);
    }

    /**
     * Construct ONE RiverForecastPoint List containing all possible
     * RiverForecastPoint objects for the HSA.
     * 
     * @param recAllRiverForecastGroupList
     *            DEEP QUERY List of all RiverForecastGroup objects
     * @return Chronologically sorted List of ALL HSA RiverForecastPoint objects
     */
    private List<RiverForecastPoint> buildAllRiverForecastList(
            List<RiverForecastGroup> recAllRiverForecastGroupList) {

        List<RiverForecastPoint> recAllRiverForecastPointList = Lists
                .newArrayList();

        if ((recAllRiverForecastGroupList != null)
                && (recAllRiverForecastGroupList.isEmpty() == false)) {
            for (RiverForecastGroup riverForecastGroup : recAllRiverForecastGroupList) {
                List<RiverForecastPoint> riverForecastPointList = riverForecastGroup
                        .getForecastPointList();
                if ((riverForecastPointList != null)
                        && (riverForecastPointList.isEmpty() == false)) {
                    recAllRiverForecastPointList.addAll(riverForecastPointList);
                }
            }
        }

        return (recAllRiverForecastPointList);
    }

    private RecommenderData buildRecommenderData(String hsaId,
            long currentSystemTime,
            List<RiverForecastGroup> riverForecastGroupList,
            List<RiverForecastPoint> riverForecastPointList,
            List<HydroEvent> hydroEventList,
            Map<String, Object> previousEventMap) {
        RecommenderData recommenderData = new RecommenderData(hsaId,
                currentSystemTime, riverForecastGroupList,
                riverForecastPointList, hydroEventList, previousEventMap);
        return (recommenderData);
    }

    /**
     * Construct Hydro Event (Previous Event) List.
     * 
     * Build a Hydro Event list from a list of chronologically sequenced, Deep
     * Query RiverForecastPoint objects.
     * <p>
     * 
     * TODO: determine if this is actually needed. Reads previous event
     * information.
     * 
     * This may not be needed, based on what we decide the role of recommenders
     * to be. At the moment, this recommender does not need to know about
     * previous events.
     * <p>
     * 
     * The construction of the HydroEvent from the RiverForecastPoint will set
     * PreviousCur and PreviousMax RiverForecastPoint values.
     * <p>
     * 2015-04-30 #6562 Chris.Cody: The existing code does not generate previous
     * data (null input map). These fields do not contain accurate data.
     * 
     * @param hsaId
     *            Hydrologic Service Area ID
     * @param forecastPointList
     *            List of RiverForecastPoint objects for the given HSA
     * @param eventMap
     *            Map of previous event data items [ < Event Id, Map [property ,
     *            value>> ]
     */
    public List<HydroEvent> constructHydroEventList(String hsaId,
            List<RiverForecastPoint> allRecforecastPointList,
            Map<String, Object> eventMap, long currentSystemTime) {
        /*
         * Take a list of Active Forecast Points. Chronologically sort them and
         * construct the Previous Info data for the given list of points.
         */
        List<HydroEvent> hydroEventList = Lists
                .newArrayListWithExpectedSize(allRecforecastPointList.size());
        for (RiverForecastPoint forecastPoint : allRecforecastPointList) {

            computeForecastPointPrevInfo(forecastPoint);
            HydroEvent hazardEvent = new HydroEvent(forecastPoint, hsaId,
                    eventMap, currentSystemTime);
            hydroEventList.add(hazardEvent);
        }

        return (hydroEventList);
    }

    /**
     * Determines the values of certain variables that are based on the previous
     * product info of the stage data for each forecast point. NOTES The
     * rise_or_fall flags for each forecast group are determined elsewhere, even
     * though since they are from the rise_or_fall flags for each forecast
     * point, they too rely on previous product data. If previous data are
     * missing, the flag is set to unchanged, which is "safe" but not really
     * valid. This function is broken out in this manner since the previous
     * product information may be updated during a given execution of the
     * RiverPro program (e.g. if a product is issued via the gui...) WAS:
     * compute_fp_prev_info
     */
    private void computeForecastPointPrevInfo(
            RiverForecastPoint riverForecastPoint) {
        int categoryValue;

        /* set the rise fall flags to unchanged, not missing */
        riverForecastPoint.setObservedRiseOrFall(HydroGraphTrend.UNCHANGED);
        riverForecastPoint.setForecastRiseOrFall(HydroGraphTrend.UNCHANGED);
        riverForecastPoint.setRiseOrFall(HydroGraphTrend.UNCHANGED);

        if (riverForecastPoint.getPreviousProductAvailable() == true) {
            if ((riverForecastPoint.getCurrentObservation().getValue() != RiverHydroConstants.MISSING_VALUE)
                    && (riverForecastPoint.getPreviousCurObsValue() != RiverHydroConstants.MISSING_VALUE)) {
                /*
                 * if data are available and if the category for the current
                 * stage is greater than the category for the previous product,
                 * then a rise occurred
                 */

                categoryValue = this.riverForecastManager
                        .computeFloodStageCategory(riverForecastPoint,
                                riverForecastPoint.getPreviousCurObsValue());

                if (riverForecastPoint.getCurrentObservationCategory() > categoryValue) {
                    riverForecastPoint
                            .setObservedRiseOrFall(HydroGraphTrend.RISE);
                } else if (riverForecastPoint.getCurrentObservationCategory() == categoryValue) {
                    riverForecastPoint
                            .setObservedRiseOrFall(HydroGraphTrend.UNCHANGED);
                } else {
                    riverForecastPoint
                            .setObservedRiseOrFall(HydroGraphTrend.FALL);
                }
            }

            if ((riverForecastPoint.getMaximumSHEFForecast().getValue() != RiverHydroConstants.MISSING_VALUE)
                    && (riverForecastPoint.getPreviousMaxFcstValue() != RiverHydroConstants.MISSING_VALUE)) {
                /*
                 * if data are available and if the category for the stage is
                 * greater than the category for the previous product, then a
                 * rise occurred
                 */

                categoryValue = this.riverForecastManager
                        .computeFloodStageCategory(riverForecastPoint,
                                riverForecastPoint.getPreviousMaxFcstValue());

                if (riverForecastPoint.getMaximumForecastCategory() > categoryValue) {
                    riverForecastPoint
                            .setForecastRiseOrFall(HydroGraphTrend.RISE);
                } else if (riverForecastPoint.getMaximumForecastCategory() == categoryValue) {
                    riverForecastPoint
                            .setForecastRiseOrFall(HydroGraphTrend.UNCHANGED);
                } else {
                    riverForecastPoint
                            .setForecastRiseOrFall(HydroGraphTrend.FALL);
                }
            }

            /*
             * get the previous obs and max fcst category if previous data
             * available for CAT threshold
             */
            if ((riverForecastPoint.getMaximumObservedForecastCategory() > HydroFloodCategories.NULL_CATEGORY
                    .getRank())
                    && (riverForecastPoint
                            .getPreviousMaxObservedForecastCategory() > HydroFloodCategories.NULL_CATEGORY
                            .getRank())) {
                if (riverForecastPoint.getMaximumObservedForecastCategory() > riverForecastPoint
                        .getPreviousMaxObservedForecastCategory()) {
                    riverForecastPoint.setRiseOrFall(HydroGraphTrend.RISE);
                } else if (riverForecastPoint
                        .getMaximumObservedForecastCategory() == riverForecastPoint
                        .getPreviousMaxObservedForecastCategory()) {
                    riverForecastPoint.setRiseOrFall(HydroGraphTrend.UNCHANGED);
                } else {
                    riverForecastPoint.setRiseOrFall(HydroGraphTrend.FALL);
                }
            }

        } /* end of if block on previous info available */

        return;
    }

    private void processHydroEventRecommendationData(long currentSystemTime,
            RecommenderData recommenderData) {

        boolean active = false;
        boolean shiftFlag = true;

        List<RiverForecastGroup> recAllRiverForecastGroupList = recommenderData
                .getRiverForecastGroupList();
        List<RiverForecastPoint> recAllRiverForecastPointList = recommenderData
                .getRiverForecastPointList();
        List<HydroEvent> recAllHydroEventList = recommenderData
                .getHydroEventList();

        // START of original recommendRiverFloodHazards() processing
        /*
         * Process each forecast point, using the previous event info already
         * extracted. There should be an event object for each river forecast
         * point.
         */
        for (HydroEvent hydroEvent : recAllHydroEventList) {
            RiverForecastPoint forecastPoint = hydroEvent.getForecastPoint();

            if (hydroEvent.isEventFound()) {
                active = hydroEvent.checkIfEventActive(currentSystemTime);
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
            Date beginTime = new Date(forecastPoint.getRiseAboveTime());

            if (hydroEvent.getPreviousFLW().getVtecInfo().getBegintime() == null) {
                beginTime = null;
            }

            long endTimeLong = this.riverForecastManager
                    .getVirtualFallBelowTime(forecastPoint, shiftFlag);
            Date endTime = new Date(endTimeLong);

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
                if (forecastPoint.getMaximumObservedForecastCategory() == HydroFloodCategories.NO_FLOOD_CATEGORY
                        .getRank()) {
                    hydroEvent
                            .setRecommendedAction(HazardConstants.CANCEL_ACTION);

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
                                    .getTime() > currentSystemTime)) {
                        hydroEvent
                                .setRecommendedAction(HazardConstants.EXTEND_IN_TIME_ACTION);
                    } else if (endTime != hydroEvent.getPreviousFLW()
                            .getVtecInfo().getEndtime()) {
                        hydroEvent
                                .setRecommendedAction(HazardConstants.EXTEND_IN_TIME_ACTION);
                    } else {
                        hydroEvent
                                .setRecommendedAction(HazardConstants.CONTINUE_ACTION);
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
                } else {
                    /*
                     * if no rise occurred for an active event, then either
                     * flooding is continuing or has ended
                     */
                    if (forecastPoint.getMaximumObservedForecastCategory() > HydroFloodCategories.NO_FLOOD_CATEGORY
                            .getRank()
                            || forecastPoint
                                    .getMaximumObservedForecastCategory() == HydroFloodCategories.NULL_CATEGORY
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
                if (forecastPoint.getMaximumObservedForecastCategory() > HydroFloodCategories.NO_FLOOD_CATEGORY
                        .getRank()) {
                    hydroEvent
                            .setRecommendationReason(HydroEventReason.FLW_NEW_FLOODING);
                    hydroEvent.setRecommendationIndex(HydroEvent.FLW);
                    hydroEvent.setRecommendedAction(HazardConstants.NEW_ACTION);
                } else if (forecastPoint.getMaximumObservedForecastCategory() == HydroFloodCategories.NO_FLOOD_CATEGORY
                        .getRank()) {
                    /*
                     * if no flooding, recommend an EXP if event recently ended.
                     * otherwise recommend an RVS
                     */
                    Date previousEndTime = hydroEvent.getPreviousFLW()
                            .getVtecInfo().getEndtime();
                    Date systemTime = new Date(currentSystemTime);
                    Vtecaction vtecaction = hydroEvent.getPreviousFLW()
                            .getVtecInfo().getVtecaction();

                    if ((previousEndTime != null)
                            && (previousEndTime.getTime() > (systemTime
                                    .getTime() - HydroEvent.END_TIME_WITHIN))
                            && (!vtecaction.getAction().equals(
                                    HazardConstants.CANCEL_ACTION))
                            && (!vtecaction.getAction().equals(
                                    HazardConstants.EXPIRE_ACTION))) {
                        hydroEvent
                                .setRecommendationReason(HydroEventReason.FLS_EXPIRED_FLOODING);
                        hydroEvent.setRecommendationIndex(HydroEvent.FLS);
                        hydroEvent
                                .setRecommendedAction(HazardConstants.EXPIRE_ACTION);
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
        int mostSevereProduct = HydroEvent.OTHER_PROD;

        for (HydroEvent hydroEvent : recAllHydroEventList) {
            if (hydroEvent.getRecommendationIndex() > mostSevereProduct) {
                mostSevereProduct = hydroEvent.getRecommendationIndex();
            }
        }

        Map<String, HydroEvent> pointIdToHydroEventMap = Maps
                .newHashMapWithExpectedSize(recAllHydroEventList.size());

        for (HydroEvent hydroEvent : recAllHydroEventList) {
            RiverForecastPoint forecastPoint = hydroEvent.getForecastPoint();

            pointIdToHydroEventMap.put(forecastPoint.getLid(), hydroEvent);

            if ((hydroEvent.getRecommendationIndex() == mostSevereProduct)
                    || (mostSevereProduct <= HydroEvent.RVS)) {
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
        for (RiverForecastGroup riverGroup : recAllRiverForecastGroupList) {
            if ((riverGroup.isRecommendAllPointsInGroup() == true)
                    && (mostSevereProduct > HydroEvent.RVS)) {

                List<RiverForecastPoint> groupForecastPointList = riverGroup
                        .getForecastPointList();
                int groupForecastPointListSize = groupForecastPointList.size();
                boolean isPointInGroupFound = false;
                RiverForecastPoint riverForecastPoint = null;
                for (int i = 0; ((isPointInGroupFound == false) && (i < groupForecastPointListSize)); i++) {
                    riverForecastPoint = groupForecastPointList.get(i);
                    if (riverForecastPoint.isIncludedInRecommendation()) {
                        processAllPointsInGroup(groupForecastPointList,
                                pointIdToHydroEventMap, mostSevereProduct);
                        isPointInGroupFound = true;
                    }
                }
            }
        }

        /* filter out any points that do not have data for non-FLS/FLW products */
        if (mostSevereProduct <= HydroEvent.RVS) {
            for (RiverForecastPoint riverForecastPoint : recAllRiverForecastPointList) {
                if (riverForecastPoint.getMaximumObservedForecastCategory() == HydroFloodCategories.NULL_CATEGORY
                        .getRank()) {
                    riverForecastPoint.setIncludedInRecommendation(false);
                }
            }
        }

        /*
         * Determine which groups (i.e. portions thereof) are included based
         * upon whether any forecast points in the group are included.
         */
        for (RiverForecastGroup riverGroup : recAllRiverForecastGroupList) {
            riverGroup.setIncludedInRecommendation(false);

            for (RiverForecastPoint riverForecastPoint : riverGroup
                    .getForecastPointList()) {
                if (riverForecastPoint.isIncludedInRecommendation()) {
                    riverGroup.setIncludedInRecommendation(true);
                    break;
                }
            }
        }

        /**
         * TODO Reinstate?? log info on the recomms. log the recs themselves,
         * then the supporting info, including the previous info and the current
         * info.
         * 
         * <pre>
         *         for (RiverForecastGroup riverGroup : recAllRiverForecastGroupList) {
         *             for (RiverForecastPoint forecastPoint : riverGroup
         *                     .getForecastPointList()) {
         *                 HydroEvent hydroEvent = pointIdToHydroEventMap
         *                         .get(forecastPoint.getLid());
         *                 String output = String.format("%s (%s): %s %s",
         *                         forecastPoint.getLid(), riverGroup.getGroupId(),
         *                         hydroEvent.getRecommendedAction(),
         *                         hydroEvent.getRecommendationReason());
         *                 statusHandler.info(output);
         * 
         *                 /* log previous info * /
         *                 if (hydroEvent.getPreviousFLW().isEventFound()) {
         *                     output = String.format("  previous: %d %s %s-%s",
         *                             hydroEvent.getPreviousFLW().isEventActive(),
         *                             hydroEvent.getPreviousFLW().getVtecInfo()
         *                                     .getVtecaction().getAction(), hydroEvent
         *                                     .getPreviousFLW().getVtecInfo()
         *                                     .getBegintime(), hydroEvent
         *                                     .getPreviousFLW().getVtecInfo()
         *                                     .getEndtime());
         *                     statusHandler.info(output);
         *                 }
         * 
         *                 /* log current info * /
         *                 output = String.format("  current: %s %s %s-%s\n",
         *                         forecastPoint.getMaximumObservedForecastCategory(),
         *                         forecastPoint.getRiseOrFall(),
         *                         forecastPoint.getRiseAboveTime(),
         *                         forecastPoint.getFallBelowTime());
         *                 statusHandler.info(output);
         *             }
         *         }
         * </pre>
         */

    }

    protected void processAllPointsInGroup(
            List<RiverForecastPoint> riverForecastPointList,
            Map<String, HydroEvent> pointIdToHydroEventMap,
            int mostSevereProduct) {

        for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
            String lid = riverForecastPoint.getLid();
            HydroEvent hydroEvent = pointIdToHydroEventMap.get(lid);

            if (hydroEvent.getRecommendedAction().equals(HydroEvent.NO_ACTION)
                    && hydroEvent.getRecommendationReason() != HydroEventReason.RVS_NO_DATA) {
                riverForecastPoint.setIncludedInRecommendation(true);

                if (mostSevereProduct == HydroEvent.FLS) {
                    hydroEvent
                            .setRecommendationReason(HydroEventReason.FLS_GROUP_IN_FLS);
                } else {
                    hydroEvent
                            .setRecommendationReason(HydroEventReason.FLW_GROUP_IN_FLW);
                }

                hydroEvent.setRecommendationIndex(mostSevereProduct);
                hydroEvent.setRecommendedAction(HazardConstants.ROUTINE_ACTION);
            }
        }
    }

}
