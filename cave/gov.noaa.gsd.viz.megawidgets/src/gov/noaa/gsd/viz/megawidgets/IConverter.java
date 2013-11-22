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
 * Description: Interface describing the methods required to perform
 * bidirectional conversion between two different object types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2013    2336    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IConverter {

    // Public Methods

    /**
     * Convert the specified value given as the second type to the first type.
     * 
     * @param value
     *            Value to be converted.
     * @return Corresponding value of the first type.
     */
    public Object toFirst(Object value);

    /**
     * Convert the specified value given as the first type to the second type.
     * 
     * @param value
     *            Value to be converted.
     * @return Corresponding value of the second type.
     */
    public Object toSecond(Object value);
}
