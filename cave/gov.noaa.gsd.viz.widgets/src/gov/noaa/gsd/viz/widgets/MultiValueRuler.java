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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * Multi-value ruler, providing the display of a linear "ruler" that may be
 * scrolled along, or have its scale expanded or contracted. The ruler line is
 * visually divided up according to the <code>HatchMark</code> specifiers it is
 * provided.
 * <p>
 * Each ruler may have zero or more marked values, meaning values that are
 * visually marked off along its length using colors as specified. Such values
 * can only be changed programmatically; they cannot be moved via mouse events.
 * Each ruler may also have zero or more constrained thumbs (constrained meaning
 * that they must be specified in increasing order, and may never be pushed past
 * their neighboring constrained thumbs, if any) representing movable values;
 * these thumbs may be represented either as normal thumbs, or as half-width
 * "bookend" thumbs, each pair of which is drawn so as to visually book-end the
 * range between the paired thumbs. Finally, each ruler may include zero or more
 * free thumbs, that is, thumbs with positions that are not constrained by those
 * of their neighbors, and may thus be specified out of order, and moved past
 * one another.
 * <p>
 * The ruler is painted using the foreground color for the border and for text
 * labels, and the background color for the background fill. The various hatch
 * marks are drawn using the colors specified in their <code>HatchMark</code>
 * specifiers. Marked values are drawn using the colors specified for them, as
 * are marked value ranges, constrained value thumbs, constrained value ranges,
 * and free value thumbs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MultiValueRuler extends MultiValueLinearControl {

    // Private Static Constants

    /**
     * Default visible value zoom calculator; this merely zooms in or out by a
     * small increment.
     */
    private static final IVisibleValueZoomCalculator DEFAULT_VISIBLE_VALUE_ZOOM_CALCULATOR = new IVisibleValueZoomCalculator() {
        @Override
        public long getVisibleValueRangeForZoom(MultiValueRuler ruler,
                boolean zoomIn, int amplitude) {
            double multiplier = 1.0;
            while (amplitude-- > 0) {
                multiplier *= (zoomIn ? 0.8 : 1.25);
            }
            return (long) ((multiplier * amplitude * (ruler
                    .getUpperVisibleValue() + 1L - ruler.getLowerVisibleValue())) + 0.5);
        }
    };

    /**
     * Minimum number of pixels between adjacent vertical ruler lines.
     */
    private static final int MIN_RULER_LINE_INTERVAL = 8;

    /**
     * Color of the background of the widget; this and other window colors
     * should be fetchable via <code>Display.getSystemColor()</code>, but the
     * colors returned by that method seem to have no relationship to the colors
     * used on the same display for other (standard) widgets such as the SWT
     * <code>Scale</code>, thus the colors defined here are hard-coded.
     * Apparently this has to do with skins, which do not report their colors to
     * SWT...
     */
    private static final java.awt.Color BACKGROUND_COLOR = new java.awt.Color(
            239, 238, 235);

    /**
     * Color of the selected area of the widget thumb.
     */
    private static final java.awt.Color BACKGROUND_SELECTION_COLOR = new java.awt.Color(
            118, 175, 227);

    /**
     * Color of the border of the widget thumb.
     */
    private static final java.awt.Color THUMB_BORDER_COLOR = new java.awt.Color(
            131, 120, 103);

    /**
     * Color of the border of the widget thumb when selected.
     */
    private static final java.awt.Color THUMB_BORDER_SELECTION_COLOR = new java.awt.Color(
            57, 98, 137);

    /**
     * Color of the shadow of the widget thumb.
     */
    private static final java.awt.Color THUMB_SHADOW_COLOR = new java.awt.Color(
            131, 120, 103, 64);

    /**
     * Default height multiplier of the widget.
     */
    private static final float DEFAULT_HEIGHT_MULTIPLIER = 4.0f;

    // Public Enumerated Types

    /**
     * Indicator (thumb or marker) pointer directions, the possible directions
     * in which indicators may be pointed.
     */
    public enum IndicatorDirection {

        // Values

        /**
         * Value indicating an indicator should point upwards.
         */
        UP,

        /**
         * Value indicating an indicator should point downwards.
         */
        DOWN
    }

    // Private Variables

    /**
     * Flag indicating whether or not the viewport is draggable via a mouse
     * click and drag.
     */
    private boolean isViewportDraggable = false;

    /**
     * Visible value zoom calculator, used to determine the new visible value
     * viewport to be used when the user zooms in or out via a mouse or keyboard
     * action.
     */
    private IVisibleValueZoomCalculator visibleValueZoomCalculator = DEFAULT_VISIBLE_VALUE_ZOOM_CALCULATOR;

    /**
     * Border color, or <code>null</code> if the border is simply the color of
     * the foreground.
     */
    private Color borderColor = null;

    /**
     * Map pairing marked value types to lists of pointing directions for those
     * marked values; the pointing direction at each index of each contained
     * list corresponds to the marked value of that type at the same index, and
     * indicates which direction that marked value points.
     */
    private Map<ValueType, List<IndicatorDirection>> markedValueDirectionsForTypes = new HashMap<ValueType, List<IndicatorDirection>>();

    /**
     * Map pairing marked value types to lists of heights for those marked
     * values; the height at each index of each contained list corresponds to
     * the marked value of that type at the same index, and indicates how far
     * from the base that marked value is to be drawn (normalized to between 0.0
     * and 1.0).
     */
    private Map<ValueType, List<Float>> markedValueHeightsForTypes = new HashMap<ValueType, List<Float>>();

    /**
     * Flag indicating whether or not the thumbs should be drawn as book-ends
     * for ranges in between them. If this flag is true, the thumbs are rendered
     * differently from the way they would be drawn if it was false.
     */
    private boolean constrainedThumbsAreBookends = false;

    /**
     * Map pairing thumb types to lists of colors for those thumbs; the color at
     * each index of each contained list corresponds to the thumb value of that
     * type at the same index. If a color is <code>
     * null</code>, a default color is used for that value's thumb.
     */
    private Map<ValueType, List<Color>> thumbColorsForTypes = new HashMap<ValueType, List<Color>>();

    /**
     * Map pairing thumb types to lists of pointing directions for those thumbs;
     * the pointing direction at each index of each contained list corresponds
     * to the thumb value of that type at the same index, and indicates which
     * direction that thumb points.
     */
    private Map<ValueType, List<IndicatorDirection>> thumbDirectionsForTypes = new HashMap<ValueType, List<IndicatorDirection>>();

    /**
     * Map pairing thumb types to lists of height specifiers for those thumbs;
     * the height specifier at each index of each contained list corresponds to
     * the thumb value of that type at the same index, and indicates how far
     * from the bottom the thumb should be drawn, with 0.0 indicating the bottom
     * of the ruler and 1.0 the top.
     */
    private Map<ValueType, List<Float>> thumbHeightsForTypes = new HashMap<ValueType, List<Float>>();

    /**
     * Map pairing thumb types to lists of thumb images for those thumbs; the
     * image at each index of each contained list corresponds to the thumb value
     * of that type at the same index when that thumb is not active.
     */
    private Map<ValueType, List<Image>> thumbImagesForTypes = new HashMap<ValueType, List<Image>>();

    /**
     * Map pairing thumb types to lists of active thumb images for those thumbs;
     * the image at each index of each contained list corresponds to the thumb
     * value of that type at the same index when that thumb is active.
     */
    private Map<ValueType, List<Image>> activeThumbImagesForTypes = new HashMap<ValueType, List<Image>>();

    /**
     * Bounds of a thumb.
     */
    private Rectangle imageBounds = null;

    /**
     * Height multiplier; the product of this and the font height gives the
     * preferred height of the widget's client area.
     */
    private float heightMultiplier = DEFAULT_HEIGHT_MULTIPLIER;

    /**
     * List of hatch mark groups, specifying the hatch marks that are to be
     * drawn on when painting the widget. The groups must be ordered from
     * largest to smallest intervals.
     */
    private List<IHatchMarkGroup> hatchMarkGroups = null;

    /**
     * Half of the width of the marked value or thumb triangle.
     */
    private int halfTriangleWidth;

    /**
     * Array of alternating X and Y coordinates used when rendering a triangle.
     * This is kept around only to avoid repeated allocations, not because it is
     * state.
     */
    private int[] triangleCoordinates = new int[6];

    /**
     * Preferred client area height.
     */
    private int preferredClientHeight = 0;

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
    public MultiValueRuler(Composite parent, long minimumValue,
            long maximumValue) {
        super(parent, minimumValue, maximumValue);

        // Initialize the widget.
        initialize(new ArrayList<IHatchMarkGroup>());
    }

    /**
     * Construct a standard instance with a resize behavior of <code>
     * ResizeBehavior.CHANGE_PIXELS_PER_TIME_UNIT</code>.
     * 
     * @param parent
     *            Parent of this widget.
     * @param minimumValue
     *            Absolute minimum possible value.
     * @param maximumValue
     *            Absolute maximum possible value.
     * @param hatchMarkGroups
     *            List of hatch mark groups.
     */
    public MultiValueRuler(Composite parent, long minimumValue,
            long maximumValue, List<IHatchMarkGroup> hatchMarkGroups) {
        super(parent, minimumValue, maximumValue);

        // Initialize the widget.
        initialize(hatchMarkGroups);
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
     * @param hatchMarkGroups
     *            List of hatch mark groups.
     * @param heightMultiplier
     *            Height multiplier; the product of this and the font height is
     *            the preferred height of the client area of the constructed
     *            widget.
     * @param resizeBehavior
     *            Resize behavior.
     */
    public MultiValueRuler(Composite parent, long minimumValue,
            long maximumValue, List<IHatchMarkGroup> hatchMarkGroups,
            float heightMultiplier, ResizeBehavior resizeBehavior) {
        super(parent, minimumValue, maximumValue, resizeBehavior);

        // Initialize the widget.
        this.heightMultiplier = heightMultiplier;
        initialize(hatchMarkGroups);
    }

    // Public Methods

    /**
     * Determine whether or not the viewport is draggable via a mouse click and
     * drag.
     * 
     * @return True if the viewport is draggable, false otherwise.
     */
    @Override
    public final boolean isViewportDraggable() {
        return isViewportDraggable;
    }

    /**
     * Get the visible value zoom calculator, used to determine the new visible
     * value viewport to be used in response to a zoom action initiated by the
     * user via a mouse or keyboard event.
     * 
     * @return Visible value zoom calculator, or <code>null</code> if a default
     *         calculator is in use.
     */
    public final IVisibleValueZoomCalculator getVisibleValueZoomCalculator() {
        return (visibleValueZoomCalculator == DEFAULT_VISIBLE_VALUE_ZOOM_CALCULATOR ? null
                : visibleValueZoomCalculator);
    }

    /**
     * Set the visible value zoom calculator, used to determine the new visible
     * value viewport to be used in response to a zoom action initiated by the
     * user via a mouse or keyboard event.
     * 
     * @param calculator
     *            Visible value zoom calculator to be used, or <code>null</code>
     *            if the default zoom calculator is to be used.
     */
    public final void setVisibleValueZoomCalculator(
            IVisibleValueZoomCalculator calculator) {
        visibleValueZoomCalculator = (calculator == null ? DEFAULT_VISIBLE_VALUE_ZOOM_CALCULATOR
                : calculator);
    }

    /**
     * Get the border color.
     * 
     * @return Border color, or <code>null</code> if no color has been set for
     *         the border.
     */
    public final Color getBorderColor() {
        return borderColor;
    }

    /**
     * Set the border color to that specified.
     * 
     * @param color
     *            New border color.
     */
    public final void setBorderColor(Color color) {
        borderColor = color;
        redraw();
    }

    /**
     * Get the pointing direction for the constrained marked value at the
     * specified index.
     * 
     * @param index
     *            Index of the constrained marked value for which the pointing
     *            direction is to be fetched.
     * @return Pointing direction.
     */
    public final IndicatorDirection getConstrainedMarkedValueDirection(int index) {
        return getMarkedValueDirection(ValueType.CONSTRAINED, index);
    }

    /**
     * Set the pointing direction for the constrained marked value at the
     * specified index.
     * 
     * @param index
     *            Index of the constrained marked value for which the pointing
     *            direction is to be set.
     * @param direction
     *            Pointing direction to use.
     */
    public final void setConstrainedMarkedValueDirection(int index,
            IndicatorDirection direction) {
        setMarkedValueDirection(ValueType.CONSTRAINED, index, direction);
    }

    /**
     * Get the pointing direction for the free marked value at the specified
     * index.
     * 
     * @param index
     *            Index of the free marked value for which the pointing
     *            direction is to be fetched.
     * @return Pointing direction.
     */
    public final IndicatorDirection getFreeMarkedValueDirection(int index) {
        return getMarkedValueDirection(ValueType.FREE, index);
    }

    /**
     * Set the pointing direction for the free marked value at the specified
     * index.
     * 
     * @param index
     *            Index of the free marked value for which the pointing
     *            direction is to be set.
     * @param direction
     *            Pointing direction to use.
     */
    public final void setFreeMarkedValueDirection(int index,
            IndicatorDirection direction) {
        setMarkedValueDirection(ValueType.FREE, index, direction);
    }

    /**
     * Get the height for the constrained marked value at the specified index.
     * 
     * @param index
     *            Index of the constrained marked value for which the height is
     *            to be fetched.
     * @return Height (normalized to between 0.0 and 1.0).
     */
    public final float getConstrainedMarkedValueHeight(int index) {
        return getMarkedValueHeight(ValueType.CONSTRAINED, index);
    }

    /**
     * Set the height for the constrained marked value at the specified index.
     * 
     * @param index
     *            Index of the constrained marked value for which the height is
     *            to be set.
     * @param height
     *            Height (normalized to between 0.0 and 1.0) to use.
     */
    public final void setConstrainedMarkedValueHeight(int index, float height) {
        setMarkedValueHeight(ValueType.CONSTRAINED, index, height);
    }

    /**
     * Get the height for the free marked value at the specified index.
     * 
     * @param index
     *            Index of the free marked value for which the height is to be
     *            fetched.
     * @return Height (normalized to between 0.0 and 1.0).
     */
    public final float getFreeMarkedValueHeight(int index) {
        return getMarkedValueHeight(ValueType.FREE, index);
    }

    /**
     * Set the height for the free marked value at the specified index.
     * 
     * @param index
     *            Index of the free marked value for which the height is to be
     *            set.
     * @param height
     *            Height (normalized to between 0.0 and 1.0) to use.
     */
    public final void setFreeMarkedValueHeight(int index, float height) {
        setMarkedValueHeight(ValueType.FREE, index, height);
    }

    /**
     * Set the flag indicating whether or not the viewport is draggable via a
     * mouse click and drag.
     * 
     * @param value
     *            Flag indicating whether or not the viewport is draggable via a
     *            mouse click and drag.
     */
    public final void setViewportDraggable(boolean value) {
        isViewportDraggable = value;
    }

    /**
     * Determine whether or not the constrained thumbs are drawn as book-ends.
     * 
     * @return True if the constrained thumbs are drawn as book-ends, false
     *         otherwise.
     */
    public final boolean areConstrainedThumbsDrawnAsBookends() {
        return constrainedThumbsAreBookends;
    }

    /**
     * Set the flag indicating whether or not constrained thumbs are drawn as
     * book-ends.
     * 
     * @param value
     *            Flag indicating whether or not constrained thumbs are drawn as
     *            bookends.
     */
    public final void setConstrainedThumbsDrawnAsBookends(boolean value) {
        constrainedThumbsAreBookends = value;
        createThumbImages(false);
        redraw();
    }

    /**
     * Get the color used to render the constrained thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the constrained thumb for which the color is to be
     *            fetched.
     * @return Color, or <code>null</code> if no color is assigned to the
     *         specified constrained thumb.
     */
    public final Color getConstrainedThumbColor(int index) {
        return getThumbColor(ValueType.CONSTRAINED, index);
    }

    /**
     * Set the color used to render the constrained thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the constrained thumb for which the color is to be
     *            set.
     * @param color
     *            Color to use, or <code>null</code> if the default color is to
     *            be used.
     */
    public final void setConstrainedThumbColor(int index, Color color) {
        setThumbColor(ValueType.CONSTRAINED, index, color);
    }

    /**
     * Get the color used to render the free thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the free thumb for which the color is to be fetched.
     * @return Color, or <code>null</code> if no color is assigned to the thumb.
     */
    public final Color getFreeThumbColor(int index) {
        return getThumbColor(ValueType.FREE, index);
    }

    /**
     * Set the color used to render the free thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the free thumb for which the color is to be set.
     * @param color
     *            Color to use, or <code>null</code> if the default color is to
     *            be used.
     */
    public final void setFreeThumbColor(int index, Color color) {
        setThumbColor(ValueType.FREE, index, color);
    }

    /**
     * Get the pointing direction of the constrained thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the constrained thumb for which the pointing
     *            direction is to be fetched.
     * @return Pointing direction.
     */
    public final IndicatorDirection getConstrainedThumbDirection(int index) {
        return getThumbDirection(ValueType.CONSTRAINED, index);
    }

    /**
     * Set the pointing direction of the constrained thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the constrained thumb for which the pointing
     *            direction is to be set.
     * @param direction
     *            Pointing direction to use.
     */
    public final void setConstrainedThumbDirection(int index,
            IndicatorDirection direction) {
        setThumbDirection(ValueType.CONSTRAINED, index, direction);
    }

    /**
     * Get the pointing direction of the free thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the free thumb for which the pointing direction is to
     *            be fetched.
     * @return Pointing direction.
     */
    public final IndicatorDirection getFreeThumbDirection(int index) {
        return getThumbDirection(ValueType.FREE, index);
    }

    /**
     * Set the pointing direction of the free thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the free thumb for which the pointing direction is to
     *            be set.
     * @param direction
     *            Pointing direction to use.
     */
    public final void setFreeThumbDirection(int index,
            IndicatorDirection direction) {
        setThumbDirection(ValueType.FREE, index, direction);
    }

    /**
     * Get the height at which to render the constrained thumb for the value at
     * the specified index.
     * 
     * @param index
     *            Index of the constrained thumb for which the height is to be
     *            fetched.
     * @return Height (normalized to between 0.0 and 1.0).
     */
    public final float getConstrainedThumbHeight(int index) {
        return getThumbHeight(ValueType.CONSTRAINED, index);
    }

    /**
     * Set the height at which to render the constrained thumb for the value at
     * the specified index.
     * 
     * @param index
     *            Index of the constrained thumb for which the height is to be
     *            set.
     * @param height
     *            Height to use (normalized to between 0.0 and 1.0).
     */
    public final void setConstrainedThumbHeight(int index, float height) {
        setThumbHeight(ValueType.CONSTRAINED, index, height);
    }

    /**
     * Get the height at which to render the free thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the free thumb for which the height is to be fetched.
     * @return Height (normalized to between 0.0 and 1.0).
     */
    public final float getFreeThumbHeight(int index) {
        return getThumbHeight(ValueType.FREE, index);
    }

    /**
     * Set the height at which to render the free thumb for the value at the
     * specified index.
     * 
     * @param index
     *            Index of the free thumb for which the height is to be set.
     * @param height
     *            Height to use (normalized to between 0.0 and 1.0).
     */
    public final void setFreeThumbHeight(int index, float height) {
        setThumbHeight(ValueType.FREE, index, height);
    }

    /**
     * Get the number of hatch mark groups.
     * 
     * @return Number of hatch mark groups.
     */
    public final int getHatchMarkGroupCount() {
        return hatchMarkGroups.size();
    }

    /**
     * Get the hatch mark group at the specified index.
     * 
     * @param index
     *            Index of the hatch mark group to be fetched.
     * @return Hatch mark group.
     */
    public final IHatchMarkGroup getHatchMarkGroup(int index) {
        return hatchMarkGroups.get(index);
    }

    /**
     * Add the specified hatch mark group.
     * 
     * @param matchMarkGroup
     *            Hatch mark group to be added.
     */
    public final void addHatchMarkGroup(IHatchMarkGroup hatchMarkGroup) {
        hatchMarkGroups.add(hatchMarkGroup);
        sortHatchMarkGroups();
        redraw();
    }

    /**
     * Remove the hatch mark group at the specified index.
     * 
     * @param index
     *            Index of the hatch mark group to be removed.
     */
    public final void removeHatchMarkGroup(int index) {
        hatchMarkGroups.remove(index);
        redraw();
    }

    /**
     * Get the current height multiplier of the widget. The product of the font
     * height and this height multiplier indicates the preferred height of the
     * client area.
     * 
     * @return Height multiplier.
     */
    public final float getHeightMultiplier() {
        return heightMultiplier;
    }

    /**
     * Set the height multiplier for the widget to that specified. The product
     * of the font height and this height multiplier indicates the preferred
     * height of the client area.
     * 
     * @param heightMultiplier
     *            Height multiplier.
     */
    public final void setHeightMultiplier(float heightMultiplier) {

        // Remember the height multiplier.
        this.heightMultiplier = heightMultiplier;

        // Recalculate the preferred size.
        computePreferredSize(true);
        redraw();
    }

    /**
     * Set the font to that specified.
     * 
     * @param font
     *            New font.
     */
    @Override
    public final void setFont(Font font) {

        // Let the superclass do its work.
        super.setFont(font);

        // Recalculate the preferred size.
        computePreferredSize(true);
        redraw();
    }

    // Protected Methods

    /**
     * Respond to a change in the enabled state of the widget. Subclasses should
     * implement this method to change the visual cues to indicate whether or
     * not the widget is enabled.
     */
    @Override
    protected final void widgetEnabledStateChanged() {

        // A disabled look has not yet been implemented for this widget!
        // See its sibling class MultiValueScale for an idea of what needs
        // to be done.
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

        // Compute the preferred size only if it has not yet been com-
        // puted, or if being forced to do it regardless of previous
        // computations.
        if (force || (getPreferredWidth() == 0)) {

            // Calculate the preferred width and height based on the
            // font currently in use.
            GC sampleGC = new GC(this);
            FontMetrics fontMetrics = sampleGC.getFontMetrics();
            int preferredWidth = 100 + getLeftInset() + getRightInset();
            int preferredHeight = ((int) ((fontMetrics.getHeight()) * heightMultiplier))
                    + getTopInset() + getBottomInset();
            setPreferredSize(preferredWidth, preferredHeight);

            // Calculate the half triangle width for the marked value
            // triangle; this is also used to calculate the height of
            // said triangle, plus value thumbs' width and height.
            halfTriangleWidth = (int) (((sampleGC.stringExtent("XX").x) / 2.0) + 0.5);
            sampleGC.dispose();

            // (Re)create the thumb images.
            createThumbImages(true);
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
        Rectangle drawingArea = getTooltipBounds();
        int height = drawingArea.height;
        int heightOffset = drawingArea.y;

        // Get the default colors, as they are needed later.
        Color background = e.gc.getBackground();
        Color foreground = e.gc.getForeground();

        // Draw the background.
        if (e.gc.getBackground() != null) {
            e.gc.fillRectangle(clientArea);
        }

        // Ensure that the drawing does not occur in the inset area,
        // only in the client area.
        e.gc.setClipping(clientArea);

        // Iterate through the constrained marked value indicators,
        // drawing the ranges between them and at either end.
        int lastMarkedValueX = mapValueToPixel(getMinimumAllowableValue()) - 1;
        for (int j = 0; j <= getConstrainedMarkedValueCount(); j++) {
            int markedValueX = mapValueToPixel(j == getConstrainedMarkedValueCount() ? getMaximumAllowableValue()
                    : getConstrainedMarkedValue(j))
                    + (j == getConstrainedMarkedValueCount() ? 1 : 0);
            Color color = getConstrainedMarkedRangeColor(j);
            if (color != null) {
                e.gc.setBackground(color);
                e.gc.fillRectangle(lastMarkedValueX + 1, clientArea.y,
                        markedValueX - lastMarkedValueX, clientArea.height);
            }
            lastMarkedValueX = markedValueX;
        }

        // Ensure that the drawing does not occur in the horizontal
        // inset area, only in the client area and the vertical padding
        // area above and below the client area.
        e.gc.setClipping(clientArea.x, 0, clientArea.width, getTopInset()
                + clientArea.height + getBottomInset());

        // Iterate through the constrained thumb value indicators, draw-
        // ing the ranges between them and at either end.
        int lastThumbValueX = mapValueToPixel(getMinimumAllowableValue()) - 1;
        for (int j = 0; j <= getConstrainedThumbValueCount(); j++) {
            int thumbValueX = mapValueToPixel(j == getConstrainedThumbValueCount() ? getMaximumAllowableValue()
                    : getConstrainedThumbValue(j))
                    + (j == getConstrainedThumbValueCount() ? 1 : 0);
            Color color = getConstrainedThumbRangeColor(j);
            if (color != null) {
                e.gc.setBackground(color);
                e.gc.fillRectangle(lastThumbValueX + 1, 0, thumbValueX
                        - lastThumbValueX, getTopInset() + clientArea.height
                        + getBottomInset());
            }
            lastThumbValueX = thumbValueX;
        }

        // Ensure that the drawing does not occur in the inset area,
        // only in the client area.
        e.gc.setClipping(clientArea);

        // Get the widget font height.
        Font font = e.gc.getFont();
        int fontHeight = e.gc.getFontMetrics().getHeight();

        // Iterate through the hatch mark groups, drawing the hatch
        // marks for each in turn.
        for (int j = 0; j < hatchMarkGroups.size(); j++) {

            // Get the hatch mark group at this index.
            IHatchMarkGroup group = hatchMarkGroups.get(j);

            // Determine the height of the font to be used for the
            // labels of this group, if any, and set that font as
            // the current one.
            int thisFontHeight = fontHeight;
            if (group.getFont() == null) {
                e.gc.setFont(font);
            } else {
                e.gc.setFont(group.getFont());
                thisFontHeight = e.gc.getFontMetrics().getHeight();
            }

            // If the height of the hatch marks is 100%, draw a hori-
            // zontal dividing line below the area where the labels
            // will be.
            if (group.getHeightFraction() == 1.0) {
                e.gc.setForeground(group.getColor() == null ? foreground
                        : group.getColor());
                e.gc.drawLine(clientArea.x, thisFontHeight + heightOffset,
                        clientArea.x + clientArea.width, thisFontHeight
                                + heightOffset);
            }

            // Get the pixel width of this interval; if it is too
            // small, do nothing more with hatch marks.
            int intervalWidth = mapValueDeltaToPixelWidth(group.getInterval());
            if (intervalWidth < MIN_RULER_LINE_INTERVAL) {
                break;
            }

            // Determine the uppermost Y value of the hatch marks for
            // this group.
            int lineY = (int) (((1.0f - group.getHeightFraction()) * height) + 0.5f);

            // Determine whether or not labels should be drawn; this
            // depends upon whether there is enough room between ad-
            // jacent hatch marks for this group.
            boolean drawLabels = ((e.gc.stringExtent(group.getLongestLabel()).x) * 1.5f <= mapValueDeltaToPixelWidth(group
                    .getInterval()));

            // Determine the Y coordinate at which the labels should
            // be drawn, if any.
            int labelY = lineY - thisFontHeight;
            if (labelY < 0) {
                labelY = 0;
            }

            // Iterate through the possible hatch marks for this
            // group, from lowest to highest, that might be visible,
            // drawing each in turn if it was not already done by a
            // previous group.
            for (long value = (getLowerVisibleValue() / group.getInterval())
                    * group.getInterval(); value < getUpperVisibleValue()
                    + group.getInterval(); value += group.getInterval()) {

                // Ensure that this hatch mark has not already been
                // dealt with by a previous group.
                boolean alreadyDone = false;
                for (int k = j - 1; k >= 0; k--) {
                    if (value % hatchMarkGroups.get(k).getInterval() == 0) {
                        alreadyDone = true;
                        break;
                    }
                }
                if (alreadyDone) {
                    continue;
                }

                // Draw the vertical line for this hatch mark.
                e.gc.setForeground(group.getColor() == null ? foreground
                        : group.getColor());
                int valueX = mapValueToPixel(value);
                e.gc.drawLine(valueX, lineY + heightOffset, valueX, height
                        + heightOffset);

                // Draw the label if appropriate.
                if (drawLabels) {

                    // Set the foreground color, and get the label for
                    // this value, and its width.
                    e.gc.setForeground(foreground);
                    String label = group.getLabel(value);
                    int labelWidth = e.gc.stringExtent(label).x;
                    int labelWidthPadding = (int) ((labelWidth * 0.25f) + 0.5f);

                    // Determine the X coordinate at which to draw the
                    // label. If the label is to be drawn between
                    // this value's hatch mark and the next one's, it
                    // may be positioned differently to ensure it
                    // remains fully visible within the current view-
                    // port if there is enough room for it to be posi-
                    // tioned thusly without running into another
                    // value's area.
                    int labelX = valueX - (labelWidth / 2);
                    if (group.getLabelPosition() == IHatchMarkGroup.LabelPosition.BETWEEN_HATCH_MARKS) {
                        labelX += intervalWidth / 2;
                        if (labelX - labelWidthPadding < clientArea.x) {
                            labelX = clientArea.x + labelWidthPadding;
                            if (labelX + labelWidth + labelWidthPadding > valueX
                                    + intervalWidth) {
                                labelX = valueX + intervalWidth
                                        - (labelWidth + labelWidthPadding);
                            }
                        }
                        if (labelX + labelWidth + labelWidthPadding > clientArea.x
                                + clientArea.width) {
                            labelX = clientArea.x + clientArea.width
                                    - (labelWidth + labelWidthPadding);
                            if (labelX - labelWidthPadding < valueX) {
                                labelX = valueX + labelWidthPadding;
                            }
                        }
                    }

                    // Draw the label.
                    e.gc.drawText(label, labelX, labelY + heightOffset, true);
                }
            }
        }

        // Draw the border around the widget.
        e.gc.setForeground(borderColor == null ? foreground : borderColor);
        e.gc.drawRectangle(drawingArea);

        // Reset the clipping region.
        e.gc.setClipping((Rectangle) null);

        // Iterate through the marked value indicators, drawing any
        // that are visible. Draw the marked value types in the order
        // called for by the current configuration.
        for (ValueType type : getMarkTypeDrawingOrder()) {
            int numValues = (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueCount()
                    : getFreeMarkedValueCount());
            for (int j = 0; j < numValues; j++) {
                int markedValueX = mapValueToPixel(type == ValueType.CONSTRAINED ? getConstrainedMarkedValue(j)
                        : getFreeMarkedValue(j));
                if ((markedValueX >= clientArea.x)
                        && (markedValueX < clientArea.x + clientArea.width)) {
                    boolean inverted = (markedValueDirectionsForTypes.get(type)
                            .size() <= j ? false
                            : markedValueDirectionsForTypes.get(type).get(j) == IndicatorDirection.DOWN);
                    e.gc.setAntialias(SWT.ON);
                    Color color = (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueColor(j)
                            : getFreeMarkedValueColor(j));
                    e.gc.setForeground(color == null ? foreground : color);
                    e.gc.drawLine(markedValueX, -1, markedValueX, getTopInset()
                            + clientArea.height + getBottomInset());
                    int markerY = ((int) (((1.0f - (markedValueHeightsForTypes
                            .get(type).size() <= j ? 0.0f
                            : markedValueHeightsForTypes.get(type).get(j))) * (clientArea.height - ((halfTriangleWidth * 2) + 1))) + 0.5f))
                            + clientArea.y;
                    triangleCoordinates[0] = markedValueX + halfTriangleWidth;
                    triangleCoordinates[1] = triangleCoordinates[3] = markerY
                            + (inverted ? 0 : halfTriangleWidth * 2);
                    triangleCoordinates[2] = markedValueX - halfTriangleWidth;
                    triangleCoordinates[4] = markedValueX;
                    triangleCoordinates[5] = markerY
                            + (inverted ? halfTriangleWidth * 2 : 0);
                    e.gc.setBackground(color == null ? background : color);
                    e.gc.fillPolygon(triangleCoordinates);
                    e.gc.setForeground(foreground);
                    e.gc.drawPolygon(triangleCoordinates);
                }
            }
        }

        // Iterate through the thumb values, drawing thumbs for any
        // that are visible. Draw the thumb types in the order
        // called for by the current configuration, excluding any
        // dragging thumb, and then draw the dragging thumb if there
        // is one.
        ThumbSpecifier activeThumb = getActiveThumb();
        ThumbSpecifier draggingThumb = getDraggingThumb();
        for (ValueType type : getThumbTypeDrawingOrder()) {
            int numValues = (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                    : getFreeThumbValueCount());
            for (int j = 0; j < numValues; j++) {
                if ((draggingThumb != null) && (draggingThumb.type == type)
                        && (draggingThumb.index == j)) {
                    continue;
                }
                paintThumb(
                        type,
                        j,
                        (((activeThumb != null) && (activeThumb.type == type) && (activeThumb.index == j)) || ((draggingThumb != null)
                                && (draggingThumb.type == type) && (draggingThumb.index == j))),
                        foreground, clientArea, e.gc);
            }
        }
        if (draggingThumb != null) {
            paintThumb(draggingThumb.type, draggingThumb.index, true,
                    foreground, clientArea, e.gc);
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
                    int thumbX = mapValueToPixel((type == ValueType.CONSTRAINED ? getConstrainedThumbValue(j)
                            : getFreeThumbValue(j)));
                    int thumbY = getTopInset()
                            + 1
                            + (int) (((1.0f - thumbHeightsForTypes.get(type)
                                    .get(j)) * (getClientAreaHeight() - (1 + imageBounds.height))) + 0.5f);
                    if (((e.x >= thumbX - 2) && (e.x <= thumbX + 2))
                            || ((e.y >= thumbY)
                                    && (e.y <= thumbY + imageBounds.height)
                                    && (e.x >= thumbX
                                            - ((type == ValueType.FREE)
                                                    || (constrainedThumbsAreBookends == false)
                                                    || (j % 2 == 0) ? imageBounds.width / 2
                                                    : 0)) && (e.x < thumbX
                                    + ((type == ValueType.FREE)
                                            || (constrainedThumbsAreBookends == false)
                                            || (j % 2 == 1) ? imageBounds.width
                                            - (imageBounds.width / 2) : 0)))) {
                        return new ThumbSpecifier(type, j);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Handle the specified unused mouse release event.
     * 
     * @param e
     *            Mouse event.
     */
    @Override
    protected void handleUnusedMouseRelease(MouseEvent e) {
        ThumbSpecifier specifier = getClosestThumbForMouseEvent(e);
        if (specifier != null) {
            if (specifier.type == ValueType.CONSTRAINED) {
                setConstrainedThumbValue(specifier.index,
                        mapPixelToValue(e.x, true),
                        ChangeSource.USER_GUI_INTERACTION);
            } else {
                setFreeThumbValue(specifier.index, mapPixelToValue(e.x, true),
                        ChangeSource.USER_GUI_INTERACTION);
            }

        }
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
        return getTopInset() + getClientAreaHeight() + 1;
    }

    /**
     * Respond to notification that a change may have occurred in the boundary
     * of the area in which a mouse hover may generate a tooltip showing the
     * value under the mouse.
     */
    @Override
    protected final void tooltipBoundsChanged() {
        Rectangle clientArea = getClientArea();
        preferredClientHeight = getPreferredHeight()
                - (getTopInset() + getBottomInset());
        int heightOffset = (preferredClientHeight / 14) + clientArea.y;
        setTooltipBounds(new Rectangle(clientArea.x, heightOffset,
                clientArea.width - 1,
                (clientArea.height > preferredClientHeight ? clientArea.height
                        : preferredClientHeight) - (preferredClientHeight / 7)));
    }

    /**
     * Respond to the disposal of the widget.
     * 
     * @param e
     *            Disposal event that triggered this invocation.
     */
    @Override
    protected final void widgetDisposed(DisposeEvent e) {
        for (ValueType type : getThumbTypeDrawingOrder()) {
            for (Image image : thumbImagesForTypes.get(type)) {
                if (image != null) {
                    image.dispose();
                }
            }
            for (Image image : activeThumbImagesForTypes.get(type)) {
                if (image != null) {
                    image.dispose();
                }
            }
        }
    }

    // Private Methods

    /**
     * Initialize the widget.
     * 
     * @param hatchMarkGroups
     *            List of hatch mark groups.
     */
    private void initialize(List<IHatchMarkGroup> hatchMarkGroups) {

        // Remember the provided hatch mark groups.
        this.hatchMarkGroups = hatchMarkGroups;
        sortHatchMarkGroups();

        // Initialize miscellaneous member data.
        for (ValueType type : ValueType.values()) {
            thumbColorsForTypes.put(type, new ArrayList<Color>());
            thumbDirectionsForTypes.put(type,
                    new ArrayList<IndicatorDirection>());
            thumbHeightsForTypes.put(type, new ArrayList<Float>());
            thumbImagesForTypes.put(type, new ArrayList<Image>());
            activeThumbImagesForTypes.put(type, new ArrayList<Image>());
            markedValueDirectionsForTypes.put(type,
                    new ArrayList<IndicatorDirection>());
            markedValueHeightsForTypes.put(type, new ArrayList<Float>());
        }

        // Add a mouse wheel listener that uses the visible value
        // zoom calculator to determine the new visible range, and
        // then changes the range.
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                long newVisibleValueRange = visibleValueZoomCalculator
                        .getVisibleValueRangeForZoom(MultiValueRuler.this,
                                (e.count > 0), 1);
                if (newVisibleValueRange > 0L) {
                    zoomVisibleValueRange(newVisibleValueRange,
                            ChangeSource.USER_GUI_INTERACTION);
                }
            }
        });
    }

    /**
     * Sort the hatch mark groups by interval, greatest to least.
     */
    private void sortHatchMarkGroups() {
        Collections.sort(hatchMarkGroups, new Comparator<IHatchMarkGroup>() {
            @Override
            public int compare(IHatchMarkGroup o1, IHatchMarkGroup o2) {
                long delta = o2.getInterval() - o1.getInterval();
                return (delta < 0L ? -1 : (delta > 0L ? 1 : 0));
            }
        });
    }

    /**
     * Get the color used to render the thumb of the specified type for the
     * value at the specified index.
     * 
     * @param type
     *            Type of the thumb.
     * @param index
     *            Index of the thumb.
     * @return Color, or <code>null</code> if no color is assigned to the
     *         specified thumb.
     */
    private Color getThumbColor(ValueType type, int index) {
        ensureArgumentIsNotNull("type", type);

        // If the index is too high for the colors list, return null if it is
        // within bounds for the thumb value list. Otherwise, an index out of
        // bounds exception will occur when the former is accessed below.
        if (index >= thumbColorsForTypes.get(type).size()) {
            if (index < (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                    : getFreeThumbValueCount())) {
                return null;
            }
        }
        return thumbColorsForTypes.get(type).get(index);
    }

    /**
     * Set the color used to render the thumb of the specified type for the
     * value at the specified index.
     * 
     * @param type
     *            Type of the thumb for which the color is to be set.
     * @param index
     *            Index of the thumb for which the color is to be set.
     * @param color
     *            Color to use, or <code>null</code> if the default color is to
     *            be used.
     */
    private void setThumbColor(ValueType type, int index, Color color) {
        ensureArgumentIsNotNull("type", type);
        ensureIndexIsWithinBounds(
                index,
                (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                        : getFreeThumbValueCount()));
        updateThumb(type, index, color, getThumbHeight(type, index),
                getThumbDirection(type, index));
        redraw();
    }

    /**
     * Get the pointing direction used to render the thumb of the specified type
     * for the value at the specified index.
     * 
     * @param type
     *            Type of the thumb.
     * @param index
     *            Index of the thumb.
     * @return Pointing direction.
     */
    private IndicatorDirection getThumbDirection(ValueType type, int index) {
        ensureArgumentIsNotNull("type", type);

        // If the index is too high for the directions list, return "up" if it
        // is within bounds for the thumb value list. Otherwise, an index out
        // of bounds exception will occur when the former is accessed below.
        if (index >= thumbDirectionsForTypes.get(type).size()) {
            if (index < (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                    : getFreeThumbValueCount())) {
                return IndicatorDirection.UP;
            }
        }
        return thumbDirectionsForTypes.get(type).get(index);
    }

    /**
     * Set the pointing direction for the thumb of the specified type for the
     * value at the specified index.
     * 
     * @param type
     *            Type of the thumb for which the pointing direction is to be
     *            set.
     * @param index
     *            Index of the thumb for which the pointing direction is to be
     *            set.
     * @param direction
     *            Pointing direction to use.
     */
    private void setThumbDirection(ValueType type, int index,
            IndicatorDirection direction) {
        ensureArgumentIsNotNull("type", type);
        ensureIndexIsWithinBounds(
                index,
                (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                        : getFreeThumbValueCount()));
        updateThumb(type, index, getThumbColor(type, index),
                getThumbHeight(type, index), direction);
        redraw();
    }

    /**
     * Get the height of the thumb of the specified type for the value at the
     * specified index.
     * 
     * @param type
     *            Type of the thumb.
     * @param index
     *            Index of the thumb.
     * @return Height (normalized to between 0.0 and 1.0).
     */
    private float getThumbHeight(ValueType type, int index) {
        ensureArgumentIsNotNull("type", type);

        // If the index is too high for the heights list, return 0.0f if it
        // is within bounds for the thumb value list. Otherwise, an index out
        // of bounds exception will occur when the former is accessed below.
        if (index >= thumbHeightsForTypes.get(type).size()) {
            if (index < (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                    : getFreeThumbValueCount())) {
                return 0.0f;
            }
        }
        return thumbHeightsForTypes.get(type).get(index);
    }

    /**
     * Set the height for the thumb of the specified type for the value at the
     * specified index.
     * 
     * @param type
     *            Type of the thumb for which the height is to be set.
     * @param index
     *            Index of the thumb for which the height is to be set.
     * @param height
     *            Height to be used (normalized to between 0.0 and 1.0).
     */
    private void setThumbHeight(ValueType type, int index, float height) {
        ensureArgumentIsNotNull("type", type);
        ensureIndexIsWithinBounds(
                index,
                (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                        : getFreeThumbValueCount()));
        updateThumb(type, index, getThumbColor(type, index), height,
                getThumbDirection(type, index));
        redraw();
    }

    /**
     * Get the pointing direction used to render the marked value of the
     * specified type for the value at the specified index.
     * 
     * @param type
     *            Type of the marked value.
     * @param index
     *            Index of the marked value.
     * @return Pointing direction.
     */
    private IndicatorDirection getMarkedValueDirection(ValueType type, int index) {
        ensureArgumentIsNotNull("type", type);

        // If the index is too high for the directions list, return "up" if it
        // is within bounds for the marked value list. Otherwise, an index out
        // of bounds exception will occur when the former is accessed below.
        if (index >= markedValueDirectionsForTypes.get(type).size()) {
            if (index < (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueCount()
                    : getFreeMarkedValueCount())) {
                return IndicatorDirection.UP;
            }
        }
        return markedValueDirectionsForTypes.get(type).get(index);
    }

    /**
     * Set the pointing direction for the marked value of the specified type for
     * the value at the specified index.
     * 
     * @param type
     *            Type of the marked value for which the pointing direction is
     *            to be set.
     * @param index
     *            Index of the marked value for which the pointing direction is
     *            to be set.
     * @param direction
     *            Pointing direction to use.
     */
    private void setMarkedValueDirection(ValueType type, int index,
            IndicatorDirection direction) {
        ensureArgumentIsNotNull("type", type);
        ensureIndexIsWithinBounds(
                index,
                (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueCount()
                        : getFreeMarkedValueCount()));
        updateMarkedValueDirection(type, index, direction);
        redraw();
    }

    /**
     * Get the height for the marked value of the specified type for the value
     * at the specified index.
     * 
     * @param type
     *            Type of the marked value for which the height is to be
     *            fetched.
     * @param index
     *            Index of the marked value for which the height is to be
     *            fetched.
     * @return Height (normalized to between 0.0 and 1.0).
     */
    private float getMarkedValueHeight(ValueType type, int index) {
        ensureArgumentIsNotNull("type", type);

        // If the index is too high for the heights list, return 0.0f if it
        // is within bounds for the thumb value list. Otherwise, an index out
        // of bounds exception will occur when the former is accessed below.
        if (index >= markedValueHeightsForTypes.get(type).size()) {
            if (index < (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueCount()
                    : getFreeMarkedValueCount())) {
                return 0.0f;
            }
        }
        return markedValueHeightsForTypes.get(type).get(index);
    }

    /**
     * Set the height for the marked value of the specified type for the value
     * at the specified index.
     * 
     * @param type
     *            Type of the marked value for which the height is to be set.
     * @param index
     *            Index of the marked value for which the height is to be set.
     * @param height
     *            Height (normalized to between 0.0 and 1.0) to use.
     */
    private final void setMarkedValueHeight(ValueType type, int index,
            float height) {
        ensureArgumentIsNotNull("type", type);
        ensureIndexIsWithinBounds(
                index,
                (type == ValueType.CONSTRAINED ? getConstrainedMarkedValueCount()
                        : getFreeMarkedValueCount()));
        updateMarkedValueHeight(type, index, height);
        redraw();
    }

    /**
     * Determine which thumb is closest to where the specified mouse occurred.
     * 
     * @param e
     *            Mouse event.
     * @return Specifier of the thumb nearest which the event occurred, or
     *         <code>null</code> if there are no thumbs.
     */
    private ThumbSpecifier getClosestThumbForMouseEvent(MouseEvent e) {

        // If the cursor is inside the client area, check to see
        // if it is over one of the thumbs by iterating through
        // the latter, and return the index of the thumb it is
        // over, if this is the case.
        ValueType closestType = null;
        int closestIndex = -1;
        int smallestDelta = 100000;
        if ((e.x >= 0)
                && (e.x < getClientAreaWidth() + getLeftInset()
                        + getRightInset())
                && (e.y >= 0)
                && (e.y < getClientAreaHeight() + getTopInset()
                        + getBottomInset())) {
            for (ValueType type : getThumbTypeHitTestOrder()) {
                for (int j = 0; j < (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                        : getFreeThumbValueCount()); j++) {
                    int delta = Math
                            .abs(mapValueToPixel((type == ValueType.CONSTRAINED ? getConstrainedThumbValue(j)
                                    : getFreeThumbValue(j)))
                                    - e.x);
                    if (smallestDelta > delta) {
                        closestType = type;
                        closestIndex = j;
                        smallestDelta = delta;
                    }
                }
            }
        }
        if (closestIndex == -1) {
            return null;
        } else {
            return new ThumbSpecifier(closestType, closestIndex);
        }
    }

    /**
     * Paint the specified thumb.
     * 
     * @param type
     *            Type of the thumb to be painted.
     * @param index
     *            Index of the thumb to be painted.
     * @param active
     *            Flag indicating whether or not the thumb is active.
     * @param foreground
     *            Foreground color.
     * @param clientArea
     *            Client area of the widget.
     * @param gc
     *            Graphics context in which to paint.
     */
    private void paintThumb(ValueType type, int index, boolean active,
            Color foreground, Rectangle clientArea, GC gc) {
        int valueX = mapValueToPixel(type == ValueType.CONSTRAINED ? getConstrainedThumbValue(index)
                : getFreeThumbValue(index));
        ensureThumbDataPresent(type);
        if (index >= thumbImagesForTypes.get(type).size()) {
            updateThumb(type, index, getThumbColor(type, index),
                    getThumbHeight(type, index), getThumbDirection(type, index));
        }
        Image image = (active ? activeThumbImagesForTypes : thumbImagesForTypes)
                .get(type).get(index);
        Rectangle bounds = image.getBounds();
        if ((valueX >= clientArea.x)
                && (valueX < clientArea.x + clientArea.width)) {
            Color color = getThumbColor(type, index);
            if (color == null) {
                color = foreground;
            }
            gc.setForeground(color);
            gc.drawLine(valueX, -1, valueX, getTopInset() + clientArea.height
                    + getBottomInset());
            int valueY = clientArea.y
                    + 1
                    + (int) (((1.0f - thumbHeightsForTypes.get(type).get(index)) * (clientArea.height - (1 + bounds.height))) + 0.5f);
            gc.drawImage(image, valueX - halfTriangleWidth, valueY);
        }
    }

    /**
     * Create or recreate the thumb images.
     * 
     * @param both
     *            Flag indicating whether or not to (re)create free thumb images
     *            as well as constrained ones; the latter are always
     *            (re)created, whereas the former are only if this flag is true.
     */
    private void createThumbImages(boolean both) {

        // Create thumb images for both types of thumbs if both are
        // called for, or just constrained thumbs if not.
        ValueType[] types = (both ? new ValueType[] { ValueType.CONSTRAINED }
                : getThumbTypeDrawingOrder());
        for (ValueType type : types) {

            // Allow the constrained thumb images to dispose of them-
            // selves if they are already in existence, and then re-
            // create them.
            for (Image image : thumbImagesForTypes.get(type)) {
                if (image != null) {
                    image.dispose();
                }
            }
            thumbImagesForTypes.get(type).clear();
            for (Image image : activeThumbImagesForTypes.get(type)) {
                if (image != null) {
                    image.dispose();
                }
            }
            activeThumbImagesForTypes.get(type).clear();
            int numValues = (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                    : getFreeThumbValueCount());
            for (int j = 0; j < numValues; j++) {
                thumbImagesForTypes
                        .get(type)
                        .add(createThumbImage(
                                (j >= thumbColorsForTypes.get(type).size() ? null
                                        : thumbColorsForTypes.get(type).get(j)),
                                type,
                                j,
                                ((halfTriangleWidth - 1) * 2) + 1,
                                ((halfTriangleWidth - 1) * 2) + 1,
                                (j >= thumbDirectionsForTypes.get(type).size() ? IndicatorDirection.UP
                                        : thumbDirectionsForTypes.get(type)
                                                .get(j)), false));
                activeThumbImagesForTypes
                        .get(type)
                        .add(createThumbImage(
                                (j >= thumbColorsForTypes.get(type).size() ? null
                                        : thumbColorsForTypes.get(type).get(j)),
                                type,
                                j,
                                ((halfTriangleWidth - 1) * 2) + 1,
                                ((halfTriangleWidth - 1) * 2) + 1,
                                (j >= thumbDirectionsForTypes.get(type).size() ? IndicatorDirection.UP
                                        : thumbDirectionsForTypes.get(type)
                                                .get(j)), true));
            }

            // Remember the thumb images' bounds.
            if (thumbImagesForTypes.get(type).size() > 0) {
                imageBounds = thumbImagesForTypes.get(type).get(0).getBounds();
            }
        }
    }

    /**
     * Create a image to be used for drawing the selected time thumb.
     * 
     * @param color
     *            Color to be used for the thumb, or <code>
     *                   null</code> if the default thumb is to be drawn.
     * @param type
     *            Type of the thumb to be drawn.
     * @param index
     *            Index of the thumb to be drawn.
     * @param width
     *            Width of the thumb to be drawn.
     * @param height
     *            Height of the thumb to be drawn.
     * @param direction
     *            Pointing direction of the thumb.
     * @param active
     *            Flag indicating whether or not the image to be created is of
     *            an active thumb.
     * @return Image that was created.
     */
    private Image createThumbImage(Color color, ValueType type, int index,
            int width, int height, IndicatorDirection direction, boolean active) {

        // Create an AWT image, since such an image can be created
        // with transparency and painted onto with varying alpha
        // levels. Then get its graphics object and configure the
        // latter.
        BufferedImage awtImage = new BufferedImage(width + 2, height + 2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = awtImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        // Set the clipping region so that lines may be drawn out-
        // side the appropriate area in order to give them the
        // correct angle.
        graphics.setClip(0, 0, width + 2, height + 1);

        // Determine whether this thumb should be full-sized, or
        // a book-end, and if the latter, which side it is on.
        // Also determine whether it should be an inverted tri-
        // angle instead of a standard one.
        boolean left = ((type == ValueType.CONSTRAINED)
                && constrainedThumbsAreBookends && (index % 2 == 0));
        boolean right = ((type == ValueType.CONSTRAINED)
                && constrainedThumbsAreBookends && (index % 2 == 1));
        boolean inverted = (direction == IndicatorDirection.DOWN);

        // Create the points for the triangle, and determine the
        // color to be used as the background. If a color was
        // provided, use it (or a brighter version if this is the
        // active thumb image being created); otherwise, use the
        // stock background or selected background color.
        int[] xPoints = { (right ? (width / 2) + 1 : 1), (width / 2) + 1,
                (left ? (width / 2) + 1 : width) };
        int[] yPoints = { (inverted ? 0 : height), (inverted ? height - 1 : 1),
                (inverted ? 0 : height) };
        java.awt.Color backgroundColor = null;
        if (color != null) {
            backgroundColor = new java.awt.Color(color.getRed(),
                    color.getGreen(), color.getBlue());
            if (active) {
                backgroundColor = backgroundColor.brighter();
            }
        } else {
            backgroundColor = (active ? BACKGROUND_SELECTION_COLOR
                    : BACKGROUND_COLOR);
        }

        // Fill in the background of the triangle.
        graphics.setColor(backgroundColor);
        graphics.fillPolygon(xPoints, yPoints, xPoints.length);

        // Draw brighter highlighting along the left edge of the
        // triangle if this is not a bookend thumb.
        java.awt.Color lightColor = backgroundColor.brighter();
        java.awt.Color lightMixColor = new java.awt.Color(
                (BACKGROUND_COLOR.getRed() + lightColor.getRed()) / 2,
                (BACKGROUND_COLOR.getGreen() + lightColor.getGreen()) / 2,
                (BACKGROUND_COLOR.getBlue() + lightColor.getBlue()) / 2);
        if (!left && !right) {
            graphics.setColor(lightMixColor);
            graphics.drawLine(xPoints[1], yPoints[1] + 2, xPoints[0],
                    yPoints[0] + 2);
            graphics.setColor(lightColor);
            graphics.drawLine(xPoints[1], yPoints[1] + 1, xPoints[0],
                    yPoints[0] + 1);
        }

        // Draw darker shadowing along the right edge of the
        // triangle.
        java.awt.Color darkestColor = backgroundColor.darker();
        graphics.setColor(new java.awt.Color(
                (backgroundColor.getRed() + darkestColor.getRed()) / 2,
                (backgroundColor.getGreen() + darkestColor.getGreen()) / 2,
                (backgroundColor.getBlue() + darkestColor.getBlue()) / 2));
        graphics.drawLine(xPoints[1] - (left ? 2 : 0), yPoints[1]
                + ((left ? 4 : 2) * (inverted ? -1 : 1)), xPoints[2]
                - (left ? 2 : 0), yPoints[2] + (left ? -2 : 2));
        graphics.setColor(darkestColor);
        graphics.drawLine(xPoints[1] - (left ? 1 : 0), yPoints[1]
                + ((left ? 3 : 1) * (inverted ? -1 : 1)), xPoints[2]
                - (left ? 1 : 0), yPoints[2] + (left ? -1 : 1));

        // Draw brighter highlighting along the left edge of the
        // triangle if this is a bookend thumb.
        if (left || right) {
            graphics.setColor(lightMixColor);
            if (left && (!inverted)) {
                graphics.drawLine(xPoints[1], yPoints[1] + 2, xPoints[0],
                        yPoints[0] + 2);
            }
            graphics.setColor(lightColor);
            graphics.drawLine(xPoints[1] + (right ? 1 : 0), yPoints[1]
                    + (inverted ? -1 : 1), xPoints[0] + (right ? 1 : 0),
                    yPoints[0] + (right ? -1 : 1));
        }

        // Draw a dark shadow along the bottom edge of the tri-
        // angle, or light highlighting if the triangle is in-
        // verted.
        graphics.setColor(inverted ? lightMixColor : darkestColor);
        graphics.drawLine(xPoints[0] + 1, yPoints[0] - 1, xPoints[2] - 1,
                yPoints[2] - 1);

        // Remove the clipping.
        graphics.setClip(0, 0, width + 2, height + 2);

        // Draw a shadow below the triangle (outside its bounds),
        // and to its right if it is a left book-end or is in-
        // verted.
        graphics.setColor(inverted ? lightMixColor : THUMB_SHADOW_COLOR);
        graphics.drawLine(xPoints[0] + 1, yPoints[0] + 1, xPoints[2] + 1,
                yPoints[2] + 1);
        if (left || inverted) {
            graphics.setColor(THUMB_SHADOW_COLOR);
            graphics.drawLine(xPoints[1] + 1, yPoints[1] + 1, xPoints[2] + 1,
                    yPoints[2] + 1);
        }

        // Draw the border of the triangle.
        graphics.setColor(color == null ? (active ? THUMB_BORDER_SELECTION_COLOR
                : THUMB_BORDER_COLOR)
                : backgroundColor.darker().darker());
        graphics.drawPolygon(xPoints, yPoints, xPoints.length);

        // Finish up with the graphics object.
        graphics.dispose();

        // Convert the image to an SWT image and return it.
        return ImageUtilities.convertAwtImageToSwt(awtImage);
    }

    /**
     * Update the marked value pointing direction.
     * 
     * @param type
     *            Type of the marked value for which the pointing direction is
     *            being updated.
     * @param index
     *            Index of the marked value pointing direction being updated;
     *            must be a valid marked value index.
     * @param direction
     *            Pointing direction to use at the specified index.
     */
    private void updateMarkedValueDirection(ValueType type, int index,
            IndicatorDirection direction) {
        while (markedValueDirectionsForTypes.get(type).size() < index) {
            markedValueDirectionsForTypes.get(type).add(IndicatorDirection.UP);
        }
        if (markedValueDirectionsForTypes.get(type).size() == index) {
            markedValueDirectionsForTypes.get(type).add(direction);
        } else {
            markedValueDirectionsForTypes.get(type).set(index, direction);
        }
    }

    /**
     * Update the marked value height.
     * 
     * @param type
     *            Type of the marked value for which the height is being
     *            updated.
     * @param index
     *            Index of the marked value height being updated; must be a
     *            valid marked value index.
     * @param height
     *            Height (normalized to between 0.0 and 1.0) to use at the
     *            specified index.
     */
    private void updateMarkedValueHeight(ValueType type, int index, float height) {
        while (markedValueHeightsForTypes.get(type).size() < index) {
            markedValueHeightsForTypes.get(type).add(0.0f);
        }
        if (markedValueHeightsForTypes.get(type).size() == index) {
            markedValueHeightsForTypes.get(type).add(height);
        } else {
            markedValueHeightsForTypes.get(type).set(index, height);
        }
    }

    /**
     * Ensure that the thumb data is present for all thumbs in existence, even
     * if it is just default data.
     * 
     * @param type
     *            Type of the thumbs for which data is to be checked.
     */
    private void ensureThumbDataPresent(ValueType type) {
        int total = (type == ValueType.CONSTRAINED ? getConstrainedThumbValueCount()
                : getFreeThumbValueCount());
        while (thumbColorsForTypes.get(type).size() < total) {
            thumbColorsForTypes.get(type).add(null);
        }
        while (thumbHeightsForTypes.get(type).size() < total) {
            thumbHeightsForTypes.get(type).add(0.0f);
        }
        while (thumbDirectionsForTypes.get(type).size() < total) {
            thumbDirectionsForTypes.get(type).add(IndicatorDirection.UP);
        }
    }

    /**
     * Update the specified thumb's color, height, direction, and images.
     * 
     * @param type
     *            Type of the thumb to be updated.
     * @param index
     *            Index of the thumb color being updated; must be a valid value
     *            index.
     * @param color
     *            Color to use at the specified index.
     * @param height
     *            Height (normalized between 0.0 and 1.0) to use at the specfied
     *            index.
     * @param direction
     *            Pointing direction of the thumb to use at the specified index.
     */
    private void updateThumb(ValueType type, int index, Color color,
            float height, IndicatorDirection direction) {
        while (thumbColorsForTypes.get(type).size() < index) {
            thumbColorsForTypes.get(type).add(null);
        }
        while (thumbHeightsForTypes.get(type).size() < index) {
            thumbHeightsForTypes.get(type).add(0.0f);
        }
        while (thumbDirectionsForTypes.get(type).size() < index) {
            thumbDirectionsForTypes.get(type).add(IndicatorDirection.UP);
        }
        while (thumbImagesForTypes.get(type).size() < index) {
            int thisIndex = thumbImagesForTypes.get(type).size();
            thumbImagesForTypes.get(type).add(
                    createThumbImage(
                            thumbColorsForTypes.get(type).get(thisIndex), type,
                            thisIndex, ((halfTriangleWidth - 1) * 2) + 1,
                            ((halfTriangleWidth - 1) * 2) + 1,
                            thumbDirectionsForTypes.get(type).get(thisIndex),
                            false));
            activeThumbImagesForTypes.get(type).add(
                    createThumbImage(
                            thumbColorsForTypes.get(type).get(thisIndex), type,
                            thisIndex, ((halfTriangleWidth - 1) * 2) + 1,
                            ((halfTriangleWidth - 1) * 2) + 1,
                            thumbDirectionsForTypes.get(type).get(thisIndex),
                            true));
        }
        Image passiveImage = createThumbImage(color, type, index,
                ((halfTriangleWidth - 1) * 2) + 1,
                ((halfTriangleWidth - 1) * 2) + 1, direction, false);
        Image activeImage = createThumbImage(color, type, index,
                ((halfTriangleWidth - 1) * 2) + 1,
                ((halfTriangleWidth - 1) * 2) + 1, direction, true);
        if (thumbColorsForTypes.get(type).size() == index) {
            thumbColorsForTypes.get(type).add(color);
        } else {
            thumbColorsForTypes.get(type).set(index, color);
        }
        if (thumbHeightsForTypes.get(type).size() == index) {
            thumbHeightsForTypes.get(type).add(height);
        } else {
            thumbHeightsForTypes.get(type).set(index, height);
        }
        if (thumbDirectionsForTypes.get(type).size() == index) {
            thumbDirectionsForTypes.get(type).add(direction);
        } else {
            thumbDirectionsForTypes.get(type).set(index, direction);
        }
        if (thumbImagesForTypes.get(type).size() == index) {
            thumbImagesForTypes.get(type).add(passiveImage);
            activeThumbImagesForTypes.get(type).add(activeImage);
        } else {
            if (thumbImagesForTypes.get(type).get(index) != null) {
                thumbImagesForTypes.get(type).get(index).dispose();
            }
            if (activeThumbImagesForTypes.get(type).get(index) != null) {
                activeThumbImagesForTypes.get(type).get(index).dispose();
            }
            thumbImagesForTypes.get(type).set(index, passiveImage);
            activeThumbImagesForTypes.get(type).set(index, activeImage);
        }
        if (imageBounds == null) {
            imageBounds = passiveImage.getBounds();
        }
    }
}
