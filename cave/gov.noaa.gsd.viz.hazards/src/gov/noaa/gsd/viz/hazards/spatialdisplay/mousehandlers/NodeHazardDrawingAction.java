/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.LineDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PointDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PolygonDrawingAttributes;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.tools.InputHandlerDefaultImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Description: Drawing action for creating hazard events boxes. Each left click
 * by the forecaster becomes a vertex in the polygon. The forecaster closes the
 * polygon via a single right mouse click.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 2011                Bryon.Lawrence      Initial creation
 * Jul 15, 2013      585   Chris.Golden        Changed to no longer be a singleton.
 * Jul 18, 2013     1264   Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov  23, 2013    1462   Bryon.Lawrence      Changed polygons to be drawn without
 *                                             fill by default.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class NodeHazardDrawingAction extends AbstractMouseHandler {

    /** for logging */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NodeHazardDrawingAction.class);

    /**
     * Shape type to be drawn.
     */
    private String shapeType = null;

    /**
     * Map of shape types to drawing attributes.
     */
    private final Map<String, HazardServicesDrawingAttributes> drawingAttributesForShapeTypes = Maps
            .newHashMap();

    private final HazardEventBuilder hazardEventBuilder;

    private final ISessionManager<ObservedHazardEvent> sessionManager;

    public NodeHazardDrawingAction(
            ISessionManager<ObservedHazardEvent> sessionManager) {
        this.sessionManager = sessionManager;
        hazardEventBuilder = new HazardEventBuilder(sessionManager);
    }

    @Override
    protected IInputHandler createMouseHandler() {
        return new NodeHazardDrawingHandler();
    }

    /**
     * Set the shape type.
     * 
     * @param shapeType
     *            Shape type.
     */
    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
        if (drawingAttributesForShapeTypes.get(shapeType) == null) {
            try {
                HazardServicesDrawingAttributes drawingAttributes = (shapeType
                        .equals(GeometryType.POLYGON.getValue()) ? new PolygonDrawingAttributes(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                .getShell(), false, sessionManager)
                        : (shapeType.equals(GeometryType.LINE.getValue()) ? new LineDrawingAttributes(
                                PlatformUI.getWorkbench()
                                        .getActiveWorkbenchWindow().getShell(),
                                sessionManager) : new PointDrawingAttributes(
                                PlatformUI.getWorkbench()
                                        .getActiveWorkbenchWindow().getShell(),
                                sessionManager)));
                drawingAttributesForShapeTypes
                        .put(shapeType, drawingAttributes);
            } catch (VizException e) {
                statusHandler.error("Could not create drawing attributes.", e);
            }
        }
    }

    /**
     * 
     * Description: Mouse handler for drawing hazard events consisting of one or
     * more distinct nodes.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * March 13, 2013            Bryon.Lawrence      Initial creation
     * 
     * </pre>
     * 
     * @author Bryon.Lawrence
     * @version 1.0
     */
    public class NodeHazardDrawingHandler extends InputHandlerDefaultImpl {

        /*
         * Points of the new watch box.
         */
        private final List<Coordinate> points = Lists.newArrayList();

        /*
         * An instance of DrawableElementFactory, which is used to create a new
         * watch box. The factory will probably need to have a tornado warning
         * box added to it.
         */
        private final DrawableElementFactory def = new DrawableElementFactory();

        @Override
        public boolean handleMouseMove(int x, int y) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(x, y);

            if (loc == null) {
                return false;
            }

            if (points != null && points.size() >= 1) {

                // create the ghost element and put it in the drawing layer
                AbstractDrawableComponent ghost = def.create(DrawableType.LINE,
                        (IAttribute) drawingAttributesForShapeTypes
                                .get(shapeType), "Line", "LINE_SOLID",
                        (ArrayList<Coordinate>) points, getDrawingLayer()
                                .getActiveLayer());

                List<Coordinate> ghostPts = Lists.newArrayList(points);
                ghostPts.add(loc);
                // ((Line) ghost)
                // .setLinePoints(Lists.newArrayList(ghostPts));
                ((Line) ghost).setLinePoints(ghostPts);

                getDrawingLayer().setGhostLine(ghost);
                getDrawingLayer().issueRefresh();
            }

            return false;
        }

        // Needed to override this to prevent
        // it from being passed on to CAVE panning routines.
        @Override
        public boolean handleMouseUp(int x, int y, int mouseButton) {
            AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                    .getInstance().getActiveEditor());
            Coordinate loc = editor.translateClick(x, y);

            if (loc == null) {
                return false;
            }

            if (mouseButton == 1) {
                if (shapeType.equals(GeometryType.POINT.getValue())) {
                    createPointShape(loc);
                } else {
                    addPointIfNotIdenticalToPreviousPoint(loc);
                }

            } else if (mouseButton == 3) {

                if (shapeType.equals(GeometryType.POINT.getValue())) {
                    createPointShape(loc);
                } else {

                    // This is where the ghost object is replaced
                    // by the new shape. For the moment just delete
                    // the ghost line.
                    addPointIfNotIdenticalToPreviousPoint(loc);
                    if (points.size() != 0) {
                        getDrawingLayer().removeGhostLine();
                        if (points.size() < (shapeType
                                .equals(GeometryType.POLYGON.getValue()) ? 3
                                : 2)) {
                            points.clear();
                            getDrawingLayer().issueRefresh();
                        } else {
                            createShapeFromCollectedPoints();
                        }
                    }
                }

                // Indicate that this drawing action is done.
                getSpatialPresenter().getView().drawingActionComplete();

                return true;

            }

            return true;

        }

        /**
         * Needed to override this to prevent odd panning behavior when drawing
         * to CAVE
         * 
         * @param
         * @return
         */
        @Override
        public boolean handleMouseDownMove(int x, int y, int mouseButton) {
            return true;
        }

        /**
         * Add the specified point to the points list if it is not the same as
         * the previous point (if the list is not empty).
         * 
         * @param loc
         *            New point to be added.
         */
        private void addPointIfNotIdenticalToPreviousPoint(Coordinate point) {
            if (points.isEmpty()
                    || (points.get(points.size() - 1).equals(point) == false)) {
                points.add(point);
            }
        }

        /**
         * Create a shape of the current type with the points that have been
         * collected, and send it off to the presenter.
         */
        private void createShapeFromCollectedPoints() {

            // If this is a polygon, it must have a point at the end of its
            // list of points that is the same as its first point.
            try {
                IHazardEvent hazardEvent;
                if (shapeType.equals(GeometryType.POLYGON.getValue())) {
                    Utilities.closeCoordinatesIfNecessary(points);
                    hazardEvent = hazardEventBuilder
                            .buildPolygonHazardEvent(pointsAsArray());
                } else {

                    hazardEvent = hazardEventBuilder
                            .buildLineHazardEvent(pointsAsArray());

                }

                SessionEventAdded action = new SessionEventAdded(
                        sessionManager.getEventManager(), hazardEvent,
                        getSpatialPresenter());
                getSpatialPresenter().fireAction(action);
            } catch (InvalidGeometryException e) {
                statusHandler.handle(Priority.WARN,
                        "Error drawing noded polygon: " + e.getMessage());
            }
            points.clear();

        }

        private Coordinate[] pointsAsArray() {
            return points.toArray(new Coordinate[points.size()]);
        }

        /**
         * Create a point shape using the specified location.
         * 
         * @param loc
         *            Location at which to place the point shape.
         */
        private void createPointShape(Coordinate loc) {
            IHazardEvent hazardEvent = hazardEventBuilder
                    .buildPointHazardEvent(loc);
            SessionEventAdded action = new SessionEventAdded(
                    sessionManager.getEventManager(), hazardEvent,
                    getSpatialPresenter());
            getSpatialPresenter().fireAction(action);
        }
    }
}
