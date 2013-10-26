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

import java.util.Map;

/**
 * Fraction spinner specifier, allowing the viewing and manipulation of a
 * fraction with a fixed number of decimal places. The fraction value is always
 * associated with a single state identifier, so the megawidget identifiers for
 * these specifiers must not consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013   2168     Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see FractionSpinnerMegawidget
 */
public class FractionSpinnerSpecifier extends SpinnerSpecifier<Double> {

    // Public Static Constants

    /**
     * Decimal precision parameter name; a megawidget may include a positive
     * integer ranging from 1 to 10 inclusive associated with this name. If it
     * does, this acts as the number of decimal places that are to follow the
     * decimal point in the spinner, and the precision of the fraction. If not
     * specified, it defaults to 1.
     */
    public static final String MEGAWIDGET_DECIMAL_PRECISION = "precision";

    // Private Static Constants

    /**
     * Error message indicating that the increment delta is too small.
     */
    private static final String INCREMENT_DELTA_TOO_SMALL_ERROR_MESSAGE = "must "
            + "be positive number no smaller than 10 raised to the power of P, "
            + "where P is precision";

    // Private Variables

    /**
     * Precision, that is, number of digits following the decimal point.
     */
    private final int precision;

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
    public FractionSpinnerSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, Double.class, null, null);

        // If the precision is present, ensure that it is a positive integer
        // between 1 and 10.
        precision = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_DECIMAL_PRECISION),
                MEGAWIDGET_DECIMAL_PRECISION, 1);
        if ((precision < 1) || (precision > 10)) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_DECIMAL_PRECISION, precision,
                    "must be positive number between 1 and 10 inclusive");
        }

        // Ensure that the range specified is representable by a megawidget
        // this specifier could construct.
        try {
            FractionSpinnerMegawidget
                    .ensureParametersWithinRepresentableBounds(
                            getMinimumValue(), getMaximumValue(), precision);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), null, null, e.getMessage(), e.getCause());
        }

        // Ensure that the increment delta is not too small.
        try {
            ensureIncrementDeltaNotTooSmall(getIncrementDelta(), precision);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), MEGAWIDGET_INCREMENT_DELTA, e.getBadValue(),
                    e.getMessage(), e.getCause());
        }
    }

    // Public Methods

    /**
     * Get the decimal precision.
     * 
     * @return Decimal precision.
     */
    public final int getDecimalPrecision() {
        return precision;
    }

    // Protected Methods

    @Override
    protected Double getSpecifierIncrementDeltaObjectFromObject(Object object,
            String paramName) throws MegawidgetSpecificationException {
        Double incrementDelta = getSpecifierDoubleObjectFromObject(object,
                paramName, 0.0);
        if (incrementDelta < 0.0) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), paramName, incrementDelta,
                    INCREMENT_DELTA_TOO_SMALL_ERROR_MESSAGE);
        }
        return incrementDelta;
    }

    /**
     * Ensure that the specified increment delta is not too small to make a
     * difference when incrementing or decrementing, given the specified
     * precision.
     * 
     * @param incrementDelta
     *            Increment delta.
     * @param precision
     *            Precision after the decimal place.
     * @throws MegawidgetException
     *             If the increment delta is too small to make a difference when
     *             incrementing or decrementing.
     */
    protected final void ensureIncrementDeltaNotTooSmall(double incrementDelta,
            int precision) throws MegawidgetException {
        if (incrementDelta < Math.pow(10, -precision)) {
            throw new MegawidgetException(
                    getIdentifier(),
                    getType(),
                    incrementDelta,
                    FractionSpinnerSpecifier.INCREMENT_DELTA_TOO_SMALL_ERROR_MESSAGE);
        }
    }
}
