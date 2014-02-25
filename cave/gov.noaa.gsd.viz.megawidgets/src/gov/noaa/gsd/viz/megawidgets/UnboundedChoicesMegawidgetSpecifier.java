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

import com.google.common.collect.Sets;

/**
 * Base class for megawidget specifiers that include open sets of choices as
 * part of their state. Since the set of choices is open, arbitrary choices may
 * be added by the user to the list of choices at any time. The list of current
 * choices is always associated with a single state identifier, so the
 * megawidget identifiers for these specifiers must not consist of
 * colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2013   2336     Chris.Golden      Initial creation.
 * Jan 28, 2014   2161     Chris.Golden      Minor fix to Javadoc.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class UnboundedChoicesMegawidgetSpecifier extends
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
    public UnboundedChoicesMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
    }

    // Protected Methods

    @SuppressWarnings("unchecked")
    @Override
    protected final Set<Class<?>> getClassesOfState() {
        return Sets.newHashSet(List.class, String.class);
    }

    @Override
    protected final IllegalChoicesProblem getIllegalChoicesProblemForIdentifier(
            String parameterName, Map<?, ?> node, Exception exception, int index) {
        return new IllegalChoicesProblem(parameterName, "[" + index + "]",
                CHOICE_NAME, node.get(CHOICE_NAME), "must be string");
    }

    @Override
    protected final String getIdentifierOfNode(Object node) {
        if (node instanceof String) {
            return (String) node;
        }
        return (String) ((Map<?, ?>) node).get(CHOICE_NAME);
    }
}
