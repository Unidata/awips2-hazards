/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationPopUpAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;
import net.engio.mbassy.listener.Handler;

/**
 * Description: {@link Presenter} for alerts shown in AlertViz
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 09, 2013  1325      daniel.s.schaffer Initial creation
 * Dec 03, 2013 2182       daniel.s.schaffer Refactoring - eliminated IHazardsIF
 * May 17, 2014 2925       Chris.Golden      Added newly required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Dec 05, 2014 4124       Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Dec 17, 2017 20739      Chris.Golden      Refactored away access to directly
 *                                           mutable session events.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AlertVizPresenter extends HazardServicesPresenter<IView<?, ?>> {

    private List<HazardEventExpirationPopUpAlert> renderedAlerts;

    private IUFStatusHandler statusHandler;

    public AlertVizPresenter(ISessionManager<ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);

    }

    @Handler
    public void alertsModified(HazardAlertsModified notification) {
        List<IHazardAlert> activeAlerts = notification.getActiveAlerts();
        alertAsNeeded(activeAlerts);
    }

    private void alertAsNeeded(List<IHazardAlert> activeAlerts) {
        for (IHazardAlert activeAlert : activeAlerts) {
            if (activeAlert.getClass()
                    .equals(HazardEventExpirationPopUpAlert.class)) {
                HazardEventExpirationPopUpAlert alert = (HazardEventExpirationPopUpAlert) activeAlert;
                if (!renderedAlerts.contains(alert)) {
                    renderedAlerts.add(alert);
                    statusHandler.error(alert.getText());
                }
            }
        }
    }

    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {
        /*
         * Nothing to do here.
         */
    }

    @Override
    protected void initialize(IView<?, ?> view) {
        renderedAlerts = Lists.newArrayList();
        statusHandler = UFStatus.getHandler(getClass());
        alertAsNeeded(getModel().getAlertsManager().getActiveAlerts());
    }

    @Override
    protected void reinitialize(IView<?, ?> view) {

        /*
         * No action.
         */
    }
}
