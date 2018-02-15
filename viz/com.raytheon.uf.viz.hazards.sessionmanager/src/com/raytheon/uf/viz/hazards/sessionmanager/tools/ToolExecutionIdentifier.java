/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.tools;

import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;

/**
 * Identifier of a particular execution instance of a tool.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 22, 2018    3782    Chris.Golden  Initial creation.
 *
 * </pre>
 *
 * @author Chris.Golden
 */
public class ToolExecutionIdentifier {

    // Protected Variables

    /**
     * Identifier of the tool being executed.
     */
    protected final String toolIdentifier;

    /**
     * Context in which the execution is occurring.
     */
    protected final RecommenderExecutionContext context;

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param toolIdentifier
     *            Identifier of the tool being executed.
     * @param context
     *            Context in which the execution is occurring.
     */
    public ToolExecutionIdentifier(String toolIdentifier,
            RecommenderExecutionContext context) {
        if (toolIdentifier == null) {
            throw new IllegalArgumentException("null tool identifier");
        }
        this.toolIdentifier = toolIdentifier;
        if (context == null) {
            throw new IllegalArgumentException("null tool execution context");
        }
        this.context = context;
    }

    // Public Methods

    /**
     * Get the tool identifier.
     * 
     * @return Tool identifier.
     */
    public String getToolIdentifier() {
        return toolIdentifier;
    }

    /**
     * Get the context in which the execution is occurring.
     * 
     * @return Context in which the execution is occurring.
     */
    public RecommenderExecutionContext getContext() {
        return context;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ToolExecutionIdentifier == false) {
            return false;
        }
        ToolExecutionIdentifier otherIdentifier = (ToolExecutionIdentifier) other;
        return (toolIdentifier.equals(otherIdentifier.toolIdentifier)
                && context.equals(otherIdentifier.context));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + toolIdentifier.hashCode();
        result = prime * result + context.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return toolIdentifier + " (" + context + ")";
    }
}
