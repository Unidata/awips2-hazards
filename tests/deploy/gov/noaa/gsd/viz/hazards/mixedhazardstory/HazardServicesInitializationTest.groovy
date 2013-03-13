package gov.noaa.gsd.viz.hazards.mixedhazardstory
import static org.junit.Assert.*;



import gov.noaa.gsd.viz.hazards.TestingUtils;
import gov.noaa.gsd.viz.hazards.display.ModelDecorator
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.utilities.Utils;
import spock.lang.*
import static TestingUtils.*

/**
 *
 * Description: Functional test of initialization of the {@link IHazardServicesModel}
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
class HazardServicesInitializationTest extends spock.lang.Specification {
    ModelDecorator model;

    def setup() {
	model = new TestingUtils().buildModel()
    }

    def cleanup() {
	System.out.println(TestingUtils.benchmarkResults(getClass().getName(), model))
    }

    def "Hazard Services Initialization"() {
	def spatialData = model.getComponentData(SPATIAL_COMPONENT, ALL_EVENTS)
	def spatialDataAsDictList = DictList.getInstance(spatialData);
	Map<String, Object> event0 = spatialDataAsDictList.get(0)
	Dict event0Shape =  event0.get("shapes").get(0)


	def timeLineDuration = model.getTimeLineDuration()
	def selectedTime = model.getSelectedTime()
	def currentTime = model.getCurrentTime()
	def consoleData = model.getComponentData(CONSOLE_COMPONENT, ALL_EVENTS)

	/**
	 * TODO  Cleanup.  consoleData is a Dict but spatialData a DictList
	 */
	Dict componentData = Dict.getInstance(consoleData)
	List eventList = componentData.get("events")
	Dict event1 = eventList.get(1)

	def settingsList = model.getSettingsList()
	Dict settingsListAsDict = Dict.getInstance(settingsList)

	/**
	 * TODO Needs to be cleaned up.  You call getSettingsList and it 
	 * returns a Dict with settingsList embedded.
	 */
	List<String> innerSettingsList = settingsListAsDict.get("settingsList")
	Dict settingsListFloodElement = innerSettingsList.get(0)

	def floodSettings = model.getStaticSettings(FLOOD_SETTING)
	Dict floodSettingsAsDict = Dict.getInstance(floodSettings)
	List<String> toolbarTools = floodSettingsAsDict.get("toolbarTools")
	Dict flashFloodToolEntry = toolbarTools.get(0)

	String filePath = getClass().getResource("frameInfo0.json").getPath();
	String frameInfo0 = Utils.textFileAsString(filePath);

	/**
	 * TODO A weak test that just makes the call but doesn't test
	 * the answer since there are no getters for the frame info.
	 */
	model.updateFrameInfo(frameInfo0)

	model.updateSelectedTime("1297137606953")

	def updateSelectedTime = model.getSelectedTime()

	expect:
	spatialDataAsDictList.size() == 3
	event0.get(EVENT_ID) == "9"
	event0Shape.get("isVisible") == "true"

	/**
	 * TODO.  Cleanup.  isVisible stored as a String but isSelected as
	 * a boolean
	 */
	event0Shape.get("isSelected") == false
	event0Shape.get("label").contains("FA.W")

	timeLineDuration == "172800000"
	selectedTime == "1297137637240"
	currentTime == "1297137637240"

	eventList.size() == 3
	event1.get("type") == "FA.Y"

	settingsListAsDict.get(CURRENT_SETTING_ID).equals("Flood")

	flashFloodToolEntry.get("displayName").equals("Flash Flood Recommender")

	updateSelectedTime == "1297137606953"
    }
}
