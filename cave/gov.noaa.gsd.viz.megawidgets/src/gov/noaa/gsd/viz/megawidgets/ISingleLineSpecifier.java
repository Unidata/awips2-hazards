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
 * components that fit within a single line must implement.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013    2168    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISingleLineSpecifier extends IControlSpecifier {

    // Public Static Constants

    /**
     * Expand to fill horizontal space parameter name; a megawidget may include
     * a boolean associated with this name to indicate whether or not the
     * container megawidget should expand to fill any available horizontal space
     * within its parent. If not specified, the megawidget is not expanded
     * horizontally.
     */
    public static final String EXPAND_HORIZONTALLY = "expandHorizontally";

    // Public Methods

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * horizontal space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         horizontally.
     */
    public boolean isHorizontalExpander();
}
