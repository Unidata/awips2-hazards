/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;

/**
 * Description: Superclass from which to derive managers of the display of
 * countdown timers. Instances of subclasses are used to track which alerts
 * require countdown timer displays, and when to update said displays. The
 * parameter <code>H</code> specifies the subclass of alerts for which countdown
 * timers are to be managed, while the parameter <code>P</code> indicates the
 * subclass of display properties that will be created by the manager in order
 * to facilitate countdown timer display painting.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 22, 2013    1936    Chris.Golden      Initial creation
 * Jun 18, 2015  7307      Chris.Cody  Added Hazard End time for requested Time Remaining calculation
 * Aug 06, 2015  9968      Chris.Cody  Minor change to continue count down until Expiration Time
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class CountdownTimersDisplayManager<H extends HazardEventExpirationAlert, P extends CountdownTimerDisplayProperties> {

    // Private Static Constants

    /**
     * Text to be displayed if no countdown timer exists for a given event.
     */
    private static final String NO_TIMER_TEXT = "";

    /**
     * Blink interval in milliseconds.
     */
    private static final long BLINK_INTERVAL = 1000L;

    // Public Enumerated Types

    /**
     * Types of updates required for a given countdown timer display.
     */
    public enum UpdateType {
        TEXT, COLOR, TEXT_AND_COLOR;
    }

    // Private Enumerated Types

    /**
     * Enumeration of all the possible string formats used to describe time
     * deltas.
     */
    private enum TimeDeltaStringFormat {

        // Values

        DAYS(TimeUnit.DAYS.toMillis(1L), TimeUnit.DAYS.toMillis(1L)), HOURS_AND_MINUTES(
                TimeUnit.HOURS.toMillis(1L), TimeUnit.MINUTES.toMillis(1L)), MINUTES(
                TimeUnit.MINUTES.toMillis(1L), TimeUnit.MINUTES.toMillis(1L)), SECONDS(
                1L, TimeUnit.SECONDS.toMillis(1L)), ZERO(0L, TimeUnit.MINUTES
                .toMillis(1L)), BLANK(Long.MIN_VALUE, Long.MAX_VALUE);

        // Private Variables

        /**
         * Lower bound (exclusive) in milliseconds for the use of this format.
         */
        private final long lowerBoundMillis;

        /**
         * Interval in milliseconds between text changes for this format.
         */
        private final long textChangeIntervalMillis;

        // Public Static Methods

        /**
         * Determine which time delta string format is suited to the specified
         * time delta.
         * 
         * @param timeDelta
         *            Time delta in milliseconds for which to fetch the string
         *            format.
         * @return Time delta string format appropriate to the given time delta.
         */
        public static TimeDeltaStringFormat getStringFormatForTimeDelta(
                long timeDelta) {
            for (TimeDeltaStringFormat value : values()) {
                if (timeDelta >= value.lowerBoundMillis) {
                    return value;
                }
            }
            return null;
        }

        /**
         * Convert the specified time delta in milliseconds to a formatted
         * descriptive string.
         * 
         * @param timeDelta
         *            Time delta in milliseconds.
         * @return Formatted descriptive string.
         */
        public static String getTimeDeltaString(long timeDelta) {
            switch (getStringFormatForTimeDelta(timeDelta)) {
            case DAYS:
                long days = TimeUnit.MILLISECONDS.toDays(timeDelta);
                return days + "-" + (days + 1) + " days";
            case HOURS_AND_MINUTES:
                long hours = TimeUnit.MILLISECONDS.toHours(timeDelta);
                return hours
                        + " "
                        + (hours == 1L ? "hr" : "hrs")
                        + " "
                        + TimeUnit.MILLISECONDS.toMinutes(timeDelta
                                - TimeUnit.HOURS.toMillis(hours)) + " min";
            case MINUTES:
                return TimeUnit.MILLISECONDS.toMinutes(timeDelta) + " min";
            case SECONDS:
                return String.format("00:00:%02d",
                        TimeUnit.MILLISECONDS.toSeconds(timeDelta));
            case ZERO:
                return "00:00:00";
            case BLANK:
            default:
                return NO_TIMER_TEXT;
            }
        }

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param lowerBoundMillis
         *            Lower bound (exclusive) in milliseconds for use of this
         *            format.
         * @param textChangeIntervalMillis
         *            Interval in milliseconds between text changes for this
         *            format.
         */
        private TimeDeltaStringFormat(long lowerBoundMillis,
                long textChangeIntervalMillis) {
            this.lowerBoundMillis = lowerBoundMillis;
            this.textChangeIntervalMillis = textChangeIntervalMillis;
        }

        // Public Methods

        /**
         * Get the interval in milliseconds between text changes for this
         * format.
         * 
         * @return Interval in milliseconds between text changes for this
         *         format.
         */
        public long getTextChangeIntervalInMillis() {
            return textChangeIntervalMillis;
        }
    }

    // Private Variables

    /**
     * Class of hazard alerts for which this manager will be handling countdown
     * timers.
     */
    private final Class<H> hazardAlertClass;

    /**
     * Countdown timers display listener.
     */
    private final CountdownTimersDisplayListener listener;

    /**
     * Flag indicating whether CAVE time is currently frozen or not.
     */
    private boolean timeIsFrozen;

    /**
     * Map of event identifiers to the display properties to be used for any
     * countdown timer displays for those events.
     */
    private final Map<String, P> countdownTimerDisplayPropertiesForEventIdentifiers = Maps
            .newHashMap();

    /**
     * Map of event identifiers to associated alerts, for those events that have
     * alerts.
     */
    private final Map<String, H> alertsForEventIdentifiers = Maps.newHashMap();

    /**
     * Map of alerts to the next epoch time (in milliseconds) at which their
     * associated timers' text, if any, needs updating.
     */
    private final Map<H, Long> updateTimesForActiveAlerts = Maps.newHashMap();

    /**
     * Simulated time change listener.
     */
    private final ISimulatedTimeChangeListener simulatedTimeChangeListener = new ISimulatedTimeChangeListener() {
        @Override
        public void timechanged() {
            timeIsFrozen = SimulatedTime.getSystemTime().isFrozen();
            Display display = Display.getCurrent();
            if (display != null) {
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        updateActiveAlerts(ImmutableList.copyOf(Lists
                                .newArrayList(alertsForEventIdentifiers
                                        .values())));
                        listener.allCountdownTimerDisplaysChanged(CountdownTimersDisplayManager.this);
                    }
                });
            }
        }
    };

    /**
     * Executor of the update-countdown-timers task.
     */
    private final Runnable updateCountdownTimersExecutor = new Runnable() {
        @Override
        public void run() {
            listener.countdownTimerDisplaysChanged(CountdownTimersDisplayManager.this);
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param hazardAlertClass
     *            Class of hazard alerts for which the manager will be handling
     *            countdown timers.
     * @param listener
     *            Listener for countdown timer display notifications.
     */
    public CountdownTimersDisplayManager(Class<H> hazardAlertClass,
            CountdownTimersDisplayListener listener) {
        this.hazardAlertClass = hazardAlertClass;
        this.listener = listener;

        // Ensure that this object gets notifications of simulated
        // time changes, and determine whether or not the current
        // time is frozen.
        SimulatedTime time = SimulatedTime.getSystemTime();
        timeIsFrozen = time.isFrozen();
        time.addSimulatedTimeChangeListener(simulatedTimeChangeListener);
    }

    // Public Methods

    /**
     * Dispose of this manager.
     */
    public void dispose() {

        // Cancel any pending timer-based update of countdown timers.
        Display.getCurrent().timerExec(-1, updateCountdownTimersExecutor);

        // Remove the time change listener.
        SimulatedTime.getSystemTime().removeSimulatedTimeChangeListener(
                simulatedTimeChangeListener);
    }

    /**
     * Get the display properties for the countdown timer that go with the
     * specified event identifier at this particular instant in time.
     * <p>
     * <strong>NOTE</strong>: The object returned should be used immediately for
     * drawing purposes; it should never be cached.
     * 
     * @param eventId
     *            Event identifier for which to fetch the countdown timer
     *            display properties.
     * @return Display properties for the specified countdown timer, or
     *         <code>null</code> if no countdown timer exists for the specified
     *         event identifier.
     */
    public final P getDisplayPropertiesForEvent(String eventId) {
        P properties = countdownTimerDisplayPropertiesForEventIdentifiers
                .get(eventId);
        if (properties == null) {
            return null;
        }

        // Set the flag indicating whether the primary or secondary color is
        // to be used as the foreground. If it is not blinking, the primary
        // color is always the foreground color. Otherwise, the current time
        // is used to determine whether the primary color is the foreground or
        // or not, to simulate blinking. If CAVE time is currently frozen, the
        // current system time is used, since the CAVE time will not tick
        // forward.
        properties
                .setPrimaryForeground(!properties.isBlinking()
                        || ((timeIsFrozen ? System.currentTimeMillis()
                                : SimulatedTime.getSystemTime().getMillis())
                                % (2L * BLINK_INTERVAL) < BLINK_INTERVAL));
        return properties;
    }

    /**
     * Get the text for the countdown timer associated with the specified event
     * identifier, or an empty string if none.
     * 
     * @param eventId
     *            Event identifier for which to fetch the countdown timer text.
     * @return Text to be displayed for the countdown identifier, or
     *         <code>null>/code> if there is none to be shown.
     */
    public final String getTextForEvent(String eventId) {
        H alert = alertsForEventIdentifiers.get(eventId);
        if (alert != null) {
            return TimeDeltaStringFormat
                    .getTimeDeltaString(getTimeDeltaUntilAlertExpiration(alert,
                            getCurrentTimeInMillis()));
        }
        return NO_TIMER_TEXT;
    }

    /**
     * Get the expiration time as an epoch time in milliseconds at which the
     * alert associated with the specified event identifier is to expire.
     * 
     * @param eventId
     *            Event identifier for which to fetch the expiration time.
     * @return Expiration time when the associated alert is to expire, or
     *         <code>Long.MAX_VALUE</code> if there is no expiration time.
     */
    public final long getAlertExpirationTimeForEvent(String eventId) {
        H alert = alertsForEventIdentifiers.get(eventId);
        if (alert != null) {
            return alert.getHazardExpiration().getTime();
        }
        return Long.MAX_VALUE;
    }

    /**
     * Get the END time as an epoch time in milliseconds at which the alert
     * associated with the specified event identifier is to END.
     * 
     * @param eventId
     *            Event identifier for which to fetch the END time.
     * @return END time when the associated alert is to END, or
     *         <code>Long.MAX_VALUE</code> if there is no END time.
     */
    public final long getAlertEndTimeForEvent(String eventId) {
        H alert = alertsForEventIdentifiers.get(eventId);
        if (alert != null) {
            return alert.getHazardEnd().getTime();
        }
        return Long.MAX_VALUE;
    }

    /**
     * Update the display properties for the countdown timer associated with the
     * specified event identifier, if any.
     * 
     * @param eventId
     *            Event identifier for which to update an associated countdown
     *            timer's display properties, if any.
     * @param baseFont
     *            Base font to be used for deriving fonts that will be used as
     *            properties of the countdown timer, if necessary.
     */
    public final void updateDisplayPropertiesForEvent(String eventId,
            Font baseFont) {
        H alert = alertsForEventIdentifiers.get(eventId);
        if ((alert != null)
                && (countdownTimerDisplayPropertiesForEventIdentifiers
                        .get(eventId) == null)) {
            countdownTimerDisplayPropertiesForEventIdentifiers.put(eventId,
                    generateDisplayPropertiesForEvent(alert));
        }
    }

    /**
     * Update the currently active alerts.
     * 
     * @param activeAlerts
     *            Currently active alerts.
     */
    public final void updateActiveAlerts(
            ImmutableList<? extends IHazardAlert> activeAlerts) {

        // Clear the maps of alerts, update times, and countdown timer
        // display properties.
        alertsForEventIdentifiers.clear();
        updateTimesForActiveAlerts.clear();
        countdownTimerDisplayPropertiesForEventIdentifiers.clear();

        // Iterate through the active alerts, finding any that are of the
        // appropriate types and associating each of these with their
        // corresponding events, as well as getting the next update time
        // for these alerts.
        long currentTimeMillis = getCurrentTimeInMillis();
        for (IHazardAlert activeAlert : activeAlerts) {

            // Skip the alert if it is not of the appropriate type.
            if (hazardAlertClass.isAssignableFrom(activeAlert.getClass()) == false) {
                continue;
            }

            // Associate the alert with its event.
            @SuppressWarnings("unchecked")
            H alert = (H) activeAlert;
            alertsForEventIdentifiers.put(alert.getEventID(), alert);

            // Associate the alert with the next time it needs to have its
            // associated countdown timer text updated, if any.
            updateTimesForActiveAlerts.put(alert,
                    getNextUpdateTimeForAlert(alert, currentTimeMillis));
        }
    }

    /**
     * Determine which of the countdown timers need to be updated, refreshing
     * the display update times for each that do need an update, so that each
     * such timer has a corresponding display update time that indicates when it
     * next must be redrawn.
     * 
     * @return Map of event identifiers that need updating to indicators of what
     *         they need updated.
     */
    public final Map<String, UpdateType> getEventsNeedingUpdateAndRefreshRedrawTimes() {

        // Iterate through the alerts, determining for each if its text
        // needs updating, and/or if it is blinking. If the former, then
        // calculate its next refresh time. If either the text needs up-
        // dating or the timer is blinking, make an entry in the map to
        // indicate what needs updating.
        Map<String, UpdateType> updateTypesForEventIdentifiers = Maps
                .newHashMap();
        long currentTimeMillis = getCurrentTimeInMillis();
        for (H alert : alertsForEventIdentifiers.values()) {
            boolean updateText = (updateTimesForActiveAlerts.get(alert) <= currentTimeMillis);
            if (!updateText && !alert.isBlinking()) {
                continue;
            }
            if (updateText) {
                updateTimesForActiveAlerts.put(alert,
                        getNextUpdateTimeForAlert(alert, currentTimeMillis));
            }
            updateTypesForEventIdentifiers.put(alert.getEventID(), (updateText
                    && alert.isBlinking() ? UpdateType.TEXT_AND_COLOR
                    : (updateText ? UpdateType.TEXT : UpdateType.COLOR)));
        }
        return updateTypesForEventIdentifiers;
    }

    /**
     * Refresh the display update times for each of the countdown timers, so
     * that each such timer has a corresponding display update time that
     * indicates when it next must be redrawn.
     */
    public void refreshAllRedrawTimes() {

        // Determine the next refresh time for each of the alerts,
        // and schedule the next update of appropriate countdown
        // timer displays if there is anything to update in the fu-
        // ture.
        long currentTimeMillis = getCurrentTimeInMillis();
        for (H alert : alertsForEventIdentifiers.values()) {
            updateTimesForActiveAlerts.put(alert,
                    getNextUpdateTimeForAlert(alert, currentTimeMillis));
        }
    }

    /**
     * Schedule the next update of appropriate countdown timer displays if there
     * is any reason to update in the future.
     * 
     * @param eventIdentifiers
     *            List of event identifiers. Alerts will be ignored if their
     *            event identifiers are not found within this list.
     */
    public void scheduleNextDisplayUpdate(List<String> eventIdentifiers) {

        // Schedule the next invocation of the method if there
        // is anything to be updated.
        int timeDelta = getTimeDeltaBeforeNextDisplayUpdate(eventIdentifiers);
        if (timeDelta != -1) {
            Display.getCurrent().timerExec(timeDelta,
                    updateCountdownTimersExecutor);
        }
    }

    // Protected Methods

    /**
     * Generate the display properties for the countdown timer associated with
     * the specified event alert.
     * 
     * @param alert
     *            Event alert.
     * @return Display properties for the associated countdown timer.
     */
    protected abstract P generateDisplayPropertiesForEvent(H alert);

    // Private Methods

    /**
     * Get the current CAVE time in milliseconds.
     * 
     * @return Current cave time in milliseconds.
     */
    private long getCurrentTimeInMillis() {
        return SimulatedTime.getSystemTime().getMillis();
    }

    /**
     * Get the time remaining (in millis) for the Hazard Event for the alert.
     * 
     * Check to see if the Hazard Event for the Alert has Ended. If it has not
     * ended, then get the time delta, in milliseconds, between the current CAVE
     * time and the expiration time for the specified alert.
     * 
     * @param alert
     *            Alert for which the delta is to be calculated.
     * @param currentTimeMillis
     *            Current CAVE time in milliseconds.
     * @return Time delta in milliseconds.
     */
    private long getTimeDeltaUntilAlertExpiration(H alert,
            long currentTimeMillis) {
        // Place endTimeMillis on the other side of the minute to compensate for
        // rounding
        long expireTimeMillis = alert.getHazardExpiration().getTime()
                + TimeUnit.MINUTES.toMillis(1L);
        if ((expireTimeMillis - currentTimeMillis) >= 0) {
            long delta = expireTimeMillis - currentTimeMillis;
            if (delta >= 0L) {
                return (delta);
            }
        }
        return (-1L);
    }

    /**
     * Get the next time, as epoch time in milliseconds, that the specified
     * alert's associated countdown timer (if any) needs its text updated.
     * 
     * @param alert
     *            Alert for which the update time is to be calculated.
     * @param currentTimeMillis
     *            Current CAVE time in milliseconds.
     * @return Update time, as epoch time in milliseconds, or
     *         <code>Long.MAX_VALUE</code> if no update is needed.
     */
    private long getNextUpdateTimeForAlert(H alert, long currentTimeMillis) {
        if (timeIsFrozen) {
            return Long.MAX_VALUE;
        }
        long baseTimeDelta = TimeDeltaStringFormat.getStringFormatForTimeDelta(
                getTimeDeltaUntilAlertExpiration(alert, currentTimeMillis))
                .getTextChangeIntervalInMillis();
        if (baseTimeDelta == Long.MAX_VALUE) {
            return baseTimeDelta;
        }
        long timeDelta = (alert.getHazardExpiration().getTime() - currentTimeMillis)
                % baseTimeDelta;
        return currentTimeMillis + timeDelta;
    }

    /**
     * Get the time delta in milliseconds indicating how much of an interval
     * should exist before the next update to the countdown timer displays.
     * 
     * @param eventIdentifiers
     *            List of event identifiers. Alerts will be ignored if their
     *            event identifiers are not found within this list.
     * @return Time delta in milliseconds, or <code>-1</code> if no update is
     *         needed.
     */
    private int getTimeDeltaBeforeNextDisplayUpdate(
            List<String> eventIdentifiers) {

        // Compile a list of active alerts that have corresponding events
        // in the provided list. At the same time, determine if any of these
        // alerts are blinking.
        List<H> activeAlerts = Lists.newArrayList();
        boolean blinking = false;
        for (H alert : alertsForEventIdentifiers.values()) {
            if (eventIdentifiers.contains(alert.getEventID())) {
                activeAlerts.add(alert);
                if (alert.isBlinking()) {
                    blinking = true;
                }
            }
        }

        // Return a result differently depending upon whether CAVE time is
        // currently frozen.
        if (SimulatedTime.getSystemTime().isFrozen()) {

            // Return a blink interval if blinking is occurring; otherwise,
            // return -1.
            if (blinking) {
                return (int) BLINK_INTERVAL;
            }
            return -1;
        } else {

            // Determine the earliest update time required of all the count-
            // down timers.
            long nextUpdate = Long.MAX_VALUE;
            for (HazardEventExpirationAlert alert : activeAlerts) {
                long thisNextUpdate = updateTimesForActiveAlerts.get(alert);
                if (thisNextUpdate < nextUpdate) {
                    nextUpdate = thisNextUpdate;
                }
            }
            long interval = Math.max(0L, nextUpdate - getCurrentTimeInMillis());

            // If blinking is occurring, calculate the actual interval as
            // the remainder of dividing the current CAVE time by the blink
            // interval if the interval calculated above is significantly
            // larger than the blink interval. Since these calculations may
            // yield 0, just use 1 millisecond in such cases, since it may
            // mean that the update that just occurred happened slightly too
            // soon and thus the timers weren't updated properly.
            int result;
            if (blinking && (interval > (BLINK_INTERVAL * 5L) / 4L)) {
                result = (int) (getCurrentTimeInMillis() % BLINK_INTERVAL);
            } else {
                if (interval > Integer.MAX_VALUE) {
                    result = Integer.MAX_VALUE;
                } else {
                    result = (int) (interval);
                }
            }
            return Math.max(1, result);
        }
    }
}
