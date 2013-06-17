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
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Significance;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsFiltersModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsIDModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.SessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypeEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

/**
 * Implementation of ISessionEventManager
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventManager implements ISessionEventManager {

    private final ISessionTimeManager timeManager;

    /*
     * A full configuration manager is needed to get access to hazard types,
     * which is not exposed in ISessionConfigurationManager
     */
    private final SessionConfigurationManager configManager;

    private final IHazardEventManager dbManager;

    private final ISessionNotificationSender notificationSender;

    private List<IHazardEvent> events = new ArrayList<IHazardEvent>();

    public SessionEventManager(ISessionTimeManager timeManager,
            SessionConfigurationManager configManager,
            IHazardEventManager dbManager,
            ISessionNotificationSender notificationSender) {
        this.configManager = configManager;
        this.timeManager = timeManager;
        this.dbManager = dbManager;
        this.notificationSender = notificationSender;
    }

    @Subscribe
    public void settingsLoaded(SettingsLoaded notification) {
        loadEventsForSettings(notification.getSettings());
    }

    @Subscribe
    public void settingsIDChanged(SettingsIDModified notification) {
        for (IHazardEvent event : getEvents()) {
            removeEvent(event);
        }
    }

    @Subscribe
    public void settingsFiltersChanged(SettingsFiltersModified notification) {
        Collection<IHazardEvent> eventsToKepp = getEvents();
        filterEventsForConfig(eventsToKepp);
        Collection<IHazardEvent> eventsToRemove = getEvents();
        eventsToRemove.removeAll(eventsToKepp);
        for (IHazardEvent event : eventsToRemove) {
            removeEvent(event);
        }
        loadEventsForSettings(notification.getSettings());
    }

    protected void filterEventsForConfig(Collection<IHazardEvent> events) {
        Settings settings = configManager.getSettings();
        List<String> siteIDs = settings.getVisibleSites();
        List<String> phenSigs = settings.getVisibleTypes();
        Set<HazardState> states = EnumSet.noneOf(HazardState.class);
        for (String state : settings.getVisibleStates()) {
            states.add(HazardState.valueOf(state.toUpperCase()));
        }
        Iterator<IHazardEvent> it = events.iterator();
        while (it.hasNext()) {
            IHazardEvent event = it.next();
            if (!states.contains(event.getState())) {
                it.remove();
            } else if (!siteIDs.contains(event.getSiteID())) {
                it.remove();
            } else {
                String key = HazardEventUtilities.getPhenSigSubType(event);
                if (!phenSigs.contains(key)) {
                    it.remove();
                }
            }
        }
    }

    private void loadEventsForSettings(Settings settings) {
        Map<String, List<Object>> filters = new HashMap<String, List<Object>>();
        List<String> visibleSites = settings.getVisibleSites();
        if (visibleSites == null || visibleSites.isEmpty()) {
            return;
        }
        filters.put(HazardConstants.SITEID, new ArrayList<Object>(visibleSites));
        List<String> visibleTypes = settings.getVisibleTypes();
        if (visibleTypes == null || visibleTypes.isEmpty()) {
            return;
        }
        filters.put(HazardConstants.PHENSIG,
                new ArrayList<Object>(visibleTypes));
        List<String> visibleStates = settings.getVisibleStates();
        if (visibleStates == null || visibleStates.isEmpty()) {
            return;
        }
        List<Object> states = new ArrayList<Object>(visibleStates.size());
        for (String state : visibleStates) {
            states.add(HazardState.valueOf(state.toUpperCase()));
        }
        filters.put(HazardConstants.STATE, states);
        Map<String, HazardHistoryList> eventsMap = dbManager
                .getEventsByFilter(filters);
        synchronized (events) {
            for (Entry<String, HazardHistoryList> entry : eventsMap.entrySet()) {
                HazardHistoryList list = entry.getValue();
                IHazardEvent event = list.get(list.size() - 1);
                if (getEventById(event.getEventID()) != null) {
                    // already have this one.
                    continue;
                }
                event = addEvent(event, false, false);
                for(IHazardEvent histEvent : list){
                    if(histEvent.getState() == HazardState.ISSUED){
                        event.addHazardAttribute(ATTR_ISSUED, true);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public IHazardEvent addEvent(IHazardEvent event) {
        return addEvent(event, true, true);
    }

    protected IHazardEvent addEvent(IHazardEvent event,
            boolean modifySelection, boolean setEventId) {
        ObservedHazardEvent oevent = new ObservedHazardEvent(event, this);

        if (setEventId) {
            oevent.setEventID(generateEventID(), false);
        }

        Settings settings = configManager.getSettings();

        if (oevent.getHazardAttribute(ATTR_HAZARD_CATEGORY) == null) {
            oevent.addHazardAttribute(ATTR_HAZARD_CATEGORY,
                    settings.getDefaultCategory(), false);
        }
        if (oevent.getStartTime() == null) {
            oevent.setStartTime(timeManager.getSelectedTime(), false);
        }
        if (oevent.getEndTime() == null) {
            long s = oevent.getStartTime().getTime();
            long d = settings.getDefaultDuration();
            oevent.setEndTime(new Date(s + d), false);
        }
        if (oevent.getState() == null) {
            oevent.setState(HazardState.PENDING, false, false);
        }
        String sig = oevent.getSignificance();
        if (sig != null) {
            try {
                // Validate significance since some recommenders use full name
                HazardConstants.significanceFromAbbreviation(sig);
            } catch (IllegalArgumentException e) {
                // This will throw an exception if its not a valid name or
                // abbreviation.
                Significance s = Significance.valueOf(sig);
                oevent.setSignificance(s.getAbbreviation(), false);
            }
        }
        oevent.setSiteID(configManager.getSiteID(), false);
        // TODO operational.
        oevent.setHazardMode(ProductClass.TEST, false);
        synchronized (events) {
            if (modifySelection
                    && !Boolean.TRUE.equals(settings.getAddToSelected())) {
                for (IHazardEvent e : events) {
                    e.addHazardAttribute(ATTR_SELECTED, false);
                }
            }
            events.add(oevent);
        }
        oevent.addHazardAttribute(ATTR_SELECTED, false, false);
        oevent.addHazardAttribute(ATTR_CHECKED, false, false);
        oevent.addHazardAttribute(ATTR_ISSUED,
                oevent.getState().equals(HazardState.ISSUED), false);
        notificationSender
                .postNotification(new SessionEventAdded(this, oevent));
        if (modifySelection) {
            oevent.addHazardAttribute(ATTR_SELECTED, true);
        }
        oevent.addHazardAttribute(ATTR_CHECKED, true);
        return oevent;
    }

    @Override
    public void removeEvent(IHazardEvent event) {
        synchronized (events) {
            if (events.remove(event)) {
                // TODO this should never delete operation issued events
                // TODO this should not delete the whole list, just any pending
                // or proposed items on the end of the list.
                HazardHistoryList histList = dbManager.getByEventID(event
                        .getEventID());
                if (histList != null && !histList.isEmpty()) {
                    dbManager.removeEvents(histList);
                }
                notificationSender.postNotification(new SessionEventRemoved(
                        this, event));
            }
        }
    }

    @Override
    public void sortEvents(Comparator<IHazardEvent> comparator) {
        synchronized (events) {
            Collections.sort(events, comparator);
        }
    }

    @Override
    public Collection<IHazardEvent> getEvents() {
        synchronized (events) {
            return new ArrayList<IHazardEvent>(events);
        }
    }

    @Override
    public IHazardEvent getEventById(String eventId) {
        synchronized (events) {
            for (IHazardEvent event : events) {
                if (event.getEventID().equals(eventId)) {
                    return event;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<IHazardEvent> getEventsByState(HazardState state) {
        synchronized (events) {
            Collection<IHazardEvent> events = new ArrayList<IHazardEvent>(
                    this.events.size());
            for (IHazardEvent event : this.events) {
                if (event.getState().equals(state)) {
                    events.add(event);
                }
            }
            return events;
        }
    }

    @Override
    public Collection<IHazardEvent> getSelectedEvents() {
        synchronized (events) {
            Collection<IHazardEvent> events = new ArrayList<IHazardEvent>(
                    this.events.size());
            for (IHazardEvent event : this.events) {
                if (Boolean.TRUE
                        .equals(event.getHazardAttribute(ATTR_SELECTED))) {
                    events.add(event);
                }
            }
            return events;
        }
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
        }
    }

    @Override
    public Collection<IHazardEvent> getCheckedEvents() {
        synchronized (events) {
            Collection<IHazardEvent> events = new ArrayList<IHazardEvent>(
                    this.events.size());
            for (IHazardEvent event : this.events) {
                if (Boolean.TRUE.equals(event.getHazardAttribute(ATTR_CHECKED))) {
                    events.add(event);
                }
            }
            return events;
        }
    }

    protected void hazardEventModifiedChange(SessionEventModified notification) {
        notification.getEvent().setState(HazardState.PENDING);
        notificationSender.postNotification(notification);
    }

    protected void hazardEventAttributeModified(
            SessionEventAttributeModified notification) {
        if (!notification.isAttrbute(ATTR_SELECTED)
                && !notification.isAttrbute(ATTR_CHECKED)
                && !notification.isAttrbute(ATTR_ISSUED)) {
            notification.getEvent().setState(HazardState.PENDING);
        }
        notificationSender.postNotification(notification);
    }

    protected void hazardEventStateModified(
            SessionEventStateModified notification, boolean persist) {
        if (persist) {
            ObservedHazardEvent event = (ObservedHazardEvent) notification
                    .getEvent();
            HazardState newState = event.getState();
            switch (newState) {
            case ISSUED:
                event.addHazardAttribute(ATTR_ISSUED, true);
            case PROPOSED:
            case ENDED:
                // issue goes through special route so it does not reset state.
                event.setIssueTime(SimulatedTime.getSystemTime().getTime(),
                        false);
                notificationSender.postNotification(new SessionEventModified(
                        this, event));
                try {
                    IHazardEvent dbEvent = dbManager.createEvent(event);
                    dbEvent.removeHazardAttribute(ATTR_ISSUED);
                    dbEvent.removeHazardAttribute(ATTR_SELECTED);
                    dbEvent.removeHazardAttribute(ATTR_CHECKED);
                    dbEvent.removeHazardAttribute(ATTR_HAZARD_CATEGORY);
                    dbManager.storeEvent(dbEvent);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            default:
                ;// do nothing.
            }
        }
        notificationSender.postNotification(notification);
    }

    @Override
    public boolean canChangeGeometry(IHazardEvent event) {
        if (hasEverBeenIssued(event)) {
            HazardTypes hts = configManager.getHazardTypes();
            HazardTypeEntry ht = hts.get(HazardEventUtilities
                    .getPhenSigSubType(event));
            if (ht != null) {
                if (!ht.isAllowAreaChange()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canChangeTimeRange(IHazardEvent event) {
        if (hasEverBeenIssued(event)) {
            HazardTypes hts = configManager.getHazardTypes();
            HazardTypeEntry ht = hts.get(HazardEventUtilities
                    .getPhenSigSubType(event));
            if (ht != null) {
                if (!ht.isAllowTimeChange()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canChangeType(IHazardEvent event) {
        if (hasEverBeenIssued(event)) {
            return false;
        }
        return true;
    }

    private boolean hasEverBeenIssued(IHazardEvent event) {
        return Boolean.TRUE.equals(event.getHazardAttribute(ATTR_ISSUED));
    }

    private String generateEventID() {
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setSiteId(configManager.getSiteID());
        String value = "";
        try {
            value = RequestRouter.route(request).toString();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to make request for hazard event id", e);
        }
        return value;
    }
}
