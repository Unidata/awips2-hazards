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
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see FractionSpinnerSpecifier
 */
public class FractionSpinnerMegawidget extends SpinnerMegawidget<Double> {

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
    protected int getSpinnerPrecision() {
        return ((BoundedFractionValidator) getStateValidator()).getPrecision();
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
}