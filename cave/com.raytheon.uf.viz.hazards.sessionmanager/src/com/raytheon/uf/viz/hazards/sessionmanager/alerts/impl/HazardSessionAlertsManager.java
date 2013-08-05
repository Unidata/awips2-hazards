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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.notification.INotificationObserver;
import com.raytheon.uf.viz.core.notification.NotificationException;
import com.raytheon.uf.viz.core.notification.NotificationMessage;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertState;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;

/**
 * Description: Manages alerting in hazard services. In order to decouple the
 * business of scheduling and canceling alerts from the logic needed to decide
 * what alerts are needed and when, this class accepts
 * {@link IHazardAlertStrategy}s. They will create {@link IHazardAlert}s and
 * call back to this class to schedule them or cancel them. Following the
 * ModelViewPresenter paradigm, Alerts are used by Presenters which leverage
 * their corresponding Views to render them. When an alert is activated or
 * canceled, this class notifies interested presenters that alerts have changed
 * via the {@link ISessionNotificationSender}. It is assumed the presenters then
 * ask this class for the list of currently active alerts, compares that list to
 * the presenter's own list of what's rendered and tells the view to put up or
 * take down any alerts appropriately.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * July 08, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardSessionAlertsManager implements IHazardSessionAlertsManager,
        INotificationObserver {

    /**
     * An object used to post {@link HazardAlertsModified}s to registered
     * objects such as {@link ConsolePresenter}
     */
    private final ISessionNotificationSender notificationSender;

    /**
     * A collection of strategies upon which {@link IHazardAlert}s are
     * generated. The keys are are used notification classes such as
     * {@link HazardNotification} that are used to partition the alerts. That
     * way, if a {@link HazardNotification} occurs, updates to alerts are only
     * made for those alerts that correspond to that notification.
     */
    private final Map<Class<?>, IHazardAlertStrategy> alertStrategies;

    /**
     * The {@link IHazardAlerts}s currently actively displayed.
     */
    private final List<IHazardAlert> activeAlerts;

    /**
     * {@link IHazardAlertJob}s that have been scheduled.
     */
    private final List<IHazardAlertJob> scheduledAlertJobs;

    /**
     * Handles the startup and shutdown of notifications from external events
     * such as a new {@link IHazardEvent} being issued. This object is injected
     * to facilitate testing.
     */
    private INotificationHandler notificationHandler;

    /**
     * Creates to create a {@link IHazardAlertJob} containing an
     * {@link IHazardAlert} when it is scheduled.
     */
    private IHazardAlertJobFactory alertJobFactory;

    private static Object sharedLock = new Object();

    public HazardSessionAlertsManager(
            ISessionNotificationSender notificationSender) {

        this.notificationSender = notificationSender;
        this.alertStrategies = Maps.newHashMap();
        this.notificationHandler = new NotificationHandler(this);
        this.activeAlerts = Lists.newArrayList();
        this.scheduledAlertJobs = Lists.newArrayList();
        this.alertJobFactory = new HazardAlertJobFactory();

    }

    @Override
    public void start() {
        notificationHandler.start();
        for (IHazardAlertStrategy alertStrategy : alertStrategies.values()) {
            alertStrategy.initializeAlerts();
        }
    }

    @Override
    public void scheduleAlert(IHazardAlert hazardAlert, long delayInMillis) {
        IHazardAlertJob alertJob = alertJobFactory.createJob(this, hazardAlert);
        synchronized (sharedLock) {

            scheduledAlertJobs.add(alertJob);
        }
        alertJob.schedule(delayInMillis);

    }

    @Override
    public void activateAlert(IHazardAlertJob hazardAlertJob) {
        synchronized (sharedLock) {
            scheduledAlertJobs.remove(hazardAlertJob);
            IHazardAlert alert = hazardAlertJob.getHazardAlert();
            alert.setState(HazardAlertState.ACTIVE);
            activeAlerts.add(alert);
        }
        postAlertsModifiedNotification();

    }

    @Override
    public void cancelAlert(IHazardAlert hazardAlert) {
        synchronized (sharedLock) {
            if (!activeAlerts.remove(hazardAlert)) {
                IHazardAlertJob alertJobToRemove = null;
                for (IHazardAlertJob hazardAlertJob : scheduledAlertJobs) {
                    if (hazardAlertJob.getHazardAlert().equals(hazardAlert)) {
                        alertJobToRemove = hazardAlertJob;
                        alertJobToRemove.cancel();
                        break;
                    }
                }
                scheduledAlertJobs.remove(alertJobToRemove);
            }
        }
        postAlertsModifiedNotification();

    }

    @Override
    public void addAlertGenerationStrategy(Class<?> notificationClass,
            IHazardAlertStrategy strategy) {
        alertStrategies.put(notificationClass, strategy);
    }

    @Override
    public void stop() {
        notificationHandler.stop();
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        try {
            for (NotificationMessage notificationMessage : messages) {
                Object payload = notificationMessage.getMessagePayload();
                handleNotification(payload);

            }
        } catch (NotificationException e) {
            throw new IllegalArgumentException("Unexpected message payload ", e);
        }
    }

    @Override
    public List<IHazardAlert> getAlerts() {
        synchronized (sharedLock) {
            List<IHazardAlert> result = Lists.newArrayList(activeAlerts);
            for (IHazardAlertJob alertJob : scheduledAlertJobs) {
                result.add(alertJob.getHazardAlert());
            }
            return Collections.unmodifiableList(result);
        }
    }

    @Override
    public List<IHazardAlert> getActiveAlerts() {
        synchronized (sharedLock) {
            return Collections.unmodifiableList(activeAlerts);
        }
    }

    public List<IHazardAlert> getScheduledAlerts() {
        synchronized (sharedLock) {
            List<IHazardAlert> result = Lists.newArrayList();
            for (IHazardAlertJob alertJob : scheduledAlertJobs) {
                result.add(alertJob.getHazardAlert());
            }
            return result;
        }
    }

    private void handleNotification(Object notification) {
        for (Class<?> notificationClass : alertStrategies.keySet()) {
            if (notificationClass.equals(notification.getClass())) {
                IHazardAlertStrategy strategy = alertStrategies
                        .get(notificationClass);
                strategy.updateAlerts(notification);
            }
        }
    }

    /**
     * Useful for test stubbing.
     */
    void setNotificationHandler(INotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    void setAlertJobFactory(IHazardAlertJobFactory alertJobFactory) {
        this.alertJobFactory = alertJobFactory;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private void postAlertsModifiedNotification() {
        notificationSender.postNotification(new HazardAlertsModified(
                getActiveAlerts()));
    }

}