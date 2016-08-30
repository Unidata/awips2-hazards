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

import gov.noaa.gsd.viz.hazards.spatialdisplay.InputHandlerType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.MutableDrawableInfo;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.SymbolDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.MultiPointElement;
import gov.noaa.nws.ncep.ui.pgen.elements.SinglePointElement;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Event;

import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Description: Input handler for selection or drawable modification modes in
 * the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation (adapted from the old
 *                                      SelectionAction innner class).
 * Aug 22, 2016   19537    Chris.Golden Renamed selected elements to be reactive
 *                                      elements, since this class only cares
 *                                      about which elements may react to a
 *                                      mouse over, not which elements are
 *                                      selected. Also changed call to spatial
 *                                      display's useHandleBarPoints() to work
 *                                      with new version. Also fixed bug in
 *                                      finalizeMouseHandling() that caused a
 *                                      NullPointerException in some cases.
 *                                      Changed code to work with new names of
 *                                      spatial display methods.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SelectionAndModificationInputHandler extends
        NonDrawingInputHandler {

    // Private Static Constants

    /**
     * Colors for the ghost element.
     */
    Color[] GHOST_COLORS = new Color[] { Color.WHITE, Color.WHITE };

    // Private Variables

    /**
     * Coordinate of the previous cursor location.
     */
    protected Coordinate previousLocation = null;

    /**
     * Coordinates from multi-point shape prior to the current edit, if any edit
     * is in progress.
     */
    List<Coordinate> preEditPoints = null;

    /**
     * Ghost drawable.
     */
    AbstractDrawableComponent ghostDrawable = null;

    /**
     * Selected point.
     */
    Coordinate selectedPoint = null;

    /**
     * Index of the line or polygon vertex over which the mouse is hovering; if
     * <code>-1</code>, it is not over a vertex.
     */
    private int vertexIndexUnderMouse = -1;

    /**
     * Flag indicating if a move of a line or polygon vertex is in progress.
     */
    private boolean vertexMoving = false;

    /**
     * Flag indicating whether or not to allow panning.
     */
    private boolean allowPanning = false;

    /**
     * Drawable element under a mouse-down.
     */
    private AbstractDrawableComponent drawableUnderMouseDown;

    /**
     * Multi-selection input handler currently being used as a delegate for this
     * handler.
     */
    private MultiSelectionInputHandler multiSelectionInputHandler;

    /**
     * Input handler factory, used to create the multi-selection input handlers
     * that may be used.
     */
    private final InputHandlerFactory inputHandlerFactory;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public SelectionAndModificationInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
        inputHandlerFactory = new InputHandlerFactory(spatialDisplay);
    }

    // Public Methods

    @Override
    public void reset() {
        super.reset();
        multiSelectionInputHandler = null;
        finalizeMouseHandling();
        getSpatialDisplay().setCursor(CursorTypes.ARROW_CURSOR);
    }

    @Override
    public boolean handleKeyUp(int key) {

        /*
         * Let the superclass do what it needs to do, and then if a delegate
         * input handler exists, let it handle the event.
         */
        boolean consumed = super.handleKeyUp(key);
        if (multiSelectionInputHandler != null) {
            return multiSelectionInputHandler.handleKeyUp(key);
        }
        return consumed;
    }

    @Override
    public boolean handleMouseEnter(Event event) {

        /*
         * Let the superclass do what it needs to do, and then if a delegate
         * input handler exists, let it handle the event. Otherwise, treat this
         * as a mouse move.
         */
        super.handleMouseEnter(event);
        if (multiSelectionInputHandler != null) {
            return multiSelectionInputHandler.handleMouseEnter(event);
        }
        return handleMouseMove(event.x, event.y);
    }

    @Override
    public boolean handleMouseDown(int x, int y, int button) {

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if (multiSelectionInputHandler != null) {
            return multiSelectionInputHandler.handleMouseDown(x, y, button);
        }

        /*
         * Determine what, if any, was clicked if the left mouse button was
         * pressed. If the middle button was pressed, allow panning.
         */
        drawableUnderMouseDown = null;
        if (button == 1) {
            Coordinate location = translatePixelToWorld(x, y);
            if (location != null) {

                /*
                 * Retrieve a list of drawables which contain this mouse click
                 * point, with the first drawable in the list being topmost.
                 */
                AbstractDrawableComponent drawable = null;
                List<AbstractDrawableComponent> containingDrawables = getSpatialDisplay()
                        .getContainingDrawables(location, x, y);

                /*
                 * Get the reactive drawable identifiers.
                 */
                Set<IEntityIdentifier> reactiveIdentifiers = getReactiveDrawableIdentifiers();

                /*
                 * If there is at least one containing drawable, make sure that
                 * the topmost drawable is equal to one of the reactive ones. If
                 * there are symbols (including points), give them precedence.
                 */
                for (AbstractDrawableComponent containingDrawable : containingDrawables) {
                    IEntityIdentifier identifier = ((IDrawable) containingDrawable)
                            .getIdentifier();
                    if (((drawable == null) || (containingDrawable instanceof SymbolDrawable))
                            && reactiveIdentifiers.contains(identifier)
                            && (getSpatialDisplay().isDrawableEditable(
                                    containingDrawable) || getSpatialDisplay()
                                    .isDrawableMovable(containingDrawable))) {
                        drawable = containingDrawable;
                        if (containingDrawable instanceof SymbolDrawable) {
                            break;
                        }
                    }
                }

                /*
                 * If none of the reactive drawables were found to have been
                 * clicked, then just pick the topmost drawable containing the
                 * click. Also choose the topmost one that is uneditable and
                 * unmovable, in case it needs to be selected during mouse-up
                 * later.
                 * 
                 * TODO: May need a better way of determining which containing
                 * drawable of multiple drawables to choose as the selected?
                 */
                if (drawable == null) {
                    for (AbstractDrawableComponent containingDrawable : containingDrawables) {
                        if (getSpatialDisplay().isDrawableEditable(
                                containingDrawable)
                                || getSpatialDisplay().isDrawableMovable(
                                        containingDrawable)) {
                            drawable = containingDrawable;
                            break;
                        } else if (drawableUnderMouseDown == null) {
                            drawableUnderMouseDown = containingDrawable;
                        }
                    }
                }

                /*
                 * If something was found to be editable, but nothing was set
                 * for the drawable element under the mouse down, set the latter
                 * to the former. This way, if nothing occurs except for a mouse
                 * up, an unselected element may be selected by the click.
                 */
                if (drawableUnderMouseDown == null) {
                    drawableUnderMouseDown = drawable;
                }

                /*
                 * Allow panning if nothing is under the mouse down.
                 */
                allowPanning = (drawable == null);

                getSpatialDisplay().setDrawableBeingEdited(drawable);
            }
        } else if (button == 2) {
            allowPanning = true;
        }

        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if (multiSelectionInputHandler != null) {
            return multiSelectionInputHandler.handleMouseMove(x, y);
        }

        /*
         * Let the superclass handle the movement.
         */
        super.handleMouseMove(x, y);

        /*
         * Update the mouse cursor and recorded vertex index, if any.
         */
        updateMouseCursorAndRecordedVertexIndex(x, y);

        return false;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if (multiSelectionInputHandler != null) {
            return multiSelectionInputHandler.handleMouseDownMove(x, y, button);
        }

        /*
         * Retrieve a list of drawables which contain the mouse cursor's
         * location, if any.
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        Coordinate location = editor.translateClick(x, y);

        /*
         * Check for a null coordinate. If it is null use the previous
         * coordinate. If not save it off as the previous.
         */
        if (location == null) {
            if (previousLocation != null) {
                location = previousLocation;
            } else {
                return false;
            }
        } else {
            previousLocation = location;
        }

        /*
         * Reset the drawable under mouse down tracker, since mouse dragging
         * means it will not be used.
         */
        drawableUnderMouseDown = null;

        /*
         * If the left button is down and a single vertex is being hovered over,
         * attempt to move it.
         */
        if ((button == 1) && (vertexIndexUnderMouse != -1)) {
            handleVertexMove(location);
            return true;
        }

        /*
         * Get the appropriate multi-selection input handler and use it as a
         * delegate from here on (until it asks to no longer be used) if one of
         * the corresponding modifier keys is down.
         */
        if (isShiftDown() || isControlDown()) {
            multiSelectionInputHandler = (MultiSelectionInputHandler) inputHandlerFactory
                    .getNonDrawingInputHandler(isShiftDown() ? InputHandlerType.RECTANGLE_MULTI_SELECTION
                            : InputHandlerType.FREEHAND_MULTI_SELECTION);
            multiSelectionInputHandler.reset();
            return true;
        }

        /*
         * If the drawable being edited is not reactive, allow panning.
         */
        AbstractDrawableComponent editedElement = getSpatialDisplay()
                .getDrawableBeingEdited();
        if ((editedElement != null)
                && (getSpatialDisplay().getReactiveDrawables().contains(
                        editedElement) == false)) {
            allowPanning = true;
        }

        /*
         * If no panning is allowed, attempt to move the entire shape if there
         * is one to move.
         */
        if (allowPanning == false) {
            handleShapeMove(location, x, y);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean handleMouseUp(final int x, final int y, int button) {

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if (multiSelectionInputHandler != null) {
            return multiSelectionInputHandler.handleMouseUp(x, y, button);
        }

        /*
         * If the right button, tell the spatial presenter that a location was
         * selected; if the middle button, add or delete a vertex if the
         * something was being edited; otherwise, if a vertex was moving, finish
         * the move; otherwise, if a whole drawable was being moved, finish the
         * move; otherwise, if an unselected element is under the mouse and the
         * mouse did not move, select that element).
         */
        boolean result = true;
        boolean updateMouseCursor = true;
        if (button == 3) {
            Coordinate location = translatePixelToWorld(x, y);
            getSpatialDisplay().handleUserSelectionOfLocation(location);
            updateMouseCursor = false;
        } else if (button == 2) {
            handleVertexAdditionOrDeletion(x, y);
        } else if (vertexMoving) {
            handleFinishVertexMove();
        } else if (ghostDrawable != null) {
            handleFinishShapeMove();
        } else if (drawableUnderMouseDown != null) {
            boolean multipleSelection = (isShiftDown() || isControlDown());
            getSpatialDisplay().handleUserSingleDrawableSelection(
                    drawableUnderMouseDown, multipleSelection);
        } else {
            updateMouseCursor = false;
            result = false;
        }

        /*
         * Finish up the handling of this sequence of mouse events.
         */
        finalizeMouseHandling();

        /*
         * If the mouse cursor should be updated, schedule this for later, once
         * the drawables have been updated.
         * 
         * TODO: This doesn't really work. Instead, the update needs to happen
         * in response to new drawables. Should it be handled within the spatial
         * display?
         */
        if (updateMouseCursor) {
            VizApp.runAsync(new Runnable() {
                @Override
                public void run() {
                    updateMouseCursorAndRecordedVertexIndex(x, y);
                }
            });
        }
        return result;
    }

    // Private Methods

    /**
     * Update the mouse cursor and the recorded vertex index as appropriate
     * given the specified mouse position.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     */
    private void updateMouseCursorAndRecordedVertexIndex(int x, int y) {

        /*
         * Get the information about any mutable (editable or movable) drawable
         * under the current mouse location.
         */
        vertexIndexUnderMouse = -1;
        MutableDrawableInfo mutableDrawableInfo = getSpatialDisplay()
                .getMutableDrawableInfoUnderPoint(x, y);

        /*
         * If there is no mutable drawable here, remove any hover element, and
         * set the cursor to be standard. Otherwise, if the drawable is
         * editable, use it for the hover element, and set the cursor according
         * to whether the mouse is over a vertex, not over a vertex but near the
         * edge of the drawable, or far from the edge. Also record the vertex if
         * one is under the mouse. Finally, if there is a movable but uneditable
         * drawable under the mouse, set the cursor to indicate this and remove
         * any hover element.
         */
        if (mutableDrawableInfo.getDrawable() == null) {
            getSpatialDisplay().setHoverDrawable(null);
            getSpatialDisplay().setCursor(CursorTypes.ARROW_CURSOR);
        } else {
            if (getSpatialDisplay().isDrawableEditable(
                    mutableDrawableInfo.getDrawable())) {
                getSpatialDisplay().setHoverDrawable(
                        mutableDrawableInfo.getDrawable());
                if (mutableDrawableInfo.getVertexIndex() == -1) {
                    getSpatialDisplay()
                            .setCursor(
                                    mutableDrawableInfo.isCloseToEdge() ? CursorTypes.DRAW_CURSOR
                                            : CursorTypes.MOVE_SHAPE_CURSOR);
                } else {
                    getSpatialDisplay().setCursor(
                            CursorTypes.MOVE_VERTEX_CURSOR);
                    vertexIndexUnderMouse = mutableDrawableInfo
                            .getVertexIndex();
                }
            } else {
                getSpatialDisplay().setHoverDrawable(null);
                getSpatialDisplay().setCursor(CursorTypes.MOVE_SHAPE_CURSOR);
            }
        }
    }

    /**
     * Get the set of reactive drawable identifiers, that is, those drawables
     * that may react when moused over to indicate they are editable, movable,
     * etc.
     * 
     * @return Set of reactive drawable identifiers.
     */
    private Set<IEntityIdentifier> getReactiveDrawableIdentifiers() {
        Set<AbstractDrawableComponent> reactiveElements = getSpatialDisplay()
                .getReactiveDrawables();
        Set<IEntityIdentifier> reactiveIdentifiers = new HashSet<>(
                reactiveElements.size(), 1.0f);
        for (AbstractDrawableComponent reactiveElement : reactiveElements) {
            reactiveIdentifiers.add(((IDrawable) reactiveElement)
                    .getIdentifier());
        }
        return reactiveIdentifiers;
    }

    /**
     * Handle the possible movenent of a shape.
     * 
     * @param location
     *            New location in world coordinates.
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     */
    private void handleShapeMove(Coordinate location, int x, int y) {
        AbstractDrawableComponent editedElement = getSpatialDisplay()
                .getDrawableBeingEdited();
        if (editedElement != null) {
            if (getSpatialDisplay().isDrawableMovable(editedElement)) {

                /*
                 * If nothing is being dragged yet, determine whether or not the
                 * drag point is close enough to the edited element, and if so,
                 * begin the drag.
                 */
                if (ghostDrawable == null) {
                    GeometryFactory gf = new GeometryFactory();
                    Point clickPoint = gf.createPoint(location);

                    /*
                     * Get the distance between the point and the edited
                     * element.
                     */
                    double distance;
                    if (editedElement instanceof SymbolDrawable) {
                        Point centroid = gf.createPoint(editedElement
                                .getPoints().get(0));
                        distance = centroid.distance(clickPoint);
                    } else {

                        /*
                         * Make a copy of the points from the edited element so
                         * that they may be turned into a path for checking
                         * distance.
                         */
                        List<Coordinate> drawnPoints = editedElement
                                .getPoints();
                        Coordinate[] drawnPointsCopy = new Coordinate[drawnPoints
                                .size() + 1];
                        drawnPointsCopy = drawnPoints.toArray(drawnPointsCopy);
                        drawnPointsCopy[drawnPoints.size()] = drawnPoints
                                .get(0);

                        /*
                         * Create a line string and get the distance between it
                         * and the point.
                         */
                        LineString ls = gf.createLineString(drawnPointsCopy);
                        distance = ls.distance(clickPoint);
                    }

                    /*
                     * If the distance is small enough, set the selected point
                     * and copy the edited element to be the ghost.
                     */
                    if (distance < SpatialDisplay.SLOP_DISTANCE_PIXELS) {
                        selectedPoint = new Coordinate(clickPoint.getX(),
                                clickPoint.getY());
                        ghostDrawable = getSpatialDisplay()
                                .getDrawableBeingEdited().copy();
                    }
                }

                /*
                 * Get the new position of the ghost drawable.
                 */
                if (ghostDrawable != null) {
                    AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                            .getInstance().getActiveEditor());
                    double[] pixelCoordinates = editor
                            .translateInverseClick(selectedPoint);
                    double deltaX = x - pixelCoordinates[0];
                    double deltaY = y - pixelCoordinates[1];

                    /*
                     * If this the beginning of the edit, copy the points first
                     * so that the edit can be reverted if necessary.
                     */
                    List<Coordinate> points = editedElement.getPoints();
                    if (preEditPoints == null) {
                        preEditPoints = new ArrayList<>(points);
                    }

                    /*
                     * Calculate coordinates of the ghost drawable.
                     */
                    for (int j = 0; j < points.size(); j++) {
                        pixelCoordinates = editor
                                .translateInverseClick(editedElement
                                        .getPoints().get(j));
                        pixelCoordinates[0] += deltaX;
                        pixelCoordinates[1] += deltaY;
                        Coordinate point = editor.translateClick(
                                pixelCoordinates[0], pixelCoordinates[1]);
                        if (point == null) {
                            continue;
                        }
                        ghostDrawable.getPoints().get(j).x = point.x;
                        ghostDrawable.getPoints().get(j).y = point.y;

                    }

                    /*
                     * Set the ghost color.
                     */
                    ghostDrawable.setColors(GHOST_COLORS);

                    /*
                     * Set the ghost and hover drawables to be the same.
                     */
                    getSpatialDisplay().setGhostOfDrawableBeingEdited(
                            ghostDrawable);
                    getSpatialDisplay().setHoverDrawable(ghostDrawable);
                    getSpatialDisplay().issueRefresh();
                }
            }
        }
    }

    /**
     * Handle the possible movement of a vertex.
     * 
     * @param location
     *            New location in world coordinates.
     */
    private void handleVertexMove(Coordinate location) {
        AbstractDrawableComponent editedElement = getSpatialDisplay()
                .getDrawableBeingEdited();
        if (editedElement != null) {

            /*
             * Only move individual points if the shape allows editing.
             */
            if (((IDrawable) editedElement).isEditable()) {
                vertexMoving = true;

                /*
                 * Replace the previous coordinate with the new one, copying
                 * them first in case the change being made now gets reverted
                 * later. If this is a polygon, the last point must be the same
                 * as the first, so ensure that this is the case if the first
                 * point is the one being moved. (The last point is never the
                 * one being moved for polygons.)
                 */
                List<Coordinate> points = editedElement.getPoints();
                if (preEditPoints == null) {
                    preEditPoints = new ArrayList<>(points);
                }
                points.set(vertexIndexUnderMouse, location);
                if (getSpatialDisplay().isPolygon(editedElement)
                        && (vertexIndexUnderMouse == 0)) {
                    points.set(points.size() - 1, location);
                }

                /*
                 * Update the ghost drawable and the handlbar points.
                 */
                ghostDrawable = editedElement.copy();
                getSpatialDisplay()
                        .setGhostOfDrawableBeingEdited(ghostDrawable);
                getSpatialDisplay().useAsHandlebarPoints(points);
                getSpatialDisplay().issueRefresh();
            }
        }
    }

    /**
     * Handle the completion of the movement of the shape being edited.
     */
    private void handleFinishShapeMove() {

        /*
         * Get the drawable being edited.
         */
        AbstractDrawableComponent editedElement = getSpatialDisplay()
                .getDrawableBeingEdited();
        if (editedElement == null) {
            return;
        }
        IDrawable originalShape = (IDrawable) editedElement;

        /*
         * Create the new geometry for the drawable.
         */
        List<Coordinate> coordinates = (ghostDrawable instanceof MultiPointElement ? ((MultiPointElement) ghostDrawable)
                .getPoints() : ((SinglePointElement) ghostDrawable).getPoints());
        Geometry modifiedGeometry = getSpatialDisplay().buildModifiedGeometry(
                originalShape, coordinates);

        /*
         * Notify the spatial display of the change, reset the hover and editing
         * drawables, and refresh the display.
         */
        getSpatialDisplay().handleUserModificationOfDrawable(editedElement,
                modifiedGeometry);
        getSpatialDisplay().setHoverDrawable(null);
        getSpatialDisplay().setDrawableBeingEdited(null);
        getSpatialDisplay().issueRefresh();
        preEditPoints = null;
    }

    /**
     * Handle the completion of the movement of the vertex over which the mouse
     * is hovering within the shape being edited.
     */
    private void handleFinishVertexMove() {

        /*
         * Reset the flag indicating vertex movement is occurring.
         */
        vertexMoving = false;

        /*
         * If there is an element being edited, complete the edit.
         */
        AbstractDrawableComponent editedElement = getSpatialDisplay()
                .getDrawableBeingEdited();
        if (editedElement != null) {
            getSpatialDisplay().setDrawableBeingEdited(null);
            IDrawable entityShape = (IDrawable) editedElement;
            Geometry modifiedGeometry = getSpatialDisplay()
                    .buildModifiedGeometry(entityShape,
                            editedElement.getPoints());
            getSpatialDisplay().setHoverDrawable(null);

            /*
             * If the newly moved vertex results in a valid geometry, use the
             * latter as a modified geometry for the spatial entity; otherwise,
             * reset the drawable's points to be what they were prior to the
             * edit.
             */
            if (getSpatialDisplay().checkGeometryValidity(modifiedGeometry)) {
                getSpatialDisplay().handleUserModificationOfDrawable(
                        editedElement, modifiedGeometry);
            } else {
                List<Coordinate> editedElementPoints = editedElement
                        .getPoints();
                editedElementPoints.clear();
                editedElementPoints.addAll(preEditPoints);
                getSpatialDisplay().issueRefresh();
            }
        }
        preEditPoints = null;
    }

    /**
     * Handle the addition or deletion of a vertex to or from an editable
     * drawable at the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     */
    private void handleVertexAdditionOrDeletion(int x, int y) {
        if (vertexIndexUnderMouse != -1) {
            getSpatialDisplay().deleteVertex(x, y);
            vertexIndexUnderMouse = -1;
        } else {
            vertexIndexUnderMouse = getSpatialDisplay().addVertex(x, y);
        }
    }

    /**
     * Finish up whatever selection or modification operation is in process.
     */
    private void finalizeMouseHandling() {
        getSpatialDisplay().setGhostOfDrawableBeingEdited(null);

        if (preEditPoints != null) {
            DrawableElement drawableBeingEdited = getSpatialDisplay()
                    .getDrawableBeingEdited();
            if (drawableBeingEdited != null) {
                List<Coordinate> editedElementPoints = drawableBeingEdited
                        .getPoints();
                editedElementPoints.clear();
                editedElementPoints.addAll(preEditPoints);
            }
            preEditPoints = null;
        }

        getSpatialDisplay().setDrawableBeingEdited(null);
        ghostDrawable = null;

        drawableUnderMouseDown = null;
        vertexMoving = false;
        allowPanning = false;
        vertexIndexUnderMouse = -1;

        getSpatialDisplay().issueRefresh();
    }
}
