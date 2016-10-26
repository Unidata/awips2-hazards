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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Graph widget, allowing the display and manipulation of one or more plotted
 * points.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 30, 2016   15931    Chris.Golden Initial creation.
 * Apr 01, 2016   15931    Chris.Golden Added capability to have user 
 *                                      edit the points via dragging them.
 * Apr 01, 2016   15931    Chris.Golden Fixed bug that caused a null pointer
 *                                      exception when the plotted points
 *                                      were set to a new list of the same
 *                                      length as the old one, and added
 *                                      antialiasing when drawing circles
 *                                      and diagonal lines to smooth the
 *                                      widget's pixel jaggies out.
 * Apr 06, 2016   15931    Chris.Golden Added ability to allow the user
 *                                      to draw points via a click, drag,
 *                                      and release mouse operation, if
 *                                      the graph is empty of points. Also
 *                                      fixed behavior of the widget if it
 *                                      loses focus while the user is in
 *                                      the midst of drawing points or
 *                                      moving a point. Also added use of
 *                                      different cursors depending upon
 *                                      the mode of the widget and where the
 *                                      mouse is hovering.
 * Oct 26, 2016   25773    Chris.Golden Added height multiplier option,
 *                                      allowing the height the widget takes
 *                                      up to be configured.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class Graph extends Canvas {

    // Private Static Constants

    /**
     * Diameter of each point marker in pixels.
     */
    private static final int POINT_DIAMETER = 13;

    /**
     * Vertical buffer between X axis labels, if any, and the X axis itself, in
     * pixels.
     */
    private static final int X_AXIS_LABEL_BUFFER = 8;

    /**
     * Horizontal buffer between Y axis labels, if any, and the Y axis itself,
     * in pixels.
     */
    private static final int Y_AXIS_LABEL_BUFFER = 8;

    /**
     * Prompt text to tell the user to draw points in an empty graph.
     */
    private static final List<String> DRAW_POINTS_PROMPT_TEXT = ImmutableList
            .copyOf(Lists.newArrayList("Click and drag", "from left to right",
                    "to plot new points."));

    // Public Enumerated Types

    /**
     * Source of a configuration change.
     */
    public enum ChangeSource {

        /**
         * Method invocation.
         */
        METHOD_INVOCATION,

        /**
         * User-GUI interaction ongoing.
         */
        USER_GUI_INTERACTION_ONGOING,

        /**
         * User-GUI interaction complete.
         */
        USER_GUI_INTERACTION_COMPLETE
    }

    // Private Variables

    /**
     * Minimum visible X value from the last time that the
     * {@link #plottedPoints} list was not empty, if ever.
     */
    private int minimumVisibleValueX;

    /**
     * Maximum visible X value from the last time that the
     * {@link #plottedPoints} list was not empty, if ever.
     */
    private int maximumVisibleValueX;

    /**
     * Minimum visible Y value.
     */
    private int minimumVisibleValueY;

    /**
     * Maximum visible Y value.
     */
    private int maximumVisibleValueY;

    /**
     * X interval between vertical hatch lines; if <code>0</code>, no vertical
     * lines are drawn.
     */
    private int intervalHatchX;

    /**
     * Y interval between horizontal hatch lines; if <code>0</code>, no
     * horizontal lines are drawn.
     */
    private int intervalHatchY;

    /**
     * X interval between labels; if <code>0</code>, no labels are drawn on the
     * X axis. Ignored if {@link #intervalHatchX} is <code>0</code>; otherwise,
     * must be a multiple of <code>xHatchInterval</code>.
     */
    private int intervalLabelX;

    /**
     * Y interval between labels; if <code>0</code>, no labels are drawn on the
     * Y axis. Ignored if {@link #intervalHatchY} is <code>0</code>; otherwise,
     * must be a multiple of <code>yHatchInterval</code>.
     */
    private int intervalLabelY;

    /**
     * Suffix to be appended to any labels along the X axis; may be
     * <code>null</code>.
     */
    private String suffixLabelX;

    /**
     * Suffix to be appended to any labels along the Y axis; may be
     * <code>null</code>.
     */
    private String suffixLabelY;

    /**
     * Height multiplier, to be applied when determining the preferred height.
     * Must be <code>0.5</code> or greater. Note that values under
     * <code>1.0</code> may result in some Y axis labels not showing due to
     * space limitations. If not set, this defaults to <code>1.0</code>.
     */
    private double heightMultiplier = 1.0;

    /**
     * <p>
     * X interval between points drawn by the user via click-drag-release mouse
     * operations; must be a non-negative number. If <code>0</code>, the drawing
     * capability is disabled. If a positive integer, it tells the graph how
     * long an interval in X axis units to place between points taken from the
     * user's sketch.
     * </p>
     * <p>
     * Note that even if this is a positive integer, drawing is only possible
     * when the {@link #plottedPoints} list is empty.
     * </p>
     */
    private int intervalDrawnPointsX;

    /**
     * List of plotted points, ordered by X value.
     */
    private final List<PlottedPoint> plottedPoints = new ArrayList<>();

    /**
     * List of colors to be used to color the "rows" in the graph, i.e. the
     * colors will vary going up the Y axis if any are specified. The interval
     * covered by each row will be determined by taking the difference between
     * the maximum and minimum visible Y values and dividing it by the number of
     * colors specified within this list. If the list is empty, the background
     * color will be used for the graph.
     */
    private final List<Color> rowColors = new ArrayList<Color>();

    /**
     * Set of listeners; these receive notifications of visible value range or
     * value changes when the latter occur.
     */
    private final Set<IGraphListener> listeners = new HashSet<>();

    /**
     * Desired width of the widget.
     */
    private int preferredWidth;

    /**
     * Desired height of the widget.
     */
    private int preferredHeight;

    /**
     * Last recorded width of the client area.
     */
    private int lastWidth;

    /**
     * Last recorded height of the client area.
     */
    private int lastHeight;

    /**
     * Number of pixels by which to inset the client area of the widget from the
     * left border.
     */
    private int leftInset;

    /**
     * Number of pixels by which to inset the client area of the widget from the
     * top border.
     */
    private int topInset;

    /**
     * Number of pixels by which to inset the client area of the widget from the
     * right border.
     */
    private int rightInset;

    /**
     * Number of pixels by which to inset the client area of the widget from the
     * bottom border.
     */
    private int bottomInset;

    /**
     * Last Y coordinate from a drag operation.
     */
    private int lastDragY;

    /**
     * Specifier of the plotted point currently being dragged, or
     * <code>null</code> if none is being dragged.
     */
    private PlottedPoint draggingPoint = null;

    /**
     * Specifier of the plotted point over which the mouse cursor is presently
     * located, or <code>null</code> if the mouse cursor is not over any plotted
     * point.
     */
    private PlottedPoint activePoint = null;

    /**
     * Flag indicating whether or not a determination of which plotted point is
     * active has been scheduled to occur later.
     */
    private boolean determinationOfActivePointScheduled = false;

    /**
     * Maximum width of an X axis label in pixels, given the current range of X
     * values and font, or <code>0</code> if no X axis labels are in use.
     */
    private int xLabelWidth;

    /**
     * Maximum width of a Y axis label in pixels, given the current range of Y
     * values and font, or <code>0</code> if no Y axis labels are in use.
     */
    private int yLabelWidth;

    /**
     * Maximum height of an X label in pixels, given the current font, or
     * <code>0</code> if no X axis labels are in use.
     */
    private int xLabelHeight;

    /**
     * Maximum height of a Y label in pixels, given the current font, or
     * <code>0</code> if no Y axis labels are in use.
     */
    private int yLabelHeight;

    /**
     * Number of pixels per X unit.
     */
    private double pixelsPerUnitX;

    /**
     * Number of pixels per Y unit.
     */
    private double pixelsPerUnitY;

    /**
     * Number of pixels per hatch interval along the X axis.
     */
    private int pixelsPerHatchX;

    /**
     * Number of pixels per hatch interval along the Y axis.
     */
    private int pixelsPerHatchY;

    /**
     * Number of pixels per interval between user-drawn points along the X axis.
     */
    private double pixelsPerDrawnPointX;

    /**
     * Graph width in pixels, not including the width of labels along the Y axis
     * if any.
     */
    private int graphWidth;

    /**
     * Graph height in pixels, not including the height of labels along the X
     * axis if any.
     */
    private int graphHeight;

    /**
     * List of upper Y boundaries for each color in {@link #rowColors}.
     */
    private final List<Integer> rowColorUpperBounds = new ArrayList<>();

    /**
     * Bidirectional map pairing rectangular boundaries for plotted points on
     * the graph with the corresponding plotted points (the latter being
     * references to those found in the {@link #plottedPoints} list).
     */
    private final BiMap<Rectangle, PlottedPoint> plottedPointsForPlottedPointBoundaries = HashBiMap
            .create();

    /**
     * Version of the graph's current font to be used for rendering prompting
     * text.
     */
    private Font promptTextFont = null;

    /**
     * List of points making up the path that the user has drawn during a
     * user-drawing-of-points operation.
     */
    private final Deque<Point> userDrawnPoints = new ArrayDeque<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent of this widget.
     * @param minimumVisibleValueY
     *            Minimum visible Y value.
     * @param maximumVisibleValueY
     *            Maximum visible Y value.
     */
    public Graph(Composite parent, int minimumVisibleValueY,
            int maximumVisibleValueY) {
        super(parent, SWT.NONE);

        /*
         * Initialize the widget.
         */
        initializeGraph(minimumVisibleValueY, maximumVisibleValueY);
    }

    // Public Methods

    /**
     * Add the specified graph control listener; the latter will be notified of
     * value changes.
     * 
     * @param listener
     *            Listener to be added.
     * @return True if this listener was not already part of the set of
     *         listeners, false otherwise.
     */
    public final boolean addGraphListener(IGraphListener listener) {
        return listeners.add(listener);
    }

    /**
     * Remove the specified graph control listener; if the latter was registered
     * previously via {@link #addGraphListener(IGraphListener)} it will no
     * longer be notified of value changes.
     * 
     * @param listener
     *            Listener to be removed.
     * @return True if the listener was found and removed, otherwise false.
     */
    public final boolean removeGraphListener(IGraphListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public final void setEnabled(boolean enable) {
        super.setEnabled(enable);

        /*
         * End any drag of a point.
         */
        if ((enable == false) && (draggingPoint != null)) {
            plottedPointDragEnded(null);
        }

        /*
         * Ensure that the correct point is active, and redraw the widget.
         */
        determineActivePoint();
        redraw();
    }

    @Override
    public final void setFont(Font font) {

        /*
         * Let the superclass do its work.
         */
        super.setFont(font);

        /*
         * Dispose of the prompt text version of the old font, if any.
         */
        if (promptTextFont != null) {
            promptTextFont.dispose();
            promptTextFont = null;
        }

        /*
         * Recalculate the preferred size and the active point, and redraw.
         */
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the minimum visible Y value.
     * 
     * @return Minimum visible Y value.
     */
    public final int getMinimumVisibleValueY() {
        return minimumVisibleValueY;
    }

    /**
     * Get the maximum visible Y value.
     * 
     * @return Maximum visible Y value.
     */
    public final int getMaximumVisibleValueY() {
        return maximumVisibleValueY;
    }

    /**
     * Set the minimum and maximum visible Y values.
     * 
     * @param minimumValue
     *            New minimum visible Y value; must be less than
     *            <code>maximumValue</code>.
     * @param maximumValue
     *            New maximum visible Y value; must be greater than
     *            <code>minimumValue</code>.
     */
    public final void setVisibleValuesY(int minimumValue, int maximumValue) {

        /*
         * Ensure that the minimum value is less than the maximum value.
         */
        if (minimumValue >= maximumValue) {
            throw new IllegalArgumentException(
                    "minimum value must be less than maximum");
        }

        /*
         * If the boundaries have not changed, do nothing more.
         */
        if ((this.minimumVisibleValueY == minimumValue)
                && (this.maximumVisibleValueY == maximumValue)) {
            return;
        }

        /*
         * Set the new values, recompute the preferred size, make sure the right
         * point is active (if any), and redraw.
         */
        this.minimumVisibleValueY = minimumValue;
        this.maximumVisibleValueY = maximumValue;
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the X interval between vertical hatch lines.
     * 
     * @return X interval between vertical hatch lines; if <code>0</code>, no
     *         vertical lines are drawn.
     */
    public final int getIntervalHatchX() {
        return intervalHatchX;
    }

    /**
     * Set the X interval between vertical hatch lines.
     * 
     * @param interval
     *            X interval between vertical hatch lines; must be a
     *            non-negative integer. If <code>0</code>, no vertical lines are
     *            drawn.
     */
    public final void setIntervalHatchX(int interval) {
        if (interval < 0) {
            throw new IllegalArgumentException(
                    "hatch interval must be non-negative");
        }

        /*
         * Set the new value, recompute the preferred size, make sure the right
         * point is active (if any), and redraw.
         */
        intervalHatchX = interval;
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the Y interval between horizontal hatch lines.
     * 
     * @return Y interval between horizontal hatch lines; if <code>0</code>, no
     *         horizontal lines are drawn.
     */
    public final int getIntervalHatchY() {
        return intervalHatchY;
    }

    /**
     * Set the Y interval between horizontal hatch lines.
     * 
     * @param interval
     *            Y interval between horizontal hatch lines; must be a
     *            non-negative integer. If <code>0</code>, no horizontal lines
     *            are drawn.
     */
    public final void setIntervalHatchY(int interval) {
        if (interval < 0) {
            throw new IllegalArgumentException(
                    "hatch interval must be non-negative");
        }

        /*
         * Set the new value, recompute the preferred size, make sure the right
         * point is active (if any), and redraw.
         */
        intervalHatchY = interval;
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the interval between X axis labels.
     * 
     * @return Interval between X axis labels; if <code>0</code>, no labels are
     *         drawn for the X axis.
     */
    public final int getIntervalLabelX() {
        return intervalLabelX;
    }

    /**
     * Set the interval between X axis labels.
     * 
     * @param interval
     *            Interval between X axis labels. Must be <code>0</code> if
     *            {@link #getIntervalHatchX()} yields <code>0</code>; if the
     *            latter yields a positive integer, then this interval must be a
     *            multiple of that integer. If <code>0</code>, no labels are
     *            drawn on the X axis.
     */
    public final void setIntervalLabelX(int interval) {
        if (((intervalHatchX == 0) && (interval != 0))
                || ((intervalHatchX > 0) && (interval % intervalHatchX != 0))) {
            throw new IllegalArgumentException(
                    "label interval must be 0 if hatch interval on same axis is 0, or multiple of hatch interval otherwise");
        }

        /*
         * Set the new value, recompute the preferred size, make sure the right
         * point is active (if any), and redraw.
         */
        intervalLabelX = interval;
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the interval between Y axis labels.
     * 
     * @return Interval between Y axis labels; if <code>0</code>, no labels are
     *         drawn for the Y axis.
     */
    public final int getIntervalLabelY() {
        return intervalLabelY;
    }

    /**
     * Set the interval between Y axis labels.
     * 
     * @param interval
     *            Interval between Y axis labels. Must be <code>0</code> if
     *            {@link #getIntervalHatchY()} yields <code>0</code>; if the
     *            latter yields a positive integer, then this interval must be a
     *            multiple of that integer. If <code>0</code>, no labels are
     *            drawn on the Y axis.
     */
    public final void setIntervalLabelY(int interval) {
        if (((intervalHatchY == 0) && (interval != 0))
                || ((intervalHatchY > 0) && (interval % intervalHatchY != 0))) {
            throw new IllegalArgumentException(
                    "label interval must be 0 if hatch interval on same axis is 0, or multiple of hatch interval otherwise");
        }

        /*
         * Set the new value, recompute the preferred size, make sure the right
         * point is active (if any), and redraw.
         */
        intervalLabelY = interval;
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Set the hatching and label intervals for both axes.
     * 
     * @param intervalHatchX
     *            X interval between vertical hatch lines; must be a
     *            non-negative integer. If <code>0</code>, no vertical lines are
     *            drawn.
     * @param intervalHatchY
     *            Y interval between horizontal hatch lines; must be a
     *            non-negative integer. If <code>0</code>, no horizontal lines
     *            are drawn.
     * @param intervalLabelX
     *            Interval between X axis labels. Must be <code>0</code> if
     *            <code>intervalHatchX</code> yields <code>0</code>; if the
     *            latter yields a positive integer, then this interval must be a
     *            multiple of that integer. If <code>0</code>, no labels are
     *            drawn on the X axis.
     * @param intervalLabelY
     *            Interval between Y axis labels. Must be <code>0</code> if
     *            <code>intervalHatchY</code> yields <code>0</code>; if the
     *            latter yields a positive integer, then this interval must be a
     *            multiple of that integer. If <code>0</code>, no labels are
     *            drawn on the Y axis.
     */
    public final void setHatchAndLabelIntervals(int intervalHatchX,
            int intervalHatchY, int intervalLabelX, int intervalLabelY) {
        if ((intervalHatchX < 0) || (intervalHatchY < 0)) {
            throw new IllegalArgumentException(
                    "hatch interval must be non-negative");
        } else if ((((intervalHatchX == 0) && (intervalLabelX != 0)) || ((intervalHatchX > 0) && (intervalLabelX
                % intervalHatchX != 0)))
                || (((intervalHatchY == 0) && (intervalLabelY != 0)) || ((intervalHatchY > 0) && (intervalLabelY
                        % intervalHatchY != 0)))) {
            throw new IllegalArgumentException(
                    "label interval must be 0 if hatch interval on same axis is 0, or multiple of hatch interval otherwise");
        }

        /*
         * Set the new values, recompute the preferred size, make sure the right
         * point is active (if any), and redraw.
         */
        this.intervalHatchX = intervalHatchX;
        this.intervalHatchY = intervalHatchY;
        this.intervalLabelX = intervalLabelX;
        this.intervalLabelY = intervalLabelY;
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the height multiplier, to be applied when determining the preferred
     * height.
     * 
     * @return Height multiplier.
     */
    public final double getHeightMultiplier() {
        return heightMultiplier;
    }

    /**
     * Set the height multiplier, to be applied when determining the preferred
     * height.
     * 
     * @param heightMultiplier
     *            New height multiplier; must be <code>0.5</code> or greater.
     *            Note that values under <code>1.0</code> may result in some Y
     *            axis labels not showing due to space limitations.
     */
    public final void setHeightMultiplier(double heightMultiplier) {
        if (heightMultiplier < 0.5) {
            throw new IllegalArgumentException(
                    "height multiplier must be 0.5 or greater");
        }
        this.heightMultiplier = heightMultiplier;
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the X interval between points drawn by the user via
     * click-drag-release mouse operations.
     * 
     * @return Non-negative X interval between points drawn by the user. If
     *         <code>0</code>, the drawing capability is disabled. If a positive
     *         integer, it tells the graph how long an interval in X axis units
     *         to place between points taken from the user's sketch. Note that
     *         even if this is a positive integer, drawing is only possible when
     *         {@link #getPlottedPoints()} returns an empty list.
     */
    public final int getIntervalDrawnPointsX() {
        return intervalDrawnPointsX;
    }

    /**
     * Set the X interval between points drawn by the user via
     * click-drag-release mouse operations.
     * 
     * @param intervalDrawnPointsX
     *            Non-negative X interval between points drawn by the user. If
     *            <code>0</code>, the drawing capability will be disabled. If a
     *            positive integer, it tells the graph how long an interval in X
     *            axis units to place between points taken from the user's
     *            sketch. Note that even if this is a positive integer, drawing
     *            is only possible when {@link #getPlottedPoints()} returns an
     *            empty list.
     */
    public final void setIntervalDrawnPointsX(int intervalDrawnPointsX) {
        if (intervalDrawnPointsX < 0) {
            throw new IllegalArgumentException(
                    "drawn points interval must be non-negative");
        }
        boolean modeChange = (((intervalDrawnPointsX == 0) || (this.intervalDrawnPointsX == 0)) && (intervalDrawnPointsX != this.intervalDrawnPointsX));
        this.intervalDrawnPointsX = intervalDrawnPointsX;
        if (modeChange) {
            updateCursor();
            redraw();
        }
    }

    /**
     * Get the suffix to be appended to any labels along the X axis.
     * 
     * @return Suffix to be appended to any labels along the X axis; may be
     *         <code>null</code>.
     */
    public final String getLabelSuffixX() {
        return suffixLabelX;
    }

    /**
     * Get the suffix to be appended to any labels along the Y axis.
     * 
     * @return Suffix to be appended to any labels along the Y axis; may be
     *         <code>null</code>.
     */
    public final String getLabelSuffixY() {
        return suffixLabelY;
    }

    /**
     * Set the suffixes to be appended to any labels along the axes.
     * 
     * @param suffixLabelX
     *            Suffix to be appended to any labels along the X axis; may be
     *            <code>null</code>.
     * @param suffixLabelY
     *            Suffix to be appended to any labels along the Y axis; may be
     *            <code>null</code>.
     */
    public final void setLabelSuffixes(String suffixLabelX, String suffixLabelY) {
        this.suffixLabelX = suffixLabelX;
        this.suffixLabelY = suffixLabelY;

        /*
         * Recalculate the preferred size and the active point, and redraw.
         */
        computePreferredSize(true);
        scheduleDetermineActivePointIfEnabled();
        redraw();
    }

    /**
     * Get the number of plotted points.
     * 
     * @return Number of plotted points.
     */
    public final int getPlottedPointCount() {
        return plottedPoints.size();
    }

    /**
     * Get the plotted points.
     * 
     * @return Copy of the list of plotted points (with each point copied as
     *         well, since {@link PlottedPoint} instances are mutable).
     */
    public final List<PlottedPoint> getPlottedPoints() {
        List<PlottedPoint> copy = new ArrayList<>(plottedPoints.size());
        for (PlottedPoint point : plottedPoints) {
            copy.add(new PlottedPoint(point));
        }
        return copy;
    }

    /**
     * Set the plotted points.
     * 
     * @param points
     *            New list of plotted points; may be an empty list.
     * @return True if the points are set, false otherwise. They will not be set
     *         if any of them have duplicate X values.
     */
    public final boolean setPlottedPoints(List<PlottedPoint> points) {

        /*
         * Ensure the points are sorted, and do not have duplicate X values.
         */
        if (points != null) {
            points = sortPlottedPoints(points);
        }
        if (points == null) {
            return false;
        }

        /*
         * Remember the new points.
         */
        setPlottedPoints(points, ChangeSource.METHOD_INVOCATION);
        return true;
    }

    /**
     * Get the row colors.
     * 
     * @return Row colors.
     */
    public final List<Color> getRowColors() {
        return new ArrayList<>(rowColors);
    }

    /**
     * Set the row colors.
     * 
     * @param rowColors
     *            New row colors.
     */
    public final void setRowColors(List<Color> rowColors) {
        this.rowColors.clear();
        if (rowColors != null) {
            this.rowColors.addAll(rowColors);
        }
        computeColorRowHeights();
        redraw();
    }

    /**
     * Get the inset in pixels from the left border.
     * 
     * @return Inset in pixels.
     */
    public final int getLeftInset() {
        return leftInset;
    }

    /**
     * Get the inset in pixels from the top border.
     * 
     * @return Inset in pixels.
     */
    public final int getTopInset() {
        return topInset;
    }

    /**
     * Get the inset in pixels from the right border.
     * 
     * @return Inset in pixels.
     */
    public final int getRightInset() {
        return rightInset;
    }

    /**
     * Get the inset in pixels from the bottom border.
     * 
     * @return Inset in pixels.
     */
    public final int getBottomInset() {
        return bottomInset;
    }

    /**
     * Set the insets around the edges of the widget, indicating the number of
     * pixels to inset each side of the client area from that side's border.
     * 
     * @param left
     *            Number of pixels to inset from the left side.
     * @param top
     *            Number of pixels to inset from the top side.
     * @param right
     *            Number of pixels to inset from the right side.
     * @param bottom
     *            Number of pixels to inset from the bottom side.
     */
    public final void setInsets(int left, int top, int right, int bottom) {

        /*
         * Remember the insets.
         */
        leftInset = left;
        topInset = top;
        rightInset = right;
        bottomInset = bottom;

        /*
         * Recalculate the preferred size.
         */
        computePreferredSize(true);

        /*
         * Determine the active plotted point.
         */
        scheduleDetermineActivePointIfEnabled();
    }

    @Override
    public final Point computeSize(int wHint, int hHint, boolean changed) {

        /*
         * Calculate the preferred size if needed.
         */
        computePreferredSize(false);

        /*
         * Return the size based upon the preferred size and the hints.
         */
        return new Point((wHint == SWT.DEFAULT ? preferredWidth : wHint),
                (hHint == SWT.DEFAULT ? preferredHeight : hHint));
    }

    @Override
    public final Rectangle computeTrim(int x, int y, int width, int height) {
        return new Rectangle(x - leftInset, y - topInset, width + leftInset
                + rightInset, height + topInset + bottomInset);
    }

    @Override
    public final Rectangle getClientArea() {
        Rectangle bounds = getBounds();
        return new Rectangle(leftInset, topInset, bounds.width
                - (leftInset + rightInset), bounds.height
                - (topInset + bottomInset));
    }

    // Private Methods

    /**
     * Initialize the widget.
     * 
     * @param minimumVisibleValueY
     *            Minimum visible Y value.
     * @param maximumVisibleValueY
     *            Maximum visible Y value.
     */
    private void initializeGraph(int minimumVisibleValueY,
            int maximumVisibleValueY) {

        /*
         * Remember the minimum and maximum values.
         */
        this.minimumVisibleValueY = minimumVisibleValueY;
        this.maximumVisibleValueY = maximumVisibleValueY;

        /*
         * Add a listener for paint request events.
         */
        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                Graph.this.paintControl(e);
            }
        });

        /*
         * Add a listener for resize events.
         */
        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Graph.this.controlResized(e);
            }
        });

        /*
         * Add a listener for dispose events.
         */
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                draggingPoint = null;
                activePoint = null;
                if (promptTextFont != null) {
                    promptTextFont.dispose();
                }
            }
        });

        /*
         * Add mouse listeners to handle drags, clicks, and mouse-over events.
         */
        addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {

                /*
                 * No action.
                 */
            }

            @Override
            public void mouseDown(MouseEvent e) {
                if ((e.button == 1) && isVisible() && isEnabled()) {
                    forceFocus();
                    if (isReadyForUserDrawingOfPoints()) {
                        addUserDrawnPoint(new Point(e.x, e.y));
                    } else {
                        mousePressOverWidget(e);
                    }
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (draggingPoint != null) {
                    plottedPointDragEnded(e);
                } else if (userDrawnPoints.isEmpty() == false) {
                    addUserDrawnPoint(new Point(e.x, e.y));
                    finishUserDrawingOfPoints();
                } else if ((e.button == 1) && isVisible() && isEnabled()) {
                    mouseOverWidget(e.x, e.y);
                }
            }
        });
        addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (draggingPoint != null) {
                    plottedPointDragged(e);
                } else if (userDrawnPoints.isEmpty() == false) {
                    addUserDrawnPoint(new Point(e.x, e.y));
                    redraw();
                } else if (isVisible() && isEnabled()) {
                    mouseOverWidget(e.x, e.y);
                }
            }
        });
        addMouseTrackListener(new MouseTrackListener() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (isVisible() && isEnabled()) {
                    mouseOverWidget(e.x, e.y);
                }
            }

            @Override
            public void mouseExit(MouseEvent e) {
                if (activePoint != null) {
                    activePoint = null;
                    updateCursor();
                    redraw();
                }
            }

            @Override
            public void mouseHover(MouseEvent e) {

                /*
                 * No action.
                 */
            }
        });

        /*
         * Add a focus listener to cancel ongoing operations when focus is lost.
         */
        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {

                /*
                 * No action.
                 */
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (userDrawnPoints.isEmpty() == false) {
                    userDrawnPoints.clear();
                }
                if (draggingPoint != null) {
                    plottedPointDragEnded(null);
                }
                if (activePoint != null) {
                    activePoint = null;
                    updateCursor();
                }
                if (isDisposed() == false) {
                    redraw();
                }
            }
        });
    }

    /**
     * Respond to the resizing of the widget.
     * 
     * @param e
     *            Control event that triggered this invocation.
     */
    private void controlResized(ControlEvent e) {

        /*
         * If the widget is visible, the width has previously been meaningfully
         * recorded, and the resize behavior requires that the viewport change
         * with size, calculate the new value range for the new size.
         */
        Rectangle clientArea = getClientArea();

        /*
         * Remember the now-current width.
         */
        lastWidth = clientArea.width;
        lastHeight = clientArea.height;

        /*
         * Recompute display measurements and determine the active point.
         */
        computeDisplayMeasurements();
        scheduleDetermineActivePointIfEnabled();
    }

    /**
     * Compute the preferred size of the widget.
     * 
     * @param force
     *            Force a recompute if the size has already been computed.
     */
    private void computePreferredSize(boolean force) {

        /*
         * Compute the preferred size only if it has not yet been computed, or
         * if being forced to do it regardless of previous computations.
         */
        if (force || (preferredWidth == 0)) {

            /*
             * Calculate the preferred width and height based on the font
             * currently in use. The preferred width allows for all the labels
             * that should be shown along the X axis to be displayed, and the
             * preferred height allows for the same along the Y axis.
             */
            GC sampleGC = new GC(this);
            FontMetrics fontMetrics = sampleGC.getFontMetrics();

            /*
             * Calculate the maximum X and Y label widths and heights. No labels
             * will be drawn for an axis if that axis's hatch interval is 0, or
             * if the label interval is 0, or if the label interval is not a
             * multiple of the hatch interval.
             */
            xLabelWidth = ((intervalLabelX == 0) || (intervalHatchX == 0)
                    || (intervalLabelX % intervalHatchX != 0) ? 0 : Math.max(
                    sampleGC.stringExtent(Integer
                            .toString(minimumVisibleValueX)
                            + (suffixLabelX == null ? "" : suffixLabelX)).x,
                    sampleGC.stringExtent(Integer
                            .toString(maximumVisibleValueX)
                            + (suffixLabelX == null ? "" : suffixLabelX)).x));
            yLabelWidth = ((intervalLabelY == 0) || (intervalHatchY == 0)
                    || (intervalLabelY % intervalHatchY != 0) ? 0 : Math.max(
                    sampleGC.stringExtent(Integer
                            .toString(minimumVisibleValueY)
                            + (suffixLabelY == null ? "" : suffixLabelY)).x,
                    sampleGC.stringExtent(Integer
                            .toString(maximumVisibleValueY)
                            + (suffixLabelY == null ? "" : suffixLabelY)).x));
            xLabelHeight = (xLabelWidth == 0 ? 0 : fontMetrics.getHeight());
            yLabelHeight = (yLabelWidth == 0 ? 0 : fontMetrics.getHeight());

            /*
             * When calculating the preferred width, take into account the
             * labels along the Y axis and the horizontal space they take up.
             */
            preferredWidth = 0;
            if (minimumVisibleValueX != maximumVisibleValueX) {
                int numLabels = (maximumVisibleValueX - minimumVisibleValueX)
                        / (intervalLabelX == 0 ? 10 : intervalLabelX);
                preferredWidth = (xLabelWidth * numLabels) + yLabelWidth;
            }
            if (preferredWidth < 400) {
                preferredWidth = 400;
            }
            preferredWidth += getLeftInset() + getRightInset();

            /*
             * When calculating the preferred height, take into account the
             * labels along the X axis and the vertical space they take up. The
             * configurable height multiplier is also considered.
             */
            if (minimumVisibleValueY != maximumVisibleValueY) {
                int numLabels = (maximumVisibleValueY - minimumVisibleValueY)
                        / (intervalLabelY == 0 ? 10 : intervalLabelY);
                preferredHeight = ((int) ((yLabelHeight) * 2.0 * heightMultiplier))
                        * (numLabels + (intervalLabelX == 0 ? 0 : 1));
            }
            preferredHeight += getTopInset() + getBottomInset();

            sampleGC.dispose();

            /*
             * Cancel any ongoing drag.
             */
            cancelOngoingDrag();

            /*
             * Recompute the display measurements.
             */
            computeDisplayMeasurements();
        }
    }

    /**
     * Compute the measurements needed for displaying the widget.
     */
    private void computeDisplayMeasurements() {

        /*
         * If there is no width or height, do not try to compute measurements.
         */
        if (isReadyForPainting() == false) {
            return;
        }

        /*
         * If there there is no X or Y range, then very little needs to be done.
         * Computation of pixels-per-unit, etc. is required if both X and Y axes
         * have ranges of values.
         */
        int xRange = maximumVisibleValueX - minimumVisibleValueX;
        int yRange = maximumVisibleValueY - minimumVisibleValueY;
        int widthWithoutLabels = lastWidth
                - (yRange > 0 ? yLabelWidth + Y_AXIS_LABEL_BUFFER : 0);
        int heightWithoutLabels = lastHeight
                - (xRange > 0 ? xLabelHeight + X_AXIS_LABEL_BUFFER : 0);

        /*
         * Calculate the number of pixels per unit in each direction.
         */
        if (xRange > 0) {
            pixelsPerUnitX = ((double) widthWithoutLabels) / (double) xRange;
        } else {
            pixelsPerUnitX = 0;
        }
        if (yRange > 0) {
            pixelsPerUnitY = ((double) heightWithoutLabels) / (double) yRange;
        } else {
            pixelsPerUnitY = 0;
        }

        /*
         * Calculate the number of pixels per hatch mark, if any, in each
         * direction. If a ridiculously small amount (or 0, or negative), which
         * will occur if there are too many hatch marks for the pixel range
         * available, or if there are not meant to be any hatch marks, then
         * assume no hatch marks, and calculate the pixels per unit exactly,
         * since no hatch marks means no concern over uneven-looking intervals.
         * Otherwise, calculate the pixels per unit by dividing the pixels per
         * hatch by the hatch interval. Also calculate the pixels per drawn
         * point interval, in case the user draws points.
         */
        if (xRange > 0) {
            pixelsPerHatchX = (int) (((double) (widthWithoutLabels * intervalHatchX)) / (double) xRange);
            if (pixelsPerHatchX < 4) {
                pixelsPerHatchX = 0;
                pixelsPerUnitX = ((double) widthWithoutLabels)
                        / (double) xRange;
                graphWidth = widthWithoutLabels;
            } else {
                pixelsPerUnitX = ((double) pixelsPerHatchX)
                        / (double) intervalHatchX;
                graphWidth = pixelsPerHatchX * (xRange / intervalHatchX);
            }
            pixelsPerDrawnPointX = ((double) (graphWidth * intervalDrawnPointsX))
                    / (double) xRange;
        } else {
            pixelsPerHatchX = 0;
            pixelsPerDrawnPointX = 0.0;
            graphWidth = widthWithoutLabels;
        }
        if (yRange > 0) {
            pixelsPerHatchY = (int) (((double) (heightWithoutLabels * intervalHatchY)) / (double) yRange);
            if (pixelsPerHatchY < 4) {
                pixelsPerHatchY = 0;
                pixelsPerUnitY = ((double) heightWithoutLabels)
                        / (double) yRange;
                graphHeight = heightWithoutLabels;
            } else {
                pixelsPerUnitY = ((double) pixelsPerHatchY)
                        / (double) intervalHatchY;
                graphHeight = pixelsPerHatchY * (yRange / intervalHatchY);
            }
        } else {
            pixelsPerHatchY = 0;
            graphHeight = heightWithoutLabels;
        }

        /*
         * Calculate the upper Y boundaries of the color rows.
         */
        computeColorRowHeights();

        /*
         * Compute the rectangular boundaries for the plotted points.
         */
        computePlottedPointBounds();
    }

    /**
     * Compute the color row heights.
     */
    private void computeColorRowHeights() {
        rowColorUpperBounds.clear();
        if (rowColors.isEmpty()) {
            return;
        }
        double pixelsPerColorRow = ((double) graphHeight)
                / (double) rowColors.size();
        double totalSoFar = 0.0;
        for (int j = 0; j < rowColors.size() - 1; j++) {
            rowColorUpperBounds
                    .add((int) (totalSoFar + pixelsPerColorRow + 0.5));
            totalSoFar += pixelsPerColorRow;
        }
        rowColorUpperBounds.add(graphHeight);
    }

    /**
     * Compute the rectangular boundaries for all the plotted points.
     */
    private void computePlottedPointBounds() {
        plottedPointsForPlottedPointBoundaries.clear();
        for (PlottedPoint point : plottedPoints) {
            computePlottedPointBounds(point);
        }
    }

    /**
     * Compute the rectangular boundaries for the specified plotted point.
     * 
     * @param plottedPoint
     *            Plotted point for which to compute the boundaries.
     */
    private void computePlottedPointBounds(PlottedPoint plottedPoint) {

        /*
         * Calculate the plotted point pixel values differently depending upon
         * whether a coordinate happens to be on one of the hatch marks for its
         * axis. If it is, calculating it this way ensures that no rounding
         * errors cause the bounding box to be slightly off. Otherwise,
         * calculate in a more straightforward manner using pixels per unit.
         */
        int x, y;
        if ((pixelsPerHatchX != 0)
                && ((plottedPoint.getX() - minimumVisibleValueX)
                        % intervalHatchX == 0)) {
            x = ((plottedPoint.getX() - minimumVisibleValueX) / intervalHatchX)
                    * pixelsPerHatchX;
        } else {
            x = (int) (((plottedPoint.getX() - minimumVisibleValueX) * pixelsPerUnitX) + 0.5);
        }
        if ((pixelsPerHatchY != 0)
                && ((plottedPoint.getY() - minimumVisibleValueY)
                        % intervalHatchY == 0)) {
            y = ((plottedPoint.getY() - minimumVisibleValueY) / intervalHatchY)
                    * pixelsPerHatchY;
        } else {
            y = (int) (((plottedPoint.getY() - minimumVisibleValueY) * pixelsPerUnitY) + 0.5);
        }
        Point offset = getPixelOffsetsToGraph(getClientArea());
        x += offset.x - (POINT_DIAMETER / 2);
        y = offset.y + graphHeight - (y + (POINT_DIAMETER / 2));

        /*
         * Remove any mapping between the plotted point and a rectangle from
         * earlier, and put in the new rectangle instead. The former has to be
         * removed because with a BiMap, a value cannot be added that has an
         * equivalent already existing as another entry's value.
         */
        plottedPointsForPlottedPointBoundaries.inverse().remove(plottedPoint);
        plottedPointsForPlottedPointBoundaries.put(new Rectangle(x, y,
                POINT_DIAMETER, POINT_DIAMETER), plottedPoint);
    }

    /**
     * Get the pixel offset from the edge of the widget to the graph on each
     * axis.
     * 
     * @param clientArea
     *            Client area of the widget.
     * @return Point holding the pixel offsets in each axis.
     */
    private Point getPixelOffsetsToGraph(Rectangle clientArea) {
        return new Point((yLabelWidth == 0 ? 0 : yLabelWidth
                + Y_AXIS_LABEL_BUFFER)
                + clientArea.x, clientArea.y);
    }

    /**
     * Set the plotted points to those specified.
     * 
     * @param points
     *            New plotted points.
     * @param source
     *            Source of the points.
     */
    private void setPlottedPoints(List<PlottedPoint> points, ChangeSource source) {

        /*
         * Remember the new plotted points.
         */
        plottedPoints.clear();
        for (PlottedPoint point : points) {
            plottedPoints.add(new PlottedPoint(point));
        }

        /*
         * Get the new minimum and maximum visible X values; if they have
         * changed, recalculate display measurements. Otherwise, cancel any
         * ongoing drag, since the new values trump anything the user was doing,
         * and recompute the plotted points' boundaries.
         */
        int minimumX = (plottedPoints.isEmpty() ? 0 : plottedPoints.get(0)
                .getX());
        int maximumX = (plottedPoints.isEmpty() ? 0 : plottedPoints.get(
                plottedPoints.size() - 1).getX());
        if ((minimumX != maximumX)
                && ((minimumX != minimumVisibleValueX) || (maximumX != maximumVisibleValueX))) {
            minimumVisibleValueX = minimumX;
            maximumVisibleValueX = maximumX;
            computePreferredSize(true);
        } else {
            cancelOngoingDrag();
            computePlottedPointBounds();
        }

        /*
         * Set the cursor appropriately.
         */
        updateCursor();

        /*
         * Notify listeners of the change and redraw.
         */
        notifyListeners(source);
        redraw();
    }

    /**
     * Paint the widget.
     * 
     * @param e
     *            Event that triggered this invocation.
     */
    private void paintControl(PaintEvent e) {

        /*
         * If not ready for painting, do nothing.
         */
        if (isReadyForPainting() == false) {
            return;
        }

        /*
         * Calculate the preferred size if needed, and determine the width and
         * height to be used when painting this time around.
         */
        computePreferredSize(false);
        Rectangle clientArea = getClientArea();
        Point offset = getPixelOffsetsToGraph(clientArea);

        /*
         * Get the default line width and colors, as they are needed later.
         */
        int lineWidth = e.gc.getLineWidth();
        Color background = e.gc.getBackground();
        Color foreground = e.gc.getForeground();

        /*
         * Draw the background.
         */
        if (e.gc.getBackground() != null) {
            e.gc.fillRectangle(clientArea);
        }

        /*
         * Fill in the colors for the rows, if any.
         */
        if (rowColors.isEmpty() == false) {
            int lastUpperBound = 0;
            for (int j = 0; j < rowColors.size(); j++) {
                int upperBound = rowColorUpperBounds.get(j);
                Rectangle colorArea = new Rectangle(offset.x, offset.y
                        + graphHeight - upperBound, graphWidth, upperBound
                        - lastUpperBound);
                e.gc.setBackground(rowColors.get(j));
                e.gc.fillRectangle(colorArea);
                lastUpperBound = upperBound;
            }
        }

        /*
         * Draw any vertical hatch marks needed.
         */
        if (pixelsPerHatchX != 0) {
            e.gc.setForeground(Display.getDefault().getSystemColor(
                    SWT.COLOR_DARK_GRAY));
            for (int x = 0; x <= graphWidth; x += pixelsPerHatchX) {
                e.gc.drawLine(x + offset.x, offset.y + graphHeight, x
                        + offset.x, offset.y);
            }
        }

        /*
         * Draw any horizontal hatch marks needed.
         */
        if (pixelsPerHatchY != 0) {
            e.gc.setForeground(Display.getDefault().getSystemColor(
                    SWT.COLOR_DARK_GRAY));
            for (int y = 0; y <= graphHeight; y += pixelsPerHatchY) {
                e.gc.drawLine(offset.x, offset.y + graphHeight - y, offset.x
                        + graphWidth, offset.y + graphHeight - y);
            }
        }

        /*
         * Draw any X axis labels needed. Any that might be too close to the
         * previous one is skipped.
         */
        if ((xLabelWidth != 0) && (pixelsPerHatchX != 0)) {
            e.gc.setForeground(Display.getDefault().getSystemColor(
                    SWT.COLOR_BLACK));
            int lastEndpoint = -10000;
            for (int x = 0, value = minimumVisibleValueX; x <= graphWidth; x += (intervalLabelX / intervalHatchX)
                    * pixelsPerHatchX, value += intervalLabelX) {
                if (lastEndpoint > x + offset.x - xLabelWidth) {
                    continue;
                }
                String label = Integer.toString(value)
                        + (suffixLabelX == null ? "" : suffixLabelX);
                Point extent = e.gc.stringExtent(label);
                e.gc.drawString(label, x + offset.x - (extent.x / 2), offset.y
                        + graphHeight + X_AXIS_LABEL_BUFFER, true);
                lastEndpoint = x + offset.x + xLabelWidth;
            }
        }

        /*
         * Draw any Y axis labels needed. Any that might be too close to the
         * previous one is skipped.
         */
        if ((yLabelWidth != 0) && (pixelsPerHatchY != 0)) {
            e.gc.setForeground(Display.getDefault().getSystemColor(
                    SWT.COLOR_BLACK));
            int lastEndpoint = -10000;
            for (int y = 0, value = minimumVisibleValueY; y <= graphHeight; y += (intervalLabelY / intervalHatchY)
                    * pixelsPerHatchY, value += intervalLabelY) {
                if (lastEndpoint > y + offset.y - yLabelHeight) {
                    continue;
                }
                String label = Integer.toString(value)
                        + (suffixLabelY == null ? "" : suffixLabelY);
                Point extent = e.gc.stringExtent(label);
                e.gc.drawString(label, offset.x
                        - (extent.x + Y_AXIS_LABEL_BUFFER), offset.y
                        + graphHeight - (y + (yLabelHeight / 2)), true);
                lastEndpoint = y + offset.y + yLabelHeight;
            }
        }

        /*
         * Draw the border around the widget.
         */
        e.gc.setForeground(Display.getDefault().getSystemColor(
                SWT.COLOR_DARK_GRAY));
        Rectangle borderRect = new Rectangle(offset.x, offset.y, graphWidth,
                graphHeight);
        e.gc.drawRectangle(borderRect);

        /*
         * Save the antialiasing flag, and turn on antialiasing so that the
         * circles and potentially diagonal lines drawn next will have less
         * jaggies.
         */
        int antialias = e.gc.getAntialias();
        e.gc.setAntialias(SWT.ON);

        /*
         * If in user-drawing-of-points mode, display prompting text and, if the
         * user has started drawing, the line drawn so far. Otherwise, if there
         * are plotted points, draw them and the line connecting them.
         */
        Font font = e.gc.getFont();
        Rectangle clippingRect = e.gc.getClipping();
        if (isUserDrawingOfPointsMode()) {

            /*
             * If the prompt text font has not been created yet for the current
             * control font, do so.
             */
            if (promptTextFont == null) {
                FontDescriptor boldDescriptor = FontDescriptor.createFrom(font)
                        .setStyle(SWT.BOLD | SWT.ITALIC);
                promptTextFont = boldDescriptor
                        .createFont(Display.getDefault());
            }
            e.gc.setFont(promptTextFont);

            /*
             * Use the previously-set color for the prompt text if drawing
             * already, otherwise use black.
             */
            if (isReadyForUserDrawingOfPoints()) {
                e.gc.setForeground(Display.getDefault().getSystemColor(
                        SWT.COLOR_BLACK));
            }

            /*
             * Iterate through the lines of text, getting their extents, and
             * then draw them one above the next in the center of the graph.
             */
            List<Point> promptTextExtents = new ArrayList<>(
                    DRAW_POINTS_PROMPT_TEXT.size());
            int textHeight = 0;
            for (String text : DRAW_POINTS_PROMPT_TEXT) {
                Point extent = e.gc.stringExtent(text);
                textHeight += extent.y;
                promptTextExtents.add(extent);
            }
            for (int y = offset.y + (graphHeight / 2) - (textHeight / 2), j = 0; j < DRAW_POINTS_PROMPT_TEXT
                    .size(); y += promptTextExtents.get(j).y, j++) {
                e.gc.drawString(DRAW_POINTS_PROMPT_TEXT.get(j), offset.x
                        + (graphWidth / 2) - (promptTextExtents.get(j).x / 2),
                        y, true);
            }

            /*
             * If any points have been drawn yet, paint those as a path.
             */
            if (userDrawnPoints.isEmpty() == false) {
                e.gc.setClipping(offset.x, offset.y, graphWidth, graphHeight);
                int[] polyLineCoords = new int[userDrawnPoints.size() * 2];
                Iterator<Point> iterator = userDrawnPoints.descendingIterator();
                for (int j = 0; iterator.hasNext(); j++) {
                    Point point = iterator.next();
                    polyLineCoords[j * 2] = point.x;
                    polyLineCoords[(j * 2) + 1] = point.y;
                }
                e.gc.setForeground(Display.getDefault().getSystemColor(
                        SWT.COLOR_BLACK));
                e.gc.setLineWidth(2);
                e.gc.drawPolyline(polyLineCoords);
            }
        } else if (plottedPoints.isEmpty() == false) {

            /*
             * Draw the points, if any, and the line connecting them together.
             * If one of the points is active, draw a halo around it after doing
             * the points themselves and the connecting line.
             */
            BiMap<PlottedPoint, Rectangle> boundariesForPlottedPoints = plottedPointsForPlottedPointBoundaries
                    .inverse();
            e.gc.setBackground(Display.getDefault().getSystemColor(
                    SWT.COLOR_BLACK));
            int[] polyLineCoords = new int[plottedPoints.size() * 2];
            Rectangle activePointBounds = null;
            for (int j = 0; j < plottedPoints.size(); j++) {
                PlottedPoint plottedPoint = plottedPoints.get(j);
                Rectangle bounds = boundariesForPlottedPoints.get(plottedPoint);
                polyLineCoords[j * 2] = bounds.x + (bounds.width / 2);
                polyLineCoords[(j * 2) + 1] = bounds.y + (bounds.height / 2);
                e.gc.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
                if ((activePoint == plottedPoint)
                        || (draggingPoint == plottedPoint)) {
                    activePointBounds = bounds;
                }
            }
            e.gc.setForeground(Display.getDefault().getSystemColor(
                    SWT.COLOR_BLACK));
            e.gc.setLineWidth(2);
            e.gc.drawPolyline(polyLineCoords);
            if (activePointBounds != null) {
                e.gc.setForeground(Display.getDefault().getSystemColor(
                        SWT.COLOR_WHITE));
                e.gc.setLineWidth(4);
                e.gc.drawOval(activePointBounds.x, activePointBounds.y,
                        activePointBounds.width, activePointBounds.height);
            }
        }

        /*
         * Reset the graphics context to its original parameters.
         */
        e.gc.setLineWidth(lineWidth);
        e.gc.setBackground(background);
        e.gc.setForeground(foreground);
        e.gc.setFont(font);
        e.gc.setClipping(clippingRect);
        e.gc.setAntialias(antialias);
    }

    /**
     * Determine whether or not the graph is currently ready for the user to
     * draw points by clicking, dragging, and releasing the mouse, but has not
     * started doing so.
     * 
     * @return True if the graph is ready for drawing, false otherwise.
     */
    private boolean isReadyForUserDrawingOfPoints() {
        return (isUserDrawingOfPointsMode() && userDrawnPoints.isEmpty());
    }

    /**
     * Determine whether or not the graph is currently in user-drawing-of-points
     * mode, meaning it is waiting for the user to do so, or the user has
     * started this process but has not yet completed it.
     * 
     * @return True if in user-drawing-of-points mode, false otherwise.
     */
    private boolean isUserDrawingOfPointsMode() {
        return ((intervalDrawnPointsX > 0) && plottedPoints.isEmpty() && (minimumVisibleValueX != maximumVisibleValueX));
    }

    /**
     * Determine which editable plotted point the specified coordinates are
     * within the bounds of, if any.
     * 
     * @param x
     *            X coordinate, relative to the widget.
     * @param y
     *            Y coordinate, relative to the widget.
     * @return Specifier of the editable plotted point within which the
     *         coordinate exists, or <code>null</code> if the coordinate is not
     *         within an editable plotted point.
     */
    private PlottedPoint getEditablePointForCoordinates(int x, int y) {
        for (Map.Entry<Rectangle, PlottedPoint> entry : plottedPointsForPlottedPointBoundaries
                .entrySet()) {
            if (entry.getValue().isEditable() && entry.getKey().contains(x, y)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Schedule a determination of the currently active plotted point to occur
     * after current events have been processed if the mouse is positioned over
     * the widget and if the widget is active. If the determination is being
     * requested by a process that should occur quickly, this method is
     * preferable to {@link #determineActivePointIfEnabled()}.
     */
    private void scheduleDetermineActivePointIfEnabled() {

        /*
         * If the requested determination is already scheduled or the widget is
         * disabled or invisible, do nothing.
         */
        if (determinationOfActivePointScheduled || (isVisible() == false)
                || (isEnabled() == false)) {
            return;
        }

        /*
         * Schedule a determination to be made after other events are processed.
         */
        determinationOfActivePointScheduled = true;
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                determineActivePointIfEnabled();
            }
        });
    }

    /**
     * Determine the currently active plotted point if the mouse is positioned
     * over the widget and if the widget is active. This method should not be
     * used if the caller requires a quick execution; in that case, use of
     * {@link #scheduleDetermineActivePointIfEnabled()} is preferable.
     */
    private void determineActivePointIfEnabled() {
        determinationOfActivePointScheduled = false;
        if ((isDisposed() == false) && isVisible() && isEnabled()) {
            determineActivePoint();
        }
    }

    /**
     * Respond to the mouse being pressed over the widget.
     * 
     * @param e
     *            Mouse event that occurred.
     */
    private void mousePressOverWidget(MouseEvent e) {

        /*
         * Get the editable point over which the mouse event occurred, if any.
         */
        PlottedPoint newDraggingPoint = getEditablePointForCoordinates(e.x, e.y);

        /*
         * If a point has become active or inactive, redraw.
         */
        if ((draggingPoint != newDraggingPoint)
                && ((draggingPoint == null) || (draggingPoint
                        .equals(newDraggingPoint) == false))) {
            lastDragY = e.y;
            draggingPoint = newDraggingPoint;
            redraw();
        }
    }

    /**
     * Respond to a point being dragged.
     * 
     * @param e
     *            Mouse event that occurred.
     */
    private void plottedPointDragged(MouseEvent e) {
        dragPlottedPointToPoint(draggingPoint, e, false);
        if (isDisposed() == false) {
            redraw();
        }
    }

    /**
     * Respond to a plotted point drag ending.
     * 
     * @param e
     *            Mouse event that occurred, if a mouse event triggered this
     *            invocation, or <code>null</code> if the invocation is the
     *            result of a programmatic change.
     */
    private void plottedPointDragEnded(MouseEvent e) {
        PlottedPoint point = draggingPoint;
        draggingPoint = null;
        if (e != null) {
            dragPlottedPointToPoint(point, e, true);
            if (isDisposed() == false) {
                activePoint = getEditablePointForCoordinates(e.x, e.y);
                updateCursor();
            }
        }
        if (isDisposed() == false) {
            redraw();
        }
    }

    /**
     * Determine the currently active plotted point if the mouse is positioned
     * over the widget.
     */
    private void determineActivePoint() {

        /*
         * Calculate the mouse location relative to this widget.
         */
        Point mouseLocation = getDisplay().getCursorLocation();
        Point offset = toDisplay(0, 0);
        mouseLocation.x -= offset.x;
        mouseLocation.y -= offset.y;

        /*
         * If the mouse is over the widget, process its position to determine
         * which plotted point, if any, should now be active.
         */
        if ((mouseLocation.x >= 0) && (mouseLocation.x < lastWidth)
                && (mouseLocation.y >= 0) && (mouseLocation.y < lastHeight)) {
            mouseOverWidget(mouseLocation.x, mouseLocation.y);
        }
    }

    /**
     * Respond to the mouse cursor moving over the widget when a drag is not
     * occurring, or to an editability change for at least one plotted point.
     * 
     * @param x
     *            X coordinate of the mouse, relative to the widget.
     * @param y
     *            Y coordinate of the mouse, relative to the widget.
     */
    private void mouseOverWidget(int x, int y) {

        /*
         * If the widget is disposed, do nothing.
         */
        if (isDisposed() || (isVisible() == false)) {
            return;
        }

        /*
         * Get the editable plotted point over which the mouse event occurred,
         * if any.
         */
        PlottedPoint newPoint = getEditablePointForCoordinates(x, y);

        /*
         * If a point has become active or inactive, redraw.
         */
        if ((activePoint != newPoint)
                && ((activePoint == null) || (activePoint.equals(newPoint) == false))) {
            activePoint = newPoint;
            updateCursor();
            redraw();
        }
    }

    /**
     * Add the specified user-drawn point to the stack of such points.
     * 
     * @param point
     *            Point to be added.
     */
    private void addUserDrawnPoint(Point point) {

        /*
         * Remove any points that are to the right of, or have the same X as,
         * the new point.
         */
        while (userDrawnPoints.isEmpty() == false) {
            Point previousPoint = userDrawnPoints.peek();
            if (previousPoint.x < point.x) {
                break;
            }
            userDrawnPoints.pop();
            if (previousPoint.x == point.x) {
                break;
            }
        }

        /*
         * Add the new point.
         */
        userDrawnPoints.push(point);
    }

    /**
     * Complete a user-drawing-of-points operation, converting the specified
     * points into plotted points.
     */
    private void finishUserDrawingOfPoints() {

        /*
         * If there is only one point, or the interval between drawn points has
         * been reset since the start of the drawing operation, do nothing.
         */
        if ((userDrawnPoints.size() < 2) || (intervalDrawnPointsX == 0)) {
            userDrawnPoints.clear();
            return;
        }

        /*
         * Iterate through the line segments lying between each pair of adjacent
         * points drawn by the user, using each to calculate the needed plotted
         * points lying to the left that segment's rightmost X boundary (or all
         * remaining ones, if it is the last segment).
         */
        Rectangle clientArea = getClientArea();
        List<PlottedPoint> points = new ArrayList<>();
        Iterator<Point> iterator = userDrawnPoints.descendingIterator();
        Point lastPoint = iterator.next();
        int pointX = minimumVisibleValueX;
        double pixelX = getPixelOffsetsToGraph(clientArea).x;
        while (iterator.hasNext()) {
            Point point = iterator.next();

            /*
             * Determine the last plotted point X value at which the line
             * between this point and the previous point should be used to
             * determine plotted point locations. If there are no more points
             * after this, then the line should be used for all remaining
             * plotted points; otherwise, it should be used for any plotted
             * points not yet calculated with X values up to and including the
             * plotted point X value of this point.
             */
            double endpointX = (iterator.hasNext() ? translatePixelToPlottedPointX(
                    point.x, clientArea) : maximumVisibleValueX);

            /*
             * If the next plotted point that is needed has an X value that is
             * less than or equal to the above-calculated last plotted point X
             * value, compute the slope and offset of the line between the point
             * and the previous point, and iterate through needed plotted
             * points, calculating each one's Y value so that it lies along the
             * line, then translating that to a plotted point Y value and
             * creating a plotted point. Stop when the next needed plotted
             * point's X value is greater than the last plotted point X value
             * for which this line is to be used.
             */
            if (pointX <= endpointX) {
                double m = ((double) (point.y - lastPoint.y))
                        / (double) (point.x - lastPoint.x);
                double b = point.y - (m * point.x);
                while (pointX <= endpointX) {
                    int pointY = translatePixelToPlottedPointY(
                            (int) (((m * pixelX) + b) + 0.5), clientArea);
                    if (pointY < minimumVisibleValueY) {
                        pointY = minimumVisibleValueY;
                    } else if (pointY > maximumVisibleValueY) {
                        pointY = maximumVisibleValueY;
                    }
                    points.add(new PlottedPoint(pointX, pointY, true));
                    pointX += intervalDrawnPointsX;
                    pixelX += pixelsPerDrawnPointX;
                }
            }
            if (pointX > maximumVisibleValueX) {
                break;
            }
            lastPoint = point;
        }

        /*
         * Get rid of the user-drawn points, as they are no longer needed, and
         * then save the plotted points.
         */
        userDrawnPoints.clear();
        setPlottedPoints(points, ChangeSource.USER_GUI_INTERACTION_COMPLETE);
    }

    /**
     * Drag the specified plotted point to the specified point, or as close to
     * it as possible.
     * 
     * @param point
     *            Plotted point to be dragged.
     * @param e
     *            Mouse event that prompted this drag.
     * @param dragEnded
     *            Flag indicating whether or not the drag has ended.
     */
    private void dragPlottedPointToPoint(PlottedPoint point, MouseEvent e,
            boolean dragEnded) {

        /*
         * Get the boundary box for the plotted point being dragged, and adjust
         * it by the delta of vertical movement since the last drag mouse event.
         */
        Rectangle bounds = plottedPointsForPlottedPointBoundaries.inverse()
                .get(point);
        int lastBoundsY = bounds.y;
        bounds.y += e.y - lastDragY;

        /*
         * Ensure that the center of the plotted point in pixels has not been
         * relocated beyond the Y axis lower and upper boundaries.
         */
        Rectangle clientArea = getClientArea();
        int y = e.y;
        if (bounds.y + (bounds.height / 2) < clientArea.y) {
            int delta = clientArea.y - (bounds.y + (bounds.height / 2));
            y += delta;
            bounds.y += delta;
        } else if (bounds.y + (bounds.height / 2) > clientArea.y + graphHeight) {
            int delta = clientArea.y + graphHeight
                    - (bounds.y + (bounds.height / 2));
            y += delta;
            bounds.y += delta;
        }

        /*
         * If nothing has changed as as result of the drag, do nothing more in
         * terms of plotted point adjustment; if the drag ended, just send a
         * notification and recompute the bounding box of the plotted point. The
         * latter is done because multiple bounding boxes may correspond to a
         * single Y value (if there are more vertical pixels than vertical
         * plotted point units). By recomputing the point's bounding box, the
         * latter is given its "canonical" position.
         */
        if (bounds.y == lastBoundsY) {
            if (dragEnded) {
                notifyListeners(ChangeSource.USER_GUI_INTERACTION_COMPLETE);
                computePlottedPointBounds(point);
            }
            return;
        }

        /*
         * Remember the new center point in pixels for the next drag mouse
         * event.
         */
        lastDragY = y;

        /*
         * Determine the new Y coordinate of the plotted point.
         */
        int pointY = translatePixelToPlottedPointY(bounds.y
                + (bounds.height / 2), clientArea);

        /*
         * If the new Y coordinate is different from the old one, replace the
         * old with the new, and notify the listeners.
         */
        if (pointY != point.getY()) {
            point.setY(pointY);
            notifyListeners(dragEnded ? ChangeSource.USER_GUI_INTERACTION_COMPLETE
                    : ChangeSource.USER_GUI_INTERACTION_ONGOING);
        }

        /*
         * Recompute the plotted point boundaries if the drag has ended. This is
         * done because multiple bounding boxes may correspond to a single Y
         * value (if there are more vertical pixels than vertical plotted point
         * units). By recomputing the point's bounding box, the latter is given
         * its "canonical" position.
         */
        if (dragEnded) {
            computePlottedPointBounds(point);
        }
    }

    /**
     * Cancel any ongoing drag.
     */
    private void cancelOngoingDrag() {
        if (draggingPoint != null) {
            draggingPoint = null;
            notifyListeners(ChangeSource.USER_GUI_INTERACTION_COMPLETE);
        }
    }

    /**
     * Translate the specified pixel X value to a plotted point X value.
     * 
     * @param pixelX
     *            Pixel X value to be translated.
     * @param clientArea
     *            Client area of the graph.
     * @return Plotted point X value, unrounded for purposes of accuracy, since
     *         this method is used to find the first line segment that has a
     *         right boundary greater than or equal to a particular plotted
     *         point X value.
     */
    private double translatePixelToPlottedPointX(int pixelX,
            Rectangle clientArea) {
        int pixelOffsetFromLeft = pixelX - getPixelOffsetsToGraph(clientArea).x;
        if ((pixelsPerHatchX != 0)
                && (Math.abs(pixelOffsetFromLeft) % pixelsPerHatchX == 0)) {
            return ((pixelOffsetFromLeft / pixelsPerHatchX) * intervalHatchX)
                    + minimumVisibleValueX;
        } else {
            return (pixelOffsetFromLeft / pixelsPerUnitX)
                    + minimumVisibleValueX;
        }
    }

    /**
     * Translate the specified pixel Y value to a plotted point Y value.
     * 
     * @param pixelY
     *            Pixel Y value to be translated.
     * @param clientArea
     *            Client area of the graph.
     * @return Plotted point Y value.
     */
    private int translatePixelToPlottedPointY(int pixelY, Rectangle clientArea) {
        int pixelOffsetFromBottom = clientArea.y + graphHeight - pixelY;
        if ((pixelsPerHatchY != 0)
                && (pixelOffsetFromBottom % pixelsPerHatchY == 0)) {
            return ((pixelOffsetFromBottom / pixelsPerHatchY) * intervalHatchY)
                    + minimumVisibleValueY;
        } else {
            return ((int) ((pixelOffsetFromBottom / pixelsPerUnitY) + 0.5))
                    + minimumVisibleValueY;
        }
    }

    /**
     * Update the cursor as appropriate to the current mode.
     */
    private void updateCursor() {
        setCursor(isEnabled() && (isDisposed() == false) ? (isUserDrawingOfPointsMode() ? Display
                .getDefault().getSystemCursor(SWT.CURSOR_CROSS)
                : (activePoint != null ? Display.getDefault().getSystemCursor(
                        SWT.CURSOR_SIZENS) : null))
                : null);
    }

    /**
     * Sort the specified plotted points, returning a sorted version of the
     * list, or <code>null</code> if any of the points have duplicate X values.
     * 
     * @param points
     *            Plotted points to be sorted.
     * @return Sorted copy of the list, or <code>null</code> if the points
     *         contain duplicate X values.
     */
    private List<PlottedPoint> sortPlottedPoints(List<PlottedPoint> points) {
        List<PlottedPoint> copy = new ArrayList<>(points);
        Collections.sort(copy);
        for (int j = 1; j < copy.size(); j++) {
            if (copy.get(j - 1).getX() == copy.get(j).getX()) {
                return null;
            }
        }
        return copy;
    }

    /**
     * Determine whether or not the widget can be displayed, based upon its
     * client size.
     * 
     * @return True if it can be displayed, false otherwise.
     */
    private boolean isReadyForPainting() {
        return ((lastWidth != 0) && (lastHeight != 0));
    }

    /**
     * Notify listeners of value changes.
     */
    private void notifyListeners(ChangeSource source) {
        for (IGraphListener listener : listeners) {
            listener.plottedPointsChanged(this, source);
        }
    }
}
