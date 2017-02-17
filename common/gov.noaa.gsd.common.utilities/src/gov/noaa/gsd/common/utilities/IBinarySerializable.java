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
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Description: Interface describing the methods that must be implemented by a
 * class that is to be serialized and deserialized directly to and from byte
 * streams, without having to go through conventional Java object-based
 * serialization as a {@link Serializable}. Avoiding such standard object-based
 * serialization when possible can be desirable because it tends to be
 * expensive.
 * <p>
 * <strong>Note</strong>: Concrete implementation subclass must include a
 * constructor that takes a {@link ByteArrayInputStream} as its sole parameter,
 * and which creates an instance using the bytes found within said stream.
 * Furthermore, this constructor must expect that the bytes in the stream were
 * created using the {@link #toBinary(OutputStream)} method. The reason a
 * constructor with this signature is required instead of including a
 * <code>fromBinary()</code> method in this interface is that the latter would
 * have required that either fields be mutable (non-<code>final</code>), or some
 * mechanism (reflection, use of the <code>sun.misc.Unsafe</code> class, etc.)
 * be used to modify immutable fields. As for the asymmetry between the
 * non-specific <code>OutputStream</code> required for serialization, versus the
 * specific <code>ByteArrayInputStream</code> required for the deserialization,
 * see the note in {@link PrimitiveAndStringBinaryTranslator}.
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
public interface IBinarySerializable {

    /**
     * Serialize this object to the specified output stream.
     * 
     * @param outputStream
     *            Output stream to which to serialize this object.
     * @throws IOException
     *             if an problem writing out the object to the stream occurs.
     */
    public void toBinary(OutputStream outputStream) throws IOException;
}
