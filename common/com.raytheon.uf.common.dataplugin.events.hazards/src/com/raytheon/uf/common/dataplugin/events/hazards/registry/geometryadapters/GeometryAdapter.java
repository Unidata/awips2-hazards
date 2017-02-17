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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.geometryadapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.raytheon.uf.common.util.ByteArrayOutputStreamPool;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * Description: Geometry adapter for serializing and deserializing
 * {@link Geometry} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 13, 2016   26663    mpduff       Initial creation.
 * Feb 07, 2017   28892    Chris.Golden Added to H.S. Experimental
 *                                      from 18-Hazard_Services
 *                                      repo, and adapted slightly.
 *                                      Also replaced hexadecimal
 *                                      string usage with base64
 *                                      string for shorter strings.
 * </pre>
 * 
 * @author mpduff
 */
public class GeometryAdapter extends XmlAdapter<String, Geometry> {

    // Private Static Constants

    /**
     * Byte buffer length.
     */
    private static final int BYTE_BUFFER_LENGTH = 4096;

    /**
     * Assumption of what sort of compression ratio is achievable.
     */
    private static final int COMPRESSION_RATIO_ASSUMPTION = 4;

    // Private Static Constants

    /**
     * Byte array used for copying from an input stream to an output stream. It
     * is thread-local because this class's methods may be called simultaneously
     * by multiple threads, and each thread must be able to marshal and
     * unmarshal geometries separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<byte[]> BYTE_BUFFER = new ThreadLocal<byte[]>() {

        @Override
        protected byte[] initialValue() {
            return new byte[BYTE_BUFFER_LENGTH];
        }
    };

    /**
     * <a
     * href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">
     * Well-Known Binary</a> reader, used for deserializing geometries. It is
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
     * <a
     * href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">
     * Well-Known Binary</a> writer, used for serializing geometries. It is
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

    // Public Methods

    @Override
    public Geometry unmarshal(String v) throws IOException, ParseException {

        /*
         * Convert from the base-64 string to an array of bytes.
         */
        byte[] bytes = DatatypeConverter.parseBase64Binary(v);

        /*
         * Uncompress the array of bytes, decode it into a geometry, and return
         * it.
         */
        Geometry geometry = null;
        try (ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(
                bytes);
                GZIPInputStream gzipInputStream = new GZIPInputStream(
                        bytesInputStream)) {

            /*
             * Get a byte array input stream from the pool, and uncompress the
             * abovbe array of bytes into it. Use a thread-local byte buffer to
             * avoid having to recreate the buffer each time.
             */
            byte[] buffer = BYTE_BUFFER.get();
            ByteArrayOutputStream bytesOutputStream = ByteArrayOutputStreamPool
                    .getInstance().getStream(
                            bytes.length * COMPRESSION_RATIO_ASSUMPTION);
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
             * Deserialize the bytes into a geometry.
             */
            geometry = WKB_READER.get().read(uncompressedBytes);
        }
        return geometry;
    }

    @Override
    public String marshal(Geometry v) throws IOException {

        /*
         * Encode the geometry as a byte array.
         */
        byte[] bytes = WKB_WRITER.get().write(v);

        /*
         * Compress the byte array. The byte array output stream taken from the
         * pool is not part of the try-with-resources statement because it would
         * then be closed twice, once by the GZIP output stream wrapping it, and
         * once by the try-with-resources block upon completion, and currently
         * the pooled byte array output streams do not handle being closed
         * multiple times well.
         */
        ByteArrayOutputStream bytesOutputStream = ByteArrayOutputStreamPool
                .getInstance().getStream(
                        bytes.length / COMPRESSION_RATIO_ASSUMPTION);
        byte[] compressedBytes = null;
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
                bytesOutputStream)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            compressedBytes = bytesOutputStream.toByteArray();
        }

        /*
         * Convert the compressed bytes into a base-64 string and return it.
         */
        return DatatypeConverter.printBase64Binary(compressedBytes);
    }
}
