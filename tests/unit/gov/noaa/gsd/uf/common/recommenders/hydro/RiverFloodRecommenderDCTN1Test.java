package gov.noaa.gsd.uf.common.recommenders.hydro;

import static org.junit.Assert.assertEquals;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.HazardSettings;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.IFloodRecommenderDAO;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverProFloodRecommender;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Tests the recommended hazard for a hydrograph which rises and
 * falls below flood stage in one forecast. This rises to moderate flood.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 07, 2012            bryon.lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class RiverFloodRecommenderDCTN1Test {

    private IFloodRecommenderDAO floodDAO;

    @Before
    public void setUp() throws Exception {

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

        Object[] forecastGroup = { "MISORIV", "Missouri River", 1, "N" };
        Object[][] countyStateList = { { "Burt|NE" }, { "Monona|IA" } };

        HazardSettings hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        Object[] riverStatus = { "DCTN1", "HG", 0, "RG", "Z", -1,
                "2011-02-08 04:00:00", null, 39.04d };

        Object[][] ingestTable = { { "1|RG" }, { "1|RX" } };

        Object[] ingestRecord = { "1|FF|DCTN1|HG" };

        String basisTime = "2011-02-08 15:06:00";

        String systemTime = "2011-02-08 04:00:00";

        String bestTS = "RG";

        Object[] flowCrest = {};

        Object[] stageCrest = { 43.5d };

        Object[] location = { 42.0072222222222d, 96.2413888888889d };

        int lookBackHoursForAllForecastPoints = 72;
        int lookForwardHoursForAllForecastPoints = 360;
        int basisHoursForAllForecastPoints = 72;
        int shiftHoursForAllForecastPoints = 6;
        double defaultStageWindow = 0.5;

        String[] forecastPointsInCounty = { "DCTN1" };

        Object[][] observations = {
                { "DCTN1", "HG", 0, "RG", "Z", "2011-02-07 17:00:00", 30.93,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2011-02-07 17:03:00", "2011-02-07 17:04:19" },
                { "DCTN1", "HG", 0, "RG", "Z", "2011-02-07 18:15:00", 32.82,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2011-02-07 19:03:00", "2011-02-07 19:06:51" },
                { "DCTN1", "HG", 0, "RG", "Z", "2011-02-07 20:15:00", 37.15,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2011-02-07 21:03:00", "2011-02-07 21:04:55" },
                { "DCTN1", "HG", 0, "RG", "Z", "2011-02-08 02:30:00", 38.85,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2011-02-08 03:03:00", "2011-02-08 03:05:04" },
                { "DCTN1", "HG", 0, "RG", "Z", "2011-02-08 04:00:00", 39.04,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2011-02-08 04:03:00", "2011-02-08 04:04:23" } };

        Object[][] forecasts = {
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-08 18:00:00",
                        "2011-02-08 15:06:00", 39.91, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-09 00:00:00",
                        "2011-02-08 15:06:00", 38.54, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-09 06:00:00",
                        "2011-02-08 15:06:00", 34.4, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-09 12:00:00",
                        "2011-02-08 15:06:00", 29.75, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-09 18:00:00",
                        "2011-02-08 15:06:00", 23.62, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-10 00:00:00",
                        "2011-02-08 15:06:00", 19.41, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-10 06:00:00",
                        "2011-02-08 15:06:00", 13.1, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-10 12:00:00",
                        "2011-02-08 15:06:00", 13.2, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-10 18:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-11 00:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-11 06:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-11 12:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-11 18:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-12 00:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-12 06:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-12 12:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-12 18:00:00",
                        "2011-02-08 15:06:00", 13.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-13 00:00:00",
                        "2011-02-08 15:06:00", 13.2, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-13 06:00:00",
                        "2011-02-08 15:06:00", 13.2, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" },
                { "DCTN1", "HG", 0, "FF", "Z", -1, "2011-02-13 12:00:00",
                        "2011-02-08 15:06:00", 13.2, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2011-02-08 15:15:00",
                        "2011-02-08 15:15:10" } };

        String stateName = "Nebraska";

        floodDAO = new FloodRecommenderTestDAO(fpInfoid, location,
                forecastGroup, countyStateList, hazardSettings, riverStatus,
                ingestTable, ingestRecord, basisTime, systemTime, bestTS,
                flowCrest, stageCrest, lookBackHoursForAllForecastPoints,
                lookForwardHoursForAllForecastPoints,
                basisHoursForAllForecastPoints, shiftHoursForAllForecastPoints,
                defaultStageWindow, forecastPointsInCounty, observations,
                forecasts, stateName);
    }

    /**
     * Tests the recommended hazard in the simple case where the hydrograph
     * rises and falls below flood stage in one forecast.
     * 
     * @param
     * @return
     */
    @Test
    public void testRiverFloodRecommender() {
        RiverProFloodRecommender recommender = new RiverProFloodRecommender(
                floodDAO);

        Map<String, Object> sessionAttributeMap = new HashMap<String, Object>();
        Map<String, Object> dialogInputMap = new HashMap<String, Object>();
        Map<String, Object> spatialInputMap = new HashMap<String, Object>();
        recommender.getRecommendation(sessionAttributeMap, dialogInputMap,
                spatialInputMap);

        EventSet<IHazardEvent> results = recommender.getPotentialRiverHazards(
                true, false);
        assertEquals(1, results.size());
        IHazardEvent recommendation = results.iterator().next();
        Map<String, Serializable> attributeMap = recommendation
                .getHazardAttributes();
        assertEquals("FL", recommendation.getPhenomenon());
        assertEquals("W", recommendation.getSignificance());
        assertEquals(1297106124943L, recommendation.getStartTime().getTime());
        assertEquals(1297292869566L, recommendation.getEndTime().getTime());
        assertEquals(1297188000000L, attributeMap.get("crest"));
        assertEquals(39.91, attributeMap.get("crestStage"));
        assertEquals(39.04, attributeMap.get("currentStage"));
        assertEquals(1297106124943L, attributeMap.get("riseAbove"));
        assertEquals(1297228069566L, attributeMap.get("fallBelow"));
        assertEquals("NO", attributeMap.get("floodRecord"));
        assertEquals("2", attributeMap.get("floodSeverity"));
        assertEquals("ER", attributeMap.get("immediateCause"));
        assertEquals("DCTN1", attributeMap.get("pointID"));
        assertEquals(33.0, attributeMap.get("actionStage"));
        assertEquals(35.0, attributeMap.get("floodStage"));
        assertEquals(HazardState.POTENTIAL, recommendation.getState());
    }
}
