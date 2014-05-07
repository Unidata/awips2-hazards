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

import java.util.List;

/**
 * Description: Validator helper used to ensure that potential choices for
 * instances of {@link BoundedChoiceValidator} are valid.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 24, 2014   2925     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SingleChoiceValidatorHelper extends
        BoundedChoiceValidatorHelper<String> {

    // Public Constructors

    /**
     * Construct a standard instance for a {@link BoundedChoiceValidator}.
     * 
     * @param choicesKey
     *            Key within the specifier parameters or mutable properties for
     *            the choices list.
     * @param elementNameKey
     *            Key within a choice element for the element name parameter.
     * @param elementIdentifierKey
     *            Key within a choice element for the element identifier
     *            parameter.
     */
    public SingleChoiceValidatorHelper(String choicesKey,
            String elementNameKey, String elementIdentifierKey) {
        super(choicesKey, elementNameKey, elementIdentifierKey);
    }

    // Protected Methods

    @Override
    protected String createDefaultSubset(List<?> available) {
        return getIdentifierOfNode(available.get(0));
    }

    @Override
    protected String convertToSubsetStructure(Object subset)
            throws InvalidChoicesException {
        if (subset instanceof String) {
            return (String) subset;
        }
        throw new InvalidChoicesException(null, null, subset,
                "must be a single choice");
    }

    @Override
    protected boolean isSubset(String subset, List<?> available) {
        for (Object node : available) {
            if (subset.equals(getIdentifierOfNode(node))) {
                return true;
            }
        }
        return false;
    }
}
