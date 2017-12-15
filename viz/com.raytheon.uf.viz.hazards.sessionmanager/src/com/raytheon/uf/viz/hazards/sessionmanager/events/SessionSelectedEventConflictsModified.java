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

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Notification of a change to the map of selected event
 * identifiers to the events with which they conflict.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 12, 2014    2925    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionSelectedEventConflictsModified
        extends SessionEventsModified {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param originator
     *            Originator of the change.
     */
    public SessionSelectedEventConflictsModified(
            ISessionEventManager eventManager, IOriginator originator) {
        super(eventManager, originator);
    }

    // Public Methods

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}
