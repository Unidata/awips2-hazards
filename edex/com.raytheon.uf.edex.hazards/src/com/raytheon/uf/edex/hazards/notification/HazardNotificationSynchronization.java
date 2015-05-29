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

import org.springframework.transaction.support.TransactionSynchronization;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Ensures that Hazard Event notifications are not send out until the
 * Transaction is complete
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardNotificationSynchronization implements
        TransactionSynchronization {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardNotificationSynchronization.class);

    /** The event to send */
    private HazardNotification notification;

    /**
     * Constructs a new HazardNotificationSynchronization
     * 
     * @param eventObject
     *            The event to send
     * @param type
     *            The type of notification
     * @param mode
     *            The mode
     */
    public HazardNotificationSynchronization(HazardNotification notification) {
        this.notification = notification;
    }

    @Override
    public void afterCompletion(int status) {
        if (status == TransactionSynchronization.STATUS_COMMITTED) {
            HazardNotifier.sendNotification(notification);
        } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
            statusHandler
                    .warn("Transaction rolled back. Discarding Notification: "
                            + notification.toString());
        } else {
            statusHandler
                    .warn("Transaction encountered an unknown status.  Discarding Notification: "
                            + notification.toString());
        }
    }

    @Override
    public void suspend() {
        // No op
    }

    @Override
    public void resume() {
        // No op
    }

    @Override
    public void flush() {
        // No op
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        // No op
    }

    @Override
    public void beforeCompletion() {
        // No op
    }

    @Override
    public void afterCommit() {
        // No op
    }
}
