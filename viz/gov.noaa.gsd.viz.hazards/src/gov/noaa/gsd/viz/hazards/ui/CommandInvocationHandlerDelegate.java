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
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

/**
 * Description: Command invocation handler delegate, used to provide thread-safe
 * access to command invocation handlers from {@link ICommandInvoker} instances
 * or elsewhere that run within the main SWT UI thread. The generic parameter
 * <code>I</code> provides the type of command invoker identifier to be used.
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
public class CommandInvocationHandlerDelegate<I> extends HandlerDelegate
        implements ICommandInvocationHandler<I> {

    // Private Constants

    /**
     * Principal for which this is acting as a delegate.
     */
    private final ICommandInvocationHandler<I> principal;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Principal for which to act as a delegate.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public CommandInvocationHandlerDelegate(
            ICommandInvocationHandler<I> principal,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(handlerScheduler);
        this.principal = principal;
    }

    // Public Methods

    @Override
    public void commandInvoked(final I identifier) {
        getHandlerInvocationScheduler().schedule(new Runnable() {

            @Override
            public void run() {
                principal.commandInvoked(identifier);
            }
        });
    }
}
