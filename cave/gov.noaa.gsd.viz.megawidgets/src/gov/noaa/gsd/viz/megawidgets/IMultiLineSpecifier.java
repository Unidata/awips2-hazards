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
 * multiple lines must implement.
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
public interface IMultiLineSpecifier extends IControlSpecifier {

    // Public Static Constants

    /**
     * Megawidget number of visible lines parameter name; a megawidget may
     * include a positive integer associated with this name to indicate that it
     * wishes to have this number of rows visible at once. If not specified, the
     * number of visible lines is assumed to be 6.
     */
    public static final String MEGAWIDGET_VISIBLE_LINES = "lines";

    // Public Methods

    /**
     * Get the number of visible lines.
     * 
     * @return Number of visible lines.
     */
    public int getNumVisibleLines();
}
