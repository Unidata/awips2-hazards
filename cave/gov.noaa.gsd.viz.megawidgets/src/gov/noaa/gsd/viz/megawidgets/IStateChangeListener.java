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

/**
 * State change listener, an interface that describes the methods that must be
 * implemented by any class that wishes to be notified when the state of an
 * <code>IStateful</code> is changing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 25, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IStateful
 */
public interface IStateChangeListener {

    // Public Methods

    /**
     * Receive notification that the given megawidget's state has changed.
     * 
     * @param megawidget
     *            Megawidget that experienced the state change.
     * @param identifier
     *            Identifier of the state that has changed.
     * @param state
     *            New state.
     */
    public void megawidgetStateChanged(IStateful megawidget, String identifier,
            Object state);
}