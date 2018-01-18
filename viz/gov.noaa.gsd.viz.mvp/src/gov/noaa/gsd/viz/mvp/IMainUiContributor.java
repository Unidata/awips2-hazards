/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.mvp;

import java.util.List;
import java.util.Map;

/**
 * Interface describing the methods that must be implemented in order to create
 * a main UI contributor. The parameter <code>I</code> is the class used in
 * implementations for the identifiers of main UI contributions returned by
 * {@link #contributeToMainUi(Object)}, the parameter <code>C</code> is the
 * class used in implementations for main UI contributions themselves returned
 * by <code>contributeToMainUi()</code>and the parameter <code>E</code> is an
 * enumerated type used in implementations to indicate which portion of the main
 * UI is being contributed to when the method <code>contributeToMainUi()</code>
 * is invoked.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Oct 22, 2013    2166    Chris.Golden  Initial creation.
 * Jan 17, 2018   33428    Chris.Golden  Changed to work with new, more flexible
 *                                       toolbar contribution code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMainUiContributor<I, C, E extends Enum<E>> {

    // Public Methods

    /**
     * Get any contributions to the main UI that the implementation desires to
     * make. Note that this method may be called multiple times per <code>type
     * </code> to (re)populate the main UI with the specified <code>type</code>;
     * implementations are responsible for cleaning up after contributed items
     * that may exist from a previous call with the same <code>type</code>.
     * 
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return Map pairing identifiers of contributions to lists of the
     *         contributions themselves; this map may be empty if none are to be
     *         made. Each value in the map is a list because some identifiers
     *         are paired with multiple contributions that must be displayed
     *         sequentially.
     */
    public Map<? extends I, List<? extends C>> contributeToMainUi(E type);
}
