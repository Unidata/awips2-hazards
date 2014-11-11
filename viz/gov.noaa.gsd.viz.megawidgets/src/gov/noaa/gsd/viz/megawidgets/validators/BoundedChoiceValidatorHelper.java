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

import java.util.List;

/**
 * Description: Validator helper used to ensure that potential choices for
 * instances of {@link BoundedChoiceValidator} are valid. The generic parameter
 * <code>T</code> is the type of the selected choice(s) for the validator using
 * this helper.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * Jun 24, 2014   4023     Chris.Golden Added ability to create a pruned
 *                                      subset.
 * Jul 02, 2014   3512     Chris.Golden Change to allow subclassing.
 * Oct 10, 2014   4042     Chris.Golden Extracted most of functionality into
 *                                      new ChoiceListValidator superclass.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class BoundedChoiceValidatorHelper<T> extends
        ChoiceListValidator {

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
    public BoundedChoiceValidatorHelper(String choicesKey,
            String elementNameKey, String elementIdentifierKey) {
        super(choicesKey, elementNameKey, elementIdentifierKey);
    }

    // Public Methods

    /**
     * Convert the specified object into a valid choice(s) subset.
     * 
     * @param available
     *            List of available choices; <code>subset</code> must be a
     *            subset of this.
     * @param subset
     *            Object holding the subset.
     * @return Subset of choices.
     * @throws MegawidgetException
     *             If the selected object is not a valid selected choice(s)
     *             subset.
     */
    public final T convertToSubset(List<?> available, Object subset)
            throws MegawidgetException {
        if (subset == null) {
            return createDefaultSubset(available);
        }
        try {
            T newSubset = convertToSubsetStructure(subset);
            if (isSubset(newSubset, available) == false) {
                throw new InvalidChoicesException(null, null, subset,
                        "not a subset of available choices");
            }
            return newSubset;
        } catch (InvalidChoicesException e) {
            throw e.toStateException(getIdentifier(), getType());
        }
    }

    /**
     * Get a copy of the specified subset that has been pruned of any choices
     * not found in the specified superset.
     * 
     * @param subset
     *            Subset that is to be pruned to not include anything that is
     *            not part of <code>superset</code>.
     * @param superset
     *            Superset to which to prune <code>subset</code>.
     * @return Properly pruned subset.
     */
    public abstract T getPrunedSubset(T subset, List<?> superset);

    // Protected Methods

    /**
     * Create a default subset for the specified available choices.
     * 
     * @param available
     *            Available list of choices.
     * @return Default subset.
     */
    protected abstract T createDefaultSubset(List<?> available);

    /**
     * Convert the specified object to have the structure of a proper subset.
     * 
     * @param subset
     *            Object to be converted to the appropriate structure.
     * @return Properly structured subset.
     * @throws InvalidChoicesException
     *             If the object does not contain a valid subset.
     */
    protected abstract T convertToSubsetStructure(Object subset)
            throws InvalidChoicesException;

    /**
     * Determine whether the specified subset is a subset of the specified
     * available choices.
     * 
     * @param subset
     *            Subset be checked to see if it is a subset of
     *            <code>available</code>.
     * @param available
     *            Available list of choices.
     * @return True if the subset is indeed a subset of the available choices,
     *         false otherwise.
     */
    protected abstract boolean isSubset(T subset, List<?> available);
}
