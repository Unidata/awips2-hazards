/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.util.Collection;
import java.util.Map;

/**
 * Description: Interface describing the methods to be implemented by any class
 * that is to be capable of applying arbitrary side effects to a set of
 * megawidgets when one of the megawidgets is invoked or has its state changed
 * (the latter either via the GUI or programmatically), or when the megawidgets
 * are first constructed. Side effects may include changes to the megawidgets'
 * mutable properties, including state, and/or other actions unrelated to the
 * megawidgets themselves.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 06, 2013   1277     Chris.Golden      Initial creation
 * Feb 14, 2014   2161     Chris.Golden      Changed Javadoc comments to
 *                                           explain exactly when a side
 *                                           effects applier is invoked, and
 *                                           altered signature of the method
 *                                           to allow for multiple trigger
 *                                           identifiers to be specified.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MegawidgetManager
 */
public interface ISideEffectsApplier {

    // Public Methods

    /**
     * Apply side effects as a result of the the specified megawidget's
     * invocation.
     * 
     * @param triggerIdentifiers
     *            List of identifiers of the megawidgets that were invoked or
     *            had their states changed, thus precipitating this method call.
     *            If <code>null</code>, this means that this call is being made
     *            to initialize the megawidgets.
     * @param mutableProperties
     *            Map of megawidget identifiers to submaps holding those
     *            megawidgets' mutable properties. The latter map the mutable
     *            property names to their values.
     * @param propertiesMayHaveChanged
     *            Flag indicating whether or not <code>mutableProperties</code>
     *            may have changed since the last invocation of this method for
     *            this instance. Implementations may use this for optimization
     *            if appropriate.
     * @return Map of megawidget identifiers that experienced a change to their
     *         mutable properties as a result of this method call to submaps
     *         holding their new mutable property values. As a hierarchy this is
     *         identical to the <code>mutableProperties</code> map, but only
     *         those property name/value pairs for which the values changed are
     *         included. Thus, any megawidget that experienced no change as a
     *         result of the side effect application is not included in the map,
     *         and any megawidget for which not all mutable properties changed
     *         only requires a submap that includes those properties which did
     *         change. If <code>null</code> is returned, no properties are to be
     *         changed.
     */
    public Map<String, Map<String, Object>> applySideEffects(
            Collection<String> triggerIdentifiers,
            Map<String, Map<String, Object>> mutableProperties,
            boolean propertiesMayHaveChanged);
}
