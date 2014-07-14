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
 * Mar 08, 2014    2155    Chris.Golden      Fixed bugs with date-time fields in
 *                                           time megawidgets that caused unexpected
 *                                           date-times to be selected when the user
 *                                           manipulated the drop-down calendar.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IDateTimeComponentHolder {

    // Public Methods

    /**
     * Get the current epoch time in milliseconds.
     * 
     * @return Current epoch time in milliseconds.
     */
    public long getCurrentTime();

    /**
     * Convert the specified changed value to one that is acceptable.
     * 
     * @param identifier
     *            Identifier of the date-time component for which the potential
     *            value change is being converted.
     * @param value
     *            Potential new value of the date-time component.
     * @return The value, modified as necessary, or <code>-1</code> if it is
     *         unacceptable.
     */
    public long renderValueChangeAcceptable(String identifier, long value);

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
