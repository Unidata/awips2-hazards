/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.entities;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;

import gov.noaa.gsd.common.visuals.SpatialEntity;

/**
 * Description: Identifier for {@link SpatialEntity} instance generated to
 * represent {@link IHazardEventView} objects in the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2016   19537    Chris.Golden Initial creation.
 * Aug 28, 2016   19537    Chris.Golden Added toString() method.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardEventEntityIdentifier
        implements IHazardEventEntityIdentifier {

    // Private Variables

    /**
     * Hazard event identifier.
     */
    private final String eventIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventIdentifier
     *            Hazard event identifier.
     */
    public HazardEventEntityIdentifier(String eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }

    // Public Methods

    /**
     * Get the event identifier.
     * 
     * @return Event identifier.
     */
    @Override
    public String getEventIdentifier() {
        return eventIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof HazardEventEntityIdentifier == false) {
            return false;
        }
        HazardEventEntityIdentifier otherIdentifier = (HazardEventEntityIdentifier) other;
        return (((eventIdentifier == otherIdentifier.eventIdentifier)
                || ((eventIdentifier != null) && eventIdentifier
                        .equals(otherIdentifier.eventIdentifier))));
    }

    @Override
    public int hashCode() {
        return (int) ((eventIdentifier == null ? 0L
                : (long) eventIdentifier.hashCode()) % Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return getEventIdentifier();
    }
}
