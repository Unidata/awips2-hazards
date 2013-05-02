package gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * This class represents a river forecast point and its associated meta
 * information and behavior.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * July 2012               Bryon.Lawrence    Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public class RiverForecastPoint {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RiverForecastPoint.class);

    /**
     * 
     * Description: Enumeration describing the fields in the FpInfo table in the
     * IHFS database.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * May 02, 2013            Bryon.Lawrence      Initial creation
     * 
     * </pre>
     * 
     * @author Bryon.Lawrence
     * @version 1.0
     */
    private enum FpInfoFieldEnum {
        LID, NAME, COUNTY, STATE, HSA, PRIMARY_BACK, SECONDARY_BACK, STREAM, BF, WSTG, FS, FQ, ACTION_FLOW, PE, USE_LATEST_FCST, PROXIMITY, REACH, GROUP_ID, ORDINAL, CHG_THRESHOLD, REC_TYPE, BACK_HRS, FORWARD_HRS, ADJUST_END_HRS, MINOR_STAGE, MODERATE_STAGE, MAJOR_STAGE, MINOR_FLOW, MODERATE_FLOW, MAJOR_FLOW
    }

    /**
     * The maximum flood category a hazard can obtain.
     */
    public final static int MAX_CAT = 5;

    /**
     * River Pro representation of a missing value.
     */
    public final static double MISSINGVAL = -9999;

    /**
     * "All Observed" typesource
     */
    public final static String ALL_OBSERVED_TYPESOURCE = "R*";

    /**
     * "All Forecast" typesource
     */

    public final static String ALL_FORECAST_TYPESOURCE = "F*";

    /**
     * Possible flood categories
     */
    public enum HydroFloodCategories {
        NULL_CATEGORY(-1), NO_FLOOD_CATEGORY(0), MINOR_FLOOD_CATEGORY(1), MODERATE_FLOOD_CATEGORY(
                2), MAJOR_FLOOD_CATEGORY(3), RECORD_FLOOD_CATEGORY(4);

        /**
         * @param rank
         *            The rank of the flood category.
         */
        private HydroFloodCategories(int rank) {
            this.rank = rank;
        }

        private int rank;

        /**
         * @return the rank of the flood category
         */
        public int getRank() {
            return rank;
        }
    }

    HydroFloodCategories cat = HydroFloodCategories.MAJOR_FLOOD_CATEGORY;

    /**
     * hydrograph trend descriptors
     */
    public enum HydroGraphTrend {
        RISE, UNCHANGED, FALL, MISSING
    };

    /**
     * Type of data - observation or forecast.
     */
    public enum HydroDataType {
        OBS_DATA, FCST_DATA
    };

    /*
     * Static E-19 type data
     */

    /**
     * River station identifier
     */
    private String id;

    /**
     * River station name
     */
    private String name;

    /**
     * River station county
     */
    private String county;

    /**
     * River station state
     */
    private String state;

    /**
     * River station stream
     */
    private String stream;

    /**
     * Bankfull stage
     */
    private double bankFull = MISSINGVAL;

    /**
     * Action stage
     */
    private double actionStage = MISSINGVAL;

    /**
     * Flood stage
     */
    private double floodStage = MISSINGVAL;

    /**
     * Flood flow
     */
    private double floodFlow = MISSINGVAL;

    /**
     * Action flow
     */
    private double actionFlow = MISSINGVAL;

    /**
     * Physical element
     */
    private String physicalElement;

    /**
     * Flag indicating whether or not the latest forecast should be used
     */
    private boolean useLatestForecast;

    /**
     * Location this forecast point is close to
     */
    private String proximity;

    /**
     * The forecast point's associated reach on the river
     */
    private String reach;

    /**
     * The river group this point belongs to
     */
    private String groupId;

    /**
     * Flood category thresholds for this forecast point
     */
    private final double floodCategory[] = { MISSINGVAL, MISSINGVAL,
            MISSINGVAL, MISSINGVAL, MISSINGVAL };

    /**
     * The hydrologic service area this point is in.
     */
    private String hsa;

    /**
     * Primary backup office responsible for this point
     */
    private String primaryBackup;

    /**
     * Secondary backup office responsible for this point
     */
    private String secondaryBackup;

    /**
     * Previously used for recommendation type, now it is used to determine
     * output the stage or flow value for impact and historical comparison
     * variables.
     */
    private String recommendationType;

    /**
     * stage/flow change for non-steady assumption.
     */
    private double changeThreshold = MISSINGVAL;

    /**
     * look back hours for observed data
     */
    private int lookBackHours;

    /**
     * Look forward hours for forecast data
     */
    private int lookFowardHours;

    /**
     * adjusted end hours for PVTEC line.
     */
    private double adjustEndHrs = MISSINGVAL;

    /**
     * previous recommended event information
     */
    /**
     * Are there previous events for this forecast point?
     */
    private boolean previousProductAvailable;

    /**
     * Previous event's observed value
     */
    private double previousCurObsValue;

    /**
     * Time of the previous event's observed value.
     */
    private Date previousCurrentObsTime;

    /**
     * Previous event's max forecast value
     */
    private double previousMaxFcstValue;

    /**
     * Previous event's max forecast time
     */
    private Date previousMaxFcstTime;

    /**
     * Previous event's max crest time
     */
    private Date previousMaxFcstCTime;

    /**
     * Previous event's maximum observed or forecast category
     */
    private int previousMaxObservedForecastCategory;

    /**
     * Current observation
     */
    private SHEFObservation currentObservation;

    /**
     * Current observation flood category
     */
    private int currentObservationCategory;

    /**
     * maximum forecast value
     */
    private SHEFObservation maximumForecast;

    /**
     * maximum forecast flood category
     */
    private int maximumForecastCategory;

    /**
     * maximum observed forecast value
     */
    private double maximumObservedForecastValue;

    /**
     * maximum observed forecast category
     */
    private int maximumObservedForecastCategory;

    /**
     * maximum observed forecast time
     */
    private Date maximumObservedForecastTime;

    /*
     * Note that these variables refer to the rise or fall with reference to the
     * previous product, not the current time series.
     */
    /**
     * Rise or fall of current hydrograph crest with respect to previous event
     * hydrograph.
     */
    private HydroGraphTrend riseOrFall;

    /**
     * Rise or fall of observed data with respect to previous event observed
     * data
     */
    private HydroGraphTrend observedRiseOrFall;

    /**
     * Rise or fall of forecast data with respect to previous event forecast
     * data
     */
    private HydroGraphTrend forecastRiseOrFall;

    /*
     * observed and forecast values for complete timeseries. loaded in
     * get_obsfcst_ts() in get_stages.c/ keep track of when the full time series
     * data are loaded. the use_obsH keeps track of how much of the observed
     * time series to use, based on the previous VTEC event end time.
     */

    /**
     * number of observations in hydrograph
     */
    private int numObsH;

    /**
     * The number of observations to use in recommendation
     */
    private int useObsH;

    /**
     * The last observation to use.
     */
    private Date obsCutoffTime;

    /**
     * The time to start loading observations at.
     */
    private Date obsLoadTime;

    /**
     * The observed hydrograph
     */
    private Hydrograph observedHydrograph;

    /**
     * The number of forecast values in the forecast hydrograph
     */
    private int numFcstH;

    /**
     * The forecast hydrograph
     */
    private Hydrograph forecastHydrograph;

    /**
     * The load time for the next possible pass into this function
     */
    private Date fullTimeSeriesLoadedTime;

    /**
     * maximum observation data index
     */
    private int observedMaximumIndex;

    /**
     * departure of observed value from flood stage
     */
    private double observedFloodStageDeparture;

    /**
     * Index of 24 hour maximum observation
     */
    private int observedMax24Index;

    /**
     * Index of 6 hour maximum observation
     */
    private int observedMax06Index;

    /*
     * values that may or may not be set depending upon the situation, but will
     * only be set if at least one stage value is available; these values are
     * determined via compute_obs_info()
     */
    /**
     * The time the observed hydrograph falls below flood stage
     */
    private Date observedFallBelowTime;

    /**
     * The time the observed hydrograph rises above flood stage
     */
    private Date observedRiseAboveTime;

    /**
     * The time the observed hydrograph crests
     */
    private double observedCrestValue;

    /**
     * the time of the observed hydrograph crest
     */
    private Date observedCrestTime;

    /*
     * derived forecast values from the full timeseries these values that will
     * be set if at least one stage value
     */

    /**
     * Index of maximum forecast element
     */
    private int maximumForecastIndex;

    /**
     * Forecast departure from flood stage
     */
    private double forecastFloodStageDeparture;

    /**
     * Index of forecast crest.
     */
    private int forecastXfCrestIndex;

    /*
     * Values that may or may not be set depending upon the situation, but will
     * only be set if at least one stage value available; these values are
     * determined via compute_fcst_info()
     */

    /**
     * The time the forecast hydrograph drops below flood stage.
     */
    private Date forecastFallBelowTime;

    /**
     * The time the forecast hydrograph rises above flood stage.
     */
    private Date forecastRiseAboveTime;

    /**
     * The value of the forecast hydrograph crest.
     */
    private double forecastCrestValue;

    /**
     * The time of the forecast crest.
     */
    private Date forecastCrestTime;

    /*
     * Overall fall, rise times
     */
    /**
     * fall below flood stage time.
     */
    private Date fallBelowTime;

    /**
     * rise above flood stage time.
     */
    private Date riseAboveTime;

    /*
     * The type source corresponding to the overall fall, rise, crest
     */
    /**
     * fall below data type source.
     */
    private String fallBelowTS;

    /**
     * rise above data type source.
     */
    private String riseAboveTS;

    /**
     * Crest type source.
     */
    private String crestTS;

    /*
     * Trend values; for observed trend and overall trend
     */

    /**
     * overall trend in observed and forecast hydrographs
     */
    private HydroGraphTrend trend;

    /**
     * Whether or not this forecast point needs to be included in the hazard
     * recommendation
     */
    private boolean includedInRecommendation;

    /**
     * The coordinates of this station's location
     */
    private double latitude;

    private double longitude;

    /**
     * The flood recommender data accessor object.
     */
    private IFloodRecommenderDAO floodDAO;

    /**
     * Default constructor
     */
    public RiverForecastPoint() {
    }

    /**
     * Creates a river forecast point object
     * 
     * @param forecastPointInfo
     *            Information specific to this forecast point
     * @param floodDAO
     *            data accessor object
     */
    public RiverForecastPoint(Object[] forecastPointInfo,
            IFloodRecommenderDAO floodDAO) {
        this.floodDAO = floodDAO;
        loadForecastPointData(forecastPointInfo);
    }

    /**
     * Given information pertaining to this forecast point, load it into this
     * forecast point object and perform any necessary processing.
     * 
     * @param forecastPointInfo
     *            The information pertaining to this forecast point.
     */
    private void loadForecastPointData(Object[] forecastPointInfo) {
        // Load in those fields that map directly...
        this.id = (String) forecastPointInfo[FpInfoFieldEnum.LID.ordinal()];
        this.name = (String) forecastPointInfo[FpInfoFieldEnum.NAME.ordinal()];
        this.county = (String) forecastPointInfo[FpInfoFieldEnum.COUNTY
                .ordinal()];
        this.state = (String) forecastPointInfo[FpInfoFieldEnum.STATE.ordinal()];
        this.stream = (String) forecastPointInfo[FpInfoFieldEnum.STREAM
                .ordinal()];
        this.proximity = (String) forecastPointInfo[FpInfoFieldEnum.PROXIMITY
                .ordinal()];
        this.reach = (String) forecastPointInfo[FpInfoFieldEnum.REACH.ordinal()];
        this.groupId = (String) forecastPointInfo[FpInfoFieldEnum.GROUP_ID
                .ordinal()];
        this.physicalElement = (String) forecastPointInfo[FpInfoFieldEnum.PE
                .ordinal()];
        this.hsa = (String) forecastPointInfo[FpInfoFieldEnum.HSA.ordinal()];
        this.primaryBackup = (String) forecastPointInfo[FpInfoFieldEnum.PRIMARY_BACK
                .ordinal()];
        this.secondaryBackup = (String) forecastPointInfo[FpInfoFieldEnum.SECONDARY_BACK
                .ordinal()];

        /*
         * This field will be used to determine whether or not to load the
         * latest timeseries.
         */
        if (((String) forecastPointInfo[FpInfoFieldEnum.USE_LATEST_FCST
                .ordinal()]).equals("T")) {
            this.useLatestForecast = true;
        } else {
            this.useLatestForecast = false;
        }

        /*
         * Retrieve the change threshold used in the load_detail_trend_info
         * function.
         */
        if (forecastPointInfo[FpInfoFieldEnum.CHG_THRESHOLD.ordinal()] != null) {
            this.changeThreshold = (Double) forecastPointInfo[FpInfoFieldEnum.CHG_THRESHOLD
                    .ordinal()];
        } else {
            statusHandler
                    .info("Missing the change of threshold for " + this.id);
            this.changeThreshold = MISSINGVAL;
        }

        if (forecastPointInfo[FpInfoFieldEnum.BACK_HRS.ordinal()] != null) {
            this.lookBackHours = (Integer) forecastPointInfo[FpInfoFieldEnum.BACK_HRS
                    .ordinal()];
        } else {
            this.lookBackHours = floodDAO
                    .getLookBackHoursForAllForecastPoints();
        }

        if (forecastPointInfo[FpInfoFieldEnum.FORWARD_HRS.ordinal()] != null) {
            this.lookFowardHours = (Integer) forecastPointInfo[FpInfoFieldEnum.FORWARD_HRS
                    .ordinal()];
        } else {
            this.lookFowardHours = floodDAO
                    .getLookForwardHoursForAllForecastPoints();
        }

        if (forecastPointInfo[FpInfoFieldEnum.ADJUST_END_HRS.ordinal()] != null) {
            this.adjustEndHrs = (Double) forecastPointInfo[FpInfoFieldEnum.ADJUST_END_HRS
                    .ordinal()];
        } else {
            this.adjustEndHrs = floodDAO.getShiftHoursForAllForecastPoints();
        }

        /*
         * Steal to use the rec_type field in rpffcstpoint. It was previously
         * used for recommendation type. Now it is used to determine whether to
         * use stage or flow for the impact/crest variables. If it is "PE", this
         * means the value will be based on the primary pe. If it is "NPE", then
         * it means the opposite.
         */

        if (forecastPointInfo[FpInfoFieldEnum.REC_TYPE.ordinal()] != null) {
            this.recommendationType = (String) forecastPointInfo[FpInfoFieldEnum.REC_TYPE
                    .ordinal()];
        } else {
            this.recommendationType = "PE";
        }

        /*
         * Load the flood stage, flood flow, bankfull stage, and warning stage
         * and check that the data are specified.
         */

        if (forecastPointInfo[FpInfoFieldEnum.FS.ordinal()] != null) {
            this.floodStage = (Double) forecastPointInfo[FpInfoFieldEnum.FS
                    .ordinal()];
        } else {
            statusHandler.info("Missing flood stage for " + this.id);
        }

        if (forecastPointInfo[FpInfoFieldEnum.FQ.ordinal()] != null) {
            this.floodFlow = (Double) forecastPointInfo[FpInfoFieldEnum.FQ
                    .ordinal()];
        } else {
            statusHandler.info("Missing flood flow for " + this.id);
        }

        if (forecastPointInfo[FpInfoFieldEnum.BF.ordinal()] != null) {
            this.bankFull = (Double) forecastPointInfo[FpInfoFieldEnum.BF
                    .ordinal()];
        } else {
            statusHandler.info("Missing bankfull stage for " + this.id);
        }

        if (forecastPointInfo[FpInfoFieldEnum.WSTG.ordinal()] != null) {
            this.actionStage = (Double) forecastPointInfo[FpInfoFieldEnum.WSTG
                    .ordinal()];
        } else {
            statusHandler.info("Missing warning stage for " + this.id);
        }

        if (forecastPointInfo[FpInfoFieldEnum.ACTION_FLOW.ordinal()] != null) {
            this.actionFlow = (Double) forecastPointInfo[FpInfoFieldEnum.ACTION_FLOW
                    .ordinal()];
        } else {
            statusHandler.info("Missing action flow for " + this.id);
        }

        /*
         * Load the categorical values. Note that cat[0] is unused to ensure
         * that fldcat1 agrees with cat[1] for convenience sake. This works
         * since category 0 is used to imply a no flood condition.
         */

        /*
         * Load the minor/moderate/major stage or flow based on the primary_pe
         * specified for the station.
         */
        String primaryPE = (String) forecastPointInfo[FpInfoFieldEnum.PE
                .ordinal()];
        char peFirstChar = primaryPE.charAt(0);
        char peSecondChar = primaryPE.charAt(1);

        if (peFirstChar != 'Q') {
            if (forecastPointInfo[FpInfoFieldEnum.MINOR_STAGE.ordinal()] != null) {
                this.floodCategory[HydroFloodCategories.MINOR_FLOOD_CATEGORY
                        .getRank()] = (Double) forecastPointInfo[FpInfoFieldEnum.MINOR_STAGE
                        .ordinal()];
            }

            if (forecastPointInfo[FpInfoFieldEnum.MODERATE_STAGE.ordinal()] != null) {
                this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                        .getRank()] = (Double) forecastPointInfo[FpInfoFieldEnum.MODERATE_STAGE
                        .ordinal()];
            }

            if (forecastPointInfo[FpInfoFieldEnum.MAJOR_STAGE.ordinal()] != null) {
                this.floodCategory[HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                        .getRank()] = (Double) forecastPointInfo[FpInfoFieldEnum.MAJOR_STAGE
                        .ordinal()];
            }

        } else if (peSecondChar != 'B' && peSecondChar != 'C'
                && peSecondChar != 'E' && peSecondChar != 'F'
                && peSecondChar != 'V') {
            /*
             * Only load Q* PE's if there are not certain types of non-flow
             * based Q* types.
             */
            if (forecastPointInfo[FpInfoFieldEnum.MINOR_FLOW.ordinal()] != null) {
                this.floodCategory[HydroFloodCategories.MINOR_FLOOD_CATEGORY
                        .getRank()] = (Double) forecastPointInfo[FpInfoFieldEnum.MINOR_FLOW
                        .ordinal()];
            }

            if (forecastPointInfo[FpInfoFieldEnum.MODERATE_FLOW.ordinal()] != null) {
                this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                        .getRank()] = (Double) forecastPointInfo[FpInfoFieldEnum.MODERATE_FLOW
                        .ordinal()];
            }

            if (forecastPointInfo[FpInfoFieldEnum.MAJOR_FLOW.ordinal()] != null) {
                this.floodCategory[HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                        .getRank()] = (Double) forecastPointInfo[FpInfoFieldEnum.MAJOR_FLOW
                        .ordinal()];
            }

        }

        /*
         * Load the record stage or flow from the crest table based on the
         * primary_pe for the station and store as a categorical value.
         */
        if (peFirstChar != 'Q') {
            List<Object[]> crestResults = floodDAO
                    .getStageCrestHistory(this.id);

            if (crestResults != null && crestResults.size() > 0) {
                Object[] record = crestResults.get(0);
                double stage = (Double) record[0];

                if (stage != 0) {
                    this.floodCategory[HydroFloodCategories.RECORD_FLOOD_CATEGORY
                            .getRank()] = stage;
                }
            }

        } else if (peSecondChar != 'B' && peSecondChar != 'C'
                && peSecondChar != 'E' && peSecondChar != 'F'
                && peSecondChar != 'V') {
            List<Object[]> crestResults = floodDAO.getFlowCrestHistory(this.id);

            if (crestResults != null && crestResults.size() > 0) {
                Object[] record = crestResults.get(0);
                Double q = (Double) record[0];

                if (q != 0) {
                    this.floodCategory[HydroFloodCategories.RECORD_FLOOD_CATEGORY
                            .getRank()] = q;
                }
            }

        }

        if (this.floodCategory[1] == MISSINGVAL
                || this.floodCategory[2] == MISSINGVAL
                || this.floodCategory[3] == MISSINGVAL
                || this.floodCategory[4] == MISSINGVAL) {
            statusHandler
                    .info("Missing the change of threshold for " + this.id);
        }
        /*
         * Check the validity of the minor, moderate and major stage data.
         */
        if (this.floodCategory[HydroFloodCategories.MINOR_FLOOD_CATEGORY
                .getRank()] != MISSINGVAL
                && this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                        .getRank()] != MISSINGVAL
                && this.floodCategory[HydroFloodCategories.MINOR_FLOOD_CATEGORY
                        .getRank()] > this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                        .getRank()]) {
            statusHandler.info("Non-increasing order of categories for "
                    + this.id);
        }

        if (this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                .getRank()] != MISSINGVAL
                && this.floodCategory[HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                        .getRank()] != MISSINGVAL
                && this.floodCategory[HydroFloodCategories.MODERATE_FLOOD_CATEGORY
                        .getRank()] > this.floodCategory[HydroFloodCategories.MAJOR_FLOOD_CATEGORY
                        .getRank()]) {
            statusHandler.info("Non-increasing order of categories for "
                    + this.id);
        }

        /*
         * Initialize observed/forecast time-series related variables.
         */
        this.numObsH = 0;
        this.numFcstH = 0;
        this.useObsH = 0;
        this.observedHydrograph = null;
        this.forecastHydrograph = null;
        this.fullTimeSeriesLoadedTime = null;

        List<Object[]> locationResults = floodDAO
                .getForecastPointCoordinates(this.id);
        Object[] latlon = locationResults.get(0);
        this.latitude = (Double) latlon[0];
        this.longitude = convertHydroLongitudesToWesternHemisphere((Double) latlon[1]);
    }

    /**
     * 
     * @param
     * @return the identifier of this forecast point
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param
     * @return the group id of this forecast point
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * 
     * @param
     * @return the observed value of the previous recommended event
     */
    public double getPreviousCurObsValue() {
        return previousCurObsValue;
    }

    /**
     * 
     * @param
     * @return The time of the previous observed value
     */
    public Date getPreviousCurrentObsTime() {
        return previousCurrentObsTime;
    }

    /**
     * @return the previous maximum forecast value
     */
    public double getPreviousMaxFcstValue() {
        return previousMaxFcstValue;
    }

    /**
     * @return the previous maximum forecast time
     */
    public Date getPreviousMaxFcstTime() {
        return previousMaxFcstTime;
    }

    /**
     * @return the previous maximum forecast crest time
     */
    public Date getPreviousMaxFcstCTime() {
        return previousMaxFcstCTime;
    }

    /**
     * @return the previous event maximum observed forecast category
     */
    public int getPreviousMaxObservedForecastCategory() {
        return previousMaxObservedForecastCategory;
    }

    /**
     * Loads the observed forecast values from a previous event.
     * 
     * @param previousEventDict
     */
    public void loadObservedForecastValues(Dict previousEventDict) {
        if (previousEventDict != null) {
            this.previousProductAvailable = true;

            Double obsValue = previousEventDict
                    .getDynamicallyTypedValue("currentObsValue");
            this.previousCurObsValue = obsValue;
            Long previousCurObsTime = previousEventDict
                    .getDynamicallyTypedValue("currentObsValueTime");
            this.previousCurrentObsTime = new Date(previousCurObsTime);
            Double maxFcstValue = previousEventDict
                    .getDynamicallyTypedValue("maxForecastValue");
            this.previousMaxFcstValue = maxFcstValue;
            Long maxForecastTime = previousEventDict
                    .getDynamicallyTypedValue("maxForecastTime");
            this.previousMaxFcstTime = new Date(maxForecastTime);
            Long maxForecastCTime = previousEventDict
                    .getDynamicallyTypedValue("maxForecastCTime");
            this.previousMaxFcstCTime = new Date(maxForecastCTime);
            Integer previousMaxObservedForecastCategory = previousEventDict
                    .getDynamicallyTypedValue("maxObservedForecastCategory");
            this.previousMaxObservedForecastCategory = previousMaxObservedForecastCategory;
        } else {
            this.previousProductAvailable = false;
            this.previousCurObsValue = MISSINGVAL;
            this.previousCurrentObsTime = null;
            this.previousMaxFcstValue = MISSINGVAL;
            this.previousMaxFcstTime = null;
            this.previousMaxFcstCTime = null;
            this.previousMaxObservedForecastCategory = HydroFloodCategories.NULL_CATEGORY
                    .getRank();
        }
    }

    /**
     * Loads or reloads updated observed and forecast data into the hydrographs
     * contained by this forecast point.
     */
    public void loadTimeSeries() {
        int obshrs;
        int fcsthrs;

        long obsBtime;
        long obsEtime;
        long fcstEtime;
        long basisBtime;

        /*
         * Retrieve the system time. This *could* be in DRT.
         */
        long systemTime = floodDAO.getSystemTime().getTime();

        obsEtime = systemTime;

        /*
         * Get some default time window values for observed look back hours,
         * forecast look forward hours and basis time look back hours. This
         * information is already included as a part of the river forecast
         * point.
         */
        obshrs = this.lookBackHours;
        fcsthrs = this.lookFowardHours;

        /*
         * Retrieve the obs/fcst time window limits based on points. The look
         * back hours/look hours should not be missing.
         */
        obsBtime = systemTime - (TimeUtil.MILLIS_PER_HOUR * obshrs);
        fcstEtime = systemTime + (TimeUtil.MILLIS_PER_HOUR * fcsthrs);
        basisBtime = systemTime
                - (TimeUtil.MILLIS_PER_HOUR * floodDAO
                        .getBasisHoursForAllForecastPoints());

        /*
         * Retrieve the current observed data from the RiverStatus table.
         */
        retrieveCurrentRiverObs(obshrs, systemTime, obsBtime, obsEtime,
                fcstEtime, basisBtime);

        /*
         * Load the forecast timeseries in RiverPro and find the maximum
         * forecast data within this forecast timeseries. The forecast
         * timeseries is limited from current time to the current time plus look
         * forward hours.
         */
        retrieveRiverForecast(fcstEtime, basisBtime);

        /*
         * Recompute the obs and forecast point mofo info. Always recompute the
         * prev info in the event that the previous info changes independent of
         * new time series data loaded in.
         */
        compute_fp_mofo_info();
        compute_fp_prev_info();

        /*
         * Print out a few diagnostics...
         */
        // String obsBtimeString = floodDAO.getDateFormat().format(
        // new Date(obsBtime));
        // String fcstEtimeString = floodDAO.getDateFormat().format(
        // new Date(fcstEtime));
        // String basisBtimeString = floodDAO.getDateFormat().format(
        // new Date(basisBtime));
        // System.out.printf(
        // "%s:%s: %s %d %s obs > %s; %s %d %s fcst < %s; basis > %s\n",
        // this.id, this.physicalElement, "obs loaded", this.numObsH, this
        // .getCurrentObservation().getTypeSource(),
        // obsBtimeString, "loaded", this.numFcstH, this
        // .getMaximumForecast().getTypeSource(), fcstEtimeString,
        // basisBtimeString);

        this.obsLoadTime = new Date(obsBtime);

        /*
         * Filter out old data if VTEC enabled for any VTEC significance and
         * data found. Otherwise, always use all obs data.
         * 
         * Skipped ... this is product-centric. May need to revisit this as this
         * seems to be the part where the obs timeseries is trimmed if there is
         * a previous product.
         */
        this.useObsH = this.numObsH;
        this.obsCutoffTime = new Date(obsBtime);

        /*
         * Always compute the derived stage values.
         */
        compute_stage_info();

    }

    /**
     * Retrieves the current observed data with lid, pe and ts as read from the
     * RiverStatus table. The retrieved type source should be between the look
     * back time and and the current time frame.
     * 
     * @param lid
     * @param pe
     * @param obsHrs
     */
    private void retrieveCurrentRiverObs(int obsHrs, long systemTime,
            long obsBtime, long obsEtime, long fcstEtime, long basisBtime) {
        boolean reloadObsTimeseries = false;

        boolean obsFound = false;
        SHEFObservation obsReport = new SHEFObservation();

        // Check this out to make sure it is actually doing something.
        int previousTSRank = 99;
        long beginValidTime = systemTime - obsHrs * TimeUtil.MILLIS_PER_HOUR;
        long bestObsValidTime = Long.MIN_VALUE;

        /*
         * Initialize this forecast point's obs values to defaults.
         */
        this.currentObservation = new SHEFObservation();
        this.currentObservation.setValue(MISSINGVAL);
        this.currentObservation.setValidTime(Long.MIN_VALUE);
        this.currentObservation.setTypeSource("");
        this.currentObservationCategory = HydroFloodCategories.NULL_CATEGORY
                .getRank();

        /*
         * In an effort to minimize reads of the database, get the RiverStatus
         * information all at once, for all ts's and for observed Data. There is
         * a validtime limit for observed data.
         */
        List<Object[]> riverStatusResults = floodDAO.retrieveRiverStatus(
                this.id, this.physicalElement, beginValidTime, systemTime);

        if ((riverStatusResults != null) && (riverStatusResults.size() > 0)) {
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
            List<Object[]> ingestResults = floodDAO.retrieveIngestSettings(id,
                    physicalElement);

            if (ingestResults != null && ingestResults.size() > 0) {
                /*
                 * Loop on the observed entries and try to get the observed
                 * data. Note that processed data is grouped in with observed
                 * data searches.
                 */
                for (Object[] ingestResult : ingestResults) {
                    /*
                     * Extract the type source and rank currently being
                     * considered.
                     */
                    String rankTS = ingestResult[0].toString();
                    int delimiterPosition = rankTS.indexOf('|');
                    String useRankString = rankTS.substring(0,
                            delimiterPosition);
                    String useTS = rankTS.substring(++delimiterPosition);
                    int useRank = Integer.parseInt(useRankString);

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
                        for (Object[] riverStatusRecord : riverStatusResults) {
                            /*
                             * Only use this river status entry if it is for the
                             * current ts and the validtime for the current ts
                             * is more recent than the previous validtime for ts
                             * with a possible matching rank.
                             */
                            String validTime = riverStatusRecord[6].toString();
                            String ts = riverStatusRecord[3].toString();

                            Date validDate = null;

                            try {
                                validDate = floodDAO.getDateFormat().parse(
                                        validTime);
                            } catch (ParseException e) {
                                /*
                                 * Print the exception and skip this record
                                 */
                                statusHandler.error(
                                        "Error parsing valid datetime", e);
                                continue;
                            }

                            long validTimeInMS = validDate.getTime();

                            if (ts.equals(useTS)
                                    && (validTimeInMS > bestObsValidTime)) {
                                obsReport
                                        .setPhysicalElement(riverStatusRecord[1]
                                                .toString());
                                obsReport.setDuration(Long
                                        .parseLong(riverStatusRecord[2]
                                                .toString()));
                                obsReport.setTypeSource(ts);
                                obsReport.setExtremum(riverStatusRecord[4]
                                        .toString().charAt(0));
                                obsReport.setValue(Double
                                        .parseDouble(riverStatusRecord[8]
                                                .toString()));
                                obsReport.setValidTime(validTimeInMS);
                                obsReport.setBasisTime(0);

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
         * Initialize the reload options. These are used to dictate whether to
         * reload the observed full time series data. Only consider the observed
         * data since the forecast time series is always reloaded.
         */
        reloadObsTimeseries = false;

        if (this.fullTimeSeriesLoadedTime == null) {
            reloadObsTimeseries = true;

            if (obsFound) {
                this.currentObservation = obsReport;
                this.currentObservationCategory = compute_stage_cat(obsReport
                        .getValue());
            }
        } else {
            /*
             * Check if there are new current observed data. If no data were
             * found then still try and load the full time series, if only to
             * have it set to missing. This strange condition could occur if the
             * latest obs was old and just barely in the time window at the time
             * of the previous retrieval but now is outside the window.
             */
            if (obsFound) {
                if (this.currentObservation.getValue() != obsReport.getValue()
                        || this.currentObservation.getValidTime() != obsReport
                                .getValidTime()) {
                    reloadObsTimeseries = true;
                    this.currentObservation = obsReport;
                }

            } else {
                reloadObsTimeseries = true;
            }
        }

        /* if loading any new time series data */
        if (reloadObsTimeseries) {
            /* reset a few values */
            this.numObsH = 0;
            this.useObsH = 0;

            /*
             * load the obs data with the ts as current obs ts, and the data
             * within the window between obsBtime and obsEtime
             */
            this.observedHydrograph = new Hydrograph(this.id,
                    this.physicalElement,
                    this.currentObservation.getTypeSource(), obsBtime,
                    obsEtime, floodDAO);

            this.numObsH = this.observedHydrograph
                    .getNumberOfShefDataElements();
            this.useObsH = this.numObsH;

            /*
             * Set the load time for the next possible pass into this function.
             */
            this.fullTimeSeriesLoadedTime = new Date(systemTime);
        }

        /*
         * Load the forecast timeseries in RiverPro and find the maximum
         * forecast data within this forecast timeseries. The fcst timeseries is
         * limited from current time to the current time plus the look forward
         * hours.
         */

    }

    /**
     * Retrieves the forecast portion of the timeseries.
     * 
     * @param fcstEtime
     *            The ending time of the forecast timeseries
     * @param basisBtime
     *            The forecast basis time (the time the forecast starts at)
     */
    private void retrieveRiverForecast(long fcstEtime, long basisBtime) {
        boolean maxForecastFound = false;

        /**
         * Initialize the forecast members of this forecast point to default
         * values.
         */
        this.maximumForecast = new SHEFObservation();
        this.maximumForecast.setValue(MISSINGVAL);
        this.maximumForecast.setValidTime(0);
        this.maximumForecast.setBasisTime(0);
        this.maximumForecast.setTypeSource("");
        this.maximumForecastCategory = HydroFloodCategories.NULL_CATEGORY
                .getRank();

        List<Object[]> ingestResults = floodDAO.getIngestTable();

        if ((ingestResults == null) || (ingestResults.size() == 0)) {
            return;
        }

        /*
         * Starting from the highest rank type source, load the timeseries and
         * find the maximum value
         */
        for (Object[] ingestRecord : ingestResults) {
            String[] fields = ingestRecord[0].toString().split("\\|");

            if (fields.length != 4) {
                /* An error was encountered parsing the unique string. */
                break;
            }

            String ts = fields[1];
            String lid = fields[2];
            String pe = fields[3];

            if (this.id.equals(lid) && this.physicalElement.equals(pe)) {
                /*
                 * perform the load_maxfcst operations for the specified lid,
                 * pe, ts
                 */
                maxForecastFound = loadForecastItem(ts, fcstEtime, basisBtime);

                if (maxForecastFound) {
                    break;
                }
            }
        }
    }

    /**
     * Loads the forecast timeseries for this forecast points lid|pe and
     * starting from highest type source. If forecast data are found, find the
     * maximum data.
     * 
     * @param ts
     * @param endValidTime
     * @param basisBTime
     * @return
     */
    private boolean loadForecastItem(String ts, long endValidTime,
            long basisBTime) {

        boolean useLatest;
        boolean maximumForecastFound = false;

        /*
         * Get the setting for the use_latest_fcst field for the current
         * location.
         */
        useLatest = this.useLatestForecast;

        /*
         * Retreive the forecast timeseries for this location, pe and ts using
         * any instructions on any type-source to screen and whether to use only
         * the latest basis time.
         */
        this.forecastHydrograph = new Hydrograph(this.id, this.physicalElement,
                ts, endValidTime, basisBTime, useLatest, floodDAO);

        if (this.forecastHydrograph.getNumberOfShefDataElements() > 0) {
            SHEFObservation maxForecastRecord = this.forecastHydrograph
                    .findMaxForecast();

            if (maxForecastRecord != null) {
                maximumForecastFound = true;
                this.numFcstH = forecastHydrograph
                        .getNumberOfShefDataElements();

                /*
                 * Load the maximum forecast data into this forecast point.
                 */
                this.maximumForecast.setValue(maxForecastRecord.getValue());
                this.maximumForecast.setValidTime(maxForecastRecord
                        .getValidTime());
                this.maximumForecast.setBasisTime(maxForecastRecord
                        .getBasisTime());
                this.maximumForecast.setTypeSource(maxForecastRecord
                        .getTypeSource());
                this.maximumForecastCategory = compute_stage_cat(maxForecastRecord
                        .getValue());
            }

        }

        return maximumForecastFound;
    }

    /**
     * Determines various values that use both observed and forecast stage data
     * for each forecast point. The information is defined in terms of index
     * values that refer to the specific item in the stage data structure.
     */
    private void compute_fp_mofo_info() {
        /* initialize */
        this.maximumObservedForecastValue = MISSINGVAL;
        this.maximumObservedForecastCategory = HydroFloodCategories.NULL_CATEGORY
                .getRank();
        this.maximumObservedForecastTime = null;

        /*
         * get the omf value and category values; this check works even if one
         * of the values are missing
         */
        if ((this.currentObservation.getValue() != MISSINGVAL)
                || this.maximumForecast.getValue() != MISSINGVAL) {
            if (this.currentObservation.getValue() > this.maximumForecast
                    .getValue()) {
                this.maximumObservedForecastValue = this.currentObservation
                        .getValue();
                this.maximumObservedForecastCategory = this.currentObservationCategory;
                this.maximumObservedForecastTime = new Date(
                        this.currentObservation.getValidTime());
            } else {
                this.maximumObservedForecastValue = this.maximumForecast
                        .getValue();
                this.maximumObservedForecastCategory = this.maximumForecastCategory;
                this.maximumObservedForecastTime = new Date(
                        this.maximumForecast.getValidTime());
            }
        }

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
     */
    private void compute_fp_prev_info() {
        int catval;

        /* set the rise fall flags to unchanged, not missing */
        this.observedRiseOrFall = HydroGraphTrend.UNCHANGED;
        this.forecastRiseOrFall = HydroGraphTrend.UNCHANGED;
        this.riseOrFall = HydroGraphTrend.UNCHANGED;

        if (this.previousProductAvailable) {
            if ((this.currentObservation.getValue() != MISSINGVAL)
                    && (this.previousCurObsValue != MISSINGVAL)) {
                /*
                 * if data are available and if the category for the current
                 * stage is greater than the category for the previous product,
                 * then a rise occurred
                 */

                catval = compute_stage_cat(this.previousCurObsValue);

                if (this.currentObservationCategory > catval) {
                    this.observedRiseOrFall = HydroGraphTrend.RISE;
                } else if (this.currentObservationCategory == catval) {
                    this.observedRiseOrFall = HydroGraphTrend.UNCHANGED;
                } else {
                    this.observedRiseOrFall = HydroGraphTrend.FALL;
                }
            }

            if ((this.maximumForecast.getValue() != MISSINGVAL)
                    && (this.previousMaxFcstValue != MISSINGVAL)) {
                /*
                 * if data are available and if the category for the stage is
                 * greater than the category for the previous product, then a
                 * rise occurred
                 */

                catval = compute_stage_cat(this.previousMaxFcstValue);

                if (this.maximumForecastCategory > catval) {
                    this.forecastRiseOrFall = HydroGraphTrend.RISE;
                } else if (this.maximumForecastCategory == catval) {
                    this.forecastRiseOrFall = HydroGraphTrend.UNCHANGED;
                } else {
                    this.forecastRiseOrFall = HydroGraphTrend.FALL;
                }
            }

            /*
             * get the previous obs and max fcst category if previous data
             * available for CAT threshold
             */
            if ((this.maximumObservedForecastCategory > HydroFloodCategories.NULL_CATEGORY
                    .getRank())
                    && (this.previousMaxObservedForecastCategory > HydroFloodCategories.NULL_CATEGORY
                            .getRank())) {
                if (this.maximumObservedForecastCategory > this.previousMaxObservedForecastCategory) {
                    this.riseOrFall = HydroGraphTrend.RISE;
                } else if (this.maximumObservedForecastCategory == this.previousMaxObservedForecastCategory) {
                    this.riseOrFall = HydroGraphTrend.UNCHANGED;
                } else {
                    this.riseOrFall = HydroGraphTrend.FALL;
                }
            }

        } /* end of if block on previous info available */

        return;
    }

    /**
     * Determine the stage category for a given stage value for a single
     * forecast point for general CAT threshold mode. Because of the system,
     * sometimes the float number is stored not exactly as it is shown. For
     * example, 23.1 is stored as 23.1000004. If the absolute difference between
     * cat_vals[i] and dataval is very small then they are considered equal.
     * 
     * @param dataValue
     * @return
     */
    private int compute_stage_cat(double dataValue) {
        int cat;

        cat = 0;

        if (dataValue == MISSINGVAL) {
            cat = HydroFloodCategories.NULL_CATEGORY.getRank();
        } else {
            for (int i = 1; i < MAX_CAT; ++i) {
                if (((dataValue >= this.floodCategory[i]) || (Math
                        .abs(dataValue - this.floodCategory[i]) < 0.0001))
                        && this.floodCategory[i] != MISSINGVAL) {
                    cat = i;
                }

            }
        }

        return cat;
    }

    /**
     * This function makes the calls to the other functions which determine
     * various information about the stage data.
     */
    private void compute_stage_info() {
        /* get the info on the observed stage data for forecast points */
        compute_obs_info();

        /* get the info on the forecast stage data for forecast points */
        compute_fcst_info();

        /* assign fp[].riseabove_time and fp[].fallbelow_time */
        compute_fp_risefall();

        /*
         * get the info on the trend data, which uses observed and forecast data
         */
        load_trend_info();
    }

    /**
     * Determines the derived values for the observed stage data for each
     * forecast point. The information is defined in terms of index values that
     * refer to the specific item in the stage data structure.
     * 
     * The fields based on the previous product information are computed
     * elsewhere.
     */
    private void compute_obs_info() {
        int k;
        float curstage;
        double maxstage;
        long system_time;
        int start_index, end_index;

        /* initialize all the data */
        int observedCurrentIndex = (int) MISSINGVAL;
        this.observedMaximumIndex = (int) MISSINGVAL;
        this.observedMax24Index = (int) MISSINGVAL;
        this.observedMax06Index = (int) MISSINGVAL;
        this.observedFloodStageDeparture = MISSINGVAL;
        this.observedRiseAboveTime = null;
        this.observedFallBelowTime = null;
        this.observedCrestValue = MISSINGVAL;
        this.observedCrestTime = null;

        /*
         * for the current pe and type-source for this station, get the time
         * series of data
         */

        /* if observed values available for this point, process the data */

        if (this.useObsH > 0) {
            /* get the max stage. if sustained max exists, get the most recent. */
            maxstage = MISSINGVAL;
            start_index = this.numObsH - this.useObsH;
            end_index = this.numObsH;
            List<SHEFObservation> obsH = this.observedHydrograph
                    .getShefHydroDataList();

            for (k = start_index; k < end_index; ++k) {
                double value = obsH.get(k).getValue();
                if (value >= maxstage && value != MISSINGVAL) {
                    maxstage = value;
                    this.observedMaximumIndex = k;
                }
            }

            /*
             * load index to the current stage value, checking for missing vals.
             * then get the flood departure. look in reverse order, accounting
             * for data usage
             */

            start_index = this.numObsH - 1;
            end_index = this.numObsH - this.useObsH;

            for (k = start_index; k >= end_index; --k) {
                double value = obsH.get(k).getValue();
                if (value != MISSINGVAL) {
                    observedCurrentIndex = k;

                    curstage = (float) obsH.get(observedCurrentIndex)
                            .getValue();

                    if (this.physicalElement.startsWith("Q")) {
                        if (this.floodFlow != MISSINGVAL) {
                            this.observedFloodStageDeparture = curstage
                                    - this.floodFlow;
                        }
                    }

                    else {
                        if (this.floodStage != MISSINGVAL) {
                            this.observedFloodStageDeparture = curstage
                                    - this.floodStage;
                        }
                    }

                    break;
                }
            }

            /* get the max value for 6 and 24 hour periods */

            system_time = floodDAO.getSystemTime().getTime();

            load_stage_in_interval(06, system_time);
            load_stage_in_interval(24, system_time);

            /* find the times that the stage crested or passed thru flood stage */
            load_special_stages(HydroDataType.OBS_DATA);
        }

        return;
    }

    /**
     * Determines the various aspects of the forecast stage data for each
     * forecast point. The information is defined in terms of index values that
     * refer to the specific item in the stage data structure.
     */
    private void compute_fcst_info() {
        int k;
        double maxstage;
        long maxbasistime, minvalidtime;

        /* initialize all the forecast point stage data */
        this.maximumForecastIndex = (int) MISSINGVAL;
        this.forecastFloodStageDeparture = MISSINGVAL;
        this.forecastXfCrestIndex = (int) MISSINGVAL;
        this.forecastRiseAboveTime = null;
        this.forecastFallBelowTime = null;
        this.forecastCrestValue = MISSINGVAL;
        this.forecastCrestTime = null;

        /* if forecast values available for this point, process the data */

        if (this.numFcstH > 0) {
            /*
             * loop on the number of forecasts for the current forecast point.
             * if the stage being checked exceeds the previous maximum, then
             * reset the maximum.
             */

            maxstage = MISSINGVAL;
            maxbasistime = (long) MISSINGVAL;
            minvalidtime = Long.MAX_VALUE;

            List<SHEFObservation> fcstH = this.forecastHydrograph
                    .getShefHydroDataList();

            for (k = 0; k < this.numFcstH; ++k) {
                double value = fcstH.get(k).getValue();

                if (value > maxstage && value != MISSINGVAL) {
                    maxstage = value;
                    this.maximumForecastIndex = k;
                }
            }

            if (this.physicalElement.startsWith("Q")) {
                if (this.floodFlow != MISSINGVAL && maxstage != MISSINGVAL) {
                    this.forecastFloodStageDeparture = maxstage
                            - this.floodFlow;
                }
            } else {
                if (this.floodStage != MISSINGVAL && maxstage != MISSINGVAL) {
                    this.forecastFloodStageDeparture = maxstage
                            - this.floodStage;
                }
            }

            /*
             * find the times that the stage crested or passed thru a flood
             * stage
             */
            load_special_stages(HydroDataType.FCST_DATA);

            /*
             * find index fcst_xrcrest_index which is the data of either FFX
             * data in the most recent basis time if exist or same as
             * fcst_crest_index.
             */
            for (k = 0; k < this.numFcstH; ++k) {
                long basistime = fcstH.get(k).getBasisTime();

                if (basistime > maxbasistime && basistime != MISSINGVAL) {
                    maxbasistime = basistime;

                }
            }

            for (k = 0; k < this.numFcstH; ++k) {
                long basistime = fcstH.get(k).getBasisTime();

                if ((basistime == maxbasistime) && (maxbasistime != MISSINGVAL)) {
                    /*
                     * look for the earliest forecast data with extremum as X
                     */
                    double value = fcstH.get(k).getValue();
                    long validtime = fcstH.get(k).getValidTime();
                    char extremum = fcstH.get(k).getExtremum();

                    if ((validtime < minvalidtime) && (validtime != MISSINGVAL)
                            && (extremum == 'X') && (value != MISSINGVAL)) {
                        minvalidtime = validtime;
                        this.forecastXfCrestIndex = k;
                    }
                }
            }
        }
    }

    /**
     * Retrieves a maximum observed stage within an given interval of hours.
     * 
     * @param interval
     *            interval in hours
     * @param system_time
     *            the current system time
     */
    private void load_stage_in_interval(int interval, long system_time) {
        int max_index;
        long num_secs;
        long begin_time;
        double maxval;
        int start_index;

        /* initialize */
        max_index = (int) MISSINGVAL;
        maxval = (int) MISSINGVAL;

        /*
         * define the beginning of the time window based on the current time and
         * the hour interval to look back
         */

        num_secs = interval * TimeUtil.MILLIS_PER_HOUR;
        begin_time = system_time - num_secs;

        /*
         * loop thru all the stage values in the time series. set the start
         * index, recognizing the order and usage specs of the data
         */
        start_index = this.numObsH - this.useObsH;

        List<SHEFObservation> obsH = this.observedHydrograph
                .getShefHydroDataList();

        for (int i = start_index; i < this.numObsH; i++) {

            double value = obsH.get(i).getValue();
            long validTime = obsH.get(i).getValidTime();

            if (value != MISSINGVAL) {
                if ((validTime - begin_time) > 0) {
                    if (max_index == MISSINGVAL) {
                        maxval = value;
                        max_index = i;
                    }

                    else if (value >= maxval) {
                        maxval = value;
                        max_index = i;
                    }
                }
            }
        }

        /* load the values in depending upon the time interval being considered */

        if (interval == 06) {
            this.observedMax06Index = max_index;
        } else if (interval == 24) {
            this.observedMax24Index = max_index;
        }

    }

    /**
     * Determines special flood stage values for a time series, this includes
     * the rise-above-flood, the fall-below-flood and crest. This function needs
     * to know whether it is dealing with observed or forecast values.
     * 
     * @param obs_or_fcst
     *            OBS_DATA or FCST_DATA
     */
    void load_special_stages(HydroDataType obs_or_fcst) {
        int numvals, numobs, numfcst, i;
        double stage, prev_stage, fld_level;
        long riseabove_time, fallbelow_time;
        int start_index, crest_index, sustained_crest_index;
        HydroGraphTrend prev_trend;
        HydroGraphTrend cur_trend;
        boolean crest_found, rise_found;
        long stagetime, prev_stagetime;
        double[] temp_value;
        long[] temp_timet;

        /* initialize ------------------------------------------------ */

        rise_found = false;
        riseabove_time = fallbelow_time = (long) MISSINGVAL;

        prev_trend = HydroGraphTrend.MISSING;
        cur_trend = HydroGraphTrend.MISSING;
        crest_found = false;
        crest_index = sustained_crest_index = (int) MISSINGVAL;

        numobs = numfcst = 0;

        /* can't do anything if not enough data */

        if ((obs_or_fcst == HydroDataType.OBS_DATA && this.useObsH == 0)
                || (obs_or_fcst == HydroDataType.FCST_DATA && this.numFcstH == 0)) {
            return;
        }

        /* load the flood stage/discharge into convenient variable. */

        if (this.physicalElement.startsWith("Q")) {
            fld_level = this.floodFlow;
        } else {
            fld_level = this.floodStage;
        }

        /* build a convenient time series -------------------------------- */

        /*
         * for obs, it only includes the portion of the time series to use,
         * which is constrained possibly by any preceding VTEC event, plus the
         * first of any forecast time series as the last value. for fcst, it
         * includes the full fcst time series, plus the latest obs value, if it
         * exists, as the first value. numvals is the total number of values in
         * the temporary time series. numobs is the number of additional obs
         * values in the fcst time series, which at most is one more,
         * representing the latest obs. numfcst is the number of additional fcst
         * value in the obs time series, which at most is one more, representing
         * the first forecast.
         */

        if (obs_or_fcst == HydroDataType.OBS_DATA) {
            if (this.numFcstH > 0) {
                numfcst = 1;
            } else {
                numfcst = 0;
            }

            numvals = this.useObsH + numfcst;
        }

        else {
            if (this.useObsH > 0) {
                numobs = 1;
            } else {
                numobs = 0;
            }

            numvals = this.numFcstH + numobs;
        }

        temp_value = new double[numvals];
        temp_timet = new long[numvals];

        /* load the temp data into the convenient arrays. */

        List<SHEFObservation> obsH = this.observedHydrograph
                .getShefHydroDataList();
        List<SHEFObservation> fcstH = this.forecastHydrograph
                .getShefHydroDataList();

        if (obs_or_fcst == HydroDataType.OBS_DATA) {
            start_index = this.numObsH - this.useObsH;

            for (i = 0; i < this.useObsH; i++) {
                temp_value[i] = obsH.get(start_index + i).getValue();
                temp_timet[i] = obsH.get(start_index + i).getValidTime();
            }

            if (numfcst > 0) {
                temp_value[numvals - 1] = fcstH.get(0).getValue();
                temp_timet[numvals - 1] = fcstH.get(0).getValidTime();
            }
        }

        else {
            if (numobs > 0) {
                start_index = this.numObsH - 1;

                temp_value[0] = obsH.get(start_index).getValue();
                temp_timet[0] = obsH.get(start_index).getValidTime();
            }

            for (i = 0; i < this.numFcstH; i++) {
                temp_value[numobs + i] = fcstH.get(i).getValue();
                temp_timet[numobs + i] = fcstH.get(i).getValidTime();
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

        for (i = 1; i < numvals; i++) {
            prev_stage = temp_value[i - 1];
            prev_stagetime = temp_timet[i - 1];

            stage = temp_value[i];
            stagetime = temp_timet[i];

            /* perform the trend check on the very first pair of values. */

            if (i == 1) {
                if (stage > prev_stage) {
                    prev_trend = HydroGraphTrend.RISE;
                } else if (stage == prev_stage) {
                    prev_trend = HydroGraphTrend.UNCHANGED;
                } else {
                    prev_trend = HydroGraphTrend.FALL;
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

                if (obs_or_fcst == HydroDataType.OBS_DATA || (!crest_found)) {
                    /* determine the current trend for later use */

                    if (stage > prev_stage) {
                        cur_trend = HydroGraphTrend.RISE;
                    } else if (stage == prev_stage) {
                        cur_trend = HydroGraphTrend.UNCHANGED;
                    } else {
                        cur_trend = HydroGraphTrend.FALL;
                    }

                    /*
                     * adjust the current trend value from unchanged to rise if
                     * the previous trend was a rise; this allows for the
                     * detection of sustained crests. also define the beginning
                     * of the sustained crest in the event that it undefined
                     */

                    if (cur_trend == HydroGraphTrend.UNCHANGED
                            && prev_trend == HydroGraphTrend.RISE) {
                        cur_trend = HydroGraphTrend.RISE;

                        if (sustained_crest_index == (int) MISSINGVAL) {
                            sustained_crest_index = i - 1;
                        }
                    } else if (cur_trend == HydroGraphTrend.RISE) {
                        sustained_crest_index = (int) MISSINGVAL;
                    }

                    /*
                     * assign the crest index where a crest occurs if the
                     * previous trend was a rise and the current trend is a fall
                     */

                    if (prev_trend == HydroGraphTrend.RISE
                            && cur_trend == HydroGraphTrend.FALL) {
                        if (sustained_crest_index == (int) MISSINGVAL) {
                            crest_index = i - 1;
                        } else {
                            crest_index = sustained_crest_index;
                        }

                        crest_found = true;
                    }

                    /*
                     * set the previous trend to be the current trend in
                     * preparation for the next time thru the loop
                     */

                    prev_trend = cur_trend;

                } /* end of if check on whether observed or crest_found */
            } /* working on second pair */
        } /* end of for loop */

        /* load the crest value and time */

        if (obs_or_fcst == HydroDataType.OBS_DATA) {
            if (crest_index != MISSINGVAL) {
                this.observedCrestValue = temp_value[crest_index];
                this.observedCrestTime = new Date(temp_timet[crest_index]);
            } else {
                this.observedCrestValue = MISSINGVAL;
                this.observedCrestTime = null;
            }
        }

        else {
            if (crest_index != MISSINGVAL) {
                this.forecastCrestValue = temp_value[crest_index];
                this.forecastCrestTime = new Date(temp_timet[crest_index]);
            } else {
                this.forecastCrestValue = MISSINGVAL;
                this.forecastCrestTime = null;
            }
        }

        /* compute the pass thru flood times ------------------------------ */

        /*
         * if flood stage not defined, force it to skip over the section below,
         * and return with the initialized missing values.
         */

        if (fld_level == MISSINGVAL) {
            numvals = 0;
        }

        /*
         * loop on the values in chronological order. note that the order of the
         * looping and the if checks affect how multiple crests or pass thru
         * events are handled. also note that in the event of a sustained stage
         * at flood stage, the pass thru stage time is the last in the series of
         * sustained values, not the first
         */

        for (i = 1; i < numvals; i++) {
            prev_stage = temp_value[i - 1];
            prev_stagetime = temp_timet[i - 1];

            stage = temp_value[i];
            stagetime = temp_timet[i];

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

            if (prev_stage < fld_level && fld_level <= stage) {
                if ((obs_or_fcst == HydroDataType.OBS_DATA && (numfcst == 0 || i != (numvals - 1)))
                        || (obs_or_fcst == HydroDataType.FCST_DATA && !rise_found)) {
                    rise_found = true;

                    if (prev_stage == stage) {
                        riseabove_time = prev_stagetime;
                    } else {
                        riseabove_time = stagetime
                                - (long) (((stage - fld_level) / (stage - prev_stage)) * (stagetime - prev_stagetime));
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

            if (prev_stage >= fld_level && fld_level > stage) {
                if ((obs_or_fcst == HydroDataType.OBS_DATA && (numfcst == 0 || i != (numvals - 1)))
                        || (obs_or_fcst == HydroDataType.FCST_DATA)) {

                    if (prev_stage == stage) {
                        fallbelow_time = prev_stagetime;
                    } else {
                        fallbelow_time = stagetime
                                - (long) (((stage - fld_level) / (stage - prev_stage)) * (stagetime - prev_stagetime));
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
        if (numvals >= 1 && obs_or_fcst == HydroDataType.FCST_DATA) {
            stage = temp_value[numvals - 1];

            if (stage > fld_level) {
                fallbelow_time = (long) MISSINGVAL;
            }
        }

        /* load the pass-thru time values */

        if (obs_or_fcst == HydroDataType.OBS_DATA) {
            if (fallbelow_time != (long) MISSINGVAL) {
                this.observedFallBelowTime = new Date(fallbelow_time);
            }

            if (riseabove_time != (long) MISSINGVAL) {
                this.observedRiseAboveTime = new Date(riseabove_time);
            }

            if (fallbelow_time < riseabove_time) {
                this.observedFallBelowTime = null;
            }
        }

        else {
            if (fallbelow_time != (long) MISSINGVAL) {
                this.forecastFallBelowTime = new Date(fallbelow_time);
            }

            if (riseabove_time != (long) MISSINGVAL) {
                this.forecastRiseAboveTime = new Date(riseabove_time);
            }

            if (fallbelow_time < riseabove_time) {
                this.forecastFallBelowTime = null;
            }
        }

    }

    /**
     * Determine the overall riseabove_time and fallbelow_time for the forecast
     * point.
     */

    private void compute_fp_risefall() {
        /* determine the rise above time */
        /*
         * if there are both obs and fcst times, use the obs rise above, and the
         * fcst fall below
         */

        if (this.observedRiseAboveTime != null) {
            this.riseAboveTime = this.observedRiseAboveTime;
            this.riseAboveTS = ALL_OBSERVED_TYPESOURCE;
        } else if (this.forecastRiseAboveTime != null) {
            this.riseAboveTime = this.forecastRiseAboveTime;
            this.riseAboveTS = ALL_FORECAST_TYPESOURCE;
        } else {
            this.riseAboveTime = null;
            this.riseAboveTS = "";
        }

        /* determine the fall below info. */

        if (this.forecastFallBelowTime != null) {
            this.fallBelowTime = this.forecastFallBelowTime;
            this.fallBelowTS = ALL_FORECAST_TYPESOURCE;
        }

        else if (this.observedFallBelowTime != null) {
            this.fallBelowTime = this.observedFallBelowTime;
            this.fallBelowTS = ALL_OBSERVED_TYPESOURCE;
        } else {
            this.fallBelowTime = null;
            this.fallBelowTS = "";
        }

        /* decide the crest_ts if both obs and fcst times exit */
        if (this.observedMaximumIndex != MISSINGVAL
                && this.maximumForecastIndex != MISSINGVAL) {
            List<SHEFObservation> obsH = this.observedHydrograph
                    .getShefHydroDataList();
            List<SHEFObservation> fcstH = this.forecastHydrograph
                    .getShefHydroDataList();

            if (obsH.get(this.observedMaximumIndex).getValue() >= fcstH.get(
                    this.maximumForecastIndex).getValue()) {
                this.crestTS = ALL_OBSERVED_TYPESOURCE;
            } else {
                this.crestTS = ALL_FORECAST_TYPESOURCE;
            }
        } else if (this.observedMaximumIndex != MISSINGVAL) {
            this.crestTS = ALL_OBSERVED_TYPESOURCE;
        } else if (this.maximumForecastIndex != MISSINGVAL) {
            this.crestTS = ALL_FORECAST_TYPESOURCE;
        } else {
            this.crestTS = "";
        }
    }

    /**
     * Loads the trend values using observed and forecast data. This function
     * also load the riseabove and fallbelow times or the observed and forecast
     * combination.
     */
    void load_trend_info() {
        double stageWindow;

        boolean min_set, max_set;
        double minstage, maxstage;
        long minstage_time, maxstage_time;
        double checkstage;
        long checkstage_time;
        double refstage;
        double compstage;
        int i, start_index, end_index;
        int num_fcst;

        /* initialize */
        HydroGraphTrend observedTrend = HydroGraphTrend.MISSING;
        this.trend = HydroGraphTrend.MISSING;

        minstage = maxstage = (long) MISSINGVAL;
        refstage = compstage = (long) MISSINGVAL;
        minstage_time = maxstage_time = 0;

        /*
         * set STAGE_WINDOW as fp[].chg_threshold, if fp[].chg_threshold is
         * missing, then use the value from token stage_window
         */
        if (this.changeThreshold != MISSINGVAL) {
            stageWindow = this.changeThreshold;
        } else {
            stageWindow = floodDAO.getDefaultStageWindow();
        }

        /*
         * first compute the observed trend. to do this, then we need to make
         * sure there are at least two observed values. use the current observed
         * value as the reference stage and compare it to the most recent max or
         * min value that is outside the stage window around the reference stage
         */
        List<SHEFObservation> obsH = this.observedHydrograph
                .getShefHydroDataList();

        if (this.useObsH > 1) {
            /*
             * find the latest non-missing obs value for use as the reference
             * stage. search the data based on the order and the usage of the
             * data.
             */

            start_index = this.numObsH - 1;
            end_index = this.numObsH - this.useObsH;

            for (i = start_index; i >= end_index; i--) {
                if (obsH.get(i).getValue() != MISSINGVAL) {
                    refstage = obsH.get(i).getValue();
                    break;
                }
            }

            /*
             * loop on all the stages and find the min or max that is outside
             * the stage window relative to the reference stage.
             */

            min_set = max_set = false;

            start_index = this.numObsH - this.useObsH;
            end_index = this.numObsH;

            for (i = start_index; i < end_index; i++) {

                checkstage = obsH.get(i).getValue();
                checkstage_time = obsH.get(i).getValidTime();

                /*
                 * if the min is not set yet, then initialize it. if it is set
                 * already, then check if there is a later min value. only
                 * consider values that are not close in value to the reference
                 * stage. note that in the event of duplicate mins/maxes, it
                 * uses the MOST RECENT min/max because the data is sorted from
                 * earliest to latest
                 */

                if (checkstage != MISSINGVAL && refstage != MISSINGVAL) {
                    if (checkstage < (refstage - stageWindow)
                            && (!min_set || checkstage <= minstage)) {
                        minstage = checkstage;
                        minstage_time = checkstage_time;
                        min_set = true;
                    }

                    if (checkstage > (refstage + stageWindow)
                            && (!max_set || checkstage >= maxstage)) {
                        maxstage = checkstage;
                        maxstage_time = checkstage_time;
                        max_set = true;
                    }
                }
            }

            /*
             * now that we have info on the min and max stages, find which is
             * the MOST RECENT and will be used to compare against the reference
             * stage.
             */

            if (min_set && max_set) {
                if (minstage_time > maxstage_time) {
                    compstage = minstage;
                } else {
                    compstage = maxstage;
                }
            } else if (min_set) {
                compstage = minstage;
            } else if (max_set) {
                compstage = maxstage;
            }
            /*
             * now determined the trend. if no mins or maxes were found outside
             * the stage window, then the trend is considered unchanged
             */

            if (min_set || max_set) {
                if (compstage > refstage) {
                    observedTrend = HydroGraphTrend.FALL;
                } else if (compstage < refstage) {
                    observedTrend = HydroGraphTrend.RISE;
                } else {
                    observedTrend = HydroGraphTrend.UNCHANGED;
                }
            }

            else {
                observedTrend = HydroGraphTrend.UNCHANGED;
            }
        } /* end of check if numobs used > 1 */

        /*
         * if only one obs value, set the reference stage of use in the overall
         * trend check below. the case of one obs value can occur because of the
         * vtec obs filter. note that this check does not account for the
         * possibility of a missing value indicator...
         */

        else if (this.useObsH == 1) {
            start_index = this.numObsH - 1;
            refstage = obsH.get(start_index).getValue();
        }

        /* -------------------------------------------------------------- */
        /* set a convenient local variable */

        num_fcst = this.numFcstH;

        /*
         * now compute the general trend. this uses forecast data to determine
         * the expected overall trend. if no forecast data exist, then the
         * general trend is set to be the same as the observed trend.
         */

        if (this.useObsH > 1 && num_fcst == 0) {
            this.trend = observedTrend;
        }

        /*
         * the reference stage is the latest observed which is compared to the
         * first max or min which is found in the forecast data, that is outside
         * the stage window around the reference stage. if no observed stage is
         * available use the first forecast value as the reference stage.
         */

        else if ((this.useObsH > 0 && num_fcst > 0)
                || (this.useObsH == 0 && num_fcst > 1)) {
            List<SHEFObservation> fcstH = this.forecastHydrograph
                    .getShefHydroDataList();

            /*
             * use the already determined refstage if obs data available. if no
             * obs available, use the first forecast value, assuming that it is
             * not missing, and set the start_index to bypass the first forecast
             * value.
             */

            if (this.useObsH > 0) {
                start_index = 0;
            }

            else {
                refstage = fcstH.get(0).getValue();
                start_index = 1;
            }

            min_set = max_set = false;

            for (i = start_index; i < this.numFcstH; i++) {
                checkstage = fcstH.get(i).getValue();
                checkstage_time = fcstH.get(i).getValidTime();

                /*
                 * if the min is not set yet, then initialize it. if it is set
                 * already, then check if there is a later min value. only
                 * consider values that are not close in value to the reference
                 * stage. note that in the event of duplicate mins/maxes, it
                 * uses the EARLIEST min/max because the data is sorted from
                 * earliest to latest
                 */

                if (checkstage != MISSINGVAL && refstage != MISSINGVAL) {
                    if (checkstage < (refstage - stageWindow)
                            && (!min_set || checkstage < minstage)) {
                        minstage = checkstage;
                        minstage_time = checkstage_time;
                        min_set = true;
                    }

                    if (checkstage > (refstage + stageWindow)
                            && (!max_set || checkstage > maxstage)) {
                        maxstage = checkstage;
                        maxstage_time = checkstage_time;
                        max_set = true;
                    }
                }
            }

            /*
             * now that we have the info on the min and max stages, find which
             * is the EARLIEST and will be used to compare against the reference
             * stage.
             */

            if (min_set && max_set) {
                if (minstage_time < maxstage_time) {
                    compstage = minstage;
                } else {
                    compstage = maxstage;
                }
            } else if (min_set) {
                compstage = minstage;
            } else if (max_set) {
                compstage = maxstage;
            }

            /*
             * now determined the trend. if no mins or maxes were found outside
             * the stage window, then the trend is considered unchanged
             */

            if (min_set || max_set) {
                if (compstage < refstage) {
                    this.trend = HydroGraphTrend.FALL;
                } else if (compstage > refstage) {
                    this.trend = HydroGraphTrend.RISE;
                } else {
                    this.trend = HydroGraphTrend.UNCHANGED;
                }
            } else {
                this.trend = HydroGraphTrend.UNCHANGED;
            }

        }

    }

    /**
     * @return the current observation flood category
     */
    public int getCurrentObservationCategory() {
        return currentObservationCategory;
    }

    /**
     * @return the current observation
     */
    public SHEFObservation getCurrentObservation() {
        return currentObservation;
    }

    /**
     * @return the maximum forecast flood category
     */
    public int getMaximumForecastCategory() {
        return maximumForecastCategory;
    }

    /**
     * @return the maximum forecast
     */
    public SHEFObservation getMaximumForecast() {
        return maximumForecast;
    }

    /**
     * @return the rise above flood stage time
     */
    public Date getRiseAboveTime() {
        return riseAboveTime != null ? new Date(riseAboveTime.getTime()) : null;
    }

    /**
     * Sets the virtual fall-below time based on any actual fall below time,
     * considering some special cases.
     * 
     * @param shiftFlag
     * @return
     */
    public Date getVirtualFallBelowTime(boolean shiftFlag) {
        double floodLevel;
        Date fallbelow_time = null;

        if (shiftFlag) {
            if (this.fallBelowTime != null) {
                Calendar fallBelowCalendar = Calendar.getInstance();
                fallBelowCalendar.setTime(this.fallBelowTime);

                if (this.adjustEndHrs != MISSINGVAL) {
                    fallBelowCalendar
                            .add(Calendar.SECOND,
                                    (int) (this.adjustEndHrs * TimeUtil.SECONDS_PER_HOUR));
                    fallbelow_time = fallBelowCalendar.getTime();
                } else {
                    fallBelowCalendar.add(Calendar.HOUR,
                            floodDAO.getShiftHoursForAllForecastPoints());
                    fallbelow_time = fallBelowCalendar.getTime();
                }
            }
        } else {
            fallbelow_time = this.fallBelowTime;
        }

        /*
         * adjust endtime for the special case where the forecast drops below
         * flood level, which results in valid fall-below time, but then it
         * rises above flood level. in this case, we don't know when the final
         * fall-below time is, so set it to missing/unknown.
         */
        if (this.numFcstH > 0) {
            if (this.physicalElement.startsWith("Q")) {
                floodLevel = this.floodFlow;
            } else {
                floodLevel = this.floodStage;
            }

            if (floodLevel != MISSINGVAL) {
                double value = this.forecastHydrograph.getShefHydroDataList()
                        .get(this.numFcstH - 1).getValue();

                if (value > floodLevel) {
                    fallbelow_time = null;
                }
            }
        }

        return fallbelow_time;
    }

    /**
     * @return the maximum observed forecast flood category
     */
    public int getMaximumObservedForecastCategory() {
        return maximumObservedForecastCategory;
    }

    /**
     * @return the trend of the hydrograph associated with this forecast point
     */
    public HydroGraphTrend getRiseOrFall() {
        return riseOrFall;
    }

    /**
     * Sets if this forecast point should be included in a hazard
     * recommendation.
     * 
     * @param includedInRecommendation
     *            whether or not to include this point in hazard recommendation
     */
    public void setIncludedInRecommendation(boolean includedInRecommendation) {
        this.includedInRecommendation = includedInRecommendation;
    }

    /**
     * @return whether or not this forecast point should be included in a
     *         recommendation
     */
    public boolean isIncludedInRecommendation() {
        return includedInRecommendation;
    }

    /**
     * @return the fall below flood stage time
     */
    public Date getFallBelowTime() {
        return fallBelowTime;
    }

    /**
     * @return the name of this forecast point
     */
    public String getName() {
        return name;
    }

    /**
     * @return this forecast points flood stage
     */
    public double getFloodStage() {
        return floodStage;
    }

    /**
     * @return this forecast points action stage
     */
    public double getActionStage() {
        return actionStage;
    }

    /**
     * @return a Coordinate representing this station's location
     */
    public Coordinate getLocation() {
        return new Coordinate(this.longitude, this.latitude);
    }

    /**
     * @return the observed crest time
     */
    public Date getObservedCrestTime() {
        return observedCrestTime;
    }

    /**
     * @return the forecast crest time
     */
    public Date getForecastCrestTime() {
        return forecastCrestTime;
    }

    /**
     * @return the maximum observed forecast value
     */
    public double getMaximumObservedForecastValue() {
        return maximumObservedForecastValue;
    }

    /**
     * @return the maximum observed forecast time
     */
    public Date getMaximumObservedForecastTime() {
        return maximumObservedForecastTime;
    }

    /**
     * @return the primary physical element
     */
    public String getPhysicalElement() {
        return physicalElement;
    }

    /**
     * @return the flood category reached by the data associated with this point
     */
    public double[] getFloodCategory() {
        return floodCategory;
    }

    /**
     * @return the proximity
     */
    public String getProximity() {
        return proximity;
    }

    /**
     * @return the river reach associated with this point
     */
    public String getReach() {
        return reach;
    }

    /**
     * @return the action flow value
     */
    public double getActionFlow() {
        return actionFlow;
    }

    /**
     * @return the county this point is in
     */
    public String getCounty() {
        return county;
    }

    /**
     * @return the state this point resides in
     */
    public String getState() {
        return state;
    }

    /**
     * @return the name of the stream this forecast point is on
     */
    public String getStream() {
        return stream;
    }

    /**
     * @return the bankfull stage
     */
    public double getBankFull() {
        return bankFull;
    }

    /**
     * @return the hsa this point belongs to
     * 
     */
    public String getHsa() {
        return hsa;
    }

    /**
     * @return the primary backup office for this point
     */
    public String getPrimaryBackup() {
        return primaryBackup;
    }

    /**
     * @return the recommendation type associated with this point
     */
    public String getRecommendationType() {
        return recommendationType;
    }

    /**
     * @return the secondary backup office for this point
     */
    public String getSecondaryBackup() {
        return secondaryBackup;
    }

    /**
     * @return the observed data cutoff time
     */
    public Date getObsCutoffTime() {
        return obsCutoffTime;
    }

    /**
     * @return the observed data load time
     */
    public Date getObsLoadTime() {
        return obsLoadTime;
    }

    /**
     * @return the observed hydrograph trend
     */
    public HydroGraphTrend getObservedRiseOrFall() {
        return observedRiseOrFall;
    }

    /**
     * @return the forecast hydrograph trend
     */
    public HydroGraphTrend getForecastRiseOrFall() {
        return forecastRiseOrFall;
    }

    /**
     * @return the observed flood stage departure
     */
    public double getObservedFloodStageDeparture() {
        return observedFloodStageDeparture;
    }

    /**
     * @return the index of the maximum 24 hour observation
     */
    public int getObservedMax24Index() {
        return observedMax24Index;
    }

    /**
     * @return the index of the maximu 6 hour observation
     */
    public int getObservedMax06Index() {
        return observedMax06Index;
    }

    /**
     * @return the observed crest value
     */
    public double getObservedCrestValue() {
        return observedCrestValue;
    }

    /**
     * @return the forecast flood stage departure
     */
    public double getForecastFloodStageDeparture() {
        return forecastFloodStageDeparture;
    }

    /**
     * @return the forecast maximum crest index (if there is more than one
     *         forecast timeseries)
     */
    public int getForecastXfCrestIndex() {
        return forecastXfCrestIndex;
    }

    /**
     * @return the forecast crest value
     */
    public double getForecastCrestValue() {
        return forecastCrestValue;
    }

    /**
     * @return the fallBelowTS
     */
    public String getFallBelowTS() {
        return fallBelowTS;
    }

    /**
     * @return the riseAboveTS
     */
    public String getRiseAboveTS() {
        return riseAboveTS;
    }

    /**
     * @return the crestTS
     */
    public String getCrestTS() {
        return crestTS;
    }

    /**
     * @return the trend
     */
    public HydroGraphTrend getTrend() {
        return trend;
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

}
