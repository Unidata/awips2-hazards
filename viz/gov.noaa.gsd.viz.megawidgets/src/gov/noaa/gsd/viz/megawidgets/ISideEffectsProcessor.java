/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.util.Collection;
import java.util.Map;

/**
 * Description: Interface describing the methods to be implemented by any class
 * that is to be capable of pre- and/or postprocessing mutable properties that
 * are being modified by a {@link ISideEffectsApplier}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 10, 2017  39151     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MegawidgetManager
 */
public interface ISideEffectsProcessor {

    // Public Methods

    /**
     * Preprocess the specified mutable properties prior to the invocation of an
     * {@link ISideEffectsApplier} instance's
     * {@link ISideEffectsProcessor#applySideEffects(Collection, Map, boolean)}
     * <p>
     * <strong>Note</strong>: Caution should be exercised in the implementation
     * of this method, since it has the capability to modify the mutable
     * properties map to be passed to the side effects applier.
     * </p>
     * 
     * @param triggerIdentifiers
     *            List of identifiers of the megawidgets that were invoked or
     *            had their states changed, thus precipitating this method call.
     *            If <code>null</code>, this means that this call is being made
     *            to initialize the megawidgets. Otherwise, if any of the items
     *            in the list are of the form
     *            <code>&lt;identifier&gt;.&lt;subcommand&gt</code>, this
     *            indicates that that subcommand of the megawidget given by that
     *            identifier was the trigger.
     * @param mutableProperties
     *            Map of megawidget identifiers to submaps holding those
     *            megawidgets' mutable properties. The latter map the mutable
     *            property names to their values.
     * @return Mutable properties, modified as appropriate.
     */
    public Map<String, Map<String, Object>> preprocessSideEffects(
            Collection<String> triggerIdentifiers,
            Map<String, Map<String, Object>> mutableProperties);

    /**
     * Postprocess the specified mutable properties following the invocation of
     * an {@link ISideEffectsApplier} instance's
     * {@link ISideEffectsProcessor#applySideEffects(Collection, Map, boolean)}.
     * This method is only invoked if the side effects applier did indeed change
     * something in the mutable properties.
     * <p>
     * <strong>Note</strong>: Caution should be exercised in the implementation
     * of this method, since it has the capability to modify the mutable
     * properties map that was returned by the side effects applier.
     * </p>
     * 
     * @param triggerIdentifiers
     *            List of identifiers of the megawidgets that were invoked or
     *            had their states changed, thus precipitating this method call.
     *            If <code>null</code>, this means that this call is being made
     *            to initialize the megawidgets. Otherwise, if any of the items
     *            in the list are of the form
     *            <code>&lt;identifier&gt;.&lt;subcommand&gt</code>, this
     *            indicates that that subcommand of the megawidget given by that
     *            identifier was the trigger.
     * @param mutableProperties
     *            Map of megawidget identifiers to submaps holding those
     *            megawidgets' mutable properties. The latter map the mutable
     *            property names to their values.
     * @return Mutable properties, modified as appropriate.
     */
    public Map<String, Map<String, Object>> postprocessSideEffects(
            Collection<String> triggerIdentifiers,
            Map<String, Map<String, Object>> mutableProperties);
}
