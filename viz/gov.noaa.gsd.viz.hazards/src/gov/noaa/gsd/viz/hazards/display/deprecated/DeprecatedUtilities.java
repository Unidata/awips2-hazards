/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.deprecated;

import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedEvent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

/**
 * Description: Waypoint during refactoring.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 08, 2014  2182      daniel.s.schaffer@noaa.gov      Initial creation
 * Feb 03, 2014  2155       Chris.Golden      Fixed bug that caused floating-
 *                                            point values to be interpreted
 *                                            as long integers when doing
 *                                            conversions to/from JSON.
 * Nov 18, 2014  4124       Chris.Golden      Adapted to new time manager.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@Deprecated
public class DeprecatedUtilities {

    public static DeprecatedEvent[] eventsAsJSONEvents(
            Collection<? extends IHazardEvent> events) {
        DeprecatedEvent[] result = new DeprecatedEvent[events.size()];
        Iterator<? extends IHazardEvent> it = events.iterator();
        for (int i = 0; i < result.length; i += 1) {
            IHazardEvent hevent = it.next();
            result[i] = new DeprecatedEvent(hevent);
        }
        return result;
    }

    /**
     * Legacy code that adapts {@link IHazardEvent}s for display.
     * 
     * TODO For events that have been replaced (i.e. FL.A to FL.W), this method
     * returns the old type, not the new. The full type and phen/sig/subtype are
     * all correct. Apparently the type is not used anywhere so nothing bad has
     * happened so far. Solve this when we refactor this method away.
     */
    @Deprecated
    public static void adaptJSONEvent(DeprecatedEvent[] jsonEvents,
            Collection<? extends IHazardEvent> events,
            ISessionConfigurationManager configManager,
            ISessionTimeManager timeManager) {

        Iterator<? extends IHazardEvent> it = events.iterator();
        for (int i = 0; i < jsonEvents.length; i++) {

            /*
             * This logic adds hazard color information to an event dict.
             * 
             * This block of code cannot be removed until all Hazard Services
             * views have been converted from using event Dicts to using event
             * objects.
             */
            IHazardEvent hevent = it.next();
            Color color = configManager.getColor(hevent);
            String fillColor = (int) (color.getRed() * 255) + " "
                    + (int) (color.getGreen() * 255) + " "
                    + (int) (color.getBlue() * 255);
            jsonEvents[i].setColor(fillColor);

            String type = jsonEvents[i].getType();
            if (type != null) {
                String headline = configManager.getHeadline(hevent);
                jsonEvents[i].setHeadline(headline);
                jsonEvents[i].setFullType(type + " (" + headline + ")");
            } else {
                /*
                 * Support the case where the type has been reset to empty, such
                 * as when switching to a new hazard category.
                 */
                jsonEvents[i].setType("");
                jsonEvents[i].setFullType("");
                jsonEvents[i].setHeadline("");
                jsonEvents[i].setPhen("");
                jsonEvents[i].setSig("");
                jsonEvents[i].setSubType("");
            }
            TimeRange hetr = new TimeRange(hevent.getStartTime(),
                    hevent.getEndTime());
            Date time = new Date(timeManager.getLowerSelectedTimeInMillis());
            if (time != null && !hetr.contains(time)) {
                jsonEvents[i]
                        .setShapes(new gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedShape[0]);
            }
        }
    }

    /**
     * This method is used to bring into the HID the arbitrary attributes that
     * can be associated with different {@link IHazardEvent}s depending on the
     * event type. Since we have to convert back and forth between JSON and
     * POJO's; this method isn't quite so bad; it's somewhat general. But what's
     * really messy about all this is {@link DeprecatedEvent} It's encoding only
     * the known (non-arbitrary) attributes. A preferred approach would be to
     * eliminate {@link DeprecatedEvent} and do all the conversion from
     * {@link IHazardEvent} to JSON in this method. But that would also require
     * separating out the other logic in {@link DeprecatedEvent} that's
     * unrelated to POJO/JSON conversion.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static String eventsAsNodeJSON(
            Collection<? extends IHazardEvent> events, DeprecatedEvent[] events2) {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        JSONConverter jsonConverter = new JSONConverter();
        ArrayNode jevents = jsonObjectMapper.createArrayNode();

        Iterator<? extends IHazardEvent> it2 = events.iterator();

        for (int ii = 0; ii < events2.length; ii++) {

            // HID needs all the extra attributes.
            JsonNode jobj = jsonConverter.fromJson(
                    jsonConverter.toJson(events2[ii]), JsonNode.class);
            ObjectNode node = (ObjectNode) jobj;
            IHazardEvent hevent = it2.next();
            for (Entry<String, Serializable> entry : hevent
                    .getHazardAttributes().entrySet()) {
                if (entry.getValue() instanceof String) {
                    node.put(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    node.put(entry.getKey(), (Boolean) entry.getValue());
                } else if (entry.getValue() instanceof Date) {
                    node.put(entry.getKey(),
                            ((Date) entry.getValue()).getTime());
                } else if (entry.getValue() instanceof String[]) {
                    ArrayNode tmpArray = jsonObjectMapper.createArrayNode();
                    for (Object obj : (String[]) entry.getValue()) {
                        tmpArray.add(obj.toString());
                    }
                    node.put(entry.getKey(), tmpArray);
                } else if (entry.getValue() instanceof Integer) {
                    node.put(entry.getKey(), (Integer) entry.getValue());
                } else if (entry.getValue() instanceof Long) {
                    node.put(entry.getKey(), (Long) entry.getValue());
                } else if (entry.getValue() instanceof Float) {
                    node.put(entry.getKey(),
                            ((Float) entry.getValue()).doubleValue());
                } else if (entry.getValue() instanceof Double) {
                    node.put(entry.getKey(), (Double) entry.getValue());
                } else if (entry.getValue() instanceof List) {
                    ArrayNode tmpArray = jsonObjectMapper.createArrayNode();
                    for (Object obj : (List<Object>) entry.getValue()) {
                        tmpArray.add(obj.toString());
                    }
                    node.put(entry.getKey(), tmpArray);
                }

            }
            jevents.add(jobj);
        }
        return jsonConverter.toJson(jevents);

    }

}
