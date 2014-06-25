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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: Validator helper used to ensure that potential choices for
 * instances of {@link BoundedChoiceValidator} that allow hierarchical choices
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
public class MultiHierarchicalChoiceValidatorHelper extends
        MultiChoiceValidatorHelper<Object> {

    // Private Variables

    /**
     * Choice element children key.
     */
    private final String elementChildrenKey;

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
     * @param elementChildrenKey
     *            Key within a choice element for the element children
     *            parameter.
     * @param orderedSubset
     *            Flag indicating whether or not subsets are to be ordered,
     *            meaning they should be a {@link List}, not merely a
     *            {@link Collection}.
     */
    public MultiHierarchicalChoiceValidatorHelper(String choicesKey,
            String elementNameKey, String elementIdentifierKey,
            String elementChildrenKey, boolean orderedSubset) {
        super(choicesKey, elementNameKey, elementIdentifierKey, orderedSubset);
        this.elementChildrenKey = elementChildrenKey;
    }

    /**
     * Get the key for the choice element children.
     * 
     * @return Key for the choice element children.
     */
    public final String getElementChildrenKey() {
        return elementChildrenKey;
    }

    // Protected Methods

    @Override
    protected final Collection<Object> createDefaultSubset(List<?> available) {
        return (isOrderedSubset() ? Collections.emptyList() : Collections
                .emptySet());
    }

    @Override
    protected Map<String, ?> deepCopyIfContentMutable(Map<String, ?> map) {
        List<?> children = (List<?>) map.get(elementChildrenKey);
        if (children != null) {
            Map<String, Object> newMap = new HashMap<>(map);
            newMap.put(elementChildrenKey, convertToUnmodifiable(children));
            return newMap;
        }
        return map;
    }

    @Override
    protected void validateChoicesMap(Map<?, ?> map, int index,
            boolean nestedLists) throws InvalidChoicesException {
        Object children = map.get(elementChildrenKey);
        if (children != null) {

            /*
             * Ensure the children entry is a collection (or a list, if
             * required).
             */
            if (nestedLists && (children instanceof List == false)) {
                throw new InvalidChoicesException("[" + index + "]",
                        elementChildrenKey, children,
                        "must be list of children");
            } else if ((nestedLists == false)
                    && (children instanceof Collection == false)) {
                throw new InvalidChoicesException("[" + index + "]",
                        elementChildrenKey, children,
                        "must be set or list of children");
            }

            /*
             * Validate the children themselves.
             */
            try {
                validateChoices((Collection<?>) children, nestedLists);
            } catch (InvalidChoicesException e) {
                e.prependParentToBadElementLocation("[" + index + "]");
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean isNodeSubset(Object subNode, Object superNode) {

        /*
         * If the subset node has children and the superset does not, the former
         * is not a subset. If both have children, then check their respective
         * lists of children to ensure that the one is a subset of the other.
         */
        if ((subNode instanceof Map)
                && (((Map<?, ?>) subNode).get(elementChildrenKey) != null)) {
            if (superNode instanceof String) {
                return false;
            }
            if (isSubset(
                    (Collection<Object>) ((Map<?, ?>) subNode)
                            .get(elementChildrenKey),
                    (List<?>) ((Map<?, ?>) superNode).get(elementChildrenKey)) == false) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object getPrunedSubsetNode(Object subNode, Object superNode) {

        /*
         * If either the superset node or the subset node is a string
         * identifier, then the pruned subset cannot have children, so return
         * the identifier. The same is true if either the superset or subset
         * node has no children.
         */
        if ((superNode instanceof String) || (subNode instanceof String)) {
            return getIdentifierOfNode(superNode);
        }
        Map<?, ?> subNodeMap = (Map<?, ?>) subNode;
        Collection<Object> subNodeChildren = (Collection<Object>) subNodeMap
                .get(elementChildrenKey);
        if ((subNodeChildren == null) || subNodeChildren.isEmpty()) {
            return getIdentifierOfNode(superNode);
        }
        List<?> superNodeChildren = (List<?>) ((Map<?, ?>) superNode)
                .get(elementChildrenKey);
        if ((superNodeChildren == null) || superNodeChildren.isEmpty()) {
            return getIdentifierOfNode(superNode);
        }

        /*
         * Get the pruned child list; if it is not empty, return a map holding
         * the name, identifier and the pruned child list as values. Otherwise,
         * just return the identifier.
         */
        subNodeChildren = getPrunedSubset(subNodeChildren, superNodeChildren);
        if (subNodeChildren.isEmpty() == false) {
            Map<String, Object> newSubNode = new HashMap<>();
            if (subNodeMap.containsKey(getElementNameKey())) {
                newSubNode.put(getElementNameKey(),
                        subNodeMap.get(getElementNameKey()));
            }
            if (subNodeMap.containsKey(getElementIdentifierKey())) {
                newSubNode.put(getElementIdentifierKey(),
                        subNodeMap.get(getElementIdentifierKey()));
            }
            newSubNode.put(elementChildrenKey, subNodeChildren);
            return newSubNode;
        }
        return getIdentifierOfNode(superNode);
    }
}
