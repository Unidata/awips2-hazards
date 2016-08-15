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
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Range;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeChanged;
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
 * May 20, 2013 1257       bsteffen     Initial creation
 * Jul 24, 2013  585       Chris.Golden Changed to allow loading from bundles.
 * Mar 19, 2014 2925       Chris.Golden Changed to fire off notification when
 *                                      visible time range changes.
 * May 12, 2014 2925       Chris.Golden Added originator to visible time
 *                                      range change notification, and
 *                                      added current time provider and
 *                                      getter.
 * Nov 18, 2014 4124       Chris.Golden Revamped to use a single SelectedTime
 *                                      object for both single selected time
 *                                      instances, and selected time ranges.
 *                                      Also added code to change the selected
 *                                      time to always intersect all selected
 *                                      events' time ranges whenever the
 *                                      event selection changes or the start
 *                                      or end time of an event changes.
 * Jan 30, 2015 2331       Chris.Golden Added timer that at regular intervals
 *                                      fires off time tick notifications.
 * Mar 30, 2015 7272       mduff        Changes to support Guava upgrade.
 * Jul 09, 2015 9359       Chris.Cody   Correct for an error when event begin time is
 *                                      after the end of the visible time window and 
 *                                      event end time is UNTIL FURTHER NOTICE
 * Nov 10, 2015 12762      Chris.Golden Added ability to schedule arbitrary
 *                                      tasks to run at regular intervals.
 * Apr 01, 2016 16225      Chris.Golden Added ability to cancel tasks that are
 *                                      scheduled to run at regular intervals.
 * Apr 05, 2016 16225      Chris.Golden Fixed bug that caused tasks that had been
 *                                      set up to run at regular intervals, and
 *                                      then canceled, to continue to run at those
 *                                      intervals.
 * Apr 15, 2016 17864      Chris.Golden Restored behavior introduced by modifications
 *                                      done for issue #4124, so that selected time
 *                                      is now as lazy as possible about changing
 *                                      itself as different hazard events are
 *                                      selected. This reverts changes made for issue
 *                                      #6898 (at least two of them unlogged in this
 *                                      list of changes).
 * Apr 25, 2016 18129      Chris.Golden Changed time-interval-triggered tasks to be
 *                                      triggered close to the instant when the CAVE
 *                                      current time ticks over to a new minute.
 * Aug 15, 2016 18376      Chris.Golden Changed current time provider to be a class
 *                                      variable instead of an instance variable, so
 *                                      that it does not contain a reference to the
 *                                      time manager object (which it doesn't need
 *                                      anyway), to facilitate in garbage collection.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SessionTimeManager implements ISessionTimeManager {

    // Private Static Constants

    /**
     * Scheduler to be used to ensure that timer notifications are published on
     * the main thread. For now, the main thread is the UI thread; when this is
     * changed, this will be rendered obsolete, as at that point there will need
     * to be a blocking queue of {@link Runnable} instances available to allow
     * the new worker thread to be fed jobs. At that point, this should be
     * replaced with an object that enqueues the <code>Runnable</code>s,
     * probably a singleton that may be accessed by the various components in
     * gov.noaa.gsd.viz.hazards and elsewhere (presumably passed to the session
     * manager when the latter is created).
     */
    @Deprecated
    private static final IRunnableAsynchronousScheduler RUNNABLE_ASYNC_SCHEDULER = new IRunnableAsynchronousScheduler() {

        @Override
        public void schedule(Runnable runnable) {

            /*
             * Since the UI thread is currently the thread being used for nearly
             * everything, just run any asynchronous tasks there.
             */
            VizApp.runAsync(runnable);
        }
    };

    /**
     * Provider of the current time.
     */
    private static final ICurrentTimeProvider CURRENT_TIME_PROVIDER = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    // Private Variables

    /**
     * Notification sender, used to send out time-related notifications.
     */
    private final ISessionNotificationSender notificationSender;

    /**
     * Currently selected time.
     */
    private SelectedTime selectedTime;

    /**
     * Time range that is "visible" in temporal displays.
     */
    private TimeRange visibleTimeRange = new TimeRange(0,
            TimeUnit.DAYS.toMillis(1));

    /**
     * Timer used to fire off regular time tick notifications when time is
     * ticking, as well as any tasks scheduled to run at regular intervals via
     * {@link #runAtRegularIntervals(Runnable, long)}.
     */
    private Timer timer;

    /**
     * Map of tasks to be executed at regular intervals to their intervals in
     * minutes. Tasks are executed in the order they are inserted into the map,
     * since this type of map iterates over its entries in the same order in
     * which said entries were initially inserted.
     */
    private final Map<Runnable, Integer> intervalsMinutesForTasks = new LinkedHashMap<>();

    /**
     * Map of tasks to be executed at regular intervals to the number of timer
     * firings that have occurred since each one was last executed.
     */
    private final Map<Runnable, Integer> minutesSinceExecutionForTasks = new HashMap<>();

    /**
     * Simulated time change listener, used to receive notifications that the
     * time has been set by the user, or frozen, or set back to current real
     * time.
     */
    private final ISimulatedTimeChangeListener simulatedTimeChangeListener = new ISimulatedTimeChangeListener() {

        @Override
        public void timechanged() {

            /*
             * Remove the scheduled timer events.
             */
            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            /*
             * Notify any listeners that the CAVE current time has changed.
             */
            publishNotificationOfTimeChange();

            /*
             * If the CAVE current time is not frozen, schedule notifications to
             * occur on the minute.
             */
            scheduleTimerNotifications();

            /*
             * Run any scheduled tasks immediately.
             */
            runScheduledTasks(true);
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param notificationSender
     *            Notification sender, used to send out time-related
     *            notifications.
     */
    public SessionTimeManager(ISessionNotificationSender notificationSender) {
        this.notificationSender = notificationSender;
        Date currentTime = getCurrentTime();
        selectedTime = new SelectedTime(currentTime.getTime());

        /*
         * Create a timer that fires every CAVE current time minute, and
         * schedule the firings. Also subscribe to notifications of simulated
         * time changes to update the timer appropriately whenever such changes
         * occur.
         */
        scheduleTimerNotifications();
        SimulatedTime.getSystemTime().addSimulatedTimeChangeListener(
                simulatedTimeChangeListener);
    }

    // Public Methods

    @Override
    public ICurrentTimeProvider getCurrentTimeProvider() {
        return CURRENT_TIME_PROVIDER;
    }

    @Override
    public long getCurrentTimeInMillis() {
        return getCurrentTime().getTime();
    }

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

    @Override
    public void runAtRegularIntervals(Runnable task, int intervalInMinutes) {

        /*
         * Remember this task for the future.
         */
        intervalsMinutesForTasks.put(task, intervalInMinutes);
        minutesSinceExecutionForTasks.put(task, 0);

        /*
         * Schedule a running of the task immediately.
         */
        RUNNABLE_ASYNC_SCHEDULER.schedule(task);
    }

    @Override
    public void cancelRunAtRegularIntervals(Runnable task) {
        intervalsMinutesForTasks.remove(task);
        minutesSinceExecutionForTasks.remove(task);
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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        SimulatedTime.getSystemTime().removeSimulatedTimeChangeListener(
                simulatedTimeChangeListener);
    }

    // Private Methods

    /**
     * Schedule the timer notifications.
     */
    private void scheduleTimerNotifications() {

        /*
         * If time is frozen, do not start a timer.
         */
        if (SimulatedTime.getSystemTime().isFrozen()) {
            return;
        }

        /*
         * Create the timer.
         */
        timer = new Timer(true);

        /*
         * Get the number of milliseconds between the current simulated time and
         * when the simulated time rolls over to the next minute.
         */
        Date simulatedTimeCurrent = SimulatedTime.getSystemTime().getTime();
        Date simulatedTimeStartOfCurrentMinute = DateUtils.truncate(
                simulatedTimeCurrent, Calendar.MINUTE);
        long offsetUntilFirstSimulatedMinuteChange = simulatedTimeStartOfCurrentMinute
                .getTime()
                + MINUTE_AS_MILLISECONDS
                - simulatedTimeCurrent.getTime();

        /*
         * Schedule the timer to fire a notification off each minute. Using this
         * method instead of schedule() ensures that even if something delays
         * the notifications of the minute changing, future notifications will
         * occur on the minute change.
         * 
         * TODO: It is possible for the "scale" of time to be set in
         * SimulatedTime, which changes time's rate of change (simulating the
         * speeding up or slowing down of time's passage). This ability is not
         * currently used, but if it ever is, the delay calculated above would
         * need to be adjusted accordingly, as would the interval between
         * firings.
         */
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {
                    @Override
                    public void run() {
                        publishNotificationOfTimeChange();
                        runScheduledTasks(false);
                    }
                });
            }
        }, offsetUntilFirstSimulatedMinuteChange, MINUTE_AS_MILLISECONDS);
    }

    /**
     * Run any tasks that have been specified previously.
     * 
     * @param forceRun
     *            Flag indicating whether or not all tasks should be run
     *            regardless of when they were last run. If <code>false</code>,
     *            then only those tasks that have had a sufficiently high
     *            interval of time pass since their last executions will be run.
     *            Note that if <code>true</code>, the elapsed time counters for
     *            all tasks are reset to 0.
     */
    private void runScheduledTasks(boolean forceRun) {

        /*
         * Reset elapsed time counters for all tasks if all tasks are to be run
         * regardless of elapsed time.
         */
        if (forceRun) {
            for (Runnable task : intervalsMinutesForTasks.keySet()) {
                minutesSinceExecutionForTasks.put(task, 0);
            }
        }

        /*
         * Iterate through the tasks, executing each one if its interval has
         * elapsed (or if force-run is true).
         */
        for (Map.Entry<Runnable, Integer> entry : intervalsMinutesForTasks
                .entrySet()) {

            /*
             * If force-run is false, then determine whether the number of
             * minutes have elapsed since last execution that are sufficient to
             * warrant running this task. If so, reset the counter to 0 for the
             * next time around; otherwise, increment the counter. Do nothing
             * more with this task if the latter.
             */
            if (forceRun == false) {
                int minutesSinceExecution = minutesSinceExecutionForTasks
                        .get(entry.getKey()) + 1;
                if (minutesSinceExecution >= entry.getValue()) {
                    minutesSinceExecution = 0;
                }
                minutesSinceExecutionForTasks.put(entry.getKey(),
                        minutesSinceExecution);
                if (minutesSinceExecution != 0) {
                    continue;
                }
            }

            /*
             * Execute the task.
             */
            entry.getKey().run();
        }
    }

    /**
     * Publish notification of a time change.
     */
    private void publishNotificationOfTimeChange() {
        notificationSender.postNotificationAsync(new CurrentTimeChanged(
                Originator.OTHER, SessionTimeManager.this));
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
        Range<Long> intersection = Range.all();
        Range<Long> span = null;
        for (ObservedHazardEvent event : events) {
            Range<Long> eventRange = Range.closed(event.getStartTime()
                    .getTime(), event.getEndTime().getTime());
            if (intersection != null) {
                if (intersection.isConnected(eventRange)) {
                    intersection = intersection.intersection(eventRange);
                } else {
                    boolean eventRangeHigher = (intersection.upperEndpoint() < eventRange
                            .lowerEndpoint());
                    span = Range.closed(
                            (eventRangeHigher ? intersection.upperEndpoint()
                                    : eventRange.upperEndpoint()),
                            (eventRangeHigher ? eventRange.lowerEndpoint()
                                    : intersection.lowerEndpoint()));
                    intersection = null;
                }
            } else if (span.isConnected(eventRange) == false) {
                boolean eventRangeHigher = (span.upperEndpoint() < eventRange
                        .lowerEndpoint());
                span = Range.closed(
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

        /*
         * Ensure that the selected time is visible, and not just at the edge of
         * the ruler.
         */
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
