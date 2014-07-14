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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Description: Validator helper used to ensure that potential choices for
 * instances of {@link BoundedChoiceValidator} that allow multiple choices are
 * valid.
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
public abstract class MultiChoiceValidatorHelper<E> extends
        BoundedChoiceValidatorHelper<Collection<E>> {

    // Private Variables

    /**
     * Flag indicating whether or not subsets are to be ordered, meaning they
     * should be a {@link List}, not merely a {@link Collection}.
     */
    private final boolean orderedSubset;

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
     * @param orderedSubset
     *            Flag indicating whether or not subsets are to be ordered,
     *            meaning they should be a {@link List}, not merely a
     *            {@link Collection}.
     */
    public MultiChoiceValidatorHelper(String choicesKey, String elementNameKey,
            String elementIdentifierKey, boolean orderedSubset) {
        super(choicesKey, elementNameKey, elementIdentifierKey);
        this.orderedSubset = orderedSubset;
    }

    // Public Methods

    @Override
    public final Collection<E> getPrunedSubset(Collection<E> subset,
            List<?> superset) {

        /*
         * Create the proper type of collection to be returned.
         */
        Collection<E> prunedSubset = (orderedSubset ? new ArrayList<E>()
                : new HashSet<E>());

        /*
         * For each node in the subset, find the equivalent node in the
         * available list, and ensure that the superset one has at least all the
         * nodes of the subset one.
         */
        for (E subNode : subset) {

            /*
             * Find the superset node equivalent to this subset node; if it is
             * found, include a copy of this subset node in the pruned subset.
             */
            String identifier = getIdentifierOfNode(subNode);
            Object superNode = null;
            for (Object node : superset) {
                if (identifier.equals(getIdentifierOfNode(node))) {
                    superNode = node;
                    break;
                }
            }
            if (superNode != null) {
                prunedSubset.add(getPrunedSubsetNode(subNode, superNode));
            }
        }

        /*
         * Return the pruned subset.
         */
        return prunedSubset;
    }

    // Protected Methods

    /**
     * Determine whether or not subsets should be ordered.
     * 
     * @return True if subsets should be ordered, false otherwise.
     */
    protected final boolean isOrderedSubset() {
        return orderedSubset;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<E> convertToSubsetStructure(Object subset)
            throws InvalidChoicesException {

        /*
         * Ensure that the subset is of the correct collection type. If it is a
         * string, simply place it in a collection of the appropriate type;
         * otherwise, assume it is a collection of that type.
         */
        Collection<?> structuredSubset;
        if (subset instanceof String) {
            structuredSubset = (orderedSubset ? Lists.newArrayList(subset)
                    : Sets.newHashSet(subset));
        } else {
            try {
                if (orderedSubset) {
                    structuredSubset = (List<?>) subset;
                } else {
                    structuredSubset = (Collection<?>) subset;
                }
            } catch (Exception e) {
                throw new InvalidChoicesException(null, null, subset,
                        "must be " + (orderedSubset ? "list" : "set or list")
                                + " of choices");
            }
        }

        /*
         * Validate the subset and return it.
         */
        validateChoices(structuredSubset, orderedSubset);
        return (Collection<E>) structuredSubset;
    }

    @Override
    protected boolean isSubset(Collection<E> subset, List<?> available) {

        /*
         * If the subset is null, it is indeed a subset.
         */
        if (subset == null) {
            return true;
        }

        /*
         * For each node in the subset, find the equivalent node in the
         * available list, and ensure that the superset one has at least all the
         * nodes of the subset one.
         */
        for (Object subNode : subset) {

            /*
             * Find the superset node equivalent to this subset node; if not
             * found, it is not a subset.
             */
            String identifier = getIdentifierOfNode(subNode);
            Object superNode = null;
            for (Object node : available) {
                if (identifier.equals(getIdentifierOfNode(node))) {
                    superNode = node;
                    break;
                }
            }
            if (superNode == null) {
                return false;
            }

            /*
             * Ensure that subclass-specific details allow the first node to be
             * a subset of the second.
             */
            if (isNodeSubset(subNode, superNode) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine whether or not the subset node is a subset of the specified
     * superset node. It is assumed that the name and/or identifier of the first
     * node has been found to be the same as that of the second when this method
     * is called. Subclasses must override this method if additional checks must
     * be performed to determine whether one is a subset of the other.
     * 
     * @param subNode
     *            Node to be checked to see if it is a subset of
     *            <code>superNode</code>. This must be either a {@link String}
     *            identifier or a {@link Map}, with the latter holding the usual
     *            elements for a choice element map.
     * @param superNode
     *            Node to be checked to see if it is a superset of
     *            <code>subNode</code>. This must be either a {@link String}
     *            identifier or a {@link Map}, with the latter holding the usual
     *            elements for a choice element map.
     * @return True if the first node is a subset of the second, false
     *         otherwise.
     */
    protected boolean isNodeSubset(Object subNode, Object superNode) {
        return true;
    }

    /**
     * Get a copy of the specified subset node that has been pruned of anything
     * not found in the specified superset node. It may be assumed that the
     * subset node and the superset node have already been confirmed to have the
     * same identifier.
     * 
     * @param subNode
     *            Subset node that is to be pruned to not include anything that
     *            is not part of <code>supersetNode</code>.
     * @param superNode
     *            Superset node to which to prune <code>subsetNode</code>.
     * @return Properly pruned subset.
     */
    protected abstract E getPrunedSubsetNode(E subNode, Object superNode);
}
