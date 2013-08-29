/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;

/**
 * Superclass from which to derive presenters for specific types of views for
 * Hazard Services.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class HazardServicesPresenter<V extends IView<?, ?>> extends
        Presenter<IHazardServicesModel, IHazardServicesModel.Element, V> {

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
    public HazardServicesPresenter(IHazardServicesModel model, V view,
            EventBus eventBus) {
        super(model, view, eventBus);
    }

    // Public Methods

    /**
     * Dispose of the presenter. This implementation does nothing, but
     * subclasses may override this to, for example, unregister for
     * notifications.
     */
    @Override
    public void dispose() {

        // No action.
    }

    /**
     * Get the session manager.
     * 
     * @return Session manager.
     */
    public ISessionManager getSessionManager() {
        return getModel().getSessionManager();
    }
}
