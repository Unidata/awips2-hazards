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

import gov.noaa.gsd.common.visuals.SpatialEntity;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Identifier for a {@link SpatialEntity} generated to represent
 * the hatching of a {@link IHazardEvent} in the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 15, 2016   19537    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardEventHatchingEntityIdentifier extends
        HazardEventEntityIdentifier {

    // Private Variables

    /**
     * UGC identifier.
     */
    private final String ugcIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventIdentifier
     *            Hazard event identifier.
     * @param ugcIdentifier
     *            UGC identifier.
     */
    public HazardEventHatchingEntityIdentifier(String eventIdentifier,
            String ugcIdentifier) {
        super(eventIdentifier);
        this.ugcIdentifier = ugcIdentifier;
    }

    // Public Methods

    /**
     * Get the UGC identifier.
     * 
     * @return UGC identifier
     */
    public String getUgcIdentifier() {
        return ugcIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if ((super.equals(other) == false)
                || (other instanceof HazardEventHatchingEntityIdentifier == false)) {
            return false;
        }
        HazardEventHatchingEntityIdentifier otherIdentifier = (HazardEventHatchingEntityIdentifier) other;
        return ((ugcIdentifier == otherIdentifier.ugcIdentifier) || ((ugcIdentifier != null) && ugcIdentifier
                .equals(otherIdentifier.ugcIdentifier)));
    }

    @Override
    public int hashCode() {
        return (int) (((ugcIdentifier == null ? 0L : (long) ugcIdentifier
                .hashCode()) + super.hashCode()) % Integer.MAX_VALUE);
    }
}
