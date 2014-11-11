/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.displaysettings;

/**
 * Description: Interface describing the methods that must be implemented by a
 * display settings object that includes information about the scroll origin of
 * a single-page megawidget, that is, a megawidget with one scrollable element.
 * The generic parameter <code>P</code> provides the type of the point used to
 * represent a scroll origin.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 20, 2014    4818    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISinglePageScrollSettings<P> extends IDisplaySettings {

    /**
     * Get the scroll origin.
     * 
     * @return Scroll origin.
     */
    public P getScrollOrigin();

    /**
     * Set the scroll origin.
     * 
     * @param scrollOrigin
     *            Scroll origin.
     */
    public void setScrollOrigin(P scrollOrigin);
}
