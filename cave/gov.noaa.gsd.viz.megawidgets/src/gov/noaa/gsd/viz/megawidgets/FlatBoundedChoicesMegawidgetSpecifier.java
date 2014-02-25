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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for megawidget specifiers that include a flat closed list of
 * choices as part of their state. Said choices are always associated with a
 * single state identifier, so the megawidget identifiers for these specifiers
 * must not consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 25, 2013   1277     Chris.Golden      Initial creation
 * Oct 23, 2013   2168     Chris.Golden      Minor cleanup.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Jan 28, 2014   2161     Chris.Golden      Changed to support use of collections
 *                                           instead of only lists for the state.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class FlatBoundedChoicesMegawidgetSpecifier extends
        BoundedChoicesMegawidgetSpecifier {

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
    public FlatBoundedChoicesMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
    }

    // Protected Methods

    @Override
    protected final String getChoicesDataStructureDescription() {
        return "list";
    }

    @Override
    protected IllegalChoicesProblem evaluateChoicesMapLegality(
            String parameterName, Map<?, ?> map, int index) {
        return NO_ILLEGAL_CHOICES_PROBLEM;
    }

    @Override
    protected final boolean isNodeSubset(Object node1, Object node2) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object> createChoicesCopy(Collection<?> list) {

        // Create a new list, and copy each element into it from
        // the old list. If an element is a map, then make a
        // copy of the map instead of using the original.
        List<Object> listCopy = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                listCopy.add(new HashMap<>((Map<String, Object>) item));
            } else {
                listCopy.add(item);
            }
        }
        return listCopy;
    }
}
