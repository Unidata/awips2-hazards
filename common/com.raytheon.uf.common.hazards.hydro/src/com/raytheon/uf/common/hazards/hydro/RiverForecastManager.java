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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroDataType;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroFloodCategories;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants.HydroGraphTrend;
import com.raytheon.uf.common.ohd.AppsDefaults;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.Pair;

/**
 * This class Manages all River Forecast Group, River Forecast Point, River
 * Station, River Status, Hydrograph Observed, Hydrograph Forecast, SHEF
 * Observed, SHEF Forecast, Hydro Event. Flood Statement, and River Point Zone
 * data, their associations and their behavior.
 * 
 * This class should be used as the API class between Python Scripts (Flood
 * Recommender) scripts and CAVE. Except for the River Flood Recommender, which
 * should use the RiverFloodRecommender object.
 * <p>
 * This class performs no setup queries upon instantiation. It does not actively
 * maintain queried data instances.
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

public class RiverForecastManager {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverForecastManager.class);

    /**
     * The maximum flood category a hazard can obtain.
     */
    public final static int MAX_CAT = 5;

    /**
     * "All Observed" typesource
     */
    public final static String ALL_OBSERVED_TYPESOURCE = "R*";

    /**
     * "All Forecast" typesource
     */

    public final static String ALL_FORECAST_TYPESOURCE = "F*";

    // AppDefaults Values
    /**
     * The number of hours to shift event end times for all forecast points.
     */
    private int shiftHoursForAllForecastPoints = Integer.MIN_VALUE;

    /**
     * max number of hours to look back for a river forecast basis time.
     */
    private int basisHoursForAllForecastPoints = Integer.MIN_VALUE;

    /**
     * The buffer around a reference stage.
     */
    private double defaultStageWindow = Double.MIN_VALUE;

    /**
     * The observation data look back hours for all forecast points.
     */
    private int lookBackHoursForAllForecastPoints = Integer.MIN_VALUE;

    /**
     * The forecast data look forward hours for all forecast points.
     */
    private int lookForwardHoursForAllForecastPoints = Integer.MIN_VALUE;

    private HazardSettings hazardSettings;

    /**
     * The flood recommender data accessor object.
     */
    private IFloodDAO floodDAO;

    /**
     * Default constructor
     */
    public RiverForecastManager() {
        this(FloodDAO.getInstance());
    }

    /**
     * Creates a river forecast Manager object.
     * 
     * @param floodDAO
     *            data accessor object
     */
    public RiverForecastManager(IFloodDAO floodDAO) {
        this.floodDAO = floodDAO;
    }

    /**
     * Retrieve default Hazard Settings values.
     * 
     * @return Default Hazard Settings
     */
    public HazardSettings getHazardSettings() {

        if (this.hazardSettings == null) {
            HazardSettings localHazardSettings = this.floodDAO
                    .retrieveSettings();
            if (localHazardSettings != null) {
                this.hazardSettings = localHazardSettings;
            }
        }

        return (this.hazardSettings);
    }

    /**
     * Retrieve the Hydrologic Service Area (HSA) ID.
     * 
     * This is configured as part of Hazard Settings.
     * 
     * @return Hydrologic Service Area (HSA) ID.
     */
    public String getHydrologicServiceAreaId() {
        HazardSettings localHazardSettings = this.getHazardSettings();
        String hsa = localHazardSettings.getHsa();

        return (hsa);
    }

    /**
     * Get a List of ALL RiverForecastGroup data.
     * 
     * This is a Variable Depth query.
     * 
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return List containing all RiverForcastGroup Data.
     */
    public List<RiverForecastGroup> getAllRiverForecastGroupList(
            boolean isSubDataNeeded) {
        List<RiverForecastGroup> riverForecastGroupList = null;

        riverForecastGroupList = floodDAO.queryAllRiverForecastGroup();

        if ((isSubDataNeeded == true) && (riverForecastGroupList != null)
                && (riverForecastGroupList.isEmpty() == false)) {
            for (RiverForecastGroup riverForecastGroup : riverForecastGroupList) {
                getRiverForecastGroupSubData(riverForecastGroup);
            }
        }
        return (riverForecastGroupList);
    }

    /**
     * Get a River Forecast Group for a given River Forecast Group Identifier
     * value.
     * 
     * This is a Variable Depth query
     * 
     * @param groupId
     *            RiverForecastGroup Group Identifier
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return RiverForecastGroup object
     */
    public RiverForecastGroup getRiverForecastGroup(String groupId,
            boolean isSubDataNeeded) {

        RiverForecastGroup riverForecastGroup = floodDAO
                .queryRiverForecastGroup(groupId);

        if ((isSubDataNeeded == true) && (riverForecastGroup != null)) {
            getRiverForecastGroupSubData(riverForecastGroup);

        }

        return (riverForecastGroup);
    }

    /**
     * Get a River Forecast Group parent for a RiverForecastPoint LID value.
     * 
     * This is a Variable Depth query.
     * 
     * @param lid
     *            RiverForecastPoint Identifier
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return RiverForecastGroup object
     */
    public RiverForecastGroup getRiverForecastGroupForRiverForecastPoint(
            String lid, boolean isSubDataNeeded) {

        RiverForecastGroup riverForecastGroup = floodDAO
                .queryRiverForecastGroupForLid(lid);
        if ((isSubDataNeeded == true) && (riverForecastGroup != null)) {
            getRiverForecastGroupSubData(riverForecastGroup);
        }

        return (riverForecastGroup);
    }

    /**
     * Get a List of ALL Hydrologic Service Area Id values.
     * 
     * This is a SHALLOW QUERY.
     * 
     * @return List containing all Hydrologic Service Area IDs.
     */
    public List<String> getHydrologicServiceAreaIdList() {

        return (floodDAO.queryHydrologicServiceAreaIdList());
    }

    /**
     * Get a List of RiverForecastPoint object for a given Hydrologic Service
     * Area (HSA) value.
     * 
     * This is a Variable Depth query.
     * 
     * @param hsaId
     *            Hydrological Service Area Identifier
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return List containing RiverForecastPoint data objects
     */
    public List<RiverForecastPoint> getHsaRiverForecastPointList(String hsaId,
            boolean isSubDataNeeded) {
        List<RiverForecastPoint> riverForecastPointList = null;

        List<String> hsaIdList = Lists.newArrayList(hsaId);
        riverForecastPointList = floodDAO.queryRiverForecastPointList(null,
                hsaIdList, null, null);
        if ((isSubDataNeeded == true) && (riverForecastPointList != null)
                && (riverForecastPointList.isEmpty() == false)) {
            for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
                getRiverForecastPointSubData(riverForecastPoint);
            }
        }
        return (riverForecastPointList);
    }

    /**
     * Get a List of RiverForecastPoint object for a given Hydrologic Service
     * Area (HSA) value.
     * 
     * This is a Variable Depth query.
     * 
     * @param hsaId
     *            Hydrological Service Area Identifier
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return List containing RiverForecastPoint data objects
     */
    public List<RiverForecastGroup> getHsaRiverForecastGroupList(String hsaId,
            boolean isSubDataNeeded) {
        List<RiverForecastGroup> forecastGroupList = null;

        List<String> groupIdList = floodDAO
                .queryHsaRiverForecastGroupIdList(hsaId);
        if ((groupIdList != null) && (groupIdList.isEmpty() == false)) {
            forecastGroupList = floodDAO.queryRiverForecastGroupList(
                    groupIdList, null);
            if ((isSubDataNeeded == true) && (forecastGroupList != null)
                    && (forecastGroupList.isEmpty() == false)) {
                for (RiverForecastGroup forecastGroup : forecastGroupList) {
                    getRiverForecastGroupSubData(forecastGroup);
                }
            }
        }
        return (forecastGroupList);
    }

    /**
     * Get a List of RiverForecastPoint objects for a given River Forecast Group
     * Id value.
     * 
     * This is a Variable Depth query
     * 
     * @param groupId
     *            RiverForecastGroup Group Identifier
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return List containing RiverForecastPoint data objects
     */
    public List<RiverForecastPoint> getGroupRiverForecastPointList(
            String groupId, boolean isSubDataNeeded) {
        List<RiverForecastPoint> riverForecastPointList = null;

        List<String> groupIdList = Lists.newArrayListWithExpectedSize(1);
        groupIdList.add(groupId);
        riverForecastPointList = floodDAO.queryRiverForecastPointList(null,
                null, groupIdList, null);
        if ((isSubDataNeeded == true) && (riverForecastPointList != null)
                && (riverForecastPointList.isEmpty() == false)) {
            for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
                getRiverForecastPointSubData(riverForecastPoint);
            }
        }

        return (riverForecastPointList);
    }

    /**
     * Get a List of RiverForecastPoint objects for a given list of LID values.
     * 
     * This is a Variable Depth query
     * 
     * @param lidList
     *            List of RiverForecastPoint Identifiers
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return List containing RiverForecastPoint data objects
     */
    public List<RiverForecastPoint> getRiverForecastPointList(
            List<String> lidList, boolean isSubDataNeeded) {
        List<RiverForecastPoint> riverForecastPointList = null;

        riverForecastPointList = floodDAO.queryRiverForecastPointList(lidList,
                null, null, null);
        if ((isSubDataNeeded == true) && (riverForecastPointList != null)
                && (riverForecastPointList.isEmpty() == false)) {
            for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
                getRiverForecastPointSubData(riverForecastPoint);
            }
        }
        return (riverForecastPointList);
    }

    /**
     * Get a single RiverForecastPoint object.
     * 
     * This is a Variable Depth query
     * 
     * @param lid
     *            RiverForecastPoint Identifier
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return RiverForecastPoint
     */
    public RiverForecastPoint getRiverForecastPoint(String lid,
            boolean isSubDataNeeded) {

        RiverForecastPoint riverForecastPoint = this.floodDAO
                .queryRiverForecastPoint(lid);

        if ((isSubDataNeeded == true) && (riverForecastPoint != null)) {
            getRiverForecastPointSubData(riverForecastPoint);
        }

        return (riverForecastPoint);
    }

    /**
     * Retrieve a list of RiverForecastPoint objects for a given, parent River
     * Forecast Group Identifier.
     * 
     * This is a Variable Depth query.
     * 
     * @param groupId
     *            RiverForecastGroup Group Identifier
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return List of RiverForecastPoint objects
     */
    public List<RiverForecastPoint> getRiverForecastGroupRiverForecastPointList(
            String groupId, boolean isSubDataNeeded) {

        List<String> groupIdList = Lists.newArrayList(groupId);
        List<RiverForecastPoint> riverForecastPointList = this.floodDAO
                .queryRiverForecastPointList(null, null, groupIdList, null);
        if ((isSubDataNeeded == true) && (riverForecastPointList != null)) {
            for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
                getRiverForecastPointSubData(riverForecastPoint);
            }
        }

        return (riverForecastPointList);
    }

    /**
     * Retrieve and set subdata for a RiverForecastGroup.
     * 
     * @param riverForecastGroup
     *            RiverForecastGroup to fill and compute data
     */
    protected void getRiverForecastGroupSubData(
            RiverForecastGroup riverForecastGroup) {

        List<RiverForecastPoint> riverForecastPointList = null;

        List<String> groupIdList = Lists.newArrayListWithExpectedSize(1);
        groupIdList.add(riverForecastGroup.getGroupId());
        riverForecastPointList = floodDAO.queryRiverForecastPointList(null,
                null, groupIdList, null);
        if ((riverForecastPointList != null)
                && (riverForecastPointList.isEmpty() == false)) {
            for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
                getRiverForecastPointSubData(riverForecastPoint);
            }
        }

        computeGroupMofo(riverForecastGroup, riverForecastPointList);
        riverForecastGroup.setForecastPointList(riverForecastPointList);
    }

    /**
     * Retrieve and set subdata for a RiverForecastPoint.
     * 
     * @param riverForecastPoint
     *            RiverForecastPoint to fill and compute data
     */
    protected void getRiverForecastPointSubData(
            RiverForecastPoint riverForecastPoint) {
        // TODO Make these configurable?
        boolean doHydrographForecastQuery = true;
        boolean doHydrographObservedQuery = true;

        long currentSystemTime = getSystemTime().getTime();

        // Not optional if any other computation is needed
        getRiverForecastCrestHistory(riverForecastPoint);

        if (doHydrographForecastQuery == true) {
            // Query and Compute Hydrograph Forecast Values
            // This map avoids re-querying the SHEF Forecast Basis Time List
            Map<Integer, List<Long>> hydrographForecastBasisTimeListMap = Maps
                    .newHashMap();
            getRiverForecastPointHydrographForecast(riverForecastPoint,
                    currentSystemTime, hydrographForecastBasisTimeListMap);
        }

        if (doHydrographObservedQuery == true) {
            // Query and Compute Hydrograph Observed Values
            getHydrographObservedData(riverForecastPoint, currentSystemTime);
            getRiverForecastPointCurrentObservation(riverForecastPoint,
                    currentSystemTime);
            computeHydrographObservedInfo(riverForecastPoint, currentSystemTime);
        }

        if ((doHydrographForecastQuery == true)
                && (doHydrographObservedQuery == true)) {

            /* find the times that the stage crested or passed thru flood stage */
            computeSpecialStages(riverForecastPoint, HydroDataType.OBS_DATA);

            /*
             * find the times that the stage crested or passed thru a flood
             * stage
             */
            computeSpecialStages(riverForecastPoint, HydroDataType.FCST_DATA);

            // Special Stages for BOTH Observed and Forecast must be computed
            // prior to calling computeHydrographRiseFall
            computeHydrographRiseFall(riverForecastPoint);

            /*
             * Recompute the obs and forecast point mofo info. Always recompute
             * the prev info in the event that the previous info changes
             * independent of new time series data loaded in.
             */
            computeForecastPointMofo(riverForecastPoint);

            /*
             * get the info on the trend data, which uses observed and forecast
             * data
             */
            computeTrendData(riverForecastPoint);

            /*
             * Set the load time for the next possible pass into this function.
             */
            riverForecastPoint.setFullTimeSeriesLoadedTime(new Date(
                    currentSystemTime));
        }
    }

    // This is how the old implementation relied on its data
    /**
     * Compute RiverForecastPoint Observation data values.
     * 
     * @param riverForecastPoint
     *            RiverForecastPoint to compute values for.
     * @param currentSystemTime
     *            Set System time for observation
     */
    public void getRiverForecastPointCurrentObservation(
            RiverForecastPoint riverForecastPoint, long currentSystemTime) {

        boolean obsFound = false;
        SHEFObserved obsReport = new SHEFObserved();

        // Check this out to make sure it is actually doing something.
        int previousTSRank = 99;
        long beginValidTime = currentSystemTime
                - this.getLookBackHoursForAllObservationPoints()
                * TimeUtil.MILLIS_PER_HOUR;
        long bestObsValidTime = Long.MIN_VALUE;

        /*
         * Initialize this forecast point's obs values to defaults.
         */
        SHEFObserved dummyCurrentSHEFObservation = new SHEFObserved();
        dummyCurrentSHEFObservation
                .setValue(RiverHydroConstants.MISSING_VALUE_DOUBLE);
        dummyCurrentSHEFObservation.setObsTime(Long.MIN_VALUE);
        dummyCurrentSHEFObservation.setTypeSource("");
        riverForecastPoint.setCurrentObservation(dummyCurrentSHEFObservation);
        riverForecastPoint
                .setCurrentObservationCategory(HydroFloodCategories.NULL_CATEGORY
                        .getRank());
        /*
         * In an effort to minimize reads of the database, get the RiverStatus
         * information all at once, for all ts's and for observed Data. There is
         * a validtime limit for observed data.
         */
        String lid = riverForecastPoint.getLid();
        String physicalElement = riverForecastPoint.getPhysicalElement();

        List<RiverStatus> riverStatusList = floodDAO.queryRiverStatusList(lid,
                physicalElement, beginValidTime, currentSystemTime);

        if ((riverStatusList != null) && (riverStatusList.size() > 0)) {
            /*
             * Retrieve a unique list of entries for typesources that match the
             * given lid and pe. The ingestfilter entries are needed because
             * they contain the type-source rank information. Insert a comma in
             * between the two fields to make them easier to parse. Note that
             * the ts rank sort method will not handle numbers greater than 9
             * (i.e. 12 is ranked higher than 3)! Also, note that the query does
             * not filter out extremums that are not "Z". Only bother retrieving
             * info if RiverStatus entries exist. We try and read RiverStatus
             * first since that table is smaller.
             */
            List<IngestFilterInfo> ingestFilterInfoList = floodDAO
                    .queryIngestSettings(lid, physicalElement);

            if ((ingestFilterInfoList != null)
                    && (ingestFilterInfoList.size() > 0)) {
                /*
                 * Loop on the observed entries and try to get the observed
                 * data. Note that processed data is grouped in with observed
                 * data searches.
                 */
                for (IngestFilterInfo ingestFilterInfo : ingestFilterInfoList) {
                    /*
                     * Extract the type source and rank currently being
                     * considered.
                     */
                    int useRank = ingestFilterInfo.getTsRank();
                    String useTS = ingestFilterInfo.getTypeSource();

                    /*
                     * Only process the observed type-sources entries, and only
                     * if the ts rank is the same as the previously checked ts
                     * or no obs data has been found at all yet.
                     */
                    if ((useTS.startsWith("R") || useTS.startsWith("P")
                            && ((useRank == previousTSRank) || !obsFound))) {
                        /*
                         * Loop on the river status entries for the observed
                         * value that matches the type-source being considered.
                         */
                        for (RiverStatus riverStatus : riverStatusList) {
                            /*
                             * Only use this river status entry if it is for the
                             * current ts and the validtime for the current ts
                             * is more recent than the previous validtime for ts
                             * with a possible matching rank.
                             */
                            long validTimeInMS = riverStatus.getValidTime();
                            String ts = riverStatus.getTypeSource();

                            if (ts.equals(useTS)
                                    && (validTimeInMS > bestObsValidTime)) {
                                obsReport.setPhysicalElement(riverStatus
                                        .getPhysicalElement().toString());
                                obsReport
                                        .setDuration(riverStatus.getDuration());
                                obsReport.setTypeSource(ts);
                                obsReport
                                        .setExtremum(riverStatus.getExtremum());
                                obsReport.setValue(riverStatus.getValue());
                                obsReport.setObsTime(validTimeInMS);

                                bestObsValidTime = validTimeInMS;

                                obsFound = true;
                                break;
                            }
                        }

                        previousTSRank = useRank;
                    }

                }
            }

        }

        /*
         * Check if there are new current observed data. If no data were found
         * then still try and load the full time series, if only to have it set
         * to missing. This strange condition could occur if the latest obs was
         * old and just barely in the time window at the time of the previous
         * retrieval but now is outside the window.
         */
        if (obsFound) {
            if (dummyCurrentSHEFObservation.getValue() != obsReport.getValue()
                    || dummyCurrentSHEFObservation.getObsTime() != obsReport
                            .getObsTime()) {
                riverForecastPoint.setCurrentObservation(obsReport);
            }
        }
    }

    /**
     * Retrieve and set Hydrograph Observed data for a RiverForecastPoint.
     * 
     * @param riverForecastPoint
     *            RiverForecastPoint to fill and compute data
     * @param currentSystemTime
     *            Current System observation time
     */
    public void getHydrographObservedData(
            RiverForecastPoint riverForecastPoint, long currentSystemTime) {

        String lid = riverForecastPoint.getLid();
        String physicalElement = riverForecastPoint.getPhysicalElement();

        /*
         * Get some default time window values for observed look back hours,
         * forecast look forward hours and basis time look back hours. This
         * information is already included as a part of the river forecast
         * point.
         */
        long obsLookBackHrs = riverForecastPoint.getBackHrs();
        if (obsLookBackHrs == RiverHydroConstants.MISSING_VALUE) {
            obsLookBackHrs = getLookBackHoursForAllObservationPoints();
        }

        long obsBeginTime = currentSystemTime
                - (obsLookBackHrs * TimeUtil.MILLIS_PER_HOUR);
        long obsEndTime = currentSystemTime;

        // Get The "Best Type Source" Value

        // Query for River Forecast Point : Hydrograph Observed and Sub
        // SHEFObserved data
        HydrographObserved hydrographObserved = getHydrographObserved(lid,
                physicalElement, null, obsBeginTime, obsEndTime);

        riverForecastPoint.setHydrographObserved(hydrographObserved);
    }

    /**
     * Retrieve Hydrograph Observed data for the given parameters.
     * 
     * @param lid
     *            RiverForecastPoint Identifier
     * @param physicalElement
     *            Physical Element identifier
     * @param typeSource
     *            Type Source for the Physical Element
     * @param obsBeginTime
     *            Time (in mils) of Observation Start
     * @param obsEndTime
     *            Time (in mils) of Observation End
     */
    public HydrographObserved getHydrographObserved(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime) {
        // obsEndTime is also currentSystemTime

        HydrographObserved hydrographObserved = floodDAO
                .queryRiverPointHydrographObserved(lid, physicalElement,
                        typeSource, obsBeginTime, obsEndTime);

        return (hydrographObserved);
    }

    /**
     * Retrieve and set Hydrograph Forecast data for a RiverForecastPoint.
     * 
     * @param riverForecastPoint
     *            RiverForecastPoint to fill and compute data
     * @param currentSystemTime
     *            Current System observation time
     * @param hydrographForecastBasisTimeListMap
     *            List of valid basis time values
     */
    public void getRiverForecastPointHydrographForecast(
            RiverForecastPoint riverForecastPoint, long currentSystemTime,
            Map<Integer, List<Long>> hydrographForecastBasisTimeListMap) {

        String lid = riverForecastPoint.getLid();
        String physicalElement = riverForecastPoint.getPhysicalElement();
        /*
         * Get some default time window values for observed look back hours,
         * forecast look forward hours and basis time look back hours. This
         * information is already included as a part of the river forecast
         * point.
         */
        long fcstLookForwardHrs = riverForecastPoint.getForwardHrs();
        if (fcstLookForwardHrs == RiverHydroConstants.MISSING_VALUE) {
            fcstLookForwardHrs = getBasisHoursForAllForecastPoints();
        }
        long basisFcstHours = getBasisHoursForAllForecastPoints();
        long fcstEndtime = currentSystemTime
                + (TimeUtil.MILLIS_PER_HOUR * fcstLookForwardHrs);
        long basisBeginTime = currentSystemTime
                - (TimeUtil.MILLIS_PER_HOUR * basisFcstHours);

        /*
         * Get the setting for the use_latest_fcst field for the current
         * location.
         */
        boolean useLatestForecast = riverForecastPoint.getUseLatestForecast();

        HydrographForecast hydrographForecast = getHydrographForecast(lid,
                physicalElement, currentSystemTime, fcstEndtime,
                basisBeginTime, useLatestForecast);
        riverForecastPoint.setHydrographForecast(hydrographForecast);

        /* get the info on the observed stage data for forecast points */
        computeHydrographForecastInfo(riverForecastPoint, currentSystemTime);
    }

    /**
     * Retrieve Hydrograph Forecast data for the given parameters.
     * 
     * @param lid
     *            RiverForecastPoint Identifier
     * @param physicalElement
     *            Physical Element identifier
     * @param currentSystemTime
     *            Current System time
     * @param fcstEndtime
     *            Time (in mils) of Forecast End
     * @param basisBeginTime
     *            Time (in mils) of Forecast Basis Begin Time
     * @param useLatestForecast
     *            Flag to use Only the Latest Forecast value when true
     */
    public HydrographForecast getHydrographForecast(String lid,
            String physicalElement, long currentSystemTime, long fcstEndtime,
            long basisBeginTime, boolean useLatestForecast) {

        HydrographForecast hydrographForecast = floodDAO
                .queryRiverPointHydrographForecast(lid, physicalElement,
                        currentSystemTime, fcstEndtime, basisBeginTime,
                        useLatestForecast);

        return (hydrographForecast);
    }

    public RiverStationInfo getRiverForecastPointRiverStationInfo(String lid) {
        RiverStationInfo riverStationInfo = null;
        List<String> lidList = Lists.newArrayList(lid);

        List<RiverStationInfo> riverStationInfoList = this.floodDAO
                .queryRiverStationInfoList(lidList);
        if ((riverStationInfoList != null)
                && (riverStationInfoList.isEmpty() == false)) {
            riverStationInfo = riverStationInfoList.get(0);
        }

        return (riverStationInfo);
    }

    /**
     * Get a List of RiverStationInfo objects for a given list of
     * RiverForecastPoint LID values.
     * 
     * @param lidList
     *            RiverForecastPoint Identifier list of values
     * @return List of RiverStationInfo objects
     */
    public List<RiverStationInfo> getRiverForecastPointRiverStationInfoList(
            List<String> lidList) {

        return (this.floodDAO.queryRiverStationInfoList(lidList));
    }

    /**
     * Get a List of RiverStatus objects for a given RiverForecastPoint LID and
     * Physical Element value.
     * 
     * @param lid
     *            RiverForecastPoint Identifier value
     * @param physicalElement
     *            PhysicalElement value
     * @return List of RiverStatus objects
     */
    public List<RiverStatus> getRiverForecastPointRiverStatus(String lid,
            String physicalElement) {

        List<RiverStatus> riverStatusList = this.floodDAO.queryRiverStatusList(
                lid, physicalElement);

        return (riverStatusList);
    }

    /**
     * Get a List of CountyStateData objects for a given list of
     * RiverForecastPoint LID values.
     * 
     * @param lidList
     *            RiverForecastPoint Identifier list of values
     * @return List of CountyStateData objects
     */
    public List<CountyStateData> getRiverForecastPointCountyStateList(
            List<String> lidList) {

        List<CountyStateData> countyStateDataList = this.floodDAO
                .queryCountyStateDataList(lidList);

        return (countyStateDataList);
    }

    /**
     * Get a CountyStateData object for a given RiverForecastPoint LID value.
     * 
     * @param lid
     *            RiverForecastPoint Identifier
     * @return CountyStateData object
     */
    public CountyStateData getRiverForecastPointCountyState(String lid) {

        CountyStateData countyStateData = this.floodDAO
                .queryCountyStateData(lid);

        return (countyStateData);
    }

    /**
     * Get a list of Impact value, Impact detail pairs for a River Forecast
     * Point and given month and day.
     * 
     * @param lid
     *            RiverForecastPoint Identifier
     * @param month
     *            Numeric month
     * @param day
     *            Numeric day of month
     * @return List of Impact Pairs (Value,Detail)
     */
    public List<Pair<Double, String>> getImpactsDataList(String lid, int month,
            int day) {
        List<Pair<Double, String>> impactPairList = null;

        List<FloodStmtData> floodStmtDataList = this.floodDAO
                .queryFloodStatementDataList(lid, month, day);
        if ((floodStmtDataList != null)
                && (floodStmtDataList.isEmpty() == false)) {

            impactPairList = Lists
                    .newArrayListWithExpectedSize(floodStmtDataList.size());
            for (FloodStmtData floodStmtData : floodStmtDataList) {
                Pair<Double, String> impactPair = floodStmtData.getImpactPair();
                impactPairList.add(impactPair);
            }
        } else {
            impactPairList = Lists.newArrayListWithExpectedSize(0);
        }

        return (impactPairList);
    }

    /**
     * Get a List of Flood Statement FloodStmtData objects for a River Forecast
     * Point and given month and day.
     * 
     * @param lid
     *            RiverForecastPoint Identifier
     * @param month
     *            Numeric month
     * @param day
     *            Numeric day of month
     * @return List of Flood Statement objects
     */
    public List<FloodStmtData> getFloodStatementDataList(String lid, int month,
            int day) {
        List<FloodStmtData> floodStmtDataList = this.floodDAO
                .queryFloodStatementDataList(lid, month, day);

        return (floodStmtDataList);
    }

    /**
     * Retrieve a RiverForecastZoneInfo object for the given LID (Point Id)
     * 
     * @param lid
     *            River Forecast Point Id (LID)
     * @return River Forecast Zone Info
     */
    public RiverPointZoneInfo getRiverForecastPointRiverZoneInfo(String lid) {
        return (this.floodDAO.queryRiverPointZoneInfo(lid));
    }

    /**
     * Determines various values that use both observed and forecast stage data
     * for each forecast point. The information is defined in terms of index
     * values that refer to the specific item in the stage data structure. Pre
     * Conditions: The River Point Forecast must have successfully queried its
     * Hydrograph Forecast Data, determined the Maximum Forecast from the
     * Forecast Time Series and set the values in the parent RiverForecastPoint
     * <code>computeHydrographForecastInfo</code>. The River Poind Forecast must
     * have also queried for its Hydrograph Observed Data and determined its
     * Current Observed value from the Observed Time Series and set the values
     * in the parent RiverForecastPoint
     * <code>computeHydrographObservedInfo</code>.
     * 
     * @param riverForecastPoint
     *            A fully queried RiverForecastPoint to compute data for
     */
    private void computeForecastPointMofo(RiverForecastPoint riverForecastPoint) {
        /* initialize */
        riverForecastPoint
                .setMaximumObservedForecastValue(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setMaximumObservedForecastCategory(HydroFloodCategories.NULL_CATEGORY
                        .getRank());
        riverForecastPoint.setMaximumObservedForecastTime(null);

        /*
         * get the omf value and category values; this check works even if one
         * of the values are missing
         */
        SHEFForecast maximumSHEFForecast = riverForecastPoint
                .getMaximumSHEFForecast();

        SHEFObserved currentSHEFObservation = riverForecastPoint
                .getCurrentObservation();

        if ((currentSHEFObservation != null) && (maximumSHEFForecast != null)) {
            if ((currentSHEFObservation.getValue() != RiverHydroConstants.MISSING_VALUE)
                    || (maximumSHEFForecast.getValue() != RiverHydroConstants.MISSING_VALUE)) {
                if (currentSHEFObservation.getValue() > maximumSHEFForecast
                        .getValue()) {
                    riverForecastPoint
                            .setMaximumObservedForecastValue(currentSHEFObservation
                                    .getValue());

                    int maxObservedForecastCategory = computeFloodStageCategory(
                            riverForecastPoint,
                            currentSHEFObservation.getValue());
                    riverForecastPoint
                            .setMaximumObservedForecastCategory(maxObservedForecastCategory);
                    riverForecastPoint.setMaximumObservedForecastTime(new Date(
                            currentSHEFObservation.getObsTime()));
                } else {
                    riverForecastPoint
                            .setMaximumObservedForecastValue(maximumSHEFForecast
                                    .getValue());
                    int maxObservedForecastCategory = computeFloodStageCategory(
                            riverForecastPoint, maximumSHEFForecast.getValue());
                    riverForecastPoint
                            .setMaximumObservedForecastCategory(maxObservedForecastCategory);
                    riverForecastPoint.setMaximumObservedForecastTime(new Date(
                            maximumSHEFForecast.getValidTime()));
                }
            }
        } else if (maximumSHEFForecast != null) {
            riverForecastPoint
                    .setMaximumObservedForecastValue(maximumSHEFForecast
                            .getValue());
            int maxObservedForecastCategory = computeFloodStageCategory(
                    riverForecastPoint, maximumSHEFForecast.getValue());
            riverForecastPoint
                    .setMaximumObservedForecastCategory(maxObservedForecastCategory);
            riverForecastPoint.setMaximumObservedForecastTime(new Date(
                    maximumSHEFForecast.getValidTime()));
        } else if (currentSHEFObservation != null) {
            riverForecastPoint
                    .setMaximumObservedForecastValue(currentSHEFObservation
                            .getValue());

            int maxObservedForecastCategory = computeFloodStageCategory(
                    riverForecastPoint, currentSHEFObservation.getValue());
            riverForecastPoint
                    .setMaximumObservedForecastCategory(maxObservedForecastCategory);
            riverForecastPoint.setMaximumObservedForecastTime(new Date(
                    currentSHEFObservation.getObsTime()));
        }
    }

    /**
     * Determine the stage category for a given stage value for a single
     * forecast point for general CAT threshold mode. Because of the system,
     * sometimes the float number is stored not exactly as it is shown. For
     * example, 23.1 is stored as 23.1000004. If the absolute difference between
     * cat_vals[i] and dataValue is very small then they are considered equal.
     * 
     * @param riverForecastPoint
     *            A RiverForecastPoint object
     * @param dataValue
     * @return
     */
    public int computeFloodStageCategory(RiverForecastPoint riverForecastPoint,
            double dataValue) {
        int cat;

        cat = 0;

        if (dataValue == RiverHydroConstants.MISSING_VALUE_DOUBLE) {
            cat = HydroFloodCategories.NULL_CATEGORY.getRank();
        } else {
            double[] floodCategory = riverForecastPoint.getFloodCategory();
            for (int i = 1; ((i < MAX_CAT) && (i < floodCategory.length)); ++i) {
                if (((dataValue >= floodCategory[i]) || (Math.abs(dataValue
                        - floodCategory[i]) < 0.0001))
                        && floodCategory[i] != RiverHydroConstants.MISSING_VALUE) {
                    cat = i;
                }
            }
        }

        return cat;
    }

    /**
     * Determines the derived values for the observed stage data for each
     * forecast point. The information is defined in terms of index values that
     * refer to the specific item in the stage data structure.
     * 
     * The fields based on the previous product information are computed
     * elsewhere.
     * 
     * @param riverForecastPoint
     *            A fully queried RiverForecastPoint object
     * @param currentSystemTime
     *            Current System Time (in mils)
     */
    private void computeHydrographObservedInfo(
            RiverForecastPoint riverForecastPoint, long currentSystemTime) {

        riverForecastPoint
                .setObservedCurrentIndex(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setObservedMaximumIndex(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setObservedMax24Index(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setObservedMax06Index(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setObservedFloodStageDeparture(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setObservedRiseAboveTime(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setObservedFallBelowTime(RiverHydroConstants.MISSING_VALUE);
        riverForecastPoint
                .setObservedCrestValue(RiverHydroConstants.MISSING_VALUE_DOUBLE);
        riverForecastPoint
                .setObservedCrestTime(RiverHydroConstants.MISSING_VALUE);

        boolean isPhysicalElementQ = false;
        if (riverForecastPoint.getPhysicalElement().startsWith("Q")) {
            isPhysicalElementQ = true;
        }

        HydrographObserved hydrographObserved = riverForecastPoint
                .getHydrographObserved();
        List<SHEFObserved> shefObservedList = hydrographObserved
                .getShefHydroDataList();
        int maximumObservedValueIndex = this
                .getMaxShefValueIndex(hydrographObserved);
        SHEFObserved maximumSHEFObserved = null;
        if (maximumObservedValueIndex >= 0) {
            maximumSHEFObserved = shefObservedList
                    .get(maximumObservedValueIndex);
        }
        riverForecastPoint.setObservedMaximumIndex(maximumObservedValueIndex);
        riverForecastPoint.setMaximumSHEFObserved(maximumSHEFObserved);

        int latestObservedObsTimeIndex = getLatestValidTimeIndex(hydrographObserved);
        SHEFObserved latestObservedValidTime = null;
        double latestObservedValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        if (latestObservedObsTimeIndex != RiverHydroConstants.MISSING_VALUE) {
            latestObservedValidTime = shefObservedList
                    .get(latestObservedObsTimeIndex);
            latestObservedValue = latestObservedValidTime.getValue();
            riverForecastPoint.setCurrentObservation(latestObservedValidTime);
        }
        int mostCurrentObservationCatRank = this.computeFloodStageCategory(
                riverForecastPoint, latestObservedValue);
        riverForecastPoint
                .setCurrentObservationCategory(mostCurrentObservationCatRank);

        if (isPhysicalElementQ == true) {
            if (riverForecastPoint.getFloodFlow() != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                riverForecastPoint
                        .setObservedFloodStageDeparture(latestObservedValue
                                - riverForecastPoint.getFloodFlow());
            }
        } else {
            if (riverForecastPoint.getFloodStage() != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                riverForecastPoint
                        .setObservedFloodStageDeparture(latestObservedValue
                                - riverForecastPoint.getFloodStage());
            }
        }

        /* get the max value for 6 and 24 hour periods */
        computeObservedStageInInterval(riverForecastPoint, 6L,
                currentSystemTime);
        computeObservedStageInInterval(riverForecastPoint, 24L,
                currentSystemTime);

        long obsBeginTime = hydrographObserved.getObsBeginTime();
        riverForecastPoint.setObsLoadTime(new Date(obsBeginTime));

        /*
         * Filter out old data if VTEC enabled for any VTEC significance and
         * data found. Otherwise, always use all obs data.
         * 
         * Skipped ... this is product-centric. May need to revisit this as this
         * seems to be the part where the obs timeseries is trimmed if there is
         * a previous product.
         */
        riverForecastPoint.setObsCutoffTime(new Date(obsBeginTime));

        return;
    }

    /**
     * Determines the various aspects of the forecast stage data for each
     * forecast point. The information is defined in terms of index values that
     * refer to the specific item in the stage data structure.
     * 
     * @param riverForecastPoint
     *            A fully queried RiverForecastPoint object
     * @param currentSystemTime
     *            Current System Time (in mils)
     */
    // compute_fcst_info
    private void computeHydrographForecastInfo(
            RiverForecastPoint riverForecastPoint, long currentSystemTime) {

        /* initialize all the forecast point stage data */

        int maximumForecastIndex = RiverHydroConstants.MISSING_VALUE;
        int shefForecastListSize = -1;
        List<SHEFForecast> shefForecastList = null;

        HydrographForecast hydrographForecast = riverForecastPoint
                .getHydrographForecast();
        if (hydrographForecast != null) {
            shefForecastList = hydrographForecast.getShefHydroDataList();
            if (shefForecastList != null) {
                shefForecastListSize = shefForecastList.size();
            }
        }

        /* if forecast values available for this point, process the data */
        if (shefForecastListSize > 0) {
            /*
             * loop on the number of forecasts for the current forecast point.
             * if the stage being checked exceeds the previous maximum, then
             * reset the maximum.
             */

            double maxForecastStageValue = RiverHydroConstants.MISSING_VALUE;
            long maxForecastBasisTime = RiverHydroConstants.MISSING_VALUE;
            for (int k = 0; k < shefForecastListSize; k++) {
                SHEFForecast shefForecast = shefForecastList.get(k);
                double forecastStageValue = shefForecast.getValue();
                if (forecastStageValue != RiverHydroConstants.MISSING_VALUE) {
                    if ((maxForecastStageValue == RiverHydroConstants.MISSING_VALUE_DOUBLE)
                            || (forecastStageValue > maxForecastStageValue)) {
                        maxForecastStageValue = forecastStageValue;
                        maximumForecastIndex = k;
                    }
                }
                long forecastBasisTime = shefForecast.getBasisTime();
                if (forecastBasisTime != RiverHydroConstants.MISSING_VALUE) {
                    if ((maxForecastBasisTime == RiverHydroConstants.MISSING_VALUE)
                            || (forecastBasisTime > maxForecastBasisTime)) {
                        maxForecastBasisTime = forecastBasisTime;
                    }
                }
            }

            SHEFForecast maximumSHEFForecast = shefForecastList
                    .get(maximumForecastIndex);
            riverForecastPoint.setMaximumSHEFForecast(maximumSHEFForecast);
            riverForecastPoint.setMaximumForecastIndex(maximumForecastIndex);

            int floodStageCategory = this.computeFloodStageCategory(
                    riverForecastPoint, maximumSHEFForecast.getValue());
            riverForecastPoint.setMaximumForecastCategory(floodStageCategory);

            if (riverForecastPoint.getPhysicalElement().startsWith("Q")) {
                if (riverForecastPoint.getFloodFlow() != RiverHydroConstants.MISSING_VALUE
                        && maxForecastStageValue != RiverHydroConstants.MISSING_VALUE) {
                    riverForecastPoint
                            .setForecastFloodStageDeparture(maxForecastStageValue
                                    - riverForecastPoint.getFloodFlow());
                }
            } else {
                if (riverForecastPoint.getFloodStage() != RiverHydroConstants.MISSING_VALUE
                        && maxForecastStageValue != RiverHydroConstants.MISSING_VALUE) {
                    riverForecastPoint
                            .setForecastFloodStageDeparture(maxForecastStageValue
                                    - riverForecastPoint.getFloodStage());
                }
            }

            /*
             * find index fcst_xrcrest_index which is the data of either FFX
             * data in the most recent basis time if exist or same as
             * fcst_crest_index.
             */
            long minValidTime = Long.MAX_VALUE;
            for (int k = 0; k < shefForecastListSize; k++) {
                SHEFForecast shefForecast = shefForecastList.get(k);
                long basisTime = shefForecast.getBasisTime();

                if (basisTime == maxForecastBasisTime) {
                    /*
                     * look for the earliest forecast data with extremum as X
                     */
                    double value = shefForecast.getValue();
                    long validTime = shefForecast.getValidTime();
                    char extremum = shefForecast.getExtremum();

                    if ((validTime < minValidTime)
                            && (validTime != RiverHydroConstants.MISSING_VALUE)
                            && (extremum == 'X')
                            && (value != RiverHydroConstants.MISSING_VALUE)) {
                        minValidTime = validTime;
                        riverForecastPoint.setForecastXfCrestIndex(k);
                    }
                }
            }
        }
    }

    /**
     * Retrieves a maximum observed stage within an given interval of hours.
     * 
     * @param riverForecastPoint
     *            A fully queried RiverForecastPoint object
     * @param interval
     *            interval in hours
     * @param currentSystemTime
     *            Current System Time (in mils)
     */
    private void computeObservedStageInInterval(
            RiverForecastPoint riverForecastPoint, long interval,
            long currentSystemTime) {
        /* initialize */
        int maxObservedIndex = RiverHydroConstants.MISSING_VALUE;
        double maxObservedValue = RiverHydroConstants.MISSING_VALUE;

        /*
         * define the beginning of the time window based on the current time and
         * the hour interval to look back
         */
        long intervalInMils = interval * TimeUtil.MILLIS_PER_HOUR;
        long intervalBeginTime = currentSystemTime - intervalInMils;

        /*
         * loop thru all the stage values in the time series. set the start
         * index, recognizing the order and usage specs of the data
         */
        List<SHEFObserved> shefObservedList = riverForecastPoint
                .getHydrographObserved().getShefHydroDataList();
        int shefObservedListSize = shefObservedList.size();
        for (int i = 0; i < shefObservedListSize; i++) {
            SHEFObserved shefObserved = shefObservedList.get(i);
            double obsValue = shefObserved.getValue();
            long obsTime = shefObserved.getObsTime();

            if ((obsValue != RiverHydroConstants.MISSING_VALUE)
                    && (obsTime != RiverHydroConstants.MISSING_VALUE)) {
                if ((obsTime - intervalBeginTime) > 0) {
                    if ((maxObservedIndex == RiverHydroConstants.MISSING_VALUE)
                            || (obsValue >= maxObservedValue)) {
                        maxObservedValue = obsValue;
                        maxObservedIndex = i;
                    }
                }
            }
        }

        /* load the values in depending upon the time interval being considered */
        if (interval == 6) {
            riverForecastPoint.setObservedMax06Index(maxObservedIndex);
        } else if (interval == 24) {
            riverForecastPoint.setObservedMax24Index(maxObservedIndex);
        }

    }

    /**
     * Determines special flood stage values for a time series, this includes
     * the rise-above-flood, the fall-below-flood and crest. This function needs
     * to know whether it is dealing with observed or forecast values.
     * 
     * @param riverForecastPoint
     *            A fully queried RiverForecastPoint object
     * @param obs_or_fcst
     *            OBS_DATA or FCST_DATA
     */
    // Was load_special_stages
    void computeSpecialStages(RiverForecastPoint riverForecastPoint,
            HydroDataType obsOrFcst) {
        long riseAboveTime = RiverHydroConstants.MISSING_VALUE;
        long fallBelowTime = RiverHydroConstants.MISSING_VALUE;
        double floodLevel = Double.MIN_VALUE;
        int numVals = 0;
        int numObs = 0;
        int numFcst = 0;
        double stage = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        double prevStage = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        int startIndex = 0;
        int crestIndex = RiverHydroConstants.MISSING_VALUE;
        int sustainedCrestIndex = RiverHydroConstants.MISSING_VALUE;
        HydroGraphTrend prevTrend = HydroGraphTrend.MISSING;
        HydroGraphTrend curTrend = HydroGraphTrend.MISSING;
        boolean crestFound = false;
        boolean riseFound = false;
        long stageTime = 0L;
        long prevStageTime = 0L;
        double[] temp_value;
        long[] temp_timet;

        /* can't do anything if not enough data */
        HydrographForecast hydrographForecast = riverForecastPoint
                .getHydrographForecast();
        if (hydrographForecast == null) {
            return;
        }
        List<SHEFForecast> shefForecastList = hydrographForecast
                .getShefHydroDataList();
        int shefForecastListSize = shefForecastList.size();

        HydrographObserved hydrographObserved = riverForecastPoint
                .getHydrographObserved();
        if (hydrographObserved == null) {
            return;
        }
        List<SHEFObserved> shefObservedList = hydrographObserved
                .getShefHydroDataList();
        int shefObservedListSize = shefObservedList.size();

        if (((obsOrFcst == HydroDataType.OBS_DATA) && (shefForecastListSize == 0))
                || ((obsOrFcst == HydroDataType.FCST_DATA) && (shefObservedListSize == 0))) {
            return;
        }

        /* load the flood stage/discharge into convenient variable. */

        if (riverForecastPoint.getPhysicalElement().startsWith("Q")) {
            floodLevel = riverForecastPoint.getFloodFlow();
        } else {
            floodLevel = riverForecastPoint.getFloodStage();
        }

        /* build a convenient time series -------------------------------- */

        /*
         * for obs, it only includes the portion of the time series to use,
         * which is constrained possibly by any preceding VTEC event, plus the
         * first of any forecast time series as the last value. for fcst, it
         * includes the full fcst time series, plus the latest obs value, if it
         * exists, as the first value. numVals is the total number of values in
         * the temporary time series. numObs is the number of additional obs
         * values in the fcst time series, which at most is one more,
         * representing the latest obs. numFcst is the number of additional fcst
         * value in the obs time series, which at most is one more, representing
         * the first forecast.
         */

        if (obsOrFcst == HydroDataType.OBS_DATA) {
            if (shefForecastListSize > 0) {
                numFcst = 1;
            } else {
                numFcst = 0;
            }

            numVals = shefObservedListSize + numFcst;
        }

        else {
            if (shefObservedListSize > 0) {
                numObs = 1;
            } else {
                numObs = 0;
            }

            numVals = shefForecastListSize + numObs;
        }

        temp_value = new double[numVals];
        temp_timet = new long[numVals];

        /* load the temp data into the convenient arrays. */

        if (obsOrFcst == HydroDataType.OBS_DATA) {
            startIndex = 0;

            for (int i = 0; i < shefObservedListSize; i++) {
                temp_value[i] = shefObservedList.get(startIndex + i).getValue();
                temp_timet[i] = shefObservedList.get(startIndex + i)
                        .getObsTime();
            }

            if (numFcst > 0) {
                temp_value[numVals - 1] = shefForecastList.get(0).getValue();
                temp_timet[numVals - 1] = shefForecastList.get(0)
                        .getValidTime();
            }
        } else {
            if (numObs > 0) {
                startIndex = shefObservedListSize - 1;

                temp_value[0] = shefObservedList.get(startIndex).getValue();
                temp_timet[0] = shefObservedList.get(startIndex).getObsTime();
            }

            for (int i = 0; i < shefForecastListSize; i++) {
                temp_value[numObs + i] = shefForecastList.get(i).getValue();
                temp_timet[numObs + i] = shefForecastList.get(i).getValidTime();
            }
        }

        /* compute the crest value ------------------------------------ */

        /*
         * loop on the number of stage values in chronological order. note that
         * the order of the looping and how the if checks affect multiple
         * crests. also note that in the event of a sustained stage at flood
         * stage, the pass thru stage time is the last in the series of
         * sustained values, not the first.
         */

        for (int i = 1; i < numVals; i++) {
            prevStage = temp_value[i - 1];
            prevStageTime = temp_timet[i - 1];

            stage = temp_value[i];
            stageTime = temp_timet[i];

            /* perform the trend check on the very first pair of values. */

            if (i == 1) {
                if (stage > prevStage) {
                    prevTrend = HydroGraphTrend.RISE;
                } else if (stage == prevStage) {
                    prevTrend = HydroGraphTrend.UNCHANGED;
                } else {
                    prevTrend = HydroGraphTrend.FALL;
                }

            }

            /*
             * check if the value has crested; this method defines a crest as a
             * maximum surrounded by values that are either equal to or below.
             * use the idea of tracking the trend of the stage to find a crest;
             * the method allows for detection of sustained crests.
             */

            else {
                /*
                 * this check ensures that the crests, whether forecast or
                 * observed, are the crests that occur closest to the current
                 * time.
                 */

                if (obsOrFcst == HydroDataType.OBS_DATA || (!crestFound)) {
                    /* determine the current trend for later use */

                    if (stage > prevStage) {
                        curTrend = HydroGraphTrend.RISE;
                    } else if (stage == prevStage) {
                        curTrend = HydroGraphTrend.UNCHANGED;
                    } else {
                        curTrend = HydroGraphTrend.FALL;
                    }

                    /*
                     * adjust the current trend value from unchanged to rise if
                     * the previous trend was a rise; this allows for the
                     * detection of sustained crests. also define the beginning
                     * of the sustained crest in the event that it undefined
                     */

                    if (curTrend == HydroGraphTrend.UNCHANGED
                            && prevTrend == HydroGraphTrend.RISE) {
                        curTrend = HydroGraphTrend.RISE;

                        if (sustainedCrestIndex == (int) RiverHydroConstants.MISSING_VALUE) {
                            sustainedCrestIndex = i - 1;
                        }
                    } else if (curTrend == HydroGraphTrend.RISE) {
                        sustainedCrestIndex = (int) RiverHydroConstants.MISSING_VALUE;
                    }

                    /*
                     * assign the crest index where a crest occurs if the
                     * previous trend was a rise and the current trend is a fall
                     */

                    if (prevTrend == HydroGraphTrend.RISE
                            && curTrend == HydroGraphTrend.FALL) {
                        if (sustainedCrestIndex == (int) RiverHydroConstants.MISSING_VALUE) {
                            crestIndex = i - 1;
                        } else {
                            crestIndex = sustainedCrestIndex;
                        }

                        crestFound = true;
                    }

                    /*
                     * set the previous trend to be the current trend in
                     * preparation for the next time thru the loop
                     */

                    prevTrend = curTrend;

                } /* end of if check on whether observed or crest_found */
            } /* working on second pair */
        } /* end of for loop */

        /* load the crest value and time */

        if (obsOrFcst == HydroDataType.OBS_DATA) {
            if (crestIndex != RiverHydroConstants.MISSING_VALUE) {
                riverForecastPoint
                        .setObservedCrestValue(temp_value[crestIndex]);
                riverForecastPoint.setObservedCrestTime(temp_timet[crestIndex]);
            } else {
                riverForecastPoint
                        .setObservedCrestValue(RiverHydroConstants.MISSING_VALUE);
                riverForecastPoint
                        .setObservedCrestTime(RiverHydroConstants.MISSING_VALUE);
            }
        } else {
            if (crestIndex != RiverHydroConstants.MISSING_VALUE) {
                riverForecastPoint
                        .setForecastCrestValue(temp_value[crestIndex]);
                riverForecastPoint.setForecastCrestTime(temp_timet[crestIndex]);
            } else {
                riverForecastPoint
                        .setForecastCrestValue(RiverHydroConstants.MISSING_VALUE);
                riverForecastPoint
                        .setForecastCrestTime(RiverHydroConstants.MISSING_VALUE);
            }
        }

        /* compute the pass thru flood times ------------------------------ */

        /*
         * if flood stage not defined, force it to skip over the section below,
         * and return with the initialized missing values.
         */

        if (floodLevel == RiverHydroConstants.MISSING_VALUE) {
            numVals = 0;
        }

        /*
         * loop on the values in chronological order. note that the order of the
         * looping and the if checks affect how multiple crests or pass thru
         * events are handled. also note that in the event of a sustained stage
         * at flood stage, the pass thru stage time is the last in the series of
         * sustained values, not the first
         */

        for (int i = 1; i < numVals; i++) {
            prevStage = temp_value[i - 1];
            prevStageTime = temp_timet[i - 1];

            stage = temp_value[i];
            stageTime = temp_timet[i];

            /*
             * check if the stage value has risen above the flood stage; compute
             * the time of stage via interpolation. if multiple rises, set the
             * rise to be the one nearest the current time, for both observed
             * and forecast data. for the rise above check, it is considered a
             * rise above even if it only hits the flood stage precisely. for
             * obs data, do not assign a rise above if it is based on the
             * appended last forecast value, since this type of rise should be
             * associated with the forecast rise above.
             */

            if (prevStage < floodLevel && floodLevel <= stage) {
                if ((obsOrFcst == HydroDataType.OBS_DATA && (numFcst == 0 || i != (numVals - 1)))
                        || (obsOrFcst == HydroDataType.FCST_DATA && !riseFound)) {
                    riseFound = true;

                    if (prevStage == stage) {
                        riseAboveTime = prevStageTime;
                    } else {
                        riseAboveTime = stageTime
                                - (long) (((stage - floodLevel) / (stage - prevStage)) * (stageTime - prevStageTime));
                    }
                }
            }

            /*
             * check if the stage value has passed below the flood stage. for
             * both obs and fcst, always use the latest fall below. for the fall
             * below check, it is considered a fall below only if it truly falls
             * below the flood level. for obs data, do not assign a fall below
             * if it is based on the appended last forecast value, since this
             * type of fall should be associated with the forecast rise above.
             */

            if (prevStage >= floodLevel && floodLevel > stage) {
                if ((obsOrFcst == HydroDataType.OBS_DATA && (numFcst == 0 || i != (numVals - 1)))
                        || (obsOrFcst == HydroDataType.FCST_DATA)) {

                    if (prevStage == stage) {
                        fallBelowTime = prevStageTime;
                    } else {
                        fallBelowTime = stageTime
                                - (long) (((stage - floodLevel) / (stage - prevStage)) * (stageTime - prevStageTime));
                    }
                }
            }
        }

        /*
         * have special check for forecast time series ending above flood stage
         * with there being a fall below time, because of a prior period above
         * flood stage. In this case, make the fall below undefined to reflect
         * the uncertainty.
         */
        if (numVals >= 1 && obsOrFcst == HydroDataType.FCST_DATA) {
            stage = temp_value[numVals - 1];

            if (stage > floodLevel) {
                fallBelowTime = (long) RiverHydroConstants.MISSING_VALUE;
            }
        }

        /* load the pass-thru time values */

        if (obsOrFcst == HydroDataType.OBS_DATA) {
            if (fallBelowTime != (long) RiverHydroConstants.MISSING_VALUE) {
                riverForecastPoint.setObservedFallBelowTime(fallBelowTime);
            }

            if (riseAboveTime != (long) RiverHydroConstants.MISSING_VALUE) {
                riverForecastPoint.setObservedRiseAboveTime(riseAboveTime);
            }

            if (fallBelowTime < riseAboveTime) {
                riverForecastPoint
                        .setObservedFallBelowTime(RiverHydroConstants.MISSING_VALUE);
            }
        } else {
            if (fallBelowTime != (long) RiverHydroConstants.MISSING_VALUE) {
                riverForecastPoint.setForecastFallBelowTime(fallBelowTime);
            }

            if (riseAboveTime != (long) RiverHydroConstants.MISSING_VALUE) {
                riverForecastPoint.setForecastRiseAboveTime(riseAboveTime);
            }

            if (fallBelowTime < riseAboveTime) {
                riverForecastPoint
                        .setForecastFallBelowTime(RiverHydroConstants.MISSING_VALUE);
            }
        }
    }

    /**
     * Determine the overall riseabove_time and fallbelow_time for the forecast
     * point.
     * 
     * @param riverForecastPoint
     *            A fully queried RiverForecastPoint object
     */
    private void computeHydrographRiseFall(RiverForecastPoint riverForecastPoint) {
        /* determine the rise above time */
        /*
         * if there are both obs and fcst times, use the obs rise above, and the
         * fcst fall below
         */

        if (riverForecastPoint.getObservedRiseAboveTime() != RiverHydroConstants.MISSING_VALUE) {
            riverForecastPoint.setRiseAboveTime(riverForecastPoint
                    .getObservedRiseAboveTime());
            riverForecastPoint.setRiseAboveTypeSource(ALL_OBSERVED_TYPESOURCE);
        } else if (riverForecastPoint.getForecastRiseAboveTime() != RiverHydroConstants.MISSING_VALUE) {
            riverForecastPoint.setRiseAboveTime(riverForecastPoint
                    .getForecastRiseAboveTime());
            riverForecastPoint.setRiseAboveTypeSource(ALL_FORECAST_TYPESOURCE);
        } else {
            riverForecastPoint
                    .setRiseAboveTime(RiverHydroConstants.MISSING_VALUE);
            riverForecastPoint.setRiseAboveTypeSource("");
        }

        /* determine the fall below info. */

        if (riverForecastPoint.getForecastFallBelowTime() != RiverHydroConstants.MISSING_VALUE) {
            riverForecastPoint.setFallBelowTime(riverForecastPoint
                    .getForecastFallBelowTime());
            riverForecastPoint.setFallBelowTypeSource(ALL_FORECAST_TYPESOURCE);
        }

        else if (riverForecastPoint.getObservedFallBelowTime() != RiverHydroConstants.MISSING_VALUE) {
            riverForecastPoint.setFallBelowTime(riverForecastPoint
                    .getObservedFallBelowTime());
            riverForecastPoint.setFallBelowTypeSource(ALL_OBSERVED_TYPESOURCE);
        } else {
            riverForecastPoint
                    .setFallBelowTime(RiverHydroConstants.MISSING_VALUE);
            riverForecastPoint.setFallBelowTypeSource("");
        }

        /* decide the crest_ts if both obs and fcst times exit */
        if (riverForecastPoint.getObservedMaximumIndex() != RiverHydroConstants.MISSING_VALUE
                && riverForecastPoint.getMaximumForecastIndex() != RiverHydroConstants.MISSING_VALUE) {
            List<SHEFObserved> shefObservedList = (List<SHEFObserved>) riverForecastPoint
                    .getHydrographObserved().getShefHydroDataList();
            List<SHEFForecast> shefForecastList = (List<SHEFForecast>) riverForecastPoint
                    .getHydrographForecast().getShefHydroDataList();

            if (shefObservedList.get(
                    riverForecastPoint.getObservedMaximumIndex()).getValue() >= shefForecastList
                    .get(riverForecastPoint.getMaximumForecastIndex())
                    .getValue()) {
                riverForecastPoint.setCrestTypeSource(ALL_OBSERVED_TYPESOURCE);
            } else {
                riverForecastPoint.setCrestTypeSource(ALL_FORECAST_TYPESOURCE);
            }
        } else if (riverForecastPoint.getObservedMaximumIndex() != RiverHydroConstants.MISSING_VALUE) {
            riverForecastPoint.setCrestTypeSource(ALL_OBSERVED_TYPESOURCE);
        } else if (riverForecastPoint.getMaximumForecastIndex() != RiverHydroConstants.MISSING_VALUE) {
            riverForecastPoint.setCrestTypeSource(ALL_FORECAST_TYPESOURCE);
        } else {
            riverForecastPoint.setCrestTypeSource("");
        }
    }

    /**
     * Sets the virtual fall-below time based on any actual fall below time,
     * considering some special cases.
     * 
     * @param riverForecastPoint
     *            A fully queried RiverForecastPoint object
     * @param shiftFlag
     *            Perform time shift operations when flag is set.
     * @return
     */
    public long getVirtualFallBelowTime(RiverForecastPoint riverForecastPoint,
            boolean shiftFlag) {
        double floodLevel;
        long fallBelowTime = RiverHydroConstants.MISSING_VALUE;

        if (shiftFlag) {
            if (riverForecastPoint.getFallBelowTime() != RiverHydroConstants.MISSING_VALUE) {
                Calendar fallBelowCalendar = Calendar.getInstance();
                fallBelowCalendar.setTimeInMillis(riverForecastPoint
                        .getFallBelowTime());
                long adjustEndHrs = (long) riverForecastPoint.getAdjustEndHrs();
                if (adjustEndHrs != RiverHydroConstants.MISSING_VALUE) {
                    fallBelowCalendar.add(Calendar.SECOND,
                            (int) (adjustEndHrs * TimeUtil.SECONDS_PER_HOUR));
                    fallBelowTime = fallBelowCalendar.getTimeInMillis();
                } else {
                    fallBelowCalendar.add(Calendar.HOUR,
                            getShiftHoursForAllForecastPoints());
                    fallBelowTime = fallBelowCalendar.getTimeInMillis();
                }
            }
        } else {
            fallBelowTime = riverForecastPoint.getFallBelowTime();
        }

        /*
         * adjust endtime for the special case where the forecast drops below
         * flood level, which results in valid fall-below time, but then it
         * rises above flood level. in this case, we don't know when the final
         * fall-below time is, so set it to missing/unknown.
         */
        HydrographForecast hydrographForecast = riverForecastPoint
                .getHydrographForecast();
        List<SHEFForecast> shefForecastList = hydrographForecast
                .getShefHydroDataList();
        int shefForecastListSize = shefForecastList.size();
        if (shefForecastListSize > 0) {
            if (riverForecastPoint.getPhysicalElement().startsWith("Q")) {
                floodLevel = riverForecastPoint.getFloodFlow();
            } else {
                floodLevel = riverForecastPoint.getFloodStage();
            }

            if (floodLevel != RiverHydroConstants.MISSING_VALUE) {
                if (shefForecastListSize > 0) {
                    SHEFForecast shefForecast = shefForecastList
                            .get(shefForecastListSize - 1);
                    double value = shefForecast.getValue();

                    if (value > floodLevel) {
                        fallBelowTime = RiverHydroConstants.MISSING_VALUE;
                    }
                }
            }
        }

        return fallBelowTime;
    }

    // AppDefaults get values methods
    /**
     * The max number of hours to look back for a river forecast basis time.
     * 
     * @return the max number of hours to look back for a river forecast basis
     *         time.
     */
    public long getBasisHoursForAllForecastPoints() {

        if (basisHoursForAllForecastPoints == Integer.MIN_VALUE) {
            AppsDefaults appsDefaults = AppsDefaults.getInstance();
            basisHoursForAllForecastPoints = appsDefaults.getInt(
                    "basis_hours_filter",
                    RiverHydroConstants.DEFAULT_OBS_FCST_BASIS_HOURS);
        }

        return (basisHoursForAllForecastPoints);
    }

    /**
     * Get time buffer around a reference stage.
     * 
     * @return The buffer around a reference stage.
     */
    public double getDefaultStageWindow() {

        if (defaultStageWindow == Double.MIN_VALUE) {
            AppsDefaults appsDefaults = AppsDefaults.getInstance();
            defaultStageWindow = appsDefaults.getDouble("rpf_stage_window",
                    RiverHydroConstants.DEFAULT_STAGE_WINDOW);
        }

        return defaultStageWindow;
    }

    /**
     * Get the number of hours to look into the future for forecast river data.
     * 
     * @return the number of hours to look into the future for forecast river
     *         data.
     */
    public long getLookForwardHoursForAllForecastPoints() {
        if (this.lookForwardHoursForAllForecastPoints == Integer.MIN_VALUE) {
            HazardSettings localHazardSettings = this.getHazardSettings();
            this.lookForwardHoursForAllForecastPoints = localHazardSettings
                    .getForecastLookForwardHours();
        }
        return lookForwardHoursForAllForecastPoints;
    }

    /**
     * Get the number of hours to look back for observed river data.
     * 
     * @return the number of hours to look back for observed river data.
     */
    public long getLookBackHoursForAllObservationPoints() {

        if (this.lookBackHoursForAllForecastPoints == Integer.MIN_VALUE) {
            HazardSettings localHazardSettings = this.getHazardSettings();
            this.lookBackHoursForAllForecastPoints = localHazardSettings
                    .getObsLookbackHours();
        }

        return lookBackHoursForAllForecastPoints;
    }

    /**
     * Get the number of hours to add to the fall below time.
     * 
     * @return The number of hours to add to the fall below time.
     */
    public int getShiftHoursForAllForecastPoints() {

        if (shiftHoursForAllForecastPoints == Integer.MIN_VALUE) {
            AppsDefaults appsDefaults = AppsDefaults.getInstance();
            shiftHoursForAllForecastPoints = appsDefaults.getInt(
                    "rpf_endtime_shifthrs",
                    RiverHydroConstants.DEFAULT_ENDTIME_SHIFT_HOURS);
            if (shiftHoursForAllForecastPoints < 0
                    || shiftHoursForAllForecastPoints > 48) {
                statusHandler
                        .info("Error in specified value for token rpf_endtime_shifthrs.\n"
                                + "Using default value of "
                                + RiverHydroConstants.DEFAULT_ENDTIME_SHIFT_HOURS);
                shiftHoursForAllForecastPoints = RiverHydroConstants.DEFAULT_ENDTIME_SHIFT_HOURS;
            }
        }

        return (shiftHoursForAllForecastPoints);
    }

    /**
     * Calculates the maximum observed or forecast value for this group.
     * 
     * @param riverForecastGroup
     *            Parent RiverForecastGroup
     * @param riverForecastPointList
     *            List of child RiverForecastPoint objects.
     */
    public void computeGroupMofo(RiverForecastGroup riverForecastGroup,
            List<RiverForecastPoint> riverForecastPointList) {
        int categoryValue;
        long timeVal = RiverHydroConstants.MISSING_VALUE;
        int maxCurObsCategory = HydroFloodCategories.NULL_CATEGORY.getRank();
        int maxLatestFcstCategory = HydroFloodCategories.NULL_CATEGORY
                .getRank();
        long maxCurObsTime = (long) RiverHydroConstants.MISSING_VALUE;
        long maxLatestFcstTime = (long) RiverHydroConstants.MISSING_VALUE;

        /* get the info for each of the forecast groups */

        for (RiverForecastPoint tempForecastPoint : riverForecastPointList) {
            /*
             * check the max current observed category value and omf category.
             * always use the earliest cur observed.
             */
            if (tempForecastPoint.getCurrentObservationCategory() != HydroFloodCategories.NULL_CATEGORY
                    .getRank()) {
                categoryValue = tempForecastPoint
                        .getCurrentObservationCategory();
                timeVal = tempForecastPoint.getCurrentObservation()
                        .getObsTime();

                if (categoryValue > maxCurObsCategory) {
                    maxCurObsCategory = categoryValue;
                    maxCurObsTime = timeVal;
                } else if (categoryValue == maxCurObsCategory) {
                    if (timeVal < maxCurObsTime
                            || maxCurObsTime == RiverHydroConstants.MISSING_VALUE) {
                        maxCurObsTime = timeVal;
                    }
                }
            }

            /*
             * check the max forecast category and omf category. always use the
             * earliest maxfcst
             */

            if (tempForecastPoint.getMaximumForecastCategory() != HydroFloodCategories.NULL_CATEGORY
                    .getRank()) {
                categoryValue = tempForecastPoint.getMaximumForecastCategory();
                SHEFForecast maximumShefForecast = tempForecastPoint
                        .getMaximumSHEFForecast();
                if (maximumShefForecast != null) {
                    timeVal = maximumShefForecast.getValidTime();
                }

                if (categoryValue > maxLatestFcstCategory) {
                    maxLatestFcstCategory = categoryValue;
                    maxLatestFcstTime = timeVal;
                } else if (categoryValue == maxLatestFcstCategory) {
                    if (timeVal < maxLatestFcstTime
                            || maxLatestFcstTime == RiverHydroConstants.MISSING_VALUE) {
                        maxLatestFcstTime = timeVal;
                    }
                }
            }
        } /* end of loop of fps in group */

        /* load the local variables into the structure */
        riverForecastGroup.setMaxCurrentObservedCategory(maxCurObsCategory);
        riverForecastGroup.setMaxCurrentObservedTime(new Date(maxCurObsTime));

        riverForecastGroup.setMaxForecastCategory(maxLatestFcstCategory);
        riverForecastGroup.setMaxForecastTime(new Date(maxLatestFcstTime));

        /*
         * if the cats are equal, use the observed since it is earlier in time.
         */

        if (riverForecastGroup.getMaxCurrentObservedCategory() >= riverForecastGroup
                .getMaxForecastCategory()) {
            riverForecastGroup.setMaxOMFCategory(riverForecastGroup
                    .getMaxCurrentObservedCategory());
            riverForecastGroup.setMaxOMFTime(riverForecastGroup
                    .getMaxCurrentObservedTime());
        } else {
            riverForecastGroup.setMaxOMFCategory(riverForecastGroup
                    .getMaxForecastCategory());
            riverForecastGroup.setMaxOMFTime(riverForecastGroup
                    .getMaxForecastTime());
        }
    }

    /**
     * Conmpute derived values for RiverForecastGroups within a defined County.
     * 
     * @param countyForecastGroup
     *            CountyForecastGroup to compute data for
     * @param riverForecastPointList
     *            List of All RiverForecastPoint objects for the given County
     */
    public void computeCountyMofo(CountyForecastGroup countyForecastGroup,
            List<RiverForecastPoint> riverForecastPointList) {

        int categoryValue;
        long timeValue;
        int maxCurObsCategory = HydroFloodCategories.NULL_CATEGORY.getRank();
        int maxLatestFcstCategory = HydroFloodCategories.NULL_CATEGORY
                .getRank();
        long maxCurObsTime = RiverHydroConstants.MISSING_VALUE;
        long maxLatestFcstTime = RiverHydroConstants.MISSING_VALUE;

        /* get the info for each of the forecast groups */

        maxCurObsCategory = maxLatestFcstCategory = HydroFloodCategories.NULL_CATEGORY
                .getRank();
        maxCurObsTime = maxLatestFcstTime = (long) RiverHydroConstants.MISSING_VALUE;

        for (RiverForecastPoint forecastPoint : riverForecastPointList) {

            /*
             * check the max current observed category value and omf category.
             * always use the earliest cur observed.
             */

            if (forecastPoint.getCurrentObservationCategory() != HydroFloodCategories.NULL_CATEGORY
                    .getRank()) {
                categoryValue = forecastPoint.getCurrentObservationCategory();
                timeValue = forecastPoint.getCurrentObservation().getObsTime();

                if (categoryValue > maxCurObsCategory) {
                    maxCurObsCategory = categoryValue;
                    maxCurObsTime = timeValue;
                } else if (categoryValue == maxCurObsCategory) {
                    if (timeValue < maxCurObsTime
                            || maxCurObsTime == RiverHydroConstants.MISSING_VALUE) {
                        maxCurObsTime = timeValue;
                    }
                }
            }

            /*
             * check the max forecast category and omf category. always use the
             * earliest maxfcst
             */

            if (forecastPoint.getMaximumForecastCategory() != HydroFloodCategories.NULL_CATEGORY
                    .getRank()) {
                categoryValue = forecastPoint.getMaximumForecastCategory();
                timeValue = forecastPoint.getMaximumSHEFForecast()
                        .getValidTime();

                if (categoryValue > maxLatestFcstCategory) {
                    maxLatestFcstCategory = categoryValue;
                    maxLatestFcstTime = timeValue;
                } else if (categoryValue == maxLatestFcstCategory) {
                    if (timeValue < maxLatestFcstTime
                            || maxLatestFcstTime == RiverHydroConstants.MISSING_VALUE) {
                        maxLatestFcstTime = timeValue;
                    }
                }
            }
        } /* end of loop of fps in group */

        /* load the local variables into the structure */
        countyForecastGroup.setMaxCurrentObservedCategory(maxCurObsCategory);
        countyForecastGroup.setMaxCurrentObservedTime(maxCurObsTime);

        countyForecastGroup.setMaxMaxForecastCategory(maxLatestFcstCategory);
        countyForecastGroup.setMaxMaxForecastTime(maxLatestFcstTime);

        /*
         * if the cats are equal, use the observed since it is earlier in time.
         */
        if (countyForecastGroup.getMaxCurrentObservedCategory() >= countyForecastGroup
                .getMaxMaxForecastCategory()) {
            countyForecastGroup.setMaxOMFCategory(countyForecastGroup
                    .getMaxCurrentObservedCategory());
            countyForecastGroup.setMaxOMFTime(countyForecastGroup
                    .getMaxCurrentObservedTime());
        } else {
            countyForecastGroup.setMaxOMFCategory(countyForecastGroup
                    .getMaxMaxForecastCategory());
            countyForecastGroup.setMaxOMFTime(countyForecastGroup
                    .getMaxMaxForecastTime());
        }
    }

    /**
     * At the moment, the longitudes stored in the Hydro database are positive
     * for the Western Hemisphere. They should be negative. This method makes
     * the appropriate conversion.
     * 
     * @param hydroLongitude
     *            The longitude representation as read from the hydro database,
     *            i.e. Western Hemisphere longitudes are positive
     * @return The hydro longitude converted to a Western Hemisphere longitude,
     *         i.e. it is made negative.
     */
    public Double convertHydroLongitudesToWesternHemisphere(
            final Double hydroLongitude) {
        return hydroLongitude * -1;
    }

    /**
     * This methods allows the flood recommender to run displaced in
     * circumstances such as Unit Tests.
     * 
     * @param
     * @return The system time.
     */
    public static Date getSystemTime() {
        return SimulatedTime.getSystemTime().getTime();
    }

    /**
     * Retrieve Maximum Shef Value from a list of SHEF base objects.
     * 
     * @param hydrograph
     *            Hydrograph Parent of SHEF object list
     * @return Maximum value contained in hydrograph
     */
    private int getMaxShefValueIndex(Hydrograph<? extends SHEFBase> hydrograph) {
        int maxShefValueIndex = -1;
        double maxShefValue = Double.MIN_VALUE;

        if (hydrograph != null) {
            List<? extends SHEFBase> shefBaseList = hydrograph
                    .getShefHydroDataList();
            if ((shefBaseList != null) && (shefBaseList.isEmpty() == false)) {
                SHEFBase shefBase = null;
                int shefBaseListSize = shefBaseList.size();
                for (int i = 0; i < shefBaseListSize; i++) {
                    shefBase = shefBaseList.get(i);
                    double shefValue = shefBase.getValue();
                    if (shefValue != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                        if (maxShefValueIndex >= 0) {
                            if (shefValue > maxShefValue) {
                                maxShefValueIndex = i;
                                maxShefValue = shefValue;
                            }
                        } else {
                            maxShefValueIndex = i;
                            maxShefValue = shefValue;
                        }
                    }
                }
            } else {
                maxShefValueIndex = RiverHydroConstants.MISSING_VALUE;
            }
        } else {
            maxShefValueIndex = RiverHydroConstants.MISSING_VALUE;
        }

        return (maxShefValueIndex);
    }

    /**
     * Get the Index of the Latest SHEFForecast (or SHEFObserved) object with a
     * valid Value set. This method does NOT rely on the ordering of the list of
     * objects.
     * <p>
     * SHEFObserved objects use the ObsTime value.
     * <p>
     * SHEFForecast objects use the ValidTime value.
     * 
     * @param hydrograph
     *            Parent hydrograph
     * @return Index of the SHEF (Observed or Forecast) with the latest time and
     *         a Valid Value
     */
    private int getLatestValidTimeIndex(
            Hydrograph<? extends SHEFBase> hydrograph) {
        int latestShefTimeIndex = -1;
        double latestShefTime = Long.MIN_VALUE;

        if (hydrograph != null) {
            List<? extends SHEFBase> shefBaseList = hydrograph
                    .getShefHydroDataList();
            if ((shefBaseList != null) && (shefBaseList.isEmpty() == false)) {
                SHEFBase shefBase = null;
                int shefBaseListSize = shefBaseList.size();
                for (int i = 0; i < shefBaseListSize; i++) {
                    shefBase = shefBaseList.get(i);
                    long shefTime = 0;
                    if (shefBase instanceof SHEFForecast) {
                        shefTime = ((SHEFForecast) shefBase).getValidTime();
                    } else if (shefBase instanceof SHEFObserved) {
                        shefTime = ((SHEFObserved) shefBase).getObsTime();
                    } else {
                        shefTime = RiverHydroConstants.MISSING_VALUE;
                    }
                    double shefValue = shefBase.getValue();
                    if ((shefValue != RiverHydroConstants.MISSING_VALUE_DOUBLE)
                            && (shefTime != RiverHydroConstants.MISSING_VALUE)) {
                        if (latestShefTimeIndex >= 0) {
                            if (shefTime > latestShefTime) {
                                latestShefTimeIndex = i;
                                latestShefTime = shefTime;
                            }
                        } else {
                            if (shefTime > 0) {
                                latestShefTimeIndex = i;
                                latestShefTime = shefTime;
                            }
                        }
                    }
                }
            } else {
                latestShefTimeIndex = RiverHydroConstants.MISSING_VALUE;
            }
        } else {
            latestShefTimeIndex = RiverHydroConstants.MISSING_VALUE;
        }

        return (latestShefTimeIndex);
    }

    /**
     * Loads the trend values using observed and forecast data. This function
     * also load the riseabove and fallbelow times or the observed and forecast
     * combination.
     * 
     * @param riverForecastPoint
     *            Fully queried RiverForecastPoint object
     */
    // Was: load_trend_info()
    void computeTrendData(RiverForecastPoint riverForecastPoint) {

        HydroGraphTrend hydroGraphTrend = HydroGraphTrend.MISSING;

        double stageWindow = RiverHydroConstants.MISSING_VALUE_DOUBLE;

        double obsRefStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        HydroGraphTrend observedTrend = HydroGraphTrend.MISSING;
        int shefObservedListSize = 0;
        double fcstRefStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
        HydroGraphTrend forecastTrend = HydroGraphTrend.MISSING;
        int shefForecastListSize = 0;
        /*
         * set STAGE_WINDOW as fp[].chg_threshold, if fp[].chg_threshold is
         * missing, then use the value from token stage_window
         */
        double changeThreshold = riverForecastPoint.getChangeThreshold();
        if (changeThreshold != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
            stageWindow = changeThreshold;
        } else {
            stageWindow = getDefaultStageWindow();
        }

        /*
         * first compute the observed trend. to do this, then we need to make
         * sure there are at least two observed values. use the current observed
         * value as the reference stage and compare it to the most recent max or
         * min value that is outside the stage window around the reference stage
         */
        // COMPUTE OBSERVED TREND
        HydrographObserved hydrographObserved = riverForecastPoint
                .getHydrographObserved();
        List<SHEFObserved> shefObservedList = hydrographObserved
                .getShefHydroDataList();

        shefObservedListSize = shefObservedList.size();
        if (shefObservedListSize > 1) {

            double obsMinStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
            long obsMinStageTime = 0;
            double obsMaxStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
            long obsMaxStageTime = 0;

            double obsCompStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
            boolean obsMinSet = false;
            boolean obsMaxSet = false;

            double obsCheckStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
            long obsCheckStageTime = 0L;

            /*
             * find the latest non-missing obs value for use as the reference
             * stage. search the data based on the order and the usage of the
             * data.
             */
            int startIndex = shefObservedListSize - 1;
            int endIndex = 0;
            for (int i = startIndex; i >= endIndex; i--) {
                SHEFObserved shefObserved = shefObservedList.get(i);
                double tempObsValue = shefObserved.getValue();
                if (tempObsValue != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
                    obsRefStageValue = shefObservedList.get(i).getValue();
                    break;
                }
            }

            /*
             * loop on all the stages and find the min or max that is outside
             * the stage window relative to the reference stage.
             */
            for (int i = 0; i < shefObservedListSize; i++) {
                SHEFObserved shefObserved = shefObservedList.get(i);
                obsCheckStageValue = shefObserved.getValue();
                obsCheckStageTime = shefObserved.getObsTime();

                /*
                 * if the min is not set yet, then initialize it. if it is set
                 * already, then check if there is a later min value. only
                 * consider values that are not close in value to the reference
                 * stage. note that in the event of duplicate mins/maxes, it
                 * uses the MOST RECENT min/max because the data is sorted from
                 * earliest to latest
                 */
                if ((obsCheckStageValue != RiverHydroConstants.MISSING_VALUE_DOUBLE)
                        && (obsRefStageValue != RiverHydroConstants.MISSING_VALUE_DOUBLE)) {
                    if ((obsCheckStageValue < (obsRefStageValue - stageWindow))
                            && ((obsMinSet == false) || (obsCheckStageValue <= obsMinStageValue))) {
                        obsMinStageValue = obsCheckStageValue;
                        obsMinStageTime = obsCheckStageTime;
                        obsMinSet = true;
                    }

                    if ((obsCheckStageValue > (obsRefStageValue + stageWindow))
                            && ((obsMaxSet == false) || (obsCheckStageValue >= obsMaxStageValue))) {
                        obsMaxStageValue = obsCheckStageValue;
                        obsMaxStageTime = obsCheckStageTime;
                        obsMaxSet = true;
                    }
                }
            }

            /*
             * now that we have info on the min and max stages, find which is
             * the MOST RECENT and will be used to compare against the reference
             * stage.
             */
            if (obsMinSet && obsMaxSet) {
                if (obsMinStageTime > obsMaxStageTime) {
                    obsCompStageValue = obsMinStageValue;
                } else {
                    obsCompStageValue = obsMaxStageValue;
                }
            } else if (obsMinSet) {
                obsCompStageValue = obsMinStageValue;
            } else if (obsMaxSet) {
                obsCompStageValue = obsMaxStageValue;
            }
            /*
             * now determined the trend. if no mins or maxes were found outside
             * the stage window, then the trend is considered unchanged
             */

            if (obsMinSet || obsMaxSet) {
                if (obsCompStageValue > obsRefStageValue) {
                    observedTrend = HydroGraphTrend.FALL;
                } else if (obsCompStageValue < obsRefStageValue) {
                    observedTrend = HydroGraphTrend.RISE;
                } else {
                    observedTrend = HydroGraphTrend.UNCHANGED;
                }
            } else {
                observedTrend = HydroGraphTrend.UNCHANGED;
            }
        } /* end of check if shefObservedListSize > 1 */
        else if (shefObservedListSize == 1) {
            /*
             * if only one obs value, set the reference stage of use in the
             * overall trend check below. the case of one obs value can occur
             * because of the vtec obs filter. note that this check does not
             * account for the possibility of a missing value indicator...
             */
            SHEFObserved shefObserved = shefObservedList.get(0);
            obsRefStageValue = shefObserved.getValue();
        }

        /* -------------------------------------------------------------- */
        // COMPUTE FORECAST TREND
        List<SHEFForecast> shefForecastList = null;
        shefForecastListSize = 0;
        HydrographForecast hydrographForecast = riverForecastPoint
                .getHydrographForecast();
        if (hydrographForecast != null) {
            shefForecastList = hydrographForecast.getShefHydroDataList();
            if (shefForecastList != null) {
                shefForecastListSize = shefForecastList.size();
            }
        }

        /*
         * now compute the general trend. this uses forecast data to determine
         * the expected overall trend. if no forecast data exist, then the
         * general trend is set to be the same as the observed trend.
         */

        if (shefObservedListSize > 1 && shefForecastListSize == 0) {
            hydroGraphTrend = observedTrend;
        } else if ((shefObservedListSize > 0 && shefForecastListSize > 0)
                || (shefObservedListSize == 0 && shefForecastListSize > 1)) {
            /*
             * the reference stage is the latest observed which is compared to
             * the first max or min which is found in the forecast data, that is
             * outside the stage window around the reference stage. if no
             * observed stage is available use the first forecast value as the
             * reference stage.
             */

            /*
             * use the already determined refstage if obs data available. if no
             * obs available, use the first forecast value, assuming that it is
             * not missing, and set the start_index to bypass the first forecast
             * value.
             */

            double fcstMinStageValue = Double.MAX_VALUE;
            long fcstMinStageTime = 0;
            double fcstMaxStageValue = Double.MIN_VALUE;
            long fcstMaxStageTime = 0;

            double fcstCompStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
            boolean fcstMinSet = false;
            boolean fcstMaxSet = false;

            double fcstCheckStageValue = RiverHydroConstants.MISSING_VALUE_DOUBLE;
            long fcstCheckStageTime = 0L;

            int fcstStartIndex = Integer.MIN_VALUE;
            ;
            if (shefObservedListSize > 0) {
                fcstStartIndex = 0;
            } else {
                fcstRefStageValue = shefForecastList.get(0).getValue();
                fcstStartIndex = 1;
            }

            for (int i = fcstStartIndex; i < shefForecastListSize; i++) {
                SHEFForecast shefForecast = shefForecastList.get(i);
                fcstCheckStageValue = shefForecast.getValue();
                fcstCheckStageTime = shefForecast.getValidTime();

                /*
                 * if the min is not set yet, then initialize it. if it is set
                 * already, then check if there is a later min value. only
                 * consider values that are not close in value to the reference
                 * stage. note that in the event of duplicate mins/maxes, it
                 * uses the EARLIEST min/max because the data is sorted from
                 * earliest to latest
                 */

                if ((fcstCheckStageValue != RiverHydroConstants.MISSING_VALUE_DOUBLE)
                        && (fcstRefStageValue != RiverHydroConstants.MISSING_VALUE_DOUBLE)) {
                    if ((fcstCheckStageValue < (fcstRefStageValue - stageWindow))
                            && ((fcstMinSet == false) || (fcstCheckStageValue < fcstMinStageValue))) {
                        fcstMinStageValue = fcstCheckStageValue;
                        fcstMinStageTime = fcstCheckStageTime;
                        fcstMinSet = true;
                    }

                    if ((fcstCheckStageValue > (fcstRefStageValue + stageWindow))
                            && ((fcstMaxSet == false) || (fcstCheckStageValue > fcstMaxStageValue))) {
                        fcstMaxStageValue = fcstCheckStageValue;
                        fcstMaxStageTime = fcstCheckStageTime;
                        fcstMaxSet = true;
                    }
                }
            }

            /*
             * now that we have the info on the min and max stages, find which
             * is the EARLIEST and will be used to compare against the reference
             * stage.
             */

            if (fcstMinSet && fcstMaxSet) {
                if (fcstMinStageTime < fcstMaxStageTime) {
                    fcstCompStageValue = fcstMinStageValue;
                } else {
                    fcstCompStageValue = fcstMaxStageValue;
                }
            } else if (fcstMinSet) {
                fcstCompStageValue = fcstMinStageValue;
            } else if (fcstMaxSet) {
                fcstCompStageValue = fcstMaxStageValue;
            }

            /*
             * now determined the trend. if no mins or maxes were found outside
             * the stage window, then the trend is considered unchanged
             */

            if (fcstMinSet || fcstMaxSet) {
                if (fcstCompStageValue < fcstRefStageValue) {
                    hydroGraphTrend = HydroGraphTrend.FALL;
                } else if (fcstCompStageValue > fcstRefStageValue) {
                    hydroGraphTrend = HydroGraphTrend.RISE;
                } else {
                    hydroGraphTrend = HydroGraphTrend.UNCHANGED;
                }
            } else {
                hydroGraphTrend = HydroGraphTrend.UNCHANGED;
            }

        }

        riverForecastPoint.setTrend(hydroGraphTrend);

    }

    /**
     * Query and set Flood Crest History for the RiverForecastPoint.
     * 
     * A RiverForecastPoint will have EITHER Stage Crest History or Flow Crest
     * History (based on its PE (Physical Element) value), but not both.
     * <p>
     * This method is also responsible for setting the appropriate values in the
     * floodCategoryArray.
     * 
     * @param riverForecastPoint
     *            fully queried RiverForecastPoint object
     */
    protected void getRiverForecastCrestHistory(
            RiverForecastPoint riverForecastPoint) {
        String lid = riverForecastPoint.getLid();
        String physicalElement = riverForecastPoint.getPhysicalElement();
        /*
         * Load the minor/moderate/major stage or flow based on the primary_pe
         * specified for the station.
         */

        char peFirstChar = physicalElement.charAt(0);
        char peSecondChar = physicalElement.charAt(1);

        double[] floodCategoryArray = riverForecastPoint.getFloodCategory();

        List<String> crestTypeList = Lists.newArrayListWithExpectedSize(2);
        crestTypeList.add(CrestHistory.PRELIM_RECORD);
        crestTypeList.add(CrestHistory.PRELIM_OFFICIAL);
        if (peFirstChar != 'Q') {
            List<CrestHistory> stageCrestHistoryList = floodDAO
                    .queryStageCrestHistory(lid);
            if ((stageCrestHistoryList != null)
                    && (stageCrestHistoryList.isEmpty() == false)) {
                for (CrestHistory crestHistory : stageCrestHistoryList) {
                    if (CrestHistory.PRELIM_RECORD.equals(crestHistory
                            .getPrelim()) == true) {
                        double stage = crestHistory.getStage();
                        if (stage != 0.0) {
                            floodCategoryArray[HydroFloodCategories.RECORD_FLOOD_CATEGORY
                                    .getRank()] = stage;
                        }
                        break;
                    }
                }
            }
            riverForecastPoint.setStageCrestHistoryList(stageCrestHistoryList);
        } else if (peSecondChar != 'B' && peSecondChar != 'C'
                && peSecondChar != 'E' && peSecondChar != 'F'
                && peSecondChar != 'V') {
            List<CrestHistory> flowCrestHistoryList = floodDAO
                    .queryFlowCrestHistory(lid, crestTypeList);
            if ((flowCrestHistoryList != null)
                    && (!flowCrestHistoryList.isEmpty())) {
                for (CrestHistory crestHistory : flowCrestHistoryList) {
                    if (CrestHistory.PRELIM_RECORD.equals(crestHistory
                            .getPrelim()) == true) {
                        int q = crestHistory.getQ();
                        if (q != 0) {
                            floodCategoryArray[HydroFloodCategories.RECORD_FLOOD_CATEGORY
                                    .getRank()] = (double) q;
                            break;
                        }
                    }
                }
            }
            riverForecastPoint.setFlowCrestHistoryList(flowCrestHistoryList);
        }

        if ((floodCategoryArray[1] == RiverHydroConstants.MISSING_VALUE_DOUBLE)
                || (floodCategoryArray[2] == RiverHydroConstants.MISSING_VALUE_DOUBLE)
                || (floodCategoryArray[3] == RiverHydroConstants.MISSING_VALUE_DOUBLE)
                || (floodCategoryArray[4] == RiverHydroConstants.MISSING_VALUE_DOUBLE)) {
            statusHandler.info("Missing the Flood change of threshold for "
                    + lid);
        }
    }

    /**
     * Get a List of Crest History objects for a given LID and Physical Element
     * pair. Only Record (R) crest histoy values are queried.
     * 
     * @param lid
     *            River Forecast Point Identifier
     * @param physicalElement
     *            Physical Element value
     * @return List of Crest Histories
     */
    public List<CrestHistory> getRiverForecastCrestHistory(String lid,
            String physicalElement) {
        List<String> crestTypeList = Lists.newArrayListWithExpectedSize(1);
        crestTypeList.add("R");

        return (getRiverForecastCrestHistory(lid, physicalElement,
                crestTypeList));
    }

    /**
     * Retrieve the River Crest History for a River Forecast Point LID.
     * 
     * This method queries based on the given PE value
     * 
     * <pre>
     * Q            Will query Stage History
     * B,C,E,F,V    Will query Flow Crest History
     * </pre>
     * 
     * @param lid
     *            River Forecast Point Identifier
     * @param physicalElement
     *            Physical Element value
     * @param crestTypeList
     *            List of valid (prelim) values
     * @return A List of Crest History objects for Either Stage or Flow crest
     *         history
     */

    public List<CrestHistory> getRiverForecastCrestHistory(String lid,
            String physicalElement, List<String> crestTypeList) {

        List<CrestHistory> crestHistoryList = null;
        /*
         * Load the minor/moderate/major stage or flow based on the primary_pe
         * specified for the station.
         */
        char peFirstChar = physicalElement.charAt(0);
        char peSecondChar = physicalElement.charAt(1);

        /*
         * Load the record stage or flow from the crest table based on the
         * primary_pe for the station and store as a categorical value.
         */
        if (peFirstChar != 'Q') {
            crestHistoryList = floodDAO.queryStageCrestHistory(lid,
                    crestTypeList);
        } else if (peSecondChar != 'B' && peSecondChar != 'C'
                && peSecondChar != 'E' && peSecondChar != 'F'
                && peSecondChar != 'V') {
            crestHistoryList = floodDAO.queryFlowCrestHistory(lid,
                    crestTypeList);
        }

        return (crestHistoryList);
    }

    /**
     * Retrieve a Map of LID (Gauge Id) to a string containing Lat/Lon coords
     * for area Flood Inundation.
     * 
     * @return A Map with the gauge 'lid' as the key and a string of latitude
     *         and longitude values as the value.
     */
    public Map<String, String> getAreaInundationCoordinates() {

        Map<String, String> mapLidToAreaMap = this.floodDAO
                .queryAreaInundationCoordinateMap();

        return (mapLidToAreaMap);
    }

    /**
     * Retrieve Lat/Lon Flood Inundation for a River Forecast Point LID
     * 
     * @param lid
     *            River forecast point identifier
     * @return A string of latitude and longitude values.
     */
    public String getAreaInundationCoordinates(String lid) {

        String lidAreaString = this.floodDAO
                .queryAreaInundationCoordinates(lid);
        return (lidAreaString);
    }

    /**
     * Construct Hydro Event (Previous Event) List.
     * 
     * Build a Hydro Event list from a list of chronologically sequenced, Deep
     * Query RiverForecastPoint objects.
     * <p>
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
            List<RiverForecastPoint> forecastPointList,
            Map<String, Object> eventMap, long currentSystemTime) {
        /*
         * Take a list of Active Forecast Points. Chronologically sort them and
         * construct the Previous Info data for the given list of points.
         */
        List<HydroEvent> hydroEventList = Lists
                .newArrayListWithExpectedSize(forecastPointList.size());
        for (RiverForecastPoint forecastPoint : forecastPointList) {

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
     * RiverPro program (e.g. if a product is issued via the gui...)
     * 
     * @param riverForecastPoint
     *            fully queried RiverForecastPoint object
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

                categoryValue = computeFloodStageCategory(riverForecastPoint,
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
                categoryValue = computeFloodStageCategory(riverForecastPoint,
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

    /**
     * This method takes a list of All queried River Forecast Group and all
     * queried River Forecast Point objects and places the River Forecast Point
     * child into the appropriate parent list.
     * 
     * @param forecastGroupList
     *            List of RiverForecastGroup objects
     * @param forecastPointList
     *            List of RiverForecastPoint ojbects
     */
    protected void integrateForecastPointsIntoGroups(
            List<RiverForecastGroup> forecastGroupList,
            List<RiverForecastPoint> forecastPointList) {

        if ((forecastGroupList != null) && (forecastPointList != null)) {
            Map<String, RiverForecastGroup> forecastGroupMap = Maps
                    .newHashMapWithExpectedSize(forecastGroupList.size());
            for (RiverForecastGroup forecastGroup : forecastGroupList) {
                String groupId = forecastGroup.getGroupId();
                forecastGroupMap.put(groupId, forecastGroup);
            }

            List<RiverForecastPoint> forecastGroupForecastPointList = null;
            RiverForecastGroup forecastGroup = null;
            String groupId = null;
            for (RiverForecastPoint forecastPoint : forecastPointList) {
                groupId = forecastPoint.getGroupId();
                if (groupId != null) {
                    forecastGroup = forecastGroupMap.get(groupId);
                    if (forecastGroup != null) {
                        forecastGroupForecastPointList = forecastGroup
                                .getForecastPointList();
                        if (forecastGroupForecastPointList == null) {
                            forecastGroupForecastPointList = Lists
                                    .newArrayList();
                            forecastGroup
                                    .setForecastPointList(forecastGroupForecastPointList);
                        }
                        // Chronological order from query will be preserved.
                        forecastGroupForecastPointList.add(forecastPoint);
                    } else {
                        statusHandler
                                .warn("Unable to find River Forecast Group Parent: "
                                        + groupId
                                        + " for logical River Forecast Point child "
                                        + forecastPoint.getLid());
                    }
                } else {
                    statusHandler
                            .warn("Null Group ID for River Forecast Point: "
                                    + forecastPoint.getLid());
                }
            }
        }
    }

    /**
     * County processing Calculates the maximum observed forecast river data for
     * this county group. This determines whether or not the county will be
     * considered in flood and the magnitude/severity of the flooding.
     * <p>
     * This also places all of the queried River Forecast Point objects in to
     * their appropriate County Forecast Group parent objects
     * 
     * @param countyForecastGroupList
     *            List of All County Forecast Group objects
     * @param countyForecastGroupList
     *            List of All County Forecast Group objects
     */
    protected void computeCountyMofo(
            List<CountyForecastGroup> countyForecastGroupList,
            Map<String, List<RiverForecastPoint>> countyToRiverForecastPointListMap) {

        if ((countyForecastGroupList != null)
                && (countyToRiverForecastPointListMap != null)) {
            for (CountyForecastGroup countyForecastGroup : countyForecastGroupList) {
                String stateCountyString = countyForecastGroup
                        .getStateCountyString();
                List<RiverForecastPoint> riverForecastPointList = countyToRiverForecastPointListMap
                        .get(stateCountyString);
                if (riverForecastPointList != null) {
                    computeCountyMofo(countyForecastGroup,
                            riverForecastPointList);
                    countyForecastGroup
                            .setForecastPointsInCountyList(riverForecastPointList);
                }
            }
        }
    }

    /**
     * Query for a Map of Lid (Point ID) to CountyStateData objects.
     * 
     * @param lidList
     *            List of RiverForecastPoint Identifier values
     * @return A Map of all input Lid values to their corresponding County Data
     *         objects
     */
    public Map<String, CountyStateData> getLidToCountyDataMap(
            List<String> lidList) {
        return (floodDAO.queryLidToCountyDataMap(lidList));
    }

    /**
     * Get a River Forecast County with its List of RiverForecastPoint objects
     * and computed data for a given State Abbreviation and County Name values.
     * 
     * This is a Variable Depth query
     * 
     * @param state
     *            2 letter State abbreviation
     * @param county
     *            Name of county (First letter capitalized)
     * @param isSubDataNeeded
     *            Flag indicating whether this is a Deep query (true) or a
     *            Shallow query (false)
     * @return List containing RiverForecastPoint data objects
     */
    public CountyForecastGroup getCountyForecastGroup(String state,
            String county, boolean isSubDataNeeded) {

        CountyForecastGroup countyForecastGroup = null;

        List<String> lidList = floodDAO.queryCountyRiverForecastPointIdList(
                state, county);

        if (lidList != null) {
            String hsa = null;
            List<RiverForecastPoint> riverForecastPointList = null;

            riverForecastPointList = floodDAO.queryRiverForecastPointList(
                    lidList, null, null, null);
            if ((isSubDataNeeded == true) && (riverForecastPointList != null)
                    && (riverForecastPointList.isEmpty() == false)) {
                for (RiverForecastPoint riverForecastPoint : riverForecastPointList) {
                    if (hsa == null) {
                        hsa = riverForecastPoint.getHsa();
                    }
                    getRiverForecastPointSubData(riverForecastPoint);
                }
            }

            countyForecastGroup = new CountyForecastGroup(state, county, hsa,
                    riverForecastPointList);
            if ((isSubDataNeeded == true) && (riverForecastPointList != null)
                    && (riverForecastPointList.isEmpty() == false)) {
                computeCountyMofo(countyForecastGroup, riverForecastPointList);
            }

            countyForecastGroup
                    .setForecastPointsInCountyList(riverForecastPointList);
        }
        return (countyForecastGroup);
    }

    /**
     * Retrieve the top ranked type source for the given parameters.
     * 
     * This is currently only used by TableText.py
     * 
     * @param lid
     *            River Forecast Point Id (Point ID)
     * @param physicalElement
     *            River Forecast Physical Element
     * @param duration
     *            Duration of measurement
     * @param extremum
     * @return Type Source for given parameters or null
     */
    public String getTopRankedTypeSource(String lid, String physicalElement,
            int duration, String extremum) {
        String topRankedTypeSource = null;

        topRankedTypeSource = this.floodDAO.queryTopRankedTypeSource(lid,
                physicalElement, duration, extremum);

        return (topRankedTypeSource);
    }

    /**
     * TODO Finish this method
     * 
     * <pre>
     * stageType - “Observed”, “Forecast”, “Precip”, “ObservedFlow”, “ForecastFlow”
     * startTime, endTime - define the range for the series to be returned.  If None, return the whole series
     * if physicalElement is None, return the primary physical element for the pointID
     *      We could add another optional argument -- selectedValue
     *      “min” -- return the minimum value in the series
     *      “max” -- return the maximum value in the series
     *      “latest” -- return the most recent value in the series
     * </pre>
     */
    public Object getTimeSeries(String lid, String physicalElement,
            String stageType, long startTime, long endTimestartTime) {

        Object returnObject = null;
        // TODO Finish fleshing out this method.
        return (returnObject);
    }

    /**
     * Retrieves the given physical element value for a river forecast point.
     * 
     * @param lid
     *            The river forecast point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param duration
     * @param typeSource
     *            The SHEF typesource code
     * @param extremum
     *            e.g. Z, X
     * @param timeArg
     *            The time specification dayOffset|hhmm|interval e.g. 0|1200|1
     *            where dayOffset is 0 today, 1 tomorrow etc. hhmm is the GMT
     *            hour of the day (24 hour clock) interval is the number of
     *            hours to create window around
     * @param timeFlag
     *            -- if True return a time string for requested value, otherwise
     *            return requested value
     * @param currentTime_ms
     *            -- current time in milliseconds
     * 
     */
    public String getPhysicalElementValue(String lid, String physicalElement,
            int duration, String typeSource, String extremum, String timeArg,
            boolean timeFlag, long currentTime_ms) {
        String queriedPhysicalElement = null;

        queriedPhysicalElement = this.floodDAO.queryPhysicalElementValue(lid,
                physicalElement, duration, typeSource, extremum, timeArg,
                timeFlag, currentTime_ms);
        return (queriedPhysicalElement);

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
     * @param riverForecastPoint
     *            River Forecast Point
     * @index Index of the SHEF Observed object in the HydrographObserved lise
     * @return Selected SHEF Observed object
     */
    public SHEFObserved getSHEFObserved(RiverForecastPoint riverForecastPoint,
            int index) {
        if ((riverForecastPoint != null)
                && (index != RiverHydroConstants.MISSING_VALUE)) {
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
     * @param riverForecastPoint
     *            River Forecast Point
     * @index Index of the SHEF Forecast object in the HydrographForecast lise
     * @return Selected SHEF Forecast object
     */
    public SHEFForecast getSHEFForecast(RiverForecastPoint riverForecastPoint,
            int index) {

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
