/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.time;

import com.google.common.collect.Range;
import com.raytheon.uf.common.time.TimeRange;

/**
 * Description: Selected time encapsulation, holding either a single instant in
 * time, or a closed, bounded range of times. This is used instead of a
 * {@link TimeRange} because the latter does not allow (a) single instants, and
 * (b) closed ranges.
 * <p>
 * This class is immutable.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Nov 18, 2014    4124    Chris.Golden Initial creation.
 * Mar 30, 2015    7272    mduff        Changes to support Guava upgrade.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SelectedTime {

    // Private Variables

    /**
     * Range backing this object.
     */
    private final Range<Long> range;

    // Public Constructors

    /**
     * Construct a standard instance representing an instant in time.
     * 
     * @param time
     *            Instant in time, as epoch time in milliseconds.
     */
    public SelectedTime(long time) {
        range = Range.closed(time, time);
    }

    /**
     * Construct a standard instance representing a range in time.
     * 
     * @param lowerTime
     *            Lower bound of the time range, as epoch time in milliseconds.
     * @param upperTime
     *            Upper bound of the time range, as epoch time in milliseconds.
     */
    public SelectedTime(long lowerTime, long upperTime) {
        range = Range.closed(lowerTime, upperTime);
    }

    // Public Methods

    /**
     * Determine if this object is equal to the specified object.
     * 
     * @param other
     *            Other object to which to compare.
     * @return True if the two objects are equivalent, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        return ((other instanceof SelectedTime) && range
                .equals(((SelectedTime) other).range));
    }

    /**
     * Get the hash code of this object.
     * 
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return range.hashCode();
    }

    /**
     * Get the string representation of this object.
     * 
     * @return String representation of this object.
     */
    @Override
    public String toString() {
        return range.toString();
    }

    /**
     * Get the lower bound of the selected time. This will yield the same result
     * as {@link #getUpperBound()} if the selected time is an instant.
     * 
     * @return Lower bound of the selected time.
     */
    public long getLowerBound() {
        return range.lowerEndpoint();
    }

    /**
     * Get the upper bound of the selected time. This will yield the same result
     * as {@link #getLowerBound()} if the selected time is an instant.
     * 
     * @return Upper bound of the selected time.
     */
    public long getUpperBound() {
        return range.upperEndpoint();
    }

    /**
     * Get the range for this selected time.
     * 
     * @return Range for this selected time.
     */
    public Range<Long> getRange() {
        return range;
    }

    /**
     * Determine whether or not the specified epoch time in milliseconds falls
     * within this selected time.
     * 
     * @param time
     *            Epoch time in milliseconds.
     * @return True if the specified time falls within this selected time, false
     *         otherwise.
     */
    public boolean contains(long time) {
        return range.contains(time);
    }

    /**
     * Determine whether or not the specified time range intersects this
     * selected time.
     * 
     * @param lowerTime
     *            Lower bound of the time range, as epoch time in milliseconds.
     * @param upperTime
     *            Upper bound of the time range, as epoch time in milliseconds.
     * @return True if the specified time range intersects this selected time,
     *         false otherwise.
     */
    public boolean intersects(long lowerTime, long upperTime) {
        return !((lowerTime > range.upperEndpoint()) || (upperTime < range
                .lowerEndpoint()));
    }
}
