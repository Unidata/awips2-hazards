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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedChoiceValidatorHelper;

import java.util.Map;

/**
 * Base class for megawidget specifiers that include a flat closed list of
 * choices as part of their state. Said choices are always associated with a
 * single state identifier, so the megawidget identifiers for these specifiers
 * must not consist of colon-separated substrings. The generic parameter
 * <code>T</code> indicates the type of the state.
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
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class FlatBoundedChoicesMegawidgetSpecifier<T> extends
        BoundedChoicesMegawidgetSpecifier<T> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @param stateValidatorHelper
     *            State validator helper.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public FlatBoundedChoicesMegawidgetSpecifier(
            Map<String, Object> parameters,
            BoundedChoiceValidatorHelper<T> stateValidatorHelper)
            throws MegawidgetSpecificationException {
        super(parameters, stateValidatorHelper);
    }
}
