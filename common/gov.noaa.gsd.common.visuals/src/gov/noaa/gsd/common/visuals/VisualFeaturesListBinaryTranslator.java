/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import com.raytheon.uf.common.util.ByteArrayOutputStreamPool;

/**
 * Description: Translator providing utility methods for converting
 * {@link VisualFeaturesList} objects into arrays of bytes (or hexadecimal
 * strings) and vice versa.
 * <p>
 * Note that the methods in this class are thread-safe.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 10, 2017   28892    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisualFeaturesListBinaryTranslator {

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

    // Public Static Methods

    /**
     * Serialize the specified visual features list to a compressed array of
     * bytes.
     * 
     * @param visualFeatures
     *            Visual features list to be serialized.
     * @return Array of bytes holding the serialized visual features list.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    public static byte[] serializeToBytes(VisualFeaturesList visualFeatures)
            throws IOException {
        ByteArrayOutputStream bytesOutputStream = ByteArrayOutputStreamPool
                .getInstance().getStream(BYTE_ARRAY_INITIAL_SIZE);
        VisualFeaturesListBinarySerializer.serialize(visualFeatures,
                bytesOutputStream);
        return bytesOutputStream.toByteArray();
    }

    /**
     * Deserialize the specified compressed array of bytes to a visual features
     * list.
     * 
     * @param bytes
     *            Bytes to be deserialized.
     * @return Visual features list that was deserialized.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    public static VisualFeaturesList deserializeFromBytes(byte[] bytes)
            throws IOException {
        VisualFeaturesList visualFeatures = null;
        try (ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(
                bytes)) {
            visualFeatures = VisualFeaturesListBinaryDeserializer
                    .deserialize(bytesInputStream);
        }
        return visualFeatures;
    }

    /**
     * Serialize the specified visual features list to a compressed array of
     * bytes.
     * 
     * @param visualFeatures
     *            Visual features list to be serialized.
     * @return Array of bytes holding the serialized visual features list.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    public static byte[] serializeToCompressedBytes(
            VisualFeaturesList visualFeatures) throws IOException {

        /*
         * Compress the byte array created by serializing the visual features
         * list. The byte array output stream taken from the pool is not part of
         * the try-with-resources statement because it would then be closed
         * twice, once by the GZIP output stream wrapping it, and once by the
         * try-with-resources block upon completion, and currently the pooled
         * byte array output streams do not handle being closed multiple times
         * well.
         */
        ByteArrayOutputStream bytesOutputStream = ByteArrayOutputStreamPool
                .getInstance().getStream(BYTE_ARRAY_INITIAL_SIZE);
        byte[] compressedBytes = null;
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
                bytesOutputStream)) {
            VisualFeaturesListBinarySerializer.serialize(visualFeatures,
                    gzipOutputStream);
            gzipOutputStream.finish();
            compressedBytes = bytesOutputStream.toByteArray();
        }
        return compressedBytes;
    }

    /**
     * Deserialize the specified compressed array of bytes to a visual features
     * list.
     * 
     * @param bytes
     *            Bytes to be deserialized.
     * @return Visual features list that was deserialized.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    public static VisualFeaturesList deserializeFromCompressedBytes(byte[] bytes)
            throws IOException {
        VisualFeaturesList visualFeatures = null;
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
            ByteArrayOutputStream bytesOutputStream = ByteArrayOutputStreamPool
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
             * Deserialize the visual features list from the uncompressed bytes.
             */
            visualFeatures = VisualFeaturesListBinaryDeserializer
                    .deserialize(new ByteArrayInputStream(uncompressedBytes));
        }
        return visualFeatures;
    }

    /**
     * Serialize the specified visual features list to a compressed array of
     * bytes converted to a base-64-encoded string.
     * 
     * @param visualFeatures
     *            Visual features list to be serialized.
     * @return Base-64-encoded string holding the serialized visual features
     *         list.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    public static String serializeToCompressedBytesInBase64String(
            VisualFeaturesList visualFeatures) throws IOException {
        byte[] bytes = serializeToCompressedBytes(visualFeatures);
        return DatatypeConverter.printBase64Binary(bytes);
    }

    /**
     * Deserialize the specified base-64-encoded string holding a compressed
     * array of bytes to a visual features list.
     * 
     * @param base64
     *            Base-64-encoded string to be deserialized.
     * @return Visual features list that was deserialized.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    public static VisualFeaturesList deserializeFromCompressedBytesInBase64String(
            String base64) throws IOException {
        byte[] bytes = DatatypeConverter.parseBase64Binary(base64);
        return deserializeFromCompressedBytes(bytes);
    }
}
