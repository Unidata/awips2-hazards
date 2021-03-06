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

/**
 * Description: Single-page scrollable display settings. The generic parameter
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
public class SinglePageScrollSettings<P> extends DisplaySettings implements
        ISinglePageScrollSettings<P> {

    // Private Variables

    /**
     * Scroll origin; if <code>null</code>, the origin is 0, 0.
     */
    private P scrollOrigin;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public SinglePageScrollSettings(Class<? extends IMegawidget> megawidgetClass) {
        super(megawidgetClass);
    }

    // Public Methods

    @Override
    public P getScrollOrigin() {
        return scrollOrigin;
    }

    @Override
    public void setScrollOrigin(P scrollOrigin) {
        this.scrollOrigin = scrollOrigin;
    }
}
