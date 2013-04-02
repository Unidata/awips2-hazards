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
 * Hierarchical choices megawidget specifier, used to allow the selection of
 * multiple values in a hierarchy of choices. The choice hierarchy, as well as
 * the hierarchy of the values chosen, are in tree form, with each value being
 * either a leaf (having no children) or a branch (having one or more child
 * choices). The hierarchy may be arbitrarily deep.
 * <p>
 * The hierarchy of values chosen is always a subset of the choices hierarchy.
 * For any hierarchy of choices, the choice names that share the same direct
 * parent must be unique with respect to one another; this is also true for
 * names of the root choices with respect to one another. Thus, a choice name
 * may be identical to that of another choice at a different level of the
 * hierarchy, or at the same level but having a different parent.
 * <p>
 * The choices are always associated with a single state identifier, so the
 * megawidget identifiers for these specifiers must not consist of
 * colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2013            Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalChoicesMegawidget
 */
public class HierarchicalChoicesMegawidgetSpecifier extends
        StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Possible megawidget state values parameter name; a megawidget must
     * include an array of one or more choices associated with this name. Each
     * such choice may be either a string, meaning that the string value is used
     * as the choice's name and it is a leaf (has no children), or else a
     * <code>Map</code> holding an entry for <code>CHOICE_NAME</code>;
     * optionally an entry for <code>CHOICE_IDENTIFIER</code>; and, if it is a
     * branch, an entry for <code>CHOICE_CHILDREN</code>.
     */
    public static final String MEGAWIDGET_VALUES = "choices";

    /**
     * Choice name parameter name; each choice in the tree associated with
     * <code>MEGAWIDGET_VALUES</code> that is a map must contain a reference to
     * a string associated with this name. The string serves to label the
     * choice, and if there is no entry for <code>CHOICE_IDENTIFIER</code>
     * within the map, as its identifier as well.
     */
    public static final String CHOICE_NAME = "displayString";

    /**
     * Choice identifier parameter name; each choice in the tree associated with
     * <code>MEGAWIDGET_VALUES</code> that is a map may contain a reference to a
     * string associated with this name. The string serves as the identifier of
     * the choice. If not provided, the <code>CHOICE_NAME</code> is used as its
     * identifier instead.
     */
    public static final String CHOICE_IDENTIFIER = "identifier";

    /**
     * Choice children parameter name; each choice in the tree associated with
     * <code>MEGAWIDGET_VALUES</code> that is a map may contain a reference to a
     * list of other choices associated with this name. These choices are the
     * children of that choice.
     */
    public static final String CHOICE_CHILDREN = "children";

    // Package Classes

    /**
     * Encapsulation of the result of an evaluation of a state hierarchy tree
     * for legality that fails.
     */
    class IllegalTreeProblem {

        // Private Variables

        /**
         * Description of the problem.
         */
        private final String problem;

        /**
         * Name of the tree sub-parameter with an illegal value, if any.
         */
        private final String subParameterName;

        /**
         * Value of the tree sub-parameter that was found to be illegal, if any.
         */
        private final Object subParameterValue;

        /**
         * Description of which tree element that is illegal, if any.
         */
        private String badElementLocation;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param badElementLocation
         *            Bad element location, or <code>null</code> if there is no
         *            one readily identifiable bad element.
         * @param subParameterName
         *            Name of the tree sub-parameter that was found to be
         *            illegal, or <code>null</code> if no particular problematic
         *            sub-parameter was found.
         * @param subParameterValue
         *            Value of the tree sub-parameter that was found to be
         *            illegal, or <code>null</code> if no particular problematic
         *            sub-parameter was found.
         * @param problem
         *            Description of the problem.
         */
        public IllegalTreeProblem(String badElementLocation,
                String subParameterName, Object subParameterValue,
                String problem) {
            this.badElementLocation = badElementLocation;
            this.problem = problem;
            this.subParameterName = subParameterName;
            this.subParameterValue = subParameterValue;
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
         * Get the name of the tree sub-parameter that was found to be illegal.
         * 
         * @return Name of the tree sub-parameter that was found to be illegal,
         *         or <code>null</code> if no particular problematic
         *         sub-parameter was found.
         */
        public String getSubParameterName() {
            return subParameterName;
        }

        /**
         * Get the value of the tree sub-parameter that was found to be illegal.
         * 
         * @return Value of the tree sub-parameter that was found to be illegal,
         *         or <code>null</code> if no particular problematic
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
         * Prepend the parent node description to the existing description of
         * the bad element location.
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
        }
    }

    // Package Variables

    /**
     * Choices structure.
     */
    final List<?> choicesList;

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
    public HierarchicalChoicesMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the possible values are present as an
        // array of choices.
        List<?> choicesList = null;
        try {
            choicesList = (List<?>) parameters.get(MEGAWIDGET_VALUES);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VALUES,
                    parameters.get(MEGAWIDGET_VALUES),
                    "must be tree of choices");
        }
        if ((choicesList == null) || choicesList.isEmpty()) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VALUES, null, null);
        }

        // Evaluate the legality of the state hierarchy.
        IllegalTreeProblem eval = evaluateLegality(choicesList);
        if (eval != null) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(),
                    MEGAWIDGET_VALUES + eval.getBadElementLocation(),
                    eval.getSubParameterValue(), "parameter \""
                            + eval.getSubParameterName() + "\" "
                            + eval.getProblem());
        }
        this.choicesList = Collections.unmodifiableList(choicesList);
    }

    // Public Methods

    /**
     * Get the hierarchical list of choices associated with this specifier. The
     * returned list is identical in structure to that provided to the specifier
     * as the <code>MEGAWIDGET_VALUES</code> parameter.
     * 
     * @return Hierarchical list of choices; this must be treated as read-only
     *         by the caller.
     */
    public final List<?> getChoices() {
        return choicesList;
    }

    // Package Methods

    /**
     * Determine whether the first state hierarchy specified is a subset of the
     * second.
     * 
     * @param tree1
     *            First state hierarchy to be checked.
     * @param tree2
     *            Second state hierarchy to be checked.
     * @return True if the first state hierarchy is a subset of the second,
     *         false otherwise.
     */
    boolean isSubset(List<?> tree1, List<?> tree2) {

        // If the subset is null, it is indeed a subset.
        if (tree1 == null) {
            return true;
        }

        // For each node in the subset, find the equivalent
        // node in the other tree, and ensure that the super-
        // set one has at least all the nodes of the subset
        // one.
        for (int j = 0; j < tree1.size(); j++) {

            // Find the superset node equivalent to this
            // subset node; if not found, it is not a subset.
            Object node1 = tree1.get(j);
            String identifier = getIdentifierOfNode(node1);
            int supersetIndex;
            for (supersetIndex = 0; supersetIndex < tree2.size(); supersetIndex++) {
                if (identifier.equals(getIdentifierOfNode(tree2
                        .get(supersetIndex)))) {
                    break;
                }
            }
            if (supersetIndex == tree2.size()) {
                return false;
            }

            // If the subset node has children and the super-
            // set does not, the former is not a subset. If
            // both have children, then check their respec-
            // tive lists of children to ensure that the one
            // is a subset of the other.
            Object node2 = tree2.get(supersetIndex);
            if (node1 instanceof Map) {
                if (node2 instanceof String) {
                    return false;
                }
                if (isSubset(
                        (List<?>) ((Map<?, ?>) node1).get(CHOICE_CHILDREN),
                        (List<?>) ((Map<?, ?>) node2).get(CHOICE_CHILDREN)) == false) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check the state hierarchy to see if it is a valid structure.
     * 
     * @param tree
     *            State hierarchy to be checked.
     * @return Problem that was found if the state hierarchy was found to be
     *         illegal, or <code>null</code> if it is legal.
     */
    IllegalTreeProblem evaluateLegality(List<?> tree) {

        // Iterate through the elements of this list,
        // checking each as a node for legality, and
        // checking that all elements have unique iden-
        // tifiers.
        Set<String> identifiers = new HashSet<String>();
        for (int j = 0; j < tree.size(); j++) {

            // Get the node at this point.
            Object node = tree.get(j);

            // If the node is just a string, it is legal;
            // otherwise, if it is a map, it must be
            // checked further; otherwise, it is not
            // legal.
            String identifier = null;
            if (node instanceof String) {
                identifier = (String) node;
            } else if (node instanceof Map) {

                // If the map does not have a name entry,
                // it is illegal.
                Map<?, ?> map = (Map<?, ?>) node;
                Object name = map.get(CHOICE_NAME);
                if ((name == null) || ((name instanceof String) == false)) {
                    return new IllegalTreeProblem("[" + j + "]", CHOICE_NAME,
                            name, "must be string");
                }

                // If this map does not have an identifier
                // entry, use the name entry instead.
                Object identifierObj = map.get(CHOICE_IDENTIFIER);
                if ((identifierObj != null)
                        && ((identifierObj instanceof String) == false)) {
                    return new IllegalTreeProblem("[" + j + "]",
                            CHOICE_IDENTIFIER, identifierObj, "must be string");
                }
                identifier = (String) (identifierObj == null ? name
                        : identifierObj);

                // If the map has something other than a
                // list for a children entry, it is
                // illegal.
                Object children = map.get(CHOICE_CHILDREN);
                if ((children != null) && ((children instanceof List) == false)) {
                    return new IllegalTreeProblem("[" + j + "]",
                            CHOICE_CHILDREN, children,
                            "must be list of children");
                }

                // Check the children lists of the tree for
                // legality, and to ensure that siblings
                // always have unique identifiers.
                if (children != null) {
                    IllegalTreeProblem eval = evaluateLegality((List<?>) children);
                    if (eval != null) {
                        eval.addParentToBadElementLocation("[" + j + "]");
                        return eval;
                    }
                }
            } else {
                return new IllegalTreeProblem("[" + j + "]", CHOICE_NAME, node,
                        "must be string");
            }

            // Ensure that this identifier is unique among
            // its siblings.
            if (identifiers.contains(identifier)) {
                return new IllegalTreeProblem("[" + j + "]", CHOICE_CHILDREN,
                        tree, "has duplicate sibling identifier");
            }
            identifiers.add(identifier);
        }

        // Having passed all the tests, the tree is legal.
        return null;
    }

    /**
     * Get the identifier of the specified state hierarchy node.
     * 
     * @param node
     *            State hierarchy node; must be of type <code>String</code> or
     *            of type <code>Map</code>; if the latter, it must have a
     *            <code>String</code> as a value paired with the key <code>
     *            CHOICE_NAME</code> or the key <code>CHOICE_IDENTIFIER</code>.
     * @return Identifier of the state hierarchy node.
     */
    String getIdentifierOfNode(Object node) {
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
