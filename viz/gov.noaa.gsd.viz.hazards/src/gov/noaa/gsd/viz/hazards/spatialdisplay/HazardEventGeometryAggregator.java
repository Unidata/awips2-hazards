package gov.noaa.gsd.viz.hazards.spatialdisplay;

/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Combine any Hazard Geometries that touch into aggregate events The new
 * composite geometry can be then displayed rather than individual basins.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 07, 2015    7279    dhladky      Initial creation.
 * Jul 25, 2016   19537    Chris.Golden Made deholePolygon() static and
 *                                      package-private, as it is needed
 *                                      in the spatial display components.
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */

public class HazardEventGeometryAggregator {

    /** Map of geometries being aggregated */
    private Map<Point, Geometry> combinedGeometries = null;

    /** Geotools factory **/
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /** size of input array **/
    private int size = 0;

    /** original size **/
    private int origSize = -1;

    /** main simplification level, matches map DB geometry simplifications **/
    private static final double SIMPLIFICATION_LEVEL = 0.0005;

    /** Simplification level for buffer **/
    private static final double BUFFER_LEVEL = SIMPLIFICATION_LEVEL / 4;

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventGeometryAggregator.class);

    /**
     * Public Constructor
     */
    public HazardEventGeometryAggregator() {

    }

    /***
     * Take the given events and aggregate them to produce semi-contiguous
     * regions.
     * 
     * @param events
     * @return events
     */
    public Collection<HazardEvent> aggregateEvents(
            Collection<BaseHazardEvent> events) {

        List<HazardEvent> list = new ArrayList<HazardEvent>(1);
        combinedGeometries = new ConcurrentHashMap<Point, Geometry>();
        // Add all events to the original map, convert to observed hazard
        for (BaseHazardEvent baseEvent : events) {

            if (baseEvent.getGeometry() instanceof GeometryCollection) {

                GeometryCollection geoCollection = (GeometryCollection) baseEvent
                        .getGeometry();

                for (int i = 0; i < geoCollection.getNumGeometries(); i++) {
                    if (geoCollection.getGeometryN(i) instanceof Polygon) {
                        Polygon poly = (Polygon) geoCollection.getGeometryN(i);
                        combinedGeometries.put(poly.getCentroid(), poly);
                    } else if (geoCollection.getGeometryN(i) instanceof MultiPolygon) {
                        MultiPolygon mpoly = (MultiPolygon) geoCollection
                                .getGeometryN(i);
                        for (int j = 0; j < mpoly.getNumGeometries(); j++) {
                            Polygon poly = (Polygon) mpoly.getGeometryN(j);
                            combinedGeometries.put(poly.getCentroid(), poly);
                        }
                    }
                }

                size = combinedGeometries.size();
                // keep track of iterations
                int intersectionCount = 0;
                // recurse over until you can combine no more geoms
                while (size != origSize) {
                    try {
                        evaluateGeometries();
                    } catch (Exception e) {
                        statusHandler
                                .error("Geometry intersection evaluation and aggreagation failed!.",
                                        e);
                    }
                    intersectionCount++;
                }

                if (combinedGeometries != null) {

                    // single pass to aggregate proximal geometries
                    size = combinedGeometries.size();
                    origSize = -1;
                    int distanceCount = 0;

                    while (size != origSize) {
                        try {
                            evaluateDistance();
                        } catch (Exception e) {
                            statusHandler
                                    .error("Geometry distance evaluation and aggreagation failed!.",
                                            e);
                        }
                        distanceCount++;
                    }

                    // create the events
                    for (Geometry aggrGeom : combinedGeometries.values()) {
                        HazardEvent event = new HazardEvent(baseEvent);
                        // attempt to clean up the geometries
                        aggrGeom = deholeGeometry(aggrGeom);
                        event.setGeometry(aggrGeom);
                        list.add(event);
                    }

                    statusHandler.info("Intersection Count: "
                            + intersectionCount + " Distance Count: "
                            + distanceCount + " iterations produced "
                            + combinedGeometries.size()
                            + " aggregate geometries from event: "
                            + baseEvent.getEventID());

                    combinedGeometries.clear();
                }

            } else if (baseEvent.getGeometry() instanceof Polygon) {
                HazardEvent event = new HazardEvent(baseEvent);
                list.add(event);
            }
        }

        if (!list.isEmpty()) {
            statusHandler.info("Returning " + list.size()
                    + " Observed HazardEvents.");
        }

        return list;
    }

    /**
     * Evaluates map of geometries and combines where possible
     */
    private void evaluateGeometries() throws Exception {

        // force evaluation of map first
        int internalOrigSize = combinedGeometries.size();
        // evaluate for touching geometries
        for (Point p1 : combinedGeometries.keySet()) {
            for (Point p2 : combinedGeometries.keySet()) {

                // extract geometries
                Geometry geom1 = combinedGeometries.get(p1);
                Geometry geom2 = combinedGeometries.get(p2);

                // get rid of garbage
                if (geom1 == null) {
                    combinedGeometries.remove(p1);
                }
                if (geom2 == null) {
                    combinedGeometries.remove(p2);
                }

                if (geom1 != null && geom2 != null && p1 != p2
                        && geom1.intersects(geom2)) {
                    // combine geometries
                    Geometry aggregateGeometry = aggregateGeometries(geom1,
                            geom2);
                    combinedGeometries.put(aggregateGeometry.getCentroid(),
                            aggregateGeometry);
                    // remove non-combined geometries
                    combinedGeometries.remove(p1);
                    combinedGeometries.remove(p2);
                }
            }
        }

        origSize = internalOrigSize;
        size = combinedGeometries.size();
    }

    /**
     * A single pass to aggregate geometries that are within a given distance of
     * each other.
     */
    private void evaluateDistance() throws Exception {

        // force evaluation of map first
        int internalOrigSize = combinedGeometries.size();

        for (Point p1 : combinedGeometries.keySet()) {
            for (Point p2 : combinedGeometries.keySet()) {

                // extract geometries
                Geometry geom1 = combinedGeometries.get(p1);
                Geometry geom2 = combinedGeometries.get(p2);

                // get rid of garbage
                if (geom1 == null) {
                    combinedGeometries.remove(p1);
                }
                if (geom2 == null) {
                    combinedGeometries.remove(p2);
                }

                if (p1 != p2 && geom1 != null && geom2 != null
                        && withInDistance(geom1, geom2)) {
                    Geometry aggrGeom = aggregateGeometries(geom1, geom2);
                    combinedGeometries.put(aggrGeom.getCentroid(), aggrGeom);
                    // remove other geometries
                    combinedGeometries.remove(p1);
                    combinedGeometries.remove(p2);
                }

            }
        }

        origSize = internalOrigSize;
        size = combinedGeometries.size();
    }

    /**
     * Combine two Geometries
     * 
     * @param geo1
     * @param geo2
     * @return geometry
     */
    private Geometry aggregateGeometries(Geometry geo1, Geometry geo2) {

        // Just mash the second one into the first and combine the geometries
        Geometry[] hucGeometries = new Geometry[2];
        hucGeometries[0] = geo1;
        hucGeometries[1] = geo2;
        Geometry combinedGeom = geometryFactory.createGeometryCollection(
                hucGeometries).buffer(0);

        return combinedGeom;
    }

    /**
     * Attempts to remove interior holes on a polygon. Will take up to 3 passes
     * over the polygon expanding any interior rings and merging rings back in.
     * 
     * @param g
     *            Geometry to be de-holed.
     * @return De-holed geometry.
     */
    private Geometry deholeGeometry(Geometry g) {

        for (int i = 0; i < g.getNumGeometries(); i++) {

            if (g.getGeometryN(i) instanceof Polygon) {
                Polygon poly = (Polygon) g.getGeometryN(i);
                poly = deholePolygon(poly, geometryFactory);
            } else if (g.getGeometryN(i) instanceof MultiPolygon) {
                MultiPolygon mpoly = (MultiPolygon) g.getGeometryN(i);
                for (int j = 0; j < mpoly.getNumGeometries(); j++) {
                    Polygon poly = (Polygon) mpoly.getGeometryN(j);
                    poly = deholePolygon(poly, geometryFactory);
                }
            }
        }

        return g;
    }

    /**
     * Attempt to remove interior holes from a polygon. Up to three passes are
     * made over the polygon, each one expanding any interior rings and merging
     * rings back in.
     * <p>
     * Note that this algorithm was added as part of Redmine issues #7624 and
     * #7279, in order to simplify polygons that are themselves elements of
     * multi-polygons, due to performance problems when rendering; and to
     * simplify complex basins for the Flash Flood Recommender.
     * </p>
     * 
     * @param polygon
     *            Polygon to have holes pruned out.
     * @param geometryFactory
     *            Geometry factory to be used during the pruning.
     * @return Polygon with the holes pruned out after three iterations.
     */
    static Polygon deholePolygon(Polygon polygon,
            GeometryFactory geometryFactory) {
        int numInteriorRings = polygon.getNumInteriorRing();
        for (int j = 0; (numInteriorRings > 0) && (j < 3); j++) {
            Geometry[] hucGeometries = new Geometry[numInteriorRings + 1];
            hucGeometries[0] = polygon;
            for (int i = 0; i < numInteriorRings; i++) {
                hucGeometries[i + 1] = polygon.getInteriorRingN(i).buffer(
                        BUFFER_LEVEL);
            }
            polygon = (Polygon) geometryFactory.createGeometryCollection(
                    hucGeometries).buffer(0);
            numInteriorRings = polygon.getNumInteriorRing();
        }
        return polygon;
    }

    /**
     * Determine whether given geometries are within distance params for
     * combining.
     */
    private boolean withInDistance(Geometry geo1, Geometry geo2) {
        // Determine if two geometries are within the required distance from
        // each other to be combined
        boolean withInDistance = false;

        double distance = geo1.distance(geo2);
        if (distance < 0.01) {
            withInDistance = true;
        }
        return withInDistance;
    }

}
