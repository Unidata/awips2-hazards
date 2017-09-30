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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
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
    private final ISessionEventManager<ObservedHazardEvent> eventManager;

    // Public Methods

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param originator
     *            Originator of the change.
     */
    public SessionEventsModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
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
    public ISessionEventManager<ObservedHazardEvent> getEventManager() {
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
            Collection<? extends IHazardEvent> events) {
        return events.stream().map(IHazardEvent::getEventID)
                .collect(Collectors.toSet());
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
    protected List<IHazardEvent> filterEventsToRemoveAnyWithIdentifiers(
            List<IHazardEvent> events, Set<String> eventIdentifiers) {
        return events.stream()
                .filter(element -> eventIdentifiers
                        .contains(element.getEventID()) == false)
                .collect(Collectors.toList());
    }
}
