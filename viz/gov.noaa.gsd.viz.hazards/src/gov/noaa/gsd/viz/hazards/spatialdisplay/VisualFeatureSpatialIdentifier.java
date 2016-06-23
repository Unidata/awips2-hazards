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
 * Description: Base class for encapsulations of identifiers for
 * {@link SpatialEntity} instances generated from {@link VisualFeature} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 15, 2016   15676    Chris.Golden Initial creation.
 * Mar 24, 2016   15676    Chris.Golden Added getXXX() methods.
 * Jun 10, 2016   19537    Chris.Golden Combined base and selected visual feature
 *                                      lists for each hazard event into one,
 *                                      replaced by visibility constraints
 *                                      based upon selection state to individual
 *                                      visual features.
 * Jun 15, 2016   19537    Chris.Golden Turned into abstract base class.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class VisualFeatureSpatialIdentifier {

    // Private Variables

    /**
     * Visual feature identifier.
     */
    private final String visualFeatureIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param visualFeatureIdentifier
     *            Visual feature identifier.
     */
    public VisualFeatureSpatialIdentifier(String visualFeatureIdentifier) {
        this.visualFeatureIdentifier = visualFeatureIdentifier;
    }

    // Public Methods

    /**
     * Get the visual feature identifier.
     * 
     * @return Visual feature identifier.
     */
    public String getVisualFeatureIdentifier() {
        return visualFeatureIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VisualFeatureSpatialIdentifier == false) {
            return false;
        }
        VisualFeatureSpatialIdentifier otherIdentifier = (VisualFeatureSpatialIdentifier) other;
        return ((visualFeatureIdentifier == otherIdentifier.visualFeatureIdentifier) || ((visualFeatureIdentifier != null) && visualFeatureIdentifier
                .equals(otherIdentifier.visualFeatureIdentifier)));
    }

    @Override
    public int hashCode() {
        return (int) ((visualFeatureIdentifier == null ? 0L
                : (long) visualFeatureIdentifier.hashCode()) % Integer.MAX_VALUE);
    }
}
