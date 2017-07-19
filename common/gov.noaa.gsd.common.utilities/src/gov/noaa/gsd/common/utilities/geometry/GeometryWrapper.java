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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.Puntal;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.operation.valid.IsValidOp;

import gov.noaa.gsd.common.utilities.IBinarySerializable;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator.ByteOrder;
import gov.noaa.gsd.common.utilities.SerializableBytes;

/**
 * Description: Wrapper for a {@link Geometry}. Note that this class is
 * serializable both in the conventional sense (implementing
 * {@link Serializable}, and in the byte stream sense (implementing
 * {@link IBinarySerializable}).
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
 * Oct 13, 2016   15928    Chris.Golden Fixed bug caused by serialization problems
 *                                      with NaN in coordinates.
 * Feb 13, 2017   28892    Chris.Golden Changed to implement IBinarySerializable.
 *                                      Also removed SerializableBytes as an inner
 *                                      class and made it its own class, as it
 *                                      could be reused elsewhere. As per Effective
 *                                      Java book's recommendation, used a
 *                                      serialization proxy to allow the member
 *                                      variables to be final.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = AdvancedGeometrySerializationAdapter.class)
public class GeometryWrapper implements IRotatable, IScaleable {

    // Private Static Classes

    /**
     * Serialization proxy for instances of this class. Among other things, use
     * of a proxy in this manner allows an enclosing class that requires custom
     * serialization/deserialization to have its fields declared
     * <code>final</code> instead of having them be mutable merely for the sake
     * of implementing <code>readObject()</code>, or having to play reflection
     * tricks or use the <code>sun.misc.Unsafe</code> class to get around
     * assignment to <code>final</code> fields.
     */
    private static class SerializationProxy implements Serializable {

        // Private Static Constants

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1977940569704025471L;

        // Private Variables

        /**
         * Byte representation of enclosing class's {@link #geometry}.
         */
        private final SerializableBytes geometryBytes;

        /**
         * Center point.
         */
        private final Coordinate centerPoint;

        /**
         * Rotation.
         */
        private final double rotation;

        // Package-Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param geometryWrapper
         *            Geometry wrapper.
         */
        SerializationProxy(GeometryWrapper geometryWrapper) {
            geometryBytes = new SerializableBytes(
                    WKB_WRITER.get().write(geometryWrapper.geometry));
            centerPoint = geometryWrapper.centerPoint;
            rotation = geometryWrapper.rotation;
        }

        // Private Methods

        /**
         * In response to deserialization, return an instance of the enclosing
         * class instead of the object of this type that was deserialized.
         * 
         * @return Instance of the enclosing class that is generated from this
         *         object, which in turn was just deserialized.
         */
        private Object readResolve() throws ObjectStreamException {
            try {
                return new GeometryWrapper(
                        WKB_READER.get().read(geometryBytes.getBytes()),
                        centerPoint, rotation);
            } catch (ParseException e) {
                throw new InvalidObjectException(
                        "unable to parse wrapped JTS geometry: " + e);
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
    private static final long serialVersionUID = -4044983979872008566L;

    // Private Variables

    /**
     * Geometry.
     */
    private final Geometry geometry;

    /**
     * Center point of the bounding box of the shape, around which any rotation
     * is done.
     */
    private final Coordinate centerPoint;

    /**
     * Rotation in counterclockwise radians.
     */
    private final double rotation;

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
        Geometry unrotatedGeometry = AffineTransformation
                .rotationInstance(rotation * -1.0, firstPoint.x, firstPoint.y)
                .transform(geometry);
        Coordinate center = unrotatedGeometry.getEnvelopeInternal().centre();
        centerPoint = new Coordinate(center.x, center.y, 0.0);
        AffineTransformation
                .rotationInstance(rotation, firstPoint.x, firstPoint.y)
                .transform(centerPoint, centerPoint);
    }

    /**
     * Construct an instance by deserializing from the specified input stream.
     * It is assumed that the serialization that is being deserialized was
     * produced by {@link #toBinary(OutputStream)}. This constructor must be
     * included because this class implements {@link IBinarySerializable}.
     * 
     * @param bytesInputStream
     *            Byte array input stream from which to deserialize.
     * @throws IOException
     *             If a deserialization error occurs.
     */
    public GeometryWrapper(ByteArrayInputStream bytesInputStream)
            throws IOException {
        try {
            this.geometry = WKB_READER.get()
                    .read(new InputStreamInStream(bytesInputStream));
        } catch (ParseException e) {
            throw new IOException("unable to parse serialized geometry", e);
        }
        this.centerPoint = new Coordinate(
                PrimitiveAndStringBinaryTranslator.readDouble(bytesInputStream,
                        ByteOrder.BIG_ENDIAN),
                PrimitiveAndStringBinaryTranslator.readDouble(bytesInputStream,
                        ByteOrder.BIG_ENDIAN),
                0.0);
        this.rotation = PrimitiveAndStringBinaryTranslator
                .readDouble(bytesInputStream, ByteOrder.BIG_ENDIAN);
    }

    // Private Constructors

    /**
     * Construct a standard instance, with the center point explictly specified
     * instead of being calculated by the constructor.
     * 
     * @param geometry
     *            Geometry defining the shape; cannot be <code>null</code>.
     * @param centerPoint
     *            Center point of the shape.
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
        return (((geometry == otherGeometryWrapper.geometry)
                || ((geometry != null)
                        && (otherGeometryWrapper.geometry != null)
                        && geometry.equals(otherGeometryWrapper.geometry)))
                && (rotation == otherGeometryWrapper.rotation));
    }

    @Override
    public int hashCode() {
        return (int) (((geometry == null ? 0L : (double) geometry.hashCode())
                + (Double.valueOf(rotation).hashCode())) % Integer.MAX_VALUE);
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
        Geometry geometry = AffineTransformation
                .rotationInstance(delta, centerPoint.x, centerPoint.y)
                .transform(getGeometry());

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
                .rotationInstance(rotation * -1.0, centerPoint.x,
                        centerPoint.y);
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
    public Geometry asGeometry(GeometryFactory geometryFactory, double flatness,
            int limit) {
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
    public void toBinary(OutputStream outputStream) throws IOException {
        WKB_WRITER.get().write(geometry,
                new OutputStreamOutStream(outputStream));
        PrimitiveAndStringBinaryTranslator.writeDouble(centerPoint.x,
                outputStream, ByteOrder.BIG_ENDIAN);
        PrimitiveAndStringBinaryTranslator.writeDouble(centerPoint.y,
                outputStream, ByteOrder.BIG_ENDIAN);
        PrimitiveAndStringBinaryTranslator.writeDouble(rotation, outputStream,
                ByteOrder.BIG_ENDIAN);
    }

    @Override
    public String toString() {
        return geometry.toString();
    }

    // Private Methods

    /**
     * Ensure that serialization attempts end up providing an instance of
     * {@link SerializationProxy} instead.
     * 
     * @return Instance of the proxy to be serialized instead of this object.
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * Since this object should never be deserialized directly, but rather an
     * instance of its {@link SerializationProxy} should be deserialized and
     * converted to an instance of this object, throw an error if anything
     * attempts a direct deserialization.
     * 
     * @param stream
     *            Input stream.
     * @throws InvalidObjectException
     *             Whenever invoked.
     */
    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("proxy required");
    }
}
