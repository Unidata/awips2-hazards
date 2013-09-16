/**
 * 
 */
package gov.noaa.gsd.common.hazards.utilities;

import java.util.HashMap;

/**
 * Description: General purpose timing used in performance testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 04, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class Timer {
    private final HashMap<String, Clock> timers = new HashMap<String, Clock>();

    /**
     * <p>
     * Turn on a timer
     * </p>
     * 
     * @param name
     *            of the timer
     */
    public void on(String name) {
        if (timers.get(name) == null) {
            timers.put(name, new Clock());
        } else if (timers.get(name).isOn()) {
        } else {
            timers.get(name).on();
        }
    }

    /**
     * <p>
     * Turn off a timer
     * </p>
     * 
     * @param name
     *            of the timer
     */
    public void off(String name) {
        if (timers.get(name) == null) {
        } else if (!timers.get(name).isOn()) {
        } else {
            timers.get(name).off();
        }
    }

    /**
     * <p>
     * Get the elapsed time for the named timer.
     * </p>
     * 
     * @param name
     * @return
     */
    public Double elapsedTime(String name) {
        if (timers.get(name) == null) {
            return Double.NaN;
        } else {
            return timers.get(name).elapsedTimeInSeconds();
        }
    }

    public int numCalls(String name) {
        try {
            int result = timers.get(name).numCalls();
            return result;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(String.format(
                    "%s never called ", name));
        }
    }

    public Double averageTime(String name) {
        try {
            Double result = timers.get(name).averageElapsedTimeInSeconds();
            return result;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(String.format(
                    "%s never called ", name));
        }
    }

    /**
     * <p>
     * "Pretty" string representation of the elapsed time
     * </p>
     * 
     * @param name
     *            timer for which elapsed time is desired
     * @return
     */
    public String elapsedTimeString(String name) {
        return name + ": " + elapsedTime(name) + "\n";
    }

    public String statsString(String name) {
        String result = String.format(
                "%s: elapsed time: %s, numCalls: %s, averageTime: %s", name,
                elapsedTime(name), numCalls(name), averageTime(name));
        return result;
    }

    /**
     * <p>
     * A version that prints the elapsed time with indentation so that nested
     * timer elapsed times will be cleanly displayed.
     * </p>
     * 
     * @param name
     * @param indentLevel
     * @return
     */
    public String elapsedTimeString(String name, Integer indentLevel) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            builder.append("  ");
        }
        builder.append(elapsedTimeString(name));
        return builder.toString();
    }

    public void clear() {
        for (String key : timers.keySet()) {
            Clock clock = timers.get(key);
            clock.clear();
        }
    }

    private static class Clock {
        private long startTime;

        private long elapsedTime;

        private boolean isOn = false;

        private int count = 0;

        private Clock() {
            on();
        }

        public void clear() {
            elapsedTime = 0;
        }

        private void on() {
            startTime = System.currentTimeMillis();
            isOn = true;
            count += 1;
        }

        private void off() {
            elapsedTime += System.currentTimeMillis() - startTime;
            isOn = false;
        }

        private Double elapsedTimeInSeconds() {

            return elapsedTime / 1000.0;

        }

        private int numCalls() {
            return count;
        }

        private Double averageElapsedTimeInSeconds() {
            return elapsedTimeInSeconds() / numCalls();
        }

        private boolean isOn() {
            return isOn;
        }
    }
}
