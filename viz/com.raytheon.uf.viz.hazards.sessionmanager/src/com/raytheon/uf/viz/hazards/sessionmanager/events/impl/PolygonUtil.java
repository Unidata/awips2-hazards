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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * Utility for polygon operations
 * 
 * Methods taken from WarnGen: com.raytheon.viz.warngen.gis.PolygonUtil
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 1, 2015  13172      Robert.Blum Initial creation
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class PolygonUtil {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PolygonUtil.class);

    private static final double SIDE_OF_LINE_THRESHOLD = 1e-9;

    private static int N_CANDIDATE_POINTS = 8;

    private static byte[] CANDIDATE_DX = { 1, 1, 1, 0, -1, -1, -1, 0 };

    private static byte[] CANDIDATE_DY = { 1, 0, -1, -1, -1, 0, 1, 1 };

    private final int maxVertices;

    public PolygonUtil(int maxVertices) {
        this.maxVertices = maxVertices;
    }

    /**
     * @return null if the original warningPolygon should be used
     */
    public Polygon awips1PointReduction(Polygon warningPolygon) {
        Coordinate[] vertices = warningPolygon.getCoordinates();
        vertices = Arrays.copyOf(vertices, vertices.length - 1);

        List<Coordinate> points = new ArrayList<Coordinate>(vertices.length);
        for (Coordinate point : vertices) {
            points.add(point);
        }
        reducePoints(points, maxVertices);
        while (points.size() > vertices.length && reducePoints2(points)) {
            reducePoints(points, maxVertices);
        }

        GeometryFactory gf = new GeometryFactory();
        points.add(new Coordinate(points.get(0)));
        Polygon rval = gf.createPolygon(gf.createLinearRing(points
                .toArray(new Coordinate[points.size()])), null);

        if (!rval.isValid()) {
            statusHandler.handle(Priority.DEBUG, String.format(
                    "Polygon %s is invalid.  Attempting to fix...", rval));
            String resultMessage = null;
            try {
                Polygon p2 = (Polygon) rval.buffer(0.0);
                rval = gf.createPolygon((LinearRing) p2.getExteriorRing());
                resultMessage = String.format("  ...fixed.  Result: %s", rval);
            } catch (TopologyException e) {
                resultMessage = "  ...fix failed";
            } catch (ClassCastException e) {
                resultMessage = "  ...resulted in something other than a polygon";
            }
            statusHandler.handle(Priority.DEBUG, resultMessage);
        }

        if (rval.isValid() == false) {
            statusHandler.handle(Priority.DEBUG, "Fixing intersected segments");
            Coordinate[] coords = rval.getCoordinates();
            adjustVertex(coords);
            round(coords, 2);
            coords = removeDuplicateCoordinate(coords);
            coords = removeOverlaidLinesegments(coords);
            rval = gf.createPolygon(gf.createLinearRing(coords), null);
        }
        return rval;
    }

    /**
     * A1 ported point reduction method
     * 
     * @param points
     * @param maxNpts
     * @return
     */
    private void reducePoints(List<Coordinate> points, int maxNpts) {
        Coordinate[] pts = points.toArray(new Coordinate[points.size()]);
        // Find the mean, the point furthest from mean, and the point furthest
        // from that.
        int npts = pts.length;
        double xavg = 0, yavg = 0;
        int[] yesList = new int[npts];
        boolean[] excludeList = new boolean[npts];
        int nyes = 0;
        int k, k1, k2, kn, y, simple;
        double bigDis, maxDis, dis, dx, dy, dx0, dy0, bas;
        for (k = 0; k < npts; k++) {
            xavg += pts[k].x;
            yavg += pts[k].y;
        }
        xavg /= npts;
        yavg /= npts;
        k1 = -1;
        maxDis = 0;
        for (k = 0; k < npts; k++) {
            dx = pts[k].x - xavg;
            dy = pts[k].y - yavg;
            dis = dx * dx + dy * dy;
            if (dis < maxDis) {
                continue;
            }
            maxDis = dis;
            k1 = k;
        }
        k2 = -1;
        maxDis = 0;
        for (k = 0; k < npts; k++) {
            dx = pts[k].x - pts[k1].x;
            dy = pts[k].y - pts[k1].y;
            dis = dx * dx + dy * dy;
            if (dis < maxDis) {
                continue;
            }
            maxDis = dis;
            k2 = k;
        }
        nyes = 2;
        if (k1 < k2) {
            yesList[0] = k1;
            yesList[1] = k2;
        } else {
            yesList[0] = k2;
            yesList[1] = k1;
        }
        dx = pts[k2].x - xavg;
        dy = pts[k2].y - yavg;
        bigDis = Math.sqrt(dx * dx + dy * dy);

        // In each pass we will include that point furthest off the midline
        // of the two points. We will always first consider those that are
        // not simple bends.
        while (nyes < maxNpts && nyes < npts) {
            simple = 1;
            maxDis = 0;
            kn = -1;
            k1 = yesList[nyes - 1];
            for (y = 0; y < nyes; y++) {
                k2 = yesList[y];
                dx0 = pts[k2].x - pts[k1].x;
                dy0 = pts[k2].y - pts[k1].y;
                bas = Math.sqrt(dx0 * dx0 + dy0 * dy0);
                dx0 /= bas;
                dy0 /= bas;
                k = k1;
                while (true) {
                    if (++k >= npts) {
                        k = 0;
                    }
                    if (k == k2) {
                        break;
                    }

                    if (excludeList[k])
                        continue;
                    dx = pts[k].x - pts[k1].x;
                    dy = pts[k].y - pts[k1].y;
                    dis = dx * dx0 + dy * dy0;
                    if (dis < 0) {
                        dis = -dis;
                    } else {
                        dis -= bas;
                    }
                    double newMaxDis = maxDis;
                    int newSimple = simple;
                    if (dis <= 0) {
                        if (simple == 0) {
                            continue;
                        }
                        dis = dx * dy0 - dy * dx0;
                        if (dis < 0) {
                            dis = -dis;
                        }
                    } else if (simple != 0) {
                        newMaxDis = newSimple = 0;
                    }
                    if (dis < newMaxDis) {
                        maxDis = newMaxDis;
                        continue;
                    }
                    if (!checkReducePointsValid(pts, yesList, nyes, k)) {
                        excludeList[k] = true;
                        continue;
                    }
                    simple = newSimple;
                    maxDis = dis;
                    kn = k;
                }
                k1 = k2;
            }

            Arrays.fill(excludeList, false);
            if (kn < 0) {
                statusHandler
                        .debug(String
                                .format("reducePoints(..., %d): Unable to find a valid point\npoints: %s",
                                        maxNpts, points));
                break;
            }

            if (simple != 0 && nyes > 2) {
                if (maxDis * 40 < bigDis) {
                    break;
                }
            }

            for (y = nyes - 1; y >= 0 && kn < yesList[y]; y--) {
                yesList[y + 1] = yesList[y];
            }
            nyes++;
            yesList[y + 1] = kn;
        }

        for (y = 0; y < nyes; y++) {
            k = yesList[y];
            pts[y] = new Coordinate(pts[k]);
        }
        npts = nyes;
        points.clear();
        points.addAll(Arrays.asList(Arrays.copyOf(pts, npts)));
    }

    private boolean checkReducePointsValid(Coordinate[] pts, int[] yesList,
            int nyes, int k) {
        Coordinate[] verts = new Coordinate[nyes + 2];
        int vi = 0;
        for (int i = 0; i < nyes; ++i) {
            if (k >= 0 && k < yesList[i]) {
                verts[vi++] = pts[k];
                k = -1;
            }
            verts[vi++] = pts[yesList[i]];
        }
        if (k >= 0) {
            verts[vi++] = pts[k];
        }
        verts[verts.length - 1] = new Coordinate(verts[0]);
        GeometryFactory gf = new GeometryFactory();
        return gf.createPolygon(verts).isValid();
    }

    /**
     * A1 ported point reduction method 2
     * 
     * @param points
     * @param maxNpts
     * @return
     */
    private boolean reducePoints2(List<Coordinate> points) {
        Coordinate[] pts = points.toArray(new Coordinate[points.size()]);
        int npts = pts.length;
        int i, j, k;
        int best = 0;
        double bestx = -1e10;
        double besty = -1e10;

        // First, determine if the points are ordered in CW or CCW order
        for (i = 0; i < npts; ++i) {
            if (pts[i].y < besty || (pts[i].y == besty && pts[i].x > bestx)) {
                best = i;
                bestx = pts[i].x;
                besty = pts[i].y;
            }
        }

        i = best;

        if (--i < 0) {
            i = npts - 1;
        }
        if ((j = i + 1) >= npts) {
            j -= npts;
        }
        if ((k = j + 1) >= npts) {
            k -= npts;
        }

        double crs = (pts[j].x - pts[i].x) * (pts[k].y - pts[j].y)
                - (pts[j].y - pts[i].y) * (pts[k].x - pts[j].x);

        int orient = crs < 0 ? -1 : (crs > 0 ? 1 : 0);
        if (orient == 0) {
            return false;
        }

        best = -1;
        double besta = 1e10;
        i = 0;

        // find smallest kink
        while (i < npts) {
            if ((j = i + 1) >= npts) {
                j -= npts;
            }
            if ((k = j + 1) >= npts) {
                k -= npts;
            }
            crs = (pts[j].x - pts[i].x) * (pts[k].y - pts[j].y)
                    - (pts[j].y - pts[i].y) * (pts[k].x - pts[j].x);
            if (orient < 0) {
                crs = -crs;
            }

            if (crs < 0) {
                crs = (pts[i].x - pts[j].x) * (pts[k].y - pts[j].y)
                        - (pts[i].y - pts[j].y) * (pts[k].x - pts[j].x);
                if (crs < 0) {
                    crs = -crs;
                }
                double area = 0.5 * crs;
                double dx = pts[k].x - pts[i].x;
                double dy = pts[k].y - pts[i].y;
                double len = Math.sqrt(dx * dx + dy * dy);

                if (area < besta && (area < 1 || area < (0.64 * len))) {
                    best = j;
                    besta = area;
                }
            }
            ++i;
        }
        if (best > 0) {
            points.remove(best);
            --npts;
        }
        return best > 0;
    }

    private void round(Coordinate[] coordinates, int decimalPlaces) {
        for (Coordinate coordinate : coordinates) {
            round(coordinate, decimalPlaces);
        }
    }

    /**
     * round() Rounding coordinates, instead of truncating them.
     * 
     * History 12/06/2012 DR 15559 Qinglu Lin Created.
     */
    private void round(Coordinate coordinate, int decimalPlaces) {
        double x = coordinate.x * Math.pow(10, decimalPlaces);
        double y = coordinate.y * Math.pow(10, decimalPlaces);

        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new IllegalArgumentException("Invalid coordinate "
                    + coordinate);
        }
        x = Math.round(x);
        y = Math.round(y);
        coordinate.x = x / Math.pow(10, decimalPlaces);
        coordinate.y = y / Math.pow(10, decimalPlaces);
    }

    private Coordinate[] removeDuplicateCoordinate(Coordinate[] verts) {
        if (verts == null) {
            return null;
        }
        if (verts.length <= 4) {
            return verts;
        }

        Set<Coordinate> coords = new LinkedHashSet<Coordinate>();
        for (Coordinate c : verts) {
            coords.add(c);
        }
        if ((verts.length - coords.size()) < 2) {
            return verts;
        }
        Coordinate[] vertices = new Coordinate[coords.size() + 1];
        Iterator<Coordinate> iter = coords.iterator();
        int i = 0;
        while (iter.hasNext()) {
            vertices[i] = new Coordinate(iter.next());
            i += 1;
        }
        vertices[i] = new Coordinate(vertices[0]);
        if (vertices.length <= 3) {
            return verts;
        } else {
            return vertices;
        }
    }

    /**
     * computeSlope compute the slope of a line.
     * 
     * History 12/06/2012 DR 15559 Qinglu Lin Created.
     */
    private double computeSlope(Coordinate[] coords, int i) {
        double min = 1.0E-08;
        double slope = 1.0E08;
        double dx = coords[i].x - coords[i + 1].x;
        if (Math.abs(dx) > min) {
            slope = (coords[i].y - coords[i + 1].y) / dx;
        }
        return slope;
    }

    private Coordinate[] removeOverlaidLinesegments(Coordinate[] coords) {
        if (coords.length <= 4) {
            return coords;
        }
        Coordinate[] expandedCoords = null;
        boolean flag = true;
        while (flag) {
            if (coords.length <= 4) {
                return coords;
            }
            expandedCoords = new Coordinate[coords.length + 1];
            flag = false;
            for (int i = 0; i < coords.length; i++) {
                expandedCoords[i] = new Coordinate(coords[i]);
            }
            expandedCoords[expandedCoords.length - 1] = new Coordinate(
                    coords[1]);
            double min = 1.0E-8;
            int m = expandedCoords.length;
            int count = 0;
            double slope = 0.0, slope1 = 0.0;
            for (int i = 0; i < m - 1; i++) {
                slope = computeSlope(expandedCoords, i);
                if (count == 0) {
                    slope1 = slope;
                    count += 1;
                } else {
                    if (Math.abs(Math.abs(slope) - Math.abs(slope1)) <= min) {
                        count += 1;
                    } else {
                        count = 0;
                        slope1 = slope;
                        count += 1;
                    }
                }
                if (count == 2) {
                    // remove the middle point, i.e., that has index of i, of
                    // the three that either form two
                    // overlaid/partial overlaid line segments or is in the
                    // middle
                    // of a straight line segment
                    coords = new Coordinate[coords.length - 1];
                    if (i == m - 2) {
                        for (int j = 1; j <= m - 2; j++) {
                            coords[j - 1] = new Coordinate(expandedCoords[j]);
                        }
                        coords[coords.length - 1] = new Coordinate(coords[0]);
                    } else {
                        for (int j = 0; j < i; j++) {
                            coords[j] = new Coordinate(expandedCoords[j]);
                        }
                        for (int j = i + 1; j < expandedCoords.length - 2; j++) {
                            coords[j - 1] = new Coordinate(expandedCoords[j]);
                        }
                        coords[coords.length - 1] = new Coordinate(coords[0]);
                    }
                    flag = true;
                    break;
                }
            }
        }
        return coords;
    }

    /**
     * Adjust the location of one vertex that cause polygon self-crossing.
     */
    private Coordinate[] adjustVertex(Coordinate[] coord) {
        GeometryFactory gf = new GeometryFactory();
        LinearRing lr;
        Polygon p;
        int length = coord.length;
        Coordinate intersectCoord = null;
        int index[] = new int[6];
        LineSegment ls1, ls2;
        double d[] = new double[6];
        int indexOfTheOtherEnd[] = new int[2];
        boolean isPolygonValid = false;
        outerLoop: for (int skippedSegment = 1; skippedSegment < length - 3; skippedSegment++) {
            for (int i = 0; i < length - 1; i++) {
                index[0] = i;
                index[1] = index[0] + 1;
                index[2] = index[1] + skippedSegment;
                if (index[2] >= length) {
                    index[2] = index[2] - length + 1;
                }
                index[3] = index[2] + 1;
                if (index[3] >= length) {
                    index[3] = index[3] - length + 1;
                }
                ls1 = new LineSegment(coord[index[0]], coord[index[1]]);
                ls2 = new LineSegment(coord[index[2]], coord[index[3]]);
                intersectCoord = ls1.intersection(ls2);
                if (intersectCoord != null) {
                    for (int j = 0; j < index.length - 2; j++) {
                        d[j] = intersectCoord.distance(coord[index[j]]);
                    }
                    if (d[0] < d[1]) {
                        index[4] = index[0];
                        d[4] = d[0];
                        indexOfTheOtherEnd[0] = index[1];
                    } else {
                        index[4] = index[1];
                        d[4] = d[1];
                        indexOfTheOtherEnd[0] = index[0];
                    }
                    if (d[2] < d[3]) {
                        index[5] = index[2];
                        d[5] = d[2];
                        indexOfTheOtherEnd[1] = index[3];
                    } else {
                        index[5] = index[3];
                        d[5] = d[3];
                        indexOfTheOtherEnd[1] = index[2];
                    }
                    // index of the vertex on a line segment (line segment A),
                    // which will be moved along line segment A.
                    int replaceIndex;
                    // index of the vertex at the other end of line segment A.
                    int theOtherIndex;
                    Coordinate b0, b1;
                    if (d[4] < d[5]) {
                        replaceIndex = index[4];
                        theOtherIndex = indexOfTheOtherEnd[0];
                        b0 = coord[index[2]];
                        b1 = coord[index[3]];
                    } else {
                        replaceIndex = index[5];
                        theOtherIndex = indexOfTheOtherEnd[1];
                        b0 = coord[index[0]];
                        b1 = coord[index[1]];
                    }

                    /*
                     * Move the bad vertex (coord[replaceIndex]), which is on
                     * line segment A and has the shortest distance to
                     * intersectCoord, along line segment A to the other side of
                     * line segment B (b0, b1) which intersects with line
                     * segment A.
                     * 
                     * The point is actually moved to the 0.01 grid point
                     * closest to intersectCoord. That point may not actually be
                     * on line segment A.
                     */
                    Coordinate c = adjustVertex2(intersectCoord,
                            coord[theOtherIndex], b0, b1);
                    if (c != null) {
                        coord[replaceIndex].x = c.x;
                        coord[replaceIndex].y = c.y;
                    }

                    round(coord[replaceIndex], 2);
                    if (replaceIndex == 0) {
                        coord[length - 1] = new Coordinate(coord[replaceIndex]);
                    } else if (replaceIndex == length - 1) {
                        coord[0] = new Coordinate(coord[replaceIndex]);
                    }
                    lr = gf.createLinearRing(coord);
                    p = gf.createPolygon(lr, null);
                    isPolygonValid = p.isValid();
                    if (isPolygonValid) {
                        break outerLoop;
                    }
                }
            }
        }
        return coord;
    }

    /**
     * Returns 1, -1, or 0 if p is on the left of, on the right of, or on pa ->
     * pb
     */
    private int sideOfLine(Coordinate p, Coordinate pa, Coordinate pb) {
        double cp = (pb.x - pa.x) * (p.y - pa.y) - (p.x - pa.x) * (pb.y - pa.y); // Cross
                                                                                 // product
        return Math.abs(cp) > SIDE_OF_LINE_THRESHOLD ? (cp < 0 ? -1
                : (cp > 0 ? 1 : 0)) : 0;
    }

    /**
     * Returns the coordinate within one grid point on the 0.01 grid next to
     * intersectCoord that is on the same side of (b0,b1) as 'destination' which
     * has the smallest angle to (inserectCoord,destination). The result may not
     * be exact so it should be passed to round(Coordinate) if used.
     * 
     * If intersectCoord is on a grid point, there are eight candidate points.
     * Otherwise there are four candidates.
     * 
     * Returns null if no point can be found.
     */
    private Coordinate adjustVertex2(Coordinate intersectCoord,
            Coordinate destination, Coordinate b0, Coordinate b1) {
        int sideOfTheOther = sideOfLine(destination, b0, b1);
        if (sideOfTheOther == 0) {
            return null;
        }

        double pxh = intersectCoord.x * 100;
        double pyh = intersectCoord.y * 100;

        double cx = Math.ceil(pxh);
        double fx = Math.floor(pxh);
        double cy = Math.ceil(pyh);
        double fy = Math.floor(pyh);

        double ox, oy;
        if (Math.abs(cx - pxh) < SIDE_OF_LINE_THRESHOLD
                || Math.abs(fx - pxh) < SIDE_OF_LINE_THRESHOLD) {
            cx = fx = pxh;
        }
        if (Math.abs(cy - pyh) < SIDE_OF_LINE_THRESHOLD
                || Math.abs(fy - pyh) < SIDE_OF_LINE_THRESHOLD) {
            cy = fy = pyh;
        }

        Coordinate best = null;
        double bestAngle = Math.PI * 2;

        for (int ci = 0; ci < N_CANDIDATE_POINTS; ++ci) {
            int dx = CANDIDATE_DX[ci];
            int dy = CANDIDATE_DY[ci];

            if (dx == 0) {
                if (cx != fx) {
                    continue;
                }
                ox = pxh;
            } else {
                if (dx > 0) {
                    ox = cx == fx ? pxh + 1 : cx;
                } else {
                    ox = cx == fx ? pxh - 1 : fx;
                }
            }
            if (dy == 0) {
                if (cy != fy) {
                    continue;
                }
                oy = pyh;
            } else {
                if (dy > 0) {
                    oy = cy == fy ? pyh + 1 : cy;
                } else {
                    oy = cy == fy ? pyh - 1 : fy;
                }
            }
            Coordinate c = new Coordinate(ox / 100.0, oy / 100.0);
            if (c != null && sideOfLine(c, b0, b1) == sideOfTheOther) {
                double a = angleBetween(intersectCoord, c, destination);
                if (a < bestAngle) {
                    best = c;
                    bestAngle = a;
                }
            }
        }
        return best;
    }

    /** Returns the angle between p -> pa and p -> pb */
    private double angleBetween(Coordinate p, Coordinate pa, Coordinate pb) {
        double ax = pa.x - p.x;
        double ay = pa.y - p.y;
        double bx = pb.x - p.x;
        double by = pb.y - p.y;
        double m = Math.sqrt((ax * ax + ay * ay) * (bx * bx + by * by));
        return m != 0 ? Math.acos((ax * bx + ay * by) / m) : 0;
    }
}
