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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Description: Helper class for serializing/deserializing JSON.
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
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class JsonConverter extends ObjectMapper {

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public JsonConverter() {
        super();
        this.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);

    }

    // Public Methods

    /**
     * Convert the specified object to JSON form.
     * 
     * @param obj
     *            Object to be converted.
     * @return Object converted to JSON form.
     */
    public String toJson(Object obj) {
        try {
            return writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert an arbitrary JSON-encoded string to an object of the specified
     * type.
     * 
     * @param str
     *            JSON-encoded string to be converted.
     * @param clazz
     *            Class of the object into which the JSON is to be converted.
     * @return Object of the specified type.
     * @deprecated Use {@link #fromJson(String)} instead.
     */
    @Deprecated
    public <T> T fromJson(String str, Class<T> clazz) {
        try {
            return readValue(str, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert an arbitrary JSON-encoded string to an object of the specified
     * type.
     * 
     * @param str
     *            JSON-encoded string to be converted.
     * @return Object of the specified type.
     * @throws IOException
     *             If an I/O problem occurs.
     * @throws JsonParseException
     *             If the conversion fails.
     */
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String str) throws IOException,
            JsonProcessingException {
        return (T) fromJsonNode(readTree(str));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    // Private Methods

    /**
     * Convert the specified JSON node into an arbitrary object of the
     * appropriate type.
     * 
     * @param node
     *            Node to be converted.
     * @return Object encoded within the node.
     */
    private Object fromJsonNode(JsonNode node) {
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
}
