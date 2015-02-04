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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.shef.tables.Vtecaction;
import com.raytheon.uf.common.hazards.hydro.HydroEvent.HydroEventReason;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint.HydroGraphTrend;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Used to retrieve hydro data utilized by recommenders and product generators
 * in hazard services.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 30, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RiverProDataManager {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverProDataManager.class);

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
     * Contains configurable settings.
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
    private final IFloodDAO floodDAO;

    /**
     * 
     */
    public RiverProDataManager() {
        this(FloodDAO.getInstance());
    }

    public RiverProDataManager(IFloodDAO floodDAO) {
        this.floodDAO = floodDAO;

        this.initialize();

        /*
         * Retrieve the static meta data associated with the forecast points,
         * forecast groups and county groups.
         */
        this.loadForecastPoints();
        this.loadRiverGroups();
        this.loadCountyGroups();

        /*
         * Load the river and previous VTEC information.
         */
        this.loadFreshData();

        this.recommendRiverFloodHazards();
    }

    /**
     * Initializes the River Flood Data Manager.
     */
    private void initialize() {
        this.hydroEventList = Lists.newArrayList();

        this.hazardSettings = this.floodDAO.retrieveSettings();
    }

    /**
     * 
     * Retrieves the river forecast points and their associated meta data for
     * this office.
     */
    private void loadForecastPoints() {
        this.forecastPointList = this.floodDAO
                .getForecastPointInfo(this.hazardSettings);
    }

    /**
     * Loads the river groups and their associated forecast points.
     */
    private void loadRiverGroups() {
        this.riverGroupList = this.floodDAO
                .getForecastGroupInfo(this.forecastPointList);
    }

    /**
     * Loads the counties and their associated forecast points.
     */
    private void loadCountyGroups() {
        this.countyForecastGroupList = this.floodDAO.getForecastCountyGroups(
                this.hazardSettings, this.forecastPointList);
    }

    /**
     * Loads updated observed and forecast data, if necessary.
     */
    private void loadFreshData() {
        this.loadPreviousEventInfo();
        this.loadTimeSeries();
        this.loadGroupMaxObservedForecastInfo();
        this.loadCountyMaxObservedForecastInfo();
    }

    /**
     * TODO: determine if this is actually need. Reads previous event
     * information.
     * 
     * This may not be needed, based on what we decide the role of recommenders
     * to be. At the moment, this recommender does not need to know about
     * previous events.
     */
    private void loadPreviousEventInfo() {
        Map<String, Object> eventMap = null;

        for (RiverForecastPoint forecastPoint : forecastPointList) {
            HydroEvent hazardEvent = new HydroEvent(forecastPoint,
                    hazardSettings, eventMap, floodDAO);
            hydroEventList.add(hazardEvent);
        }
    }

    /**
     * Loads the observed and forecast data for each river forecast point.
     */
    private void loadTimeSeries() {
        for (RiverForecastPoint forecastPoint : forecastPointList) {
            String primary_pe = forecastPoint.getPhysicalElement();
            forecastPoint.loadTimeSeries(primary_pe);
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
                                    .getTime() > floodDAO.getSystemTime()
                                    .getTime())) {
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
                    hydroEvent.setRecommendedAction(HazardConstants.NEW_ACTION);
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
                                    .setRecommendedAction(HazardConstants.ROUTINE_ACTION);
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

    public List<RiverForecastPoint> getForecastPointList() {
        return forecastPointList;
    }

    public List<RiverForecastGroup> getRiverGroupList() {
        return riverGroupList;
    }

    public List<CountyForecastGroup> getCountyForecastGroupList() {
        return countyForecastGroupList;
    }

    public List<HydroEvent> getHydroEventList() {
        return hydroEventList;
    }

    public HazardSettings getHazardSettings() {
        return hazardSettings;
    }

    public Map<String, HydroEvent> getForecastPointEventMap() {
        return forecastPointEventMap;
    }

    public IFloodDAO getFloodDAO() {
        return floodDAO;
    }
}