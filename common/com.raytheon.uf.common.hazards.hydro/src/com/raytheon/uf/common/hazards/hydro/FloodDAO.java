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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil;
import com.raytheon.uf.common.dataaccess.util.DatabaseQueryUtil.QUERY_MODE;
import com.raytheon.uf.common.ohd.AppsDefaults;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Description: Product data accessor implementation of the IFloodDAO.
 * 
 * This class performs queries and creates data objects as a result of the
 * queries.
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
 * Sep 19, 2014 2394       mpduff for nash     Updated for interface changes
 * Dec 17, 2014 2394       Ramer               Updated Interface
 * Feb 21, 2015 4959       Dan Schaffer        Improvements to add/remove UGCs
 * Feb 24, 2015 5960       Manross             Grab flood inundation areas
 * Apr 9,  2015 7091       Hansen              No longer parsing double pipe
 * May 08, 2015 6562       Chris.Cody          Restructure River Forecast Points/Recommender
 * May 28, 2015 7139       Chris.Cody          Add curpp and curpc HydrographPrecip query and processing
 * Jun 16, 2015 8782       Chris.Cody          Flood DAO does not properly handle Hydro DB Location longitude
 * Oct 15, 2015 12564      mduff               Change rpfparams query to be sql.
 * Oct 21, 2015 12571      mduff               Add check for null PE.
 * Jan 14, 2016  9643      Roger.Ferrel        Limit {@link #queryTopRankedTypeSource(String, String, int, String)
 *                                             to type sources starting with F or R.
 * Jan 18, 2016 12942      Roger.Ferrel        Corrected errors in computing dates in
 *                                             {@link #queryPhysicalElementValue(String, String, int, String, String, String, boolean, long)}.
 * Feb 19, 2016 15014      Robert.Blum         Fix Zone and County data queries.
 * May 04, 2016 15584      Kevin.Bisanz        queryRiverPointHydrographObserved(...) now uses times in query.
 * Jan 12, 2016 28034      mduff               Added queryHydrologicServiceAreaForGageId.
 * Jan 13, 2017 28014      bkowal              Added {@link #queryRankedTypeSources(String, String, int, String)}.
 * Jan 20, 2017 28389      Kevin.Bisanz        Fix error in {@link #queryPhysicalElementValue(String, String, int, String, String, String, boolean, long)}
 *                                             where java Date object was
 *                                             performing time zone conversion
 *                                             when DB column is of type
 *                                             "timestamp without time zone".
 * Feb 08, 2017 28335      Robert.Blum         Removed code that was setting a redunant variable.
 * Feb 10, 2017 28946      mduff               Added queryRiverMetadata method.
 * Mar 13, 2017 29675      Kevin.Bisanz        Return value and time from queryPhysicalElementValue(..).
 * Apr 27, 2017 29292      bkowal              Handle {@code null} ordinal values in the fpinfo view.
 * Jul 10, 2017 35819      Robert.Blum         Reducing the number of conversion done with hydro data.
 * </pre>
 * 
 */
public class FloodDAO implements IFloodDAO {

    /** String constant ihfs */
    public static final String IHFS = "ihfs";

    private static String COLUMN_Q = "Q";

    private static String COLUMN_STAGE = "STAGE";

    /** First letter in type source that indicates it is a forecast. */
    private static String TYPE_SOURCE_FORECAST = "Ff";

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(FloodDAO.class);

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
    }

    /**
     * Retrieves an instance of this flood recommender data access object. This
     * class follows the Singleton Pattern.
     * 
     * All instances of this object must be retrieved using this method.
     * 
     * @return An instance of this flood recommender data access object
     */
    public synchronized static IFloodDAO getInstance() {

        if (floodDAOInstance == null) {
            floodDAOInstance = new FloodDAO();
        }

        return floodDAOInstance;
    }

    @Override
    public List<String> queryHydrologicServiceAreaIdList() {

        List<String> hsaIdList = null;
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT DISTINCT(hsa) FROM ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(" ORDER BY hsa ASC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast FPINFO");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            hsaIdList = Lists.newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                String hsaId = (String) queryResult[0];
                hsaIdList.add(hsaId);
            }
        } else {
            hsaIdList = Lists.newArrayListWithExpectedSize(0);
        }

        return (hsaIdList);
    }

    @Override
    public List<RiverForecastGroup> queryAllRiverForecastGroup() {

        List<RiverForecastGroup> riverGroupList = null;
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT ");
        querySB.append(RiverForecastGroup.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(" ORDER BY ordinal, group_id ASC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast group");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            riverGroupList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                RiverForecastGroup riverGroup = new RiverForecastGroup(
                        queryResult);
                riverGroupList.add(riverGroup);
            }
        } else {
            riverGroupList = Lists.newArrayListWithExpectedSize(0);
        }

        return (riverGroupList);
    }

    @Override
    public RiverForecastGroup queryRiverForecastGroup(String groupId) {

        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(RiverForecastGroup.COLUMN_NAME_STRING);
        querySB.append(" FROM rpffcstgroup WHERE group_id = '");
        querySB.append(groupId);
        querySB.append("'");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast group");

        RiverForecastGroup riverGroup = null;
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            riverGroup = new RiverForecastGroup(queryResults.get(0));
        }

        return (riverGroup);
    }

    @Override
    public RiverForecastGroup queryRiverForecastGroupForLid(String lid) {

        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(RiverForecastGroup.DISTINCT_COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(", ");
        querySB.append(RiverForecastPoint.TABLE_NAME);
        querySB.append(" WHERE ");
        querySB.append(RiverForecastPoint.TABLE_NAME);
        querySB.append(".group_id = ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(".group_id");
        querySB.append(" AND ");
        querySB.append(RiverForecastPoint.TABLE_NAME);
        querySB.append(".lid = '");
        querySB.append(lid);
        querySB.append("'");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast group");

        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            for (Object[] queryResult : queryResults) {
                RiverForecastGroup riverGroup = new RiverForecastGroup(
                        queryResult);
                return (riverGroup);
            }
        }

        return (null);
    }

    @Override
    public List<String> queryHsaRiverForecastGroupIdList(String hsaId) {

        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(".group_id FROM ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(", ");
        querySB.append(RiverForecastPoint.TABLE_NAME);
        querySB.append(" WHERE ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(".group_id = ");
        querySB.append(RiverForecastPoint.TABLE_NAME);
        querySB.append(".group_id AND ");
        querySB.append(RiverForecastPoint.TABLE_NAME);
        querySB.append(".hsa = '");
        querySB.append(hsaId);
        querySB.append("'");

        List<String> groupIdList = Lists.newArrayList();
        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "rpffcstgroup. HSA Id");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            groupIdList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                groupIdList.add((String) queryResult[0]);
            }
        } else {
            groupIdList = Lists.newArrayListWithExpectedSize(0);
        }

        return (groupIdList);
    }

    @Override
    public List<RiverForecastGroup> queryRiverForecastGroupList(
            List<String> groupIdList, List<String> groupNameList) {
        List<RiverForecastGroup> forecastGroupList = null;

        boolean isFirst = true;
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT ");
        querySB.append(RiverForecastGroup.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(RiverForecastGroup.TABLE_NAME);
        querySB.append(" WHERE ");

        if (groupIdList != null) {
            appendToWhereClause(querySB, "group_id", groupIdList, true);
            isFirst = false;
        }

        if (groupNameList != null) {
            if (isFirst == false) {
                querySB.append(" AND ");
            }
            appendToWhereClause(querySB, "group_name", groupNameList, true);
        }
        querySB.append("ORDER BY group_id, ordinal ASC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast Group");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            forecastGroupList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                // Create a new forecast Group.
                RiverForecastGroup riverForecastGroup = new RiverForecastGroup(
                        queryResult);
                forecastGroupList.add(riverForecastGroup);
            }
        } else {
            forecastGroupList = Lists.newArrayListWithExpectedSize(0);
        }

        return (forecastGroupList);
    }

    @Override
    public List<String> queryCountyRiverForecastPointIdList(String state,
            String county) {
        List<String> lidList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT DISTINCT(lid) FROM ");
        querySB.append(FpInfo.TABLE_NAME);
        querySB.append(" WHERE ");
        querySB.append(" state = '");
        querySB.append(state);
        querySB.append("' AND county = '");
        querySB.append(county);
        querySB.append(" ORDER BY state, county ");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast FpInfo");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            lidList = Lists.newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                lidList.add((String) queryResult[0]);
            }
        } else {
            lidList = Lists.newArrayListWithExpectedSize(0);
        }

        return (lidList);
    }

    @Override
    public RiverForecastPoint queryRiverForecastPoint(String lid) {
        RiverForecastPoint riverForecastPoint = null;

        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(FpInfo.COLUMN_NAME_STRING);
        querySB.append(" FROM FpInfo WHERE lid = '");
        querySB.append(lid);
        querySB.append("' and pe IS NOT NULL");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast point");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            for (Object[] queryResult : queryResults) {
                // Create a new forecast point.
                riverForecastPoint = new RiverForecastPoint(queryResult);
            }

            Map<String, RiverForecastPoint> riverForecastPointMap = Maps
                    .newHashMapWithExpectedSize(1);
            riverForecastPointMap.put(riverForecastPoint.getLid(),
                    riverForecastPoint);
            queryRiverForecastPointExpandedData(riverForecastPointMap);
        }
        return (riverForecastPoint);
    }

    @Override
    public List<RiverForecastPoint> queryRiverForecastPointList(
            List<String> lidList, List<String> hsaList,
            List<String> groupIdList, List<String> physicalElementList) {

        List<RiverForecastPoint> forecastPointList = null;
        boolean isFirst = true;
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT ");
        querySB.append(FpInfo.COLUMN_NAME_STRING);
        querySB.append(" FROM FpInfo WHERE ");

        if (lidList != null) {
            appendToWhereClause(querySB, "lid", lidList, true);
            isFirst = false;
        }

        if (hsaList != null) {
            if (isFirst == false) {
                querySB.append(" AND ");
            } else {
                isFirst = false;
            }
            appendToWhereClause(querySB, "hsa", hsaList, true);
        }

        if (groupIdList != null) {
            if (isFirst == false) {
                querySB.append(" AND ");
            } else {
                isFirst = false;
            }
            appendToWhereClause(querySB, "group_id", groupIdList, true);
        }

        if (physicalElementList != null) {
            if (isFirst == false) {
                querySB.append(" AND ");
            }
            appendToWhereClause(querySB, "pe", physicalElementList, true);
        }
        querySB.append(" AND pe IS NOT NULL");
        querySB.append(" ORDER BY ordinal NULLS LAST, lid ASC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast point");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            forecastPointList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            // This is useful to avoid searching the list while placing
            // locations.
            Map<String, RiverForecastPoint> riverForecastPointMap = Maps
                    .newHashMapWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                // Create a new forecast point.
                RiverForecastPoint riverForecastPoint = new RiverForecastPoint(
                        queryResult);
                forecastPointList.add(riverForecastPoint);
                riverForecastPointMap.put(riverForecastPoint.getLid(),
                        riverForecastPoint);
            }

            if (riverForecastPointMap.isEmpty() == false) {
                queryRiverForecastPointExpandedData(riverForecastPointMap);
            }
        } else {
            forecastPointList = Lists.newArrayListWithExpectedSize(0);
        }

        return (forecastPointList);
    }

    /**
     * Query for "expanded" FpInfo data.
     * 
     * Query for the location (latitude and longitude from Location table) and
     * the Primary Physical Element (Primary PE from RiverStat table) for the
     * Map of LID to RiverForecastPoint objects.
     * 
     * This is not considered to be part of a deep query.
     * 
     * @param riverForecastPointMap
     */
    protected void queryRiverForecastPointExpandedData(
            Map<String, RiverForecastPoint> riverForecastPointMap) {

        if ((riverForecastPointMap != null)
                && (riverForecastPointMap.isEmpty() == false)) {
            List<String> lidList = new ArrayList<String>(
                    riverForecastPointMap.keySet());
            List<Object[]> locationQueryResultList = queryForecastPointCoordinates(
                    lidList);
            RiverForecastPoint riverForecastPoint = null;
            for (Object[] locationQueryResult : locationQueryResultList) {
                String coordLid = (String) locationQueryResult[0];
                Double latitude = (Double) locationQueryResult[1];
                Double longitude = (Double) locationQueryResult[2];
                if ((latitude != null) && (longitude != null)) {
                    riverForecastPoint = riverForecastPointMap.get(coordLid);
                    if (riverForecastPoint != null) {
                        riverForecastPoint
                                .setLatitude((Double) locationQueryResult[1]);
                        double revisedLongitude = convertHydroLongitudesToWesternHemisphere(
                                longitude.doubleValue());
                        riverForecastPoint.setLongitude(revisedLongitude);
                    }
                }
            }
        }
    }

    /**
     * River Forecast Point data (FpInfo) elements
     * 
     * This method negates positive values for Location data which stores
     * Western Hemisphere Longitude values as POSITIVE values.
     * <p>
     * This method is Western Hemisphere centric. If the value is already
     * negative; it will NOT be converted into a positive value.
     * 
     * @param hydroLongitude
     *            a Western Hemisphere Longitute stored as a POSITIVE VALUE
     * @return a NEGATED Longitude value.
     */
    private double convertHydroLongitudesToWesternHemisphere(
            final double hydroLongitude) {
        if (hydroLongitude > 0.0d) {
            return hydroLongitude * -1;
        } else {
            return hydroLongitude;
        }
    }

    @Override
    public List<RiverPointZoneInfo> queryAllRiverPointZoneInfo() {
        List<RiverPointZoneInfo> riverPointZoneInfoList = null;
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT ");
        querySB.append(RiverPointZoneInfo.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(RiverPointZoneInfo.TABLE_NAME);
        querySB.append(" ORDER BY lid ASC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "zone info");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            riverPointZoneInfoList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                RiverPointZoneInfo zoneInfo = new RiverPointZoneInfo(
                        queryResult);
                riverPointZoneInfoList.add(zoneInfo);
            }
        } else {
            riverPointZoneInfoList = Lists.newArrayListWithExpectedSize(0);
        }

        return riverPointZoneInfoList;
    }

    @Override
    public List<RiverPointZoneInfo> queryRiverPointZoneInfo(String lid) {
        return queryRiverPointZoneInfoList(Lists.newArrayList(lid), null, null);
    }

    @Override
    public List<RiverPointZoneInfo> queryRiverPointZoneInfoList(
            List<String> lidList, List<String> stateList,
            List<String> zoneNumberList) {

        List<RiverPointZoneInfo> riverPointZoneInfoList = null;
        boolean isFirst = true;
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT ");
        querySB.append(RiverPointZoneInfo.COLUMN_NAME_STRING);
        querySB.append(" FROM ZoneInfo WHERE ");
        if (lidList != null) {
            appendToWhereClause(querySB, "lid", lidList, true);
            isFirst = false;
        }

        if (stateList != null) {
            if (isFirst == false) {
                querySB.append(" AND ");
            } else {
                isFirst = false;
            }
            appendToWhereClause(querySB, "state", stateList, true);
        }

        if (zoneNumberList != null) {
            if (isFirst == false) {
                querySB.append(" AND ");
            }
            appendToWhereClause(querySB, "zoneNum", zoneNumberList, true);
        }
        querySB.append("ORDER BY lid ASC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "zone info");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            riverPointZoneInfoList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                RiverPointZoneInfo zoneInfo = new RiverPointZoneInfo(
                        queryResult);
                riverPointZoneInfoList.add(zoneInfo);
            }
        } else {
            riverPointZoneInfoList = Lists.newArrayListWithExpectedSize(0);
        }

        return riverPointZoneInfoList;
    }

    @Override
    public List<String> queryCountyStateListForHsa(String hsaId) {
        List<String> countyStateList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT DISTINCT(county||'|'||state) ");
        querySB.append(" FROM Countynum WHERE lid in ");
        querySB.append("(SELECT lid FROM rpffcstpoint) AND lid IN ");
        querySB.append("(SELECT lid FROM location WHERE hsa='");
        querySB.append(hsaId);
        querySB.append("' AND (type IS NULL OR type NOT LIKE '%I%'));");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "forecast county group");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            countyStateList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                countyStateList.add((String) queryResult[0]);
            }
        } else {
            countyStateList = Lists.newArrayListWithExpectedSize(0);
        }

        return (countyStateList);
    }

    @Override
    public HazardSettings retrieveSettings() {
        /*
         * Create the SQL Query for the IHFS RpfParams table.
         */
        String query = "select obshrs, fcsthrs, rvsexphrs, flsexphrs, flwexphrs from rpfparams";
        /*
         * Retrieve configuration information from the RpfParams table.
         */
        List<Object[]> rpfParmsList = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "IHFS hazard settings");
        HazardSettings hazardSettings = new HazardSettings();

        if ((rpfParmsList != null) && (rpfParmsList.isEmpty() == false)) {
            Object[] paramObject = rpfParmsList.get(0);
            int i = 0;
            hazardSettings.setObsLookbackHours((int) paramObject[i++]);
            hazardSettings.setForecastLookForwardHours((int) paramObject[i++]);
            hazardSettings.setRvsExpirationHours((int) paramObject[i++]);
            hazardSettings.setFlsExpirationHours((int) paramObject[i++]);
            hazardSettings.setFlwExpirationHours((int) paramObject[i++]);
        } else {
            statusHandler.info("Could not load RpfParams information.");
        }

        query = "SELECT hsa from Admin";

        List<Object[]> adminList = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "admin");

        if ((adminList != null) && (adminList.isEmpty() == false)) {
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

    @Override
    public List<IngestFilterInfo> queryIngestSettings(String lid,
            String physicalElement) {
        /*
         * ((OLD COMMENT)) Retrieve a unique list of entries for typesources
         * that match the given lid and pe. The ingestfilter entries are needed
         * because they contain the type-source rank information. Insert a comma
         * in between the two fields to make them easier to parse. Note that the
         * ts rank sort method will not handle numbers greater than 9 (i.e. 12
         * is ranked higher than 3)! Also, note that the query does not filter
         * out extremums that are not "Z". Only bother retrieving info if
         * RiverStatus entries exist. We try and read RiverStatus first since
         * that table is smaller.
         */
        List<IngestFilterInfo> ingestFilterInfoList = null;
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT ");
        querySB.append(IngestFilterInfo.COLUMN_NAME_STRING);
        querySB.append(" FROM IngestFilter WHERE lid = '");
        querySB.append(lid);
        querySB.append("' AND pe = '");
        querySB.append(physicalElement);
        querySB.append("' AND ingest = 'T' ");
        querySB.append("ORDER BY ts_rank");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS ingest");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            ingestFilterInfoList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                IngestFilterInfo ingestFilterInfo = new IngestFilterInfo(
                        queryResult);
                ingestFilterInfoList.add(ingestFilterInfo);
            }
        } else {
            ingestFilterInfoList = Lists.newArrayListWithExpectedSize(0);
        }

        return ingestFilterInfoList;
    }

    @Override
    public List<IngestFilterInfo> queryHydrographForecastIngestFilter(
            String primary_pe) {
        /*
         * load the type source which pe starts as 'H' or 'Q' and ts starts as
         * 'F'.
         */
        List<IngestFilterInfo> ingestFilterInfoList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(IngestFilterInfo.DISTINCT_COLUMN_NAME_STRING);
        querySB.append(" FROM IngestFilter WHERE ts LIKE 'F%%' ");
        querySB.append("AND (pe = '");
        querySB.append(primary_pe);
        querySB.append("') AND ingest = 'T'");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IngestFilter");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            ingestFilterInfoList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                IngestFilterInfo ingestFilterInfo = new IngestFilterInfo(
                        queryResult);
                ingestFilterInfoList.add(ingestFilterInfo);
            }
        } else {
            ingestFilterInfoList = Lists.newArrayListWithExpectedSize(0);
        }
        return ingestFilterInfoList;
    }

    @Override
    public List<String> queryLidListForCountyStateHSA(String state,
            String county, String hsaID) {

        List<String> lidList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT lid FROM Countynum WHERE county = '");
        querySB.append(county);
        querySB.append("'  AND state = '");
        querySB.append(state);
        querySB.append("'  AND lid in (SELECT lid FROM rpffcstpoint) ");
        querySB.append(" AND lid IN (SELECT lid FROM location WHERE hsa='");
        querySB.append(hsaID);
        querySB.append("' AND (type IS NULL OR type NOT LIKE '%I%'));");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS countynum");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            lidList = Lists.newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                lidList.add((String) queryResult[0]);
            }
        } else {
            lidList = Lists.newArrayListWithExpectedSize(0);
        }

        return lidList;
    }

    @Override
    public String queryHydrologicServiceAreaForGageId(String lid) {
        String query = "select hsa from location where lid = '" + lid
                + "' limit 1";

        List<Object[]> results = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "Location");
        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            if (row != null) {
                return (String) row[0];
            }
        }

        return "";
    }

    @Override
    public Map<String, String> queryStateAbbreviationToNameMap() {

        Map<String, String> stateAbbrToNameMap = null;

        String query = "SELECT state, name FROM State";

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query, IHFS, "IHFS state table");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            stateAbbrToNameMap = Maps.newHashMapWithExpectedSize(50);
            for (Object[] queryResult : queryResults) {
                stateAbbrToNameMap.put((String) queryResult[0],
                        (String) queryResult[1]);
            }
        } else {
            stateAbbrToNameMap = Maps.newHashMapWithExpectedSize(0);
        }
        return (stateAbbrToNameMap);
    }

    @Override
    public List<RiverStatus> queryRiverStatusList(String lid,
            String physicalElement, long beginValidTime, long systemTime) {

        List<RiverStatus> riverStatusList = null;
        SimpleDateFormat dateFormat = RiverHydroConstants.getDateFormat();
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(RiverStatus.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(RiverStatus.TABLE_NAME);
        querySB.append(" WHERE lid = '");
        querySB.append(lid);
        querySB.append("' AND pe = '");
        querySB.append(physicalElement);
        querySB.append("' AND ( validtime >= '");
        querySB.append(dateFormat.format(beginValidTime));
        querySB.append("' AND validtime <= '");
        querySB.append(dateFormat.format(systemTime));
        querySB.append("') AND basistime IS NULL ");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "RiverStatus");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            riverStatusList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                RiverStatus riverStatus = new RiverStatus(queryResult);
                riverStatusList.add(riverStatus);
            }
        } else {
            riverStatusList = Lists.newArrayListWithExpectedSize(0);
        }

        return riverStatusList;
    }

    @Override
    public List<RiverStatus> queryRiverStatusList(String lid,
            String physicalElement) {

        List<RiverStatus> riverStatusList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(RiverStatus.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(RiverStatus.TABLE_NAME);
        querySB.append(" WHERE lid = '");
        querySB.append(lid);
        querySB.append("' AND pe = '");
        querySB.append(physicalElement);
        querySB.append("'");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "RiverStatus");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            riverStatusList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                RiverStatus riverStatus = new RiverStatus(queryResult);
                riverStatusList.add(riverStatus);
            }
        } else {
            riverStatusList = Lists.newArrayListWithExpectedSize(0);
        }

        return (riverStatusList);
    }

    @Override
    public HydrographObserved queryRiverPointHydrographObserved(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime) {

        List<SHEFObserved> shefObservedList = null;
        if ((typeSource == null) || (typeSource.length() == 0)) {
            typeSource = queryBestObservedTypeSource(lid, physicalElement,
                    obsBeginTime, obsEndTime);
        }

        SimpleDateFormat dateFormat = RiverHydroConstants.getDateFormat();
        /* Determine the table name to use. */
        String tableName = this.getTableNameForPhysicalElement(physicalElement,
                false);

        if ((typeSource != null) && (typeSource.length() > 0)) {
            /*
             * Get the data for the specified time window and for the determined
             * PEDTSEP entry. Build the where clause depending upon whether
             * considering only passed qc data.
             */
            StringBuilder querySB = new StringBuilder();
            querySB.append("SELECT ");
            querySB.append(SHEFObserved.COLUMN_NAME_STRING);
            querySB.append(" FROM ");
            querySB.append(tableName);
            querySB.append(" WHERE lid = '");
            querySB.append(lid);
            querySB.append("' AND pe = '");
            querySB.append(physicalElement);
            querySB.append("' AND ts = '");
            querySB.append(typeSource);
            querySB.append("' AND obstime >= '");
            querySB.append(dateFormat.format(new Date(obsBeginTime)));
            querySB.append("' AND obstime <= '");
            querySB.append(dateFormat.format(new Date(obsEndTime)));
            querySB.append("' AND value != ");
            querySB.append(RiverHydroConstants.MISSING_VALUE_STRING);
            querySB.append(" AND quality_code >= ");
            querySB.append(RiverHydroConstants.QUESTIONABLE_BAD_THRESHOLD);
            querySB.append(" ORDER BY obstime ASC ");

            List<Object[]> queryResults = DatabaseQueryUtil
                    .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                            querySB.toString(), IHFS,
                            "river observed hydrograph");
            if ((queryResults != null) && (queryResults.isEmpty() == false)) {
                shefObservedList = Lists
                        .newArrayListWithExpectedSize(queryResults.size());
                for (Object[] queryResult : queryResults) {
                    SHEFObserved shefObserved = new SHEFObserved(queryResult);
                    shefObservedList.add(shefObserved);
                }
            } else {
                shefObservedList = Lists.newArrayListWithExpectedSize(0);
            }
        } else {
            shefObservedList = Lists.newArrayListWithExpectedSize(0);
        }
        HydrographObserved hydrographObserved = new HydrographObserved(lid,
                physicalElement, typeSource, obsBeginTime, obsEndTime,
                shefObservedList);

        return (hydrographObserved);
    }

    /**
     * Query the Best Observed Type Source from the IngestFilter table.
     * 
     * This is different from the public "queryBestObservedTypeSource" method.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @return Type Source value
     */
    protected String queryBestObservedTypeSource(String lid,
            String physicalElement) {
        String typeSource = null;

        /*
         * get the ingest filter entries for this location. note that the
         * retrieval is ordered so that if multiple best ranks exist, there is
         * some predictability for the identified best one. also note that this
         * approach ignores the duration, extremum, and probability code.
         */
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(IngestFilterInfo.DISTINCT_SETTING_COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(IngestFilterInfo.TABLE_NAME);
        querySB.append(" WHERE lid ='");
        querySB.append(lid);
        querySB.append("' AND pe  = '");
        querySB.append(physicalElement);
        querySB.append(
                "' AND ts LIKE 'R%' AND ingest = 'T' ORDER BY ts_rank, ts ");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IngestFilter (TS) table");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            Object[] queryResult = queryResults.get(0);
            typeSource = (String) queryResult[1];
        }

        return (typeSource);
    }

    @Override
    public String queryBestObservedTypeSource(String lid,
            String physicalElement, long obsBeginTime, long obsEndTime) {

        String bestTypeSource = null;
        /*
         * In an effort to minimize reads of the database, get the RiverStatus
         * information all at once, for all ts's and for observed Data. There is
         * a validtime limit for observed data.
         */
        List<RiverStatus> riverStatusList = queryRiverStatusList(lid,
                physicalElement, obsBeginTime, obsEndTime);

        if ((riverStatusList != null) && (riverStatusList.isEmpty() == false)) {
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
            List<IngestFilterInfo> ingestFilterInfoList = queryIngestSettings(
                    lid, physicalElement);

            if ((ingestFilterInfoList != null)
                    && (ingestFilterInfoList.isEmpty() == false)) {

                for (IngestFilterInfo ingestFilterInfo : ingestFilterInfoList) {

                    String testTypeSource = ingestFilterInfo.getTypeSource();
                    if ((testTypeSource.startsWith("R") == true)
                            || (testTypeSource.startsWith("P") == true)) {

                        boolean hasRiverStatusRec = findRiverStatusTypeSource(
                                riverStatusList, testTypeSource);
                        if (hasRiverStatusRec == true) {
                            bestTypeSource = testTypeSource;
                            break;
                        }
                    }
                }

                if (bestTypeSource == null) {
                    // Still do not have a valid Type Source for the given Lid,
                    // Physical Element, and Valid Time range
                    for (IngestFilterInfo ingestFilterInfo : ingestFilterInfoList) {
                        String testTypeSource = ingestFilterInfo
                                .getTypeSource();
                        if (testTypeSource.startsWith("R") == true) {
                            bestTypeSource = testTypeSource;
                            break;
                        }
                    }
                }
            }
        }

        return (bestTypeSource);
    }

    /**
     * Test Whether the given Type Source value is found within the list of
     * RiverStatus objects.
     * 
     * @param riverStatusList
     *            List of queried RiverStatus objects
     * @param testTypeSource
     *            Type Source value to fine
     * @return true if found; false otherwise
     */
    protected boolean findRiverStatusTypeSource(
            List<RiverStatus> riverStatusList, String testTypeSource) {
        boolean isFound = false;
        if ((riverStatusList != null) && (riverStatusList.isEmpty() == false)) {
            for (RiverStatus riverStatus : riverStatusList) {
                if (testTypeSource.equals(riverStatus.getTypeSource())) {
                    isFound = true;
                    break;
                }
            }
        }

        return (isFound);
    }

    @Override
    public HydrographPrecip queryRiverPointHydrographPrecip(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime) {

        List<SHEFPrecip> shefPrecipList = null;
        if ((typeSource == null) || (typeSource.length() == 0)) {
            // This is the same query used for SHEFObserved objects.
            typeSource = queryBestObservedTypeSource(lid, physicalElement);
        }

        SimpleDateFormat dateFormat = RiverHydroConstants.getDateFormat();
        /* Determine the table name to use. */
        String tableName = this
                .getPrecipTableNameForPhysicalElement(physicalElement);

        if ((typeSource != null) && (typeSource.length() > 0)) {
            /*
             * Get the data for the specified time window and for the determined
             * PEDTSEP entry. Build the where clause depending upon whether
             * considering only passed qc data. (This is from existing SHEF
             * Observed query.)
             */
            StringBuilder querySB = new StringBuilder();
            querySB.append("SELECT ");
            querySB.append(SHEFPrecip.COLUMN_NAME_STRING);
            querySB.append(" FROM ");
            querySB.append(tableName);
            querySB.append(" WHERE lid = '");
            querySB.append(lid);
            querySB.append("' AND pe = '");
            querySB.append(physicalElement);
            querySB.append("' AND ts = '");
            querySB.append(typeSource);
            querySB.append("' AND obstime >= '");
            querySB.append(dateFormat.format(new Date(obsBeginTime)));
            querySB.append("' AND obstime <= '");
            querySB.append(dateFormat.format(new Date(obsEndTime)));
            querySB.append("' AND value != ");
            querySB.append(RiverHydroConstants.MISSING_VALUE_STRING);
            querySB.append(" AND quality_code >= ");
            querySB.append(RiverHydroConstants.QUESTIONABLE_BAD_THRESHOLD);
            querySB.append(" ORDER BY obstime ASC ");

            List<Object[]> queryResults = DatabaseQueryUtil
                    .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                            querySB.toString(), IHFS,
                            "river precip hydrograph");
            if ((queryResults != null) && (queryResults.isEmpty() == false)) {
                shefPrecipList = Lists
                        .newArrayListWithExpectedSize(queryResults.size());
                for (Object[] queryResult : queryResults) {
                    SHEFPrecip shefPrecip = new SHEFPrecip(queryResult);
                    shefPrecipList.add(shefPrecip);
                }
            } else {
                shefPrecipList = Lists.newArrayListWithExpectedSize(0);
            }
        } else {
            shefPrecipList = Lists.newArrayListWithExpectedSize(0);
        }
        HydrographPrecip hydrographPrecip = new HydrographPrecip(lid,
                physicalElement, typeSource, obsBeginTime, obsEndTime,
                shefPrecipList);

        return (hydrographPrecip);
    }

    @Override
    public HydrographForecast queryRiverPointHydrographForecast(String lid,
            String physicalElement, long currentSystemTime, long endValidTime,
            long basisBeginTime, boolean useLatestForecast) {
        HydrographForecast returnHydrographForecast = null;

        HydrographForecast hydrographForecast = null;
        List<Long> uniqueBasisList = null;
        List<IngestFilterInfo> ingestFilterInfoList = this
                .queryIngestSettings(lid, physicalElement);
        if ((ingestFilterInfoList != null)
                && (ingestFilterInfoList.isEmpty() == false)) {
            for (IngestFilterInfo ingestFilterInfo : ingestFilterInfoList) {
                String typeSource = ingestFilterInfo.getTypeSource();

                uniqueBasisList = queryHydrographForecastBasisTimeList(lid,
                        physicalElement, typeSource, currentSystemTime,
                        endValidTime, basisBeginTime);
                if ((uniqueBasisList != null)
                        && (uniqueBasisList.isEmpty() == false)) {
                    hydrographForecast = this.queryRiverPointHydrographForecast(
                            lid, physicalElement, typeSource, currentSystemTime,
                            endValidTime, basisBeginTime, useLatestForecast,
                            uniqueBasisList);
                    if (hydrographForecast != null) {
                        List<SHEFForecast> shefForecastList = hydrographForecast
                                .getShefHydroDataList();
                        if ((shefForecastList != null)
                                && (shefForecastList.isEmpty() == false)) {
                            break;
                        }
                    }
                } else {
                    hydrographForecast = new HydrographForecast(lid,
                            physicalElement, typeSource, currentSystemTime,
                            endValidTime, basisBeginTime, useLatestForecast,
                            null, null);
                }
            }
        }

        if (hydrographForecast != null) {
            List<SHEFForecast> origForecastList = hydrographForecast
                    .getShefHydroDataList();
            if ((origForecastList != null)
                    && (origForecastList.isEmpty() == false)) {
                List<SHEFForecast> shefForecastList = processKeepShefForecast(
                        origForecastList, uniqueBasisList);
                hydrographForecast.setShefHydroDataList(shefForecastList);
            }
            returnHydrographForecast = hydrographForecast;
        } else {
            returnHydrographForecast = new HydrographForecast(lid,
                    physicalElement, "", currentSystemTime, endValidTime,
                    basisBeginTime, useLatestForecast, null, null);

        }

        return (returnHydrographForecast);
    }

    /**
     * Retrieves the FORECAST hydrograph for a river forecast point.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF typesource code
     * @param systemTime
     *            The system time
     * @param endValidTime
     *            The latest valid forecast time to accept
     * @param basisBeginTime
     *            The earliest basis time to search for
     * @param useLatestForecast
     *            Only consider the latest forecast
     * @param basisTimeList
     *            Available forecast basis times (as Long)
     * @return A HydrographForecast object. Containing an array corresponding to
     *         matching rows from the IHFS FcstHeight or FcstDischarge tables.
     */
    protected HydrographForecast queryRiverPointHydrographForecast(String lid,
            String physicalElement, String typeSource, long systemTime,
            long endValidTime, long basisBeginTime, boolean useLatestForecast,
            List<Long> basisTimeList) {

        SimpleDateFormat dateFormat = RiverHydroConstants.getDateFormat();

        /*
         * Set the table name to use.
         */
        String tableName = this.getTableNameForPhysicalElement(physicalElement,
                true);

        StringBuilder querySB = new StringBuilder();

        if (useLatestForecast || basisTimeList.size() == 1) {
            Long basisTimeValue = basisTimeList.get(0);
            querySB.append("SELECT ");
            querySB.append(SHEFForecast.COLUMN_NAME_STRING);
            querySB.append(" FROM ");
            querySB.append(tableName);
            querySB.append(" WHERE lid = '");
            querySB.append(lid);
            querySB.append("' AND pe = '");
            querySB.append(physicalElement);
            querySB.append("' AND ts = '");
            querySB.append(typeSource);
            querySB.append("' AND probability < 0.0 ");
            querySB.append("AND ( validtime >= '");
            querySB.append(dateFormat.format(systemTime));
            querySB.append("' AND validtime <= '");
            querySB.append(dateFormat.format(endValidTime));
            querySB.append("') AND basistime = '");
            querySB.append(dateFormat.format(basisTimeValue));
            querySB.append("' AND value != ");
            querySB.append(RiverHydroConstants.MISSING_VALUE);
            querySB.append(" AND quality_code >= ");
            querySB.append(RiverHydroConstants.QUESTIONABLE_BAD_THRESHOLD);
            querySB.append(" ORDER BY validtime ASC");
        } else {
            querySB.append("SELECT ");
            querySB.append(SHEFForecast.COLUMN_NAME_STRING);
            querySB.append(" FROM ");
            querySB.append(tableName);
            querySB.append(" WHERE lid = '");
            querySB.append(lid);
            querySB.append("' AND pe = '");
            querySB.append(physicalElement);
            querySB.append("' AND ts = '");
            querySB.append(typeSource);
            querySB.append("' AND probability < 0.0 AND ( validtime >= '");
            querySB.append(dateFormat.format(systemTime));
            querySB.append("' AND validtime <= '");
            querySB.append(dateFormat.format(endValidTime));
            querySB.append("') AND basistime >= '");
            querySB.append(dateFormat.format(basisBeginTime));
            querySB.append(
                    "' AND value != " + RiverHydroConstants.MISSING_VALUE);
            querySB.append(" AND quality_code >= ");
            querySB.append(RiverHydroConstants.QUESTIONABLE_BAD_THRESHOLD);
            querySB.append(" ORDER BY validtime ASC");
        }

        List<SHEFForecast> shefForecastList = null;
        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "river forecast hydrograph");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            shefForecastList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                SHEFForecast shefForecast = new SHEFForecast(queryResult);
                shefForecastList.add(shefForecast);
            }
        } else {
            shefForecastList = Lists.newArrayListWithExpectedSize(0);
        }
        HydrographForecast hydrographForecast = new HydrographForecast(lid,
                physicalElement, typeSource, systemTime, endValidTime,
                basisBeginTime, useLatestForecast, basisTimeList,
                shefForecastList);

        return (hydrographForecast);
    }

    /**
     * Returns a list of basis times (the time each river forecast time series
     * was created).
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF typesource code
     * @param systemTime
     *            The system time
     * @param endValidTime
     *            The latest possible forecast valid time
     * @param basisBeginTime
     *            The earliest basistime to accept
     * @return A list of long integers containing a single, system basistime
     *         value.
     * 
     */
    protected List<Long> queryHydrographForecastBasisTimeList(String lid,
            String physicalElement, String typeSource, long systemTime,
            long endValidTime, long basisBeginTime) {

        List<Long> basisTimeList = null;
        String tableName = this.getTableNameForPhysicalElement(physicalElement,
                true);

        SimpleDateFormat dateFormat = RiverHydroConstants.getDateFormat();
        /*
         * Retrieve a list of unique basis times; use descending sort. Only
         * consider forecast data before some ending time, and with some limited
         * basis time ago.
         */
        StringBuilder querySB = new StringBuilder();

        querySB.append("SELECT DISTINCT(basistime) FROM ");
        querySB.append(tableName);
        querySB.append(" WHERE lid = '");
        querySB.append(lid);
        querySB.append("' AND pe = '");
        querySB.append(physicalElement);
        querySB.append("' AND ts = '");
        querySB.append(typeSource);
        querySB.append("' AND probability < 0.0 ");
        querySB.append("AND ( validtime >= '");
        querySB.append(dateFormat.format(systemTime));
        querySB.append("' AND validtime <= '");
        querySB.append(dateFormat.format(endValidTime));
        querySB.append("') AND basistime >= '");
        querySB.append(dateFormat.format(basisBeginTime));
        querySB.append("' AND value != ");
        querySB.append(RiverHydroConstants.MISSING_VALUE);
        querySB.append(" AND quality_code >= ");
        querySB.append(RiverHydroConstants.QUESTIONABLE_BAD_THRESHOLD);
        querySB.append(" ORDER BY basistime DESC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "river forecast basis time");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            basisTimeList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                Date date = (Date) queryResult[0];
                basisTimeList.add(date.getTime());
            }
        } else {
            basisTimeList = Lists.newArrayListWithExpectedSize(0);
        }
        return basisTimeList;
    }

    /**
     * Process through the queried SHEFForcast objects; comparing to a List of
     * Unique Basis Time values and choose which entries to keep.
     * <p>
     * 
     * This is code for RiverForecastPoint->HydrographForecast->SHEFForecast
     * queries which was originally written in C, then ported to Java and
     * re-written to RiverPro recommender operations.
     * <p>
     * The gist of the operations here is to process the results of a
     * SHEFForecast query (which may have results from either the "fcstheight"
     * or "fcstdischarge" table.
     * <p>
     * This code endures that the resultant Valid Time SHEFForecast value are
     * between the valid Basis Times
     * 
     * @param shefForecastList
     * @param uniqueBasisList
     * @return
     */
    protected List<SHEFForecast> processKeepShefForecast(
            List<SHEFForecast> shefForecastList, List<Long> uniqueBasisList) {

        List<SHEFForecast> keepSHEFForecastList = null;

        /* Size of forecastList */
        int[] basisIndex = new int[shefForecastList.size()];

        /* Size of uniqueBasisList */
        long[] tsStartTime = new long[uniqueBasisList.size()];

        /* Size of uniqueBasisList */
        long[] tsEndTime = new long[uniqueBasisList.size()];

        /* Size of uniqueBasisList */
        boolean[] tsFirstCheck = new boolean[uniqueBasisList.size()];

        for (int i = 0; i < tsFirstCheck.length; ++i) {
            tsFirstCheck[i] = false;
        }

        long[] tsBasisTime = new long[uniqueBasisList.size()];

        /*
         * Now loop through the retrieved time series data values and get the
         * start and end times for each of the basis times found.
         */
        for (int i = 0; i < shefForecastList.size(); ++i) {
            SHEFForecast shefForecastRecord = shefForecastList.get(i);
            long forecastBasisTime = shefForecastRecord.getBasisTime();

            /*
             * Find out which basis time's time series this value belongs to.
             */
            basisIndex[i] = RiverHydroConstants.MISSING_VALUE;

            for (int j = 0; ((j < uniqueBasisList.size()
                    && basisIndex[i] == RiverHydroConstants.MISSING_VALUE)); j++) {
                Long uniqueBasisTime = uniqueBasisList.get(j);

                if (forecastBasisTime == uniqueBasisTime.longValue()) {
                    basisIndex[i] = j;
                }
            }

            if (basisIndex[i] == RiverHydroConstants.MISSING_VALUE) {
                statusHandler.debug(
                        "Unexpected error assigning basis_index for " + i);
            }

            /*
             * Check if the values constitute the start or end time for the time
             * series and record this times if they do.
             */
            long validTime = shefForecastRecord.getValidTime();

            if (tsFirstCheck[basisIndex[i]]) {
                if (validTime < tsStartTime[basisIndex[i]]) {
                    tsStartTime[basisIndex[i]] = validTime;
                } else if (validTime > tsEndTime[basisIndex[i]]) {
                    tsEndTime[basisIndex[i]] = validTime;
                }
            } else {
                tsStartTime[basisIndex[i]] = validTime;
                tsEndTime[basisIndex[i]] = validTime;
                tsFirstCheck[basisIndex[i]] = true;
            }

        }

        /*
         * For each of the unique basis times, assign the basis time in a
         * convenient array for use in the adjust_started function.
         */
        for (int j = 0; j < uniqueBasisList.size(); ++j) {
            long basisTime = uniqueBasisList.get(j).longValue();
            tsBasisTime[j] = basisTime;
        }

        /*
         * Knowing the actual start and end times for the multiple time series,
         * loop thru the time series and adjust the start and end time so that
         * they reflect the time span to use; i.e. there is no overlap. THIS IS
         * THE KEY STEP IN THE PROCESS OF DEFINING AN AGGREGATE VIRTUAL TIME
         * SERIES!!!
         */
        adjustStartEnd(uniqueBasisList, tsBasisTime, tsStartTime, tsEndTime);

        /*
         * Loop through the complete retrieved time series and only keep the
         * value if it lies between the start and end time for this basis time.
         */
        int shefForecastListSize = shefForecastList.size();
        keepSHEFForecastList = Lists
                .newArrayListWithExpectedSize(shefForecastListSize);
        for (int i = 0; i < shefForecastListSize; ++i) {
            SHEFForecast forecastRecord = shefForecastList.get(i);
            long validTime = forecastRecord.getValidTime();

            if (validTime >= tsStartTime[basisIndex[i]]
                    && validTime <= tsEndTime[basisIndex[i]]) {
                // Keep
                keepSHEFForecastList.add(forecastRecord);
            }
        }

        return (keepSHEFForecastList);
    }

    /**
     * This method uses the time series with the latest basis time first, and
     * uses it in its entirety. Then the time series with the next latest basis
     * time is used. If it overlaps portions of the already saved time series,
     * then only that portion which doesn't overlap is used. This process
     * continues until all time series have been considered. In essences, this
     * method adjoins adjacent time series.
     * 
     * @param uniqueBasisList
     *            List of unique forecast basis times
     * @param basisTime
     *            - basis times of forecasts
     * @param startTime
     *            - start times of forecasts
     * @param endTime
     *            - end times of forecasts
     */
    private void adjustStartEnd(List<Long> uniqueBasisList, long basisTime[],
            long startTime[], long endTime[]) {
        boolean found = false;
        long tmpTime = 0;
        long fullStartValidTime = 0;
        long fullEndValidTime = 0;
        int currentIndex = 0;

        /*
         * Initialize the array to keep track of order of the basis time series
         */
        int[] basisOrder = new int[uniqueBasisList.size()];

        for (int i = 0; i < basisOrder.length; ++i) {
            basisOrder[i] = -1;
        }

        /*
         * Find the order of the time series by their latest basis time. If two
         * time series have the same basis time, use the one that has the
         * earlier starting time. Note that the order is such that the latest
         * basis time is last in the resulting order array.
         */
        for (int i = 0; i < uniqueBasisList.size(); ++i) {
            tmpTime = 0;
            currentIndex = 0;

            for (int j = 0; j < uniqueBasisList.size(); j++) {

                /*
                 * Only consider the time series if it hasn't been accounted for
                 * in the order array
                 */
                found = false;
                for (int k = 0; k < i; k++) {
                    if (j == basisOrder[k]) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (basisTime[j] > tmpTime) {
                        currentIndex = j;
                        tmpTime = basisTime[j];
                    } else if (basisTime[j] == tmpTime) {
                        if (startTime[j] < startTime[currentIndex]) {
                            currentIndex = j;
                            tmpTime = basisTime[j];
                        }
                    }
                }
            }

            basisOrder[i] = currentIndex;
        }

        /*
         * do NOT adjust the start and end time of the time series with the
         * latest ending time. loop through all the other time series and adjust
         * their start and end times as necessary so that they do not overlap
         * the time limits of the being-built aggregate time series.
         */

        currentIndex = basisOrder[0];
        if (currentIndex >= 0 && (currentIndex < startTime.length)
                && (currentIndex < endTime.length)) {
            fullStartValidTime = startTime[currentIndex];
            fullEndValidTime = endTime[currentIndex];
            return;
        }

        for (int i = 1; i < uniqueBasisList.size(); i++) {
            currentIndex = basisOrder[i];

            /*
             * each additional time series being considered is checked to see if
             * it falls outside the time window already encompassed by the
             * assembled time series. there are four cases that can occur; each
             * is handled below.
             */

            /*
             * if the basis time series being considered is fully within the
             * time of the already existing time series, then ignore it
             * completely, and reset its times.
             */

            if (startTime[currentIndex] >= fullStartValidTime
                    && endTime[currentIndex] <= fullEndValidTime) {
                startTime[currentIndex] = 0;
                endTime[currentIndex] = 0;
            }

            /*
             * if the basis time series being considered covers time both before
             * and after the existing time series, use the portion of it that is
             * before the time series. it is not desirable to use both the
             * before and after portion (this results in a non-contiguous
             * time-series that is weird), and given a choice it is better to
             * use the forecast data early on than the later forecast data, so
             * use the before portion
             */

            else if (startTime[currentIndex] <= fullStartValidTime
                    && endTime[currentIndex] >= fullEndValidTime) {
                endTime[currentIndex] = fullStartValidTime - 1;
                fullStartValidTime = startTime[currentIndex];
            }

            /*
             * if the basis time series being considered straddles the beginning
             * or is completely before the existing time series, then use the
             * portion of it that is before the time series.
             */

            else if (startTime[currentIndex] <= fullStartValidTime
                    && endTime[currentIndex] <= fullEndValidTime) {
                endTime[currentIndex] = fullStartValidTime - 1;
                fullStartValidTime = startTime[currentIndex];
            }

            /*
             * if the basis time series being considered straddles the end or is
             * completely after the existing time series, then use the portion
             * of it that is after the time series.
             */

            else if (startTime[currentIndex] >= fullStartValidTime
                    && endTime[currentIndex] >= fullEndValidTime) {
                startTime[currentIndex] = fullEndValidTime + 1;
                fullEndValidTime = endTime[currentIndex];
            }

        } /* end for loop on the unique ordered basis times */

    }

    @Override
    public String queryTopRankedTypeSource(String lid, String primary_pe,
            int duration, String extremum) {
        List<String> typeSourcesList = queryRankedTypeSources(lid, primary_pe,
                duration, extremum);
        if (typeSourcesList.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return typeSourcesList.iterator().next();
    }

    @Override
    public List<String> queryRankedTypeSources(String lid, String primary_pe,
            int duration, String extremum) {
        StringBuilder querySB = new StringBuilder();

        querySB.append(
                "SELECT ts, ts_rank FROM ingestFilter where ingest = 'T' and lid ='");
        querySB.append(lid);
        querySB.append("' AND extremum = '");
        querySB.append(extremum);
        querySB.append("' AND dur = ");
        querySB.append(duration);
        /*
         * Per Mark Armstrong email to work like RiverPro limit type sources to
         * those starting with F or R.
         */
        querySB.append("AND ( ts LIKE 'F%%' OR ts LIKE 'R%%')");
        querySB.append(" ORDER BY ts_rank, ts");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IngestFilter: type source");
        if (queryResults == null || queryResults.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> typeSourcesSet = new LinkedHashSet<>(queryResults.size(),
                1.0f);
        for (Object[] objects : queryResults) {
            typeSourcesSet.add((String) objects[0]);
        }
        return new ArrayList<>(typeSourcesSet);
    }

    @Override
    public Map<String, Object> queryPhysicalElementValue(String lid,
            String physicalElement, int duration, String typeSource,
            String extremum, String timeArg, long currentTime_ms) {
        SimpleDateFormat dateFormat = RiverHydroConstants.getDateFormat();

        Calendar currentTimeCal = TimeUtil.newGmtCalendar();
        currentTimeCal.setTimeInMillis(currentTime_ms);

        String valueKey = "value";
        String validtimeKey = "validtime";

        Map<String, Object> retVal = new HashMap<>();
        retVal.put(valueKey, RiverHydroConstants.MISSING_VALUE);
        retVal.put(validtimeKey, RiverHydroConstants.MISSING_VALUE);

        String queryTimeConstraint = "";
        if (!RiverHydroConstants.NEXT.equals(timeArg)) {
            boolean isForecast = TYPE_SOURCE_FORECAST
                    .contains(typeSource.substring(0, 1));

            /*
             * Set the table name to use.
             */
            String tableName = getTableNameForPhysicalElement(physicalElement,
                    isForecast);

            /*
             * Build the validTime from timeArg
             */
            String[] stringArray = timeArg.split("\\|");
            int dayOffset = Integer.parseInt(stringArray[0]);
            int baseHours = Integer.parseInt(stringArray[1]);
            int windowHours = Integer.parseInt(stringArray[2]);

            Calendar referenceCal = TimeUtil.newCalendar(currentTimeCal);
            referenceCal.set(Calendar.MILLISECOND, 0);
            referenceCal.set(Calendar.SECOND, 0);
            referenceCal.set(Calendar.MINUTE, baseHours % 100);
            referenceCal.set(Calendar.HOUR_OF_DAY, baseHours / 100);
            referenceCal.add(Calendar.DAY_OF_MONTH, dayOffset);

            Calendar lowerBoundCal = TimeUtil.newCalendar(referenceCal);
            lowerBoundCal.add(Calendar.HOUR_OF_DAY, -windowHours);
            Calendar upperBoundCal = TimeUtil.newCalendar(referenceCal);
            upperBoundCal.add(Calendar.HOUR_OF_DAY, windowHours);
            String lowerBoundStr = dateFormat.format(lowerBoundCal.getTime());
            String upperBoundStr = dateFormat.format(upperBoundCal.getTime());

            if (isForecast) {
                queryTimeConstraint = "(validtime >= '" + lowerBoundStr
                        + "' and validtime <= '" + upperBoundStr + "')";
            } else {
                queryTimeConstraint = "(obstime >= '" + lowerBoundStr
                        + "' and obstime <= '" + upperBoundStr + "')";
            }

            String basisTime = lowerBoundStr;

            /*
             * if forecast: pull out basis time and send into next query to get
             * value I’m looking for
             * 
             * if obs use value passed in translate into database format -- see
             * Date formats)
             */
            StringBuilder querySB = new StringBuilder();
            if (isForecast) {
                /*
                 * pull out the basistime for the next query
                 */
                querySB.append("SELECT basistime FROM ");
                querySB.append(tableName);
                querySB.append(" WHERE lid = '");
                querySB.append(lid);
                querySB.append("' AND pe = '");
                querySB.append(physicalElement);
                querySB.append("' AND ts = '");
                querySB.append(typeSource);
                querySB.append("' AND extremum = '");
                querySB.append(extremum);
                querySB.append("' ORDER BY basisTime desc limit 1");

                List<Object[]> basisTimes = DatabaseQueryUtil
                        .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                                querySB.toString(), IHFS, "basis time");
                if ((basisTimes != null) && (!basisTimes.isEmpty())) {
                    basisTime = dateFormat.format(basisTimes.get(0)[0]);
                }
                /*
                 * For typeSource "F" -- Building validTime from timeArg if
                 * 'NEXT', validTime = current time else if x|HH:MM|y, current
                 * time + x days and then find the validTime closest to the
                 * HH:MM within the +/- y interval
                 */
                String validTimeCondition;
                querySB.setLength(0);

                String currentTime_ms_str = dateFormat
                        .format(currentTimeCal.getTime());
                validTimeCondition = ">= '" + currentTime_ms_str + "'";
                querySB.append("SELECT value, validTime FROM ");
                querySB.append(tableName);
                querySB.append(" WHERE lid = '");
                querySB.append(lid);
                querySB.append("' AND pe = '");
                querySB.append(physicalElement);
                querySB.append("' AND ts = '");
                querySB.append(typeSource);
                querySB.append("' AND basistime = '");
                querySB.append(basisTime);
                querySB.append("' AND extremum = '");
                querySB.append(extremum);
                querySB.append("' ");
                if (RiverHydroConstants.NEXT.equals(timeArg)) {
                    /*
                     * (Original, legacy. Leave in for now.) This will never be
                     * true because it's checked up above
                     */
                    querySB.append("AND validTime ");
                    querySB.append(validTimeCondition);
                    querySB.append(" ORDER BY validTime limit 1");
                } else {
                    querySB.append("AND ");
                    querySB.append(queryTimeConstraint);
                }
            }

            if (querySB.length() == 0) {
                return retVal;
            }

            List<Object[]> queryResults = DatabaseQueryUtil
                    .executeDatabaseQuery(QUERY_MODE.MODE_SQLQUERY,
                            querySB.toString(), IHFS, "physical element");

            if ((queryResults != null) && (queryResults.isEmpty() == false)) {
                int closestIndex = 0;
                long minDiff = 0;
                Date resultDate;
                /*
                 * loop through results and valid time results from query and
                 * choose the closest value/time to the given value time (ref
                 * time)
                 */
                for (int i = 0; i < queryResults.size(); ++i) {
                    try {
                        resultDate = (Date) queryResults.get(i)[1];
                    } catch (Exception e) {
                        statusHandler.error("Invalid result date: "
                                + queryResults.get(i)[1], e);
                        return retVal;
                    }
                    long diff = Math.abs(resultDate.getTime()
                            - referenceCal.getTimeInMillis());
                    if (i == 0 || diff < minDiff) {
                        minDiff = diff;
                        closestIndex = i;
                    }
                }

                Object validTimeObj = queryResults.get(closestIndex)[1];
                if (validTimeObj instanceof Date) {
                    retVal.put(valueKey, queryResults.get(closestIndex)[0]);
                    retVal.put(validtimeKey, ((Date) validTimeObj).getTime());
                }
            }
        }
        return retVal;
    }

    public List<CrestHistory> getCrestHistory(String lid, String peColumnName,
            List<String> crestTypeList) {

        List<CrestHistory> crestHistoryList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(CrestHistory.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(CrestHistory.TABLE_NAME);
        querySB.append(" WHERE ");
        querySB.append(" lid = '");
        querySB.append(lid);
        querySB.append("'");
        if (crestTypeList != null) {
            if (crestTypeList.size() == 1) {
                querySB.append(" AND prelim = '");
                querySB.append(crestTypeList.get(0));
                querySB.append("' ");
            } else {
                boolean isFirst = true;
                for (String type : crestTypeList) {
                    if (isFirst == true) {
                        querySB.append(" AND ( prelim = '");
                        isFirst = false;
                    } else {
                        querySB.append(" OR prelim = '");
                    }
                    querySB.append(type);
                    querySB.append("'");
                }
                querySB.append(") ");
            }
        }
        if (peColumnName != null) {
            querySB.append(" AND " + peColumnName + " is NOT NULL ");
        }
        querySB.append(" ORDER BY lid, datcrst ASC");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS crest table");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            crestHistoryList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                CrestHistory crestHistory = new CrestHistory(queryResult);
                crestHistoryList.add(crestHistory);
            }
        } else {
            crestHistoryList = Lists.newArrayListWithExpectedSize(0);
        }

        return (crestHistoryList);
    }

    @Override
    public List<CrestHistory> queryFlowCrestHistory(String lid,
            List<String> crestTypeList) {

        /* Build the query depending on the crest types we want. */
        return (queryCrestHistory(lid, COLUMN_Q, crestTypeList));
    }

    @Override
    public List<CrestHistory> queryFlowCrestHistory(String lid) {
        List<String> crestTypeList = Lists.newArrayListWithExpectedSize(2);
        crestTypeList.add("R");
        crestTypeList.add("O");
        return (queryCrestHistory(lid, COLUMN_Q, crestTypeList));
    }

    @Override
    public List<CrestHistory> queryStageCrestHistory(String lid,
            List<String> crestTypeList) {

        /* Build the query depending on the crest types we want. */
        return (queryCrestHistory(lid, COLUMN_STAGE, crestTypeList));
    }

    @Override
    public List<CrestHistory> queryStageCrestHistory(String lid) {
        List<String> crestTypeList = Lists.newArrayListWithExpectedSize(2);
        crestTypeList.add("R");
        crestTypeList.add("O");
        return queryCrestHistory(lid, COLUMN_STAGE, crestTypeList);
    }

    @Override
    public Map<String, String> queryAreaInundationCoordinateMap() {
        Map<String, String> lidAreas = null;
        String query = new String("SELECT lid, area FROM locarea order by lid");
        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, query.toString(), IHFS,
                "IHFS locarea");

        if (queryResults != null) {
            lidAreas = Maps.newHashMapWithExpectedSize(queryResults.size());
            for (Object[] queryResult : queryResults) {
                String lid = (String) queryResult[0];
                String areaCandidates = (String) queryResult[1];
                if (areaCandidates != null && areaCandidates.length() > 0) {
                    lidAreas.put(lid, areaCandidates);
                }
            }
        } else {
            lidAreas = Maps.newHashMapWithExpectedSize(0);
        }

        return lidAreas;
    }

    @Override
    public String queryAreaInundationCoordinates(String lid) {
        /*
         * Load the lat/lon coords of this forecast point.
         */
        String[] areaAndCenter = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT area FROM locarea WHERE lid = '");
        querySB.append(lid);
        querySB.append("'");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS location");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            Object[] queryResult = queryResults.get(0);
            String area = (String) queryResult[0];
            areaAndCenter = area.split(Pattern.quote("||"));
        } else {
            areaAndCenter = new String[1];
        }

        return areaAndCenter[0];
    }

    /**
     * Query for the Lat/Lon Coordinates for a List of River Forecast Point Id
     * (LID) values.
     * 
     * This is part of the automatic sub query for all RiverForecastPoint
     * objects.
     * 
     * @param lidList
     *            River forecast point identifier List
     * @return A list of Object[], each of which contains a latitude and a
     *         longitude value. NOTE: WESTERN HEMISPHERE LONGITUDES ARE STORED
     *         AS POSITIVE VALUES! These values are NOT corrected within this
     *         method.
     * 
     *         For example:
     * 
     *         new Object[]{ LID, 42.0072222222222d, 96.2413888888889d } This
     *         refers to a coordinate in the WESTERN HEMISPHERE (Normally a
     *         Negative (and WEST) longitude.
     */
    protected List<Object[]> queryForecastPointCoordinates(
            List<String> lidList) {
        /*
         * Load the lat/lon coords of this forecast point.
         */
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT lid, lat, lon FROM Location WHERE ");
        appendToWhereClause(querySB, "lid", lidList, true);

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS location");
        return queryResults;
    }

    @Override
    public List<CrestHistory> queryCrestHistory(String lid,
            String crestValueColumn, List<String> crestTypeList) {

        /* Build the query depending on the crest types we want. */
        List<CrestHistory> crestHistoryList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(CrestHistory.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(CrestHistory.TABLE_NAME);
        querySB.append(" WHERE ");
        querySB.append(" lid = '");
        querySB.append(lid);
        querySB.append("' AND ");
        appendToWhereClause(querySB, "prelim", crestTypeList, true);
        querySB.append(" AND ");
        querySB.append(crestValueColumn);
        querySB.append(" is not NULL ");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS Crest");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            crestHistoryList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            CrestHistory crestHistory = null;
            for (Object[] queryResult : queryResults) {
                crestHistory = new CrestHistory(queryResult);
                crestHistoryList.add(crestHistory);
            }
        } else {
            crestHistoryList = Lists.newArrayListWithExpectedSize(0);
        }

        return (crestHistoryList);
    }

    @Override
    public List<FloodStmtData> queryFloodStatementDataList(String lid,
            int month, int day) {

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
        List<FloodStmtData> floodStmtDataList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(FloodStmtData.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(FloodStmtData.TABLE_NAME);
        querySB.append(" WHERE lid = '");
        querySB.append(lid);
        querySB.append("' ");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS floodstmt table");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            floodStmtDataList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            FloodStmtData floodStmtData = null;
            for (Object[] queryResult : queryResults) {
                floodStmtData = new FloodStmtData(queryResult);

                int floodStmtMonthStart = floodStmtData.getMonthStart();
                int floodStmtDayStart = floodStmtData.getDayStart();
                int floodStmtMonthEnd = floodStmtData.getMonthEnd();
                int floodStmtDayEnd = floodStmtData.getDayEnd();
                boolean isValid = true;
                if ((month >= floodStmtMonthStart)
                        && (month <= floodStmtMonthEnd)) {
                    if ((month == floodStmtMonthStart)
                            && (day < floodStmtDayStart)) {
                        isValid = false;
                    }
                    if ((month == floodStmtMonthEnd)
                            && (day > floodStmtDayEnd)) {
                        isValid = false;
                    }
                } else {
                    isValid = false;
                }

                if (isValid == true) {
                    floodStmtDataList.add(floodStmtData);
                }
            }
        } else {
            floodStmtDataList = Lists.newArrayListWithExpectedSize(0);
        }

        return (floodStmtDataList);
    }

    @Override
    public List<RiverStationInfo> queryRiverStationInfoList(
            List<String> lidList) {

        List<RiverStationInfo> riverStationInfoList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append(RiverStationInfo.COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(RiverStationInfo.TABLE_NAME);
        querySB.append(" WHERE ");
        appendToWhereClause(querySB, "lid", lidList, true);

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS River Station table");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            riverStationInfoList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            RiverStationInfo riverStationInfo = null;
            for (Object[] result : queryResults) {
                riverStationInfo = new RiverStationInfo(result);
                riverStationInfoList.add(riverStationInfo);
            }
        } else {
            riverStationInfoList = Lists.newArrayListWithExpectedSize(0);
        }
        return (riverStationInfoList);
    }

    @Override
    public List<CountyStateData> queryCountyData(String lid) {
        return queryLidToCountyDataMap(Lists.newArrayList(lid)).get(lid);
    }

    @Override
    public Map<String, List<CountyStateData>> queryLidToCountyDataMap(
            List<String> lidList) {
        Map<String, List<CountyStateData>> lidToCountyStateDataMap = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT countynum.lid, ");
        querySB.append(CountyStateData.QUALIFIED_COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append(CountyStateData.TABLE_NAME);
        querySB.append(", CountyNum WHERE ");
        appendToWhereClause(querySB, "CountyNum.lid", lidList, true);
        querySB.append(
                " AND Counties.state = CountyNum.state AND Counties.county = CountyNum.county ");

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS Counties-CountyNum ");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            lidToCountyStateDataMap = Maps
                    .newHashMapWithExpectedSize(queryResults.size());
            CountyStateData countyStateData = null;
            for (Object[] result : queryResults) {
                countyStateData = new CountyStateData(result);
                String lid = countyStateData.getLid();
                List<CountyStateData> tmpList;
                if (lidToCountyStateDataMap.containsKey(lid)) {
                    tmpList = lidToCountyStateDataMap.get(lid);
                } else {
                    tmpList = new ArrayList<CountyStateData>(result.length);
                }
                tmpList.add(countyStateData);
                lidToCountyStateDataMap.put(lid, tmpList);
            }
        } else {
            lidToCountyStateDataMap = Maps.newHashMapWithExpectedSize(0);
        }
        return lidToCountyStateDataMap;
    }

    @Override
    public List<String> queryLidListForCounty(List<String> countyNameList,
            List<String> countyNumList) {

        List<String> lidList = null;
        boolean isFirst = true;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT lid FROM ");
        querySB.append(CountyStateData.TABLE_NAME);
        querySB.append(" WHERE ");
        if (countyNameList != null) {
            this.appendToWhereClause(querySB, "county", countyNameList, true);
            isFirst = false;
        }

        if (countyNumList != null) {
            if (isFirst == false) {
                querySB.append(" AND ");
            }
            appendToWhereClause(querySB, "countyNum", countyNumList, true);
        }

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS CountyNum table");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            lidList = Lists.newArrayListWithExpectedSize(queryResults.size());
            for (Object[] result : queryResults) {
                lidList.add((String) result[0]);
            }
        } else {
            lidList = Lists.newArrayListWithExpectedSize(0);
        }
        return (lidList);
    }

    @Override
    public List<CountyStateData> queryCountyStateData(String lid) {
        return queryCountyStateDataList(Lists.newArrayList(lid));
    }

    @Override
    public List<CountyStateData> queryCountyStateDataList(
            List<String> lidList) {

        List<CountyStateData> countyStateDataList = null;
        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT ");
        querySB.append("CountyNum.lid, ");
        querySB.append(CountyStateData.QUALIFIED_COLUMN_NAME_STRING);
        querySB.append(" FROM ");
        querySB.append("CountyNum, ");
        querySB.append(CountyStateData.TABLE_NAME);
        querySB.append(" WHERE ");
        appendToWhereClause(querySB, "CountyNum.lid", lidList, true);
        querySB.append(" AND CountyNum.state = ");
        querySB.append(CountyStateData.TABLE_NAME);
        querySB.append(".state AND CountyNum.county = ");
        querySB.append(CountyStateData.TABLE_NAME);
        querySB.append(".county");
        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS Counties, CountyNum tables");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            countyStateDataList = Lists
                    .newArrayListWithExpectedSize(queryResults.size());
            CountyStateData countyStateData = null;
            for (Object[] result : queryResults) {
                countyStateData = new CountyStateData(result);
                countyStateDataList.add(countyStateData);
            }
        } else {
            countyStateDataList = Lists.newArrayListWithExpectedSize(0);

        }
        return (countyStateDataList);
    }

    @Override
    public List<RiverMetadata> queryRiverMetadata(String hsa) {
        String query = "SELECT lid, name, stream, group_id, ordinal, hsa "
                + "FROM FpInfo";

        String where = " WHERE group_id in (select group_id "
                + "from rpffcstgroup) AND pe IS NOT NULL";

        String orderBy = " ORDER BY group_id, ordinal ASC NULLS LAST";

        StringBuilder buffer = new StringBuilder(query);
        buffer.append(where);
        if (hsa != null) {
            buffer.append(" and hsa = '").append(hsa).append("' ");
        }
        buffer.append(orderBy);

        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, buffer.toString(), IHFS,
                "River Metadata");
        Map<String, RiverMetadata> riverData = new HashMap<>();
        if ((queryResults != null) && (!queryResults.isEmpty())) {
            for (Object[] oa : queryResults) {
                int i = 0;
                String lid = (String) oa[i++];
                String name = (String) oa[i++];
                String stream = (String) oa[i++];
                String groupId = (String) oa[i++];
                Object object = oa[i++];
                int ordinal = 0;
                if (object != null) {
                    ordinal = (int) object;
                }
                String streamHSA = (String) oa[i++];
                RiverGageMetadata rgm = new RiverGageMetadata();
                rgm.setGroupId(groupId);
                rgm.setLid(lid);
                rgm.setName(name);
                rgm.setOrdinal(ordinal);

                RiverMetadata river = riverData.get(stream);
                if (river == null) {
                    river = new RiverMetadata();
                    river.setGroupId(groupId);
                    river.setGroup(stream);
                    river.setHsa(streamHSA);
                    riverData.put(stream, river);
                }
                river.add(rgm);
            }

            // Sort the river gages on each river
            for (RiverMetadata rm : riverData.values()) {
                List<RiverGageMetadata> gageList = rm.getRiverGages();
                gageList.sort(new RiverGageMetadataComparator());
            }
        }

        statusHandler.info("RiverData: " + riverData.values());
        return new ArrayList<RiverMetadata>(riverData.values());
    }

    /**
     * Query "Primary" Physical Element Value Map for a list of River Forecast
     * Point Id (LID) values.
     * 
     * This is part of the default River Forecast Point (shallow) query.
     * 
     * @param lid
     *            River Forecast Point identifier List
     * @return Map of Lid to Primary Physical Element values
     */
    protected Map<String, String> queryRiverStatPrimaryPEMap(
            List<String> lidList) {

        StringBuilder querySB = new StringBuilder();
        querySB.append("SELECT lid, primary_pe FROM ");
        querySB.append(RiverStationInfo.TABLE_NAME);
        querySB.append(" WHERE ");
        this.appendToWhereClause(querySB, "lid", lidList, true);

        Map<String, String> lidToPrimaryPEMap = Maps
                .newHashMapWithExpectedSize(lidList.size());
        List<Object[]> queryResults = DatabaseQueryUtil.executeDatabaseQuery(
                QUERY_MODE.MODE_SQLQUERY, querySB.toString(), IHFS,
                "IHFS RiverStat table");
        if ((queryResults != null) && (queryResults.isEmpty() == false)) {
            for (Object[] result : queryResults) {
                String lid = (String) result[0];
                String primaryPE = (String) result[1];
                lidToPrimaryPEMap.put(lid, primaryPE);
            }
        }
        return (lidToPrimaryPEMap);
    }

    /**
     * Get the correct table name for queries base on the Physical Element value
     * and isForecast flag.
     * 
     * @param pe
     *            The SHEF physical element code
     * @param isForecast
     *            Boolean flag denoting whether this is for a Forecast query or
     *            not
     * @return Table Name String
     */
    private String getTableNameForPhysicalElement(String pe,
            boolean isForecast) {
        String tableName;
        boolean isHeight = pe.startsWith("h") || pe.startsWith("H");
        // Forecast
        if (isForecast) {
            if (isHeight) {
                tableName = SHEFForecast.TABLE_NAME_HEIGHT; // "fcstheight";
            } else {
                tableName = SHEFForecast.TABLE_NAME_DISCHARGE; // "fcstdischarge";
            }
        } else {
            // observed
            if (isHeight) {
                tableName = SHEFObserved.TABLE_NAME_HEIGHT; // "height";
            } else {
                tableName = SHEFObserved.TABLE_NAME_DISCHARGE; // "discharge";
            }
        }

        return tableName;
    }

    /**
     * Get the correct Precipitation table name for queries base on the Physical
     * Element value. A value of "PC" will query the curpc table. A value of
     * "PP" (default) will query the curpp table.
     * 
     * @param pe
     *            The SHEF physical element code
     * @return Table Name String
     */
    private String getPrecipTableNameForPhysicalElement(String pe) {
        String tableName;

        boolean isAccumulative = true;
        if ("PC".compareToIgnoreCase(pe) == 0) {
            isAccumulative = false;
        }
        // Precip
        if (isAccumulative) {
            tableName = SHEFPrecip.TABLE_NAME_CURPP; // "curpp";
        } else {
            tableName = SHEFPrecip.TABLE_NAME_CURPC; // "curpc";
        }

        return tableName;
    }

    /**
     * Append an element or list of elements to a WHERE clause.
     * 
     * If there is only 1 element in the list than a "<field name> = <val 1>"
     * construct will be used. Otherwise, the clause
     * "<field name> IN (<val 1>, <val 2>, ..., <val n>)" will be generated.
     * 
     * @param querySB
     *            String Builder with the existng SELECT FROM WHERE clauses
     * @param columnName
     *            Name of table column to query
     * @param columnValueList
     *            List of values to query for
     * @param addSingleTicks
     *            true for querying quoted string values
     */
    private void appendToWhereClause(StringBuilder querySB, String columnName,
            List<? extends Object> columnValueList, boolean addSingleTicks) {

        if ((querySB != null) && (columnName != null)
                && (columnValueList != null)
                && (columnValueList.isEmpty() == false)) {
            querySB.append(" ");
            if (columnValueList.size() == 1) {
                querySB.append(columnName);
                querySB.append(" = ");
                if (addSingleTicks == true) {
                    querySB.append("'");
                }
                querySB.append(columnValueList.get(0));
                if (addSingleTicks == true) {
                    querySB.append("' ");
                }
            } else {
                // For simplicity a simple "IN(...) clause is implemented
                boolean isFirst = true;
                querySB.append(columnName);
                querySB.append(" IN (");
                for (Object val : columnValueList) {
                    if (isFirst == false) {
                        querySB.append(",");
                    } else {
                        isFirst = false;
                    }
                    if (addSingleTicks == true) {
                        querySB.append("'");
                    }
                    querySB.append(val);
                    if (addSingleTicks == true) {
                        querySB.append("'");
                    }
                }
                querySB.append(") ");
            }
        }
    }
}
