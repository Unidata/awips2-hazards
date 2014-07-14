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

import org.eclipse.swt.widgets.Widget;

/**
 * A command invoker delegate, used to provide thread-safe access to command
 * invokers that are {@link Widget SWT widgets}, or are composed of SWT widgets.
 * The generic parameter <code>I</code> provides the type of command invoker
 * identifier to be used, and <code>W</code> is the type of principal this
 * delegate is to represent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class CommandInvokerDelegate<I, W extends ICommandInvoker<I>> extends
        WidgetDelegate<I, W> implements ICommandInvoker<I> {

    // Private Classes

    /**
     * Command invocation handler delegate, used to provide thread-safe access
     * to command invocation handlers from {@link ICommandInvoker} instances
     * that run within the main SWT UI thread.
     */
    private class CommandInvocationHandlerDelegate implements
            ICommandInvocationHandler<I> {

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
         */
        public CommandInvocationHandlerDelegate(
                ICommandInvocationHandler<I> principal) {
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

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param helper
     *            Widget delegate helper.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public CommandInvokerDelegate(IWidgetDelegateHelper<I, W> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setCommandInvocationHandler(final I identifier,
            final ICommandInvocationHandler<I> handler) {

        /*
         * Since handlers must be registered with the current view at all times,
         * persist the registration task so that it is executed each time the
         * view is (re)created.
         */
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().setCommandInvocationHandler(identifier,
                        new CommandInvocationHandlerDelegate(handler));
            }
        }, true);
    }
}
