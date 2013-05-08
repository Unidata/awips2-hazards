/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.uf.common.recommenders.hydro;

import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.CountyForecastGroup;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.HazardSettings;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.IFloodRecommenderDAO;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverForecastGroup;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverForecastPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.common.collect.Lists;

/**
 * Description: Flood Recommender DAO for use with unit tests. This allows the
 * tests to define the test data as a part of their setup. SOFTWARE HISTORY Date
 * Ticket# Engineer Description ------------ ---------- -----------
 * -------------------------- Dec 7, 2012 bryon.lawrence Initial creation </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class FloodRecommenderTestDAO implements IFloodRecommenderDAO {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public final static int DEFAULT_OBS_FCST_BASIS_HOURS = 72;

    public final static int DEFAULT_ENDTIME_SHIFT_HOURS = 6;

    public final static float DEFAULT_STAGE_WINDOW = 0.5f;

    private final int shiftHoursForAllForecastPoints;

    private final int basisHoursForAllForecastPoints;

    private final int lookBackHoursForAllForecastPoints;

    private final int lookForwardHoursForAllForecastPoints;

    private final double defaultStageWindow;

    private final Object[] fpInfoid;

    private final Object[] location;

    private final Object[] forecastGroup;

    private final Object[][] countyStateList;

    private final HazardSettings hazardSettings;

    private final Object[] riverStatus;

    private final Object[][] ingestTable;

    private final Object[] ingestRecord;

    private final String basisTime;

    private final String systemTime;

    private final String bestTS;

    private final Object[] flowCrest;

    private final Object[] stageCrest;

    private final String[] forecastPointsInCounty;

    private final Object[][] observations;

    private final Object[][] forecasts;

    static public final long QUESTIONABLE_BAD_THRESHOLD = 1073741824;

    /**
     * 
     */
    public FloodRecommenderTestDAO(Object[] fpInfoid, Object[] location,
            Object[] forecastGroup, Object[][] countyStateList,
            HazardSettings hazardSettings, Object[] riverStatus,
            Object[][] ingestTable, Object[] ingestRecord, String basisTime,
            String systemTime, String bestTS, Object[] flowCrest,
            Object[] stageCrest, int lookBackHoursForAllForecastPoints,
            int lookForwardHoursForAllForecastPoints,
            int basisHoursForAllForecastPoints,
            int shiftHoursForAllForecastPoints, double defaultStageWindow,
            String[] forecastPointsInCounty, Object[][] observations,
            Object[][] forecasts) {
        this.fpInfoid = fpInfoid;
        this.location = location;
        this.forecastGroup = forecastGroup;
        this.countyStateList = countyStateList;
        this.hazardSettings = hazardSettings;
        this.riverStatus = riverStatus;
        this.ingestTable = ingestTable;
        this.ingestRecord = ingestRecord;
        this.basisTime = basisTime;
        this.systemTime = systemTime;
        this.bestTS = bestTS;
        this.flowCrest = flowCrest;
        this.stageCrest = stageCrest;
        this.lookBackHoursForAllForecastPoints = lookBackHoursForAllForecastPoints;
        this.lookForwardHoursForAllForecastPoints = lookForwardHoursForAllForecastPoints;
        this.basisHoursForAllForecastPoints = basisHoursForAllForecastPoints;
        this.shiftHoursForAllForecastPoints = shiftHoursForAllForecastPoints;
        this.defaultStageWindow = defaultStageWindow;
        this.forecastPointsInCounty = forecastPointsInCounty;
        this.observations = observations;
        this.forecasts = forecasts;
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public List<RiverForecastPoint> getForecastPointInfo(
            HazardSettings hazardSettings) {

        ArrayList<RiverForecastPoint> forecastPointList = Lists.newArrayList();

        // Create a new forecast point.
        // reference to fp structure.
        RiverForecastPoint fp = new RiverForecastPoint(fpInfoid, this);
        forecastPointList.add(fp);

        return forecastPointList;

    }

    @Override
    public List<RiverForecastGroup> getForecastGroupInfo(
            List<RiverForecastPoint> forecastPointList) {

        List<RiverForecastGroup> riverGroupList = Lists.newArrayList();

        ArrayList<RiverForecastPoint> forecastPointsInGroupList = new ArrayList<RiverForecastPoint>();
        forecastPointsInGroupList.add(forecastPointList.get(0));

        RiverForecastGroup riverGroup = new RiverForecastGroup(
                forecastPointList, this.forecastGroup,
                forecastPointsInGroupList);
        riverGroupList.add(riverGroup);

        return riverGroupList;
    }

    @Override
    public List<CountyForecastGroup> getForecastCountyGroups(
            HazardSettings hazardSettings,
            List<RiverForecastPoint> forecastPointList) {
        List<Object[]> countyStateList = Lists.newArrayList();

        for (Object[] countyStateRecord : this.countyStateList) {
            countyStateList.add(countyStateRecord);
        }

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
    public HazardSettings retrieveRecommenderSettings() {

        return this.hazardSettings;

    }

    @Override
    public List<Object[]> retrieveRiverStatus(String id,
            String physicalElement, long beginValidTime, long systemTime) {
        List<Object[]> riverStatusResults = Lists.newArrayList();

        if (this.riverStatus != null) {
            riverStatusResults.add(this.riverStatus);
        }

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

        for (Object[] ingestRecord : this.ingestTable) {
            ingestResults.add(ingestRecord);
        }

        return ingestResults;

    }

    @Override
    public List<Object[]> getIngestTable() {
        List<Object[]> ingestResults = Lists.newArrayList();
        ingestResults.add(this.ingestRecord);

        return ingestResults;
    }

    @Override
    public List<Object[]> getRiverObservedHydrograph(String lid,
            String physicalElement, String typeSource, long obsBeginTime,
            long obsEndTime) {

        List<Object[]> observationRecordList = Lists.newArrayList();

        for (Object[] observation : this.observations) {
            observationRecordList.add(observation);
        }

        return observationRecordList;
    }

    @Override
    public List<Object[]> getRiverForecastBasisTimes(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime) {

        List<Object[]> basisTimeResults = Lists.newArrayList();
        basisTimeResults.add(new Object[] { this.basisTime });

        return basisTimeResults;

    }

    @Override
    public List<Object[]> getRiverForecastHydrograph(String lid,
            String physicalElement, String typeSource, Date systemTime,
            long endValidTime, long basisBTime, boolean useLatestForecast,
            List<Object[]> basisTimeResults) {

        List<Object[]> forecastResults = Lists.newArrayList();

        for (Object[] forecast : this.forecasts) {
            forecastResults.add(forecast);
        }

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
        return this.bestTS;
    }

    @Override
    public List<Object[]> getForecastPointsInCountyStateHSA(String state,
            String county, String hsaID) {
        List<Object[]> countyForecastPointList = Lists.newArrayList();

        Object[] record = new Object[] { this.forecastPointsInCounty[0], state,
                county };
        countyForecastPointList.add(record);

        return countyForecastPointList;

    }

    @Override
    public List<Object[]> getForecastPointCoordinates(String lid) {
        /*
         * Load the lat/lon coords of this forecast point.
         */
        List<Object[]> locationResults = Lists.newArrayList();
        locationResults.add(this.location);

        return locationResults;
    }

    @Override
    public List<Object[]> getFlowCrestHistory(String lid) {
        List<Object[]> crestResults = Lists.newArrayList();
        crestResults.add(this.flowCrest);
        return crestResults;
    }

    @Override
    public List<Object[]> getStageCrestHistory(String lid) {
        List<Object[]> crestResults = Lists.newArrayList();
        crestResults.add(this.stageCrest);
        return crestResults;
    }

    @Override
    public Date getSystemTime() {
        Date systemDate = null;

        try {
            systemDate = getDateFormat().parse(this.systemTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return systemDate;
    }

}
