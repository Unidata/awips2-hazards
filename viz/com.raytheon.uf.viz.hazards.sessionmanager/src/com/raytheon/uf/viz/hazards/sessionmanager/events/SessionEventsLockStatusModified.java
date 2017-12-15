/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Notification that will be sent out to notify all components that
 * the lock status for a particular hazard event has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 12, 2016   21504    Robert.Blum  Initial creation.
 * Apr 05, 2017   32733    Robert.Blum  Changed to notify of multiple event lock
 *                                      status changes.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public class SessionEventsLockStatusModified extends SessionEventsModified {

    // Private Variables

    /**
     * Identifiers of events that have had their lock statuses changed.
     */
    private final Set<String> eventIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param eventIdentifiers
     *            Identifiers of events that have had their time range
     *            boundaries modified.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventsLockStatusModified(ISessionEventManager eventManager,
            Set<String> eventIdentifiers, IOriginator originator) {
        super(eventManager, originator);
        this.eventIdentifiers = ImmutableSet.copyOf(eventIdentifiers);
    }

    // Public Methods

    /**
     * Get the identifiers of the events that have had their lock statuses
     * modified. Note that the returned set is not modifiable.
     * 
     * @return Identifiers of the events.
     */
    public final Set<String> getEventIdentifiers() {
        return eventIdentifiers;
    }

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {

        /*
         * If the new notification has the same originator as this one, and is
         * of the same type, merge the two together by combining their event
         * identifier sets; otherwise, the merge has failed.
         */
        if ((modified instanceof SessionEventsLockStatusModified)
                && getOriginator()
                        .equals(((SessionEventsLockStatusModified) modified)
                                .getOriginator())) {
            HashSet<String> combinedEventIdentifiers = new HashSet<>(
                    getEventIdentifiers());
            combinedEventIdentifiers
                    .addAll(((SessionEventsLockStatusModified) modified)
                            .getEventIdentifiers());
            return IMergeable.Helper.getSuccessObjectCancellationResult(
                    new SessionEventsLockStatusModified(getEventManager(),
                            combinedEventIdentifiers, getOriginator()));
        }
        return IMergeable.Helper.getFailureResult();
    }
}
