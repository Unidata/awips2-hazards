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
class EndTime0ShiftHoursTest extends spock.lang.Specification {
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
        ]
        countyStateList = [
            ["Burt|NE"],
            ["Monona|IA" ]
        ]

        hazardSettings = new HazardSettings();

        hazardSettings.setRvsExpirationHours(24);
        hazardSettings.setFlsExpirationHours(15);
        hazardSettings.setFlwExpirationHours(12);
        hazardSettings.setObsLookbackHours(72);
        hazardSettings.setForecastLookForwardHours(360);
        hazardSettings.setHsa("OAX");
        hazardSettings.setDefaultTimeZone(null);

        riverStatus = [
            "DCTN1",
            "HG",
            0,
            "RG",
            "Z",
            -1,
            "2011-02-08 04:00:00",
            null,
            39.04d
        ]

        ingestTable = [["1|RG"], ["1|RX"]]

        ingestRecord = ["1|FF|DCTN1|HG"];

        basisTime = "2011-02-08 15:06:00";

        systemTime = "2011-02-08 04:00:00";

        bestTS = "RG";

        flowCrest = [];

        stageCrest = [43.5d];

        location = [
            42.0072222222222d,
            96.2413888888889d
        ];

        lookBackHoursForAllForecastPoints = 72;
        lookForwardHoursForAllForecastPoints = 360;
        basisHoursForAllForecastPoints = 72;
        shiftHoursForAllForecastPoints = 6;
        defaultStageWindow = 0.5;

        forecastPointsInCounty = ["DCTN1"];

        stateName = "Nebraska";

        observations = [
            [
                "DCTN1",
                "HG",
                0,
                "RG",
                "Z",
                "2011-02-07 17:00:00",
                30.93,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2011-02-07 17:03:00",
                "2011-02-07 17:04:19"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "RG",
                "Z",
                "2011-02-07 18:15:00",
                32.82,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2011-02-07 19:03:00",
                "2011-02-07 19:06:51"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "RG",
                "Z",
                "2011-02-07 20:15:00",
                37.15,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2011-02-07 21:03:00",
                "2011-02-07 21:04:55"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "RG",
                "Z",
                "2011-02-08 02:30:00",
                38.85,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2011-02-08 03:03:00",
                "2011-02-08 03:05:04"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "RG",
                "Z",
                "2011-02-08 04:00:00",
                39.04,
                "Z",
                1879048191,
                0,
                "KWOHRRSOAX",
                "2011-02-08 04:03:00",
                "2011-02-08 04:04:23" ]
        ];

        forecasts = [
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-08 18:00:00",
                "2011-02-08 15:06:00",
                39.91,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-09 00:00:00",
                "2011-02-08 15:06:00",
                38.54,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-09 06:00:00",
                "2011-02-08 15:06:00",
                34.4,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-09 12:00:00",
                "2011-02-08 15:06:00",
                29.75,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-09 18:00:00",
                "2011-02-08 15:06:00",
                23.62,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-10 00:00:00",
                "2011-02-08 15:06:00",
                19.41,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-10 06:00:00",
                "2011-02-08 15:06:00",
                13.1,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-10 12:00:00",
                "2011-02-08 15:06:00",
                13.2,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-10 18:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-11 00:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-11 06:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-11 12:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-11 18:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-12 00:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-12 06:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-12 12:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-12 18:00:00",
                "2011-02-08 15:06:00",
                13.3,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-13 00:00:00",
                "2011-02-08 15:06:00",
                13.2,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-13 06:00:00",
                "2011-02-08 15:06:00",
                13.2,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10"
            ],
            [
                "DCTN1",
                "HG",
                0,
                "FF",
                "Z",
                -1,
                "2011-02-13 12:00:00",
                "2011-02-08 15:06:00",
                13.2,
                "Z",
                1879048191,
                1,
                "KKRFRVFMOM",
                "2011-02-08 15:15:00",
                "2011-02-08 15:15:10" ]
        ];
    }


    def "Test 0 Hour Shift"() {
        def shiftHours = 0d

        Object[] fpInfoid = [
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
            "MISORIV",
            2,
            0.5d,
            "PE",
            12,
            240,
            shiftHours,
            35d,
            38d,
            41d,
            115688d,
            154688d,
            269686d
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

        when:"The Flood Recommender is Run"

        Map<String, Object> sessionAttributeMap = new HashMap<String, Object>();
        Map<String, Object> dialogInputMap = new HashMap<String, Object>();
        Map<String, Object> spatialInputMap = new HashMap<String, Object>();
        recommender.getRecommendation(sessionAttributeMap,
                dialogInputMap,
                spatialInputMap)

        then: "One hazard should be recommended"
        Set<IHazardEvent> resultSet = recommender.getPotentialRiverHazards(false, dialogInputMap)
        resultSet.size() == 1

        and: "The hazard end time should equal the fall below time"
        Iterator iterator = resultSet.iterator()
        IHazardEvent recommendation = iterator.next()
        Date endDate = recommendation.getEndTime()
        Map<String, Serializable> attributeMap = recommendation.getHazardAttributes()
        long fallBelowTime = attributeMap.get("fallBelow")

        endDate.getTime() == fallBelowTime
    }
}
