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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.time.VisibleTimeRangeChanged;

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
 * Jul 24, 2013  585       C. Golden   Changed to allow loading from bundles.
 * Mar 19, 2014 2925       C. Golden   Changed to fire off notification when
 *                                     visible time range changes.
 * May 12, 2014 2925       C. Golden   Added originator to visible time
 *                                     range change notification, and
 *                                     added current time provider and
 *                                     getter.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionTimeManager implements ISessionTimeManager {

    private final ICurrentTimeProvider currentTimeProvider = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    private final ISessionNotificationSender notificationSender;

    private Date selectedTime;

    private TimeRange selectedTimeRange;

    private TimeRange visibleRange = new TimeRange(0, TimeUnit.DAYS.toMillis(1));

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
    public ICurrentTimeProvider getCurrentTimeProvider() {
        return currentTimeProvider;
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
        notificationSender.postNotificationAsync(new SelectedTimeChanged(this));
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
        notificationSender.postNotificationAsync(new SelectedTimeChanged(this));
    }

    @Override
    public TimeRange getVisibleRange() {
        return visibleRange;
    }

    @Override
    public void setVisibleRange(TimeRange visibleRange, IOriginator originator) {
        this.visibleRange = visibleRange;
        notificationSender.postNotificationAsync(new VisibleTimeRangeChanged(
                this, originator));
    }

    @Handler
    public void eventSelected(SessionEventAttributesModified notification) {
        if (notification.containsAttribute(ISessionEventManager.ATTR_SELECTED)) {
            if (Boolean.TRUE.equals(notification.getEvent().getHazardAttribute(
                    ISessionEventManager.ATTR_SELECTED))) {
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

    @Override
    public void shutdown() {

        /*
         * Nothing to do right now.
         */
    }
}
