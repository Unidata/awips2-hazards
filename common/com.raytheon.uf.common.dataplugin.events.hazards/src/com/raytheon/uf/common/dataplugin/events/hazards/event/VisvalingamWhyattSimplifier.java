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

package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Triangle;

/**
 * A Visvalingam Whyatt Geometry Simplifier.
 * 
 * This Simplifier works for Valid GeometryCollection, Polygon, and MultiPolygon
 * objects. This algorithm is adapted from a Visvalingam-Whyatt simplifier
 * written in javascript and published at:
 * <code>http://web.cs.sunyit.edu/~poissad/projects/Curve/about_algorithms/whyatt</code>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#    Engineer      Description
 * ------------  ---------- -----------   --------------------------
 * Sep 09, 2015  10207      Chris.Cody    Initial creation
 */
public class VisvalingamWhyattSimplifier {

    public static Geometry reduceGeometry(Geometry geometry, int numberOfPoints) {
        Geometry reducedGeometry = null;
        if (geometry instanceof GeometryCollection) {
            reducedGeometry = reduceGeometryCollection(
                    (GeometryCollection) geometry, numberOfPoints);
        } else if (geometry instanceof Polygon) {
            reducedGeometry = reducePolygon((Polygon) geometry, numberOfPoints);
        } else if (geometry instanceof MultiPolygon) {
            reducedGeometry = reduceMultiPolygon((MultiPolygon) geometry,
                    numberOfPoints);
        }

        return (reducedGeometry);
    }

    public static GeometryCollection reduceGeometryCollection(
            GeometryCollection geoCollection, int numberOfPointsPerPolygon) {
        return (reduceGeometryCollection(null, geoCollection,
                numberOfPointsPerPolygon));
    }

    private static GeometryCollection reduceGeometryCollection(
            GeometryFactory gf, GeometryCollection geoCollection,
            int numberOfPointsPerPolygon) {

        GeometryCollection reducedGeoCollection = null;

        if (gf == null) {
            gf = new GeometryFactory();
        }

        int numGeometries = geoCollection.getNumGeometries();

        Geometry[] reducedGeometryArray = new Geometry[numGeometries];

        for (int i = 0; i < numGeometries; i++) {
            Geometry geometry = geoCollection.getGeometryN(i);
            if (geometry instanceof Polygon) {
                Polygon origPolygon = (Polygon) geometry;
                Polygon reducedPolygon = null;
                if (origPolygon.getNumPoints() > numberOfPointsPerPolygon) {
                    reducedPolygon = reducePolygon(gf, origPolygon,
                            numberOfPointsPerPolygon);
                } else {
                    reducedPolygon = origPolygon;
                }
                reducedGeometryArray[i] = reducedPolygon;
            }
        }
        reducedGeoCollection = gf
                .createGeometryCollection(reducedGeometryArray);
        return (reducedGeoCollection);
    }

    public static GeometryCollection reduceMultiPolygon(
            MultiPolygon multiPolygon, int numberOfPoints) {

        return (reduceMultiPolygon(null, multiPolygon, numberOfPoints));
    }

    private static GeometryCollection reduceMultiPolygon(GeometryFactory gf,
            MultiPolygon multiPolygon, int numberOfPointsPerPolygon) {

        GeometryCollection reducedGeoCollection = null;

        if (gf == null) {
            gf = new GeometryFactory();
        }

        int numGeometries = multiPolygon.getNumGeometries();
        Geometry[] reducedGeometryArray = new Geometry[numGeometries];
        for (int n = 0; n < numGeometries; n++) {
            Geometry geometry = multiPolygon.getGeometryN(n);
            Polygon reducedPolygon = null;
            if (geometry instanceof Polygon) {
                Polygon origPolygon = (Polygon) geometry;
                if (origPolygon.getNumPoints() > numberOfPointsPerPolygon) {
                    reducedPolygon = reducePolygon(gf, origPolygon,
                            numberOfPointsPerPolygon);
                } else {
                    reducedPolygon = origPolygon;
                }
                reducedGeometryArray[n] = reducedPolygon;
            }
        }
        reducedGeoCollection = gf
                .createGeometryCollection(reducedGeometryArray);
        return (reducedGeoCollection);
    }

    public static Polygon reducePolygon(Polygon polygon, int numberOfPoints) {
        return (reducePolygon(null, polygon, numberOfPoints));
    }

    private static Polygon reducePolygon(GeometryFactory gf, Polygon polygon,
            int numberOfPoints) {

        Polygon reducedPolygon = null;

        if (gf == null) {
            gf = new GeometryFactory();
        }
        Coordinate[] coordArray = polygon.getCoordinates();
        List<Coordinate> coordinateList = Arrays.asList(coordArray);
        List<Coordinate> reducedCoordinateList = visvalingamWhyattReducer(
                coordinateList, numberOfPoints);

        Coordinate[] reducedCoordinateArray = new Coordinate[reducedCoordinateList
                .size()];
        reducedCoordinateArray = reducedCoordinateList
                .toArray(reducedCoordinateArray);
        reducedPolygon = gf.createPolygon(reducedCoordinateArray);

        return (reducedPolygon);
    }

    private static List<Coordinate> visvalingamWhyattReducer(
            List<Coordinate> coordinateList, int maxNumberOfPoints) {
        List<Coordinate> internalCoordinateList = new ArrayList<>(
                coordinateList);
        int numPointsToRemove = internalCoordinateList.size()
                - maxNumberOfPoints;
        for (int i = 0; i < numPointsToRemove; i++) {
            int minIndex = 1;
            double minArea = Triangle.area(internalCoordinateList.get(0),
                    internalCoordinateList.get(1),
                    internalCoordinateList.get(2));
            for (int j = 2; j < (internalCoordinateList.size() - 1); j++) {
                double area = Triangle.area(internalCoordinateList.get(j - 1),
                        internalCoordinateList.get(j),
                        internalCoordinateList.get(j + 1));
                if (area < minArea) {
                    minIndex = j;
                    minArea = area;
                }
            }
            internalCoordinateList.remove(minIndex);
        }
        return internalCoordinateList;
    }

}
