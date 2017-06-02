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
package com.raytheon.uf.edex.hazards.notification;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;

/**
 * Notifies the appropriate topic that something about hazards has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jun 27, 2013            mnash         Initial creation
 * Mar 24, 2014  3323      bkowal        Mode is now required to construct
 *                                       HazardNotification
 * May 29, 2015  6895      Ben.Phillippe Refactored Hazard Service data access
 * May 06, 2016 18202      Robert.Blum   Changes for operational mode.
 * Feb 16, 2017 29138      Chris.Golden  Changed to use HazardEvent instead of
 *                                       IHazardEvent, since only the former
 *                                       has a unique identifier.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public class HazardNotifier {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardNotifier.class);

    /** Hazard topic uri */
    private static final String SEND_URI = "jms-generic:topic:"
            + HazardNotification.HAZARD_TOPIC + "?timeToLive=60000";

    /**
     * Places a notification on the topic
     * 
     * @param notification
     *            The notification to send
     */
    protected static void sendNotification(HazardNotification notification) {
        try {
            byte[] bytes = SerializationUtil.transformToThrift(notification);
            EDEXUtil.getMessageProducer().sendAsyncUri(SEND_URI, bytes);
        } catch (EdexException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to send to the hazards topic", e);
        } catch (SerializationException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to transform hazard to bytes for transfer", e);
        }
    }

    /**
     * Sends a notification to the notification topic. Notifications may not be
     * sent out immediately. They are cached until the current
     * session/transaction is complete at which time they are sent. If the
     * transaction is rolled back, the notifications are discarded.
     * 
     * @param event
     *            The hazard event
     * @param type
     *            The type of notification
     * @param practice
     *            The practice or operational mode flag
     */
    public void notify(HazardEvent event, NotificationType type,
            boolean practice) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot publish a null event");
        }
        HazardNotification notification = new HazardNotification(event, type,
                practice);

        /*
         * If there is a transaction currently active, cache the notifications
         * until the transaction is complete
         */
        if (isTransactionActive()) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager
                        .registerSynchronization(new HazardNotificationSynchronization(
                                notification));
            }
        } else {
            if (statusHandler.isPriorityEnabled(Priority.DEBUG)) {
                statusHandler
                        .debug("Sending event from non-transactional operation");
            }
            sendNotification(notification);
        }
    }

    /**
     * Check to see if a transaction is active.
     * 
     * @return true if a transaction is active
     */
    protected boolean isTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}
