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

import java.util.Map;

/**
 * Description: Validator used to validate arbitrary text state.
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
public class TextValidator extends SingleStateValidator<String> {

    // Private Variables

    /**
     * Map of parameters used to create the specifier. This may be
     * <code>null</code> if the validator has been constructed as already
     * initialized.
     */
    private final Map<String, Object> parameters;

    /**
     * Key in {@link #parameters} for the maximum characters parameter.
     */
    private final String maxCharactersKey;

    /**
     * Maximum number of characters; if 0, there is no limit.
     */
    private int maxCharacters;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param maxCharactersKey
     *            Key in <code>parameters</code> for the maximum character count
     *            parameter.
     */
    public TextValidator(Map<String, Object> parameters, String maxCharactersKey) {
        this.parameters = parameters;
        this.maxCharactersKey = maxCharactersKey;
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
    protected TextValidator(TextValidator other) {
        super(other);
        parameters = null;
        maxCharactersKey = other.maxCharactersKey;
        maxCharacters = other.maxCharacters;
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new TextValidator(this);
    }

    /**
     * Get the maximum number of characters.
     * 
     * @return Maximum number of characters; if 0, there is no limit.
     */
    public final int getMaxCharacters() {
        return maxCharacters;
    }

    @Override
    public String convertToStateValue(Object object) throws MegawidgetException {
        if (object == null) {
            return "";
        }
        String value = object.toString();
        if ((maxCharacters > 0) && (value.length() > maxCharacters)) {
            throw new MegawidgetException(getIdentifier(), getType(), object,
                    "string exceeds length limit of " + maxCharacters);
        }
        return value;
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {

        /*
         * Ensure that the maximum length, if present, is acceptable.
         */
        maxCharacters = ConversionUtilities.getSpecifierIntegerValueFromObject(
                getIdentifier(), getType(), parameters.get(maxCharactersKey),
                maxCharactersKey, 0);
        if (maxCharacters < 0) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), maxCharactersKey, maxCharacters,
                    "must be non-negative integer");
        }
    }
}
