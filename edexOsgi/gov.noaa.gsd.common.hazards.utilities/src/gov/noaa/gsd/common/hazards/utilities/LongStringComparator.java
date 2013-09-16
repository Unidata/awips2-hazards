/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.hazards.utilities;

import java.util.Comparator;

/**
 * Description: Class that compares two long integers given as strings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2013            Chris.Golden      Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class LongStringComparator implements Comparator<String> {

    // Public Methods

    /**
     * Compare the two numbers.
     * 
     * @param o1
     *            First number.
     * @param o2
     *            Second number.
     * @return Integer that is less than, equal to, or greater than 0 depending
     *         upon whether the first number is less than, equal to, or greater
     *         than the second date, respectively.
     */
    @Override
    public int compare(String o1, String o2) {
        double value1 = Long.parseLong(o1);
        double value2 = Long.parseLong(o2);
        if (value1 < value2) {
            return -1;
        } else if (value1 > value2) {
            return 1;
        } else {
            return 0;
        }
    }
}
