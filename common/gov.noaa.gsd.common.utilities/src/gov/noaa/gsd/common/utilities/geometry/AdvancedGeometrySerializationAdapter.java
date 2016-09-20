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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Description: Thrift serializer to serialize advanced geometries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 02, 2016   15934    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class AdvancedGeometrySerializationAdapter implements
        ISerializationTypeAdapter<IAdvancedGeometry> {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(AdvancedGeometrySerializationAdapter.class);

    @Override
    public void serialize(ISerializationContext serializer,
            IAdvancedGeometry object) throws SerializationException {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
            byte[] serializedBytes = baos.toByteArray();
            serializer.writeBinary(serializedBytes);
        } catch (Exception e) {
            throw new SerializationException("Error serializing Class Object",
                    e);
        } finally {

            /*
             * Only close oos since its closure will cascade to baos.
             */
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    statusHandler.error("Error closing output stream", e);
                }
            }
        }
    }

    @Override
    public IAdvancedGeometry deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        IAdvancedGeometry attr = null;
        byte[] data = deserializer.readBinary();
        if (data.length > 0) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bais);
                attr = (IAdvancedGeometry) ois.readObject();
            } catch (Exception e) {
                throw new SerializationException(
                        "Error deserializing Class Object", e);
            } finally {

                /*
                 * Only close ois since its closure will cascade to bais
                 */
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        statusHandler.error("Error closing input stream", e);
                    }
                }
            }
        }
        return attr;
    }
}
