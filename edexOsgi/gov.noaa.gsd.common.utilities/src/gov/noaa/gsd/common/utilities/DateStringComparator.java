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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

/**
 * Description: Class that compares two dates given as strings. Each such string
 * may hold any number that is representable via a <code>double</code>
 * primitive.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2013            Chris.Golden      Initial creation
 * Nov 25, 2013    2336    Chris.Golden      Moved to gov.noaa.gsd.common.utilities.
 * Feb 16, 2014    2161    Chris.Golden      Added specification of a text string
 *                                           that is always treated as the largest
 *                                           possible date value when sorting.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class DateStringComparator implements Comparator<String> {

    // Private Variables

    /**
     * Date format used to parse dates from strings.
     */
    private DateFormat dateFormat = null;

    /**
     * Special string to treat as largest possible value when sorting.
     */
    private String largestPossibleValue = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param dateFormat
     *            Date format to be used for parsing the dates from strings.
     * @param largestPossibleValue
     *            Special string that when encountered in a sort is to be
     *            treated as the largest possible value.
     */
    public DateStringComparator(DateFormat dateFormat,
            String largestPossibleValue) {
        this.dateFormat = dateFormat;
        this.largestPossibleValue = largestPossibleValue;
    }

    // Public Methods

    @Override
    public int compare(String o1, String o2) {
        boolean firstLargest = o1.equals(largestPossibleValue);
        boolean secondLargest = o2.equals(largestPossibleValue);
        if (firstLargest && secondLargest) {
            return 0;
        }
        if (firstLargest) {
            return 1;
        }
        if (secondLargest) {
            return -1;
        }
        Date date1 = null, date2 = null;
        try {
            date1 = dateFormat.parse(o1);
            date2 = dateFormat.parse(o2);
        } catch (ParseException e) {
            throw new IllegalArgumentException("could not parse \""
                    + (date1 == null ? o1 : o2) + "\" as date", e);
        }
        return date1.compareTo(date2);
    }
}