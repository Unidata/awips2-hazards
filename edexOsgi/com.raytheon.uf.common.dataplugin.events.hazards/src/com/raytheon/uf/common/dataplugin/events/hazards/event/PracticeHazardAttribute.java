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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Strings;
import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * An attribute for a practice hazard, written to the database in a separate
 * key-value pair table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@Entity
@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@Table(name = "practice_hazard_attributes")
@SequenceGenerator(name = "ATTRIBUTE_SEQ", sequenceName = "attribute_sequence", allocationSize = 1)
public class PracticeHazardAttribute extends PersistableDataObject implements
        IHazardAttribute, ISerializableObject {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(PracticeHazardAttribute.class);

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ATTRIBUTE_SEQ")
    private int generatedId;

    @Column
    @DynamicSerializeElement
    @XmlElement
    @PrimaryKeyJoinColumn
    private PracticeHazardEventPK id;

    @Column(updatable = true)
    @DynamicSerializeElement
    @XmlElement
    private String key;

    @Column
    @DynamicSerializeElement
    @XmlElement
    private byte[] valueSerializable;

    public PracticeHazardAttribute() {
    }

    /**
     * 
     */
    public PracticeHazardAttribute(PracticeHazardEventPK eventId, String key,
            Serializable value) {
        this.id = eventId;
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
     * @return the generatedId
     */
    public int getGeneratedId() {
        return generatedId;
    }

    /**
     * @param generatedId
     *            the generatedId to set
     */
    public void setGeneratedId(int generatedId) {
        this.generatedId = generatedId;
    }

    /**
     * @return the id
     */
    public PracticeHazardEventPK getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(PracticeHazardEventPK id) {
        this.id = id;
    }

    /**
     * @return the eventId
     */
    public String getEventId() {
        return id.getEventId();
    }

    /**
     * @param eventId
     *            the eventId to set
     */
    public void setEventId(String eventId) {
        this.id.setEventId(eventId);
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
        if (Strings.isNullOrEmpty(key) == false && id != null
                && valueSerializable != null) {
            return true;
        }
        throw new ValidationException("Unable to validate " + key.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime
                * result
                + ((valueSerializable == null) ? 0 : valueSerializable
                        .hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof PracticeHazardAttribute == false)
            return false;
        PracticeHazardAttribute other = (PracticeHazardAttribute) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (valueSerializable == null) {
            if (other.valueSerializable != null)
                return false;
        } else if (!valueSerializable.equals(other.valueSerializable))
            return false;
        return true;
    }
}