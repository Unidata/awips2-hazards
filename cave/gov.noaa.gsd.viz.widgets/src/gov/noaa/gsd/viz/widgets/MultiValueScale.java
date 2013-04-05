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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MultiValueScale extends MultiValueLinearControl {

    // Private Static Constants

    /**
     * Default longitudinal dimension of a thumb in pixels.
     */
    private static final int DEFAULT_THUMB_LONGITUDINAL_DIMENSION = 13;

    /**
     * Default lateral dimension of a thumb in pixels.
     */
    private static final int DEFAULT_THUMB_LATERAL_DIMENSION = 27;

    /**
     * Corner arc divisor, by which the longest of the two thumb dimensions is
     * divided to yield the thumb corner arc size in pixels.
     */
    private static final int THUMB_ARC_SIZE_DIVIDER = 2;

    /**
     * Thickness of the track in pixels.
     */
    private static final int DEFAULT_TRACK_THICKNESS = 5;

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

    // Initialize the colors.
    static {
        Color color = Display.getCurrent().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND);
        BACKGROUND_COLOR = new java.awt.Color(color.getRed(), color.getGreen(),
                color.getBlue());
    }

    // Private Enumerated Types

    /**
     * State of a thumb.
     */
    private enum ThumbState {
        DISABLED, NORMAL, ACTIVE
    };

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
     * axis parallel to the track.
     */
    private int thumbLongitudinalDimension = DEFAULT_THUMB_LONGITUDINAL_DIMENSION;

    /**
     * Lateral dimension of the thumbs, this being the dimension along the axis
     * perpendicular to the track.
     */
    private int thumbLateralDimension = DEFAULT_THUMB_LATERAL_DIMENSION;

    /**
     * Thumb corner arc size, used to round the corners of the thumb.
     */
    private int thumbCornerArcSize = 0;

    /**
     * Thickness of the track.
     */
    private int trackThickness = DEFAULT_TRACK_THICKNESS;

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

        // Initialize the widget.
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

        // Initialize the widget.
        initialize();
    }

    // Public Methods

    /**
     * Determine whether or not the viewport is draggable via a mouse click and
     * drag. This implementation always returns false.
     * 
     * @return True if the viewport is draggable, false otherwise.
     */
    @Override
    public final boolean isViewportDraggable() {
        return false;
    }

    /**
     * Get the longitudinal dimension of the thumb. The longitudinal dimension
     * is the dimension of the thumb along the axis parallel to the track.
     * 
     * @return Longitudinal dimension of the thumb.
     */
    public final int getThumbLongitudinalDimension() {
        return thumbLongitudinalDimension;
    }

    /**
     * Get the lateral dimension of the thumb. The lateral dimension is the
     * dimension of the thumb along the axis perpendicular to the track.
     * 
     * @return Lateral dimension of the thumb.
     */
    public final int getThumbLateralDimension() {
        return thumbLateralDimension;
    }

    /**
     * Get the track thickness.
     * 
     * @return Track thickness.
     */
    public final int getTrackThickness() {
        return trackThickness;
    }

    /**
     * Set the dimensions of the visual components of the widget. Note that for
     * best visual results, the longitudinal dimension of the thumb must be an
     * odd number, and the lateral thumb dimension and track thickness must both
     * be odd or both be even.
     * 
     * @param thumbLongitudinal
     *            Longitudinal dimension of the thumb, this being the dimension
     *            along the axis parallel to the track. This value must be an
     *            odd positive integer.
     * @param thumbLateral
     *            Lateral dimension of the thumb, this being the dimension along
     *            the axis perpendicular to the track.
     * @param trackThickness
     *            Thickness of the track; must be 3 or more.
     */
    public final void setComponentDimensions(int thumbLongitudinal,
            int thumbLateral, int trackThickness) {

        // Remember the new component dimensions.
        boolean thumbChanged = ((this.thumbLongitudinalDimension != thumbLongitudinal) || (this.thumbLateralDimension != thumbLateral));
        this.thumbLongitudinalDimension = thumbLongitudinal;
        this.thumbLateralDimension = thumbLateral;
        boolean trackChanged = (this.trackThickness != trackThickness);
        this.trackThickness = trackThickness;
        if (this.trackThickness < 3) {
            throw new IllegalArgumentException(
                    "track thickness must be 3 or greater");
        }

        // Recreate the track tile images using the new thickness, if the
        // thickness changed.
        if (trackChanged) {
            for (int j = 0; j < trackTileImages.size(); j++) {
                if (trackTileImages.get(j) != null) {
                    trackTileImages.get(j).dispose();
                }
                trackTileImages.set(j,
                        createTrackTileImage(getConstrainedThumbRangeColor(j)));
            }
            disabledTrackTileImage = createTrackTileImage(null);
        }

        // Recreate the thumb images using the new dimensions, if the di-
        // mensions changed.
        if (thumbChanged) {
            calculateThumbCornerArcSize();
            if (thumbImage != null) {
                thumbImage.dispose();
            }
            thumbImage = createThumbImage(ThumbState.NORMAL);
            if (activeThumbImage != null) {
                activeThumbImage.dispose();
            }
            activeThumbImage = createThumbImage(ThumbState.ACTIVE);
            if (disabledThumbImage != null) {
                disabledThumbImage.dispose();
            }
            disabledThumbImage = createThumbImage(ThumbState.DISABLED);
        }

        // Recalculate the preferred size and redraw the widget if a change
        // in one of the dimensions occurred.
        if (trackChanged || thumbChanged) {
            computePreferredSize(true);
            redraw();
        }
    }

    // Protected Methods

    /**
     * Respond to a change in the enabled state of the widget. Subclasses should
     * implement this method to change the visual cues to indicate whether or
     * not the widget is enabled.
     */
    @Override
    protected final void widgetEnabledStateChanged() {
        redraw();
    }

    /**
     * Calculate the preferred size of the widget.
     * 
     * @param force
     *            Flag indicating whether or not the preferred size should be
     *            recomputed if already found to be computed.
     */
    @Override
    protected final void computePreferredSize(boolean force) {

        // Compute the preferred size only if it has not yet been computed,
        // or if being forced to do it regardless of previous computations.
        if (force || (getPreferredWidth() == 0)) {
            int preferredWidth = (thumbLongitudinalDimension * getConstrainedThumbValueCount())
                    + getLeftInset() + getRightInset();
            int preferredHeight = thumbLateralDimension + 2 + getTopInset()
                    + getBottomInset();
            setPreferredSize(preferredWidth, preferredHeight);
        }
    }

    /**
     * Paint the widget.
     * 
     * @param e
     *            Event that triggered this invocation.
     */
    @Override
    protected final void paintControl(PaintEvent e) {

        // Do nothing if the client width or height are 0.
        if ((getClientAreaWidth() == 0) || (getClientAreaHeight() == 0)) {
            return;
        }

        // Calculate the preferred size if needed, and determine the
        // width and height to be used when painting this time around.
        computePreferredSize(false);
        Rectangle clientArea = getClientArea();

        // Get the default colors, as they are needed later.
        Color background = e.gc.getBackground();
        Color foreground = e.gc.getForeground();

        // Draw the background.
        if (e.gc.getBackground() != null) {
            e.gc.fillRectangle(clientArea);
        }

        // Ensure that the drawing does not occur in the horizontal inset
        // area, only in the client area and the vertical padding area
        // above and below the client area.
        e.gc.setClipping(clientArea.x, 0, clientArea.width, getTopInset()
                + clientArea.height + getBottomInset());

        // Iterate through the constrained marked value indicators, draw-
        // ing the ranges between them and at either end.
        int lastMarkedValueX = mapValueToPixel(getMinimumAllowableValue()) - 1;
        for (int j = 0; j <= getConstrainedMarkedValueCount(); j++) {
            int markedValueX = mapValueToPixel(j == getConstrainedMarkedValueCount() ? getMaximumAllowableValue()
                    : getConstrainedMarkedValue(j))
                    + (j == getConstrainedMarkedValueCount() ? 1 : 0);
            Color color = getConstrainedMarkedRangeColor(j);
            if (color != null) {
                e.gc.setBackground(color);
                e.gc.fillRectangle(lastMarkedValueX + 1, 0, markedValueX
                        - lastMarkedValueX, getTopInset() + clientArea.height
                        + getBottomInset());
            }
            lastMarkedValueX = markedValueX;
        }

        // Reset the clipping region.
        e.gc.setClipping((Rectangle) null);

        // Determine the Y coordinate where the track will be drawn.
        int yTrack = getTopInset() + 2
                + ((thumbLateralDimension - trackThickness) / 2);

        // Draw the shadowed and highlighted lines around where the
        // track border will be in order to make it look more three-
        // dimensional.
        e.gc.setForeground(TRACK_BORDER_SHADOW_COLOR);
        e.gc.drawLine(getLeftInset() - 1, yTrack - 2, getLeftInset()
                + clientArea.width, yTrack - 2);
        e.gc.setForeground(TRACK_BORDER_HIGHLIGHT_COLOR);
        e.gc.drawLine(getLeftInset() - 1, yTrack + trackThickness - 1,
                getLeftInset() + clientArea.width, yTrack + trackThickness - 1);

        // Iterate through the marked value indicators, drawing any that
        // are visible. Draw the marked value types in the order called
        // for by the current configuration.
        e.gc.setAntialias(SWT.ON);
        for (ValueType type : getMarkTypeDrawingOrder()) {
            int numValues = (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueCount()
                    : getFreeMarkedValueCount());
            for (int j = 0; j < numValues; j++) {
                int markedValueX = mapValueToPixel(type == ValueType.CONSTRAINED ? getConstrainedMarkedValue(j)
                        : getFreeMarkedValue(j));
                if ((markedValueX >= clientArea.x)
                        && (markedValueX < clientArea.x + clientArea.width)) {
                    Color color = (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueColor(j)
                            : getFreeMarkedValueColor(j));
                    e.gc.setForeground(color == null ? foreground : color);
                    e.gc.drawLine(markedValueX, -1, markedValueX, getTopInset()
                            + clientArea.height + getBottomInset());
                }
            }
        }

        // Turn off interpolation so that the stretched track images
        // used below do not get translucent parts at their edges.
        int interpolation = e.gc.getInterpolation();
        e.gc.setInterpolation(SWT.NONE);

        // Iterate through the gaps between the thumbs and between the
        // start and end thumbs and their respective ends of the track,
        // drawing the track for that section using the appropriate
        // image via tiling.
        int numValues = getConstrainedThumbValueCount();
        int lastX = mapValueToPixel(getMinimumAllowableValue());
        if (disabledTrackTileImage == null) {
            disabledTrackTileImage = createTrackTileImage(null);
        }
        for (int j = 0; j <= numValues; j++) {

            // Create the image for this portion of the track, if
            // it has not already been created.
            if (j >= trackTileImages.size()) {
                updateTrackTileImage(j, getConstrainedThumbRangeColor(j));
            }

            // Determine the X boundaries for this section of the track,
            // and if they are both offscreen, remember to skip the ac-
            // tual drawing of this section.
            int startX = lastX;
            int endX = mapValueToPixel(j == numValues ? getMaximumAllowableValue()
                    : getConstrainedThumbValue(j));
            lastX = endX;
            if ((startX >= getLeftInset() + getClientAreaWidth() - 2)
                    || (endX < getLeftInset() + 1)) {
                continue;
            }

            // Adjust the boundaries to ensure they fit within the
            // visible area of the track.
            if (startX < getLeftInset() + 1) {
                startX = getLeftInset() + 1;
            }
            if (endX >= getLeftInset() + getClientAreaWidth() - 2) {
                endX = getLeftInset() + getClientAreaWidth() - 2;
            }

            // Stretch the track tile image horizontally to cover the
            // entire track area, using the disabled image if the wid-
            // get is disabled.
            e.gc.drawImage((isEnabled() ? trackTileImages.get(j)
                    : disabledTrackTileImage), 0, 0, 1, trackThickness - 2,
                    startX, yTrack, endX + 1 - startX, trackThickness - 2);
        }

        // Set interpolation back to what it was before drawing track
        // images.
        e.gc.setInterpolation(interpolation);

        // Draw the border around the track area.
        e.gc.setForeground(TRACK_BORDER_COLOR);
        e.gc.drawRectangle(getLeftInset(), yTrack - 1, clientArea.width - 1,
                trackThickness - 1);

        // Iterate through the thumbs, drawing each if the value it re-
        // presents is currently visible on the display. Draw the thumb
        // types in the order called for by the current configuration,
        // except for the dragging thumb, if any, which is drawn after
        // all the others.
        ThumbSpecifier activeThumb = getActiveThumb();
        ThumbSpecifier draggingThumb = getDraggingThumb();
        for (ValueType type : getThumbTypeDrawingOrder()) {
            numValues = (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                    : getFreeThumbValueCount());
            for (int j = 0; j < numValues; j++) {
                if ((draggingThumb != null) && (draggingThumb.type == type)
                        && (draggingThumb.index == j)) {
                    continue;
                }
                paintThumb(
                        type,
                        j,
                        (isEnabled() ? (((activeThumb != null)
                                && (activeThumb.type == type) && (activeThumb.index == j))
                                || ((draggingThumb != null)
                                        && (draggingThumb.type == type) && (draggingThumb.index == j)) ? ThumbState.ACTIVE
                                : ThumbState.NORMAL)
                                : ThumbState.DISABLED), e.gc);
            }
        }
        if (draggingThumb != null) {
            paintThumb(draggingThumb.type, draggingThumb.index,
                    ThumbState.ACTIVE, e.gc);
        }

        // Reset the colors.
        e.gc.setBackground(background);
        e.gc.setForeground(foreground);
    }

    /**
     * Determine which thumb the specified mouse event occurred within the
     * bounds of, if any.
     * 
     * @param e
     *            Mouse event.
     * @return Specifier of the thumb over which the event occurred, or
     *         <code>null</code> if the event did not occur over a thumb.
     */
    @Override
    protected final ThumbSpecifier getThumbForMouseEvent(MouseEvent e) {

        // If the cursor is inside the client area, check to see if it
        // is over one of the thumbs by iterating through the latter,
        // and return the index of the thumb it is over, if this is
        // the case. Otherwise, return -1 to indicate that the event
        // did not occur over a thumb.
        if ((e.x >= 0)
                && (e.x < getClientAreaWidth() + getLeftInset()
                        + getRightInset())
                && (e.y >= 0)
                && (e.y < getClientAreaHeight() + getTopInset()
                        + getBottomInset())) {
            for (ValueType type : getThumbTypeHitTestOrder()) {
                for (int j = 0; j < (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                        : getFreeThumbValueCount()); j++) {
                    int thumbX = mapValueToPixel(type == ValueType.CONSTRAINED ? getConstrainedThumbValue(j)
                            : getFreeThumbValue(j));
                    if ((e.x >= thumbX - thumbLongitudinalDimension / 2)
                            && (e.x < thumbX + thumbLongitudinalDimension
                                    - (thumbLongitudinalDimension / 2))
                            && (e.y >= getTopInset() + 1)
                            && (e.y < getTopInset() + getClientAreaHeight())) {
                        return new ThumbSpecifier(type, j);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the vertical offset from the top of the widget that is where the top
     * of a tooltip being displayed for a thumb is located.
     * 
     * @return Vertical offset in pixels.
     */
    @Override
    protected final int getThumbTooltipVerticalOffsetFromTop(
            ThumbSpecifier thumb) {
        return getTopInset() + thumbLateralDimension + 1;
    }

    /**
     * Respond to notification that a change may have occurred in the boundary
     * of the area in which a mouse hover may generate a tooltip showing the
     * value under the mouse. Since multi-value scale widgets never show
     * tooltips along the track, this implementation simply sets the tooltip
     * bounds to <code>
     * null</code>.
     * <p>
     * If this class is changed to show tooltip bounds along the track, this
     * method will have to be called whenever the track size changes.
     */
    @Override
    protected final void tooltipBoundsChanged() {
        setTooltipBounds(null);
    }

    /**
     * Respond to the disposal of the widget.
     * 
     * @param e
     *            Disposal event that triggered this invocation.
     */
    @Override
    protected final void widgetDisposed(DisposeEvent e) {
        for (Image image : trackTileImages) {
            if (image != null) {
                image.dispose();
            }
        }
        if (disabledTrackTileImage != null) {
            disabledTrackTileImage.dispose();
        }
        if (thumbImage != null) {
            thumbImage.dispose();
        }
        if (activeThumbImage != null) {
            activeThumbImage.dispose();
        }
        if (disabledThumbImage != null) {
            disabledThumbImage.dispose();
        }
        TRACK_BORDER_COLOR.dispose();
        TRACK_BORDER_SHADOW_COLOR.dispose();
        TRACK_BORDER_HIGHLIGHT_COLOR.dispose();
    }

    // Private Methods

    /**
     * Initialize the widget.
     */
    private void initialize() {

        // Initialize the member data and the thumb images as well.
        calculateThumbCornerArcSize();
        thumbImage = createThumbImage(ThumbState.NORMAL);
        activeThumbImage = createThumbImage(ThumbState.ACTIVE);
        disabledThumbImage = createThumbImage(ThumbState.DISABLED);
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
    private void paintThumb(ValueType type, int index, ThumbState state, GC gc) {
        int xThumb = mapValueToPixel(type == ValueType.CONSTRAINED ? getConstrainedThumbValue(index)
                : getFreeThumbValue(index));
        if ((xThumb >= getLeftInset())
                && (xThumb <= getLeftInset() + getClientAreaWidth())) {
            gc.drawImage((state == ThumbState.ACTIVE ? activeThumbImage
                    : (state == ThumbState.DISABLED ? disabledThumbImage
                            : thumbImage)), xThumb
                    - (thumbLongitudinalDimension / 2), getTopInset() + 1);
        }
    }

    /**
     * Create a image to be used for drawing the thumb(s).
     * 
     * @param state
     *            State of the thumb to be created.
     * @return Image that was created.
     */
    private Image createThumbImage(ThumbState state) {

        // Create an AWT image, since such an image can be created with
        // transparency and painted onto with varying alpha levels.
        // Then get its graphics object and configure the latter.
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

        // Draw the drop shadow behind the thumb.
        graphics.setColor(SHADOW_COLOR);
        graphics.drawRoundRect(1, 1, thumbLongitudinalDimension - 1,
                thumbLateralDimension - 1, thumbCornerArcSize,
                thumbCornerArcSize);

        // Determine whether or not the thumb is to be drawn as active,
        // or as disabled.
        boolean active = (state == ThumbState.ACTIVE);
        boolean disabled = (state == ThumbState.DISABLED);

        // Paint the base background color fill of the thumb.
        graphics.setColor(active ? BACKGROUND_SELECTION_COLOR
                : (disabled ? BACKGROUND_DISABLED_COLOR : BACKGROUND_COLOR));
        graphics.fillRoundRect(0, 0, thumbLongitudinalDimension,
                thumbLateralDimension, thumbCornerArcSize, thumbCornerArcSize);

        // Draw the gradient fill for the thumb background.
        if (disabled == false) {
            paintGradient(graphics, (active ? java.awt.Color.WHITE
                    : BORDER_COLOR), (active ? 0.75f : 0.05f), (active ? 0.10f
                    : 0.14f), 1, thumbLongitudinalDimension - 2, 1,
                    thumbLateralDimension - 2, thumbCornerArcSize / 2);
        }

        // Draw the lines bounding the center of the thumb, if the
        // width and height are different enough.
        graphics.setColor(active ? DETAIL_SELECTION_COLOR : DETAIL_COLOR);
        if (thumbLongitudinalDimension + 2 <= thumbLateralDimension) {
            int indent = (thumbCornerArcSize - (thumbLateralDimension - thumbLongitudinalDimension)) / 2;
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
            int indent = (thumbCornerArcSize - (thumbLongitudinalDimension - thumbLateralDimension)) / 2;
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
                            + thumbLateralDimension - 1, indent,
                    ((thumbLongitudinalDimension - thumbLateralDimension) / 2)
                            + thumbLateralDimension - 1, thumbLateralDimension
                            - (1 + indent));
        }

        // Draw the background for the center of the thumb.
        graphics.setColor(disabled ? BACKGROUND_DISABLED_CENTER_COLOR
                : BACKGROUND_COLOR);
        int xOffset = (thumbLongitudinalDimension > thumbLateralDimension ? (thumbLongitudinalDimension - thumbLateralDimension) / 2
                : 0) + 1;
        int yOffset = (thumbLongitudinalDimension > thumbLateralDimension ? 0
                : (thumbLateralDimension - thumbLongitudinalDimension) / 2) + 1;
        graphics.fillRect(
                xOffset,
                yOffset,
                Math.min(thumbLongitudinalDimension, thumbLateralDimension) - 2,
                Math.min(thumbLongitudinalDimension, thumbLateralDimension) - 2);

        // Draw the gradient fill for the center of the thumb.
        if (disabled == false) {
            paintGradient(
                    graphics,
                    BORDER_COLOR,
                    0.04f,
                    0.25f,
                    xOffset,
                    xOffset
                            + Math.min(thumbLongitudinalDimension,
                                    thumbLateralDimension) - 3,
                    yOffset,
                    yOffset
                            + Math.min(thumbLongitudinalDimension,
                                    thumbLateralDimension) - 3, 0);
        }

        // Draw the pips on the center of the thumb.
        if (thumbLongitudinalDimension < thumbLateralDimension) {
            int pipOffset = ((thumbLongitudinalDimension - 2) % 3) / 2;
            for (int y = ((thumbLateralDimension - thumbLongitudinalDimension) / 2)
                    + 1 + pipOffset; y < ((thumbLateralDimension - thumbLongitudinalDimension) / 2)
                    + thumbLongitudinalDimension - 2; y += 3) {
                for (int x = 1 + pipOffset; x < thumbLongitudinalDimension - 2; x += 3) {
                    graphics.setColor(BACKGROUND_COLOR);
                    graphics.fillRect(x + 1, y + 1, 2, 2);
                    graphics.setColor(DETAIL_COLOR);
                    graphics.fillRect(x + 1, y + 1, 1, 1);
                }
            }
        } else {
            int pipOffset = ((thumbLateralDimension - 2) % 3) / 2;
            for (int y = 1 + pipOffset; y < thumbLateralDimension - 2; y += 3) {
                for (int x = ((thumbLongitudinalDimension - thumbLateralDimension) / 2)
                        + 1 + pipOffset; x < ((thumbLongitudinalDimension - thumbLateralDimension) / 2)
                        + thumbLateralDimension - 2; x += 3) {
                    graphics.setColor(BACKGROUND_COLOR);
                    graphics.fillRect(x + 1, y + 1, 2, 2);
                    graphics.setColor(DETAIL_COLOR);
                    graphics.fillRect(x + 1, y + 1, 1, 1);
                }
            }
        }

        // Draw the thumb border.
        graphics.setColor(active ? BORDER_SELECTION_COLOR
                : (disabled ? BORDER_DISABLED_COLOR : BORDER_COLOR));
        graphics.drawRoundRect(0, 0, thumbLongitudinalDimension - 1,
                thumbLateralDimension - 1, thumbCornerArcSize,
                thumbCornerArcSize);

        // Finish up with the graphics object.
        graphics.dispose();

        // Convert the image to an SWT image and return it.
        return ImageUtilities.convertAwtImageToSwt(awtImage);
    }

    /**
     * Create image to be used for painting the track area by tiling it.
     * 
     * @param color
     *            Color to be used for the track area image, or
     *            <code>null</code> if the image should use the default color.
     * @return Image that was created.
     */
    private Image createTrackTileImage(Color color) {

        // Make note of whether the default image or a specifically
        // colored image is to be created, and get the color ready
        // if the latter.
        java.awt.Color awtColor = (color == null ? BORDER_COLOR
                : new java.awt.Color(color.getRed(), color.getGreen(),
                        color.getBlue()));

        // Create an AWT image, since such an image can be created
        // with transparency and painted onto with varying alpha
        // levels. Then get its graphics object.
        BufferedImage awtImage = new BufferedImage(1, trackThickness - 2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = awtImage.createGraphics();

        // Draw the base background color of the track area.
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, awtImage.getWidth(), awtImage.getHeight());

        // Paint the gradient fill overlay.
        paintGradient(graphics, awtColor, (color == null ? 0.4f : 0.8f),
                (color == null ? 0.23f : 0.25f), 0, 1, 0, trackThickness - 2, 0);

        // Finish up with the graphics object.
        graphics.dispose();

        // Convert the image to an SWT image and return it.
        return ImageUtilities.convertAwtImageToSwt(awtImage);
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
                    rgbComponents[1], rgbComponents[2], alphaStart
                            + (alphaOffset * (y - yStart))));
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
            trackTileImages.add(createTrackTileImage(null));
        }
        if (trackTileImages.size() == index) {
            trackTileImages.add(createTrackTileImage(color));
        } else {
            if (trackTileImages.get(index) != null) {
                trackTileImages.get(index).dispose();
            }
            trackTileImages.set(index, createTrackTileImage(color));
        }
    }
}
