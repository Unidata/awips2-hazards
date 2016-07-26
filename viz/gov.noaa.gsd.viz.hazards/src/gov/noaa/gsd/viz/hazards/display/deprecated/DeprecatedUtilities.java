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

import gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedEvent;

import java.util.Collection;
import java.util.Iterator;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
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
 * Dec 05, 2014  4124       Chris.Golden      Changed to work with newly
 *                                            parameterized config manager.
 * Jul 25, 2016 19537       Chris,Golden      Removed unused method.
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
            ISessionConfigurationManager<ObservedSettings> configManager,
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
        }
    }
}
