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

import gov.noaa.gsd.viz.megawidgets.displaysettings.IDisplaySettings;
import gov.noaa.gsd.viz.megawidgets.displaysettings.SinglePageScrollSettings;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * Description: Single-page container megawidget, a megawidget that itself
 * contains other megawidgets within a single panel or page.
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
public abstract class SinglePageMegawidget extends ContainerMegawidget
        implements IResizer {

    // Private Variables

    /**
     * Scrolled composite being used to display the child megawidgets; if
     * <code>null</code>, there is no scrolled composite.
     */
    private ScrolledComposite scrolledComposite;

    /**
     * Display settings.
     */
    private final SinglePageScrollSettings<Point> displaySettings = new SinglePageScrollSettings<>(
            getClass());

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     */
    public SinglePageMegawidget(SinglePageMegawidgetSpecifier specifier) {
        super(specifier);
    }

    // Public Methods

    @Override
    public IDisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDisplaySettings(IDisplaySettings displaySettings) {
        if ((displaySettings.getMegawidgetClass() == getClass())
                && (displaySettings instanceof SinglePageScrollSettings)
                && (scrolledComposite != null)) {
            final Point scrollOrigin = ((SinglePageScrollSettings<Point>) displaySettings)
                    .getScrollOrigin();
            if (scrollOrigin != null) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (scrolledComposite.isDisposed() == false) {
                            scrolledComposite.setOrigin(scrollOrigin);
                            SinglePageMegawidget.this.displaySettings
                                    .setScrollOrigin(scrollOrigin);
                        }
                    }
                });
            }
        }
    }

    // Protected Methods

    /**
     * Set the scrolled composite.
     * 
     * @param scrolledComposite
     *            Scrolled composite being used to scroll the child megawidget's
     *            panel.
     */
    protected final void setScrolledComposite(
            ScrolledComposite scrolledComposite) {
        this.scrolledComposite = scrolledComposite;
    }

    /**
     * Get the single-page scroll settings.
     * 
     * @return Single-page scroll settings.
     */
    protected final SinglePageScrollSettings<Point> getSinglePageScrollSettings() {
        return displaySettings;
    }
}
