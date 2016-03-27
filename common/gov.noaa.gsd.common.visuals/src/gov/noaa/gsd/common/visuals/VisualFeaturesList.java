/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

import java.util.ArrayList;

/**
 * Description: List of visual features. Each of the elements must have an
 * identifier that is unique within the list.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 16, 2016   15676    Chris.Golden Initial creation.
 * Mar 26, 2016   15676    Chris.Golden Added convenience lookup methods.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisualFeaturesList extends ArrayList<VisualFeature> {

    // Private Static Constants

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = -2655616243967777587L;

    // Public Methods

    /**
     * Get a copy of the visual feature with the specified identifier.
     * 
     * @param identifier
     *            Identifier of the desired visual feature.
     * @return Visual feature, or <code>null</code> if none is found with the
     *         specified identifier.
     */
    public VisualFeature getByIdentifier(String identifier) {
        for (VisualFeature visualFeature : this) {
            if (visualFeature.getIdentifier().equals(identifier)) {
                return new VisualFeature(visualFeature);
            }
        }
        return null;
    }

    /**
     * Get the index of the the visual feature with the specified identifier.
     * 
     * @param identifier
     *            Identifier of the desired visual feature.
     * @return Index of the visual feature, or <code>-1</code> if none is found
     *         with the specified identifier.
     */
    public int indexOfByIdentifier(String identifier) {
        for (int j = 0; j < size(); j++) {
            if (get(j).getIdentifier().equals(identifier)) {
                return j;
            }
        }
        return -1;
    }
}
