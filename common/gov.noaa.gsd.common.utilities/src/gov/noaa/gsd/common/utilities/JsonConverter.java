/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Class providing methods allowing the conversion of various
 * objects to and from JSON.
 * <p>
 * When the objects to be serialized and deserialized are common container (that
 * is, {@link Map} or {@link Collection}) instances, or basic simple class
 * instances ({@link String}, {@link Integer}, etc.), the methods
 * {@link #toJson(Object)} and {@link #fromJson(String)} may be used.
 * </p>
 * <p>
 * When the objects are something other than the above, the methods
 * {@link #toJsonIncludingType(Object)} and
 * {@link #fromJsonIncludingType(String)} may be used. These methods encode and
 * expect, respectively, a dictionary holding two entries, one holding the fully
 * qualified class name of the object, the other a subdictionary containing the
 * JSONified object itself.
 * </p>
 * <p>
 * The second set of methods requires that the class being serialized and
 * deserialized either:
 * </p>
 * <ol>
 * <li>be mutable, with getter and setter methods for its member data;</li>
 * <li>include a <code>public</code> constructor annotated with {@literal}
 * {@link JsonCreator}, with parameters all appropriately annotated with
 * {@literal @}{@link JsonProperty}; or</li>
 * <li>include a <code>public static</code> method annotated with {@literal}
 * {@link JsonFactory}, with parameters all appropriately annotated with
 * {@literal @}{@link JsonProperty}.</li>
 * </ol>
 * <p>
 * Objects that are to be serialized and deserialized in cases where, upon
 * deserialization, the exact type of the object is not known, merely its
 * superclass, (e.g. a reference is declared as an
 * <code>IAdvancedGeometry</code>, but the object may be an <code>Ellipse</code>
 * ) may be converted to and from JSON with the <code>toJson(Object)</code> and
 * <code>fromJson(String, Class)</code> methods as long as the superclasses of
 * said objects are annotated with {@literal @}{@link JsonTypeInfo}, so that the
 * serialization includes the class name, and the deserialization knows what
 * subclass of the reference's type to instantiate. Such annotations will be
 * needed even when using <code>toJsonIncludingType(Object)</code> and
 * <code>fromJsonIncludingType(String)</code> if the objects being converted
 * have nested references to component objects that are subclasses of the
 * reference types.
 * </p>
 * <p>
 * Note that {@link Geometry} objects are serialized as Well-Known Text (WKT)
 * strings.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2013            daniel.s.schaffer Initial creation
 * Nov 26, 2013    2366    Chris.Golden      Moved to non-hazards-specific package.
 * Dec 03, 2013    2182    daniel.s.schaffer Refactoring - added fromDate to support refactoring
 * Apr 24, 2014    2925    Chris.Golden      Added method to convert an arbitrary JSON
 *                                           string to an arbitrary object.
 * Jul 25, 2016   19537    Chris.Golden      Renamed and cleaned up.
 * Sep 02, 2016   15934    Chris.Golden      Converted to a non-instantiating class with
 *                                           public static utility methods, since only one
 *                                           instance is needed. Also added methods to
 *                                           allow the serialization and deserialization
 *                                           of objects other than the usual container and
 *                                           simple types; these methods include and expect
 *                                           the class name to be an explicit part of the
 *                                           JSON.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class JsonConverter extends ObjectMapper {

    // Private Static Constants

    /**
     * Key for the class name.
     */
    private static final String KEY_CLASS = "class";

    /**
     * Key for the map holding the serialized instance's member data.
     */
    private static final String KEY_INSTANCE = "instance";

    /**
     * Object mapper used to convert arbitrary objects to and from JSON. (Note
     * that this class <a href="http://wiki.fasterxml.com/JacksonFAQ">is
     * thread-safe</a> as long as it is configured here and not during use, and
     * thus is safe for multiple threads to use to serialize or deserialize
     * visual features simultaneously.) The mapper uses a standard approach to
     * serialization and deserialization, except when dealing with
     * {@link Geometry} objects, which it serializes to and deserializes from
     * Well-Known Text.
     */
    private static final ObjectMapper CONVERTER = new ObjectMapper();
    static {

        /*
         * Configure the converter to ignore unknown properties, and to include
         * in serialization all non-null members of an object.
         */
        CONVERTER
                .configure(
                        DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false);
        CONVERTER.getSerializationConfig().setSerializationInclusion(
                Inclusion.NON_NULL);

        /*
         * Configure the converter to serialize and deserialize objects expected
         * to be of type Geometry using Well-Known Text format.
         */
        CONVERTER.registerModule(JtsJsonConversionModule.getInstance());
    }

    // Public Static Methods

    /**
     * Convert an arbitrary JSON-encoded string to an object of the specified
     * type. This will work for deserializing arbitrary types of objects; if the
     * specified object type may be a superclass of the actual object being
     * deserialized, then said object or one of its superclasses or interfaces
     * must be annotated with {@literal @}{@link JsonTypeInfo} in order to
     * ensure that the specified JSON string includes the class name.
     * 
     * @param json
     *            JSON-encoded string to be converted.
     * @param objectType
     *            Class of the object into which the JSON is to be converted.
     * @return Object of the specified type.
     * @throws IOException
     *             If a problem occurs while deserializing the object.
     */
    public static <T> T fromJson(String json, Class<T> objectType)
            throws IOException {

        /*
         * If the node is null, return null.
         */
        JsonNode rootNode = CONVERTER.readTree(json);
        if (rootNode.isNull()) {
            return null;
        }

        /*
         * Deserialize the node.
         */
        return fromJsonNode(rootNode, objectType);
    }

    /**
     * Convert an arbitrary JSON-encoded string to an object of the specified
     * type. This will only work when the type of the converted object is one of
     * the standard containers ({@link Map} or {@link Collection}), or a simple
     * type (e.g., {@link String}, {@link Integer}, etc.).
     * 
     * @param json
     *            JSON-encoded string to be converted.
     * @return Object of the specified type.
     * @throws IOException
     *             If a problem occurs while deserializing the object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json) throws IOException {
        return (T) fromJsonNode(CONVERTER.readTree(json));
    }

    /**
     * Convert the specified object to JSON form.
     * 
     * @param object
     *            Object to be converted.
     * @return Object converted to JSON form.
     * @throws IOException
     *             If a problem occurs while serializing the object.
     */
    public static String toJson(Object object) throws IOException {
        return CONVERTER.writeValueAsString(object);
    }

    /**
     * Convert the specified diciontary (map) that was deserialized from JSON to
     * an object of the specified type. Note that this is useful for situations
     * in which JSON has been deserialized into a dictionary, but not yet to an
     * object of the desired type. (This is used within the
     * VisualFeaturesHandler.py Python code, for example.)
     * 
     * @param dictionary
     *            Dictionary to be converted.
     * @param objectType
     *            Type of the desired object.
     * @return Object of the desired type.
     */
    public static <T> T fromDictionary(Map<?, ?> dictionary, Class<T> objectType) {
        return CONVERTER.convertValue(dictionary, objectType);
    }

    /**
     * Convert the specified object to a dictionary (map) suitable for
     * conversion to JSON. Note that this is useful for situations in which JSON
     * is ultimately desired, but the object is to be converted as an
     * intermediate step before the conversion to JSON is done. (This is used
     * within the VisualFeaturesHandler.py Python code, for example.)
     * 
     * @param object
     *            Object to be converted.
     * @return Object converted to dictionary form.
     */
    public static Map<?, ?> toDictionary(Object object) {
        return CONVERTER.convertValue(object, Map.class);
    }

    /**
     * Convert the specified JSON string encoding a dictionary holding the class
     * of an object and its instance member data into an object of that type
     * with that state. This method should be used when the JSON was generated
     * via the @{link {@link #toJsonIncludingType(Object)} method.
     * 
     * @param json
     *            JSON-encoded string to be converted.
     * @return Object, or <code>null</code> if the provided string indicated
     *         null.
     * @throws IOException
     *             If an error occurs when reading the nodes from the parser.
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJsonIncludingType(String json) throws IOException {

        /*
         * If the node is null, return null.
         */
        JsonNode rootNode = CONVERTER.readTree(json);
        if (rootNode.isNull()) {
            return null;
        }

        /*
         * Get the fully qualified class name from the encoded dictionary.
         */
        Class<?> objectType = null;
        try {
            objectType = Class.forName(rootNode.get(KEY_CLASS).getTextValue());
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to deserialize object from JSON.", e);
        }

        /*
         * Convert the node holding the instance to the object of the class
         * determined above.
         */
        return (T) fromJsonNode(rootNode.get(KEY_INSTANCE), objectType);
    }

    /**
     * Convert the specified object into a JSON string. The latter will encode a
     * dictionary holding the class of an object and its instance member data.
     * This method should be used when the object being converted is not, and
     * cannot be, annotated with {@literal @}{@link JsonTypeInfo} (e.g. it is
     * from a third-party library) and thus using {@link #toJson(Object)} would
     * result in a JSON encoding that does not include the object's type, and it
     * is expected that the latter will be needed when deserializing it.
     * Deserialization of the result of this method must be performed by the
     * {@link #fromJsonIncludingType(String)} method.
     * 
     * @param object
     *            Object to be encoded; may be <code>null</code>.
     * @return JSON string.
     * @throws IOException
     *             If an error occurs when writing the nodes to the generator.
     */
    public static String toJsonIncludingType(Object object) throws IOException {

        /*
         * Return a string indicating null if the object is null.
         */
        if (object == null) {
            return NullNode.getInstance().toString();
        }

        /*
         * Create a dictionary in JSON node form, and add two entries, the fully
         * qualified class name of the object, and the object itself converted
         * to a JSON node.
         */
        ObjectNode rootNode = CONVERTER.createObjectNode();
        rootNode.put(KEY_CLASS, object.getClass().getCanonicalName());
        rootNode.put(KEY_INSTANCE,
                CONVERTER.convertValue(object, JsonNode.class));

        /*
         * Convert the dictionary to a JSON string.
         */
        return rootNode.toString();
    }

    /**
     * Convert an arbitrary JSON node to an object of the specified type. This
     * will work for deserializing arbitrary types of objects; if the specified
     * object type may be a superclass of the actual object being deserialized,
     * then said object or one of its superclasses or interfaces must be
     * annotated with {@literal @}{@link JsonTypeInfo} in order to ensure that
     * the specified JSON string includes the class name.
     * <p>
     * <strong>NOTE</strong>: The specified object type's class must be
     * accessible to the class loader used by this class. If it is not, the
     * alternative method {@link #fromJsonNode(JsonNode, Class, ClassLoader)}
     * should be used instead.
     * </p>
     * 
     * @param node
     *            JSON node to be converted.
     * @param objectType
     *            Class of the object into which the JSON is to be converted.
     * @return Object of the specified type.
     * @throws IOException
     *             If a problem occurs while deserializing the object.
     */
    public static <T> T fromJsonNode(JsonNode node, Class<T> objectType)
            throws IOException {
        return fromJsonNode(node, objectType,
                JsonConverter.class.getClassLoader());
    }

    /**
     * Convert an arbitrary JSON node to an object of the specified type. This
     * will work for deserializing arbitrary types of objects; if the specified
     * object type may be a superclass of the actual object being deserialized,
     * then said object or one of its superclasses or interfaces must be
     * annotated with {@literal @}{@link JsonTypeInfo} in order to ensure that
     * the specified JSON string includes the class name.
     * 
     * @param node
     *            JSON node to be converted.
     * @param objectType
     *            Class of the object into which the JSON is to be converted.
     * @param classLoader
     *            Class loader to be used when deserializing; this must be a
     *            loader that has access to the <code>objectType</code> class.
     * @return Object of the specified type.
     * @throws IOException
     *             If a problem occurs while deserializing the object.
     */
    public static <T> T fromJsonNode(JsonNode node, Class<T> objectType,
            ClassLoader classLoader) throws IOException {

        /*
         * Save the current class loader for this thread so that it can be used
         * again later, and install the specified class loader. Then attempt to
         * deserialize the object from the node, and prior to returning it,
         * reset the class loader to what it was before. The reason for this
         * fussing with the thread-specific context class loader is explained
         * here:
         * 
         * http://stackoverflow.com/questions/19694928/jackson-jersey-deserialize
         * -exception-for-id-type-id-class-no-such-class
         * 
         * Essentially, the Jackson object mapper, if attempting to deserialize
         * an instance of a (sub)class of a class or interface that uses the
         * JsonTypeInfo annotation to ensure that the class name gets included
         * in the JSON during encoding, attempts to get the class using the
         * thread-specific context class loader, which, at least in some cases
         * when attempting to look up a subclass of IAdvancedGeometry, fails
         * miserably. Using this class's class loader instead allows the class
         * to be found, and deserialization to succeed.
         */
        ClassLoader originalClassLoader = Thread.currentThread()
                .getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return CONVERTER.readValue(node, objectType);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    // Private Static Methods

    /**
     * Convert the specified JSON node into an arbitrary standard object of the
     * appropriate type.
     * 
     * @param node
     *            Node to be converted.
     * @return Object encoded within the node.
     */
    private static Object fromJsonNode(JsonNode node) {
        if (node.isArray()) {
            List<Object> list = new ArrayList<>(node.size());
            for (int j = 0; j < node.size(); j++) {
                list.add(fromJsonNode(node.get(j)));
            }
            return list;
        } else if (node.isObject()) {
            Map<String, Object> map = new HashMap<>(node.size());
            Iterator<String> keys = node.getFieldNames();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, fromJsonNode(node.get(key)));
            }
            return map;
        } else if (node.isBoolean()) {
            return node.getBooleanValue();
        } else if (node.isDouble()) {
            return node.getDoubleValue();
        } else if (node.isLong()) {
            return node.getLongValue();
        } else if (node.isInt()) {
            return node.getIntValue();
        } else if (node.isNumber()) {
            return node.getNumberValue().floatValue();
        } else if (node.isTextual()) {
            return node.getTextValue();
        } else if (node.isNull()) {
            return null;
        } else {
            throw new IllegalArgumentException("could not parse JSON node \""
                    + node + "\"");
        }
    }

    // Private Constructors

    /**
     * Private constructor preventing creation of an instance.
     */
    private JsonConverter() {
    }
}
