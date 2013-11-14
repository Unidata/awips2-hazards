/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.events;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Description: Provides a utility method which creates a BaseHazardEvent object
 * from a map of hazard attributes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 26, 2013            Bryon.Lawrence      Initial creation
 * Jun 04, 2013            Bryon.Lawrence      Added support for events
 *                                             with multiple polygons.
 * Jun 25, 2013            Bryon.Lawrence      Added a static method 
 *                                             which builds 
 *                                             a Base Hazard Event object
 *                                             from a map.
 * Jun 27, 2013            Bryon.Lawrence      Removed everything but the
 *                                             static method which builds
 *                                             a Base Hazard Event object
 *                                             from a map. This class
 *                                             no longer extends 
 *                                             BaseHazardEvent.
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class HazardServicesEvent {

    /**
     * Used for creating geometry objects.
     */
    private static GeometryFactory geoFactory = new GeometryFactory();

    /**
     * Initializes a new Base Hazard Event from a map. This map could have been
     * constructed from a python dict.
     * 
     * @param attributeMap
     *            Map of attributes
     * @return An initialized BaseHazardEvent
     */
    public static BaseHazardEvent buildBaseHazardEventFromMap(
            Map<String, Serializable> attributeMap) {

        BaseHazardEvent baseEvent = new BaseHazardEvent();

        Set<String> keySet = attributeMap.keySet();

        for (String key : keySet) {
            Serializable attribute = attributeMap.get(key);

            if (key.equals(HazardConstants.HAZARD_EVENT_IDENTIFIER)) {
                baseEvent.setEventID((String) attribute);
            } else if (key.equals(HazardConstants.HAZARD_EVENT_STATE)) {
                String state = (String) attribute;

                if (state.equalsIgnoreCase(HazardConstants.HazardState.PENDING
                        .getValue())) {
                    baseEvent.setState(HazardState.PENDING);
                } else if (state
                        .equalsIgnoreCase(HazardConstants.HazardState.PROPOSED
                                .getValue())) {
                    baseEvent.setState(HazardState.PROPOSED);
                } else if (state
                        .equalsIgnoreCase(HazardConstants.HazardState.ISSUED
                                .getValue())) {
                    baseEvent.setState(HazardState.ISSUED);
                } else if (state
                        .equalsIgnoreCase(HazardConstants.HazardState.ENDED
                                .getValue())) {
                    baseEvent.setState(HazardState.ENDED);
                }
            } else if (key.equals(HazardConstants.HAZARD_EVENT_PHEN)) {
                baseEvent.setPhenomenon((String) attribute);
            } else if (key.equals(HazardConstants.HAZARD_EVENT_SIG)) {
                baseEvent.setSignificance((String) attribute);
            } else if (key.equals(HazardConstants.HAZARD_EVENT_SUB_TYPE)) {
                baseEvent.setSubtype((String) attribute);
            } else if (key.equals(HazardConstants.HAZARD_EVENT_START_TIME)) {
                long startInMillis = ((Number) attribute).longValue();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(startInMillis);
                baseEvent.setStartTime(cal.getTime());
            } else if (key.equals(HazardConstants.HAZARD_EVENT_END_TIME)) {
                long endInMillis = ((Number) attribute).longValue();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(endInMillis);
                baseEvent.setEndTime(cal.getTime());
            } else if (key.equals(HazardConstants.HAZARD_EVENT_SHAPES)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Serializable>> shapesList = (List<Map<String, Serializable>>) attribute;

                List<Polygon> polygonList = Lists.newArrayList();

                for (Map<String, Serializable> shape : shapesList) {
                    String shapeType = (String) shape
                            .get(HazardConstants.HAZARD_EVENT_SHAPE_TYPE);

                    List<Coordinate> coordinateList = Lists.newArrayList();

                    if (shapeType
                            .equalsIgnoreCase(HazardConstants.HAZARD_EVENT_SHAPE_TYPE_POLYGON)) {
                        @SuppressWarnings("unchecked")
                        List<List<Float>> points = (List<List<Float>>) shape
                                .get("points");

                        for (List<Float> point : points) {
                            double lat = point.get(1);
                            double lon = point.get(0);
                            Coordinate coord = new Coordinate(lon, lat);
                            coordinateList.add(coord);
                        }

                        /*
                         * Make sure that the coordinate list is closed. This is
                         * necessary for building a linear ring.
                         */
                        Coordinate firstCoord = coordinateList.get(0);
                        coordinateList.add(firstCoord);

                        LinearRing linearRing = geoFactory
                                .createLinearRing(coordinateList
                                        .toArray(new Coordinate[0]));

                        Polygon polygon = geoFactory.createPolygon(linearRing,
                                null);

                        polygonList.add(polygon);
                    }
                }

                Geometry geometry = geoFactory.createMultiPolygon(polygonList
                        .toArray(new Polygon[0]));
                baseEvent.setGeometry(geometry);

            } else {
                baseEvent.addHazardAttribute(key, attribute);
            }
        }

        return baseEvent;
    }
}
