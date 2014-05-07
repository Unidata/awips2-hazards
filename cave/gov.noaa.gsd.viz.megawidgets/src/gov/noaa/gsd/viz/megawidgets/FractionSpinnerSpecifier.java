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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedFractionValidator;

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
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
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
        super(parameters, new BoundedFractionValidator(parameters,
                MEGAWIDGET_MIN_VALUE, MEGAWIDGET_MAX_VALUE,
                MEGAWIDGET_INCREMENT_DELTA, MEGAWIDGET_DECIMAL_PRECISION,
                -Double.MAX_VALUE, Double.MAX_VALUE));
    }

    // Public Methods

    /**
     * Get the decimal precision.
     * 
     * @return Decimal precision.
     */
    public final int getDecimalPrecision() {
        return ((BoundedFractionValidator) getStateValidator()).getPrecision();
    }
}
