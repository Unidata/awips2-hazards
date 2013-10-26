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
 * Interface describing the methods to be implemented by a megawidget that holds
 * state that may be set or queried externally, and that may be altered via
 * user-GUI interaction. When the latter occurs, it notifies an
 * <code>IStateChangeListener</code> that its state is changing. Any subclasses
 * of <code>Megawidget</code> must implement this interface if they are to have
 * accessible state and issue such notifications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Oct 23, 2013   2168     Chris.Golden      Minor cleanup.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IStateChangeListener
 * @see Megawidget
 * @see IStatefulSpecifier
 */
public interface IStateful extends IMegawidget {

    // Public Static Constants

    /**
     * State change listener megawidget creation time parameter name; if
     * specified in the map passed to <code>createMegawidget()</code>, its value
     * must be an object of type <code>IStateChangeListener</code>.
     */
    public static final String STATE_CHANGE_LISTENER = "stateChangeListener";

    // Public Methods

    /**
     * Get the current state for the specified identifier.
     * 
     * @param identifier
     *            Identifier for which state is desired.
     * @return Object making up the current state for that identifier.
     * @throws MegawidgetStateException
     *             If the supplied state identifier is not valid.
     */
    public Object getState(String identifier) throws MegawidgetStateException;

    /**
     * Set the current state for the specified identifier.
     * 
     * @param identifier
     *            Identifier for which state is to be set.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if its state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this <code>IStateful
     *             </code> implementation, or if the supplied state identifier
     *             is not valid.
     */
    public void setState(String identifier, Object state)
            throws MegawidgetStateException;

    /**
     * Get a shortened description of the specified state for the specified
     * identifier.
     * 
     * @param identifier
     *            Identifier to which the state would be assigned.
     * @param state
     *            State for which to generate a shortened description.
     * @return Description of the specified state.
     * @throws MegawidetStateException
     *             If the specified state is not of a valid type for this
     *             <code>IStateful</code> implementation, or if the supplied
     *             state identifier is not valid.
     */
    public String getStateDescription(String identifier, Object state)
            throws MegawidgetStateException;
}