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

import gov.noaa.gsd.common.utilities.JsonConverter;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
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
import javax.xml.bind.annotation.XmlTransient;

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
 * Apr 28, 2016   18267    Chris.Golden  Fixed bug that caused sets and geometries
 *                                       that were the value of the attribute
 *                                       (that is, not nested within the value)
 *                                       to be deserialized incorrectly. Also
 *                                       fixed bug that caused strings substituted
 *                                       in for geometries that were the entire
 *                                       value to not be converted to geometries
 *                                       upon deserialization. Also made any
 *                                       collections or maps holding a mix of
 *                                       null values and non-nulls, the latter all
 *                                       of the same simple type, to not be
 *                                       slottable. Finally, changed the validity
 *                                       check to allow collections/maps with null
 *                                       entries.
 * Sep 03, 2016   15934    Chris.Golden  Changed substitution of Well-Known-Text
 *                                       for Geometry code to be generalized, so
 *                                       that other substitutions could be added
 *                                       easily in the future. Also added the
 *                                       ability to substitute JSON strings for
 *                                       IAdvancedGeometry objects, so that these
 *                                       new advanced geometries may be used as
 *                                       attribute values or components thereof.
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

        GEOMETRY(Geometry.class), ADVANCED_GEOMETRY(IAdvancedGeometry.class), SET(
                Set.class);

        // Private Static Constants

        /**
         * Map of types to instances.
         */
        private static final Map<Class<?>, Substitution> INSTANCES_FOR_TYPES;
        static {
            Map<Class<?>, Substitution> map = new HashMap<>();
            for (Substitution value : values()) {
                map.put(value.getType(), value);
            }
            INSTANCES_FOR_TYPES = ImmutableMap.copyOf(map);
        }

        // Private Variables

        /**
         * Type to which this substitution applies for instances of said type
         * (and instances of subclasses).
         */
        private final Class<?> type;

        // Public Static Methods

        /**
         * Get the types that require substitution.
         * 
         * @return Types that require substitution.
         */
        public static Set<Class<?>> getTypesRequiringSubstitution() {
            return INSTANCES_FOR_TYPES.keySet();
        }

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param type
         *            Type to which this substitution applies.
         */
        private Substitution(Class<?> type) {
            this.type = type;
        }

        // Public Methods

        /**
         * Get the type to which this substitution applies for instances of said
         * type (and instances of subclasses).
         * 
         * @return Type.
         */
        public Class<?> getType() {
            return type;
        }
    };

    // Private Interfaces

    /**
     * Interface describing the method that must be implemented in order for a
     * class to act as a preprocessor for leaf-type (that is, not {@link Map} or
     * {@link Collection}) substitutable objects that are to be converted to
     * strings before serialization.
     */
    private interface IPreprocessor {

        /**
         * Preprocess the specified object.
         * 
         * @param object
         *            Object to be preprocessed.
         * @return Resulting string.
         */
        public String preprocess(Object object);
    }

    /**
     * Interface describing the method that must be implemented in order for a
     * class to act as a postprocessor for substitutable objects that are to be
     * converted to their appropriate types after deserialization.
     */
    private interface IPostprocessor {

        /**
         * Postprocess the specified object.
         * 
         * @param object
         *            Object to be postprocessed.
         * @return Resulting object.
         */
        public Object postprocess(Object object);
    }

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
     * Substitutions applying to {@link Map} subclasses.
     */
    private static final EnumSet<Substitution> SUBSTITUTABLE_MAPS = EnumSet
            .noneOf(Substitution.class);

    /**
     * Substitutions applying to {@link Collection} subclasses.
     */
    private static final EnumSet<Substitution> SUBSTITUTABLE_COLLECTIONS = EnumSet
            .of(Substitution.SET);

    /**
     * Substitutions applying to types other than subclasses of {@link Map} or
     * {@link Collection}.
     */
    private static final EnumSet<Substitution> SUBSTITUTABLE_LEAF_OBJECTS = EnumSet
            .complementOf(SUBSTITUTABLE_COLLECTIONS);

    /**
     * Map pairing substitutables found in {@link #SUBSTITUTABLE_LEAF_OBJECTS}
     * with their preprocessors. Note that those found in
     * {@link #SUBSTITUTABLE_COLLECTIONS} and {@link #SUBSTITUTABLE_MAPS} do not
     * require entries, since all collections and maps are converted to
     * {@link ArrayList} and {@link HashMap} instances, respectively.
     */
    private static final Map<Substitution, IPreprocessor> PREPROCESSORS_FOR_SUBSTITUTIONS;
    static {
        Map<Substitution, IPreprocessor> map = new HashMap<>(2, 1.0f);
        map.put(Substitution.GEOMETRY, new IPreprocessor() {

            @Override
            public String preprocess(Object object) {
                return object.toString();
            }
        });
        map.put(Substitution.ADVANCED_GEOMETRY, new IPreprocessor() {

            @Override
            public String preprocess(Object object) {
                try {
                    return JsonConverter.toJson(object);
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "could not marshal object of type IAdvancedGeometry to JSON",
                            e);
                }
            }
        });
        PREPROCESSORS_FOR_SUBSTITUTIONS = ImmutableMap.copyOf(map);
    }

    /**
     * Map pairing all substitutables with their postprocessors.
     */
    private static final Map<Substitution, IPostprocessor> POSTPROCESSORS_FOR_SUBSTITUTIONS;
    static {
        Map<Substitution, IPostprocessor> map = new HashMap<>(3, 1.0f);
        map.put(Substitution.GEOMETRY, new IPostprocessor() {

            @Override
            public Object postprocess(Object object) {
                try {
                    return WKT_READER.get().read((String) object);
                } catch (ParseException e) {
                    throw new IllegalStateException(
                            "could not unmarshal Well-Known-Text to Geometry",
                            e);
                }
            }
        });
        map.put(Substitution.ADVANCED_GEOMETRY, new IPostprocessor() {

            @Override
            public Object postprocess(Object object) {
                try {
                    return JsonConverter.fromJson((String) object,
                            IAdvancedGeometry.class);
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "could not unmarshal JSON to IAdvancedGeometry", e);
                }
            }
        });
        map.put(Substitution.SET, new IPostprocessor() {

            @Override
            public Object postprocess(Object object) {
                return new HashSet<Object>((Collection<?>) object);
            }
        });
        POSTPROCESSORS_FOR_SUBSTITUTIONS = ImmutableMap.copyOf(map);
    }

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
     * Map pairing substitution types with list of paths to objects of those
     * types in the original {@link #value}.
     * <p>
     * Each path is a string consisting of substrings and/or integers separated
     * by newlines (avoiding the use of a nested list in order to make
     * serialization easier); each substring is a key into a {@link Map},
     * whereas each integer is the index into a {@link List}. As an example, the
     * path:<code><pre>     foo
     *     3
     *     bar</pre></code> indicates that the top-level value is a
     * <code>Map</code>, and that its key "foo" is associated with a
     * <code>List</code> as a value; the 3rd element within the list is a nested
     * <code>Map</code>; and the object found as the value associated with the
     * key "bar" in this map was originally of the type with which this
     * substitution is associated.
     * </p>
     * <p>
     * TODO: Make this a list of lists of strings and indices (perhaps the
     * sublist should have generic parameter of <code>Serializable</code>)
     * instead of a list of strings; the latter was done only because it aided
     * in serialization.
     * </p>
     * <p>
     * TODO: This map is currently not serialized into XML due to problems with
     * doing so with lists nested within a map; instead, the map is populated by
     * {@link #getPathsToInstancesFor()} before it is used. This is clumsy; the
     * lists used as values within the map (e.g. {@link #pathsToSets}) should
     * not be member variables themselves, but rather should simply be
     * components of this map, with this map being serialized and deserialized
     * correctly. When there is time to figure out how to do this, make it so.
     * </p>
     */
    @XmlTransient
    private final EnumMap<Substitution, List<String>> pathsToInstancesForSubstitutables = new EnumMap<>(
            Substitution.class);

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
     * <p>
     * TODO: Make this a list of lists of strings and indices (perhaps the
     * sublist should have generic parameter of <code>Serializable</code>)
     * instead of a list of strings; the latter was done only because it aided
     * in serialization.
     * </p>
     * <p>
     * TODO: This should not be a member variable; it should simply be
     * serialized and deserialized as part of
     * {@link #pathsToInstancesForSubstitutables}, if serialization and
     * deserialization of the latter becomes possible.
     * </p>
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
     * <p>
     * TODO: Make this a list of lists of strings and indices (perhaps the
     * sublist should have generic parameter of <code>Serializable</code>)
     * instead of a list of strings; the latter was done only because it aided
     * in serialization.
     * </p>
     * <p>
     * TODO: This should not be a member variable; it should simply be
     * serialized and deserialized as part of
     * {@link #pathsToInstancesForSubstitutables}, if serialization and
     * deserialization of the latter becomes possible.
     * </p>
     */
    @DynamicSerializeElement
    @XmlElement
    private final List<String> pathsToGeometries = new ArrayList<>();

    /**
     * List of paths to {@link IAdvancedGeometry} objects in the original
     * {@link #value}. Each path is a string consisting of substrings and/or
     * integers separated by newlines (avoiding the use of a nested list in
     * order to make serialization easier); each substring is a key into a
     * {@link Map}, whereas each integer is the index into a {@link List}. As an
     * example, the path: <code><pre>     20
     *     baz</pre></code> indicates that the top-level value is a
     * <code>List</code>; that the 20th element in said list is a
     * <code>Map</code>; and the {@link String} found as the value associated
     * with the key "baz" in this map was originally an
     * <code>IAdvancedGeometry</code>.
     * <p>
     * TODO: Make this a list of lists of strings and indices (perhaps the
     * sublist should have generic parameter of <code>Serializable</code>)
     * instead of a list of strings; the latter was done only because it aided
     * in serialization.
     * </p>
     * <p>
     * TODO: This should not be a member variable; it should simply be
     * serialized and deserialized as part of
     * {@link #pathsToInstancesForSubstitutables}, if serialization and
     * deserialization of the latter becomes possible.
     * </p>
     */
    @DynamicSerializeElement
    @XmlElement
    private final List<String> pathsToAdvancedGeometries = new ArrayList<>();

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
        Object result = value;
        if (valueType != null) {

            /*
             * If the collection value type was recorded, then the value was
             * serialized in a slottable manner; deserialize it accordingly.
             * Otherwise, it was serialized as JSON, so deserialize the latter.
             */
            if (collectionValueType != null) {
                if (Map.class.isAssignableFrom(valueType)) {
                    result = unmarshalMap(collectionValueType, (String) value);
                } else {
                    result = unmarshalList(collectionValueType, (String) value);
                }
            } else {
                try {
                    result = JsonConverter.fromJson((String) value);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "internal error while deserializing JSON", e);
                }
            }
        }

        /*
         * Perform any substitutions needed, then return the result.
         */
        ensurePathsToInstancesForSubstitutablesIsPopulated();
        return postprocessValueAfterDeserialization(result);
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
     * Get the paths to any advanced geometries.
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as the property being fetched is internal state.
     * </p>
     * 
     * @return Paths to any advanced geometries.
     */
    public List<String> getPathsToAdvancedGeometries() {
        return pathsToAdvancedGeometries;
    }

    /**
     * Set the hazard event identifier.
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
     * Set the paths to any advanced geometries.
     * <p>
     * <strong>NOTE</strong>: Required for JAXB; should not be used elsewhere
     * outside this class, as setting this property may leave the object in an
     * undefined state.
     * </p>
     * 
     * @param pathsToAdvancedGeometries
     *            New paths to any advanced geometries.
     */
    public void setPathsToAdvancedGeometries(
            List<String> pathsToAdvancedGeometries) {
        this.pathsToAdvancedGeometries.clear();
        this.pathsToAdvancedGeometries.addAll(pathsToAdvancedGeometries);
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
     * Ensure that the paths-to-instances-for-substitutables map has been
     * populated.
     * <p>
     * TODO: This method can be removed once the variable is properly serialized
     * and deserialized; see the description of
     * {@link #pathsToInstancesForSubstitutables}.
     * </p>
     */
    private void ensurePathsToInstancesForSubstitutablesIsPopulated() {
        if (pathsToInstancesForSubstitutables.isEmpty()) {
            pathsToInstancesForSubstitutables
                    .put(Substitution.SET, pathsToSets);
            pathsToInstancesForSubstitutables.put(Substitution.GEOMETRY,
                    pathsToGeometries);
            pathsToInstancesForSubstitutables.put(
                    Substitution.ADVANCED_GEOMETRY, pathsToAdvancedGeometries);
        }
    }

    /**
     * Convert the value to something serializable.
     */
    private void convertValue() {

        /*
         * If the value is or contains any objects requiring substitution,
         * replace them, making a note of their positions so that they can be
         * restored when unmarshalling.
         */
        ensurePathsToInstancesForSubstitutablesIsPopulated();
        for (List<String> pathsToInstances : pathsToInstancesForSubstitutables
                .values()) {
            pathsToInstances.clear();
        }
        if (isValueTypeRequiringSubstitution(value)) {
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
            try {
                value = JsonConverter.toJson(value);
            } catch (IOException e) {
                throw new RuntimeException(
                        "failed to serialize object of type " + valueType, e);
            }
        }
    }

    /**
     * Determine whether the specified value is of a type that requires
     * substitution, or contains at some level an object that requires
     * substitution.
     * 
     * @param value
     *            Value to be checked.
     * @return <code>true</code> if the value requires substitution, or contains
     *         an object that requires substitution, <code>false</code>
     *         otherwise.
     */
    private boolean isValueTypeRequiringSubstitution(Object value) {

        /*
         * If the value is one of the types requiring substitution, return true;
         * otherwise, if the value is a map or collection, recursively determine
         * if the value contains any objects with types requiring substitution;
         * otherwise, just return false.
         */
        for (Class<?> type : Substitution.getTypesRequiringSubstitution()) {
            if (type.isAssignableFrom(value.getClass())) {
                return true;
            }
        }
        if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (isValueTypeRequiringSubstitution(entry.getValue())) {
                    return true;
                }
            }
            return false;
        } else if (value instanceof Collection) {
            for (Object element : (Collection<?>) value) {
                if (isValueTypeRequiringSubstitution(element)) {
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
     * returning said serializable object. All objects that are to have strings
     * substituted for them have this done here. This method also makes note of
     * the paths to any substituted values in the appropriate list within
     * {@link #pathsToInstancesForSubstitutables}.
     * 
     * @param value
     *            Value to be converted.
     * @param path
     *            Path within the top-level value to this value.
     * @return Converted value.
     */
    private Serializable preprocessValueForSerialization(Object value,
            Deque<Serializable> path) {

        /*
         * If the value is a map or a collection, copy it, making a note of its
         * path if it is a substitutable; otherwise, handle it as a leaf object.
         */
        if (value instanceof Map) {

            /*
             * Record the path if the value is of a type that is a substitutable
             * map.
             */
            recordPathIfValueTypeInSet(value, path, SUBSTITUTABLE_MAPS);

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
             * Record the path if the value is of a type that is a substitutable
             * collection.
             */
            recordPathIfValueTypeInSet(value, path, SUBSTITUTABLE_COLLECTIONS);

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
        } else {

            /*
             * If the value needs substitution, return the string version;
             * otherwise, assume it is serializable.
             */
            String convertedValue = recordPathAndConvertIfValueTypeInSet(value,
                    path, SUBSTITUTABLE_LEAF_OBJECTS);
            if (convertedValue != null) {
                return convertedValue;
            }
            try {
                return (Serializable) value;
            } catch (Exception e) {
                throw new IllegalArgumentException("value \"" + value
                        + "\" of type " + value.getClass()
                        + " not serializable");
            }
        }
    }

    /**
     * Record the path of the specified value if its type is one of the types
     * associated with the specified substitutions, or a subclass of one of
     * those types.
     * 
     * @param value
     *            Value to be checked.
     * @param path
     *            Path to be recorded if the value is one of those associated
     *            with the substitutions.
     * @param substitutions
     *            Substitutions with the types for which to check.
     */
    private void recordPathIfValueTypeInSet(Object value,
            Deque<Serializable> path, EnumSet<Substitution> substitutions) {
        for (Substitution substitution : substitutions) {
            if (substitution.getType().isAssignableFrom(value.getClass())) {
                pathsToInstancesForSubstitutables.get(substitution).add(
                        convertPathToString(path));
                break;
            }
        }
    }

    /**
     * Record the path of the specified value if its type is one of the types
     * associated with the specified substitutions, or a subclass of one of
     * those types, and convert the value to a string.
     * 
     * @param value
     *            Value to be checked and possibly converted.
     * @param path
     *            Path to be recorded if the value is one of those associated
     *            with the substitutions.
     * @param substitutions
     *            Substitutions with the types for which to check.
     * @return String holding the converted value, or <code>null</code> if the
     *         value is not one that requires preprocessing.
     */
    private String recordPathAndConvertIfValueTypeInSet(Object value,
            Deque<Serializable> path, EnumSet<Substitution> substitutions) {
        for (Substitution substitution : substitutions) {
            if (substitution.getType().isAssignableFrom(value.getClass())) {
                pathsToInstancesForSubstitutables.get(substitution).add(
                        convertPathToString(path));
                return PREPROCESSORS_FOR_SUBSTITUTIONS.get(substitution)
                        .preprocess(value);
            }
        }
        return null;
    }

    /**
     * Process the specified object and/or any nested objects (if the object is
     * a {@link Map} or {@link List}) by reversing any substitutions, using the
     * paths found in {@link #pathsToInstancesForSubstitutables} to determine
     * which strings need to be converted to what type of objects.
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
             * performed, and reverse it. The latter is done because, if
             * replacing collections, it is imperative that the most nested ones
             * are replaced first, since the replacements may have no inherent
             * ordering (e.g. sets) and thus the indices found within the paths
             * would be useless with them.
             */
            List<String> paths = new ArrayList<>(
                    pathsToInstancesForSubstitutables.get(substitution));
            Collections.reverse(paths);

            /*
             * Iterate through the paths, performing the substitution
             * appropriate for each in turn.
             */
            for (String pathString : paths) {
                value = substituteForObjectAtPath(value, substitution,
                        (pathString.trim().isEmpty() ? new String[0]
                                : pathString.split("\n")), 0);
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
     *            Type of substitution to be performed.
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
         * If the index into the path is past the end of the path array, then
         * the value that was passed in is the object for which a substitution
         * is to be made. Otherwise, the passed-in value is a container in which
         * the object for which a substitution must be made resides, either
         * directly or at some arbitrarily nested depth.
         */
        if (index == path.length) {
            return POSTPROCESSORS_FOR_SUBSTITUTIONS.get(type)
                    .postprocess(value);
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
     * and/or integers, semantically identical to what would result from taking
     * a path string from one of the lists in
     * {@link #pathsToInstancesForSubstitutables} and breaking it into a list at
     * the newlines. The returned string, obviously, is exactly what is found as
     * elements in the lists within
     * <code>pathsToInstancesForSubstitutables</code>.
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
     * if it is a simple type ({@link String}, {@link Integer}, {@link Boolean},
     * etc.), or a {@link Map} or {@link Collection} that contains only one of
     * the abovementioned simple types (and is not empty). So, for example, a
     * {@link List} holding <code>String</code> objects exclusively is
     * considered slottable, but one holding other collections, or holding one
     * <code>String</code> and one <code>Integer</code>, is not.
     * 
     * @return <code>true</code> if the current value is slottable,
     *         <code>false</code> otherwise.
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

                /*
                 * TODO: This check ensures that if there are any empty strings,
                 * the value is not considered slottable. This should not really
                 * be necessary, but for some reason the
                 * HazardAttributeSlotConverter is turning empty strings into
                 * null entries when doing the conversion, which results in bad
                 * behavior when the object is deserialized.
                 */
                if ((elementClass == String.class)
                        && ((String) element).isEmpty()) {
                    return false;
                }
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
     * if it is a simple type ({@link String}, {@link Integer}, {@link Boolean},
     * etc.), or an instance of one of the types associated with the
     * substitutions in {@link #SUBSTITUTABLE_LEAF_OBJECTS}, or a {@link Map} or
     * {@link Collection} that contains only the abovementioned simple types
     * and/or substitution types, and/or nested maps or collections which in
     * turn must follow the same rules.
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
                if ((element != null) && (isValueValid(element) == false)) {
                    return false;
                }
            }
            return true;
        }

        /*
         * Since the value is not a map or collection, see if it is a simple
         * type or one of the leaf types that may be substituted.
         */
        if (getSimpleType(value) != null) {
            return true;
        }
        for (Substitution substitution : SUBSTITUTABLE_LEAF_OBJECTS) {
            if (substitution.getType().isAssignableFrom(value.getClass())) {
                return true;
            }
        }
        return false;
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
