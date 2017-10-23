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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionBatchNotificationsToggled;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.utilities.Merger;

/**
 * Sender of {@link ISessionNotification} messages. This posts any notifications
 * onto an event bus, which ensures that such notifications are delivered to any
 * handlers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 10, 2013            bsteffen     Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Added intra-managerial notifications
 *                                      and message batching capability.
 * Oct 23, 2017   21730    Chris.Golden Pass notification to be handled to the
 *                                      IIntraNotificationHander.isSynchronous()
 *                                      method since it now requires this, and
 *                                      also changed the notification that is
 *                                      sent out about notification accumulation
 *                                      being ended to be sent after all the
 *                                      notifications that were accumulated have
 *                                      themselves been sent.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SessionNotificationSender implements ISessionNotificationSender {

    /**
     * Event bus used for actual posting of notifications.
     */
    private final BoundedReceptionEventBus<Object> bus;

    /**
     * Map of notification types to any intra-managerial notification handlers
     * that are to be notified when notifications of those types are received
     * for posting. Note that the the values are sets that use the order in
     * which handlers were added to the sets as the order of iteration, allowing
     * handlers that were ordered earlier to be executed earlier.
     */
    private final Map<Class<? extends ISessionNotification>, Set<IIntraNotificationHandler<? extends ISessionNotification>>> intraHandlersForNotificationTypes = new HashMap<>();

    /**
     * Counter for calls to {@link #startAccumulatingAsyncNotifications()}
     * versus {@link #finishAccumulatingAsyncNotifications()}.
     */
    private int accumulationCounter;

    /**
     * List of notifications, if any, that have accumulated. This list will be
     * non-empty only if {@link #accumulationCounter} is positive, and if
     * {@link #postNotificationAsync(ISessionNotification)} has been called at
     * least once since the counter was last non-positive.
     */
    private final List<ISessionNotification> accumulatedNotifications = new ArrayList<>();

    /**
     * Runnable asynchronous scheduler, used to schedule execution of
     * {@link Runnable} instances to occur later.
     */
    private final IRunnableAsynchronousScheduler runnableAsynchronousScheduler;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param bus
     *            Event bus to be used to post the notifications.
     * @param runnableAsynchronousScheduler
     *            Runnable asynchronous scheduler, used to schedule execution of
     *            {@link Runnable} instances to occur later.
     */
    public SessionNotificationSender(BoundedReceptionEventBus<Object> bus,
            IRunnableAsynchronousScheduler runnableAsynchronousScheduler) {
        this.bus = bus;
        this.runnableAsynchronousScheduler = runnableAsynchronousScheduler;
    }

    // Public Methods

    @Override
    public <N extends ISessionNotification> void registerIntraNotificationHandler(
            Class<N> type, IIntraNotificationHandler<N> handler) {
        associateNotificationTypeWithHandler(type, handler);
    }

    @Override
    public <N extends ISessionNotification> void registerIntraNotificationHandler(
            Set<Class<? extends N>> types,
            IIntraNotificationHandler<N> handler) {
        for (Class<? extends N> type : types) {
            associateNotificationTypeWithHandler(type, handler);
        }
    }

    @Override
    public <N extends ISessionNotification> void unregisterIntraNotificationHandler(
            IIntraNotificationHandler<N> handler) {
        for (Set<IIntraNotificationHandler<?>> handlers : intraHandlersForNotificationTypes
                .values()) {
            handlers.remove(handler);
        }
    }

    @Override
    public void postNotification(ISessionNotification notification) {

        /*
         * Immediately notify any managers that are interested in this
         * notification.
         */
        notifyIntraNotificationHandlers(notification);

        /*
         * Post the notification synchronously.
         */
        bus.publish(notification);
    }

    @Override
    public void postNotificationAsync(ISessionNotification notification) {

        /*
         * Immediately notify any managers that are interested in this
         * notification.
         */
        notifyIntraNotificationHandlers(notification);

        /*
         * If asynchronous notifications are being accumulated for posting
         * later, add the new notification into the old ones, merging it with
         * any accumulated notifications as appropriate.
         */
        synchronized (accumulatedNotifications) {
            if (accumulationCounter > 0) {
                Merger.merge(accumulatedNotifications, notification);
                return;
            }
        }

        /*
         * Since notifications are not accumulating, simply post the
         * notification now.
         */
        bus.publishAsync(notification);
    }

    @Override
    public void startAccumulatingAsyncNotifications() {
        synchronized (accumulatedNotifications) {
            if (accumulationCounter == 0) {
                postNotificationAsync(
                        new SessionBatchNotificationsToggled(true));
            }
            accumulationCounter++;
        }
    }

    @Override
    public void finishAccumulatingAsyncNotifications() {
        synchronized (accumulatedNotifications) {
            if (accumulationCounter == 0) {
                return;
            }
            accumulationCounter--;
            if (accumulationCounter == 0) {
                for (ISessionNotification notification : accumulatedNotifications) {
                    bus.publishAsync(notification);
                }
                accumulatedNotifications.clear();
                postNotificationAsync(
                        new SessionBatchNotificationsToggled(false));
            }
        }
    }

    // Private Methods

    /**
     * Associate the specified notification type with the specified
     * intra-managerial notification handler.
     * 
     * @param type
     *            Type of notifications to associate with the handler.
     * @param handler
     *            Handler to be associated.
     */
    private void associateNotificationTypeWithHandler(
            Class<? extends ISessionNotification> type,
            IIntraNotificationHandler<? extends ISessionNotification> handler) {
        Set<IIntraNotificationHandler<?>> handlers = intraHandlersForNotificationTypes
                .get(type);

        /*
         * If no handler set was found, create a set that iterates in the order
         * in which handlers were added. This allows the order in which handlers
         * are registered using this method to be the order in which they are
         * executed.
         */
        if (handlers == null) {
            handlers = new LinkedHashSet<>();
            intraHandlersForNotificationTypes.put(type, handlers);
        }

        handlers.add(handler);
    }

    /**
     * Notify any intra-managerial notification handlers that are interested in
     * the specified notification that it is being posted.
     * 
     * @param notification
     *            Notification to be provided to any intra-managerial
     *            notification handlers that are interested.
     */
    @SuppressWarnings("unchecked")
    private <N extends ISessionNotification> void notifyIntraNotificationHandlers(
            final N notification) {
        Set<IIntraNotificationHandler<?>> handlers = intraHandlersForNotificationTypes
                .get(notification.getClass());
        if (handlers != null) {
            for (final IIntraNotificationHandler<?> handler : handlers) {
                if (((IIntraNotificationHandler<? super N>) handler)
                        .isSynchronous(notification)) {
                    ((IIntraNotificationHandler<? super N>) handler)
                            .handleNotification(notification);
                } else {
                    runnableAsynchronousScheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            ((IIntraNotificationHandler<? super N>) handler)
                                    .handleNotification(notification);
                        }
                    });
                }
            }
        }
    }
}
