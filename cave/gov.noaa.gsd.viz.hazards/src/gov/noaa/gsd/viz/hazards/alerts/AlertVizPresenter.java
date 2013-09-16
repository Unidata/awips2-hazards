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

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel.Element;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationPopUpAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;

/**
 * Description: {@link Presenter} for alerts shown in AlertViz
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 09, 2013  1325      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AlertVizPresenter extends HazardServicesPresenter<IView<?, ?>> {

    private List<HazardEventExpirationPopUpAlert> renderedAlerts;

    private IUFStatusHandler statusHandler;

    public AlertVizPresenter(IHazardServicesModel model, IView<?, ?> view,
            EventBus eventBus) {
        super(model, view, eventBus);

    }

    @Subscribe
    public void alertsModified(HazardAlertsModified notification) {
        ImmutableList<IHazardAlert> activeAlerts = notification
                .getActiveAlerts();
        alertAsNeeded(activeAlerts);
    }

    private void alertAsNeeded(ImmutableList<IHazardAlert> activeAlerts) {
        for (IHazardAlert activeAlert : activeAlerts) {
            if (activeAlert.getClass().equals(
                    HazardEventExpirationPopUpAlert.class)) {
                HazardEventExpirationPopUpAlert alert = (HazardEventExpirationPopUpAlert) activeAlert;
                if (!renderedAlerts.contains(alert)) {
                    renderedAlerts.add(alert);
                    statusHandler.error(alert.getText());
                }
            }
        }
    }

    @Override
    public void modelChanged(EnumSet<Element> changed) {
        /*
         * Nothing to do here.
         */
    }

    @Override
    protected void initialize(IView<?, ?> view) {
        renderedAlerts = Lists.newArrayList();
        statusHandler = UFStatus.getHandler(getClass());
        getModel().getSessionManager().registerForNotification(this);
        alertAsNeeded(getModel().getSessionManager().getAlertsManager()
                .getActiveAlerts());
    }
}
