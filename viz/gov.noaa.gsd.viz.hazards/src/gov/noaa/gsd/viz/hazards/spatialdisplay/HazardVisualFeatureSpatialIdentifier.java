/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.VisualFeature;

/**
 * Description: Encapsulation of an identifier for a {@link SpatialEntity}
 * generated from a {@link VisualFeature} that is part or all of the
 * representation of a hazard event on the spatial display.
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
public class HazardVisualFeatureSpatialIdentifier extends
        VisualFeatureSpatialIdentifier {

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
     * @param visualFeatureIdentifier
     *            Visual feature identifier.
     */
    public HazardVisualFeatureSpatialIdentifier(String eventIdentifier,
            String visualFeatureIdentifier) {
        super(visualFeatureIdentifier);
        this.eventIdentifier = eventIdentifier;
    }

    // Public Methods

    /**
     * Get the event identifier.
     * 
     * @return Event identifier.
     */
    public String getEventIdentifier() {
        return eventIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof HazardVisualFeatureSpatialIdentifier == false) {
            return false;
        }
        HazardVisualFeatureSpatialIdentifier otherIdentifier = (HazardVisualFeatureSpatialIdentifier) other;
        return (super.equals(other) && ((eventIdentifier == otherIdentifier.eventIdentifier) || ((eventIdentifier != null) && eventIdentifier
                .equals(otherIdentifier.eventIdentifier))));
    }

    @Override
    public int hashCode() {
        return (int) (((eventIdentifier == null ? 0L : (long) eventIdentifier
                .hashCode()) + super.hashCode()) % Integer.MAX_VALUE);
    }
}
