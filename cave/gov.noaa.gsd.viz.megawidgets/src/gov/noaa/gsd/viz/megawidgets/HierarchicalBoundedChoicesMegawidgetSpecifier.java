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

import gov.noaa.gsd.viz.megawidgets.validators.MultiHierarchicalChoiceValidatorHelper;

import java.util.Collection;
import java.util.Map;

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
 * Jan 28, 2014   2161     Chris.Golden      Changed to support use of collections
 *                                           instead of only lists for the state.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalBoundedChoicesMegawidget
 */
public class HierarchicalBoundedChoicesMegawidgetSpecifier extends
        BoundedChoicesMegawidgetSpecifier<Collection<Object>> {

    // Public Static Constants

    /**
     * Choice children parameter name; each choice in the tree associated with
     * {@link #MEGAWIDGET_VALUE_CHOICES} that is a map may contain a reference
     * to a list of other choices associated with this name. These choices are
     * the children of that choice.
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
        super(parameters, new MultiHierarchicalChoiceValidatorHelper(
                MEGAWIDGET_VALUE_CHOICES, CHOICE_NAME, CHOICE_IDENTIFIER,
                CHOICE_CHILDREN, false));
    }
}
