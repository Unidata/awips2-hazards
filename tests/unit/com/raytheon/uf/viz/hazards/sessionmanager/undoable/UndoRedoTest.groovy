package com.raytheon.uf.viz.hazards.sessionmanager.undoable
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import spock.lang.*

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LinearRing

/**
 *
 * Description: Tests the undo/redo functionality associated
 *              with the ObservedHazardEvent geometry.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * August 9, 2013  1265    blawrence    Initial creation in response
 *                                      to code review.
 * Dec  1, 2014 4188       Dan Schaffer Now allowing hazards to be shrunk or expanded when appropriate.
 *
 * </pre>
 *
 * @author bryon.lawrence
 * @version 1.0
 */
class UndoRedoTest extends spock.lang.Specification {

    /**
     * Mocked session event manager
     */
    SessionEventManager sessionEventManager;

    /**
     * Observed hazard event to be tested
     */
    ObservedHazardEvent observedHazardEvent;

    /**
     *  Factory for building test geometries 
     */
    GeometryFactory geometryFactory;

    static Geometry testGeometry1;
    static Geometry testGeometry2;
    static Geometry testGeometry3;

    /**
     * Coordinates to build test
     * polygon 1
     */
    Coordinate[] polygon1Coords = [
        [0, 0],
        [0, 1],
        [1, 1],
        [1, 0],
        [0, 0]
    ]

    /**
     * Coordinates to build test
     * polygon 2
     */
    Coordinate [] polygon2Coords = [
        [0, 0],
        [0, 1],
        [1, 3],
        [1, 0],
        [0, 0]
    ]

    /**
     * Coordinates to build test
     * polygon 3
     */
    Coordinate [] polygon3Coords = [
        [0, 0],
        [0, 2],
        [1, 1],
        [1, 0],
        [0, 0]
    ]

    /**
     * Sets up this test.
     */
    def setup() {
        geometryFactory = new GeometryFactory()
        mockSessionEventManager()
        testGeometry1 = buildTestGeometry(polygon1Coords)
        testGeometry2 = buildTestGeometry(polygon2Coords)
        testGeometry3 = buildTestGeometry(polygon3Coords)
        IHazardEvent event = new BaseHazardEvent()
        event.setGeometry(testGeometry1)
        observedHazardEvent = new ObservedHazardEvent(event, sessionEventManager)
    }

    /**
     * Cleans up any resources used by this test.
     */
    def cleanup() {
    }

    def "An event's geometry cannot be undone or redone when first initialized"() {
        when: "the observed hazard event is first initialized"
        then: "it's geometry cannot be undone"
        observedHazardEvent.isUndoable() == false
        and:  "it's geometry cannot be redone"
        observedHazardEvent.isRedoable() == false
        and: "the event's geometry is the original one"
        observedHazardEvent.getGeometry() == testGeometry1
    }

    def "When first modified, an event's geometry can be undone but not redone"(){
        when: "the event's geometry is changed"
        observedHazardEvent.setGeometry(testGeometry2)
        then: "the event's geometry can undone"
        observedHazardEvent.isUndoable() == true
        and:  "the event's geometry cannot be redone"
        observedHazardEvent.isRedoable() == false
        and:  "the event contains the modified geometry"
        observedHazardEvent.getGeometry() == testGeometry2
    }

    def "When an event's geometry is undone, then it contains the orginal geometry"() {
        when: "the event's geometry is changed"
        observedHazardEvent.setGeometry(testGeometry2)
        and: "the event's geometry change is undone"
        observedHazardEvent.undo()
        then: "the event contains the original geometry"
        observedHazardEvent.getGeometry() == testGeometry1
        and: "the event's geometry is not undoable"
        observedHazardEvent.isUndoable() == false
        and: "the event's geometry is redoable"
        observedHazardEvent.isRedoable() == true
    }

    def "multiple undo operations followed by the same number of redo operations should result in the same event geometry"(){
        when: "the event's geometry is changed"
        observedHazardEvent.setGeometry(testGeometry2)
        observedHazardEvent.setGeometry(testGeometry3)
        and: "the geometry changes are undone"
        observedHazardEvent.undo()
        observedHazardEvent.undo()
        and: "the geometry changes are redone"
        observedHazardEvent.redo()
        observedHazardEvent.redo()
        then: "then event should contain the correct geometry"
        observedHazardEvent.getGeometry() == testGeometry3
    }

    def "test clearing an event's undo/redo history"() {
        when: "the event's geometry is changed"
        observedHazardEvent.setGeometry(testGeometry2)
        observedHazardEvent.setGeometry(testGeometry3)
        and: "the event's change history is cleared"
        observedHazardEvent.clearUndoRedo()
        then: "the event should contain the last geometry change"
        observedHazardEvent.getGeometry() == testGeometry3
        and: "the event should not be undoable"
        observedHazardEvent.isUndoable() == false
        and: "the event should not be redoable"
        observedHazardEvent.isRedoable() == false
    }

    /**
     * Builds a polygon geometry.
     * 
     * @param An array of coordinates. They must form
     *        a closed line.
     * @return
     *       A polygon.
     */
    private Geometry buildTestGeometry(Coordinate[] coordinates) {

        LinearRing ls = geometryFactory.createLinearRing(coordinates);
        return geometryFactory.createPolygon(ls, null)
    }

    /**
     * Creates a mocked session event manager.
     * @param
     * @return
     */
    private mockSessionEventManager() {
        sessionEventManager = mock(SessionEventManager.class)
    }
}
