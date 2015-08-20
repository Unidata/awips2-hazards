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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedMultiDoubleValidator;

import java.util.Map;

/**
 * Fraction range specifier. The fraction values are always associated with two
 * state identifiers, so the megawidget identifiers for these specifiers must
 * consist of two substrings separated by a colon.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 08, 2015    4123    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IntegerRangeMegawidget
 */
public class FractionRangeSpecifier extends RangeSpecifier<Double> {

    // Public Static Constants

    /**
     * Decimal precision parameter name; a megawidget may include a positive
     * integer ranging from 1 to 10 inclusive associated with this name. If it
     * does, this acts as the number of decimal places that are to follow the
     * decimal point in the spinners, and the precision of the fraction. If not
     * specified, it defaults to 1.
     */
    public static final String DECIMAL_PRECISION = "precision";

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
    public FractionRangeSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new BoundedMultiDoubleValidator(parameters,
                MINIMUM_ALLOWABLE_VALUE, MAXIMUM_ALLOWABLE_VALUE,
                MINIMUM_INTERVAL, PAGE_INCREMENT_DELTA, DECIMAL_PRECISION,
                false, -Double.MAX_VALUE, Double.MAX_VALUE));
    }
}