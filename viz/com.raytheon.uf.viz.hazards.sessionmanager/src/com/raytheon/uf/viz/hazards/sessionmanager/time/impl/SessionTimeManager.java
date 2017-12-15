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

import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTimeRangeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender.IIntraNotificationHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeMinuteTicked;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeReset;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeSecondTicked;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.time.VisibleTimeRangeChanged;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.common.utilities.TimeResolution;

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
 * Oct 19, 2016 21873      Chris.Golden Added time resolution tracking, which when
 *                                      set to seconds causes time-change messages to
 *                                      go out for each second, not just each minute.
 *                                      Also changed managing of selected time to
 *                                      ensure it always lies on unit boundaries (e.g.
 *                                      if time resolution is minutes, it must lie on
 *                                      a minute boundary).
 * Feb 01, 2017 15556      Chris.Golden Changed to use new selection manager. Also
 *                                      moved code that was in the console here that
 *                                      deals with settings loading, as it should be
 *                                      handled in the model.
 * Feb 21, 2017 29138      Chris.Golden Added use of session manager's runnable
 *                                      asynchronous scheduler.
 * Jun 02, 2017  1961      Chris.Golden Totally different implementation of changeset
 *                                      merged into 18-Hazard_Services to allow the
 *                                      selected time to change if the current time
 *                                      is set by the user to something more than an
 *                                      hour from the previous current time.
 * Sep 27, 2017 38072      Chris.Golden Added use of intra-managerial notifications.
 * Oct 23, 2017 21730      Chris.Golden Adjusted IIntraNotificationHander
 *                                      implementations to make their isSynchronous()
 *                                      methods take the new parameter.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable
 *                                      session events.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SessionTimeManager implements ISessionTimeManager {

    // Private Static Constants

    /**
     * Provider of the current time.
     */
    private static final ICurrentTimeProvider CURRENT_TIME_PROVIDER = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    /**
     * Map of time resolutions to the number of milliseconds in the resolutions'
     * units.
     */
    private static final Map<TimeResolution, Long> UNIT_IN_MILLISECONDS_FOR_TIME_RESOLUTION;

    static {
        Map<TimeResolution, Long> map = new EnumMap<>(TimeResolution.class);
        map.put(TimeResolution.MINUTES, TimeUnit.MINUTES.toMillis(1L));
        map.put(TimeResolution.SECONDS, TimeUnit.SECONDS.toMillis(1L));
        UNIT_IN_MILLISECONDS_FOR_TIME_RESOLUTION = ImmutableMap.copyOf(map);
    }

    // Private Variables

    /**
     * Session manager.
     */
    private final ISessionManager<ObservedSettings> sessionManager;

    /**
     * Notification sender, used to send out time-related notifications.
     */
    private final ISessionNotificationSender notificationSender;

    /**
     * Time resolution.
     */
    private TimeResolution timeResolution = TimeResolution.MINUTES;

    /**
     * CAVE current time as last recorded (within a minute of the actual CAVE
     * time), as epoch time in millliseconds.
     */
    private long approximateCurrentTime;

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
     * Timer task that sends out notifications of seconds ticking forward, if
     * {@link #timeResolution} is {@link TimeResolution#SECONDS} and the clock
     * is ticking forward.
     */
    private TimerTask secondsTickTimerTask;

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
            cancelTimerNotifications();

            /*
             * Notify any listeners that the CAVE current time has been reset.
             */
            notificationSender.postNotificationAsync(new CurrentTimeReset(
                    Originator.OTHER, SessionTimeManager.this));

            /*
             * If the delta between the new current time and what it was when
             * last checked is more than an hour, change the selected time to
             * match. Regardless, record the new current time.
             */
            long currentTime = getCurrentTimeInMillis();
            if (Math.abs(currentTime
                    - approximateCurrentTime) > TimeUtil.MILLIS_PER_HOUR) {
                setSelectedTime(new SelectedTime(currentTime),
                        Originator.OTHER);
            }
            approximateCurrentTime = currentTime;

            /*
             * If the CAVE current time is not frozen, schedule notifications to
             * occur regularly.
             */
            scheduleTimerNotifications(false);

            /*
             * Run any scheduled tasks immediately.
             */
            runScheduledTasks(true);
        }
    };

    /**
     * Intra-managerial notification handler for selected events changes.
     */
    private IIntraNotificationHandler<SessionSelectedEventsModified> selectedEventsChangeHandler = new IIntraNotificationHandler<SessionSelectedEventsModified>() {

        @Override
        public void handleNotification(
                SessionSelectedEventsModified notification) {
            selectedEventsModified(notification);
        }

        @Override
        public boolean isSynchronous(
                SessionSelectedEventsModified notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for event time range changes.
     */
    private IIntraNotificationHandler<SessionEventModified> eventTimeRangeChangeHandler = new IIntraNotificationHandler<SessionEventModified>() {

        @Override
        public void handleNotification(SessionEventModified notification) {
            if (notification.getClassesOfModifications()
                    .contains(EventTimeRangeModification.class)) {
                eventTimeRangeModified(notification);
            }
        }

        @Override
        public boolean isSynchronous(SessionEventModified notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for settings changes.
     */
    private IIntraNotificationHandler<SettingsModified> settingsChangeHandler = new IIntraNotificationHandler<SettingsModified>() {

        @Override
        public void handleNotification(SettingsModified notification) {
            settingsModified(notification);
        }

        @Override
        public boolean isSynchronous(SettingsModified notification) {
            return true;
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager.
     * @param notificationSender
     *            Notification sender, used to send out time-related
     *            notifications.
     */
    @SuppressWarnings("unchecked")
    public SessionTimeManager(ISessionManager<ObservedSettings> sessionManager,
            ISessionNotificationSender notificationSender) {
        this.sessionManager = sessionManager;
        this.notificationSender = notificationSender;
        Date currentTime = truncateDateForTimeResolution(getCurrentTime(),
                timeResolution);
        approximateCurrentTime = currentTime.getTime();
        selectedTime = new SelectedTime(currentTime.getTime());

        /*
         * Register handlers for notifications from other managers.
         */
        notificationSender.registerIntraNotificationHandler(
                SessionSelectedEventsModified.class,
                selectedEventsChangeHandler);
        notificationSender.registerIntraNotificationHandler(
                SessionEventModified.class, eventTimeRangeChangeHandler);
        notificationSender.registerIntraNotificationHandler(
                Sets.newHashSet(SettingsModified.class, SettingsLoaded.class),
                settingsChangeHandler);

        /*
         * Schedule timer notifications, and subscribe to notifications of
         * simulated time changes to update the timer appropriately whenever
         * such changes occur.
         */
        scheduleTimerNotifications(false);
        SimulatedTime.getSystemTime()
                .addSimulatedTimeChangeListener(simulatedTimeChangeListener);
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

        /*
         * Ensure the selected time is truncated as appropriate given the
         * current time resolution.
         */
        Date lowerBound = truncateDateForTimeResolution(
                new Date(selectedTime.getLowerBound()), timeResolution);
        Date upperBound = truncateDateForTimeResolution(
                new Date(selectedTime.getUpperBound()), timeResolution);
        selectedTime = new SelectedTime(lowerBound.getTime(),
                upperBound.getTime());

        /*
         * If nothing has changed, do no more. Otherwise, remember the new time,
         * and send out a notification.
         */
        if (selectedTime.equals(this.selectedTime)) {
            return;
        }
        this.selectedTime = selectedTime;
        notificationSender.postNotificationAsync(
                new SelectedTimeChanged(this, selectedTime, originator));
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
    public void setVisibleTimeRange(TimeRange timeRange,
            IOriginator originator) {
        assert (timeRange != null);
        if (timeRange.equals(visibleTimeRange)) {
            return;
        }
        this.visibleTimeRange = timeRange;
        notificationSender.postNotificationAsync(
                new VisibleTimeRangeChanged(this, originator));
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
        sessionManager.getRunnableAsynchronousScheduler().schedule(task);
    }

    @Override
    public void cancelRunAtRegularIntervals(Runnable task) {
        intervalsMinutesForTasks.remove(task);
        minutesSinceExecutionForTasks.remove(task);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void shutdown() {
        notificationSender.unregisterIntraNotificationHandler(
                selectedEventsChangeHandler);
        notificationSender.unregisterIntraNotificationHandler(
                eventTimeRangeChangeHandler);
        notificationSender
                .unregisterIntraNotificationHandler(settingsChangeHandler);
        cancelTimerNotifications();
        SimulatedTime.getSystemTime()
                .removeSimulatedTimeChangeListener(simulatedTimeChangeListener);
    }

    // Private Methods

    /**
     * Handle a change in the selected events.
     * 
     * @param change
     *            Change that occurred.
     */
    private void selectedEventsModified(SessionSelectedEventsModified change) {
        if (change.getEventIdentifiers().isEmpty()) {
            return;
        }
        SelectedTime newSelectedTime = getSelectedTimeIntersectingEvents(
                change.getSelectionManager().getSelectedEvents());
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
    private void eventTimeRangeModified(SessionEventModified change) {
        Set<String> eventIdentifiers = sessionManager.getSelectionManager()
                .getSelectedEventIdentifiers();
        if (eventIdentifiers.contains(change.getEvent().getEventID())) {
            setSelectedTime(
                    getSelectedTimeIntersectingEvents(sessionManager
                            .getSelectionManager().getSelectedEvents()),
                    Originator.OTHER);
        }
    }

    /**
     * Handle a change in the settings.
     * 
     * @param change
     *            Change that occurred.
     */
    private void settingsModified(SettingsModified change) {

        /*
         * See if the time resolution has changed; if so, then schedule a timer
         * task for seconds ticking forward if the resolution now includes
         * seconds, or cancel any such task if it now is only minutes. Also set
         * the selected time if the latter is the case, since seconds in the
         * selected time must be truncated to minutes.
         */
        TimeResolution newTimeResolution = sessionManager
                .getConfigurationManager().getSettingsValue(
                        HazardConstants.TIME_RESOLUTION, change.getSettings());
        if (timeResolution != newTimeResolution) {
            timeResolution = newTimeResolution;
            if (timeResolution == TimeResolution.SECONDS) {
                scheduleTimerNotifications(true);
            } else if (secondsTickTimerTask != null) {
                secondsTickTimerTask.cancel();
                secondsTickTimerTask = null;
                setSelectedTime(new SelectedTime(selectedTime),
                        Originator.OTHER);
            }
        }

        /*
         * If a new settings has been loaded, update the visible time range to
         * match the time delta included in the new settings.
         */
        if (change instanceof SettingsLoaded) {

            /*
             * Get the new visible time range boundaries.
             */
            long visibleTimeDelta = change.getSettings()
                    .getDefaultTimeDisplayDuration();
            long lower = this.selectedTime.getLowerBound()
                    - (visibleTimeDelta / 4L);
            long upper = lower + visibleTimeDelta - 1L;

            /*
             * Use the new visible time range boundaries.
             */
            setVisibleTimeRange(new TimeRange(lower, upper), Originator.OTHER);
        }
    }

    /**
     * Schedule the timer notifications.
     * 
     * @param onlyScheduleSecondsTick
     *            Flag indicating whether or not only the seconds tick timer
     *            event should be scheduled. If <code>false</code>, then the
     *            minutes tick will be scheduled, and the seconds tick may be
     *            scheduled as well if the time resolution calls for it.
     */
    private void scheduleTimerNotifications(boolean onlyScheduleSecondsTick) {

        /*
         * If time is frozen, do not start a timer.
         */
        if (SimulatedTime.getSystemTime().isFrozen()) {
            return;
        }

        /*
         * Create the timer if it is needed. If only seconds ticks are to be
         * scheduled, it should already be created.
         */
        if (timer == null) {
            timer = new Timer(true);
        }

        /*
         * Determine which time tick notifications should be scheduled. If only
         * the seconds one is needed, schedule only it; if the time resolution
         * is minutes, schedule only the minutes one; otherwise, schedule both.
         */
        Set<TimeResolution> timeResolutions = (onlyScheduleSecondsTick
                ? Sets.newHashSet(TimeResolution.SECONDS)
                : (timeResolution == TimeResolution.MINUTES
                        ? Sets.newHashSet(TimeResolution.MINUTES)
                        : Sets.newHashSet(TimeResolution.values())));

        /*
         * Create timer tasks to fire off once a minute and/or once a second, as
         * determined above.
         */
        for (final TimeResolution timeResolution : timeResolutions) {

            /*
             * Get the number of milliseconds between the current simulated time
             * and when the simulated time rolls over to the next unit.
             */
            Date simulatedTimeCurrent = SimulatedTime.getSystemTime().getTime();
            Date simulatedTimeStartOfCurrentUnit = truncateDateForTimeResolution(
                    simulatedTimeCurrent, timeResolution);
            long offsetUntilFirstSimulatedMinuteChange = simulatedTimeStartOfCurrentUnit
                    .getTime()
                    + UNIT_IN_MILLISECONDS_FOR_TIME_RESOLUTION
                            .get(timeResolution)
                    - simulatedTimeCurrent.getTime();

            /*
             * Create a timer task to fire a notification off each interval. If
             * this is a minute interval notification, also execute any
             * scheduled tasks.
             */
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    sessionManager.getRunnableAsynchronousScheduler()
                            .schedule(new Runnable() {
                        @Override
                        public void run() {
                            approximateCurrentTime = getCurrentTimeInMillis();
                            notificationSender.postNotificationAsync(
                                    (timeResolution == TimeResolution.MINUTES
                                            ? new CurrentTimeMinuteTicked(
                                                    Originator.OTHER,
                                                    SessionTimeManager.this)
                                            : new CurrentTimeSecondTicked(
                                                    Originator.OTHER,
                                                    SessionTimeManager.this)));
                            if (timeResolution == TimeResolution.MINUTES) {
                                runScheduledTasks(false);
                            }
                        }
                    });
                }
            };

            /*
             * If this is the seconds tick forward task, make a record of it so
             * that it can be canceled independently of the timer later if need
             * be.
             */
            if (timeResolution == TimeResolution.SECONDS) {
                secondsTickTimerTask = timerTask;
            }

            /*
             * Schedule the timer to fire a notification off each interval.
             * Using this method instead of schedule() ensures that even if
             * something delays the notifications of the tick forward, future
             * notifications will occur on the unit change. If this is a minute
             * interval notification, also execute any scheduled tasks.
             * 
             * TODO: It is possible for the "scale" of time to be set in
             * SimulatedTime, which changes time's rate of change (simulating
             * the speeding up or slowing down of time's passage). This ability
             * is not currently used, but if it ever is, the delay calculated
             * above would need to be adjusted accordingly, as would the
             * interval between firings.
             */
            timer.scheduleAtFixedRate(timerTask,
                    offsetUntilFirstSimulatedMinuteChange,
                    UNIT_IN_MILLISECONDS_FOR_TIME_RESOLUTION
                            .get(timeResolution));
        }
    }

    /**
     * Cancel any timer notifications.
     */
    private void cancelTimerNotifications() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            secondsTickTimerTask = null;
        }
    }

    /**
     * Truncate the specified date for the specified time resolution.
     * 
     * @param date
     *            Date-time to be truncated.
     * @param timeResolution
     *            Time resolution that will determine what unit boundary to
     *            which to truncate.
     * @return Truncated date-time.
     */
    private Date truncateDateForTimeResolution(Date date,
            TimeResolution timeResolution) {
        return DateUtils.truncate(date,
                HazardConstants.TRUNCATION_UNITS_FOR_TIME_RESOLUTIONS
                        .get(timeResolution));
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
     * Get a selected time that intersects all the specified events. If the
     * existing selected time does so, then it is returned; otherwise,a new
     * selected time is returned.
     * 
     * @param eventViews
     *            Views of the hazard events with which the selected time must
     *            intersect.
     * @return Selected time that intersects the specified events.
     */
    private SelectedTime getSelectedTimeIntersectingEvents(
            Collection<? extends IHazardEventView> eventViews) {

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
        for (IHazardEventView eventView : eventViews) {
            Range<Long> eventRange = Range.closed(
                    eventView.getStartTime().getTime(),
                    eventView.getEndTime().getTime());
            if (intersection != null) {
                if (intersection.isConnected(eventRange)) {
                    intersection = intersection.intersection(eventRange);
                } else {
                    boolean eventRangeHigher = (intersection
                            .upperEndpoint() < eventRange.lowerEndpoint());
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
                        (eventRangeHigher ? span.lowerEndpoint()
                                : eventRange.upperEndpoint()),
                        (eventRangeHigher ? eventRange.lowerEndpoint()
                                : span.upperEndpoint()));
            }
        }

        /*
         * If there is an intersection, use the existing selected time if the
         * latter intersects with the intersection; otherwise, use the lower
         * bound of the intersection. If instead there is a minimum range, use
         * the existing selected time if the latter encloses the former,
         * otherwise, use the span.
         */
        SelectedTime selectedTime = this.selectedTime;
        if (intersection != null) {
            if (intersection.isConnected(selectedTime.getRange()) == false) {
                selectedTime = new SelectedTime(
                        selectedTime.getLowerBound() > intersection
                                .upperEndpoint() ? intersection.upperEndpoint()
                                        : intersection.lowerEndpoint());
            }
        } else {
            if (selectedTime.getRange().encloses(span) == false) {
                selectedTime = new SelectedTime(span.lowerEndpoint(),
                        span.upperEndpoint());
            }
        }

        /*
         * Ensure the selected time is appropriate given the time resolution.
         * This only requires work if the time resolution is minute-level. In
         * that case, if the lower bound is not on a minute boundary, round it
         * down to the nearest minute; and/or if the upper bound is not on a
         * minute boundary, round it up to the nearest minute.
         */
        if (timeResolution == TimeResolution.MINUTES) {
            boolean changed = false;

            long lowerBoundMillis = selectedTime.getLowerBound();
            Date lowerBound = new Date(lowerBoundMillis);
            Date truncatedLowerBound = truncateDateForTimeResolution(lowerBound,
                    timeResolution);
            if (lowerBound.equals(truncatedLowerBound) == false) {
                lowerBoundMillis = truncatedLowerBound.getTime();
                changed = true;
            }

            long upperBoundMillis = selectedTime.getUpperBound();
            Date upperBound = new Date(upperBoundMillis);
            Date truncatedUpperBound = truncateDateForTimeResolution(upperBound,
                    timeResolution);
            if (upperBound.equals(truncatedUpperBound) == false) {
                upperBoundMillis = truncatedUpperBound.getTime()
                        + UNIT_IN_MILLISECONDS_FOR_TIME_RESOLUTION
                                .get(timeResolution);
                changed = true;
            }

            if (changed) {
                selectedTime = new SelectedTime(lowerBoundMillis,
                        upperBoundMillis);
            }

        }
        return selectedTime;
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
