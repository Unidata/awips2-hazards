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

import com.google.common.eventbus.EventBus;

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
 * Aug  9, 2013    1921    daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 22, 2013    1936    Chris.Golden   Added console countdown timers.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class Presenter<M, E extends Enum<E>, V extends IView<?, ?>, A> {

    // Private Variables

    /**
     * View.
     */
    protected V view;

    /**
     * Model.
     */
    private final M model;

    /**
     * Event bus used to signal changes.
     */
    protected final EventBus eventBus;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            View to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public Presenter(M model, V view, EventBus eventBus) {

        // Remember the model and the event bus.
        this.model = model;
        this.eventBus = eventBus;

        // Set the view to that specified and initialize it.
        setView(view);
        eventBus.register(this);

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

        // Remember this view, and notify the view that it will be handled
        // by this presenter.
        this.view = view;
    }

    /**
     * Fire an action off to listeners.
     * 
     * @param action
     *            Action.
     */
    public final void fireAction(A action) {
        eventBus.post(action);
    }

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    public abstract void modelChanged(EnumSet<E> changed);

    /**
     * Dispose of the presenter. This may be implemented, for example, to
     * unregister for notifications for which the presenter was listening.
     */
    public abstract void dispose();

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    public abstract void initialize(V view);

    /**
     * Get the model.
     * 
     * @return Model.
     */
    protected final M getModel() {
        return model;
    }
}
