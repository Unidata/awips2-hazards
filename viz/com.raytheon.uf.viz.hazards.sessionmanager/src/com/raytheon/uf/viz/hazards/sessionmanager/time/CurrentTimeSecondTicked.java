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

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Notification that indicates that the current CAVE time has ticked forward to
 * a new second. Note that there is no guarantee that the second tick will
 * arrive exactly on the second boundary, but it should arrive soon after,
 * possibly several in rapid succession if the system fell behind.
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
public class CurrentTimeSecondTicked extends CurrentTimeChanged {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the notification.
     * @param timeManager
     *            Time manager.
     */
    public CurrentTimeSecondTicked(IOriginator originator,
            ISessionTimeManager timeManager) {
        super(originator, timeManager);
    }
}
