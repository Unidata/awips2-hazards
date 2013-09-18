/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.hazards.sessionmanager.events.impl;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.gfe.HazardEventConverter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.notification.INotificationObserver;
import com.raytheon.uf.viz.core.notification.NotificationException;
import com.raytheon.uf.viz.core.notification.NotificationMessage;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;

/**
 * An INotificationObserver that keeps the session event manager in sync with
 * the database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2013 1257       bsteffen    Initial creation
 * Aug 16, 2013 1325       daniel.s.schaffer@noaa.gov    Alerts integration
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionHazardNotificationListener implements INotificationObserver {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionHazardNotificationListener.class);

    private final Reference<ISessionEventManager> manager;

    private HazardEventConverter hazardEventConverter;

    public SessionHazardNotificationListener(ISessionEventManager manager) {
        this(manager, true);
    }

    public SessionHazardNotificationListener(ISessionEventManager manager,
            boolean observe) {
        this.manager = new WeakReference<ISessionEventManager>(manager);
        if (observe) {
            NotificationManagerJob.addObserver(HazardNotification.HAZARD_TOPIC,
                    this);
        }

        try {
            this.hazardEventConverter = new HazardEventConverter(
                    LocalizationManager.getInstance().getCurrentSite());
        } catch (Exception e) {
            statusHandler.error("Unable to instantiate hazard event converter",
                    e);
        }
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        ISessionEventManager manager = this.manager.get();
        if (manager == null) {
            NotificationManagerJob.removeObserver(
                    HazardNotification.HAZARD_TOPIC, this);
            return;
        }
        for (NotificationMessage message : messages) {
            try {
                Object payload = message.getMessagePayload();
                if (payload instanceof HazardNotification) {
                    handleNotification((HazardNotification) payload);
                }
            } catch (NotificationException e) {
                statusHandler
                        .handle(Priority.ERROR, e.getLocalizedMessage(), e);
            }
        }
    }

    public void handleNotification(HazardNotification notification) {
        ISessionEventManager manager = this.manager.get();
        IHazardEvent newEvent = notification.getEvent();
        IHazardEvent oldEvent = manager.getEventById(newEvent.getEventID());
        switch (notification.getType()) {
        case DELETE:
            if (oldEvent != null) {
                manager.removeEvent(oldEvent);
            }
            break;
        case UPDATE:
        case STORE:
            if (oldEvent != null) {
                SessionEventUtilities.mergeHazardEvents(newEvent, oldEvent);
                return;
            }
            manager.addEvent(newEvent);
            // if (hazardEventConverter != null
            // && hazardEventConverter.needsConversion(newEvent)) {
            // Date currentDate = new Date(SimulatedTime.getSystemTime()
            // .getMillis());
            // hazardEventConverter.storeHazardEventAsGrid(newEvent,
            // currentDate);
            // }
            break;
        }
    }

}
