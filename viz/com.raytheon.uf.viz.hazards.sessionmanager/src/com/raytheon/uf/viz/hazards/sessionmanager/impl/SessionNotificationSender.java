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
package com.raytheon.uf.viz.hazards.sessionmanager.impl;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsTimeRangeBoundariesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * Uses eventbus to send out notifications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2013            bsteffen     Initial creation
 * Apr 10, 2015  6898      Chris.Cody   Refactored async messaging
 * May 20, 2015  7624      mduff        Add notificiation class name to debug message.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionNotificationSender implements ISessionNotificationSender {

    private final BoundedReceptionEventBus<Object> bus;

    // TODO HERE CODY #6898 REMOVE TRACKING
    private boolean trackMsg = false;

    private boolean trackSource = false;

    public SessionNotificationSender(BoundedReceptionEventBus<Object> bus) {
        this.bus = bus;

        String notificationMessageTracking = System
                .getenv("HAZARD_SERVICES_NOTIFICATION_TRACKING");
        if (notificationMessageTracking != null) {
            if (notificationMessageTracking.compareToIgnoreCase("ON") == 0) {
                trackMsg = true;
            } else if (notificationMessageTracking.compareToIgnoreCase("FULL") == 0) {
                trackMsg = true;
                trackSource = true;
            }
        }

    }

    @Override
    public void postNotification(ISessionNotification notification) {
        // TODO HERE CCODY #6898 REMOVE DEBUG
        if (trackMsg == true) {
            long now = System.currentTimeMillis();
            System.out.println("Posting SYNC: " + now + "  "
                    + notification.getClass().getName());
        }
        if (trackSource == true) {
            Exception ex = new Exception("FROM "
                    + notification.getClass().getName());
            ex.printStackTrace();
        }
        bus.publish(notification);
    }

    @Override
    public void postNotificationAsync(ISessionNotification notification) {
        // TODO HERE CCODY #6898 REMOVE DEBUG
        if (trackMsg == true) {
            long now = System.currentTimeMillis();
            String msg = "Posting ASYNC: " + now + "  "
                    + notification.getClass().getName();
            if (notification instanceof OriginatedSessionNotification) {
                OriginatedSessionNotification o = (OriginatedSessionNotification) notification;
                msg += " O: " + o.getOriginator();
            }
            if (notification instanceof SessionEventAdded) {
                SessionEventAdded evt = (SessionEventAdded) notification;
                msg += " ID: " + evt.getEvent().getEventID();
            } else if (notification instanceof SessionEventModified) {
                SessionEventModified evt = (SessionEventModified) notification;
                msg += " ID: " + evt.getEvent().getEventID();
            } else if (notification instanceof SessionEventRemoved) {
                SessionEventRemoved evt = (SessionEventRemoved) notification;
                msg += " ID: " + evt.getEvent().getEventID();
            } else if (notification instanceof SessionEventsTimeRangeBoundariesModified) {
                SessionEventsTimeRangeBoundariesModified evt = (SessionEventsTimeRangeBoundariesModified) notification;
                msg += " ID: " + evt.getEventIds();
            }

            msg += "  " + notification.getClass().getName();

            System.out.println(msg);
        }
        if (trackSource == true) {
            Exception ex = new Exception("FROM "
                    + notification.getClass().getName());
            ex.printStackTrace();
        }
        bus.publishAsync(notification);
    }
}
