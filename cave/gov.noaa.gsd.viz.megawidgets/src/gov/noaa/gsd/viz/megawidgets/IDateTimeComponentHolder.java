/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import com.google.common.collect.Range;

/**
 * Description: Interface describing the methods that must be implemented by
 * megawidgets that hold one or more {@link DateTimeComponent} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 14, 2013    2545    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IDateTimeComponentHolder {

    // Public Methods

    /**
     * Get the range representing the bounds on the value that the specified
     * date-time component is currently allowed to hold. The date-time component
     * will use this whenever bounds-checking; it is acceptable for the
     * implementation to return different ranges each time it is called.
     * 
     * @param identifier
     *            Identifier of the date-time component for which the range is
     *            being requested.
     * @return Range of values that the date-time component is allowed to hold
     *         at this instant.
     */
    public Range<Long> getAllowableRange(String identifier);

    /**
     * Get the current epoch time in milliseconds.
     * 
     * @return Current epoch time in milliseconds.
     */
    public long getCurrentTime();

    /**
     * Determine whether or not the specified value change is acceptable. Note
     * that implementations do not need to check the value against the range
     * boundaries; this has already been done prior to any invocation of this
     * method via a call to <code>getAllowableRange()</code>.
     * 
     * @param identifier
     *            Identifier of the date-time component for which the potential
     *            value change is being checked.
     * @param value
     *            Potential new value of the date-time component.
     * @return True if the value change is acceptable, false otherwise.
     */
    public boolean isValueChangeAcceptable(String identifier, long value);

    /**
     * Receive notification that the value of the specified date-time component
     * has been changed as a result of GUI manipulation.
     * 
     * @param identifier
     *            Identifier of the date-time component for which the value has
     *            changed.
     * @param value
     *            New value of the date-time component.
     * @param rapidChange
     *            Flag indicating whether or not this value change is part of a
     *            rapid set of such changes.
     */
    public void valueChanged(String identifier, long value, boolean rapidChange);
}
