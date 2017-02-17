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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardAttribute;
import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Thrift serializer to serialize the Hazard Attributes
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardAttributeSerializationAdapter implements
        ISerializationTypeAdapter<HazardAttribute> {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardAttributeSerializationAdapter.class);

    @Override
    public void serialize(ISerializationContext serializer,
            HazardAttribute object) throws SerializationException {
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
            // Only close oos since its closure will cascade to baos
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
    public HazardAttribute deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        HazardAttribute attr = null;
        byte[] data = deserializer.readBinary();
        if (data.length > 0) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bais);
                attr = (HazardAttribute) ois.readObject();
            } catch (Exception e) {
                throw new SerializationException(
                        "Error deserializing Class Object", e);
            } finally {
                // Only close ois since its closure will cascade to bais
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
