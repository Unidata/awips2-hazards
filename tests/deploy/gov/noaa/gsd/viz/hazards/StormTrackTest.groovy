package gov.noaa.gsd.viz.hazards
import static org.junit.Assert.*;

import gov.noaa.gsd.viz.hazards.display.ModelDecorator
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict



import spock.lang.*

/**
 *
 * Description: Functional test of modifying a storm track.
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
class StormTrackTest extends spock.lang.Specification {
    ModelDecorator model
    TestingUtils testingUtils


    def setup() {
	testingUtils = new TestingUtils()
	model = testingUtils.buildModel()
    }

    def cleanup() {
	System.out.println(TestingUtils.benchmarkResults(getClass().getName(), model))
    }

    def "Storm Track Modification" () {
	String stormEvent = testingUtils.testFileAsString("stormEvent0.json");
	String modifiedStorm = testingUtils.testFileAsString("modifiedStorm0.json");
	model.putEvent(stormEvent)
	model.modifyEventArea(modifiedStorm)
	String modifiedEvent = model.getSessionEvent("84")
	Dict modifiedEventAsDict = Dict.getInstance(modifiedEvent)
	List point = modifiedEventAsDict.get("draggedPoints").get(0).get(0)
	double x = point.get(0)
	double y = point.get(1)

	expect:
	assertEquals(x, -96.837, 0.01)
	assertEquals(y, 40.44, 0.01)
    }
}
