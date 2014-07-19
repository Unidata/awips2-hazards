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

import java.util.Map;

/**
 * State change listener, an interface that describes the methods that must be
 * implemented by any class that wishes to be notified when the state of an
 * {@link IStateful} is changing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 25, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget" in
 *                                           comments and variable names.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 30, 2014    3512    Chris.Golden      Changed to include new method that
 *                                           allows notification of multiple
 *                                           simultaneous state changes.
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

    /**
     * Receive notification that the given megawidget's states have changed.
     * 
     * @param megawidget
     *            Megawidget that experienced the state changes.
     * @param statesForIdentifiers
     *            Map pairing state identifiers with their new values.
     *            Multi-state megawidgets do not have to include all of their
     *            states in this map, only those that have changed. Note that
     *            this map may be modified by implementations of this method,
     *            since it will be a copy of any map of states kept by the
     *            megawidget.
     */
    public void megawidgetStatesChanged(IStateful megawidget,
            Map<String, Object> statesForIdentifiers);
}