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
 * 
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

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param dateFormat
     *            Date format to be used for parsing the dates from strings.
     */
    public DateStringComparator(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    // Public Methods

    /**
     * Compare the two dates, each specified in the format given by
     * <code>dateFormat</code>.
     * 
     * @param o1
     *            First date.
     * @param o2
     *            Second date.
     * @return Integer that is less than, equal to, or greater than 0 depending
     *         upon whether the first date is less than, equal to, or greater
     *         than the second date, respectively.
     */
    @Override
    public int compare(String o1, String o2) {
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