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
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * A Notification that will be sent out through the SessionManager to notify all
 * components that a persistence (database) operation has completed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 10, 2015 6898      Chris.Cody  Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */

public class PersistenceOperationCompleted extends
        OriginatedSessionNotification {

    private final NotificationType notificationType;

    private final IHazardEvent event;

    public PersistenceOperationCompleted(NotificationType notificationType,
            IHazardEvent event, IOriginator originator) {
        super(originator);
        this.notificationType = notificationType;
        this.event = event;
    }

    public PersistenceOperationCompleted(NotificationType notificationType,
            IOriginator originator) {
        super(originator);
        this.notificationType = notificationType;
        this.event = null;
    }

    public NotificationType getNotificationType() {
        return (this.notificationType);
    }

    public IHazardEvent getEvent() {
        return (this.event);
    }
}
