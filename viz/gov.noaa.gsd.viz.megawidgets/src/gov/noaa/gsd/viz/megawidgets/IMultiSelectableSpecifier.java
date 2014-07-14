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
 * Description: Interface describing the methods that a megawidget providing
 * multiple selection must implement.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013    2168    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMultiSelectableSpecifier extends IStatefulSpecifier,
        IControlSpecifier {

    // Public Static Constants

    /**
     * Megawidget include-select-all/select-none-buttons parameter name; a
     * megawidget may include a boolean value associated with this name to
     * indicate whether or not it wishes to have All and None buttons included
     * to allow the user to easily select or deselect all the items in the check
     * list. If not specified, it is assumed to be true.
     */
    public static final String MEGAWIDGET_SHOW_ALL_NONE_BUTTONS = "showAllNoneButtons";

    // Public Methods

    /**
     * Determine whether or not the All and None buttons should be shown.
     * 
     * @return Flag indicating whether or not the All and None buttons should be
     *         shown.
     */
    public boolean shouldShowAllNoneButtons();
}
