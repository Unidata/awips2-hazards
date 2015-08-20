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
 * Integer range megawidget, allowing the manipulation of lower and upper
 * boundaries consisting of integer values.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 08, 2015    4123    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IntegerRangeSpecifier
 */
public class IntegerRangeMegawidget extends RangeMegawidget<Integer> {

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
    protected IntegerRangeMegawidget(IntegerRangeSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, parent, new IntegerSpinnerAndScaleComponentHelper(
                specifier, specifier.getStateIdentifiers(), parent), paramMap);
    }

    // Protected Methods

    @Override
    protected int getPrecision() {
        return 0;
    }
}