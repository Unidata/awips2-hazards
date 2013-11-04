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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;

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
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsFiltersModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsIDModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypeEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;

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
 * Jul 19, 2013 1257       bsteffen    Notification support for session manager.
 * Sep 10, 2013  752       blawrenc    Modified addEvent to check if the event
 *                                     being added already exists.
 * Sep 12, 2013 717        jsanchez    Converted certain hazard events to grids.
 * Oct 23, 2013 2277       jsanchez    Removed HazardEventConverter from viz.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventManager extends AbstractSessionEventManager {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionEventManager.class);

    private final ISessionTimeManager timeManager;

    /*
     * A full configuration manager is needed to get access to hazard types,
     * which is not exposed in ISessionConfigurationManager
     */
    private final ISessionConfigurationManager configManager;

    private final IHazardEventManager dbManager;

    private final ISessionNotificationSender notificationSender;

    private final List<IHazardEvent> events = new ArrayList<IHazardEvent>();

    private final Deque<String> eventModifications = new LinkedList<String>();

    private Timer eventExpirationTimer = new Timer(true);

    private final Map<String, TimerTask> expirationTasks = new ConcurrentHashMap<String, TimerTask>();

    private ISimulatedTimeChangeListener timeListener;

    public SessionEventManager(ISessionTimeManager timeManager,
            ISessionConfigurationManager configManager,
            IHazardEventManager dbManager,
            ISessionNotificationSender notificationSender) {
        this.configManager = configManager;
        this.timeManager = timeManager;
        this.dbManager = dbManager;
        this.notificationSender = notificationSender;
        new SessionHazardNotificationListener(this);
        SimulatedTime.getSystemTime().addSimulatedTimeChangeListener(
                createTimeListener());
    }

    @Subscribe
    public void settingsLoaded(SettingsLoaded notification) {
        loadEventsForSettings(notification.getSettings());
    }

    @Subscribe
    public void settingsIDChanged(SettingsIDModified notification) {
        for (IHazardEvent event : getEvents()) {
            removeEvent(event, false);
        }
    }

    @Subscribe
    public void settingsFiltersChanged(SettingsFiltersModified notification) {
        Collection<IHazardEvent> eventsToKepp = getEvents();
        filterEventsForConfig(eventsToKepp);
        Collection<IHazardEvent> eventsToRemove = getEvents();
        eventsToRemove.removeAll(eventsToKepp);
        for (IHazardEvent event : eventsToRemove) {
            removeEvent(event, false);
        }
        loadEventsForSettings(notification.getSettings());
    }

    protected void filterEventsForConfig(Collection<IHazardEvent> events) {
        Settings settings = configManager.getSettings();
        Set<String> siteIDs = settings.getVisibleSites();
        Set<String> phenSigs = settings.getVisibleTypes();
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
        Set<String> visibleSites = settings.getVisibleSites();
        if (visibleSites == null || visibleSites.isEmpty()) {
            return;
        }
        filters.put(HazardConstants.SITEID, new ArrayList<Object>(visibleSites));
        Set<String> visibleTypes = settings.getVisibleTypes();
        if (visibleTypes == null || visibleTypes.isEmpty()) {
            return;
        }
        filters.put(HazardConstants.PHENSIG,
                new ArrayList<Object>(visibleTypes));
        Set<String> visibleStates = settings.getVisibleStates();
        if (visibleStates == null || visibleStates.isEmpty()) {
            return;
        }
        List<Object> states = new ArrayList<Object>(visibleStates.size());
        for (String state : visibleStates) {
            states.add(HazardState.valueOf(state.toUpperCase()));
        }
        filters.put(HazardConstants.HAZARD_EVENT_STATE, states);
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
                event = addEvent(event, false);
                for (IHazardEvent histEvent : list) {
                    if (histEvent.getState() == HazardState.ISSUED) {
                        event.addHazardAttribute(ATTR_ISSUED, true);
                        break;
                    }
                }
            }
            for (IHazardEvent event : events) {
                scheduleExpirationTask((ObservedHazardEvent) event);
            }
        }
    }

    @Override
    public IHazardEvent addEvent(IHazardEvent event) {
        HazardState state = event.getState();
        if (state == null || state == HazardState.PENDING) {
            return addEvent(event, true);
        } else if (state == HazardState.POTENTIAL) {
            return addEvent(event, false);
        } else {
            List<IHazardEvent> list = new ArrayList<IHazardEvent>();
            list.add(event);
            filterEventsForConfig(list);
            if (!list.isEmpty()) {
                return addEvent(event, false);
            } else {
                return null;
            }
        }
    }

    protected IHazardEvent addEvent(IHazardEvent event, boolean localEvent) {
        ObservedHazardEvent oevent = new ObservedHazardEvent(event, this);

        /*
         * Need to account for the case where the event being added already
         * exists in the event manager. This can happen with recommender
         * callbacks. For example, the ModifyStormTrackTool will modify
         * information corresponding to an existing event.
         */
        String eventID = oevent.getEventID();

        if (eventID.length() > 0) {
            IHazardEvent existingEvent = getEventById(eventID);

            if (existingEvent != null) {
                SessionEventUtilities.mergeHazardEvents(oevent, existingEvent);
                return existingEvent;
            }
        }

        if (event.getState() == null || event.getState() == HazardState.PENDING
                || event.getState() == HazardState.POTENTIAL) {
            oevent.setEventID(generateEventID(), false);
        }

        Settings settings = configManager.getSettings();

        if (configManager.getHazardCategory(oevent) == null
                && oevent.getHazardAttribute(ATTR_HAZARD_CATEGORY) == null) {
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

        if (SessionEventUtilities.isEnded(oevent)) {
            oevent.setState(HazardState.ENDED);
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
        ProductClass productClass;
        switch (CAVEMode.getMode()) {
        case OPERATIONAL:
            productClass = ProductClass.OPERATIONAL;
            break;
        case PRACTICE:
            // TODO, for now do it this way, maybe need to add user changeable
            productClass = ProductClass.OPERATIONAL;
            break;
        default:
            productClass = ProductClass.TEST;
        }
        oevent.setHazardMode(productClass, false);
        synchronized (events) {
            if (localEvent && !Boolean.TRUE.equals(settings.getAddToSelected())) {
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
        if (localEvent) {
            oevent.addHazardAttribute(ATTR_SELECTED, true);
        }
        oevent.addHazardAttribute(ATTR_CHECKED, true);
        return oevent;
    }

    @Override
    public void removeEvent(IHazardEvent event) {
        removeEvent(event, true);
    }

    private void removeEvent(IHazardEvent event, boolean delete) {
        synchronized (events) {
            if (events.remove(event)) {
                // TODO this should never delete operation issued events
                // TODO this should not delete the whole list, just any pending
                // or proposed items on the end of the list.
                if (delete) {
                    HazardHistoryList histList = dbManager.getByEventID(event
                            .getEventID());
                    if (histList != null && !histList.isEmpty()) {
                        dbManager.removeEvents(histList);
                    }
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

    protected void hazardEventModified(SessionEventModified notification) {
        notification.getEvent().setState(HazardState.PENDING);
        addModification(notification.getEvent().getEventID());
        notificationSender.postNotification(notification);
    }

    protected void hazardEventAttributeModified(
            SessionEventAttributeModified notification) {
        if (!notification.isAttrbute(ATTR_SELECTED)
                && !notification.isAttrbute(ATTR_CHECKED)
                && !notification.isAttrbute(ATTR_ISSUED)) {
            notification.getEvent().setState(HazardState.PENDING);
        }
        addModification(notification.getEvent().getEventID());
        notificationSender.postNotification(notification);
    }

    protected void hazardEventStateModified(
            SessionEventStateModified notification, boolean persist) {
        if (persist) {

            ObservedHazardEvent event = (ObservedHazardEvent) notification
                    .getEvent();
            HazardState newState = event.getState();
            boolean needsPersist = false;
            switch (newState) {
            case ISSUED:
                event.addHazardAttribute(ATTR_ISSUED, true);
                needsPersist = true;
                break;
            case PROPOSED:
                needsPersist = true;
                break;
            case ENDED:
                event.addHazardAttribute(ATTR_SELECTED, false);
                needsPersist = true;
                break;
            default:
                ;// do nothing.
            }
            if (needsPersist) {
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
                    scheduleExpirationTask(event);
                } catch (Throwable e) {
                    statusHandler.handle(Priority.PROBLEM,
                            e.getLocalizedMessage(), e);
                }
            }
        }

        addModification(notification.getEvent().getEventID());
        notificationSender.postNotification(notification);
    }

    /**
     * Schedules the tasks on the {@link Timer} to be executed at a later time,
     * unless they are already past the time necessary at which it will happen
     * immediately then.
     * 
     * @param event
     */
    private void scheduleExpirationTask(final ObservedHazardEvent event) {
        if (eventExpirationTimer != null) {
            if (event.getState() == HazardState.ISSUED) {
                final String eventId = event.getEventID();
                TimerTask existingTask = expirationTasks.get(eventId);
                if (existingTask != null) {
                    existingTask.cancel();
                    expirationTasks.remove(eventId);
                }
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        event.setState(HazardState.ENDED, true, false);
                        expirationTasks.remove(eventId);
                    }
                };
                Date scheduledTime = event.getEndTime();
                // need to determine what to do with this, somewhere we need to
                // be resetting the expiration time if we manually end the
                // hazard?
                // if (event.getHazardAttribute(HazardConstants.EXPIRATIONTIME)
                // != null) {
                // scheduledTime = new Date(
                // // TODO, change this when we are getting back
                // // expiration time as a date
                // (Long) event
                // .getHazardAttribute(HazardConstants.EXPIRATIONTIME));
                // }

                // round down to the nearest minute, so we see exactly when it
                // happens
                scheduledTime = DateUtils.truncate(scheduledTime,
                        Calendar.MINUTE);
                long scheduleTimeMillis = Math.max(0, scheduledTime.getTime()
                        - SimulatedTime.getSystemTime().getTime().getTime());
                System.out.println(eventId);
                System.out.println("ms : " + scheduleTimeMillis);
                System.out.println(TimeUnit.SECONDS.convert(scheduleTimeMillis,
                        TimeUnit.MILLISECONDS));
                if (SimulatedTime.getSystemTime().isFrozen() == false
                        || (SimulatedTime.getSystemTime().isFrozen() && scheduleTimeMillis == 0)) {
                    eventExpirationTimer.schedule(task, scheduleTimeMillis);
                    expirationTasks.put(eventId, task);
                }
            }
        }
    }

    /**
     * Creates a time listener so that we can reschedule the {@link TimerTask}
     * when necessary (the Simulated Time has changed or is frozen)
     * 
     * @return
     */
    private ISimulatedTimeChangeListener createTimeListener() {
        timeListener = new ISimulatedTimeChangeListener() {

            @Override
            public void timechanged() {
                for (TimerTask task : expirationTasks.values()) {
                    task.cancel();
                    expirationTasks.clear();
                }

                for (IHazardEvent event : events) {
                    if (event.getState() == HazardState.ENDED) {
                        event.setState(HazardState.ISSUED);
                    }
                    scheduleExpirationTask((ObservedHazardEvent) event);
                }
            }
        };
        return timeListener;
    }

    private void addModification(String eventId) {
        eventModifications.remove(eventId);
        eventModifications.push(eventId);
    }

    @Override
    public IHazardEvent getLastModifiedSelectedEvent() {
        if (eventModifications.isEmpty()) {
            return null;
        }
        IHazardEvent event = getEventById(eventModifications.peek());
        if (event != null
                && Boolean.TRUE.equals(event.getHazardAttribute(ATTR_SELECTED))) {
            return event;
        } else {
            eventModifications.pop();
            return getLastModifiedSelectedEvent();
        }
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

    @Override
    public void shutdown() {
        eventExpirationTimer.cancel();
        eventExpirationTimer = null;
        SimulatedTime.getSystemTime().removeSimulatedTimeChangeListener(
                timeListener);
    }
}
