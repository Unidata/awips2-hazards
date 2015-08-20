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

import org.eclipse.swt.widgets.Composite;

/**
 * Fraction range megawidget, allowing the manipulation of lower and upper
 * boundaries consisting of double values.
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
 * @see FractionRangeSpecifier
 */
public class FractionRangeMegawidget extends RangeMegawidget<Double> {

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
    protected FractionRangeMegawidget(FractionRangeSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, parent, new DoubleSpinnerAndScaleComponentHelper(
                specifier, specifier.getStateIdentifiers(), parent), paramMap);
    }

    // Protected Methods

    @Override
    protected int getPrecision() {
        return ((BoundedMultiDoubleValidator) getStateValidator())
                .getPrecision();
    }
}