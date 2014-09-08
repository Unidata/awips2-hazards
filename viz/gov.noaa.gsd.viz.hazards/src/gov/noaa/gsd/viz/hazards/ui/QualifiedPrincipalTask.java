/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import gov.noaa.gsd.viz.mvp.widgets.IQualifiedWidget;

/**
 * Description: Base class for tasks that are to be run in the main UI thread
 * using a qualified principal. The principal is set after construction using
 * {@link #setPrincipal(IQualifiedWidget)}, but must be set before the task is
 * executed. Subclasses must access the principal when attempting to execute
 * their tasks via {@link #getPrincipal()}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class QualifiedPrincipalTask<Q, I, W extends IQualifiedWidget<Q, I>> {

    // Private Variables

    /**
     * Principal that is to run the task.
     */
    private W principal;

    // Public Methods

    /**
     * Set the principal to that specified.
     * 
     * @param principal
     *            Principal to be used.
     */
    public final void setPrincipal(W principal) {
        this.principal = principal;
    }

    // Protected Methods

    /**
     * Get the principal.
     * 
     * @return Principal.
     */
    protected final W getPrincipal() {
        return principal;
    }
}
