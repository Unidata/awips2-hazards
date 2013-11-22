/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Base class for megawidget specifiers that include a closed hierarchical list
 * of choices as part of their state. The choice hierarchy, as well as the
 * hierarchy of the values chosen, are in tree form, with each value being
 * either a leaf (having no children) or a branch (having one or more child
 * choices). The hierarchy may be arbitrarily deep.
 * <p>
 * The hierarchy of values chosen is always a subset of the choices hierarchy.
 * For any hierarchy of choices, the choice names that share the same direct
 * parent must be unique with respect to one another; this is also true for
 * names of the root choices with respect to one another. Thus, a choice name
 * may be identical to that of another choice at a different level of the
 * hierarchy, or at the same level but having a different parent.
 * <p>
 * The choices are always associated with a single state identifier, so the
 * megawidget identifiers for these specifiers must not consist of
 * colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2013            Chris.Golden      Initial creation.
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Minor cleanup.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalBoundedChoicesMegawidget
 */
public class HierarchicalBoundedChoicesMegawidgetSpecifier extends
        BoundedChoicesMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Choice children parameter name; each choice in the tree associated with
     * <code>MEGAWIDGET_VALUE_CHOICES</code> that is a map may contain a
     * reference to a list of other choices associated with this name. These
     * choices are the children of that choice.
     */
    public static final String CHOICE_CHILDREN = "children";

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public HierarchicalBoundedChoicesMegawidgetSpecifier(
            Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
    }

    // Protected Methods

    @Override
    protected final Set<Class<?>> getClassesOfState() {
        Set<Class<?>> classes = Sets.newHashSet();
        classes.add(List.class);
        return classes;
    }

    @Override
    protected final String getChoicesDataStructureDescription() {
        return "tree";
    }

    @Override
    protected final IllegalChoicesProblem evaluateChoicesMapLegality(
            String parameterName, Map<?, ?> map, int index) {

        // If the map has something other than a
        // list for a children entry, it is
        // illegal.
        Object children = map.get(CHOICE_CHILDREN);
        if ((children != null) && ((children instanceof List) == false)) {
            return new IllegalChoicesProblem(parameterName, "[" + index + "]",
                    CHOICE_CHILDREN, children, "must be list of children");
        }

        // Check the children lists of the tree for
        // legality, and to ensure that siblings
        // always have unique identifiers.
        if (children != null) {
            IllegalChoicesProblem eval = evaluateChoicesLegality(parameterName,
                    (List<?>) children);
            if (eval != NO_ILLEGAL_CHOICES_PROBLEM) {
                eval.addParentToBadElementLocation("[" + index + "]");
                return eval;
            }
        }
        return NO_ILLEGAL_CHOICES_PROBLEM;
    }

    @Override
    protected final boolean isNodeSubset(Object node1, Object node2) {

        // If the subset node has children and the superset does not, the
        // former is not a subset. If both have children, then check their
        // respective lists of children to ensure that the one is a subset
        // of the other.
        if ((node1 instanceof Map)
                && (((Map<?, ?>) node1).get(CHOICE_CHILDREN) != null)) {
            if (node2 instanceof String) {
                return false;
            }
            if (isSubset((List<?>) ((Map<?, ?>) node1).get(CHOICE_CHILDREN),
                    (List<?>) ((Map<?, ?>) node2).get(CHOICE_CHILDREN)) == false) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected final List<Object> createChoicesCopy(List<?> list) {

        // Create a new list, and copy each element into it from
        // the old list. If an element is a map, then make a
        // copy of the map instead of using the original, and
        // also copy the child list within that map.
        List<Object> listCopy = Lists.newArrayList();
        for (Object item : list) {
            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                Map<String, Object> mapCopy = Maps.newHashMap();
                for (Object key : map.keySet()) {
                    if (key.equals(HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_CHILDREN)) {
                        mapCopy.put((String) key,
                                createChoicesCopy((List<?>) map.get(key)));
                    } else {
                        mapCopy.put((String) key, map.get(key));
                    }
                }
                listCopy.add(mapCopy);
            } else {
                listCopy.add(item);
            }
        }
        return listCopy;
    }
}
