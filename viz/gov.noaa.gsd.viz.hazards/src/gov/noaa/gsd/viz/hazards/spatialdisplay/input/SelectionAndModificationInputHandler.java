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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import com.raytheon.uf.common.util.Pair;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import gov.noaa.gsd.viz.hazards.spatialdisplay.InputHandlerType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.MutableDrawableInfo;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.MovementManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.RotationManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ScaleManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.SymbolDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.VertexManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

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
 * Sep 12, 2016   15934    Chris.Golden Changed to work with advanced geometries.
 * Sep 23, 2016   15934    Chris.Golden Simplified code for vertex and shape
 *                                      moving edits so that it no longer
 *                                      requires a copy of the pre-edit points
 *                                      to be kept around in case of reversion.
 *                                      Also added ability to move advanced
 *                                      geometries that are not simply a list
 *                                      of coordinates.
 * Sep 29, 2016   15928    Chris.Golden Added method to update visual cues.
 *                                      Changed to use manipulation points in
 *                                      determining what the drawable being
 *                                      edited is on a mouse down. Refactored
 *                                      the ability to move a drawable or a
 *                                      drawable's vertex into new input
 *                                      handler subclasses; such attempts are
 *                                      now delegated to instances of those
 *                                      classes. Added ability to use other
 *                                      new input handlers for delegation of
 *                                      rotating and resizing drawables.
 * Jan 22, 2018   25765    Chris.Golden Changed to bring together algorithms
 *                                      to determine which drawable is the
 *                                      best fit for a certain point into one
 *                                      place, combine them where possible,
 *                                      and ensure consistency, all in the
 *                                      service of handling mouse events that
 *                                      select or modify said drawables.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SelectionAndModificationInputHandler
        extends ModificationCapableInputHandler<ManipulationPoint> {

    // Private Variables

    /**
     * SWT component that acts as the spatial display.
     */
    private Control spatialDisplayControl;

    /**
     * Last X coordinate in display pixels recorded by the mouse event handling
     * methods.
     */
    private int lastX;

    /**
     * Last Y coordinate in display pixels recorded by the mouse event handling
     * methods.
     */
    private int lastY;

    /**
     * Last point in lat-lon coordinates at which a mouse button was pressed.
     */
    private Coordinate lastButtonDownLocation;

    /**
     * Flag indicating whether or not the active drawable, if any, is actually
     * ready to be edited.
     */
    private boolean active;

    /**
     * Flag indicating whether or not the drawable currently being edited is
     * active.
     */
    private boolean editingDrawableActive;

    /**
     * Flag indicating whether or not to allow panning.
     */
    private boolean allowPanning;

    /**
     * Drawable under a mouse-down.
     */
    private AbstractDrawableComponent drawableUnderMouseDown;

    /**
     * Specialist input handler currently being used as a delegate for this
     * handler.
     */
    private BaseInputHandler specialistInputHandler;

    /**
     * Input handler factory, used to create the specialist input handlers that
     * may be used.
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
        specialistInputHandler = null;
        getSpatialDisplay().setCursor(CursorType.ARROW_CURSOR);
    }

    @Override
    public void updateVisualCues() {
        if (isMouseOverSpatialDisplay()) {
            updateMouseCursorAndRecordedVertexIndex(lastX, lastY);
        }
    }

    @Override
    public boolean handleKeyUp(int key) {

        /*
         * Let the superclass do what it needs to do, and then if a delegate
         * input handler exists, let it handle the event.
         */
        boolean consumed = super.handleKeyUp(key);
        if (specialistInputHandler != null) {
            return specialistInputHandler.handleKeyUp(key);
        }
        return consumed;
    }

    @Override
    public boolean handleMouseEnter(Event event) {

        /*
         * Remember these cursor coordinates in case the last known mouse
         * position is needed later.
         */
        lastX = event.x;
        lastY = event.y;

        /*
         * Let the superclass do what it needs to do, and then if a delegate
         * input handler exists, let it handle the event. Otherwise, treat this
         * as a mouse move. Also determine the spatial display control, so that
         * when a check must be done to determine whether the cursor is over the
         * display in the future, it will be possible to do this. (The latter is
         * needed because overriding handleMouseExit() does nothing, as it is
         * never called when the mouse exits the spatial display.)
         */
        super.handleMouseEnter(event);
        spatialDisplayControl = Display.getCurrent().getCursorControl();
        if (specialistInputHandler != null) {
            return specialistInputHandler.handleMouseEnter(event);
        }
        return handleMouseMove(event.x, event.y);
    }

    @Override
    public boolean handleMouseDown(int x, int y, int button) {

        /*
         * Remember these cursor coordinates in case the last known mouse
         * position is needed later.
         */
        lastX = x;
        lastY = y;

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if ((specialistInputHandler != null)
                && specialistInputHandler.handleMouseDown(x, y, button)) {
            return true;
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
                 * Determine whether there is a drawable to be the one being
                 * edited or not. If there is one with a manipulation point
                 * under the mouse cursor, use it. Otherwise, look for one.
                 */
                AbstractDrawableComponent drawable = null;
                ManipulationPoint manipulationPoint = getManipulationPointUnderMouse();
                if (manipulationPoint != null) {
                    editingDrawableActive = true;
                    drawable = manipulationPoint.getDrawable();
                    drawableUnderMouseDown = drawable;
                } else {

                    /*
                     * Get the active and reactive drawable identifiers.
                     */
                    Set<IEntityIdentifier> activeIdentifiers = getSpatialDisplay()
                            .getActiveSpatialEntityIdentifiers();
                    Set<IEntityIdentifier> reactiveIdentifiers = getReactiveDrawableIdentifiers();

                    /*
                     * Get the primary and alternate drawables for the point.
                     */
                    Pair<AbstractDrawableComponent, AbstractDrawableComponent> drawablesUnderPoint = getSpatialDisplay()
                            .getDrawablesBestMatchingPoint(activeIdentifiers,
                                    reactiveIdentifiers, location, x, y);
                    drawable = drawablesUnderPoint.getFirst();
                    drawableUnderMouseDown = drawablesUnderPoint.getSecond();
                    editingDrawableActive = ((drawable != null)
                            && activeIdentifiers.contains(
                                    ((IDrawable<?>) drawable).getIdentifier()));
                }

                /*
                 * Allow panning if nothing is under the mouse down.
                 */
                allowPanning = (drawable == null);

                /*
                 * Set the editable drawable to that found above.
                 */
                getSpatialDisplay().setDrawableBeingEdited(drawable);

                /*
                 * This may end up being a shape move; in case it is, remember
                 * the starting position.
                 */
                lastButtonDownLocation = new Coordinate(location);
            }
        } else if (button == 2) {
            allowPanning = true;
        }

        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {

        /*
         * Remember these cursor coordinates in case the last known mouse
         * position is needed later.
         */
        lastX = x;
        lastY = y;

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if ((specialistInputHandler != null)
                && specialistInputHandler.handleMouseMove(x, y)) {
            return true;
        }

        /*
         * Let the superclass handle the movement.
         */
        super.handleMouseMove(x, y);

        /*
         * Update the mouse cursor and recorded vertex index, if any.
         */
        updateMouseCursorAndRecordedVertexIndex(x, y);
        getSpatialDisplay().issueRefresh();

        return false;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {

        /*
         * Remember these cursor coordinates in case the last known mouse
         * position is needed later.
         */
        lastX = x;
        lastY = y;

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if ((specialistInputHandler != null)
                && specialistInputHandler.handleMouseDownMove(x, y, button)) {
            return true;
        }

        /*
         * Get the cursor location in lat-lon coordinates.
         */
        Coordinate location = getLocationFromPixels(x, y);
        if (location == null) {
            return false;
        }

        /*
         * Reset the drawable under mouse down tracker, since mouse dragging
         * means it will not be used.
         */
        drawableUnderMouseDown = null;

        /*
         * If the left button is down and a manipulation point is under the
         * mouse cursor, get the appropriate input handler to start modifying
         * the drawable in the manner appropriate given the manipulation point.
         */
        if ((button == 1) && active) {
            AbstractDrawableComponent drawableBeingEdited = getSpatialDisplay()
                    .getDrawableBeingEdited();
            if (getManipulationPointUnderMouse() instanceof VertexManipulationPoint) {
                VertexMoveInputHandler handler = inputHandlerFactory
                        .getNonDrawingInputHandler(
                                InputHandlerType.VERTEX_MOVE);
                handler.initialize(drawableBeingEdited,
                        (VertexManipulationPoint) getManipulationPointUnderMouse(),
                        location);
                specialistInputHandler = handler;
                return true;
            } else if (getManipulationPointUnderMouse() instanceof RotationManipulationPoint) {

                /*
                 * Create a new rotation manipulation point that is the same as
                 * the one triggering this rotation, but with its location being
                 * the last place the mouse button was pressed, as this will
                 * make any movement of the cursor be relative to where it was
                 * pressed, not the center of the original manipulation point
                 * (which may be different by a few pixels, making relative
                 * cursor movement calculations get thrown off).
                 */
                RotationManipulationPoint originalManipulationPoint = (RotationManipulationPoint) getManipulationPointUnderMouse();
                RotationManipulationPoint manipulationPoint = new RotationManipulationPoint(
                        originalManipulationPoint.getDrawable(),
                        lastButtonDownLocation,
                        originalManipulationPoint.getCenter());
                RotateInputHandler handler = inputHandlerFactory
                        .getNonDrawingInputHandler(InputHandlerType.ROTATE);
                handler.initialize(drawableBeingEdited, manipulationPoint,
                        location);
                specialistInputHandler = handler;
                return true;
            } else if (getManipulationPointUnderMouse() instanceof ScaleManipulationPoint) {

                /*
                 * Create a new scale manipulation point that is the same as the
                 * one triggering this resizing, but with its location being the
                 * last place the mouse button was pressed, as this will make
                 * any movement of the cursor be relative to where it was
                 * pressed, not the center of the original manipulation point
                 * (which may be different by a few pixels, making relative
                 * cursor movement calculations get thrown off).
                 */
                ScaleManipulationPoint originalManipulationPoint = (ScaleManipulationPoint) getManipulationPointUnderMouse();
                ScaleManipulationPoint manipulationPoint = new ScaleManipulationPoint(
                        originalManipulationPoint.getDrawable(),
                        lastButtonDownLocation,
                        originalManipulationPoint.getCenter(),
                        originalManipulationPoint.getDirection());
                ResizeInputHandler handler = inputHandlerFactory
                        .getNonDrawingInputHandler(InputHandlerType.RESIZE);
                handler.initialize(drawableBeingEdited, manipulationPoint,
                        location);
                specialistInputHandler = handler;
                return true;
            }
        }

        /*
         * Get the appropriate multi-selection input handler and use it as a
         * delegate from here on (until it asks to no longer be used) if one of
         * the corresponding modifier keys is down.
         */
        if (isShiftDown() || isControlDown()) {
            specialistInputHandler = inputHandlerFactory
                    .getNonDrawingInputHandler(isShiftDown()
                            ? InputHandlerType.RECTANGLE_MULTI_SELECTION
                            : InputHandlerType.FREEHAND_MULTI_SELECTION);
            return true;
        }

        /*
         * If the drawable being edited is not active, allow panning.
         */
        AbstractDrawableComponent editedDrawable = getSpatialDisplay()
                .getDrawableBeingEdited();
        if ((editedDrawable != null) && (editingDrawableActive == false)) {
            allowPanning = true;
        }

        /*
         * If no panning is allowed, begin the move of the entire shape if there
         * is one to move.
         */
        if ((allowPanning == false) && active) {
            beginShapeMove(location);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean handleMouseUp(final int x, final int y, int button) {

        /*
         * Remember these cursor coordinates in case the last known mouse
         * position is needed later.
         */
        lastX = x;
        lastY = y;

        /*
         * If a delegate input handler exists, let it handle the event.
         */
        if ((specialistInputHandler != null)
                && specialistInputHandler.handleMouseUp(x, y, button)) {
            return true;
        }

        /*
         * If the right button, tell the spatial presenter that a location was
         * selected; if the middle button, add or delete a vertex if something
         * was being edited; otherwise, otherwise, if an unselected drawable is
         * under the mouse and the mouse did not move, select that drawable.
         */
        boolean result = true;
        if (button == 3) {
            Coordinate location = translatePixelToWorld(x, y);
            getSpatialDisplay().handleUserSelectionOfLocation(location);
        } else if (button == 2) {
            handleVertexAdditionOrDeletion(x, y);
        } else if (drawableUnderMouseDown != null) {
            boolean multipleSelection = (isShiftDown() || isControlDown());
            getSpatialDisplay().handleUserSingleDrawableSelection(
                    drawableUnderMouseDown, multipleSelection);
        } else {
            result = false;
        }

        /*
         * Finish up the handling of this sequence of mouse events.
         */
        finalizeMouseHandling();
        return result;
    }

    // Private Methods

    /**
     * Determine whether or not the mouse is currently over the spatial display.
     * 
     * @return <code>true</code> if the mouse is over the spatial display,
     *         <code>false</code> otherwise.
     */
    private boolean isMouseOverSpatialDisplay() {
        return (spatialDisplayControl == Display.getCurrent()
                .getCursorControl());
    }

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
        active = false;
        clearManipulationPointUnderMouse();
        MutableDrawableInfo mutableDrawableInfo = getSpatialDisplay()
                .getMutableDrawableInfoUnderPoint(x, y, false);

        /*
         * If there is no mutable drawable here, remove any highlit drawable,
         * and set the cursor to be standard. Otherwise, make the drawable
         * highlit, and if the drawable is editable, use its vertices for the
         * handle bars. Also if editable, set the cursor according to whether
         * the mouse is over a vertex, not over a vertex but near the edge of
         * the drawable, or far from the edge. Also record the vertex if one is
         * under the mouse. Finally, if there is a movable but uneditable
         * drawable under the mouse, set the cursor to indicate this and remove
         * any handle bars.
         */
        if (mutableDrawableInfo.getDrawable() == null) {
            getSpatialDisplay().clearHighlitDrawable();
            getSpatialDisplay().setHandlebarPoints(null);
            getSpatialDisplay().setCursor(CursorType.ARROW_CURSOR);
        } else {
            active = mutableDrawableInfo.isActive();
            AbstractDrawableComponent drawable = mutableDrawableInfo
                    .getDrawable();
            getSpatialDisplay().setHighlitDrawable(drawable, active);
            List<ManipulationPoint> manipulationPoints = ((IDrawable<?>) drawable)
                    .getManipulationPoints();
            if (manipulationPoints.isEmpty() == false) {
                getSpatialDisplay()
                        .setHandlebarPoints(active ? manipulationPoints : null);
                if (active && (mutableDrawableInfo
                        .getManipulationPoint() == null)) {
                    getSpatialDisplay()
                            .setCursor(mutableDrawableInfo.getEdgeIndex() > -1
                                    ? CursorType.DRAW_CURSOR
                                    : CursorType.MOVE_SHAPE_CURSOR);
                } else {
                    setManipulationPointUnderMouse(
                            mutableDrawableInfo.getManipulationPoint());
                    if (active) {
                        getSpatialDisplay().setCursor(mutableDrawableInfo
                                .getManipulationPoint().getCursor());
                    }
                }
            } else {
                getSpatialDisplay().setHandlebarPoints(null);
                if (active) {
                    getSpatialDisplay().setCursor(CursorType.MOVE_SHAPE_CURSOR);
                }
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
        Set<AbstractDrawableComponent> reactiveDrawables = getSpatialDisplay()
                .getReactiveDrawables();
        Set<IEntityIdentifier> reactiveIdentifiers = new HashSet<>(
                reactiveDrawables.size(), 1.0f);
        for (AbstractDrawableComponent reactiveDrawable : reactiveDrawables) {
            reactiveIdentifiers
                    .add(((IDrawable<?>) reactiveDrawable).getIdentifier());
        }
        return reactiveIdentifiers;
    }

    /**
     * Begin the possible movement of the edited drawable.
     * 
     * @param location
     *            New location in world coordinates.
     */
    private void beginShapeMove(Coordinate location) {
        AbstractDrawableComponent editedDrawable = getSpatialDisplay()
                .getDrawableBeingEdited();
        if (editedDrawable != null) {
            if (getSpatialDisplay().isDrawableMovable(editedDrawable)) {

                /*
                 * If nothing is being dragged yet, determine whether or not the
                 * drag point is close enough to the edited drawable, and if so,
                 * begin the drag.
                 */
                if (getGhostDrawable() == null) {
                    GeometryFactory geometryFactory = new GeometryFactory();
                    Point clickPoint = geometryFactory.createPoint(location);

                    /*
                     * Get the distance between the point and the edited
                     * drawable.
                     */
                    double distance;
                    if (editedDrawable instanceof SymbolDrawable) {
                        Point centroid = geometryFactory
                                .createPoint(editedDrawable.getPoints().get(0));
                        distance = centroid.distance(clickPoint);
                    } else {

                        /*
                         * Make a copy of the points from the edited drawable so
                         * that they may be turned into a path for checking
                         * distance.
                         */
                        List<Coordinate> drawnPoints = editedDrawable
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
                        LineString lineString = geometryFactory
                                .createLineString(drawnPointsCopy);
                        distance = lineString.distance(clickPoint);
                    }

                    /*
                     * If the distance is small enough, let the movement
                     * specialist input handler do the work.
                     */
                    if (distance < SpatialDisplay.SLOP_DISTANCE_PIXELS) {
                        MoveInputHandler handler = inputHandlerFactory
                                .getNonDrawingInputHandler(
                                        InputHandlerType.MOVE);
                        handler.initialize(editedDrawable,
                                new MovementManipulationPoint(editedDrawable,
                                        lastButtonDownLocation),
                                location);
                        specialistInputHandler = handler;
                    }
                }
            }
        }
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
        if (active == false) {
            return;
        }
        if (getManipulationPointUnderMouse() instanceof VertexManipulationPoint) {
            getSpatialDisplay().deleteVertex(x, y);
            clearManipulationPointUnderMouse();
        } else {
            setManipulationPointUnderMouse(getSpatialDisplay().addVertex(x, y));
        }
    }

    /**
     * Finish up whatever selection or modification operation is in process.
     */
    @Override
    protected void finalizeMouseHandling() {
        drawableUnderMouseDown = null;
        allowPanning = false;
        active = false;
        super.finalizeMouseHandling();
    }
}
