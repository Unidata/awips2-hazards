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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Notification that will be sent out to notify all components that one or more
 * new events have been added to the session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013    1257    bsteffen     Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Altered to allow notification of
 *                                      more than one event being added, and
 *                                      implemented merge() method.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SessionEventsAdded extends SessionEventsModified {

    // Private Variables

    /**
     * Events that have been added.
     */
    private final List<IHazardEvent> events;

    // Public Constructors

    /**
     * Construct a standard instance indicating a single event was added.
     * 
     * @param eventManager
     *            Event manager.
     * @param event
     *            Event that is being added.
     * @param originator
     *            Originator of the addition.
     */
    public SessionEventsAdded(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            IHazardEvent event, IOriginator originator) {
        super(eventManager, originator);
        this.events = ImmutableList.of(event);
    }

    /**
     * Construct a standard instance indicating a multiple events were added.
     * 
     * @param eventManager
     *            Event manager.
     * @param events
     *            Events that are being added.
     * @param originator
     *            Originator of the addition.
     */
    public SessionEventsAdded(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            Collection<IHazardEvent> events, IOriginator originator) {
        super(eventManager, originator);
        this.events = ImmutableList.copyOf(events);
    }

    // Public Methods

    /**
     * Get the events that were added. Note that the returned list is not
     * modifiable.
     * 
     * @return Events that were added.
     */
    public List<IHazardEvent> getEvents() {
        return events;
    }

    @Override
    public MergeResult<? extends ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {

        /*
         * If the modified notification is of added events (with the same
         * originator), or the original notification is of removed events (with
         * any originator), attempt to merge them. Otherwise, no merge is
         * possible.
         */
        if ((modified instanceof SessionEventsAdded) && getOriginator()
                .equals(((SessionEventsAdded) modified).getOriginator())) {

            /*
             * Create a list via filtering the new notification's events list,
             * minus any with identifiers of events that this notification
             * already has.
             */
            List<IHazardEvent> newEvents = filterEventsToRemoveAnyWithIdentifiers(
                    ((SessionEventsAdded) modified).getEvents(),
                    getEventIdentifiers(getEvents()));

            /*
             * Create a list that is a concatenation of the two lists, old ones
             * first, and use that to build a replacement for this notification.
             */
            List<IHazardEvent> combinedEvents = new ArrayList<>(getEvents());
            combinedEvents.addAll(newEvents);
            return IMergeable.Helper.getSuccessObjectCancellationResult(
                    new SessionEventsAdded(getEventManager(), combinedEvents,
                            getOriginator()));

        } else if (original instanceof SessionEventsRemoved) {

            /*
             * Create a list of added events that is the same as the existing
             * notification's events list, minus any events that the new
             * notification indicates are being removed.
             */
            List<IHazardEvent> addedEvents = filterEventsToRemoveAnyWithIdentifiers(
                    getEvents(), getEventIdentifiers(
                            ((SessionEventsRemoved) original).getEvents()));

            /*
             * If the modified version of the notification indicates events are
             * being removed, create a list of removed events that is the same
             * as the new notification's events list, minus any events that the
             * existing notification indicates were being added (since those
             * have, as a result of the code immediately above, been removed
             * from the list of added events -- thus they don't need any
             * notification of their removal). Otherwise, just assume an empty
             * list, because the modified version is probably a null
             * notification.
             */
            List<IHazardEvent> removedEvents = (modified instanceof SessionEventsRemoved
                    ? filterEventsToRemoveAnyWithIdentifiers(
                            ((SessionEventsRemoved) modified).getEvents(),
                            getEventIdentifiers(getEvents()))
                    : Collections.<IHazardEvent> emptyList());

            /*
             * Return the appropriate result depending upon whether neither,
             * one, or both of the newly generated lists are empty.
             */
            if (addedEvents.isEmpty() && removedEvents.isEmpty()) {
                return IMergeable.Helper.getSuccessMutualCancellationResult();
            } else if (addedEvents.isEmpty()) {
                return IMergeable.Helper.getSuccessSubjectCancellationResult(
                        new SessionEventsRemoved(getEventManager(),
                                removedEvents, getOriginator()));
            } else if (removedEvents.isEmpty()) {
                return IMergeable.Helper.getSuccessObjectCancellationResult(
                        new SessionEventsAdded(getEventManager(), addedEvents,
                                getOriginator()));
            } else {
                return IMergeable.Helper.getSuccessBothReplacedResult(
                        new SessionEventsAdded(getEventManager(), addedEvents,
                                getOriginator()),
                        new SessionEventsRemoved(getEventManager(),
                                removedEvents, getOriginator()));
            }
        } else {
            return IMergeable.Helper.getFailureResult();
        }
    }
}
