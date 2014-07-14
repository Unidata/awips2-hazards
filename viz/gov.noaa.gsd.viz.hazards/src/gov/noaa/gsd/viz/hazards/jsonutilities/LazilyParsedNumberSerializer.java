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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * This class extends the GSON Serializer and enables it to serialize a
 * LazilyParsedNumber. Since JSON only allows for floating point or integer
 * values, this class will look for a decimal point in the input number. If it
 * has a decimal point, then the input number is serialized as a double.
 * Otherwise, it is serialized as an integer.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 6/11/2012               Bryon.Lawrence    Initial creation
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class LazilyParsedNumberSerializer implements
        JsonSerializer<ComparableLazilyParsedNumber> {

    /**
     * Serializes a LazilyParsedNumber object into either a double or integer
     * value.
     * 
     * @param number
     *            The number to serialize
     * @param type
     *            This is "com.google.gson.LazilyParsedNumber"
     * @param context
     *            The GSON context being used to serialize this object.
     * @return A JsonPrimitive object containing an integer or a floating point
     *         value.
     */
    @Override
    public JsonElement serialize(ComparableLazilyParsedNumber number,
            Type typeOfSrc, JsonSerializationContext context) {
        /*
         * Determine whether or not there is a decimal within the string
         * representation of this number. If there is, then we can store it as a
         * float. Otherwise, it needs to be stored as a integer.
         */
        String numberString = number.toString();
        JsonPrimitive numberPrimitive;

        if (numberString.contains(".")) {
            numberPrimitive = new JsonPrimitive(number.doubleValue());
        } else {
            numberPrimitive = new JsonPrimitive(number.longValue());
        }

        return numberPrimitive;
    }

}
