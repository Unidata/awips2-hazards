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

import gov.noaa.gsd.viz.widgets.MultiValueScale;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

/**
 * Description: Helper class for handling some of the grunt work of creating and
 * configuring megawidgets containing both {@link Spinner} and
 * {@link MultiValueScale} components used to manipulate double values. See the
 * superclass for usage instructions.
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
public class DoubleSpinnerAndScaleComponentHelper extends
        SpinnerAndScaleComponentHelper<Double> {

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier for the megawidget to be built.
     * @param stateIdentifiers
     *            List of state identifiers for the megawidget to be built.
     * @param parent
     *            Parent of the megawidget.
     */
    public DoubleSpinnerAndScaleComponentHelper(
            IRapidlyChangingStatefulSpecifier specifier,
            List<String> stateIdentifiers, Composite parent) {
        super(specifier, stateIdentifiers, parent);
    }

    // Protected Methods

    @Override
    protected int getDigitsForValue(Double value) {
        return ((int) Math.floor(Math.log10(Math.round(Math.abs(value)))))
                + (value < 0 ? 1 : 0) + getHolder().getPrecision() + 2;
    }

    @Override
    protected int convertValueToSpinner(Double value) {
        return (int) convertValueToScale(value);
    }

    @Override
    protected Double convertSpinnerToValue(int value) {
        return convertScaleToValue(value);
    }

    @Override
    protected long convertValueToScale(Double value) {
        return Math.round(value * Math.pow(10, getHolder().getPrecision()));
    }

    @Override
    protected Double convertScaleToValue(long value) {
        return (value) / Math.pow(10, getHolder().getPrecision());
    }
}
