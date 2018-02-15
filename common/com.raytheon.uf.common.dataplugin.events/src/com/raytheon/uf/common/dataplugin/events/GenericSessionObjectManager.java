/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic session object manager, used to hold generic objects for the duration
 * of a session. These are not shared between clients, nor do they persist
 * between sessions.
 * <p>
 * Implementation note: This class does not subclass one of the {@link Map}
 * implementations, nor may one of those implementations be used in its place,
 * due to the fact that it needs to be converted to a Python
 * <code>GenericSessionObjectManager</code>, and not some sort of Python
 * dictionary, when it crosses the Java/Python boundary and is used in a
 * recommender (for example). If it were a Java <code>Map</code>, it would be
 * automatically converted to a Python dictionary, which could incur a
 * significant performance cost if it held large and complex Java objects. By
 * using an object that is not an instance of any <code>Map</code>
 * implementation, this may be avoided.
 * </p>
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 30, 2018   14791    Chris.Golden  Initial creation.
 *
 * </pre>
 *
 * @author Chris.Golden
 */
public class GenericSessionObjectManager implements Serializable {

    // Private Variables

    /**
     * Backing map.
     */
    private final Map<String, Serializable> map = new HashMap<>();

    // Public Methods

    /**
     * Get the session object associated with the specified key.
     * 
     * @param key
     *            Key for which to fetch a session object.
     * @return Session object associated with the key, or <code>null</code> if
     *         no associated object is found.
     */
    public Serializable get(String key) {
        return map.get(key);
    }

    /**
     * Set the session object associated with the specified key.
     * 
     * @param key
     *            Key for the new object.
     * @param value
     *            Object to be associated with the key.
     */
    public void set(String key, Serializable value) {
        map.put(key, value);
    }
}
