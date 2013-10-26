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
 * Integer spinner specifier. The integer value is always associated with a
 * single state identifier, so the megawidget identifiers for these specifiers
 * must not consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Changed to extend new SpinnerSpecifier
 *                                           class.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IntegerSpinnerMegawidget
 */
public class IntegerSpinnerSpecifier extends SpinnerSpecifier<Integer> {

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
    public IntegerSpinnerSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, Integer.class, null, null);
    }

    // Protected Methods

    @Override
    protected Integer getSpecifierIncrementDeltaObjectFromObject(Object object,
            String paramName) throws MegawidgetSpecificationException {
        Integer incrementDelta = getSpecifierIntegerObjectFromObject(object,
                paramName, 1);
        if (incrementDelta < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), paramName, incrementDelta,
                    "must be positive integer");
        }
        return incrementDelta;
    }
}
