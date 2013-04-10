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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * Class for deserializing a JSON string into a list of Dict object. This class
 * is based on GSON (Google JSON) utilities. Since all interprocess
 * communication in Hazard Services is supported by JSON dicts, this class
 * allows the CAVE developer to translate JSON to an EventDict without having to
 * know much about GSON.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 4/5/2012                Bryon.Lawrence    Initial creation
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class DictListDeserializer implements JsonDeserializer<DictList> {
    /**
     * Method called by the gson interpreter to deserialize a json string into a
     * List of Dict objects.
     * 
     * @param arg0
     *            Represents the root of the GSON parse tree.
     * @param arg2
     *            Context for deserialization that is passed to this custom
     *            deserializer at invokation.
     * 
     * @return A List of Dict objects.
     */
    @Override
    public DictList deserialize(JsonElement arg0, Type arg1,
            JsonDeserializationContext arg2) throws JsonParseException {

        DictList dictList = new DictList();
        JsonArray obj = arg0.getAsJsonArray();
        Iterator<JsonElement> it = obj.iterator();

        while (it.hasNext()) {
            JsonElement element = it.next();
            Object value = parseGsonObject(element);
            dictList.add(value);
        }

        return dictList;
    }

    /*
     * Routine used to recursively parse a JSON element all the way down to its
     * primitive elements.
     */
    private Object parseGsonObject(JsonElement object) {
        Object returnObject = null;

        if ((object != null) && (!object.isJsonNull())) {
            if (object.isJsonObject()) {
                returnObject = buildDict(object.getAsJsonObject());
            } else if (object.isJsonArray()) {
                returnObject = buildArrayList(object.getAsJsonArray());
            } else if (object.isJsonPrimitive()) {
                returnObject = buildPrimitive(object.getAsJsonPrimitive());
            }
        }

        return returnObject;
    }

    /*
     * Routine to build a Dict object from the provided JsonObject. Recursive...
     */
    private Object buildDict(JsonObject object) {
        Dict dict = new Dict();

        Iterator<Entry<String, JsonElement>> it = object.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, JsonElement> pairs = it.next();

            String key = pairs.getKey();
            JsonElement element = pairs.getValue();
            Object value = parseGsonObject(element);
            dict.put(key, value);
        }

        return dict;
    }

    /*
     * Routine to build an ArrayList recursively from a JSON object.
     */
    private Object buildArrayList(JsonArray object) {
        List<Object> list = new ArrayList<Object>();

        Iterator<JsonElement> it = object.iterator();

        while (it.hasNext()) {
            JsonElement element = it.next();
            Object value = parseGsonObject(element);
            list.add(value);
        }

        return list;
    }

    /*
     * Routine to return a representation of a JsonPrimitive object. No
     * recursion here...
     */
    private Object buildPrimitive(JsonPrimitive object) {
        Object returnObject = null;

        if (object.isBoolean()) {
            returnObject = object.getAsBoolean();
        } else if (object.isNumber()) {
            Number number = object.getAsNumber();
            returnObject = new ComparableLazilyParsedNumber(number);
        } else if (object.isString()) {
            returnObject = object.getAsString();
        }

        return returnObject;
    }
}
