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

import java.util.Map;

/**
 * Radio buttons megawidget specifier. Each radio button may have zero or more
 * detail fields associated with it, each of the latter being itself a
 * megawidget.
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see RadioButtonsMegawidget
 */
public class RadioButtonsSpecifier extends
        FlatChoicesWithDetailMegawidgetSpecifier {

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
    public RadioButtonsSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
    }
}
