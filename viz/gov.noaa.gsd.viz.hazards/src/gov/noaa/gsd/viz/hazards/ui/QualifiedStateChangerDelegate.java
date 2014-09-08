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
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Map;

import org.eclipse.swt.widgets.Widget;

/**
 * A qualified state changer delegate, used to provide thread-safe access to
 * state changers that are {@link Widget SWT widgets}, or are composed of SWT
 * widgets. The generic parameter <code>Q</code> provides the type of state
 * changer qualifier to be used, <code>I</code> provides the type of state
 * changer identifier to be used, <code>S</code> provides the type of state, and
 * <code>W</code> is the type of principal this delegate is to represent.
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
public class QualifiedStateChangerDelegate<Q, I, S, W extends IQualifiedStateChanger<Q, I, S>>
        extends QualifiedWidgetDelegate<Q, I, W> implements
        IQualifiedStateChanger<Q, I, S> {

    // Private Classes

    /**
     * State change handler delegate, used to provide thread-safe access to
     * state change handlers from {@link IStateChanger} instances that run
     * within the main SWT UI thread.
     */
    private class QualifiedStateChangeHandlerDelegate implements
            IQualifiedStateChangeHandler<Q, I, S> {

        // Private Constants

        /**
         * Principal for which this is acting as a delegate.
         */
        private final IQualifiedStateChangeHandler<Q, I, S> principal;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param principal
         *            Principal for which to act as a delegate.
         */
        public QualifiedStateChangeHandlerDelegate(
                IQualifiedStateChangeHandler<Q, I, S> principal) {
            this.principal = principal;
        }

        // Public Methods

        @Override
        public void stateChanged(final Q qualifier, final I identifier,
                final S value) {
            getHandlerInvocationScheduler().schedule(new Runnable() {

                @Override
                public void run() {
                    principal.stateChanged(qualifier, identifier, value);
                }
            });
        }

        @Override
        public void statesChanged(final Q qualifier,
                final Map<I, S> valuesForIdentifiers) {
            getHandlerInvocationScheduler().schedule(new Runnable() {

                @Override
                public void run() {
                    principal.statesChanged(qualifier, valuesForIdentifiers);
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
    public QualifiedStateChangerDelegate(
            IQualifiedWidgetDelegateHelper<Q, I, W> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setEditable(final Q qualifier, final I identifier,
            final boolean editable) {
        runOrScheduleTask(new QualifiedPrincipalRunnableTask<Q, I, W>() {

            @Override
            public void run() {
                getPrincipal().setEditable(qualifier, identifier, editable);
            }
        });
    }

    @Override
    public S getState(final Q qualifier, final I identifier) {
        return callTask(new QualifiedPrincipalCallableTask<Q, I, W, S>() {

            @Override
            public S call() throws Exception {
                return getPrincipal().getState(qualifier, identifier);
            }
        });
    }

    @Override
    public void setState(final Q qualifier, final I identifier, final S value) {
        runOrScheduleTask(new QualifiedPrincipalRunnableTask<Q, I, W>() {

            @Override
            public void run() {
                getPrincipal().setState(qualifier, identifier, value);
            }
        });
    }

    @Override
    public void setStates(final Q qualifier,
            final Map<I, S> valuesForIdentifiers) {
        runOrScheduleTask(new QualifiedPrincipalRunnableTask<Q, I, W>() {

            @Override
            public void run() {
                getPrincipal().setStates(qualifier, valuesForIdentifiers);
            }
        });
    }

    @Override
    public void setStateChangeHandler(
            final IQualifiedStateChangeHandler<Q, I, S> handler) {

        /*
         * Since handlers must be registered with the current view at all times,
         * persist the registration task so that it is executed each time the
         * view is (re)created.
         */
        runOrScheduleTask(new QualifiedPrincipalRunnableTask<Q, I, W>() {

            @Override
            public void run() {
                getPrincipal().setStateChangeHandler(
                        new QualifiedStateChangeHandlerDelegate(handler));
            }
        }, true);
    }
}
