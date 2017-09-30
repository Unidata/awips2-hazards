/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Notification that will be sent out to notify all components that
 * the history associated with a particular hazard event has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 29, 2016   15556    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionEventHistoryModified extends AbstractSessionEventModified {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param event
     *            Event that was changed.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventHistoryModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ObservedHazardEvent event, IOriginator originator) {
        super(eventManager, event, originator);
    }

    // Public Methods

    @Override
    public MergeResult<ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}
