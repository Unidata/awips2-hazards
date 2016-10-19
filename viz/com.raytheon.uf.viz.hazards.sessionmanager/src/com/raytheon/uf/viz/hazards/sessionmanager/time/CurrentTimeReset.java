/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.time;

import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Notification that indicates that the current CAVE time has been reset due to
 * the {@link SimulatedTime} being explicitly reset, or because it has been
 * frozen or unfrozen.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 13, 2016   21873    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class CurrentTimeReset extends CurrentTimeChanged {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the notification.
     * @param timeManager
     *            Time manager.
     */
    public CurrentTimeReset(IOriginator originator,
            ISessionTimeManager timeManager) {
        super(originator, timeManager);
    }
}
