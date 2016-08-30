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

import java.util.Set;

import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Description: Notification of a change to the list of selected events, whether
 * the change is a reordering of what was already in the list, the addition of
 * one or more events, the removal of one or more events, or more than one of
 * these conditions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * Aug 18, 2016   19537    Chris.Golden Added set of identifiers of hazard
 *                                      events that have had their selection
 *                                      state changed.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionSelectedEventsModified extends SessionEventsModified {

    // Private Variables

    /**
     * Identifiers of hazard events that have had their selection state changed.
     */
    private final Set<String> eventIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param eventIdentifiers
     *            Identifiers of the hazard events that have had their selection
     *            state changed.
     * @param originator
     *            Originator of the event.
     */
    public SessionSelectedEventsModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            Set<String> eventIdentifiers, IOriginator originator) {
        super(eventManager, originator);
        this.eventIdentifiers = eventIdentifiers;
    }

    // Public Methods

    /**
     * Get the identifiers of the hazard events that have had their selection
     * state changed.
     * 
     * @return Identifiers of the hazard events.
     */
    public Set<String> getEventIdentifiers() {
        return eventIdentifiers;
    }
}
