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
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Number} instances, and are within boundaries.
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
public abstract class BoundedNumberValidator<T extends Number & Comparable<T>>
        extends BoundedComparableValidator<T> {

    // Private Variables

    /**
     * Key in map from {@link #getParameters()} for the increment delta
     * parameter.
     */
    private final String incrementDeltaKey;

    /**
     * Increment delta.
     */
    private T incrementDelta;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param minimumValueKey
     *            Key in <code>parameters</code> for the minimum value
     *            parameter.
     * @param maximumValueKey
     *            Key in <code>parameters</code> for the maximum value
     *            parameter.
     * @param incrementDeltaKey
     *            Key in <code>parameters</code> for the increment delta
     *            parameter.
     * @param comparableClass
     *            Type of state.
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     */
    public BoundedNumberValidator(Map<String, Object> parameters,
            String minimumValueKey, String maximumValueKey,
            String incrementDeltaKey, Class<T> comparableClass, T lowest,
            T highest) {
        super(parameters, minimumValueKey, maximumValueKey, comparableClass,
                lowest, highest);
        this.incrementDeltaKey = incrementDeltaKey;
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
    protected BoundedNumberValidator(BoundedNumberValidator<T> other) {
        super(other);
        incrementDeltaKey = other.incrementDeltaKey;
        incrementDelta = other.incrementDelta;
    }

    // Public Methods

    /**
     * Get the increment delta.
     * 
     * @return increment delta.
     */
    public final T getIncrementDelta() {
        return incrementDelta;
    }

    /**
     * Set the increment delta to that specified.
     * 
     * @param incrementDelta
     *            Object to be used as the new increment delta.
     * @throws MegawidgetPropertyException
     *             If the new value is not valid.
     */
    public void setIncrementDelta(Object incrementDelta)
            throws MegawidgetPropertyException {
        try {
            this.incrementDelta = setIncrementDelta(incrementDeltaKey,
                    incrementDelta);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(incrementDeltaKey, e);
        }
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {
        super.doInitialize();
        try {
            incrementDelta = setIncrementDelta(incrementDeltaKey,
                    getParameters().get(incrementDeltaKey));
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(incrementDeltaKey, e);
        }
    }

    /**
     * Get the increment delta from the specified object.
     * 
     * @param incrementDeltaKey
     *            Key for the increment delta mutable property.
     * @param incrementDelta
     *            New increment delta.
     * @throws MegawidgetSpecificationException
     *             If the specified increment delta is invalid.
     */
    protected abstract T setIncrementDelta(String incrementDeltaKey,
            Object incrementDelta) throws MegawidgetException;
}
