package gov.noaa.gsd.viz.hazards.mixedhazardstory
import static org.junit.Assert.*;

import com.google.common.collect.Lists

import gov.noaa.gsd.viz.hazards.TestingUtils;
import gov.noaa.gsd.viz.hazards.display.ModelDecorator
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList
import gov.noaa.gsd.common.utilities.Utils


import spock.lang.*
import static TestingUtils.*

/**
 * 
 * Description: Functional test of the dam break recommender piece of the mixed
 * hazard story.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 13, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
@Ignore
class DamBreakRecommenderTest extends spock.lang.Specification {
    ModelDecorator model;

    final static String DAM_BREAK_FLOOD_RECOMMENDER = "DamBreakFloodRecommender"
    final static String BRANCH_OAK_DAM = "Branch Oak Dam"
    List<String> originalEventIds


    def setup() {
        File vtecDir = new File("/tmp/com.raytheon.uf.common.localization.PathManagerFactoryTest/utility/cave_static/user/none/gfe/userPython");
        vtecDir.mkdirs();
        model = new TestingUtils().buildModel()
        def spatialAsString = model.getComponentData(SPATIAL_COMPONENT, ALL_EVENTS)
        DictList originalSpatial = DictList.getInstance(spatialAsString)
        originalEventIds = extractEventIdsFromDictList(originalSpatial)
    }


    def cleanup() {
        System.out.println(TestingUtils.benchmarkResults(getClass().getName(), model))
    }

    def "Dam Break Recommender"() {
        when: "Spatial info for the " + DAM_BREAK_FLOOD_RECOMMENDER + " is retrieved"
        def spatialInfo = model.getSpatialInfo(DAM_BREAK_FLOOD_RECOMMENDER)

        then: "There is none"
        spatialInfo == null

        when: "Dialog info is retrieved for the " + DAM_BREAK_FLOOD_RECOMMENDER
        def dialogInfo = model.getDialogInfo(DAM_BREAK_FLOOD_RECOMMENDER)
        def dialogInfoAsDict = Dict.getInstance(dialogInfo)
        String title = dialogInfoAsDict.getDynamicallyTypedValue("title")
        title = title.toLowerCase()

        then: "The title is appropriate for the" +  DAM_BREAK_FLOOD_RECOMMENDER
        title.contains("dam") and title.contains("break") and title.contains("recommender")

        when: DAM_BREAK_FLOOD_RECOMMENDER + " is run for " + BRANCH_OAK_DAM
        String runData0 = testFileAsString("damBreakRundata0.json");
        def damBreakRecommendation = model.runTool(DAM_BREAK_FLOOD_RECOMMENDER,
                runData0)
        then: "The result is null because tools are now run asychronously"
        damBreakRecommendation == null
    }
    //        Dict damBreakRecommendationAsDict = Dict.getInstance(damBreakRecommendation).get("resultData")
    //        String damName = damBreakRecommendationAsDict.get("damName")
    //        String eventID = damBreakRecommendationAsDict.get(EVENT_ID)
    //
    //        String selectedEventsAfterTool = model.getSelectedEvents()
    //        def hid = model.getComponentData(HID_COMPONENT, ALL_EVENTS)
    //        DictList hidDictList = DictList.getInstance(hid)
    //        Dict hidDict = hidDictList.get(0)
    //
    //        def spatialAsString = model.getComponentData(SPATIAL_COMPONENT, ALL_EVENTS)
    //        DictList spatialEvents = DictList.getInstance(spatialAsString)
    //        List<String> newSpatialEventIds = extractEventIdsFromDictList(spatialEvents)
    //
    //        def consoleAsString = model.getComponentData(CONSOLE_COMPONENT, ALL_EVENTS)
    //        Dict console = Dict.getInstance(consoleAsString)
    //        List<String> consoleEvents = console.get("events")
    //        List<String> newConsoleEventIds = extractEventIdsFromList(consoleEvents)
    //        Dict foundSpatialEvent = eventFromEventID(spatialEvents, eventID)
    //        Dict foundConsoleEvent = eventFromEventID(consoleEvents, eventID)
    //
    //        then: "the dam name " + BRANCH_OAK_DAM
    //        damName.equals(BRANCH_OAK_DAM)
    //
    //        and: "The hazard information display has the appropriate information"
    //        selectedEventsAfterTool.contains(eventID)
    //        hidDict.get(EVENT_ID) == eventID
    //        hidDict.get(CAUSE) == "Dam Failure"
    //        hidDict.get(TYPE) == "FF.W.NonConvective"
    //        hidDict.get(SUBTYPE) == "NonConvective"
    //        hidDict.get(DAM_NAME) == BRANCH_OAK_DAM
    //        hidDict.get(STATE) == PENDING
    //
    //        and: "The spatial display has the new event"
    //        newSpatialEventIds.size() == originalEventIds.size() + 1
    //        newSpatialEventIds.contains(eventID)
    //        foundSpatialEvent != null
    //
    //        and: "The console events table has the new event and is pending and selected"
    //        newConsoleEventIds.size() == originalEventIds.size() + 1
    //        newConsoleEventIds.contains(eventID)
    //        foundConsoleEvent != null
    //        foundConsoleEvent.get(STATE) == PENDING
    //        foundConsoleEvent.get(SELECTED) == true
    //
    //
    //        when: "issue event"
    //        model.createProductsFromEventIDs(TRUE)
    //        consoleAsString = model.getComponentData(CONSOLE_COMPONENT, ALL_EVENTS)
    //        console = Dict.getInstance(consoleAsString)
    //        consoleEvents = console.get("events")
    //        foundConsoleEvent = eventFromEventID(consoleEvents, eventID)
    //
    //        /**
    //         * TODO  This got broken and needs to be fixed.
    //         */
    //        then: "State of that event is issued"
    //        //        foundConsoleEvent.get(STATE) == ISSUED
    //
    //        when: "reset events"
    //        /**
    //         * TODO Mitigates but does not solve race condition.  Multiple
    //         * tests are reading and writing from the database
    //         * in a way that is not thread safe.
    //         */
    //        model.reset(TestingUtils.EVENTS)
    //        model.initialize("1297137637240", "1297137637240", "Flood", "", "Operational", "OAX", "{}")
    //        consoleAsString = model.getComponentData(CONSOLE_COMPONENT, ALL_EVENTS)
    //        console = Dict.getInstance(consoleAsString)
    //        consoleEvents = console.get("events")
    //        List<String> consoleEventIds = extractEventIdsFromList(consoleEvents)
    //
    //        spatialAsString = model.getComponentData(SPATIAL_COMPONENT, ALL_EVENTS)
    //        spatialEvents = DictList.getInstance(spatialAsString)
    //        List<String> spatialEventIds = extractEventIdsFromDictList(spatialEvents)
    //
    //        then: "Only the original events are left"
    //        consoleEventIds.size() == originalEventIds.size()
    //        spatialEventIds.size() == originalEventIds.size()


    private Dict eventFromEventID(List consoleEvents, String eventID) {
        Dict foundEvent = null
        for (Dict consoleEvent : consoleEvents) {
            if (consoleEvent.get(EVENT_ID) == eventID) {
                foundEvent = consoleEvent
                break
            }
        }
        return foundEvent
    }

    private List<String> extractEventIdsFromList(List<Dict> spatial) {
        List<String> result = Lists.newArrayList()
        for (Dict event : spatial) {
            result.add(event.get(EVENT_ID))
        }
        return result
    }

    private List<String> extractEventIdsFromDictList(DictList dictList) {
        List<String> result = Lists.newArrayList()
        for (Dict event : dictList) {
            result.add(event.get(EVENT_ID))
        }
        return result
    }

    private String asJsonArray(String eventID) {
        String eventIDString = String.format("[\"%s\"]", eventID)
        return eventIDString
    }

    private String testFileAsString(String fileName) {
        String filePath = getClass().getResource(fileName).getPath()
        return Utils.textFileAsString(filePath)
    }
}
