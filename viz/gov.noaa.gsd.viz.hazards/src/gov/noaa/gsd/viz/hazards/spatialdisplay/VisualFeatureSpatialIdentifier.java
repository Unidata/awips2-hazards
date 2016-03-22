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
 * generated from a {@link VisualFeature}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 15, 2016   15676    Chris.Golden Initial creation.
 * Mar 24, 2016   15676    Chris.Golden Added getXXX() methods.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisualFeatureSpatialIdentifier {

    // Private Variables

    /**
     * Hazard event identifier.
     */
    private final String hazardEventIdentifier;

    /**
     * Visual feature identifier.
     */
    private final String visualFeatureIdentifier;

    /**
     * Flag indicating whether or not the visual feature is for display only
     * when selected.
     */
    private final boolean displayOnlyWhenSelected;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param hazardEventIdentifier
     *            Hazard event identifier.
     * @param visualFeatureIdentifier
     *            Visual feature identifier.
     * @param displayOnlyWhenSelected
     *            Flag indicating whether or not the visual feature is for
     *            display only when selected.
     */
    public VisualFeatureSpatialIdentifier(String hazardEventIdentifier,
            String visualFeatureIdentifier, boolean displayOnlyWhenSelected) {
        this.hazardEventIdentifier = hazardEventIdentifier;
        this.visualFeatureIdentifier = visualFeatureIdentifier;
        this.displayOnlyWhenSelected = displayOnlyWhenSelected;
    }

    // Public Methods

    /**
     * Get the hazard event identifier.
     * 
     * @return Hazard event identifier.
     */
    public String getHazardEventIdentifier() {
        return hazardEventIdentifier;
    }

    /**
     * Get the visual feature identifier.
     * 
     * @return Visual feature identifier.
     */
    public String getVisualFeatureIdentifier() {
        return visualFeatureIdentifier;
    }

    /**
     * Determine whether or not this visual feature is for display only when
     * selected.
     * 
     * @return True if for display only when selected, false otherwise.
     */
    public boolean isDisplayOnlyWhenSelected() {
        return displayOnlyWhenSelected;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VisualFeatureSpatialIdentifier == false) {
            return false;
        }
        VisualFeatureSpatialIdentifier otherIdentifier = (VisualFeatureSpatialIdentifier) other;
        return ((displayOnlyWhenSelected == otherIdentifier.displayOnlyWhenSelected)
                && ((hazardEventIdentifier == otherIdentifier.hazardEventIdentifier) || ((hazardEventIdentifier != null) && hazardEventIdentifier
                        .equals(otherIdentifier.hazardEventIdentifier))) && ((visualFeatureIdentifier == otherIdentifier.visualFeatureIdentifier) || ((visualFeatureIdentifier != null) && visualFeatureIdentifier
                .equals(otherIdentifier.visualFeatureIdentifier))));
    }

    @Override
    public int hashCode() {
        return (int) (((displayOnlyWhenSelected ? 1L : 0L)
                + (hazardEventIdentifier == null ? 0L
                        : (long) hazardEventIdentifier.hashCode()) + (visualFeatureIdentifier == null ? 0L
                    : (long) visualFeatureIdentifier.hashCode())) % Integer.MAX_VALUE);
    }
}
