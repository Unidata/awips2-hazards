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

import gov.noaa.gsd.common.utilities.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Proximate map using classes as keys, where a key within the map
 * is <i>related</i> to a test key if the latter is an ancestor of the former
 * (i.e. a superclass or superinterface),or the latter is equivalent to the
 * former, and where the <i>proximate</i> key is the key from the pool of
 * related keys that is, generationally speaking, the closest to the test key
 * (with a key that is the same class as the test key always being the proximate
 * key, if such an equivalent key exists within the map).
 * <p>
 * More specifically, in cases where there is no equivalent key, but there are
 * multiple related keys, the latter are compared in order to determine which
 * has the fewest intervening classes within the class hierarchy, and that one
 * is considered to be the proximate key. If multiple keys are an identical
 * distance from the test key in the class hierarchy, then a superclass is
 * considered more closely related than a superinterface. If multiple
 * superinterfaces are found that are identical generational remove from the
 * test key, there is no guarantee as to which of them will have its associated
 * value returned.
 * </p>
 * <p>
 * Note that the proximate key for a given test key K may change depending upon
 * what other keys (not K) are inserted. For example, suppose a single pairing,
 * of the class Serializable to value V1, is inserted, and <code>getProximate()
 * </code> is then called with the test key of class String; because String
 * implements Serializable, V1 will be returned. Now another insertion, of the
 * pairing of class CharSequence with value V2, is performed, and <code>
 * getProximate()</code> is again called with String as its test key class. This
 * time, the result may be either V1 or V2, since both are paired with ancestors
 * of String of equal generational distance. If one of the two was closer,
 * generationally speaking, than the other from String, that one's associated
 * value would be returned.
 * </p>
 * <p>
 * This map is a subclass of <code>HashMap</code>, so the same restrictions and
 * capabilities apply, as outlined in the latter's documentation. Note that if a
 * <code>null</code> key is within the map, it will not be related to any other
 * key, but is only the proximate for a <code>null</code> test key. Likewise, a
 * <code>null</code> test key will never have any related keys in the map, and
 * only has as its proximate a <code>null</code> key.
 * </p>
 * <p>
 * Performance of the methods involving searching for a proximate or related key
 * is generally far worse than that for <code>containsKey()</code> or <code>
 * get()</code>, since at least a portion of the map's key set must be iterated
 * over for <code>containsRelatedKey()</code>, or the entire key set in the case
 * of the proximate-related methods, and additionally reflection is performed on
 * the various keys found.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 25, 2013    2336    Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ClassKeyedMap<V> extends HashMap<Class<?>, V> implements
        IProximateMap<Class<?>, V> {

    // Private Static Constants

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 3314483547003501048L;

    // Public Constructors

    /**
     * Construct an empty map with the default initial capacity (16) and the
     * default load factor (0.75).
     */
    public ClassKeyedMap() {

        // No action.
    }

    /**
     * Construct an empty map with the specified initial capacity and the
     * default load factor (0.75).
     * 
     * @param initialCapacity
     *            Initial capacity.
     */
    public ClassKeyedMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Construct an empty map with the specified initial capacity and load
     * factor.
     * 
     * @param initialCapacity
     *            Initial capacity.
     * @param loadFactor
     *            loadFactor
     */
    public ClassKeyedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Construct a map with the same mappings as the specified map.
     * 
     * @param map
     *            Map from which to copy the mappings.
     */
    public ClassKeyedMap(Map<? extends Class<?>, ? extends V> map) {
        super(map);
    }

    // Public Methods

    @Override
    public boolean containsProximateKey(Object key) {
        if ((key == null) && containsKey(null)) {
            return true;
        }
        return (getProximateKey(key) != null);
    }

    @Override
    public boolean containsRelatedKey(Object key) {

        // A related key will exist only if the test key is a class,
        // or it is null and there is a null key mapping.
        if (key == null) {
            return containsKey(null);
        } else if (key instanceof Class) {

            // If this test key is actually a key, return it.
            if (containsKey(key)) {
                return true;
            }

            // Iterate through all the keys, seeing if any are super-
            // classes of the test key; if so, there is at least one
            // related key.
            Class<?> testClass = (Class<?>) key;
            for (Class<?> thisClass : keySet()) {
                if ((thisClass != null)
                        && thisClass.isAssignableFrom(testClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V getProximate(Object key) {

        // Get the proximate for the test key, if any, and return
        // its associated value; otherwise return nothing.
        Class<?> bestMatch = getProximateKey(key);
        if (bestMatch != null) {
            return get(bestMatch);
        }
        return null;
    }

    @Override
    public Class<?> getProximateKey(Object key) {

        // A proximate key will exist only if the test key is a class.
        if (key instanceof Class) {

            // If this test key is actually a key, return it.
            if (containsKey(key)) {
                return (Class<?>) key;
            }

            // Iterate through all the keys, seeing if any are super-
            // classes of the test key and determining which of these,
            // if there are any such superclasses, is the closest to
            // the test key.
            Class<?> testClass = (Class<?>) key;
            Class<?> bestMatch = null;
            int generationDelta = 0;
            for (Class<?> thisClass : keySet()) {
                if ((thisClass != null)
                        && thisClass.isAssignableFrom(testClass)) {

                    // Get the number of generations between the test
                    // class and this class.
                    int thisGenerationDelta = Utils
                            .getGenerationsBetweenClasses(thisClass, testClass);

                    // If no other superclass has yet been found, or
                    // if this superclass has fewer intervening gener-
                    // ations than the previous best match (or it has
                    // the same number of generations but it is a
                    // class as opposed to an interface), record it as
                    // the best match so far.
                    if ((bestMatch == null)
                            || ((thisGenerationDelta < generationDelta) || ((thisGenerationDelta == generationDelta)
                                    && bestMatch.isInterface() && !thisClass
                                        .isInterface()))) {
                        bestMatch = thisClass;
                        generationDelta = thisGenerationDelta;
                    }
                }
            }
            return bestMatch;
        }
        return null;
    }
}
