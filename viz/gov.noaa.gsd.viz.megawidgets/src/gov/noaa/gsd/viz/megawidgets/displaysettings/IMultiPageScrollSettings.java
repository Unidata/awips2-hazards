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

import java.util.Map;

/**
 * Description: Interface describing the methods that must be implemented by a
 * display settings object that includes information about the scroll origins of
 * a multi-page megawidget, that is, a megawidget with potentially more than one
 * scrollable element. The generic parameter <code>P</code> provides the type of
 * the point used to represent a scroll origin.
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
public interface IMultiPageScrollSettings<P> extends IDisplaySettings {

    /**
     * Get the scroll origin for the specified page identifier.
     * 
     * @param identifier
     *            Page identifier.
     * @return Scroll origin.
     */
    public P getScrollOriginForPage(String identifier);

    /**
     * Get all of the scroll origins.
     * 
     * @return Map of page identifiers to their scroll origins; if
     *         <code>null</code>, there are no scrollable elements.
     */
    public Map<String, P> getScrollOriginsForPages();

    /**
     * Set the scroll origin for the specified page identifier.
     * 
     * @param identifier
     *            Page identifier.
     * @param scrollOrigin
     *            Scroll origin.
     */
    public void setScrollOriginForPage(String identifier, P scrollOrigin);
}
