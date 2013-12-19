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

import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Significance;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypeEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.deprecated.Event;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Geometry;

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
 * Oct 21, 2013 2177       blawrenc    Added logic to check for event conflicts.
 * Oct 23, 2013 2277       jsanchez    Removed HazardEventConverter from viz.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 29, 2013 2378       blawrenc    Changed to not set modified
 *                                     events back to PENDING.
 * 
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
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

    @Override
    public Collection<IHazardEvent> getEventsForCurrentSettings() {
        Collection<IHazardEvent> result = getEvents();
        filterEventsForConfig(result);
        return result;
    }

    private void filterEventsForConfig(Collection<IHazardEvent> events) {
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
                String key = HazardEventUtilities.getHazardType(event);
                /*
                 * Check for null key ensures we don't filter out events for
                 * which a type has not yet been defined.
                 */
                if (key != null && !phenSigs.contains(key)) {
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

        if (eventID != null && eventID.length() > 0) {
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
        IHazardEvent event = notification.getEvent();
        addModification(event.getEventID());
        if (event instanceof ObservedHazardEvent) {
            ((ObservedHazardEvent) event).setModified(true);
        }
        notificationSender.postNotification(notification);
    }

    protected void hazardEventAttributeModified(
            SessionEventAttributeModified notification) {
        IHazardEvent event = notification.getEvent();
        addModification(event.getEventID());
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
                        event.setState(HazardState.ENDED, true, true);
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
                    .getHazardType(event));
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
                    .getHazardType(event));
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
    public Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents() {

        Map<String, Collection<IHazardEvent>> conflictingHazardMap = Maps
                .newHashMap();

        Collection<IHazardEvent> selectedEvents = getSelectedEvents();

        for (IHazardEvent eventToCheck : selectedEvents) {

            Map<IHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
                    eventToCheck, eventToCheck.getStartTime(),
                    eventToCheck.getEndTime(), eventToCheck.getGeometry(),
                    HazardEventUtilities.getHazardType(eventToCheck));

            if (!conflictingHazards.isEmpty()) {
                conflictingHazardMap.put(eventToCheck.getEventID(),
                        conflictingHazards.keySet());
            }

        }

        return conflictingHazardMap;

    }

    @Override
    public Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> getAllConflictingEvents() {

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictingHazardMap = Maps
                .newHashMap();
        /*
         * Find the union of the session events and those retrieved from the
         * hazard event manager. Ignore "Ended" events.
         */
        List<IHazardEvent> eventsToCheck = getEventsToCheckForConflicts(new HazardQueryBuilder());

        for (IHazardEvent eventToCheck : eventsToCheck) {

            Map<IHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
                    eventToCheck, eventToCheck.getStartTime(),
                    eventToCheck.getEndTime(), eventToCheck.getGeometry(),
                    HazardEventUtilities.getHazardType(eventToCheck));

            if (!conflictingHazards.isEmpty()) {
                conflictingHazardMap.put(eventToCheck, conflictingHazards);
            }

        }

        return conflictingHazardMap;
    }

    @Override
    public Map<IHazardEvent, Collection<String>> getConflictingEvents(
            final IHazardEvent eventToCompare, final Date startTime,
            final Date endTime, final Geometry geometry, String phenSigSubtype) {

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = Maps
                .newHashMap();

        /*
         * A hazard type may not always be assigned to an event yet.
         */
        if (phenSigSubtype != null) {

            /*
             * Retrieve the list of conflicting hazards associated with this
             * type.
             */
            HazardTypes hazardTypes = configManager.getHazardTypes();
            HazardTypeEntry hazardTypeEntry = hazardTypes.get(phenSigSubtype);

            if (hazardTypeEntry != null) {

                List<String> hazardConflictList = hazardTypeEntry
                        .getHazardConflictList();

                if (!hazardConflictList.isEmpty()) {

                    String cwa = LocalizationManager
                            .getContextName(LocalizationLevel.SITE);

                    String hazardHatchArea = hazardTypeEntry
                            .getHazardHatchArea();

                    String hazardHatchAreaLabel = hazardTypeEntry
                            .getHazardHatchLabel();

                    String hazardHatchLabel = hazardTypeEntry
                            .getHazardHatchLabel();

                    Set<IGeometryData> hatchedAreasForEvent = HazardEventUtilities
                            .buildHatchedAreaForEvent(hazardHatchArea,
                                    hazardHatchLabel, cwa, eventToCompare);

                    /*
                     * Retrieve matching events from the Hazard Event Manager
                     * Also, include those from the session state.
                     */
                    HazardQueryBuilder hazardQueryBuilder = new HazardQueryBuilder();

                    hazardQueryBuilder.addKey(
                            HazardConstants.HAZARD_EVENT_START_TIME,
                            eventToCompare.getStartTime());
                    hazardQueryBuilder.addKey(
                            HazardConstants.HAZARD_EVENT_END_TIME,
                            eventToCompare.getEndTime());
                    for (String conflictPhenSig : hazardConflictList) {
                        hazardQueryBuilder.addKey(HazardConstants.PHENSIG,
                                conflictPhenSig);
                    }

                    hazardQueryBuilder.addKey(
                            HazardConstants.HAZARD_EVENT_STATE,
                            HazardState.ISSUED);

                    hazardQueryBuilder.addKey(
                            HazardConstants.HAZARD_EVENT_STATE,
                            HazardState.PROPOSED);

                    List<IHazardEvent> eventsToCheck = getEventsToCheckForConflicts(hazardQueryBuilder);

                    /*
                     * Loop over the existing events.
                     */
                    TimeRange modifiedEventTimeRange = new TimeRange(
                            eventToCompare.getStartTime(),
                            eventToCompare.getEndTime());

                    for (IHazardEvent eventToCheck : eventsToCheck) {

                        /*
                         * Test the events for overlap in time. If they do not
                         * overlap in time, then there is no need to test for
                         * overlap in area.
                         */
                        TimeRange eventToCheckTimeRange = new TimeRange(
                                eventToCheck.getStartTime(),
                                eventToCheck.getEndTime());

                        if (modifiedEventTimeRange
                                .overlaps(eventToCheckTimeRange)) {
                            if (!eventToCheck.getEventID().equals(
                                    eventToCompare.getEventID())) {

                                String otherEventPhenSigSubtype = HazardEventUtilities
                                        .getHazardType(eventToCheck);

                                if (hazardConflictList
                                        .contains(otherEventPhenSigSubtype)) {

                                    hazardTypeEntry = hazardTypes
                                            .get(otherEventPhenSigSubtype);

                                    if (hazardTypeEntry != null) {
                                        String hazardHatchAreaToCheck = hazardTypeEntry
                                                .getHazardHatchArea();
                                        String hazardHatchToCheckLabel = hazardTypeEntry
                                                .getHazardHatchLabel();

                                        Set<IGeometryData> hatchedAreasEventToCheck = HazardEventUtilities
                                                .buildHatchedAreaForEvent(
                                                        hazardHatchAreaToCheck,
                                                        hazardHatchToCheckLabel,
                                                        cwa, eventToCheck);

                                        conflictingHazardsMap
                                                .putAll(buildConflictMap(
                                                        eventToCompare,
                                                        eventToCheck,
                                                        hatchedAreasForEvent,
                                                        hatchedAreasEventToCheck,
                                                        hazardHatchArea,
                                                        hazardHatchAreaLabel,
                                                        hazardHatchAreaToCheck,
                                                        hazardHatchToCheckLabel));
                                    } else {
                                        statusHandler
                                                .warn("No entry defined in HazardTypes.py for hazard type "
                                                        + phenSigSubtype);
                                    }

                                }
                            }
                        }
                    }
                }
            } else {
                statusHandler
                        .warn("No entry defined in HazardTypes.py for hazard type "
                                + phenSigSubtype);
            }

        }

        return conflictingHazardsMap;
    }

    @Deprecated
    @Override
    public void modifyEventArea(String jsonText) {
        Event jevent = jsonConverter.fromJson(jsonText, Event.class);
        IHazardEvent event = getEventById(jevent.getEventID());

        if (event != null) {

            Geometry modifiedGeometry = jevent.getGeometry();

            if (!canChangeGeometry(event)) {
                event = new BaseHazardEvent(event);
                Collection<IHazardEvent> selection = getSelectedEvents();
                event = addEvent(event);
                selection.add(event);
                setSelectedEvents(selection);
            }
            event.setGeometry(modifiedGeometry);
            event.addHazardAttribute(HazardConstants.POLYGON_MODIFIED,
                    new Boolean(true));
        }
    }

    /**
     * Retrieves events for conflict testing.
     * 
     * These events will include those from the current session and those
     * retrieved from the hazard event manager.
     * 
     * Other sources of hazard event information could be added to this as need.
     * 
     * @param hazardQueryBuilder
     *            Used to filter the the hazards retrieved from the
     *            HazardEventManager
     * @return
     */
    private List<IHazardEvent> getEventsToCheckForConflicts(
            final HazardQueryBuilder hazardQueryBuilder) {

        /*
         * Retrieve matching events from the Hazard Event Manager Also, include
         * those from the session state.
         */
        Map<String, HazardHistoryList> eventMap = this.dbManager
                .getEventsByFilter(hazardQueryBuilder.getQuery());
        List<IHazardEvent> eventsToCheck = Lists.newArrayList(getEvents());
        Map<String, IHazardEvent> sessionEventMap = Maps.newHashMap();

        for (IHazardEvent sessionEvent : eventsToCheck) {
            sessionEventMap.put(sessionEvent.getEventID(), sessionEvent);
        }

        for (String eventID : eventMap.keySet()) {
            HazardHistoryList historyList = eventMap.get(eventID);
            IHazardEvent eventFromManager = historyList.get(0);

            if (!sessionEventMap.containsKey(eventID)) {
                if (eventFromManager.getState() != HazardState.ENDED) {
                    eventsToCheck.add(eventFromManager);
                }
            }

        }

        return eventsToCheck;
    }

    @Override
    public void shutdown() {
        eventExpirationTimer.cancel();
        eventExpirationTimer = null;
        SimulatedTime.getSystemTime().removeSimulatedTimeChangeListener(
                timeListener);
    }

    /**
     * Based on the hatched areas associated with two hazard events, build a map
     * of conflicting areas (zones, counties, etc). Polygons are a special case
     * in which the polygon is the hatched area.
     * 
     * @param firstEvent
     *            The first of the two events to compare for conflicts
     * @param secondEvent
     *            The second of the two events to compare for conflicts
     * @param hatchedAreasFirstEvent
     *            The hatcheded areas associated with the first event
     * @param hatchedAreasSecondEvent
     *            The hatched areas associated with the second event
     * @param firstEventHatchArea
     *            The hatch area definition of the first event.
     * @param firstEventLabelParameter
     *            The label (if any) associated with the first event hazard
     *            area.
     * @param secondEventHatchArea
     *            The hatch area definition of the second event.
     * @param secondEventLabelParameter
     *            The label (if any) associated with the second event hazard
     *            area.
     * @return A map containing conflicting hazard events and associated areas
     *         (counties, zones, etc.) where they conflict (if available).
     * 
     */
    private Map<IHazardEvent, Collection<String>> buildConflictMap(
            IHazardEvent firstEvent, IHazardEvent secondEvent,
            Set<IGeometryData> hatchedAreasFirstEvent,
            Set<IGeometryData> hatchedAreasSecondEvent,
            String firstEventHatchArea, String firstEventLabelParameter,
            String secondEventHatchArea, String secondEventLabelParameter) {

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = Maps
                .newHashMap();

        List<String> geometryNames = Lists.newArrayList();

        if (!firstEventHatchArea.equalsIgnoreCase(HazardConstants.POLYGON_TYPE)
                && !secondEventHatchArea
                        .equalsIgnoreCase(HazardConstants.POLYGON_TYPE)) {

            Set<IGeometryData> commonHatchedAreas = Sets.newHashSet();
            commonHatchedAreas.addAll(hatchedAreasFirstEvent);
            commonHatchedAreas.retainAll(hatchedAreasSecondEvent);

            if (!commonHatchedAreas.isEmpty()) {

                for (IGeometryData hatchedArea : commonHatchedAreas) {

                    geometryNames.add(hatchedArea
                            .getString(firstEventLabelParameter));
                }

                conflictingHazardsMap.put(secondEvent, geometryNames);
            }
        } else {

            String labelFieldName = null;
            Set<IGeometryData> geoWithLabelInfo = null;

            if (!firstEventHatchArea
                    .equalsIgnoreCase(HazardConstants.POLYGON_TYPE)) {
                labelFieldName = firstEventLabelParameter;
                geoWithLabelInfo = hatchedAreasFirstEvent;
            } else if (!secondEventHatchArea
                    .equalsIgnoreCase(HazardConstants.POLYGON_TYPE)) {
                labelFieldName = secondEventLabelParameter;
                geoWithLabelInfo = hatchedAreasSecondEvent;
            }

            boolean conflictFound = false;

            for (IGeometryData hatchedArea : hatchedAreasFirstEvent) {
                for (IGeometryData hatchedAreaToCheck : hatchedAreasSecondEvent) {

                    if (hatchedArea.getGeometry().intersects(
                            hatchedAreaToCheck.getGeometry())) {

                        conflictFound = true;

                        if (labelFieldName != null) {

                            if (geoWithLabelInfo == hatchedAreasFirstEvent) {
                                geometryNames.add(hatchedArea
                                        .getString(labelFieldName));
                            } else {
                                geometryNames.add(hatchedAreaToCheck
                                        .getString(labelFieldName));
                            }

                        }
                    }
                }
            }

            if (conflictFound) {
                conflictingHazardsMap.put(secondEvent, geometryNames);
            }

        }

        return conflictingHazardsMap;
    };

    @Override
    public void endEvent(IHazardEvent event) {
        if (event.getClass().equals(ObservedHazardEvent.class)) {
            ObservedHazardEvent observedEvent = (ObservedHazardEvent) event;
            observedEvent.addHazardAttribute(
                    ISessionEventManager.ATTR_SELECTED, false);
            observedEvent.setState(HazardState.ENDED, true, true);
            clearUndoRedo(observedEvent);
            observedEvent.setModified(false);
        }
    }

    @Override
    public void issueEvent(IHazardEvent event) {
        if (event.getClass().equals(ObservedHazardEvent.class)) {
            ObservedHazardEvent observedEvent = (ObservedHazardEvent) event;
            observedEvent.setState(HazardState.ISSUED, true, true);
            clearUndoRedo(observedEvent);
            observedEvent.setModified(false);
        }

    }

    @Override
    public void proposeEvent(IHazardEvent event) {
        if (event.getClass().equals(ObservedHazardEvent.class)) {
            ObservedHazardEvent observedEvent = (ObservedHazardEvent) event;
            observedEvent.setState(HazardState.PROPOSED, true, true);
            clearUndoRedo(observedEvent);
            observedEvent.setModified(false);
        }
    }

    /**
     * Clears the undo/redo stack for the hazard event.
     * 
     * @param event
     *            Event for which to clear the undo/redo stack
     * @return
     */
    private void clearUndoRedo(IHazardEvent event) {
        ((IUndoRedoable) event).clearUndoRedo();
    }
}
