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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.Puntal;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.operation.valid.IsValidOp;

/**
 * Description: Wrapper for a {@link Geometry}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 02, 2016   15934    Chris.Golden Initial creation.
 * Sep 29, 2016   15928    Chris.Golden Made scaleable, switched to use rotation
 *                                      angles in radians, added methods to get
 *                                      rotated or scaled copies, and added
 *                                      calculation of center point.
 * Oct 12, 2016   15928    Chris.Golden Changed behavior to allow resizing
 *                                      to cause geometries to flip over the
 *                                      appropriate axis if the user crosses
 *                                      that axis while resizing.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = AdvancedGeometrySerializationAdapter.class)
public class GeometryWrapper implements IRotatable, IScaleable {

    // Private Classes

    /**
     * Serializable array of bytes.
     */
    private class SerializableBytes implements Serializable {

        // Private Static Constants

        /**
         * Serialization version UID.
         */
        private static final long serialVersionUID = -5954561615947845039L;

        // Private Variables

        /**
         * Array of bytes.
         */
        private byte[] bytes;

        // Public Constructors

        /**
         * Construct an instance.
         */
        public SerializableBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        // Public Methods

        /**
         * Get the underlying byte array.
         * 
         * @return Byte array.
         */
        public byte[] getBytes() {
            return bytes;
        }

        // Private Methods

        /**
         * Write out the object for serialization purposes. The length of the
         * byte array is written, then the array itself if not zero-length.
         * 
         * @param stream
         *            Stream to which to write out the object.
         * @throws IOException
         *             If the object cannot be written out.
         */
        private void writeObject(ObjectOutputStream stream) throws IOException {
            if ((bytes == null) || (bytes.length == 0)) {
                stream.writeInt(0);
            } else {
                stream.writeInt(bytes.length);
                stream.write(bytes);
            }
        }

        /**
         * Read in the object for deserialization purposes. The length of the
         * byte array is read, then the array itself if the length is not zero.
         * 
         * @param stream
         *            Stream from which to read in the object.
         * @throws IOException
         *             If the object cannot be read in.
         * @throws ClassNotFoundException
         *             If the class of a serialized object cannot be found.
         */
        private void readObject(ObjectInputStream stream) throws IOException,
                ClassNotFoundException {

            /*
             * Read in the length needed, and then if it is greater than zero,
             * read in the bytes. Multiple passes may be needed to read in the
             * entire buffer, as the stream's read() methods are not guaranteed
             * to return all the bytes in one pass.
             */
            int length = stream.readInt();
            bytes = new byte[length];
            if (length > 0) {
                for (int count = 0, thisCount = 0; count < length; count += thisCount) {
                    thisCount = stream.read(bytes, count, length - count);
                }
            }
        }
    }

    // Private Static Constants

    /**
     * Well-Known Binary reader, used for deserializing geometries. It is
     * thread-local because <code>WKBReader</code> is not explicitly declared to
     * be thread-safe. This class's static methods may be called simultaneously
     * by multiple threads, and each thread must be able to deserialize
     * geometries separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<WKBReader> WKB_READER = new ThreadLocal<WKBReader>() {

        @Override
        protected WKBReader initialValue() {
            return new WKBReader();
        }
    };

    /**
     * Well-Known Binary writer, used for serializing geometries. It is
     * thread-local because <code>WKBWriter</code> is not explicitly declared to
     * be thread-safe. This class's static methods may be called simultaneously
     * by multiple threads, and each thread must be able to serialize geometries
     * separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<WKBWriter> WKB_WRITER = new ThreadLocal<WKBWriter>() {

        @Override
        protected WKBWriter initialValue() {
            return new WKBWriter();
        }
    };

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 205008170162262896L;

    // Private Variables

    /**
     * Geometry.
     * <p>
     * <strong>NOTE</strong>: This field would be declared <code>final</code> if
     * it did not need to be altered by {@link #readObject(ObjectInputStream)}.
     * </p>
     */
    private Geometry geometry;

    /**
     * Center point of the bounding box of the shape, around which any rotation
     * is done.
     * <p>
     * <strong>NOTE</strong>: This field would be declared <code>final</code> if
     * it did not need to be altered by {@link #readObject(ObjectInputStream)}.
     * </p>
     */
    private Coordinate centerPoint;

    /**
     * Rotation in counterclockwise radians.
     * <p>
     * <strong>NOTE</strong>: This field would be declared <code>final</code> if
     * it did not need to be altered by {@link #readObject(ObjectInputStream)}.
     * </p>
     */
    private double rotation;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param geometry
     *            Geometry defining the shape; cannot be <code>null</code>.
     * @param rotation
     *            Rotation in counterclockwise radians.
     */
    @JsonCreator
    public GeometryWrapper(@JsonProperty("geometry") Geometry geometry,
            @JsonProperty("rotation") double rotation) {
        this.geometry = geometry;

        /*
         * Ensure rotation is between 0 (inclusive) and 360 (exclusive).
         */
        this.rotation = (rotation + (2.0 * Math.PI)) % (2.0 * Math.PI);

        /*
         * Unrotate the geometry around an arbitrary point to get its bounding
         * box with sides parallel to the X and Y axes, then take the center
         * point of the bounding box and rotate it back around the same point to
         * get the center point.
         */
        Coordinate firstPoint = geometry.getCoordinate();
        Geometry unrotatedGeometry = AffineTransformation.rotationInstance(
                rotation * -1.0, firstPoint.x, firstPoint.y)
                .transform(geometry);
        centerPoint = unrotatedGeometry.getEnvelopeInternal().centre();
        AffineTransformation.rotationInstance(rotation, firstPoint.x,
                firstPoint.y).transform(centerPoint, centerPoint);
    }

    // Private Constructors

    /**
     * Construct a standard instance, with the center point explictly specified
     * instead of being calculated by the constructor.
     * 
     * @param geometry
     *            Geometry defining the shape; cannot be <code>null</code>.
     * @param rotation
     *            Rotation in counterclockwise radians; must be in the range
     *            <code>[0, 2 * Pi)</code>.
     */
    private GeometryWrapper(Geometry geometry, Coordinate centerPoint,
            double rotation) {
        this.geometry = geometry;
        this.centerPoint = centerPoint;
        this.rotation = rotation;
    }

    // Public Methods

    /**
     * Get the geometry.
     * 
     * @return Geometry.
     */
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public Coordinate getCenterPoint() {
        return new Coordinate(centerPoint);
    }

    @Override
    public double getRotation() {
        return rotation;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GeometryWrapper == false) {
            return false;
        }
        GeometryWrapper otherGeometryWrapper = (GeometryWrapper) other;
        return (((geometry == otherGeometryWrapper.geometry) || ((geometry != null)
                && (otherGeometryWrapper.geometry != null) && geometry
                    .equals(otherGeometryWrapper.geometry))) && (rotation == otherGeometryWrapper.rotation));
    }

    @Override
    public int hashCode() {
        return (int) (((geometry == null ? 0L : (double) geometry.hashCode()) + (Double
                .valueOf(rotation).hashCode())) % Integer.MAX_VALUE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <G extends IAdvancedGeometry> G copyOf() {
        return (G) new GeometryWrapper((Geometry) geometry.clone(),
                new Coordinate(centerPoint), rotation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <G extends IRotatable> G rotatedCopyOf(double delta) {

        /*
         * Just return a straight copy if no delta was given.
         */
        if (delta == 0.0) {
            return copyOf();
        }

        /*
         * Rotate the geometry by the specified delta.
         */
        Geometry geometry = AffineTransformation.rotationInstance(delta,
                centerPoint.x, centerPoint.y).transform(getGeometry());

        /*
         * Return a new instance with the rotated geometry and the altered
         * rotation angle.
         */
        return (G) new GeometryWrapper(geometry, new Coordinate(centerPoint),
                (rotation + delta + (2.0 * Math.PI) % (2.0 * Math.PI)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <G extends IScaleable> G scaledCopyOf(double horizontalMultiplier,
            double verticalMultiplier) {

        /*
         * Sanity check the parameters.
         */
        if ((horizontalMultiplier == 0.0) || (verticalMultiplier == 0.0)) {
            throw new IllegalArgumentException(
                    "scale multipliers must be non-zero numbers");
        }

        /*
         * Just return a straight copy if no change in scale was specified.
         */
        if ((horizontalMultiplier == 1.0) && (verticalMultiplier == 1.0)) {
            return copyOf();
        }

        /*
         * Compose a transform that unrotates the geometry, centers the geometry
         * on the origin, applies the scaling (which is always centered on the
         * origin, thus the translation), recenters the geometry on its actual
         * center point, and rotates it back to the original rotation.
         */
        AffineTransformation transformer = AffineTransformation
                .rotationInstance(rotation * -1.0, centerPoint.x, centerPoint.y);
        transformer.translate(centerPoint.x * -1.0, centerPoint.y * -1.0);
        transformer.scale(horizontalMultiplier, verticalMultiplier);
        transformer.translate(centerPoint.x, centerPoint.y);
        transformer.rotate(rotation, centerPoint.x, centerPoint.y);

        /*
         * Return a new instance with the transformed geometry.
         */
        return (G) new GeometryWrapper(transformer.transform(geometry),
                new Coordinate(centerPoint), rotation);
    }

    @Override
    public boolean isPunctual() {
        return (geometry instanceof Puntal);
    }

    @Override
    public boolean isLineal() {
        return (geometry instanceof Lineal);
    }

    @Override
    public boolean isPolygonal() {
        return (geometry instanceof Polygonal);
    }

    @Override
    public boolean isPotentiallyCurved() {
        return false;
    }

    /**
     * Get the wrapped geometry. This implementation simply calls
     * {@link #getGeometry()}, ignoring the passed-in parameters.
     */
    @Override
    public Geometry asGeometry(GeometryFactory geometryFactory,
            double flatness, int limit) {
        return getGeometry();
    }

    @Override
    public Coordinate getCentroid(GeometryFactory geometryFactory,
            double flatness, int limit) {
        return geometry.getCentroid().getCoordinate();
    }

    @Override
    public boolean isValid() {
        return ((geometry != null) && geometry.isValid());
    }

    @Override
    public String getValidityProblemDescription() {
        if (isValid()) {
            return null;
        }
        return new IsValidOp(geometry).getValidationError().getMessage();
    }

    @Override
    public String toString() {
        return geometry.toString();
    }

    // Private Methods

    /**
     * Write out the object for serialization purposes. This is required because
     * the {@link Geometry} object found within {@link #geometry} cannot easily
     * be deserialized (sometimes resulting in {@link ClassNotFoundException}
     * being thrown).
     * 
     * @param stream
     *            Stream to which to write out the object.
     * @throws IOException
     *             If the object cannot be written out.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(new SerializableBytes(WKB_WRITER.get().write(
                geometry)));
        stream.writeObject(centerPoint);
        stream.writeDouble(rotation);
    }

    /**
     * Read in the object for deserialization purposes. This is required because
     * the {@link Geometry} object found within {@link #geometry} cannot easily
     * be deserialized (sometimes resulting in {@link ClassNotFoundException}
     * being thrown).
     * 
     * @param stream
     *            Stream from which to read in the object.
     * @throws IOException
     *             If the object cannot be read in.
     * @throws ClassNotFoundException
     *             If the class of a serialized object cannot be found.
     */
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        SerializableBytes bytes = (SerializableBytes) stream.readObject();
        try {
            geometry = WKB_READER.get().read(bytes.getBytes());
        } catch (ParseException e) {
            throw new IOException("could not read in geometry", e);
        }
        centerPoint = (Coordinate) stream.readObject();
        rotation = stream.readDouble();
    }
}
