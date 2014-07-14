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
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * 
 * Description: An EventDict deserializer for use by the Gson json utilities.
 * This class enables Gson to properly deserialize a json string representing an
 * EventDict into java EventDict object.
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class EventDictDeserializer implements JsonDeserializer<EventDict> {

    @Override
    public EventDict deserialize(JsonElement arg0, Type arg1,
            JsonDeserializationContext arg2) throws JsonParseException {

        JsonObject obj = arg0.getAsJsonObject();
        Iterator<Entry<String, JsonElement>> it = obj.entrySet().iterator();
        EventDict eventDict = new EventDict();

        while (it.hasNext()) {
            Entry<String, JsonElement> pairs = it.next();
            eventDict.put(pairs.getKey(), pairs.getValue());
        }

        return eventDict;
    }

}
