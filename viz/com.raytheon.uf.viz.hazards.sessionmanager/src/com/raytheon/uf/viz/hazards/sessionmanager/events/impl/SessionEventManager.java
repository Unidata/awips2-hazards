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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTAINED_UGCS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ETNS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EXPIRATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ISSUE_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PILS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.REPLACED_BY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VTEC_CODES;
import gov.noaa.gsd.viz.megawidgets.IParentSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.TimeMegawidgetSpecifier;

import java.io.File;
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

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Significance;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.config.HazardEventMetadata;
import com.raytheon.uf.viz.hazards.sessionmanager.config.IEventModifyingScriptJobListener;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ModifiedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAllowUntilFurtherNoticeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventMetadataModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventScriptExtraDataAvailable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionLastChangedEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.hatching.MapUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.IugcToMapGeometryDataBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.CountyUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.FireWXZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.MarineZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.NullUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.OffshoreZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.ugcbuilder.impl.ZoneUGCBuilder;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.valid.IsValidOp;
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
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * Jan 14, 2014 2755       bkowal      No longer create new Event IDs for events that
 *                                     are created EDEX-side for interoperability purposes.
 * Feb 17, 2014 2161       Chris.Golden Added code to change the end time or fall-
 *                                      below time to the "until further notice"
 *                                      value if the corresponding "until further
 *                                      notice" flag is set to true. Also added code
 *                                      to track the set of hazard events that can
 *                                      have "until further notice" applied to
 *                                      them. Added Javadoc comments to appropriate
 *                                      methods (those that post notifications on
 *                                      the event bus) identifying them as potential
 *                                      hooks into addition/removal/modification of
 *                                      events.
 * Mar 3, 2014  3034       bkowal      Constant for GFE interoperability flag
 * Apr 28, 2014 3556       bkowal      Updated to use the new hazards common 
 *                                     configuration plugin.
 * Apr 29, 2014 2925       Chris.Golden Moved business logic that was scattered
 *                                      elsewhere into here where it belongs. Also
 *                                      changed notifications being posted to be
 *                                      asynchronous, added notification posting for
 *                                      for when the allowable "until further notice"
 *                                      set has changed, and changed logic of "until
 *                                      further notice" to use the old value for the
 *                                      corresponding attribute or end time when
 *                                      possible when "until further notice" is
 *                                      toggled off. Also added fetching and caching
 *                                      of megawidget specifier managers for hazard
 *                                      events, in support of class-based metadata
 *                                      work.
 * May 15, 2014 2925       Chris.Golden Added methods to set hazard category, set
 *                                      last modified event, and get set of hazards
 *                                      for which proposal is possible. Also added
 *                                      tracking of which selected events have what
 *                                      conflicts with other events, as opposed to
 *                                      calculating it every time it is asked for via
 *                                      the public method. Made a few additional
 *                                      minor changes to support new HID.
 * Jun 24, 2014 4010       Chris.Golden Fixed bug uncovered by testing use of expand
 *                                      bar megawidgets within the event metadata
 *                                      specifiers that caused "until further notice"
 *                                      functionality to fail and give a warning if
 *                                      a time scale megawidget to be manipulated
 *                                      via an UFN checkbox was not a top-level
 *                                      megawidget, but was instead embedded within
 *                                      a parent megawidget.
 * Jun 25, 2014 4009       Chris.Golden Removed all code related to "until further
 *                                      notice" for arbitrary attribute values; only
 *                                      the end time "until further notice" code
 *                                      belongs here. The rest has been moved to
 *                                      interdependency scripts that go with the
 *                                      appropriate metadata megawidget specifiers.
 * Jul 03, 2014 3512       Chris.Golden Added code to set new events, and those that
 *                                      have undergone a type change, to have end
 *                                      times equal to their start times plus their
 *                                      default durations (by event type). Also
 *                                      changed to erase any recorded interval
 *                                      between start and end time before "until
 *                                      further notice" was turned on for an event
 *                                      if the type of the event changes.
 * Aug 20, 2014 4243       Chris.Golden Added implementation of new method to receive
 *                                      notification of a script command having been
 *                                      invoked.
 * Sep 04, 2014 4560       Chris.Golden Added code to find metadata-reload-triggering
 *                                      megawidgets.
 * Sep 16, 2014 4753       Chris.Golden Changed event script to include mutable
 *                                      properties.
 * Nov 18, 2014 4124       Chris.Golden Changed to work with revamped time manager.
 * Dec  1, 2014 4188       Dan Schaffer Now allowing hazards to be shrunk or expanded
 *                                      when appropriate.
 * Dec 05, 2014 4124       Chris.Golden Changed to work with parameterized config
 *                                      manager, and to properly use ObservedSettings.
 * Dec 13, 2014 4486       Dan Schaffer Eliminating effect of changed CAVE time on
 *                                      hazard status.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Jan 08, 2015 5700       Chris.Golden Changed to generalize the meaning of a command
 *                                      invocation for a particular event, since it no
 *                                      longer only means that an event-modifying
 *                                      script is to be executed; it may also trigger
 *                                      a metadata refresh. Previously, the latter was
 *                                      only possible on a hazard attribute state
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventManager implements
        ISessionEventManager<ObservedHazardEvent> {

    private static final String GEOMETRY_MODIFICATION_ERROR = "Geometry Modification Error";

    private static final String EMPTY_GEOMETRY_ERROR = "Deleting this UGC would leave the hazard with an empty geometry";

    /**
     * Contains the mappings between geodatabase table names and the UGCBuilders
     * which correspond to them.
     */
    private static Map<String, IugcToMapGeometryDataBuilder> geoTableUGCBuilderMap;

    /**
     * Look-up IUGCBuilders for tables in the maps geodatabase.
     */
    static {
        Map<String, IugcToMapGeometryDataBuilder> tempMap = new HashMap<>();

        tempMap.put(HazardConstants.MAPDATA_COUNTY, new CountyUGCBuilder());
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
    private final ISessionConfigurationManager<ObservedSettings> configManager;

    private final IHazardEventManager dbManager;

    private final ISessionNotificationSender notificationSender;

    private final List<ObservedHazardEvent> events = new ArrayList<ObservedHazardEvent>();

    private final Deque<String> eventModifications = new LinkedList<String>();

    private Timer eventExpirationTimer = new Timer(true);

    private final Map<String, TimerTask> expirationTasks = new ConcurrentHashMap<String, TimerTask>();

    private ISimulatedTimeChangeListener timeListener;

    private final Set<String> identifiersOfEventsAllowingUntilFurtherNotice = new HashSet<>();

    private final Map<String, Collection<IHazardEvent>> conflictingEventsForSelectedEventIdentifiers = new HashMap<>();

    private final Map<String, MegawidgetSpecifierManager> megawidgetSpecifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> metadataReloadTriggeringIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, File> scriptFilesForEventIdentifiers = new HashMap<>();

    private final Map<String, Map<String, String>> eventModifyingScriptsForEventIdentifiers = new HashMap<>();

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

    private ObservedHazardEvent currentEvent;

    private final MapUtilities mapUtilities;

    /**
     * Listener for event modifying script completions.
     */
    private final IEventModifyingScriptJobListener eventModifyingScriptListener = new IEventModifyingScriptJobListener() {

        @Override
        public void scriptExecutionComplete(String identifier,
                ModifiedHazardEvent hazardEvent) {
            eventModifyingScriptExecutionComplete(hazardEvent);
        }
    };

    /**
     * Comparator can be used with sortEvents to send selected events to the
     * front of the list.
     */
    public static final Comparator<ObservedHazardEvent> SEND_SELECTED_FRONT = new Comparator<ObservedHazardEvent>() {

        @Override
        public int compare(ObservedHazardEvent o1, ObservedHazardEvent o2) {
            boolean s1 = Boolean.TRUE.equals(o1
                    .getHazardAttribute(HAZARD_EVENT_SELECTED));
            boolean s2 = Boolean.TRUE.equals(o2
                    .getHazardAttribute(HAZARD_EVENT_SELECTED));
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

    public SessionEventManager(ISessionTimeManager timeManager,
            ISessionConfigurationManager<ObservedSettings> configManager,
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
        this.mapUtilities = new MapUtilities(this.configManager);

    }

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
            if (Boolean.TRUE.equals(event
                    .getHazardAttribute(HAZARD_EVENT_SELECTED))) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public boolean isSelected(ObservedHazardEvent event) {
        return getSelectedEvents().contains(event);

    }

    @Override
    public void setSelectedEvents(
            Collection<ObservedHazardEvent> selectedEvents,
            IOriginator originator) {
        for (ObservedHazardEvent event : getSelectedEvents()) {
            if (!selectedEvents.contains(event)) {
                event.addHazardAttribute(HAZARD_EVENT_SELECTED, false,
                        originator);
            }
        }
        for (ObservedHazardEvent event : selectedEvents) {
            event.addHazardAttribute(HAZARD_EVENT_SELECTED, true, originator);

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
    public void setSelectedEventForIDs(Collection<String> selectedEventIDs,
            IOriginator originator) {
        Collection<ObservedHazardEvent> selectedEvents = fromIDs(selectedEventIDs);
        setSelectedEvents(selectedEvents, originator);
    }

    @Override
    public Collection<ObservedHazardEvent> getCheckedEvents() {
        Collection<ObservedHazardEvent> allEvents = getEventsForCurrentSettings();
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (Boolean.TRUE.equals(event
                    .getHazardAttribute(HAZARD_EVENT_CHECKED))) {
                events.add(event);
            }
        }
        return events;
    }

    @Handler(priority = 1)
    public void settingsModified(SettingsModified notification) {
        loadEventsForSettings(notification.getSettings());
    }

    @Override
    public Collection<ObservedHazardEvent> getEventsForCurrentSettings() {
        Collection<ObservedHazardEvent> result = getEvents();

        filterEventsForConfig(result);
        return result;
    }

    @Override
    public void setEventCategory(ObservedHazardEvent event, String category,
            IOriginator originator) {
        if (!canChangeType(event)) {
            throw new IllegalStateException("cannot change type of event "
                    + event.getEventID());
        }
        event.addHazardAttribute(ATTR_HAZARD_CATEGORY, category);
        event.setHazardType(null, null, null, Originator.OTHER);
    }

    @Override
    public boolean setEventType(ObservedHazardEvent event, String phenomenon,
            String significance, String subType, IOriginator originator) {
        ObservedHazardEvent oldEvent = null;

        /*
         * If the event cannot change type, create a new event with the new
         * type.
         */
        if (!canChangeType(event)) {
            oldEvent = event;
            IHazardEvent baseEvent = new BaseHazardEvent(event);
            baseEvent.setEventID("");
            baseEvent.setStatus(HazardStatus.PENDING);
            baseEvent.addHazardAttribute(HazardConstants.REPLACES,
                    configManager.getHeadline(oldEvent));

            /*
             * New event should not have product information.
             */
            baseEvent.removeHazardAttribute(EXPIRATION_TIME);
            baseEvent.removeHazardAttribute(ISSUE_TIME);
            baseEvent.removeHazardAttribute(VTEC_CODES);
            baseEvent.removeHazardAttribute(ETNS);
            baseEvent.removeHazardAttribute(PILS);

            /*
             * The originator should be the session manager, since the addition
             * of a new event is occurring.
             */
            originator = Originator.OTHER;

            /*
             * Add the event, and add it to the selection as well. The old
             * selection is fetched before the addition, because the addition
             * will change the selection.
             */
            Collection<ObservedHazardEvent> selection = getSelectedEvents();
            event = addEvent(baseEvent, originator);
            selection.add(event);
            setSelectedEvents(selection, originator);
        }

        /*
         * Change the event type as specified, whether it is being set or
         * cleared.
         */
        if (phenomenon != null) {

            /*
             * This is tricky, but in replace-by operations you need to make
             * sure that modifications to the old event are completed before
             * modifications to the new event. This puts the new event at the
             * top of the modification queue which ultimately controls things
             * like which event tab gets focus in the HID. The originator is
             * also changed to the session manager, since any changes to the
             * type of the new event are being done by the session manager, not
             * by the original originator.
             */
            if (oldEvent != null) {

                IHazardEvent tempEvent = new BaseHazardEvent();
                tempEvent.setPhenomenon(phenomenon);
                tempEvent.setSignificance(significance);
                tempEvent.setSubType(subType);
                oldEvent.addHazardAttribute(REPLACED_BY,
                        configManager.getHeadline(tempEvent), originator);
                oldEvent.setStatus(HazardStatus.ENDING);
            }

            /*
             * Assign the new type.
             */
            event.setHazardType(phenomenon, significance, subType, originator);

            /*
             * Make sure the updated hazard type is a part of the visible types
             * in the current setting. If not, add it.
             */
            Set<String> visibleTypes = configManager.getSettings()
                    .getVisibleTypes();
            visibleTypes.add(HazardEventUtilities.getHazardType(event));
            configManager.getSettings().setVisibleTypes(visibleTypes,
                    Originator.OTHER);
        } else {
            event.setHazardType(null, null, null, originator);
        }

        /*
         * Set the event's end time to have a distance from the start time equal
         * to its default duration. Also remove any recorded interval from
         * before "until further notice" had been turned on, in case it was,
         * since this could lead to the wrong interval being used for the new
         * event type if "until further notice" is subsequently turned off.
         */
        event.setEndTime(new Date(event.getStartTime().getTime()
                + configManager.getDefaultDuration(event)), Originator.OTHER);
        event.removeHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);

        return (originator != Originator.OTHER);
    }

    /**
     * Respond to the addition of a hazard event by firing off a notification
     * that the list of selected events has changed, if appropriate.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardAdded(SessionEventAdded change) {
        if (Boolean.TRUE.equals(change.getEvent().getHazardAttribute(
                HAZARD_EVENT_SELECTED))) {
            notificationSender
                    .postNotificationAsync(new SessionSelectedEventsModified(
                            this, change.getOriginator()));
        }
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
    }

    /**
     * Respond to the addition of a hazard event by firing off a notification
     * that the list of selected events has changed, if appropriate.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardRemoved(SessionEventRemoved change) {
        if (Boolean.TRUE.equals(change.getEvent().getHazardAttribute(
                HAZARD_EVENT_SELECTED))) {
            notificationSender
                    .postNotificationAsync(new SessionSelectedEventsModified(
                            this, change.getOriginator()));
        }
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                true);
    }

    /**
     * Respond to a hazard event's type change by firing off a notification that
     * the event may have new metadata.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardTypeChanged(SessionEventTypeModified change) {
        updateEventMetadata(change.getEvent());
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
    }

    /**
     * Respond to a hazard event's status change by firing off a notification
     * that the event may have new metadata.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardStatusChanged(SessionEventStatusModified change) {
        updateEventMetadata(change.getEvent());
    }

    @Override
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(
            ObservedHazardEvent event) {
        MegawidgetSpecifierManager manager = megawidgetSpecifiersForEventIdentifiers
                .get(event.getEventID());
        if (manager != null) {
            return manager;
        }
        updateEventMetadata(event);
        return megawidgetSpecifiersForEventIdentifiers.get(event.getEventID());
    }

    @Override
    public void eventCommandInvoked(ObservedHazardEvent event,
            String identifier,
            Map<String, Map<String, Object>> mutableProperties) {

        /*
         * If the command that was invoked is a metadata refresh trigger,
         * perform the refresh.
         */
        if (metadataReloadTriggeringIdentifiersForEventIdentifiers
                .containsKey(event.getEventID())
                && metadataReloadTriggeringIdentifiersForEventIdentifiers.get(
                        event.getEventID()).contains(identifier)) {
            updateEventMetadata(event);
            return;
        }

        /*
         * Find the event modifying script that goes with this identifier, if
         * any, and execute it.
         */
        Map<String, String> eventModifyingFunctionNamesForIdentifiers = eventModifyingScriptsForEventIdentifiers
                .get(event.getEventID());
        if (eventModifyingFunctionNamesForIdentifiers == null) {
            statusHandler
                    .error("No event modifying script identifiers found for event "
                            + event.getEventID());
            return;
        }
        String eventModifyingFunctionName = eventModifyingFunctionNamesForIdentifiers
                .get(identifier);
        if (eventModifyingFunctionName == null) {
            statusHandler
                    .error("No event modifying script function name found for identifier "
                            + identifier + " for event " + event.getEventID());
            return;
        }
        configManager.runEventModifyingScript(event,
                scriptFilesForEventIdentifiers.get(event.getEventID()),
                eventModifyingFunctionName, mutableProperties,
                eventModifyingScriptListener);
    }

    /**
     * Respond to the completion of an event modifying script execution.
     * 
     * @param event
     *            Hazard event that was returned, indicating what hazard
     *            attributes have changed. If <code>null</code>, no changes were
     *            made.
     */
    private void eventModifyingScriptExecutionComplete(ModifiedHazardEvent event) {
        IHazardEvent hazardEvent = event.getHazardEvent();
        ObservedHazardEvent originalEvent = getEventById(hazardEvent
                .getEventID());
        if (originalEvent != null) {
            if (event.getMutableProperties() != null) {
                notificationSender
                        .postNotificationAsync(new SessionEventScriptExtraDataAvailable(
                                this, originalEvent, event
                                        .getMutableProperties(),
                                Originator.OTHER));
            }
            originalEvent
                    .setHazardAttributes(hazardEvent.getHazardAttributes());
        }
    }

    /**
     * Update the specified event's metadata in response to some sort of change
     * (creation of the event, updating of status or hazard type) that may
     * result in the available metadata changing.
     * 
     * @param event
     *            Event for which metadata may need updating.
     */
    private void updateEventMetadata(ObservedHazardEvent event) {

        /*
         * Get a new megawidget specifier manager for this event, and store it
         * in the cache. Also get the event modifiers map if one was provided,
         * and cache it away as well.
         */
        HazardEventMetadata metadata = configManager
                .getMetadataForHazardEvent(event);
        MegawidgetSpecifierManager manager = metadata
                .getMegawidgetSpecifierManager();

        assert (manager != null);
        assert (event.getStartTime() != null);
        assert (event.getEndTime() != null);

        megawidgetSpecifiersForEventIdentifiers
                .put(event.getEventID(), manager);
        metadataReloadTriggeringIdentifiersForEventIdentifiers
                .put(event.getEventID(),
                        metadata.getRefreshTriggeringMetadataKeys());
        Map<String, String> eventModifiers = metadata
                .getEventModifyingFunctionNamesForIdentifiers();
        if (eventModifiers != null) {
            scriptFilesForEventIdentifiers.put(event.getEventID(),
                    metadata.getScriptFile());
            eventModifyingScriptsForEventIdentifiers.put(event.getEventID(),
                    eventModifiers);
        }

        /*
         * Fire off a notification that the metadata may have changed for this
         * event.
         */
        notificationSender
                .postNotificationAsync(new SessionEventMetadataModified(this,
                        event, Originator.OTHER));

        /*
         * Get a copy of the current attributes of the hazard event, so that
         * they may be modified as required to work with the new metadata
         * specifiers. Then add any missing specifiers' starting states (and
         * correct those that are not valid for these specifiers), and assign
         * the modified attributes back to the event.
         * 
         * TODO: ObservedHazardEvent should probably return a defensive copy of
         * the attributes, or better yet, an unmodifiable view (i.e. using
         * Collections.unmodifiableMap()), so that the original within the
         * ObservedHazardEvent cannot be modified. This should be done with any
         * other mutable objects returned by ObservedXXXX instances, since they
         * need to know when their components are modified so that they can send
         * out notifications in response.
         * 
         * TODO: Consider making megawidgets take Serializable states, instead
         * of using states of type Object. This is a bit complex, since those
         * states that are of various types of Collection subclasses are not
         * serializable; in those cases it might be difficult to pull this off.
         * For now, copying back and forth between maps holding Object values
         * and those holding Serializable values must be done.
         */
        boolean eventModified = event.isModified();
        Map<String, Serializable> attributes = event.getHazardAttributes();
        Map<String, Object> newAttributes = new HashMap<>(attributes.size());
        for (String name : attributes.keySet()) {
            newAttributes.put(name, attributes.get(name));
        }
        populateTimeAttributesStartingStates(manager.getSpecifiers(),
                newAttributes, event.getStartTime().getTime(), event
                        .getEndTime().getTime());
        manager.populateWithStartingStates(newAttributes);
        attributes = new HashMap<>(newAttributes.size());
        for (String name : newAttributes.keySet()) {
            attributes.put(name, (Serializable) newAttributes.get(name));
        }
        event.setHazardAttributes(attributes);
        event.setModified(eventModified);
    }

    /**
     * Find any time-based megawidget specifiers in the specified list and, for
     * each one, if the given attributes map does not include values for all of
     * its state identifiers, fill in default states for those identifiers.
     * 
     * @param specifiers
     *            Megawidget specifiers.
     * @param attributes
     *            Map of hazard attribute names to their values.
     * @param mininumTime
     *            Minimum time to use when coming up with default values.
     * @param maximumTime
     *            Maximum time to use when coming up with default values.
     */
    @SuppressWarnings("unchecked")
    private void populateTimeAttributesStartingStates(
            List<ISpecifier> specifiers, Map<String, Object> attributes,
            long minimumTime, long maximumTime) {

        /*
         * Iterate through the specifiers, looking for any that are time
         * specifiers and filling in default values for those, and for any that
         * are parent specifiers to as to be able to search their descendants
         * for the same reason.
         */
        for (ISpecifier specifier : specifiers) {
            if (specifier instanceof TimeMegawidgetSpecifier) {

                /*
                 * Determine whether or not the attributes handled by this
                 * specifier already have valid values, meaning that they must
                 * have non-null values that are in increasing order.
                 */
                TimeMegawidgetSpecifier timeSpecifier = ((TimeMegawidgetSpecifier) specifier);
                List<String> identifiers = timeSpecifier.getStateIdentifiers();
                long lastValue = -1L;
                boolean populate = false;
                for (String identifier : identifiers) {
                    Number valueObj = (Number) attributes.get(identifier);
                    if ((valueObj == null)
                            || ((lastValue != -1L) && (lastValue >= valueObj
                                    .longValue()))) {
                        populate = true;
                        break;
                    }
                    lastValue = valueObj.longValue();
                }

                /*
                 * If the values are not valid, create default values for them,
                 * equally spaced between the given minimum and maximum times,
                 * unless there is only one attribute for this specifier, in
                 * which case simply make it the same as the minimum time.
                 */
                if (populate) {
                    long interval = (identifiers.size() == 1 ? 0L
                            : (maximumTime - minimumTime)
                                    / (identifiers.size() - 1L));
                    long defaultValue = (identifiers.size() == 1 ? (minimumTime + maximumTime) / 2L
                            : minimumTime);
                    for (int j = 0; j < identifiers.size(); j++, defaultValue += interval) {
                        String identifier = identifiers.get(j);
                        attributes.put(identifier, defaultValue);
                    }
                }
            }
            if (specifier instanceof IParentSpecifier) {

                /*
                 * Ensure that any descendant time specifiers' attributes have
                 * proper default values as well.
                 */
                populateTimeAttributesStartingStates(
                        ((IParentSpecifier<ISpecifier>) specifier)
                                .getChildMegawidgetSpecifiers(),
                        attributes, minimumTime, maximumTime);
            }
        }
    }

    /**
     * Ensure that toggles of end time "until further notice" flags result in
     * the appropriate time being set to "until further notice" or, if the flag
     * has been set to false, an appropriate default time. Also ensure that any
     * metadata state changes that should trigger a metadata reload do so.
     * Finally, generate notifications that the list of selected hazard events
     * has changed if the selected attribute is found to have been altered.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardAttributesChanged(SessionEventAttributesModified change) {

        /*
         * If the hazard selection attribute has changed and it is within the
         * current set of all events being managed, send out a notification of
         * indicating that selected events have changed, and update the
         * conflicts for selected events map. The reason that the check is made
         * for whether the event is currently being managed is to avoid such
         * updates when an event has its attribute changes before it is added or
         * after it is removed.
         */
        if (change.containsAttribute(HAZARD_EVENT_SELECTED)
                && getEvents().contains(change.getEvent())) {
            notificationSender
                    .postNotificationAsync(new SessionSelectedEventsModified(
                            this, change.getOriginator()));
            updateConflictingEventsForSelectedEventIdentifiers(
                    change.getEvent(), false);
        }

        /*
         * If the end time "until further notice" flag has changed value but was
         * not removed, change the end time in a corresponding manner.
         */
        if (change
                .containsAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)
                && change
                        .getEvent()
                        .getHazardAttributes()
                        .containsKey(
                                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEventEndTimeForUntilFurtherNotice(
                    change.getEvent(),
                    Boolean.TRUE
                            .equals(change
                                    .getEvent()
                                    .getHazardAttribute(
                                            HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)));
        }

        /*
         * If any of the attributes changed are metadata-reload triggers, then
         * reload the metadata.
         */
        Set<String> metadataReloadTriggeringIdentifiers = metadataReloadTriggeringIdentifiersForEventIdentifiers
                .get(change.getEvent().getEventID());
        if ((metadataReloadTriggeringIdentifiers != null)
                && (Sets.intersection(metadataReloadTriggeringIdentifiers,
                        change.getAttributeKeys()).isEmpty() == false)) {
            updateEventMetadata(change.getEvent());
        }
    }

    /**
     * Ensure that changes to an event's time range cause the selected hazard
     * conflicts map to be updated.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardTimeRangeChanged(SessionEventTimeRangeModified change) {
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
    }

    /**
     * Ensure that changes to an event's geometry cause the selected hazard
     * conflicts map to be updated.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardGeometryChanged(SessionEventGeometryModified change) {
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
    }

    @Override
    public Set<String> getEventIdsAllowingUntilFurtherNotice() {
        return Collections
                .unmodifiableSet(identifiersOfEventsAllowingUntilFurtherNotice);
    }

    /**
     * Set the end time for the specified event with respect to the specified
     * value for "until further notice".
     * 
     * @param event
     *            Event to have its end time set.
     * @param untilFurtherNotice
     *            Flag indicating whether or not the end time should be
     *            "until further notice".
     */
    private void setEventEndTimeForUntilFurtherNotice(IHazardEvent event,
            boolean untilFurtherNotice) {

        /*
         * If "until further notice" has been toggled on for the end time, save
         * the current end time for later (in case it is toggled off again), and
         * change the end time to the "until further notice" value; otherwise,
         * change the end time to be the same interval distant from the start
         * time as it was before "until further notice" was toggled on. If no
         * interval is found to have been saved, perhaps due to a metadata
         * change or a type change, just use the default duration for the event.
         */
        if (untilFurtherNotice) {
            if (event
                    .getHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE) == null) {
                long interval = event.getEndTime().getTime()
                        - event.getStartTime().getTime();
                event.addHazardAttribute(
                        HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE,
                        interval);
            }
            event.setEndTime(new Date(
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
        } else if ((event.getEndTime() != null)
                && (event.getEndTime().getTime() == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)) {
            Long interval = (Long) event
                    .getHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            event.removeHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            if (interval == null) {
                interval = configManager.getDefaultDuration(event);
            }
            event.setEndTime(new Date(event.getStartTime().getTime() + interval));
        }
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
            ObservedHazardEvent event, boolean removed) {

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

        /*
         * Make the change required; if this actually results in a change to the
         * set, fire off a notification.
         */
        boolean changed;
        if (allowsUntilFurtherNotice) {
            changed = identifiersOfEventsAllowingUntilFurtherNotice.add(event
                    .getEventID());
        } else {
            changed = identifiersOfEventsAllowingUntilFurtherNotice
                    .remove(event.getEventID());
        }
        if (changed) {
            notificationSender
                    .postNotificationAsync(new SessionEventAllowUntilFurtherNoticeModified(
                            this, event, Originator.OTHER));
        }
    }

    /**
     * Ensure that the end time "until further notice" mode, if present in the
     * specified event, is appropriate; if it is not, remove it.
     * 
     * @param event
     *            Event to be checked.
     */
    private void ensureEventEndTimeUntilFurtherNoticeAppropriate(
            IHazardEvent event) {

        /*
         * If this event cannot have "until further notice", ensure it is not
         * one of its attributes.
         */
        if (identifiersOfEventsAllowingUntilFurtherNotice.contains(event
                .getEventID()) == false) {

            /*
             * If the attributes contains the flag, remove it. If it was set to
             * true, then reset the end time to an appropriate non-"until
             * further notice" value.
             */
            Boolean untilFurtherNotice = (Boolean) event
                    .getHazardAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
            if (untilFurtherNotice != null) {
                event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
                if (untilFurtherNotice.equals(Boolean.TRUE)) {
                    setEventEndTimeForUntilFurtherNotice(event, false);
                }
            }
        }
    }

    private void filterEventsForConfig(Collection<? extends IHazardEvent> events) {
        ISettings settings = configManager.getSettings();
        Set<String> siteIDs = settings.getVisibleSites();
        Set<String> phenSigs = settings.getVisibleTypes();
        Set<HazardStatus> statuses = EnumSet.noneOf(HazardStatus.class);
        for (String state : settings.getVisibleStatuses()) {
            statuses.add(HazardStatus.valueOf(state.toUpperCase()));
        }
        Iterator<? extends IHazardEvent> it = events.iterator();
        while (it.hasNext()) {
            IHazardEvent event = it.next();
            if (!statuses.contains(event.getStatus())) {
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

    private void loadEventsForSettings(ObservedSettings settings) {
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
        Set<String> visibleStatuses = settings.getVisibleStatuses();
        if (visibleStatuses == null || visibleStatuses.isEmpty()) {
            return;
        }
        List<Object> statuses = new ArrayList<Object>(visibleStatuses.size());
        for (String state : visibleStatuses) {
            statuses.add(HazardStatus.valueOf(state.toUpperCase()));
        }
        filters.put(HazardConstants.HAZARD_EVENT_STATUS, statuses);
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
                event = addEvent(event, false, Originator.OTHER);
                for (IHazardEvent histEvent : list) {
                    if (HazardStatus.issuedButNotEnded(histEvent.getStatus())) {
                        event.addHazardAttribute(ATTR_ISSUED, true);
                        break;
                    }
                }
            }
            for (ObservedHazardEvent event : events) {
                scheduleExpirationTask(event);
            }
        }
    }

    @Override
    public ObservedHazardEvent addEvent(IHazardEvent event,
            IOriginator originator) {
        HazardStatus status = event.getStatus();
        if (status == null || status == HazardStatus.PENDING) {
            return addEvent(event, true, originator);
        } else if (status == HazardStatus.POTENTIAL) {
            return addEvent(event, false, originator);
        } else {
            List<IHazardEvent> list = new ArrayList<IHazardEvent>();
            list.add(event);
            filterEventsForConfig(list);
            if (!list.isEmpty()) {
                return addEvent(event, false, originator);
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
    protected ObservedHazardEvent addEvent(IHazardEvent event,
            boolean localEvent, IOriginator originator) {
        ObservedHazardEvent oevent = new ObservedHazardEvent(event, this);

        /*
         * Need to account for the case where the event being added already
         * exists in the event manager. This can happen with recommender
         * callbacks. For example, the ModifyStormTrackTool will modify
         * information corresponding to an existing event.
         */
        String eventID = oevent.getEventID();

        if (eventID != null && eventID.length() > 0) {
            ObservedHazardEvent existingEvent = getEventById(eventID);

            if (existingEvent != null) {
                SessionEventUtilities.mergeHazardEvents(oevent, existingEvent);
                return existingEvent;
            }
        }

        // verify that the hazard was not created server-side to fulfill
        // interoperability requirements
        if ((event.getStatus() == null
                || event.getStatus() == HazardStatus.PENDING || event
                .getStatus() == HazardStatus.POTENTIAL)
                && event.getHazardAttributes().containsKey(
                        HazardConstants.GFE_INTEROPERABILITY) == false) {

            /*
             * Can only add geometry to selected if the hazard type is empty.
             */
            if ((Boolean.TRUE.equals(configManager.getSettings()
                    .getAddGeometryToSelected()))
                    && (event.getHazardType() == null)
                    && (getSelectedEvents().size() == 1)) {
                ObservedHazardEvent existingEvent = getSelectedEvents()
                        .iterator().next();
                Geometry existingGeometries = existingEvent.getGeometry();
                List<Geometry> geometryList = new ArrayList<>();

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
                // combine the geometryCollection together!
                Geometry geom = geometryCollection.union();
                existingEvent.setGeometry(geom);
                existingEvent
                        .removeHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
                return existingEvent;

            } else {
                try {
                    oevent.setEventID(
                            HazardEventUtilities.generateEventID(
                                    configManager.getSiteID(),
                                    CAVEMode.getMode() == CAVEMode.PRACTICE),
                            false, originator);
                } catch (Exception e) {
                    statusHandler.error("Unable to set event id", e);
                }
            }
        }

        ObservedSettings settings = configManager.getSettings();

        Set<String> visibleSites = configManager.getSettings()
                .getVisibleSites();
        if (visibleSites.contains(configManager.getSiteID()) == false) {
            visibleSites.add(configManager.getSiteID());
            configManager.getSettings().setVisibleSites(visibleSites,
                    Originator.OTHER);
        }
        if (configManager.getHazardCategory(oevent) == null
                && oevent.getHazardAttribute(ATTR_HAZARD_CATEGORY) == null) {
            oevent.addHazardAttribute(ATTR_HAZARD_CATEGORY,
                    settings.getDefaultCategory(), false, originator);
        }
        if (oevent.getStartTime() == null) {
            Date timeToUse = timeManager.getCurrentTime();
            Date selectedTime = new Date(timeManager.getSelectedTime()
                    .getLowerBound());
            if (selectedTime.after(timeManager.getCurrentTime())) {
                timeToUse = selectedTime;
            }
            oevent.setStartTime(timeToUse, false, originator);
        }
        if (oevent.getEndTime() == null) {
            long s = oevent.getStartTime().getTime();
            long d = configManager.getDefaultDuration(oevent);
            oevent.setEndTime(new Date(s + d), false, originator);
        }
        if (oevent.getStatus() == null) {
            oevent.setStatus(HazardStatus.PENDING, false, false, originator);
        }

        if (SessionEventUtilities.isEnded(oevent)) {
            oevent.setStatus(HazardStatus.ENDED);
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
                oevent.setSignificance(s.getAbbreviation(), false, originator);
            }
        }
        oevent.setSiteID(configManager.getSiteID(), false, originator);
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
        oevent.setHazardMode(productClass, false, originator);
        synchronized (events) {
            if (localEvent && !Boolean.TRUE.equals(settings.getAddToSelected())) {
                for (IHazardEvent e : events) {
                    e.addHazardAttribute(HAZARD_EVENT_SELECTED, false);
                }
            }
            events.add(oevent);
        }
        oevent.addHazardAttribute(HAZARD_EVENT_SELECTED, false, false,
                originator);
        oevent.addHazardAttribute(HAZARD_EVENT_CHECKED, false, false,
                originator);
        oevent.addHazardAttribute(ATTR_ISSUED,
                HazardStatus.issuedButNotEnded(oevent.getStatus()), false,
                originator);

        if (localEvent) {
            oevent.addHazardAttribute(HAZARD_EVENT_SELECTED, true, false);
        }
        oevent.addHazardAttribute(HAZARD_EVENT_CHECKED, true);
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(oevent, false);
        notificationSender.postNotificationAsync(new SessionEventAdded(this,
                oevent, originator));
        return oevent;
    }

    @Override
    public void removeEvent(ObservedHazardEvent event, IOriginator originator) {
        removeEvent(event, true, originator);
    }

    @Override
    public void removeEvents(Collection<ObservedHazardEvent> events,
            IOriginator originator) {
        /*
         * Avoid concurrent modification since events is backed by this.events
         */
        List<ObservedHazardEvent> eventsToRemove = new ArrayList<ObservedHazardEvent>(
                events);
        for (int i = 0; i < events.size(); i++) {
            removeEvent(eventsToRemove.get(i), true, originator);
        }
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
    private void removeEvent(IHazardEvent event, boolean delete,
            IOriginator originator) {
        synchronized (events) {
            if (events.remove(event)) {
                // TODO this should never delete operation issued events
                // TODO this should not delete the whole list, just any pending
                // or proposed items on the end of the list.
                String eventIdentifier = event.getEventID();
                if (delete) {
                    HazardHistoryList histList = dbManager
                            .getByEventID(eventIdentifier);
                    if (histList != null && !histList.isEmpty()) {
                        dbManager.removeEvents(histList);
                    }
                }
                updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
                        (ObservedHazardEvent) event, true);
                megawidgetSpecifiersForEventIdentifiers.remove(eventIdentifier);
                metadataReloadTriggeringIdentifiersForEventIdentifiers
                        .remove(eventIdentifier);
                scriptFilesForEventIdentifiers.remove(eventIdentifier);
                eventModifyingScriptsForEventIdentifiers
                        .remove(eventIdentifier);
                notificationSender
                        .postNotificationAsync(new SessionEventRemoved(this,
                                event, originator));
            }
        }
    }

    @Override
    public void sortEvents(Comparator<ObservedHazardEvent> comparator) {
        synchronized (events) {
            Collections.sort(events, comparator);
        }
    }

    @Override
    public Collection<ObservedHazardEvent> getEvents() {
        synchronized (events) {
            return new ArrayList<ObservedHazardEvent>(events);
        }
    }

    /**
     * Receive notification from an event that it was modified in any way
     * <strong>except</strong> for status changes (for example, Pending to
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
        addModification(event.getEventID(), notification.getOriginator());
        if (event instanceof ObservedHazardEvent) {
            ((ObservedHazardEvent) event).setModified(true);
        }
        /*
         * TODO The casting here is indicative of a larger problem. Fix it.
         */
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
                (ObservedHazardEvent) event, false);
        ensureEventEndTimeUntilFurtherNoticeAppropriate(event);
        notificationSender.postNotificationAsync(notification);
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
            SessionEventAttributesModified notification) {
        IHazardEvent event = notification.getEvent();
        addModification(event.getEventID(), notification.getOriginator());
        notificationSender.postNotificationAsync(notification);
    }

    /**
     * Receive notification from an event that the latter experienced a status
     * change (for example, Pending to Issued).
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event
     * experiences a status change in the current session, regardless of the
     * source of the change. Additional logic (method calls, etc.) may therefore
     * be added to this method's implementation as necessary if said logic must
     * be run whenever an event is so modified.
     */
    protected void hazardEventStatusModified(
            SessionEventStatusModified notification, boolean persist) {
        if (persist) {

            ObservedHazardEvent event = notification.getEvent();
            HazardStatus newStatus = event.getStatus();

            boolean needsPersist = false;
            switch (newStatus) {
            case ISSUED:
                event.addHazardAttribute(ATTR_ISSUED, true);
                needsPersist = true;
                break;
            case PROPOSED:
                needsPersist = true;
                break;
            case ENDED:
                event.addHazardAttribute(HAZARD_EVENT_SELECTED, false);
                needsPersist = true;
                break;
            default:
                ;// do nothing.
            }
            if (needsPersist) {
                try {
                    IHazardEvent dbEvent = dbManager.createEvent(event);
                    dbEvent.removeHazardAttribute(ATTR_ISSUED);
                    dbEvent.removeHazardAttribute(HAZARD_EVENT_SELECTED);
                    dbEvent.removeHazardAttribute(HAZARD_EVENT_CHECKED);
                    dbEvent.removeHazardAttribute(ATTR_HAZARD_CATEGORY);
                    dbManager.storeEvent(dbEvent);
                    scheduleExpirationTask(event);
                } catch (Throwable e) {
                    statusHandler.handle(Priority.PROBLEM,
                            e.getLocalizedMessage(), e);
                }
            }
        }

        addModification(notification.getEvent().getEventID(),
                notification.getOriginator());
        notificationSender.postNotificationAsync(notification);
        updateConflictingEventsForSelectedEventIdentifiers(
                notification.getEvent(), false);
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
            if (HazardStatus.issuedButNotEnded(event.getStatus())) {
                final String eventId = event.getEventID();
                TimerTask existingTask = expirationTasks.get(eventId);
                if (existingTask != null) {
                    existingTask.cancel();
                    expirationTasks.remove(eventId);
                }
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        event.setStatus(HazardStatus.ENDED, true, true,
                                Originator.OTHER);
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

                for (ObservedHazardEvent event : events) {
                    scheduleExpirationTask(event);
                }
            }
        };
        return timeListener;
    }

    /**
     * Add the specified event identifier to the head of the stack of modified
     * events, removing it from elsewhere in the stack if it is found further
     * down.
     * 
     * @param eventId
     *            Identifier to be added.
     * @param originator
     *            Originator of the modification.
     */
    private void addModification(String eventId, IOriginator originator) {
        if (eventId.equals(eventModifications.peek()) == false) {
            eventModifications.remove(eventId);
            eventModifications.push(eventId);
            notificationSender
                    .postNotificationAsync(new SessionLastChangedEventModified(
                            this, originator));
        }
    }

    @Override
    public ObservedHazardEvent getLastModifiedSelectedEvent() {
        if (eventModifications.isEmpty()) {
            return null;
        }
        ObservedHazardEvent event = getEventById(eventModifications.peek());
        if (event != null
                && Boolean.TRUE.equals(event
                        .getHazardAttribute(HAZARD_EVENT_SELECTED))) {
            return event;
        } else {
            eventModifications.pop();
            return getLastModifiedSelectedEvent();
        }
    }

    @Override
    public void setLastModifiedSelectedEvent(ObservedHazardEvent event,
            IOriginator originator) {
        addModification(event.getEventID(), originator);
    }

    @Override
    public boolean canChangeTimeRange(ObservedHazardEvent event) {
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
    public boolean canChangeType(ObservedHazardEvent event) {
        if (hasEverBeenIssued(event)) {
            return false;
        }
        return true;
    }

    private boolean hasEverBeenIssued(IHazardEvent event) {
        return Boolean.TRUE.equals(event.getHazardAttribute(ATTR_ISSUED));
    }

    @Override
    public Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents() {
        return Collections
                .unmodifiableMap(conflictingEventsForSelectedEventIdentifiers);
    }

    /**
     * Update the map of selected event identifiers to their collections of
     * conflicting events. This is to be called whenever something that affects
     * the selected events' potential conflicts changes.
     * 
     * @param event
     *            Event that has been added, removed, or modified.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event.
     */
    private void updateConflictingEventsForSelectedEventIdentifiers(
            IHazardEvent event, boolean removed) {

        /*
         * TODO: If this is found to take too much time, an optimization could
         * be implemented in which each selected event is checked against the
         * event specified as a parameter. If the latter has been removed, then
         * it would be removed from any conflicts collections, as well as from
         * the map itself if its identifier was a key. Other optimizations could
         * be performed if other changes had occurred.
         * 
         * Currently, however, the entire map is rebuilt from scratch. This is
         * still an improvement over before, when it was rebuilt each time
         * getConflictingEventsForSelectedEvents() was invoked; now it is only
         * rebuilt whenever this method is called in response to a change of
         * some sort.
         */
        Map<String, Collection<IHazardEvent>> oldMap = new HashMap<>(
                conflictingEventsForSelectedEventIdentifiers);
        conflictingEventsForSelectedEventIdentifiers.clear();

        Collection<ObservedHazardEvent> selectedEvents = getSelectedEvents();

        for (IHazardEvent eventToCheck : selectedEvents) {

            Map<IHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
                    eventToCheck, eventToCheck.getStartTime(),
                    eventToCheck.getEndTime(), eventToCheck.getGeometry(),
                    HazardEventUtilities.getHazardType(eventToCheck));

            if (!conflictingHazards.isEmpty()) {
                conflictingEventsForSelectedEventIdentifiers.put(eventToCheck
                        .getEventID(), Collections
                        .unmodifiableSet(conflictingHazards.keySet()));
            }

        }

        if (oldMap.equals(conflictingEventsForSelectedEventIdentifiers) == false) {
            notificationSender
                    .postNotificationAsync(new SessionSelectedEventConflictsModified(
                            this, Originator.OTHER));
        }

    }

    @Override
    public Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> getAllConflictingEvents() {

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictingHazardMap = new HashMap<>();
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

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

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

                    String ugcType = hazardTypeEntry.getUgcType();

                    String ugcLabel = hazardTypeEntry.getUgcLabel();

                    Set<IGeometryData> hatchedAreasForEvent = mapUtilities
                            .buildHatchedAreaForEvent(ugcType, ugcLabel, cwa,
                                    eventToCompare,
                                    isPolygonBased(eventToCompare));

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
                            HazardConstants.HAZARD_EVENT_STATUS,
                            HazardStatus.ISSUED);
                    hazardQueryBuilder.addKey(
                            HazardConstants.HAZARD_EVENT_STATUS,
                            HazardStatus.ENDING);

                    hazardQueryBuilder.addKey(
                            HazardConstants.HAZARD_EVENT_STATUS,
                            HazardStatus.PROPOSED);

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

                                    HazardTypeEntry otherHazardTypeEntry = hazardTypes
                                            .get(otherEventPhenSigSubtype);
                                    String otherUgcType = otherHazardTypeEntry
                                            .getUgcType();
                                    String otherUgcLabel = otherHazardTypeEntry
                                            .getUgcLabel();

                                    if (hazardTypeEntry != null) {
                                        Set<IGeometryData> hatchedAreasEventToCheck = mapUtilities
                                                .buildHatchedAreaForEvent(
                                                        otherUgcType,
                                                        otherUgcLabel,
                                                        cwa,
                                                        eventToCheck,
                                                        isPolygonBased(eventToCheck));

                                        conflictingHazardsMap
                                                .putAll(buildConflictMap(
                                                        eventToCompare,
                                                        eventToCheck,
                                                        hatchedAreasForEvent,
                                                        hatchedAreasEventToCheck,
                                                        ugcLabel, otherUgcLabel));
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
        List<IHazardEvent> eventsToCheck = new ArrayList<IHazardEvent>(
                getEvents());
        Map<String, IHazardEvent> sessionEventMap = new HashMap<>();

        for (IHazardEvent sessionEvent : eventsToCheck) {
            sessionEventMap.put(sessionEvent.getEventID(), sessionEvent);
        }

        for (String eventID : eventMap.keySet()) {
            HazardHistoryList historyList = eventMap.get(eventID);
            IHazardEvent eventFromManager = historyList.get(0);

            if (!sessionEventMap.containsKey(eventID)) {
                if (eventFromManager.getStatus() != HazardStatus.ENDED) {
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
     *            The hatched areas associated with the first event
     * @param hatchedAreasSecondEvent
     *            The hatched areas associated with the second event
     * @param firstEventLabelParameter
     *            The label (if any) associated with the first event hazard
     *            area.
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
            String firstEventLabelParameter, String secondEventLabelParameter) {

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

        List<String> geometryNames = new ArrayList<>();

        if (!isPolygonBased(firstEvent) && !isPolygonBased(secondEvent)) {

            Set<IGeometryData> commonHatchedAreas = new HashSet<>();
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

            if (!isPolygonBased(firstEvent)) {
                labelFieldName = firstEventLabelParameter;
                geoWithLabelInfo = hatchedAreasFirstEvent;
            } else if (!isPolygonBased(secondEvent)) {
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
    public boolean isPolygonBased(IHazardEvent hazardEvent) {
        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);

        HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes().get(
                hazardType);

        return hazardTypeEntry.isPolygonBased();
    }

    @Override
    public void endEvent(ObservedHazardEvent event, IOriginator originator) {
        event.addHazardAttribute(HAZARD_EVENT_SELECTED, false);
        event.setStatus(HazardStatus.ENDED, true, true, originator);
        clearUndoRedo(event);
        event.setModified(false);
    }

    @Override
    public void issueEvent(ObservedHazardEvent event, IOriginator originator) {
        event.clearUndoRedo();
        event.setModified(false);
    }

    @Override
    public Set<String> getEventIdsAllowingProposal() {
        List<ObservedHazardEvent> selectedEvents = getSelectedEvents();
        Set<String> set = new HashSet<String>(selectedEvents.size());
        for (ObservedHazardEvent event : selectedEvents) {
            HazardStatus status = event.getStatus();

            if (isProposedStateAllowed(event, status)) {
                set.add(event.getEventID());
            }
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public void proposeEvent(ObservedHazardEvent event, IOriginator originator) {

        /*
         * Only propose events that are not already proposed, and are not issued
         * or ended, and that have a valid type.
         */
        HazardStatus status = event.getStatus();
        if (isProposedStateAllowed(event, status)) {
            event.setStatus(HazardStatus.PROPOSED, true, true, originator);
            clearUndoRedo(event);
            event.setModified(false);
        }
    }

    @Override
    public void proposeEvents(Collection<ObservedHazardEvent> events,
            IOriginator originator) {
        for (ObservedHazardEvent observedHazardEvent : events) {
            proposeEvent(observedHazardEvent, originator);
        }
    }

    private boolean isProposedStateAllowed(ObservedHazardEvent event,
            HazardStatus status) {
        return !HazardStatus.hasEverBeenIssued(status)
                && status != HazardStatus.PROPOSED
                && event.getPhenomenon() != null;
    }

    @Override
    public boolean clipSelectedHazardGeometries() {
        /*
         * Clip the selected hazard polygons to the forecast area boundary. If
         * the returned polygon is empty, then do not generate the product.
         */
        boolean success = true;

        HazardTypes hazardTypes = configManager.getHazardTypes();
        Collection<ObservedHazardEvent> selectedEvents = this
                .getSelectedEvents();
        String cwa = configManager.getSiteID();

        for (ObservedHazardEvent selectedEvent : selectedEvents) {

            if (!selectedEvent.isClipped()) {
                HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                        .getHazardType());

                if (canBeClipped(selectedEvent, hazardType)) {

                    Set<IGeometryData> geoDataSet = mapUtilities
                            .getClippedMapGeometries(
                                    hazardType.getHazardClipArea(), null, cwa,
                                    selectedEvent);

                    List<Geometry> geometryList = new ArrayList<>();

                    for (IGeometryData geoData : geoDataSet) {
                        for (int i = 0; i < geoData.getGeometry()
                                .getNumGeometries(); ++i) {
                            Geometry geometry = geoData.getGeometry()
                                    .getGeometryN(i);

                            if (!geometry.isEmpty()) {
                                geometryList.add(geometry);
                            }
                        }
                    }

                    if (geometryList.isEmpty()) {
                        StringBuffer warningMessage = new StringBuffer();
                        warningMessage.append("Event "
                                + selectedEvent.getEventID() + " ");
                        warningMessage
                                .append("is outside of the forecast area.\n");
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
                    selectedEvent.setClipped(true);
                }
            }
        }

        return success;
    }

    private boolean canBeClipped(ObservedHazardEvent selectedEvent,
            HazardTypeEntry hazardType) {
        return hazardType != null
                && (!HazardStatus.hasEverBeenIssued(selectedEvent.getStatus()) || (HazardStatus
                        .issuedButNotEnded(selectedEvent.getStatus()) && selectedEvent
                        .isModified()));
    }

    @Override
    public void reduceSelectedHazardGeometries() {

        HazardTypes hazardTypes = configManager.getHazardTypes();
        Collection<ObservedHazardEvent> selectedEvents = getSelectedEvents();

        for (ObservedHazardEvent selectedEvent : selectedEvents) {

            if (!selectedEvent.isReduced()) {
                boolean clippedState = selectedEvent.isClipped();
                HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                        .getHazardType());

                if (canBeClipped(selectedEvent, hazardType)) {

                    /*
                     * Test if point reduction is necessary...
                     */
                    int pointLimit = hazardType.getHazardPointLimit();

                    if (pointLimit > 0) {

                        List<Geometry> geometryList = new ArrayList<>();

                        /**
                         * TODO: Eventually we want to share the same logic
                         * WarnGen uses to reduce points. This is not accessible
                         * right not, at least without creating a dependency
                         * between Hazard Services and WarnGen.
                         */
                        Geometry geometryCollection = selectedEvent
                                .getGeometry();

                        for (int i = 0; i < geometryCollection
                                .getNumGeometries(); ++i) {

                            Geometry geometry = geometryCollection
                                    .getGeometryN(i);

                            if (geometry.getNumPoints() > pointLimit) {

                                double distanceTolerance = DEFAULT_DISTANCE_TOLERANCE;

                                DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(
                                        geometry);
                                Geometry newGeometry = null;

                                do {
                                    simplifier
                                            .setDistanceTolerance(distanceTolerance);
                                    newGeometry = simplifier
                                            .getResultGeometry();
                                    distanceTolerance += DEFAULT_DISTANCE_TOLERANCE_INCREMENT;
                                } while (newGeometry.getNumPoints() > pointLimit);

                                if (!newGeometry.isEmpty()) {
                                    geometryList.add(newGeometry);
                                }

                            } else {
                                geometryList.add(geometry);
                            }
                        }

                        Geometry geoCollection = geoFactory
                                .createGeometryCollection(geometryList
                                        .toArray(new Geometry[0]));
                        selectedEvent.setGeometry(geoCollection);
                        Serializable containedUgcs = (Serializable) buildContainedUGCs(selectedEvent);
                        selectedEvent.addHazardAttribute(CONTAINED_UGCS,
                                containedUgcs);

                        selectedEvent.setReduced(true);
                        selectedEvent.setClipped(clippedState);
                    }

                }
            }
        }
    }

    @Override
    public boolean canEventAreaBeChanged(ObservedHazardEvent hazardEvent) {
        return hazardEvent.getStatus() != HazardStatus.ENDED;
    }

    @Override
    public void setCurrentEvent(String eventId) {
        setCurrentEvent(getEventById(eventId));
    }

    @Override
    public void setCurrentEvent(ObservedHazardEvent event) {
        this.currentEvent = event;
    }

    @Override
    public ObservedHazardEvent getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void noCurrentEvent() {
        this.currentEvent = null;
    }

    @Override
    public boolean isCurrentEvent() {
        return currentEvent != null;
    }

    /**
     * Clears the undo/redo stack for the hazard event.
     * 
     * @param event
     *            Event for which to clear the undo/redo stack
     * @return
     */
    private void clearUndoRedo(IUndoRedoable event) {
        event.clearUndoRedo();
    }

    @Override
    public void updateSelectedHazardUGCs() {

        for (IHazardEvent hazardEvent : getSelectedEvents()) {
            String hazardType = HazardEventUtilities.getHazardType(hazardEvent);

            if (hazardType != null) {
                List<String> ugcs = buildUGCs(hazardEvent);
                hazardEvent.addHazardAttribute(HazardConstants.UGCS,
                        (Serializable) ugcs);
            }
        }

    }

    @Override
    public List<String> buildUGCs(IHazardEvent hazardEvent) {
        String hazardType = hazardEvent.getHazardType();
        String mapDBtableName = configManager.getHazardTypes().get(hazardType)
                .getUgcType();

        Set<IGeometryData> hazardArea;

        if (isPolygonBased(hazardEvent)) {
            hazardArea = mapUtilities.getIntersectingMapGeometries(true,
                    hazardEvent);
        } else {
            hazardArea = mapUtilities.buildHatchedAreaForEvent(hazardEvent,
                    isPolygonBased(hazardEvent));
        }
        Set<String> ugcs = ugcsToGeometryData(mapDBtableName, hazardArea)
                .keySet();

        return new ArrayList<>(ugcs);
    }

    @Override
    public List<String> buildContainedUGCs(IHazardEvent hazardEvent) {

        Set<IGeometryData> hazardArea = mapUtilities
                .getContainedMapGeometries(hazardEvent);
        String hazardType = hazardEvent.getHazardType();
        String mapDBtableName = configManager.getHazardTypes().get(hazardType)
                .getUgcType();

        Set<String> ugcs = ugcsToGeometryData(mapDBtableName, hazardArea)
                .keySet();

        return new ArrayList<>(ugcs);
    }

    private Map<String, IGeometryData> ugcsToGeometryData(
            String mapDBtableName, Set<IGeometryData> geometryData) {
        IugcToMapGeometryDataBuilder ugcBuilder = getUGCBuilder(mapDBtableName);
        Map<String, IGeometryData> result = ugcBuilder
                .ugcsToMapGeometryData(geometryData);
        return result;
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
    private IugcToMapGeometryDataBuilder getUGCBuilder(String geoTableName) {

        if (geoTableUGCBuilderMap.containsKey(geoTableName)) {
            return geoTableUGCBuilderMap.get(geoTableName);
        } else {
            statusHandler.error("No UGC handler found for maps database table "
                    + geoTableName);
            return new NullUGCBuilder();
        }
    }

    @Override
    public boolean isValidGeometryChange(Geometry geometry,
            ObservedHazardEvent hazardEvent) {
        boolean result = true;
        if (!geometry.isValid()) {
            IsValidOp op = new IsValidOp(geometry);
            statusHandler.warn("Invalid Geometry: "
                    + op.getValidationError().getMessage()
                    + ": Geometry modification undone");
            result = false;

        } else if (hasEverBeenIssued(hazardEvent)) {
            HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(hazardEvent));
            if (hazardTypeEntry != null) {
                if (!hazardTypeEntry.isAllowAreaChange()) {
                    @SuppressWarnings("unchecked")
                    List<String> oldUGCs = (List<String>) hazardEvent
                            .getHazardAttribute(HazardConstants.UGCS);
                    ObservedHazardEvent eventWithNewGeometry = new ObservedHazardEvent(
                            hazardEvent, this);
                    eventWithNewGeometry.setGeometry(geometry);
                    List<String> newUGCs = buildUGCs(eventWithNewGeometry);
                    newUGCs.removeAll(oldUGCs);

                    if (!newUGCs.isEmpty()) {
                        statusHandler
                                .warn("This hazard event cannot be expanded in area.  Please create a new hazard event for the new areas.");
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    private Collection<ObservedHazardEvent> fromIDs(Collection<String> eventIDs) {
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                eventIDs.size());
        for (String eventId : eventIDs) {
            events.add(getEventById(eventId));
        }
        return events;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager
     * #addOrRemoveEnclosingUGCs(com.vividsolutions.jts.geom.Coordinate)
     * 
     * TODO. Fails if you start off by creating a hazard via "Select by County".
     * The reason is that the geometry created in this fashion is not the same
     * geometry you get when you MB3 in a location and retrieve the enclosing
     * geometry.
     * 
     * TODO. Fails if you have already clipped the hazard.
     */
    @Override
    public void addOrRemoveEnclosingUGCs(Coordinate location) {
        List<ObservedHazardEvent> selectedEvents = getSelectedEvents();
        if (selectedEvents.size() != 1) {
            messenger
                    .getWarner()
                    .warnUser(GEOMETRY_MODIFICATION_ERROR,
                            "Cannot add or remove UGCs unless exactly one hazard event is selected");
            return;
        }
        ObservedHazardEvent hazardEvent = selectedEvents.get(0);
        String hazardType = hazardEvent.getHazardType();
        if (hazardType == null) {
            messenger
                    .getWarner()
                    .warnUser(GEOMETRY_MODIFICATION_ERROR,
                            "Cannot add or remove UGCs for a hazard with an undefined type");
            return;
        }
        if (hazardEvent.getStatus().equals(HazardStatus.ENDED)) {
            messenger.getWarner().warnUser(GEOMETRY_MODIFICATION_ERROR,
                    "Cannot add or remove UGCs for an ended hazard");
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> hazardUGCs = (List<String>) hazardEvent
                .getHazardAttribute(CONTAINED_UGCS);
        String mapDBtableName = configManager.getHazardTypes().get(hazardType)
                .getUgcType();

        String mapLabelParameter = configManager.getHazardTypes()
                .get(hazardType).getUgcLabel();

        String cwa = configManager.getSiteID();
        Set<IGeometryData> mapGeometryData = mapUtilities.getMapGeometries(
                mapDBtableName, mapLabelParameter, cwa);
        Geometry locationAsGeometry = geoFactory.createPoint(location);
        Set<IGeometryData> mapGeometryDataContainingLocation = mapUtilities
                .getContainingMapGeometries(mapGeometryData, locationAsGeometry);

        Map<String, IGeometryData> ugcsEnclosingUserSelectedLocation = ugcsToGeometryData(
                mapDBtableName, mapGeometryDataContainingLocation);

        Map<String, IGeometryData> allUGCs = ugcsToGeometryData(mapDBtableName,
                mapGeometryData);

        Geometry hazardEventGeometry = hazardEvent.getGeometry();

        /*
         * Handle the possibility that a hazard geometry can include single
         * {@link Point}s as well as polygons by pulling them out, taking the
         * adding or removing the UGC to the union of the polygons and then
         * putting everything back together into a {@link GeometryCollection}.
         * By ensuring any point geometries are not merged into surrounding
         * polygon geometries, the points remain distinct in the resulting
         * modified geometry.
         */
        GeometryCollection asGeometryCollection = (GeometryCollection) hazardEventGeometry;
        List<Geometry> modifiedGeometries = new ArrayList<>();
        List<Geometry> pointHazards = collectPointGeometries(asGeometryCollection);
        modifiedGeometries.addAll(pointHazards);
        List<Geometry> nonPointHazards = collectNonPointGeometries(asGeometryCollection);

        if (!nonPointHazards.isEmpty()) {
            Geometry nonPointGeometry = asUnion(nonPointHazards);
            for (String enclosingUGC : ugcsEnclosingUserSelectedLocation
                    .keySet()) {
                Geometry enclosingUgcGeometry = allUGCs.get(enclosingUGC)
                        .getGeometry();
                if (nonPointGeometry.equalsTopo(enclosingUgcGeometry)) {
                    statusHandler.warn(EMPTY_GEOMETRY_ERROR);
                    return;
                }
                if (hazardUGCs.contains(enclosingUGC)
                        && !enclosingUgcGeometry.contains(nonPointGeometry)) {

                    nonPointGeometry = nonPointGeometry
                            .difference(enclosingUgcGeometry);
                    hazardUGCs.remove(enclosingUGC);
                } else {
                    nonPointGeometry = nonPointGeometry
                            .union(enclosingUgcGeometry);
                    hazardUGCs.add(enclosingUGC);
                }
            }
            modifiedGeometries.add(nonPointGeometry);
        }

        hazardEventGeometry = geoFactory
                .createGeometryCollection(modifiedGeometries
                        .toArray(new Geometry[0]));
        hazardEvent.setGeometry(hazardEventGeometry);
        hazardEvent.addHazardAttribute(CONTAINED_UGCS,
                (Serializable) hazardUGCs);

    }

    private List<Geometry> collectPointGeometries(
            GeometryCollection geometryCollection) {
        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            Geometry g = geometryCollection.getGeometryN(i);
            if (g instanceof Point) {
                result.add(g);
            }
        }
        return result;
    }

    private List<Geometry> collectNonPointGeometries(
            GeometryCollection geometryCollection) {
        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            Geometry g = geometryCollection.getGeometryN(i);
            if (!(g instanceof Point)) {
                result.add(g);
            }
        }
        return result;
    }

    private Geometry asUnion(List<Geometry> geometries) {
        Geometry result = geometries.get(0);
        for (int i = 1; i < geometries.size(); i++) {
            result = result.union(geometries.get(i));
        }
        return result;
    }
}
