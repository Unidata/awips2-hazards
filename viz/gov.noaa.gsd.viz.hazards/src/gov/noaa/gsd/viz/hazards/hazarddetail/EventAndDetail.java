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
 * Description: Simple encapsulation of an event identifier and a detail
 * identifier, used to specify a particular part of an event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EventAndDetail {

    // Private Variables

    /**
     * Event identifier.
     */
    private final String eventIdentifier;

    /**
     * Detail identifier.
     */
    private final String detailIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param detailIdentifier
     *            Detail identifier.
     */
    public EventAndDetail(String eventIdentifier, String detailIdentifier) {
        this.eventIdentifier = eventIdentifier;
        this.detailIdentifier = detailIdentifier;
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
}
