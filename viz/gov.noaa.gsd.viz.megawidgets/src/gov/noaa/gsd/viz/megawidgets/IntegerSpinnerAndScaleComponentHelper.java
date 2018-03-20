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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import gov.noaa.gsd.viz.widgets.MultiValueScale;

/**
 * Description: Helper class for handling some of the grunt work of creating and
 * configuring megawidgets containing both {@link Spinner} and
 * {@link MultiValueScale} components used to manipulate integer values. See the
 * superclass for usage instructions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 06, 2015    4123    Chris.Golden Initial creation.
 * Mar 20, 2018   48027    Chris.Golden Fixed bug in calculation of number
 *                                      of digits for a specified value.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class IntegerSpinnerAndScaleComponentHelper
        extends SpinnerAndScaleComponentHelper<Integer> {

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
    public IntegerSpinnerAndScaleComponentHelper(
            IRapidlyChangingStatefulSpecifier specifier,
            List<String> stateIdentifiers, Composite parent) {
        super(specifier, stateIdentifiers, parent);
    }

    // Protected Methods

    @Override
    protected int getDigitsForValue(Integer value) {
        return (value == 0 ? 1
                : ((int) Math.floor(Math.log10(Math.abs(value))))
                        + (value < 0 ? 1 : 0) + 1);
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
    protected long convertValueToScale(Integer value) {
        return value;
    }

    @Override
    protected Integer convertScaleToValue(long value) {
        return (int) value;
    }
}
