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

import gov.noaa.gsd.viz.megawidgets.IMegawidget;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Multi-page scrollable display settings. The generic parameter
 * <code>P</code> provides the type of the point used to represent a scroll
 * origin.
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
public class MultiPageScrollSettings<P> extends DisplaySettings implements
        IMultiPageScrollSettings<P> {

    // Private Variables

    /**
     * Map of page identifiers to their associated scroll origins. Any page
     * identifiers that have <code>null</code> as their values have scroll
     * origins of 0, 0.
     */
    private final Map<String, P> scrollOriginsForPageIdentifiers = new HashMap<String, P>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public MultiPageScrollSettings(Class<? extends IMegawidget> megawidgetClass) {
        super(megawidgetClass);
    }

    // Public Methods

    @Override
    public P getScrollOriginForPage(String identifier) {
        return scrollOriginsForPageIdentifiers.get(identifier);
    }

    @Override
    public Map<String, P> getScrollOriginsForPages() {
        return new HashMap<>(scrollOriginsForPageIdentifiers);
    }

    @Override
    public void setScrollOriginForPage(String identifier, P scrollOrigin) {
        scrollOriginsForPageIdentifiers.put(identifier, scrollOrigin);
    }
}
