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

import java.util.HashMap;
import java.util.Map;

/**
 * Explicit-commit stateful megawidget created by a megawidget specifier. Such
 * megawidgets can have state changes made just as a <code>StatefulMegawidget
 * </code> (meaning that state changes occur immediately), or they may have
 * multiple state changes accumulated and then committed all at once.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see StatefulMegawidgetSpecifier
 */
public abstract class ExplicitCommitStatefulMegawidget extends
        StatefulMegawidget implements IExplicitCommitStateful {

    // Private Variables

    /**
     * Mapping of state identifiers to values they should take on when an
     * explicit commit is ordered.
     */
    private final Map<String, Object> uncommittedStatesForIds;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected ExplicitCommitStatefulMegawidget(
            StatefulMegawidgetSpecifier specifier, Map<String, Object> paramMap) {
        super(specifier, paramMap);
        uncommittedStatesForIds = new HashMap<String, Object>();
    }

    // Public Methods

    /**
     * Add a state change to occur for the specified identifier later, when
     * <code>commitStateChanges()</code> is invoked.
     * 
     * @param identifier
     *            Identifier for which state is to be changed later, when an
     *            explicit commit is requested.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if its state should be reset.
     * @throws MegawidgetStateException
     *             If the new state is not of a valid type for this <code>
     *             StatefulMegawidget</code> implementation, or if the supplied
     *             state identifier is not valid.
     */
    @Override
    public final void setUncommittedState(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure that the state identifier is valid.
        ((StatefulMegawidgetSpecifier) getSpecifier())
                .ensureStateIdentifierIsValid(identifier);

        // Ensure that the supplied state is valid.
        ensureStateIsValid(identifier, state);

        // Compare with the old state, the uncommitted
        // one if one exists for this identifier, or
        // the previously-committed one if not; if they
        // are the same, do nothing more.
        Object oldState = uncommittedStatesForIds.get(identifier);
        if (oldState == null) {
            oldState = doGetState(identifier);
        }
        if ((oldState == state)
                || ((oldState != null) && (state != null) && state
                        .equals(oldState))) {
            return;
        }

        // Add the state to the mapping of uncommitted
        // changes.
        uncommittedStatesForIds.put(identifier, state);
    }

    /**
     * Commit any accumulated uncommitted state changes.
     * 
     * @throws MegawidgetStateException
     *             If the new states are not valid due to interdependency
     *             conflicts.
     */
    @Override
    public final void commitStateChanges() throws MegawidgetStateException {

        // Ensure that the state is not being set al-
        // ready before actually committing accumulated
        // state changes.
        if (isSettingState) {
            return;
        }

        // If there are no uncommitted state changes,
        // do nothing.
        if (uncommittedStatesForIds.isEmpty()) {
            return;
        }

        // Commit uncommitted state changes, ensuring
        // that the flag that indicates state is being
        // set is high for the duration of the commit.
        isSettingState = true;
        doCommitStateChanges(uncommittedStatesForIds);
        uncommittedStatesForIds.clear();
        isSettingState = false;
    }

    // Protected Methods

    /**
     * Ensure that the specified state is valid for the specified identifier.
     * 
     * @param identifier
     *            Identifier for which state is intended. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this widget.
     * @param state
     *            Object making up the state that is intended for this
     *            identifier, or <code>null</code> if the intention is to reset
     *            the state.
     * @throws MegawidgetStateException
     *             If new state is not valid for this <code>
     *             ExplicitCommitStatefulMegawidget</code> implementation.
     */
    protected abstract void ensureStateIsValid(String identifier, Object state)
            throws MegawidgetStateException;

    /**
     * Commit all specified uncommitted states for their corresponding
     * identifiers. This method is called by <code>commitStateChanges()</code>
     * only after the latter has ensured that there are state changes in the
     * specified mapping to be made, that they are valid, and after it has set a
     * flag that indicates that this committing of the state will not trigger
     * the widget to notify its listener of an invocation. This method should
     * commit all state changes in an atomic fashion, that is, any
     * interdependencies of the states should be checked only after all state
     * changes have been committed.
     * 
     * @param newStatesForIds
     *            Mapping from state identifiers to the uncommitted state
     *            changes each should experience.
     * @throws MegawidgetStateException
     *             If the new states are not valid due to interdependency
     *             conflicts.
     */
    protected abstract void doCommitStateChanges(
            Map<String, Object> newStatesForIds)
            throws MegawidgetStateException;

    /**
     * Set the states to the values in the specified map.
     * 
     * @param states
     *            Map containing keys drawn from the set of all valid state
     *            identifiers, with associated values being the new values for
     *            the states. Any state with a identifier-value pair found
     *            within this map is set to the given value; all states for
     *            which no identifier-value pairs exist remain as they were
     *            before.
     * @throws MegawidgetPropertyException
     *             If at least one identifier specifies a nonexistent state, or
     *             if at least one value is invalid.
     */
    @Override
    protected void setStates(Map<String, Object> states)
            throws MegawidgetPropertyException {

        // Iterate through the pairs, setting each value as the uncommitted
        // state for the corresponding identifier, and then committing them
        // all at once.
        try {
            for (String identifier : states.keySet()) {
                setUncommittedState(identifier, states.get(identifier));
            }
            commitStateChanges();
        } catch (MegawidgetStateException e) {
            throw new MegawidgetPropertyException(getSpecifier()
                    .getIdentifier(),
                    StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES,
                    getSpecifier().getType(), states, "bad map of values", e);
        }
    }
}