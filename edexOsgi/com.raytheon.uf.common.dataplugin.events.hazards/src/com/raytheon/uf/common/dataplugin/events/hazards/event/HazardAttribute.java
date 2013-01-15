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
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Strings;
import com.raytheon.uf.common.dataplugin.events.IValidator;
import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.annotations.SlotAttribute;
import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * A hazard attribute that allows for any value to be added to the registry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 17, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@XmlRootElement(name = "attributes")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@RegistryObject({ HazardConstants.EVENTID, "key" })
public class HazardAttribute implements IHazardAttribute, ISerializableObject,
        IValidator {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardAttribute.class);

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.EVENTID)
    private String eventId;

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute
    private String key;

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute
    private byte[] valueSerializable;

    /**
     * 
     */
    public HazardAttribute() {
    }

    /**
     * 
     */
    public HazardAttribute(String eventId, String key, Serializable value) {
        this.eventId = eventId;
        this.key = key;
        ObjectOutputStream stream = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        try {
            stream = new ObjectOutputStream(bStream);
            stream.writeObject(value);
        } catch (IOException e) {
            statusHandler.handle(Priority.ERROR, "Unable to write object", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    statusHandler.handle(Priority.DEBUG,
                            e.getLocalizedMessage(), e);
                }
            }
        }

        this.valueSerializable = bStream.toByteArray();
    }

    /**
     * @return the eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @param eventId
     *            the eventId to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardAttributes
     * #getKey()
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardAttributes
     * #getValue()
     */
    @Override
    public Serializable getValue() {
        ObjectInputStream stream = null;
        ByteArrayInputStream bStream = new ByteArrayInputStream(
                this.valueSerializable);
        Serializable ser = null;
        try {
            stream = new ObjectInputStream(bStream);
            ser = (Serializable) stream.readObject();
        } catch (IOException e) {
            statusHandler.handle(Priority.ERROR, e.getLocalizedMessage(), e);
        } catch (ClassNotFoundException e) {
            statusHandler.handle(Priority.ERROR, e.getLocalizedMessage(), e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    statusHandler.handle(Priority.DEBUG,
                            e.getLocalizedMessage(), e);
                }
            }
        }
        return ser;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(Serializable value) {
        ObjectOutputStream stream = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        try {
            stream = new ObjectOutputStream(bStream);
            stream.writeObject(value);
        } catch (IOException e) {
            statusHandler.handle(Priority.ERROR, e.getLocalizedMessage(), e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    statusHandler.handle(Priority.DEBUG,
                            e.getLocalizedMessage(), e);
                }
            }
        }
        this.valueSerializable = bStream.toByteArray();
    }

    /**
     * @return the valueSerializable
     */
    public byte[] getValueSerializable() {
        return valueSerializable;
    }

    /**
     * @param valueSerializable
     *            the valueSerializable to set
     */
    public void setValueSerializable(byte[] valueSerializable) {
        this.valueSerializable = valueSerializable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.IValidator#isValid()
     */
    @Override
    public boolean isValid() throws ValidationException {
        if (Strings.isNullOrEmpty(key) == false
                && Strings.isNullOrEmpty(eventId) == false
                && valueSerializable != null) {
            return true;
        }
        throw new ValidationException("Unable to validate " + key.toString());
    }
}
