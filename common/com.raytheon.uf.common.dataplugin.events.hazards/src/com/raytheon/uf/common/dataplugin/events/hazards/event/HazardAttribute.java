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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * A hazard attribute that allows for any value to be added to the registry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Sep 17, 2012            mnash         Initial creation.
 * Nov  04, 2013   2182    Dan.Schaffer  Started refactoring.
 * Nov 14, 2013    1472    bkowal        Remove ISerializableObject.
 * May 29, 2015    6895    Ben.Phillippe Refactored Hazard Service data access.
 * Mar 03, 2016   16145    Chris.Golden  Added ability to handle Geometry
 *                                       objects found within hazard attributes.
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

    // Private Static Constants

    /**
     * Serial version unique ID.
     */
    private static final long serialVersionUID = -7904265699776408418L;

    /**
     * Key begin tag used for parsing maps.
     */
    private static final String KEY_BEGIN_TAG = "<key>";

    /**
     * Key end tag used for parsing maps.
     */
    private static final String KEY_END_TAG = "</key>";

    /**
     * Value begin tag used for parsing maps.
     */
    private static final String VALUE_BEGIN_TAG = "<value>";

    /**
     * Value end tag used for parsing maps.
     */
    private static final String VALUE_END_TAG = "</value>";

    /**
     * Well-Known-Text reader, used for deserializing {@link Geometry} objects.
     * This is thread-local because WKT readers are not thread-safe.
     */
    private static final ThreadLocal<WKTReader> WKT_READER = new ThreadLocal<WKTReader>() {
        @Override
        protected WKTReader initialValue() {
            return new WKTReader();
        }
    };

    // Private Variables

    /**
     * Event identifier of the hazard event associated with this attribute.
     */
    @DynamicSerializeElement
    @XmlElement
    private String eventID;

    /**
     * Attribute key.
     */
    @DynamicSerializeElement
    @XmlElement
    private String key;

    /**
     * Value of the attribute.
     */
    @DynamicSerializeElement
    @XmlElement
    private Object value;

    /**
     * If the value is a collection, map, or geometry, this is the type of the
     * value.
     */
    @DynamicSerializeElement
    @XmlElement
    private Class<?> valueType;

    /**
     * If the value is a collection or map, this stores the type of object
     * stored within the map or collection.
     */
    @DynamicSerializeElement
    @XmlElement
    private Class<? extends Object> collectionValueType;

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public HazardAttribute() {
    }

    /**
     * Construct a standard instance.
     * 
     * @param eventID
     *            Identifier of the hazard event.
     * @param key
     *            Attribute key.
     * @param value
     *            Attribute value.
     */
    public HazardAttribute(String eventID, String key, Object value) {
        this.eventID = eventID;
        this.key = key;
        setValue(value);
    }

    // Public Methods

    @Override
    public boolean isValid() throws ValidationException {
        return eventID != null && key != null & value != null;
    }

    /**
     * Get the value object.
     * 
     * @return Value object.
     */
    public Object getValueObject() {
        Object retVal = value;
        if (valueType != null && (value instanceof String)) {
            if (Geometry.class.isAssignableFrom(valueType)) {
                retVal = unmarshalGeometry((String) value);
            } else if (Map.class.isAssignableFrom(valueType)) {
                retVal = unmarshalMap(collectionValueType, (String) value);
            } else if (List.class.isAssignableFrom(valueType)) {
                retVal = unmarshalList(collectionValueType, (String) value);
            } else if (Set.class.isAssignableFrom(valueType)) {
                retVal = unmarshalSet(collectionValueType, (String) value);
            }
        }
        return retVal;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HazardAttribute other = (HazardAttribute) obj;
        if (collectionValueType == null) {
            if (other.collectionValueType != null) {
                return false;
            }
        } else if (!collectionValueType.equals(other.collectionValueType)) {
            return false;
        }
        if (eventID == null) {
            if (other.eventID != null) {
                return false;
            }
        } else if (!eventID.equals(other.eventID)) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        if (valueType == null) {
            if (other.valueType != null) {
                return false;
            }
        } else if (!valueType.equals(other.valueType)) {
            return false;
        }
        return true;
    }

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
            builder.append("\n\tWithin collection Value Type: ").append(
                    collectionValueType);
        }
        return builder.toString();
    }

    /**
     * Get the hazard event identifier.
     * 
     * @return Hazard event identifier.
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Get the attribute key.
     * 
     * @return Attribute key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the attribute value.
     * 
     * @return Attribute value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the hazard event identifier.
     * 
     * @param eventID
     *            New hazard event identifier.
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Set the attribute key.
     * 
     * @param key
     *            New key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Set the attribute value.
     * 
     * @param value
     *            New value.
     */
    public void setValue(Object value) {
        this.value = value;
        convertValue();
    }

    /**
     * Get the value type.
     * 
     * @return Value type.
     */
    public Class<?> getValueType() {
        return valueType;
    }

    /**
     * Set the value type.
     * 
     * @param valueType
     *            New value type.
     */
    public void setValueType(Class<?> valueType) {
        this.valueType = valueType;
    }

    /**
     * Get the type of the values within the collection or map that comprises
     * the attribute value, if the latter is the case.
     * 
     * @return Type of the values within the collection or map that comprises
     *         the attribute value, or <code>null</code> if the attribute value
     *         is not a collection or map.
     */
    public Class<? extends Object> getCollectionValueType() {
        return collectionValueType;
    }

    /**
     * Set the type of the values within the collection or map that comprises
     * the attribute value, if the latter is the case.
     * 
     * @param collectionValueType
     *            New collection value type.
     */
    public void setCollectionValueType(
            Class<? extends Object> collectionValueType) {
        this.collectionValueType = collectionValueType;
    }

    // Private Methods

    /**
     * Convert the value to a string.
     */
    @SuppressWarnings("unchecked")
    private void convertValue() {
        if (value instanceof Geometry) {
            valueType = value.getClass();
            value = marshalGeometry((Geometry) value);
        } else if (value instanceof Collection) {
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
     * Marshal the specified map to a string.
     * 
     * @param map
     *            Map to be marshalled.
     * @return Marshalled map.
     */
    private String marshalMap(Map<String, ?> map) {
        StringBuilder str = new StringBuilder();
        for (Entry<String, ?> entry : map.entrySet()) {
            str.append(KEY_BEGIN_TAG).append(entry.getKey())
                    .append(KEY_END_TAG);
            str.append(VALUE_BEGIN_TAG).append(marshal(entry.getValue()))
                    .append(VALUE_END_TAG);
            str.append("\n");
        }
        ;
        return str.toString();
    }

    /**
     * Unmarshal a map from the specified string.
     * 
     * @param collectionValueType
     *            Type of object stored in the map.
     * @param str
     *            Marshalled string.
     * @return Hash map.
     */
    private HashMap<String, ? extends Object> unmarshalMap(
            Class<? extends Object> collectionValueType, String str) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String[] lines = str.split(VALUE_END_TAG + "\n");
        for (String line : lines) {
            String[] keyVal = line.split(KEY_END_TAG + VALUE_BEGIN_TAG);
            String value = keyVal.length == 2 ? keyVal[1] : null;
            map.put(keyVal[0].replace(KEY_BEGIN_TAG, ""),
                    unmarshal(collectionValueType, value));
        }

        return map;
    }

    /**
     * Marshal the specified collection to a string.
     * 
     * @param collection
     *            Collection to be marshalled.
     * @return Marshalled collection.
     */
    private String marshalCollection(Collection<?> collection) {
        StringBuilder str = new StringBuilder();
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            str.append(VALUE_BEGIN_TAG).append(marshal(iterator.next()))
                    .append(VALUE_END_TAG);
            str.append("\n");
        }

        return str.toString();
    }

    /**
     * Unmarshal a set from the specified string.
     * 
     * @param collectionValueType
     *            Type of object stored in the set.
     * @param str
     *            Marshalled string.
     * @return Hash set.
     */
    @SuppressWarnings("unchecked")
    private HashSet<? extends Object> unmarshalSet(
            Class<? extends Object> collectionValueType, String str) {
        return unmarshalCollection(HashSet.class, collectionValueType, str);
    }

    /**
     * Unmarshal a list from the specified string.
     * 
     * @param collectionValueType
     *            Type of object stored in the list.
     * @param str
     *            Marshalled string.
     * @return Array list.
     */
    @SuppressWarnings("unchecked")
    private ArrayList<? extends Object> unmarshalList(
            Class<? extends Object> collectionValueType, String str) {
        return unmarshalCollection(ArrayList.class, collectionValueType, str);
    }

    /**
     * Unmarshal a collection from the specified string.
     * 
     * @param type
     *            Type of collection.
     * @param collectionValueType
     *            Type of object object stored in the collection.
     * @param str
     *            Marshalled string.
     * @return Collection.
     */
    private <T extends Collection<Object>> T unmarshalCollection(Class<T> type,
            Class<? extends Object> collectionValueType, String str) {
        try {
            T coll = type.newInstance();
            String[] lines = str.split(VALUE_END_TAG + "\n");
            for (String line : lines) {
                coll.add(unmarshal(collectionValueType,
                        line.replace(VALUE_BEGIN_TAG, "")));
            }
            return coll;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Marshal the specified object to a string.
     * 
     * @param object
     *            Object to be converted.
     * @return String version of the object.
     */
    private String marshal(Object object) {
        if (object instanceof Geometry) {
            return marshalGeometry((Geometry) object);
        }
        return (object == null ? null : object.toString());
    }

    /**
     * Unmarshal the specified string into the specified type of object.
     * 
     * @param clazz
     *            Type of object into which to unmarshal the string.
     * @param str
     *            Marshalled string.
     * @return Converted object.
     */
    private Object unmarshal(Class<? extends Object> clazz, String str) {
        if (clazz == null) {
            return null;
        }
        Object retVal = null;
        if (String.class.equals(clazz)) {
            retVal = str;
        } else if (Integer.class.equals(clazz)) {
            retVal = Integer.parseInt(str);
        } else if (Float.class.equals(clazz)) {
            retVal = Float.parseFloat(str);
        } else if (Double.class.equals(clazz)) {
            retVal = Double.parseDouble(str);
        } else if (Long.class.equals(clazz)) {
            retVal = Long.parseLong(str);
        } else if (Date.class.equals(clazz)) {
            retVal = new Date(Long.parseLong(str));
        } else if (Geometry.class.isAssignableFrom(clazz)) {
            retVal = unmarshalGeometry(str);
        }
        return retVal;
    }

    /**
     * Marshal the specified geometry into Well-Known-Text format.
     * 
     * @param geometry
     *            Geometry to be marshaled.
     * @return String holding the geometry as Well-Known-Text.
     */
    private String marshalGeometry(Geometry geometry) {
        return geometry.toText();
    }

    /**
     * Unmarshal the specified Well-Known-Text representation of a geometry into
     * a geometry object.
     * 
     * @param geometry
     *            String holding the geometry in Well-Known-Text format.
     * @return Geometry.
     */
    private Geometry unmarshalGeometry(String text) {
        try {
            return WKT_READER.get().read(text);
        } catch (ParseException e) {
            throw new IllegalStateException(
                    "could not unmarshal Well-Known-Text geometry", e);
        }
    }
}
