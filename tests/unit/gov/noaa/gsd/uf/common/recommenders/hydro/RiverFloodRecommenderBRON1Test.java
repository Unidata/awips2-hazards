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
 * Description: Tests the hazard recommendation based on a complex hydrograph
 * which oscillates around flood stage.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 7, 2012            bryon.lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class RiverFloodRecommenderBRON1Test {

    private IFloodRecommenderDAO floodDAO;

    @Before
    public void setUp() throws Exception {

        Object[] fpInfoid = new Object[] {
                "BRON1",
                "Brownsville",
                "Nemaha",
                "NE",
                "OAX",
                "GID",
                "FSD",
                "Missouri River",
                32d,
                30d,
                32d,
                75500d,
                62500d,
                "HG",
                "T",
                "At",
                "This is from the mouth of the Nishnabotna River upstream, downstream to Corning.",
                "MISORIV", 6, 0.5d, "PE", 12, 240, 18d, 32d, 37d, 43d, 75500d,
                117500d, 442600d };

        Object[] forecastGroup = { "MISORIV", "Missouri River", 1, "N" };
        Object[][] countyStateList = { { "Nemaha|NE" } };

        HazardSettings hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        Object[] riverStatus = { "BRON1", "HG", 0, "RG", "Z", -1,
                "2012-12-07 21:24:00", null, 21.95d };

        Object[][] ingestTable = { { "1|RG" }, { "1|RX" } };

        Object[] ingestRecord = { "1|FF|BRON1|HG" };

        String basisTime = "2012-12-07 15:17:00";

        String systemTime = "2012-12-07 22:00:00";

        String bestTS = "RG";

        Object[] flowCrest = {};

        Object[] stageCrest = { 44.3d };

        Object[] location = { 40.3988888888889d, 95.6533333333333d };

        int lookBackHoursForAllForecastPoints = 72;
        int lookForwardHoursForAllForecastPoints = 360;
        int basisHoursForAllForecastPoints = 72;
        int shiftHoursForAllForecastPoints = 6;
        double defaultStageWindow = 0.5;

        String[] forecastPointsInCounty = { "BRON1" };

        Object[][] observations = {
                { "BRON1", "HG", 0, "RG", "Z", "2012-12-07 20:15:00", 22.13,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 20:24:00", "2012-12-07 20:25:13" },
                { "BRON1", "HG", 0, "RG", "Z", "2012-12-07 20:30:00", 22.13,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 21:24:00", "2012-12-07 21:25:11" },
                { "BRON1", "HG", 0, "RG", "Z", "2012-12-07 20:45:00", 21.94,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 21:24:00", "2012-12-07 21:25:11" },
                { "BRON1", "HG", 0, "RG", "Z", "2012-12-07 21:00:00", 21.95,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 21:24:00", "2012-12-07 21:25:11" },
                { "BRON1", "HG", 0, "RG", "Z", "2012-12-07 21:15:00", 21.95,
                        "Z", 1879048191, 0, "KWOHRRSOAX",
                        "2012-12-07 21:24:00", "2012-12-07 21:25:11" }

        };

        Object[][] forecasts = {
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-07 18:00:00",
                        "2012-12-07 15:17:00", 21.9, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-08 00:00:00",
                        "2012-12-07 15:17:00", 30.27, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-08 06:00:00",
                        "2012-12-07 15:17:00", 32.5, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-08 12:00:00",
                        "2012-12-07 15:17:00", 32.14, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-08 18:00:00",
                        "2012-12-07 15:17:00", 31.24, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-09 00:00:00",
                        "2012-12-07 15:17:00", 32.43, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-09 06:00:00",
                        "2012-12-07 15:17:00", 31.28, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-09 12:00:00",
                        "2012-12-07 15:17:00", 32.32, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-09 18:00:00",
                        "2012-12-07 15:17:00", 31.31, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-10 00:00:00",
                        "2012-12-07 15:17:00", 32.5, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-10 06:00:00",
                        "2012-12-07 15:17:00", 31.1, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-10 12:00:00",
                        "2012-12-07 15:17:00", 32.57, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-10 18:00:00",
                        "2012-12-07 15:17:00", 31.06, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 22:05:23" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-11 00:00:00",
                        "2012-12-07 15:17:00", 21.4, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-11 06:00:00",
                        "2012-12-07 15:17:00", 21.5, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-11 12:00:00",
                        "2012-12-07 15:17:00", 21.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-11 18:00:00",
                        "2012-12-07 15:17:00", 21.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-12 00:00:00",
                        "2012-12-07 15:17:00", 21.3, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-12 06:00:00",
                        "2012-12-07 15:17:00", 21.1, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" },
                { "BRON1", "HG", 0, "FF", "Z", -1, "2012-12-12 12:00:00",
                        "2012-12-07 15:17:00", 20.8, "Z", 1879048191, 1,
                        "KKRFRVFMOM", "2012-12-07 15:27:00",
                        "2012-12-07 15:28:11" } };

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
     * Tests the recommended hazard for the case where the hydrograph oscillates
     * around flood stage in one forecast.
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
        assertEquals(1354941556951L, recommendation.getStartTime().getTime());
        assertEquals(1355213753643L, recommendation.getEndTime().getTime());
        assertEquals(1355140800000L, attributeMap.get("crest"));
        assertEquals(32.57, attributeMap.get("crestStage"));
        assertEquals(21.95, attributeMap.get("currentStage"));
        assertEquals(1354941556951L, attributeMap.get("riseAbove"));
        assertEquals(1355148953643L, attributeMap.get("fallBelow"));
        assertEquals("NO", attributeMap.get("floodRecord"));
        assertEquals("1", attributeMap.get("floodSeverity"));
        assertEquals("ER", attributeMap.get("immediateCause"));
        assertEquals("BRON1", attributeMap.get("pointID"));
        assertEquals(30.0, attributeMap.get("actionStage"));
        assertEquals(32.0, attributeMap.get("floodStage"));
        assertEquals(HazardState.POTENTIAL, recommendation.getState());
    }
}
