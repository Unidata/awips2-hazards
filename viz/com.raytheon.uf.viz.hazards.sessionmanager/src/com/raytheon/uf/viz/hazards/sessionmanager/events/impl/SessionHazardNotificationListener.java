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
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.notification.INotificationObserver;
import com.raytheon.uf.viz.core.notification.NotificationException;
import com.raytheon.uf.viz.core.notification.NotificationMessage;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * An INotificationObserver that keeps the session event manager in sync with
 * the database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2013 1257       bsteffen     Initial creation
 * Aug 16, 2013 1325       daniel.s.schaffer@noaa.gov    Alerts integration
 * Oct 23, 2013 2277       jsanchez     Removed HazardEventConverter from viz.
 * Apr 28, 2016 18267      Chris.Golden Changed to work with new version of
 *                                      mergeHazardEvents() method.
 * May 13, 2016 15676      Chris.Golden Changed to use "database" as the
 *                                      originator for any hazard event
 *                                      additions or merges resulting from
 *                                      the database.
 * Jun 23, 2016 19537      Chris.Golden Changed to use new parameter for
 *                                      merging hazard events.
 * Aug 15, 2016 18376      Chris.Golden Added use of new temporary session
 *                                      event manager method isShutDown() to
 *                                      prevent forwarding of notifications
 *                                      to event managers that are shut down,
 *                                      until Redmine issue #21271 is resolved
 *                                      and garbage collection problems no
 *                                      longer exist.
 * Feb 01, 2017 15556      Chris.Golden Cleaned up, added note about race
 *                                      condition to be addressed in future.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionHazardNotificationListener implements INotificationObserver {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionHazardNotificationListener.class);

    private final Reference<SessionEventManager> manager;

    public SessionHazardNotificationListener(SessionEventManager manager) {
        this(manager, true);
    }

    public SessionHazardNotificationListener(SessionEventManager manager,
            boolean observe) {
        this.manager = new WeakReference<SessionEventManager>(manager);
        if (observe) {
            NotificationManagerJob.addObserver(HazardNotification.HAZARD_TOPIC,
                    this);
        }
    }

    /*
     * TODO: Remove use of manager.isShutDown() within method body once garbage
     * collection issues have been sorted out; see Redmine issue #21271.
     * 
     * TODO: If this method is called from a thread other than the session
     * manager worker thread, there is a race condition here; checking to see if
     * the manager is shut down and then responding accordingly in another
     * thread is inherently risky. This also means that the call to the
     * handleNotification() method here should occur within the session manager
     * worker thread.
     */
    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        ISessionEventManager<ObservedHazardEvent> manager = this.manager.get();
        if ((manager == null) || manager.isShutDown()) {
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

    /**
     * Handle the specified notification.
     * <p>
     * TODO: This is only <code>public</code> because it is used by test classes
     * outside this package. Some other way should be found to test it, so that
     * this may be rendered package-private or private.
     * </p>
     * <p>
     * TODO: If this method is called from a thread other than the session
     * manager worker thread, there is a race condition here; checking to see if
     * the manager has a version of a particular hazard event and then
     * responding accordingly in another thread is inherently risky.
     * </p>
     * 
     * @param notification
     *            Notification that has arrived.
     */
    public void handleNotification(HazardNotification notification) {
        if (CAVEMode.getMode() == CAVEMode.PRACTICE
                && notification.isPracticeMode() == false) {
            return;
        }
        IHazardEvent newEvent = notification.getEvent();
        SessionEventManager manager = this.manager.get();
        if (manager == null) {
            return;
        }

        /*
         * TODO: Handle the DELETE_ALL notification type.
         */
        switch (notification.getType()) {
        case DELETE:
            manager.handleEventRemovalFromDatabase(newEvent);
            break;
        case UPDATE:
        case STORE:
            manager.handleEventAdditionToDatabase(newEvent);
        }
    }

}
