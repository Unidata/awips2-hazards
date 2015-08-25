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
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.tools.InputHandlerDefaultImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
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
 * Sep 09, 2014     3994   Robert.Blum         Added handleMouseEnter to reset the 
 *                                             cursor type.
 * Dec 05, 2014     4124   Chris.Golden        Changed to work with newly parameterized
 *                                             config manager.
 * Feb  6, 2015     4375   Dan Schaffer        Slight patch to geometry intersection error message
 * Feb 12, 2015     4959   Dan Schaffer        Modify MB3 add/remove UGCs to match Warngen
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

    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    public VertexHazardDrawingAction(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager) {
        this.sessionManager = sessionManager;
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
     * Sep   10, 2014  3793    Robert.Blum     Modified handleMouseUp to return false for
     *                                         when the middle mouse button is pressed.
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
        public boolean handleMouseEnter(Event event) {
            getSpatialPresenter().getView().setCursor(
                    SpatialViewCursorTypes.DRAW_CURSOR);
            return handleMouseMove(event.x, event.y);
        }

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
                        (ArrayList<Coordinate>) points, getSpatialDisplay()
                                .getActiveLayer());

                List<Coordinate> ghostPts = Lists.newArrayList(points);
                ghostPts.add(loc);

                ((Line) ghost).setLinePoints(ghostPts);

                getSpatialDisplay().setGhostLine(ghost);
                getSpatialDisplay().issueRefresh();
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

            addPointIfNotIdenticalToPreviousPoint(loc);
            if (mouseButton == 1) {
                return true;
            } else if (mouseButton == 3) {

                /*
                 * This is where the ghost object is replaced by the new shape.
                 * First, delete the ghost line.
                 */
                getSpatialDisplay().removeGhostLine();

                List<Coordinate> pointsCopy = new ArrayList<Coordinate>(points);
                points.clear();

                // Indicate that this drawing action is done.
                getSpatialPresenter().drawingActionComplete(
                        new ArrayList<Coordinate>(pointsCopy));

                return true;
            }
            return false;
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
    }
}
