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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for megawidget specifiers that include choices as part of their
 * state. Said choices are always associated with a single state identifier, so
 * the megawidget identifiers for these specifiers must not consist of
 * colon-separated substrings.
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
 * @see ChoicesMegawidget
 */
public abstract class ChoicesMegawidgetSpecifier extends
        StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Possible megawidget state values parameter name; a megawidget must
     * include an array of one or more choices associated with this name. Each
     * such choice may be either a string, meaning that the string value is used
     * as both the choice's description and its identifier, or else a
     * <code>Map</code> holding an entry for <code>CHOICE_NAME</code> and
     * optionally, an entry for <code>CHOICE_IDENTIFIER</code>. Regardless, each
     * string must occur at most once as a choice name and once as a choice
     * identifier. Note that subclasses may have other items within the <code>
     * Map</code>.
     */
    public static final String MEGAWIDGET_VALUE_CHOICES = "choices";

    /**
     * Choice name parameter name; each choice in the array of choices
     * associated with <code>MEGAWIDGET_VALUE_CHOICES</code> that is a map must
     * contain a reference to a string associated with this name. The string
     * serves to label the choice, and if there is no entry for <code>
     * CHOICE_IDENTIFIER</code> within the map, as its identifier as well. Each
     * name must be unique in the set of all choice names.
     */
    public static final String CHOICE_NAME = "displayString";

    /**
     * Choice identifier parameter name; each choice in the array of choices
     * associated with <code>MEGAWIDGET_VALUE_CHOICES</code> that is a map may
     * contain a reference to a string associated with this name. The string
     * serves as the identifier of the choice. If not provided, the <code>
     * CHOICE_NAME</code> is used as its identifier instead. Each identifier
     * must be unique in the set of all choice identifiers.
     */
    public static final String CHOICE_IDENTIFIER = "identifier";

    // Protected Classes

    /**
     * Encapsulation of the result of an evaluation of a choices list for
     * legality that fails. This allows for descriptions of problems that occur
     * with hierarchical choices lists, for any subclasses that require such
     * hierarchies in their choices.
     */
    protected final class IllegalChoicesProblem {

        // Private Variables

        /**
         * Description of the problem.
         */
        private final String problem;

        /**
         * Name of the choices list sub-parameter with an illegal value, if any.
         */
        private final String subParameterName;

        /**
         * Value of the choices list sub-parameter that was found to be illegal,
         * if any.
         */
        private final Object subParameterValue;

        /**
         * Description of the choices list element that is illegal, if any.
         */
        private String badElementLocation;

        /**
         * Depth in the hierarchy at which illegality was found. For flat
         * choices lists,
         */
        private int depth;

        // Public Constructors

        /**
         * Construct a standard instance with a depth of 1.
         * 
         * @param badElementLocation
         *            Bad element location, or <code>null</code> if there is no
         *            one readily identifiable bad element.
         * @param subParameterName
         *            Name of the choices list sub-parameter that was found to
         *            be illegal, or <code>null</code> if no particular
         *            problematic sub-parameter was found.
         * @param subParameterValue
         *            Value of the choices list sub-parameter that was found to
         *            be illegal, or <code>null</code> if no particular
         *            problematic sub-parameter was found.
         * @param problem
         *            Description of the problem.
         */
        public IllegalChoicesProblem(String badElementLocation,
                String subParameterName, Object subParameterValue,
                String problem) {
            this(badElementLocation, subParameterName, subParameterValue,
                    problem, 1);
        }

        /**
         * Construct a standard instance.
         * 
         * @param badElementLocation
         *            Bad element location, or <code>null</code> if there is no
         *            one readily identifiable bad element.
         * @param subParameterName
         *            Name of the choices list sub-parameter that was found to
         *            be illegal, or <code>null</code> if no particular
         *            problematic sub-parameter was found.
         * @param subParameterValue
         *            Value of the choices list sub-parameter that was found to
         *            be illegal, or <code>null</code> if no particular
         *            problematic sub-parameter was found.
         * @param problem
         *            Description of the problem.
         * @param depth
         *            Depth within the hierarchy at which the problem is found.
         */
        public IllegalChoicesProblem(String badElementLocation,
                String subParameterName, Object subParameterValue,
                String problem, int depth) {
            this.badElementLocation = badElementLocation;
            this.problem = problem;
            this.subParameterName = subParameterName;
            this.subParameterValue = subParameterValue;
            this.depth = depth;
        }

        // Public Methods

        /**
         * Get the description of the bad element location.
         * 
         * @return Description of the bad element location, or <code>null</code>
         *         if no single element was readily identifiable as a problem.
         */
        public String getBadElementLocation() {
            return badElementLocation;
        }

        /**
         * Get the name of the choices sub-parameter that was found to be
         * illegal.
         * 
         * @return Name of the choices sub-parameter that was found to be
         *         illegal, or <code>null</code> if no particular problematic
         *         sub-parameter was found.
         */
        public String getSubParameterName() {
            return subParameterName;
        }

        /**
         * Get the value of the choices sub-parameter that was found to be
         * illegal.
         * 
         * @return Value of the choices sub-parameter that was found to be
         *         illegal, or <code>null</code> if no particular problematic
         *         sub-parameter was found.
         */
        public Object getSubParameterValue() {
            return subParameterValue;
        }

        /**
         * Get the description of the problem.
         * 
         * @return Description of the problem, or <code>null</code> if there is
         *         no problem.
         */
        public String getProblem() {
            return problem;
        }

        /**
         * Get the depth within the hierarchy at which the problem was found.
         * 
         * @return Depth within the hierarchy at which the problem was found.
         */
        public int getDepth() {
            return depth;
        }

        /**
         * Prepend the parent node description to the existing description of
         * the bad element location, incrementing the depth count as well.
         * 
         * @param parentLocation
         *            Description of the parent node location to be added.
         */
        public void addParentToBadElementLocation(String parentLocation) {
            if (badElementLocation == null) {
                badElementLocation = parentLocation;
            } else {
                badElementLocation = parentLocation + badElementLocation;
            }
            depth++;
        }
    }

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
    public ChoicesMegawidgetSpecifier(Map<String, Object> parameters)
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
        IllegalChoicesProblem eval = evaluateChoicesLegality(choicesList);
        if (eval != null) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VALUE_CHOICES
                            + eval.getBadElementLocation(),
                    eval.getSubParameterValue(), (eval.getDepth() == 0 ? ""
                            : "parameter \"" + eval.getSubParameterName()
                                    + "\" ")
                            + eval.getProblem());
        }
        this.choicesList = Collections.unmodifiableList(choicesList);
    }

    // Public Methods

    /**
     * Get the list of choices associated with this specifier. The returned list
     * is identical in structure to that provided to the specifier as the <code>
     * MEGAWIDGET_VALUE_CHOICES</code> parameter.
     * 
     * @return Hierarchical list of choices; this list is not modifiable.
     */
    public final List<?> getChoices() {
        return choicesList;
    }

    // Protected Methods

    /**
     * Get a simple description of the data structure used to specify the
     * choices list given by the <code>MEGAWIDGET_VALUE_CHOICES</code>
     * parameter, such as "list" or "tree".
     * 
     * @return Simple description of the data structure.
     */
    protected abstract String getChoicesDataStructureDescription();

    /**
     * Check the specified choices element map to see if it is valid in a
     * subclass-specific manner. It is assumed that the name and/or identifier
     * of the map has been found to be valid when this method is called.
     * Implementations must determine whether or not any subclass-specific
     * details of the map are legal or not.
     * 
     * @param map
     *            Map to be checked for legality.
     * @param index
     *            Index of the choices element being checked.
     * @return Problem that was found if the choices element map was found to be
     *         illegal, or <code>null</code> if it is legal.
     */
    protected abstract IllegalChoicesProblem evaluateChoicesMapLegality(
            Map<?, ?> map, int index);

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
     *            This must be either a <code>String</code> giving an identifier
     *            or else a <code>Map</code>, with the latter holding the usual
     *            elements for a choice element map.
     * @param node2
     *            Node to be checked to see if it is a superset of the other
     *            node. This must be either a <code>String</code> giving an
     *            identifier or else a <code>Map</code>, with the latter holding
     *            the usual elements for a choice element map.
     * @return True if the first node is a subset of the second, false
     *         otherwise.
     */
    protected abstract boolean isNodeSubset(Object node1, Object node2);

    /**
     * Make a copy of the specified child list in a subclass-specific manner.
     * 
     * @param list
     *            Child list.
     * @return Copy of the child list.
     */
    protected abstract List<Object> createChoicesCopy(List<?> list);

    /**
     * Get a choices list from the specified object. The only guarantee
     * concerning the returned value is that it is a list of objects; its
     * validity is not checked.
     * 
     * @param choicesObj
     *            Object holding the choices list.
     * @return Choices list.
     * @throws MegawidgetException
     *             If the object is not a non-empty list.
     */
    protected final List<?> getChoicesFromObject(Object choicesObj)
            throws MegawidgetException {

        // Ensure that the possible values are present as an
        // array of choices.
        List<?> choicesList = null;
        try {
            choicesList = (List<?>) choicesObj;
        } catch (Exception e) {
            throw new MegawidgetException(getIdentifier(), getType(),
                    choicesObj, "must be "
                            + getChoicesDataStructureDescription()
                            + " of choices");
        }
        if ((choicesList == null) || choicesList.isEmpty()) {
            throw new MegawidgetException(getIdentifier(), getType(), null,
                    null);
        }
        return choicesList;
    }

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
    protected final boolean isSubset(List<?> list1, List<?> list2) {

        // If the subset is null, it is indeed a subset.
        if (list1 == null) {
            return true;
        }

        // For each node in the subset, find the equivalent
        // node in the other list, and ensure that the super-
        // set one has at least all the nodes of the subset
        // one.
        for (int j = 0; j < list1.size(); j++) {

            // Find the superset node equivalent to this
            // subset node; if not found, it is not a subset.
            Object node1 = list1.get(j);
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

    /**
     * Check the specified choices list to see if it is a valid structure.
     * 
     * @param list
     *            Choices list to be checked.
     * @return Problem that was found if the choices list was found to be
     *         illegal, or <code>null</code> if it is legal.
     */
    protected final IllegalChoicesProblem evaluateChoicesLegality(List<?> list) {

        // Iterate through the elements of this list, checking
        // each as a node for legality, and checking that all
        // elements have unique identifiers.
        Set<String> identifiers = new HashSet<String>();
        for (int j = 0; j < list.size(); j++) {

            // Get the node at this point.
            Object node = list.get(j);

            // If the node is just a string, it is legal; other-
            // wise, if it is a map, it must be checked further;
            // otherwise, it is not legal.
            String identifier = null;
            if (node instanceof String) {
                identifier = (String) node;
            } else if (node instanceof Map) {

                // If the map does not have a name entry, it is
                // illegal.
                Map<?, ?> map = (Map<?, ?>) node;
                Object name = map.get(CHOICE_NAME);
                if ((name == null) || ((name instanceof String) == false)) {
                    return new IllegalChoicesProblem("[" + j + "]",
                            CHOICE_NAME, name, "must be string");
                }

                // If this map does not have an identifier
                // entry, use the name entry instead.
                Object identifierObj = map.get(CHOICE_IDENTIFIER);
                if ((identifierObj != null)
                        && ((identifierObj instanceof String) == false)) {
                    return new IllegalChoicesProblem("[" + j + "]",
                            CHOICE_IDENTIFIER, identifierObj, "must be string");
                }
                identifier = (String) (identifierObj == null ? name
                        : identifierObj);

                // Evaluate the legality of the node in a
                // subclass-specific manner.
                IllegalChoicesProblem result = evaluateChoicesMapLegality(map,
                        j);
                if (result != null) {
                    return result;
                }
            } else {
                return new IllegalChoicesProblem("[" + j + "]", CHOICE_NAME,
                        node, "must be string");
            }

            // Ensure that this identifier is unique among
            // its siblings.
            if (identifiers.contains(identifier)) {
                return new IllegalChoicesProblem("[" + j + "]", null, list,
                        "has duplicate sibling identifier", 0);
            }
            identifiers.add(identifier);
        }

        // Having passed all the tests, the tree is legal.
        return null;
    }

    /**
     * Get the name of the specified choices list element.
     * 
     * @param node
     *            Choices list element; must be of type <code>String</code> or
     *            of type <code>Map</code>; if the latter, it must have a
     *            <code>String</code> as a value paired with the key <code>
     *            CHOICE_NAME</code> or the key <code>CHOICE_IDENTIFIER</code>.
     * @return Identifier of the state hierarchy node.
     */
    protected final String getNameOfNode(Object node) {
        if (node instanceof String) {
            return (String) node;
        } else {
            return (String) ((Map<?, ?>) node).get(CHOICE_NAME);
        }
    }

    /**
     * Get the identifier of the specified choices list element.
     * 
     * @param node
     *            Choices list element; must be of type <code>String</code> or
     *            of type <code>Map</code>; if the latter, it must have a
     *            <code>String</code> as a value paired with the key <code>
     *            CHOICE_NAME</code> or the key <code>CHOICE_IDENTIFIER</code>.
     * @return Identifier of the state hierarchy node.
     */
    protected final String getIdentifierOfNode(Object node) {
        if (node instanceof String) {
            return (String) node;
        } else {
            Map<?, ?> map = (Map<?, ?>) node;
            if (map.containsKey(CHOICE_IDENTIFIER)) {
                return (String) map.get(CHOICE_IDENTIFIER);
            } else {
                return (String) map.get(CHOICE_NAME);
            }
        }
    }
}
