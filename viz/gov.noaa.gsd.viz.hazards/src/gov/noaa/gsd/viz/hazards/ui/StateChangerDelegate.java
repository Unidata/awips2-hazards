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
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Map;

import org.eclipse.swt.widgets.Widget;

/**
 * A state changer delegate, used to provide thread-safe access to state
 * changers that are {@link Widget SWT widgets}, or are composed of SWT widgets.
 * The generic parameter <code>I</code> provides the type of state changer
 * identifier to be used, <code>S</code> provides the type of state, and
 * <code>W</code> is the type of principal this delegate is to represent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * Jun 30, 2014    3512    Chris.Golden Simplified setting of handler by
 *                                      removing identifier; one handler must
 *                                      be used for all identifiers, since
 *                                      the handler may be called upon to
 *                                      receive notification of multiple
 *                                      simultaneous state changes.
 * Oct 03, 2014    4042    Chris.Golden Promoted handler inner class to public
 *                                      top-level class so that it may be used
 *                                      in cases where the presenter only needs
 *                                      to be notified by the view, and never
 *                                      needs to configure the widget.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class StateChangerDelegate<I, S, W extends IStateChanger<I, S>> extends
        WidgetDelegate<I, W> implements IStateChanger<I, S> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param helper
     *            Widget delegate helper.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public StateChangerDelegate(IWidgetDelegateHelper<I, W> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setEditable(final I identifier, final boolean editable) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().setEditable(identifier, editable);
            }
        });
    }

    @Override
    public S getState(final I identifier) {
        return callTask(new PrincipalCallableTask<I, W, S>() {

            @Override
            public S call() throws Exception {
                return getPrincipal().getState(identifier);
            }
        });
    }

    @Override
    public void setState(final I identifier, final S value) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().setState(identifier, value);
            }
        });
    }

    @Override
    public void setStates(final Map<I, S> valuesForIdentifiers) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().setStates(valuesForIdentifiers);
            }
        });
    }

    @Override
    public void setStateChangeHandler(final IStateChangeHandler<I, S> handler) {

        /*
         * Since handlers must be registered with the current view at all times,
         * persist the registration task so that it is executed each time the
         * view is (re)created.
         */
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().setStateChangeHandler(
                        new StateChangeHandlerDelegate<I, S>(handler,
                                getHandlerInvocationScheduler()));
            }
        }, true);
    }
}
