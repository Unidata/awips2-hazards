/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

import java.util.Comparator;

/**
 * Description: Class that compares two doubles given as strings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 14, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class DoubleStringComparator implements Comparator<String> {

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
        Double value1 = Double.valueOf(o1);
        Double value2 = Double.valueOf(o2);
        return value1.compareTo(value2);
    }
}
