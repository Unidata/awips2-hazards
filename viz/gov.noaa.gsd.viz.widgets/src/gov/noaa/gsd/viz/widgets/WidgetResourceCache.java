/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Resource;

/**
 * Cache for holding SWT {@link Resource} instances for use by multiple widgets.
 * The generic parameter <code>K</code> is the key type with which the resources
 * are associated in the cache, and <code>R</code> is the type of resource.
 * Resources that are added should have {@link Resource#isDisposed()} return
 * <code>false</code> at the time of addition, and they should never be disposed
 * of by the client of the cache; the cache is responsible for disposing of any
 * resources it is managing when and if it is reset via {@link #prune()}.
 * <p>
 * Note that it is important that a client call {@link #release(Object)} and/or
 * {@link #release(Resource)} for a particular key a number of times equivalent
 * to sum of the number of times that same client called
 * {@link #add(Object, Resource)} (which should be either 0 or 1 times for a
 * particular key) and {@link #acquire(Object)}. Failure to do this will leave
 * the usage count for a particular resource positive, meaning that if
 * {@link #prune()} is called, the specified resource will not be disposed of.
 * </p>
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 19, 2018   33787    Chris.Golden  Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public class WidgetResourceCache<K, R extends Resource> {

    // Private Variables

    /**
     * Map used as the actual cache.
     */
    private final Map<K, R> cache = new HashMap<>();

    /**
     * Reverse of {@link #cache}, but using identity to speed up operations.
     */
    private final Map<R, K> keysForResources = new IdentityHashMap<>();

    /**
     * Map pairing each resource found in {@link #cache} with the number of
     * users that resource.
     */
    private final Map<R, Integer> userCountsForResources = new IdentityHashMap<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public WidgetResourceCache() {
    }

    // Public Methods

    /**
     * Add the specified resource to the cache, associating it with the
     * specified key. Note that after it is added, it is considered to be in use
     * by one user (i.e. as if {@link #acquire(Object)} had been called once
     * following its addition).
     * <p>
     * <strong>WARNING</strong>: The supplied resource should never have its
     * {@link Resource#dispose()} method invoked; that should only be done by
     * the cache. It also must not be disposed at the time that it is added.
     * Finally, the supplied key must not already be associated with an existing
     * resource in the cache.
     * </p>
     * 
     * @param key
     *            Key with which the resource is to be associated.
     * @param resource
     *            Resource to be added. Must not be <code>null</code>, and must
     *            not be disposed.
     */
    public void add(K key, R resource) {
        if (cache.containsKey(key)) {
            throw new IllegalArgumentException("key already in use");
        }
        if (resource == null) {
            throw new IllegalArgumentException("null resource supplied");
        }
        cache.put(key, resource);
        keysForResources.put(resource, key);
        userCountsForResources.put(resource, 1);
    }

    /**
     * Acquire the resource associated with the specified key. If a resource is
     * associated with said key, then its usage count is incremented by 1 and it
     * is returned.
     * <p>
     * <strong>WARNING</strong>: The returned resource should never have its
     * {@link Resource#dispose()} method invoked; that should only be done by
     * the cache.
     * </p>
     * 
     * @param key
     *            Key with which the resource to be fetched is associated.
     * @return Resource that is associated with the key, or <code>null</code> if
     *         there is no resource associated with the key.
     */
    public R acquire(K key) {
        if (cache.containsKey(key)) {
            R resource = cache.get(key);
            userCountsForResources.put(resource,
                    userCountsForResources.get(resource) + 1);
            return resource;
        }
        return null;
    }

    /**
     * Release the resource in the cache that is associated with the specified
     * key. This decrements the associated resource's usage count by 1, if an
     * associated resource is found. The associated resource is not disposed of
     * or forgotten.
     * 
     * @param key
     *            Key for which the associated resource is to be released.
     */
    public void release(K key) {
        if (cache.containsKey(key)) {
            R resource = cache.get(key);
            int count = userCountsForResources.get(resource);
            if (count > 0) {
                userCountsForResources.put(resource, count - 1);
            }
        }
    }

    /**
     * Release the specified resource in the cache. This decrements the
     * resource's usage count by 1, if the resource is found within the cache.
     * The associated resource is not disposed of or forgotten.
     * 
     * @param resource
     *            Resource that is to be released.
     */
    public void release(R resource) {
        K key = keysForResources.get(resource);
        if (key != null) {
            int count = userCountsForResources.get(resource);
            if (count > 0) {
                userCountsForResources.put(resource, count - 1);
            }
        }
    }

    /**
     * Prune the cache of any resources that have usage counts of 0.
     */
    public void prune() {
        for (Iterator<Map.Entry<K, R>> iterator = cache.entrySet()
                .iterator(); iterator.hasNext();) {
            R resource = iterator.next().getValue();
            if (userCountsForResources.get(resource) == 0) {
                userCountsForResources.remove(resource);
                keysForResources.remove(resource);
                iterator.remove();
                resource.dispose();
            }
        }
    }
}
