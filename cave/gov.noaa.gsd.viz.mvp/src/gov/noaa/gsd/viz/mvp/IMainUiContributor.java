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

/**
 * Interface describing the methods that must be implemented in order to create
 * a main UI contributor. The parameter <code>C</code> is the class used in
 * implementations for main UI contributions returned by <code>
 * contributeToMainUI()</code>. The parameter <code>E</code> is an enumerated
 * type used in implementations to indicate which portion of the main UI is
 * being contributed to when the method <code>contributeToMainUI()</code> is
 * invoked.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013   2166     Chris.Golden     Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMainUiContributor<C, E extends Enum<E>> {

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
     * @return List of contributions; this may be empty if none are to be made.
     */
    public List<? extends C> contributeToMainUI(E type);
}
