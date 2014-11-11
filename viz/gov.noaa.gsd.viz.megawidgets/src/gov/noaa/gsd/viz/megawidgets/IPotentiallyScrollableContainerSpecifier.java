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
 * Description: Interface describing the methods that must be implemented by a
 * container megawidget specifier that may be configured to have scrollable
 * client area(s). The <code>C</code> parameter indicates what type of
 * {@link ISpecifier} each child specifier must be.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 16, 2014    4818    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IPotentiallyScrollableContainerSpecifier<C extends ISpecifier>
        extends IContainerSpecifier<C> {

    // Public Static Constants

    /**
     * Scrollable parameter name; a megawidget may include a boolean associated
     * with this name to indicate whether or not the container's client area,
     * where child megawidgets reside, is to be given scrollbars when it is not
     * large enough to show its child megawidgets. If not specified, it does not
     * use scrollbars.
     */
    public static final String SCROLLABLE = "scrollable";

    // Public Methods

    /**
     * Determine whether or not the megawidget is to provide scrollbars when its
     * client area is too small to show its child megawidgets.
     * 
     * @return True if the megawidget is scrollable, false otherwise,.
     */
    public boolean isScrollable();
}
