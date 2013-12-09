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

import gov.noaa.gsd.common.utilities.JSONConverter;

import java.util.ArrayList;
import java.util.Collection;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public abstract class AbstractSessionEventManager implements
        ISessionEventManager {

    /**
     * TODO This will go away with JSON refactor.
     */
    protected final JSONConverter jsonConverter = new JSONConverter();

    @Override
    public IHazardEvent getEventById(String eventId) {
        for (IHazardEvent event : getEvents()) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    @Override
    public Collection<IHazardEvent> getEventsByState(HazardState state) {
        Collection<IHazardEvent> allEvents = getEvents();
        Collection<IHazardEvent> events = new ArrayList<IHazardEvent>(
                allEvents.size());
        for (IHazardEvent event : allEvents) {
            if (event.getState().equals(state)) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public Collection<IHazardEvent> getSelectedEvents() {
        Collection<IHazardEvent> allEvents = getEventsForCurrentSettings();
        Collection<IHazardEvent> events = new ArrayList<IHazardEvent>(
                allEvents.size());
        for (IHazardEvent event : allEvents) {
            if (Boolean.TRUE.equals(event.getHazardAttribute(ATTR_SELECTED))) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public void setSelectedEvents(Collection<IHazardEvent> selectedEvents) {
        for (IHazardEvent event : getSelectedEvents()) {
            if (!selectedEvents.contains(event.getEventID())) {
                event.addHazardAttribute(ISessionEventManager.ATTR_SELECTED,
                        false);
            }
        }
        for (IHazardEvent event : selectedEvents) {
            event.addHazardAttribute(ISessionEventManager.ATTR_SELECTED, true);

            /*
             * Once selected, a potential event or set of events should be set
             * to PENDING.
             */
            if (event.getState() == HazardState.POTENTIAL) {
                event.setState(HazardState.PENDING);
            }
        }
    }

    @Override
    public Collection<IHazardEvent> getCheckedEvents() {
        Collection<IHazardEvent> allEvents = getEventsForCurrentSettings();
        Collection<IHazardEvent> events = new ArrayList<IHazardEvent>(
                allEvents.size());
        for (IHazardEvent event : allEvents) {
            if (Boolean.TRUE.equals(event.getHazardAttribute(ATTR_CHECKED))) {
                events.add(event);
            }
        }
        return events;
    }

    /*
     * HazardDetailPresenter will need to move to front on selection.
     */
    @Deprecated
    @Override
    public String getLastSelectedEventID() {
        IHazardEvent event = getLastModifiedSelectedEvent();
        if (event != null) {
            return event.getEventID();
        }
        return "";
    }

}
