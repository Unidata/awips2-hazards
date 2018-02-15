/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import java.util.List;
import java.util.Map;

import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.IView;

/**
 * Description: Interface that a delegate for the product staging view must
 * implement.
 * <p>
 * A product staging view must provide a way of displaying potential products
 * for issuance.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 06, 2014    4042    Chris.Golden Initial creation.
 * Feb 24, 2016   13929    Robert.Blum  Remove first part of staging dialog.
 * Mar 21, 2017   29996    Robert.Blum  Added refreshStagingMetadata().
 * Jan 17, 2018   33428    Chris.Golden Changed to work with new, more flexible
 *                                      toolbar contribution code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IProductStagingViewDelegate<I, C, E extends Enum<E>>
        extends IView<I, C, E>, IProductStagingView {

    // Public Methods

    /**
     * Show the product staging dialog.
     * 
     * @param productNames
     *            Names of the products for which widgets are to be shown to
     *            allow the changing of product-specific metadata.
     * @param megawidgetSpecifierManagersForProductNames
     *            Map of product names to megawidget specifier managers
     *            providing the specifiers for the megawidgets to be built for
     *            said products.
     * @param minimumVisibleTime
     *            Minimum visible time for any widgets displaying time
     *            graphically.
     * @param maximumVisibleTime
     *            Maximum visible time for any widgets displaying time
     *            graphically.
     */
    public void showStagingDialog(List<String> productNames,
            Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames,
            long minimumVisibleTime, long maximumVisibleTime);

    /**
     * Hide the dialog.
     */
    public void hide();

    /**
     * Refresh the megawidgets on the staging dialog and reset the correct
     * visible pages.
     * 
     * @param productName
     * @param megawidgetSpecifierManager
     * @param visiblePages
     */
    public void refreshStagingMetadata(String productName,
            MegawidgetSpecifierManager megawidgetSpecifierManager,
            Map<String, Map<String, Object>> visiblePages);

    /**
     * Get the megawidget manager for the specified product.
     * 
     * @param productName
     * @return
     */
    public MegawidgetManager getMegawidgetManager(String productName);
}
