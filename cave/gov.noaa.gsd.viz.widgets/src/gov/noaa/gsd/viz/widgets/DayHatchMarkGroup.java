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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Description: Hatch mark group specifier for day-interval hatch marks, to be
 * used with {@link MultiValueRuler} instances with values representing time
 * units.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2013            Chris.Golden      Initial creation
 * Jan 28, 2014    2161    Chris.Golden      Removed extraneous Javadoc comments,
 *                                           and prettied up the Javadoc as well.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class DayHatchMarkGroup implements IHatchMarkGroup {

    // Private Static Constants

    /**
     * Largest string in terms of pixel length for a date value.
     */
    private static final String LARGEST_DATE_STRING = "XXXXXX";

    /**
     * Date format string.
     */
    private static final String DATE_FORMAT_STRING = "d-MMM";

    /**
     * Number of milliseconds in a day.
     */
    private static final long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);

    // Private Variables

    /**
     * Date formatter used for generating labels.
     */
    private final SimpleDateFormat dateFormatter;

    /**
     * Date used for generating labels.
     */
    private final Date date = new Date();

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public DayHatchMarkGroup() {
        dateFormatter = new SimpleDateFormat(DATE_FORMAT_STRING);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // Public Methods

    @Override
    public long getInterval() {
        return MILLIS_PER_DAY;
    }

    @Override
    public float getHeightFraction() {
        return 1.0f;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public Font getFont() {
        return null;
    }

    @Override
    public String getLongestLabel() {
        return LARGEST_DATE_STRING;
    }

    @Override
    public String getLabel(long value) {
        date.setTime(value);
        return dateFormatter.format(date);
    }

    @Override
    public LabelPosition getLabelPosition() {
        return LabelPosition.BETWEEN_HATCH_MARKS;
    }
}
