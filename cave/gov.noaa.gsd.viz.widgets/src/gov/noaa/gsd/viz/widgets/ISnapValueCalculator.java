/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

/**
 * Interface describing the methods that must be implemented in order to be a
 * snap value calculator. The latter may be used to implement snap-to behavior
 * for a widget, allowing the degree of coarseness for values to be specified.
 * For example, if values range from 0 to 100, but only values that are a
 * multiple of 10 are allowed, then given a value of 6, it may snap to 10; given
 * a value of 43, it may snap to 40; etc.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MultiValueLinearControl
 */
public interface ISnapValueCalculator {

    // Public Methods

    /**
     * Get the snap value for the specified thumb value, between the specified
     * minimum and maximum values (inclusive).
     * 
     * @param value
     *            Thumb value.
     * @param minimum
     *            Minimum value that this value can take on.
     * @param maximum
     *            Maximum value that this value can take on.
     * @return Snap value closest to the specified value and within the
     *         specified bounds.
     */
    public long getSnapThumbValue(long value, long minimum, long maximum);
}