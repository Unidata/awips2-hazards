/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;

/**
 * Description: Generic registry property adapter for serializing and
 * deserializing {@link GenericRegistryObject#properties} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 02, 2017   38506    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class GenericRegistryPropertyAdapter
        extends XmlAdapter<String, HashMap<String, Serializable>> {

    @SuppressWarnings("unchecked")
    @Override
    public HashMap<String, Serializable> unmarshal(String v)
            throws IOException {
        HashMap<String, Serializable> map = null;
        byte[] bytes = DatatypeConverter.parseBase64Binary(v);
        if (bytes.length > 0) {
            ByteArrayInputStream byteInputStream = null;
            ObjectInputStream objectInputStream = null;
            try {
                byteInputStream = new ByteArrayInputStream(bytes);
                objectInputStream = new ObjectInputStream(byteInputStream);
                map = (HashMap<String, Serializable>) objectInputStream
                        .readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("class problem", e);
            } finally {

                /*
                 * Only close the object stream since its closure will cascade
                 * to the byte stream.
                 */
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return map;
    }

    @Override
    public String marshal(HashMap<String, Serializable> v) throws IOException {
        ByteArrayOutputStream byteOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        if (v == null) {
            return "";
        }
        try {
            byteOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(v);
            objectOutputStream.close();
            return DatatypeConverter
                    .printBase64Binary(byteOutputStream.toByteArray());
        } finally {

            /*
             * Only close the object stream since its closure will cascade to
             * the byte stream.
             */
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
