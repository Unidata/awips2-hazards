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
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;

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
 * Jun 08, 2017   16373    Chris.Golden Removed product viewer selection dialog 
 *                                      usage, as the product view and presenter
 *                                      take care of this now.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
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
            Map<IHazardEventView, Map<IHazardEventView, Collection<String>>> areasForConflictingEventsForEvents);

    /**
     * Handle notification that the console was disposed of.
     */
    public void consoleDisposed();
}
