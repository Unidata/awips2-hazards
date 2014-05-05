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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
 * 
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IFloodDAO {
    /**
     * Retrieves a list of river forecast points.
     * 
     * @param hazardSettings
     *            Flood recommender configuration values
     * @return List of river forecast points.
     */
    public List<RiverForecastPoint> getForecastPointInfo(
            HazardSettings hazardSettings);

    /**
     * Retrieves a list of river forecast groups.
     * 
     * @param riverForecastPoints
     *            A list of river forecast points
     * @return List of river forecast groups.
     */
    public List<RiverForecastGroup> getForecastGroupInfo(
            List<RiverForecastPoint> riverForecastPoints);

    /**
     * Retrieves the county river point forecast groups.
     * 
     * @param hazardSettings
     *            Flood recommender configuration values
     * @param riverForecastPoints
     *            List of river forecast points
     * @return List of county forecast groups
     */
    public List<CountyForecastGroup> getForecastCountyGroups(
            HazardSettings hazardSettings,
            List<RiverForecastPoint> riverForecastPoints);

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
    public List<Object[]> retrieveRiverStatus(String id,
            String physicalElement, long beginValidTime, long systemTime);

    /**
     * Retrieves data from the IHFS IngestFilter Table.
     * 
     * @param
     * @return A list of object arrays, where each array element corresponds to
     *         a pipe delimited string: ts_rank|ts|lid|pe
     * 
     *         For example: new Object[] {"1|FF|DCTN1|HG"}
     */
    public List<Object[]> getIngestTable();

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
    public List<Object[]> retrieveIngestSettings(String id,
            String physicalElement);

    /**
     * Retrieves the configuration information which determines how the hazard
     * recommendation algorithm works.
     * 
     * @param
     * @return A HazardSettings object.
     */
    public HazardSettings retrieveSettings();

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
    public List<Object[]> getRiverObservedHydrograph(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime);

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
    public List<Object[]> getRiverForecastBasisTimes(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime);

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
    public List<Object[]> getRiverForecastHydrograph(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime, boolean useLatestForecast,
            List<Object[]> basisTimeResults);

    /**
     * 
     * @param
     * @return Number of hours to look back for observed data
     */
    public int getLookBackHoursForAllForecastPoints();

    /**
     * 
     * @param
     * @return Number of hours to look forward for forecast data
     */
    public int getLookForwardHoursForAllForecastPoints();

    /**
     * 
     * @param
     * @return The buffer around a reference stage .
     */
    public double getDefaultStageWindow();

    /**
     * 
     * @param
     * @return The max number of hours to look back for a river forecast basis
     *         time.
     */
    public int getBasisHoursForAllForecastPoints();

    /**
     * 
     * @param
     * @return The number of hours to add to the fall below time.
     * 
     */
    public int getShiftHoursForAllForecastPoints();

    /**
     * The date format expected by the persistence mechanism backing this DAO.
     * 
     * @param
     * @return A SimpleDateFormat object which can be used to convert back and
     *         forth between a date string representation.
     */
    public SimpleDateFormat getDateFormat();

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
    public String getBestTS(String lid, String pe, String ts_prefix, int ordinal);

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
    public List<Object[]> getForecastPointsInCountyStateHSA(String state,
            String county, String HSA);

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
    public List<Object[]> getForecastPointCoordinates(String lid);

    /**
     * 
     * @param lid
     *            River forecast point identifier
     * @return A list of Object[], each of which contains a single double value
     *         representing a flow crest
     */
    public List<Object[]> getFlowCrestHistory(String lid);

    /**
     * 
     * @param lid
     *            River forecast point identifier
     * @return A list of Object[], each of which contains a single double value
     *         representing a stage crest
     */
    public List<Object[]> getStageCrestHistory(String lid);

    /**
     * This methods allows the flood recommender to run displaced in
     * circumstances such as Unit Tests.
     * 
     * @param
     * @return The system time.
     */
    public Date getSystemTime();

    /**
     * Translates a state abbreviation into a state name using the state table
     * in the IHFS database.
     * 
     * @param stateAbbreviation
     *            The two letter state abbreviation. These should be capital
     *            letters.
     * @return The name of the state which matches the abbreviation.
     */

    public String getStateNameForAbbreviation(String stateAbbreviation);

    /**
     * Retrieves the record from the riverstat table in the IHFS database for
     * the specified river point.
     * 
     * @param lid
     *            River point identifier
     * @return The record in the riverstat for the specified river point
     */
    public List<Object[]> getRiverStationInfo(String lid);
}
