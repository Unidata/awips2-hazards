/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.hazards.hydro;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * 
 * The Data Accessor Object (DAO) interface for the River Data Manager. The DAO
 * should provide methods for retrieving all of the data required by the River
 * Data Manager. The River Data Manager should retrieve all of its data from the
 * DAO. The DAO abstracts the details of how data are retrieved. It also allows
 * for easier testing, i.e. test and production DAOs can be created. Also, the
 * DAO can more easily be mocked or stubbed using tools such as JMock, EasyStub,
 * or Groovy Spock.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 27, 2012            bryon.lawrence      Initial creation
 * May 1, 2014  3581       bkowal      Relocate to common hazards hydro
 * Sep 19, 2014   2394     mpduff for nash  interface changes
 * Dec 17, 2014 2394       Ramer/Manross    Updated Interface
 * Feb 21, 2015 4959       Dan Schaffer     Improvements to add/remove UGCs
 * Feb 24, 2015 5960       Manross             Grab flood inundation areas
 * May 08, 2015 6562       Chris.Cody  Restructure River Forecast Points/Recommender
 * May 28, 2015 7139       Chris.Cody  Add curpp and curpc HydrographPrecip query and processing
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IFloodDAO {

    /**
     * Query for a list of Formatted "<County Name>|<State Abbreviation>" values
     * for a Hydrologic Service Area.
     * 
     * @param hsaId
     *            Hydrologic Service Area
     * @return A list of strings containing "<County Name>|<State Abbreviation>"
     *         values
     */
    public List<String> queryCountyStateListForHsa(String hsaId);

    /**
     * Query for a Map of State abbreviation to full State names.
     * 
     * @return A Map of State abbreviation to full State names.
     */
    public Map<String, String> queryStateAbbreviationToNameMap();

    /**
     * Query for All Hydrologic Service Area Id values.
     * 
     * @return List of all HSA ID values
     */
    public List<String> queryHydrologicServiceAreaIdList();

    /**
     * Query for a List of LID (Point Id) values for a: County, or County Num
     * value.
     * 
     * Either List but not both may be null.
     * 
     * @param countyNameList
     *            List of Count Names The name of the county, first letter
     *            capitalized
     * @param countyNumList
     *            List of County Num values
     * @return A list of Lid values which each array corresponding to one record
     *         in the IHFS CountyNum table.
     */
    public List<String> queryLidListForCounty(List<String> countyNameList,
            List<String> countyNumList);

    /**
     * Retrieves a List of RiverForecastPoint objects.
     * 
     * RiverForecastPoint objects contain data from FpInfo, Location, and the
     * Primary PE (Primary Physical Element) value from the RiverStat table.
     * 
     * All but one of the given parameters can be null. All given parameters are
     * AND'ed together.
     * 
     * This is a shallow query.
     * 
     * @param lidList
     *            A list of specific Point Id (lid) values
     * @param hsaList
     *            A list of specific HSA (Hazard Area) values (OAX, etc)
     * @param groupIdList
     *            A list of specific Group Id values
     * @param physicalElementList
     *            A list of specific SHEF Physical Element values
     * 
     * @return {@link RiverForecastPoint} data objects
     */
    public List<RiverForecastPoint> queryRiverForecastPointList(
            List<String> lidList, List<String> hsaList,
            List<String> groupIdList, List<String> physicalElementList);

    /**
     * Query for a single River Forecast Point
     * 
     * This is a shallow query.
     * 
     * @param lid
     *            River Forecast Point LID value
     * @return River Forecast Point object.
     */
    public RiverForecastPoint queryRiverForecastPoint(String lid);

    /**
     * Query for a List of RiverPointZone (ZONENUM) objects for the given
     * Parameters.
     * 
     * At least one input parameter should not be null.
     * 
     * @param lidList
     *            River forecast point identifier list
     * @param stateList
     *            List of State abbreviations
     * @param zoneNumberList
     *            List of Zone Numbers
     * @return A List of RiverPointZone objects
     */
    public List<RiverPointZoneInfo> queryRiverPointZoneInfoList(
            List<String> lidList, List<String> stateList,
            List<String> zoneNumberList);

    /**
     * Query for a RiverPointZone (ZONENUM) object for the given Lid
     * 
     * @param lid
     *            River forecast point identifier
     * @return A RiverPointZone object
     */
    public RiverPointZoneInfo queryRiverPointZoneInfo(String lid);

    /**
     * Query for All river forecast groups.
     * 
     * This is a shallow query.
     * 
     * @return List of river forecast groups.
     */
    public List<RiverForecastGroup> queryAllRiverForecastGroup();

    /**
     * Query for a list of RiverForecastGroup Group Id strings for a Hydrologic
     * Service Area Id.
     * 
     * @param hsaId
     *            A Hydrological Service Area (HSA) Id
     * @return List of RiverForecastGroup Group Id Strings.
     */
    public List<String> queryHsaRiverForecastGroupIdList(String hsa);

    /**
     * Query for a single RiverForecastGroup.
     * 
     * This is a shallow query.
     * 
     * @param groupId
     *            the RiverForecastGroup Group Id value
     * @return One River forecast group.
     */
    public RiverForecastGroup queryRiverForecastGroup(String groupId);

    /**
     * Query for a list of RiverForecastGroup objects.
     * 
     * Query parameters are and-ed together. Either but not both may be null.
     * This is a shallow query.
     * 
     * @param groupIdList
     *            A list of RiverForecastGroup Group Id values
     * @param groupNameList
     *            A list of RiverForecastGroup Group Name values
     * @return List of RiverForecastGroup objects.
     */
    public List<RiverForecastGroup> queryRiverForecastGroupList(
            List<String> groupIdList, List<String> groupNameList);

    /**
     * Query for a single RiverForecastGroup for a RiverForecastPoint lid value.
     * 
     * @param lid
     *            the RiverForecastPoint lid value
     * @return Parent RiverForecastGroup.
     */
    public RiverForecastGroup queryRiverForecastGroupForLid(String lid);

    /**
     * Query for a List of River Forecast Point (LID) values for a given state
     * and county
     * 
     * @param state
     *            State abbreviation
     * @param count
     *            county County Name
     * @return List of LID values
     */
    public List<String> queryCountyRiverForecastPointIdList(String state,
            String county);

    /**
     * Query for the CountyStateData for a River Forecast Point List.
     * 
     * @param lidList
     *            a list of River Forecast Point LID (Point ID) values
     * @return a list of CountyStateData objects
     */
    public List<CountyStateData> queryCountyStateDataList(List<String> lidList);

    /**
     * Query for the CountyStateData for a River Forecast Point.
     * 
     * @param lid
     *            River Forecast Point LID (Point ID)
     * @return CountyStateData object
     */
    public CountyStateData queryCountyStateData(String lid);

    /**
     * 
     * @param lid
     *            River forecast point identifier
     * @param month
     *            1-12 month of the year.
     * @param day
     *            1-31 day of the month.
     * @return A list of FloodStmtData objects for the lid; and where the
     *         specified month and day fall within the start and end dates.
     */
    public List<FloodStmtData> queryFloodStatementDataList(String lid,
            int month, int day);

    /**
     * Retrieves a list of records from the RIVERSTAT table in the IHFS database
     * for the specified river point.
     * 
     * @param lidList
     *            River point identifier list
     * @return The List of records in the RIVERSTAT for the specified river
     *         point lid values
     */
    public List<RiverStationInfo> queryRiverStationInfoList(List<String> lidList);

    /**
     * Query for a List of LID (River Forecast Point Id) values for a State,
     * County and/or Hydrological Service Area Id.
     * 
     * All parameters must be set.
     * 
     * @param state
     *            The two letter, state code (uppercase)
     * @param county
     *            The name of the county, first letter capitalized
     * @param HSA
     *            The Hydrologic Service Area (HSA) id
     * @return A list of Lid values.
     */
    public List<String> queryLidListForCountyStateHSA(String state,
            String county, String hsaID);

    /**
     * Query for the most recent observations for a river forecast point.
     * 
     * @param lid
     *            River forecast point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param beginValidTime
     *            The earliest time to search for a value
     * @param systemTime
     *            The latest time to search for a value
     * @return A list of RiverStatus objects for the given parameters.
     */
    public List<RiverStatus> queryRiverStatusList(String lid,
            String physicalElement, long beginValidTime, long systemTime);

    /**
     * Query for all observation for a river forecast point.
     * 
     * @param lid
     *            River forecast point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @return A list of RiverStatus objects.
     */
    public List<RiverStatus> queryRiverStatusList(String lid,
            String physicalElement);

    /**
     * Query for all River Point Zone (ZONENUM) objects in the database.
     * 
     * @return A list of all RiverPointZoneInfo objects.
     */
    public List<RiverPointZoneInfo> queryAllRiverPointZoneInfo();

    /**
     * Query Ingest data from the IHFS IngestFilter Table.
     * 
     * Data queried has Physical Element value of 'H' or 'Q' and TS (Type
     * Source) starts with 'F'.
     * 
     * @param Physical
     *            Element value
     * @return A list of IngestFilterInfo objects for the given parameters
     */
    public List<IngestFilterInfo> queryHydrographForecastIngestFilter(
            String primary_pe);

    /**
     * Query for IngestFilter settings for a given station (LID) and physical
     * element.
     * 
     * @param lid
     *            River Forecast Point lid
     * @param physicalElement
     *            The SHEF Physical element code
     * @return List of (partial) IngestFilterInfo objects containing ts_rank and
     *         ts
     */
    public List<IngestFilterInfo> queryIngestSettings(String lid,
            String physicalElement);

    /**
     * Query the Best Observed Type Source from the RiverStatus table.
     * 
     * @param lid
     *            River forecast point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param obsBeginTime
     *            Observation Begin Timestamp
     * @param obsEndTime
     *            Observation End Timestamp
     * @return typeSource
     */
    public String queryBestObservedTypeSource(String lid,
            String physicalElement, long obsBeginTime, long obsEndTime);

    /**
     * Query for the configuration information which determines how the hazard
     * recommendation algorithm works.
     * 
     * @return A HazardSettings object.
     */
    public HazardSettings retrieveSettings();

    /**
     * Query for the Observed Hydrograph for a river forecast point.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF typesource code
     * @param obsBeginTime
     *            The lower bound of time window to retrieve observations for
     * @param obsEndTime
     *            The upper bound of the time window to retrieve observations
     *            for. (Current System Time
     * @return A HydrographObserved object. Containing a SHEF Observed object
     *         list with each object corresponding to one row from the IHFS
     *         Discharge or Height table.
     */
    public HydrographObserved queryRiverPointHydrographObserved(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime);

    /**
     * Query a Complete HydrographForecast object.
     * <p>
     * Query for all of the fcstheight or fcstdischarge elements which match the
     * given query parameters. Query for a list of valid basis time values (from
     * basistime table). Match the valid basis time values to the basis time
     * values queried from the SHEF data (fcstheight or fcstdischarge). Keep
     * only the ones with valid basis data times.
     * 
     * Use this query to retrieve the correct HydrographForecast for a
     * RiverForecast Point.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF typesource code
     * @param currentSystemTime
     *            Current system time
     * @param endValidTime
     *            Timestamp marking the end of the valid forecast period
     * @param basisBeginTime
     *            Timestamp for the beginning of the valid forecast period
     * @param useLatestForecast
     *            Use Latest Results flag
     * 
     * @return A HydrographForecast object. Containing a SHEF Forecast object
     *         list with each object corresponding to one row from the IHFS
     *         FcstHeight or FcstDischarge table.
     * 
     */
    public HydrographForecast queryRiverPointHydrographForecast(String lid,
            String physicalElement, long currentSystemTime, long endValidTime,
            long basisBeginTime, boolean useLatestForecast);

    /**
     * Query for the Precipitation Hydrograph for a river forecast point.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param typeSource
     *            The SHEF Type Source code
     * @param obsBeginTime
     *            The lower bound of time window to retrieve observations for
     * @param obsEndTime
     *            The upper bound of the time window to retrieve observations
     *            for. (Current System Time)
     * @return A HydrographPrecip object. Containing a SHEF Precip object list
     *         with each object corresponding to one row from the IHFS curpp or
     *         curpc table.
     */
    public HydrographPrecip queryRiverPointHydrographPrecip(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime);

    /**
     * Get the highest ranked type source given a primary physical element.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param physicalElement
     *            The SHEF physical element code
     * @param duration
     * @param extremum
     *            e.g. Z, X
     */
    public String queryTopRankedTypeSource(String lid, String primary_pe,
            int duration, String extremum);

    /**
     * Retrieves the given physical element value for a river forecast point.
     * 
     * @param lid
     *            River Forecast Point identifier
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
     * 
     *            For example:
     * 
     *            new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
     *            "2011-02-08 18:00:00", "2011-02-08 15:06:00", 39.91, "Z",
     *            1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
     *            "2011-02-08 15:15:10" });
     * @param timeFlag
     *            -- if True return a time string for requested value, otherwise
     *            return requested value
     * @param currentTime_ms
     *            -- current time in milliseconds
     * 
     */
    public String queryPhysicalElementValue(String lid, String physicalElement,
            int duration, String typeSource, String extremum, String timeArg,
            boolean timeFlag, long currentTime_ms);

    /**
     * Query for a Map of LID values to their inundation Lat Lon coordinates
     * 
     * @return A Map with the gauge 'lid' as the key and a string of latitude
     *         and longitude values as the value.
     */
    public Map<String, String> queryAreaInundationCoordinateMap();

    /**
     * Query for a Lat Lon coordinate for a River Forecast Point inundation.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @return A string of latitude and longitude values.
     */
    public String queryAreaInundationCoordinates(String lid);

    /**
     * Query for a List of Crest History values either for (Height or Discharge)
     * based on LID and Historical type
     * 
     * @param lid
     *            River forecast point identifier
     * @param crestValueColumn
     *            Determines whether to query for Height (Crest) or flow (Q)
     *            crest values.
     * @param crestTypes
     *            A list of strings containing single character list of crest
     *            types to filter on. P=preliminary, O=official, R=record. Empty
     *            string means take all types.
     * @return A list of CrestHistory objects which will contain Height or Flow
     *         crest values
     */
    public List<CrestHistory> queryCrestHistory(String lid,
            String crestValueColumn, List<String> crestTypeList);

    /**
     * Query for a List of FLOW Crest values based on a list of Crest Types.
     * 
     * @param lid
     *            River Forecast Point identifier
     * @param crestTypeList
     *            A List containing possible crest types to filter on.
     *            P=preliminary, O=official, R=record. Empty string means take
     *            all types.
     * @return A list of CrestHistory objects.
     */
    public List<CrestHistory> queryFlowCrestHistory(String lid,
            List<String> crestTypeList);

    /**
     * Query for a List of FLOW Crest values based on River Forecast Point id
     * and a list of Crest Types.
     * 
     * FLOW Crest Types are "R" and "O" for this query
     * 
     * @param lid
     *            River forecast point identifier
     * @return A list of CrestHistory objects.
     */
    public List<CrestHistory> queryFlowCrestHistory(String lid);

    /**
     * Query for a List of Stage Crest values based on a list of Crest Types.
     * 
     * @param lid
     *            River forecast point identifier
     * @param crestTypeList
     *            A List containing possible crest types to filter on.
     *            P=preliminary, O=official, R=record. Empty string means take
     *            all types.
     * @return A list of CrestHistory objects.
     */
    public List<CrestHistory> queryStageCrestHistory(String lid,
            List<String> crestTypeList);

    /**
     * Query for a List of Stage Crest values.
     * 
     * Stage Crest Types are "R" and "O" for this query
     * 
     * @param lid
     *            River forecast point identifier
     * @return A list of CrestHistory objects.
     */
    public List<CrestHistory> queryStageCrestHistory(String lid);

    /**
     * Query for a County State Data for a LID (Point Id)
     * 
     * @param lid
     *            LID value
     * @return CountyStateData
     */
    public CountyStateData queryCountyData(String lid);

    /**
     * Query for a Map of LID (Point ID) to County State Data Map
     * 
     * @param lidList
     *            List of LID values
     * @return Map of LID to CountyStateData pairs
     */
    public Map<String, CountyStateData> queryLidToCountyDataMap(
            List<String> lidList);

}
