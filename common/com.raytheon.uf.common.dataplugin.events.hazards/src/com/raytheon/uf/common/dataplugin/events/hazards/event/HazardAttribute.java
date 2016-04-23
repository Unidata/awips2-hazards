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

import gov.noaa.gsd.common.utilities.JSONConverter;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
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
 * <p>
 * A hazard attribute that allows for any value to be added to the registry.
 * </p>
 * <p>
 * Note that if the value is a collection or map that only has values of a
 * single basic type (<code>String</code>, <code>Integer</code>,
 * <code>Boolean</code>, etc.), then it tracks each such value within the
 * collection or map in such a way that they will show up as slots during slot
 * conversion. If instead, however, the value is a collection or map that itself
 * has (a) heterogeneous value types (e.g. <code>String</code> for one element,
 * <code>Integer</code> for another), (b) values that are themselves nested
 * collections or maps, or (c) both (a) and (b), then there is no such tracking.
 * The attribute will serialize and deserialize properly, but slot conversion
 * will not create slots for the values in the collection or map.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Sep 17, 2012            mnash         Initial creation.
 * Nov 04, 2013    2182    Dan.Schaffer  Started refactoring.
 * Nov 14, 2013    1472    bkowal        Remove ISerializableObject.
 * May 29, 2015    6895    Ben.Phillippe Refactored Hazard Service data access.
 * Mar 03, 2016   16145    Chris.Golden  Added ability to handle Geometry
 *                                       objects found within hazard attributes.
 * Apr 23, 2016   18094    Chris.Golden  Extensively altered to allow values that
 *                                       consist of a collection or map with
 *                                       heterogeneous types of elements within
 *                                       it, and/or nested collections and maps,
 *                                       to be serialized and deserialized
 *                                       properly. The leaf nodes in such values
 *                                       may be any of the basic types (String,
 *                                       Integer, Boolean, etc.), or Date, or
 *                                       a subclass of Geometry.
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

    // Private Enumerated Types

    /**
     * Type of substitution to be done when postprocessing a value after it has
     * been deserialized within
     * {@link #postprocessValuesAfterDeserialization(Object)}.
     */
    private enum Substitution {
        GEOMETRY, SET
    };

    // Private Interfaces

    /**
     * Interface describing the method that must be implemented in order for a
     * class to act as a deserializer.
     */
    private interface IDeserializer {

        /**
         * Deserialize the specified string.
         * 
         * @param string
         *            String to be deserialized.
         * @return Deserialization result.
         */
        public Object deserialize(String string);
    }

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
     * JSON converter. This object is thread-safe, as stated <a
     * href="http://wiki.fasterxml.com/JacksonFAQ#Data_Binding.2C_general"
     * >here</a>, as long as it is not configured during serialization or
     * deserialization.
     */
    private static final JSONConverter JSON_CONVERTER = new JSONConverter();

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

    /**
     * Map pairing simple class types with their unmarshalling methods. A
     * "simple" class is considered to be any that are found as keys in this
     * map.
     */
    private static final Map<Class<?>, IDeserializer> DESERIALIZERS_FOR_SIMPLE_TYPES;
    static {
        Map<Class<?>, IDeserializer> map = new HashMap<>(7, 1.0f);
        map.put(String.class, new IDeserializer() {

            @Override
            public Object deserialize(String string) {
                return string;
            }
        });
        map.put(Boolean.class, new IDeserializer() {

            @Override
            public Object deserialize(String string) {
                return Boolean.parseBoolean(string);
            }
        });
        map.put(Integer.class, new IDeserializer() {

            @Override
            public Object deserialize(String string) {
                return Integer.parseInt(string);
            }
        });
        map.put(Long.class, new IDeserializer() {

            @Override
            public Object deserialize(String string) {
                return Long.parseLong(string);
            }
        });
        map.put(Float.class, new IDeserializer() {

            @Override
            public Object deserialize(String string) {
                return Float.parseFloat(string);
            }
        });
        map.put(Double.class, new IDeserializer() {

            @Override
            public Object deserialize(String string) {
                return Double.parseDouble(string);
            }
        });
        map.put(Date.class, new IDeserializer() {

            @Override
            public Object deserialize(String string) {
                return new Date(Long.parseLong(string));
            }
        });
        DESERIALIZERS_FOR_SIMPLE_TYPES = ImmutableMap.copyOf(map);
    }

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
     * If the value is a collection or map, this is the type of the value.
     */
    @DynamicSerializeElement
    @XmlElement
    private Class<?> valueType;

    /**
     * If the value is a collection or map and the attribute is slottable, this
     * stores the type of object stored within the map or collection.
     */
    @DynamicSerializeElement
    @XmlElement
    private Class<? extends Object> collectionValueType;

    /**
     * List of paths to {@link Set} objects in the original {@link #value}. Each
     * path is a string consisting of substrings and/or integers separated by
     * newlines (avoiding the use of a nested list in order to make
     * serialization easier); each substring is a key into a {@link Map},
     * whereas each integer is the index into a {@link List}. As an example, the
     * path:<code><pre>     foo
     *     3
     *     bar</pre></code> indicates that the top-level value is a
     * <code>Map</code>, and that its key "foo" is associated with a
     * <code>List</code> as a value; the 3rd element within the list is a nested
     * <code>Map</code>; and the collection found as the value associated with
     * the key "bar" in this map was originally a <code>Set</code>.
     */
    @DynamicSerializeElement
    @XmlElement
    private final List<String> pathsToSets = new ArrayList<>();

    /**
     * List of paths to {@link Geometry} objects in the original {@link #value}.
     * Each path is a string consisting of substrings and/or integers separated
     * by newlines (avoiding the use of a nested list in order to make
     * serialization easier); each substring is a key into a {@link Map},
     * whereas each integer is the index into a {@link List}. As an example, the
     * path: <code><pre>     20
     *     baz</pre></code> indicates that the top-level value is a
     * <code>List</code>; that the 20th element in said list is a
     * <code>Map</code>; and the {@link String} found as the value associated
     * with the key "baz" in this map was originally a <code>Geometry</code>.
     */
    @DynamicSerializeElement
    @XmlElement
    private final List<String> pathsToGeometries = new ArrayList<>();

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
        setKey(key);
        setValue(value);
    }

    // Public Methods

    @Override
    public boolean isValid() throws ValidationException {
        return ((eventID != null) && (key != null) & (value != null));
    }

    /**
     * Get the value object, that is, the deserialized version.
     * 
     * @return Value object.
     */
    public Object getValueObject() {

        /*
         * If the value type was recorded, then the value is currently
         * serialized as a string, so deserialize and return it.
         */
        if (valueType != null) {

            /*
             * If the collection value type was recorded, then the value was
             * serialized in a slottable manner; deserialize it accordingly.
             * Otherwise, it was serialized as JSON, so deserialize the latter.
             */
            Object result = null;
            if (collectionValueType != null) {
                if (Map.class.isAssignableFrom(valueType)) {
                    result = unmarshalMap(collectionValueType, (String) value);
                } else {
                    result = unmarshalList(collectionValueType, (String) value);
                }
            } else {
                try {
                    result = JSON_CONVERTER.fromJson((String) value);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "internal error while deserializing JSON", e);
                }
            }

            /*
             * Perform any substitutions of geometries for strings, and/or sets
             * for lists, that are needed, then return the result.
             */
            return postprocessValueAfterDeserialization(result);
        }

        /*
         * Return the value itself, since it was not serialized.
         */
        return value;
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
     * Get the paths to any sets.
     * 
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as the property being fetched is internal state.
     * </p>
     * 
     * @return Paths to any sets.
     */
    public List<String> getPathsToSets() {
        return pathsToSets;
    }

    /**
     * Get the paths to any geometries.
     * 
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as the property being fetched is internal state.
     * </p>
     * 
     * @return Paths to any geometries.
     */
    public List<String> getPathsToGeometries() {
        return pathsToGeometries;
    }

    /**
     * Set the hazard event identifier.
     * 
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as setting this property may leave the object in an
     * undefined state.
     * </p>
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
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as setting this property may leave the object in an
     * undefined state.
     * </p>
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
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; would otherwise be private, as
     * setting this property outside this class may leave the object in an
     * undefined state.
     * </p>
     * 
     * @param value
     *            New value.
     */
    public void setValue(Object value) {
        if (isValueValid(value) == false) {
            throw new IllegalArgumentException("bad attribute \"" + key
                    + "\": attribute value must be of a "
                    + "simple type, or else a map or "
                    + "collection holding simple types "
                    + "and/or nested maps or collections "
                    + "that in turn must follow the same rules");
        }
        this.value = value;
        convertValue();
    }

    /**
     * Set the paths to any sets.
     * 
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as setting this property may leave the object in an
     * undefined state.
     * </p>
     * 
     * @param pathsToSets
     *            New paths to any sets.
     */
    public void setPathsToSets(List<String> pathsToSets) {
        this.pathsToSets.clear();
        this.pathsToSets.addAll(pathsToSets);
    }

    /**
     * Set the paths to any geometries.
     * 
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as setting this property may leave the object in an
     * undefined state.
     * </p>
     * 
     * @param pathsToGeometries
     *            New paths to any geometries.
     */
    public void setPathsToGeometries(List<String> pathsToGeometries) {
        this.pathsToGeometries.clear();
        this.pathsToGeometries.addAll(pathsToGeometries);
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
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as setting this property may leave the object in an
     * undefined state.
     * </p>
     * 
     * @param valueType
     *            New value type.
     */
    public void setValueType(Class<?> valueType) {
        this.valueType = valueType;
    }

    /**
     * Get the type of the values within the collection or map that comprises
     * the attribute value, if the latter is the case and if the attribute is
     * slottable.
     * 
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as the property being fetched is internal state.
     * </p>
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
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as setting this property may leave the object in an
     * undefined state.
     * </p>
     * 
     * @param collectionValueType
     *            New collection value type; if not <code>null</code>, the
     *            attribute will be considered slottable.
     */
    public void setCollectionValueType(
            Class<? extends Object> collectionValueType) {
        this.collectionValueType = collectionValueType;
    }

    /**
     * Determine whether or not the attribute is slottable.
     * 
     * @return True if the attribute is slottable, false otherwise.
     */
    public boolean isSlottable() {

        /*
         * The value is slottable if the primary type is not recorded, meaning
         * it is not a container (map or collection), or if it is recorded and
         * its contained elements all have the same simple type.
         */
        return ((valueType == null) || (collectionValueType != null));
    }

    // Private Methods

    /**
     * Convert the value to something serializable.
     */
    private void convertValue() {

        /*
         * If the value is or contains any Set or Geometry objects, replace
         * them, making a note of their positions so that they can be restored
         * when unmarshalling.
         */
        pathsToSets.clear();
        pathsToGeometries.clear();
        if (isValueTypeOrContainingType(value, Set.class)
                || isValueTypeOrContainingType(value, Geometry.class)) {
            value = preprocessValueForSerialization(value,
                    new ArrayDeque<Serializable>());
        }

        /*
         * If the value is slottable, marshal it so that its individual elements
         * may each have a slot generated for them. Otherwise, simply generate
         * JSON as the marshalled version.
         */
        if (isValueObjectSlottable()) {
            if (value instanceof Collection) {
                valueType = value.getClass();
                Collection<?> coll = (Collection<?>) value;
                if (!coll.isEmpty()) {
                    Object obj = coll.iterator().next();
                    if (obj != null) {
                        collectionValueType = getSimpleType(obj);
                    }
                }
                value = marshalCollection(coll);
            } else if (value instanceof Map) {
                valueType = value.getClass();
                Map<?, ?> map = (Map<?, ?>) value;
                if (!map.isEmpty()) {
                    Object obj = map.values().iterator().next();
                    if (obj != null) {
                        collectionValueType = getSimpleType(obj);
                    }
                }
                value = marshalMap(map);
            }
        } else {
            valueType = value.getClass();
            collectionValueType = null;
            value = JSON_CONVERTER.toJson(value);
        }
    }

    /**
     * Determine whether the specified value is of the specified type, or
     * contains at some level an object of the specified type\.
     * 
     * @param value
     *            Value to be checked.
     * @param typeClass
     *            Class of the type to be found.
     * @return True if the value is of the specified type or contains an object
     *         of that type, false otherwise.
     */
    private boolean isValueTypeOrContainingType(Object value, Class<?> typeClass) {

        /*
         * If the value is of the specified type, return true; otherwise, if the
         * value is a map or collection, recursively determine if the value
         * contains any objects of the specified type; otherwise, just return
         * false.
         */
        if (typeClass.isAssignableFrom(value.getClass())) {
            return true;
        } else if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (isValueTypeOrContainingType(entry.getValue(), typeClass)) {
                    return true;
                }
            }
            return false;
        } else if (value instanceof Collection) {
            for (Object element : (Collection<?>) value) {
                if (isValueTypeOrContainingType(element, typeClass)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Convert the specified object into something serializable via JSON,
     * returning said serializable object. The latter will have any {@link Set}
     * objects in the original replaced with {@link List} objects, as well as
     * any {@link Geometry} objects replaced with {@link String} objects
     * providing the geometry in <a
     * href="https://en.wikipedia.org/wiki/Well-known_text">Well-Known Text</a>
     * format. This method also makes note of any <code>Set</code> it replaces
     * in the {@link #pathsToSets} list, and likewise any <code>Geometry</code>
     * it replaces is noted in the {@link #pathsToGeometries} list.
     * 
     * @param value
     *            Value to be converted. If not a collection or map, it must be
     *            {@link Serializable}.
     * @param path
     *            Path within the top-level value to this value.
     * @return Converted value.
     */
    private Serializable preprocessValueForSerialization(Object value,
            Deque<Serializable> path) {

        /*
         * If the value is a map or a collection, copy it (recording its path if
         * it is a set); if a geometry, record its path and replace it; if none
         * of the above, then just assume the value is serializable and return
         * it.
         */
        if (value instanceof Map) {

            /*
             * Make a copy with serializable as the value type generic
             * parameter, and recursively convert any values within the map to
             * serializable ones.
             */
            Map<?, ?> original = (Map<?, ?>) value;
            HashMap<String, Serializable> copy = new HashMap<>(original.size(),
                    1.0f);
            for (Map.Entry<?, ?> entry : original.entrySet()) {
                path.push(entry.getKey().toString());
                copy.put(entry.getKey().toString(),
                        preprocessValueForSerialization(entry.getValue(), path));
                path.pop();
            }
            return copy;
        } else if (value instanceof Collection) {

            /*
             * If the value is a set, make a note of its path.
             */
            if (value instanceof Set) {
                pathsToSets.add(convertPathToString(path));
            }

            /*
             * Make a copy with serializable as the element type generic
             * parameter, and recursively convert any elements to serializable
             * ones.
             */
            Collection<?> original = (Collection<?>) value;
            ArrayList<Serializable> copy = new ArrayList<>(original.size());
            int index = 0;
            for (Object element : original) {
                path.push(index++);
                copy.add(preprocessValueForSerialization(element, path));
                path.pop();
            }
            return copy;
        } else if (value instanceof Geometry) {

            /*
             * Make a note of the geometry's path and then convert it to
             * Well-Known Text format.
             */
            pathsToGeometries.add(convertPathToString(path));
            return value.toString();
        } else {

            /*
             * Assume anything else is already serializable.
             */
            try {
                return (Serializable) value;
            } catch (Exception e) {
                throw new IllegalArgumentException("Value \"" + value
                        + "\" of type " + value.getClass()
                        + " not serializable");
            }
        }
    }

    /**
     * Process the specified object and/or any nested objects (if the object is
     * a {@link Map} or {@link List}) by replacing any <code>List</code>
     * instance that was used as a stand-in for a {@link Set} for serialization
     * purposes with a <code>Set</code>, and likewise by replacing any
     * {@link String} that was used as a stand-in for a {@link Geometry} for
     * serialization purposes with a <code>Geometry</code>.
     * 
     * @param value
     *            Value to be converted.
     * @return Converted value.
     */
    private Object postprocessValueAfterDeserialization(Object value) {

        /*
         * Restore any geometries first, then any sets.
         */
        for (Substitution substitution : Substitution.values()) {

            /*
             * Get the paths list appropriate to the substitution to be
             * performed, and reverse it. The latter is done because, when
             * replacing lists with sets, it is imperative that the most nested
             * ones are replaced first, since the sets have no inherent ordering
             * and thus the indices found within the paths would be useless with
             * them.
             */
            List<String> paths = new ArrayList<>(
                    substitution == Substitution.GEOMETRY ? pathsToGeometries
                            : pathsToSets);
            Collections.reverse(paths);

            /*
             * Iterate through the paths, performing the substitution
             * appropriate for each in turn.
             */
            for (String pathString : paths) {
                value = substituteForObjectAtPath(value, substitution,
                        pathString.split("\n"), 0);
            }
        }
        return value;
    }

    /**
     * Substitute a new object of a different type for the object found at the
     * specified path within the specified value.
     * 
     * @param value
     *            Value in which the substitution is to take place, either of
     *            the value itself or of a nested object.
     * @param type
     *            Type of substitution to be performed. If
     *            {@link Substitution#GEOMETRY}, then a {@link String} in
     *            Well-Known Text format will be replaced with a
     *            {@link Geometry}; otherwise, a {@link List} will be replaced
     *            with a {@link Set}.
     * @param path
     *            Path to the object upon which to perform the substitution; the
     *            level that this method is to examine is provided by
     *            <code>index</code>.
     * @param index
     *            Index into <code>path</code>; may be past the end of the path,
     *            in which case <code>value</code> itself is to be substituted.
     * @return Object with the substitution performed.
     */
    @SuppressWarnings("unchecked")
    private Object substituteForObjectAtPath(Object value, Substitution type,
            String[] path, int index) {

        /*
         * If the index is past the end of the list, then the value that was
         * passed in is the object for which a substitution is to be made.
         * Otherwise, the passed-in value is a container in which the object for
         * which a substitution must be made resides, either directly or at some
         * arbitrarily nested depth.
         */
        if (index == path.length) {

            /*
             * If the substitution is for geometry, turn the string into the
             * geometry; otherwise, turn the list into a set.
             */
            if (type == Substitution.GEOMETRY) {
                try {
                    return WKT_READER.get().read((String) value);
                } catch (ParseException e) {
                    throw new IllegalStateException(
                            "could not unmarshal Well-Known-Text geometry", e);
                }
            } else {
                return new HashSet<Object>((Collection<?>) value);
            }
        } else {

            /*
             * If the container is a map, fetch the target value using the path
             * element as a key; if it is a list, treat the path element as an
             * index and fetch it that way.
             */
            int listIndex = -1;
            Object targetValue;
            if (value instanceof Map) {
                targetValue = ((Map<String, Object>) value).get(path[index]);
            } else {
                listIndex = Integer.parseInt(path[index]);
                targetValue = ((List<Object>) value).get(listIndex);
            }

            /*
             * Recursively perform the substitution.
             */
            targetValue = substituteForObjectAtPath(targetValue, type, path,
                    index + 1);

            /*
             * If the substitution occurred for an object directly contained by
             * this container, put the new object in the container.
             */
            if (index == path.length - 1) {
                if (value instanceof Map) {
                    ((Map<String, Object>) value).put(path[index], targetValue);
                } else {
                    ((List<Object>) value).set(listIndex, targetValue);
                }
            }
            return value;
        }
    }

    /**
     * Convert the specified path into a string. A path is a list of strings
     * and/or integers, identical to what would result from taking a path string
     * from {@link #pathToSets} or {@link #pathToGeometries} and breaking it
     * into a list at the newlines. The returned string, obviously, is exactly
     * what is found within <code>pathToSets</code> and
     * <code>pathToGeometries</code>.
     * 
     * @param path
     *            Path to be converted.
     * @return String version of the path.
     */
    private String convertPathToString(Deque<Serializable> path) {
        List<Serializable> pathList = new ArrayList<>(path);
        Collections.reverse(pathList);
        return Joiner.on("\n").join(pathList);
    }

    /**
     * Determine whether the current value is "slottable", meaning that it can
     * have one or more slot entries written for it. It is considered to be so
     * if it is a simple type (<code>String</code>, <code>Integer</code>,
     * <code>Boolean</code>, etc.), or a <code>Map</code> or
     * <code>Collection</code> that contains only one of the abovementioned
     * simple types (and is not empty). So, for example, a <code>List</code>
     * holding <code>String</code> objects exclusively is considered slottable,
     * but one holding other collections, or holding one <code>String</code> and
     * one <code>Integer</code>, is not.
     * 
     * @return True if the current value is slottable, false otherwise.
     */
    private boolean isValueObjectSlottable() {

        /*
         * If the value is a map or collection, see if all its contained
         * elements are of the same simple type; if they are, it is slottable,
         * otherwise they are not. If is is empty, then it is not slottable
         * either.
         */
        Collection<?> elements = null;
        if (value instanceof Map) {
            elements = ((Map<?, ?>) value).values();
        } else if (value instanceof Collection) {
            elements = (Collection<?>) value;
        }
        if (elements != null) {
            if (elements.isEmpty()) {
                return false;
            }
            Class<?> firstElementClass = null;
            for (Object element : elements) {
                if (element == null) {
                    return false;
                }
                Class<?> elementClass = getSimpleType(element);
                if (elementClass == null) {
                    return false;
                } else if (firstElementClass == null) {
                    firstElementClass = elementClass;
                } else if (firstElementClass.equals(elementClass) == false) {
                    return false;
                }
            }
            return true;
        }

        /*
         * Since the value is not a map or collection, see if it is a simple
         * type; if it is, it is slottable.
         */
        return (getSimpleType(value) != null);
    }

    /**
     * Determine whether the specified value is valid. It is considered to be so
     * if it is a simple type (<code>String</code>, <code>Integer</code>,
     * <code>Boolean</code>, etc.), or a <code>Geometry</code>, or a
     * <code>Map</code> or <code>Collection</code> that contains only the
     * abovementioned simple types, <code>Geometry</code> instances, and/or
     * nested maps or collections which in turn must follow the same rules.
     * 
     * @param value
     *            Value to be checked.
     * @return True if the specified value is valid, false otherwise.
     */
    private boolean isValueValid(Object value) {

        /*
         * If the value is a map or collection, see if all its contained
         * elements are valid; if they are, it is valid.
         */
        Collection<?> elements = null;
        if (value instanceof Map) {
            elements = ((Map<?, ?>) value).values();
        } else if (value instanceof Collection) {
            elements = (Collection<?>) value;
        }
        if (elements != null) {
            for (Object element : elements) {
                if ((element == null) || (isValueValid(element) == false)) {
                    return false;
                }
            }
            return true;
        }

        /*
         * Since the value is not a map or collection, see if it is a simple
         * type.
         */
        return ((getSimpleType(value) != null) || (value instanceof Geometry));
    }

    /**
     * Get the simple type of the specified value, or <code>null</code> if it is
     * not of a simple type.
     * 
     * @param value
     *            Value to be checked.
     * @return Simple type of the value, if any.
     */
    private Class<?> getSimpleType(Object value) {
        Class<?> valueClass = value.getClass();
        for (Class<?> type : DESERIALIZERS_FOR_SIMPLE_TYPES.keySet()) {
            if (type.isAssignableFrom(valueClass)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Marshal the specified map to a string.
     * 
     * @param map
     *            Map to be marshalled.
     * @return Marshalled map.
     */
    private String marshalMap(Map<?, ?> map) {
        StringBuilder str = new StringBuilder();
        for (Entry<?, ?> entry : map.entrySet()) {
            str.append(KEY_BEGIN_TAG).append(entry.getKey())
                    .append(KEY_END_TAG);
            str.append(VALUE_BEGIN_TAG).append(marshal(entry.getValue()))
                    .append(VALUE_END_TAG);
            str.append("\n");
        }
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
     * Unmarshal a list from the specified string.
     * 
     * @param collectionValueType
     *            Type of object stored in the list.
     * @param str
     *            Marshalled string.
     * @return Array list.
     */
    private ArrayList<?> unmarshalList(Class<?> collectionValueType, String str) {
        try {
            ArrayList<Object> coll = new ArrayList<>();
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
        return (object == null ? null : object.toString());
    }

    /**
     * Unmarshal the specified string into the specified type of object.
     * 
     * @param typeClass
     *            Type of object into which to unmarshal the string.
     * @param str
     *            Marshalled string.
     * @return Converted object.
     */
    private Object unmarshal(Class<?> typeClass, String str) {
        if (typeClass == null) {
            return null;
        }
        IDeserializer deserializer = DESERIALIZERS_FOR_SIMPLE_TYPES
                .get(typeClass);
        if (deserializer == null) {
            return null;
        }
        return deserializer.deserialize(str);
    }
}
