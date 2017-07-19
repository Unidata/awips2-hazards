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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.common.util.ByteArrayOutputStreamPool;
import com.raytheon.uf.common.util.PooledByteArrayOutputStream;

import gov.noaa.gsd.common.utilities.IBinarySerializable;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator.ByteOrder;

/**
 * Description: Translator providing utility methods for converting
 * {@link IAdvancedGeometry} objects into arrays of bytes (or hexadecimal
 * strings) and vice versa. This takes advantage of the fact that all
 * <code>IAdvancedGeometry</code> subclasses implement
 * {@link IBinarySerializable}.
 * <p>
 * Note that the methods in this class are thread-safe.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 07, 2017   28892    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class AdvancedGeometryBinaryTranslator {

    // Private Static Constants

    /**
     * Initial size of byte arrays used for serialization.
     */
    private static final int BYTE_ARRAY_INITIAL_SIZE = 8092;

    /**
     * Byte buffer length.
     */
    private static final int BYTE_BUFFER_LENGTH = 4096;

    /**
     * Byte array used for copying from an input stream to an output stream. It
     * is thread-local because this class's methods may be called simultaneously
     * by multiple threads, and each thread must be able to serialize and
     * deserialize objects separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<byte[]> BYTE_BUFFER = new ThreadLocal<byte[]>() {

        @Override
        protected byte[] initialValue() {
            return new byte[BYTE_BUFFER_LENGTH];
        }
    };

    // Private Enumerated Types

    /**
     * Types of advanced geometries.
     * <p>
     * <strong>Note</strong>: The ordering of the values must not change, as the
     * ordinals are used in serialization and deserialization of
     * {@link IAdvancedGeometry} instances.
     * </p>
     */
    private enum Type {
        COLLECTION(AdvancedGeometryCollection.class, new IDeserializer() {

            @Override
            public IAdvancedGeometry deserializeFromBinary(
                    ByteArrayInputStream bytesInputStream) throws IOException {
                return new AdvancedGeometryCollection(bytesInputStream);
            }
        }), ELLIPSE(Ellipse.class, new IDeserializer() {

            @Override
            public IAdvancedGeometry deserializeFromBinary(
                    ByteArrayInputStream bytesInputStream) throws IOException {
                return new Ellipse(bytesInputStream);
            }
        }), GEOMETRY_WRAPPER(GeometryWrapper.class, new IDeserializer() {

            @Override
            public IAdvancedGeometry deserializeFromBinary(
                    ByteArrayInputStream bytesInputStream) throws IOException {
                return new GeometryWrapper(bytesInputStream);
            }
        });

        // Private Static Constants

        /**
         * Map of advanced geometry classes to their types.
         */
        private static final Map<Class<? extends IAdvancedGeometry>, Type> TYPES_FOR_CLASSES;

        static {
            Map<Class<? extends IAdvancedGeometry>, Type> map = new HashMap<>(
                    Type.values().length);
            for (Type type : Type.values()) {
                map.put(type.advancedGeometryClass, type);
            }
            TYPES_FOR_CLASSES = ImmutableMap.copyOf(map);
        }

        /**
         * List of all possible values, indexed by their ordinals. This is
         * cached because {@link #values()} creates a new array each time it is
         * called.
         */
        private static final List<Type> ALL_VALUES = ImmutableList
                .copyOf(values());

        // Private Variables

        /**
         * Advanced geometry class associated with this type.
         */
        private final Class<? extends IAdvancedGeometry> advancedGeometryClass;

        /**
         * Deserializer for objects of this type.
         */
        private final IDeserializer deserializer;

        // Public Static Methods

        /**
         * Get the type associated with the specified class.
         * 
         * @param advancedGeometryClass
         *            Class of advanced geometry for which to get the type.
         * @return Type.
         */
        public static Type getTypeForClass(
                Class<? extends IAdvancedGeometry> advancedGeometryClass) {
            return TYPES_FOR_CLASSES.get(advancedGeometryClass);
        }

        /**
         * Get the value with the specified ordinal. Invoking this method rather
         * than {@link #values()} is preferable because the latter creates a new
         * array each time it is called.
         * 
         * @param ordinal
         *            Ordinal for which to fetch the value.
         * @return Value.
         * @throws IllegalArgumentException
         *             If the ordinal value is not within range.
         */
        public static Type getValueForOrdinal(int ordinal)
                throws IllegalArgumentException {
            if ((ordinal < 0) || (ordinal >= ALL_VALUES.size())) {
                throw new IllegalArgumentException(
                        "ordinal value " + ordinal + " is out of range");
            }
            return ALL_VALUES.get(ordinal);
        }

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param advancedGeometryClass
         *            Class of the advanced geometry associated with this type.
         * @param deserializer
         *            Deserializer for objects of this type.
         */
        private Type(Class<? extends IAdvancedGeometry> advancedGeometryClass,
                IDeserializer deserializer) {
            this.advancedGeometryClass = advancedGeometryClass;
            this.deserializer = deserializer;
        }

        // Public Methods

        /**
         * Get the deserializer for objects of this type.
         * 
         * @return Deserializer for objects of this type.
         */
        public IDeserializer getDeserializer() {
            return deserializer;
        }
    };

    // Private Interfaces

    /**
     * Interface that must be implemented to create a binary deserializer for an
     * advanced geometry.
     */
    private interface IDeserializer {

        /**
         * Deserialize an advanced geometry from the specified input stream.
         * 
         * @param bytesinputStream
         *            Byte array input stream from which to deserialize the
         *            advanced geometry.
         * @return Deserialized advanced geometry.
         * @throws IOException
         *             If an error occurs while deserializing.
         */
        public IAdvancedGeometry deserializeFromBinary(
                ByteArrayInputStream bytesinputStream) throws IOException;
    }

    // Public Static Methods

    /**
     * Serialize the specified advanced geometry to binary form in the specified
     * output stream.
     * 
     * @param geometry
     *            Advanced geometry to be serialized.
     * @param outputStream
     *            Output stream to which to serialize the geometry.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    public static void serializeToBinaryStream(IAdvancedGeometry geometry,
            OutputStream outputStream) throws IOException {

        /*
         * Get the type to be serialized, and write its ordinal to the stream.
         */
        Type type = Type.getTypeForClass(geometry.getClass());
        if (type == null) {
            throw new IllegalStateException(
                    "cannot find way to serialize IAdvancedGeometry subclass "
                            + geometry.getClass());
        }
        PrimitiveAndStringBinaryTranslator.writeShort((short) type.ordinal(),
                outputStream, ByteOrder.BIG_ENDIAN);

        /*
         * Write the geometry to the stream.
         */
        geometry.toBinary(outputStream);
    }

    /**
     * Deserialize an advanced geometry from binary form in the specified input
     * stream.
     * 
     * @param bytesInputStream
     *            Input stream from which to deserialize the geometry.
     * @return Advanced geometry that was deserialized.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    public static IAdvancedGeometry deserializeFromBinaryStream(
            ByteArrayInputStream bytesInputStream) throws IOException {

        /*
         * Get the type of the geometry to be deserialized based upon its
         * ordinal read from the stream.
         */
        int ordinal = PrimitiveAndStringBinaryTranslator
                .readShort(bytesInputStream, ByteOrder.BIG_ENDIAN);
        Type type = null;
        try {
            type = Type.getValueForOrdinal(ordinal);
        } catch (IllegalArgumentException e) {
            throw new IOException("unknown advanced geometry type", e);
        }

        /*
         * Read the geometry from the stream.
         */
        return type.getDeserializer().deserializeFromBinary(bytesInputStream);
    }

    /**
     * Serialize the specified advanced geometry to a compressed array of bytes.
     * 
     * @param geometry
     *            Advanced geometry to be serialized.
     * @return Array of bytes holding the serialized geometry.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    public static byte[] serializeToCompressedBytes(IAdvancedGeometry geometry)
            throws IOException {

        /*
         * Compress the byte array created by serializing the geometry. The byte
         * array output stream taken from the pool is not part of the
         * try-with-resources statement because it would then be closed twice,
         * once by the GZIP output stream wrapping it, and once by the
         * try-with-resources block upon completion, and currently the pooled
         * byte array output streams do not handle being closed multiple times
         * well.
         */
        PooledByteArrayOutputStream bytesOutputStream = ByteArrayOutputStreamPool
                .getInstance().getStream(BYTE_ARRAY_INITIAL_SIZE);
        byte[] compressedBytes = null;
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
                bytesOutputStream)) {
            serializeToBinaryStream(geometry, gzipOutputStream);
            gzipOutputStream.finish();
            compressedBytes = bytesOutputStream.toByteArray();
        }
        return compressedBytes;
    }

    /**
     * Deserialize the specified compressed array of bytes to an advanced
     * geometry.
     * 
     * @param bytes
     *            Bytes to be deserialized.
     * @return Advanced geometry that was deserialized.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    public static IAdvancedGeometry deserializeFromCompressedBytes(byte[] bytes)
            throws IOException {
        IAdvancedGeometry geometry = null;
        try (ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(
                bytes);
                GZIPInputStream gzipInputStream = new GZIPInputStream(
                        bytesInputStream)) {

            /*
             * Get a byte array input stream from the pool, and uncompress the
             * array of bytes into it. Use a thread-local byte buffer to avoid
             * having to recreate the buffer each time.
             */
            byte[] buffer = BYTE_BUFFER.get();
            PooledByteArrayOutputStream bytesOutputStream = ByteArrayOutputStreamPool
                    .getInstance().getStream(BYTE_ARRAY_INITIAL_SIZE);
            int readCount;
            while ((readCount = gzipInputStream.read(buffer)) > 0) {
                bytesOutputStream.write(buffer, 0, readCount);
            }

            /*
             * Get the resulting uncompressed bytes, and close the output stream
             * to return it to the pool.
             */
            byte[] uncompressedBytes = bytesOutputStream.toByteArray();
            bytesOutputStream.close();

            /*
             * Deserialize the geometry from the uncompressed bytes.
             */
            geometry = deserializeFromBinaryStream(
                    new ByteArrayInputStream(uncompressedBytes));
        }
        return geometry;
    }

    /**
     * Serialize the specified advanced geometry to a compressed array of bytes
     * converted to a base-64-encoded string.
     * 
     * @param geometry
     *            Advanced geometry to be serialized.
     * @return Base-64-encoded string holding the serialized geometry.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    public static String serializeToCompressedBytesInBase64String(
            IAdvancedGeometry geometry) throws IOException {
        byte[] bytes = serializeToCompressedBytes(geometry);
        return DatatypeConverter.printBase64Binary(bytes);
    }

    /**
     * Deserialize the specified base-64-encoded string holding a compressed
     * array of bytes to an advanced geometry.
     * 
     * @param base64
     *            Base-64-encoded string to be deserialized.
     * @return Advanced geometry that was deserialized.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    public static IAdvancedGeometry deserializeFromCompressedBytesInBase64String(
            String base64) throws IOException {
        byte[] bytes = DatatypeConverter.parseBase64Binary(base64);
        return deserializeFromCompressedBytes(bytes);
    }
}
