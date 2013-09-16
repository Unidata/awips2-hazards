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

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardEventAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertConfigCriterion;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.HazardType;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

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
 * July 19, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardEventExpirationAlertStrategy implements IHazardAlertStrategy {

    private final IHazardSessionAlertsManager alertsManager;

    private final ISessionTimeManager sessionTimeManager;

    private final IHazardEventManager hazardEventManager;

    private final HazardEventExpirationAlertsConfig alertConfiguration;

    private final IHazardFilterStrategy hazardFilterStrategy;

    private final HazardEventExpirationAlertFactory alertFactory;

    /**
     * {@link IHazardEvent}s for which there are alerts.
     */
    private final Map<String, IHazardEvent> alertedEvents;

    public HazardEventExpirationAlertStrategy(
            IHazardSessionAlertsManager alertsManager,
            ISessionTimeManager sessionTimeManager,
            ISessionConfigurationManager sessionConfigurationManager,
            IHazardEventManager hazardEventManager,
            IHazardFilterStrategy hazardFilterStrategy) {
        this.alertsManager = alertsManager;
        this.sessionTimeManager = sessionTimeManager;
        this.alertConfiguration = loadAlertConfiguration(sessionConfigurationManager);
        this.hazardEventManager = hazardEventManager;
        this.hazardFilterStrategy = hazardFilterStrategy;
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
            ISessionConfigurationManager sessionManager) {
        HazardAlertsConfig config = sessionManager.getAlertConfig();
        return config.getEventExpirationConfig();
    }

    @Override
    public void initializeAlerts() {
        /**
         * Start with the basic filter (ie all hazards or just for the settings)
         */
        Map<String, List<Object>> filter = hazardFilterStrategy.getFilter();

        /**
         * 
         * Tack on a filter to look for issued hazards.
         */
        HazardQueryBuilder queryBuilder = new HazardQueryBuilder();
        queryBuilder.addKey(HazardConstants.STATE,
                HazardConstants.HazardState.ISSUED);
        filter.putAll(queryBuilder.getQuery());
        Collection<HazardHistoryList> hazardHistories = hazardEventManager
                .getEventsByFilter(filter).values();
        for (HazardHistoryList hazardHistoryList : hazardHistories) {
            IHazardEvent hazardEvent = hazardHistoryList.get(hazardHistoryList
                    .size() - 1);
            generateAlertsForIssuedHazardEvent(hazardEvent);

        }
    }

    @Override
    public void updateAlerts(Object notification) {
        HazardNotification hazardNotification = (HazardNotification) notification;

        switch (hazardNotification.getType()) {

        case STORE:
            IHazardEvent hazardEvent = hazardNotification.getEvent();
            if (hazardEvent.getState().equals(HazardState.ISSUED)) {
                if (!alertedEvents.containsKey(hazardEvent.getEventID())) {
                    generateAlertsForIssuedHazardEvent(hazardEvent);
                } else {
                    IHazardEvent alertedEvent = alertedEvents.get(hazardEvent
                            .getEventID());
                    Date alertedEventExpirationTime = new Date(
                            (Long) alertedEvent
                                    .getHazardAttribute(HazardConstants.EXPIRATIONTIME));
                    Date eventExpirationTime = new Date(
                            (Long) hazardEvent
                                    .getHazardAttribute(HazardConstants.EXPIRATIONTIME));
                    if (!alertedEventExpirationTime.equals(eventExpirationTime)) {
                        /**
                         * Cancel previous alerts and re-raise them as necessary
                         */
                        updatesAlertsForDeletedHazard(alertedEvent);
                        generateAlertsForIssuedHazardEvent(hazardEvent);
                    }

                }
            } else if (hazardEvent.getState().equals(HazardState.ENDED)) {
                updatesAlertsForDeletedHazard(hazardNotification.getEvent());
            } else {
                throw new IllegalArgumentException("Unexpected state "
                        + hazardEvent.getState());
            }
            break;

        case DELETE:
            updatesAlertsForDeletedHazard(hazardNotification.getEvent());
            break;

        case UPDATE:
            throw new IllegalArgumentException(String.format(
                    "Currently, notification %s is not expected and so not"
                            + "handled.", hazardNotification.getType()));
        }

    }

    private void generateAlertsForIssuedHazardEvent(IHazardEvent hazardEvent) {
        List<HazardEventExpirationAlertConfigCriterion> alertCriteria = alertConfiguration
                .getCriteria(new HazardType(hazardEvent.getPhenomenon(),
                        hazardEvent.getSignificance(), hazardEvent.getSubtype()));
        for (HazardEventExpirationAlertConfigCriterion alertCriterion : alertCriteria) {
            alertedEvents.put(hazardEvent.getEventID(), hazardEvent);
            List<IHazardEventAlert> alerts = alertFactory.createAlerts(
                    alertCriterion, hazardEvent);
            for (IHazardEventAlert alert : alerts) {

                Long delay = Math.max(0, alert.getActivationTime().getTime()
                        - sessionTimeManager.getCurrentTime().getTime());
                alertsManager.scheduleAlert(alert, delay);
            }

        }
    }

    private void updatesAlertsForDeletedHazard(IHazardEvent hazardEvent) {
        alertedEvents.remove(hazardEvent.getEventID());
        List<IHazardAlert> currentAlerts = alertsManager.getAlerts();
        for (IHazardAlert hazardAlert : currentAlerts) {
            if (hazardAlert instanceof IHazardEventAlert) {
                IHazardEventAlert hazardEventAlert = (IHazardEventAlert) hazardAlert;
                if (hazardEventAlert.getEventID().equals(
                        hazardEvent.getEventID())) {
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

}
