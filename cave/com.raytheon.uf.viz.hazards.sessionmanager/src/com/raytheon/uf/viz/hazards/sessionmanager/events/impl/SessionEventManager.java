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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ETNS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EXPIRATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ISSUE_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PILS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PREVIEW_STATE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.REPLACED_BY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VTEC_CODES;
import gov.noaa.gsd.viz.megawidgets.IParentSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.TimeMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier;

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

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.time.DateUtils;

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
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAllowUntilFurtherNoticeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventMetadataModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionLastChangedEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.hatching.HatchingUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventManager extends AbstractSessionEventManager {

    /**
     * Default duration for hazard events that do not have a type.
     */
    private static final long DEFAULT_HAZARD_DURATION = TimeUnit.HOURS
            .toMillis(8);

    /**
     * Default interval between two attributes for when the second attribute's
     * "until further notice" is toggled off.
     */
    private static final long DEFAULT_INTERVAL_AFTER_UNTIL_FURTHER_NOTICE = TimeUnit.HOURS
            .toMillis(1);

    /**
     * Contains the mappings between geodatabase table names and the UGCBuilders
     * which correspond to them.
     */
    private static Map<String, IUGCBuilder> geoTableUGCBuilderMap;

    /**
     * Look-up IUGCBuilders for tables in the maps geodatabase.
     */
    static {
        Map<String, IUGCBuilder> tempMap = new HashMap<>();

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

    private final List<ObservedHazardEvent> events = new ArrayList<ObservedHazardEvent>();

    private final Deque<String> eventModifications = new LinkedList<String>();

    private Timer eventExpirationTimer = new Timer(true);

    private final Map<String, TimerTask> expirationTasks = new ConcurrentHashMap<String, TimerTask>();

    private ISimulatedTimeChangeListener timeListener;

    private final Set<String> identifiersOfEventsAllowingUntilFurtherNotice = new HashSet<>();

    private final Map<String, Collection<IHazardEvent>> conflictingEventsForSelectedEventIdentifiers = new HashMap<>();

    private final Map<String, MegawidgetSpecifierManager> megawidgetSpecifiersForEventIdentifiers = new HashMap<>();

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
                oldEvent.addHazardAttribute(PREVIEW_STATE,
                        HazardConstants.HazardStatus.ENDED.getValue(),
                        originator);
            }

            /*
             * Assign the new type.
             */
            event.setHazardType(phenomenon, significance, subType, originator);

            /*
             * Make sure the updated hazard type is a part of the visible types
             * in the current setting. If not, add it.
             * 
             * TODO: ObservedSettings should use defensive copying and return a
             * copy of the visibleTypes, but since it doesn't, the copying is
             * done here. Once the lack of defensive copying is addressed, this
             * copying can be removed.
             */
            Set<String> visibleTypes = new HashSet<>(configManager
                    .getSettings().getVisibleTypes());
            visibleTypes.add(HazardEventUtilities.getHazardType(event));
            configManager.getSettings().setVisibleTypes(visibleTypes);
        } else {
            event.setHazardType(null, null, null, originator);
        }
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
                ATTR_SELECTED))) {
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
                ATTR_SELECTED))) {
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

    /**
     * Update the specified event's metadata in response to some sort of change
     * (creation of the event, updating of status or hazard type) that may
     * result in the available metadata changing.
     * 
     * @param event
     *            Event for which metadata may need updating.
     */
    private void updateEventMetadata(IHazardEvent event) {

        /*
         * Get a new megawidget specifier manager for this event, and store it
         * in the cache.
         */
        MegawidgetSpecifierManager manager = configManager
                .getMegawidgetSpecifiersForHazardEvent(event);

        assert (manager != null);
        assert (event.getStartTime() != null);
        assert (event.getEndTime() != null);

        megawidgetSpecifiersForEventIdentifiers
                .put(event.getEventID(), manager);

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
        boolean eventModified = ((ObservedHazardEvent) event).isModified();
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
        ((ObservedHazardEvent) event).setModified(eventModified);
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
     * Ensure that toggles of "until further notice" flags result in the
     * appropriate time being set to "until further notice" or, if the flag has
     * been set to false, an appropriate default time. Also generate
     * notifications that the list of selected hazard events has changed if the
     * selected attribute is found to have been altered.
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
        if (change.containsAttribute(ATTR_SELECTED)
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
         * For any attribute with the "until further notice" suffix that has
         * changed value, find the corresponding attribute in a time scale
         * specifier, and change its value appropriately.
         */
        for (String key : change.getAttributeKeys()) {
            if (key.endsWith(HazardConstants.UNTIL_FURTHER_NOTICE_SUFFIX)
                    && (key.equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE) == false)) {

                /*
                 * Get the name of the attribute that should have its value
                 * changed as a result of the "until further notice" toggle.
                 */
                String attributeBeingChanged = getAttributeForUntilFurtherNoticeAttribute(key);
                if (attributeBeingChanged == null) {
                    continue;
                }

                /*
                 * Find the time scale specifier that includes this attribute.
                 * It may be the last attribute in a multi-state specifier, or
                 * its only state.
                 */
                ObservedHazardEvent event = (ObservedHazardEvent) change
                        .getEvent();
                TimeScaleSpecifier timeScaleSpecifier = getTimeScaleSpecifierForAttribute(
                        event, attributeBeingChanged, key);
                if (timeScaleSpecifier == null) {
                    continue;
                }
                boolean singleStateSpecifier = timeScaleSpecifier
                        .getIdentifier().equals(attributeBeingChanged);

                /*
                 * Get the name of the attribute used to store the information
                 * concerning the previous value of the attribute to be changed.
                 */
                String lastValueKey = HazardConstants.BEFORE_UNTIL_FURTHER_NOTICE_PREFIX
                        + attributeBeingChanged;

                /*
                 * If the specifier has only one state, just use the last-value
                 * attribute as the last value before "until further notice" was
                 * toggled on; if it has multiple states, use the last-value
                 * attribute to hold the interval between the value of the
                 * attribute to be changed, and the previous attribute within
                 * the specifier.
                 */
                boolean untilFurtherNotice = Boolean.TRUE.equals(event
                        .getHazardAttribute(key));
                boolean untilFurtherNoticeWasOn = ((event
                        .getHazardAttribute(attributeBeingChanged) instanceof Long) && ((Long) event
                        .getHazardAttribute(attributeBeingChanged) == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
                if (singleStateSpecifier) {
                    setSingleStateAttributeForUntilFurtherNoticeChange(event,
                            attributeBeingChanged, lastValueKey,
                            untilFurtherNotice, untilFurtherNoticeWasOn);
                } else {
                    setMultiStateAttributeForUntilFurtherNoticeChange(event,
                            timeScaleSpecifier, attributeBeingChanged,
                            lastValueKey, untilFurtherNotice,
                            untilFurtherNoticeWasOn);
                }
            }
        }
    }

    /**
     * Get the event attribute name associated with the specified
     * "until further notice" event attribute name.
     * 
     * @param key
     *            Name of the "until further notice" attribute.
     * @return Name of the associated attribute, or <code>null</code> if it
     *         cannot be taken from the specified key.
     */
    private String getAttributeForUntilFurtherNoticeAttribute(String key) {
        int endIndex = key.length()
                - HazardConstants.UNTIL_FURTHER_NOTICE_SUFFIX.length();
        if (endIndex < 1) {
            statusHandler.error("Illegal to use \""
                    + HazardConstants.UNTIL_FURTHER_NOTICE_SUFFIX
                    + "\" as complete metadata identifier; must be "
                    + "suffix for identifier to which until "
                    + "further notice may be applied.");
            return null;
        }
        return key.substring(0, endIndex);
    }

    /**
     * Get the time scale megawidget specifier associated with the specified
     * key.
     * 
     * @param event
     *            Hazard event for which the specifier is to be fetched.
     * @param key
     *            Key with which the specifier to be fetched is associated,
     *            meaning that said specifier has the key as one of its state
     *            identifiers.
     * @param untilFurtherNoticeKey
     *            Key of the "until further notice" attribute that may be used
     *            to modify the <code>key</code> attribute's value.
     * @return Time scale specifier, or <code>null</code> if no time scale
     *         specifier can be found for the specified key.
     */
    private TimeScaleSpecifier getTimeScaleSpecifierForAttribute(
            ObservedHazardEvent event, String key, String untilFurtherNoticeKey) {
        String megawidgetIdentifierSuffix = ":" + key;
        MegawidgetSpecifierManager specifierManager = getMegawidgetSpecifiers(event);
        ISpecifier targetSpecifier = null;
        for (ISpecifier specifier : specifierManager.getSpecifiers()) {
            if (specifier.getIdentifier().endsWith(megawidgetIdentifierSuffix)
                    || specifier.getIdentifier().equals(key)) {
                targetSpecifier = specifier;
                break;
            }
        }
        if ((targetSpecifier instanceof TimeScaleSpecifier) == false) {
            statusHandler.warn("Unable to find time scale specifier "
                    + "for attribute \"" + key
                    + "\" that may be manipulated by toggling of "
                    + "attribute \"" + untilFurtherNoticeKey + "\".");
            return null;
        }
        return (TimeScaleSpecifier) targetSpecifier;
    }

    /**
     * Set the specified attribute for the specified event to use either the
     * "until further notice" value (if "until further notice" is true), or the
     * value it was using before "until further notice" was last turned on. This
     * method is suitable for use when the time scale specifier used to modify
     * the attribute has only a single state.
     * 
     * @param event
     *            Event to have its attribute values modified.
     * @param key
     *            Attribute to which the "until further notice" value is to be
     *            assigned (if "until further notice" is being turned on), or
     *            the previously saved value is to be assigned (if being turned
     *            off).
     * @param lastValueKey
     *            Attribute to which the value that <code>key</code> had prior
     *            to the last switching on of "until further notice" was
     *            assigned.
     * @param untilFurtherNotice
     *            Flag indicating whether or not "until further notice" has been
     *            switched on.
     * @param untilFurtherNoticeWasOn
     *            Flag indicating that "until further notice" was previously on
     *            for <code>key</code>, which is to say that the attribute with
     *            that name was set to the special "until further notice" value.
     */
    private void setSingleStateAttributeForUntilFurtherNoticeChange(
            ObservedHazardEvent event, String key, String lastValueKey,
            boolean untilFurtherNotice, boolean untilFurtherNoticeWasOn) {

        /*
         * If "until further notice" has been toggled on, remember the old value
         * for this attribute before giving it the special
         * "until further notice" value. If instead it was toggled off, use the
         * old value stored when it was toggled on to set its new current value.
         * In the latter case, check to ensure that the old value stored is
         * indeed a Date, since it is possible (although very unlikely) that the
         * metadata may have changed for this event, making it have a
         * single-state time scale specifier where it used to have a multi-state
         * one.
         */
        if (untilFurtherNotice) {
            if (event.getHazardAttribute(lastValueKey) == null) {
                event.addHazardAttribute(lastValueKey,
                        new Date((Long) event.getHazardAttribute(key)), false,
                        Originator.OTHER);
            }
            event.addHazardAttribute(key,
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
        } else if (untilFurtherNoticeWasOn) {
            Object savedValue = event.getHazardAttribute(lastValueKey);
            event.removeHazardAttribute(lastValueKey, false, Originator.OTHER);
            event.addHazardAttribute(
                    key,
                    (savedValue instanceof Date ? (Date) savedValue : event
                            .getEndTime()).getTime());
        }
    }

    /**
     * Set the specified attribute for the specified event to use either the
     * "until further notice" value (if "until further notice" is true), or the
     * value it was using before "until further notice" was last turned on. This
     * method is suitable for use when the time scale specifier used to modify
     * the attribute has multiple states, one of which is this attribute.
     * 
     * @param event
     *            Event to have its attribute values modified.
     * @param specifier
     *            Time scale specifier that is used to modify the attribute
     *            named <code>key</code>. It is assumed that the latter is the
     *            last of its state identifiers.
     * @param key
     *            Attribute to which the "until further notice" value is to be
     *            assigned (if "until further notice" is being turned on), or
     *            the previously saved value is to be assigned (if being turned
     *            off).
     * @param lastValueKey
     *            Attribute to which the value that <code>key</code> had prior
     *            to the last switching on of "until further notice" was
     *            assigned.
     * @param untilFurtherNotice
     *            Flag indicating whether or not "until further notice" has been
     *            switched on.
     * @param untilFurtherNoticeWasOn
     *            Flag indicating that "until further notice" was previously on
     *            for <code>key</code>, which is to say that the attribute with
     *            that name was set to the special "until further notice" value.
     */
    private void setMultiStateAttributeForUntilFurtherNoticeChange(
            ObservedHazardEvent event, TimeScaleSpecifier specifier,
            String key, String lastValueKey, boolean untilFurtherNotice,
            boolean untilFurtherNoticeWasOn) {

        /*
         * If "until further notice" has been toggled on, remember the interval
         * between this attribute and the previous attribute in the time scale
         * specifier. If instead it was toggled off, find the old value that was
         * stored. If it is a Date object (which is very unlikely, but would
         * occur if the metadata has changed for this event since the last
         * toggle, and it changed from a single-state specifier to a multi-state
         * one at that time), use that as the time if possible, or if too early,
         * use a time one hour after the previous attribute's time. However, it
         * will generally be a long integer interval; in that case, add said
         * interval to the previous attribute's value to get a new Date and use
         * that.
         */
        String previousAttribute = specifier.getStateIdentifiers().get(
                specifier.getStateIdentifiers().size() - 2);
        if (untilFurtherNotice) {
            if (event.getHazardAttribute(lastValueKey) == null) {
                long interval = ((Long) event.getHazardAttribute(key))
                        - ((Long) event.getHazardAttribute(previousAttribute));
                event.addHazardAttribute(lastValueKey, interval, false,
                        Originator.OTHER);
            }
            event.addHazardAttribute(key,
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
        } else if (untilFurtherNoticeWasOn) {
            Object savedValue = event.getHazardAttribute(lastValueKey);
            event.removeHazardAttribute(lastValueKey, false, Originator.OTHER);
            Long newValue;
            if (savedValue instanceof Long) {
                newValue = ((Long) event.getHazardAttribute(previousAttribute))
                        + (Long) savedValue;
            } else {
                Long previousAttributeValue = (Long) event
                        .getHazardAttribute(previousAttribute);
                if ((savedValue == null)
                        || (((Date) savedValue).getTime() <= previousAttributeValue)) {
                    newValue = previousAttributeValue
                            + DEFAULT_INTERVAL_AFTER_UNTIL_FURTHER_NOTICE;
                } else {
                    newValue = ((Date) savedValue).getTime();
                }
            }
            event.addHazardAttribute(key, newValue);
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
         * time as it was before "until further notice" was toggled on. (If no
         * interval was saved, perhaps due to a metadata change, just use 4
         * hours as the default interval; this is an edge case.)
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
                interval = DEFAULT_HAZARD_DURATION;
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
        Settings settings = configManager.getSettings();
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
                    if (histEvent.getStatus() == HazardStatus.ISSUED) {
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
                oevent.setEventID(generateEventID(), false, originator);
            }
        }

        Settings settings = configManager.getSettings();

        if (configManager.getHazardCategory(oevent) == null
                && oevent.getHazardAttribute(ATTR_HAZARD_CATEGORY) == null) {
            oevent.addHazardAttribute(ATTR_HAZARD_CATEGORY,
                    settings.getDefaultCategory(), false, originator);
        }
        if (oevent.getStartTime() == null) {
            oevent.setStartTime(timeManager.getSelectedTime(), false,
                    originator);
        }
        if (oevent.getEndTime() == null) {
            long s = oevent.getStartTime().getTime();
            long d = settings.getDefaultDuration();
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
                    e.addHazardAttribute(ATTR_SELECTED, false);
                }
            }
            events.add(oevent);
        }
        oevent.addHazardAttribute(ATTR_SELECTED, false, false, originator);
        oevent.addHazardAttribute(ATTR_CHECKED, false, false, originator);
        oevent.addHazardAttribute(ATTR_ISSUED,
                oevent.getStatus().equals(HazardStatus.ISSUED), false,
                originator);

        if (localEvent) {
            oevent.addHazardAttribute(ATTR_SELECTED, true);
        }
        oevent.addHazardAttribute(ATTR_CHECKED, true);
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(oevent, false);
        notificationSender.postNotificationAsync(new SessionEventAdded(this,
                oevent, originator));
        return oevent;
    }

    @Override
    public void removeEvent(IHazardEvent event, IOriginator originator) {
        removeEvent(event, true, originator);
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
                if (delete) {
                    HazardHistoryList histList = dbManager.getByEventID(event
                            .getEventID());
                    if (histList != null && !histList.isEmpty()) {
                        dbManager.removeEvents(histList);
                    }
                }
                updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(event,
                        true);
                megawidgetSpecifiersForEventIdentifiers.remove(event
                        .getEventID());
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
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(event, false);
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

            ObservedHazardEvent event = (ObservedHazardEvent) notification
                    .getEvent();
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
                event.addHazardAttribute(ATTR_SELECTED, false);
                needsPersist = true;
                break;
            default:
                ;// do nothing.
            }
            if (needsPersist) {
                notificationSender.postNotification(new SessionEventModified(
                        this, event, notification.getOriginator()));
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
            if (event.getStatus() == HazardStatus.ISSUED) {
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
                    if (event.getStatus() == HazardStatus.ENDED) {
                        event.setStatus(HazardStatus.ISSUED);
                    }
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
                && Boolean.TRUE.equals(event.getHazardAttribute(ATTR_SELECTED))) {
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
    public boolean canChangeGeometry(ObservedHazardEvent event) {
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

    private String generateEventID() {
        HazardEventIdRequest request = new HazardEventIdRequest();
        request.setSiteId(configManager.getSiteID());
        request.setPractice(CAVEMode.getMode() == CAVEMode.PRACTICE);
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
                            HazardConstants.HAZARD_EVENT_STATUS,
                            HazardStatus.ISSUED);

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

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

        List<String> geometryNames = new ArrayList<>();

        if (!firstEventHatchArea.equalsIgnoreCase(HazardConstants.POLYGON_TYPE)
                && !secondEventHatchArea
                        .equalsIgnoreCase(HazardConstants.POLYGON_TYPE)) {

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
    public void endEvent(ObservedHazardEvent event, IOriginator originator) {
        event.addHazardAttribute(ISessionEventManager.ATTR_SELECTED, false);
        event.setStatus(HazardStatus.ENDED, true, true, originator);
        clearUndoRedo(event);
        event.setModified(false);
    }

    @Override
    public void issueEvent(ObservedHazardEvent event, IOriginator originator) {
        event.setStatus(HazardStatus.ISSUED, true, true, originator);
        clearUndoRedo(event);
        event.setModified(false);
    }

    @Override
    public Set<String> getEventIdsAllowingProposal() {
        List<ObservedHazardEvent> selectedEvents = getSelectedEvents();
        Set<String> set = new HashSet<String>(selectedEvents.size());
        for (ObservedHazardEvent event : selectedEvents) {
            HazardStatus status = event.getStatus();
            if ((status != HazardStatus.ISSUED)
                    && (status != HazardStatus.ENDED)
                    && (status != HazardStatus.PROPOSED)
                    && (event.getPhenomenon() != null)) {
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
        if ((status != HazardStatus.ISSUED) && (status != HazardStatus.ENDED)
                && (status != HazardStatus.PROPOSED)
                && (event.getPhenomenon() != null)) {
            event.setStatus(HazardStatus.PROPOSED, true, true, originator);
            clearUndoRedo(event);
            event.setModified(false);
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
        Collection<ObservedHazardEvent> selectedEvents = this
                .getSelectedEvents();
        String cwa = configManager.getSiteID();

        for (ObservedHazardEvent selectedEvent : selectedEvents) {

            if (!selectedEvent.isClipped()) {
                HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                        .getHazardType());

                if ((selectedEvent.getStatus() != HazardStatus.ENDED && selectedEvent
                        .getStatus() != HazardStatus.ISSUED)
                        || (selectedEvent.getStatus() == HazardStatus.ISSUED
                                && hazardType.isAllowAreaChange() && selectedEvent
                                    .isModified())) {

                    Set<IGeometryData> geoDataSet = HatchingUtilities
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

    @Override
    public void reduceSelectedHazardGeometries() {

        HazardTypes hazardTypes = configManager.getHazardTypes();
        Collection<ObservedHazardEvent> selectedEvents = getSelectedEvents();

        for (ObservedHazardEvent selectedEvent : selectedEvents) {

            if (!selectedEvent.isReduced()) {
                boolean clippedState = selectedEvent.isClipped();
                HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                        .getHazardType());

                if ((selectedEvent.getStatus() != HazardStatus.ENDED && selectedEvent
                        .getStatus() != HazardStatus.ISSUED)
                        || (selectedEvent.getStatus() == HazardStatus.ISSUED
                                && hazardType.isAllowAreaChange() && selectedEvent
                                    .isModified())) {

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

                        selectedEvent.setReduced(true);
                        selectedEvent.setClipped(clippedState);
                    }

                }
            }
        }
    }

    @Override
    public boolean canEventAreaBeChanged(ObservedHazardEvent hazardEvent) {
        HazardTypes hazardTypes = configManager.getHazardTypes();

        HazardTypeEntry hazardTypeEntry = hazardTypes.get(hazardEvent
                .getHazardType());

        if (hazardTypeEntry != null
                && hazardEvent.getStatus() == HazardStatus.ISSUED) {
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
    private void clearUndoRedo(IUndoRedoable event) {
        event.clearUndoRedo();
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
