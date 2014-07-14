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
 * Interface describing the methods to be implemented by a megawidget that is
 * stateful, and that also allows allows state to be set but not committed, and
 * then committed explicitly later. This can be useful, for example, in cases
 * where a megawidget holds multiple states that are interdependent, and thus
 * requires that all its state changes occur atomically from its perspective.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 */
public interface IExplicitCommitStateful extends IStateful {

    // Public Methods

    /**
     * Set the current state for the specified identifier without committing it.
     * 
     * @param identifier
     *            Identifier for which state is desired.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if its state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this <code>IStateful
     *             </code> implementation, or if the supplied state identifier
     *             is not valid.
     */
    public void setUncommittedState(String identifier, Object state)
            throws MegawidgetStateException;

    /**
     * Commit any accumulated uncommitted state changes.
     * 
     * @throws MegawidgetStateException
     *             If the new states are not valid due to interdependency
     *             conflicts.
     */
    public void commitStateChanges() throws MegawidgetStateException;
}