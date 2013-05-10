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

import gov.noaa.gsd.viz.megawidgets.ChoicesMegawidgetSpecifier.IllegalChoicesProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hierarchical choices megawidget, allowing the selection of zero or more
 * choices in a hierarchy, presented to the user in tree form.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2013            Chris.Golden      Initial creation
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalChoicesMegawidgetSpecifier
 */
public abstract class HierarchicalChoicesMegawidget extends ChoicesMegawidget {

    // Protected Variables

    /**
     * State associated with the tree.
     */
    protected final List<Object> state = new ArrayList<Object>();

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
    protected HierarchicalChoicesMegawidget(
            HierarchicalChoicesMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
    }

    // Public Methods

    /**
     * Get the available choices hierarchy.
     * 
     * @return Available choices hierarchy.
     */
    public final List<?> getChoices() {
        return doGetChoices();
    }

    /**
     * Set the choices to those specified. If the current state is not a subset
     * of the new choices, the state will be set to <code>null</code>.
     * 
     * @param value
     *            List of new choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    public final void setChoices(Object value)
            throws MegawidgetPropertyException {
        doSetChoices(value);
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return true;
    }

    /**
     * Get the current state for the specified identifier. This method is called
     * by <code>getState()</code> only after the latter has ensured that the
     * supplied state identifier is valid.
     * 
     * @param identifier
     *            Identifier for which state is desired. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @return Object making up the current state for the specified identifier.
     */
    @Override
    protected final Object doGetState(String identifier) {
        return ((HierarchicalChoicesMegawidgetSpecifier) getSpecifier())
                .createChoicesCopy(state);
    }

    /**
     * Set the current state for the specified identifier. This method is called
     * by <code>setState()</code> only after the latter has ensured that the
     * supplied state identifier is valid, and has set a flag that indicates
     * that this setting of the state will not trigger the megawidget to notify
     * its listener of an invocation.
     * 
     * @param identifier
     *            Identifier for which state is to be set. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if this state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this <code>
     *             StatefulWidget</code> implementation.
     */
    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure that the provided hierarchy is legal.
        List<?> hierarchy = (List<?>) state;
        if (hierarchy != null) {
            HierarchicalChoicesMegawidgetSpecifier specifier = getSpecifier();
            IllegalChoicesProblem eval = specifier
                    .evaluateChoicesLegality(hierarchy);
            if (eval != null) {
                throw new MegawidgetStateException(identifier,
                        specifier.getType(), hierarchy,
                        (eval.getDepth() == 0 ? "" : "parameter \""
                                + eval.getSubParameterName() + "\" ")
                                + eval.getProblem());
            }

            // Ensure that the provided hierarchy is a subset
            // of the choices hierarchy.
            if (specifier.isSubset(hierarchy, choices) == false) {
                throw new MegawidgetStateException(identifier,
                        specifier.getType(), hierarchy,
                        "not a subset of the choices hierarchy");
            }

            // Set the state to match the provided state.
            this.state.clear();
            this.state.addAll(specifier.createChoicesCopy(hierarchy));
        } else {
            this.state.clear();
        }

        // Synchronize user-facing widgets to the new state.
        synchronizeWidgetsToState();
    }

    /**
     * Get a shortened description of the specified state for the specified
     * identifier. This method is called by <code>getStateDescription() only
     * after the latter has ensured that the supplied state identifier is
     * valid.
     * 
     * @param identifier
     *            Identifier to which the state would be assigned.
     *            Implementations may assume that the state identifier supplied
     *            by this parameter is valid for this megawidget.
     * @param state
     *            State for which to generate a shortened description.
     * @return Description of the specified state.
     * @throws MegawidgetStateException
     *             If the specified state is not of a valid type for this
     *             <code>StatefulWidget </code> implementation.
     */
    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {

        // It would be difficult to condense a tree down
        // to a shortened descriptions, so this is not
        // supported.
        throw new UnsupportedOperationException();
    }

    /**
     * Notify any listeners of a state change and invocation.
     */
    protected final void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(), state);
        notifyListener();
    }
}