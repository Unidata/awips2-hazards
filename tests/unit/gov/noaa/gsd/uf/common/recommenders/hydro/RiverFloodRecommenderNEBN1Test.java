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
 * Description: Tests the recommended hazard for a hydrograph which rises above
 * flood stage and stays above flood for the remainder of the forecast.
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
public class RiverFloodRecommenderNEBN1Test {

    private IFloodRecommenderDAO floodDAO;

    @Before
    public void setUp() throws Exception {
        Object[] fpInfoid = new Object[] {
                "NEBN1",
                "Nebraska City",
                "Otoe",
                "NE",
                "OAX",
                "GID",
                "FSD",
                "Missouri River",
                18d,
                16d,
                18d,
                83340d,
                69440d,
                "HG",
                "T",
                "At",
                "This is an area upstream a couple of miles and downstream a couple of miles.",
                "MISORIV", 5, 0.5d, "PE", 12, 240, 18d, 18d, 25.5d, 29.6d,
                83340d, 187975d, 303643d };

        Object[] forecastGroup = { "MISORIV", "Missouri River", 1, "N" };
        Object[][] countyStateList = { { "Freemont|IA" }, { "Otoe|NE" } };

        HazardSettings hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        Object[] riverStatus = { "NEBN1", "HG", 0, "RG", "Z", -1,
                "2012-12-07 17:00:00", null, 6.58d };

        Object[][] ingestTable = { { "1|RG" }, { "1|RX" } };

        Object[] ingestRecord = { "1|FF|NEBN1|HG" };

        String basisTime = "2012-12-07 15:17:00";

        String systemTime = "2012-12-07 16:00:00";

        String bestTS = "RG";

        Object[] flowCrest = {};

        Object[] stageCrest = { 27.66d };

        Object[] location = { 40.6819444444444d, 95.8466666666667d };

        int lookBackHoursForAllForecastPoints = 72;
        int lookForwardHoursForAllForecastPoints = 360;
        int basisHoursForAllForecastPoints = 72;
        int shiftHoursForAllForecastPoints = 6;
        double defaultStageWindow = 0.5;

        String[] forecastPointsInCounty = { "NEBN1" };

        Object[][] observations = {
                { "NEBN1", "HG", 0, "RG", "Z", "2012-12-07 16:00:00", 15.14,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 16:03:00", "2012-12-07 16:27:10" },
                { "NEBN1", "HG", 0, "RG", "Z", "2012-12-07 16:15:00", 6.58,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 17:03:00", "2012-12-07 17:04:27" },
                { "NEBN1", "HG", 0, "RG", "Z", "2012-12-07 16:30:00", 6.58,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 17:03:00", "2012-12-07 17:04:27" },
                { "NEBN1", "HG", 0, "RG", "Z", "2012-12-07 16:45:00", 6.58,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 17:03:00", "2012-12-07 17:04:27" },
                { "NEBN1", "HG", 0, "RG", "Z", "2012-12-07 17:00:00", 6.58,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 17:03:00", "2012-12-07 17:04:27" } };

        Object[][] forecasts = {
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-07 18:00:00",
                        "2012-12-07 15:17:00", 15.78, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-08 00:00:00",
                        "2012-12-07 15:17:00", 17, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-08 06:00:00",
                        "2012-12-07 15:17:00", 17.91, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-08 12:00:00",
                        "2012-12-07 15:17:00", 18.21, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-08 18:00:00",
                        "2012-12-07 15:17:00", 18.36, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-09 00:00:00",
                        "2012-12-07 15:17:00", 18.41, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-09 06:00:00",
                        "2012-12-07 15:17:00", 18.47, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-09 12:00:00",
                        "2012-12-07 15:17:00", 18.45, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-09 18:00:00",
                        "2012-12-07 15:17:00", 18.51, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-10 00:00:00",
                        "2012-12-07 15:17:00", 18.43, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-10 06:00:00",
                        "2012-12-07 15:17:00", 18.38, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-10 12:00:00",
                        "2012-12-07 15:17:00", 18.45, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-10 18:00:00",
                        "2012-12-07 15:17:00", 18.51, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-11 00:00:00",
                        "2012-12-07 15:17:00", 18.56, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-11 06:00:00",
                        "2012-12-07 15:17:00", 18.66, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-11 12:00:00",
                        "2012-12-07 15:17:00", 18.6, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-11 18:00:00",
                        "2012-12-07 15:17:00", 18.58, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-12 00:00:00",
                        "2012-12-07 15:17:00", 18.66, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-12 06:00:00",
                        "2012-12-07 15:17:00", 18.71, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" },
                { "NEBN1", "HG", 0, "FF", "Z", -1, "2012-12-12 12:00:00",
                        "2012-12-07 15:17:00", 18.58, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 16:30:22" }

        };

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

        EventSet<IHazardEvent> results = recommender.getPotentialRiverHazards(true,
                false);
        assertEquals(1, results.size());
        IHazardEvent recommendation = results.iterator().next();
        Map<String, Serializable> attributeMap = recommendation
                .getHazardAttributes();

        assertEquals("FL", recommendation.getPhenomenon());
        assertEquals("W", recommendation.getSignificance());

        assertEquals(1354952880000L, recommendation.getStartTime().getTime());
        assertEquals(1354996080000L, recommendation.getEndTime().getTime());
        assertEquals(1355292000000L, attributeMap.get("crest"));
        assertEquals(18.71, attributeMap.get("crestStage"));
        assertEquals(6.58, attributeMap.get("currentStage"));
        assertEquals(1354952880000L, attributeMap.get("riseAbove"));
        assertEquals(0, attributeMap.get("fallBelow"));
        assertEquals("NO", attributeMap.get("floodRecord"));
        assertEquals("1", attributeMap.get("floodSeverity"));
        assertEquals("ER", attributeMap.get("immediateCause"));
        assertEquals("NEBN1", attributeMap.get("pointID"));
        assertEquals(16.0, attributeMap.get("actionStage"));
        assertEquals(18.0, attributeMap.get("floodStage"));
        assertEquals(HazardState.POTENTIAL, recommendation.getState());
    }

}
