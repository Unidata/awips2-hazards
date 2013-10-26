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
 * Hierarchical megawidget menu specifier, used to specify a megawidget that
 * allows the selection of multiple values in a hierarchy of choices, using a
 * menu with checkable menu items and/or nested submenus.
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
 * Mar 27, 2013            Chris.Golden Initial creation.
 * Oct 21, 2013    2168    Chris.Golden Changed to use options manager to
 *                                      avoid code duplication.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalChoicesMenuMegawidget
 */
public class HierarchicalChoicesMenuSpecifier extends
        HierarchicalChoicesMegawidgetSpecifier implements IMenuSpecifier {

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
    public HierarchicalChoicesMenuSpecifier(Map<String, Object> parameters)
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
