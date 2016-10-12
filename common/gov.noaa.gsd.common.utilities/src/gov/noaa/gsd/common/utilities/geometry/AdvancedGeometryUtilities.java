/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities.geometry;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.AffineTransformation;

/**
 * Description: Utilities for creating and modifying {@link IAdvancedGeometry}
 * instances, as well as extracting {@link Geometry} objects from them.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 08, 2016   15934    Chris.Golden Initial creation.
 * Sep 21, 2016   15934    Chris.Golden Changed to provide more flexibility in
 *                                      terms of JTS geometries having the option
 *                                      of being wrapped in a GeometryCollection,
 *                                      instead of the latter being mandatory.
 * Sep 29, 2016   15928    Chris.Golden Added methods for closing and opening
 *                                      lists of coordinates, getting coordinates
 *                                      of multi-ringed polygons, and getting the
 *                                      rotated bounding box of a geometry.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class AdvancedGeometryUtilities {

    // Private Static Constants

    /**
     * Flattening maximum deviation for latitude-longitude space. This is used
     * when flattening a curve existing in lat-lon space into one or more
     * straight line segments to determine roughly how far any given straight
     * line segment may deviate from the curve it is simulating.
     */
    private static final double LAT_LON_FLATTENING_MAXIMUM_DEVIATION = 0.001;

    /**
     * Maximum level of recursion allowed when generating straight line segments
     * for the flattening of a curve existing in lat-lon space when generating a
     * JTS geometry. The maximum number of line segments generated for a given
     * curve will be approximately 2<sup><i>n</i></sup> where <i>n</i> is this
     * value.
     */
    private static final int LAT_LON_FLATTENING_RECURSION_LIMIT_FOR_GEOMETRY = 8;

    /**
     * Maximum level of recursion allowed when generating straight line segments
     * for the flattening of a curve existing in lat-lon space when determining
     * a geometry's centroid. The maximum number of line segments generated for
     * a given curve will be approximately 2<sup><i>n</i></sup> where <i>n</i>
     * is this value. This value is lower than the one used for
     * {@link #LAT_LON_FLATTENING_RECURSION_LIMIT_FOR_GEOMETRY} because a less
     * accurate pseudo-curve is needed when calculating the approximate
     * centroid.
     */
    private static final int LAT_LON_FLATTENING_RECURSION_LIMIT_FOR_CENTROID = 4;

    /**
     * Geometry factory, used to create {@link Geometry} instances.. It is
     * thread-local because <code>GeometryFactory</code> is not explicitly
     * declared to be thread-safe. Since this factory could be used
     * simultaneously by multiple threads, and each thread must be able to
     * create geometries separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<GeometryFactory> GEOMETRY_FACTORY = new ThreadLocal<GeometryFactory>() {

        @Override
        protected GeometryFactory initialValue() {
            return new GeometryFactory();
        }
    };

    // Public Static Methods

    /**
     * Get a geometry factory that is safe to use for the current thread.
     * 
     * @return Geometry factory.
     */
    public static GeometryFactory getGeometryFactory() {
        return GEOMETRY_FACTORY.get();
    }

    /**
     * Get a JTS geometry from the specified advanced geometry.
     * 
     * @param advancedGeometry
     *            Advanced geometry from which to generate the geometry.
     * @return JTS geometry, or <code>null</code> if no advanced geometry was
     *         provided.
     */
    public static Geometry getJtsGeometry(IAdvancedGeometry advancedGeometry) {
        return (advancedGeometry == null ? null : advancedGeometry.asGeometry(
                GEOMETRY_FACTORY.get(), LAT_LON_FLATTENING_MAXIMUM_DEVIATION,
                LAT_LON_FLATTENING_RECURSION_LIMIT_FOR_GEOMETRY));
    }

    /**
     * Get a JTS geometry from the specified advanced geometry. The result is
     * always a {@link GeometryCollection}, even if it only holds one
     * {@link Geometry}.
     * <p>
     * TODO: Why is it necessary to return a collection? Hazard Services code
     * seems to assume in many places that all {@link Geometry} objects are
     * collections.
     * </p>
     * 
     * @param advancedGeometry
     *            Advanced geometry from which to generate the geometry.
     * @return JTS geometry collection, or <code>null</code> if no advanced
     *         geometry was provided.
     */
    public static Geometry getJtsGeometryAsCollection(
            IAdvancedGeometry advancedGeometry) {
        return getJtsGeometryAsCollection(getJtsGeometry(advancedGeometry));
    }

    /**
     * Get the approximate centroid from the specified advanced geometry.
     * 
     * @param advancedGeometry
     *            Advanced geometry from which to get the centroid.
     * @return Centroid.
     */
    public static Coordinate getCentroid(IAdvancedGeometry advancedGeometry) {
        return advancedGeometry.getCentroid(GEOMETRY_FACTORY.get(),
                LAT_LON_FLATTENING_MAXIMUM_DEVIATION,
                LAT_LON_FLATTENING_RECURSION_LIMIT_FOR_CENTROID);
    }

    /**
     * Get the corners of the bounding box around the specified advanced
     * geometry.
     * 
     * @param advancedGeometry
     *            Advanced geometry that is rotatable and/or scaleable.
     * @return List of corner points of the bounding box, in latitude-longitude
     *         coordinates, in the order
     *         <code>[lowerRight, upperRight, upperLeft, lowerLeft]</code>. The
     *         corner points are rotated around the centroid of the geometry by
     *         the amount that the geometry is rotated, if the latter is an
     *         {@link IRotatable} instance.
     */
    public static List<Coordinate> getBoundingBoxCornerPoints(
            IAdvancedGeometry advancedGeometry) {

        /*
         * Get the JTS geometry version.
         */
        Geometry geometry = getJtsGeometry(advancedGeometry);

        /*
         * If the geometry has inherent rotation, counter-rotate it back to an
         * angle of zero prior to calculating the bounding box, and create an
         * affine transformation to be used to re-rotate the bounding box back
         * after said box is calculated.
         */
        AffineTransformation rotationTransformation = null;
        if (advancedGeometry instanceof IRotatable) {
            IRotatable rotatableGeometry = (IRotatable) advancedGeometry;
            double rotation = rotatableGeometry.getRotation();
            if (rotation != 0.0) {
                Coordinate centerPoint = rotatableGeometry.getCenterPoint();
                geometry = AffineTransformation.rotationInstance(
                        rotation * -1.0, centerPoint.x, centerPoint.y)
                        .transform(geometry);
                rotationTransformation = AffineTransformation.rotationInstance(
                        rotation, centerPoint.x, centerPoint.y);
            }
        }

        /*
         * Calculate the bounding box, and record the four corner points, in the
         * order lower-right, upper-right, upper-left, and lower-left.
         */
        Envelope envelope = geometry.getEnvelopeInternal();
        List<Coordinate> coordinates = Lists.newArrayList(new Coordinate(
                envelope.getMaxX(), envelope.getMinY()), new Coordinate(
                envelope.getMaxX(), envelope.getMaxY()), new Coordinate(
                envelope.getMinX(), envelope.getMaxY()), new Coordinate(
                envelope.getMinX(), envelope.getMinY()));

        /*
         * If re-rotation must be done, rotate the calculated corner points back
         * to the locations they must have to bound the geometry at its actual
         * rotation.
         */
        if (rotationTransformation != null) {
            for (int j = 0; j < coordinates.size(); j++) {
                rotationTransformation.transform(coordinates.get(j),
                        coordinates.get(j));
            }
        }

        return coordinates;
    }

    /**
     * Get the coordinates of the specified geometry. It is assumed that the
     * geometry is not a {@link GeometryCollection}.
     * 
     * @param geometry
     *            Geometry for which to get the coordinates.
     * @return List of arrays of coordinates, with at least one such array in
     *         the list. If just one array is included, it holds the coordinates
     *         of the geometry. If more than one is provided, then the geometry
     *         is a {@link Polygon}; the first holds the coordinates of the
     *         exterior shell of the polygon, while any other arrays hold the
     *         coordinates of the holes in the polygon.
     */
    public static List<Coordinate[]> getCoordinates(Geometry geometry) {
        List<Coordinate[]> coordinates = null;
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            coordinates = new ArrayList<>(polygon.getNumInteriorRing() + 1);
            Coordinate[] ringCoordinates = polygon.getExteriorRing()
                    .getCoordinates();
            coordinates.add(ringCoordinates);
            for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                ringCoordinates = polygon.getInteriorRingN(j).getCoordinates();
                coordinates.add(ringCoordinates);
            }
        } else {
            coordinates = new ArrayList<>(1);
            Coordinate[] ringCoordinates = geometry.getCoordinates();
            coordinates.add(ringCoordinates);
        }
        return coordinates;
    }

    /**
     * If the specified list of coordinates does not have the same first and
     * last point, duplicate the first point and append it to the list. This is
     * required because JTS {@link LinearRing} (used in {@link Polygon}
     * instances) always have the same first and last coordinates.
     * 
     * @param coordinates
     *            List of coordinates to be modified if necessary.
     */
    public static void addDuplicateLastCoordinate(List<Coordinate> coordinates) {
        if (coordinates.size() > 1) {
            Coordinate firstPoint = coordinates.get(0);
            if (firstPoint.equals(coordinates.get(coordinates.size() - 1)) == false) {
                coordinates.add((Coordinate) firstPoint.clone());
            }
        }
    }

    /**
     * If the specified list of coordinates has the same point at the end as it
     * does at the beginning, remove the last point.
     * 
     * @param coordinates
     *            List of coordinates to be modified if necessary.
     */
    public static void removeDuplicateLastCoordinate(
            List<Coordinate> coordinates) {
        if ((coordinates.size() > 1)
                && (coordinates.get(0).equals(coordinates.get(coordinates
                        .size() - 1)))) {
            coordinates.remove(coordinates.size() - 1);
        }
    }

    /**
     * Ensure that the specified geometry is a collection.
     * <p>
     * TODO: Why is this necessary? Hazard Services code seems to assume in many
     * places that all {@link Geometry} objects are collections.
     * </p>
     * 
     * @param geometry
     *            Geometry to be checked.
     * @return Geometry as a collection, if it was not already so, or
     *         <code>null</code> if no geometry was specified.
     */
    public static Geometry getJtsGeometryAsCollection(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof GeometryCollection == false) {
            return GEOMETRY_FACTORY.get().createGeometryCollection(
                    new Geometry[] { geometry });
        }
        return geometry;
    }

    /**
     * Create an ellipse.
     * 
     * @param x
     *            Longitude coordinate of the center point.
     * @param y
     *            Latitude coordinate of the center point.
     * @param width
     *            Width in units given by <code>units</code>.
     * @param height
     *            Height in units given by <code>units</code>.
     * @param units
     *            Units for <code>width</code> and <code>height</code>; must not
     *            be <code>null</code>.
     * @param rotation
     *            Rotation in counterclockwise radians.
     * @return Advanced geometry that was created.
     */
    public static IAdvancedGeometry createEllipse(double x, double y,
            double width, double height, LinearUnit units, double rotation) {
        return new Ellipse(new Coordinate(x, y, 0.0), width, height, units,
                rotation);
    }

    /**
     * Create a wrapped geometry. Note that if the specified geometry is a
     * {@link GeometryCollection}, multiple wrapped geometries may be created
     * and returned as a collection.
     * 
     * @param geometry
     *            Geometry defining the shape; cannot be <code>null</code> or an
     *            empty <code>GeometryCollection</code>.
     * @param rotation
     *            Rotation in counterclockwise radians.
     * @return Advanced geometry that was created.
     */
    public static IAdvancedGeometry createGeometryWrapper(Geometry geometry,
            double rotation) {

        /*
         * If the geometry is a collection, flatten it if necessary.
         */
        if (geometry instanceof GeometryCollection) {
            geometry = getFlattenedGeometryCollection((GeometryCollection) geometry);
        }

        /*
         * If the geometry is a collection, create a wrapper for each geometry
         * within the collection, and then return a collection of the wrappers.
         * Otherwise, just create a single wrapper.
         */
        if (geometry instanceof GeometryCollection) {
            List<IAdvancedGeometry> geometryWrappers = new ArrayList<>(
                    geometry.getNumGeometries());
            for (int j = 0; j < geometry.getNumGeometries(); j++) {
                geometryWrappers.add(new GeometryWrapper(geometry
                        .getGeometryN(j), rotation));
            }
            return createCollection(geometryWrappers);
        }
        return new GeometryWrapper(geometry, rotation);
    }

    /**
     * Create a collection holding the specified advanced geometries. If any of
     * the latter are themselves collections, their component subgeometries are
     * made part of this collection, that is, the resulting collection should
     * have no subcollections.
     * 
     * @param advancedGeometries
     *            Advanced geometries to be included in the collection.
     * @return Advanced geometry that was created.
     */
    public static IAdvancedGeometry createCollection(
            List<IAdvancedGeometry> advancedGeometries) {

        /*
         * Iterate through the geometries, compiling a list of any that are not
         * collections, or the children of any that are collections.
         */
        List<IAdvancedGeometry> leafGeometries = null;
        for (int j = 0; j < advancedGeometries.size(); j++) {
            IAdvancedGeometry geometry = advancedGeometries.get(j);
            if (geometry instanceof AdvancedGeometryCollection) {
                if (leafGeometries == null) {
                    leafGeometries = new ArrayList<>(
                            advancedGeometries.subList(0, j));
                }
                leafGeometries.addAll(((AdvancedGeometryCollection) geometry)
                        .getChildren());
            } else if (leafGeometries != null) {
                leafGeometries.add(geometry);
            }
        }
        return new AdvancedGeometryCollection(
                leafGeometries != null ? leafGeometries : advancedGeometries);
    }

    /**
     * Create a collection holding the specified advanced geometries. If any of
     * the latter are themselves collections, their component subgeometries are
     * made part of this collection, that is, the resulting collection should
     * have no subcollections.
     * 
     * @param advancedGeometries
     *            One or more advanced geometries to be included in the
     *            collection. Any of these may be collections, but said
     *            collections' may not themselves be collections.
     * @return Advanced geometry that was created.
     */
    public static IAdvancedGeometry createCollection(
            IAdvancedGeometry... advancedGeometries) {
        return createCollection(Lists.newArrayList(advancedGeometries));
    }

    // Private Static Methods

    /**
     * Flatten the specified geometry collection, returning a single
     * non-collection geometry if the collection contains only one "leaf"
     * geometry, or a collection of leaf geometries gathered from any nested
     * collections.
     * 
     * @param geometry
     *            Geometry collection.
     * @return Flattened geometry collection, or single geometry if only one
     *         leaf geometry was found.
     */
    private static Geometry getFlattenedGeometryCollection(
            GeometryCollection geometry) {

        /*
         * Collect the leaf geometries, that is, those that are not
         * GeometryCollection instances, while traversing the tree of geometry
         * collections.
         */
        List<Geometry> leafGeometries = new LinkedList<>();
        Deque<GeometryCollection> collections = new LinkedList<>();
        collections.push(geometry);
        boolean nestedCollection = false;
        while (collections.isEmpty() == false) {
            GeometryCollection collection = collections.pop();
            for (int j = 0; j < collection.getNumGeometries(); j++) {
                Geometry subGeometry = collection.getGeometryN(j);
                if (subGeometry instanceof GeometryCollection) {
                    nestedCollection = true;
                    collections.push((GeometryCollection) subGeometry);
                } else {
                    leafGeometries.add(subGeometry);
                }
            }
        }

        /*
         * If only one leaf geometry was found, return it; otherwise, if there
         * were no nested collections found, return the original geometry;
         * otherwise, return a new geometry collection holding the leaves.
         */
        if (leafGeometries.size() == 1) {
            return leafGeometries.iterator().next();
        }
        if (nestedCollection == false) {
            return geometry;
        }
        return GEOMETRY_FACTORY.get().createGeometryCollection(
                leafGeometries.toArray(new Geometry[leafGeometries.size()]));
    }

    // Private Constructors

    /**
     * Private constructor preventing creation of an instance.
     */
    private AdvancedGeometryUtilities() {
    }
}
