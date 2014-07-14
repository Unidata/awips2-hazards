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

import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Integer} instances, and are within boundaries.
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
public class BoundedIntegerValidator extends BoundedNumberValidator<Integer> {

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
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     */
    public BoundedIntegerValidator(Map<String, Object> parameters,
            String minimumValueKey, String maximumValueKey,
            String incrementDeltaKey, Integer lowest, Integer highest) {
        super(parameters, minimumValueKey, maximumValueKey, incrementDeltaKey,
                Integer.class, lowest, highest);
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
    protected BoundedIntegerValidator(BoundedIntegerValidator other) {
        super(other);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedIntegerValidator(this);
    }

    // Protected Methods

    @Override
    protected Integer setIncrementDelta(String incrementDeltaKey,
            Object incrementDelta) throws MegawidgetException {
        Integer newDelta = ConversionUtilities
                .getSpecifierIntegerObjectFromObject(getIdentifier(),
                        getType(), getParameters().get(incrementDeltaKey),
                        incrementDeltaKey, 1);
        if (newDelta < 1) {
            throw new MegawidgetException(getIdentifier(), getType(),
                    incrementDelta, "must be positive integer");
        }
        return newDelta;
    }
}
