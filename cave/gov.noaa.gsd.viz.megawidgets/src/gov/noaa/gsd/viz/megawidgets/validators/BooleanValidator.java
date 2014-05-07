/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.validators;

import gov.noaa.gsd.viz.megawidgets.ConversionUtilities;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

/**
 * Description: Validator used to validate boolean state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BooleanValidator extends SingleStateValidator<Boolean> {

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     */
    public BooleanValidator() {
    }

    // Protected Constructors

    /**
     * Construct an instance that is a copy of another already-initialized
     * instance.
     * 
     * @param other
     *            Validator to be copied.
     * @throws IllegalArgumentException
     *             If <code>other</code> has not yet been initialized.
     */
    protected BooleanValidator(BooleanValidator other) {
        super(other);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BooleanValidator(this);
    }

    @Override
    public Boolean convertToStateValue(Object object)
            throws MegawidgetException {
        return ConversionUtilities.getStateBooleanObjectFromObject(
                getIdentifier(), getType(), object, Boolean.FALSE);
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {

        /*
         * No action.
         */
    }
}
