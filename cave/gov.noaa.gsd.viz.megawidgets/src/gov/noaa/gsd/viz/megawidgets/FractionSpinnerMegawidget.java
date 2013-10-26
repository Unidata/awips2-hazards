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
 * Fraction spinner megawidget, allowing the manipulation of a double value.
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
 * @see FractionSpinnerSpecifier
 */
public class FractionSpinnerMegawidget extends SpinnerMegawidget<Double> {

    // Protected Static Methods

    /**
     * Ensure that the minimum and maximum values, combined with the precision
     * required, are representable by an SWT spinner widget (since the latter
     * actually uses an integer, not a double, to represent floating-point
     * numbers).
     * 
     * @param minimum
     *            Minimum possible value.
     * @param maximum
     *            Maximum possible value.
     * @param precision
     *            Precision after the decimal place.
     * @throws MegawidgetException
     *             If the values are not within representable bounds. Note that
     *             the <code>identifier</code>, <code>type</code>, and
     *             <code>badValue</code> parameters of the exception will be
     *             <code>null</code>.
     */
    protected final static void ensureParametersWithinRepresentableBounds(
            double minimum, double maximum, int precision)
            throws MegawidgetException {
        double precisionMultiplier = Math.pow(10, precision);
        double minAllowable = Integer.MIN_VALUE / precisionMultiplier;
        double maxAllowable = Integer.MAX_VALUE / precisionMultiplier;
        if ((minimum < minAllowable) || (minimum > maxAllowable)
                || (maximum < minAllowable) || (maximum > maxAllowable)) {
            throw new MegawidgetException(null, null, null, "combination of "
                    + FractionSpinnerSpecifier.MEGAWIDGET_MIN_VALUE + ", "
                    + FractionSpinnerSpecifier.MEGAWIDGET_MAX_VALUE + ", and "
                    + FractionSpinnerSpecifier.MEGAWIDGET_DECIMAL_PRECISION
                    + " results in out of bounds range");
        }
    }

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
    protected FractionSpinnerMegawidget(FractionSpinnerSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, parent, paramMap);
    }

    // Protected Methods

    @Override
    protected void ensureValueRangeRepresentable(Double minimum, Double maximum)
            throws MegawidgetPropertyException {
        try {
            ensureParametersWithinRepresentableBounds(minimum, maximum,
                    getSpinnerPrecision());
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(getSpecifier()
                    .getIdentifier(), null, getSpecifier().getType(), null,
                    e.getMessage(), e.getCause());
        }
    }

    @Override
    protected int getSpinnerPrecision() {
        return ((FractionSpinnerSpecifier) getSpecifier())
                .getDecimalPrecision();
    }

    @Override
    protected int getDigitsForValue(Double value) {
        return ((int) Math.floor(Math.log10(Math.round(Math.abs(value)))))
                + (value < 0 ? 1 : 0) + getSpinnerPrecision() + 2;
    }

    @Override
    protected int convertValueToSpinner(Double value) {
        return (int) Math.round(value * Math.pow(10, getSpinnerPrecision()));
    }

    @Override
    protected Double convertSpinnerToValue(int value) {
        return (value) / Math.pow(10, getSpinnerPrecision());
    }

    @Override
    protected Double getPropertyIncrementDeltaObjectFromObject(Object object,
            String name) throws MegawidgetPropertyException {
        Double incrementDelta = getPropertyDoubleObjectFromObject(object,
                SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA, null);
        try {
            ((FractionSpinnerSpecifier) getSpecifier())
                    .ensureIncrementDeltaNotTooSmall(incrementDelta,
                            getSpinnerPrecision());
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(),
                    SpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA, e.getType(),
                    object, e.getMessage(), e.getCause());
        }
        return incrementDelta;
    }
}