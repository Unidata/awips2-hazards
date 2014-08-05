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

import gov.noaa.gsd.viz.megawidgets.validators.MultiFlatChoiceValidatorHelper;

import java.util.Collection;
import java.util.Map;

/**
 * Checkboxes megawidget specifier. Each checkbox may have zero or more detail
 * fields associated with it, each of the latter being itself a megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013    1277    Chris.Golden      Added support for mutable properties.
 * Sep 25, 2013    2168    Chris.Golden      Added support for optional detail
 *                                           fields next to the choice buttons.
 * Nov 04, 2013    2336    Chris.Golden      Implemented newly required method
 *                                           specified in abstract superclass.
 * Jan 28, 2014    2161    Chris.Golden      Changed to support use of collections
 *                                           instead of only lists for the state.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jul 23, 2014    4122    Chris.Golden      Changed to work with new parameter
 *                                           of superclass's constructor.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckBoxesMegawidget
 */
public class CheckBoxesSpecifier extends
        FlatChoicesWithDetailMegawidgetSpecifier<Collection<String>> {

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
    public CheckBoxesSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters,
                new MultiFlatChoiceValidatorHelper(MEGAWIDGET_VALUE_CHOICES,
                        CHOICE_NAME, CHOICE_IDENTIFIER, false), false);
    }
}
