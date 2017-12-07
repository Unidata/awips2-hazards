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
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Base class for notifications that indicate that the current CAVE time has
 * changed, either because the {@link SimulatedTime} has been explicitly set or
 * reset, or because it has been frozen or unfrozen, or simply because a new
 * minute in has been reached (if not frozen) as simulated time ticks away.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 30, 2015    2331    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class CurrentTimeChanged extends OriginatedSessionNotification {

    // Private Variables

    /**
     * Time manager.
     */
    private final ISessionTimeManager timeManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the notification.
     * @param timeManager
     *            Time manager.
     */
    public CurrentTimeChanged(IOriginator originator,
            ISessionTimeManager timeManager) {
        super(originator);
        this.timeManager = timeManager;
    }

    // Public Methods

    /**
     * Get the time manager.
     * 
     * @return Time manager.
     */
    public ISessionTimeManager getTimeManager() {
        return timeManager;
    }

    // Public Methods

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}
