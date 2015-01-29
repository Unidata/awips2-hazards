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

import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Long} instances, are within fixed boundaries, and are in ascending
 * order for multiple-state {@link IStateful} and {@link IStatefulSpecifier}
 * objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * Jun 27, 2014   3512     Chris.Golden Made minimum interval parameter
 *                                      optional.
 * Jan 28, 2015   2331     Chris.Golden Made subclass of new class called
 *                                      BoundedMultiNumberValidator, which
 *                                      took much of this class's abilities
 *                                      and genericized it.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BoundedMultiLongValidator extends
        BoundedMultiNumberValidator<Long> {

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
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     */
    public BoundedMultiLongValidator(Map<String, Object> parameters,
            String minimumValuesKey, String maximumValuesKey,
            String minimumIntervalKey,
            boolean individualBoundsOnlyForFirstIdentifier, Long lowest,
            Long highest) throws MegawidgetSpecificationException {
        super(parameters, minimumValuesKey, maximumValuesKey,
                minimumIntervalKey, individualBoundsOnlyForFirstIdentifier,
                Long.class, lowest, highest);
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
    protected BoundedMultiLongValidator(BoundedMultiLongValidator other) {
        super(other);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedMultiLongValidator(this);
    }

    // Protected Methods

    @Override
    protected Long getSmallestAllowableInterval() {
        return 0L;
    }

    @Override
    protected Long add(Long first, Long second) {
        return first + second;
    }

    @Override
    protected Long subtract(Long minuend, Long subtrahend) {
        return minuend - subtrahend;
    }

    @Override
    protected Long multiply(Long first, Long second) {
        return first * second;
    }

    @Override
    protected Long divide(Long dividend, Long divisor) {
        if (divisor == 0L) {
            throw new ArithmeticException("divide by zero");
        }
        return dividend / divisor;
    }

    @Override
    protected Long convertToNumber(int value) {
        return new Long(value);
    }
}
