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
 * Checkboxes menu megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2013            Chris.Golden      Initial creation
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Changed to use options manager to
 *                                           avoid code duplication.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckBoxesMenuMegawidget
 */
public class CheckBoxesMenuSpecifier extends FlatChoicesMegawidgetSpecifier
        implements IMenuSpecifier {

    // Private Variables

    /**
     * Menu options manager.
     */
    private final MenuSpecifierOptionsManager optionsManager;

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
    public CheckBoxesMenuSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
        optionsManager = new MenuSpecifierOptionsManager(this, parameters);
    }

    // Public Methods

    @Override
    public final boolean shouldBeOnParentMenu() {
        return optionsManager.shouldBeOnParentMenu();
    }

    @Override
    public final boolean shouldShowSeparator() {
        return optionsManager.shouldShowSeparator();
    }
}
