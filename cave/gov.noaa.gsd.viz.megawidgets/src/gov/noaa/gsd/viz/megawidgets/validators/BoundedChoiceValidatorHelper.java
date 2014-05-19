/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.validators;

import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Validator helper used to ensure that potential choices for
 * instances of {@link BoundedChoiceValidator} are valid. The generic parameter
 * <code>T</code> is the type of the selected choice(s) for the validator using
 * this helper.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class BoundedChoiceValidatorHelper<T> {

    // Protected Static Classes

    /**
     * Encapsulation of an invalid choices problem that is found during the
     * evaluation of a choices list for legality.
     */
    @SuppressWarnings("serial")
    protected static class InvalidChoicesException extends Exception {

        // Private Variables

        /**
         * Description of the choices list element that is illegal, if any.
         */
        private String badElementLocation;

        /**
         * Name of the choices list sub-parameter with an illegal value, if any.
         */
        private final String subParameterName;

        /**
         * Value of the choices list sub-parameter that was found to be illegal,
         * if any.
         */
        private final Object subParameterValue;

        // Protected Constructors

        /**
         * Construct a standard instance with no nested cause.
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
         * @param message
         *            Description of the problem.
         */
        protected InvalidChoicesException(String badElementLocation,
                String subParameterName, Object subParameterValue,
                String message) {
            this(badElementLocation, subParameterName, subParameterValue,
                    message, null);
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
         * @param message
         *            Description of the problem.
         * @param cause
         *            Throwable that triggered this problem.
         */
        protected InvalidChoicesException(String badElementLocation,
                String subParameterName, Object subParameterValue,
                String message, Throwable cause) {
            super(message, cause);
            this.badElementLocation = badElementLocation;
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
        public final String getBadElementLocation() {
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
        public final String getSubParameterName() {
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
        public final Object getSubParameterValue() {
            return subParameterValue;
        }

        /**
         * Generate a specification exception based upon this problem.
         * 
         * @param identifier
         *            Megawidget identifier.
         * @param type
         *            Megawidget type.
         * @param parameterName
         *            Name of the parameter that has a problematic choices
         *            collection.
         * @return Specification exception.
         */
        public MegawidgetSpecificationException toSpecificationException(
                String identifier, String type, String parameterName) {
            return new MegawidgetSpecificationException(identifier, type,
                    parameterName
                            + (getBadElementLocation() == null ? ""
                                    : getBadElementLocation()),
                    getSubParameterValue(), (subParameterName == null ? ""
                            : "parameter \"" + getSubParameterName() + "\" ")
                            + getMessage(), getCause());
        }

        /**
         * Generate a property exception based upon this problem.
         * 
         * @param identifier
         *            Megawidget identifier.
         * @param type
         *            Megawidget type.
         * @param parameterName
         *            Name of the parameter that has a problematic choices
         *            collection.
         * @return Property exception.
         */
        public MegawidgetPropertyException toPropertyException(
                String identifier, String type, String parameterName) {
            return new MegawidgetPropertyException(identifier, parameterName
                    + (getBadElementLocation() == null ? ""
                            : getBadElementLocation()), type,
                    getSubParameterValue(), (subParameterName == null ? ""
                            : "parameter \"" + getSubParameterName() + "\" ")
                            + getMessage(), getCause());
        }

        /**
         * Generate a state exception based upon this problem.
         * 
         * @param identifier
         *            Megawidget identifier.
         * @param type
         *            Megawidget type.
         * @return Property exception.
         */
        public MegawidgetStateException toStateException(String identifier,
                String type) {
            return new MegawidgetStateException(identifier
                    + (getBadElementLocation() == null ? ""
                            : getBadElementLocation()), type,
                    getSubParameterValue(), (subParameterName == null ? ""
                            : "parameter \"" + getSubParameterName() + "\" ")
                            + getMessage(), getCause());
        }

        // Package Methods

        /**
         * Prepend the specified parent location description to the existing
         * description of the bad element location.
         * 
         * @param parentLocation
         *            Description of the parent node location to be prepended.
         */
        void prependParentToBadElementLocation(String parentLocation) {
            badElementLocation = parentLocation
                    + (badElementLocation == null ? "" : badElementLocation);
        }
    }

    // Private Variables

    /**
     * Type of the megawidget.
     */
    private String type;

    /**
     * Identifier of the megawidget.
     */
    private String identifier;

    /**
     * Choices key within the specifier parameters or mutable properties.
     */
    private final String choicesKey;

    /**
     * Choice element name key.
     */
    private final String elementNameKey;

    /**
     * Choice element identifier key.
     */
    private final String elementIdentifierKey;

    // Public Constructors

    /**
     * Construct a standard instance for a {@link BoundedChoiceValidator}.
     * 
     * @param choicesKey
     *            Key within the specifier parameters or mutable properties for
     *            the choices list.
     * @param elementNameKey
     *            Key within a choice element for the element name parameter.
     * @param elementIdentifierKey
     *            Key within a choice element for the element identifier
     *            parameter.
     */
    public BoundedChoiceValidatorHelper(String choicesKey,
            String elementNameKey, String elementIdentifierKey) {
        this.choicesKey = choicesKey;
        this.elementNameKey = elementNameKey;
        this.elementIdentifierKey = elementIdentifierKey;
    }

    // Public Methods

    /**
     * Initialize the validator.
     * 
     * @param type
     *            Type of the megawidget.
     * @param identifier
     *            State identifier of the megawidget.
     */
    public final void initialize(String type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    /**
     * Get the type of the megawidget.
     * 
     * @return Type of the megawidget.
     */
    public final String getType() {
        return type;
    }

    /**
     * Get the identifier of the megawidget.
     * 
     * @return Identifier of the megawidget.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Get the key for the choices in the specifier parameters or mutable
     * properties.
     * 
     * @return Key for the choices.
     */
    public final String getChoicesKey() {
        return choicesKey;
    }

    /**
     * Get the key for the choice element name.
     * 
     * @return Key for the choice element name.
     */
    public final String getElementNameKey() {
        return elementNameKey;
    }

    /**
     * Get the key for the choice element identifier.
     * 
     * @return Key for the choice element identifier.
     */
    public final String getElementIdentifierKey() {
        return elementIdentifierKey;
    }

    /**
     * Get the name of the specified choices collection element.
     * 
     * @param node
     *            Choices collection element; must be of type {@link String} or
     *            of type {@link Map}; if the latter, it must have a
     *            {@link String} as a value paired with the key
     *            <code>elementNameKey</code> specified at
     *            {@linkplain #BoundedChoiceValidatorHelper(String, String, String)
     *            creation time}.
     * @return Identifier of the choices collection element.
     */
    public final String getNameOfNode(Object node) {
        if (node instanceof String) {
            return (String) node;
        } else {
            return (String) ((Map<?, ?>) node).get(elementNameKey);
        }
    }

    /**
     * Get the identifier of the specified choices list element.
     * 
     * @param node
     *            Choices collection element; must be of type {@link String} or
     *            of type {@link Map}; if the latter, this implementation
     *            requires that it have a {@link String} as a value paired with
     *            the key <code>elementNameKey</code> specified at
     *            {@linkplain #BoundedChoiceValidatorHelper(String, String, String)
     *            creation time}.
     * @return Identifier of the choices collection element.
     * @throws IllegalArgumentException
     *             If the node is neither a {@link String} nor a {@link Map}, or
     *             if it is a {@link Map} but the value associated with the
     *             appropriate key is not a {@link String}.
     */
    public final String getIdentifierOfNode(Object node) {
        try {
            if (node instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) node;
                String identifier = (String) map.get(elementIdentifierKey);
                if (identifier != null) {
                    return identifier;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(elementIdentifierKey);
        }
        return getNameOfNode(node);
    }

    /**
     * Convert the specified object into a valid list of available choices for a
     * specifier.
     * 
     * @param choices
     *            Object holding the list of available choices.
     * @return List of available choices.
     * @throws MegawidgetSpecificationException
     *             If the object is not a valid list of available choices.
     */
    public final List<?> convertToAvailableForSpecifier(Object choices)
            throws MegawidgetSpecificationException {
        try {
            return convertToAvailable(choices);
        } catch (InvalidChoicesException e) {
            throw e.toSpecificationException(identifier, type, choicesKey);
        }
    }

    /**
     * Convert the specified object into a valid list of available choices for a
     * mutable property.
     * 
     * @param choices
     *            Object holding the list of available choices.
     * @return List of available choices.
     * @throws MegawidgetPropertyException
     *             If the object is not a valid list of available choices.
     */
    public final List<?> convertToAvailableForProperty(Object choices)
            throws MegawidgetPropertyException {
        try {
            return convertToAvailable(choices);
        } catch (InvalidChoicesException e) {
            throw e.toPropertyException(identifier, type, choicesKey);
        }
    }

    /**
     * Convert the specified object into a valid choice(s) subset.
     * 
     * @param available
     *            List of available choices; <code>selected</code> must be a
     *            subset of this.
     * @param subset
     *            Object holding the subset.
     * @return Subset of choices.
     * @throws MegawidgetException
     *             If the selected object is not a valid selected choice(s)
     *             subset.
     */
    public final T convertToSubset(List<?> available, Object subset)
            throws MegawidgetException {
        if (subset == null) {
            return createDefaultSubset(available);
        }
        try {
            T newSubset = convertToSubsetStructure(subset);
            if (isSubset(newSubset, available) == false) {
                throw new InvalidChoicesException(null, null, subset,
                        "not a subset of available choices");
            }
            return newSubset;
        } catch (InvalidChoicesException e) {
            throw e.toStateException(getIdentifier(), getType());
        }
    }

    /**
     * Convert the specified available choices list to an unmodifiable version.
     * All sub-collections are rendered unmodifiable as well.
     * 
     * @param choices
     *            Valid available choices list.
     * @return Unmodifiable version of the list.
     */
    @SuppressWarnings("unchecked")
    public final List<?> convertToUnmodifiable(List<?> choices) {
        List<Object> list = new ArrayList<>(choices);
        boolean contentMutable = false;
        for (int j = 0; j < list.size(); j++) {
            if (list.get(j) instanceof Map) {
                contentMutable = true;
                list.set(
                        j,
                        Collections
                                .unmodifiableMap(deepCopyIfContentMutable((Map<String, ?>) list
                                        .get(j))));
            }
        }
        return Collections.unmodifiableList(contentMutable ? list : choices);
    }

    // Protected Methods

    /**
     * Copy the specified map if any of its values are mutable. This
     * implementation simply returns the specified map; subclasses must override
     * this method to copy the map if it may contain mutable content.
     * 
     * @param map
     *            Map to be copied.
     * @return Copied map, or <code>map</code> if none of its values are
     *         mutable.
     */
    protected Map<String, ?> deepCopyIfContentMutable(Map<String, ?> map) {
        return map;
    }

    /**
     * Check the specified choices collection to see if it is a valid structure.
     * 
     * @param collection
     *            Choices collection to be checked.
     * @param nestedLists
     *            Flag indicating whether or not nested collections, if any,
     *            must be lists instead of just collections.
     * @throws InvalidChoicesException
     *             If the collection of choices is invalid.
     */
    protected final void validateChoices(Collection<?> collection,
            boolean nestedLists) throws InvalidChoicesException {

        /*
         * Iterate through the elements of this collection, checking each as a
         * node for legality, and checking that all elements have unique
         * identifiers.
         */
        Set<String> identifiers = new HashSet<>();
        Iterator<?> iterator = collection.iterator();
        for (int j = 0; j < collection.size(); j++) {

            /*
             * If the node is just a string, it is legal; otherwise, if it is a
             * map, it must be checked further; otherwise, it is not legal.
             */
            Object node = iterator.next();
            String identifier = null;
            if (node instanceof String) {
                identifier = (String) node;
            } else if (node instanceof Map) {

                /*
                 * If the map does not have a name entry, it is illegal.
                 */
                Map<?, ?> map = (Map<?, ?>) node;
                Object name = map.get(elementNameKey);
                if ((name == null) || ((name instanceof String) == false)) {
                    throw new InvalidChoicesException("[" + j + "]",
                            elementNameKey, name, "must be string");
                }

                /*
                 * Attempt to extract the identifier in a subclass-specific
                 * manner; if this fails, the node is illegal.
                 */
                try {
                    identifier = getIdentifierOfNode(node);
                } catch (IllegalArgumentException e) {
                    throw new InvalidChoicesException("[" + j + "]",
                            e.getMessage(), node, "must be string", e);
                }

                /*
                 * Evaluate the legality of the node in a subclass-specific
                 * manner.
                 */
                validateChoicesMap(map, j, nestedLists);
            } else {
                throw new InvalidChoicesException("[" + j + "]",
                        elementNameKey, node, "must be string");
            }

            /*
             * Ensure that this identifier is unique among its siblings.
             */
            if (identifiers.contains(identifier)) {
                throw new InvalidChoicesException("[" + j + "]", null,
                        collection, "has duplicate sibling identifier");
            }
            identifiers.add(identifier);
        }
    }

    /**
     * Check the specified choices element map to see if it is valid; it may be
     * safely assumed that the name and/or identifier of the map has been found
     * to be valid when this method is called. This implementation assumes
     * validity; subclasses may override this method if they have additional
     * checks to perform.
     * 
     * @param map
     *            Choice element map to be checked for legality.
     * @param index
     *            Index of the choice element being checked.
     * @param nestedLists
     *            Flag indicating whether or not nested collections, if any,
     *            must be lists instead of just collections.
     * @throws InvalidChoicesException
     *             If the choice element map was found to be invalid.
     */
    protected void validateChoicesMap(Map<?, ?> map, int index,
            boolean nestedLists) throws InvalidChoicesException {

        /*
         * Nothing to be done; this base class has no additional validation to
         * be performed.
         */
    }

    /**
     * Create a default subset for the specified available choices.
     * 
     * @param available
     *            Available list of choices.
     * @return Default subset.
     */
    protected abstract T createDefaultSubset(List<?> available);

    /**
     * Convert the specified object to have the structure of a proper subset.
     * 
     * @param subset
     *            Object to be converted to the appropriate structure.
     * @return Properly structured subset.
     * @throws InvalidChoicesException
     *             If the object does not contain a valid subset.
     */
    protected abstract T convertToSubsetStructure(Object subset)
            throws InvalidChoicesException;

    /**
     * Determine whether the specified subset is a subset of the specified
     * available choices.
     * 
     * @param subset
     *            Subset be checked to see if it is a subset of
     *            <code>available</code>.
     * @param available
     *            Available list of choices.
     * @return True if the subset is indeed a subset of the available choices,
     *         false otherwise.
     */
    protected abstract boolean isSubset(T subset, List<?> available);

    // Private Methods

    /**
     * Convert the specified object into a valid list of available choices.
     * 
     * @param choices
     *            Object holding the list of available choices.
     * @return List of available choices.
     * @throws InvalidChoicesException
     *             If the object is not a valid list of available choices.
     */
    private List<?> convertToAvailable(Object choices)
            throws InvalidChoicesException {

        /*
         * Ensure that the object is a non-empty list.
         */
        if (choices instanceof List == false) {
            throw new InvalidChoicesException(identifier, type, choices,
                    "must be non-empty list of choices");
        }
        List<?> choicesList = (List<?>) choices;
        if (choicesList.isEmpty()) {
            throw new InvalidChoicesException(identifier, type, choices,
                    "must be non-empty list of choices");
        }

        /*
         * Perform validation of the elements of the list, and if successful,
         * return the list.
         */
        validateChoices(choicesList, true);
        return choicesList;
    }
}
