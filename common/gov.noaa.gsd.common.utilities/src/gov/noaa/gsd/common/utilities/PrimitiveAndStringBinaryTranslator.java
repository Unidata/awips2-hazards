/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description: Translator for converting Java primitive values and
 * {@link String} objects into arrays of bytes and vice versa.
 * <p>
 * Note that the various <code>readXXXX()</code> methods require a
 * {@link ByteArrayInputStream} as a parameter, whereas the corresponding
 * <code>writeXXXX()</code> methods require a more general {@link OutputStream}.
 * This is because while an output stream's various <code>write()</code> methods
 * are guaranteed to write out <i>all</i> the bytes supplied to them as
 * parameters if they do not encounter an {@link IOException}, the
 * {@link InputStream}'s corresponding <code>read()</code> methods are
 * <i>not</i> guaranteed to read in all of the bytes requested. Only the
 * <code>ByteArrayInputStream</code>'s <code>write()</code> methods make this
 * guarantee. Similarly, the latter class's
 * {@link ByteArrayInputStream#available()} method provides the exact number of
 * bytes remaining in the stream to be read, whereas the implementations of this
 * method in other subclasses of <code>InputStream</code> do not necessarily
 * provide such an exact count. Both of these properties are relied upon by the
 * <code>readXXXX()</code> methods defined herein.
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
public class PrimitiveAndStringBinaryTranslator {

    // Public Static Constants

    /**
     * Number of bits in a byte.
     */
    public static final int BITS_IN_BYTE = 8;

    /**
     * Number of bytes in a short.
     */
    public static final int BYTES_IN_SHORT = Short.SIZE / BITS_IN_BYTE;

    /**
     * Number of bytes in an integer.
     */
    public static final int BYTES_IN_INTEGER = Integer.SIZE / BITS_IN_BYTE;

    /**
     * Number of bytes in a long.
     */
    public static final int BYTES_IN_LONG = Long.SIZE / BITS_IN_BYTE;

    /**
     * Number of bytes in a float.
     */
    public static final int BYTES_IN_FLOAT = BYTES_IN_INTEGER;

    /**
     * Number of bytes in a double.
     */
    public static final int BYTES_IN_DOUBLE = BYTES_IN_LONG;

    /**
     * Maximum number of bytes in a "short" string, that is, a string that is
     * not particularly long.
     */
    public static final int BYTES_IN_SHORT_STRING = 1024;

    // Private Static Constants

    /**
     * Byte array used for translation. It is thread-local because this class's
     * static methods may be called simultaneously by multiple threads, and each
     * thread must be able to translate primitives separately in order to avoid
     * cross-thread pollution.
     */
    private static final ThreadLocal<byte[]> BYTE_BUFFER = new ThreadLocal<byte[]>() {

        @Override
        protected byte[] initialValue() {
            return new byte[BYTES_IN_SHORT_STRING];
        }
    };

    // Public Enumerated Types

    /**
     * Byte order.
     */
    public enum ByteOrder {
        BIG_ENDIAN, LITTLE_ENDIAN
    }

    // Public Static Methods

    /**
     * Get a boolean value from the specified bytes.
     * 
     * @param bytes
     *            Array of bytes holding the boolean value; must be at least 1
     *            byte in length.
     * @return Boolean value extracted from the bytes.
     */
    public static boolean getBooleanFromBytes(byte[] bytes) {
        return (bytes[0] != (byte) 0);
    }

    /**
     * Get a byte value from the specified bytes.
     * 
     * @param bytes
     *            Array of bytes holding the byte value; must be at least 1 byte
     *            in length.
     * @return Byte value extracted from the bytes; will be between 0 and 255
     *         inclusive.
     */
    public static short getByteFromBytes(byte[] bytes) {
        return (short) (bytes[0] & 0xff);
    }

    /**
     * Get a short value from the specified bytes in the specified byte order.
     * 
     * @param bytes
     *            Array of bytes holding the short value using the given byte
     *            order; must be at least @link #BYTES_IN_SHORT} bytes in
     *            length.
     * @param order
     *            Byte order.
     * @return Short value extracted from the bytes in the specified byte order.
     */
    public static short getShortFromBytes(byte[] bytes, ByteOrder order) {
        short result = 0;
        for (short j = 0; j < BYTES_IN_SHORT; j++) {
            result |= (short) (bytes[order == ByteOrder.BIG_ENDIAN ? (BYTES_IN_SHORT - 1)
                    - j
                    : j] & 0xff) << (j * BITS_IN_BYTE);
        }
        return result;
    }

    /**
     * Get an integer value from the specified bytes in the specified byte
     * order.
     * 
     * @param bytes
     *            Array of bytes holding the integer value using the given byte
     *            order; must be at least {@link #BYTES_IN_INTEGER} bytes in
     *            length.
     * @param order
     *            Byte order.
     * @return Integer value extracted from the bytes in the specified byte
     *         order.
     */
    public static int getIntegerFromBytes(byte[] bytes, ByteOrder order) {
        int result = 0;
        for (int j = 0; j < BYTES_IN_INTEGER; j++) {
            result |= (bytes[order == ByteOrder.BIG_ENDIAN ? (BYTES_IN_INTEGER - 1)
                    - j
                    : j] & 0xff) << (j * BITS_IN_BYTE);
        }
        return result;
    }

    /**
     * Get a long value from the specified bytes in the specified byte order.
     * 
     * @param bytes
     *            Array of bytes holding the long value using the given byte
     *            order; must be at least {@link #BYTES_IN_LONG} bytes in
     *            length.
     * @param order
     *            Byte order.
     * @return Long value extracted from the bytes in the specified byte order.
     */
    public static long getLongFromBytes(byte[] bytes, ByteOrder order) {
        long result = 0;
        for (int j = 0; j < BYTES_IN_LONG; j++) {
            result |= (long) (bytes[order == ByteOrder.BIG_ENDIAN ? (BYTES_IN_LONG - 1)
                    - j
                    : j] & 0xff) << (j * BITS_IN_BYTE);
        }
        return result;
    }

    /**
     * Get a float value from the specified bytes in the specified byte order.
     * 
     * @param bytes
     *            Array of bytes holding the float value using the given byte
     *            order; must be at least {@link #BYTES_IN_FLOAT} bytes in
     *            length.
     * @param order
     *            Byte order.
     * @return Float value extracted from the bytes in the specified byte order.
     */
    public static float getFloatFromBytes(byte[] bytes, ByteOrder order) {
        return Float.intBitsToFloat(getIntegerFromBytes(bytes, order));
    }

    /**
     * Get a double value from the specified bytes in the specified byte order.
     * 
     * @param bytes
     *            Array of bytes holding the double value using the given byte
     *            order; must be at least {@link #BYTES_IN_DOUBLE} bytes in
     *            length.
     * @param order
     *            Byte order.
     * @return Double value extracted from the bytes in the specified byte
     *         order.
     */
    public static double getDoubleFromBytes(byte[] bytes, ByteOrder order) {
        return Double.longBitsToDouble(getLongFromBytes(bytes, order));
    }

    /**
     * Get a string value from the specified bytes in the specified byte order.
     * 
     * @param bytes
     *            Array of bytes holding the string value using the given byte
     *            order; must be at least {@link #BYTES_IN_INTEGER} bytes, plus
     *            the number of bytes equal to the value of the integer held in
     *            those first few bytes, in length.
     * @param order
     *            Byte order.
     * @return String value extracted from the bytes in the specified byte
     *         order, or <code>null</code> if no string was found in the byte
     *         array.
     */
    public static String getStringFromBytes(byte[] bytes, ByteOrder order) {
        int length = getIntegerFromBytes(bytes, order);
        if (length > 0) {
            return new String(bytes, BYTES_IN_INTEGER, length);
        }
        return null;
    }

    /**
     * Read a boolean value from the specified input stream.
     * 
     * @param stream
     *            Input stream holding the boolean value; must have at least 1
     *            byte available.
     * @return Boolean value extracted from the stream.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static boolean readBoolean(ByteArrayInputStream stream)
            throws IOException {
        if (stream.available() < 1) {
            throw new IOException(
                    "stream does not have enough bytes to read boolean value");
        }
        return (stream.read() != 0);
    }

    /**
     * Read a byte value from the specified input stream.
     * 
     * @param stream
     *            Input stream holding the byte value; must have at least 1 byte
     *            available.
     * @return Byte value extracted from the stream; will be between 0 and 255
     *         inclusive.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static short readByte(ByteArrayInputStream stream)
            throws IOException {
        if (stream.available() < 1) {
            throw new IOException(
                    "stream does not have enough bytes to read byte value");
        }
        return (short) stream.read();
    }

    /**
     * Get a short value from the specified input stream in the specified byte
     * order.
     * 
     * @param stream
     *            Input stream holding the short value; must have at least
     *            {@link #BYTES_IN_SHORT} bytes available.
     * @param order
     *            Byte order.
     * @return Short value extracted from the stream in the specified byte
     *         order.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static short readShort(ByteArrayInputStream stream, ByteOrder order)
            throws IOException {
        if (stream.available() < BYTES_IN_SHORT) {
            throw new IOException(
                    "stream does not have enough bytes to read short value");
        }
        byte[] bytes = BYTE_BUFFER.get();
        stream.read(bytes, 0, BYTES_IN_SHORT);
        return getShortFromBytes(bytes, order);
    }

    /**
     * Read an integer value from the specified input stream in the specified
     * byte order.
     * 
     * @param stream
     *            Input stream holding the integer value; must have at least
     *            {@link #BYTES_IN_INTEGER} bytes available.
     * @param order
     *            Byte order.
     * @return Integer value extracted from the stream in the specified byte
     *         order.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static int readInteger(ByteArrayInputStream stream, ByteOrder order)
            throws IOException {
        if (stream.available() < BYTES_IN_INTEGER) {
            throw new IOException(
                    "stream does not have enough bytes to read integer value");
        }
        byte[] bytes = BYTE_BUFFER.get();
        stream.read(bytes, 0, BYTES_IN_INTEGER);
        return getIntegerFromBytes(bytes, order);
    }

    /**
     * Read a long value from the specified input stream in the specified byte
     * order.
     * 
     * @param stream
     *            Input stream holding the long value; must have at least
     *            {@link #BYTES_IN_LONG} bytes available.
     * @param order
     *            Byte order.
     * @return Long value extracted from the stream in the specified byte order.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static long readLong(ByteArrayInputStream stream, ByteOrder order)
            throws IOException {
        if (stream.available() < BYTES_IN_LONG) {
            throw new IOException(
                    "stream does not have enough bytes to read long value");
        }
        byte[] bytes = BYTE_BUFFER.get();
        stream.read(bytes, 0, BYTES_IN_LONG);
        return getLongFromBytes(bytes, order);
    }

    /**
     * Read a float value from the specified input stream in the specified byte
     * order.
     * 
     * @param stream
     *            Input stream holding the float value; must have at least
     *            {@link #BYTES_IN_FLOAT} bytes available.
     * @param order
     *            Byte order.
     * @return Float value extracted from the stream in the specified byte
     *         order.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static float readFloat(ByteArrayInputStream stream, ByteOrder order)
            throws IOException {
        if (stream.available() < BYTES_IN_FLOAT) {
            throw new IOException(
                    "stream does not have enough bytes to read float value");
        }
        byte[] bytes = BYTE_BUFFER.get();
        stream.read(bytes, 0, BYTES_IN_FLOAT);
        return getFloatFromBytes(bytes, order);
    }

    /**
     * Read a double value from the specified input stream in the specified byte
     * order.
     * 
     * @param stream
     *            Input stream holding the double value; must have at least
     *            {@link #BYTES_IN_DOUBLE} bytes available.
     * @param order
     *            Byte order.
     * @return Double value extracted from the stream in the specified byte
     *         order.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static double readDouble(ByteArrayInputStream stream, ByteOrder order)
            throws IOException {
        if (stream.available() < BYTES_IN_DOUBLE) {
            throw new IOException(
                    "stream does not have enough bytes to read double value");
        }
        byte[] bytes = BYTE_BUFFER.get();
        stream.read(bytes, 0, BYTES_IN_DOUBLE);
        return getDoubleFromBytes(bytes, order);
    }

    /**
     * Read a string value from the specified input stream in the specified byte
     * order.
     * 
     * @param stream
     *            Input stream holding the string value; must have at least
     *            {@link #BYTES_IN_INTEGER} bytes, plus the number of bytes
     *            equal to the value of the integer held in those first few
     *            bytes, available.
     * @param order
     *            Byte order.
     * @return String value extracted from the stream in the specified byte
     *         order, or <code>null</code> if there was no string serialized.
     * @throws IOException
     *             If the input stream does not have the right number of bytes
     *             available.
     */
    public static String readString(ByteArrayInputStream stream, ByteOrder order)
            throws IOException {
        int length;
        try {
            length = readInteger(stream, order);
        } catch (IOException e) {
            throw new IOException("stream does not have enough bytes to "
                    + "read length of string as integer");
        }
        if (length == 0) {
            return null;
        }

        /*
         * TODO: For now, the class-scoped but thread-local byte array is used
         * if the string to be read is sufficiently short, but a new byte array
         * is allocated if the string is longer than the thread-local array can
         * handle. This is not ideal; it might be worth considering having a
         * byte array pool to allow their reuse.
         */
        if (stream.available() < length) {
            throw new IOException(
                    "stream does not have enough bytes to read string "
                            + "value of expected length " + length);
        }
        byte[] bytes = (length > BYTES_IN_SHORT_STRING ? new byte[length]
                : BYTE_BUFFER.get());
        stream.read(bytes, 0, length);
        return new String(bytes, 0, length);
    }

    /**
     * Get the bytes from the specified boolean value.
     * 
     * @param value
     *            Boolean value to be placed in a byte array.
     * @param bytes
     *            Optional array of bytes to be used to hold the boolean value;
     *            must be at least 1 byte in length. If <code>null</code>, a new
     *            array will be created.
     * @return Byte array holding the boolean; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     */
    public static byte[] getBytesFromBoolean(boolean value, byte[] bytes) {
        if (bytes == null) {
            bytes = new byte[1];
        }
        bytes[0] = (byte) (value ? 1 : 0);
        return bytes;
    }

    /**
     * Get the bytes from the specified byte value.
     * 
     * @param value
     *            Byte value to be placed in a byte array; must be between 0 and
     *            255 inclusive.
     * @param bytes
     *            Optional array of bytes to be used to hold the boolean value;
     *            must be at least 1 byte in length. If <code>null</code>, a new
     *            array will be created.
     * @return Byte array holding the byte; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     * @throws IllegalArgumentException
     *             If the value is not between 0 and 255 inclusive.
     */
    public static byte[] getBytesFromByte(short value, byte[] bytes) {
        if ((value < 0) || (value > 255)) {
            throw new IllegalArgumentException("value " + value
                    + " is not between 0 and 255");
        }
        if (bytes == null) {
            bytes = new byte[1];
        }
        bytes[0] = (byte) value;
        return bytes;
    }

    /**
     * Get the bytes from the specified short value in the specified byte order.
     * 
     * @param value
     *            Short value to be placed in a byte array using the specified
     *            byte order.
     * @param order
     *            Byte order.
     * @param bytes
     *            Optional array of bytes to be used to hold the short value
     *            using the given byte order; must be at least
     *            {@link #BYTES_IN_SHORT} bytes in length. If <code>null</code>,
     *            a new array will be created.
     * @return Byte array holding the short; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     */
    public static byte[] getBytesFromShort(short value, ByteOrder order,
            byte[] bytes) {
        if (bytes == null) {
            bytes = new byte[BYTES_IN_SHORT];
        }
        for (int j = 0; j < BYTES_IN_SHORT; j++) {
            bytes[order == ByteOrder.BIG_ENDIAN ? (BYTES_IN_SHORT - 1) - j : j] = (byte) (value >> (j * BITS_IN_BYTE));
        }
        return bytes;
    }

    /**
     * Get the bytes from the specified integer value in the specified byte
     * order.
     * 
     * @param value
     *            Integer value to be placed in a byte array using the specified
     *            byte order.
     * @param order
     *            Byte order.
     * @param bytes
     *            Optional array of bytes to be used to hold the integer value
     *            using the given byte order; must be at least
     *            {@link #BYTES_IN_INTEGER} bytes in length. If
     *            <code>null</code>, a new array will be created.
     * @return Byte array holding the integer; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     */
    public static byte[] getBytesFromInteger(int value, ByteOrder order,
            byte[] bytes) {
        if (bytes == null) {
            bytes = new byte[BYTES_IN_INTEGER];
        }
        for (int j = 0; j < BYTES_IN_INTEGER; j++) {
            bytes[order == ByteOrder.BIG_ENDIAN ? (BYTES_IN_INTEGER - 1) - j
                    : j] = (byte) (value >> (j * BITS_IN_BYTE));
        }
        return bytes;
    }

    /**
     * Get the bytes from the specified long value in the specified byte order.
     * 
     * @param value
     *            Long value to be placed in a byte array using the specified
     *            byte order.
     * @param order
     *            Byte order.
     * @param bytes
     *            Optional array of bytes to be used to hold the long value
     *            using the given byte order; must be at least
     *            {@link #BYTES_IN_LONG} bytes in length. If <code>null</code>,
     *            a new array will be created.
     * @return Byte array holding the long; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     */
    public static byte[] getBytesFromLong(long value, ByteOrder order,
            byte[] bytes) {
        if (bytes == null) {
            bytes = new byte[BYTES_IN_LONG];
        }
        for (int j = 0; j < BYTES_IN_LONG; j++) {
            bytes[order == ByteOrder.BIG_ENDIAN ? (BYTES_IN_LONG - 1) - j : j] = (byte) ((value >> (j * BITS_IN_BYTE)) & 0xff);
        }
        return bytes;
    }

    /**
     * Get the bytes from the specified float value in the specified byte order.
     * 
     * @param value
     *            Float value to be placed in a byte array using the specified
     *            byte order.
     * @param order
     *            Byte order.
     * @param bytes
     *            Optional array of bytes to be used to hold the float value
     *            using the given byte order; must be at least
     *            {@link #BYTES_IN_FLOAT} bytes in length. If <code>null</code>,
     *            a new array will be created.
     * @return Byte array holding the float; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     */
    public static byte[] getBytesFromFloat(float value, ByteOrder order,
            byte[] bytes) {
        return getBytesFromInteger(Float.floatToIntBits(value), order, bytes);
    }

    /**
     * Get the bytes from the specified double value in the specified byte
     * order.
     * 
     * @param value
     *            Double value to be placed in a byte array using the specified
     *            byte order.
     * @param order
     *            Byte order.
     * @param bytes
     *            Optional array of bytes to be used to hold the double value
     *            using the given byte order; must be at least
     *            {@link #BYTES_IN_DOUBLE} bytes in length. If <code>null</code>
     *            , a new array will be created.
     * @return Byte array holding the double; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     */
    public static byte[] getBytesFromDouble(double value, ByteOrder order,
            byte[] bytes) {
        return getBytesFromLong(Double.doubleToLongBits(value), order, bytes);
    }

    /**
     * Get the bytes from the specified string value in the specified byte
     * order.
     * 
     * @param value
     *            String value to be placed in a byte array using the specified
     *            byte order; may be <code>null</code>.
     * @param order
     *            Byte order.
     * @param bytes
     *            Optional array of bytes to be used to hold the string value
     *            using the given byte order; must be at least
     *            {@link #BYTES_IN_INTEGER} bytes, plus the number of bytes that
     *            <code>value.</code>{@link String#getBytes() getBytes()}
     *            returns, in length. If <code>null</code> , a new array will be
     *            created.
     * @return Byte array holding the string; this will be the specified byte
     *         array if one was supplied, otherwise it will be a newly created
     *         one.
     */
    public static byte[] getBytesFromString(String value, ByteOrder order,
            byte[] bytes) {
        if (value == null) {
            if (bytes == null) {
                bytes = new byte[BYTES_IN_INTEGER];
                getBytesFromInteger(0, order, bytes);
                return bytes;
            }
        }
        byte[] stringBytes = value.getBytes();
        if (bytes == null) {
            bytes = new byte[BYTES_IN_INTEGER + stringBytes.length];
        }
        getBytesFromInteger(stringBytes.length, order, bytes);
        System.arraycopy(stringBytes, 0, bytes, BYTES_IN_INTEGER,
                stringBytes.length);
        return bytes;
    }

    /**
     * Write the specified boolean value into the specified output stream.
     * 
     * @param value
     *            Boolean value to be written.
     * @param stream
     *            Output stream into which to write the value.
     * @throws IOException
     *             If the output stream cannot complete the write.
     */
    public static void writeBoolean(boolean value, OutputStream stream)
            throws IOException {
        stream.write(value ? 1 : 0);
    }

    /**
     * Write the specified byte value into the specified output stream.
     * 
     * @param value
     *            Byte value to be written; must be between 0 and 255 inclusive.
     * @param stream
     *            Output stream into which to write the value.
     * @throws IOException
     *             If the output stream cannot complete the write.
     * @throws IllegalArgumentException
     *             If the value is not between 0 and 255 inclusive.
     */
    public static void writeByte(short value, OutputStream stream)
            throws IOException {
        if ((value < 0) || (value > 255)) {
            throw new IllegalArgumentException("value " + value
                    + " is not between 0 and 255");
        }
        stream.write(value);
    }

    /**
     * Write the specified short value into the specified output stream in the
     * specified byte order.
     * 
     * @param value
     *            Short value to be written.
     * @param stream
     *            Output stream into which to write the value.
     * @param order
     *            Byte order.
     * @throws IOException
     *             If the output stream cannot complete the write.
     */
    public static void writeShort(short value, OutputStream stream,
            ByteOrder order) throws IOException {
        byte[] bytes = BYTE_BUFFER.get();
        getBytesFromShort(value, order, bytes);
        stream.write(bytes, 0, BYTES_IN_SHORT);
    }

    /**
     * Write the specified integer value into the specified output stream in the
     * specified byte order.
     * 
     * @param value
     *            Integer value to be written.
     * @param stream
     *            Output stream into which to write the value.
     * @param order
     *            Byte order.
     * @throws IOException
     *             If the output stream cannot complete the write.
     */
    public static void writeInteger(int value, OutputStream stream,
            ByteOrder order) throws IOException {
        byte[] bytes = BYTE_BUFFER.get();
        getBytesFromInteger(value, order, bytes);
        stream.write(bytes, 0, BYTES_IN_INTEGER);
    }

    /**
     * Write the specified long value into the specified output stream in the
     * specified byte order.
     * 
     * @param value
     *            Long value to be written.
     * @param stream
     *            Output stream into which to write the value.
     * @param order
     *            Byte order.
     * @throws IOException
     *             If the output stream cannot complete the write.
     */
    public static void writeLong(long value, OutputStream stream,
            ByteOrder order) throws IOException {
        byte[] bytes = BYTE_BUFFER.get();
        getBytesFromLong(value, order, bytes);
        stream.write(bytes, 0, BYTES_IN_LONG);
    }

    /**
     * Write the specified float value into the specified output stream in the
     * specified byte order.
     * 
     * @param value
     *            Float value to be written.
     * @param stream
     *            Output stream into which to write the value.
     * @param order
     *            Byte order.
     * @throws IOException
     *             If the output stream cannot complete the write.
     */
    public static void writeFloat(float value, OutputStream stream,
            ByteOrder order) throws IOException {
        byte[] bytes = BYTE_BUFFER.get();
        getBytesFromFloat(value, order, bytes);
        stream.write(bytes, 0, BYTES_IN_FLOAT);
    }

    /**
     * Write the specified double value into the specified output stream in the
     * specified byte order.
     * 
     * @param value
     *            Double value to be written.
     * @param stream
     *            Output stream into which to write the value.
     * @param order
     *            Byte order.
     * @throws IOException
     *             If the output stream cannot complete the write.
     */
    public static void writeDouble(double value, OutputStream stream,
            ByteOrder order) throws IOException {
        byte[] bytes = BYTE_BUFFER.get();
        getBytesFromDouble(value, order, bytes);
        stream.write(bytes, 0, BYTES_IN_DOUBLE);
    }

    /**
     * Write the specified string value into the specified output stream in the
     * specified byte order.
     * 
     * @param value
     *            String value to be written; may be <code>null</code>.
     * @param stream
     *            Output stream into which to write the value.
     * @param order
     *            Byte order.
     * @throws IOException
     *             If the output stream cannot complete the write.
     */
    public static void writeString(String value, OutputStream stream,
            ByteOrder order) throws IOException {
        if ((value == null) || value.isEmpty()) {
            writeInteger(0, stream, order);
            return;
        }
        byte[] stringBytes = value.getBytes();
        writeInteger(stringBytes.length, stream, order);
        stream.write(stringBytes, 0, stringBytes.length);
    }
}
