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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
 * Nov 04, 2013   2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Dec 05, 2014   4124     Chris.Golden        Removed obsolete methods.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class JSONUtilities {

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
}
