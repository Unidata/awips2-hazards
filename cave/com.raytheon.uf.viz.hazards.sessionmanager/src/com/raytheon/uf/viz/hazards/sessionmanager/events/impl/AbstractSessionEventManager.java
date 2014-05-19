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
package com.raytheon.uf.viz.hazards.sessionmanager.events.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

/**
 * Provides basic functionality og ISessionEventManager
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013 1257       bsteffen    Initial creation
 * Aug 29, 2013 1921       blawrenc    Updated to set selected
 *                                     potential events to pending.
 *                                     This is in keeping with the
 *                                     Mixed Hazard Story.
 * 
 *  
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * May 09, 2014 2925       Chris.Golden Changed getSelectedEvents() to return a list, as it
 *                                      has order, instead of a collection.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public abstract class AbstractSessionEventManager implements
        ISessionEventManager<ObservedHazardEvent> {
    /**
     * Comparator can be used with sortEvents to send selected events to the
     * front of the list.
     */
    public static final Comparator<ObservedHazardEvent> SEND_SELECTED_FRONT = new Comparator<ObservedHazardEvent>() {

        @Override
        public int compare(ObservedHazardEvent o1, ObservedHazardEvent o2) {
            boolean s1 = Boolean.TRUE.equals(o1
                    .getHazardAttribute(ISessionEventManager.ATTR_SELECTED));
            boolean s2 = Boolean.TRUE.equals(o2
                    .getHazardAttribute(ISessionEventManager.ATTR_SELECTED));
            if (s1) {
                if (s2) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (s2) {
                return -1;
            }
            return 0;
        }

    };

    /**
     * Comparator can be used with sortEvents to send selected events to the
     * back of the list.
     */
    public static final Comparator<ObservedHazardEvent> SEND_SELECTED_BACK = Collections
            .reverseOrder(SEND_SELECTED_FRONT);

    @Override
    public ObservedHazardEvent getEventById(String eventId) {
        for (ObservedHazardEvent event : getEvents()) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    @Override
    public Collection<ObservedHazardEvent> getEventsByStatus(HazardStatus state) {
        Collection<ObservedHazardEvent> allEvents = getEvents();
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (event.getStatus().equals(state)) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public List<ObservedHazardEvent> getSelectedEvents() {

        /*
         * TODO: Consider having getEventsForCurrentSettings() return a list as
         * well. It's questionable as to whether that method should return a
         * list, because maybe implying an inherent ordering of the events is
         * incorrect; however, the order is relied upon (since the code here
         * iterates through all those events) to return a consistently ordered
         * set of events. Perhaps the order should be set via the GUI, since the
         * console allows sorting of events, and maybe the order in which those
         * events occur should provide the order in which the selected events,
         * which are after all in the console list, occur.
         * 
         * TODO: Consider having this list rebuilt each time selection changes
         * (by doing so before sending off a SessionSelectedEventsModified
         * message in SessionEventManager), and then simply returning an
         * unmodifiable version of that list each time this method is called. Or
         * better yet, simply use the same list each time, modifying it as
         * appropriate when the selected events are changing, and again simply
         * returning an unmodifiable view of it to callers of this method. In
         * this case, any caller that wished to cache the selected events list
         * for comparison to updates to it later (i.e. classes such as
         * HazardDetailPresenter) would need to make a copy of what they got
         * from invoking this method.
         */
        Collection<ObservedHazardEvent> allEvents = getEventsForCurrentSettings();
        List<ObservedHazardEvent> events = new ArrayList<>(allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (Boolean.TRUE.equals(event.getHazardAttribute(ATTR_SELECTED))) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public void setSelectedEvents(
            Collection<ObservedHazardEvent> selectedEvents,
            IOriginator originator) {
        for (ObservedHazardEvent event : getSelectedEvents()) {
            if (!selectedEvents.contains(event.getEventID())) {
                event.addHazardAttribute(ISessionEventManager.ATTR_SELECTED,
                        false, originator);
            }
        }
        for (ObservedHazardEvent event : selectedEvents) {
            event.addHazardAttribute(ISessionEventManager.ATTR_SELECTED, true,
                    originator);

            /*
             * Once selected, a potential event or set of events should be set
             * to PENDING.
             */
            if (event.getStatus() == HazardStatus.POTENTIAL) {
                event.setStatus(HazardStatus.PENDING, Originator.OTHER);
            }
        }
    }

    @Override
    public Collection<ObservedHazardEvent> getCheckedEvents() {
        Collection<ObservedHazardEvent> allEvents = getEventsForCurrentSettings();
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (Boolean.TRUE.equals(event.getHazardAttribute(ATTR_CHECKED))) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public String getLastSelectedEventID() {
        IHazardEvent event = getLastModifiedSelectedEvent();
        if (event != null) {
            return event.getEventID();
        }
        return "";
    }

}
