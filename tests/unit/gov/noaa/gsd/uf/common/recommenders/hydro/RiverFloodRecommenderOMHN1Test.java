package gov.noaa.gsd.uf.common.recommenders.hydro;

import static org.junit.Assert.*;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.HazardSettings;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.IFloodRecommenderDAO;
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverProFloodRecommender;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.shef.tables.FpinfoId;

/**
 * Description: Test the recommended hazard for a hydrograph which starts above
 * flood stage and falls below flood stage during the forecast period.
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
public class RiverFloodRecommenderOMHN1Test {

    private IFloodRecommenderDAO floodDAO;

    @Before
    public void setUp() throws Exception {
        FpinfoId fpInfoid = new FpinfoId(
                "OMHN1",
                "Omaha",
                "Douglas",
                "NE",
                "OAX",
                "GID",
                "FSD",
                "Missouri River",
                29d,
                27d,
                29d,
                142396d,
                112396d,
                "HG",
                "T",
                "At",
                "This is the area from the Boyer River downstream to Bellevue.",
                "MISORIV", 4, 0.5d, "PE", 12, 240, 18d, 29d, 35d, 40d, 142396d,
                268396d, 392394d);

        Object[] forecastGroup = { "MISORIV", "Missouri River", 1, "N" };
        Object[][] countyStateList = { { "Douglas|NE" },
                { "Pottawatttamie|IA" } };

        HazardSettings hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        Object[] riverStatus = { "OMHN1", "HG", 0, "RG", "Z", -1,
                "2012-12-07 20:00:00", null, 32d };

        Object[][] ingestTable = { { "1|RG" }, { "1|RX" } };

        Object[] ingestRecord = { "1|FF|OMHN1|HG" };

        String basisTime = "2012-12-07 15:17:00";

        String systemTime = "2012-12-07 21:00:00";

        String bestTS = "RG";

        Object[] flowCrest = {};

        Object[] stageCrest = { 40.2d };

        Object[] location = { 41.2588888888889d, 95.922222222222d };

        int lookBackHoursForAllForecastPoints = 72;
        int lookForwardHoursForAllForecastPoints = 360;
        int basisHoursForAllForecastPoints = 72;
        int shiftHoursForAllForecastPoints = 6;
        double defaultStageWindow = 0.5;

        String[] forecastPointsInCounty = { "OMHN1" };

        Object[][] observations = {
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 18:00:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 18:03:00",
                        "2012-12-07 18:04:26" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 18:15:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 19:03:00",
                        "2012-12-07 19:07:38" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 18:30:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 19:03:00",
                        "2012-12-07 19:07:38" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 18:45:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 19:03:00",
                        "2012-12-07 19:07:38" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 19:00:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 19:03:00",
                        "2012-12-07 19:07:38" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 19:15:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 20:03:00",
                        "2012-12-07 20:04:05" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 19:30:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 20:03:00",
                        "2012-12-07 20:04:05" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 19:45:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 20:03:00",
                        "2012-12-07 20:04:05" },
                { "OMHN1", "HG", 0, "RG", "Z", "2012-12-07 20:00:00", 32, "Z",
                        1879048191, 0, "KWOHRRSOAX", "2012-12-07 20:03:00",
                        "2012-12-07 20:04:05" }

        };

        Object[][] forecasts = {
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-07 18:00:00",
                        "2012-12-07 15:17:00", 32.5, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-08 00:00:00",
                        "2012-12-07 15:17:00", 32.84, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-08 06:00:00",
                        "2012-12-07 15:17:00", 33.23, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-08 12:00:00",
                        "2012-12-07 15:17:00", 33.36, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-08 18:00:00",
                        "2012-12-07 15:17:00", 33.36, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-09 00:00:00",
                        "2012-12-07 15:17:00", 33.36, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-09 06:00:00",
                        "2012-12-07 15:17:00", 33.41, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-09 12:00:00",
                        "2012-12-07 15:17:00", 33.15, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-09 18:00:00",
                        "2012-12-07 15:17:00", 32.65, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-10 00:00:00",
                        "2012-12-07 15:17:00", 32.09, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-10 06:00:00",
                        "2012-12-07 15:17:00", 31.59, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-10 12:00:00",
                        "2012-12-07 15:17:00", 30.97, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-10 18:00:00",
                        "2012-12-07 15:17:00", 30.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-11 00:00:00",
                        "2012-12-07 15:17:00", 29.57, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-11 06:00:00",
                        "2012-12-07 15:17:00", 28.84, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-11 12:00:00",
                        "2012-12-07 15:17:00", 28.23, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-11 18:00:00",
                        "2012-12-07 15:17:00", 27.65, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-12 00:00:00",
                        "2012-12-07 15:17:00", 26.38, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-12 06:00:00",
                        "2012-12-07 15:17:00", 25.97, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" },
                { "OMHN1", "HG", 0, "FF", "Z", -1, "2012-12-12 12:00:00",
                        "2012-12-07 15:17:00", 25.65, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 20:28:48" } };

        floodDAO = new FloodRecommenderTestDAO(fpInfoid, location,
                forecastGroup, countyStateList, hazardSettings, riverStatus,
                ingestTable, ingestRecord, basisTime, systemTime, bestTS,
                flowCrest, stageCrest, lookBackHoursForAllForecastPoints,
                lookForwardHoursForAllForecastPoints,
                basisHoursForAllForecastPoints, shiftHoursForAllForecastPoints,
                defaultStageWindow, forecastPointsInCounty, observations,
                forecasts);
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

        List<IHazardEvent> results = recommender.getFloodDictList(true);
        assertEquals(1, results.size());
        IHazardEvent recommendation = results.get(0);
        Map<String, Serializable> attributeMap = recommendation
                .getHazardAttributes();

        assertEquals("FL", recommendation.getPhenomenon());
        assertEquals("W", recommendation.getSignificance());
        assertEquals(1354914000000L, recommendation.getStartTime().getTime());
        assertEquals(1355265665754L, recommendation.getEndTime().getTime());
        assertEquals(1355032800000L, attributeMap.get("crest"));
        assertEquals(33.41, attributeMap.get("crestStage"));
        assertEquals(32.0, attributeMap.get("currentStage"));
        assertEquals(0, attributeMap.get("riseAbove"));
        assertEquals(1355200865754L, attributeMap.get("fallBelow"));
        assertEquals("NO", attributeMap.get("floodRecord"));
        assertEquals("1", attributeMap.get("floodSeverity"));
        assertEquals("ER", attributeMap.get("immediateCause"));
        assertEquals("OMHN1", attributeMap.get("pointID"));
        assertEquals(27.0, attributeMap.get("actionStage"));
        assertEquals(29.0, attributeMap.get("floodStage"));
        assertEquals(HazardState.POTENTIAL, recommendation.getState());
    }

}
