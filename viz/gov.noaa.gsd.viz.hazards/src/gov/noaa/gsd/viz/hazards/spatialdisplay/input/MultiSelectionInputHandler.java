/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.input;

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.PolygonDrawableAttributes;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Base class from which to derive input handlers used for
 * multiple-selection modes in the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation.
 * Aug 28, 2016   19537    Chris.Golden Changed to build drawables
 *                                      without a PGEN layer. Also
 *                                      altered to work with new
 *                                      spatial display method names,
 *                                      and moved code that didn't
 *                                      belong here for searching
 *                                      through drawables into the
 *                                      drawable manager.
 * Sep 29, 2016   15928    Chris.Golden Added updating of visual
 *                                      cues.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class MultiSelectionInputHandler extends NonDrawingInputHandler {

    /**
     * Set of the selected drawables.
     */
    private final Set<AbstractDrawableComponent> selectedDrawables = new HashSet<>();

    /**
     * Drawable element factory, used to create the selection shape.
     */
    private final DrawableElementFactory drawableFactory = new DrawableElementFactory();

    /**
     * Drawing attributes for a selection shape, used to configure a drawable
     * used to show the area being drawn in order to select other drawables.
     */
    private final PolygonDrawableAttributes drawingAttributes;

    /**
     * Key that triggers or ends the use of this multi-selection mode; must be
     * {@link SWT#CTRL} or {@link SWT#SHIFT}.
     */
    private final int triggerKey;

    /**
     * Flag indicating whether or not enough movement has occurred after the
     * start of a multiple selection attempt for the multiple selection shape to
     * be used to select drawables. Note that even if this is <code>false</code>
     * , a selection shape may be shown; it just will not be used for selection
     * changes until this is <code>true</code>.
     */
    private boolean selectionByShape;

    /**
     * Starting pixel coordinates of a potential selection by shape action.
     */
    private Coordinate startPixelLocation;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     * @param triggerKey
     *            Key that triggers or ends the use of this multi-selection
     *            mode; must be {@link SWT#CTRL} or {@link SWT#SHIFT}.
     */
    public MultiSelectionInputHandler(SpatialDisplay spatialDisplay,
            int triggerKey) {
        super(spatialDisplay);
        this.triggerKey = triggerKey;
        drawingAttributes = new PolygonDrawableAttributes(false);
    }

    // Public Methods

    @Override
    public void reset() {
        super.reset();
        getSpatialDisplay().setCursor(CursorType.DRAW_CURSOR);
        clearAnyShape();
        selectedDrawables.clear();
        selectionByShape = false;
        startPixelLocation = null;
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        super.handleMouseEnter(event);
        getSpatialDisplay().setCursor(CursorType.DRAW_CURSOR);
        return handleMouseMove(event.x, event.y);
    }

    @Override
    public boolean handleKeyUp(int key) {
        if (key == triggerKey) {
            if (selectionByShape) {
                finishSelectionByShape(null);
            } else {
                getSpatialDisplay().handleUserResetOfInputMode();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseDown(int x, int y, int button) {

        /*
         * Ensure that the location is within the geographic extent.
         */
        Coordinate location = translatePixelToWorld(x, y);
        if (location == null) {
            return false;
        }

        /*
         * Handle the start of potential multi-selection.
         */
        handlePotentialSelectionByShapeStart(location);
        startPixelLocation = new Coordinate(x, y);
        return true;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {

        /*
         * Ignore any drags involving buttons other than the left one.
         */
        if (button != 1) {
            return false;
        }

        /*
         * Ensure that the location is within the geographic extent.
         */
        Coordinate location = translatePixelToWorld(x, y);
        if (location == null) {
            return false;
        }

        /*
         * If this point is used as the potential start of the selection shape,
         * remember its starting pixel location. Otherwise, handle it as a point
         * to be added.
         */
        if (handlePotentialSelectionByShapeStart(location)) {
            startPixelLocation = new Coordinate(x, y);
        } else {

            /*
             * If the selection shape is still merely potential, see if it may
             * be made definitive if the user has dragged far enough.
             */
            if (selectionByShape == false) {
                if ((startPixelLocation != null)
                        && (startPixelLocation.distance(new Coordinate(x, y)) > SpatialDisplay.SLOP_DISTANCE_PIXELS)) {
                    selectionByShape = true;
                }
            }

            /*
             * Handle the new point added to the selection shape.
             */
            handleSelectionByShapeNewPoint(location);

            /*
             * Get the points of the selection shape, and from it, create a
             * ghost element to be displayed.
             */
            Coordinate[] points = getPointsOfSelectionShape();
            AbstractDrawableComponent ghost = drawableFactory.create(
                    DrawableType.LINE, drawingAttributes, "Line",
                    "LINE_DASHED_2", points, null);
            ((Line) ghost).setLinePoints(Lists.newArrayList(points));
            getSpatialDisplay().setGhostOfDrawableBeingEdited(ghost);
            getSpatialDisplay().visualCuesNeedUpdatingAtNextRefresh();
            getSpatialDisplay().issueRefresh();
        }
        return true;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int button) {

        /*
         * If this is not a left button release, ignore it.
         */
        if (button != 1) {
            return false;
        }

        /*
         * Ensure that the location is within the geographic extent.
         */
        Coordinate location = translatePixelToWorld(x, y);
        if (location == null) {
            return false;
        }

        /*
         * If selecting using a selection shape, finish up use of the shape.
         * Otherwise, toggle the selection state of any drawables under the
         * cursor.
         */
        if (selectionByShape) {
            finishSelectionByShape(location);
        } else if (getSpatialDisplay().getDrawableBeingEdited() == null) {
            toggleSelectionOfDrawables(location, x, y);
        }
        return true;
    }

    // Protected Methods

    /**
     * Handle the start of a potential selection by shape action, if there is
     * not already one recorded.
     * 
     * @param location
     *            Location of the potential start in world coordinates.
     * @return <code>true</code> if the potential start was used,
     *         <code>false</code> if a potential start point was already
     *         recorded.
     */
    protected abstract boolean handlePotentialSelectionByShapeStart(
            Coordinate location);

    /**
     * Handle a new point for a selection by shape action.
     * 
     * @param location
     *            Location of the new point in world coordinates. May be
     *            <code>null</code>, in which case it should be ignored.
     */
    protected abstract void handleSelectionByShapeNewPoint(Coordinate location);

    /**
     * Handle the reset of a selection by shape action.
     */
    protected abstract void handleSelectionByShapeReset();

    /**
     * Determine whether the selection shape is valid.
     * 
     * @return <code>true</code> if the selection shape is valid,
     *         <code>false</code> otherwise.
     */
    protected abstract boolean isSelectionShapeValid();

    /**
     * Get the array of points describing the selection shape.
     * 
     * @return Array of points describing the selection shape.
     */
    protected abstract Coordinate[] getPointsOfSelectionShape();

    /**
     * Get the geometry of the selection shape.
     * 
     * @return Geometry of the selection shape.
     */
    protected abstract Geometry getGeometryOfSelectionShape();

    // Private Methods

    /**
     * Finish the selection by shape action that is currently in progress.
     * 
     * @param location
     *            Location of the final point in the shape; may be
     *            <code>null</code> if the finish is triggered by something
     *            other than a new point.
     */
    private void finishSelectionByShape(Coordinate location) {

        /*
         * If selection by shape is occurring, finish it up. Otherwise, simply
         * reset.
         */
        if (isSelectionShapeValid()) {

            /*
             * Add the new point.
             */
            handleSelectionByShapeNewPoint(location);

            /*
             * Get the selection shape as a geometry.
             */
            Geometry selectionShape = getGeometryOfSelectionShape();

            /*
             * Clear the list of selected drawables and rebuild it, based upon
             * which drawables intersect with the selection shape.
             */
            selectedDrawables.clear();
            selectedDrawables.addAll(getSpatialDisplay()
                    .getIntersectingDrawables(selectionShape));

            /*
             * Tell the spatial display which drawables the user is attempting
             * to select.
             */
            getSpatialDisplay().handleUserMultipleDrawablesSelection(
                    selectedDrawables);
            selectedDrawables.clear();
        }

        /*
         * Remove the ghost element, and reset.
         */
        clearAnyShape();

        /*
         * Get the spatial display to refresh.
         */
        getSpatialDisplay().visualCuesNeedUpdatingAtNextRefresh();
        getSpatialDisplay().issueRefresh();

        /*
         * Indicate that this drawing action is done.
         */
        getSpatialDisplay().handleUserResetOfInputMode();
    }

    /**
     * Clear any shape that is being drawn.
     */
    private void clearAnyShape() {
        getSpatialDisplay().setGhostOfDrawableBeingEdited(null);
        handleSelectionByShapeReset();
        startPixelLocation = null;
        selectionByShape = false;
    }

    /**
     * Toggle the selection state of the drawables in the specified location.
     * 
     * @param location
     *            Location for which to toggle interecting drawables.
     * @param x
     *            Pixel X coordinate of <code>location</code>.
     * @param y
     *            Pixel Y coordinate of <code>location</code>.
     */
    private void toggleSelectionOfDrawables(Coordinate location, int x, int y) {

        /*
         * Remove the ghost element, and reset.
         */
        getSpatialDisplay().setGhostOfDrawableBeingEdited(null);
        handleSelectionByShapeReset();
        startPixelLocation = null;

        /*
         * Get the spatial display to refresh.
         */
        getSpatialDisplay().visualCuesNeedUpdatingAtNextRefresh();
        getSpatialDisplay().issueRefresh();

        /*
         * Iterate through any drawables under the cursor, toggling the
         * selection state of each.
         */
        for (AbstractDrawableComponent drawable : getSpatialDisplay()
                .getContainingDrawables(location, x, y)) {
            if (selectedDrawables.contains(drawable)) {
                selectedDrawables.remove(drawable);
            } else {
                selectedDrawables.add(drawable);
            }
        }

        /*
         * Tell the spatial display which drawables the user is attempting to
         * select.
         */
        getSpatialDisplay().handleUserMultipleDrawablesSelection(
                selectedDrawables);
    }
}
