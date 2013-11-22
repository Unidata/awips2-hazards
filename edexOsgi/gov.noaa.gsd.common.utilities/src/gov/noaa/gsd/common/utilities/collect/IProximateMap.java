/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities.collect;

import java.util.Map;

/**
 * Description: Interface describing the methods that must be implemented in
 * order to create a proximate map. The latter is an object that maps keys to
 * values, much like a standard {@link java.util.Map}, but which also allows
 * values to be retrieved by keys other than those with which the values are
 * associated if the former keys are similar enough to the latter. Two keys
 * considered similar enough are said to be <i>related</i>, while the key that
 * is most closely related to another key is said to be the <i>proximate</i> key
 * for the latter. For any given test key K, may be 0 to N related keys, but
 * only 0 or 1 proximate keys.
 * <p>
 * Thus, if a mapping of K1 to V1 exists, and test key K2 is deemed by the
 * implementation to be close enough to K1 to be considered functionally
 * equivalent as a key (i.e. proximate), then V1 may be fetched by querying the
 * map using K2.
 * </p>
 * <p>
 * Implementations are free to set arbitrary rules indicating which keys are
 * related, which means they may determine that for some test keys no existing
 * keys within the mappings are related. However, they must always consider any
 * exact match to be related. Implementations may also determine that a test key
 * is closer to one key than another when calculating which key is the proximate
 * one for a given test key. This determination may be done in whatever way they
 * see fit, as long as any exact match is considered the proximate one if such a
 * match exists.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 25, 2013    2336    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IProximateMap<K, V> extends Map<K, V> {

    /**
     * Determine whether or not the specified key has a proximate key within the
     * map.
     * 
     * @param key
     *            Key to be checked for a mapping.
     * @return True if the key or its proximate is found within the map.
     * @throws ClassCastException
     *             If the key is of an inappropriate type for this map
     *             (optional).
     * @throws NullPointerException
     *             If the specified key is <code>null</code> and this map does
     *             not permit null keys (optional).
     */
    public boolean containsProximateKey(Object key);

    /**
     * Determine whether or not the specified key has one or more related keys
     * within the map.
     * 
     * @param key
     *            Key to be checked for related keys.
     * @return True if the key has one or more related keys that are within the
     *         map.
     * @throws ClassCastException
     *             If the key is of an inappropriate type for this map
     *             (optional).
     * @throws NullPointerException
     *             If the specified key is <code>null</code> and this map does
     *             not permit null keys (optional).
     */
    public boolean containsRelatedKey(Object key);

    /**
     * Get the value associated with the key that is proximate to the specified
     * key.
     * 
     * @param key
     *            Key for which to fetch the proximate value, that is, the value
     *            associated with this key's proximate.
     * @return Value associated with the specified key's proximate key, or
     *         <code>null</code> if no proximate key is found within the map.
     * @throws ClassCastException
     *             If the key is of an inappropriate type for this map
     *             (optional).
     * @throws NullPointerException
     *             If the specified key is <code>null</code> and this map does
     *             not permit null keys (optional).
     */
    public V getProximate(Object key);

    /**
     * Get the proximate key for the specified key.
     * 
     * @param key
     *            Key for which to find the proximate key.
     * @return Proximate key for this key; note that this may be <code>null
     *         </code> if there is no proximate key, or may be <code>key</code>
     *         itself if an exact match is found.
     * @throws ClassCastException
     *             If the key is of an inappropriate type for this map
     *             (optional).
     * @throws NullPointerException
     *             If the specified key is <code>null</code> and this map does
     *             not permit null keys (optional).
     */
    public K getProximateKey(Object key);
}
