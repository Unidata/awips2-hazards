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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;

/**
 * Description: Superclass from which to derive managers of the display of
 * countdown timers. Instances of subclasses are used to track which events
 * require countdown timer displays, and when to update said displays. The
 * generic parameter <code>C</code> specifies the subclass of countdown timers
 * to be managed, while <code>P</code> indicates the subclass of display
 * properties that will be created by the manager in order to facilitate
 * countdown timer display painting.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 22, 2013    1936    Chris.Golden Initial creation.
 * Jun 18, 2015    7307    Chris.Cody   Added Hazard End time for requested
 *                                      Time Remaining calculation.
 * Aug 06, 2015    9968    Chris.Cody   Minor change to continue count down
 *                                      until Expiration Time.
 * Feb 01, 2017   15556    Chris.Golden Changed to remove dependencies upon
 *                                      session manager code, making this
 *                                      more view-appropriate in the MVP
 *                                      context.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class CountdownTimersDisplayManager<C extends CountdownTimer, P extends CountdownTimerDisplayProperties> {

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
     * Class of the countdown timers being managed.
     */
    private final Class<C> countdownTimerClass;

    /**
     * Countdown timers display listener.
     */
    private final ICountdownTimersDisplayListener listener;

    /**
     * Map of event identifiers to the display properties to be used for any
     * countdown timer displays for those events.
     */
    private final Map<String, P> countdownTimerDisplayPropertiesForEventIdentifiers = new HashMap<>();

    /**
     * Map of event identifiers to associated expiration times.
     */
    private final Map<String, C> countdownTimersForEventIdentifiers = new HashMap<>();

    /**
     * Map of event identifiers to the next epoch time (in milliseconds) at
     * which their associated timers' text, if any, needs updating.
     */
    private final Map<String, Long> updateTimesForEventIdentifiers = new HashMap<>();

    /**
     * Flag indicating whether CAVE time is currently frozen or not.
     */
    private boolean timeIsFrozen;

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
                        updateCountdownTimers(ImmutableMap
                                .copyOf(countdownTimersForEventIdentifiers));
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
     * @param countdownTimerClass
     *            Class of countdown timers that is to be managed.
     * @param listener
     *            Listener for countdown timer display notifications.
     */
    public CountdownTimersDisplayManager(Class<C> countdownTimerClass,
            ICountdownTimersDisplayListener listener) {
        this.countdownTimerClass = countdownTimerClass;
        this.listener = listener;

        /*
         * Ensure that this object gets notifications of simulated time changes,
         * and determine whether or not the current time is frozen.
         */
        SimulatedTime time = SimulatedTime.getSystemTime();
        timeIsFrozen = time.isFrozen();
        time.addSimulatedTimeChangeListener(simulatedTimeChangeListener);
    }

    // Public Methods

    /**
     * Dispose of this manager.
     */
    public void dispose() {

        /*
         * Cancel any pending timer-based update of countdown timers.
         */
        Display.getCurrent().timerExec(-1, updateCountdownTimersExecutor);

        /*
         * Remove the time change listener.
         */
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
     * @param eventIdentifier
     *            Event identifier for which to fetch the countdown timer
     *            display properties.
     * @return Display properties for the specified countdown timer, or
     *         <code>null</code> if no countdown timer exists for the specified
     *         event identifier.
     */
    public final P getDisplayPropertiesForEvent(String eventIdentifier) {
        P properties = countdownTimerDisplayPropertiesForEventIdentifiers
                .get(eventIdentifier);
        if (properties == null) {
            return null;
        }

        /*
         * Set the flag indicating whether the primary or secondary color is to
         * be used as the foreground. If it is not blinking, the primary color
         * is always the foreground color. Otherwise, the current time is used
         * to determine whether the primary color is the foreground or or not,
         * to simulate blinking. If CAVE time is currently frozen, the current
         * system time is used, since the CAVE time will not tick forward.
         */
        properties
                .setPrimaryForeground((properties.isBlinking() == false)
                        || ((timeIsFrozen ? System.currentTimeMillis()
                                : SimulatedTime.getSystemTime().getMillis())
                                % (2L * BLINK_INTERVAL) < BLINK_INTERVAL));
        return properties;
    }

    /**
     * Get the text for the countdown timer associated with the specified event
     * identifier, or an empty string if none.
     * 
     * @param eventIdentifier
     *            Event identifier for which to fetch the countdown timer text.
     * @return Text to be displayed for the countdown timer, or
     *         <code>null>/code> if there is none to be shown.
     */
    public final String getTextForEvent(String eventIdentifier) {
        CountdownTimer countdownTimer = countdownTimersForEventIdentifiers
                .get(eventIdentifier);
        if (countdownTimer != null) {
            return TimeDeltaStringFormat
                    .getTimeDeltaString(getTimeDeltaUntilCountdownTimerExpiration(
                            countdownTimer.getExpireTime(),
                            getCurrentTimeInMillis()));
        }
        return NO_TIMER_TEXT;
    }

    /**
     * Get the expiration time as an epoch time in milliseconds at which the
     * countdown timer associated with the specified event identifier is to
     * expire.
     * 
     * @param eventIdentifier
     *            Event identifier for which to fetch the expiration time.
     * @return Expiration time when the associated countdown timer is to expire,
     *         or <code>Long.MAX_VALUE</code> if there is no expiration time.
     */
    public final long getCountdownExpirationTimeForEvent(String eventIdentifier) {
        CountdownTimer countdownTimer = countdownTimersForEventIdentifiers
                .get(eventIdentifier);
        if (countdownTimer != null) {
            return countdownTimer.getExpireTime().getTime();
        }
        return Long.MAX_VALUE;
    }

    /**
     * Update the display properties for the countdown timer associated with the
     * specified event identifier, if any.
     * 
     * @param eventIdentifier
     *            Event identifier for which to update an associated countdown
     *            timer's display properties, if any.
     * @param baseFont
     *            Base font to be used for deriving fonts that will be used as
     *            properties of the countdown timer, if necessary.
     */
    public final void updateDisplayPropertiesForEvent(String eventIdentifier,
            Font baseFont) {
        C countdownTimer = countdownTimersForEventIdentifiers
                .get(eventIdentifier);
        if ((countdownTimer != null)
                && (countdownTimerDisplayPropertiesForEventIdentifiers
                        .get(eventIdentifier) == null)) {
            countdownTimerDisplayPropertiesForEventIdentifiers.put(
                    eventIdentifier,
                    generateDisplayPropertiesForCountdownTimer(countdownTimer));
        }
    }

    /**
     * Update the currently active countdown timers.
     * 
     * @param countdownTimersForEventIdentifiers
     *            Map of event identifiers to their countdown timers for any
     *            events that have such.
     */
    @SuppressWarnings("unchecked")
    public final void updateCountdownTimers(
            ImmutableMap<String, ? extends CountdownTimer> countdownTimersForEventIdentifiers) {

        /*
         * Clear the maps of countdown timers, update times, and countdown timer
         * display properties.
         */
        this.countdownTimersForEventIdentifiers.clear();
        updateTimesForEventIdentifiers.clear();
        countdownTimerDisplayPropertiesForEventIdentifiers.clear();

        /*
         * Iterate through the countdown timers, finding any that are of the
         * appropriate types and associating each of these with their
         * corresponding events, as well as getting the next update time for
         * these alerts.
         */
        long currentTimeMillis = getCurrentTimeInMillis();
        for (Map.Entry<String, ? extends CountdownTimer> entry : countdownTimersForEventIdentifiers
                .entrySet()) {

            /*
             * Skip the alert if it is not of the appropriate type.
             */
            if (countdownTimerClass.isAssignableFrom(entry.getValue()
                    .getClass()) == false) {
                continue;
            }

            /*
             * Associate the alert with its event.
             */
            C countdownTimer = (C) entry.getValue();
            this.countdownTimersForEventIdentifiers.put(entry.getKey(),
                    countdownTimer);

            /*
             * Associate the countdown timer with the next time it needs to have
             * its text updated, if any.
             */
            updateTimesForEventIdentifiers.put(
                    entry.getKey(),
                    getNextUpdateTimeForCountdownTimer(countdownTimer,
                            currentTimeMillis));
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

        /*
         * Iterate through the countdown timers, determining for each if its
         * text needs updating, and/or if it is blinking. If the former, then
         * calculate its next refresh time. If either the text needs updating or
         * the timer is blinking, make an entry in the map to indicate what
         * needs updating.
         */
        Map<String, UpdateType> updateTypesForEventIdentifiers = new HashMap<>();
        long currentTimeMillis = getCurrentTimeInMillis();
        for (Map.Entry<String, C> entry : countdownTimersForEventIdentifiers
                .entrySet()) {
            boolean updateText = (updateTimesForEventIdentifiers.get(entry
                    .getKey()) <= currentTimeMillis);
            if ((updateText == false)
                    && (entry.getValue().isBlinking() == false)) {
                continue;
            }
            if (updateText) {
                updateTimesForEventIdentifiers.put(
                        entry.getKey(),
                        getNextUpdateTimeForCountdownTimer(entry.getValue(),
                                currentTimeMillis));
            }
            updateTypesForEventIdentifiers
                    .put(entry.getKey(),
                            (updateText && entry.getValue().isBlinking() ? UpdateType.TEXT_AND_COLOR
                                    : (updateText ? UpdateType.TEXT
                                            : UpdateType.COLOR)));
        }
        return updateTypesForEventIdentifiers;
    }

    /**
     * Refresh the display update times for each of the countdown timers, so
     * that each such timer has a corresponding display update time that
     * indicates when it next must be redrawn.
     */
    public void refreshAllRedrawTimes() {

        /*
         * Determine the next refresh time for each of the countdown timers, and
         * schedule the next update of appropriate countdown timer displays if
         * there is anything to update in the future.
         */
        long currentTimeMillis = getCurrentTimeInMillis();
        for (Map.Entry<String, C> entry : countdownTimersForEventIdentifiers
                .entrySet()) {
            updateTimesForEventIdentifiers.put(
                    entry.getKey(),
                    getNextUpdateTimeForCountdownTimer(entry.getValue(),
                            currentTimeMillis));
        }
    }

    /**
     * Schedule the next update of appropriate countdown timer displays if there
     * is any reason to update in the future.
     * 
     * @param eventIdentifiers
     *            List of event identifiers. Countdown timers will be ignored if
     *            their event identifiers are not found within this list.
     */
    public void scheduleNextDisplayUpdate(List<String> eventIdentifiers) {

        /*
         * Schedule the next invocation of the method if there is anything to be
         * updated.
         */
        int timeDelta = getTimeDeltaBeforeNextDisplayUpdate(eventIdentifiers);
        if (timeDelta != -1) {
            Display.getCurrent().timerExec(timeDelta,
                    updateCountdownTimersExecutor);
        }
    }

    // Protected Methods

    /**
     * Generate the display properties for the countdown timer associated with
     * the specified countdown timer.
     * 
     * @param countdownTimer
     *            Countdown timer.
     * @return Display properties for the associated countdown timer.
     */
    protected abstract P generateDisplayPropertiesForCountdownTimer(
            C countdownTimer);

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
     * Get the time remaining in milliseconds for the hazard event for the
     * countdown timer.
     * 
     * @param expireTime
     *            Time at which the countdown timer is to expire.
     * @param currentTimeMillis
     *            Current CAVE time in milliseconds.
     * @return Time delta in milliseconds.
     */
    private long getTimeDeltaUntilCountdownTimerExpiration(Date expireTime,
            long currentTimeMillis) {

        /*
         * Add a minute to the expiration time to compensate for rounding.
         * 
         * TODO: Is this needed? Shouldn't the code be changed elsewhere to
         * ensure that expiration time is always on a second or minute boundary
         * (as appropriate for the time resolution of the particular hazard
         * event)?
         */
        long expireTimeMillis = expireTime.getTime()
                + TimeUnit.MINUTES.toMillis(1L);

        /*
         * If the expiration time is later than the current time, return the
         * delta; otherwise return -1 to indicate that the expiration time has
         * passed.
         */
        long delta = expireTimeMillis - currentTimeMillis;
        return (delta >= 0 ? delta : -1L);
    }

    /**
     * Get the next time, as epoch time in milliseconds, that the specified
     * countdown timer (if any) needs its text updated.
     * 
     * @param countdownTimer
     *            Countdown timer for which the update time is to be calculated.
     * @param currentTimeMillis
     *            Current CAVE time in milliseconds.
     * @return Update time, as epoch time in milliseconds, or
     *         <code>Long.MAX_VALUE</code> if no update is needed.
     */
    private long getNextUpdateTimeForCountdownTimer(C countdownTimer,
            long currentTimeMillis) {
        if (timeIsFrozen) {
            return Long.MAX_VALUE;
        }
        long baseTimeDelta = TimeDeltaStringFormat.getStringFormatForTimeDelta(
                getTimeDeltaUntilCountdownTimerExpiration(
                        countdownTimer.getExpireTime(), currentTimeMillis))
                .getTextChangeIntervalInMillis();
        if (baseTimeDelta == Long.MAX_VALUE) {
            return baseTimeDelta;
        }
        long timeDelta = (countdownTimer.getExpireTime().getTime() - currentTimeMillis)
                % baseTimeDelta;
        return currentTimeMillis + timeDelta;
    }

    /**
     * Get the time delta in milliseconds indicating how much of an interval
     * should exist before the next update to the countdown timer displays.
     * 
     * @param eventIdentifiers
     *            List of event identifiers. Countdown timers will be ignored if
     *            their event identifiers are not found within this list.
     * @return Time delta in milliseconds, or <code>-1</code> if no update is
     *         needed.
     */
    private int getTimeDeltaBeforeNextDisplayUpdate(
            List<String> eventIdentifiers) {

        /*
         * Compile a set that is the intersection of the passed in event
         * identifiers and those which are associated with countdown timers. At
         * the same time, determine if any of the subset's associated countdown
         * timers are blinking.
         */
        Set<String> activeEventIdentifiers = new HashSet<>(
                countdownTimersForEventIdentifiers.size(), 1.0f);
        boolean blinking = false;
        for (Map.Entry<String, C> entry : countdownTimersForEventIdentifiers
                .entrySet()) {
            if (eventIdentifiers.contains(entry.getKey())) {
                activeEventIdentifiers.add(entry.getKey());
                if (entry.getValue().isBlinking()) {
                    blinking = true;
                }
            }
        }

        /*
         * Return a result differently depending upon whether CAVE time is
         * currently frozen.
         */
        if (SimulatedTime.getSystemTime().isFrozen()) {

            /*
             * Return a blink interval if blinking is occurring; otherwise,
             * return -1.
             */
            if (blinking) {
                return (int) BLINK_INTERVAL;
            }
            return -1;
        } else {

            /*
             * Determine the earliest update time required of all the countdown
             * timers.
             */
            long nextUpdate = Long.MAX_VALUE;
            for (String eventIdentifier : activeEventIdentifiers) {
                long thisNextUpdate = updateTimesForEventIdentifiers
                        .get(eventIdentifier);
                if (thisNextUpdate < nextUpdate) {
                    nextUpdate = thisNextUpdate;
                }
            }
            long interval = Math.max(0L, nextUpdate - getCurrentTimeInMillis());

            /*
             * If blinking is occurring, calculate the actual interval as the
             * remainder of dividing the current CAVE time by the blink interval
             * if the interval calculated above is significantly larger than the
             * blink interval. Since these calculations may yield 0, just use 1
             * millisecond in such cases, since it may mean that the update that
             * just occurred happened slightly too soon and thus the timers
             * weren't updated properly.
             */
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
