/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.jsonutilities;

import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

/**
 * A convenience class for creating an event dictionary and converting this to a
 * JSON String.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 2012                Bryon.Lawrence    Initial creation
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public final class EventDict extends TreeMap<String, Object> {
    transient private static final long serialVersionUID = 1L;

    /**
     * 
     */
    transient private List<Shape> shapes;

    /**
     * Default constructor
     */
    public EventDict() {
        shapes = Lists.newArrayList();
        put(HazardConstants.HAZARD_EVENT_SHAPES, shapes);
    }

    /**
     * Creates an event dict initialized with the provided arguments.
     * 
     * @param eventID
     *            The event id
     * @param state
     *            The event state (e.g. proposed)
     * @param type
     *            The event type (e.g. FL.W)
     * @param startTime
     *            The event start time in milliseconds
     * @param endTime
     *            The event end time in milliseconds
     * @param creationTime
     *            The event creation time in milliseconds
     */
    public EventDict(String eventID, String state, String type, long startTime,
            long endTime, long creationTime) {

        this();

        put(HazardConstants.HAZARD_EVENT_IDENTIFIER, eventID);
        put(HazardConstants.HAZARD_EVENT_STATE, state);
        put(HazardConstants.HAZARD_EVENT_TYPE, type);
        put(HazardConstants.HAZARD_EVENT_START_TIME, new Long(startTime));
        put(HazardConstants.HAZARD_EVENT_END_TIME, new Long(endTime));
        put("creationTime", new Long(creationTime));

    }

    /**
     * Adds a shape to this event.
     * 
     * @param shape
     *            shape to add to this event.
     * @return
     */
    public void addShape(Shape shape) {
        shapes.add(shape);
    }

    /**
     * @return A JSON respresentation of this event object.
     */
    public String toJSONString() {
        Gson gson = JSONUtilities.createGsonInterpreter();
        return gson.toJson(this);
    }

}
