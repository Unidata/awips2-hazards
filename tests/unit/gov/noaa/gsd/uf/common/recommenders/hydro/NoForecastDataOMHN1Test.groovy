/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.uf.common.recommenders.hydro;

import static org.junit.Assert.*
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverProFloodRecommender
import spock.lang.*

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent
import com.raytheon.uf.common.hazards.hydro.HazardSettings
import com.raytheon.uf.common.hazards.hydro.RiverProDataManager


/**
 * Description: Tests to make sure that adjustments to the
 *              endtime shift hours will actually result in
 *              the correct endtime.
 * 
 * <pre>
 * 
 * SOFTWAREs HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 11, 2012            bryon.lawrence      Initial creation
 * Sep 30, 2013            bryon.lawrence      Fixed test failure
 * May 1, 2014  3581       bkowal      Updated to use common hazards hydro
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
class NoForecastDataOMHN1Test extends spock.lang.Specification {
    @Shared Object[] forecastGroup
    @Shared Object[][] countyStateList
    @Shared HazardSettings hazardSettings
    @Shared Object[] riverStatus
    @Shared Object[][] ingestTable
    @Shared Object[] ingestRecord
    @Shared String basisTime
    @Shared String systemTime
    @Shared String bestTS
    @Shared Object[] flowCrest
    @Shared Object[] stageCrest
    @Shared Object[] location
    @Shared int lookBackHoursForAllForecastPoints
    @Shared int lookForwardHoursForAllForecastPoints
    @Shared int basisHoursForAllForecastPoints
    @Shared int shiftHoursForAllForecastPoints
    @Shared double defaultStageWindow
    @Shared String[] forecastPointsInCounty
    @Shared Object[][] observations
    @Shared Object[][] forecasts
    @Shared String stateName

    def setupSpec() {

        forecastGroup = [
            "MISORIV",
            "Missouri River",
            1,
            "N"
        ];
        countyStateList = [
            ["Douglas|NE"],
            [
                "Pottawatttamie|IA" ]
        ];

        hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        riverStatus = [
            "OMHN1",
            "HG",
            0,
            "RG",
            "Z",
            -1,
            "2012-12-12 20:00:00",
            null,
            33.28d
        ];

        ingestTable = [["1|RG"], ["1|RX"]];

        ingestRecord = ["1|FF|OMHN1|HG"];

        basisTime = "2012-12-07 15:17:00";

        systemTime = "2012-12-12 21:00:00";

        bestTS = "RG";

        flowCrest = [];

        stageCrest = [40.2d];

        location = [
            41.2588888888889d,
            95.922222222222d
        ];

        lookBackHoursForAllForecastPoints = 72;
        lookForwardHoursForAllForecastPoints = 360;
        basisHoursForAllForecastPoints = 72;
        shiftHoursForAllForecastPoints = 6;
        defaultStageWindow = 0.5;

        forecastPointsInCounty = ["OMHN1"];

        stateName = "Nebraska";

        observations = [
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 06:45:00",
                8.58,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 07:03:00",
                "2012-12-12 13:44:21"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 07:00:00",
                8.58,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 07:03:00",
                "2012-12-12 13:44:21"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 07:15:00",
                8.58,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 08:03:00",
                "2012-12-12 13:52:27"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 07:30:00",
                8.56,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 08:03:00",
                "2012-12-12 13:52:27"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 07:45:00",
                8.56,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 08:03:00",
                "2012-12-12 13:52:27"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 08:00:00",
                8.57,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 08:03:00",
                "2012-12-12 13:52:27"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 13:15:00",
                26.55,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 14:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 13:30:00",
                27.47,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 14:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 13:45:00",
                28.51,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 14:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 14:00:00",
                29.91,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 14:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 14:15:00",
                29.82,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 15:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 14:30:00",
                29.61,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 15:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 14:45:00",
                29.48,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 15:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 15:00:00",
                29.54,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 15:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 15:15:00",
                29.48,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 16:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 15:30:00",
                29.48,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 16:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 15:45:00",
                30,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 16:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 16:00:00",
                29.83,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 16:03:00",
                "2012-12-12 20:54:15"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 17:15:00",
                29.43,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 18:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 17:30:00",
                29.77,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 18:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 17:45:00",
                29.48,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 18:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 18:00:00",
                29.77,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 18:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 18:15:00",
                30,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 19:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 18:30:00",
                30.8,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 19:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 18:45:00",
                31.44,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 19:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 19:00:00",
                31.95,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 19:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 19:15:00",
                32.24,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 20:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 19:30:00",
                32.41,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 20:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 19:45:00",
                32.87,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 20:03:00",
                "2012-12-12 20:53:25"
            ],
            [
                "OMHN1",
                "HG",
                0,
                "RG",
                "Z",
                "2012-12-12 20:00:00",
                33.28,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2012-12-12 20:03:00",
                "2012-12-12 20:53:25"]
        ];

        forecasts = []
    }


    def "Test No Observed Data"() {

        Object[] fpInfoid = [
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
            "MISORIV",
            4,
            0.5d,
            "PE",
            12,
            240,
            18d,
            29d,
            35d,
            40d,
            142396d,
            268396d,
            392394d
        ];

        def floodDAO = new FloodRecommenderTestDAO(fpInfoid, location,
                forecastGroup, countyStateList, hazardSettings, riverStatus,
                ingestTable, ingestRecord, basisTime, systemTime, bestTS,
                flowCrest, stageCrest, lookBackHoursForAllForecastPoints,
                lookForwardHoursForAllForecastPoints,
                basisHoursForAllForecastPoints, shiftHoursForAllForecastPoints,
                defaultStageWindow, forecastPointsInCounty, observations,
                forecasts, stateName);

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new RiverProDataManager(floodDAO))

        Map<String, Object> sessionAttributeMap = new HashMap<String, Object>();
        Map<String, Object> dialogInputMap = new HashMap<String, Object>();
        Map<String, Object> spatialInputMap = new HashMap<String, Object>();


        when:"The Flood Recommender is Run without Observations"

        recommender.getRecommendation(sessionAttributeMap,
                dialogInputMap,
                spatialInputMap)

        then: "One hazard should be recommended"

        Set<IHazardEvent> resultSet = recommender.getPotentialRiverHazards(false)
        resultSet.size() == 1

        and: "There should be no rise above time"

        Iterator iterator = resultSet.iterator()
        IHazardEvent recommendation = iterator.next()
        Map<String, Serializable> attributeMap = recommendation.getHazardAttributes()

        recommendation.getEndTime().getTime() == 1355363415000
        attributeMap.riseAbove == 1355320215000
        attributeMap.crest == 1355342400000
        attributeMap.fallBelow == 0
        recommendation.getStartTime().getTime() == 1355320215000
        attributeMap.crestStage == 33.28
        attributeMap.immediateCause == "ER"
        attributeMap.currentStage == 33.28
        attributeMap.pointID == "OMHN1"
        attributeMap.actionStage == 27d
        attributeMap.floodStage == 29d
        recommendation.getStatus() == HazardStatus.POTENTIAL
    }
}
