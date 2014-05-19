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

import gov.noaa.gsd.viz.mvp.widgets.IWidget;

/**
 * Description: Base class for tasks that are to be run in the main UI thread
 * using a principal. The principal is set after construction using
 * {@link #setPrincipal(IWidget)}, but must be set before the task is executed.
 * Subclasses must access the principal when attempting to execute their tasks
 * via {@link #getPrincipal()}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 10, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class PrincipalTask<I, W extends IWidget<I>> {

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
