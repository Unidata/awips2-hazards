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

import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Integer} instances, are within fixed boundaries, and are in ascending
 * order for multiple-state {@link IStateful} and {@link IStatefulSpecifier}
 * objects.
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
public class BoundedMultiIntegerValidator extends
        BoundedMultiNumberValidator<Integer> {

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
    public BoundedMultiIntegerValidator(Map<String, Object> parameters,
            String minimumValuesKey, String maximumValuesKey,
            String minimumIntervalKey, String incrementDeltaKey,
            boolean individualBoundsOnlyForFirstIdentifier, Integer lowest,
            Integer highest) throws MegawidgetSpecificationException {
        super(parameters, minimumValuesKey, maximumValuesKey,
                minimumIntervalKey, incrementDeltaKey,
                individualBoundsOnlyForFirstIdentifier, Integer.class, lowest,
                highest);
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
    protected BoundedMultiIntegerValidator(BoundedMultiIntegerValidator other) {
        super(other);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedMultiIntegerValidator(this);
    }

    @Override
    public Integer add(Integer first, Integer second) {
        return first + second;
    }

    @Override
    public Integer subtract(Integer minuend, Integer subtrahend) {
        return minuend - subtrahend;
    }

    @Override
    public Integer multiply(Integer first, Integer second) {
        return first * second;
    }

    @Override
    public Integer divide(Integer dividend, Integer divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("divide by zero");
        }
        return dividend / divisor;
    }

    // Protected Methods

    @Override
    protected Integer getSmallestAllowableInterval() {
        return 0;
    }

    @Override
    protected Integer getIncrementDelta(String incrementDeltaKey,
            Object incrementDelta) throws MegawidgetException {
        Integer newDelta = ConversionUtilities
                .getSpecifierIntegerObjectFromObject(getIdentifier(),
                        getType(), getParameters().get(incrementDeltaKey),
                        incrementDeltaKey, 1);
        if (newDelta < 1) {
            throw new MegawidgetException(getIdentifier(), getType(),
                    incrementDelta, "must be positive number");
        }
        return newDelta;
    }

    @Override
    protected Integer convertToNumber(int value) {
        return new Integer(value);
    }
}
