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

import gov.noaa.gsd.viz.megawidgets.validators.StateValidator;

import java.util.Map;

/**
 * Base class for megawidget specifiers that include sets of choices (whether
 * closed or open) as part of their state. Said choices are always associated
 * with a single state identifier, so the megawidget identifiers for these
 * specifiers must not consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Sep 25, 2013   2168     Chris.Golden      Added support for subclasses that
 *                                           include detail child megawidgets as
 *                                           part of their choices.
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
public abstract class ChoicesMegawidgetSpecifier extends
        StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Choice name parameter name; each choice in an array of choices that is a
     * map must contain a reference to a string associated with this name. The
     * string serves to label and to uniquely identify the choice; thus, each
     * name must be unique in the set of all choice names.
     */
    public static final String CHOICE_NAME = "displayString";

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @param stateValidator
     *            State validator.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public ChoicesMegawidgetSpecifier(Map<String, Object> parameters,
            StateValidator stateValidator)
            throws MegawidgetSpecificationException {
        super(parameters, stateValidator);
    }
}
