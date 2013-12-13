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

import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class HazardServicesPresenter<V extends IView<?, ?>> extends
        Presenter<ISessionManager, HazardConstants.Element, V, Object> {

    protected JSONConverter jsonConverter = new JSONConverter();

    protected final ISessionTimeManager timeManager;

    protected final ISessionConfigurationManager configurationManager;

    protected final ISessionEventManager eventManager;

    protected final IHazardSessionAlertsManager alertsManager;

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
    public HazardServicesPresenter(ISessionManager model, V view,
            EventBus eventBus) {
        super(model, view, eventBus);
        this.timeManager = model.getTimeManager();
        this.configurationManager = model.getConfigurationManager();
        this.eventManager = model.getEventManager();
        this.alertsManager = model.getAlertsManager();

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
        return getModel();
    }
}
