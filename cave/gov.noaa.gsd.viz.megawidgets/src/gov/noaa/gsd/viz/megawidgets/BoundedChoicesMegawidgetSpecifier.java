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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Base class for megawidget specifiers that include closed sets of choices as
 * part of their state. Said choices are always associated with a single state
 * identifier, so the megawidget identifiers for these specifiers must not
 * consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2013   2336     Chris.Golden      Initial creation.
 * Jan 28, 2014   2161     Chris.Golden      Changed to support use of
 *                                           collections instead of only
 *                                           lists for the state.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedChoicesMegawidget
 */
public abstract class BoundedChoicesMegawidgetSpecifier extends
        ChoicesMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Possible megawidget state values parameter name; a megawidget must
     * include an array of one or more choices associated with this name. Each
     * such choice may be either a string, meaning that the string value is used
     * as the choice's name, or else a {@link Map} holding an entry for
     * {@link #CHOICE_NAME} and, optionally, an entry for
     * {@link #CHOICE_IDENTIFIER}. Subclasses may have additional required or
     * optional entries in the map. Regardless, a given string must occur at
     * most once as a choice name.
     */
    public static final String MEGAWIDGET_VALUE_CHOICES = "choices";

    /**
     * Choice identifier parameter name; each choice in the array of choices
     * associated with {@link #MEGAWIDGET_VALUE_CHOICES} that is a map may
     * contain a reference to a string associated with this name. The string
     * serves as the identifier of the choice. If not provided, the
     * {@link #CHOICE_NAME} is used as its identifier instead. Each identifier
     * must be unique in the set of all choice identifiers.
     */
    public static final String CHOICE_IDENTIFIER = "identifier";

    // Private Variables

    /**
     * Choices structure.
     */
    private final List<?> choicesList;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public BoundedChoicesMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the possible values are present as a list
        // of objects.
        List<?> choicesList = null;
        try {
            choicesList = getChoicesFromObject(parameters
                    .get(MEGAWIDGET_VALUE_CHOICES));
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), MEGAWIDGET_VALUE_CHOICES, e.getBadValue(),
                    e.getMessage());
        }

        // Evaluate the legality of the choices list.
        IllegalChoicesProblem eval = evaluateChoicesLegality(
                MEGAWIDGET_VALUE_CHOICES, choicesList);
        if (eval != NO_ILLEGAL_CHOICES_PROBLEM) {
            throw eval.toSpecificationException(this);
        }
        this.choicesList = ImmutableList.copyOf(choicesList);
    }

    // Public Methods

    /**
     * Get the list of choices associated with this specifier. The returned list
     * is identical in structure to that provided to the specifier as the
     * {@link #MEGAWIDGET_VALUE_CHOICES} parameter.
     * 
     * @return List of choices; this list is not modifiable.
     */
    public final List<?> getChoices() {
        return choicesList;
    }

    // Protected Methods

    /**
     * Determine whether or not the first specified node, taken from the choices
     * list, is a subset of the second based upon subclass-specific details. It
     * is assumed that the name and/or identifier of the first node has been
     * found to be the same as that of the second when this method is called.
     * Implementations must determine whether any subclass-specific details of
     * the nodes allow the first to be a subset of the second.
     * 
     * @param node1
     *            Node to be checked to see if it is a subset of the other node.
     *            This must be either a {@link String} giving an identifier or
     *            else a {@link Map}, with the latter holding the usual elements
     *            for a choice element map.
     * @param node2
     *            Node to be checked to see if it is a superset of the other
     *            node. This must be either a {@link String} giving an
     *            identifier or else a {@link Map}, with the latter holding the
     *            usual elements for a choice element map.
     * @return True if the first node is a subset of the second, false
     *         otherwise.
     */
    protected abstract boolean isNodeSubset(Object node1, Object node2);

    /**
     * Determine whether the first choices list specified is a subset of the
     * second.
     * 
     * @param list1
     *            Choices list to be checked to see if it is a subset of the
     *            other list.
     * @param list2
     *            Choices list to be checked to see if it is a superset of the
     *            other list.
     * @return True if the first choices list is a subset of the second, false
     *         otherwise.
     */
    protected final boolean isSubset(Collection<?> list1, List<?> list2) {

        // If the subset is null, it is indeed a subset.
        if (list1 == null) {
            return true;
        }

        // For each node in the subset, find the equivalent
        // node in the other list, and ensure that the super-
        // set one has at least all the nodes of the subset
        // one.
        for (Object node1 : list1) {

            // Find the superset node equivalent to this
            // subset node; if not found, it is not a subset.
            String identifier = getIdentifierOfNode(node1);
            int supersetIndex;
            for (supersetIndex = 0; supersetIndex < list2.size(); supersetIndex++) {
                if (identifier.equals(getIdentifierOfNode(list2
                        .get(supersetIndex)))) {
                    break;
                }
            }
            if (supersetIndex == list2.size()) {
                return false;
            }

            // Ensure that subclass-specific details allow
            // the first node to be a subset of the second.
            if (isNodeSubset(node1, list2.get(supersetIndex)) == false) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected final IllegalChoicesProblem getIllegalChoicesProblemForIdentifier(
            String parameterName, Map<?, ?> node, Exception exception, int index) {
        return new IllegalChoicesProblem(parameterName, "[" + index + "]",
                CHOICE_IDENTIFIER, node.get(CHOICE_NAME), "must be string");
    }

    @Override
    protected final String getIdentifierOfNode(Object node) {
        if (node instanceof String) {
            return (String) node;
        } else {
            Map<?, ?> map = (Map<?, ?>) node;
            if (map.containsKey(CHOICE_IDENTIFIER)) {
                return (String) map.get(CHOICE_IDENTIFIER);
            }
            return (String) map.get(CHOICE_NAME);
        }
    }
}
