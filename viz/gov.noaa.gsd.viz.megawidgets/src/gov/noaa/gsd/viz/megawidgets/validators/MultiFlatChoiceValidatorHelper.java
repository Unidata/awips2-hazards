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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Description: Validator helper used to ensure that potential choices for
 * instances of {@link BoundedChoiceValidator} that allow a flat set of choices
 * are valid.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 24, 2014   2925     Chris.Golden Initial creation.
 * Jun 24, 2014   4023     Chris.Golden Added ability to create a pruned
 *                                      subset.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MultiFlatChoiceValidatorHelper extends
        MultiChoiceValidatorHelper<String> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param choicesKey
     *            Key within the specifier parameters or mutable properties for
     *            the choices list.
     * @param elementNameKey
     *            Key within a choice element for the element name parameter.
     * @param elementIdentifierKey
     *            Key within a choice element for the element identifier
     *            parameter.
     * @param orderedSubset
     *            Flag indicating whether or not subsets are to be ordered,
     *            meaning they should be a {@link List}, not merely a
     *            {@link Collection}.
     */
    public MultiFlatChoiceValidatorHelper(String choicesKey,
            String elementNameKey, String elementIdentifierKey,
            boolean orderedSubset) {
        super(choicesKey, elementNameKey, elementIdentifierKey, orderedSubset);
    }

    // Protected Methods

    @Override
    protected final Collection<String> createDefaultSubset(List<?> available) {
        return (isOrderedSubset() ? Collections.<String> emptyList()
                : Collections.<String> emptySet());
    }

    @Override
    protected String getPrunedSubsetNode(String subNode, Object superNode) {
        return subNode;
    }
}
