/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.uf.common.recommenders.hydro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.hazards.hydro.CountyForecastGroup;
import com.raytheon.uf.common.hazards.hydro.HazardSettings;
import com.raytheon.uf.common.hazards.hydro.IFloodDAO;
import com.raytheon.uf.common.hazards.hydro.RiverForecastGroup;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint;

/**
 * Description: TODO
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * MMM DD, YYYY            bryon.lawrence      Initial creation
 * May 1, 2014  3581       bkowal      Updated for hazards hydro refactor
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class FloodRecommenderDCTN1TestDAO implements IFloodDAO {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public final static int DEFAULT_OBS_FCST_BASIS_HOURS = 72;

    public final static int DEFAULT_ENDTIME_SHIFT_HOURS = 6;

    public final static float DEFAULT_STAGE_WINDOW = 0.5f;

    public static int shiftHoursForAllForecastPoints;

    public static int basisHoursForAllForecastPoints;

    public static int lookBackHoursForAllForecastPoints;

    public static int lookForwardHoursForAllForecastPoints;

    public static double defaultStageWindow;

    /*
     * Initialize the back hours, forward hours, adjust end hours, and shift
     * hours fields. This just needs to be done once. These are the base values
     * for all forecast points. Also, load only once information from the
     * IngestFilter table.
     */
    static {
        getHourValues();
    }

    /**
     * Retrieve the time window hourly "FF"sets for general use. These defaults
     * apply to all forecast points. Individual forecast points may override
     * them.
     */
    private static void getHourValues() {
        lookBackHoursForAllForecastPoints = 72;
        lookForwardHoursForAllForecastPoints = 360;
        basisHoursForAllForecastPoints = 72;
        shiftHoursForAllForecastPoints = 6;
        defaultStageWindow = 0.5;
    }

    static public final long QUESTIONABLE_BAD_THRESHOLD = 1073741824;

    private static List<Object[]> ingestResults = null;

    @Override
    public List<RiverForecastPoint> getForecastPointInfo(
            HazardSettings hazardSettings) {

        ArrayList<RiverForecastPoint> forecastPointList = Lists.newArrayList();
        /*
         * Retrieve all of the forecast group information, which has the defined
         * groups.
         */
        /*
         * Loop on the number of groups defined in the table and determine the
         * number of forecast points included per group. This is necessary since
         * some groups may not be used, either because they have no forecast
         * points, or because their forecast points are for a di"FF"erent
         * o"FF"ice.
         */
        Object[] fpInfoid = new Object[] {
                "DCTN1",
                "Decatur",
                "Burt",
                "NE",
                "OAX",
                "GID",
                "FSD",
                "Missouri River",
                35d,
                33d,
                35d,
                115688d,
                97089d,
                "HG",
                "T",
                "At",
                "This is the area upstream to Salix and downstream to Little Sioux.",
                "MISORIV", 2, 0.5d, "PE", 12, 240, 18d, 35d, 38d, 41d, 115688d,
                154688d, 269686d };

        // Create a new forecast point.
        // reference to fp structure.
        RiverForecastPoint fp = new RiverForecastPoint(fpInfoid, this);
        forecastPointList.add(fp);

        return forecastPointList;

    }

    @Override
    public List<RiverForecastGroup> getForecastGroupInfo(
            List<RiverForecastPoint> forecastPointList) {
        Object[] record = { "MISORIV", "Missouri River", 1, "N" };

        List<RiverForecastGroup> riverGroupList = Lists.newArrayList();

        ArrayList<RiverForecastPoint> forecastPointsInGroupList = new ArrayList<RiverForecastPoint>();
        forecastPointsInGroupList.add(forecastPointList.get(0));

        RiverForecastGroup riverGroup = new RiverForecastGroup(
                forecastPointList, record, forecastPointsInGroupList);
        riverGroupList.add(riverGroup);

        return riverGroupList;
    }

    @Override
    public List<CountyForecastGroup> getForecastCountyGroups(
            HazardSettings hazardSettings,
            List<RiverForecastPoint> forecastPointList) {
        List<Object[]> countyStateList = Lists.newArrayList();
        countyStateList.add(new Object[] { "Burt|NE" });
        countyStateList.add(new Object[] { "Monona|IA" });

        ArrayList<CountyForecastGroup> countyForecastGroupList = Lists
                .newArrayList();
        for (Object[] countyRecord : countyStateList) {
            CountyForecastGroup countyForecastGroup = new CountyForecastGroup(
                    forecastPointList, countyRecord, hazardSettings.getHsa(),
                    this);
            countyForecastGroupList.add(countyForecastGroup);
        }

        return countyForecastGroupList;

    }

    @Override
    public HazardSettings retrieveSettings() {
        HazardSettings hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        return hazardSettings;

    }

    @Override
    public List<Object[]> retrieveRiverStatus(String id,
            String physicalElement, long beginValidTime, long systemTime) {
        List<Object[]> riverStatusResults = Lists.newArrayList();
        riverStatusResults.add(new Object[] { "DCTN1", "HG", 0, "RG", "Z", -1,
                "2011-02-08 04:00:00", null, 39.04d });
        return riverStatusResults;
    }

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

        List<Object[]> ingestResults = Lists.newArrayList();
        ingestResults.add(new Object[] { "1|RG" });
        ingestResults.add(new Object[] { "1|RX" });

        return ingestResults;

    }

    @Override
    public List<Object[]> getIngestTable() {
        /*
         * load the type source which pe starts as 'H' or 'Q' and ts starts as
         * 'F'. Only load once.
         */
        if (ingestResults == null) {
            ingestResults = Lists.newArrayList();
            ingestResults.add(new Object[] { "1|FF|DCTN1|HG" });
        }

        return ingestResults;
    }

    @Override
    public List<Object[]> getRiverObservedHydrograph(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime) {

        List<Object[]> observationRecordList = Lists.newArrayList();

        observationRecordList.add(new Object[] { "DCTN1", "HG", 0, "RG", "Z",
                "2011-02-07 17:00:00", 30.93, "Z", 1879048191, 0, "KWOHRRSOAX",
                "2011-02-07 17:03:00", "2011-02-07 17:04:19" });
        observationRecordList.add(new Object[] { "DCTN1", "HG", 0, "RG", "Z",
                "2011-02-07 18:15:00", 32.82, "Z", 1879048191, 0, "KWOHRRSOAX",
                "2011-02-07 19:03:00", "2011-02-07 19:06:51" });
        observationRecordList.add(new Object[] { "DCTN1", "HG", 0, "RG", "Z",
                "2011-02-07 20:15:00", 37.15, "Z", 1879048191, 0, "KWOHRRSOAX",
                "2011-02-07 21:03:00", "2011-02-07 21:04:55" });
        observationRecordList.add(new Object[] { "DCTN1", "HG", 0, "RG", "Z",
                "2011-02-08 02:30:00", 38.85, "Z", 1879048191, 0, "KWOHRRSOAX",
                "2011-02-08 03:03:00", "2011-02-08 03:05:04" });
        observationRecordList.add(new Object[] { "DCTN1", "HG", 0, "RG", "Z",
                "2011-02-08 04:00:00", 39.04, "Z", 1879048191, 0, "KWOHRRSOAX",
                "2011-02-08 04:03:00", "2011-02-08 04:04:23" });

        return observationRecordList;
    }

    @Override
    public List<Object[]> getRiverForecastBasisTimes(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime) {

        List<Object[]> basisTimeResults = Lists.newArrayList();
        basisTimeResults.add(new Object[] { "2011-02-08 15:06:00" });

        return basisTimeResults;

    }

    @Override
    public List<Object[]> getRiverForecastHydrograph(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime, boolean useLatestForecast,
            List<Object[]> basisTimeResults) {

        List<Object[]> forecastResults = Lists.newArrayList();
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-08 18:00:00", "2011-02-08 15:06:00", 39.91, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-09 00:00:00", "2011-02-08 15:06:00", 38.54, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-09 06:00:00", "2011-02-08 15:06:00", 34.4, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-09 12:00:00", "2011-02-08 15:06:00", 29.75, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-09 18:00:00", "2011-02-08 15:06:00", 23.62, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-10 00:00:00", "2011-02-08 15:06:00", 19.41, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-10 06:00:00", "2011-02-08 15:06:00", 13.1, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-10 12:00:00", "2011-02-08 15:06:00", 13.2, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-10 18:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-11 00:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-11 06:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-11 12:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-11 18:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-12 00:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-12 06:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-12 12:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-12 18:00:00", "2011-02-08 15:06:00", 13.3, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-13 00:00:00", "2011-02-08 15:06:00", 13.2, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-13 06:00:00", "2011-02-08 15:06:00", 13.2, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });
        forecastResults.add(new Object[] { "DCTN1", "HG", 0, "FF", "Z", -1,
                "2011-02-13 12:00:00", "2011-02-08 15:06:00", 13.2, "Z",
                1879048191, 1, "KKRFRVFMOM", "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" });

        return forecastResults;
    }

    @Override
    public int getLookBackHoursForAllForecastPoints() {
        return lookBackHoursForAllForecastPoints;
    }

    @Override
    public int getLookForwardHoursForAllForecastPoints() {
        return lookForwardHoursForAllForecastPoints;
    }

    @Override
    public double getDefaultStageWindow() {
        return defaultStageWindow;
    }

    @Override
    public int getBasisHoursForAllForecastPoints() {
        return basisHoursForAllForecastPoints;
    }

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

    @Override
    public String getBestTS(String lid, String pe, String ts_prefix, int ordinal) {
        return "RG";
    }

    @Override
    public List<Object[]> getForecastPointsInCountyStateHSA(String state,
            String county, String hsaID) {
        List<Object[]> countyForecastPointList = Lists.newArrayList();

        Object[] record = new Object[] { "DCTN1", state, county };
        countyForecastPointList.add(record);

        return countyForecastPointList;

    }

    @Override
    public List<Object[]> getForecastPointCoordinates(String lid) {
        /*
         * Load the lat/lon coords of this forecast point.
         */
        List<Object[]> locationResults = Lists.newArrayList();
        locationResults
                .add(new Object[] { 42.0072222222222d, 96.2413888888889d });

        return locationResults;
    }

    @Override
    public List<Object[]> getFlowCrestHistory(String lid) {
        List<Object[]> crestResults = Lists.newArrayList();
        return crestResults;
    }

    @Override
    public List<Object[]> getStageCrestHistory(String lid) {
        List<Object[]> crestResults = Lists.newArrayList();
        crestResults.add(new Object[] { 43.5d });
        return crestResults;
    }

    @Override
    public Date getSystemTime() {
        Date systemDate = null;

        try {
            systemDate = getDateFormat().parse("2011-02-08 04:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return systemDate;
    }

    @Override
    public String getStateNameForAbbreviation(String stateAbbreviation) {
        return "Nebraska";
    }

    @Override
    public List<Object[]> getRiverStationInfo(String lid) {
        List<Object[]> riverStatInfo = Lists.newArrayList();
        riverStatInfo
                .add(new Object[] {
                        "DCTN1",
                        "HG",
                        35,
                        null,
                        316140,
                        0,
                        null,
                        null,
                        35,
                        06601200,
                        null,
                        691,
                        null,
                        "10/1/1929 - Present",
                        "USGS",
                        42.007928,
                        96.242898,
                        "Gage is approx. 500 feet upstream of Highway 175 bridge and along the right bank. Crest data from 1959 to 1974 available in hardcopy at Omaha WFO.",
                        "2012-08-25", "USGS", "Missouri River", "None", "None",
                        "NGVD 1929", null, 33, 1010, "2013-02-17", null, 0, "T" });
        return riverStatInfo;
    }
    @Override
    public String getPhysicalElement(String lid, String physicalElement,
            int duration, String typeSource, String extremum, String timeArg,
            String derivationInstruction, boolean timeFlag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrimaryPE(String lid) {
        throw new UnsupportedOperationException();
    }

}
