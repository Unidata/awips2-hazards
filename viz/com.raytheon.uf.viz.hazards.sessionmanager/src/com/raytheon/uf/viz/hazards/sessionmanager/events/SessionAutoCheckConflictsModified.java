/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Notification of a change to the state for the
 * "auto-check conflicts" toggle.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 12, 2014    2925    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionAutoCheckConflictsModified
        extends OriginatedSessionNotification {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the change.
     */
    public SessionAutoCheckConflictsModified(IOriginator originator) {
        super(originator);
    }

    // Public Methods

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}
