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
class NoObservedDataNEBN1Test extends spock.lang.Specification {
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
            ["Freemont|IA"],
            ["Otoe|NE" ]
        ];

        hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        riverStatus = null;

        ingestTable = [["1|RG"], ["1|RX"]];

        ingestRecord = ["1|FF|NEBN1|HG"];

        basisTime = "2012-12-12 15:40:00";

        systemTime = "2012-12-12 18:00:00";

        bestTS = "RG";

        flowCrest = [];

        stageCrest = [27.66d];

        location = [
            40.6819444444444d,
            95.8466666666667d
        ];

        lookBackHoursForAllForecastPoints = 72;
        lookForwardHoursForAllForecastPoints = 360;
        basisHoursForAllForecastPoints = 72;
        shiftHoursForAllForecastPoints = 6;
        defaultStageWindow = 0.5;

        forecastPointsInCounty = ["NEBN1"];

        observations = [];

        stateName = "Nebraska";

        forecasts = [
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-12 18:00:00",
                "2012-12-12 15:40:00",
                18.38,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:59:19"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-13 00:00:00",
                "2012-12-12 15:40:00",
                18.25,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:59:19"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-13 06:00:00",
                "2012-12-12 15:40:00",
                18.17,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:59:19"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-13 12:00:00",
                "2012-12-12 15:40:00",
                18.17,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:59:19"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-13 18:00:00",
                "2012-12-12 15:40:00",
                17.76,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:59:19"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-14 00:00:00",
                "2012-12-12 15:40:00",
                17.39,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:59:19"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-14 06:00:00",
                "2012-12-12 15:40:00",
                15.74,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:32:07"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-14 12:00:00",
                "2012-12-12 15:40:00",
                14.77,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:32:07"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-14 18:00:00",
                "2012-12-12 15:40:00",
                12.72,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:32:07"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-15 00:00:00",
                "2012-12-12 15:40:00",
                10.6,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:32:07"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-15 06:00:00",
                "2012-12-12 15:40:00",
                6.98,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:32:07"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-15 12:00:00",
                "2012-12-12 15:40:00",
                5.97,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 17:32:07"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-15 18:00:00",
                "2012-12-12 15:40:00",
                5.7,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 15:43:26"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-16 00:00:00",
                "2012-12-12 15:40:00",
                5.7,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 15:43:26"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-16 06:00:00",
                "2012-12-12 15:40:00",
                5.6,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 15:43:26"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-16 12:00:00",
                "2012-12-12 15:40:00",
                5.6,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 15:43:26"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-16 18:00:00",
                "2012-12-12 15:40:00",
                5.6,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 15:43:26"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-17 00:00:00",
                "2012-12-12 15:40:00",
                5.6,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 15:43:26"
            ],
            [
                "NEBN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2012-12-17 06:00:00",
                "2012-12-12 15:40:00",
                5.6,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2012-12-12 15:43:00",
                "2012-12-12 15:43:26"]
        ];
    }


    def "Test No Observed Data"() {

        Object[] fpInfoid = [
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
            "MISORIV",
            5,
            0.5d,
            "PE",
            12,
            240,
            18d,
            18d,
            25.5d,
            29.6d,
            83340d,
            187975d,
            303643d
        ]

        def floodDAO = new FloodRecommenderTestDAO(fpInfoid, location,
                forecastGroup, countyStateList, hazardSettings, riverStatus,
                ingestTable, ingestRecord, basisTime, systemTime, bestTS,
                flowCrest, stageCrest, lookBackHoursForAllForecastPoints,
                lookForwardHoursForAllForecastPoints,
                basisHoursForAllForecastPoints, shiftHoursForAllForecastPoints,
                defaultStageWindow, forecastPointsInCounty, observations,
                forecasts, stateName);


        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new RiverProDataManager(floodDAO))

        when:"The Flood Recommender is Run without Observations"

        Map<String, Object> sessionAttributeMap = new HashMap<String, Object>();
        Map<String, Object> dialogInputMap = new HashMap<String, Object>();
        Map<String, Object> spatialInputMap = new HashMap<String, Object>();
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

        recommendation.getEndTime().getTime() == 1355473756098
        attributeMap.fallBelow == 1355408956098
        attributeMap.riseAbove == 0
        recommendation.getStartTime().getTime() == 1355335200000
    }
}
