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

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionHazardNotificationListener implements INotificationObserver{
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionHazardNotificationListener.class);

    private final Reference<ISessionEventManager> manager;

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
                oldEvent.setSiteID(newEvent.getSiteID());
                oldEvent.setEndTime(newEvent.getEndTime());
                oldEvent.setStartTime(newEvent.getStartTime());
                oldEvent.setIssueTime(newEvent.getIssueTime());
                oldEvent.setGeometry(newEvent.getGeometry());
                oldEvent.setPhenomenon(newEvent.getPhenomenon());
                oldEvent.setSignificance(newEvent.getSignificance());
                oldEvent.setSubtype(newEvent.getSubtype());
                oldEvent.setHazardMode(newEvent.getHazardMode());
                Map<String, Serializable> newAttr = newEvent
                        .getHazardAttributes();
                Map<String, Serializable> oldAttr = oldEvent
                        .getHazardAttributes();
                if (oldAttr != null) {
                    oldAttr = new HashMap<String, Serializable>(oldAttr);
                } else {
                    oldAttr = new HashMap<String, Serializable>();
                }
                if (newAttr != null) {
                    for (Entry<String, Serializable> entry : newAttr.entrySet()) {
                        oldEvent.addHazardAttribute(entry.getKey(),
                                entry.getValue());
                        oldAttr.remove(entry.getKey());
                    }
                } else {
                    newAttr = Collections.emptyMap();
                }
                oldAttr.remove(ISessionEventManager.ATTR_CHECKED);
                oldAttr.remove(ISessionEventManager.ATTR_SELECTED);
                oldAttr.remove(ISessionEventManager.ATTR_ISSUED);
                for (String key : oldAttr.keySet()) {
                    oldEvent.removeHazardAttribute(key);
                }
                if (oldEvent instanceof ObservedHazardEvent) {
                    ObservedHazardEvent obEvent = ((ObservedHazardEvent) oldEvent);
                    obEvent.setState(newEvent.getState(), true, false);
                } else {
                    oldEvent.setState(newEvent.getState());
                }
                return;
            }
            manager.addEvent(newEvent);
            break;
        }
    }
    
}
