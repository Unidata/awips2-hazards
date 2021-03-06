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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.raytheon.uf.common.util.Pair;

/**
 * Multi-value scale widget, allowing the display and manipulation of one or
 * more values represented visually by "thumbs" along a scale track. This widget
 * is similar to the SWT <code>Scale</code>, except that it allows multiple
 * thumbs, colored ranges between the thumbs, and marked values along the track
 * length that, unlike the thumb values, may not be changed via user
 * interaction, but may still be manipulated programmatically.
 * <p>
 * Thumb sizes, and the width of the track along which the thumbs run, may be
 * configured. Additionally, marked values may be given colors with which they
 * are to be drawn, as may the ranges between the marked values, and the track
 * areas (ranges) between adjacent thumbs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jun 10, 2013            Chris.Golden      Added method to dispose of track
 *                                           images upon a thumb range color
 *                                           change in order to fix bug that
 *                                           caused such color changes to not
 *                                           show up when repainting. Also
 *                                           fixed minor painting bug causing
 *                                           colored tracks to not fill the
 *                                           track all the way to the right.
 * Jan 15, 2014    2704    Chris.Golden      Changed to make its visual
 *                                           components size themselves pro-
 *                                           portionally to the current font.
 * Jan 28, 2014    2161    Chris.Golden      Added ability to render each
 *                                           thumb editable or read-only
 *                                           individuallly, instead of
 *                                           controlling editability at a
 *                                           coarser widget level.
 * Jul 03, 2014    3512    Chris.Golden      Added code to make all of the
 *                                           constrained thumbs look active
 *                                           if one of them is active or
 *                                           dragging and the constrained
 *                                           intervals are locked. Also
 *                                           changed to better inline comment
 *                                           style.
 * Apr 19, 2018   33787    Chris.Golden      Changed multi-value scale widgets
 *                                           to cache the images they use for
 *                                           representing themselves visually,
 *                                           and to reuse said images across
 *                                           instances of the widgets.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MultiValueScale extends MultiValueLinearControl {

    // Public Static Constants

    /**
     * Base font height; if the current font height is not the same as this,
     * dimensions of visual components are multiplied by the current font height
     * divided by this value.
     */
    public static final float BASE_FONT_HEIGHT = 17.0f;

    // Private Static Constants

    /**
     * Default longitudinal dimension of a thumb in pixels, assuming a font
     * height of {@link #BASE_FONT_HEIGHT}; if the font height is not the
     * latter, this value is multiplied by the font height divided by
     * {@link #BASE_FONT_HEIGHT}.
     */
    private static final int DEFAULT_THUMB_LONGITUDINAL_DIMENSION = 13;

    /**
     * Default lateral dimension of a thumb in pixels, assuming a font height of
     * {@link #BASE_FONT_HEIGHT}; if the font height is not the latter, this
     * value is multiplied by the font height divided by
     * {@link #BASE_FONT_HEIGHT}.
     */
    private static final int DEFAULT_THUMB_LATERAL_DIMENSION = 27;

    /**
     * Thickness of the track in pixels, assuming a font height of
     * {@link #BASE_FONT_HEIGHT}; if the font height is not the latter, this
     * value is multiplied by the font height divided by
     * {@link #BASE_FONT_HEIGHT}.
     */
    private static final int DEFAULT_TRACK_THICKNESS = 5;

    /**
     * Corner arc divisor, by which the longest of the two thumb dimensions is
     * divided to yield the thumb corner arc size in pixels.
     */
    private static final int THUMB_ARC_SIZE_DIVIDER = 2;

    /**
     * Color of the border of the widget; this should be fetchable via <code>
     * Display.getSystemColor()</code>, but the colors returned by that method
     * seem to have no relationship to the colors used on the same display for
     * SWT widgets such as <code>Scale</code>, thus the colors defined here are
     * hard-coded. Apparently this has to do with skins, which do not report
     * their colors to SWT...
     */
    private static final java.awt.Color BORDER_COLOR = new java.awt.Color(131,
            120, 103);

    /**
     * Color of the border of the widget when selected.
     */
    private static final java.awt.Color BORDER_SELECTION_COLOR = new java.awt.Color(
            57, 98, 137);

    /**
     * Color of the border of the widget when disabled.
     */
    private static final java.awt.Color BORDER_DISABLED_COLOR = new java.awt.Color(
            179, 167, 149);

    /**
     * Color of the shadow of the widget.
     */
    private static final java.awt.Color SHADOW_COLOR = new java.awt.Color(131,
            120, 103, 64);

    /**
     * Color of the detailed etchings of the widget.
     */
    private static final java.awt.Color DETAIL_COLOR = new java.awt.Color(202,
            201, 200);

    /**
     * Color of the selected detailed etchings of the widget.
     */
    private static final java.awt.Color DETAIL_SELECTION_COLOR = new java.awt.Color(
            113, 160, 204);

    /**
     * Color of the background of the widget.
     */
    private static final java.awt.Color BACKGROUND_COLOR;

    /**
     * Color of the selected area of the widget.
     */
    private static final java.awt.Color BACKGROUND_SELECTION_COLOR = new java.awt.Color(
            118, 175, 227);

    /**
     * Color of the disabled background of the widget thumbs.
     */
    private static final java.awt.Color BACKGROUND_DISABLED_COLOR = new java.awt.Color(
            237, 233, 227);

    /**
     * Color of the disabled background of the widget thumbs.
     */
    private static final java.awt.Color BACKGROUND_DISABLED_CENTER_COLOR = new java.awt.Color(
            220, 212, 200);

    /*
     * Initialize the colors.
     */
    static {
        Color color = Display.getCurrent()
                .getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        BACKGROUND_COLOR = new java.awt.Color(color.getRed(), color.getGreen(),
                color.getBlue());
    }

    /**
     * Cache used by all instances of this class to store thumb images.
     */
    private static final WidgetResourceCache<ThumbImageParameters, Image> THUMB_IMAGE_CACHE = new WidgetResourceCache<>();

    /**
     * Cache used by all instances of this class to store thumb images. The pair
     * used as the key holds the track tile thickness and the components of the
     * color of the track, respectively.
     */
    private static final WidgetResourceCache<Pair<Integer, RGB>, Image> TRACK_TILE_IMAGE_CACHE = new WidgetResourceCache<>();

    // Private Enumerated Types

    /**
     * State of a thumb.
     */
    private enum ThumbState {
        DISABLED, NORMAL, ACTIVE
    };

    // Private Static Classes

    /**
     * Parameters of a thumb image that differentiate it from other thumb
     * images.
     */
    private static class ThumbImageParameters {

        // Private Variables

        /**
         * Longitudinal dimension of the thumb in pixels.
         */
        private final int longitudinalDimension;

        /**
         * Lateral dimension of the thumb in pixels.
         */
        private final int lateralDimension;

        /**
         * Arc size for each corner of the thumb in pixels.
         */
        private final int cornerArcSize;

        /**
         * State of the thumb that the associated image is to represent.
         */
        private final ThumbState state;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param longitudinalDimension
         *            Longitudinal dimension of the thumb in pixels.
         * @param lateralDimension
         *            Lateral dimension of the thumb in pixels.
         * @param cornerArcSize
         *            Arc size for each corner of the thumb in pixels.
         * @param state
         *            State of the thumb that the associated image is to
         *            represent.
         */
        public ThumbImageParameters(int longitudinalDimension,
                int lateralDimension, int cornerArcSize, ThumbState state) {
            this.longitudinalDimension = longitudinalDimension;
            this.lateralDimension = lateralDimension;
            this.cornerArcSize = cornerArcSize;
            this.state = state;
        }

        // Public Methods

        @Override
        public boolean equals(Object other) {
            if ((other == null)
                    || (other instanceof ThumbImageParameters == false)) {
                return false;
            }
            ThumbImageParameters otherParams = (ThumbImageParameters) other;
            return ((longitudinalDimension == otherParams.longitudinalDimension)
                    && (lateralDimension == otherParams.lateralDimension)
                    && (cornerArcSize == otherParams.cornerArcSize)
                    && (state == otherParams.state));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + longitudinalDimension;
            result = prime * result + lateralDimension;
            result = prime * result + cornerArcSize;
            result = prime * result + (state == null ? 0 : state.hashCode());
            return result;
        }
    }

    // Private Constants

    /**
     * Track border color; this is instance- rather than class-scoped so that it
     * may be disposed of when the widget is disposed.
     */
    private final Color TRACK_BORDER_COLOR = new Color(Display.getCurrent(),
            131, 120, 103);

    /**
     * Track border shadow color; this is instance- rather than class-scoped so
     * that it may be disposed of when the widget is disposed.
     */
    private final Color TRACK_BORDER_SHADOW_COLOR = new Color(
            Display.getCurrent(), 228, 224, 218);

    /**
     * Track border highlight color; this is instance- rather than class-scoped
     * so that it may be disposed of when the widget is disposed.
     */
    private final Color TRACK_BORDER_HIGHLIGHT_COLOR = new Color(
            Display.getCurrent(), 246, 244, 241);

    // Private Variables

    /**
     * Longitudinal dimension of the thumbs, this being the dimension along the
     * axis parallel to the track, before adjusting for font size.
     */
    private int baseThumbLongitudinalDimension = DEFAULT_THUMB_LONGITUDINAL_DIMENSION;

    /**
     * Lateral dimension of the thumbs, this being the dimension along the axis
     * perpendicular to the track, before adjusting for font size.
     */
    private int baseThumbLateralDimension = DEFAULT_THUMB_LATERAL_DIMENSION;

    /**
     * Thickness of the track, before adjusting for font size.
     */
    private int baseTrackThickness = DEFAULT_TRACK_THICKNESS;

    /**
     * Longitudinal dimension of the thumbs, this being the dimension along the
     * axis parallel to the track. This is the actual value in pixels, that is,
     * {@link #baseThumbLongitudinalDimension} multiplied by the current font
     * height divided by {@link #BASE_FONT_HEIGHT}.
     */
    private int thumbLongitudinalDimension;

    /**
     * Lateral dimension of the thumbs, this being the dimension along the axis
     * perpendicular to the track. This is the actual value in pixels, that is,
     * {@link #baseThumbLateralDimension} multiplied by the current font height
     * divided by {@link #BASE_FONT_HEIGHT}.
     */
    private int thumbLateralDimension;

    /**
     * Thickness of the track. This is the actual value in pixels, that is,
     * {@link #baseTrackThickness} multiplied by the current font height divided
     * by {@link #BASE_FONT_HEIGHT}.
     */
    private int trackThickness;

    /**
     * Thumb corner arc size, used to round the corners of the thumb.
     */
    private int thumbCornerArcSize = 0;

    /**
     * List of 1-pixel-wide images to be used as tiles to draw the track area
     * between two adjacent constrained thumbs or between a constrained thumb
     * and the start or end of the track. The first image is used for the track
     * area between the left end of the track and the first constrained thumb;
     * the second for the track area between the first and second constrained
     * thumbs (or the first constrained thumb and the end, if only one
     * constrained thumb is in use), etc. Thus, there must always be one less
     * value in this list than the number returned by <code>
     * getConstrainedThumbValueCount()</code>.
     */
    private final List<Image> trackTileImages = new ArrayList<Image>();

    /**
     * 1-pixel-wide image to be used as the tile to draw the track area if the
     * widget is disabled.
     */
    private Image disabledTrackTileImage = null;

    /**
     * Thumb image.
     */
    private Image thumbImage = null;

    /**
     * Active thumb image.
     */
    private Image activeThumbImage = null;

    /**
     * Disabled thumb image.
     */
    private Image disabledThumbImage = null;

    // Public Static Methods

    /**
     * Dispose of any unused images created by instances of this class but not
     * removed.
     */
    public static void purgeUnusedResources() {
        THUMB_IMAGE_CACHE.prune();
        TRACK_TILE_IMAGE_CACHE.prune();
    }

    // Public Constructors

    /**
     * Construct a standard instance with a resize behavior of <code>
     * ResizeBehavior.CHANGE_PIXELS_PER_VALUE_UNIT</code>.
     * 
     * @param parent
     *            Parent of this widget.
     * @param minimumValue
     *            Absolute minimum possible value.
     * @param maximumValue
     *            Absolute maximum possible value.
     */
    public MultiValueScale(Composite parent, long minimumValue,
            long maximumValue) {
        super(parent, minimumValue, maximumValue);

        /*
         * Initialize the widget.
         */
        initialize();
    }

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent of this widget.
     * @param minimumValue
     *            Absolute minimum possible value.
     * @param maximumValue
     *            Absolute maximum possible value.
     * @param resizeBehavior
     *            Resize behavior.
     */
    public MultiValueScale(Composite parent, long minimumValue,
            long maximumValue, ResizeBehavior resizeBehavior) {
        super(parent, minimumValue, maximumValue, resizeBehavior);

        /*
         * Initialize the widget.
         */
        initialize();
    }

    // Public Methods

    @Override
    public final boolean isViewportDraggable() {
        return false;
    }

    /**
     * Get the longitudinal dimension of the thumb. The longitudinal dimension
     * is the dimension of the thumb along the axis parallel to the track. It is
     * not adjusted for font height; to adjust for the latter, the returned
     * value may be multiplied by the current font height divided by
     * {@link #BASE_FONT_HEIGHT} and rounding to the nearest integer, yielding
     * the true longitudinal dimension in pixels.
     * 
     * @return Longitudinal dimension of the thumb.
     */
    public final int getThumbLongitudinalDimension() {
        return baseThumbLongitudinalDimension;
    }

    /**
     * Get the lateral dimension of the thumb. The lateral dimension is the
     * dimension of the thumb along the axis perpendicular to the track. It is
     * not adjusted for font height; to adjust for the latter, the returned
     * value may be multiplied by the current font height divided by
     * {@link #BASE_FONT_HEIGHT} and rounding to the nearest integer, yielding
     * the true lateral dimension in pixels.
     * 
     * @return Lateral dimension of the thumb.
     */
    public final int getThumbLateralDimension() {
        return baseThumbLateralDimension;
    }

    /**
     * Get the track thickness. It is not adjusted for font height; to adjust
     * for the latter, the returned value may be multiplied by the current font
     * height divided by {@link #BASE_FONT_HEIGHT} and rounding to the nearest
     * integer, yielding the true track thickness in pixels.
     * 
     * @return Track thickness.
     */
    public final int getTrackThickness() {
        return baseTrackThickness;
    }

    /**
     * Set the dimensions of the visual components of the widget. Note that for
     * best visual results, the longitudinal dimension of the thumb must be an
     * odd number, and the lateral thumb dimension and track thickness must both
     * be odd or both be even.
     * <p>
     * Also note that the values here are not true pixel values; they are values
     * relative to font size. To determine the true pixel values, each of the
     * values would be multiplied by the current font height divided by
     * {@link #BASE_FONT_HEIGHT} and rounded to the nearest integer.
     * 
     * @param thumbLongitudinal
     *            Longitudinal dimension of the thumb, this being the dimension
     *            along the axis parallel to the track. This value must be an
     *            odd positive integer.
     * @param thumbLateral
     *            Lateral dimension of the thumb, this being the dimension along
     *            the axis perpendicular to the track.
     * @param trackThickness
     *            Thickness of the track.
     */
    public final void setComponentDimensions(int thumbLongitudinal,
            int thumbLateral, int trackThickness) {

        /*
         * Remember the new component dimensions.
         */
        boolean thumbChanged = ((this.baseThumbLongitudinalDimension != thumbLongitudinal)
                || (this.baseThumbLateralDimension != thumbLateral));
        this.baseThumbLongitudinalDimension = thumbLongitudinal;
        this.baseThumbLateralDimension = thumbLateral;
        boolean trackChanged = (this.baseTrackThickness != trackThickness);
        this.baseTrackThickness = trackThickness;

        /*
         * Recalculate the font-relative pixel dimensions.
         */
        calculateFontRelativePixelDimensions();

        /*
         * Recreate the track tile images using the new thickness, if the
         * thickness changed.
         */
        if (trackChanged) {
            for (int j = 0; j < trackTileImages.size(); j++) {
                if (trackTileImages.get(j) != null) {
                    TRACK_TILE_IMAGE_CACHE.release(trackTileImages.get(j));
                }
                trackTileImages.set(j,
                        getTrackTileImage(getConstrainedThumbRangeColor(j)));
            }
            if (disabledTrackTileImage != null) {
                TRACK_TILE_IMAGE_CACHE.release(disabledTrackTileImage);
            }
            disabledTrackTileImage = getTrackTileImage(null);
        }

        /*
         * Recreate the thumb images using the new dimensions, if the dimensions
         * changed.
         */
        if (thumbChanged) {
            calculateThumbCornerArcSize();
            if (thumbImage != null) {
                THUMB_IMAGE_CACHE.release(thumbImage);
            }
            thumbImage = getThumbImage(ThumbState.NORMAL);
            if (activeThumbImage != null) {
                THUMB_IMAGE_CACHE.release(activeThumbImage);
            }
            activeThumbImage = getThumbImage(ThumbState.ACTIVE);
            if (disabledThumbImage != null) {
                THUMB_IMAGE_CACHE.release(disabledThumbImage);
            }
            disabledThumbImage = getThumbImage(ThumbState.DISABLED);
        }

        /*
         * Recalculate the preferred size and the active thumb, and redraw the
         * widget, if a change in one of the dimensions occurred.
         */
        if (trackChanged || thumbChanged) {
            computePreferredSize(true);
            scheduleDetermineActiveThumbIfEnabled();
            redraw();
        }
    }

    @Override
    public final void setFont(Font font) {

        /*
         * Let the superclass do its work.
         */
        super.setFont(font);

        /*
         * Recalculate the font-relative pixel dimensions.
         */
        calculateFontRelativePixelDimensions();

        /*
         * Recalculate the preferred size and the active thumb, and redraw.
         */
        computePreferredSize(true);
        scheduleDetermineActiveThumbIfEnabled();
        redraw();
    }

    // Protected Methods

    @Override
    protected final void widgetEnabledStateChanged() {
        redraw();
    }

    @Override
    protected final void computePreferredSize(boolean force) {

        /*
         * Compute the preferred size only if it has not yet been computed, or
         * if being forced to do it regardless of previous computations.
         */
        if (force || (getPreferredWidth() == 0)) {
            int preferredWidth = (thumbLongitudinalDimension
                    * getConstrainedThumbValueCount()) + getLeftInset()
                    + getRightInset();
            int preferredHeight = thumbLateralDimension + 2 + getTopInset()
                    + getBottomInset();
            setPreferredSize(preferredWidth, preferredHeight);
        }
    }

    @Override
    protected final void paintControl(PaintEvent e) {

        /*
         * Do nothing if the client width or height are 0.
         */
        if ((getClientAreaWidth() == 0) || (getClientAreaHeight() == 0)) {
            return;
        }

        /*
         * Calculate the preferred size if needed, and determine the width and
         * height to be used when painting this time around.
         */
        computePreferredSize(false);
        Rectangle clientArea = getClientArea();

        /*
         * Get the default colors, as they are needed later.
         */
        Color background = e.gc.getBackground();
        Color foreground = e.gc.getForeground();

        /*
         * Draw the background.
         */
        if (e.gc.getBackground() != null) {
            e.gc.fillRectangle(clientArea);
        }

        /*
         * Ensure that the drawing does not occur in the horizontal inset area,
         * only in the client area and the vertical padding area above and below
         * the client area.
         */
        e.gc.setClipping(clientArea.x, 0, clientArea.width,
                getTopInset() + clientArea.height + getBottomInset());

        /*
         * Iterate through the constrained marked value indicators, drawing the
         * ranges between them and at either end.
         */
        int lastMarkedValueX = mapValueToPixel(getMinimumAllowableValue()) - 1;
        for (int j = 0; j <= getConstrainedMarkedValueCount(); j++) {
            int markedValueX = mapValueToPixel(
                    j == getConstrainedMarkedValueCount()
                            ? getMaximumAllowableValue()
                            : getConstrainedMarkedValue(j))
                    + (j == getConstrainedMarkedValueCount() ? 1 : 0);
            Color color = getConstrainedMarkedRangeColor(j);
            if (color != null) {
                e.gc.setBackground(color);
                e.gc.fillRectangle(lastMarkedValueX + 1, 0,
                        markedValueX - lastMarkedValueX,
                        getTopInset() + clientArea.height + getBottomInset());
            }
            lastMarkedValueX = markedValueX;
        }

        /*
         * Reset the clipping region.
         */
        e.gc.setClipping((Rectangle) null);

        /*
         * Determine the Y coordinate where the track will be drawn.
         */
        int yTrack = getTopInset() + 2
                + ((thumbLateralDimension - trackThickness) / 2);

        /*
         * Draw the shadowed and highlighted lines around where the track border
         * will be in order to make it look more three-dimensional.
         */
        e.gc.setForeground(TRACK_BORDER_SHADOW_COLOR);
        e.gc.drawLine(getLeftInset() - 1, yTrack - 2,
                getLeftInset() + clientArea.width, yTrack - 2);
        e.gc.setForeground(TRACK_BORDER_HIGHLIGHT_COLOR);
        e.gc.drawLine(getLeftInset() - 1, yTrack + trackThickness - 1,
                getLeftInset() + clientArea.width, yTrack + trackThickness - 1);

        /*
         * Iterate through the marked value indicators, drawing any that are
         * visible. Draw the marked value types in the order called for by the
         * current configuration.
         */
        e.gc.setAntialias(SWT.ON);
        for (ValueType type : getMarkTypeDrawingOrder()) {
            int numValues = (type == ValueType.CONSTRAINED
                    ? getConstrainedMarkedValueCount()
                    : getFreeMarkedValueCount());
            for (int j = 0; j < numValues; j++) {
                int markedValueX = mapValueToPixel(type == ValueType.CONSTRAINED
                        ? getConstrainedMarkedValue(j) : getFreeMarkedValue(j));
                if ((markedValueX >= clientArea.x)
                        && (markedValueX < clientArea.x + clientArea.width)) {
                    Color color = (type == ValueType.CONSTRAINED
                            ? getConstrainedMarkedValueColor(j)
                            : getFreeMarkedValueColor(j));
                    e.gc.setForeground(color == null ? foreground : color);
                    e.gc.drawLine(markedValueX, -1, markedValueX, getTopInset()
                            + clientArea.height + getBottomInset());
                }
            }
        }

        /*
         * Turn off the GC's advanced mode so that the stretched track images
         * used below do not get translucent or transparent parts at their
         * edges.
         */
        boolean advanced = e.gc.getAdvanced();
        e.gc.setAdvanced(false);

        /*
         * Iterate through the gaps between the thumbs and between the start and
         * end thumbs and their respective ends of the track, drawing the track
         * for that section using the appropriate image via tiling.
         */
        int numValues = getConstrainedThumbValueCount();
        int lastX = mapValueToPixel(getMinimumAllowableValue());
        if (disabledTrackTileImage == null) {
            disabledTrackTileImage = getTrackTileImage(null);
        }
        for (int j = 0; j <= numValues; j++) {

            /*
             * Create the image for this portion of the track, if it has not
             * already been created.
             */
            if (j >= trackTileImages.size()) {
                updateTrackTileImage(j, getConstrainedThumbRangeColor(j));
            }

            /*
             * Determine the X boundaries for this section of the track, and if
             * they are both offscreen, remember to skip the actual drawing of
             * this section.
             */
            int startX = lastX;
            int endX = mapValueToPixel(j == numValues
                    ? getMaximumAllowableValue() : getConstrainedThumbValue(j));
            lastX = endX;
            if ((startX >= getLeftInset() + getClientAreaWidth() - 2)
                    || (endX < getLeftInset() + 1)) {
                continue;
            }

            /*
             * Adjust the boundaries to ensure they fit within the visible area
             * of the track.
             */
            if (startX < getLeftInset() + 1) {
                startX = getLeftInset() + 1;
            }
            if (endX >= getLeftInset() + getClientAreaWidth() - 2) {
                endX = getLeftInset() + getClientAreaWidth() - 2;
            }

            /*
             * Stretch the track tile image horizontally to cover the entire
             * track area, using the disabled image if the widget is disabled.
             */
            e.gc.drawImage(
                    (isEnabled() ? trackTileImages.get(j)
                            : disabledTrackTileImage),
                    0, 0, 1, trackThickness - 2, startX, yTrack,
                    endX + 1 - startX, trackThickness - 2);
        }

        /*
         * Turn GC advanced mode back on now that image stretching is done.
         */
        e.gc.setAdvanced(advanced);

        /*
         * Draw the border around the track area.
         */
        e.gc.setForeground(TRACK_BORDER_COLOR);
        e.gc.drawRectangle(getLeftInset(), yTrack - 1, clientArea.width - 1,
                trackThickness - 1);

        /*
         * Iterate through the thumbs, drawing each if the value it represents
         * is currently visible on the display. Draw the thumb types in the
         * order called for by the current configuration, except for the
         * dragging thumb, if any, which is drawn after all the others.
         */
        ThumbSpecifier activeThumb = getActiveThumb();
        ThumbSpecifier draggingThumb = getDraggingThumb();
        for (ValueType type : getThumbTypeDrawingOrder()) {
            numValues = (type == ValueType.CONSTRAINED
                    ? getConstrainedThumbValueCount()
                    : getFreeThumbValueCount());
            for (int j = 0; j < numValues; j++) {
                if ((draggingThumb != null) && (draggingThumb.type == type)
                        && (draggingThumb.index == j)) {
                    continue;
                }
                paintThumb(type, j, (isEnabled()
                        ? (isVisuallyActive(activeThumb, type, j)
                                || isVisuallyActive(draggingThumb, type, j)
                                        ? ThumbState.ACTIVE : ThumbState.NORMAL)
                        : ThumbState.DISABLED), e.gc);
            }
        }
        if (draggingThumb != null) {
            paintThumb(draggingThumb.type, draggingThumb.index,
                    ThumbState.ACTIVE, e.gc);
        }

        /*
         * Reset the colors.
         */
        e.gc.setBackground(background);
        e.gc.setForeground(foreground);
    }

    @Override
    protected final ThumbSpecifier getEditableThumbForCoordinates(int x,
            int y) {

        /*
         * If the cursor is inside the client area, check to see if it is over
         * one of the editable thumbs by iterating through the latter, and
         * return the index of the thumb it is over, if this is the case.
         * Otherwise, return -1 to indicate that the coordinate is not over an
         * editable thumb.
         */
        if ((x >= 0)
                && (x < getClientAreaWidth() + getLeftInset() + getRightInset())
                && (y >= 0) && (y < getClientAreaHeight() + getTopInset()
                        + getBottomInset())) {
            for (ValueType type : getThumbTypeHitTestOrder()) {
                for (int j = 0; j < (type == ValueType.CONSTRAINED
                        ? getConstrainedThumbValueCount()
                        : getFreeThumbValueCount()); j++) {
                    if ((type == ValueType.CONSTRAINED
                            ? isConstrainedThumbEditable(j)
                            : isFreeThumbEditable(j)) == false) {
                        continue;
                    }
                    int thumbX = mapValueToPixel(type == ValueType.CONSTRAINED
                            ? getConstrainedThumbValue(j)
                            : getFreeThumbValue(j));
                    if ((x >= thumbX - thumbLongitudinalDimension / 2)
                            && (x < thumbX + thumbLongitudinalDimension
                                    - (thumbLongitudinalDimension / 2))
                            && (y >= getTopInset() + 1)
                            && (y < getTopInset() + getClientAreaHeight())) {
                        return new ThumbSpecifier(type, j);
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected final int getThumbTooltipVerticalOffsetFromTop(
            ThumbSpecifier thumb) {
        return getTopInset() + thumbLateralDimension + 1;
    }

    @Override
    protected final void tooltipBoundsChanged() {
        setTooltipBounds(null);
    }

    @Override
    protected final void widgetDisposed(DisposeEvent e) {
        for (Image image : trackTileImages) {
            if (image != null) {
                TRACK_TILE_IMAGE_CACHE.release(image);
            }
        }
        if (disabledTrackTileImage != null) {
            TRACK_TILE_IMAGE_CACHE.release(disabledTrackTileImage);
        }
        if (thumbImage != null) {
            THUMB_IMAGE_CACHE.release(thumbImage);
        }
        if (activeThumbImage != null) {
            THUMB_IMAGE_CACHE.release(activeThumbImage);
        }
        if (disabledThumbImage != null) {
            THUMB_IMAGE_CACHE.release(disabledThumbImage);
        }
        TRACK_BORDER_COLOR.dispose();
        TRACK_BORDER_SHADOW_COLOR.dispose();
        TRACK_BORDER_HIGHLIGHT_COLOR.dispose();
    }

    @Override
    protected final void constrainedThumbRangeColorChanged(int index,
            Color color) {
        while (trackTileImages.size() > index) {
            Image image = trackTileImages.remove(index);
            if (image != null) {
                TRACK_TILE_IMAGE_CACHE.release(image);
            }
        }
    }

    // Private Methods

    /**
     * Initialize the widget.
     */
    private void initialize() {

        /*
         * Calculate the font-relative pixel dimensions.
         */
        calculateFontRelativePixelDimensions();

        /*
         * Initialize the member data and the thumb images as well.
         */
        calculateThumbCornerArcSize();
        thumbImage = getThumbImage(ThumbState.NORMAL);
        activeThumbImage = getThumbImage(ThumbState.ACTIVE);
        disabledThumbImage = getThumbImage(ThumbState.DISABLED);
    }

    /**
     * Record the font height in pixels.
     */
    private void calculateFontRelativePixelDimensions() {
        GC sampleGC = new GC(this);
        float fontMultiplier = sampleGC.getFontMetrics().getHeight()
                / BASE_FONT_HEIGHT;
        sampleGC.dispose();
        thumbLongitudinalDimension = toPixels(baseThumbLongitudinalDimension,
                fontMultiplier);
        thumbLateralDimension = toPixels(baseThumbLateralDimension,
                fontMultiplier);
        trackThickness = Math.max(toPixels(baseTrackThickness, fontMultiplier),
                3);
    }

    /**
     * Convert the specified dimensional value to a pixel value. This is done by
     * multiplying it by the current font height divided by the base font
     * height.
     * 
     * @param dimensionalValue
     *            Dimensional value to be converted to a pixel value.
     * @return Pixel value.
     */
    private int toPixels(int dimensionalValue, float fontMultiplier) {
        return Math.round(dimensionalValue * fontMultiplier);
    }

    /**
     * Determine whether or not the specified thumb should look active.
     * 
     * @param thumb
     *            Thumb to be checked for an active look.
     * @param type
     *            Type of thumb currently being drawn; if <code>thumb</code> is
     *            not of this type, this method will return false.
     * @param index
     *            Index of the thumb currently being drawn; if
     *            <code>thumb</code> is not at this index, this method will
     *            return false.
     * @return True if the thumb should look active, false otherwise.
     */
    private boolean isVisuallyActive(ThumbSpecifier thumb, ValueType type,
            int index) {
        return ((thumb != null) && (thumb.type == type)
                && ((thumb.index == index) || ((type == ValueType.CONSTRAINED)
                        && isConstrainedThumbIntervalLocked())));
    }

    /**
     * Paint the specified thumb.
     * 
     * @param type
     *            Type of thumb to be painted.
     * @param index
     *            Index of the the thumb to be painted.
     * @param state
     *            State indicating whether the thumb is to be drawn as active,
     *            normal, or disabled.
     * @param gc
     *            Graphics context in which to draw the thumb.
     */
    private void paintThumb(ValueType type, int index, ThumbState state,
            GC gc) {
        int xThumb = mapValueToPixel(type == ValueType.CONSTRAINED
                ? getConstrainedThumbValue(index) : getFreeThumbValue(index));
        if ((xThumb >= getLeftInset())
                && (xThumb <= getLeftInset() + getClientAreaWidth())) {
            gc.drawImage(
                    (state == ThumbState.ACTIVE ? activeThumbImage
                            : (state == ThumbState.DISABLED ? disabledThumbImage
                                    : thumbImage)),
                    xThumb - (thumbLongitudinalDimension / 2),
                    getTopInset() + 1);
        }
    }

    /**
     * Get an image to be used for drawing the thumb(s) in the specified state.
     * 
     * @param state
     *            State of the thumb image to be fetched.
     * @return Thumb image.
     */
    private Image getThumbImage(ThumbState state) {

        /*
         * See if the cache has the image; if so, just use it instead of
         * creating one.
         */
        ThumbImageParameters parameters = new ThumbImageParameters(
                thumbLongitudinalDimension, thumbLateralDimension,
                thumbCornerArcSize, state);
        Image image = THUMB_IMAGE_CACHE.acquire(parameters);
        if (image != null) {
            return image;
        }

        /*
         * Since the cache did not have the image, it must be created. First,
         * create an AWT image, since such an image can be created with
         * transparency and painted onto with varying alpha levels. Then get its
         * graphics object and configure the latter.
         */
        BufferedImage awtImage = new BufferedImage(
                thumbLongitudinalDimension + 1, thumbLateralDimension + 1,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = awtImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        /*
         * Draw the drop shadow behind the thumb.
         */
        graphics.setColor(SHADOW_COLOR);
        graphics.drawRoundRect(1, 1, thumbLongitudinalDimension - 1,
                thumbLateralDimension - 1, thumbCornerArcSize,
                thumbCornerArcSize);

        /*
         * Determine whether or not the thumb is to be drawn as active, or as
         * disabled.
         */
        boolean active = (state == ThumbState.ACTIVE);
        boolean disabled = (state == ThumbState.DISABLED);

        /*
         * Paint the base background color fill of the thumb.
         */
        graphics.setColor(active ? BACKGROUND_SELECTION_COLOR
                : (disabled ? BACKGROUND_DISABLED_COLOR : BACKGROUND_COLOR));
        graphics.fillRoundRect(0, 0, thumbLongitudinalDimension,
                thumbLateralDimension, thumbCornerArcSize, thumbCornerArcSize);

        /*
         * Draw the gradient fill for the thumb background.
         */
        if (disabled == false) {
            paintGradient(graphics,
                    (active ? java.awt.Color.WHITE : BORDER_COLOR),
                    (active ? 0.75f : 0.05f), (active ? 0.10f : 0.14f), 1,
                    thumbLongitudinalDimension - 2, 1,
                    thumbLateralDimension - 2, thumbCornerArcSize / 2);
        }

        /*
         * Draw the lines bounding the center of the thumb, if the width and
         * height are different enough.
         */
        graphics.setColor(active ? DETAIL_SELECTION_COLOR : DETAIL_COLOR);
        if (thumbLongitudinalDimension + 2 <= thumbLateralDimension) {
            int indent = (thumbCornerArcSize
                    - (thumbLateralDimension - thumbLongitudinalDimension)) / 2;
            if (indent < 0) {
                indent = 0;
            }
            graphics.drawLine(indent,
                    (thumbLateralDimension - thumbLongitudinalDimension) / 2,
                    thumbLongitudinalDimension - (1 + indent),
                    (thumbLateralDimension - thumbLongitudinalDimension) / 2);
            graphics.drawLine(indent,
                    ((thumbLateralDimension - thumbLongitudinalDimension) / 2)
                            + thumbLongitudinalDimension - 1,
                    thumbLongitudinalDimension - (1 + indent),
                    ((thumbLateralDimension - thumbLongitudinalDimension) / 2)
                            + thumbLongitudinalDimension - 1);
        } else if (thumbLongitudinalDimension - 2 >= thumbLateralDimension) {
            int indent = (thumbCornerArcSize
                    - (thumbLongitudinalDimension - thumbLateralDimension)) / 2;
            if (indent < 0) {
                indent = 0;
            }
            graphics.drawLine(
                    (thumbLongitudinalDimension - thumbLateralDimension) / 2,
                    indent,
                    (thumbLongitudinalDimension - thumbLateralDimension) / 2,
                    thumbLateralDimension - (1 + indent));
            graphics.drawLine(
                    ((thumbLongitudinalDimension - thumbLateralDimension) / 2)
                            + thumbLateralDimension - 1,
                    indent,
                    ((thumbLongitudinalDimension - thumbLateralDimension) / 2)
                            + thumbLateralDimension - 1,
                    thumbLateralDimension - (1 + indent));
        }

        /*
         * Draw the background for the center of the thumb.
         */
        graphics.setColor(
                disabled ? BACKGROUND_DISABLED_CENTER_COLOR : BACKGROUND_COLOR);
        int xOffset = (thumbLongitudinalDimension > thumbLateralDimension
                ? (thumbLongitudinalDimension - thumbLateralDimension) / 2 : 0)
                + 1;
        int yOffset = (thumbLongitudinalDimension > thumbLateralDimension ? 0
                : (thumbLateralDimension - thumbLongitudinalDimension) / 2) + 1;
        graphics.fillRect(xOffset, yOffset,
                Math.min(thumbLongitudinalDimension, thumbLateralDimension) - 2,
                Math.min(thumbLongitudinalDimension, thumbLateralDimension)
                        - 2);

        /*
         * Draw the gradient fill for the center of the thumb.
         */
        if (disabled == false) {
            paintGradient(graphics, BORDER_COLOR, 0.04f, 0.25f, xOffset,
                    xOffset + Math.min(thumbLongitudinalDimension,
                            thumbLateralDimension) - 3,
                    yOffset, yOffset + Math.min(thumbLongitudinalDimension,
                            thumbLateralDimension) - 3,
                    0);
        }

        /*
         * Draw the pips on the center of the thumb.
         */
        if (thumbLongitudinalDimension < thumbLateralDimension) {
            int pipOffset = ((thumbLongitudinalDimension - 2) % 3) / 2;
            for (int y = ((thumbLateralDimension - thumbLongitudinalDimension)
                    / 2)
                    + 1
                    + pipOffset; y < ((thumbLateralDimension
                            - thumbLongitudinalDimension) / 2)
                            + thumbLongitudinalDimension - 2; y += 3) {
                for (int x = 1 + pipOffset; x < thumbLongitudinalDimension
                        - 2; x += 3) {
                    graphics.setColor(BACKGROUND_COLOR);
                    graphics.fillRect(x + 1, y + 1, 2, 2);
                    graphics.setColor(DETAIL_COLOR);
                    graphics.fillRect(x + 1, y + 1, 1, 1);
                }
            }
        } else {
            int pipOffset = ((thumbLateralDimension - 2) % 3) / 2;
            for (int y = 1 + pipOffset; y < thumbLateralDimension - 2; y += 3) {
                for (int x = ((thumbLongitudinalDimension
                        - thumbLateralDimension) / 2)
                        + 1
                        + pipOffset; x < ((thumbLongitudinalDimension
                                - thumbLateralDimension) / 2)
                                + thumbLateralDimension - 2; x += 3) {
                    graphics.setColor(BACKGROUND_COLOR);
                    graphics.fillRect(x + 1, y + 1, 2, 2);
                    graphics.setColor(DETAIL_COLOR);
                    graphics.fillRect(x + 1, y + 1, 1, 1);
                }
            }
        }

        /*
         * Draw the thumb border.
         */
        graphics.setColor(active ? BORDER_SELECTION_COLOR
                : (disabled ? BORDER_DISABLED_COLOR : BORDER_COLOR));
        graphics.drawRoundRect(0, 0, thumbLongitudinalDimension - 1,
                thumbLateralDimension - 1, thumbCornerArcSize,
                thumbCornerArcSize);

        /*
         * Finish up with the graphics object.
         */
        graphics.dispose();

        /*
         * Convert the image to an SWT image, add it to the cache, and return
         * it.
         */
        image = ImageUtilities.convertAwtImageToSwt(awtImage);
        THUMB_IMAGE_CACHE.add(parameters, image);
        return image;
    }

    /**
     * Get the image to be used for painting the track area by tiling it.
     * 
     * @param color
     *            Color to be used for the track area image, or
     *            <code>null</code> if the image should use the default color.
     * @return Track tile image.
     */
    private Image getTrackTileImage(Color color) {

        /*
         * See if the cache has the image; if so, just use it instead of
         * creating one.
         */
        Pair<Integer, RGB> parameters = new Pair<>(trackThickness,
                (color == null ? null
                        : new RGB(color.getRed(), color.getGreen(),
                                color.getBlue())));
        Image image = TRACK_TILE_IMAGE_CACHE.acquire(parameters);
        if (image != null) {
            return image;
        }

        /*
         * Since the cache does not have the image, it has to be created. First,
         * make note of whether the default image or a specifically colored
         * image is to be created, and get the color ready if the latter.
         */
        java.awt.Color awtColor = (color == null ? BORDER_COLOR
                : new java.awt.Color(color.getRed(), color.getGreen(),
                        color.getBlue()));

        /*
         * Create an AWT image, since such an image can be created with
         * transparency and painted onto with varying alpha levels. Then get its
         * graphics object.
         */
        BufferedImage awtImage = new BufferedImage(1, trackThickness - 2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = awtImage.createGraphics();

        /*
         * Draw the base background color of the track area.
         */
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, awtImage.getWidth(), awtImage.getHeight());

        /*
         * Paint the gradient fill overlay.
         */
        paintGradient(graphics, awtColor, (color == null ? 0.4f : 0.8f),
                (color == null ? 0.23f : 0.25f), 0, 1, 0, trackThickness - 2,
                0);

        /*
         * Finish up with the graphics object.
         */
        graphics.dispose();

        /*
         * Convert the image to an SWT image, add it to the cache, and return
         * it.
         */
        image = ImageUtilities.convertAwtImageToSwt(awtImage);
        TRACK_TILE_IMAGE_CACHE.add(parameters, image);
        return image;
    }

    /**
     * Paint the specified graphics with a vertical gradient using the specified
     * color but with the alpha varying between the specified starting and
     * ending values, over the rectangle described by the four X and Y boundary
     * values, and with the specified corner indent at the edges.
     * 
     * @param graphics
     *            Graphics object on which to draw.
     * @param color
     *            Color specifying RGB values to be used.
     * @param alphaStart
     *            Starting alpha for the gradient.
     * @param alphaEnd
     *            Ending alpha for the gradient.
     * @param xStart
     *            X start coordinate for the gradient.
     * @param xEnd
     *            X end coordinate for the gradient.
     * @param yStart
     *            Y start coordinate for the gradient.
     * @param yEnd
     *            Y end coordinate for the gradient.
     * @param cornerIndent
     *            Indent in pixels at the corners for rounded corners, or
     *            <code>0</code> if there is no indent.
     */
    private void paintGradient(Graphics2D graphics, java.awt.Color color,
            float alphaStart, float alphaEnd, int xStart, int xEnd, int yStart,
            int yEnd, int cornerIndent) {
        float[] rgbComponents = color.getColorComponents(null);
        float alphaOffset = ((alphaEnd - alphaStart)) / (yEnd + 1 - yStart);
        int yHalfway = ((yEnd + 1 - yStart) / 2) + yStart;
        for (int y = yStart; y <= yEnd; y++) {
            graphics.setColor(new java.awt.Color(rgbComponents[0],
                    rgbComponents[1], rgbComponents[2],
                    alphaStart + (alphaOffset * (y - yStart))));
            int xOffset = (cornerIndent)
                    - ((y >= yHalfway ? yEnd + 1 - y : y) + 1);
            if (xOffset < 0) {
                xOffset = 0;
            }
            graphics.drawLine(xStart + xOffset, y, xEnd - xOffset, y);
        }
    }

    /**
     * Calculate the thumb corner arc size.
     */
    private void calculateThumbCornerArcSize() {
        thumbCornerArcSize = Math.min(thumbLongitudinalDimension,
                thumbLateralDimension) / THUMB_ARC_SIZE_DIVIDER;
        if (thumbCornerArcSize % 2 == 1) {
            thumbCornerArcSize -= 1;
        }
    }

    /**
     * Update the track tile image.
     * 
     * @param index
     *            Index of the track tile image being updated; must be between 0
     *            and the number of thumbs inclusive.
     * @param color
     *            Color to use at the specified index.
     */
    private void updateTrackTileImage(int index, Color color) {
        while (trackTileImages.size() < index) {
            trackTileImages.add(getTrackTileImage(null));
        }
        if (trackTileImages.size() == index) {
            trackTileImages.add(getTrackTileImage(color));
        } else {
            if (trackTileImages.get(index) != null) {
                TRACK_TILE_IMAGE_CACHE.release(trackTileImages.get(index));
            }
            trackTileImages.set(index, getTrackTileImage(color));
        }
    }
}
