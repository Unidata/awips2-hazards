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
 * Notification that will be sent out to notify all components that the
 * boundaries constraining the range of allowable values for the start and/or
 * end time of one or more events in the session have changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 02, 2015    2331    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionEventsTimeRangeBoundariesModified
        extends SessionEventsModified {

    // Private Variables

    /**
     * Identifiers of events that have had their time range boundaries changed.
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
    public SessionEventsTimeRangeBoundariesModified(
            ISessionEventManager eventManager, Set<String> eventIdentifiers,
            IOriginator originator) {
        super(eventManager, originator);
        this.eventIdentifiers = ImmutableSet.copyOf(eventIdentifiers);
    }

    /**
     * Get the identifiers of the events that have had their time range
     * boundaries modified. Note that the returned set is not modifiable.
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
        if ((modified instanceof SessionEventsTimeRangeBoundariesModified)
                && getOriginator()
                        .equals(((SessionEventsTimeRangeBoundariesModified) modified)
                                .getOriginator())) {
            HashSet<String> combinedEventIdentifiers = new HashSet<>(
                    getEventIdentifiers());
            combinedEventIdentifiers
                    .addAll(((SessionEventsTimeRangeBoundariesModified) modified)
                            .getEventIdentifiers());
            return IMergeable.Helper.getSuccessObjectCancellationResult(
                    new SessionEventsTimeRangeBoundariesModified(
                            getEventManager(), combinedEventIdentifiers,
                            getOriginator()));
        }
        return IMergeable.Helper.getFailureResult();
    }
}
