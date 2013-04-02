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

/**
 * Interface describing the methods to be implemented by a megawidget specifier
 * that creates a menu-based megawidgets. Any subclasses of <code>
 * MegawidgetSpecifier</code> must implement this interface if they are to
 * create such megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2013            Chris.Golden      Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMenuSpecifier {

    // Public Static Constants

    /**
     * Megawidget show on parent menu parameter name; a megawidget may include a
     * boolean value associated with this name to indicate whether or not it
     * wishes to have its menu items made a part of the parent menu it is given
     * when created. If false, the items are instead placed on a submenu that
     * pulls out from the parent menu. If not specified, it is assumed to be
     * false.
     */
    public static final String MEGAWIDGET_ON_PARENT_MENU = "onParentMenu";

    /**
     * Megawidget show separator parameter name; a megawidget may include a
     * boolean value associated with this name to indicate whether or not it
     * wishes to have a separator inserted above its menu items or submenu. If
     * not specified, it is assumed to be false.
     */
    public static final String MEGAWIDGET_SHOW_SEPARATOR = "showSeparator";

    // Public Methods

    /**
     * Determine whether or not the menu items should be part of the parent
     * menu.
     * 
     * @return Flag indicating whether or not the menu items should be part of
     *         the parent menu. If <code>false</code>, they are to be shown on a
     *         separate submenu.
     */
    public boolean shouldBeOnParentMenu();

    /**
     * Determine whether or not a separator should be shown above this
     * megawidget's menu items or submenu.
     * 
     * @return Flag indicating whether or not a separator should be shown above
     *         this megawidget's menu items or submenu.
     */
    public boolean shouldShowSeparator();
}
