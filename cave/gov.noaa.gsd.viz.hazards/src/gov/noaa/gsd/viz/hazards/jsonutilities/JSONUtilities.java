/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.jsonutilities;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_GEOMETRY_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.POINTS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SPATIAL_INFO;
import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesLine;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesPolygon;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesSymbol;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.IHazardServicesShape;
import gov.noaa.nws.ncep.ui.pgen.display.IMultiPoint;
import gov.noaa.nws.ncep.ui.pgen.display.ISinglePoint;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * JSON helper methods for creating JSON messages. Also, contains utilities for
 * writing JSON to files, which is useful for debugging.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class JSONUtilities {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(JSONUtilities.class);

    /**
     * Convenience method for creating a JSON string describing a modified
     * hazard event. A modified event is one for which the shape coordinates
     * have changed.
     * 
     * @param modifiedShape
     *            The modified shape
     * @param shapesForEvent
     *            A list of shapes for the event.
     * @param newCoords
     *            The new coordinates of the boundary of the modified polygon.
     * 
     * @return JSON string representing a modified event.
     */
    static public String createModifiedHazardJSON(
            IHazardServicesShape modifiedShape,
            List<? extends IHazardServicesShape> shapesForEvent,
            List<Coordinate> newCoords) {
        String eventID = modifiedShape.getID();

        EventDict modifiedAreaObject = new EventDict();

        modifiedAreaObject
                .put(HazardConstants.HAZARD_EVENT_IDENTIFIER, eventID);
        modifiedAreaObject
                .put(HazardConstants.HAZARD_EVENT_SHAPE_TYPE,
                        (modifiedShape instanceof HazardServicesLine ? GeometryType.LINE
                                .getValue()
                                : (modifiedShape instanceof HazardServicesPolygon ? GeometryType.POLYGON
                                        .getValue() : GeometryType.POINT
                                        .getValue())));

        for (IHazardServicesShape shape : shapesForEvent) {

            Shape newJsonShape = null;

            Coordinate coord = null;
            Coordinate[] coords = null;

            if (shape.equals(modifiedShape)) {
                if (shape instanceof IMultiPoint) {
                    coords = newCoords
                            .toArray(new Coordinate[newCoords.size()]);
                } else {
                    coord = newCoords.get(0);
                }
            } else {
                if (shape instanceof IMultiPoint) {
                    coords = ((IMultiPoint) shape).getLinePoints();
                } else {
                    coord = ((ISinglePoint) shape).getLocation();
                }
            }

            if (shape.getClass().equals(HazardServicesLine.class)) {
                newJsonShape = new Line("", "true", "true", "true", "", 2,
                        coords);
            } else if (shape.getClass().equals(HazardServicesPolygon.class)) {
                newJsonShape = new Polygon("", "true", "true", "true", "", 2,
                        "", "", coords);
            } else if (shape.getClass().equals(HazardServicesPoint.class)
                    || shape.getClass().equals(HazardServicesSymbol.class)) {
                newJsonShape = new Point("", "true", "true", "true", "", coord,
                        eventID);
            } else {
                statusHandler
                        .error("JSONUtilities cannot convert shape of type "
                                + shape.getClass() + " to JSON.");
            }

            modifiedAreaObject.addShape(newJsonShape);

        }

        return modifiedAreaObject.toJSONString();

    }

    /**
     * Convenience method for creating the JSON for the drag drop dot.
     * 
     * @param
     * @return
     */
    static public String createDragDropPointJSON(double lat, double lon,
            long selectedTime) {
        String json = "{\"" + SPATIAL_INFO + "\":{\"points\":[[[" + lat + ","
                + lon + "]," + selectedTime + "]]}}";
        return json;

    }

    /**
     * 
     * @param
     * @return
     */
    static public Gson createGsonInterpreter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EventDict.class,
                new EventDictDeserializer());
        gsonBuilder.registerTypeAdapter(Dict.class, new DictDeserializer());
        gsonBuilder.registerTypeAdapter(DictList.class,
                new DictListDeserializer());
        gsonBuilder.registerTypeAdapter(ComparableLazilyParsedNumber.class,
                new LazilyParsedNumberSerializer());

        return gsonBuilder.create();
    }

    public static Geometry geometryFromJSONShapes(List<Dict> shapes) {
        GeometryFactory geometryFactory = new GeometryFactory();
        com.vividsolutions.jts.geom.Geometry[] geometries = new com.vividsolutions.jts.geom.Geometry[shapes
                .size()];
        for (int i = 0; i < shapes.size(); i++) {

            Dict shape = shapes.get(i);

            List<List<Double>> points = shape.getDynamicallyTypedValue(POINTS);
            List<Coordinate> coordinates = Lists.newArrayList();
            for (List<Double> point : points) {
                coordinates.add(new Coordinate(point.get(0), point.get(1)));
            }

            GeometryType geometryType = GeometryType.valueOf((String) shape
                    .getDynamicallyTypedValue(HAZARD_EVENT_GEOMETRY_TYPE));

            Coordinate[] coordinateArray = coordinates
                    .toArray(new Coordinate[coordinates.size()]);

            switch (geometryType) {
            case POINT:
                geometries[i] = geometryFactory.createPoint(coordinateArray[0]);
                break;
            case LINE:
                geometries[i] = geometryFactory
                        .createLineString(coordinateArray);
                break;
            case POLYGON:
                geometries[i] = geometryFactory
                        .createPolygon(geometryFactory
                                .createLinearRing(coordinateArray), null);
                break;
            }

        }

        return geometryFactory.createGeometryCollection(geometries);

    }

    public static Settings settingsFromJSON(String settingsAsJSON) {
        return new JSONConverter().fromJson(settingsAsJSON, Settings.class);
    }

}
