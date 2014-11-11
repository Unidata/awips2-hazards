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

import java.util.Map;

/**
 * Description: Qualified state change handler delegate, used to provide
 * thread-safe access to state change handlers from
 * {@link IQualifiedStateChanger} instances or elsewhere that run within the
 * main SWT UI thread. The generic parameter <code>Q</code> provides the type of
 * state changer qualifier to be used, <code>I</code> provides the type of state
 * changer identifier to be used, and <code>S</code> provides the type of state.
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
public class QualifiedStateChangeHandlerDelegate<Q, I, S> extends
        HandlerDelegate implements IQualifiedStateChangeHandler<Q, I, S> {

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
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public QualifiedStateChangeHandlerDelegate(
            IQualifiedStateChangeHandler<Q, I, S> principal,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(handlerScheduler);
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
