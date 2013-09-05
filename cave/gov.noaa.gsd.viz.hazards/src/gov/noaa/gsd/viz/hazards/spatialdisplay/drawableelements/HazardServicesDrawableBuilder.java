/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.LineDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PointDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PolygonDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.StarDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.StormTrackDotDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.TextPositioner;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayer;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * Description: Contains factory methods for building the PGEN drawables
 * displayed in Hazard Services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 02, 2012            bryon.lawrence      Added java doc,
 *                                             refactored to 
 *                                             reduce coupling with
 *                                             drawable classes.
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class HazardServicesDrawableBuilder {

    /**
     * TODO Are these available anywhere in PGEN? I couldn't find them but they
     * should be!
     */
    private static final String LINE = "Line";

    private static final String DOT = "DOT";

    private static final String FILLED_STAR = "FILLED_STAR";

    private static final String TEXT = "TEXT";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesDrawableBuilder.class);

    private HazardServicesDrawingAttributes drawingAttributes;

    private final ISessionManager sessionManager;

    public HazardServicesDrawableBuilder(ISessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public AbstractDrawableComponent buildPoint(IHazardEvent hazardEvent,
            int shapeNum, Layer activeLayer) {
        DECollection collectionComponent = new DECollection();
        try {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();

            drawingAttributes = new PointDrawingAttributes(shell,
                    sessionManager, PointDrawingAttributes.Element.OUTER);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);

            collectionComponent.add(new HazardServicesPoint(drawingAttributes,
                    DOT, DOT, points, activeLayer, hazardEvent.getEventID(),
                    true));

            HazardServicesDrawingAttributes drawingAttributes = new PointDrawingAttributes(
                    shell, sessionManager, PointDrawingAttributes.Element.INNER);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);
            points = drawingAttributes.buildCoordinates(shapeNum, hazardEvent);

            collectionComponent.add(new HazardServicesPoint(drawingAttributes,
                    DOT, DOT, points, activeLayer, hazardEvent.getEventID(),
                    false));
        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildPoint(): build "
                            + "of shape failed.", e);
        }

        return collectionComponent;
    }

    /**
     * Builds a PGEN drawable representing a line.
     */
    public AbstractDrawableComponent buildLine(IHazardEvent hazardEvent,
            int shapeNum, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new LineDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    sessionManager);

            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);

            drawableComponent = new HazardServicesLine(drawingAttributes, LINE,
                    drawingAttributes.getLineStyle().toString(), points,
                    activeLayer, hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildLine(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * Builds a PGEN drawable representing a polygon
     */
    public AbstractDrawableComponent buildPolygon(IHazardEvent hazardEvent,
            int shapeNum, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new PolygonDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    sessionManager);

            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);

            drawableComponent = new HazardServicesPolygon(drawingAttributes,
                    LINE, drawingAttributes.getLineStyle().toString(), points,
                    activeLayer, hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildPolygon(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * Builds a PGEN drawable representing a star
     * 
     * @param shapeNum
     */
    public AbstractDrawableComponent buildStar(IHazardEvent hazardEvent,
            int shapeNum, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new StarDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    sessionManager);
            List<Coordinate> points = drawingAttributes.buildCoordinates(
                    shapeNum, hazardEvent);
            drawingAttributes.setAttributes(shapeNum, hazardEvent);

            drawableComponent = new HazardServicesSymbol(drawingAttributes,
                    FILLED_STAR, FILLED_STAR, points, activeLayer,
                    hazardEvent.getEventID());

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildStar(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * @return the drawingAttributes
     */
    public HazardServicesDrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    public AbstractDrawableComponent buildStormTrackDotComponent(
            Layer activeLayer) {
        AbstractDrawableComponent result = null;
        try {
            StormTrackDotDrawingAttributes drawingAttributes = new StormTrackDotDrawingAttributes(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(), sessionManager);
            this.drawingAttributes = drawingAttributes;
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            List<Coordinate> points = drawingAttributes.buildCoordinates();
            drawingAttributes.setAttributes();

            result = new HazardServicesSymbol(drawingAttributes, DOT, DOT,
                    points, activeLayer, Utilities.DRAG_DROP_DOT);
            return result;
        } catch (VizException e) {
            statusHandler.error("Could not build storm track dot", e);
        }
        return result;
    }

    public AbstractDrawableComponent buildText(Geometry geometry, String id,
            Layer activeLayer, GeometryFactory geoFactory) {
        Point centerPoint;
        AbstractDrawableComponent drawableComponent = null;
        Coordinate[] coords = geometry.getCoordinates();

        if (coords.length > 1) {
            Coordinate[] fullyEnclosedCoords = new Coordinate[coords.length + 1];
            System.arraycopy(coords, 0, fullyEnclosedCoords, 0, coords.length);
            fullyEnclosedCoords[coords.length] = coords[0];
            LineString ls = geoFactory.createLineString(fullyEnclosedCoords);
            centerPoint = ls.getCentroid();
        } else {
            centerPoint = geoFactory.createPoint(coords[0]);
        }

        drawableComponent = new HazardServicesText(drawingAttributes,
                drawingAttributes.getString()[0], TEXT, centerPoint,
                activeLayer, id);

        return drawableComponent;
    }

    public List<AbstractDrawableComponent> buildDrawableComponents(
            ToolLayer toolLayer, IHazardEvent hazardEvent, Layer activeLayer) {

        List<AbstractDrawableComponent> result = Lists.newArrayList();
        for (int shapeNum = 0; shapeNum < hazardEvent.getGeometry()
                .getNumGeometries(); shapeNum++) {

            AbstractDrawableComponent drawableComponent = addShapeComponent(
                    toolLayer, hazardEvent, activeLayer, result, shapeNum);

            addTextComponent(toolLayer, hazardEvent.getEventID(), result,
                    drawableComponent);
        }
        return result;
    }

    private AbstractDrawableComponent addShapeComponent(ToolLayer toolLayer,
            IHazardEvent hazardEvent, Layer activeLayer,
            List<AbstractDrawableComponent> drawableComponents, int shapeNum) {
        AbstractDrawableComponent drawableComponent = null;

        Class<?> geometryClass = hazardEvent.getGeometry().getClass();

        if (geometryClass.equals(Point.class)) {
            drawableComponent = buildPoint(hazardEvent, shapeNum, activeLayer);
        } else if (geometryClass.equals(LineString.class)) {
            drawableComponent = buildLine(hazardEvent, shapeNum, activeLayer);
        } else if (geometryClass.equals(Polygon.class)
                || geometryClass.equals(MultiPolygon.class)) {
            drawableComponent = buildPolygon(hazardEvent, shapeNum, activeLayer);
        }

        /**
         * TODO Handle stars for Storm track tool
         */
        else {
            throw new IllegalArgumentException("Not yet supporting geometry "
                    + geometryClass);
        }

        /**
         * TODO Figure out how to get rid of this kludge. The kludge works
         * around a failure somewhere else to properly identify whether or not
         * an event is selected. As a result, you cannot modify the polygons.
         */
        if (drawingAttributes.getLineWidth() == 4.0) {
            toolLayer.setSelectedHazardIHISLayer(drawableComponent);
        }

        toolLayer.addElement(drawableComponent);
        drawableComponents.add(drawableComponent);
        return drawableComponent;
    }

    public void addTextComponent(ToolLayer toolLayer, String id,
            List<AbstractDrawableComponent> drawableComponents,
            AbstractDrawableComponent associatedShape) {
        if (drawingAttributes.getString() != null
                && drawingAttributes.getString().length > 0) {
            IHazardServicesShape shape = (IHazardServicesShape) (associatedShape instanceof DECollection ? ((DECollection) associatedShape)
                    .getPrimaryDE() : associatedShape);
            AbstractDrawableComponent text = buildText(shape.getGeometry(), id,
                    toolLayer.getActiveLayer(), toolLayer.getGeoFactory());
            toolLayer.addElement(text);
            drawableComponents.add(text);
        }
    }

}
