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

import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolExecutionIdentifier;

import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.VisualFeature;

/**
 * Description: Identifier for a {@link SpatialEntity} generated from a
 * {@link VisualFeature} that was in turn created by a tool to gather
 * information in the spatial display from the user prior to the tool's
 * execution.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 15, 2016   19537    Chris.Golden Initial creation.
 * Aug 28, 2016   19537    Chris.Golden Added toString() method.
 * Sep 27, 2017   38072    Chris.Golden Removed unneeded tool identifier.
 * May 22, 2018    3782    Chris.Golden Changed to use tool execution identifier.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolVisualFeatureEntityIdentifier
        implements IVisualFeatureEntityIdentifier {

    // Private Variables

    /**
     * Tool execution identifier.
     */
    private final ToolExecutionIdentifier toolIdentifier;

    /**
     * Visual feature identifier.
     */
    private final String visualFeatureIdentifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param toolIdentifier
     *            Tool execution identifier.
     * @param visualFeatureIdentifier
     *            Visual feature identifier.
     */
    public ToolVisualFeatureEntityIdentifier(
            ToolExecutionIdentifier toolIdentifier,
            String visualFeatureIdentifier) {
        this.toolIdentifier = toolIdentifier;
        this.visualFeatureIdentifier = visualFeatureIdentifier;
    }

    // Public Methods

    /**
     * Get the tool execution identifier.
     * 
     * @return Tool execution identifier.
     */
    public ToolExecutionIdentifier getToolIdentifier() {
        return toolIdentifier;
    }

    @Override
    public String getVisualFeatureIdentifier() {
        return visualFeatureIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ToolVisualFeatureEntityIdentifier == false) {
            return false;
        }
        ToolVisualFeatureEntityIdentifier otherIdentifier = (ToolVisualFeatureEntityIdentifier) other;
        return (((toolIdentifier == otherIdentifier.toolIdentifier)
                || ((toolIdentifier != null) && toolIdentifier
                        .equals(otherIdentifier.toolIdentifier)))
                && ((visualFeatureIdentifier == otherIdentifier.visualFeatureIdentifier)
                        || ((visualFeatureIdentifier != null)
                                && visualFeatureIdentifier.equals(
                                        otherIdentifier.visualFeatureIdentifier))));
    }

    @Override
    public int hashCode() {
        return (int) (((toolIdentifier == null ? 0L
                : (long) toolIdentifier.hashCode())
                + (visualFeatureIdentifier == null ? 0L
                        : (long) visualFeatureIdentifier.hashCode()))
                % Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return getToolIdentifier() + " (visual feature = \""
                + getVisualFeatureIdentifier() + "\")";
    }
}
