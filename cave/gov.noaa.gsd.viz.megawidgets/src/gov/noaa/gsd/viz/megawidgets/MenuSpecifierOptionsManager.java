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
 * Description: Manager of any options associated with megawidget specifiers
 * that implement @{link IMenuSpecifierj}. This class may be used by such
 * classes to handle the setting and getting of such options.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2013    2168    Chris.Golden      Initial creation.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MenuSpecifierOptionsManager {

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
     * Construct a standard instance to manage the control-related options of
     * the provided specifier.
     * 
     * @param specifier
     *            Specifier for which to manage the control-related options.
     * @param parameters
     *            Map containing the parameters to be used to construct the
     *            megawidget specifier, including mappings for the options
     *            needed for the control aspect of said specifier.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public MenuSpecifierOptionsManager(MegawidgetSpecifier specifier,
            Map<String, Object> parameters)
            throws MegawidgetSpecificationException {

        /*
         * Record the value of the on parent menu flag.
         */
        onParentMenu = ConversionUtilities.getSpecifierBooleanValueFromObject(
                specifier.getIdentifier(), specifier.getType(),
                parameters.get(IMenuSpecifier.MEGAWIDGET_ON_PARENT_MENU),
                IMenuSpecifier.MEGAWIDGET_ON_PARENT_MENU, false);

        /*
         * Record the value of the show separator flag.
         */
        showSeparator = ConversionUtilities.getSpecifierBooleanValueFromObject(
                specifier.getIdentifier(), specifier.getType(),
                parameters.get(IMenuSpecifier.MEGAWIDGET_SHOW_SEPARATOR),
                IMenuSpecifier.MEGAWIDGET_SHOW_SEPARATOR, false);
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
    public final boolean shouldShowSeparator() {
        return showSeparator;
    }
}
