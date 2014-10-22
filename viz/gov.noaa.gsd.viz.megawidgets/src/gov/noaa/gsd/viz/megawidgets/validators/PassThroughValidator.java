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

import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

/**
 * Description: State validator that accepts any value without checking it.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 22, 2014    5050    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class PassThroughValidator extends SingleStateValidator<Object> {

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     */
    public PassThroughValidator() {

        /*
         * No action.
         */
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
    protected PassThroughValidator(PassThroughValidator other) {
        super(other);
    }

    // Public Methods

    @Override
    public Object convertToStateValue(Object object) throws MegawidgetException {
        return object;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new PassThroughValidator(this);
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {

        /*
         * No action.
         */
    }
}
