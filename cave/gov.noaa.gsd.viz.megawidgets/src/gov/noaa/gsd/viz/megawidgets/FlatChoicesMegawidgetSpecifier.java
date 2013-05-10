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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for megawidget specifiers that include a flat list of choices as
 * part of their state. Said choices are always associated with a single state
 * identifier, so the megawidget identifiers for these specifiers must not
 * consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 25, 2013   1277     Chris.Golden      Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class FlatChoicesMegawidgetSpecifier extends
        ChoicesMegawidgetSpecifier {

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
    public FlatChoicesMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
    }

    // Protected Methods

    @Override
    protected final String getChoicesDataStructureDescription() {
        return "list";
    }

    @Override
    protected final IllegalChoicesProblem evaluateChoicesMapLegality(
            Map<?, ?> map, int index) {
        return null;
    }

    @Override
    protected final boolean isNodeSubset(Object node1, Object node2) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final List<Object> createChoicesCopy(List<?> list) {

        // Create a new list, and copy each element into it from
        // the old list. If an element is a map, then make a
        // copy of the map instead of using the original.
        List<Object> listCopy = new ArrayList<Object>();
        for (Object item : list) {
            if (item instanceof Map) {
                listCopy.add(new HashMap<String, Object>(
                        (Map<String, Object>) item));
            } else {
                listCopy.add(item);
            }
        }
        return listCopy;
    }
}
