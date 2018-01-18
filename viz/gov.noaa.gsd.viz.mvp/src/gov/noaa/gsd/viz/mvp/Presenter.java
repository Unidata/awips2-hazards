/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.mvp;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;

/**
 * Superclass from which to derive presenters for specific types of views. Its
 * parameter <code>M</code> specifies the model with which it will be
 * interacting; the parameter <code>E</code> specifies the enumerated type
 * listing the different elements of the model that may be changed and the
 * parameter <code>V</code> specifies the view managed by the presenter; and the
 * <code>A</code> specifies actions that can be fired off to listeners.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2013     585    Chris.Golden   Changed to have event bus passed
 *                                        in so that it did not have to be a
 *                                        singleton.
 * Aug  9, 2013    1921    Dan Schaffer   Support of replacement of JSON with POJOs
 * Aug 22, 2013    1936    Chris.Golden   Added console countdown timers.
 * May 17, 2014    2925    Chris.Golden   Changed to initialize a view when that
 *                                        view is first attached, or reinitialize
 *                                        it if it is reattached later. Also
 *                                        removed setView() from constructor, so
 *                                        that views are not set before the
 *                                        constructor, and subclass constructors,
 *                                        finish executing.
 * Oct 07, 2014    4042    Chris.Golden   Deprecated methods for firing off events
 *                                        intended for the model on the event bus,
 *                                        and receiving model-changed notifications
 *                                        (since presenters should manipulate the
 *                                        model directly, and should be notified of
 *                                        changes to the model via @Handler methods.
 * Dec 13, 2014    4959    Dan Schaffer   Spatial Display cleanup and other bug fixes
 * Aug 15, 2016   18376    Chris.Golden   Changed reference to model to be non-final
 *                                        so that it may be zeroed out when this
 *                                        class's instances are disposed of, and also
 *                                        added new abstract doDispose() method that
 *                                        is called by dispose(), which is now final.
 * Dec 16, 2016   26573    Kevin.Bisanz   Unsubscribe from eventBus in dispose().
 * Jan 17, 2018   33428    Chris.Golden   Changed to work with new, more flexible
 *                                        toolbar contribution code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class Presenter<M, E extends Enum<E>, V extends IView<?, ?, ?>, A> {

    // Private Variables

    /**
     * View.
     */
    private V view;

    /**
     * Model.
     */
    private M model;

    /**
     * Event bus used to signal changes.
     */
    private final BoundedReceptionEventBus<Object> eventBus;

    /**
     * Set of views that have been attached to this presenter at least once.
     */
    private final Set<V> previouslyAttachedViews = new HashSet<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public Presenter(M model, BoundedReceptionEventBus<Object> eventBus) {

        /*
         * Remember the model and the event bus.
         */
        this.model = model;
        this.eventBus = eventBus;

        /*
         * Register for notifications.
         */
        eventBus.subscribe(this);
    }

    // Public Methods

    /**
     * Get the view.
     * 
     * @return View.
     */
    public final V getView() {
        return view;
    }

    /**
     * Set the view to a new view, initializing the new view as well.
     * 
     * @param view
     *            New view to be used.
     */
    public final void setView(V view) {

        /*
         * Remember this view, and initialize the view if this is its first time
         * attaching to this presenter; if it has been attached to this
         * presenter previously, reinitialize it instead.
         */
        this.view = view;
        if (previouslyAttachedViews.contains(view)) {
            reinitialize(view);
        } else {
            previouslyAttachedViews.add(view);
            initialize(view);
        }
    }

    /**
     * Once the MVP design is complete, the presenters will directly change the
     * model instead of publishing.
     */
    @Deprecated
    public final void publish(A action) {
        eventBus.publish(action);
    }

    /**
     * Receive notification of a model change.
     * <p>
     * <strong>Note</strong>: This method has been deprecated. Presenters should
     * receive notifications of model changes via <code>@Handler</code> methods,
     * since they subscribe to the event bus.
     * 
     * @param changed
     *            Set of elements within the model that have changed.
     */
    @Deprecated
    public abstract void modelChanged(EnumSet<E> changed);

    /**
     * Dispose of the presenter.
     */
    public final void dispose() {
        doDispose();
        eventBus.unsubscribe(this);
        model = null;
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner. This is
     * invoked the first time a view is attached to a presenter, and only the
     * first time.
     * 
     * @param view
     *            View to be initialized.
     */
    protected abstract void initialize(V view);

    /**
     * Reinitialize the specified view in a subclass-specific manner. This is
     * invoked whenever a view is reattached to a presenter, meaning that it has
     * previously been attached and has had {@link #initialize(IView)} called
     * upon it once previously.
     * 
     * @param view
     *            View to be initialized.
     */
    protected abstract void reinitialize(V view);

    /**
     * Perform any subclass-specific disposal. This method is called by
     * {@link #dispose()} before the latter removes access to the model, i.e.
     * for the duration of the execution of this method, {@link #getModel()}
     * will return whatever model was passed in at creation time.
     */
    protected abstract void doDispose();

    /**
     * Get the model.
     * 
     * @return Model.
     */
    protected final M getModel() {
        return model;
    }
}
