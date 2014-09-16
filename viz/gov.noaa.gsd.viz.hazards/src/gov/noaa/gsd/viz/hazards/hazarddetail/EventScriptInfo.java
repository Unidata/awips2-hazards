/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import java.util.Map;

/**
 * Description: Simple encapsulation of an event identifier, script identifier,
 * and megawidget mutable properties, used to specify a script command to be run
 * for an event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * Sep 16, 2014    4753    Chris.Golden Changed name, and added mutable properties.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EventScriptInfo {

    // Private Variables

    /**
     * Event identifier.
     */
    private final String eventIdentifier;

    /**
     * Detail identifier.
     */
    private final String detailIdentifier;

    /**
     * Mutable properties of all metadata megawidgets for the event.
     */
    private final Map<String, Map<String, Object>> mutableProperties;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param detailIdentifier
     *            Detail identifier.
     * @param mutableProperties
     *            Mutable properties of all metadata megawidgets for the event.
     */
    public EventScriptInfo(String eventIdentifier, String detailIdentifier,
            Map<String, Map<String, Object>> mutableProperties) {
        this.eventIdentifier = eventIdentifier;
        this.detailIdentifier = detailIdentifier;
        this.mutableProperties = mutableProperties;
    }

    // Public Methods

    /**
     * Get the event identifier.
     * 
     * @return Event identifier.
     */
    public final String getEventIdentifier() {
        return eventIdentifier;
    }

    /**
     * Get the detail identifier.
     * 
     * @return detail identifier.
     */
    public final String getDetailIdentifier() {
        return detailIdentifier;
    }

    /**
     * Get the mutable properties of all metadata megawidgets for the event.
     * 
     * @return Mutable properties of all metadata megawidgets for the event.
     */
    public final Map<String, Map<String, Object>> getMutableProperties() {
        return mutableProperties;
    }
}
