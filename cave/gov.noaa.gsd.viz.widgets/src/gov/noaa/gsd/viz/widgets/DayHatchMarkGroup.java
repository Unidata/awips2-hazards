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
 * used with <code>MultiValueRuler</code> instances with values representing
 * time units.
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

    /**
     * Get the interval between hatch marks for this group.
     * 
     * @return Interval between hatch marks for this group.
     */
    @Override
    public long getInterval() {
        return MILLIS_PER_DAY;
    }

    /**
     * Get the height of the vertical line drawn to represent the hatch mark, as
     * a fraction of the total height of the ruler, with 0 meaning that no line
     * is drawn, and 1 meaning that the line is drawn from the bottom up to the
     * top of the ruler.
     * 
     * @return Height of the vertical line.
     */
    @Override
    public float getHeightFraction() {
        return 1.0f;
    }

    /**
     * Get the color to be used for the hatch mark at the specified value.
     * 
     * @return Color to be used for the hatch mark at the specified value; if
     *         <code>null</code>, the widget's foreground color will be used
     *         instead.
     */
    @Override
    public Color getColor() {
        return null;
    }

    /**
     * Get the font to be used to draw labels.
     * 
     * @return Font to be used to draw labels, or <code>null</code> if the
     *         widget font is to be used.
     */
    @Override
    public Font getFont() {
        return null;
    }

    /**
     * Get the longest possible label that this group may use for labeling its
     * hatch marks.
     * 
     * @return Longest possible label that this group may use for labeling its
     *         hatch marks.
     */
    @Override
    public String getLongestLabel() {
        return LARGEST_DATE_STRING;
    }

    /**
     * Get the label for the hatch mark at the specified value.
     * 
     * @param value
     *            Value for which the label is to be fetched.
     * @return Label for the hatch mark at this value.
     */
    @Override
    public String getLabel(long value) {
        date.setTime(value);
        return dateFormatter.format(date);
    }

    /**
     * Get the horizontal positioning of the labels with respect to the hatch
     * marks they are labeling.
     * 
     * @return Horizontal positioning of the labels.
     */
    @Override
    public LabelPosition getLabelPosition() {
        return LabelPosition.BETWEEN_HATCH_MARKS;
    }
}
