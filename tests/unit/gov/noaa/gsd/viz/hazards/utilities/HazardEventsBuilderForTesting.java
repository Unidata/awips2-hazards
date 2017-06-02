/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.utilities;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CREST;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.FALL_BELOW;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_PHEN;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SHAPES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SIG;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_STATUS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SUB_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_VTEC_MODE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.RISE_ABOVE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SITE_ID;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.hazardStatusFromString;
import gov.noaa.gsd.common.utilities.DateTimes;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Description: Utility method for building {@link HazardEvent}s from a JSON
 * representation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            daniel.s.schaffer      Initial creation
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class HazardEventsBuilderForTesting {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final List<IHazardEvent> events;

    public HazardEventsBuilderForTesting(String eventsAsJson) {
        events = Lists.newArrayList();
        Dict dict = Dict.getInstance(eventsAsJson);
        for (String eventId : dict.keySet()) {

            IHazardEvent event = new HazardEventManager(true).createEvent();
            Dict eventDict = dict.getDynamicallyTypedValue(eventId);
            Map<String, Serializable> attributes = Maps.newHashMap();
            for (String key : eventDict.keySet()) {
                if (key.equals(HAZARD_EVENT_IDENTIFIER)) {
                    event.setEventID(eventId);
                } else if (key.equals(HAZARD_EVENT_STATUS)) {
                    String value = eventDict.getDynamicallyTypedValue(key);
                    event.setStatus(hazardStatusFromString(value));
                } else if (key.equals(SITE_ID)) {
                    String value = eventDict.getDynamicallyTypedValue(key);
                    event.setSiteID(value);
                } else if (key.equals(HAZARD_EVENT_START_TIME)) {
                    Date date = dateFromMillis(eventDict, key);
                    event.setStartTime(date);

                    /**
                     * Deal with the fact that the handling of IssueTime needs
                     * work. See Issue #694.
                     */
                    event.setCreationTime(date);
                } else if (key.equals(HAZARD_EVENT_END_TIME)) {
                    Date date = dateFromMillis(eventDict, key);
                    event.setEndTime(date);
                } else if (key.equals(SITE_ID)) {
                    String site = eventDict.getDynamicallyTypedValue(key);
                    event.setSiteID(site);
                } else if (key.equals(HAZARD_EVENT_VTEC_MODE)) {
                    String mode = eventDict.getDynamicallyTypedValue(key);
                    if (mode.equals("operational")) {
                        event.setHazardMode(ProductClass.OPERATIONAL);
                    } else {
                        throw new UnsupportedOperationException(
                                "Do not support mode " + mode);
                    }
                } else if (key.equals(HAZARD_EVENT_PHEN)) {
                    String value = eventDict.getDynamicallyTypedValue(key);
                    event.setPhenomenon(value);
                } else if (key.equals(HAZARD_EVENT_SIG)) {
                    String value = eventDict.getDynamicallyTypedValue(key);
                    event.setSignificance(value);
                } else if (key.equals(HAZARD_EVENT_SUB_TYPE)) {
                    String value = eventDict.getDynamicallyTypedValue(key);
                    event.setSubType(value);
                } else if (key.equals(HAZARD_EVENT_SHAPES)) {
                    List<Dict> shapes = eventDict
                            .getDynamicallyTypedValue(HAZARD_EVENT_SHAPES);
                    if (shapes.size() > 1) {
                        throw new UnsupportedOperationException(
                                "Only support shapes with one polygon");
                    }
                    Dict shape = shapes.get(0);
                    List<List<Double>> points = shape
                            .getDynamicallyTypedValue("points");
                    Geometry geometry = buildGeometry(points);
                    event.setGeometry(new GeometryWrapper(geometry, 0.0));
                } else if (key.equals(RISE_ABOVE) || key.equals(CREST)
                        || key.equals(FALL_BELOW)) {
                    Number value = eventDict.getDynamicallyTypedValue(key);
                    attributes.put(key, value.longValue());
                } else {
                    Serializable value = eventDict
                            .getDynamicallyTypedValue(key);
                    attributes.put(key, value);
                }
            }
            event.setHazardAttributes(attributes);
            events.add(event);
        }
    }

    private Geometry buildGeometry(List<List<Double>> points) {
        /**
         * We have to deal with the fact that the first point has to be
         * replicated in order to make this a polygon.
         */
        Coordinate[] coordinates = new Coordinate[points.size() + 1];
        int index = 0;
        for (List<Double> point : points) {
            coordinates[index] = new Coordinate(point.get(0), point.get(1));
            index += 1;
        }
        coordinates[index] = coordinates[0];
        LinearRing shell = geometryFactory.createLinearRing(coordinates);
        Polygon result = geometryFactory.createPolygon(shell, null);
        return result;
    }

    private Date dateFromMillis(Dict eventDict, String key) {
        Number secondsAsNumber = eventDict.getDynamicallyTypedValue(key);
        Long timeInMillis = secondsAsNumber.longValue();
        return new DateTimes().newDateTime(timeInMillis).toDate();
    }

    public List<IHazardEvent> getEvents() {
        return events;
    }

}
