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

import java.io.IOException;

import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;

/**
 * Description: Thrift serializer to serialize and deserialize
 * {@link VisualFeaturesList} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 02, 2016   18094    Chris.Golden Initial creation.
 * Feb 10, 2017   28892    Chris.Golden Changed to work on lists of
 *                                      visual features instead of just
 *                                      one visual feature at a time,
 *                                      which included changing the
 *                                      name. Changed implementation to
 *                                      use the new binary translator.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisualFeaturesListSerializationAdapter implements
        ISerializationTypeAdapter<VisualFeaturesList> {

    @Override
    public void serialize(ISerializationContext serializer,
            VisualFeaturesList object) throws SerializationException {
        try {
            serializer.writeBinary(VisualFeaturesListBinaryTranslator
                    .serializeToCompressedBytes(object));
        } catch (IOException e) {
            throw new SerializationException("problem during serialization", e);
        }
    }

    @Override
    public VisualFeaturesList deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        try {
            return VisualFeaturesListBinaryTranslator
                    .deserializeFromCompressedBytes(deserializer.readBinary());
        } catch (IOException e) {
            throw new SerializationException("problem during deserialization",
                    e);
        }
    }
}
