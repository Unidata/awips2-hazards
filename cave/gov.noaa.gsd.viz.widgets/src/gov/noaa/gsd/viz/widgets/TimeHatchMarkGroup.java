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
 * than a day) hatch marks, to be used with {@link MultiValueRuler} instances
 * with values representing time units.
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

    @Override
    public long getInterval() {
        return interval;
    }

    @Override
    public float getHeightFraction() {
        return heightFraction;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Font getFont() {
        return (interval < MILLIS_PER_HOUR ? minuteFont : null);
    }

    @Override
    public String getLongestLabel() {
        return LARGEST_DOUBLE_DIGIT_STRING;
    }

    @Override
    public String getLabel(long value) {
        date.setTime(value);
        return dateFormatter.format(date);
    }

    @Override
    public LabelPosition getLabelPosition() {
        return LabelPosition.OVER_HATCH_MARK;
    }
}
