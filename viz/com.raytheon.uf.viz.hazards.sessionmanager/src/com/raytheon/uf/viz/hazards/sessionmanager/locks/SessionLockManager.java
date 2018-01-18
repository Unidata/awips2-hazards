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
package com.raytheon.uf.viz.hazards.sessionmanager.locks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardLockNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardLockNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.LockHazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.request.LockRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.LockRequest.LockRequestType;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsLockStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionPreviewOrIssueOngoingModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender.IIntraNotificationHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.SessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Session lock manager, handling locking of hazard events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Dec 12, 2016   21504    Robert.Blum   Initial creation.
 * Jan 06, 2017   21504    Robert.Blum   Performance fix.
 * Mar 06, 2017   21504    Robert.Blum   Fixed issue with threadId.
 * Apr 05, 2017   32733    Robert.Blum   Notifications are for multiple 
 *                                       hazards.
 * Apr 07, 2017   32734    mduff         Check for orphaned locks on
 *                                       startup.
 * Jun 30, 2017   35726    Robert.Blum   Added shutdown() method.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class SessionLockManager
        implements ISessionLockManager, INotificationObserver {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionLockManager.class);

    // Private Variables

    /**
     * Notification sender, for broadcasting lock change notifications.
     */
    private final ISessionNotificationSender sender;

    /**
     * Map of event identifiers to associated lock information. This acts as a
     * cache to avoid having to constantly query edex to determine lock status.
     */
    private final Map<String, LockInfo> lockInfoMap = new HashMap<>();

    /**
     * Set of identifiers of those events that are currently locked for product
     * generation only.
     */
    private final Set<String> identifiersOfEventsLockedForProductGeneration = new HashSet<>();

    /**
     * Current workstation.
     */
    private final WsId thisWorkstation = VizApp.getWsId();

    /**
     * Messenger, for displaying questions and warnings to the user and
     * retrieving answers.
     */
    private final IMessenger messenger;

    /**
     * Flag indicating whether in practice mode or not.
     */
    private final boolean practice = !CAVEMode.OPERATIONAL
            .equals(CAVEMode.getMode());

    /**
     * Session manager.
     */
    private final SessionManager sessionManager;

    /**
     * Intra-managerial notification handler for settings changes.
     */
    private IIntraNotificationHandler<SettingsModified> settingsChangeHandler = new IIntraNotificationHandler<SettingsModified>() {

        @Override
        public void handleNotification(SettingsModified notification) {
            populateLockInfoCache();
        }

        @Override
        public boolean isSynchronous(SettingsModified notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for preview-/issue-ongoing changes.
     */
    private IIntraNotificationHandler<SessionPreviewOrIssueOngoingModified> previewOrIssueOngoingChangeHandler = new IIntraNotificationHandler<SessionPreviewOrIssueOngoingModified>() {

        @Override
        public void handleNotification(
                SessionPreviewOrIssueOngoingModified notification) {
            sessionPreviewOrIssueOngoingModified();
        }

        @Override
        public boolean isSynchronous(
                SessionPreviewOrIssueOngoingModified notification) {
            return true;
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager.
     * @param sender
     *            Notification sender, for broadcasting messages about changes
     *            to lock status.
     * @param messenger
     *            Messenger, for communicating with the user.
     */
    @SuppressWarnings("unchecked")
    public SessionLockManager(SessionManager sessionManager,
            ISessionNotificationSender sender, IMessenger messenger) {
        this.sessionManager = sessionManager;
        this.sender = sender;
        this.messenger = messenger;
        NotificationManagerJob.addObserver(HazardLockNotification.HAZARD_TOPIC,
                this);
        sender.registerIntraNotificationHandler(
                Sets.newHashSet(SettingsModified.class, SettingsLoaded.class),
                settingsChangeHandler);
        sender.registerIntraNotificationHandler(
                SessionPreviewOrIssueOngoingModified.class,
                previewOrIssueOngoingChangeHandler);
        breakOrphanedLocks();
    }

    // Public Methods

    @Override
    public boolean lockHazardEvent(String eventId) {
        return lockHazardEvents(Sets.newHashSet(eventId));
    }

    @Override
    public boolean lockHazardEvents(Set<String> eventIds) {
        return doLockHazardEvents(eventIds, false);
    }

    @Override
    public boolean lockHazardEventsForProductGeneration(Set<String> eventIds) {
        return doLockHazardEvents(eventIds, true);
    }

    @Override
    public boolean unlockHazardEvent(String eventId) {
        return unlockHazardEvents(Sets.newHashSet(eventId));
    }

    @Override
    public boolean unlockHazardEvents(Set<String> eventIds) {

        /*
         * Iterate through the events, determining which are locked by this
         * workstation.
         */
        List<String> eventsToUnlock = new ArrayList<>(eventIds.size());
        for (String eventId : eventIds) {
            if (lockInfoMap.containsKey(eventId)) {
                if (lockInfoMap.get(eventId)
                        .getLockStatus() == LockStatus.LOCKED_BY_ME) {

                    /*
                     * Can only unlock locks that are held by this workstation.
                     */
                    eventsToUnlock.add(eventId);
                }
            }
        }

        /*
         * If none are locked by this workstation, do nothing more.
         */
        if (eventsToUnlock.isEmpty()) {
            return false;
        }

        /*
         * Submit the request and return the resulting success.
         */
        LockHazardEventResponse response = null;
        try {
            response = sendLockRequest(LockRequestType.UNLOCK, eventsToUnlock);
        } catch (HazardEventServiceException e) {
            statusHandler.warn("Problem while attempting to unlock event(s) "
                    + Joiner.on(", ").join(eventsToUnlock) + ": "
                    + e.getMessage());
            return false;
        }

        /*
         * Remove any identifiers of events that were locked but are now
         * unlocked from the set of events locked for product generation.
         */
        if (response.isSuccess()) {
            identifiersOfEventsLockedForProductGeneration
                    .removeAll(eventsToUnlock);
        }

        return response.isSuccess();
    }

    @Override
    public boolean breakHazardEventLock(String eventId) {
        try {
            return sendLockRequest(LockRequestType.BREAK,
                    Lists.newArrayList(eventId)).isSuccess();
        } catch (HazardEventServiceException e) {
            statusHandler
                    .warn("Problem while attempting to break lock for event "
                            + eventId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public LockInfo getHazardEventLockInfo(String eventId) {
        if (lockInfoMap.containsKey(eventId)) {
            return lockInfoMap.get(eventId);
        } else {

            /*
             * The event is not in the cache, so must be a new event. Create a
             * new lock info object and add it to the cache instead of
             * submitting a request, since the fact that the info is not in the
             * cache means that there is no lock.
             */
            LockInfo info = new LockInfo();
            info.setLockStatus(LockStatus.LOCKABLE);
            lockInfoMap.put(eventId, info);
            return info;
        }
    }

    @Override
    public LockStatus getHazardEventLockStatus(String eventId) {
        return getHazardEventLockInfo(eventId).getLockStatus();
    }

    @Override
    public WsId getWorkStationHoldingHazardLock(String eventId) {
        return getHazardEventLockInfo(eventId).getWorkstation();
    }

    @Override
    public Set<String> getHazardsWithLockStatus(LockStatus status) {
        Set<String> lockStatusEventIds = new HashSet<>(lockInfoMap.size(),
                1.0f);
        for (Entry<String, LockInfo> entry : lockInfoMap.entrySet()) {
            if (entry.getValue().getLockStatus() == status) {
                lockStatusEventIds.add(entry.getKey());
            }
        }
        return lockStatusEventIds;
    }

    @Override
    public void notificationArrived(NotificationMessage[] messages) {

        /*
         * Iterate through notifications that have arrived, handling each that
         * is about hazard locks.
         */
        for (NotificationMessage notificationMessage : messages) {
            Object payload = null;
            try {
                payload = notificationMessage.getMessagePayload();
            } catch (NotificationException e) {
                statusHandler.error("Unexpected message payload ", e);
            }
            if (payload instanceof HazardLockNotification) {
                handleNotification((HazardLockNotification) payload);
            }
        }
    }

    @Override
    public void shutdown() {
        NotificationManagerJob
                .removeObserver(HazardLockNotification.HAZARD_TOPIC, this);
        sender.unregisterIntraNotificationHandler(
                previewOrIssueOngoingChangeHandler);
        sender.unregisterIntraNotificationHandler(settingsChangeHandler);
    }

    // Private Methods

    /**
     * Respond to session preview or issue ongoing modifications.
     * 
     * @deprecated Not needed once better separation of concerns is achieved
     *             with respect to awareness of product generation; see
     *             {@link #lockHazardEventsForProductGeneration(List)} for more
     *             information.
     */
    @Deprecated
    private void sessionPreviewOrIssueOngoingModified() {
        if ((sessionManager.isPreviewOngoing() == false)
                && (sessionManager.isIssueOngoing() == false)
                && (identifiersOfEventsLockedForProductGeneration
                        .isEmpty() == false)) {
            unlockHazardEvents(new HashSet<>(
                    identifiersOfEventsLockedForProductGeneration));
            identifiersOfEventsLockedForProductGeneration.clear();
        }
    }

    /**
     * Send the specified lock request.
     * 
     * @param type
     *            Type of the request to be sent.
     * @param eventIds
     *            Event identifiers to be part of the request.
     * @return Response to the request.
     * @throws HazardEventServiceException
     *             If an error occurs when attempting to send the request.
     */
    private LockHazardEventResponse sendLockRequest(LockRequestType type,
            List<String> eventIds) throws HazardEventServiceException {
        LockRequest lockRequest = new LockRequest(practice);
        lockRequest.setType(type);
        lockRequest.setWorkstationId(thisWorkstation);
        lockRequest.setEventIdList(eventIds);
        try {
            return (LockHazardEventResponse) RequestRouter.route(lockRequest);
        } catch (Exception e) {
            throw new HazardEventServiceException(e);
        }
    }

    /**
     * Populate the lock information cache.
     */
    private void populateLockInfoCache() {

        /*
         * Get the lock statuses and cache them.
         */
        LockHazardEventResponse response = null;
        try {
            response = sendLockRequest(LockRequestType.STATUS,
                    Collections.<String> emptyList());
        } catch (HazardEventServiceException e) {
            statusHandler
                    .warn("Problem while attempting populate lock info cache: "
                            + e.getMessage());
            return;
        }
        lockInfoMap.putAll(response.getLockInfoMap());

        /*
         * Iterate through the lock statuses, and for any that are not locked by
         * this workstation, remove their associated event identifiers from the
         * set of those locked for product generation, since they cannot be
         * locked for the latter if they are not locked by this workstation.
         */
        for (Map.Entry<String, LockInfo> entry : lockInfoMap.entrySet()) {
            if (entry.getValue().getLockStatus() != LockStatus.LOCKED_BY_ME) {
                identifiersOfEventsLockedForProductGeneration
                        .remove(entry.getKey());
            }
        }
    }

    /**
     * Determine which of the specified hazard events have a lockable (not
     * already locked by this workstation or any other) status.
     * 
     * @param eventIds
     *            Identifiers of events to be checked.
     * @return Identifiers of those events that have {@link LockStatus#LOCKABLE}
     *         status.
     * @throws IllegalArgumentException
     *             If one or more of the events is locked by someone else.
     */
    private Set<String> getHazardEventIdentifiersThatAreLockable(
            Set<String> eventIds) throws IllegalArgumentException {

        /*
         * Iterate through the locks, determining which are locked by another
         * workstation and which are already locked by this workstation.
         */
        Set<String> lockableEvents = new HashSet<>(eventIds.size());
        for (String eventId : eventIds) {
            if (lockInfoMap.containsKey(eventId)) {
                LockInfo info = lockInfoMap.get(eventId);
                LockStatus status = info.getLockStatus();
                if (status == LockStatus.LOCKED_BY_OTHER) {
                    throw new IllegalArgumentException("hazard event " + eventId
                            + " is already locked by " + info.getWorkstation());
                } else if (status == LockStatus.LOCKABLE) {
                    lockableEvents.add(eventId);
                }
            }
        }
        return lockableEvents;
    }

    /**
     * Lock the specified hazard events for either modification or product
     * generation.
     * 
     * @param eventIds
     *            Identifiers of events to be locked.
     * @param forProductGeneration
     *            Flag indicating whether or not the lock is intended for
     *            product generation.
     * @return <code>true</code> if the locking is successful,
     *         <code>false</code> otherwise.
     */
    private boolean doLockHazardEvents(Set<String> eventIds,
            boolean forProductGeneration) {

        /*
         * Determine which of the events are lockable. If there are no events to
         * be locked, that means that all the specified events are already
         * locked.
         */
        Set<String> eventsToLock = null;
        try {
            eventsToLock = getHazardEventIdentifiersThatAreLockable(eventIds);
        } catch (IllegalArgumentException e) {
            statusHandler.warn(
                    "Problem while attempting to lock: " + e.getMessage());
            return false;
        }
        if (eventsToLock.isEmpty()) {
            return true;
        }

        /*
         * Submit the request and return the resulting success.
         */

        LockHazardEventResponse response = null;
        try {
            response = sendLockRequest(LockRequestType.LOCK,
                    new ArrayList<>(eventsToLock));
        } catch (HazardEventServiceException e) {
            statusHandler.warn("Problem while attempting to lock event(s) "
                    + Joiner.on(", ").join(eventsToLock) + ": "
                    + e.getMessage());
            return false;
        }

        /*
         * If this locking is successful, then if it is intended for product
         * generation, add the event identifiers to be locked to the record of
         * such; if successful but not for product generation, remove said event
         * identifiers from the record of such, since they are now locked not
         * just for product generation, and should not be auto-unlocked.
         */
        if (response.isSuccess()) {
            if (forProductGeneration) {
                identifiersOfEventsLockedForProductGeneration
                        .addAll(eventsToLock);
            } else {
                identifiersOfEventsLockedForProductGeneration
                        .removeAll(eventsToLock);
            }
        }

        return response.isSuccess();
    }

    /**
     * Handle the specified hazard lock change notification.
     * 
     * @param notification
     *            Notification to be handled.
     */
    private void handleNotification(HazardLockNotification notification) {

        /*
         * If the notification is for the wrong mode, do nothing with it.
         */
        if (notification.isPracticeMode() != (CAVEMode
                .getMode() != CAVEMode.OPERATIONAL)) {
            return;
        }

        final List<String> eventIds = notification.getEventIds();
        NotificationType type = notification.getType();
        WsId workstation = notification.getWorkstation();

        List<String> brokenLocks = new ArrayList<>();
        for (String eventId : eventIds) {
            LockInfo newInfo = new LockInfo();
            newInfo.setWorkstation(workstation);
            if (type == NotificationType.LOCK) {
                if (lockInfoMap.containsKey(eventId)) {

                    /*
                     * Check for a lock break.
                     */
                    LockInfo currentInfo = lockInfoMap.get(eventId);
                    if ((currentInfo.getLockStatus() == LockStatus.LOCKED_BY_ME)
                            && (HazardEventUtilities.compareWsIds(workstation,
                                    thisWorkstation) == false)) {
                        brokenLocks.add(eventId);
                    }
                }
                if (HazardEventUtilities.compareWsIds(workstation,
                        thisWorkstation)) {
                    newInfo.setLockStatus(LockStatus.LOCKED_BY_ME);
                } else {
                    newInfo.setLockStatus(LockStatus.LOCKED_BY_OTHER);
                }
            } else {
                newInfo.setLockStatus(LockStatus.LOCKABLE);
            }

            /*
             * Update the lock info cache.
             */
            lockInfoMap.put(eventId, newInfo);
        }

        /*
         * If there is a broken lock, notify the user and revert the hazard.
         */
        if ((brokenLocks.isEmpty() == false)
                && (sessionManager.getEventManager() != null)) {
            if (brokenLocks.size() == 1) {
                messenger.getWarner().warnUser("Hazard Lock Break",
                        "Your lock for event " + brokenLocks.get(0)
                                + " has been broken by "
                                + workstation.getUserName()
                                + "! The event has been reverted to the last saved version."
                                + " If you have the Product Editor open, it needs to be closed.");
            } else {
                List<String> brokenLockEventIds = new ArrayList<>(brokenLocks);
                String lastBrokenLockEventIdentifier = brokenLockEventIds
                        .remove(brokenLocks.size() - 1);
                messenger.getWarner().warnUser("Hazard Lock Break",
                        "Your locks for events "
                                + Joiner.on(", ").join(brokenLockEventIds)
                                + " and " + lastBrokenLockEventIdentifier
                                + " have been broken by "
                                + workstation.getUserName()
                                + "! The events have been reverted to the last saved versions."
                                + " If you have the Product Editor open, it needs to be closed.");
            }
            for (String eventId : brokenLocks) {
                sessionManager.getEventManager().revertEventToLastSaved(eventId,
                        Originator.DATABASE);
            }

            /*
             * Remove any identifiers of events that were locked but which have
             * now had those locks broken from the set of events locked for
             * product generation.
             */
            identifiersOfEventsLockedForProductGeneration
                    .removeAll(brokenLocks);
        }

        /*
         * Send out a message to broadcast notification of the changing of a
         * lock.
         */
        if (sender != null) {
            ISessionEventManager eventManager = sessionManager
                    .getEventManager();
            if (eventManager != null) {
                Set<String> sessionEventIds = new HashSet<>(eventIds);
                for (Iterator<String> iterator = sessionEventIds
                        .iterator(); iterator.hasNext();) {
                    if (eventManager.getEventById(iterator.next()) == null) {
                        iterator.remove();
                    }
                }
                if (sessionEventIds.isEmpty() == false) {
                    SessionEventsLockStatusModified message = new SessionEventsLockStatusModified(
                            eventManager, sessionEventIds, Originator.DATABASE);
                    sender.postNotificationAsync(message);
                }
            }
        }
    }

    /**
     * Break any orphaned locks that are found.
     */
    private void breakOrphanedLocks() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                statusHandler.info("Running orphan lock check.");
                sendLockRequest(LockRequestType.ORPHAN_CHECK,
                        Collections.<String> emptyList());
            }
        };

        new Thread(runnable).start();
    }
}
