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

import gov.noaa.gsd.viz.megawidgets.ConversionUtilities;
import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Number} and {@link Comparable} instances, are within fixed boundaries,
 * and are in ascending order for multiple-state {@link IStateful} and
 * {@link IStatefulSpecifier} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 26, 2015   2331     Chris.Golden Initial creation (refactored out of
 *                                      BoundedMultiLongValidator).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class BoundedMultiNumberValidator<T extends Number & Comparable<T>>
        extends MultiStateValidator<T> {

    // Private Variables

    /**
     * Map of parameters used to create the specifier. This may be
     * <code>null</code> if the validator has been constructed as already
     * initialized.
     */
    private final Map<String, Object> parameters;

    /**
     * Key in {@link #parameters} for the map of state identifiers to minimum
     * values; if <code>null</code>, the minimum for all state identifiers is
     * assumed to be {@link #lowest}.
     */
    private final String minimumValuesKey;

    /**
     * Key in {@link #parameters} for the map of state identifiers to maximum
     * values; if <code>null</code>, the maximum for all state identifiers is
     * assumed to be {@link #highest}.
     */
    private final String maximumValuesKey;

    /**
     * Key in {@link #parameters} for the minimum interval parameter; if
     * <code>null</code>, the minimum interval is assumed to be the value
     * returned by {@link #getSmallestAllowableInterval()}.
     */
    private final String minimumIntervalKey;

    /**
     * Type of state.
     */
    private final Class<T> comparableClass;

    /**
     * Lowest allowable value; the minimum value may not be lower than this.
     */
    private final T lowest;

    /**
     * Highest allowable value; the maximum value may not be higher than this.
     */
    private final T highest;

    /**
     * Minimum interval allowed between adjacent values.
     */
    private T minimumInterval;

    /**
     * Flag indicating whether or not only the first state identifier can have
     * its own allowable boundaries.
     */
    private final boolean individualBoundsOnlyForFirstIdentifier;

    /**
     * Map of range validator helpers, one for each state identifier.
     */
    private final Map<String, RangeValidatorHelper<T>> helpersForStateIdentifiers;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param minimumValuesKey
     *            Key in <code>parameters</code> for the map of state
     *            identifiers to minimum values; if <code>null</code>, the
     *            minimum for all state identifiers is assumed to be
     *            <code>lowest</code>.
     * @param maximumValuesKey
     *            Key in <code>parameters</code> for the map of state
     *            identifiers to maximum values; if <code>null</code>, the
     *            maximum for all state identifiers is assumed to be
     *            <code>highest</code>.
     * @param minimumIntervalKey
     *            Key in <code>parameters</code> for the minimum interval
     *            parameter; if <code>null</code>, no minimum interval may be
     *            specified and it is assumed to be 0.
     * @param individualBoundsOnlyForFirstIdentifier
     *            Flag indicating whether or not only the first state identifier
     *            can have its own allowable boundaries.
     * @param comparableClass
     *            Type of state.
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     */
    public BoundedMultiNumberValidator(Map<String, Object> parameters,
            String minimumValuesKey, String maximumValuesKey,
            String minimumIntervalKey,
            boolean individualBoundsOnlyForFirstIdentifier,
            Class<T> comparableClass, T lowest, T highest)
            throws MegawidgetSpecificationException {
        this.parameters = parameters;
        this.minimumValuesKey = minimumValuesKey;
        this.maximumValuesKey = maximumValuesKey;
        this.minimumIntervalKey = minimumIntervalKey;
        this.comparableClass = comparableClass;
        this.lowest = lowest;
        this.highest = highest;
        this.individualBoundsOnlyForFirstIdentifier = individualBoundsOnlyForFirstIdentifier;
        this.helpersForStateIdentifiers = new HashMap<>();
    }

    // Protected Constructors

    /**
     * Construct an instance that is a copy of another already-initialized
     * instance.
     * 
     * @param other
     *            Validator to be copied.
     * @throws IllegalArgumentException
     *             If <code>other</code> has not yet been initialized.
     */
    protected BoundedMultiNumberValidator(BoundedMultiNumberValidator<T> other) {
        super(other);
        parameters = null;
        minimumValuesKey = other.minimumValuesKey;
        maximumValuesKey = other.maximumValuesKey;
        minimumIntervalKey = other.minimumIntervalKey;
        comparableClass = other.comparableClass;
        lowest = other.lowest;
        highest = other.highest;
        individualBoundsOnlyForFirstIdentifier = other.individualBoundsOnlyForFirstIdentifier;
        helpersForStateIdentifiers = new HashMap<>(
                other.helpersForStateIdentifiers.size(), 1.0f);
        for (Map.Entry<String, RangeValidatorHelper<T>> entry : other.helpersForStateIdentifiers
                .entrySet()) {
            helpersForStateIdentifiers.put(entry.getKey(),
                    new RangeValidatorHelper<T>(entry.getValue()));
        }
        minimumInterval = other.minimumInterval;
    }

    // Public Methods

    /**
     * Get the lowest possible allowed value.
     * 
     * @return Lowest possible allowed value.
     */
    public final T getLowestAllowableValue() {
        return lowest;
    }

    /**
     * Get the highest possible allowed value.
     * 
     * @return Highest possible allowed value.
     */
    public final T getHighestAllowableValue() {
        return highest;
    }

    /**
     * Get the minimum allowed value for the specified state identifier.
     * 
     * @param identifier
     *            State identifier for which to get the minimum allowed value.
     * @return Minimum allowed value for the specified state identifier.
     */
    public final T getMinimumValue(String identifier) {
        return helpersForStateIdentifiers.get(identifier).getMinimumValue();
    }

    /**
     * Get the maximum allowed value for the specified state identifier.
     * 
     * @param identifier
     *            State identifier for which to get the maximum allowed value.
     * @return Maximum allowed value for the specified state identifier.
     */
    public final T getMaximumValue(String identifier) {
        return helpersForStateIdentifiers.get(identifier).getMaximumValue();
    }

    /**
     * Set the minimum value for the specified state identifier to that
     * specified.
     * 
     * @param identifier
     *            State identifier for which to set the minimum allowed value.
     * @param minimum
     *            Object to be used as the new minimum value.
     * @throws MegawidgetPropertyException
     *             If the new value is not valid.
     */
    public void setMinimumValue(String identifier, Object minimum)
            throws MegawidgetPropertyException {
        if ((individualBoundsOnlyForFirstIdentifier == false)
                || identifier.equals(getIdentifiers().get(0))) {
            helpersForStateIdentifiers.get(identifier).setMinimumValue(minimum);
        }
    }

    /**
     * Set the maximum value for the specified state identifier to that
     * specified.
     * 
     * @param identifier
     *            State identifier for which to set the maximum allowed value.
     * @param maximum
     *            Object to be used as the new maximum value.
     * @throws MegawidgetPropertyException
     *             If the new value is not valid.
     */
    public void setMaximumValue(String identifier, Object maximum)
            throws MegawidgetPropertyException {
        if ((individualBoundsOnlyForFirstIdentifier == false)
                || identifier.equals(getIdentifiers().get(0))) {
            helpersForStateIdentifiers.get(identifier).setMaximumValue(maximum);
        }
    }

    /**
     * Set the minimum and maximum values for the specified state identifier to
     * those specified.
     * 
     * @param identifier
     *            State identifier for which to set the minimum and maximum
     *            allowed values.
     * @param minimum
     *            Object to be used as the new minimum value.
     * @param maximum
     *            Object to be used as the new maximum value.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    public void setRange(String identifier, Object minimum, Object maximum)
            throws MegawidgetPropertyException {
        if ((individualBoundsOnlyForFirstIdentifier == false)
                || identifier.equals(getIdentifiers().get(0))) {
            helpersForStateIdentifiers.get(identifier).setRange(minimum,
                    maximum);
        }
    }

    /**
     * Get the minimum values for all states.
     * 
     * @return Map of state identifiers to their minimum values.
     */
    public Map<String, T> getMinimumValues() {
        Map<String, T> map = new HashMap<>(getIdentifiers().size(), 1.0f);
        for (String identifier : getIdentifiers()) {
            map.put(identifier, helpersForStateIdentifiers.get(identifier)
                    .getMinimumValue());
        }
        return map;
    }

    /**
     * Get the maximum values for all states.
     * 
     * @return Map of state identifiers to their maximum values.
     */
    public Map<String, T> getMaximumValues() {
        Map<String, T> map = new HashMap<>(getIdentifiers().size(), 1.0f);
        for (String identifier : getIdentifiers()) {
            map.put(identifier, helpersForStateIdentifiers.get(identifier)
                    .getMaximumValue());
        }
        return map;
    }

    /**
     * Set the minimum values for all states.
     * 
     * @param values
     *            Map of state identifiers to the new values they should have
     *            for minimums.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    public void setMinimumValues(Object values)
            throws MegawidgetPropertyException {
        Map<String, Object> valuesForIdentifiers = getPropertyValuesForIdentifiers(
                minimumValuesKey, values, "minimum");
        Map<String, T> oldMinimumValues = getMinimumValues(valuesForIdentifiers
                .keySet());
        String firstIdentifier = getIdentifiers().get(0);
        try {
            for (Map.Entry<String, Object> entry : valuesForIdentifiers
                    .entrySet()) {
                if ((individualBoundsOnlyForFirstIdentifier == false)
                        || entry.getKey().equals(firstIdentifier)) {
                    helpersForStateIdentifiers.get(entry.getKey())
                            .setMinimumValue(entry.getValue());
                }
            }
            ensureBoundariesAreNotContradictory(minimumValuesKey);
        } catch (Exception e) {
            restoreMinimumValues(oldMinimumValues);
            throw e;
        }
    }

    /**
     * Set the maximum values for all states.
     * 
     * @param values
     *            Map of state identifiers to the new values they should have
     *            for maximums.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    public void setMaximumValues(Object values)
            throws MegawidgetPropertyException {
        Map<String, Object> valuesForIdentifiers = getPropertyValuesForIdentifiers(
                maximumValuesKey, values, "maximum");
        Map<String, T> oldMaximumValues = getMaximumValues(valuesForIdentifiers
                .keySet());
        String firstIdentifier = getIdentifiers().get(0);
        try {
            for (Map.Entry<String, Object> entry : valuesForIdentifiers
                    .entrySet()) {
                if ((individualBoundsOnlyForFirstIdentifier == false)
                        || entry.getKey().equals(firstIdentifier)) {
                    helpersForStateIdentifiers.get(entry.getKey())
                            .setMaximumValue(entry.getValue());
                }
            }
            ensureBoundariesAreNotContradictory(maximumValuesKey);
        } catch (Exception e) {
            restoreMaximumValues(oldMaximumValues);
            throw e;
        }
    }

    /**
     * Set the minimum and maximum values for all states.
     * 
     * @param minimumValues
     *            Map of state identifiers to the new values they should have
     *            for minimums.
     * @param maximumValues
     *            Map of state identifiers to the new values they should have
     *            for maximums.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    public void setRanges(Object minimumValues, Object maximumValues)
            throws MegawidgetPropertyException {
        Map<String, Object> minimumValuesForIdentifiers = getPropertyValuesForIdentifiers(
                minimumValuesKey, minimumValues, "minimum");
        Map<String, Object> maximumValuesForIdentifiers = getPropertyValuesForIdentifiers(
                maximumValuesKey, maximumValues, "maximum");
        Map<String, T> oldMinimumValues = getMinimumValues(minimumValuesForIdentifiers
                .keySet());
        Map<String, T> oldMaximumValues = getMaximumValues(maximumValuesForIdentifiers
                .keySet());
        String firstIdentifier = getIdentifiers().get(0);
        try {
            Set<String> identifiers = Sets.union(
                    minimumValuesForIdentifiers.keySet(),
                    maximumValuesForIdentifiers.keySet());
            for (String identifier : identifiers) {
                if ((individualBoundsOnlyForFirstIdentifier == false)
                        || identifier.equals(firstIdentifier)) {
                    helpersForStateIdentifiers.get(identifier).setRange(
                            minimumValuesForIdentifiers.get(identifier),
                            maximumValuesForIdentifiers.get(identifier));
                }
            }
            ensureBoundariesAreNotContradictory(minimumValuesKey + "\" and \""
                    + maximumValuesKey);
        } catch (MegawidgetPropertyException e) {
            restoreMinimumValues(oldMinimumValues);
            restoreMaximumValues(oldMaximumValues);
            throw e;
        }
    }

    /**
     * Get the minimum interval.
     * 
     * @return Minimum interval.
     */
    public T getMinimumInterval() {
        return minimumInterval;
    }

    @Override
    public T convertToStateValue(String identifier, Object object)
            throws MegawidgetException {
        RangeValidatorHelper<T> helper = helpersForStateIdentifiers
                .get(identifier);
        T value = ConversionUtilities.getStateDynamicallyTypedObjectFromObject(
                identifier, getType(), object, comparableClass,
                helper.getMinimumValue());
        if ((value.compareTo(helper.getMinimumValue()) < 0)
                || (value.compareTo(helper.getMaximumValue()) > 0)) {
            throw new MegawidgetStateException(getIdentifiers().get(
                    getIdentifiers().size() - 1), getType(), value,
                    "out of bounds");
        }
        return value;
    }

    @Override
    public Map<String, T> convertToStateValues(
            Map<String, ?> objectsForIdentifiers) throws MegawidgetException {

        /*
         * If the map is empty, come up with some default values equally spaced
         * along the allowable range. For any value, if this results in it
         * violating the boundaries within which it must stay, just place it
         * within those boundaries, and make the next value the minimum interval
         * away from it (unless the latter violates its boundaries, in which
         * case it is moved inside those boundaries, and so on).
         */
        if ((objectsForIdentifiers == null) || objectsForIdentifiers.isEmpty()) {
            Map<String, T> defaultMap = new HashMap<>();
            T range = subtract(highest, lowest);
            T interval = divide(range,
                    convertToNumber(getIdentifiers().size() + 1));
            T value = interval;
            for (int j = 0; j < getIdentifiers().size(); j++) {
                String identifier = getIdentifiers().get(j);
                RangeValidatorHelper<T> helper = helpersForStateIdentifiers
                        .get(identifier);
                T thisValue = value;
                boolean changed = true;
                if (value.compareTo(helper.getMinimumValue()) < 0) {
                    thisValue = helper.getMinimumValue();
                } else if (value.compareTo(helper.getMaximumValue()) > 0) {
                    thisValue = helper.getMaximumValue();
                } else {
                    changed = false;
                }
                defaultMap.put(identifier, thisValue);
                if (changed) {
                    value = add(thisValue, minimumInterval);
                } else {
                    value = add(value, interval);
                }
            }
            objectsForIdentifiers = defaultMap;
        }

        /*
         * Build a map of the converted values, checking each value in order to
         * ensure that it is within bounds and that all values are in ascending
         * order with respect to the order of the state identifiers, and have at
         * least the minimum interval in between them.
         */
        Map<String, T> valuesForIdentifiers = new HashMap<>(
                objectsForIdentifiers.size(), 1.0f);
        T lastValue = null;
        for (int j = 0; j < getIdentifiers().size(); j++) {
            String identifier = getIdentifiers().get(j);
            T value = ConversionUtilities
                    .getStateDynamicallyTypedObjectFromObject(identifier,
                            getType(), objectsForIdentifiers.get(identifier),
                            comparableClass, null);
            RangeValidatorHelper<T> helper = helpersForStateIdentifiers
                    .get(identifier);
            if ((value.compareTo(helper.getMinimumValue()) < 0)
                    || (value.compareTo(helper.getMaximumValue()) > 0)) {
                throw new MegawidgetStateException(identifier, getType(),
                        value, "out of bounds");
            } else if ((j > 0)
                    && (value.compareTo(add(lastValue, minimumInterval)) < 0)) {
                throw new MegawidgetStateException(identifier, getType(),
                        value, "not in ascending order");
            }
            valuesForIdentifiers.put(identifier, value);
            lastValue = value;
        }
        return valuesForIdentifiers;
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {

        /*
         * Get the maps of minimum and maximum values for state identifiers,
         * respectively.
         */
        Map<String, Object> minimumsForIdentifiers = getSpecifierValuesForIdentifiers(
                minimumValuesKey, "minimum");
        Map<String, Object> maximumsForIdentifiers = getSpecifierValuesForIdentifiers(
                maximumValuesKey, "maximum");

        /*
         * Create the range validator helpers, one per state identifier, since
         * each of the latter may have different minimum and maximum values. If
         * only the first identifier can have its own allowable boundaries, then
         * just use a default helper with lowest and highest as its boundaries
         * for any state identifiers after the first one.
         */
        RangeValidatorHelper<T> defaultHelper = (individualBoundsOnlyForFirstIdentifier
                && (getIdentifiers().size() > 1) ? new RangeValidatorHelper<T>(
                getType(), getMegawidgetIdentifier(), parameters, null, null,
                comparableClass, lowest, highest) : null);
        boolean useDefaultHelper = false;
        for (String identifier : getIdentifiers()) {
            helpersForStateIdentifiers.put(identifier,
                    (useDefaultHelper ? defaultHelper
                            : new RangeValidatorHelper<T>(getType(),
                                    getMegawidgetIdentifier(),
                                    minimumsForIdentifiers.get(identifier),
                                    maximumsForIdentifiers.get(identifier),
                                    minimumValuesKey + "[" + identifier + "]",
                                    maximumValuesKey + "[" + identifier + "]",
                                    comparableClass, lowest, highest)));
            useDefaultHelper = (defaultHelper != null);
        }
        if (minimumIntervalKey != null) {
            minimumInterval = getMinimumInterval(minimumIntervalKey, parameters);
        } else {
            minimumInterval = getSmallestAllowableInterval();
        }
    }

    /**
     * Get the smallest allowable interval. This also serves as the default
     * interval if none is supplied at initialization.
     * 
     * @return Smallest allowable interval.
     */
    protected abstract T getSmallestAllowableInterval();

    /**
     * Add the specified values together and return the result.
     * 
     * @param first
     *            First value.
     * @param second
     *            Second value.
     * @return Result of the addition of the two values.
     */
    protected abstract T add(T first, T second);

    /**
     * Subtract the second value from the first and return the result.
     * 
     * @param minuend
     *            Value from which to subtract.
     * @param subtrahend
     *            Value to be subtracted.
     * @return Result of the subtraction of the second value from the first.
     */
    protected abstract T subtract(T minuend, T subtrahend);

    /**
     * Multiply the specified values together and return the result.
     * 
     * @param first
     *            First value.
     * @param second
     *            Second value.
     * @return Result of the multiplication of the two values.
     */
    protected abstract T multiply(T first, T second);

    /**
     * Divide the first value by the second and return the result.
     * 
     * @param dividend
     *            Value to be divided.
     * @param divisor
     *            Value by which to divide.
     * @return Result of the division of the first value by the second.
     */
    protected abstract T divide(T dividend, T divisor);

    /**
     * Convert the specified integer to the appropriate type of number.
     * 
     * @param value
     *            Value to be converted.
     * @return Result of the conversion.
     */
    protected abstract T convertToNumber(int value);

    // Private Methods

    /**
     * Get a map of state identifiers to values from the {@link #parameters}.
     * 
     * @param key
     *            Key under which to search for the map in
     *            <code>parameters</code>; if <code>null</code>, an empty map
     *            will be returned.
     * @param description
     *            Description of the function of the values to be fetched, to be
     *            used if an exception is thrown.
     * @return Map of state identifiers to values.
     * @throws MegawidgetSpecificationException
     *             If the value found in <code>parameters</code> is not a map.
     */
    private Map<String, Object> getSpecifierValuesForIdentifiers(String key,
            String description) throws MegawidgetSpecificationException {
        try {
            return getValuesForIdentifiers(parameters.get(key), description);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(key, e);
        }
    }

    /**
     * Get a map of property state identifiers to values from the given value.
     * 
     * @param name
     *            Name of the property.
     * @param value
     *            Value to be changed into the map; if <code>null</code>, an
     *            empty map will be returned.
     * @param description
     *            Description of the function of the values to be fetched, to be
     *            used if an exception is thrown.
     * @return Map of state identifiers to values.
     * @throws MegawidgetPropertyException
     *             If the value is invalid.
     */
    private Map<String, Object> getPropertyValuesForIdentifiers(String name,
            Object value, String description)
            throws MegawidgetPropertyException {
        try {
            return getValuesForIdentifiers(value, description);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a map of state identifiers to values from the given value.
     * 
     * @param value
     *            Value to be changed into the map; if <code>null</code>, an
     *            empty map will be returned.
     * @param description
     *            Description of the function of the values to be fetched, to be
     *            used if an exception is thrown.
     * @return Map of state identifiers to values.
     * @throws MegawidgetException
     *             If the value is invalid.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getValuesForIdentifiers(Object value,
            String description) throws MegawidgetException {
        try {
            if (value == null) {
                return Collections.emptyMap();
            } else if (value instanceof Map) {
                return (Map<String, Object>) value;
            } else if (comparableClass.isAssignableFrom(value.getClass()) == false) {
                throw new Exception();
            } else {
                Map<String, Object> map = new HashMap<>(
                        getIdentifiers().size(), 1.0f);
                for (String identifier : getIdentifiers()) {
                    map.put(identifier, value);
                }
                return map;
            }
        } catch (Exception e) {
            throw new MegawidgetException(getMegawidgetIdentifier(), getType(),
                    value, "must be map of state identifiers to " + description
                            + " values", e);
        }
    }

    /**
     * Ensure that the boundaries for the different state identifiers are not
     * contradictory, that is, that a state value has boundaries that make it
     * impossible for it to be less than or equal to the next state value, minus
     * the minimum interval.
     * 
     * @param propertyName
     *            Name of the property that changed, and thus required this
     *            invocation.
     * @throws MegawidgetPropertyException
     *             If the boundaries contradict one another.
     */
    private void ensureBoundariesAreNotContradictory(String propertyName)
            throws MegawidgetPropertyException {
        String previousIdentifier = getIdentifiers().get(0);
        for (int j = 1; j < getIdentifiers().size(); j++) {
            String identifier = getIdentifiers().get(j);
            if (helpersForStateIdentifiers
                    .get(previousIdentifier)
                    .getMinimumValue()
                    .compareTo(
                            add(helpersForStateIdentifiers.get(identifier)
                                    .getMaximumValue(), minimumInterval)) > 0) {
                throw new MegawidgetPropertyException(
                        getMegawidgetIdentifier(), propertyName, getType(),
                        null, "states \"" + previousIdentifier + "\" and \""
                                + identifier
                                + "\" have incompatible boundaries");
            }
            previousIdentifier = identifier;
        }
    }

    /**
     * Get the minimum interval from the specified parameters map using the
     * specified key.
     * 
     * @param key
     *            Key with which object to be used as the minimum interval is
     *            associated in <code>parameters</code>.
     * @param parameters
     *            Map holding parameters.
     * @return Minimum interval.
     * @throws MegawidgetSpecificationException
     *             If a valid minimum interval cannot be found.
     */
    private T getMinimumInterval(String key, Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        T minimumInterval = ConversionUtilities
                .getSpecifierDynamicallyTypedObjectFromObject(
                        getMegawidgetIdentifier(), getType(),
                        parameters.get(key), key, comparableClass,
                        getSmallestAllowableInterval());
        if (minimumInterval.compareTo(getSmallestAllowableInterval()) < 0L) {
            throw new MegawidgetSpecificationException(
                    getMegawidgetIdentifier(), getType(), key,
                    parameters.get(key), "must be greater than or equal to "
                            + getSmallestAllowableInterval());
        }
        return minimumInterval;
    }

    /**
     * Get a map of the specified identifiers to their current minimum values.
     * 
     * @param identifiers
     *            Identifiers for which to build the map.
     * @return Map of the identifiers to their current minimum values.
     */
    private Map<String, T> getMinimumValues(Set<String> identifiers) {
        Map<String, T> minimumValues = new HashMap<>(identifiers.size(), 1.0f);
        for (String identifier : identifiers) {
            minimumValues.put(identifier,
                    helpersForStateIdentifiers.get(identifier)
                            .getMinimumValue());
        }
        return minimumValues;
    }

    /**
     * Get a map of the specified identifiers to their current maximum values.
     * 
     * @param identifiers
     *            Identifiers for which to build the map.
     * @return Map of the identifiers to their current maximum values.
     */
    private Map<String, T> getMaximumValues(Set<String> identifiers) {
        Map<String, T> maximumValues = new HashMap<>(identifiers.size(), 1.0f);
        for (String identifier : identifiers) {
            maximumValues.put(identifier,
                    helpersForStateIdentifiers.get(identifier)
                            .getMinimumValue());
        }
        return maximumValues;
    }

    /**
     * Restore the values given in the specified map as the minimum values for
     * their associated identifiers.
     * 
     * @param valuesForIdentifiers
     *            Map of identifiers to the minimum values they should have.
     * @throws MegawidgetPropertyException
     *             If any of the values are invalid as minimums.
     */
    private void restoreMinimumValues(Map<String, T> valuesForIdentifiers)
            throws MegawidgetPropertyException {
        for (Map.Entry<String, T> entry : valuesForIdentifiers.entrySet()) {
            helpersForStateIdentifiers.get(entry.getKey()).setMinimumValue(
                    entry.getValue());
        }
    }

    /**
     * Restore the values given in the specified map as the maximum values for
     * their associated identifiers.
     * 
     * @param valuesForIdentifiers
     *            Map of identifiers to the maximum values they should have.
     * @throws MegawidgetPropertyException
     *             If any of the values are invalid as maximums.
     */
    private void restoreMaximumValues(Map<String, T> valuesForIdentifiers)
            throws MegawidgetPropertyException {
        for (Map.Entry<String, T> entry : valuesForIdentifiers.entrySet()) {
            helpersForStateIdentifiers.get(entry.getKey()).setMaximumValue(
                    entry.getValue());
        }
    }
}
