/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesLine;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesPolygon;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesSymbol;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.IHazardServicesShape;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Drawing action associated with the selection and manipulation of hazard
 * polygons.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * November 2011             Bryon.Lawrence   Initial creation
 * Jul 15, 2013      585     Chris.Golden     Changed to no longer be a singleton.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Sep 10, 2013      782     Bryon.Lawrence   Updated getShapesForEvents to 
 *                                            consider storm track events
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Sep 09, 2014  3994     Robert.Blum         Added handleMouseEnter to reset the cursor type.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes.  Also 5591, 
 *                                      fix of intermittent inability to select/deselect hazards
 * Jan  7, 2015 4959       Dan Schaffer Ability to right click to add/remove polygons from hazards
 * Feb  3, 2015 6096       Dan Schaffer Fixed spatial display panning.
 * 
 * Feb 09, 2015 6260       Dan Schaffer Fixed bugs in multi-polygon handling
 * Feb 17, 2015 4209       Dan Schaffer Fixed bug in lassoing hazards
 * Feb 24, 2015 6499       Dan Schaffer Disable moving/drawing of point hazards
 * Mar 13, 2015 6090       Dan Schaffer Relaxed geometry validity check.
 * Apr 02, 2015 6542       Robert.Blum  Checking for a null cursor location when dragging
 *                                      a point of a polygon.
 * Apr 03, 2015 5591       Dan Schaffer Fixed bug in spatial display handling storm track
 *                                      If the user moused over the circle in the middle
 *                                      then the storm polygon became un-editable.
 * Apr 10, 2015 6898       Chris.Cody   Refactored async messaging
 * May 20, 2015 6730       mduff        Fix to not zoom on middle mouse button click to remove vertex.
 * Jun 02, 2015 8500       Chris.Cody   Single click does not select HE on Spatial Display
 * Jul 06, 2015 6930       Chris.Cody   Change containing object for SYMBOL_NEW_LAT_LON
 * Jul 17, 2015 8890       Chris.Cody   Vertices appearing incorrectly on display
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class SelectionAction extends NonDrawingAction {

    /**
     * Defines the mouse selection sensitivity in pixels.
     */
    public static final double SELECTION_DISTANCE_PIXELS = 15.0;

    private final SpatialPresenter spatialPresenter;

    private final ISessionEventManager<ObservedHazardEvent> eventManager;

    /**
     * Defines the type of move operation. SINGLE_POINT -- the user is moving a
     * vertex ALL_POINTS -- the user is moving the entire polygon.
     */
    private enum MoveType {
        SINGLE_POINT, ALL_POINTS;
    }

    public SelectionAction(SpatialPresenter spatialPresenter) {
        this.spatialPresenter = spatialPresenter;
        this.eventManager = spatialPresenter.getSessionManager()
                .getEventManager();

    }

    @Override
    protected IInputHandler createMouseHandler() {
        return new SelectionHandler();
    }

    /**
     * Retrieves the mouse handler associated with the Selection Drawing Action.
     */
    @Override
    public IInputHandler getMouseHandler() {
        IInputHandler handler = super.getMouseHandler();

        /*
         * Ensure key state variables are set to false.
         */
        ((SelectionHandler) handler).shiftKeyIsDown = false;
        ((SelectionHandler) handler).ctrlKeyIsDown = false;

        return handler;
    }

    /**
     * 
     * Description: Mouse handler associated with the Selection Drawing Action.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * Date         Ticket#    Engineer             Description
     * ------------ ---------- -----------          --------------------------
     * November 2011           bryon.lawrence       Initial creation
     * Sep 10, 2014 3793       Robert.Blum          Modified handleMouseUp to return false for
     *                                              when the middle mouse button is pressed.
     * 
     * </pre>
     * 
     * @author bryon.lawrence
     * @version 1.0
     */
    public class SelectionHandler extends NonDrawingAction.NonDrawingHandler {

        private final GeometryFactory geometryFactory = new GeometryFactory();

        /**
         * Flag which keeps track of the pressed state of the shift key.
         */
        private boolean shiftKeyIsDown = false;

        /**
         * Flag which keeps track of the pressed state of the ctrl key.
         */
        private boolean ctrlKeyIsDown = false;

        /**
         * For move operations
         */
        private MoveType moveType = null;

        /**
         * The index of the polygon point being moved.
         */
        private int movePointIndex = -1;

        /**
         * Flag indicating if this a move of a polygon vertex.
         */
        private boolean isVertexMove = false;

        /**
         * Flag indicating whether or not to allow panning.
         */
        private boolean allowPanning = false;

        /**
         * Listens for the press of the SHIFT or CTRL keys. If the mouse pointer
         * is over a hazard, this signifies multiple selection via left mouse
         * click. If the mouse pointer is not over a hazard, then either a
         * selection box (SHIFT key) or selection lasso (CTRL key) can be drawn
         * to select multiple hazards.
         */
        @Override
        public boolean handleKeyDown(final int key) {

            if (key == SWT.CTRL) {
                ctrlKeyIsDown = true;
            } else if (key == SWT.SHIFT) {
                shiftKeyIsDown = true;
            }

            return true;

        }

        /**
         * Listens for the release of the Shift or Ctrl key. This signals an
         * exit from multiple selection mode.
         * 
         * @param key
         *            the key number.
         * @return Whether or not the event was handled.
         */
        @Override
        public boolean handleKeyUp(int key) {
            if (key == SWT.CTRL) {
                ctrlKeyIsDown = false;
            } else if (key == SWT.SHIFT) {
                shiftKeyIsDown = false;
            }
            return true;
        }

        /**
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int,
         *      int, int)
         */
        @Override
        public boolean handleMouseDown(int anX, int aY, int button) {
            if (button == 1) {
                Coordinate loc = toCoordinate(anX, aY);

                if (loc != null) {
                    findSelectedDE(loc, anX, aY);
                }
            }

            return false;
        }

        @Override
        public boolean handleMouseUp(int x, int y, int button) {

            boolean result = true;
            if (button == 3) {
                Coordinate loc = toCoordinate(x, y);
                spatialPresenter.zoneSelected(loc);
            }

            /*
             * Button 2 functionality for adding/deleting vertices... This
             * mimics WarnGen.
             */
            else if (button == 2) {
                if (getSpatialDisplay().getSelectedHazardLayer() != null) {
                    handleVertexAdditionOrDeletion();
                }
                result = true;
            } else if (isVertexMove) {
                handleVertexMove();

            } else if (ghostEl != null) {
                DrawableElement selectedDE = getSpatialDisplay()
                        .getSelectedDE();
                if (selectedDE != null) {
                    IHazardServicesShape origShape = (IHazardServicesShape) selectedDE;
                    Class<?> selectedDEclass = selectedDE.getClass();
                    if (selectedDEclass.equals(HazardServicesSymbol.class)) {
                        handleStormTrackModification(selectedDE);

                    } else {
                        handleShapeMove(origShape, selectedDEclass);
                    }

                }
            } else if (!allowPanning) {
                /*
                 * Treat this has a hazard selection.
                 */
                boolean multipleSelection = shiftKeyIsDown || ctrlKeyIsDown;
                getSpatialDisplay().elementClicked(
                        getSpatialDisplay().getSelectedDE(), multipleSelection);

            } else {
                result = false;
            }
            finalizeMouseHandling();

            return result;

        }

        private Coordinate toCoordinate(int x, int y) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(x, y);
            return loc;
        }

        /**
         * Determines if the mouse pointer is within a polygon. If it is, then
         * this becomes the containing DE.
         * 
         * @param loc
         *            The coordinate representing the location of the mouse
         *            pointer.
         * @param x
         *            X coordinate in pixel space.
         * @param y
         *            Y coordinate in pixel space.
         * @return Whether or not a containing component was found.
         */
        private boolean findSelectedDE(Coordinate loc, int x, int y) {
            boolean selectedDEFound = true;

            AbstractDrawableComponent nadc = null;

            /*
             * Retrieve a list of drawables which contain this mouse click point
             */
            List<AbstractDrawableComponent> containingComponentsList = getSpatialDisplay()
                    .getContainingComponents(loc, x, y);

            /*
             * Retrieve the currently selected hazard shape
             */
            AbstractDrawableComponent selectedElement = getSpatialDisplay()
                    .getSelectedHazardLayer();

            if (selectedElement != null) {
                String selectedElementEventID = getSpatialDisplay()
                        .eventIDForElement(selectedElement);

                /*
                 * If there is more than one containing component, make sure
                 * that the topmost element is equal to the selected element. If
                 * there are symbols (including points), give them precedence.
                 */
                for (AbstractDrawableComponent comp : containingComponentsList) {
                    if (comp instanceof HazardServicesSymbol) {
                        String containingComponentEventID = getSpatialDisplay()
                                .eventIDForElement(comp);

                        if (containingComponentEventID
                                .equals(selectedElementEventID)) {
                            nadc = comp;
                            break;
                        }
                    }

                }

                if (nadc == null && containingComponentsList.size() > 0) {
                    AbstractDrawableComponent comp = containingComponentsList
                            .get(0);
                    String containingComponentEventID = getSpatialDisplay()
                            .eventIDForElement(comp);

                    if (containingComponentEventID
                            .equals(selectedElementEventID)) {
                        nadc = comp;
                    }
                }
            }

            if (nadc == null) {
                /*
                 * We need a better way of determining which containing
                 * component of multiple components to chose as the selected.
                 */
                if (containingComponentsList.size() > 0) {
                    nadc = containingComponentsList.get(0);
                }
            }

            if (nadc != null) {
                allowPanning = false;
            } else {
                // Pass this event on.
                allowPanning = true;
                selectedDEFound = false;
            }
            getSpatialDisplay().setSelectedDE(nadc);

            return selectedDEFound;
        }

        private void handleShapeMove(IHazardServicesShape origShape,
                Class<?> selectedDEclass) {
            if ((selectedDEclass.equals(HazardServicesPolygon.class))
                    || (selectedDEclass.equals(HazardServicesLine.class))) {
                List<Coordinate> coords = ((Line) ghostEl).getPoints();

                Geometry modifiedGeometry = buildModifiedGeometry(origShape,
                        coords);
                eventManager.setModifiedEventGeometry(origShape.getID(),
                        modifiedGeometry, true);

            }
        }

        private Geometry buildModifiedGeometry(IHazardServicesShape origShape,
                List<Coordinate> coords) {
            Geometry origShapeGeometry = geometryFromShape(origShape);
            Geometry newShapeGeometry = geometryFromCoordinates(origShape,
                    coords);
            List<Geometry> eventGeometries = getGeometriesForEvent(origShape
                    .getID());
            Geometry modifiedGeometry = mergeInNewGeometry(origShapeGeometry,
                    newShapeGeometry, eventGeometries);

            return modifiedGeometry;
        }

        private Geometry geometryFromShape(IHazardServicesShape shape) {
            /**
             * In the case of a point, the geometry stored in the shape is the
             * surrounding polygon. We just want the point itself included in
             * the {@link SessionEventGeometryModified}
             */
            if (shape.getClass() == HazardServicesPoint.class) {
                return geometryFromPointLocation((HazardServicesPoint) shape);
            } else {
                return shape.getGeometry();
            }
        }

        public Geometry geometryFromCoordinates(IHazardServicesShape origShape,
                List<Coordinate> coordinates) {
            Coordinate[] coordinatesAsArray = Utils.listAsArray(coordinates);
            Geometry result;

            if (origShape.getClass() == HazardServicesLine.class) {
                result = geometryFactory.createLineString(coordinatesAsArray);
            } else if (origShape.getClass() == HazardServicesPoint.class) {
                result = geometryFactory.createPoint(coordinatesAsArray[0]);
            } else if (origShape.getClass() == HazardServicesPolygon.class) {
                result = geometryFactory.createPolygon(
                        geometryFactory.createLinearRing(coordinatesAsArray),
                        null);
            } else {
                throw new IllegalArgumentException("Unexpected geometry "
                        + origShape.getClass());
            }
            return result;
        }

        private Geometry mergeInNewGeometry(Geometry origShapeGeometry,
                Geometry newShapeGeometry, List<Geometry> eventGeometries) {
            List<Geometry> modifiedGeometries = Lists.newArrayList();
            for (Geometry eventGeometry : eventGeometries) {
                if (eventGeometry.equals(origShapeGeometry)) {
                    modifiedGeometries.add(newShapeGeometry);
                } else {
                    modifiedGeometries.add(eventGeometry);
                }
            }

            return geometryFactory.buildGeometry(modifiedGeometries);
        }

        private List<Geometry> getGeometriesForEvent(String eventID) {
            List<Geometry> result = Lists.newArrayList();

            List<AbstractDrawableComponent> hazards = getSpatialDisplay()
                    .getDataManager().getActiveLayer().getDrawables();

            for (AbstractDrawableComponent hazard : hazards) {
                Class<?> hazardClass = hazard.getClass();
                String hazardID = ((IHazardServicesShape) hazard).getID();

                if (hazardID.equals(eventID)) {
                    if (hazardClass.equals(HazardServicesPolygon.class)) {
                        Geometry geometry = ((IHazardServicesShape) hazard)
                                .getGeometry();
                        result.add(geometry);

                    } else if (hazardClass.equals(HazardServicesPoint.class)
                            && ((HazardServicesPoint) hazard).isOuter() == false) {
                        HazardServicesPoint point = (HazardServicesPoint) hazard;
                        Geometry geometry = geometryFromPointLocation(point);
                        result.add(geometry);

                    }
                }
            }
            return result;

        }

        private Geometry geometryFromPointLocation(HazardServicesPoint point) {
            Geometry geometry = geometryFactory
                    .createPoint(point.getLocation());
            return geometry;
        }

        private void handleStormTrackModification(DrawableElement selectedDE) {
            Symbol movedSymbol = (Symbol) ghostEl;

            HazardServicesSymbol origSymbol = (HazardServicesSymbol) selectedDE;
            String eventID = origSymbol.getID();
            Coordinate newCoord = movedSymbol.getLocation();

            Map<String, Serializable> modifiedAreaObject = Maps.newHashMap();

            // Look for a pointID. If it is there, then
            // include it in the tools parameters. If it is
            // not there, then don't include it in the parameters.
            long pointID = origSymbol.getPointID();

            modifiedAreaObject.put(HazardConstants.POINTID, pointID);

            modifiedAreaObject.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                    eventID);
            modifiedAreaObject.put(HazardConstants.HAZARD_EVENT_SHAPE_TYPE,
                    HazardConstants.HAZARD_EVENT_SHAPE_TYPE_DOT);
            String newLatLonAsString = String.format("[%f,%f]", newCoord.x,
                    newCoord.y);
            modifiedAreaObject.put(HazardConstants.SYMBOL_NEW_LAT_LON,
                    newLatLonAsString);
            getSpatialDisplay().notifyModifiedStormTrack(modifiedAreaObject);
        }

        private void handleVertexMove() {
            getSpatialDisplay().setSelectedDE(null);

            isVertexMove = false;

            AbstractDrawableComponent selectedElement = getSpatialDisplay()
                    .getSelectedHazardLayer();

            IHazardServicesShape eventShape = (IHazardServicesShape) selectedElement;
            Geometry modifiedGeometry = buildModifiedGeometry(eventShape,
                    selectedElement.getPoints());
            eventManager.setModifiedEventGeometry(eventShape.getID(),
                    modifiedGeometry, true);

        }

        private void handleVertexAdditionOrDeletion() {
            if (moveType == MoveType.SINGLE_POINT) {
                deleteVertex();
            } else {
                addVertex();
            }
        }

        private void finalizeMouseHandling() {
            getSpatialDisplay().removeGhostLine();

            getSpatialDisplay().setSelectedDE(null);
            ghostEl = null;

            getSpatialDisplay().issueRefresh();

            movePointIndex = -1;
            moveType = null;
        }

        @Override
        public boolean handleMouseDownMove(int anX, int aY, int button) {
            /*
             * Retrieve a list of drawables if any which contain the mouse click
             * point
             */
            final AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());

            Coordinate loc = editor.translateClick(anX, aY);

            /*
             * Check for a null coordinate. If it is null use the previous
             * coordinate. If not save it off as the prevoius.
             */
            if (loc == null) {
                if (prevLoc != null) {
                    loc = prevLoc;
                } else {
                    return false;
                }
            } else {
                prevLoc = loc;
            }
            /*
             * Check to see if the user is moving a point in a hazard polygon...
             */
            if ((moveType != null) && (moveType == MoveType.SINGLE_POINT)
                    && (button == 1)) {
                AbstractDrawableComponent selectedElement = getSpatialDisplay()
                        .getSelectedHazardLayer();

                if ((selectedElement != null) && (movePointIndex >= 0)) {
                    isVertexMove = true;

                    /*
                     * Replace the previous coordinate with the new one. If this
                     * is a polygon, the last point must be the same as the
                     * first, so ensure that this is the case if the first point
                     * is the one being moved. (The last point is never the one
                     * being moved for poly- gons.)
                     */
                    List<Coordinate> coords = selectedElement.getPoints();
                    coords.set(movePointIndex, loc);
                    if (selectedElement.getClass().equals(
                            HazardServicesPolygon.class)
                            && (movePointIndex == 0)) {
                        coords.set(coords.size() - 1, loc);
                    }

                    /*
                     * The shape's coords are updated...
                     */
                    ghostEl = selectedElement.copy();
                    getSpatialDisplay().setGhostLine(ghostEl);
                    getSpatialDisplay().issueRefresh();
                }

                return true;
            }

            /*
             * Handle multiselection action.
             */
            if (shiftKeyIsDown) {
                getSpatialPresenter().getView().setMouseHandler(
                        HazardServicesMouseHandlers.RECTANGLE_MULTI_SELECTION,
                        new String[] {});
                return true;

            } else if (ctrlKeyIsDown) {
                getSpatialPresenter().getView().setMouseHandler(
                        HazardServicesMouseHandlers.FREE_HAND_MULTI_SELECTION,
                        new String[] {});
                return true;

            }

            /*
             * Are we trying to move something that is not currently selected?
             * Then allow panning.
             */
            AbstractDrawableComponent selectedComponent = getSpatialDisplay()
                    .getSelectedDE();

            if ((selectedComponent != null
                    && (selectedComponent != getSpatialDisplay()
                            .getSelectedHazardLayer()) && !(selectedComponent instanceof HazardServicesSymbol))) {
                allowPanning = true;
                // Shapes have vertices
                this.isVertexMove = true;
            }

            if (!allowPanning || moveType == MoveType.ALL_POINTS) {
                return super.handleMouseDownMove(anX, aY, button);
            } else {
                return false;
            }
        }

        /**
         * This checks the mouse pointer to see if it is over the selected
         * drawable element. If it is, then the mouse cursor sets itself to a
         * move icon. If the mouse is over a vertex in the polygon, then the
         * cursor turns into a hand.
         */
        @Override
        public boolean handleMouseMove(int x, int y) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());

            editor.getActiveDisplayPane().setFocus();

            if (moveType == MoveType.ALL_POINTS) {
                handleMouseDownMove(x, y, 1);
            } else {

                // Need to check here if the user selected a move mode from the
                // right-click context menu. If so, then call
                // handleMouseDownMove().
                // Treat this as if the user is holding down the mouse button

                // AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                // .getInstance().getActiveEditor());
                Coordinate loc = editor.translateClick(x, y);
                moveType = null;

                if (loc != null) {
                    AbstractDrawableComponent selectedElement = getSpatialDisplay()
                            .getSelectedHazardLayer();

                    if (selectedElement != null) {

                        boolean isEditable = SelectionAction.this
                                .isComponentEditable(selectedElement);

                        if (isEditable) {
                            String selectedElementEventID = getSpatialDisplay()
                                    .eventIDForElement(selectedElement);

                            AbstractDrawableComponent nadc = null;
                            // First try to find a component that completely
                            // contains
                            // the click point. There could be several of these.
                            List<AbstractDrawableComponent> containingComponentList = getSpatialDisplay()
                                    .getContainingComponents(loc, x, y);

                            for (AbstractDrawableComponent comp : containingComponentList) {
                                String containingComponentEventID = getSpatialDisplay()
                                        .eventIDForElement(comp);

                                if (containingComponentEventID
                                        .equals(selectedElementEventID)) {
                                    /*
                                     * Since there may be multiple geometries
                                     * associated with an event ID, make sure
                                     * that the selectedElement in the tool
                                     * layer reflects the correct geometry.
                                     */
                                    if (!comp.equals(selectedElement)
                                            && (comp instanceof HazardServicesPolygon)) {
                                        getSpatialDisplay()
                                                .setSelectedHazardLayer(comp);
                                    }
                                    /*
                                     * Need a bit more logic here. Just because
                                     * two components have the same event id
                                     * doesn't make them equal.
                                     */
                                    if (comp.equals(selectedElement)) {
                                        nadc = comp;
                                        break;
                                    }
                                }
                            }

                            if (nadc == null) {
                                // There is no containing element.
                                // Try to find the closest element...
                                AbstractDrawableComponent comp = getSpatialDisplay()
                                        .getNearestComponent(loc);

                                if (comp != null) {
                                    String containingComponentEventID = getSpatialDisplay()
                                            .eventIDForElement(comp);

                                    if (containingComponentEventID
                                            .equals(selectedElementEventID)) {
                                        nadc = comp;
                                    }
                                }
                            }

                            if (nadc != null) {
                                // Set the mouse cursor to a move symbol
                                getSpatialPresenter()
                                        .getView()
                                        .setCursor(
                                                SpatialViewCursorTypes.MOVE_SHAPE_CURSOR);

                                IHazardServicesShape shape = (IHazardServicesShape) nadc;

                                if (shape.isEditable()) {
                                    // Set the flag indicating that the handle
                                    // bars on the selected polygon may be
                                    // displayed.
                                    getSpatialDisplay()
                                            .setDrawSelectedHandleBars(true);

                                    // Test to determine if the mouse is close
                                    // to
                                    // the border of the geometry.
                                    Coordinate mouseScreenCoord = new Coordinate(
                                            x, y);

                                    Point clickPointScreen = geometryFactory
                                            .createPoint(mouseScreenCoord);

                                    // Create a line string with screen
                                    // coordinates.
                                    Coordinate[] shapeCoords = shape
                                            .getGeometry().getCoordinates();

                                    Coordinate[] shapeScreenCoords = new Coordinate[shapeCoords.length];

                                    for (int i = 0; i < shapeCoords.length; ++i) {
                                        double[] coords = editor
                                                .translateInverseClick(shapeCoords[i]);
                                        shapeScreenCoords[i] = new Coordinate(
                                                coords[0], coords[1]);
                                    }

                                    LineString ls2 = geometryFactory
                                            .createLineString(shapeScreenCoords);

                                    double dist = clickPointScreen
                                            .distance(ls2);

                                    if (dist <= SELECTION_DISTANCE_PIXELS) {
                                        getSpatialPresenter()
                                                .getView()
                                                .setCursor(
                                                        SpatialViewCursorTypes.DRAW_CURSOR);
                                    }

                                    // Test to determine if the mouse is close
                                    // to
                                    // one of the hazard's vertices...
                                    List<Coordinate> coordList = selectedElement
                                            .getPoints();
                                    Coordinate coords[] = coordList
                                            .toArray(new Coordinate[coordList
                                                    .size()]);

                                    double minDistance = Double.MAX_VALUE;

                                    // Convert to screen coords (pixels)
                                    for (int i = 0; i < coords.length
                                            && coords[i] != null; ++i) {
                                        double[] screen = editor
                                                .translateInverseClick(coords[i]);
                                        Coordinate vertexScreenCoord = new Coordinate(
                                                screen[0], screen[1]);

                                        dist = mouseScreenCoord
                                                .distance(vertexScreenCoord);

                                        if (dist <= SELECTION_DISTANCE_PIXELS) {
                                            if (dist < minDistance) {
                                                getSpatialPresenter()
                                                        .getView()
                                                        .setCursor(
                                                                SpatialViewCursorTypes.MOVE_VERTEX_CURSOR);
                                                moveType = MoveType.SINGLE_POINT;
                                                movePointIndex = i;
                                                minDistance = dist;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                getSpatialDisplay().setDrawSelectedHandleBars(
                                        false);

                                getSpatialPresenter().getView().setCursor(
                                        SpatialViewCursorTypes.ARROW_CURSOR);
                            }
                        }

                    }
                }
            }

            return false;
        }

        @Override
        public boolean handleMouseEnter(Event event) {
            ctrlKeyIsDown = (event.stateMask & SWT.CTRL) == SWT.CTRL;
            shiftKeyIsDown = (event.stateMask & SWT.SHIFT) == SWT.SHIFT;
            return handleMouseMove(event.x, event.y);
        }

        /**
         * Add a new vertex to a selected geometry.
         */
        public void addVertex() {
            AbstractEditor editor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            AbstractDrawableComponent selectedElement = getSpatialDisplay()
                    .getSelectedHazardLayer();

            if (selectedElement != null) {
                int lastMouseX = editor.getActiveDisplayPane().getLastMouseX();
                int lastMouseY = editor.getActiveDisplayPane().getLastMouseY();

                Coordinate clickScreenCoord = new Coordinate(lastMouseX,
                        lastMouseY);

                /*
                 * Try using the geometry associated with the drawable.
                 */
                IHazardServicesShape shape = ((IHazardServicesShape) selectedElement);
                Coordinate[] coords = shape.getGeometry().getCoordinates();

                int numCoordPoints = coords.length;

                /*
                 * The last coordinate may sometimes be equal to the first
                 * coordinate. In this case, do not consider the last
                 * coordinate.
                 */
                if (coords[0].equals(coords[numCoordPoints - 1])) {
                    --numCoordPoints;
                }

                double minDistance = Double.MAX_VALUE;
                int minPosition = Integer.MIN_VALUE;

                Coordinate coLinearCoord = null;

                for (int i = 1; i <= numCoordPoints; i++) {
                    double screenCoords[] = editor
                            .translateInverseClick(coords[i - 1]);
                    Coordinate screenCoordA = new Coordinate(screenCoords[0],
                            screenCoords[1]);

                    if (i < numCoordPoints) {
                        screenCoords = editor.translateInverseClick(coords[i]);
                    } else {
                        screenCoords = editor.translateInverseClick(coords[0]);
                    }

                    Coordinate screenCoordB = new Coordinate(screenCoords[0],
                            screenCoords[1]);
                    LineSegment lineSegment = new LineSegment(screenCoordA,
                            screenCoordB);

                    double dist = lineSegment.distance(clickScreenCoord);

                    if (dist <= SELECTION_DISTANCE_PIXELS) {
                        if (dist < minDistance) {
                            minDistance = dist;
                            minPosition = i;

                            coLinearCoord = lineSegment
                                    .closestPoint(clickScreenCoord);
                        }
                    }
                }

                if (coLinearCoord != null) {
                    Coordinate[] coords2 = new Coordinate[numCoordPoints + 1];

                    int k = 0;

                    for (k = 0; k < minPosition; k++) {
                        coords2[k] = coords[k];
                    }

                    // The vertex being added.
                    coords2[k] = editor.translateClick(coLinearCoord.x,
                            coLinearCoord.y);

                    for (k += 1; k < coords2.length; k++) {
                        coords2[k] = coords[k - 1];
                    }

                    IHazardServicesShape eventShape = (IHazardServicesShape) selectedElement;
                    List<Coordinate> coordsAsList = Lists.newArrayList(coords2);
                    if (eventShape.getGeometry().getClass() == Polygon.class) {
                        Utilities.closeCoordinatesIfNecessary(coordsAsList);
                    }
                    Geometry modifiedGeometry = buildModifiedGeometry(
                            eventShape, coordsAsList);
                    eventManager.setModifiedEventGeometry(eventShape.getID(),
                            modifiedGeometry, true);

                    movePointIndex = -1;
                    moveType = null;
                }
            }
        }

        /**
         * Delete a vertex from a selected geometry.
         */
        public void deleteVertex() {
            AbstractDrawableComponent selectedElement = getSpatialDisplay()
                    .getSelectedHazardLayer();

            if ((selectedElement != null) && (moveType != null)
                    && (moveType == MoveType.SINGLE_POINT)
                    && (movePointIndex >= 0)
                    && (((IHazardServicesShape) selectedElement).isEditable())) {
                List<Coordinate> c2 = selectedElement.getPoints();
                List<Coordinate> coords = new ArrayList<>(c2);

                // For now, make sure there are at least three points left for
                // paths, or four points for polygons (since the latter need
                // to have the last point be identical to the first point).
                boolean isPolygon = selectedElement.getClass().equals(
                        HazardServicesPolygon.class);
                if (coords.size() > (isPolygon ? 4 : 3)) {
                    coords.remove(movePointIndex);

                    if (isPolygon) {
                        Utilities.closeCoordinatesIfNecessary(coords);
                    }

                    IHazardServicesShape eventShape = (IHazardServicesShape) selectedElement;
                    Geometry modifiedGeometry = buildModifiedGeometry(
                            eventShape, coords);
                    eventManager.setModifiedEventGeometry(eventShape.getID(),
                            modifiedGeometry, true);

                    movePointIndex = -1;
                    moveType = null;
                }
            }
        }

        /**
         * Moves the entire hazard element.
         * 
         * @param
         * @return
         */
        public void setMoveEntireElement() {
            moveType = MoveType.ALL_POINTS;
            AbstractDrawableComponent selectedElement = getSpatialDisplay()
                    .getSelectedHazardLayer();
            getSpatialDisplay().setSelectedDE(selectedElement);
        }
    }
}
