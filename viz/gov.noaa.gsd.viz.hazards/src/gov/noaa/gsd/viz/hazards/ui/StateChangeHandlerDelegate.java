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

/**
 * Description: State change handler delegate, used to provide thread-safe
 * access to state change handlers from {@link IStateChanger} instances or
 * elsewhere that run within the main SWT UI thread. The generic parameter
 * <code>I</code> provides the type of state changer identifier to be used, and
 * <code>S</code> provides the type of state.
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
public class StateChangeHandlerDelegate<I, S> extends HandlerDelegate implements
        IStateChangeHandler<I, S> {

    // Private Constants

    /**
     * Principal for which this is acting as a delegate.
     */
    private final IStateChangeHandler<I, S> principal;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Principal for which to act as a delegate.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public StateChangeHandlerDelegate(IStateChangeHandler<I, S> principal,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(handlerScheduler);
        this.principal = principal;
    }

    // Public Methods

    @Override
    public void stateChanged(final I identifier, final S value) {
        getHandlerInvocationScheduler().schedule(new Runnable() {

            @Override
            public void run() {
                principal.stateChanged(identifier, value);
            }
        });
    }

    @Override
    public void statesChanged(final Map<I, S> valuesForIdentifiers) {
        getHandlerInvocationScheduler().schedule(new Runnable() {

            @Override
            public void run() {
                principal.statesChanged(valuesForIdentifiers);
            }
        });
    }
}