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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * Base class for notifications to be sent out to notify all components that the
 * set of events in the session has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013    1257    bsteffen     Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Added helper methods for subclasses.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SessionEventsModified extends OriginatedSessionNotification {

    // Private Variables

    /**
     * Event manager.
     */
    private final ISessionEventManager eventManager;

    // Public Methods

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventsModified(ISessionEventManager eventManager,
            IOriginator originator) {
        super(originator);
        this.eventManager = eventManager;
    }

    // Public Methods

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }

    /**
     * Get the event manager.
     * 
     * @return Event manager.
     */
    public ISessionEventManager getEventManager() {
        return eventManager;
    }

    // Protected Methods

    /**
     * Get a set of the specified events' identifiers.
     * 
     * @param events
     *            Events from which to create the set.
     * @return Set of identifiers.
     */
    protected Set<String> getEventIdentifiers(
            Collection<? extends IHazardEventView> events) {

        /*
         * TODO: When moving to Java 8, remove the code below that is not
         * commented out, and then uncomment the commented out code immediately
         * below it.
         */
        Set<String> eventIdentifiers = new HashSet<>(events.size(), 1.0f);
        for (IHazardEventView event : events) {
            eventIdentifiers.add(event.getEventID());
        }
        return eventIdentifiers;
        // return events.stream().map(IHazardEvent::getEventID)
        // .collect(Collectors.toSet());
    }

    /**
     * Filter the specified list of events to remove any events that have the
     * specified identifiers.
     * 
     * @param events
     *            List of events to filter.
     * @param eventIdentifiers
     *            Identifiers of events to be filtered out of the list.
     * @return Filtered list.
     */
    protected List<IHazardEventView> filterEventsToRemoveAnyWithIdentifiers(
            List<? extends IHazardEventView> events,
            Set<String> eventIdentifiers) {

        /*
         * TODO: When moving to Java 8, remove the code below that is not
         * commented out, and then uncomment the commented out code immediately
         * below it.
         */
        List<IHazardEventView> prunedEvents = new ArrayList<>(events.size());
        for (IHazardEventView event : events) {
            if (eventIdentifiers.contains(event.getEventID()) == false) {
                prunedEvents.add(event);
            }
        }
        return prunedEvents;
        // return events.stream()
        // .filter(element -> eventIdentifiers
        // .contains(element.getEventID()) == false)
        // .collect(Collectors.toList());
    }
}
