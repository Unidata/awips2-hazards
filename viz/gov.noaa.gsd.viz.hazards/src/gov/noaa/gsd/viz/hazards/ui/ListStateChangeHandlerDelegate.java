/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;

/**
 * Description: List state change handler delegate, used to provide thread-safe
 * access to list state change handlers from {@link IListStateChanger} instances
 * or elsewhere that run within the main SWT UI thread. The generic parameter
 * <code>I</code> provides the type of list state changer identifier to be used,
 * and <code>E</code> provides the type of elements within the lists.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 22, 2016   19537    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ListStateChangeHandlerDelegate<I, E> extends HandlerDelegate
        implements IListStateChangeHandler<I, E> {

    // Private Constants

    /**
     * Principal for which this is acting as a delegate.
     */
    private final IListStateChangeHandler<I, E> principal;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Principal for which to act as a delegate.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public ListStateChangeHandlerDelegate(
            IListStateChangeHandler<I, E> principal,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(handlerScheduler);
        this.principal = principal;
    }

    // Public Methods

    @Override
    public void listElementChanged(final I identifier, final E element) {
        getHandlerInvocationScheduler().schedule(new Runnable() {

            @Override
            public void run() {
                principal.listElementChanged(identifier, element);
            }
        });
    }
}