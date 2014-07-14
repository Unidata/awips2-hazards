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

import gov.noaa.gsd.viz.megawidgets.validators.UnboundedChoiceValidator;

import java.util.Map;

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
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
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
        super(parameters, new UnboundedChoiceValidator());
    }
}
