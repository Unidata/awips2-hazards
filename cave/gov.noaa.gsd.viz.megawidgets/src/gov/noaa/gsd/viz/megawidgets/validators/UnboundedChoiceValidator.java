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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link List} instances holding unique {@link String} objects.
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
public class UnboundedChoiceValidator extends
        SingleStateValidator<List<String>> {

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     */
    public UnboundedChoiceValidator() {
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
    protected UnboundedChoiceValidator(UnboundedChoiceValidator other) {
        super(other);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new UnboundedChoiceValidator(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> convertToStateValue(Object object)
            throws MegawidgetException {

        /*
         * If no object has been provided, return an empty list.
         */
        if (object == null) {
            return Collections.emptyList();
        }

        /*
         * Ensure that the subset is of the correct collection type.
         */
        List<String> choices;
        try {
            choices = (List<String>) object;
        } catch (Exception e) {
            throw new MegawidgetException(getIdentifier(), getType(), object,
                    "must be list of choices");
        }

        /*
         * Ensure that the list contains no repeated choices, or any empty
         * strings.
         */
        Set<String> identifiers = new HashSet<>(choices.size());
        for (String choice : choices) {
            if ((choice == null) || choice.isEmpty()) {
                throw new MegawidgetException(getIdentifier(), getType(),
                        object, "choice cannot be null or empty string");
            }
            if (identifiers.contains(choice)) {
                throw new MegawidgetException(getIdentifier(), getType(),
                        object,
                        "choices list cannot contain duplicate elements");
            }
        }
        return choices;
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {

        /*
         * No action.
         */
    }
}
