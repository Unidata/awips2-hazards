/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.jsonutilities;

import java.util.LinkedHashMap;

import com.google.gson.Gson;

/**
 * This class is an attempt to mimic a Python dictionary. It is a Map<String,
 * Object> so like a Python dict, it can contain key/value pairs, allowing
 * non-homogeneous values to be stored in it.
 * 
 * Dict may be an unnecessary abstraction. The developer could just create a
 * Map<?,?> to contain the json, but Dict removes the need to know about
 * generics and hides the fact that it is a Map.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Fall 2011               Bryon.Lawrence    Initial creation
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public final class Dict extends LinkedHashMap<String, Object> {
    transient private static final long serialVersionUID = 80466L;

    /**
     * Return the value associated with the given key. Can be used without
     * explicit casting.
     * 
     * @param key
     *            The key in the dict for which to retrieve a value.
     * @return A value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getDynamicallyTypedValue(String key) {
        return (T) get(key);
    }

    /**
     * @return This object converted to a JSON string.
     */
    public String toJSONString() {
        Gson gson = JSONUtilities.createGsonInterpreter();
        return gson.toJson(this);
    }

    /**
     * Builds a Dict instance from a JSON string.
     * 
     * @param A
     *            JSON string containing a dictionary as the top most data
     *            structure
     * @return
     */
    public static Dict getInstance(String json) {
        Gson gson = JSONUtilities.createGsonInterpreter();
        return gson.fromJson(json, Dict.class);
    }
}
