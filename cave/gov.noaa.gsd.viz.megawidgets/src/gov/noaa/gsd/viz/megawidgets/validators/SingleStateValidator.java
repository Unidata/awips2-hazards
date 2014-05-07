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
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

/**
 * Class to be used as the basis of single state validator classes. Such a class
 * is used to validate potential values by {@link IStateful} and
 * {@link IStatefulSpecifier} instances that have a single state identifier and
 * value. The generic parameter <code>T</code> provides the type of the state
 * value.
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
public abstract class SingleStateValidator<T> extends StateValidator {

    // Private Variables

    /**
     * State identifier.
     */
    private String identifier;

    // Protected Constructors

    /**
     * Construct an uninitialized instance.
     */
    protected SingleStateValidator() {
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
    protected SingleStateValidator(SingleStateValidator<T> other) {
        super(other);
        identifier = other.identifier;
    }

    // Public Methods

    /**
     * Initialize the validator for a specifier. This method should only be
     * called if the object has not already been initialized, that is, if it was
     * created using a public constructor.
     * 
     * @param type
     *            Type of the megawidget; must not be <code>null</code>.
     * @param identifier
     *            State identifier for the megawidget; must not be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If either parameter is <code>null</code>.
     * @throws IllegalStateException
     *             If this validator has already been initialized.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specification is incorrect.
     */
    public final void initialize(String type, String identifier)
            throws MegawidgetSpecificationException {
        initializeType(type);
        if (identifier == null) {
            throw new IllegalArgumentException("identifier cannot be null");
        }
        this.identifier = identifier;
        doInitialize();
    }

    /**
     * Get the state identifier.
     * 
     * @return State identifier.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Convert the specified object to a valid state value if possible,
     * providing a default value if no object is specified.
     * 
     * @param object
     *            Object to be converted. If <code>null</code, a default value
     *            will be returned.
     * @return Valid state value.
     * @throws MegawidgetException
     *             If the object is not a valid state value.
     */
    public abstract T convertToStateValue(Object object)
            throws MegawidgetException;
}
