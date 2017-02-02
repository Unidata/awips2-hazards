/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Description: Utilities used to create and manipulate SWT widgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 17, 2016   21873    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class WidgetUtilities {

    // Private Static Classes

    /**
     * Snap value calculator, used to generate snap-to values for the scale
     * widget.
     */
    private static class TimeLineRulerSnapValueCalculator implements
            ISnapValueCalculator {

        // Private Variables

        /**
         * Interval between snap-to values.
         */
        private final long interval;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param interval
         *            Interval to be used.
         */
        public TimeLineRulerSnapValueCalculator(long interval) {
            this.interval = interval;
        }

        // Public Methods

        @Override
        public long getSnapThumbValue(long value, long minimum, long maximum) {
            long remainder = value % interval;
            if (remainder < interval / 2L) {
                value -= remainder;
            } else {
                value += interval - remainder;
            }
            if (value < minimum) {
                value += interval
                        * (((minimum - value) / interval) + ((minimum - value)
                                % interval == 0 ? 0L : 1L));
            } else if (value > maximum) {
                value -= interval
                        * (((value - maximum) / interval) + ((value - maximum)
                                % interval == 0 ? 0L : 1L));
            }
            return value;
        }
    }

    // Private Static Constants

    /**
     * Number of milliseconds in a second.
     */
    private static final long SECOND_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    /**
     * Number of milliseconds in a minute.
     */
    private static final long MINUTE_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    /**
     * Number of milliseconds in an hour.
     */
    private static final long HOUR_INTERVAL = TimeUnit.HOURS.toMillis(1);

    /**
     * Number of milliseconds in a day.
     */
    private static final long DAY_INTERVAL = TimeUnit.DAYS.toMillis(1);

    /**
     * Snap value calculator for time line rulers with resolutions of seconds.
     */
    private static final ISnapValueCalculator TIME_LINE_SNAP_VALUE_CALCULATOR_SECONDS = new TimeLineRulerSnapValueCalculator(
            SECOND_INTERVAL);

    /**
     * Snap value calculator for time line rulers with resolutions of minutes.
     */
    private static final ISnapValueCalculator TIME_LINE_SNAP_VALUE_CALCULATOR_MINUTES = new TimeLineRulerSnapValueCalculator(
            MINUTE_INTERVAL);

    /**
     * Minimum visible time range in milliseconds.
     */
    private static final long MIN_VISIBLE_TIME_RANGE = 1L * MINUTE_INTERVAL;

    /**
     * Maximum visible time range in milliseconds.
     */
    private static final long MAX_VISIBLE_TIME_RANGE = 8L * DAY_INTERVAL;

    // Public Static Methods

    /**
     * Create a time line ruler allowing the user to zoom down to seconds
     * resolution.
     * 
     * @param parent
     *            Parent composite of the new time line ruler.
     * @param minimumTime
     *            Minimum time the ruler may show.
     * @param maximumTime
     *            Maximum time the ruler may show.
     * @return Time line ruler.
     */
    public static MultiValueRuler createTimeLineRuler(Composite parent,
            long minimumTime, long maximumTime) {

        /*
         * Create the colors for the time line ruler hatch marks, and add them
         * to a list of resources that are to be disposed of when the ruler is
         * disposed of.
         */
        final List<Resource> resources = new ArrayList<>();
        Color[] colors = { new Color(Display.getCurrent(), 128, 0, 0),
                new Color(Display.getCurrent(), 0, 0, 128),
                new Color(Display.getCurrent(), 0, 128, 0),
                new Color(Display.getCurrent(), 131, 120, 103) };
        for (Color color : colors) {
            resources.add(color);
        }

        /*
         * Create the time line ruler's hatch mark groups.
         */
        List<IHatchMarkGroup> hatchMarkGroups = new ArrayList<>();
        hatchMarkGroups.add(new DayHatchMarkGroup());
        hatchMarkGroups.add(new TimeHatchMarkGroup(6L * HOUR_INTERVAL, 0.33f,
                colors[0]));
        hatchMarkGroups.add(new TimeHatchMarkGroup(HOUR_INTERVAL, 0.28f,
                colors[0]));
        hatchMarkGroups.add(new TimeHatchMarkGroup(30L * MINUTE_INTERVAL,
                0.21f, colors[1]));
        hatchMarkGroups.add(new TimeHatchMarkGroup(10L * MINUTE_INTERVAL,
                0.14f, colors[1]));
        hatchMarkGroups.add(new TimeHatchMarkGroup(MINUTE_INTERVAL, 0.10f,
                colors[1]));
        hatchMarkGroups.add(new TimeHatchMarkGroup(15L * SECOND_INTERVAL,
                0.05f, colors[2]));
        hatchMarkGroups.add(new TimeHatchMarkGroup(SECOND_INTERVAL, 0.02f,
                colors[2]));

        /*
         * Create the time line widget. The actual widget is an instance of an
         * anonymous subclass; the latter is needed because background and
         * foreground color changes must be ignored, since the ModeListener
         * objects may try to change the colors when the CAVE mode changes,
         * which in this case is undesirable.
         */
        MultiValueRuler ruler = new MultiValueRuler(parent, minimumTime,
                maximumTime, hatchMarkGroups) {
            @Override
            public void setBackground(Color background) {

                /*
                 * No action.
                 */
            }

            @Override
            public void setForeground(Color foreground) {

                /*
                 * No action.
                 */
            }
        };

        /*
         * Create the fonts to be used for minutes and seconds, and add them to
         * the list of resources to be disposed of when the ruler is disposed
         * of.
         */
        FontData fontData = ruler.getFont().getFontData()[0];
        Font minuteFont = new Font(Display.getCurrent(), fontData.getName(),
                (fontData.getHeight() * 7) / 10, fontData.getStyle());
        resources.add(minuteFont);
        Font secondFont = new Font(Display.getCurrent(), fontData.getName(),
                (fontData.getHeight() * 5) / 10, fontData.getStyle());
        resources.add(secondFont);
        for (int j = 2; j < hatchMarkGroups.size(); j++) {
            ((TimeHatchMarkGroup) hatchMarkGroups.get(j))
                    .setMinuteAndSecondFonts(minuteFont, secondFont);
        }

        /*
         * Set up the zooming parameters for the ruler.
         */
        ruler.setVisibleValueZoomCalculator(new IVisibleValueZoomCalculator() {
            @Override
            public long getVisibleValueRangeForZoom(MultiValueRuler ruler,
                    boolean zoomIn, int amplitude) {
                long range;
                if (zoomIn) {
                    range = getTimeLineRulerZoomedInRange(ruler);
                    if (range < MIN_VISIBLE_TIME_RANGE) {
                        return 0L;
                    }
                } else {
                    range = getTimeLineRulerZoomedOutRange(ruler);
                    if (range > MAX_VISIBLE_TIME_RANGE) {
                        return 0L;
                    }
                }
                return range;
            }
        });

        /*
         * Set the ruler's border color and height multiplier, and configure it
         * to allow its viewport to be dragged.
         */
        ruler.setBorderColor(colors[3]);
        ruler.setHeightMultiplier(2.95f);
        ruler.setViewportDraggable(true);

        /*
         * Ensure that the SWT colors and fonts are disposed of when the ruler
         * is.
         */
        ruler.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                for (Resource resource : resources) {
                    resource.dispose();
                }
            }
        });

        return ruler;
    }

    /**
     * Get the minimum visible time range in milliseconds for time line rulers.
     * 
     * @return Minimum visible time range in milliseconds for time line rulers.
     */
    public static long getTimeLineRulerMinimumVisibleTimeRange() {
        return MIN_VISIBLE_TIME_RANGE;
    }

    /**
     * Get the maximum visible time range in milliseconds for time line rulers.
     * 
     * @return Maximum visible time range in milliseconds for time line rulers.
     */
    public static long getTimeLineRulerMaximumVisibleTimeRange() {
        return MAX_VISIBLE_TIME_RANGE;
    }

    /**
     * Get a time line snap value calculator with seconds resolution.
     * 
     * @return Time line snap value calculator with seconds resolution.
     */
    public static ISnapValueCalculator getTimeLineSnapValueCalculatorWithSecondsResolution() {
        return TIME_LINE_SNAP_VALUE_CALCULATOR_SECONDS;
    }

    /**
     * Get a time line snap value calculator with minutes resolution.
     * 
     * @return Time line snap value calculator with minutes resolution.
     */
    public static ISnapValueCalculator getTimeLineSnapValueCalculatorWithMinutesResolution() {
        return TIME_LINE_SNAP_VALUE_CALCULATOR_MINUTES;
    }

    /**
     * Get the visible time range that would result if the specified time line
     * ruler as it is currently was zoomed out one level.
     * 
     * @param ruler
     *            Ruler for which the potential zoomed-out visible time range is
     *            to be fetched.
     * @return Visible time range that would result if the time line was zoomed
     *         out.
     */
    public static long getTimeLineRulerZoomedOutRange(MultiValueRuler ruler) {
        return (getTimeLineRulerVisibleTimeRange(ruler) * 3L) / 2L;
    }

    /**
     * Get the visible time range that would result if the specified time line
     * ruler as it is currently was zoomed in one level.
     * 
     * @param ruler
     *            Ruler for which the potential zoomed-in visible time range is
     *            to be fetched.
     * @return Visible time range that would result if the time line was zoomed
     *         in.
     */
    public static long getTimeLineRulerZoomedInRange(MultiValueRuler ruler) {
        return (getTimeLineRulerVisibleTimeRange(ruler) * 2L) / 3L;
    }

    /**
     * Get the visible time range of the specified time line ruler.
     * 
     * @param ruler
     *            Ruler for which the visible time range is to be fetched.
     * @return Visible time range.
     */
    public static long getTimeLineRulerVisibleTimeRange(MultiValueRuler ruler) {
        return ruler.getUpperVisibleValue() + 1L - ruler.getLowerVisibleValue();
    }
}
