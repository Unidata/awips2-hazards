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
import gov.noaa.gsd.common.visuals.VisualFeature;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Identifier for a {@link SpatialEntity} generated from a
 * {@link VisualFeature} that is part of the representation of a
 * {@link IHazardEvent} in the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 15, 2016   19537    Chris.Golden Initial creation.
 * Jun 27, 2016   19537    Chris.Golden Revamped class hierarchy.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardEventVisualFeatureEntityIdentifier implements
        IHazardEventEntityIdentifier, IVisualFeatureEntityIdentifier {

    // Private Variables

    /**
     * Hazard event identifier.
     */
    private final String eventIdentifier;

    /**
     * Visual feature identifier.
     */
    private final String visualFeatureIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventIdentifier
     *            Hazard event identifier.
     * @param visualFeatureIdentifier
     *            Visual feature identifier.
     */
    public HazardEventVisualFeatureEntityIdentifier(String eventIdentifier,
            String visualFeatureIdentifier) {
        this.eventIdentifier = eventIdentifier;
        this.visualFeatureIdentifier = visualFeatureIdentifier;
    }

    // Public Methods

    @Override
    public String getEventIdentifier() {
        return eventIdentifier;
    }

    @Override
    public String getVisualFeatureIdentifier() {
        return visualFeatureIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof HazardEventVisualFeatureEntityIdentifier == false) {
            return false;
        }
        HazardEventVisualFeatureEntityIdentifier otherIdentifier = (HazardEventVisualFeatureEntityIdentifier) other;
        return (((eventIdentifier == otherIdentifier.eventIdentifier) || ((eventIdentifier != null) && eventIdentifier
                .equals(otherIdentifier.eventIdentifier))) && ((visualFeatureIdentifier == otherIdentifier.visualFeatureIdentifier) || ((visualFeatureIdentifier != null) && visualFeatureIdentifier
                .equals(otherIdentifier.visualFeatureIdentifier))));
    }

    @Override
    public int hashCode() {
        return (int) (((eventIdentifier == null ? 0L : (long) eventIdentifier
                .hashCode()) + (visualFeatureIdentifier == null ? 0L
                : (long) visualFeatureIdentifier.hashCode())) % Integer.MAX_VALUE);
    }
}
