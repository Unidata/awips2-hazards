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

import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

/**
 * Class to be used as the basis of validator classes. Such a class is used to
 * validate potential values by {@link IStateful} and {@link IStatefulSpecifier}
 * instances.
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
public abstract class StateValidator {

    // Private Variables

    /**
     * Type of the megawidget for which validation is being performed.
     */
    private String type;

    // Protected Constructors

    /**
     * Construct an uninitialized instance.
     */
    protected StateValidator() {
    }

    /**
     * Construct an instance that is a copy of another already-initialized
     * instance.
     * 
     * @param other
     *            Validator to be copied.
     * @throws IllegalArgumentException
     *             If <code>other</code> has not yet been initialized.
     */
    protected StateValidator(StateValidator other) {
        if (other.type == null) {
            throw new IllegalArgumentException(
                    "cannot copy uninitialized validator");
        }
        this.type = other.type;
    }

    // Public Methods

    /**
     * Get a copy of this state validator.
     * 
     * @return Copy of this state validator.
     */
    public abstract <V extends StateValidator> V copyOf();

    /**
     * Get the type of the megawidget for which validation is being performed.
     * 
     * @return Type.
     */
    public final String getType() {
        return type;
    }

    // Protected Methods

    /**
     * Initialize the type. This method should only be called once.
     * 
     * @param type
     *            Type to be used; must not be <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>type</code> is <code>null</code>.
     * @throws IllegalStateException
     *             If this method has already been called on this object.
     */
    protected final void initializeType(String type) {
        if (this.type != null) {
            throw new IllegalStateException("cannot reinitialize");
        } else if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.type = type;
    }

    /**
     * Perform subclass-specific initialization. This should only be called
     * after the rest of initialization for any superclasses has been performed.
     * 
     * @throws MegawidgetSpecificationException
     *             If the initialization fails due to bad megawidget
     *             specification.
     */
    protected abstract void doInitialize()
            throws MegawidgetSpecificationException;
}
