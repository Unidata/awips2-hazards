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

package com.raytheon.uf.viz.hazards.sessionmanager.events.impl;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Polygon Simplifier that uses WarnGen's reduction algorithm.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#    Engineer      Description
 * ------------  ---------- -----------   --------------------------
 * Dec 01, 2015   13172     Robert.Blum   Initial creation
 * Dec 08, 2015    8765     Robert.Blum   Ensure point reduction is not done
 *                                        if target point size is less than 3.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public class WarnGenPolygonSimplifier {

    private static GeometryFactory gf = new GeometryFactory();

    /** main simplification level, matches map DB geometry simplifications **/
    private static final double SIMPLIFICATION_LEVEL = 0.0005;

    /** Simplification level for buffer **/
    private static final double BUFFER_LEVEL = SIMPLIFICATION_LEVEL / 4;

    /**
     * Reduces the Geometry passed in to the pointLimit.
     * 
     * @param geometry
     * @param pointLimit
     * @return
     */
    public static Geometry reduceGeometry(Geometry geometry, int pointLimit) {
        /*
         * Test if point reduction is necessary...
         */
        if ((pointLimit > 2) && (geometry.getNumPoints() > pointLimit)) {

            // Remove any interior holes
            geometry = deholeGeometry(geometry);

            if (geometry instanceof GeometryCollection) {
                geometry = reduceGeometryCollection(geometry, pointLimit);
            } else if (geometry instanceof MultiPolygon) {
                geometry = reduceMultiPolygon((MultiPolygon) geometry,
                        pointLimit);
            } else if (geometry instanceof Polygon) {
                geometry = reducePolygon((Polygon) geometry, pointLimit);
            }
        }
        return geometry;
    }

    /**
     * Reduces each Polygon or MultiPolygon in the Geometry Collection and
     * returns a single MultiPolygon.
     * 
     * Note that currently each Polygon is reduced to the pointLimit.
     * 
     * @param geometry
     * @param pointLimit
     * @return
     */
    private static Geometry reduceGeometryCollection(Geometry geometry,
            int pointLimit) {
        List<Polygon> reducedPolygons = new ArrayList<Polygon>(
                geometry.getNumGeometries());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry geom = geometry.getGeometryN(i);
            if (geom instanceof Polygon) {
                geom = reducePolygon((Polygon) geom, pointLimit);
                reducedPolygons.add((Polygon) geom);
            } else if (geom instanceof MultiPolygon) {
                geom = reduceMultiPolygon((MultiPolygon) geom, pointLimit);
                for (int j = 0; j < geom.getNumGeometries(); j++) {
                    reducedPolygons.add((Polygon) geom.getGeometryN(j));
                }
            }
        }
        if (reducedPolygons.isEmpty() == false) {
            return gf.createMultiPolygon(reducedPolygons
                    .toArray(new Polygon[reducedPolygons.size()]));
        }
        return (Geometry) geometry.clone();
    }

    /**
     * Reduces each Polygon in the MulitPolygon.
     * 
     * Note that currently each Polygon is reduced to the pointLimit.
     * 
     * @param geometry
     * @param pointLimit
     * @return
     */
    private static Geometry reduceMultiPolygon(MultiPolygon geometry,
            int pointLimit) {
        List<Polygon> reducedPolygons = new ArrayList<Polygon>(
                geometry.getNumGeometries());
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry geom = geometry.getGeometryN(i);
            if (geom instanceof Polygon) {
                geom = reducePolygon((Polygon) geom, pointLimit);
                reducedPolygons.add((Polygon) geom);
            }
        }
        if (reducedPolygons.isEmpty() == false) {
            return gf.createMultiPolygon(reducedPolygons
                    .toArray(new Polygon[reducedPolygons.size()]));
        }
        return (Geometry) geometry.clone();
    }

    /**
     * Reduces the Polygon to the pointLimit.
     * 
     * @param geometry
     * @param pointLimit
     * @return
     */
    private static Geometry reducePolygon(Polygon geometry, int pointLimit) {
        PolygonUtil polygonUtil = new PolygonUtil(pointLimit);

        Polygon rval = null;
        rval = polygonUtil.awips1PointReduction(geometry);
        if (rval == null) {
            return (Geometry) geometry.clone();
        }
        return rval;
    }

    /**
     * Attempts to remove interior holes on a polygon. Will take up to 3 passes
     * over the polygon expanding any interior rings and merging rings back in.
     * 
     * @param g
     * @return
     */
    private static Geometry deholeGeometry(Geometry g) {

        List<Geometry> deholedGeometries = new ArrayList<Geometry>();
        for (int i = 0; i < g.getNumGeometries(); i++) {

            if (g.getGeometryN(i) instanceof Polygon) {
                Polygon poly = (Polygon) g.getGeometryN(i);
                poly = deholePolygon(poly);
                deholedGeometries.add(poly);
            } else if (g.getGeometryN(i) instanceof MultiPolygon) {
                MultiPolygon mpoly = (MultiPolygon) g.getGeometryN(i);
                for (int j = 0; j < mpoly.getNumGeometries(); j++) {
                    Polygon poly = (Polygon) mpoly.getGeometryN(j);
                    poly = deholePolygon(poly);
                    deholedGeometries.add(poly);
                }
            }
        }

        return gf.createMultiPolygon(deholedGeometries
                .toArray(new Polygon[deholedGeometries.size()]));
    }

    /**
     * Dehole the polygon
     * 
     * @param p
     * @return
     */
    private static Polygon deholePolygon(Polygon p) {

        int interiorRings = p.getNumInteriorRing();
        int iterations = 0;
        while ((interiorRings > 0) && (iterations < 3)) {
            Geometry[] hucGeometries = new Geometry[interiorRings + 1];
            hucGeometries[0] = p;
            for (int i = 0; i < interiorRings; i++) {
                hucGeometries[i + 1] = p.getInteriorRingN(i).buffer(
                        BUFFER_LEVEL);
            }
            p = (Polygon) gf.createGeometryCollection(hucGeometries).buffer(0);
            iterations++;
            interiorRings = p.getNumInteriorRing();
        }

        return p;
    }
}
