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

/**
 * Interface describing the methods that must be implemented in order to create
 * a view. The parameter <code>C</code> is the class used in implementations as
 * a main user-interface contribution manager, allowing each view to contribute
 * to the main UI if appropriate. The parameter <code>E</code> is an enumerated
 * type used in implementations to indicate which portion of the main UI is
 * being contributed to when the method <code>contributeToMainUI()</code> is
 * invoked.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IView<C, E extends Enum<E>> {

    // Public Methods

    /**
     * Get the view ready for disposal.
     */
    public void dispose();

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type</code> to (re)populate the main UI
     * with the specified <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist from a previous call
     * with the same <code>type</code>.
     * 
     * @param mainUI
     *            Main user interface to which to contribute.
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return True if items were contributed, otherwise false.
     */
    public boolean contributeToMainUI(C mainUI, E type);
}
