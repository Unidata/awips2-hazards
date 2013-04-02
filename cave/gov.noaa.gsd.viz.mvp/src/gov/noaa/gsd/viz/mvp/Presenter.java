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
 * listing the different elements of the model that may be changed; and the
 * parameter <code>V</code> specifies the view managed by the presenter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class Presenter<M, E extends Enum<E>, V extends IView<?, ?>> {

    // Private Variables

    /**
     * View.
     */
    private V view = null;

    /**
     * Model.
     */
    private M model = null;

    /**
     * Event bus.
     */
    private EventBus eventBus = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            View to be handled by this presenter.
     */
    public Presenter(M model, V view) {

        // Remember the model.
        this.model = model;

        // Get the event bus, used for communicating state changes.
        eventBus = EventBusSingleton.getInstance();

        // Set the view to that specified and initialize it.
        setView(view);
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

        // Initialize the view.
        initialize(view);
    }

    /**
     * Fire an action off to listeners.
     * 
     * @param action
     *            Action.
     */
    public final void fireAction(IAction action) {
        eventBus.post(action);
    }

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    public abstract void modelChanged(EnumSet<E> changed);

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    protected abstract void initialize(V view);

    /**
     * Get the model.
     * 
     * @return Model.
     */
    protected final M getModel() {
        return model;
    }
}
