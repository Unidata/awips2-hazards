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

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IWidget;

/**
 * Description: Handler delegate, an abstract base class used to provide
 * thread-safe access to handlers from {@link IWidget} instances or elsewhere
 * that run within the main SWT UI thread.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 03, 2014    4042    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class HandlerDelegate {

    // Private Constants

    /**
     * Handler invocation scheduler.
     */
    private final IRunnableAsynchronousScheduler handlerScheduler;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public HandlerDelegate(IRunnableAsynchronousScheduler handlerScheduler) {
        this.handlerScheduler = handlerScheduler;
    }

    // Protected Methods

    /**
     * Get the runnable asynchronous scheduler that is to be used to schedule
     * invocations of the principal's handler (either
     * {@link ICommandInvocationHandler} or {@link IStateChangeHandler}).
     * 
     * @return Runnable asynchronous scheduler.
     */
    protected final IRunnableAsynchronousScheduler getHandlerInvocationScheduler() {
        return handlerScheduler;
    }
}
