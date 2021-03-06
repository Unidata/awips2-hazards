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

import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;

/**
 * Description: Thrift serializer to serialize advanced geometries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 02, 2016   15934    Chris.Golden Initial creation.
 * Feb 13, 2017   28892    Chris.Golden Changed to use the binary translator
 *                                      to do the marshaling and unmarshaling.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class AdvancedGeometrySerializationAdapter implements
        ISerializationTypeAdapter<IAdvancedGeometry> {

    @Override
    public void serialize(ISerializationContext serializer,
            IAdvancedGeometry object) throws SerializationException {
        try {
            serializer.writeBinary(AdvancedGeometryBinaryTranslator
                    .serializeToCompressedBytes(object));
        } catch (IOException e) {
            throw new SerializationException("problem during serialization", e);
        }
    }

    @Override
    public IAdvancedGeometry deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        try {
            return AdvancedGeometryBinaryTranslator
                    .deserializeFromCompressedBytes(deserializer.readBinary());
        } catch (IOException e) {
            throw new SerializationException("problem during deserialization",
                    e);
        }
    }
}
