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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
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
 * Nov 18, 2014 4124       C. Golden   Revamped to use a single SelectedTime
 *                                     object for both single selected time
 *                                     instances, and selected time ranges.
 *                                     Also added code to change the selected
 *                                     time to always intersect all selected
 *                                     events' time ranges whenever the
 *                                     event selection changes or the start
 *                                     or end time of an event changes.
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

    private SelectedTime selectedTime;

    private TimeRange visibleTimeRange = new TimeRange(0,
            TimeUnit.DAYS.toMillis(1));

    public SessionTimeManager(ISessionNotificationSender notificationSender) {
        this.notificationSender = notificationSender;
        Date currentTime = getCurrentTime();
        selectedTime = new SelectedTime(currentTime.getTime());
    }

    @Override
    public ICurrentTimeProvider getCurrentTimeProvider() {
        return currentTimeProvider;
    }

    /**
     * Get the current system time, as an epoch time in milliseconds.
     * 
     * @return Current system time, as an epoch time in milliseconds.
     */
    @Override
    public long getCurrentTimeInMillis() {
        return getCurrentTime().getTime();
    }

    /**
     * Get the current system time.
     * 
     * @return Current system time.
     */
    @Override
    public Date getCurrentTime() {
        return SimulatedTime.getSystemTime().getTime();
    }

    @Override
    public long getLowerSelectedTimeInMillis() {
        return selectedTime.getLowerBound();
    }

    @Override
    public long getUpperSelectedTimeInMillis() {
        return selectedTime.getUpperBound();
    }

    @Override
    public SelectedTime getSelectedTime() {
        return selectedTime;
    }

    @Override
    public void setSelectedTime(SelectedTime selectedTime,
            IOriginator originator) {
        assert (selectedTime != null);
        if (selectedTime.equals(this.selectedTime)) {
            return;
        }
        this.selectedTime = selectedTime;
        notificationSender.postNotificationAsync(new SelectedTimeChanged(this,
                originator));
    }

    @Override
    public long getLowerVisibleTimeInMillis() {
        return visibleTimeRange.getStart().getTime();
    }

    @Override
    public long getUpperVisibleTimeInMillis() {
        return visibleTimeRange.getEnd().getTime();
    }

    @Override
    public TimeRange getVisibleTimeRange() {
        return visibleTimeRange.clone();
    }

    @Override
    public void setVisibleTimeRange(TimeRange timeRange, IOriginator originator) {
        assert (timeRange != null);
        if (timeRange.equals(visibleTimeRange)) {
            return;
        }
        this.visibleTimeRange = timeRange;
        notificationSender.postNotificationAsync(new VisibleTimeRangeChanged(
                this, originator));
    }

    /**
     * Handle a change in the selected events.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void selectedEventsModified(SessionSelectedEventsModified change) {
        SelectedTime newSelectedTime = getSelectedTimeIntersectingEvents(change
                .getEventManager().getSelectedEvents());
        if (newSelectedTime.equals(selectedTime) == false) {
            setSelectedTime(newSelectedTime, Originator.OTHER);
            ensureVisibleTimeRangeIncludesLowerSelectedTime();
        }
    }

    /**
     * Handle a change in the time range of an event.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void eventTimeRangeModified(SessionEventTimeRangeModified change) {
        List<ObservedHazardEvent> events = change.getEventManager()
                .getSelectedEvents();
        if (events.contains(change.getEvent())) {
            setSelectedTime(getSelectedTimeIntersectingEvents(events),
                    Originator.OTHER);
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

    /**
     * Get a selected time that intersects all the specified events. If the
     * existing selected time does so, then it is returned; otherwise,a new
     * selected time is returned.
     * 
     * @param events
     *            Hazard events with which the selected time must intersect.
     * @return Selected time that intersects the specified events.
     */
    private SelectedTime getSelectedTimeIntersectingEvents(
            Collection<ObservedHazardEvent> events) {

        /*
         * Iterate through the events, starting with an unbounded range and
         * narrowing the range by intersecting it with each event's time range.
         * (Any value within this intersection range may then be used as the
         * selected time.) If in the course of doing this the intersection is
         * reduced to nothing, begin building up a range indicating the minimum
         * range required to intersect all the events.
         */
        Range<Long> intersection = Ranges.all();
        Range<Long> span = null;
        for (ObservedHazardEvent event : events) {
            Range<Long> eventRange = Ranges.closed(event.getStartTime()
                    .getTime(), event.getEndTime().getTime());
            if (intersection != null) {
                if (intersection.isConnected(eventRange)) {
                    intersection = intersection.intersection(eventRange);
                } else {
                    boolean eventRangeHigher = (intersection.upperEndpoint() < eventRange
                            .lowerEndpoint());
                    span = Ranges.closed(
                            (eventRangeHigher ? intersection.upperEndpoint()
                                    : eventRange.upperEndpoint()),
                            (eventRangeHigher ? eventRange.lowerEndpoint()
                                    : intersection.lowerEndpoint()));
                    intersection = null;
                }
            } else if (span.isConnected(eventRange) == false) {
                boolean eventRangeHigher = (span.upperEndpoint() < eventRange
                        .lowerEndpoint());
                span = Ranges.closed(
                        (eventRangeHigher ? span.lowerEndpoint() : eventRange
                                .upperEndpoint()),
                        (eventRangeHigher ? eventRange.lowerEndpoint() : span
                                .upperEndpoint()));
            }
        }

        /*
         * If there is an intersection, use the existing selected time if the
         * latter intersects with the intersection; otherwise, use the lower
         * bound of the intersection. If instead there is a minimum range, use
         * the existing selected time if the latter encloses the former,
         * otherwise, use the span.
         */
        if (intersection != null) {
            if (intersection.isConnected(selectedTime.getRange())) {
                return selectedTime;
            } else {
                return new SelectedTime(
                        selectedTime.getLowerBound() > intersection
                                .upperEndpoint() ? intersection.upperEndpoint()
                                : intersection.lowerEndpoint());
            }
        } else {
            if (selectedTime.getRange().encloses(span)) {
                return selectedTime;
            } else {
                return new SelectedTime(span.lowerEndpoint(),
                        span.upperEndpoint());
            }
        }
    }

    /**
     * Ensure that the visible time range intersects the selected time range.
     */
    private void ensureVisibleTimeRangeIncludesLowerSelectedTime() {

        // Ensure that the selected time is visible, and not just at
        // the edge of the ruler.
        long lower = visibleTimeRange.getStart().getTime();
        long upper = visibleTimeRange.getEnd().getTime();
        long range = visibleTimeRange.getDuration();
        long selectedTimeStart = getLowerSelectedTimeInMillis();
        if ((selectedTimeStart < lower + (range / 8L))
                || (selectedTimeStart > upper - (range / 8L))) {
            lower = selectedTimeStart - (range / 8L);
            upper = lower + range;
            setVisibleTimeRange(new TimeRange(lower, upper), Originator.OTHER);
        }
    }

}
