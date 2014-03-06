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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Hierarchical choices megawidget, allowing the selection of zero or more
 * choices in a closed-set hierarchy, presented to the user in tree form.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2013            Chris.Golden      Initial creation
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Minor cleanup, and added missing
 *                                           implementation of doGetStateDescription().
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Jan 28, 2014   2161     Chris.Golden      Changed to support use of collections
 *                                           instead of only lists for the state.
 * Mar 06, 2014   2155     Chris.Golden      Fixed bug caused by a lack of
 *                                           defensive copying of the state when
 *                                           notifying a state change listener of
 *                                           a change.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalBoundedChoicesMegawidgetSpecifier
 */
public abstract class HierarchicalBoundedChoicesMegawidget extends
        BoundedChoicesMegawidget {

    // Protected Variables

    /**
     * State associated with the tree.
     */
    protected final List<Object> state = new ArrayList<>();

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
    protected HierarchicalBoundedChoicesMegawidget(
            HierarchicalBoundedChoicesMegawidgetSpecifier specifier,
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

    @Override
    protected final Object doGetState(String identifier) {
        return ((HierarchicalBoundedChoicesMegawidgetSpecifier) getSpecifier())
                .createChoicesCopy(state);
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // If the state is not a collection, an error has occurred.
        if ((state != null) && ((state instanceof Collection) == false)) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be list of choices");
        }

        // Ensure that the provided hierarchy is legal.
        Collection<?> hierarchy = (Collection<?>) state;
        if (hierarchy != null) {
            HierarchicalBoundedChoicesMegawidgetSpecifier specifier = getSpecifier();
            ChoicesMegawidgetSpecifier.IllegalChoicesProblem eval = specifier
                    .evaluateChoicesLegality(identifier, hierarchy);
            if (eval != HierarchicalBoundedChoicesMegawidgetSpecifier.NO_ILLEGAL_CHOICES_PROBLEM) {
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

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {

        // If the state is not a list and is not null, an error
        // has occurred.
        if (state == null) {
            return "";
        } else if ((state instanceof List) == false) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be list of choices");
        }

        // Iterate through the elements of the provided state,
        // concatenating the description of each to the buffer.
        StringBuilder buffer = new StringBuilder();
        addElementsDescriptionsToBuffer(buffer, (List<?>) state);
        return buffer.toString();
    }

    /**
     * Notify any listeners of a state change and invocation.
     */
    protected final void notifyListeners() {
        notifyListener(
                getSpecifier().getIdentifier(),
                ((HierarchicalBoundedChoicesMegawidgetSpecifier) getSpecifier())
                        .createChoicesCopy(state));
        notifyListener();
    }

    // Private Methods

    /**
     * Get a text string describing the specified choice state element.
     * 
     * @param element
     *            State element; may be either a string or a map giving the
     *            parameters governing the choice.
     * @return Text string describing the specified choice state element.
     */
    private String getStateElementDescription(Object element) {
        StringBuilder buffer = new StringBuilder(
                ((BoundedChoicesMegawidgetSpecifier) getSpecifier())
                        .getNameOfNode(element));
        if (element instanceof Map) {
            List<?> children = (List<?>) ((Map<?, ?>) element)
                    .get(HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_CHILDREN);
            if (children != null) {
                buffer.append(" (");
                addElementsDescriptionsToBuffer(buffer, children);
                buffer.append(")");
            }
        }
        return buffer.toString();
    }

    /**
     * Add the specified choice elements' descriptions to the specified string
     * buffer.
     * 
     * @param buffer
     *            Buffer to which descriptions are to be added.
     * @param elements
     *            List of elements for which to generate descriptions.
     */
    private void addElementsDescriptionsToBuffer(StringBuilder buffer,
            List<?> elements) {
        boolean beyondFirst = false;
        for (Object element : elements) {
            if (beyondFirst) {
                buffer.append(", ");
            } else {
                beyondFirst = true;
            }
            buffer.append(getStateElementDescription(element));
        }
    }
}