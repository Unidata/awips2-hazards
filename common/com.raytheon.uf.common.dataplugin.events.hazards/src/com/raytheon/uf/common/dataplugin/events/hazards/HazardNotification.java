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

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The notification event for hazard events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2013            mnash     Initial creation
 * Mar 24, 2014 3323       bkowal    The mode is now required.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@DynamicSerialize
public class HazardNotification {

    public static final String HAZARD_TOPIC = "edex.alerts.hazards";

    public static enum NotificationType {
        STORE, UPDATE, DELETE, DELETE_ALL;
    }

    @DynamicSerializeElement
    private IHazardEvent event;

    @DynamicSerializeElement
    private NotificationType type;

    @DynamicSerializeElement
    private boolean practiceMode;

    /**
     * Used only for serialization
     */
    public HazardNotification() {
    }

    public HazardNotification(IHazardEvent event, NotificationType type,
            Mode mode) {
        this.event = event;
        this.type = type;
        this.practiceMode = (mode == Mode.PRACTICE);
    }

    public Mode getMode() {
        return this.practiceMode ? Mode.PRACTICE : Mode.OPERATIONAL;
    }

    /**
     * @return the event
     */
    public IHazardEvent getEvent() {
        return event;
    }

    /**
     * @param event
     *            the event to set
     */
    public void setEvent(IHazardEvent event) {
        this.event = event;
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

    public boolean isPracticeMode() {
        return practiceMode;
    }

    public void setPracticeMode(boolean practiceMode) {
        this.practiceMode = practiceMode;
    }
}