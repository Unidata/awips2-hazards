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

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Description: Tests for the class-keyed map.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 26, 2013    2336    Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ClassKeyedMapTest {

    // Private Variables

    /**
     * Class-keyed map.
     */
    private final ClassKeyedMap<String> map = new ClassKeyedMap<String>();

    // Public Methods

    /**
     * Clear the map before running a test.
     */
    @Before
    public void init() {
        map.clear();
    }

    /**
     * Null keys should always be proximate for null test keys.
     */
    @Test
    public void testNullMatchesNullQuery() {
        insertClassIntoMap(null);
        Assert.assertTrue(map.containsProximateKey(null));
    }

    /**
     * Non-null keys should never be proximate for null test keys.
     */
    @Test
    public void testNonNullNotProximateForNullQuery() {
        insertClassIntoMap(Object.class);
        Assert.assertFalse(map.containsProximateKey(null));
    }

    /**
     * Null keys should never be proximate to non-null test keys.
     */
    @Test
    public void testNullNotProximateForNonNullQuery() {
        insertClassIntoMap(null);
        Assert.assertFalse(map.containsProximateKey(Object.class));
    }

    /**
     * Non-null keys should never be related to null test keys.
     */
    @Test
    public void testNonNullNotRelatedToNullQuery() {
        insertClassIntoMap(Object.class);
        Assert.assertFalse(map.containsRelatedKey(null));
    }

    /**
     * Null keys should never be related to non-null test keys.
     */
    @Test
    public void testNullNotRelatedToNonNullQuery() {
        insertClassIntoMap(null);
        Assert.assertFalse(map.containsRelatedKey(Object.class));
    }

    /**
     * Keys should always be proximate to equivalent test keys.
     */
    @Test
    public void testKeyQueryProximateForEquivalentKey() {
        insertClassIntoMap(Object.class);
        Assert.assertTrue(map.containsProximateKey(Object.class));
    }

    /**
     * Keys should always be related to equivalent test keys.
     */
    @Test
    public void testKeyQueryRelatedToEquivalentKey() {
        insertClassIntoMap(Object.class);
        Assert.assertTrue(map.containsRelatedKey(Object.class));
    }

    /**
     * The <code>Object</code> key should be proximate for all test keys if
     * nothing else matches.
     */
    @Test
    public void testObjectClassProximateForArbitraryTestClasses() {
        insertClassIntoMap(Object.class);
        boolean stringIsObject = map.containsProximateKey(String.class);
        boolean hashMapIsObject = map.containsProximateKey(HashMap.class);
        Assert.assertTrue(stringIsObject && hashMapIsObject);
    }

    /**
     * A superclass or superinterface at one generational remove should always
     * be chosen over a superclass or superinterface at two generational
     * removes.
     */
    @Test
    public void testSmallestGenerationalDifferenceProximate() {
        insertClassIntoMap(Object.class);
        insertClassIntoMap(Serializable.class);
        Assert.assertTrue(map.getProximateKey(HashMap.class).equals(
                Serializable.class));
    }

    /**
     * A superclass at the same generational distance from a test key class as
     * an interface implemented by the latter should always be chosen as the
     * proximate key.
     */
    @Test
    public void testClassProximateBeforeInterfaceWhenGenerationallyEquidistant() {
        insertClassIntoMap(Serializable.class);
        boolean serializableMayBeProximate = map
                .containsProximateKey(HashMap.class);
        insertClassIntoMap(AbstractMap.class);
        boolean classProximateBeforeInterface = (map
                .getProximateKey(HashMap.class).equals(AbstractMap.class));
        Assert.assertTrue(serializableMayBeProximate
                && classProximateBeforeInterface);
    }

    // Private Methods

    /**
     * Create a key-value pair in the map for the specified class, with the
     * value being the simple name of the class. If the class is
     * <code>null</code>, a "(null)" is inserted as the value.
     * 
     * @param mappingClass
     *            Class to be placed in the map.
     */
    private void insertClassIntoMap(Class<?> mappingClass) {
        if (mappingClass == null) {
            map.put(null, "(null)");
        } else {
            map.put(mappingClass, mappingClass.getSimpleName());
        }
    }

}
