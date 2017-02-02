/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;

/**
 * Description: Interface describing the methods that must be implemented by
 * classes that are to act as console handlers, dealing with notifications from
 * the console about disposal, etc.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 15, 2016   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IConsoleHandler {

    /**
     * Show a warning to the user about conflicting hazards.
     * 
     * @param areasForConflictingEventsForEvents
     *            Map pairing hazard events with the sub-maps, each sub-map
     *            pairing any hazard events that conflict with the former with
     *            the names of any geometry areas that conflict.
     */
    public void showUserConflictingHazardsWarning(
            Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> areasForConflictingEventsForEvents);

    /**
     * Show the view product selection dialog to the user.
     * 
     * @param productData
     *            List of product data elements to be shown for selection
     *            purposes.
     */
    public void showUserProductViewerSelectionDialog(
            List<ProductData> productData);

    /**
     * Handle notification that the console was disposed of.
     */
    public void consoleDisposed();
}
