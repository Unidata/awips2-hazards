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
 * {@link Double} instances, and are within boundaries that are themselves
 * representable (since not all <code>Double</code> values are representable
 * given that only a certain level of precision is allowable).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * Jun 04, 2014   2155     Chris.Golden Corrected invalid increment delta
 *                                      error message, and changed ordering
 *                                      of parameter checking when being
 *                                      initialized so that the increment
 *                                      delta can be checked against the
 *                                      precision.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BoundedFractionValidator extends BoundedNumberValidator<Double> {

    // Private Variables

    /**
     * Key in map from {@link #getParameters()} for the precision parameter.
     */
    private final String precisionKey;

    /**
     * Precision, that is, number of digits following the decimal point.
     */
    private int precision;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param minimumValueKey
     *            Key in <code>parameters</code> for the minimum value
     *            parameter.
     * @param maximumValueKey
     *            Key in <code>parameters</code> for the maximum value
     *            parameter.
     * @param incrementDeltaKey
     *            Key in <code>parameters</code> for the increment delta
     *            parameter.
     * @param precisionKey
     *            Key in <code>parameters</code> for the precision parameter.
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     */
    public BoundedFractionValidator(Map<String, Object> parameters,
            String minimumValueKey, String maximumValueKey,
            String incrementDeltaKey, String precisionKey, Double lowest,
            Double highest) {
        super(parameters, minimumValueKey, maximumValueKey, incrementDeltaKey,
                Double.class, lowest, highest);
        this.precisionKey = precisionKey;
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
    protected BoundedFractionValidator(BoundedFractionValidator other) {
        super(other);
        precisionKey = other.precisionKey;
        precision = other.precision;
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedFractionValidator(this);
    }

    /**
     * Get the precision.
     * 
     * @return Precision.
     */
    public final int getPrecision() {
        return precision;
    }

    @Override
    public void setMinimumValue(Object minimum)
            throws MegawidgetPropertyException {

        /*
         * Remember the old value, and attempt to use the new value.
         */
        RangeValidatorHelper<Double> helper = getRangeValidator();
        Double lastMin = helper.getMinimumValue();
        helper.setMinimumValue(minimum);

        /*
         * Ensure that the resulting range is representable; if it is not,
         * restore the old value.
         */
        try {
            ensureRangeRepresentable();
        } catch (MegawidgetException e) {
            helper.setMinimumValue(lastMin);
            new MegawidgetPropertyException(e.getIdentifier(),
                    helper.getMinimumPropertyName(), e.getType(), minimum,
                    e.getMessage(), e.getCause());
        }
    }

    @Override
    public void setMaximumValue(Object maximum)
            throws MegawidgetPropertyException {

        /*
         * Remember the old value, and attempt to use the new value.
         */
        RangeValidatorHelper<Double> helper = getRangeValidator();
        Double lastMax = helper.getMaximumValue();
        helper.setMaximumValue(maximum);

        /*
         * Ensure that the resulting range is representable; if it is not,
         * restore the old value.
         */
        try {
            ensureRangeRepresentable();
        } catch (MegawidgetException e) {
            helper.setMaximumValue(lastMax);
            new MegawidgetPropertyException(e.getIdentifier(),
                    helper.getMaximumPropertyName(), e.getType(), maximum,
                    e.getMessage(), e.getCause());
        }
    }

    @Override
    public void setRange(Object minimum, Object maximum)
            throws MegawidgetPropertyException {

        /*
         * Remember the old values, and attempt to use the new values.
         */
        RangeValidatorHelper<Double> helper = getRangeValidator();
        Double lastMin = helper.getMinimumValue();
        Double lastMax = helper.getMaximumValue();
        helper.setRange(minimum, maximum);

        /*
         * Ensure that the resulting range is representable; if it is not,
         * restore the old values.
         */
        try {
            ensureRangeRepresentable();
        } catch (MegawidgetException e) {
            helper.setRange(lastMin, lastMax);
            throw new MegawidgetPropertyException(null, e);
        }
    }

    @Override
    public Double convertToStateValue(Object object) throws MegawidgetException {

        /*
         * Convert the value to a double, then round it to the closest possible
         * value given the precision.
         */
        RangeValidatorHelper<Double> helper = getRangeValidator();
        Double value = ConversionUtilities.getStateDoubleObjectFromObject(
                getIdentifier(), getType(), object, helper.getMinimumValue());
        double precisionDenominator = Math.pow(10.0, precision);
        value = (Math.round(value * precisionDenominator))
                / precisionDenominator;

        /*
         * Ensure that the value is within bounds.
         */
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

        /*
         * If the precision is present, ensure that it is a positive integer
         * between 1 and 10.
         */
        precision = ConversionUtilities.getSpecifierIntegerValueFromObject(
                getIdentifier(), getType(), getParameters().get(precisionKey),
                precisionKey, 1);
        if ((precision < 1) || (precision > 10)) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), precisionKey, precision,
                    "must be positive number between 1 and 10 inclusive");
        }

        /*
         * Let the superclass handle the rest of the parameters' initialization.
         */
        super.doInitialize();

        /*
         * Make sure that the the range, given the precision, is representable.
         */
        try {
            ensureRangeRepresentable();
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(null, e);
        }
    }

    @Override
    protected Double setIncrementDelta(String incrementDeltaKey,
            Object incrementDelta) throws MegawidgetException {
        Double newDelta = ConversionUtilities
                .getSpecifierDoubleObjectFromObject(getIdentifier(), getType(),
                        getParameters().get(incrementDeltaKey),
                        incrementDeltaKey, 0.0);
        if (newDelta < Math.pow(10, -precision)) {
            throw new MegawidgetException(getIdentifier(), getType(),
                    incrementDelta,
                    "must be positive number no smaller than 10 raised to "
                            + "the power of -P, where P is precision");
        }
        return newDelta;
    }

    // Private Methods

    /**
     * Ensure that the specified range is representable, given the precision in
     * use.
     * 
     * @throws MegawidgetException
     *             If the range is not representable.
     */
    private void ensureRangeRepresentable() throws MegawidgetException {
        double precisionMultiplier = Math.pow(10, precision);
        double minAllowable = Integer.MIN_VALUE / precisionMultiplier;
        double maxAllowable = Integer.MAX_VALUE / precisionMultiplier;
        RangeValidatorHelper<Double> helper = getRangeValidator();
        double minValue = helper.getMinimumValue();
        double maxValue = helper.getMaximumValue();
        if ((minValue < minAllowable) || (minValue > maxAllowable)
                || (maxValue < minAllowable) || (maxValue > maxAllowable)) {
            throw new MegawidgetException(getIdentifier(), getType(), null,
                    "combination of minimum and maximum values and precision "
                            + "results in out of bounds range");
        }
    }
}
