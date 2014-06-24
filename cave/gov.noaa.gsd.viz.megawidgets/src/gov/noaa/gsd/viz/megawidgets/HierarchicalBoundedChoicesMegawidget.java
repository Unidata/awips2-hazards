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
import java.util.HashMap;
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
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 24, 2014   4010     Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalBoundedChoicesMegawidgetSpecifier
 */
public abstract class HierarchicalBoundedChoicesMegawidget extends
        BoundedChoicesMegawidget<Collection<Object>> {

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
        state.addAll((Collection<?>) specifier.getStartingState(specifier
                .getIdentifier()));
    }

    // Public Methods

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
        return createChoicesCopy(state);
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Convert the provided state to a valid value, and record it.
         */
        Collection<?> hierarchy;
        try {
            hierarchy = getStateValidator().convertToStateValue(state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        this.state.clear();
        this.state.addAll(createChoicesCopy(hierarchy));

        /*
         * Synchronize user-facing widgets to the new state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * If the state is not a list and is not null, an error has occurred.
         */
        if (state == null) {
            return "";
        } else if ((state instanceof List) == false) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be list or set of choices");
        }

        /*
         * Iterate through the elements of the provided state, concatenating the
         * description of each to the buffer.
         */
        StringBuilder buffer = new StringBuilder();
        addElementsDescriptionsToBuffer(buffer, (List<?>) state);
        return buffer.toString();
    }

    /**
     * Notify any listeners of a state change and invocation.
     */
    protected final void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(), createChoicesCopy(state));
    }

    // Private Methods

    /**
     * Create a copy of the specified choices collection.
     * 
     * @param choices
     *            Collection to be copied.
     * @return Copy of the specified choices.
     */
    private List<?> createChoicesCopy(Collection<?> choices) {

        /*
         * Create a new list, and copy each element into it from the old list.
         * If an element is a map, then make a copy of the map instead of using
         * the original, and also copy the child list within that map.
         */
        List<Object> listCopy = new ArrayList<>();
        for (Object item : choices) {
            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                Map<String, Object> mapCopy = new HashMap<>();
                for (Object key : map.keySet()) {
                    if (key.equals(HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_CHILDREN)) {
                        mapCopy.put((String) key,
                                createChoicesCopy((Collection<?>) map.get(key)));
                    } else {
                        mapCopy.put((String) key, map.get(key));
                    }
                }
                listCopy.add(mapCopy);
            } else {
                listCopy.add(item);
            }
        }
        return listCopy;
    }

    /**
     * Get a text string describing the specified choice state element.
     * 
     * @param element
     *            State element; may be either a string or a map giving the
     *            parameters governing the choice.
     * @return Text string describing the specified choice state element.
     */
    @SuppressWarnings("unchecked")
    private String getStateElementDescription(Object element) {
        StringBuilder buffer = new StringBuilder(
                ((BoundedChoicesMegawidgetSpecifier<Collection<?>>) getSpecifier())
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