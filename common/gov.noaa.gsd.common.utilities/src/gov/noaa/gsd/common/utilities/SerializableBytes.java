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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Description: Serializable array of bytes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 08, 2017   28892    Chris.Golden Initial creation (taken from
 *                                      former inner class of
 *                                      GeometryWrapper).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SerializableBytes implements Serializable {

    // Private Static Constants

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = -2764065826671495656L;

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
     * Write out the object for serialization purposes. The length of the byte
     * array is written, then the array itself if not zero-length.
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
     * Read in the object for deserialization purposes. The length of the byte
     * array is read, then the array itself if the length is not zero.
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
         * Read in the length needed, and then if it is greater than zero, read
         * in the bytes. Multiple passes may be needed to read in the entire
         * buffer, as the stream's read() methods are not guaranteed to return
         * all the bytes in one pass.
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
