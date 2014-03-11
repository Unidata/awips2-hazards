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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
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
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypeEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.hatching.HatchingUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.IUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.CountyUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.FireWXZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.MarineZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.NullUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.OffshoreZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.ZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

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
 * Jan 14, 2014 2755       bkowal      No longer create new Event IDs for events that
 *                                     are created EDEX-side for interoperability purposes.
 * Feb 17, 2014 2161       Chris.Golden Added code to change the end time or fall-
 *                                      below time to the "until further notice"
 *                                      value if the corresponding "until further
 *                                      notice" flag is set high. Also added code
 *                                      to track the set of hazard events that can
 *                                      have "until further notice" applied to
 *                                      them. Added Javadoc comments to appropriate
 *                                      methods (those that post notifications on
 *                                      the event bus) identifying them as potential
 *                                      hooks into addition/removal/modification of
 *                                      events.
 * Mar 3, 2014  3034       bkowal      Constant for GFE interoperability flag 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventManager extends AbstractSessionEventManager {

    /**
     * Default duration for hazard events that do not have a type.
     */
    private static final long NULL_HAZARD_TYPE_DEFAULT_DURATION = TimeUnit.HOURS
            .toMillis(8);

    /**
     * Contains the mappings between geodatabase table names and the UGCBuilders
     * which correspond to them.
     */
    private static Map<String, IUGCBuilder> geoTableUGCBuilderMap;

    /**
     * Look-up IUGCBuilders for tables in the maps geodatabase.
     */
    static {
        Map<String, IUGCBuilder> tempMap = Maps.newHashMap();

        tempMap.put(HazardConstants.MAPDATA_COUNTY, new CountyUGCBuilder());
        tempMap.put(HazardConstants.POLYGON_TYPE, new CountyUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_ZONE, new ZoneUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_FIRE_ZONES,
                new FireWXZoneUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_MARINE_ZONES,
                new MarineZoneUGCBuilder());
        tempMap.put(HazardConstants.MAPDATA_OFFSHORE,
                new OffshoreZoneUGCBuilder());

        geoTableUGCBuilderMap = Collections.unmodifiableMap(tempMap);
    }

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionEventManager.class);

    /**
     * Default distance tolerance and increment for use in geometry point
     * reduction algorithm.
     */
    private static double DEFAULT_DISTANCE_TOLERANCE = 0.001f;

    private static double DEFAULT_DISTANCE_TOLERANCE_INCREMENT = 0.001f;

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

    private final Set<String> identifiersOfEventsAllowingUntilFurtherNotice = new HashSet<>();

    /*
     * The messenger for displaying questions and warnings to the user and
     * retrieving answers. This allows the viz side (App Builder) to be
     * responsible for these dialogs, but gives the event manager and other
     * managers access to them without creating a dependency on the
     * gov.noaa.gsd.viz.hazards plugin. Since all parts of Hazard Services can
     * use the same code for creating these dialogs, it makes it easier for them
     * to be stubbed for testing.
     */
    private final IMessenger messenger;

    private final GeometryFactory geoFactory;

    public SessionEventManager(ISessionTimeManager timeManager,
            ISessionConfigurationManager configManager,
            IHazardEventManager dbManager,
            ISessionNotificationSender notificationSender, IMessenger messenger) {
        this.configManager = configManager;
        this.timeManager = timeManager;
        this.dbManager = dbManager;
        this.notificationSender = notificationSender;
        new SessionHazardNotificationListener(this);
        SimulatedTime.getSystemTime().addSimulatedTimeChangeListener(
                createTimeListener());
        this.messenger = messenger;
        geoFactory = new GeometryFactory();
    }

    @Subscribe
    public void settingsModified(SettingsModified notification) {
        loadEventsForSettings(notification.getSettings());
    }

    @Override
    public Collection<IHazardEvent> getEventsForCurrentSettings() {
        Collection<IHazardEvent> result = getEvents();
        filterEventsForConfig(result);
        return result;
    }

    /**
     * Ensure that toggles of "until further notice" flags result in the
     * appropriate time being set to "until further notice".
     * 
     * @param change
     *            Change that occurred.
     */
    @Subscribe
    public void hazardAttributesChanged(SessionEventAttributeModified change) {
        if (change.getAttributeKey().equals(
                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            change.getEvent().setEndTime(
                    getTimeResultingFromUntilFurtherNoticeToggle(change
                            .getEvent(), (Boolean) change.getAttributeValue(),
                            change.getEvent().getStartTime(), 1L));
        } else if (change.getAttributeKey().equals(
                HazardConstants.FALL_BELOW_UNTIL_FURTHER_NOTICE)) {
            Object crestTime = change.getEvent().getHazardAttribute(
                    HazardConstants.CREST);
            Date date = (crestTime instanceof Date ? (Date) crestTime
                    : new Date(((Number) crestTime).longValue()));
            change.getEvent().addHazardAttribute(
                    HazardConstants.FALL_BELOW,
                    getTimeResultingFromUntilFurtherNoticeToggle(
                            change.getEvent(),
                            (Boolean) change.getAttributeValue(), date, 2L));
        }
    }

    @Override
    public Set<String> getEventIdsAllowingUntilFurtherNotice() {
        return Collections
                .unmodifiableSet(identifiersOfEventsAllowingUntilFurtherNotice);
    }

    /**
     * Get the time to be used as a result of an "until further notice" toggle.
     * 
     * @param event
     *            Event that had its "until further notice" toggle changed.
     * @param value
     *            New value of the "until further notice" toggle; if <code>
     *            null</code>, it is considered false.
     * @param baseTime
     *            Base time to which to add the default duration if
     *            "until further notice" was toggled off.
     * @param denominator
     *            Number by which to divide the default duration when applying
     *            it if "until further notice" has been toggled off.
     * @return Time to be used as a result of an "until further notice" toggle.
     */
    private Date getTimeResultingFromUntilFurtherNoticeToggle(
            IHazardEvent event, Boolean value, Date baseTime, long denominator) {

        /*
         * If "until further notice" was toggled on, use the time value that
         * indicates "until further notice".
         */
        if (Boolean.TRUE.equals(value)) {
            return new Date(HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
        }

        /*
         * Since "until further notice" was toggled off, use the time value
         * resulting from adding the default duration to the base time.
         */
        HazardTypes hts = configManager.getHazardTypes();
        HazardTypeEntry ht = hts.get(HazardEventUtilities.getHazardType(event));
        long defaultDuration = (ht != null ? ht.getDefaultDuration()
                : NULL_HAZARD_TYPE_DEFAULT_DURATION) / denominator;
        return new Date(baseTime.getTime() + defaultDuration);
    }

    /**
     * Update the set of identifiers of events allowing the toggling of
     * "until further notice" mode. This is to be called whenever one or more
     * events have been added, removed, or had their hazard types changed.
     * 
     * @param event
     *            Event that has been added, removed, or modified.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event.
     */
    private void updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
            IHazardEvent event, boolean removed) {

        /*
         * Assume the event should be removed from the set unless it is not
         * being removed from the session, and it has a hazard type that allows
         * "until further notice".
         */
        boolean allowsUntilFurtherNotice = false;
        if (removed == false) {
            HazardTypeEntry hazardType = configManager.getHazardTypes().get(
                    HazardEventUtilities.getHazardType(event));
            if ((hazardType != null) && hazardType.isAllowUntilFurtherNotice()) {
                allowsUntilFurtherNotice = true;
            }
        }

        if (allowsUntilFurtherNotice) {
            identifiersOfEventsAllowingUntilFurtherNotice.add(event
                    .getEventID());
        } else {
            identifiersOfEventsAllowingUntilFurtherNotice.remove(event
                    .getEventID());
        }
    }

    /**
     * Ensure that the "until further notice" mode, if present in the specified
     * event, is appropriate; if it is not, remove it.
     * 
     * @param event
     *            Event to be checked.
     */
    private void ensureEventUntilFurtherNoticeAppropriate(IHazardEvent event) {

        /*
         * If this event cannot have "until further notice", ensure it is not
         * one of its attributes.
         */
        if (identifiersOfEventsAllowingUntilFurtherNotice.contains(event
                .getEventID()) == false) {

            /*
             * If the attributes contains the flag, remove it. If it was set
             * high, then reset the end time to the start time plus the default
             * duration for this event type.
             */
            Boolean untilFurtherNotice = (Boolean) event
                    .getHazardAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
            if (untilFurtherNotice != null) {
                event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
                if (untilFurtherNotice.equals(Boolean.TRUE)) {
                    event.setEndTime(getTimeResultingFromUntilFurtherNoticeToggle(
                            event, Boolean.FALSE, event.getStartTime(), 1L));
                }
            }
        }
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
        filters.put(HazardConstants.SITE_ID,
                new ArrayList<Object>(visibleSites));
        Set<String> visibleTypes = settings.getVisibleTypes();
        if (visibleTypes == null || visibleTypes.isEmpty()) {
            return;
        }
        filters.put(HazardConstants.PHEN_SIG, new ArrayList<Object>(
                visibleTypes));
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

    /**
     * Add the specified hazard event.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is added
     * to the current session, regardless of the source of the event. Additional
     * logic (method calls, etc.) may therefore be added to this method's
     * implementation as necessary if said logic must be run whenever an event
     * is added.
     */
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

        // verify that the hazard was not created server-side to fulfill
        // interoperability requirements
        if ((event.getState() == null
                || event.getState() == HazardState.PENDING || event.getState() == HazardState.POTENTIAL)
                && event.getHazardAttributes().containsKey(
                        HazardConstants.GFE_INTEROPERABILITY) == false) {

            /*
             * Can only add geometry to selected if the hazard type is empty.
             */
            if ((Boolean.TRUE.equals(configManager.getSettings()
                    .getAddGeometryToSelected()))
                    && (event.getHazardType() == null)
                    && (getSelectedEvents().size() == 1)) {
                IHazardEvent existingEvent = getSelectedEvents().iterator()
                        .next();
                Geometry existingGeometries = existingEvent.getGeometry();
                List<Geometry> geometryList = Lists.newArrayList();

                for (int i = 0; i < existingGeometries.getNumGeometries(); ++i) {
                    geometryList.add(existingGeometries.getGeometryN(i));
                }

                Geometry newGeometries = oevent.getGeometry();

                for (int i = 0; i < newGeometries.getNumGeometries(); ++i) {
                    geometryList.add(newGeometries.getGeometryN(i));
                }

                GeometryCollection geometryCollection = geoFactory
                        .createGeometryCollection(geometryList
                                .toArray(new Geometry[geometryList.size()]));

                existingEvent.setGeometry(geometryCollection);
                existingEvent
                        .removeHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
                return existingEvent;

            } else {

                oevent.setEventID(generateEventID(), false);
            }
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

        if (localEvent) {
            oevent.addHazardAttribute(ATTR_SELECTED, true);
        }
        oevent.addHazardAttribute(ATTR_CHECKED, true);
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(oevent, false);
        notificationSender
                .postNotification(new SessionEventAdded(this, oevent));
        return oevent;
    }

    @Override
    public void removeEvent(IHazardEvent event) {
        removeEvent(event, true);
    }

    /**
     * Remove the specified hazard event.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is removed
     * from the current session, regardless of the source of the change.
     * Additional logic (method calls, etc.) may therefore be added to this
     * method's implementation as necessary if said logic must be run whenever
     * an event is removed.
     */
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
                updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(event,
                        true);
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

    /**
     * Receive notification from an event that it was modified in any way
     * <strong>except</strong> for state changes (for example, Pending to
     * Issued), or the addition or removal of individual attributes.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is
     * modified as detailed above within the current session, regardless of the
     * source of the change. Additional logic (method calls, etc.) may therefore
     * be added to this method's implementation as necessary if said logic must
     * be run whenever an event is so modified.
     */
    protected void hazardEventModified(SessionEventModified notification) {
        IHazardEvent event = notification.getEvent();
        addModification(event.getEventID());
        if (event instanceof ObservedHazardEvent) {
            ((ObservedHazardEvent) event).setModified(true);
        }
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(event, false);
        ensureEventUntilFurtherNoticeAppropriate(event);
        notificationSender.postNotification(notification);
    }

    /**
     * Receiver notification from an event that the latter experienced the
     * modification of an individual attribute.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is
     * modified as detailed above within the current session, regardless of the
     * source of the change. Additional logic (method calls, etc.) may therefore
     * be added to this method's implementation as necessary if said logic must
     * be run whenever an event is so modified.
     */
    protected void hazardEventAttributeModified(
            SessionEventAttributeModified notification) {
        IHazardEvent event = notification.getEvent();
        addModification(event.getEventID());
        notificationSender.postNotification(notification);
    }

    /**
     * Receive notification from an event that the latter experienced a state
     * change (for example, Pending to Issued).
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event
     * experiences a state change in the current session, regardless of the
     * source of the change. Additional logic (method calls, etc.) may therefore
     * be added to this method's implementation as necessary if said logic must
     * be run whenever an event is so modified.
     */
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

                    String cwa = configManager.getSiteID();

                    String hazardHatchArea = hazardTypeEntry
                            .getHazardHatchArea();

                    String hazardHatchAreaLabel = hazardTypeEntry
                            .getHazardHatchLabel();

                    String hazardHatchLabel = hazardTypeEntry
                            .getHazardHatchLabel();

                    Set<IGeometryData> hatchedAreasForEvent = HatchingUtilities
                            .buildHatchedAreaForEvent(hazardHatchArea,
                                    hazardHatchLabel, cwa, eventToCompare,
                                    configManager);

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
                        hazardQueryBuilder.addKey(HazardConstants.PHEN_SIG,
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

                                        Set<IGeometryData> hatchedAreasEventToCheck = HatchingUtilities
                                                .buildHatchedAreaForEvent(
                                                        hazardHatchAreaToCheck,
                                                        hazardHatchToCheckLabel,
                                                        cwa, eventToCheck,
                                                        configManager);

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

    @Override
    public boolean clipSelectedHazardGeometries() {
        /*
         * Clip the selected hazard polygons to the forecast area boundary. If
         * the returned polygon is empty, then do not generate the product.
         */
        boolean success = true;

        HazardTypes hazardTypes = configManager.getHazardTypes();
        Collection<IHazardEvent> selectedEvents = this.getSelectedEvents();
        String cwa = LocalizationManager.getContextName(LocalizationLevel.SITE);

        for (IHazardEvent selectedEvent : selectedEvents) {

            HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                    .getHazardType());

            if ((selectedEvent.getState() != HazardState.ENDED && selectedEvent
                    .getState() != HazardState.ISSUED)
                    || (selectedEvent.getState() == HazardState.ISSUED
                            && hazardType.isAllowAreaChange() && ((ObservedHazardEvent) selectedEvent)
                                .isModified())) {

                Set<IGeometryData> geoDataSet = HatchingUtilities
                        .getClippedMapGeometries(
                                hazardType.getHazardClipArea(), null, cwa,
                                selectedEvent);

                List<Geometry> geometryList = Lists.newArrayList();

                for (IGeometryData geoData : geoDataSet) {
                    for (int i = 0; i < geoData.getGeometry()
                            .getNumGeometries(); ++i) {
                        geometryList.add(geoData.getGeometry().getGeometryN(i));
                    }
                }

                if (geometryList.isEmpty()) {
                    StringBuffer warningMessage = new StringBuffer();
                    warningMessage.append("Event " + selectedEvent.getEventID()
                            + " ");
                    warningMessage.append("is outside of the forecast area.\n");
                    warningMessage.append("Product generation halted.");
                    messenger.getWarner().warnUser("Clip Error",
                            warningMessage.toString());
                    success = false;
                    break;
                }

                Geometry geoCollection = geoFactory
                        .createGeometryCollection(geometryList
                                .toArray(new Geometry[0]));
                selectedEvent.setGeometry(geoCollection);
            }
        }

        return success;
    }

    @Override
    public void reduceSelectedHazardGeometries() {

        HazardTypes hazardTypes = configManager.getHazardTypes();
        Collection<IHazardEvent> selectedEvents = getSelectedEvents();

        for (IHazardEvent selectedEvent : selectedEvents) {

            HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                    .getHazardType());

            if ((selectedEvent.getState() != HazardState.ENDED && selectedEvent
                    .getState() != HazardState.ISSUED)
                    || (selectedEvent.getState() == HazardState.ISSUED
                            && hazardType.isAllowAreaChange() && ((ObservedHazardEvent) selectedEvent)
                                .isModified())) {

                /*
                 * Test if point reduction is necessary...
                 */
                int pointLimit = hazardType.getHazardPointLimit();

                if (pointLimit > 0) {

                    List<Geometry> geometryList = Lists.newArrayList();

                    /**
                     * TODO: Eventually we want to share the same logic WarnGen
                     * uses to reduce points. This is not accessible right not,
                     * at least without creating a dependency between Hazard
                     * Services and WarnGen.
                     */
                    Geometry geometryCollection = selectedEvent.getGeometry();

                    for (int i = 0; i < geometryCollection.getNumGeometries(); ++i) {

                        Geometry geometry = geometryCollection.getGeometryN(i);

                        if (geometry.getNumPoints() > pointLimit) {

                            double distanceTolerance = DEFAULT_DISTANCE_TOLERANCE;

                            DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(
                                    geometry);
                            Geometry newGeometry = null;

                            do {
                                simplifier
                                        .setDistanceTolerance(distanceTolerance);
                                newGeometry = simplifier.getResultGeometry();
                                distanceTolerance += DEFAULT_DISTANCE_TOLERANCE_INCREMENT;
                            } while (newGeometry.getNumPoints() > pointLimit);

                            geometryList.add(newGeometry);
                        } else {
                            geometryList.add(geometry);
                        }
                    }

                    Geometry geoCollection = geoFactory
                            .createGeometryCollection(geometryList
                                    .toArray(new Geometry[0]));
                    selectedEvent.setGeometry(geoCollection);

                }

            }
        }
    }

    @Override
    public boolean canEventAreaBeChanged(IHazardEvent hazardEvent) {
        HazardTypes hazardTypes = configManager.getHazardTypes();

        HazardTypeEntry hazardTypeEntry = hazardTypes.get(hazardEvent
                .getHazardType());

        if (hazardTypeEntry != null
                && hazardEvent.getState() == HazardState.ISSUED) {

            return hazardTypeEntry.isAllowAreaChange();
        } else {
            return true;
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

    @Override
    public void updateSelectedHazardUGCs() {

        for (IHazardEvent hazardEvent : getSelectedEvents()) {
            String hazardType = HazardEventUtilities.getHazardType(hazardEvent);

            if (hazardType != null) {
                String mapDBtableName = configManager.getHazardTypes()
                        .get(hazardType).getHazardHatchArea();

                String mapLabelParameter = configManager.getHazardTypes()
                        .get(hazardType).getHazardHatchLabel();

                String cwa = configManager.getSiteID();

                Set<IGeometryData> hazardArea;

                if (mapDBtableName.equals(HazardConstants.POLYGON_TYPE)) {
                    hazardArea = HatchingUtilities
                            .getIntersectingMapGeometries(
                                    HazardConstants.MAPDATA_COUNTY,
                                    mapLabelParameter, cwa, true,
                                    configManager, hazardEvent);
                } else {
                    hazardArea = HatchingUtilities.buildHatchedAreaForEvent(
                            mapDBtableName, mapLabelParameter, cwa,
                            hazardEvent, configManager);
                }

                /*
                 * TODO Will need to support user-additions/removals to/from UGC
                 * List.
                 */
                IUGCBuilder ugcBuilder = getUGCBuilder(mapDBtableName);
                List<String> ugcList = ugcBuilder.buildUGCList(hazardArea);
                hazardEvent.addHazardAttribute(HazardConstants.UGCS,
                        (Serializable) ugcList);
            }
        }

    }

    /**
     * Factory method which builds the correct IUGCBuilder based on the provided
     * geodatabase table name.
     * 
     * @param geoTableName
     *            The name of the geodatabase table
     * @return An IUGCBuilder object which knows how to construct UGCs for the
     *         specified geodatabase table.
     */
    private IUGCBuilder getUGCBuilder(String geoTableName) {

        if (geoTableUGCBuilderMap.containsKey(geoTableName)) {
            return geoTableUGCBuilderMap.get(geoTableName);
        } else {
            statusHandler.error("No UGC handler found for maps database table "
                    + geoTableName);
            return new NullUGCBuilder();
        }
    }
}
