package com.raytheon.uf.edex.hazards;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;

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

/**
 * Notifies the appropriate topic that something about hazards has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2013            mnash     Initial creation
 * Mar 24, 2014 #3323      bkowal    Mode is now required to construct HazardNotification
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public class HazardNotifier {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardNotifier.class);

    private static final String SEND_URI = "jms-generic:topic:"
            + HazardNotification.HAZARD_TOPIC + "?timeToLive=60000";

    public static void notify(IHazardEvent event, NotificationType type,
            Mode mode) {
        try {
            HazardNotification notification = new HazardNotification(event,
                    type, mode);
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
}
