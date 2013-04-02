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
 * Description: Description: Hatch mark group specifier for time-interval (less
 * than a day) hatch marks, to be used with <code>MultiValueRuler</code>
 * instances with values representing time units.
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
public class TimeHatchMarkGroup implements IHatchMarkGroup {

    // Private Static Constants

    /**
     * Largest string in terms of pixel length for a double-digit value.
     */
    private static final String LARGEST_DOUBLE_DIGIT_STRING = "XX";

    /**
     * Milliseconds per hour.
     */
    private static final long MILLIS_PER_HOUR = TimeUnit.HOURS.toMillis(1);

    // Private Variables

    /**
     * Interval in milliseconds between these hatch marks.
     */
    private final long interval;

    /**
     * Height fraction of these hatch marks.
     */
    private final float heightFraction;

    /**
     * Color of these hatch marks.
     */
    private final Color color;

    /**
     * Date formatter used for generating labels.
     */
    private final SimpleDateFormat dateFormatter;

    /**
     * Date used for generating labels.
     */
    private final Date date = new Date();

    /**
     * Font used for labeling minute hatch marks.
     */
    private Font minuteFont = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param interval
     *            Interval in milliseconds between hatch marks.
     * @param heightFraction
     *            Height fraction of hatch marks.
     * @param color
     *            Color of hatch marks.
     */
    public TimeHatchMarkGroup(long interval, float heightFraction, Color color,
            Font minuteFont) {
        this.interval = interval;
        this.heightFraction = heightFraction;
        this.color = color;
        this.minuteFont = minuteFont;
        dateFormatter = new SimpleDateFormat(interval < MILLIS_PER_HOUR ? "mm"
                : "HH");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // Public Methods

    /**
     * Set the minute font.
     * 
     * @param minuteFont
     *            Minute font to be used.
     */
    public void setMinuteFont(Font minuteFont) {
        this.minuteFont = minuteFont;
    }

    /**
     * Get the interval between hatch marks for this group.
     * 
     * @return Interval between hatch marks for this group.
     */
    @Override
    public long getInterval() {
        return interval;
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
        return heightFraction;
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
        return color;
    }

    /**
     * Get the font to be used to draw labels.
     * 
     * @return Font to be used to draw labels, or <code>null</code> if the
     *         widget font is to be used.
     */
    @Override
    public Font getFont() {
        return (interval < MILLIS_PER_HOUR ? minuteFont : null);
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
        return LARGEST_DOUBLE_DIGIT_STRING;
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
        return LabelPosition.OVER_HATCH_MARK;
    }
}
