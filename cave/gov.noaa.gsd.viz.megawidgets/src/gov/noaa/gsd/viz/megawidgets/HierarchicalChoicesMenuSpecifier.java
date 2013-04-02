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
     * Flag indicating whether or not the menu items should be part of the
     * parent menu, instead of on a separate submenu.
     */
    private final boolean onParentMenu;

    /**
     * Flag indicating whether or not a separator should be showing above the
     * menu items or submenu.
     */
    private final boolean showSeparator;

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

        // Record the value of the on parent menu flag.
        onParentMenu = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_ON_PARENT_MENU),
                MEGAWIDGET_ON_PARENT_MENU, false);

        // Record the value of the show separator flag.
        showSeparator = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_SHOW_SEPARATOR),
                MEGAWIDGET_SHOW_SEPARATOR, false);
    }

    // Public Methods

    /**
     * Determine whether or not the menu items should be part of the parent
     * menu.
     * 
     * @return Flag indicating whether or not the menu items should be part of
     *         the parent menu. If <code>false</code>, they are to be shown on a
     *         separate submenu.
     */
    @Override
    public final boolean shouldBeOnParentMenu() {
        return onParentMenu;
    }

    /**
     * Determine whether or not a separator should be shown above this
     * megawidget's menu items or submenu.
     * 
     * @return Flag indicating whether or not a separator should be shown above
     *         this megawidget's menu items or submenu.
     */
    @Override
    public final boolean shouldShowSeparator() {
        return showSeparator;
    }
}
