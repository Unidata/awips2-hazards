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

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

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
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * May 17, 2014 2925       Chris.Golden      Removed protected variables; everything
 *                                           they provided is either accessible using
 *                                           getModel().getXxxxManager(), or is for
 *                                           deprecated JSON-to-Java code.
 * Dec 05, 2014 4124       Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class HazardServicesPresenter<V extends IView<?, ?>>
        extends
        Presenter<ISessionManager<ObservedHazardEvent, ObservedSettings>, HazardConstants.Element, V, Object> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public HazardServicesPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
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
     * TODO: Get rid of this method, replacing calls to it with
     * {@link #getModel()}, which is built into the {@link Presenter superclass}
     * . That is part of the point of the presenter, to have direct access to
     * the model. The model, in Hazard Services, is the session manager.
     * 
     * It is public here because some view code is using it to get at the model,
     * but such use is incorrect, since the view should not be accessing the
     * model. We need this method for now, though, since the views have not yet
     * been fully reimplemented to be loosely coupled to the presenter and not
     * at all to the model.
     * 
     * @return Session manager.
     */
    @Deprecated
    public ISessionManager<ObservedHazardEvent, ObservedSettings> getSessionManager() {
        return getModel();
    }
}
