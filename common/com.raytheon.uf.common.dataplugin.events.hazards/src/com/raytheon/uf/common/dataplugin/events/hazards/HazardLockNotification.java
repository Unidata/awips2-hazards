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
package com.raytheon.uf.common.dataplugin.events.hazards;

import java.util.List;

import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The notification event for hazard event locks.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 12, 2016 21504      Robert.Blum Initial creation
 * Apr 05, 2017 32733      Robert.Blum Contains list of eventIds now.
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
@DynamicSerialize
public class HazardLockNotification {

    public static final String HAZARD_TOPIC = "edex.alerts.locks";

    public static enum NotificationType {
        LOCK, UNLOCK;
    }

    @DynamicSerializeElement
    private List<String> eventIds;

    @DynamicSerializeElement
    private NotificationType type;

    @DynamicSerializeElement
    private boolean practiceMode;

    @DynamicSerializeElement
    private WsId workstation;

    /**
     * Used only for serialization
     */
    public HazardLockNotification() {
    }

    public HazardLockNotification(List<String> eventIds, NotificationType type,
            boolean practice, WsId workstation) {
        this.eventIds = eventIds;
        this.type = type;
        this.practiceMode = practice;
        this.workstation = workstation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Event: ");
        if (eventIds == null) {
            builder.append(" null ");
        } else {
            builder.append(eventIds);
        }
        builder.append(" Mode: ");
        builder.append(practiceMode);
        builder.append(" Type: ");
        builder.append(type);
        return builder.toString();
    }

    /**
     * @return the eventIds
     */
    public List<String> getEventIds() {
        return eventIds;
    }

    /**
     * @param eventId
     *            the eventId to set
     */
    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    /**
     * @return the type
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * @return the workstation
     */
    public WsId getWorkstation() {
        return workstation;
    }

    /**
     * @param workstation
     *            the workstation to set
     */
    public void setWorkstation(WsId workstation) {
        this.workstation = workstation;
    }

    /**
     * @return the practiceMode
     */
    public boolean isPracticeMode() {
        return practiceMode;
    }

    /**
     * @param practiceMode
     *            the practiceMode to set
     */
    public void setPracticeMode(boolean practiceMode) {
        this.practiceMode = practiceMode;
    }
}