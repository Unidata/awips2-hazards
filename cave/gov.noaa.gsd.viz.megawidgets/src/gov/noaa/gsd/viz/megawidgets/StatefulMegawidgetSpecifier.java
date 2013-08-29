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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.ImmutableList;

/**
 * Stateful megawidget specifier base class, from which specific types of
 * stateful megawidget specifiers may be derived. A stateful megawidget
 * specifier allows the specification of megawidgets for later creation that
 * hold state, and may notify listeners when their state is changed.
 * <p>
 * Stateful megawidget specifiers associate state in key-value pairs; the keys
 * are implicitly defined when constructed by the identifier. The latter is
 * split by any colon (:) characters to yield one or more state identifiers.
 * When the state is asked for or updated, the state identifier (which again is
 * a substring of the megawidget identifier) must be supplied.
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
 * @see StatefulMegawidget
 * @see ExplicitCommitStatefulMegawidget
 */
public abstract class StatefulMegawidgetSpecifier extends
        NotifierMegawidgetSpecifier implements IStatefulSpecifier {

    // Protected Interfaces

    /**
     * Interface describing the methods required of a value converter, used to
     * convert an arbitrary object supplied as a parameter of a stateful
     * specifier to a value of the parameterized type.
     */
    protected interface IValueConverter<T> {

        /**
         * Get the value of the parameterized type from the specified object.
         * 
         * @param object
         *            Object from which to take the value.
         * @return Value of the parameterized type.
         * @throws IllegalArgumentException
         *             If the value cannot be converted.
         */
        public T getValueFromObject(Object object)
                throws IllegalArgumentException;
    }

    // Protected Static Constants

    /**
     * Integer value converter, used to convert objects supplied as parameters
     * to integers.
     */
    protected static final IValueConverter<Integer> POSITIVE_INTEGER_VALUE_CONVERTER = new IValueConverter<Integer>() {
        @Override
        public Integer getValueFromObject(Object object)
                throws IllegalArgumentException {
            int value;
            try {
                value = ((Number) object).intValue();
                if (value < 1) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("must be positive integer");
            }
            return value;
        }
    };

    // Private Variables

    /**
     * List of all state identifiers that are valid for this megawidget
     * specifier.
     */
    private final List<String> stateIdentifiers;

    /**
     * Map pairing state identifier keys with state label values.
     */
    private final Map<String, String> labelsForStateIdentifiers;

    /**
     * Map pairing state identifier keys with short state label values.
     */
    private final Map<String, String> shortLabelsForStateIdentifiers;

    /**
     * Map pairing state identifier keys with relative visual weight values. The
     * latter are used to specify their associated state's visual weight
     * relative to the that of each other state that is part of this or another
     * stateful megawidget in a grouping; it must be a positive integer if it is
     * specified. This is not used for the megawidget's own display, but may be
     * utilized as a hint as to the relative importance of the state in
     * comparison with other states of this or other specifiers if they have
     * their data tabulated in a table or some other form. If specified, the
     * relative importance of this state is calculated as this value divided by
     * the total of all such values for all the states of stateful megawidget
     * specifiers in the group. If not specified, it is assumed to be 1 for each
     * state.
     */
    private final Map<String, Integer> relativeWeightsForStateIdentifiers;

    /**
     * Map pairing state identifiers with starting values for each state, if
     * any.
     */
    private final Map<String, Object> valuesForStateIdentifiers;

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
    public StatefulMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Populate the state identifiers set in order to
        // determine which state identifiers are legal, and
        // ensure that no repeating or zero-length state
        // identifiers were supplied.
        List<String> stateIdentifiers = new ArrayList<String>();
        String[] identifiers = getIdentifier().split(":");
        for (String identifier : identifiers) {
            if ((identifier == null) || (identifier.length() == 0)) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), null, null,
                        "stateful megawidget specifier must have "
                                + "non-null colon-separated state identifiers");
            } else if (stateIdentifiers.contains(identifier)) {
                throw new MegawidgetSpecificationException(
                        getIdentifier(),
                        getType(),
                        null,
                        null,
                        "stateful megawidget specifier must have "
                                + "non-repeating colon-separated state identifiers");
            } else if (stateIdentifiers.size() >= getMaximumStateIdentifierCount()) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), null, null,
                        "state identifier count too high for this class (maximum "
                                + getMaximumStateIdentifierCount() + ")");
            }
            stateIdentifiers.add(identifier);
        }
        this.stateIdentifiers = ImmutableList.copyOf(stateIdentifiers);

        // Ensure that the state labels, if present, are
        // acceptable.
        labelsForStateIdentifiers = getStateMappedParametersFromObject(
                parameters, MEGAWIDGET_STATE_LABELS, "string", "", null, null);

        // Ensure that the short state labels, if present,
        // are acceptable.
        shortLabelsForStateIdentifiers = getStateMappedParametersFromObject(
                parameters, MEGAWIDGET_STATE_SHORT_LABELS, "string", "",
                labelsForStateIdentifiers, null);

        // Ensure that the relative state weights, if pre-
        // sent, are acceptable.
        relativeWeightsForStateIdentifiers = getStateMappedParametersFromObject(
                parameters, MEGAWIDGET_STATE_RELATIVE_WEIGHTS,
                "positive integer", 1, null, POSITIVE_INTEGER_VALUE_CONVERTER);

        // Ensure that the starting state values, if present, are
        // acceptable.
        valuesForStateIdentifiers = getStateMappedParametersFromObject(
                parameters, MEGAWIDGET_STATE_VALUES, "state value", null, null,
                null);
    }

    // Public Methods

    /**
     * Get the state identifiers valid for this specifier.
     * 
     * @return Set of valid state identifiers.
     */
    @Override
    public final List<String> getStateIdentifiers() {
        return stateIdentifiers;
    }

    /**
     * Get the state label for the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which the label is desired.
     * @return State label.
     */
    @Override
    public final String getStateLabel(String identifier) {
        return labelsForStateIdentifiers.get(identifier);
    }

    /**
     * Get the short state label for the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which the short label is desired.
     * @return Short state label.
     */
    @Override
    public final String getStateShortLabel(String identifier) {
        return shortLabelsForStateIdentifiers.get(identifier);
    }

    /**
     * Get the relative weight of the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which the relative weight is
     *            desired.
     * @return State relative weight.
     */
    @Override
    public final int getRelativeWeight(String identifier) {
        return relativeWeightsForStateIdentifiers.get(identifier);
    }

    /**
     * Get the default value of the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which the default value is
     *            desired.
     * @return Default state value.
     */
    @Override
    public final Object getStartingState(String identifier) {
        return valuesForStateIdentifiers.get(identifier);
    }

    /**
     * Create the GUI components making up the specified megawidget.
     * 
     * @param parent
     *            Parent widget in which to place the megawidget.
     * @param creationParams
     *            Hash table mapping identifiers to values that subclasses might
     *            require when creating a megawidget.
     * @return Created megawidget.
     * @throws MegawidgetException
     *             If an exception occurs during creation or initialization of
     *             the megawidget.
     */
    @Override
    public <P extends Widget> Megawidget createMegawidget(P parent,
            Map<String, Object> creationParams) throws MegawidgetException {
        return setToDefaultState((StatefulMegawidget) super.createMegawidget(
                parent, creationParams));
    }

    // Protected Methods

    /**
     * Get the maximum number of state identifiers that may be associated with
     * this megawidget specifier. This implementation simply returns 1, meaning
     * that only subclasses that allow or require multiple state identifiers per
     * megawidget specifier have to override this method.
     * <p>
     * <strong>Note</strong>: This method is invoked during
     * <code>StatefulMegawidgetSpecifier</code> construction, and thus must be
     * implemented to not rely upon (or alter) subclass-member-specific
     * variables. Thus, it should return a constant.
     * 
     * @return Maximum number of state identifiers that may be associated with
     *         the megawidget specifier.
     */
    protected int getMaximumStateIdentifierCount() {
        return 1;
    }

    /**
     * Get a mapping of state identifiers to parameters of the specified type
     * from the specified object.
     * 
     * @param parameters
     *            Map of specifier parameter keys to their associated values in
     *            which to find the parameter.
     * @param key
     *            Key to be used within <code>parameters</code> to retrieve the
     *            value.
     * @param description
     *            Description of the type of value expected for each state
     *            mapped parameter within <code>parameters</code>.
     * @param defaultValue
     *            Default parameter value to be used if no mapping is given.
     * @param defaultMap
     *            Optional default mapping to to be used if no mapping is given.
     *            If provided, this is what is returned in the absence of a
     *            mapping; if <code>null</code>, a new mapping is constructed
     *            that maps all state identifiers to the value of <code>
     *            defaultParameter</code>.
     * @param valueConverter
     *            Optional value converter to be used on the value(s) contained
     *            by <code>object</code> to convert them to values of the
     *            appropriate type. If <code>null</code>, the value is converted
     *            by a simple typecast.
     * @return Mapping of state identifiers to parameters of the specified type.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    protected final <T> Map<String, T> getStateMappedParametersFromObject(
            Map<String, Object> parameters, String key, String description,
            T defaultValue, Map<String, T> defaultMap,
            IValueConverter<T> valueConverter)
            throws MegawidgetSpecificationException {

        // If no object is given to provide the mapping,
        // return a default mapping; otherwise, if it is
        // a map, iterate through the list's values and
        // add them to the mapping; otherwise, just add
        // the single value to the mapping.
        Object object = parameters.get(key);
        Map<String, T> map = null;
        if (object == null) {

            // If no default mapping was supplied, create
            // a new mapping for all keys to the default
            // parameter value.
            if (defaultMap == null) {
                map = new HashMap<String, T>();
                for (String identifier : stateIdentifiers) {
                    map.put(identifier, defaultValue);
                }
            } else {
                map = defaultMap;
            }
        } else if (object instanceof Map) {

            // Ensure that the map has the right number
            // of values within it.
            Map<?, ?> providedMap = (Map<?, ?>) object;
            if (providedMap.size() != stateIdentifiers.size()) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), key, providedMap,
                        "map must contain same number of parameters "
                                + "as megawidget has state identifiers");
            }

            // Create the mapping based on the list's
            // values, associating each value with the
            // state identifier at the corresponding
            // index.
            map = new HashMap<String, T>();
            for (String identifier : stateIdentifiers) {
                map.put(identifier,
                        convertValue(identifier, providedMap.get(identifier),
                                valueConverter, key, description));
            }
        } else {

            // Ensure that there is only one state
            // identifier.
            if (stateIdentifiers.size() != 1) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), key, object,
                        "map must contain same number of parameters "
                                + "as megawidget has state identifiers");
            }

            // Create the mapping of the single state
            // identifier to the given value.
            map = new HashMap<String, T>();
            map.put(stateIdentifiers.get(0),
                    convertValue(stateIdentifiers.get(0), object,
                            valueConverter, key, description));
        }

        // Return the result.
        return map;
    }

    /**
     * Set the specified megawidget to use the default state, if any.
     * 
     * @param megawidget
     *            Megawidget to be set to the default state.
     * @return Megawidget that had its state set, for chaining convenience
     *         purposes.
     * @throws MegawidgetStateException
     *             If the default state is invalid.
     */
    protected final StatefulMegawidget setToDefaultState(
            StatefulMegawidget megawidget) throws MegawidgetStateException {

        // Iterate through the state identifiers, set-
        // ting each corresponding state to its de-
        // fault value, if any. If the megawidget can
        // have state changes committed later, then
        // wait on the commit until afterward.
        boolean toBeCommitted = false;
        for (String identifier : stateIdentifiers) {
            Object defaultValue = getStartingState(getIdentifier());
            if (defaultValue != null) {
                if (megawidget instanceof IExplicitCommitStateful) {
                    ((IExplicitCommitStateful) megawidget).setUncommittedState(
                            identifier, defaultValue);
                    toBeCommitted = true;
                } else {
                    megawidget.setState(identifier, defaultValue);
                }
            }
        }

        // If the megawidget has state changes that
        // are to be committed, commit them now.
        if (toBeCommitted) {
            ((IExplicitCommitStateful) megawidget).commitStateChanges();
        }
        return megawidget;
    }

    // Private Methods

    /**
     * Convert the specified object to a value of the parameterized type, using
     * the specified value converter.
     * 
     * @param identifier
     *            State identifier to which object applies.
     * @param object
     *            Object to be converted.
     * @param valueConverter
     *            Value converter to be used; if <code>null</code>, the value is
     *            converted via a typecast.
     * @param parameterName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param description
     *            Description of the type of value that is expected.
     * @return Converted value.
     * @throws MegawidgetSpecificationException
     *             If the conversion attempt failed.
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(String identifier, Object object,
            IValueConverter<T> valueConverter, String parameterName,
            String description) throws MegawidgetSpecificationException {
        if (object == null) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), parameterName + "[\"" + identifier + "\"]",
                    null, null);
        }
        try {
            if (valueConverter == null) {
                return (T) object;
            } else {
                return valueConverter.getValueFromObject(object);
            }
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), parameterName + "[\"" + identifier + "\"]",
                    object, "must be " + description);
        }
    }

    /**
     * Ensure the specified state identifier is valid, throwing an exception if
     * not.
     * 
     * @param identifier
     *            State identifier to be checked for validity.
     * @throws MegawidgetStateException
     *             If the state identifier is not valid.
     */
    void ensureStateIdentifierIsValid(String identifier)
            throws MegawidgetStateException {
        if (stateIdentifiers.contains(identifier) == false) {
            throw new MegawidgetStateException(identifier, getType(), null,
                    "invalid state identifier");
        }
    }
}
