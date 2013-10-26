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

import org.eclipse.swt.widgets.Composite;

/**
 * Integer spinner megawidget, allowing the manipulation of an integer.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 22, 2013   2168     Chris.Golden      Changed to extend the new
 *                                           SpinnerMegawidget superclass.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IntegerSpinnerSpecifier
 */
public class IntegerSpinnerMegawidget extends SpinnerMegawidget<Integer> {

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected IntegerSpinnerMegawidget(IntegerSpinnerSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, parent, paramMap);
    }

    // Protected Methods

    @Override
    protected void ensureValueRangeRepresentable(Integer minimum,
            Integer maximum) throws MegawidgetPropertyException {

        // No action; any integer range is always representable.
    }

    @Override
    protected int getSpinnerPrecision() {
        return 0;
    }

    @Override
    protected int getDigitsForValue(Integer value) {
        return ((int) Math.floor(Math.log10(Math.abs(value))))
                + (value < 0 ? 1 : 0) + 1;
    }

    @Override
    protected int convertValueToSpinner(Integer value) {
        return value;
    }

    @Override
    protected Integer convertSpinnerToValue(int value) {
        return value;
    }

    @Override
    protected Integer getPropertyIncrementDeltaObjectFromObject(Object object,
            String name) throws MegawidgetPropertyException {
        Integer incrementDelta = getPropertyIntegerObjectFromObject(object,
                SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA, null);
        if (incrementDelta < 1) {
            IntegerSpinnerSpecifier specifier = getSpecifier();
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA,
                    specifier.getType(), object, "must be positive integer");
        }
        return incrementDelta;
    }
}