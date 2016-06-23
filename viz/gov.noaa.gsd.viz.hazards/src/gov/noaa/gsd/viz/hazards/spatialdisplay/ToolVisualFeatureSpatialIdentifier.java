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

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;

/**
 * Description: Encapsulation of an identifier for a {@link SpatialEntity}
 * generated from a {@link VisualFeature} that was in turn created by a tool to
 * gather spatial information from the user prior to the tool's execution.
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
public class ToolVisualFeatureSpatialIdentifier extends
        VisualFeatureSpatialIdentifier {

    // Private Variables

    /**
     * Tool type.
     */
    private final ToolType toolType;

    /**
     * Tool identifier.
     */
    private final String toolIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param toolType
     *            Type of the tool.
     * @param toolIdentifier
     *            Hazard event identifier.
     * @param visualFeatureIdentifier
     *            Visual feature identifier.
     */
    public ToolVisualFeatureSpatialIdentifier(ToolType toolType,
            String toolIdentifier, String visualFeatureIdentifier) {
        super(visualFeatureIdentifier);
        this.toolType = toolType;
        this.toolIdentifier = toolIdentifier;
    }

    // Public Methods

    /**
     * Get the tool type.
     * 
     * @return Tool type.
     */
    public ToolType getToolType() {
        return toolType;
    }

    /**
     * Get the tool identifier.
     * 
     * @return Tool identifier.
     */
    public String getToolIdentifier() {
        return toolIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ToolVisualFeatureSpatialIdentifier == false) {
            return false;
        }
        ToolVisualFeatureSpatialIdentifier otherIdentifier = (ToolVisualFeatureSpatialIdentifier) other;
        return (super.equals(other)
                && ((toolType == otherIdentifier.toolType) || ((toolType != null) && toolType
                        .equals(otherIdentifier.toolType))) && ((toolIdentifier == otherIdentifier.toolIdentifier) || ((toolIdentifier != null) && toolIdentifier
                .equals(otherIdentifier.toolIdentifier))));
    }

    @Override
    public int hashCode() {
        return (int) (((toolType == null ? 0L : (long) toolType.hashCode())
                + (toolIdentifier == null ? 0L : (long) toolIdentifier
                        .hashCode()) + super.hashCode()) % Integer.MAX_VALUE);
    }
}
