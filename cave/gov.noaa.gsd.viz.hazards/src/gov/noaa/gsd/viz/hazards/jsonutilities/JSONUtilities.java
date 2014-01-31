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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SPATIAL_INFO;
import gov.noaa.gsd.common.utilities.JSONConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * JSON helper methods for creating JSON messages. Also, contains utilities for
 * writing JSON to files, which is useful for debugging.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class JSONUtilities {

    /**
     * Convenience method for creating the JSON for the drag drop dot.
     * 
     * @param
     * @return
     */
    static public String createDragDropPointJSON(double lat, double lon,
            long selectedTime) {
        String json = "{\"" + SPATIAL_INFO + "\":{\"points\":[[[" + lat + ","
                + lon + "]," + selectedTime + "]]}}";
        return json;

    }

    /**
     * 
     * @param
     * @return
     */
    static public Gson createGsonInterpreter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EventDict.class,
                new EventDictDeserializer());
        gsonBuilder.registerTypeAdapter(Dict.class, new DictDeserializer());
        gsonBuilder.registerTypeAdapter(DictList.class,
                new DictListDeserializer());
        gsonBuilder.registerTypeAdapter(ComparableLazilyParsedNumber.class,
                new LazilyParsedNumberSerializer());

        return gsonBuilder.create();
    }

    public static Settings settingsFromJSON(String settingsAsJSON) {
        return new JSONConverter().fromJson(settingsAsJSON, Settings.class);
    }

}
