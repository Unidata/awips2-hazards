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
package com.raytheon.uf.edex.hazards.interop.gfe;

import java.util.Date;
import java.util.List;

import org.geotools.geometry.jts.JTS;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory;
import com.raytheon.uf.common.dataplugin.gfe.GridDataHistory.OriginType;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridLocation;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GridParmInfo;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.TimeConstraints;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DBit;
import com.raytheon.uf.common.dataplugin.gfe.grid.Grid2DByte;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceData;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceData.CoordinateType;
import com.raytheon.uf.common.dataplugin.gfe.reference.ReferenceID;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.dataplugin.gfe.util.GfeUtil;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.time.TimeRange;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Utility methods to help create and store GFE records.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 27, 2013 2277       jsanchez     Initial creation
 * Feb 18, 2014 2877       bkowal       Added a utility method to convert
 *                                      a hazard geometry to a gfe geometry.
 * Feb 20, 2014 2999       bkowal       Handle geometries consisting of multiple
 *                                      polygons correctly when converting a
 *                                      geometry to a 'gfe geometry'
 * Apr 08, 2014 3357       bkowal       Added additional utility methods to support
 *                                      the updated gfe interoperability matching
 *                                      method.
 * Oct 14, 2015 12494      Chris Golden Reworked to allow hazard types to include
 *                                      only phenomenon (i.e. no significance) where
 *                                      appropriate.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class GFERecordUtil {

    private GFERecordUtil() {

    }

    /**
     * Creates a time range from the start and end time of the event. To fit the
     * time constraints of a GFE grid, the start time is rounded down to the
     * closest hour while the end time is rounded up to the closest hour.
     * 
     * @param startTime
     * @param endTime
     * 
     * @return
     */
    public static TimeRange createGridTimeRange(Date startTime, Date endTime,
            TimeConstraints timeConstraint) {
        timeConstraint.expandTRToQuantum(new TimeRange(startTime, endTime));
        return timeConstraint.expandTRToQuantum(new TimeRange(startTime,
                endTime));
    }

    /**
     * Creates the time range of all the GFE records to be replaced.
     * 
     * @param records
     * @return
     */
    public static TimeRange getReplacementTimeRange(List<GFERecord> records) {
        Date earliest = null;
        Date latest = null;
        for (GFERecord record : records) {
            TimeRange timeRange = record.getTimeRange();
            if (earliest == null || timeRange.getStart().before(earliest)) {
                earliest = timeRange.getStart();
            }

            if (latest == null || timeRange.getEnd().after(latest)) {
                latest = timeRange.getEnd();
            }
        }

        return new TimeRange(earliest, latest);
    }

    /**
     * Converts the hazardEvent to a GFERecord.
     * 
     * @param hazardEvent
     * @param gridParmInfo
     * @return
     * @throws TransformException
     */
    public static GFERecord createGFERecord(IHazardEvent hazardEvent,
            GridParmInfo gridParmInfo) throws TransformException {
        TimeRange timeRange = createGridTimeRange(hazardEvent.getStartTime(),
                hazardEvent.getEndTime(), gridParmInfo.getTimeConstraints());
        ParmID parmID = gridParmInfo.getParmID();
        GFERecord record = new GFERecord(parmID, timeRange);

        // create discrete keys
        String phensig = HazardEventUtilities.getHazardPhenSig(hazardEvent);
        DiscreteKey[] discretekeys = DiscreteGridSliceUtil
                .createSimpleDiscreteKeys(parmID, phensig);

        // create Grid History
        GridDataHistory gdh = new GridDataHistory(OriginType.CALCULATED,
                parmID, timeRange);
        gdh.setUpdateTime(new Date(System.currentTimeMillis()));
        GridDataHistory[] gdha = new GridDataHistory[] { gdh };
        record.setGridHistory(gdha);

        // create the Grid2DByte
        GridLocation gridLocation = gridParmInfo.getGridLoc();
        if (isPointSetClosed(hazardEvent.getProductGeometry().getCoordinates()) == false) {
            return null;
        }
        MultiPolygon polygon = GfeUtil.createPolygon(hazardEvent
                .getProductGeometry().getCoordinates());
        polygon = (MultiPolygon) JTS.transform(polygon, MapUtil
                .getTransformFromLatLon(PixelOrientation.CENTER, gridLocation));
        Grid2DBit grid2DBit = GfeUtil.filledBitArray(polygon, gridLocation);
        Grid2DByte grid2DByte = new Grid2DByte(grid2DBit.getXdim(),
                grid2DBit.getYdim(), grid2DBit.getBuffer());

        // set discrete grid slice in message data
        DiscreteGridSlice gridSlice = new DiscreteGridSlice(timeRange,
                gridParmInfo, gdha, grid2DByte, discretekeys);
        record.setMessageData(gridSlice);

        return record;
    }

    public static Geometry translateHazardPolygonToGfe(
            GridLocation gridLocation, Geometry geometry)
            throws TransformException {
        Geometry gfePolygon = null;

        /*
         * If the geometry is not closed, return the original geometry. Certain
         * GFE geometries consist of a set of points that are not closed.
         */
        if (isPointSetClosed(geometry.getCoordinates()) == false) {
            return geometry;
        }

        if (geometry.getNumGeometries() == 1) {
            MultiPolygon polygon = GfeUtil.createPolygon(geometry
                    .getCoordinates());
            polygon = (MultiPolygon) JTS.transform(polygon, MapUtil
                    .getTransformFromLatLon(PixelOrientation.CENTER,
                            gridLocation));
            Grid2DBit grid2DBit = GfeUtil.filledBitArray(polygon, gridLocation);
            ReferenceData referenceData = new ReferenceData(gridLocation,
                    new ReferenceID("temp"), grid2DBit);

            gfePolygon = referenceData.getPolygons(CoordinateType.LATLON);
        } else {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Geometry multiPolygon = translateHazardPolygonToGfe(
                        gridLocation, geometry.getGeometryN(i));
                if (gfePolygon == null) {
                    gfePolygon = multiPolygon;
                } else {
                    gfePolygon = gfePolygon.union(multiPolygon);
                }
            }
        }

        return gfePolygon;
    }

    public static Grid2DBit translateHazardPolygonGrid2Bit(
            GridLocation gridLocation, Geometry geometry)
            throws TransformException {
        Grid2DBit grid2DBit = null;

        if (geometry.getNumGeometries() == 1) {
            MultiPolygon polygon = GfeUtil.createPolygon(geometry
                    .getCoordinates());
            polygon = (MultiPolygon) JTS.transform(polygon, MapUtil
                    .getTransformFromLatLon(PixelOrientation.CENTER,
                            gridLocation));
            grid2DBit = GfeUtil.filledBitArray(polygon, gridLocation);
        } else {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                Grid2DBit data = translateHazardPolygonGrid2Bit(gridLocation,
                        geometry.getGeometryN(i));
                if (grid2DBit == null) {
                    grid2DBit = data;
                } else {
                    grid2DBit = grid2DBit.or(data);
                }
            }
        }

        return grid2DBit;
    }

    private static boolean isPointSetClosed(Coordinate[] coordinates) {
        GeometryFactory gf = new GeometryFactory();
        LineString ls = gf.createLineString(coordinates);

        return ls.isClosed();
    }
}