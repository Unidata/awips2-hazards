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
 * a view. The parameter <code>I</code> is the class used in implementations for
 * the identifiers of main UI contributions returned by
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
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2013     585    Chris.Golden     Changed to have contributions to
 *                                          main UI be returned instead of made
 *                                          directly by the contribute method.
 *                                          This allows for delayed use of said
 *                                          contributions in case the main UI
 *                                          is not available at the time the
 *                                          method is called.
 * Oct 22, 2013    2166    Chris.Golden     Made subinterface of new interface
 *                                          IMainUiContributor, in order to
 *                                          separate the latter's functionality
 *                                          out so as to allow non-view objects
 *                                          to contribute to the main UI.
 * Jan 17, 2018   33428    Chris.Golden     Changed to work with new, more
 *                                          flexible toolbar contribution code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IView<I, C, E extends Enum<E>>
        extends IMainUiContributor<I, C, E> {

    // Public Methods

    /**
     * Get the view ready for disposal.
     */
    public void dispose();
}
