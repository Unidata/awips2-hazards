package gov.noaa.gsd.uf.common.recommenders.hydro
import static org.junit.Assert.*
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.HazardSettings
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverForecastPoint
import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverProFloodRecommender
import spock.lang.*

class RecordFloodStatusTest extends spock.lang.Specification {

    def setupSpec() {
        println "first time only"
    }

    def "New Flood Stage of Record"() {

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "HG"
        forecastPoint.getMaximumObservedForecastValue() >> 21.0d
        forecastPoint.getFloodCategory() >> [0d, 5d, 10d, 15d, 20d].toArray()
        hazardSettings.getVtecRecordStageOffset() >> 2.0d

        when: "The River Flood Recommender is run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The Flood Status should be Near Record"

        recordStatus == "NR"
    }

    def "Close to Flood Stage of Record"() {

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "HG"
        forecastPoint.getMaximumObservedForecastValue() >> 18.0d
        forecastPoint.getFloodCategory() >> [0d, 5d, 10d, 15d, 20d].toArray()
        hazardSettings.getVtecRecordStageOffset() >> 2.0d

        when: "The River Flood Recommender is run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The Flood Status Should be near record (NR)"

        recordStatus == "NR"
    }


    def "Not a Flood Stage of Record" () {

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "HG"
        forecastPoint.getMaximumObservedForecastValue() >> 17.9d
        forecastPoint.getFloodCategory() >> [0d, 5d, 10d, 15d, 20d].toArray()
        hazardSettings.getVtecRecordStageOffset() >> 2.0d

        when: "The River Flood Recommender is Run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The flood status should be no record (NO)"

        recordStatus == "NO"
    }

    def "Missing Flood Stage of Record"(){

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "HG"
        forecastPoint.getMaximumObservedForecastValue() >> 17.9d
        forecastPoint.getFloodCategory() >> [
            0d,
            5d,
            10d,
            15d,
            RiverForecastPoint.MISSINGVAL
        ].toArray()
        hazardSettings.getVtecRecordStageOffset() >> 2.0d

        when: "The River Flood Recommender is Run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The flood status should be no period of record (UU)"

        recordStatus == "UU"
    }

    def "Missing Maximum Observed Forecast Stage Value"(){

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "HG"
        forecastPoint.getMaximumObservedForecastValue() >> RiverForecastPoint.MISSINGVAL
        forecastPoint.getFloodCategory() >> [0d, 5d, 10d, 15d, 20d].toArray()
        hazardSettings.getVtecRecordStageOffset() >> 2.0d

        when: "The River Flood Recommender is Run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The Flood Status should be no period of record (UU)"

        recordStatus == "UU"
    }

    def "New Flood Flow of Record"() {

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "QR"
        forecastPoint.getMaximumObservedForecastValue() >> 7000d
        forecastPoint.getFloodCategory() >> [
            0d,
            500d,
            1000d,
            3000d,
            6000d
        ].toArray()
        hazardSettings.getVtecRecordFlowOffset() >> 1000d

        when: "The River Flood Recommender is Run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The Flood Status should be near record flood (NR)"

        recordStatus == "NR"
    }

    def "Close to Flood Flow of Record"() {

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "QR"
        forecastPoint.getMaximumObservedForecastValue() >> 5000d
        forecastPoint.getFloodCategory() >> [
            0d,
            500d,
            1000d,
            3000d,
            6000d
        ].toArray()
        hazardSettings.getVtecRecordFlowOffset() >> 1000d

        when: "The River Flood Recommender is Run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The Flood Status should be near record flood (NR)"

        recordStatus == "NR"
    }

    def "Not a Flood Flow of Record"() {

        RiverForecastPoint forecastPoint = Mock()
        HazardSettings hazardSettings = Mock()

        forecastPoint.getPhysicalElement() >> "QR"
        forecastPoint.getMaximumObservedForecastValue() >> 3000d
        forecastPoint.getFloodCategory() >> [
            0d,
            500d,
            1000d,
            3000d,
            6000d
        ].toArray()
        hazardSettings.getVtecRecordFlowOffset() >> 1000d

        when: "The River Flood Recommender is Run"

        RiverProFloodRecommender recommender = new RiverProFloodRecommender(new FloodRecommenderDCTN1TestDAO());
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings, forecastPoint);

        then: "The Flood Status should be no record flood (NO)"

        recordStatus == "NO"
    }
}
