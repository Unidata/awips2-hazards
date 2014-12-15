/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.hazards.hydro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil.QUERY_MODE;
import com.raytheon.uf.common.dataplugin.shef.tables.Rpfparams;
import com.raytheon.uf.common.ohd.AppsDefaults;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.util.Pair;

/**
 * Description: Product data accessor implementation of the IFloodDAO.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer            Description
 * ------------ ---------- -----------         --------------------------
 * June 2011               bryon.lawrence      Initial creation
 * April 17, 2013          bryon.lawrence      Made a Singleton based on 
 *                                             code review feedback.
 * May 1, 2014  3581       bkowal              Relocate to common hazards hydro
 * Sep 19, 2014   2394     mpduff for nash     Updated for interface changes
 * Dec 17, 2014 2394       Ramer               Updated Interface
 * 
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class FloodDAO implements IFloodDAO {

    private static final String IHFS = "ihfs";

    private static final String MISSING_VALUE = "-9999";

    private static final String COLUMN_STAGE = "stage";

    private static final String COLUMN_Q = "q";

    /**
     * For logging...
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(FloodDAO.class);

    /**
     * Standard date format for hydro data.
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    /**
     * Default hours to look for forecast basis time.
     */
    public final static int DEFAULT_OBS_FCST_BASIS_HOURS = 72;

    /**
     * Default number of hours to shift forward flood event end time.
     */
    public final static int DEFAULT_ENDTIME_SHIFT_HOURS = 6;

    /**
     * The default stage window.
     */
    public final static float DEFAULT_STAGE_WINDOW = 0.5f;

    /**
     * The number of hours to shift event end times for all forecast points.
     */
    private int shiftHoursForAllForecastPoints;

    /**
     * The basis hours for all forecast points.
     */
    private int basisHoursForAllForecastPoints;

    /**
     * The observation data look back hours for all forecast points.
     */
    private int lookBackHoursForAllForecastPoints;

    /**
     * The forecast data look forward hours for all forecast points.
     */
    private int lookForwardHoursForAllForecastPoints;

    /**
     * The default stage buffer window.
     */
    private double defaultStageWindow;

    /**
     * The questionable/bad observed river data qc value.
     */
    static public final long QUESTIONABLE_BAD_THRESHOLD = 1073741824;

    /**
     * Results from the IHFS ingestfilter table.
     */
    private List<Object[]> ingestResults = null;

    /*
     * Initialize the back hours, forward hours, adjust end hours, and shift
     * hours fields. This just needs to be done once. These are the base values
     * for all forecast points.
     * 
     * Also, load only once information from the IngestFilter table.
     */
    static {
    }

    /**
     * Retrieve the time window hourly offsets for general use. These defaults
     * apply to all forecast points. Individual forecast points may override
     * them.
     */
    private void getHourValues() {
        lookBackHoursForAllForecastPoints = DEFAULT_OBS_FCST_BASIS_HOURS;
        lookForwardHoursForAllForecastPoints = DEFAULT_OBS_FCST_BASIS_HOURS;
        basisHoursForAllForecastPoints = DEFAULT_OBS_FCST_BASIS_HOURS;

        String query = "FROM "
                + com.raytheon.uf.common.dataplugin.shef.tables.Rpfparams.class
                        .getName();

        /*
         * Retrieve configuration information from the RpfParams table.
         */

        List<Object[]> rpfParmsList = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_HQLQUERY, query, IHFS, "RpfParams db");

        if (rpfParmsList != null && rpfParmsList.size() > 0) {
            Object[] paramObject = rpfParmsList.get(0);
            Rpfparams params = (Rpfparams) paramObject[0];
            lookBackHoursForAllForecastPoints = params.getId().getObshrs();
            lookForwardHoursForAllForecastPoints = params.getId().getFcsthrs();
        }

        AppsDefaults appsDefaults = AppsDefaults.getInstance();
        basisHoursForAllForecastPoints = appsDefaults.getInt(
                "basis_hours_filter", DEFAULT_OBS_FCST_BASIS_HOURS);
        shiftHoursForAllForecastPoints = appsDefaults.getInt(
                "rpf_endtime_shifthrs", DEFAULT_ENDTIME_SHIFT_HOURS);

        if (shiftHoursForAllForecastPoints < 0
                || shiftHoursForAllForecastPoints > 48) {
            statusHandler
                    .info("Error in specified value for token rpf_endtime_shifthrs.\n"
                            + "Using default value of "
                            + DEFAULT_ENDTIME_SHIFT_HOURS);
        }

        defaultStageWindow = appsDefaults.getDouble("rpf_stage_window",
                DEFAULT_STAGE_WINDOW);
    }

    /**
     * Singleton instance of this flood data access object
     */
    private static IFloodDAO floodDAOInstance = null;

    /**
     * Private constructor. This prevents it from being called and helps to
     * enforce the Singleton Pattern.
     * 
     * The getInstance method must be used to retrieve an instance of this
     * class.
     */
    private FloodDAO() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        getHourValues();
    }

    /**
     * Retrieves an instance of this flood recommender data access object. This
     * class follows the Singleton Pattern.
     * 
     * All instances of this object must be retrieved using this method.
     * 
     * @return An instance of this flood recommender data access object
     */
    public static IFloodDAO getInstance() {

        if (floodDAOInstance == null) {
            floodDAOInstance = new FloodDAO();
        }

        return floodDAOInstance;
    }

    /**
     * Retrieves a list of river forecast points.
     * 
     * @param hazardSettings
     *            Flood recommender configuration values
     * @return List of river forecast points.
     */
    @Override
    public List<RiverForecastPoint> getForecastPointInfo(
            HazardSettings hazardSettings) {
        /*
         * Retrieve all of the forecast group information, which has the defined
         * groups.
         */
        List<RiverForecastPoint> forecastPointList = Lists.newArrayList();
        String query;

        query = "SELECT * FROM FpInfo WHERE hsa = '" + hazardSettings.getHsa()
                + "' ORDER BY ordinal, lid ASC";

        List<Object[]> fpInfoResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "forecast point");
        /*
         * Loop on the number of groups defined in the table and determine the
         * number of forecast points included per group. This is necessary since
         * some groups may not be used, either because they have no forecast
         * points, or because their forecast points are for a different office.
         */
        if (fpInfoResults != null) {
            for (Object[] infoRecord : fpInfoResults) {
                if (infoRecord != null) {
                    // Create a new forecast point.
                    // reference to fp structure.
                    RiverForecastPoint fp = new RiverForecastPoint(infoRecord,
                            this);
                    forecastPointList.add(fp);
                }
            }
        }

        return forecastPointList;

    }

    /**
     * Retrieves a list of river forecast groups.
     * 
     * @param riverForecastPoints
     *            A list of river forecast points
     * @return List of river forecast groups.
     */
    @Override
    public List<RiverForecastGroup> getForecastGroupInfo(
            List<RiverForecastPoint> forecastPointList) {

        List<RiverForecastGroup> riverGroupList = Lists.newArrayList();
        String query = "SELECT * FROM rpffcstgroup ORDER BY ordinal, group_id ASC";

        List<Object[]> rpffcstgroupResults = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY, query, IHFS,
                        "forecast group");

        if (rpffcstgroupResults != null) {
            for (Object[] record : rpffcstgroupResults) {
                int forecastPointCounter = 0;
                ArrayList<RiverForecastPoint> forecastPointsInGroupList = new ArrayList<RiverForecastPoint>();
                String groupID = record[0].toString();

                for (RiverForecastPoint forecastPoint : forecastPointList) {

                    String forecastPointGroupID = forecastPoint.getGroupId();

                    if (groupID.equals(forecastPointGroupID)) {
                        forecastPointsInGroupList.add(forecastPoint);
                        forecastPointCounter++;
                    }

                }

                if (forecastPointCounter > 0) {
                    RiverForecastGroup riverGroup = new RiverForecastGroup(
                            forecastPointList, record,
                            forecastPointsInGroupList);
                    riverGroupList.add(riverGroup);
                }
            }
        }

        return riverGroupList;
    }

    /**
     * Retrieves the county river point forecast groups.
     * 
     * @param hazardSettings
     *            Flood recommender configuration values
     * @param forecastPointList
     *            List of river forecast points
     * @return List of county forecast groups
     */
    @Override
    public List<CountyForecastGroup> getForecastCountyGroups(
            HazardSettings hazardSettings,
            List<RiverForecastPoint> forecastPointList) {
        List<CountyForecastGroup> countyForecastGroupList = Lists
                .newArrayList();

        String query = "SELECT DISTINCT(county||'|'||state) "
                + "FROM Countynum " + "WHERE lid in "
                + "(SELECT lid FROM rpffcstpoint) " + "AND lid IN "
                + "(SELECT lid FROM location WHERE hsa='"
                + hazardSettings.getHsa() + "' "
                + "AND (type IS NULL OR type NOT LIKE '%I%'));";

        List<Object[]> countyStateList = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY, query, IHFS,
                        "forecast county group");

        if (countyStateList != null && countyStateList.size() > 0) {
            for (Object[] countyRecord : countyStateList) {
                CountyForecastGroup countyForecastGroup = new CountyForecastGroup(
                        forecastPointList, countyRecord,
                        hazardSettings.getHsa(), this);
                countyForecastGroupList.add(countyForecastGroup);
            }
        } else {
            statusHandler.info("No records in CountyNum table");
        }

        return countyForecastGroupList;

    }

    /**
     * Retrieves the configuration information which determines how the hazard
     * recommendation algorithm works.
     * 
     * @param
     * @return A HazardSettings object.
     */
    @Override
    public HazardSettings retrieveSettings() {
        /*
         * Create the SQL Query for the IHFS RpfParams table.
         */
        String query = "FROM "
                + com.raytheon.uf.common.dataplugin.shef.tables.Rpfparams.class
                        .getName();

        /*
         * Retrieve configuration information from the RpfParams table.
         */
        List<Object[]> rpfParmsList = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_HQLQUERY, query, IHFS, "IHFS hazard settings");
        HazardSettings hazardSettings = new HazardSettings();

        if (rpfParmsList != null && rpfParmsList.size() > 0) {
            Object[] paramObject = rpfParmsList.get(0);
            Rpfparams params = (Rpfparams) paramObject[0];

            hazardSettings.setRvsExpirationHours(params.getId().getRvsexphrs());
            hazardSettings.setFlsExpirationHours(params.getId().getFlsexphrs());
            hazardSettings.setFlwExpirationHours(params.getId().getFlwexphrs());
            hazardSettings.setObsLookbackHours(params.getId().getObshrs());
            hazardSettings.setForecastLookForwardHours(params.getId()
                    .getFcsthrs());
        } else {
            statusHandler.info("Could not load RpfParams information.");
        }

        query = "SELECT hsa from Admin";

        List<Object[]> adminList = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "admin");

        if (adminList != null && adminList.size() > 0) {
            Object[] adminRecord = adminList.get(0);
            hazardSettings.setHsa(adminRecord[0].toString());
        } else {
            statusHandler.info("Could not load Admin information");
        }

        hazardSettings.setDefaultTimeZone(System.getenv("TZ"));

        /*
         * Retrieve the defaults which define the flood record values.
         */
        AppsDefaults appsDefaults = AppsDefaults.getInstance();
        double vtecRecordStageOffset = appsDefaults.getDouble(
                "vtec_record_stageoffset",
                HazardSettings.DEFAULT_VTECRECORD_STAGE);
        double vtecRecordFlowOffset = appsDefaults.getDouble(
                "vtec_record_flowoffset",
                HazardSettings.DEFAULT_VTECRECORD_FLOW);

        if (vtecRecordStageOffset <= 0.0d) {
            vtecRecordStageOffset = HazardSettings.DEFAULT_VTECRECORD_STAGE;
        }

        if (vtecRecordFlowOffset <= 0.0d) {
            vtecRecordFlowOffset = HazardSettings.DEFAULT_VTECRECORD_FLOW;
        }

        hazardSettings.setVtecRecordStageOffset(vtecRecordStageOffset);
        hazardSettings.setVtecRecordFlowOffset(vtecRecordFlowOffset);

        return hazardSettings;

    }

    /**
     * Retrieves the most recent observation for a river forecast point.
     * 
     * @param id
     *            River forecast point identifier
     * @param physicalElement
     *            The physical element to retrieve the more recent datum for.
     * @param beginValidTime
     *            The earliest time to search for a value
     * @param systemTime
     *            The latest time to search for a value
     * @return A list of object arrays, where each array element corresponds to
     *         a column in the IHFS RiverStatus table. For Example,
     * 
     *         new Object[]{ "DCTN1", "HG", 0, "RG", "Z", -1,
     *         "2011-02-08 04:00:00", null, 39.04d }
     */
    @Override
    public List<Object[]> retrieveRiverStatus(String id,
            String physicalElement, long beginValidTime, long systemTime) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM RiverStatus ");
        query.append("WHERE lid = '" + id + "' ");
        query.append("AND pe = '" + physicalElement + "' ");
        query.append("AND ( validtime >= '" + dateFormat.format(beginValidTime)
                + "' ");
        query.append("AND validtime <= '" + dateFormat.format(systemTime)
                + "') ");
        query.append("AND basistime IS NULL ");

        List<Object[]> riverStatusResults = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                        query.toString(), IHFS, "RiverStatus");

        return riverStatusResults;

    }

    /**
     * Retrieves IngestFilter settings for a given station and physical element.
     * 
     * @param id
     *            Forecast point id
     * @param physicalElement
     *            The SHEF Physical element code
     * @return List of object arrays, where each array element corresponds to a
     *         pipe delimited string with values from the IngestFilter table:
     *         ts_rank | ts
     * 
     *         For example:
     * 
     *         new Object[] {"1|RX"}
     */
    @Override
    public List<Object[]> retrieveIngestSettings(String id,
            String physicalElement) {
        /*
         * Retrieve a unique list of entries for typesources that match the
         * given lid and pe. The ingestfilter entries are needed because they
         * contain the type-source rank information. Insert a comma in between
         * the two fields to make them easier to parse. Note that the ts rank
         * sort method will not handle numbers greater than 9 (i.e. 12 is ranked
         * higher than 3)! Also, note that the query does not filter out
         * extremums that are not "Z". Only bother retrieving info if
         * RiverStatus entries exist. We try and read RiverStatus first since
         * that table is smaller.
         */
        StringBuffer query = new StringBuffer(
                "SELECT DISTINCT(ts_rank||'|'||ts) ");
        query.append("FROM IngestFilter WHERE lid = '" + id + "' ");
        query.append("AND pe = '" + physicalElement + "' ");
        query.append("AND ingest = 'T' ORDER BY 1 ");

        List<Object[]> ingestResults = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                        query.toString(), IHFS, "IHFS ingest");

        return ingestResults;

    }

    /**
     * Retrieves data from the IHFS IngestFilter Table.
     * 
     * @param
     * @return A list of object arrays, where each array element corresponds to
     *         a pipe delimited string: ts_rank|ts|lid|pe
     * 
     *         For example: new Object[] {"1|FF|DCTN1|HG"}
     */
    @Override
    public List<Object[]> getIngestTable() {
        /*
         * load the type source which pe starts as 'H' or 'Q' and ts starts as
         * 'F'. Only load once.
         */
        if (ingestResults == null) {

            StringBuffer query = new StringBuffer(
                    "SELECT DISTINCT(ts_rank||'|'||ts||'|'||lid||'|'||pe) ");
            query.append("FROM IngestFilter WHERE ts LIKE 'F%%' ");
            query.append("AND ( pe LIKE 'H%%' OR pe LIKE 'Q%%') ");
            query.append("AND ingest = 'T'");

            ingestResults = DatabaseQueryUtil.executeDatabaseQuery(
                    QUERY_MODE.MODE_SQLQUERY, query.toString(), IHFS,
                    "IngestFilter");
        }

        return ingestResults;
    }

    /**
     * Retrieves the observed hydrograph for a river forecast point.
     * 
     * @param lid
     *            The river forecast point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF typesource code
     * @param obsBeginTime
     *            The lower bound of time window to retrieve observations for
     * @param obsEndTime
     *            The upper bound of the time window to retrieve observations
     *            for.
     * @return A list of Object arrays where each array corresponds to a row of
     *         data from either the IHFS Discharge or Height table.
     * 
     *         For example:
     * 
     *         new Object[] { "DCTN1", "HG", 0, "RG", "Z",
     *         "2011-02-07 17:00:00", 30.93, "Z", 1879048191, 0, "KWOHRRSOAX",
     *         "2011-02-07 17:03:00", "2011-02-07 17:04:19" });
     */
    @Override
    public List<Object[]> getRiverObservedHydrograph(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime) {
        /* Determine the table name to use. */
        String tableName;

        if (physicalElement.startsWith("Q") || physicalElement.startsWith("q")) {
            tableName = "discharge";
        } else {
            tableName = "height";
        }

        /*
         * Get the data for the specified time window and for the determined
         * PEDTSEP entry. Build the where clause depending upon whether
         * considering only passed qc data.
         */
        StringBuffer query = new StringBuffer("SELECT * FROM " + tableName
                + " ");
        query.append("WHERE lid = '" + lid + "' ");
        query.append("AND pe = '" + physicalElement + "' ");
        query.append("AND ts = '" + typeSource + "' ");
        query.append("AND obstime >= '"
                + dateFormat.format(new Date(obsBeginTime)) + "' ");
        query.append("AND obstime <= '"
                + dateFormat.format(new Date(obsEndTime)) + "' ");
        query.append("AND value != " + RiverForecastPoint.MISSINGVAL + " ");
        query.append("AND quality_code >= " + QUESTIONABLE_BAD_THRESHOLD + " ");
        query.append("ORDER BY obstime ASC ");

        List<Object[]> observationRecordList = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                        query.toString(), IHFS, "observed river hydrograph");

        return observationRecordList;
    }

    /**
     * Returns a list of basis times (the time each river forecast timeseries
     * was created).
     * 
     * @param lid
     *            River forecast point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF typesource code
     * @param systemTime
     *            The system time
     * @param endValidTime
     *            The latest possible forecast valid time
     * @param basisBTime
     *            The earliest basistime to accept
     * @return A list object arrays. Each array contains a single basistime
     *         value string.
     * 
     *         For example:
     * 
     *         new Object[]{ "2011-02-08 15:06:00" }
     */
    @Override
    public List<Object[]> getRiverForecastBasisTimes(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime) {
        /*
         * Set the table name to use.
         */
        String tableName;

        if (physicalElement.startsWith("h") || physicalElement.startsWith("H")) {
            tableName = "fcstheight";
        } else {
            tableName = "fcstdischarge";
        }

        /*
         * Retrieve a list of unique basis times; use descending sort. Only
         * consider forecast data before some ending time, and with some limited
         * basis time ago.
         */
        StringBuffer query = new StringBuffer();
        query.append("SELECT DISTINCT(basistime) FROM " + tableName + " ");
        query.append("WHERE lid = '" + lid + "' ");
        query.append("AND pe = '" + physicalElement + "' ");
        query.append("AND ts = '" + typeSource + "' ");
        query.append("AND probability < 0.0 ");
        query.append("AND ( validtime >= '" + dateFormat.format(systemTime)
                + "' ");
        query.append("AND validtime <= '" + dateFormat.format(endValidTime)
                + "') ");
        query.append("AND basistime >= '" + dateFormat.format(basisBTime)
                + "' ");
        query.append("AND value != " + RiverForecastPoint.MISSINGVAL + " ");
        query.append("AND quality_code >= " + QUESTIONABLE_BAD_THRESHOLD + " ");
        query.append("ORDER BY basistime DESC");

        List<Object[]> basisTimeResults = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                        query.toString(), IHFS, "river forecast basis time");

        return basisTimeResults;

    }

    /**
     * Retrieves the forecast hydrograph for a river forecast point.
     * 
     * @param lid
     *            The river forecast point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF typesource code
     * @param systemTime
     *            The system time
     * @param endValidTime
     *            The latest valid forecast time to accept
     * @param basisBTime
     *            The earliest basis time to search for
     * @param useLatestForecast
     *            Only consider the latest forecast
     * @param basisTimeResults
     *            Available forecast basis times
     * 
     * 
     * @return A list of Object arrays. Each array corresponds to one row from
     *         the IHFS FcstHeight or FcstDischarge tables.
     * 
     *         For example:
     * 
     *         new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
     *         "2011-02-08 18:00:00", "2011-02-08 15:06:00", 39.91, "Z",
     *         1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
     *         "2011-02-08 15:15:10" });
     */
    @Override
    public List<Object[]> getRiverForecastHydrograph(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime, boolean useLatestForecast,
            List<Object[]> basisTimeResults) {
        /*
         * Set the table name to use.
         */
        String tableName;

        if (physicalElement.startsWith("h") || physicalElement.startsWith("H")) {
            tableName = "fcstheight";
        } else {
            tableName = "fcstdischarge";
        }

        StringBuffer query = new StringBuffer();

        if (useLatestForecast || basisTimeResults.size() == 1) {
            Object[] record = basisTimeResults.get(0);
            String basisTime = record[0].toString();
            query.append("SELECT * FROM " + tableName + " ");
            query.append("WHERE lid = '" + lid + "' ");
            query.append("AND pe = '" + physicalElement + "' ");
            query.append("AND ts = '" + typeSource + "' ");
            query.append("AND probability < 0.0 ");
            query.append("AND ( validtime >= '" + dateFormat.format(systemTime)
                    + "' ");
            query.append("AND validtime <= '" + dateFormat.format(endValidTime)
                    + "') ");
            query.append("AND basistime = '" + basisTime + "' ");
            query.append("AND value != " + RiverForecastPoint.MISSINGVAL + " ");
            query.append("AND quality_code >= "
                    + Hydrograph.QUESTIONABLE_BAD_THRESHOLD + " ");
            query.append("ORDER BY validtime ASC");
        } else {
            query.append("SELECT * FROM " + tableName + " ");
            query.append("WHERE lid = '" + lid + "' ");
            query.append("AND pe = '" + physicalElement + "' ");
            query.append("AND ts = '" + typeSource + "' ");
            query.append("AND probability < 0.0 ");
            query.append("AND ( validtime >= '" + dateFormat.format(systemTime)
                    + "' ");
            query.append("AND validtime <= '" + dateFormat.format(endValidTime)
                    + "') ");
            query.append("AND basistime >= '" + dateFormat.format(basisBTime)
                    + "' ");
            query.append("AND value != " + RiverForecastPoint.MISSINGVAL + " ");
            query.append("AND quality_code >= "
                    + Hydrograph.QUESTIONABLE_BAD_THRESHOLD + " ");
            query.append("ORDER BY validtime ASC");
        }

        List<Object[]> forecastResults = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                        query.toString(), IHFS, "river forecast hydrograph");

        return forecastResults;
    }

    /**
     * Retrieves the given physical element for a river forecast point.
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
     *            The time specification e.g. 0|12:00|1 day|hh:mm|interval
     *            around hour in hours -- 0 today, 1 tomorrow etc.
     * @param derivationInstruction
     *            e.g. "Time", "Max24", etc.
     * 
     *            For example:
     * 
     *            new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
     *            "2011-02-08 18:00:00", "2011-02-08 15:06:00", 39.91, "Z",
     *            1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
     *            "2011-02-08 15:15:10" });
     * 
     */
    @Override
    public String getPhysicalElement(String lid, String physicalElement,
            int duration, String typeSource, String extremum, String timeArg,
            String derivationInstruction, boolean timeFlag) {

        /*
         * Set the table name to use.
         */
        String tableName = "";

        if ((physicalElement.startsWith("h") || physicalElement.startsWith("H"))
                && (typeSource.startsWith("f'") || typeSource.startsWith("F"))) {
            tableName = "fcstheight";
        } else if ((physicalElement.startsWith("q") || physicalElement
                .startsWith("Q"))
                && (typeSource.startsWith("f'") || typeSource.startsWith("F"))) {
            tableName = "fcstdischarge";
        } else if ((physicalElement.startsWith("h") || physicalElement
                .startsWith("H"))
                && (typeSource.startsWith("r'") || typeSource.startsWith("R"))) {
            tableName = "height";
        } else if ((physicalElement.startsWith("q") || physicalElement
                .startsWith("Q"))
                && (typeSource.startsWith("r'") || typeSource.startsWith("R"))) {
            tableName = "discharge";
        }

        StringBuffer query = new StringBuffer();
        String selectItem = "value, validTime";
        String basisTime = "";

        /*
         * if forecast: pull out basis time and send into next query to get
         * value I’m looking for
         * 
         * if obs use value passed in translate into database format -- see Date
         * formats)
         */

        if (typeSource.startsWith("f") || typeSource.startsWith("F")) {
            /*
             * pull out the basistime for the next query
             */
            query.append("SELECT basistime FROM " + tableName + " ");
            query.append("WHERE lid = '" + lid + "' ");
            query.append("AND pe = '" + physicalElement + "' ");
            query.append("AND ts = '" + typeSource + "' ");
            query.append("AND extremum = '" + extremum + "' ");
            query.append("ORDER BY basisTime desc limit 1");
            List<Object[]> basisTimes = DatabaseQueryUtil.executeDatabaseQuery(
                    QUERY_MODE.MODE_SQLQUERY, query.toString(), IHFS,
                    "basis time");
            if (basisTimes.isEmpty() == false) {
                basisTime = basisTimes.get(0)[0].toString();
            }
            /*
             * For typeSource "F" -- Building validTime from timeArg if 'NEXT',
             * validTime = current time else if x|HH:MM|y, current time + x days
             * and then find the validTime closest to the HH:MM within the +/- y
             * interval
             */
            // TODO, this needs to be passed in
            String currentTime = "2011-02-08 04:00:00.0";
            String validTimeCondition;
            if ("NEXT".equals(timeArg)) {
                query = new StringBuffer();
                validTimeCondition = ">= '" + currentTime + "'";
                query.append("SELECT " + selectItem + " FROM " + tableName
                        + " ");
                query.append("WHERE lid = '" + lid + "' ");
                query.append("AND pe = '" + physicalElement + "' ");
                query.append("AND ts = '" + typeSource + "' ");
                query.append("AND basistime = '" + basisTime + "' ");
                query.append("AND extremum = '" + extremum + "' ");
                query.append("AND validTime " + validTimeCondition + " ");
                query.append("ORDER BY validTime limit 1");
            } else {
                validTimeCondition = null;
            }
        } else {
            if (timeFlag) {
                selectItem = "obsTime";
            }
        }

        List<Object[]> forecastResults = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                        query.toString(), IHFS, "physical element");
        if (forecastResults.isEmpty() == false) {
            if (timeFlag) {
                return forecastResults.get(0)[1].toString();
            } else {
                return forecastResults.get(0)[0].toString();
            }
        }

        return MISSING_VALUE;

    }

    /**
     * @return the number of hours to look back for observed river data.
     */
    @Override
    public int getLookBackHoursForAllForecastPoints() {
        return lookBackHoursForAllForecastPoints;
    }

    /**
     * @return the number of hours to look into the future for forecast river
     *         data.
     */
    @Override
    public int getLookForwardHoursForAllForecastPoints() {
        return lookForwardHoursForAllForecastPoints;
    }

    /**
     * @return The buffer around a reference stage.
     */
    @Override
    public double getDefaultStageWindow() {
        return defaultStageWindow;
    }

    /**
     * 
     * @param
     * @return the max number of hours to look back for a river forecast basis
     *         time.
     */
    @Override
    public int getBasisHoursForAllForecastPoints() {
        return basisHoursForAllForecastPoints;
    }

    /**
     * 
     * @param
     * @return The number of hours to add to the fall below time.
     */
    @Override
    public int getShiftHoursForAllForecastPoints() {
        return shiftHoursForAllForecastPoints;
    }

    /**
     * @return the dateFormat
     */
    @Override
    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Determines the best TS to use for retrieval of observed hydrographs.
     * 
     * @param lid
     *            River forecast point identifier
     * @param pe
     *            The SHEF physical element code.
     * @param ts_prefix
     *            The type character in the SHEF typesource code
     * @param ordinal
     *            The type source ranking value
     * 
     * @return The typesource for which to retrieve data.
     */
    @Override
    public String getBestTS(String lid, String pe, String ts_prefix, int ordinal) {

        int cnt;
        String tsFound = null;

        /*
         * get the ingest filter entries for this location. note that the
         * retrieval is ordered so that if multiple best ranks exist, there is
         * some predictability for the identified best one. also note that this
         * approach ignores the duration, extremum, and probability code.
         */
        StringBuffer query = new StringBuffer("SELECT * ");
        query.append("FROM IngestFilter WHERE lid ='" + lid + "' ");
        query.append("AND pe  = '" + pe + "' ");
        query.append("AND ts LIKE '" + ts_prefix + "%' ");
        query.append("AND ingest = 'T' ORDER BY ts_rank, ts");

        List<Object[]> ingestResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query.toString(), IHFS,
                "IngestFilter table");

        if (ingestResults != null && ingestResults.size() > 0) {
            /*
             * if no specific ordinal number was requested, return with the
             * highest rank.
             */
            if (ordinal <= 0) {
                tsFound = ingestResults.get(0)[3].toString();
            }
            /* if a specific ordinal number was requested. */
            else {
                /*
                 * get a count of the number of matching ts entries. if the
                 * requested ordinal number is greater than the number available
                 * then return with a not found status.
                 */
                cnt = ingestResults.size();

                if (ordinal <= cnt) {
                    Object[] ingestRecord = ingestResults.get(ordinal);
                    tsFound = ingestRecord[3].toString();
                }
            }

        }

        return tsFound;
    }

    /**
     * 
     * @param state
     *            The two letter, state code (uppercase)
     * @param county
     *            The name of the county, first letter capitalized
     * @param HSA
     *            The HSA id
     * @return A list of Object[] arrays which each array corresponding to one
     *         record in the IHFS CountyNum table.
     */
    @Override
    public List<Object[]> getForecastPointsInCountyStateHSA(String state,
            String county, String hsaID) {
        String query = "SELECT * " + "FROM Countynum " + "WHERE county = '"
                + county + "' " + "AND state = '" + state + "' "
                + " AND lid in " + "(SELECT lid FROM rpffcstpoint) "
                + "AND lid IN " + "(SELECT lid FROM location WHERE hsa='"
                + hsaID + "' " + "AND (type IS NULL OR type NOT LIKE '%I%'));";

        List<Object[]> countyForecastPointList = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY, query, IHFS,
                        "IHFS countynum");

        return countyForecastPointList;

    }

    /**
     * 
     * @param lid
     *            River forecast point identifier
     * @return A list of Object[], each of which contains a latitude and a
     *         longitude value. Note that Western Hemisphere longitudes are
     *         positive.
     * 
     *         For example:
     * 
     *         new Object[]{ 42.0072222222222d, 96.2413888888889d }
     */
    @Override
    public List<Object[]> getForecastPointCoordinates(String lid) {
        /*
         * Load the lat/lon coords of this forecast point.
         */
        StringBuffer query = new StringBuffer("SELECT lat, lon ");
        query.append("FROM Location WHERE lid = '" + lid + "'");

        List<Object[]> locationResults = DatabaseQueryUtil
                .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                        query.toString(), IHFS, "IHFS location");

        return locationResults;
    }

    public List<Object[]> getCrestHistory(String lid, String crestTypes,
            String pe) {

        /* Build the query depending on the crest types we want. */
        String query = "SELECT " + pe + ", datcrst FROM Crest WHERE lid = '"
                + lid + "' ";
        if (crestTypes.length() == 1) {
            query += "AND prelim = '" + crestTypes + "' ";
        } else if (crestTypes.length() == 2) {
            query += "AND ( prelim = '" + crestTypes.substring(0, 1)
                    + "' OR prelim = '" + crestTypes.substring(1, 2) + "' ) ";
        }
        query += "AND " + pe + " is NOT NULL";

        List<Object[]> crestResults = null;
        if (pe.equals(COLUMN_STAGE.toString())) {
            crestResults = DatabaseQueryUtil.executeDatabaseQuery(
                    QUERY_MODE.MODE_SQLQUERY, query.toString(), IHFS,
                    "IHFS crest table");
        } else {
            crestResults = DatabaseQueryUtil.executeDatabaseQuery(
                    QUERY_MODE.MODE_SQLQUERY, query.toString(), IHFS,
                    "IHFS Crest");
        }

        return crestResults;

    }

    /**
     * 
     * @param lid
     *            River forecast point identifier
     * @param crestTypes
     *            A string containing possible list of crest types to filter on.
     *            P=preliminary, O=official, R=record. Empty string means take
     *            all types.
     * @return A list of Pair objects, each of which contains a double
     *         representing a historical flow crest and its date.
     */
    @Override
    public List<Pair<Integer, Date>> getFlowCrestHistory(String lid,
            String crestTypes) {

        /* Build the query depending on the crest types we want. */
        List<Object[]> crestResults = getCrestHistory(lid, crestTypes, COLUMN_Q);

        List<Pair<Integer, Date>> list = new ArrayList<>(crestResults.size());

        for (Object[] ob : crestResults) {

            Integer q = (Integer) ob[0];
            Date datcrst = new java.util.Date(((Date) ob[1]).getTime());
            Pair<Integer, Date> pair = new Pair<>(q, datcrst);
            list.add(pair);
        }
        return list;
    }

    /**
     * Define "RO" as the default crest types.
     */
    @Override
    public List<Pair<Integer, Date>> getFlowCrestHistory(String lid) {
        return this.getFlowCrestHistory(lid, RO);
    }

    /**
     * 
     * @param lid
     *            River forecast point identifier
     * @param crestTypes
     *            A string containing possible list of crest types to filter on.
     *            P=preliminary, O=official, R=record. Empty string means take
     *            all types.
     * @return A list of Pair objects, each of which contains a double
     *         representing a historical stage crest and its date.
     */
    @Override
    public List<Pair<Double, Date>> getStageCrestHistory(String lid,
            String crestTypes) {

        /* Build the query depending on the crest types we want. */
        List<Object[]> crestResults = getCrestHistory(lid, crestTypes,
                COLUMN_STAGE);

        List<Pair<Double, Date>> list = new ArrayList<>(crestResults.size());

        for (Object[] ob : crestResults) {

            Double q = (Double) ob[0];
            Date datcrst = new java.util.Date(((Date) ob[1]).getTime());
            Pair<Double, Date> pair = new Pair<>(q, datcrst);
            list.add(pair);
        }
        return list;
    }

    /**
     * Define "RO" as the default crest types.
     */
    @Override
    public List<Pair<Double, Date>> getStageCrestHistory(String lid) {
        return this.getStageCrestHistory(lid, RO);
    }

    /**
     * 
     * @param lid
     *            River forecast point identifier
     * @param month
     *            1-12 month of the year.
     * @param day
     *            1-31 day of the month.
     * @return A list of Pair objects, each of which contains a double for the
     *         stage/flow threshold for an impact, and a string describing the
     *         impact.
     */
    @Override
    public List<Pair<Double, String>> getImpactValues(String lid, int month,
            int day) {

        int reqcmp = month * 100 + day;
        /*
         * Build the query to get the database columns we want.
         * 
         * Looking for impacts for the full year (01/01 - 12/31) as opposed to
         * cold season only (01/01 - 03/31) or warm season only (04/01 - 11/01)
         * [per Mark Armstrong email dated Dec 2, 2014]
         * 
         * Comparison below is a method to find all dates between 01/01 - 12/31
         * by turning the dates into digit, such as 01/01 = 101 and 12/31 = 1231
         */
        String query = "SELECT impact_value,datestart,dateend,rf,statement"
                + " FROM floodstmt WHERE lid = '" + lid + "' ";

        List<Object[]> impactResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "IHFS floodstmt table");

        List<Pair<Double, String>> list = new ArrayList<>(impactResults.size());
        for (Object[] ob : impactResults) {
            String[] startmmdd = ob[1].toString().split("/");
            if (startmmdd.length != 2) {
                continue;
            }
            int startcmp = (Integer.parseInt(startmmdd[0]) * 100 + (Integer
                    .parseInt(startmmdd[1])));
            String[] endmmdd = ob[2].toString().split("/");
            if (endmmdd.length != 2) {
                continue;
            }
            int endcmp = (new Integer(endmmdd[0])).intValue() * 100
                    + (new Integer(endmmdd[1])).intValue();
            if (startcmp != 101 && endcmp != 1231) {
                if (reqcmp < startcmp || reqcmp > endcmp) {
                    continue;
                }
            }
            Double value = Double.parseDouble(ob[0].toString());
            String impact = ob[3].toString().equals("R") ? "-Rising||"
                    : "-Falling||";
            impact = "-" + ob[1].toString() + "-" + ob[2].toString() + impact
                    + ob[4].toString();
            Pair<Double, String> pair = new Pair<>(value, impact);
            list.add(pair);
        }

        return list;
    }

    /**
     * This methods allows the flood recommender to run displaced in
     * circumstances such as Unit Tests.
     * 
     * @param
     * @return The system time.
     */
    @Override
    public Date getSystemTime() {
        return SimulatedTime.getSystemTime().getTime();
    }

    /**
     * Translates a state abbreviation into a state name using the state table
     * in the IHFS database.
     * 
     * @param stateAbbreviation
     *            The two letter state abbreviation. These should be capital
     *            letters.
     * @return The name of the state which matches the abbreviation.
     */
    @Override
    public String getStateNameForAbbreviation(String stateAbbreviation) {
        String query = "SELECT name FROM State WHERE state = '"
                + stateAbbreviation + "'";

        List<Object[]> stateResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "IHFS state table");

        if (stateResults.size() > 0) {
            Object[] record = stateResults.get(0);
            return (String) record[0];
        } else {
            return null;
        }
    }

    @Override
    public List<Object[]> getRiverStationInfo(String lid) {
        String query = "SELECT * FROM RiverStat WHERE lid = '" + lid + "'";

        List<Object[]> statResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "IHFS state table");

        return statResults;
    }

    @Override
    public String getPrimaryPE(String lid) {
        String query = "SELECT primary_pe from RiverStat WHERE lid = '" + lid
                + "';";
        List<Object[]> primaryPE = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "IHFS state table");
        if (primaryPE.isEmpty() == false) {
            return primaryPE.get(0)[0].toString();
        }
        return null;
    }

}
