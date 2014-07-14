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
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Comparable} instances, and are within boundaries.
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
public class BoundedComparableValidator<T extends Comparable<T>> extends
        SingleStateValidator<T> {

    // Private Variables

    /**
     * Map of parameters used to create the specifier. This may be
     * <code>null</code> if the validator has been constructed as already
     * initialized.
     */
    private final Map<String, Object> parameters;

    /**
     * Key in {@link #parameters} for the minimum value parameter; if
     * <code>null</code>, {@link #lowest} is used for the minimum value.
     */
    private final String minimumValueKey;

    /**
     * Key in {@link #parameters} for the maximum value parameter; if
     * <code>null</code>, {@link #highest} is used for the maximum value.
     */
    private final String maximumValueKey;

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
     * Range validator helper.
     */
    private RangeValidatorHelper<T> helper;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
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
     */
    public BoundedComparableValidator(Map<String, Object> parameters,
            String minimumValueKey, String maximumValueKey,
            Class<T> comparableClass, T lowest, T highest) {
        this.parameters = parameters;
        this.minimumValueKey = minimumValueKey;
        this.maximumValueKey = maximumValueKey;
        this.comparableClass = comparableClass;
        this.lowest = lowest;
        this.highest = highest;
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
    protected BoundedComparableValidator(BoundedComparableValidator<T> other) {
        super(other);
        parameters = null;
        minimumValueKey = other.minimumValueKey;
        maximumValueKey = other.maximumValueKey;
        comparableClass = other.comparableClass;
        lowest = other.lowest;
        highest = other.highest;
        helper = new RangeValidatorHelper<T>(other.helper);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedComparableValidator<T>(this);
    }

    /**
     * Get the minimum value.
     * 
     * @return Minimum value.
     */
    public final T getMinimumValue() {
        return getRangeValidator().getMinimumValue();
    }

    /**
     * Get the maximum value.
     * 
     * @return Maximum value.
     */
    public final T getMaximumValue() {
        return getRangeValidator().getMaximumValue();
    }

    /**
     * Get the type of state.
     * 
     * @return Type of state.
     */
    public final Class<T> getStateType() {
        return comparableClass;
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
        getRangeValidator().setMinimumValue(minimum);
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
        getRangeValidator().setMaximumValue(maximum);
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
        getRangeValidator().setRange(minimum, maximum);
    }

    @Override
    public T convertToStateValue(Object object) throws MegawidgetException {

        /*
         * Ensure that the new state is within bounds and of the correct type.
         */
        T value = ConversionUtilities.getStateDynamicallyTypedObjectFromObject(
                getIdentifier(), getType(), object, comparableClass,
                helper.getMinimumValue());
        if ((value.compareTo(helper.getMinimumValue()) < 0)
                || (value.compareTo(helper.getMaximumValue()) > 0)) {
            throw new MegawidgetException(getIdentifier(), getType(), value,
                    "out of bounds (minimum = " + helper.getMinimumValue()
                            + ", maximum = " + helper.getMaximumValue()
                            + " (inclusive))");
        }
        return value;
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {
        helper = new RangeValidatorHelper<T>(getType(), getIdentifier(),
                parameters, minimumValueKey, maximumValueKey, comparableClass,
                lowest, highest);
    }

    /**
     * Get the parameters for the specifier.
     * 
     * @return Parameters for the specifier.
     */
    protected final Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Get the range validator helper.
     * 
     * @return Range validator helper.
     */
    protected final RangeValidatorHelper<T> getRangeValidator() {
        return helper;
    }
}
