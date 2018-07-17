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
package com.raytheon.uf.edex.hazards.handlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.comm.HttpServerException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardLockNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.LockHazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.request.LockRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.LockRequest.LockRequestType;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterTask;
import com.raytheon.uf.edex.esb.camel.jms.IBrokerRestProvider;
import com.raytheon.uf.edex.esb.camel.jms.JMSConfigurationException;
import com.raytheon.uf.edex.hazards.notification.HazardLockNotifier;
import com.raytheon.uf.common.comm.CommunicationException;

/**
 * 
 * Handler used to process LockRequests
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 26, 2016  7623      Ben.Phillippe Initial creation
 * Dec 12, 2016 21504      Robert.Blum   Updates for new locking design.
 * Jan 06, 2017 21504      Robert.Blum   Updates for time out overflow issue.
 * Mar 06, 2017 21504      Robert.Blum   Fixed issue with threadId.
 * Apr 05, 2017 32733      Robert.Blum   Remove query for hazard events to
 *                                       improve performance.
 * Apr 07, 2017 32734      mduff         Change to check for orphaned locks
 *                                       on startup.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventLockHandler implements IRequestHandler<LockRequest> {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventLockHandler.class);

    /**
     * The timeout value for Hazard Locks. Note that we do not want locks to
     * timeout, however using Long.MAX_VALUE will cause overflow errors. So we
     * set this to be a relatively high amount of time.
     */
    private static final Long HAZARD_LOCK_TIMEOUT = TimeUtil.MILLIS_PER_YEAR;

    /**
     * Task name used for Operational HazardEventLocks in the cluster task table
     */
    private static final String OPERATIONAL_LOCK_TASK_NAME = "OperationalHazardEventLock";

    /**
     * Task name used for Practice HazardEventLocks in the cluster task table
     */
    private static final String PRACTICE_LOCK_TASK_NAME = "PracticeHazardEventLock";

    /** Template for response messages used for lock requests */
    private static final String LOCK_RESPONSE_MESSAGE = "Workstation ID [%s] %s lock for HazardEvent [%s]";

    /**
     * Template for response messages used for lock requests which request locks
     * currently held
     */
    private static final String ALREADY_LOCKED_RESPONSE_MESSAGE = LOCK_RESPONSE_MESSAGE
            + "  Lock held by [%s] as of [%s]";

    /** Lock granted token */
    private static final String LOCK_GRANTED = "GRANTED";

    /** Lock denied token */
    private static final String LOCK_DENIED = "DENIED";

    /** Lock failed token */
    private static final String LOCK_FAILED = "FAILED";

    /** Qpid connection provider */
    private static IBrokerRestProvider provider;

    private HazardLockNotifier notifier;
        
    public void setProvider(IBrokerRestProvider provider) {
    	HazardEventLockHandler.provider = provider;
    	this.notifier = new HazardLockNotifier();
    }

    @Override
    public LockHazardEventResponse handleRequest(LockRequest request)
            throws Exception {
        LockRequestType type = request.getType();
        boolean practice = request.isPractice();
        String taskName = practice ? PRACTICE_LOCK_TASK_NAME
                : OPERATIONAL_LOCK_TASK_NAME;
        LockHazardEventResponse response = null;
        switch (type) {
        case LOCK:
            response = lockHazard(request, taskName);
            break;
        case STATUS:
            response = getLockStatus(request, taskName);
            break;
        case UNLOCK:
            response = unlockHazard(request, taskName, true);
            break;
        case BREAK:
            response = unlockHazard(request, taskName, false);
            if (response.isSuccess()) {
                response = lockHazard(request, taskName);
            }
            break;
        case ORPHAN_CHECK:
            checkForOrphanedLocks(practice, taskName);
            break;
        default:
            break;
        }
        return response;
    }

    private LockHazardEventResponse lockHazard(LockRequest request,
            String taskName) {
        LockHazardEventResponse response = new LockHazardEventResponse();
        WsId workstationId = request.getWorkstationId();
        boolean practice = request.isPractice();
        StringBuilder message = new StringBuilder();
        response.setSuccess(true);
        List<ClusterTask> grantedLocks = new ArrayList<>();
        for (String eventID : request.getEventIdList()) {
            statusHandler.info("Workstation ID [" + workstationId
                    + "] attempting to acquire lock for HazardEvent [" + eventID
                    + "]...");
            boolean lockGranted = false;
            ClusterTask clusterTask = ClusterLockUtils.lock(taskName, eventID,
                    workstationId.toString(), HAZARD_LOCK_TIMEOUT, false);

            String currentMessage = null;
            switch (clusterTask.getLockState()) {
            case ALREADY_RUNNING:
                if (compareWsIds(clusterTask.getExtraInfo(),
                        workstationId.toString())) {
                    currentMessage = String.format(LOCK_RESPONSE_MESSAGE,
                            workstationId, LOCK_GRANTED, eventID);
                    lockGranted = true;
                } else {
                    currentMessage = String.format(
                            ALREADY_LOCKED_RESPONSE_MESSAGE, workstationId,
                            LOCK_DENIED, eventID, clusterTask.getExtraInfo(),
                            new Date(clusterTask.getLastExecution())
                                    .toString());
                }
                break;
            case FAILED:
                currentMessage = String.format(LOCK_RESPONSE_MESSAGE,
                        workstationId, LOCK_FAILED, eventID);
                break;
            case OLD:
                /*
                 * This case should not happen as we are using the current time
                 * lock handler
                 */
                currentMessage = String.format(LOCK_RESPONSE_MESSAGE,
                        workstationId, LOCK_DENIED, eventID);
                break;
            case SUCCESSFUL:
                currentMessage = String.format(LOCK_RESPONSE_MESSAGE,
                        workstationId, LOCK_GRANTED, eventID);
                lockGranted = true;
                break;
            default:
                break;
            }

            message.append(currentMessage).append("\n");
            response.setPayload(lockGranted);

            if (lockGranted) {
                statusHandler.info(currentMessage);
                grantedLocks.add(clusterTask);
            } else {
                statusHandler.warn(
                        currentMessage + " Unlocking " + grantedLocks.size()
                                + " previously acquired locks...");
                for (ClusterTask ct : grantedLocks) {
                    statusHandler.info("Releasing lock [" + eventID + "]...");
                    if (ClusterLockUtils.unlock(ct, true)) {
                        statusHandler.info("Lock [" + eventID + "] released.");
                    } else {
                        statusHandler.warn(
                                "Failed to release lock [" + eventID + "]!");
                    }
                }
                response.setSuccess(false);
                break;
            }
        }
        if (response.isSuccess()) {
            /*
             * Send out a single notification to all Cave instances with the
             * updated lock status of all the events.
             */
            notifier.notify(request.getEventIdList(), NotificationType.LOCK,
                    practice, workstationId);
        }
        response.setMessage(message.toString());
        return response;
    }

    private LockHazardEventResponse unlockHazard(LockRequest request,
            String taskName, boolean notify) {
        LockHazardEventResponse response = new LockHazardEventResponse();
        StringBuilder message = new StringBuilder();
        WsId workstationId = request.getWorkstationId();
        boolean practice = request.isPractice();
        response.setSuccess(true);
        String logMsg = "";
        List<String> unlockedEventIds = new ArrayList<>(
                request.getEventIdList().size());
        for (String eventID : request.getEventIdList()) {
            statusHandler.info("Workstation ID [" + workstationId
                    + "] attempting to release lock for HazardEvent [" + eventID
                    + "]...");
            if (ClusterLockUtils.unlock(taskName, eventID)) {
                logMsg = "Lock [" + eventID + "] released.";
                statusHandler.info(logMsg);
                message.append(logMsg).append("\n");
                unlockedEventIds.add(eventID);
            } else {
                response.setSuccess(false);
                logMsg = "Failed to release lock [" + eventID + "]!";
                statusHandler.warn(logMsg);
                message.append(logMsg).append("\n");
            }
        }

        /*
         * Send out a single notification to all Cave instances with the updated
         * lock statuses.
         */
        if (notify && unlockedEventIds.isEmpty() == false) {
            notifier.notify(unlockedEventIds, NotificationType.UNLOCK, practice,
                    workstationId);
        }
        response.setMessage(message.toString());
        return response;
    }

    private LockHazardEventResponse getLockStatus(LockRequest request,
            String taskName) {
        LockHazardEventResponse response = new LockHazardEventResponse();
        String workstationId = request.getWorkstationId().toString();
        List<ClusterTask> locks = ClusterLockUtils.getLocks(taskName);
        List<String> eventIDs = request.getEventIdList();
        if (eventIDs.isEmpty()) {
            /*
             * Empty list implies that status for all locks should be returned.
             */
            for (ClusterTask lock : locks) {
                LockInfo info = new LockInfo();
                setLockInfo(info, workstationId, lock);
                response.addLockInfo(lock.getId().getDetails(), info);
            }
        } else {
            for (String eventID : request.getEventIdList()) {
                boolean found = false;
                LockInfo info = new LockInfo();
                for (ClusterTask lock : locks) {
                    String lockID = lock.getId().getDetails();
                    if (eventID.equals(lockID)) {
                        found = true;
                        setLockInfo(info, workstationId, lock);
                        response.addLockInfo(eventID, info);
                        break;
                    }
                }
                if (!found) {
                    info.setLockStatus(LockStatus.LOCKABLE);
                    response.addLockInfo(eventID, info);
                }
            }
        }
        return response;
    }

    private void setLockInfo(LockInfo info, String workstationId,
            ClusterTask lock) {
        if (lock.isRunning()) {
            if (compareWsIds(workstationId, lock.getExtraInfo())) {
                info.setLockStatus(LockStatus.LOCKED_BY_ME);
            } else {
                info.setLockStatus(LockStatus.LOCKED_BY_OTHER);
            }
            info.setWorkstation(new WsId(lock.getExtraInfo()));
        } else {
            info.setLockStatus(LockStatus.LOCKABLE);
        }
    }

    /**
     * Checks for orphaned hazard event locks
     * @throws  
     * @throws JMSConfigurationException 
     * @throws HttpServerException 
     * 
     * @throws Exception
     *             If errors occur getting qpid clients or persisting new state
     *             of HazardEvent
     */
    private void checkForOrphanedLocks(boolean practice, String taskName)
            throws CommunicationException, HttpServerException, JMSConfigurationException {
        Set<String> clients = new HashSet<String>(provider.getConnections());
        List<ClusterTask> locks = ClusterLockUtils.getLocks(taskName);
        List<String> unlockedOrphans = new ArrayList<>();
        for (ClusterTask lock : locks) {
            if (!clients.contains(lock.getExtraInfo())) {
                statusHandler.info("Deleting orphaned Hazard Services lock ["
                        + lock.getId().getName() + "]");
                if (ClusterLockUtils.deleteLock(lock.getId().getName(),
                        lock.getId().getDetails())) {
                    unlockedOrphans.add(lock.getId().getDetails());
                }
            }
        }
        if (unlockedOrphans.isEmpty() == false) {
            /*
             * Send out a notification to all Cave instances with the updated
             * lock status.
             */
            notifier.notify(unlockedOrphans, NotificationType.UNLOCK, practice,
                    new WsId(null, "HazardEventLockHandler",
                            HazardConstants.REQUEST));
        }
    }

    /**
     * Compares two WsIds as Strings, ignoring the threadID field.
     * 
     * @param wsId1
     * @param wsId2
     * @return
     */
    private boolean compareWsIds(String wsId1, String wsId2) {
        try {
            // Drop the threadID from both strings
            wsId1 = wsId1.substring(0, wsId1.lastIndexOf(":"));
            wsId2 = wsId2.substring(0, wsId2.lastIndexOf(":"));
        } catch (IndexOutOfBoundsException e) {
            statusHandler.error("Attempted to compare invalid WsIds.", e);
            return false;
        }
        return wsId1.equals(wsId2);
    }
}
