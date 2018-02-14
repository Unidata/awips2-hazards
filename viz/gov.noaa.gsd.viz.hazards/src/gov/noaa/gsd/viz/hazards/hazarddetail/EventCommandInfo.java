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

/**
 * Description: Simple encapsulation of an event identifier and an attribute
 * identifier, used to specify a command to be run for an event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * Sep 16, 2014    4753    Chris.Golden Changed name, and added mutable properties.
 * Feb 13, 2018   44514    Chris.Golden Removed event-modifying script code, as such
 *                                      scripts are not to be used. Also changed the
 *                                      class name to more accurately reflect its
 *                                      raison d'etre.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EventCommandInfo {

    // Private Variables

    /**
     * Event identifier.
     */
    private final String eventIdentifier;

    /**
     * Attribute identifier.
     */
    private final String attributeIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param attributeIdentifier
     *            Attribute identifier.
     */
    public EventCommandInfo(String eventIdentifier,
            String attributeIdentifier) {
        this.eventIdentifier = eventIdentifier;
        this.attributeIdentifier = attributeIdentifier;
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
     * Get the attribute identifier.
     * 
     * @return Attribute identifier.
     */
    public final String getAttributeIdentifier() {
        return attributeIdentifier;
    }
}
