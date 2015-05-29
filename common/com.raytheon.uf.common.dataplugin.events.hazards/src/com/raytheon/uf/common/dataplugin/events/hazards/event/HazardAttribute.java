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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.IValidator;
import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters.HazardAttributeSerializationAdapter;
import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;

/**
 * A hazard attribute that allows for any value to be added to the registry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 17, 2012            mnash       Initial creation
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 14, 2013 1472       bkowal      Remove ISerializableObject
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@XmlRootElement(name = "HazardAttribute")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = HazardAttributeSerializationAdapter.class)
@RegistryObject({ HazardConstants.HAZARD_EVENT_IDENTIFIER, "key" })
public class HazardAttribute implements IValidator, Serializable {

    /** Serial version unique ID */
    private static final long serialVersionUID = -7904265699776408418L;

    /** Key begin tag used for parsing maps */
    private static final String KEY_BEGIN_TAG = "<key>";

    /** Key end tag used for parsing maps */
    private static final String KEY_END_TAG = "</key>";

    /** Value begin tag used for parsing maps */
    private static final String VALUE_BEGIN_TAG = "<value>";

    /** Value end tag used for parsing maps */
    private static final String VALUE_END_TAG = "</value>";

    /** Event ID of the Hazard Event associated with this attribute */
    @DynamicSerializeElement
    @XmlElement
    private String eventID;

    /** The attribute key */
    @DynamicSerializeElement
    @XmlElement
    private String key;

    /** The value of the attribute */
    @DynamicSerializeElement
    @XmlElement
    private Object value;

    /**
     * If the value is a collection or map, this is object type stored within
     * the map or collection
     */
    @DynamicSerializeElement
    @XmlElement
    private Class<?> valueType;

    /**
     * If the value is a collection or map, this stores the type of collection
     * or map
     */
    @DynamicSerializeElement
    @XmlElement
    private Class<? extends Object> collectionValueType;

    /**
     * Creates an empty HazardAttribute
     */
    public HazardAttribute() {

    }

    /**
     * Creates a new HazardAttribute for the event with the provided ID
     * 
     * @param eventID
     *            The ID of the HazardEvent
     * @param key
     *            The attribute key
     * @param value
     *            The attribute value
     */
    public HazardAttribute(String eventID, String key, Object value) {
        this.eventID = eventID;
        this.key = key;
        setValue(value);
    }

    public boolean isValid() throws ValidationException {
        return eventID != null && key != null & value != null;
    }

    /**
     * Sets the collectionValueType
     * 
     * @param collectionValueType
     *            the collectionValueType to set
     */
    public void setCollectionValueType(
            Class<? extends Object> collectionValueType) {
        this.collectionValueType = collectionValueType;
    }

    /**
     * Gets the value object
     * 
     * @return The value object
     */
    public Object getValueObject() {
        Object retVal = value;
        if (valueType != null && (value instanceof String)) {
            if (Map.class.isAssignableFrom(valueType)) {
                retVal = unmarshalMap(collectionValueType, (String) value);
            } else if (List.class.isAssignableFrom(valueType)) {
                retVal = unmarshalList(collectionValueType, (String) value);
            } else if (Set.class.isAssignableFrom(valueType)) {
                retVal = unmarshalSet(collectionValueType, (String) value);
            }
        }
        return retVal;
    }

    /**
     * Converts the value to a String
     */
    @SuppressWarnings("unchecked")
    private void convertValue() {
        if (value instanceof Collection) {
            valueType = value.getClass();
            Collection<Object> coll = (Collection<Object>) value;
            if (!coll.isEmpty()) {
                Object obj = coll.iterator().next();
                if (obj != null) {
                    collectionValueType = obj.getClass();
                }
            }
            value = marshalCollection(coll);
        } else if (value instanceof Map) {
            valueType = value.getClass();
            Map<String, ?> map = (Map<String, ?>) value;
            if (!map.isEmpty()) {
                Object obj = map.values().iterator().next();
                if (obj != null) {
                    collectionValueType = obj.getClass();
                }
            }
            value = marshalMap(map);
        }
    }

    /**
     * Unmarshals a map
     * 
     * @param collectionValueType
     *            The type of object stored in the map
     * @param str
     *            The marshalled String
     * @return A HashMap
     */
    private HashMap<String, ? extends Object> unmarshalMap(
            Class<? extends Object> collectionValueType, String str) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String[] lines = str.split(VALUE_END_TAG + "\n");
        for (String line : lines) {
            String[] keyVal = line.split(KEY_END_TAG + VALUE_BEGIN_TAG);
            String value = keyVal.length == 2 ? keyVal[1] : null;
            map.put(keyVal[0].replace(KEY_BEGIN_TAG, ""),
                    convert(collectionValueType, value));
        }

        return map;
    }

    /**
     * Marshals a map
     * 
     * @param map
     *            The map to marshal
     * @return The marshalled map
     */
    private String marshalMap(Map<String, ?> map) {
        StringBuilder str = new StringBuilder();
        for (Entry<String, ?> entry : map.entrySet()) {
            str.append(KEY_BEGIN_TAG).append(entry.getKey())
                    .append(KEY_END_TAG);
            str.append(VALUE_BEGIN_TAG).append(entry.getValue())
                    .append(VALUE_END_TAG);
            str.append("\n");
        }
        ;
        return str.toString();
    }

    /**
     * Marshals a collection
     * 
     * @param coll
     *            The collection to marshal
     * @return
     */
    private String marshalCollection(Collection<?> coll) {
        StringBuilder str = new StringBuilder();
        Iterator<?> iterator = coll.iterator();
        while (iterator.hasNext()) {
            str.append(VALUE_BEGIN_TAG).append(iterator.next())
                    .append(VALUE_END_TAG);
            str.append("\n");
        }

        return str.toString();
    }

    /**
     * Unmarshals a set
     * 
     * @param collectionValueType
     *            The type of items stored in the set
     * @param str
     *            The marshalled set
     * @return A HashSet
     */
    @SuppressWarnings("unchecked")
    private HashSet<? extends Object> unmarshalSet(
            Class<? extends Object> collectionValueType, String str) {
        return unmarshalCollection(HashSet.class, collectionValueType, str);
    }

    /**
     * Unmarshals a List
     * 
     * @param collectionValueType
     *            The type of object in the List
     * @param str
     *            The marshalled List
     * @return An ArrayList
     */
    @SuppressWarnings("unchecked")
    private ArrayList<? extends Object> unmarshalList(
            Class<? extends Object> collectionValueType, String str) {
        return unmarshalCollection(ArrayList.class, collectionValueType, str);
    }

    /**
     * General method to unmarshal a collection
     * 
     * @param type
     *            The type of collection
     * @param collectionValueType
     *            The type object object stored in the collection
     * @param str
     *            The marshalled collection
     * @return The unmarshalled collection
     */
    private <T extends Collection<Object>> T unmarshalCollection(Class<T> type,
            Class<? extends Object> collectionValueType, String str) {
        try {
            T coll = (T) type.newInstance();
            String[] lines = str.split(VALUE_END_TAG + "\n");
            for (String line : lines) {
                coll.add(convert(collectionValueType,
                        line.replace(VALUE_BEGIN_TAG, "")));
            }
            return coll;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Converts a String to a different object type
     * 
     * @param clazz
     *            The class to convert it to
     * @param s
     *            The string
     * @return The converted object
     */
    private Object convert(Class<? extends Object> clazz, String s) {
        Object retVal = null;
        if (String.class.equals(clazz)) {
            retVal = s;
        } else if (Integer.class.equals(clazz)) {
            retVal = Integer.parseInt(s);
        } else if (Float.class.equals(clazz)) {
            retVal = Float.parseFloat(s);
        } else if (Double.class.equals(clazz)) {
            retVal = Double.parseDouble(s);
        } else if (Long.class.equals(clazz)) {
            retVal = Long.parseLong(s);
        } else if (Date.class.equals(clazz)) {
            retVal = new Date(Long.parseLong(s));
        }
        return retVal;
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
        result = prime
                * result
                + ((collectionValueType == null) ? 0 : collectionValueType
                        .hashCode());
        result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result
                + ((valueType == null) ? 0 : valueType.hashCode());
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
        if (getClass() != obj.getClass())
            return false;
        HazardAttribute other = (HazardAttribute) obj;
        if (collectionValueType == null) {
            if (other.collectionValueType != null)
                return false;
        } else if (!collectionValueType.equals(other.collectionValueType))
            return false;
        if (eventID == null) {
            if (other.eventID != null)
                return false;
        } else if (!eventID.equals(other.eventID))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (valueType == null) {
            if (other.valueType != null)
                return false;
        } else if (!valueType.equals(other.valueType))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HazardAttribute:");
        builder.append("\n\t  Key: ").append(key);
        builder.append("\n\tValue: ").append(value);
        if (valueType != null) {
            builder.append("\n\tValue Type: ").append(valueType);
        }
        if (collectionValueType != null) {
            builder.append("\n\tCollection Value Type: ").append(
                    collectionValueType);
        }
        return builder.toString();
    }

    /**
     * Gets the eventID
     * 
     * @return The eventID
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Gets the key
     * 
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value
     * 
     * @return The value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the eventID
     * 
     * @param eventID
     *            the eventID to set
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Sets the key
     * 
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Sets the value
     * 
     * @param value
     *            the value to set
     */
    public void setValue(Object value) {
        this.value = value;
        convertValue();
    }

    /**
     * Gets the valueType
     * 
     * @return the valueType
     */
    public Class<?> getValueType() {
        return valueType;
    }

    /**
     * Sets the valueType
     * 
     * @param valueType
     *            the valueType to set
     */
    public void setValueType(Class<?> valueType) {
        this.valueType = valueType;
    }

    /**
     * Gets the collectionValueType
     * 
     * @return the collectionValueType
     */
    public Class<? extends Object> getCollectionValueType() {
        return collectionValueType;
    }

}
