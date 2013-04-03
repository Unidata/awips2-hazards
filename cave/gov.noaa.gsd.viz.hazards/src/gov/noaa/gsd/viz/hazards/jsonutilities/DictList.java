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

import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * This class is an attempt to mimic a Python list. It is a List<Object>. The
 * list can contain objects which may be either primitive values or other Lists
 * or Maps. This allows non-homogeneous values to be stored in it.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public final class DictList extends ArrayList<Object> {
    transient private static final long serialVersionUID = 80466L;

    /**
     * Return the value at the give index in the list. Can be used without
     * explicit casting.
     * 
     * @param index
     *            The list index
     * @return The value at that list index
     */
    @SuppressWarnings("unchecked")
    public <T> T getDynamicallyTypedValue(int index) {
        return (T) get(index);
    }

    /**
     * @return This object converted to a JSON string.
     */
    public String toJSONString() {
        Gson gson = JSONUtilities.createGsonInterpreter();
        return gson.toJson(this);
    }

    /**
     * Builds a DictList instance from a JSON string.
     * 
     * @param A
     *            JSON string containing a list as the top most data structure.
     * @return
     */
    public static DictList getInstance(String json) {
        Gson gson = JSONUtilities.createGsonInterpreter();
        return gson.fromJson(json, DictList.class);
    }
}
