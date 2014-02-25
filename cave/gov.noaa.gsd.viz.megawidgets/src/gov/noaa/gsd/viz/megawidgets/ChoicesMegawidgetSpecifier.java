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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for megawidget specifiers that include sets of choices (whether
 * closed or open) as part of their state. Said choices are always associated
 * with a single state identifier, so the megawidget identifiers for these
 * specifiers must not consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Sep 25, 2013   2168     Chris.Golden      Added support for subclasses that
 *                                           include detail child megawidgets as
 *                                           part of their choices.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Jan 28, 2014   2161     Chris.Golden      Changed to support use of collections
 *                                           instead of only lists for the state.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class ChoicesMegawidgetSpecifier extends
        StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Choice name parameter name; each choice in an array of choices that is a
     * map must contain a reference to a string associated with this name. The
     * string serves to label and to uniquely identify the choice; thus, each
     * name must be unique in the set of all choice names.
     */
    public static final String CHOICE_NAME = "displayString";

    // Protected Static Classes

    /**
     * Encapsulation of the result of an evaluation of a choices list for
     * legality that fails. This allows for descriptions of problems that occur
     * with hierarchical choices lists, for any subclasses that require such
     * hierarchies in their choices.
     */
    protected static final class IllegalChoicesProblem {

        // Private Variables

        /**
         * Description of the problem.
         */
        private final String problem;

        /**
         * Name of the parameter that has the illegal choices list.
         */
        private final String parameterName;

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
         * choices lists, this will always be 1.
         */
        private int depth;

        /**
         * Throwable that caused the problem, if any.
         */
        private Throwable cause;

        // Public Constructors

        /**
         * Construct a standard instance with a depth of 1 and no nested cause.
         * 
         * @param parameterName
         *            Name of the parameter with which the bad choices list is
         *            associated.
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
        public IllegalChoicesProblem(String parameterName,
                String badElementLocation, String subParameterName,
                Object subParameterValue, String problem) {
            this(parameterName, badElementLocation, subParameterName,
                    subParameterValue, problem, 1);
        }

        /**
         * Construct a standard instance with no nested cause.
         * 
         * @param parameterName
         *            Name of the parameter with which the bad choices list is
         *            associated.
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
        public IllegalChoicesProblem(String parameterName,
                String badElementLocation, String subParameterName,
                Object subParameterValue, String problem, int depth) {
            this.parameterName = parameterName;
            this.problem = problem;
            this.subParameterName = subParameterName;
            this.subParameterValue = subParameterValue;
        }

        /**
         * Construct a standard instance with a depth of 1.
         * 
         * @param parameterName
         *            Name of the parameter with which the bad choices list is
         *            associated.
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
         * @param cause
         *            Throwable that triggered this problem.
         */
        public IllegalChoicesProblem(String parameterName,
                String badElementLocation, String subParameterName,
                Object subParameterValue, String problem, Throwable cause) {
            this.parameterName = parameterName;
            this.badElementLocation = badElementLocation;
            this.problem = problem;
            this.subParameterName = subParameterName;
            this.subParameterValue = subParameterValue;
            this.cause = cause;
        }

        // Private Constructors

        /**
         * Construct an instance indicating that there is no problem.
         */
        private IllegalChoicesProblem() {
            this.parameterName = null;
            this.badElementLocation = null;
            this.problem = null;
            this.subParameterName = null;
            this.subParameterValue = null;
            this.cause = null;
        }

        // Public Methods

        /**
         * Get the name of the parameter with which the bad choices list is
         * associated.
         * 
         * @return Name of the parameter with which the bad choices list is
         *         associated.
         */
        public String getParameterName() {
            return parameterName;
        }

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
         * Get the nested cause, if any.
         * 
         * @return Nested cause, or <code>null</code> if there is no nested
         *         cause.
         */
        public Throwable getCause() {
            return cause;
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

        /**
         * Generate a specification exception based upon this problem.
         * 
         * @param specifier
         *            Specifier that is generating this exception.
         * @return Specification exception.
         */
        public MegawidgetSpecificationException toSpecificationException(
                ISpecifier specifier) {
            return new MegawidgetSpecificationException(
                    specifier.getIdentifier(), specifier.getType(),
                    parameterName + getBadElementLocation(),
                    getSubParameterValue(), (getDepth() == 0 ? ""
                            : "parameter \"" + getSubParameterName() + "\" ")
                            + getProblem(), getCause());
        }
    }

    // Protected Constants

    /**
     * Implementation of an illegal choices problem that indicates there is
     * actually no problem (as per the null object pattern).
     */
    protected static final IllegalChoicesProblem NO_ILLEGAL_CHOICES_PROBLEM = new IllegalChoicesProblem();

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
    }

    // Protected Methods

    /**
     * Get a simple description of the data structure used to specify the
     * choices list, such as "list" or "tree".
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
     * @param parameterName
     *            Name of the parameter with which the choices list containing
     *            the map is associated.
     * @param map
     *            Map to be checked for legality.
     * @param index
     *            Index of the choices element being checked.
     * @return Problem that was found if the choices element map was found to be
     *         illegal, or {@link #NO_ILLEGAL_CHOICES_PROBLEM} if it is legal.
     */
    protected abstract IllegalChoicesProblem evaluateChoicesMapLegality(
            String parameterName, Map<?, ?> map, int index);

    /**
     * Make a copy of the specified child list in a subclass-specific manner.
     * 
     * @param list
     *            Child list.
     * @return Copy of the child list.
     */
    protected abstract List<Object> createChoicesCopy(Collection<?> list);

    /**
     * Get the problem that occurred when attempting to fetch the unique
     * identifier from the specified choice node.
     * 
     * @param parameterName
     *            Name of the parameter with which the bad choices list is
     *            associated.
     * @param node
     *            Illegal node map.
     * @param exception
     *            Exception that occurred.
     * @param index
     *            Index of the node within the choices list.
     */
    protected abstract IllegalChoicesProblem getIllegalChoicesProblemForIdentifier(
            String parameterName, Map<?, ?> node, Exception exception, int index);

    /**
     * Get the identifier of the specified choices list element.
     * 
     * @param node
     *            Choices list element; must be of type {@link String} or of
     *            type {@link Map}; if the latter, it must have a {@link String}
     *            as a value paired with the key {@link #CHOICE_NAME}.
     *            Subclasses may fetch the identifier from another key-value
     *            pairing within a the map.
     * @return Identifier of the state hierarchy node, or <code>null</code> if
     *         no identifier is found.
     * @exception ClassCastException
     *                If the node is neither a {@link String} nor a {@link Map},
     *                or if it is a {@link Map} but the value associated with
     *                {@link #CHOICE_NAME} or a subclass-specific key is not a
     *                {@link String}.
     */
    protected abstract String getIdentifierOfNode(Object node);

    /**
     * Get the name of the specified choices list element.
     * 
     * @param node
     *            Choices list element; must be of type {@link String} or of
     *            type {@link Map}; if the latter, it must have a {@link String}
     *            as a value paired with the key {@link #CHOICE_NAME}.
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
     * Check the specified choices list to see if it is a valid structure.
     * 
     * @param parameterName
     *            Name of the parameter with which this choices list is
     *            associated.
     * @param collection
     *            Choices collection to be checked.
     * @return Problem that was found if the choices list was found to be
     *         illegal, or {@link #NO_ILLEGAL_CHOICES_PROBLEM} if it is legal.
     */
    protected final IllegalChoicesProblem evaluateChoicesLegality(
            String parameterName, Collection<?> collection) {

        // Iterate through the elements of this list, checking
        // each as a node for legality, and checking that all
        // elements have unique identifiers.
        Set<String> identifiers = new HashSet<>();
        Iterator<?> iterator = collection.iterator();
        for (int j = 0; j < collection.size(); j++) {

            // Get the node at this point.
            Object node = iterator.next();

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
                    return new IllegalChoicesProblem(parameterName, "[" + j
                            + "]", CHOICE_NAME, name, "must be string");
                }

                // Attempt to extract the identifier in a sub-
                // class-specific manner; if this fails, the
                // node is illegal.
                try {
                    identifier = getIdentifierOfNode(node);
                } catch (Exception e) {
                    return getIllegalChoicesProblemForIdentifier(parameterName,
                            (Map<?, ?>) node, e, j);
                }

                // Evaluate the legality of the node in a
                // subclass-specific manner.
                IllegalChoicesProblem result = evaluateChoicesMapLegality(
                        parameterName, map, j);
                if (result != NO_ILLEGAL_CHOICES_PROBLEM) {
                    return result;
                }
            } else {
                return new IllegalChoicesProblem(parameterName, "[" + j + "]",
                        CHOICE_NAME, node, "must be string");
            }

            // Ensure that this identifier is unique among
            // its siblings.
            if (identifiers.contains(identifier)) {
                return new IllegalChoicesProblem(parameterName, "[" + j + "]",
                        null, collection, "has duplicate sibling identifier", 0);
            }
            identifiers.add(identifier);
        }

        // Having passed all the tests, the tree is legal.
        return NO_ILLEGAL_CHOICES_PROBLEM;
    }
}
