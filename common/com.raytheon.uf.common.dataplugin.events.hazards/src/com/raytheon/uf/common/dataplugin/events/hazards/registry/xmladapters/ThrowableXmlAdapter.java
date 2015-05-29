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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * 
 * Thrift serializer to serialize a list of Throwables
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
public class ThrowableXmlAdapter extends XmlAdapter<String, List<Throwable>> {

    private HexBinaryAdapter hexAdapter = new HexBinaryAdapter();

    @SuppressWarnings("unchecked")
    @Override
    public List<Throwable> unmarshal(String v) throws Exception {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        
        try{
            byte[] serializedBytes = hexAdapter.unmarshal(v);
            bais = new ByteArrayInputStream(serializedBytes);
            ois = new ObjectInputStream(bais);
            List<Throwable> result = (List<Throwable>) ois.readObject();
            return result;
        }finally{
            if(ois != null){
                ois.close();
            }
        }

    }

    @Override
    public String marshal(List<Throwable> v) throws Exception {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(v);
            byte[] serializedBytes = baos.toByteArray();
            return hexAdapter.marshal(serializedBytes);
        } finally {
            if(oos != null){
                oos.close();
            }
        }

    }

}
