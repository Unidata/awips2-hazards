/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.spatialdisplay.CircleDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.DotDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.LineDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.PolygonDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.StarDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.TextPositioner;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.display.IArc;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

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
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class HazardServicesDrawableBuilder implements
        IHazardServicesDrawableBuilder {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesDrawableBuilder.class);

    private HazardServicesDrawingAttributes drawingAttributes = null;

    @Override
    /**
     *  Builds a PGEN drawable representing a single point.
     */
    public AbstractDrawableComponent buildPoint(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer) {
        throw new UnsupportedOperationException();
    }

    @Override
    /**
     * Builds a PGEN drawable representing a line.
     */
    public AbstractDrawableComponent buildLine(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new LineDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell());

            points = drawingAttributes.updateFromEventDict(shape);

            // Use the PGEN LINE to create the polygon. A closed
            // line is a polygon.
            drawableComponent = new HazardServicesLine(drawingAttributes,
                    "Line", drawingAttributes.getLineStyle(), points,
                    activeLayer, eventID);

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildLine(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    @Override
    /**
     * Builds a PGEN drawable representing a polygon
     */
    public AbstractDrawableComponent buildPolygon(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new PolygonDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell());

            points = drawingAttributes.updateFromEventDict(shape);

            // Use the PGEN LINE to create the polygon. A closed
            // line is a polygon.
            drawableComponent = new HazardServicesPolygon(drawingAttributes,
                    "Line", drawingAttributes.getLineStyle(), points,
                    activeLayer, eventID);

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildPolygon(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    @Override
    /**
     * Builds a PGEN drawable representing a circle.
     */
    public AbstractDrawableComponent buildCircle(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer, MapDescriptor descriptor) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new CircleDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell());

            // Just need a center point and a radius
            points = drawingAttributes.updateFromEventDict(shape);

            // Create the Arc object ... This is a temporary object
            // just to get the points.
            // Then these are passed to a multipoint object.
            // Apparently, arcs cannot be
            // filled and we need the dot/circle to be fillable.

            /*
             * No choice but to convert to an ArrayList here. The PGEN interface
             * expects an ArrayList not a List.
             */
            ArrayList<Coordinate> pointsList = Lists.newArrayList(points);

            drawableComponent = new DrawableElementFactory().create(
                    DrawableType.ARC, drawingAttributes, "Line", "LINE_SOLID",
                    pointsList, activeLayer);

            points = getCirclePoints((IArc) drawableComponent, descriptor);

            drawableComponent = new HazardServicesCircle(drawingAttributes,
                    "Line", drawingAttributes.getLineStyle(), points,
                    activeLayer, eventID);

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildCircle(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    @Override
    /**
     * Builds a PGEN drawable representing a dot
     */
    public AbstractDrawableComponent buildDot(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new DotDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell());
            drawingAttributes.setTextPosition(TextPositioner.TOP);
            points = drawingAttributes.updateFromEventDict(shape);

            drawableComponent = new HazardServicesSymbol(drawingAttributes,
                    "DOT", "DOT", points, activeLayer, eventID);

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildDot(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    @Override
    /**
     * Builds a PGEN drawable representing a star
     */
    public AbstractDrawableComponent buildStar(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer) {
        AbstractDrawableComponent drawableComponent = null;

        try {
            drawingAttributes = new StarDrawingAttributes(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell());

            points = drawingAttributes.updateFromEventDict(shape);

            drawableComponent = new HazardServicesSymbol(drawingAttributes,
                    "FILLED_STAR", "FILLED_STAR", points, activeLayer, eventID);

        } catch (VizException e) {
            statusHandler.error(
                    "HazardServicesDrawableBuilder.buildStar(): build "
                            + "of shape failed.", e);
        }

        return drawableComponent;
    }

    /**
     * Builds a PGEN drawable representing a circle.
     * 
     * @param
     * @return
     */
    private ArrayList<Coordinate> getCirclePoints(IArc arc,
            MapDescriptor descriptor) {
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
        /*
         * Convert center and circumference point from lat/lon to pixel
         * coordinates.
         */
        double[] tmp = { arc.getCenterPoint().x, arc.getCenterPoint().y, 0.0 };
        double[] center = descriptor.worldToPixel(tmp);
        double[] tmp2 = { arc.getCircumferencePoint().x,
                arc.getCircumferencePoint().y, 0.0 };
        double[] circum = descriptor.worldToPixel(tmp2);

        /*
         * calculate angle of major axis
         */
        double axisAngle = Math.toDegrees(Math.atan2((circum[1] - center[1]),
                (circum[0] - center[0])));
        double cosineAxis = Math.cos(Math.toRadians(axisAngle));
        double sineAxis = Math.sin(Math.toRadians(axisAngle));

        /*
         * calculate half lengths of major and minor axes
         */
        double diff[] = { circum[0] - center[0], circum[1] - center[1] };
        double major = Math.sqrt((diff[0] * diff[0]) + (diff[1] * diff[1]));
        double minor = major * arc.getAxisRatio();

        /*
         * Calculate points along the arc
         */
        double angle = arc.getStartAngle();
        int numpts = (int) Math.round(arc.getEndAngle() - arc.getStartAngle()
                + 1.0);

        double[] pixelCoords = new double[2];
        double[] worldCoords = new double[2];

        for (int j = 0; j < numpts; j++) {
            double thisSine = Math.sin(Math.toRadians(angle));
            double thisCosine = Math.cos(Math.toRadians(angle));
            // Can maybe use simpler less expensive calculations for circle,
            // if ever necessary.
            // if ( arc.getAxisRatio() == 1.0 ) {
            // path[j][0] = center[0] + (major * thisCosine );
            // path[j][1] = center[1] + (minor * thisSine );
            // }
            // else {

            pixelCoords[0] = center[0] + (major * cosineAxis * thisCosine)
                    - (minor * sineAxis * thisSine);

            pixelCoords[1] = center[1] + (major * sineAxis * thisCosine)
                    + (minor * cosineAxis * thisSine);

            // Convert back to world coordinates.
            worldCoords = descriptor.pixelToWorld(pixelCoords);

            Coordinate coord = new Coordinate(worldCoords[0], worldCoords[1], 0);
            coordinates.add(coord);
            // }

            angle += 1.0;
        }

        return coordinates;
    }

    /**
     * @return the drawingAttributes
     */
    public HazardServicesDrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    @Override
    public AbstractDrawableComponent buildText(Dict shape, String eventID,
            final List<Coordinate> points, Layer activeLayer,
            GeometryFactory geoFactory) {
        Point centerPoint;
        AbstractDrawableComponent drawableComponent = null;

        // Make a copy of the points list. Do not modify the reference passed
        // in.
        Coordinate[] coords = new Coordinate[points.size() + 1];
        coords = points.toArray(coords);

        if (points.size() > 1) {
            coords[points.size()] = coords[0];
            LineString ls = geoFactory.createLineString(coords);
            centerPoint = ls.getCentroid();
        } else {
            centerPoint = geoFactory.createPoint(points.get(0));
        }

        drawableComponent = new HazardServicesText(drawingAttributes,
                drawingAttributes.getString()[0], "TEXT", centerPoint,
                activeLayer, eventID);

        return drawableComponent;
    }

    @Override
    public AbstractDrawableComponent buildDrawableComponent(Dict shape,
            String eventID, List<Coordinate> points, Layer activeLayer,
            MapDescriptor descriptor) {
        AbstractDrawableComponent drawableComponent = null;

        String shapeType = (String) shape
                .get(Utilities.HAZARD_EVENT_SHAPE_TYPE);

        // Create a shape factory here...
        if (shapeType.equalsIgnoreCase(Utilities.HAZARD_EVENT_SHAPE_TYPE_POINT)) {
            // Store point attributes
        } else if (shapeType
                .equalsIgnoreCase(Utilities.HAZARD_EVENT_SHAPE_TYPE_LINE)) {
            drawableComponent = this.buildLine(shape, eventID, points,
                    activeLayer);
        } else if (shapeType
                .equalsIgnoreCase(Utilities.HAZARD_EVENT_SHAPE_TYPE_POLYGON)) {
            drawableComponent = this.buildPolygon(shape, eventID, points,
                    activeLayer);
        } else if (shapeType
                .equalsIgnoreCase(Utilities.HAZARD_EVENT_SHAPE_TYPE_CIRCLE)) {
            drawableComponent = this.buildCircle(shape, eventID, points,
                    activeLayer, descriptor);
        } else if (shapeType
                .equalsIgnoreCase(Utilities.HAZARD_EVENT_SHAPE_TYPE_DOT)) {
            drawableComponent = this.buildDot(shape, eventID, points,
                    activeLayer);
        } else if (shapeType
                .equalsIgnoreCase(Utilities.HAZARD_EVENT_SHAPE_TYPE_STAR)) {
            drawableComponent = this.buildStar(shape, eventID, points,
                    activeLayer);
        } else {
            statusHandler.debug("Unknown Shape " + shapeType);
        }

        return drawableComponent;
    }

}
