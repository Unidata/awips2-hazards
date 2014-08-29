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
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.viz.ui.EditorUtil;
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
public class VertexHazardDrawingAction extends AbstractMouseHandler {

    /** for logging */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(VertexHazardDrawingAction.class);

    /**
     * Shape type to be drawn.
     */
    private GeometryType shapeType = null;

    /**
     * Map of shape types to drawing attributes.
     */
    private final Map<GeometryType, HazardServicesDrawingAttributes> drawingAttributesForShapeTypes = Maps
            .newHashMap();

    private final HazardEventBuilder hazardEventBuilder;

    private final ISessionManager<ObservedHazardEvent> sessionManager;

    public VertexHazardDrawingAction(
            ISessionManager<ObservedHazardEvent> sessionManager) {
        this.sessionManager = sessionManager;
        hazardEventBuilder = new HazardEventBuilder(sessionManager);
    }

    @Override
    protected IInputHandler createMouseHandler() {
        return new VertexHazardDrawingHandler();
    }

    /**
     * Set the shape type.
     * 
     * @param shapeType
     *            Shape type.
     */
    public void setShapeType(String shapeType) {
        this.shapeType = GeometryType.valueOf(shapeType.toUpperCase());
        if (drawingAttributesForShapeTypes.get(shapeType) == null) {
            try {
                HazardServicesDrawingAttributes drawingAttributes = null;
                if (this.shapeType == GeometryType.LINE) {
                    drawingAttributes = new LineDrawingAttributes(
                            sessionManager);
                } else if (this.shapeType == GeometryType.POLYGON) {
                    drawingAttributes = new PolygonDrawingAttributes(false,
                            sessionManager);
                } else {
                    drawingAttributes = new PointDrawingAttributes(
                            sessionManager);
                }
                drawingAttributesForShapeTypes.put(this.shapeType,
                        drawingAttributes);
            } catch (VizException e) {
                statusHandler.error("Could not create drawing attributes.", e);
            }
        }
    }

    /**
     * 
     * Description: Mouse handler for drawing hazard events consisting of one or
     * more distinct vertices.
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
    public class VertexHazardDrawingHandler extends InputHandlerDefaultImpl {

        /*
         * Points of the new watch box.
         */
        private final List<Coordinate> points = new ArrayList<>();

        /*
         * An instance of DrawableElementFactory, which is used to create a new
         * watch box. The factory will probably need to have a tornado warning
         * box added to it.
         */
        private final DrawableElementFactory def = new DrawableElementFactory();

        @Override
        public boolean handleMouseMove(int x, int y) {
            AbstractEditor editor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            Coordinate loc = editor.translateClick(x, y);

            if (loc == null) {
                return false;
            }

            if (points != null && points.size() >= 1) {

                // create the ghost element and put it in the drawing layer
                AbstractDrawableComponent ghost = def.create(DrawableType.LINE,
                        (IAttribute) drawingAttributesForShapeTypes
                                .get(shapeType), "Line", "LINE_SOLID",
                        (ArrayList<Coordinate>) points, getToolLayer()
                                .getActiveLayer());

                List<Coordinate> ghostPts = Lists.newArrayList(points);
                ghostPts.add(loc);

                ((Line) ghost).setLinePoints(ghostPts);

                getToolLayer().setGhostLine(ghost);
                getToolLayer().issueRefresh();
            }

            return false;
        }

        // Needed to override this to prevent
        // it from being passed on to CAVE panning routines.
        @Override
        public boolean handleMouseUp(int x, int y, int mouseButton) {
            AbstractEditor editor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            Coordinate loc = editor.translateClick(x, y);

            if (loc == null) {
                return false;
            }

            if (mouseButton == 1) {
                if (shapeType == GeometryType.POINT) {
                    createPointShape(loc);
                } else {
                    addPointIfNotIdenticalToPreviousPoint(loc);
                }

            } else if (mouseButton == 3) {

                if (shapeType == GeometryType.POINT) {
                    createPointShape(loc);
                } else {
                    /*
                     * This is where the ghost object is replaced by the new
                     * shape. For the moment just delete the ghost line.
                     */
                    addPointIfNotIdenticalToPreviousPoint(loc);
                    if (points.size() != 0) {
                        getToolLayer().removeGhostLine();
                        if (points.size() < (shapeType == GeometryType.POLYGON ? 3
                                : 2)) {
                            points.clear();
                            getToolLayer().issueRefresh();
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
            // allow panning if it is mouse button 2 to be consistent with other
            // applications
            if (mouseButton == 2) {
                return false;
            }
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
                if (shapeType == GeometryType.POLYGON) {
                    Utilities.closeCoordinatesIfNecessary(points);
                    hazardEvent = hazardEventBuilder
                            .buildPolygonHazardEvent(pointsAsArray());
                } else {
                    hazardEvent = hazardEventBuilder
                            .buildLineHazardEvent(pointsAsArray());
                }
                hazardEventBuilder.addEvent(hazardEvent, getSpatialPresenter());
            } catch (InvalidGeometryException e) {
                statusHandler.handle(Priority.WARN,
                        "Error drawing vertex polygon: ", e);
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
            hazardEventBuilder.addEvent(hazardEvent, getSpatialPresenter());
        }
    }
}
