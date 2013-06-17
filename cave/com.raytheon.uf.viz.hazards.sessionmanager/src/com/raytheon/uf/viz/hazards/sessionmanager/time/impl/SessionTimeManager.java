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
package com.raytheon.uf.viz.hazards.sessionmanager.time.impl;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;

/**
 * Implementation of ISessionTimeManager
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionTimeManager implements ISessionTimeManager {

    private final ISessionNotificationSender notificationSender;

    private Date selectedTime;

    private TimeRange selectedTimeRange;

    private TimeRange visibleRange;

    public SessionTimeManager(ISessionNotificationSender notificationSender) {
        this.notificationSender = notificationSender;
        selectedTime = getCurrentTime();
        selectedTimeRange = new TimeRange(selectedTime, selectedTime);
    }

    @Override
    public Date getCurrentTime() {
        return SimulatedTime.getSystemTime().getTime();
    }

    @Override
    public Date getSelectedTime() {
        return selectedTime;
    }

    @Override
    public void setSelectedTime(Date selectedTime) {
        if (selectedTime == null) {
            if (this.selectedTime == null) {
                return;
            }
        } else if (selectedTime.equals(this.selectedTime)) {
            return;
        }
        this.selectedTime = selectedTime;
        if (!selectedTimeRange.isValid()) {
            selectedTimeRange = new TimeRange(selectedTime, selectedTime);
        }
        notificationSender.postNotification(new SelectedTimeChanged(this));
    }

    @Override
    public TimeRange getSelectedTimeRange() {
        return selectedTimeRange;
    }

    @Override
    public void setSelectedTimeRange(TimeRange selectedTimeRange) {
        if (selectedTimeRange == null) {
            if (this.selectedTimeRange == null) {
                return;
            }
        } else if (selectedTimeRange.equals(this.selectedTimeRange)) {
            return;
        }
        this.selectedTimeRange = selectedTimeRange;
        notificationSender.postNotification(new SelectedTimeChanged(this));
    }

    @Override
    public TimeRange getVisibleRange() {
        return visibleRange;
    }

    @Override
    public void setVisibleRange(TimeRange visibleRange) {
        this.visibleRange = visibleRange;
    }

    @Subscribe
    public void eventSelected(SessionEventAttributeModified notification) {
        if (notification.isAttrbute(ISessionEventManager.ATTR_SELECTED)) {
            if (Boolean.TRUE.equals(notification.getAttributeValue())) {
                IEvent event = notification.getEvent();
                TimeRange eventRange = new TimeRange(event.getStartTime(),
                        event.getEndTime());
                if (!eventRange.contains(selectedTime)) {
                    setSelectedTime(notification.getEvent().getStartTime());
                }
            }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
