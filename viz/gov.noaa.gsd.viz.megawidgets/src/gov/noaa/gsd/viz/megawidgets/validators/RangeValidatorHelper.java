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
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.Map;

/**
 * Description: Validator helper used to ensure that potential boundaries for
 * {@link Comparable} instances are valid.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * Jan 28, 2015   2331     Chris.Golden Added ability to be used by specifiers
 *                                      to manage ranges not defined directly
 *                                      within the megawidget specifiers'
 *                                      parameters.
 * Feb 03, 2015   2331     Chris.Golden Fixed bug that disallowed zero-length
 *                                      ranges.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class RangeValidatorHelper<T extends Comparable<T>> {

    // Protected Classes

    /**
     * Problematic range elements, defining the possible combinations of problem
     * sources resulting from a boundary range check.
     */
    protected enum RangeCheckProblemElements {
        MINIMUM, MAXIMUM, BOTH
    }

    /**
     * Range check problem, encapsulating any problem found while performing a
     * boundary range check.
     */
    protected final class RangeCheckProblem {

        // Private Variables

        /**
         * Problem elements.
         */
        private final RangeCheckProblemElements elements;

        /**
         * Problem value, or <code>null</code> if if {@link #elements} is
         * {@link RangeCheckProblemElements#BOTH}, .
         */
        private final T value;

        /**
         * Problem description.
         */
        private final String description;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param elements
         *            Elements that were found to be problematic during the
         *            range check.
         * @param value
         *            Problem value; if <code>problemElements</code> is
         *            {@link RangeCheckProblemElements#BOTH}, this should be
         *            <code>null</code>.
         * @param description
         *            Description of the problem.
         */
        public RangeCheckProblem(RangeCheckProblemElements elements, T value,
                String description) {
            this.elements = elements;
            this.value = value;
            this.description = description;
        }

        // Private Constructors

        /**
         * Construct an instance indicating that there is no problem.
         */
        private RangeCheckProblem() {
            this.elements = null;
            this.value = null;
            this.description = null;
        }

        // Public Methods

        /**
         * Get the problem elements.
         * 
         * @return Problem elements.
         */
        public final RangeCheckProblemElements getElements() {
            return elements;
        }

        /**
         * Get the problem value.
         * 
         * @return Problem value, or <code>null</code> if {@link #getElements()}
         *         returns {@link RangeCheckProblemElements#BOTH}.
         */
        public final T getValue() {
            return value;
        }

        /**
         * Get the problem description.
         * 
         * @return Problem description, or <code>null</code> if there is no
         *         problem.
         */
        public final String getDescription() {
            return description;
        }
    }

    // Protected Constants

    /**
     * Range check result indicating that there is no problem (as per the null
     * object pattern).
     */
    protected final RangeCheckProblem NO_RANGE_PROBLEM = new RangeCheckProblem();

    // Private Variables

    /**
     * Type of the megawidget.
     */
    private final String type;

    /**
     * Identifier of the megawidget.
     */
    private final String identifier;

    /**
     * Type of the state that is to be validated.
     */
    private final Class<T> comparableClass;

    /**
     * Lowest allowable value that {@link #minimumValue} may be.
     */
    private final T lowestAllowableValue;

    /**
     * Highest allowable value that {@link #maximumValue} may be.
     */
    private final T highestAllowableValue;

    /**
     * Minimum value of any state.
     */
    private T minimumValue;

    /**
     * Maximum value of any state.
     */
    private T maximumValue;

    /**
     * Minimum value property name.
     */
    private final String minimumPropertyName;

    /**
     * Maximum value property name.
     */
    private final String maximumPropertyName;

    // Public Constructors

    /**
     * Construct a standard instance for a {@link StateValidator} used by an
     * {@link IStatefulSpecifier}.
     * 
     * @param type
     *            Type of the megawidget.
     * @param identifier
     *            State identifier of the megawidget.
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param minimumValueKey
     *            Key in <code>parameters</code> for the minimum value
     *            parameter; if <code>null</code>, <code>lowest</code> is used
     *            for the minimum value.
     * @param maximumValueKey
     *            Key in <code>parameters</code> for the maximum value
     *            parameter; if <code>null</code>, <code>highest</code> is used
     *            for the maximum value.
     * @param comparableClass
     *            Type of state.
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     * @throws MegawidgetSpecificationException
     *             If an error occurs during construction, such as if the
     *             minimum value is lower than <code>lowest</code>, the maximum
     *             value is higher than <code>highest</code>, the minimum or
     *             maximum are missing, or the minimum is not less than the
     *             maximum.
     */
    public RangeValidatorHelper(String type, String identifier,
            Map<String, Object> parameters, String minimumValueKey,
            String maximumValueKey, Class<T> comparableClass, T lowest,
            T highest) throws MegawidgetSpecificationException {
        this.type = type;
        this.identifier = identifier;
        this.minimumPropertyName = minimumValueKey;
        this.maximumPropertyName = maximumValueKey;
        this.comparableClass = comparableClass;
        this.lowestAllowableValue = lowest;
        this.highestAllowableValue = highest;

        /*
         * Get the minimum and maximum values.
         */
        minimumValue = (minimumValueKey == null ? lowest : ConversionUtilities
                .getSpecifierDynamicallyTypedObjectFromObject(identifier, type,
                        parameters.get(minimumValueKey), minimumValueKey,
                        comparableClass, null));
        maximumValue = (maximumValueKey == null ? highest : ConversionUtilities
                .getSpecifierDynamicallyTypedObjectFromObject(identifier, type,
                        parameters.get(maximumValueKey), maximumValueKey,
                        comparableClass, null));

        /*
         * Check the resulting range for validity.
         */
        checkStartingRangeForValidity();
    }

    /**
     * Construct a standard instance for a {@link StateValidator} used by an
     * {@link IStatefulSpecifier}. This constructor is intended for situations
     * in which the lower and upper boundaries are always to be used, even if
     * they are the default values taken from <code>lowest</code> and
     * <code>highest</code>.
     * 
     * @param type
     *            Type of the megawidget.
     * @param identifier
     *            State identifier of the megawidget.
     * @param lowerBoundary
     *            Lower boundary for this range; if <code>null</code>, then
     *            <code>lowest</code> will be used. Otherwise, it should be of
     *            type <code>T</code>.
     * @param upperBoundary
     *            Upper boundary for this range; if <code>null</code>, then
     *            <code>highest</code> will be used. Otherwise, it should be of
     *            type <code>T</code>.
     * @param minimumValuePropertyName
     *            Name of the minimum value property, to be used when an error
     *            occurs to describe the problem in the resulting exception.
     * @param maximumValuePropertyName
     *            Name of the maximum value property, to be used when an error
     *            occurs to describe the problem in the resulting exception.
     * @param comparableClass
     *            Type of state.
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     * @throws MegawidgetSpecificationException
     *             If an error occurs during construction, such as if the
     *             minimum value is lower than <code>lowest</code>, the maximum
     *             value is higher than <code>highest</code>, the minimum or
     *             maximum are missing, or the minimum is not less than the
     *             maximum.
     */
    public RangeValidatorHelper(String type, String identifier,
            Object lowerBoundary, Object upperBoundary,
            String minimumValuePropertyName, String maximumValuePropertyName,
            Class<T> comparableClass, T lowest, T highest)
            throws MegawidgetSpecificationException {
        this.type = type;
        this.identifier = identifier;
        this.minimumPropertyName = minimumValuePropertyName;
        this.maximumPropertyName = maximumValuePropertyName;
        this.comparableClass = comparableClass;
        this.lowestAllowableValue = lowest;
        this.highestAllowableValue = highest;

        /*
         * Get the minimum and maximum values.
         */
        minimumValue = ConversionUtilities
                .getSpecifierDynamicallyTypedObjectFromObject(identifier, type,
                        lowerBoundary, minimumPropertyName, comparableClass,
                        lowest);
        maximumValue = ConversionUtilities
                .getSpecifierDynamicallyTypedObjectFromObject(identifier, type,
                        upperBoundary, maximumPropertyName, comparableClass,
                        highest);

        /*
         * Check the resulting range for validity.
         */
        checkStartingRangeForValidity();
    }

    /**
     * Construct a copy of the specified instance, to be used by a
     * {@link StateValidator} that is associated with a {@link IStateful}.
     * 
     * @param other
     *            Other validator helper to be copied.
     */
    public RangeValidatorHelper(RangeValidatorHelper<T> other) {
        type = other.type;
        identifier = other.identifier;
        minimumPropertyName = other.minimumPropertyName;
        maximumPropertyName = other.maximumPropertyName;
        comparableClass = other.comparableClass;
        minimumValue = other.minimumValue;
        maximumValue = other.maximumValue;
        lowestAllowableValue = other.lowestAllowableValue;
        highestAllowableValue = other.highestAllowableValue;
    }

    // Public Methods

    /**
     * Get the name of the minimum value mutable property.
     * 
     * @return Name of the minimum value mutable property, or <code>null</code>
     *         if there is no such mutable property.
     */
    public final String getMinimumPropertyName() {
        return minimumPropertyName;
    }

    /**
     * Get the name of the maximum value mutable property.
     * 
     * @return Name of the maximum value mutable property, or <code>null</code>
     *         if there is no such mutable property.
     */
    public final String getMaximumPropertyName() {
        return maximumPropertyName;
    }

    /**
     * Get the minimum value.
     * 
     * @return Minimum value.
     */
    public final T getMinimumValue() {
        return minimumValue;
    }

    /**
     * Get the maximum value.
     * 
     * @return Maximum value.
     */
    public final T getMaximumValue() {
        return maximumValue;
    }

    /**
     * Set the minimum value to that specified.
     * 
     * @param minimum
     *            Object to be used as the new minimum value.
     * @throws MegawidgetPropertyException
     *             If the new value is not valid.
     */
    public void setMinimumValue(Object minimum)
            throws MegawidgetPropertyException {

        /*
         * Convert the object to a value of the appropriate type.
         */
        T minimumValue = ConversionUtilities
                .getPropertyDynamicallyTypedObjectFromObject(identifier, type,
                        minimum, minimumPropertyName, comparableClass,
                        lowestAllowableValue);

        /*
         * Ensure the value, combined with the other boundary, constitute a
         * valid range.
         */
        RangeCheckProblem result = validateRange(minimumValue, maximumValue);
        if (result != NO_RANGE_PROBLEM) {
            String name = null;
            switch (result.getElements()) {
            case MINIMUM:
                name = minimumPropertyName;
                break;
            default:
                break;
            }
            throw new MegawidgetPropertyException(identifier, name, type,
                    result.getValue(), result.getDescription());
        }

        /*
         * Remember the new value.
         */
        this.minimumValue = minimumValue;
    }

    /**
     * Set the maximum value to that specified.
     * 
     * @param maximum
     *            Object to be used as the new maximum value.
     * @throws MegawidgetPropertyException
     *             If the new value is not valid.
     */
    public void setMaximumValue(Object maximum)
            throws MegawidgetPropertyException {

        /*
         * Convert the object to a value of the appropriate type.
         */
        T maximumValue = ConversionUtilities
                .getPropertyDynamicallyTypedObjectFromObject(identifier, type,
                        maximum, maximumPropertyName, comparableClass,
                        highestAllowableValue);

        /*
         * Ensure the value, combined with the other boundary, constitute a
         * valid range.
         */
        RangeCheckProblem result = validateRange(minimumValue, maximumValue);
        if (result != NO_RANGE_PROBLEM) {
            String name = null;
            switch (result.getElements()) {
            case MAXIMUM:
                name = maximumPropertyName;
                break;
            default:
                break;
            }
            throw new MegawidgetPropertyException(identifier, name, type,
                    result.getValue(), result.getDescription());
        }

        /*
         * Remember the new value.
         */
        this.maximumValue = maximumValue;
    }

    /**
     * Set the minimum and maximum values to those specified.
     * 
     * @param minimum
     *            Object to be used as the new minimum value.
     * @param maximum
     *            Object to be used as the new maximum value.
     * @throws MegawidgetPropertyException
     *             If the new values are not valid.
     */
    public void setRange(Object minimum, Object maximum)
            throws MegawidgetPropertyException {

        /*
         * Convert the objects to values of the appropriate type.
         */
        T minimumValue = ConversionUtilities
                .getPropertyDynamicallyTypedObjectFromObject(identifier, type,
                        minimum, minimumPropertyName, comparableClass,
                        lowestAllowableValue);
        T maximumValue = ConversionUtilities
                .getPropertyDynamicallyTypedObjectFromObject(identifier, type,
                        maximum, maximumPropertyName, comparableClass,
                        highestAllowableValue);

        /*
         * Ensure the values constitute a valid range.
         */
        RangeCheckProblem result = validateRange(minimumValue, maximumValue);
        if (result != NO_RANGE_PROBLEM) {
            String name = null;
            switch (result.getElements()) {
            case MINIMUM:
                name = minimumPropertyName;
                break;
            case MAXIMUM:
                name = maximumPropertyName;
                break;
            default:
                break;
            }
            throw new MegawidgetPropertyException(identifier, name, type,
                    result.getValue(), result.getDescription());
        }

        /*
         * Remember the new values.
         */
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    // Private Methods

    /**
     * Validate the specified minimum and maximum values.
     * 
     * @param minimumValue
     *            Object to be used as the new minimum value.
     * @param maximumValue
     *            Object to be used as the new maximum value.
     * @return Result of the validation attempt.
     */
    private RangeCheckProblem validateRange(T minimumValue, T maximumValue) {
        if (lowestAllowableValue.compareTo(minimumValue) > 0) {
            return new RangeCheckProblem(RangeCheckProblemElements.MINIMUM,
                    minimumValue, "must be no less than "
                            + lowestAllowableValue);
        } else if (highestAllowableValue.compareTo(maximumValue) < 0) {
            return new RangeCheckProblem(RangeCheckProblemElements.MAXIMUM,
                    maximumValue, "must be no greater than "
                            + highestAllowableValue);
        } else if (minimumValue.compareTo(maximumValue) > 0) {
            return new RangeCheckProblem(RangeCheckProblemElements.BOTH, null,
                    "minimum value must be less than or equal to maximum "
                            + "(minimum = " + minimumValue + ", maximum = "
                            + maximumValue + ")");
        }
        return NO_RANGE_PROBLEM;
    }

    /**
     * Validate the starting minimum and maximum values.
     * 
     * @throws MegawidgetSpecificationException
     *             If the starting minimum and maximum values are invalid.
     */
    private void checkStartingRangeForValidity()
            throws MegawidgetSpecificationException {
        RangeCheckProblem result = validateRange(minimumValue, maximumValue);
        if (result != NO_RANGE_PROBLEM) {
            String name = null;
            switch (result.getElements()) {
            case MINIMUM:
                name = minimumPropertyName;
                break;
            case MAXIMUM:
                name = maximumPropertyName;
                break;
            default:
                break;
            }
            throw new MegawidgetSpecificationException(identifier, type, name,
                    result.getValue(), result.getDescription());
        }
    }
}
