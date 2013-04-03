/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.utilities;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.geotools.data.DefaultQuery;
import org.geotools.data.shapefile.indexed.IndexType;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Reads a shapefile and returns it to the caller as a JTS Geometry.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class ShapefileReader {
    public static Geometry readShapefile(String shapeFilePath)
            throws IOException {
        List<Coordinate> floodAreaCoordinates = Lists.newArrayList();
        File shapeFile = new File(shapeFilePath);
        FeatureIterator<SimpleFeature> featureIterator = null;

        IndexedShapefileDataStore ds = new IndexedShapefileDataStore(shapeFile
                .toURI().toURL(), null, true, true, IndexType.QIX);

        String[] types = ds.getTypeNames();

        DefaultQuery query = new DefaultQuery();
        query.setTypeName(types[0]);

        query.setFilter(Filter.INCLUDE);

        featureIterator = ds.getFeatureSource().getFeatures(query).features();

        // List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();

        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();

            // May need to add a check for geometry type in the future.
            // The files for this prototype contain shapefile Point geometries.
            Object defaultGeometry = feature.getDefaultGeometry();

            if ((defaultGeometry != null) && (defaultGeometry instanceof Point)) {
                Point point = (Point) defaultGeometry;
                Coordinate c = point.getCoordinate();

                // Evil ... the shapefile coordinates are flipped from
                // what JTS expects.
                floodAreaCoordinates.add(new Coordinate(c.y, c.x));
            }

        }

        // Make sure the polygon is closed.
        // Otherwise, the createLinearRing method will blow up.
        floodAreaCoordinates.add(floodAreaCoordinates.get(0));
        GeometryFactory gf = new GeometryFactory();
        Geometry floodAreaPolygon = gf.createLinearRing(floodAreaCoordinates
                .toArray(new Coordinate[0]));

        return floodAreaPolygon;
    }

}
