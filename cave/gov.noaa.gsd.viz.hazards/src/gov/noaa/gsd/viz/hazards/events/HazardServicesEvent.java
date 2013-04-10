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

import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Description: Extends the Base Hazard Event class. Adds a constructor which
 * takes a map of values for initialization.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 26, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class HazardServicesEvent extends BaseHazardEvent {

    /**
     * Used for creating geometry objects.
     */
    private static GeometryFactory geoFactory = new GeometryFactory();

    /**
     * Constructs an empty instance of a HazardServicesEvent.
     */
    public HazardServicesEvent() {
        super();
    }

    /**
     * Initializes this hazard event from a map. This map could have been
     * constructed from a python dict.
     * 
     * @param attributeMap
     *            Map of attributes
     * @return
     */
    public void initializeFromMap(Map<String, Serializable> attributeMap) {
        Set<String> keySet = attributeMap.keySet();

        for (String key : keySet) {
            Serializable attribute = attributeMap.get(key);

            if (key.equals(Utilities.HAZARD_EVENT_IDENTIFIER)) {
                setEventID((String) attribute);
            } else if (key.equals(Utilities.HAZARD_EVENT_STATE)) {
                String state = (String) attribute;

                if (state
                        .equalsIgnoreCase(Utilities.HAZARD_EVENT_STATE_PENDING)) {
                    setState(HazardState.PENDING);
                } else if (state
                        .equalsIgnoreCase(Utilities.HAZARD_EVENT_STATE_PROPOSED)) {
                    setState(HazardState.PROPOSED);
                } else if (state
                        .equalsIgnoreCase(Utilities.HAZARD_EVENT_STATE_ISSUED)) {
                    setState(HazardState.ISSUED);
                } else if (state
                        .equalsIgnoreCase(Utilities.HAZARD_EVENT_STATE_ENDED)) {
                    setState(HazardState.ENDED);
                }
            } else if (key.equals(Utilities.HAZARD_EVENT_PHEN)) {
                setPhenomenon((String) attribute);
            } else if (key.equals(Utilities.HAZARD_EVENT_SIG)) {
                setSignificance((String) attribute);
            } else if (key.equals(Utilities.HAZARD_EVENT_SUB_TYPE)) {
                setSubtype((String) attribute);
            } else if (key.equals(Utilities.HAZARD_EVENT_START_TIME)) {
                long startInMillis = (Long) attribute;
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(startInMillis);
                setStartTime(cal.getTime());
            } else if (key.equals(Utilities.HAZARD_EVENT_END_TIME)) {
                long endInMillis = (Long) attribute;
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(endInMillis);
                setEndTime(cal.getTime());
            } else if (key.equals(Utilities.HAZARD_EVENT_SHAPES)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Serializable>> shapesList = (List<Map<String, Serializable>>) attribute;

                for (Map<String, Serializable> shape : shapesList) {
                    String shapeType = (String) shape
                            .get(Utilities.HAZARD_EVENT_SHAPE_TYPE);

                    List<Coordinate> coordinateList = Lists.newArrayList();

                    if (shapeType
                            .equalsIgnoreCase(Utilities.HAZARD_EVENT_SHAPE_TYPE_POLYGON)) {
                        @SuppressWarnings("unchecked")
                        List<List<Float>> points = (List<List<Float>>) shape
                                .get("points");

                        for (List<Float> point : points) {
                            double lat = point.get(1);
                            double lon = point.get(0);
                            Coordinate coord = new Coordinate(lon, lat);
                            coordinateList.add(coord);
                        }

                        Geometry geometry = geoFactory
                                .createMultiPoint(coordinateList
                                        .toArray(new Coordinate[0]));
                        setGeometry(geometry);
                    }
                }
            } else {
                addHazardAttribute(key, attribute);
            }
        }
    }
}
