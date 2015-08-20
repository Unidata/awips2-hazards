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
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Double} instances, are within boundaries that are themselves
 * representable (since not all <code>Double</code> values are representable
 * given that only a certain level of precision is allowable), and are in
 * ascending order for multiple-state {@link IStateful} and
 * {@link IStatefulSpecifier} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 06, 2015    4123    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BoundedMultiDoubleValidator extends
        BoundedMultiNumberValidator<Double> {

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
     * @param incrementDeltaKey
     *            Key in <code>parameters</code> for the increment delta
     *            parameter; if <code>null</code>, no increment delta is to be
     *            used.
     * @param precisionKey
     *            Key in <code>parameters</code> for the precision parameter.
     * @param individualBoundsOnlyForFirstIdentifier
     *            Flag indicating whether or not only the first state identifier
     *            can have its own allowable boundaries.
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     */
    public BoundedMultiDoubleValidator(Map<String, Object> parameters,
            String minimumValuesKey, String maximumValuesKey,
            String minimumIntervalKey, String incrementDeltaKey,
            String precisionKey,
            boolean individualBoundsOnlyForFirstIdentifier, Double lowest,
            Double highest) throws MegawidgetSpecificationException {
        super(parameters, minimumValuesKey, maximumValuesKey,
                minimumIntervalKey, incrementDeltaKey,
                individualBoundsOnlyForFirstIdentifier, Double.class, lowest,
                highest);
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
    protected BoundedMultiDoubleValidator(BoundedMultiDoubleValidator other) {
        super(other);
        precisionKey = other.precisionKey;
        precision = other.precision;
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedMultiDoubleValidator(this);
    }

    @Override
    public Double convertToStateValue(String identifier, Object object)
            throws MegawidgetException {

        /*
         * Convert the value to a double, then round it to the closest possible
         * value given the precision.
         */
        RangeValidatorHelper<Double> helper = getRangeValidator(identifier);
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
            throw new MegawidgetException(identifier, getType(), value,
                    "out of bounds (minimum = " + helper.getMinimumValue()
                            + ", maximum = " + helper.getMaximumValue()
                            + " (inclusive))");
        }
        return value;
    }

    @Override
    public Map<String, Double> convertToStateValues(
            Map<String, ?> objectsForIdentifiers) throws MegawidgetException {
        Map<String, Double> results = new HashMap<String, Double>(
                getStateIdentifiers().size(), 1.0f);
        if ((objectsForIdentifiers == null) || objectsForIdentifiers.isEmpty()) {
            objectsForIdentifiers = super.convertToStateValues(null);
        }
        for (Map.Entry<String, ?> entry : objectsForIdentifiers.entrySet()) {
            results.put(entry.getKey(),
                    convertToStateValue(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    @Override
    public Double add(Double first, Double second) {
        return first + second;
    }

    @Override
    public Double subtract(Double minuend, Double subtrahend) {
        return minuend - subtrahend;
    }

    @Override
    public Double multiply(Double first, Double second) {
        return first * second;
    }

    @Override
    public Double divide(Double dividend, Double divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("divide by zero");
        }
        return dividend / divisor;
    }

    /**
     * Get the precision.
     * 
     * @return Precision.
     */
    public int getPrecision() {
        return precision;
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
    protected Double getSmallestAllowableInterval() {
        return 0.0;
    }

    @Override
    protected Double getIncrementDelta(String incrementDeltaKey,
            Object incrementDelta) throws MegawidgetException {
        Double newDelta = ConversionUtilities
                .getSpecifierDoubleObjectFromObject(getIdentifier(), getType(),
                        getParameters().get(incrementDeltaKey),
                        incrementDeltaKey, 1.0);
        if (newDelta <= 0.0) {
            throw new MegawidgetException(getIdentifier(), getType(),
                    incrementDelta, "must be positive number");
        }
        return newDelta;
    }

    @Override
    protected Double convertToNumber(int value) {
        return new Double(value);
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
        for (String identifier : getStateIdentifiers()) {
            RangeValidatorHelper<Double> helper = getRangeValidator(identifier);
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
}
