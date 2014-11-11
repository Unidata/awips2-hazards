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

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.List;
import java.util.Map;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IProductStagingViewDelegate<C, E extends Enum<E>> extends
        IView<C, E>, IProductStagingView {

    // Public Methods

    /**
     * Show the product staging dialog in its first step configuration.
     * 
     * @param productNames
     *            Names of the products for which widgets are to be shown to
     *            allow the selection of hazard events to be associated with
     *            said products.
     * @param possibleEventIdsForProductNames
     *            Map of product names to lists of hazard events that may be
     *            associated with said products by the user.
     * @param possibleEventDescriptionsForProductNames
     *            Map of product names to lists of descriptions of hazard events
     *            that may be associated with said products by the user. For
     *            each such list, the descriptions within it are associated with
     *            the event identifiers at the same indices within the list for
     *            the corresponding product found in
     *            <code>possibleEventIdsForProductNames</code>.
     * @param selectedEventIdsForProductNames
     *            Map of product names to lists of hazard events that should
     *            start out as associated with said products when the widgets
     *            allowing the changing of selection are first displayed.
     */
    public void showFirstStep(List<String> productNames,
            Map<String, List<String>> possibleEventIdsForProductNames,
            Map<String, List<String>> possibleEventDescriptionsForProductNames,
            Map<String, List<String>> selectedEventIdsForProductNames);

    /**
     * Show the product staging dialog in its second step configuration.
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
     * @param firstStepSkipped
     *            Flag indicating whether or not the first step was skipped.
     */
    public void showSecondStep(
            List<String> productNames,
            Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames,
            long minimumVisibleTime, long maximumVisibleTime,
            boolean firstStepSkipped);

    /**
     * Hide the dialog.
     */
    public void hide();
}
