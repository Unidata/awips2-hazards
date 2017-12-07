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

import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Notification that will be sent out to notify all components that the visible
 * time range has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 19, 2014    2925    Chris.Golden Initial creation.
 * May 10, 2014    2925    Chris.Golden Added originator.
 * Nov 18, 2014    4124    Chris.Golden Changed to work with revamped time
 *                                      manager.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisibleTimeRangeChanged extends OriginatedSessionNotification {

    // Private Variables

    /**
     * Time manager.
     */
    private final ISessionTimeManager timeManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param timeManager
     *            Time manager.
     * @param originator
     *            Originator of the change.
     */
    public VisibleTimeRangeChanged(ISessionTimeManager timeManager,
            IOriginator originator) {
        super(originator);
        this.timeManager = timeManager;
    }

    // Public Methods

    /**
     * Get the visible time range.
     * 
     * @return Visible time range.
     */
    public TimeRange getVisibleTimeRange() {
        return timeManager.getVisibleTimeRange();
    }

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}
