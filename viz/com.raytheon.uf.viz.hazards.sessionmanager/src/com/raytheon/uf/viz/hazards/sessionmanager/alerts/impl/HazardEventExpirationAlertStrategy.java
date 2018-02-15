/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertConfigCriterion;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.HazardType;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Description: An {@link IHazardAlertStrategy} based on expiration times of
 * {@link IHazardEvent}s
 * 
 * Upon {@link HazardNotification}s, generates or cancels
 * {@link HazardEventAlert}s based on {@link HazardEventExpirationAlertsConfig}.
 * Delegates the business of constructing the alert to the
 * {@link HazardEventExpirationAlertFactory}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013    1325    daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 04, 2013   2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov  14, 2013   1472    bkowal     Renamed hazard subtype to subType
 * Nov 20, 2013   2159     daniel.s.schaffer@noaa.gov Now interoperable with DRT
 *                                                    Also, fix to issue 2448
 * March 19, 2014 3277     bkowal      Eliminate false errors due to standard
 *                                     and expected interoperability actions.
 * Dec  1, 2014 3249       Dan Schaffer Issue #3249.  Fixed problem where stale 
 *                                                    alerts would appear when 
 *                                                    you leave hazard services 
 *                                                    and come back much later.
 * Dec 05, 2014  4124      Chris.Golden Changed to work with parameterized config
 *                                         manager.
 * May 29, 2015 6895       Ben.Phillippe Refactored Hazard Service data access
 * Aug 06, 2015 9968       Chris.Cody    Changes for processing ENDED/ELAPSED events
 * Aug 20, 2015 6895       Ben.Phillippe Routing registry requests through request server
 * Sep 15, 2015 7629       Robert.Blum   Updates for saving pending hazards.
 * Oct 29, 2015 11864      Robert.Blum   Expiration time field is now a Date instead
 *                                       of long and also added null safety check.
 * Mar 14, 2016 12145      mduff         Handle error thrown by event manager.
 * May 06, 2016 18202      Robert.Blum   Changes for operational/test mode.
 * May 26, 2016 17529      bkowal        Do not schedule alerts for ended Hazard Events.
 * Sep 15, 2016 21460      Robert.Blum   Added null check for expiration time.
 * Feb 16, 2017 29138      Chris.Golden  Changed to use more efficient database
 *                                       query.
 * May 05, 2017 33738      Robert.Blum   Added addAlerts().
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventExpirationAlertStrategy
        implements IHazardAlertStrategy {

    private final IHazardSessionAlertsManager alertsManager;

    private final ISessionEventManager sessionEventManager;

    private final HazardEventExpirationAlertsConfig alertConfiguration;

    private final HazardEventExpirationAlertFactory alertFactory;

    /**
     * {@link IReadableHazardEvent}s for which there are alerts.
     */
    private final Map<String, IReadableHazardEvent> alertedEvents;

    private final ISessionTimeManager sessionTimeManager;

    public HazardEventExpirationAlertStrategy(
            IHazardSessionAlertsManager alertsManager,
            ISessionTimeManager sessionTimeManager,
            ISessionConfigurationManager<ObservedSettings> sessionConfigurationManager,
            ISessionEventManager sessionEventManager) {
        this.alertsManager = alertsManager;
        this.alertConfiguration = loadAlertConfiguration(
                sessionConfigurationManager);
        this.sessionEventManager = sessionEventManager;
        this.sessionTimeManager = sessionTimeManager;
        this.alertFactory = new HazardEventExpirationAlertFactory(
                sessionTimeManager);
        this.alertedEvents = Maps.newHashMap();
    }

    /**
     * TODO This may need to be public to support updating the alert
     * configuration if the user changes the alert config while an Hazard
     * Session is active. Not clear if we need to support that pathway.
     */
    private HazardEventExpirationAlertsConfig loadAlertConfiguration(
            ISessionConfigurationManager<ObservedSettings> sessionConfigManager) {
        HazardAlertsConfig config = sessionConfigManager.getAlertConfig();
        return config.getEventExpirationConfig();
    }

    @Override
    public void initializeAlerts() {

        /*
         * Tack on a filter to look for issued hazards.
         */
        Collection<IHazardEventView> hazardEvents = null;
        hazardEvents = sessionEventManager.getEvents();
        for (IHazardEventView hazardEvent : hazardEvents) {
            if (hazardEvent.getStatus() == HazardStatus.ENDED) {
                continue;
            }
            if (hazardEvent.getExpirationTime() == null) {
                continue;
            }
            generateAlertsForIssuedHazardEvent(hazardEvent);
        }
    }

    @Override
    public void addAlerts(Set<String> eventIDs) {
        for (String id : eventIDs) {
            checkForNewAlerts(sessionEventManager.getEventById(id));
        }
    }

    @Override
    public void updateAlerts(Object notification) {
        HazardNotification hazardNotification = (HazardNotification) notification;
        /*
         * should we do anything with this alert? (are we in the right mode for
         * it)
         */
        if ((CAVEMode.OPERATIONAL.equals(CAVEMode.getMode()) == false)
                && hazardNotification.isPracticeMode() == false) {
            return;
        }

        switch (hazardNotification.getType()) {

        case STORE:
            checkForNewAlerts(hazardNotification.getEvent());
            break;

        case DELETE:
            updatesAlertsForDeletedHazard(hazardNotification.getEvent());
            break;

        case UPDATE:
            /*
             * Do nothing for now. Hazards may occasionally be updated for
             * interoperability purposes.
             */

        default:
            /*
             * Do Nothing. Eliminate Warnings.
             */
        }
    }

    private void checkForNewAlerts(IReadableHazardEvent hazardEvent) {
        HazardStatus status = hazardEvent.getStatus();
        if (HazardStatus.issuedButNotEndedOrElapsed(status)) {
            if (!alertedEvents.containsKey(hazardEvent.getEventID())) {
                generateAlertsForIssuedHazardEvent(hazardEvent);
            } else {
                IReadableHazardEvent alertedEvent = alertedEvents
                        .get(hazardEvent.getEventID());
                Date alertedEventExpirationTime = alertedEvent
                        .getExpirationTime();
                Date eventExpirationTime = hazardEvent.getExpirationTime();
                if (!alertedEventExpirationTime.equals(eventExpirationTime)) {
                    /*
                     * Cancel previous alerts and re-raise them as necessary
                     */
                    updatesAlertsForDeletedHazard(alertedEvent);
                    generateAlertsForIssuedHazardEvent(hazardEvent);
                }

            }
        } else if ((status.equals(HazardStatus.ENDED))
                || (status.equals(HazardStatus.ELAPSED))) {
            updatesAlertsForDeletedHazard(hazardEvent);
        } else if (status.equals(HazardStatus.PROPOSED)) {
            /*
             * Nothing to do here
             */
        } else if (status.equals(HazardStatus.PENDING)
                && hazardEvent.getHazardAttributes()
                        .containsKey(HazardConstants.GFE_INTEROPERABILITY)) {
            /*
             * Nothing to do here - this hazard was created for GFE
             * interoperability which can be in the PENDING state if it was
             * created in response to the save of a GFE grid.
             */
        } else if (status.equals(HazardStatus.PENDING)) {
            /*
             * Nothing to do here - User saved pending hazard(s).
             */
        } else {
            throw new IllegalArgumentException(
                    "Unexpected state " + hazardEvent.getStatus());
        }
    }

    private void generateAlertsForIssuedHazardEvent(
            IReadableHazardEvent hazardEvent) {

        /*
         * No alerts needed if no expiration time. Note: This only occurs when
         * something has gone wrong.
         */
        if (hazardEvent.getExpirationTime() == null) {
            return;
        }

        List<HazardEventExpirationAlertConfigCriterion> alertCriteria = alertConfiguration
                .getCriteria(new HazardType(hazardEvent.getPhenomenon(),
                        hazardEvent.getSignificance(),
                        hazardEvent.getSubType()));
        List<IHazardEventAlert> alerts = Lists.newArrayList();
        for (HazardEventExpirationAlertConfigCriterion alertCriterion : alertCriteria) {
            alertedEvents.put(hazardEvent.getEventID(), hazardEvent);
            alerts.addAll(
                    alertFactory.createAlerts(alertCriterion, hazardEvent));

        }
        alertFactory.addImmediateAlertsAsNecessary(hazardEvent, alerts);
        removeStaleAlerts(alerts);
        removeSupercededAlerts(alerts);
        for (IHazardEventAlert alert : alerts) {
            alertsManager.scheduleAlert(alert);
        }
    }

    private void removeStaleAlerts(List<IHazardEventAlert> alerts) {
        List<IHazardEventAlert> alertsToRemove = Lists.newArrayList();
        for (IHazardEventAlert alert : alerts) {
            if (alert.getDeactivationTime()
                    .before(sessionTimeManager.getCurrentTime())) {
                alertsToRemove.add(alert);
            }
        }
        alerts.removeAll(alertsToRemove);
    }

    private void removeSupercededAlerts(List<IHazardEventAlert> alerts) {
        List<IHazardEventAlert> alertsToRemove = Lists.newArrayList();
        for (IHazardEventAlert thisAlert : alerts) {
            if (isReadyToActivate(thisAlert)) {
                for (IHazardEventAlert thatAlert : alerts) {
                    if (!thisAlert.equals(thatAlert)
                            && thisAlert.getActivationTime()
                                    .getTime() < thatAlert.getActivationTime()
                                            .getTime()
                            && thisAlert.getClass() == thatAlert.getClass()
                            && isReadyToActivate(thatAlert)) {
                        alertsToRemove.add(thisAlert);
                    }
                }
            }
        }
        alerts.removeAll(alertsToRemove);
    }

    private boolean isReadyToActivate(IHazardEventAlert alert) {
        return alert.getActivationTime().getTime() < sessionTimeManager
                .getCurrentTime().getTime();
    }

    private void updatesAlertsForDeletedHazard(
            IReadableHazardEvent hazardEvent) {
        alertedEvents.remove(hazardEvent.getEventID());
        List<IHazardAlert> currentAlerts = alertsManager.getAlerts();
        for (IHazardAlert hazardAlert : currentAlerts) {
            if (hazardAlert instanceof IHazardEventAlert) {
                IHazardEventAlert hazardEventAlert = (IHazardEventAlert) hazardAlert;
                if (hazardEventAlert.getEventID()
                        .equals(hazardEvent.getEventID())) {
                    alertsManager.cancelAlert(hazardEventAlert);

                }
            }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public IHazardSessionAlertsManager getAlertsManager() {
        return alertsManager;
    }

    @Override
    public List<IHazardAlert> findSupercededAlerts(IHazardAlert alert,
            List<IHazardAlert> activeAlerts) {
        List<IHazardAlert> result = Lists.newArrayList();

        /*
         * TODO How can we do this without reflection.
         */
        if (alert instanceof IHazardEventAlert) {
            for (IHazardAlert activeAlert : activeAlerts) {
                if (activeAlert.getClass() == alert.getClass()) {

                    IHazardEventAlert activeEventAlert = (IHazardEventAlert) activeAlert;
                    IHazardEventAlert eventAlert = (IHazardEventAlert) alert;
                    if (activeEventAlert.getEventID()
                            .equals(eventAlert.getEventID())) {
                        result.add(activeAlert);
                    }

                }
            }
        }
        return result;
    }
}
