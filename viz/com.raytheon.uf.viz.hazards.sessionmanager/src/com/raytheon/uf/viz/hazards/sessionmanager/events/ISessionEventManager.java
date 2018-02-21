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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Range;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent.Source;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

/**
 * Manages all events in a session.
 * <p>
 * Note that the events provided by {@link #getEvents()} is the list of all
 * events that have been managed within this session, whether they are displayed
 * currently or not. In contrast, {@link #getEventsForCurrentSettings()} returns
 * the subset of the list provided by <code>getEvents()</code>, filtered by the
 * current settings to remove any events that do not have a visible hazard type,
 * status, or site identifier. The {@link #getEventHistoryById(String)} method
 * returns the list of historical events, if any, for the specified event
 * identifier.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * Oct 22, 2013 1463       blawrence   Added methods for hazard conflict
 *                                     detection.
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * Nov 29, 2013 2378       blawrenc    Added methods for proposing,
 *                                     issuing and ending hazard 
 *                                     events. This keeps the 
 *                                     knowledge of what to do
 *                                     in these cases better
 *                                     encapsulated in the
 *                                     event manager.
 * Apr 09, 2014 2925       Chris.Golden Added method to set event type, and anotherto get the
 *                                      megawidget specifier manager for a given hazard event.
 * May 15, 2014 2925       Chris.Golden Added methods to set hazard category, set last modified
 *                                      event, and get set of hazards for which proposal is
 *                                      possible. Also changed getSelectedEvents() to return a
 *                                      list.
 * Aug 20, 2014 4243       Chris.Golden Added new method to receive notification of a script
 *                                      command having been invoked.
 * Sep 16, 2014 4753       Chris.Golden Changed event script to include mutable properties.
 * Dec  1, 2014 4188       Dan Schaffer Now allowing hazards to be shrunk or expanded when appropriate.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Jan 08, 2015 5700       Chris.Golden Changed to generalize the meaning of a command invocation
 *                                      for a particular event, since it no longer only means
 *                                      that an event-modifying script is to be executed.
 * Jan  7, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect hazard area designation.
 * Feb  1, 2015 2331       Chris.Golden Added code to track the allowable boundaries of all hazard
 *                                      events' start and end times, so that the user will not move
 *                                      them beyond the allowed ranges.
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Mar 13, 2015 6090       Dan Schaffer Relaxed geometry validity check.
 * Sep 15, 2015 7629       Robert.Blum  Added method that persists a list of events.
 * Feb 24, 2016 14667      Robert.Blum  Limiting Flash Flood Recommender to basins inside the CWA.
 * Mar 14, 2016 12145      mduff        Handle the case where a new event identifier cannot be
 *                                      generated.
 * Mar 24, 2016 15676      Chris.Golden Changed setModifiedEventGeometry() to return true if it
 *                                      succeeds in changing the geometry, false otherwise.
 * Mar 26, 2016 15676      Chris.Golden Removed geometry validity checks (that is, checks to see
 *                                      if Geometry objects pass the isValid() test), as the
 *                                      session event manager shouldn't be policing this; it should
 *                                      assume it gets valid geometries.
 * Apr 04, 2016 15192      Robert.Blum  Added new copyEvents() method.
 * Jun 06, 2016 19432      Chris.Golden Added method to set a flag indicating whether newly-created
 *                                      (by the user) hazard events should be added to the selected
 *                                      set or not.
 * Jul 25, 2016 19537      Chris.Golden Changed collections of events that were returned into lists,
 *                                      since the unordered nature of the collections was not
 *                                      appropriate. Added originator parameters for methods for
 *                                      setting high- and low-res geometries for hazard events.
 *                                      Removed obsolete set-geometry method.
 * Aug 15, 2016 18376      Chris.Golden Added temporary method isShutDown() to allow the session
 *                                      hazard event notification listener to know whether or not
 *                                      to forward on notifications.
 * Aug 18, 2016 19537      Chris.Golden Added originator to sortEvents() method.
 * Sep 12, 2016 15934      Chris.Golden Changed to work with advanced geometries now used by
 *                                      hazard events.
 * Sep 26, 2016 21758      Chris.Golden Changed removeEvent() and removeEvents() to include a
 *                                      boolean parameter indicating whether confirmation should
 *                                      be done or not.
 * Oct 04, 2016 22573      Chris.Golden Added method to clear CWA geometry.
 * Oct 19, 2016 21873      Chris.Golden Added time resolution tracking for individual events.
 * Dec 12, 2016 21504      Robert.Blum  Changed method name to updateHazardEventToLastSaved().
 * Feb 01, 2017 15556      Chris.Golden Added methods to get the history count and visible history
 *                                      count for a hazard event. Also moved selection methods to
 *                                      new selection manager, and added method for reverting an
 *                                      event to the most recent saved version.
 * Feb 17, 2017 21676      Chris.Golden Changed to place old SessionEventUtilities merge method
 *                                      into this interface.
 * Feb 17, 2017 29138      Chris.Golden Removed notion of visible history list (since all events
 *                                      in history list are now visible). Also added support for
 *                                      saving to history list versus new "latest version" set
 *                                      in database.
 * Mar 16, 2017 15528      Chris.Golden Added methods to get and set checked state of a hazard
 *                                      event.
 * Mar 30, 2017 15528      Chris.Golden Changed to reset modified flag when asked to do so
 *                                      during the persistence of a hazard event.
 * Sep 27, 2017 38072      Chris.Golden Removed definitions of constants that did not belong here.
 * Oct 23, 2017 21730      Chris.Golden Added method to set a hazard event to the default hazard
 *                                      type as configured, if any.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable session events.
 *                                      Also changed addEvent() to no longer be capable of merging
 *                                      hazard events, as that is the job of mergeHazardEvents().
 * Feb 06, 2018 46258      Chris.Golden Fixed null pointer exception bug when checking for hazard
 *                                      conflicts.
 * Feb 13, 2018 44514      Chris.Golden Removed event-modifying script code, as such scripts are
 *                                      not to be used.
 * Feb 21, 2018 46736      Chris.Golden Simplified the mergeHazardEvents() method.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionEventManager {

    // Public Enumerated Types

    /**
     * Result of an event property change attempt.
     */
    public enum EventPropertyChangeResult {
        SUCCESS, FAILURE_DUE_TO_EVENT_NOT_FOUND, FAILURE_DUE_TO_LOCK_STATUS, FAILURE_DUE_TO_BAD_VALUE
    }

    // Public Static Classes

    /**
     * Hazard event property change specifier. The generic parameter
     * <code>T</code> provides the type of the value that is passed when
     * attempting a particular property change.
     * <p>
     * <strong>Note</strong>: This class is not intended to be subclassed
     * outside of this interface definition, and thus has no way of being
     * constructed.
     * </p>
     */
    public static class EventPropertyChange<T> {

        // Private Constructors

        /**
         * Construct a standard instance. This is private so as to disallow the
         * instantiation of subclasses of this inner class that are created
         * outside of {@link ISessionEventManager}.
         */
        private EventPropertyChange() {
        }
    }

    /**
     * Event type value.
     */
    public static final class EventType {

        // Private Variables

        /**
         * Phenomenon; may be <code>null</code>.
         */
        private final String phenomenon;

        /**
         * Significance; may be <code>null</code>. If {@link #phenomenon} is
         * <code>null</code>, this is ignored.
         */
        private final String significance;

        /**
         * Sub-type; may be <code>null</code>. If {@link #significance} is
         * <code>null</code>, this is ignored.
         */
        private final String subType;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param phenomenon
         *            Phenomenon; may be <code>null</code>.
         * @param significance
         *            Significance; may be <code>null</code>. If
         *            {@link #phenomenon} is <code>null</code>, this is ignored.
         * @param subType
         *            Sub-type; may be <code>null</code>. If
         *            {@link #significance} is <code>null</code>, this is
         *            ignored.
         */
        public EventType(String phenomenon, String significance,
                String subType) {
            this.phenomenon = phenomenon;
            this.significance = significance;
            this.subType = subType;
        }

        // Public Methods

        /**
         * Get the phenomenon.
         * 
         * @return Phenomenon; may be <code>null</code>.
         */
        public final String getPhenomenon() {
            return phenomenon;
        }

        /**
         * Get the significance.
         * 
         * @return Significance; may be <code>null</code>. If
         *         {@link #getPhenomenon()} is <code>null</code>, this is
         *         ignored.
         */
        public final String getSignificance() {
            return significance;
        }

        /**
         * Get the sub-type.
         * 
         * @return Sub-type; may be <code>null</code>. If
         *         {@link #getSignificance()} is <code>null</code>, this should
         *         be ignored.
         */
        public final String getSubType() {
            return subType;
        }
    }

    // Public Static Constants

    /**
     * Set event category property specifier.
     */
    public static final EventPropertyChange<String> SET_EVENT_CATEGORY = new EventPropertyChange<>();

    /**
     * Set event type property specifier.
     */
    public static final EventPropertyChange<EventType> SET_EVENT_TYPE = new EventPropertyChange<>();

    /**
     * Set event type property to default specifier. The associated object is
     * ignored.
     */
    public static final EventPropertyChange<Object> SET_EVENT_TYPE_TO_DEFAULT = new EventPropertyChange<>();

    /**
     * Set event creation time specifier.
     */
    public static final EventPropertyChange<Date> SET_EVENT_CREATION_TIME = new EventPropertyChange<>();

    /**
     * Set event start time specifier.
     */
    public static final EventPropertyChange<Date> SET_EVENT_START_TIME = new EventPropertyChange<>();

    /**
     * Set event end time specifier.
     */
    public static final EventPropertyChange<Date> SET_EVENT_END_TIME = new EventPropertyChange<>();

    /**
     * Set event time range specifier. The associated pair holds the new start
     * and end times, respectively.
     */
    public static final EventPropertyChange<Pair<Date, Date>> SET_EVENT_TIME_RANGE = new EventPropertyChange<>();

    /**
     * Set event geometry specifier.
     */
    public static final EventPropertyChange<IAdvancedGeometry> SET_EVENT_GEOMETRY = new EventPropertyChange<>();

    /**
     * Replace event visual feature specifier.
     */
    public static final EventPropertyChange<VisualFeature> REPLACE_EVENT_VISUAL_FEATURE = new EventPropertyChange<>();

    /**
     * Set event visual features specifier.
     */
    public static final EventPropertyChange<VisualFeaturesList> SET_EVENT_VISUAL_FEATURES = new EventPropertyChange<>();

    /**
     * Set event source specifier.
     */
    public static final EventPropertyChange<Source> SET_EVENT_SOURCE = new EventPropertyChange<>();

    /**
     * Set event workstation identifier specifier.
     */
    public static final EventPropertyChange<WsId> SET_EVENT_WORKSTATION_IDENTIFIER = new EventPropertyChange<>();

    /**
     * Set event hazard attributes specifier. The associated map holds entries
     * for all the key-value pairings that comprise the new attributes.
     */
    public static final EventPropertyChange<Map<String, Serializable>> SET_EVENT_ATTRIBUTES = new EventPropertyChange<>();

    /**
     * Add event hazard attribute specifier. The associated pair holds the key
     * and value of the new attribute.
     */
    public static final EventPropertyChange<Pair<String, Serializable>> ADD_EVENT_ATTRIBUTE = new EventPropertyChange<>();

    /**
     * Add event hazard attributes specifier. The associated map holds entries
     * for all the key-value pairings that should be added to the attributes.
     */
    public static final EventPropertyChange<Map<String, Serializable>> ADD_EVENT_ATTRIBUTES = new EventPropertyChange<>();

    /**
     * Remove event hazard attribute specifier. The associated string holds the
     * key of the attribute to be removed.
     */
    public static final EventPropertyChange<String> REMOVE_EVENT_ATTRIBUTE = new EventPropertyChange<>();

    // Public Methods

    /**
     * Add the specified event to the session. The event may either be
     * completely new (indicated by its event identifier being <code>null</code>
     * ), or else an event that was stored in the database.
     * <p>
     * <strong>Note</strong>: No event should be supplied that has an event
     * identifier that is identical to one already being managed within the
     * session. For merging changes from one event into another, use
     * {@link #mergeHazardEvents(IReadableHazardEvent, IHazardEventView, boolean, boolean, boolean, boolean, IOriginator)}
     * instead.
     * </p>
     * 
     * @param event
     *            Event to be added.
     * @param originator
     *            Originator of the addition.
     * @return Event that was added.
     * @throws HazardEventServiceException
     *             If a problem occurs while attempting to add the hazard event.
     */
    public IHazardEventView addEvent(IReadableHazardEvent event,
            IOriginator originator) throws HazardEventServiceException;

    /**
     * Merge the contents of the new event into the old event, using the
     * specified originator for any notifications that are sent out as a result.
     * 
     * @param newEvent
     *            Event to be merged into the old event.
     * @param oldEvent
     *            Event into which to merge the new event.
     * @param forceMerge
     *            If <code>true</code>, the event manager will not be used to
     *            set time range and hazard type, meaning the values from the
     *            new event will not be checked for correctness before being
     *            merged into the old event. If <code>false</code>, such checks
     *            will occur.
     * @param persistOnStatusChange
     *            Flag indicating whether or not the event should be saved to
     *            the database (persisted) if its status is being changed as a
     *            result of this merge.
     * @param useModifiedValue
     *            Flag indicating whether or not the new event's modified flag
     *            value should be used for the event it is being merged into.
     * @param originator
     *            Originator of this action.
     * @return Result of the attempt.
     */
    public EventPropertyChangeResult mergeHazardEvents(
            IReadableHazardEvent newEvent, IHazardEventView oldEvent,
            boolean forceMerge, boolean persistOnStatusChange,
            boolean useModifiedValue, IOriginator originator);

    /**
     * Remove an event from the session.
     * 
     * @param event
     *            Event to be removed.
     * @param confirm
     *            Flag indicating whether or not confirmation should be received
     *            from the user as necessary.
     * @param originator
     *            Originator of the change.
     */
    public void removeEvent(IHazardEventView event, boolean confirm,
            IOriginator originator);

    /**
     * Remove events from the session.
     * 
     * @param events
     *            Events to be removed.
     * @param confirm
     *            Flag indicating whether or not confirmation should be received
     *            from the user as necessary.
     * @param originator
     *            Originator of the change.
     */
    public void removeEvents(Collection<? extends IHazardEventView> events,
            boolean confirm, IOriginator originator);

    /**
     * Reset events, removing all of them from the database as well.
     * <p>
     * <strong>Caution</strong>: This is a heavy-handed action, intended for
     * practice mode only.
     * </p>
     *
     * @param originator
     *            Originator of the change.
     */
    public void resetEvents(IOriginator originator);

    /**
     * Sort the events using the specified comparator.
     * 
     * @param comparator
     *            Comparator with which to sort.
     * @param originator
     *            Originator of the change.
     */
    public void sortEvents(Comparator<IReadableHazardEvent> comparator,
            IOriginator originator);

    /**
     * Make the specified hazard events pending if they are potential.
     * 
     * @param events
     *            Events to be made pending.
     * @param originator
     *            Originator of the change.
     */
    public void setPotentialEventsToPending(
            Collection<? extends IHazardEventView> events);

    /**
     * Set the status of the specified event to PROPOSED, persists it to the
     * database and notifies all listeners of this. If the user directly
     * requested the proposal, and the proposal attempt fails due to the event
     * being locked, the user will be notified.
     * 
     * @param event
     *            Event to be proposed.
     * @param originator
     *            Originator of the change.
     * @return Result of the attempt.
     */
    public EventPropertyChangeResult proposeEvent(IHazardEventView event,
            IOriginator originator);

    /**
     * Set the statuses of the specified events to PROPOSED, persists them to
     * the database and notifies all listeners of this. If user directly
     * requested the proposals, and the any proposal attempts fail due to events
     * being locked, the user will be notified.
     * 
     * @param events
     *            Events to be proposed.
     * @param originator
     *            Originator of the change.
     * @return Map pairing the event identifiers of the specified events with
     *         the result of the proposal attempts for those events.
     */
    public Map<String, EventPropertyChangeResult> proposeEvents(
            Collection<? extends IHazardEventView> events,
            IOriginator originator);

    /**
     * Sets the state of the event to ISSUED, persists it to the database and
     * notifies all listeners of this.
     * 
     * @param event
     *            Event to be issued.
     * @param originator
     *            Originator of the change.
     * @return Result of the attempt.
     */
    public void issueEvent(IHazardEventView event, IOriginator originator);

    /**
     * Initiate the ending process for the specified hazard event. If the user
     * directly requested the ending, and the attempt fails due to the event
     * being locked, the user will be notified.
     * 
     * @param event
     *            Event to be set to ENDING.
     * @param originator
     *            Originator of the change.
     * @return Result of the attempt.
     */
    public EventPropertyChangeResult initiateEventEndingProcess(
            IHazardEventView event, IOriginator originator);

    /**
     * Revert the ending process for the specified hazard event. If the user
     * directly requested the reversion and the attempt fails due to the event
     * being locked, the user will be notified.
     * 
     * @param event
     *            Event to be reverted from ENDING status.
     * @param originator
     *            Originator of the change.
     */
    public EventPropertyChangeResult revertEventEndingProcess(
            IHazardEventView event, IOriginator originator);

    /**
     * Sets the state of the event to ENDED, persists it to the database and
     * notifies all listeners of this state change.
     * 
     * @param event
     *            Event to be ended.
     * @param originator
     *            Originator of the change.
     */
    public void endEvent(IHazardEventView event, IOriginator originator);

    /**
     * Save the specified events to the database. If the user directly requested
     * the saves, and any save attempts fail due to the events in question being
     * locked, the user will be notified.
     * 
     * @param events
     *            Events to be saved.
     * @param addToHistory
     *            Flag indicating whether or not the snapshots of the events
     *            created to be saved to the database should be part of their
     *            respective events' history lists.
     * @param keepLocked
     *            Flag indicating whether or not the events being saved are to
     *            be kept locked, instead of unlocked. If
     *            <code>addToHistory</code> is <code>true</code>, the provided
     *            value is ignored, and the events are all unlocked regardless.
     * @param treatAsIssuance
     *            Flag indicating whether or not the save is part of the
     *            issuance of the hazard events.
     * @param originator
     *            Originator of the change.
     * @return Map pairing the event identifiers of the specified events with
     *         the result of the save attempts for those events.
     */
    public Map<String, EventPropertyChangeResult> saveEvents(
            List<? extends IHazardEventView> events, boolean addToHistory,
            boolean keepLocked, boolean treatAsIssuance,
            IOriginator originator);

    /**
     * Copy the specified hazard events, creating a copy with pending status and
     * no type for each.
     * 
     * @param events
     *            Events to be copied.
     */
    public void copyEvents(List<? extends IHazardEventView> events);

    /**
     * Revert the event with the specified identifier to the most recently saved
     * version of that event, if any. If the user directly requested the
     * reversion, and the revert attempts fail due to the events in question not
     * being locked by this workstation, the user will be notified.
     * 
     * @param eventIdentifier
     *            Identifier of the event to be reverted.
     * @param originator
     *            Originator of the change.
     * @return Result of the attempt.
     */
    public EventPropertyChangeResult revertEventToLastSaved(String identifier,
            IOriginator originator);

    /**
     * Get the event with the given identifier.
     * 
     * @param identifier
     *            Identifier of the event to be fetched.
     * @return Event, or <code>null</code> if there is no such event in the
     *         session.
     */
    public IHazardEventView getEventById(String identifier);

    /**
     * Get the history list for the event with the given identifier.
     * 
     * @param identifier
     *            Identifier of the event for which to fetch the history list.
     * @return History list of the event, or <code>null</code> if there is no
     *         such event in the session.
     */
    public List<IHazardEventView> getEventHistoryById(String identifier);

    /**
     * Get the number of historical versions (that is, the size of the history
     * list) that exist for the specified event.
     * 
     * @param identifier
     *            Identifier of the event for which the number of historical
     *            versions is to be fetched.
     * @return Number of historical versions.
     */
    public int getHistoricalVersionCountForEvent(String identifier);

    /**
     * Get all events that are currently being managed by this session. This may
     * include events that are not currently visible due to filtering based upon
     * the current settings; see {@link #getEventsForCurrentSettings()} if only
     * the filtered list is desired.
     * 
     * @return List of all events that are currently being managed by this
     *         session.
     */
    public List<IHazardEventView> getEvents();

    /**
     * Get the events that are currently being managed by this session, filtered
     * by the current settings. For an unfiltered list, use the
     * {@link #getEvents()} method.
     * 
     * @return List of events that are currently being managed by this session,
     *         filtered by the current settings.
     */
    public List<IHazardEventView> getEventsForCurrentSettings();

    /**
     * Get the events that are currently checked.
     * 
     * @return Events that are currently checked.
     */
    public List<IHazardEventView> getCheckedEvents();

    /**
     * Get all events with the given status from the session.
     * 
     * @param status
     *            Status of the hazards to be fetched.
     * @param includeUntyped
     *            Flag indicating whether or not to include untyped hazard
     *            events (those without types).
     * @return Events with the specified status, including untyped events if
     *         appropriate.
     */
    public Collection<IHazardEventView> getEventsByStatus(HazardStatus status,
            boolean includeUntyped);

    /**
     * Get a set indicating which selected hazard event identifiers are allowed
     * to have their status changed to "proposed".
     * 
     * TODO: For now, the set is not kept current, so it is valid only at the
     * time it is retrieved via this method and should not be cached for future
     * checks. It would be far less wasteful to have it behave like the "until
     * further notice" set, and have it be kept current by the instance of this
     * class, so that it will continue to be valid as long as the session event
     * manager exists. At any given instant after it is fetched via this method,
     * it could be queried to determine whether or not a specific hazard event
     * within this session may have its status changed to "proposed".
     * <p>
     * Note that the set is unmodifiable; attempts to modify it will result in
     * an {@link UnsupportedOperationException}.
     * 
     * @return Set of hazard event identifiers indicating which events may have
     *         their status changed to "proposed".
     */
    public Set<String> getSelectedEventIdsAllowingProposal();

    /**
     * Change the specified event's specified property in the specified manner
     * if possible, marking the change as having originated from a side effect
     * of something else that changed (not directly from an action in the user
     * interface).
     * 
     * @param event
     *            Event to be modified.
     * @param propertyChange
     *            Property and manner in which the property is to be changed.
     * @param parameters
     *            Parameters needed to effect the change.
     * @return Result of the attempt.
     */
    public <T> EventPropertyChangeResult changeEventProperty(
            IHazardEventView event, EventPropertyChange<T> propertyChange,
            T parameters);

    /**
     * Change the specified event's specified property in the specified manner
     * if possible. If the change is directly the result of user input, lock the
     * event if it is not already locked by this workstation, or notify the user
     * if the lock is held by another workstation.
     * 
     * @param event
     *            Event to be modified.
     * @param propertyChange
     *            Property and manner in which the property is to be changed.
     * @param parameters
     *            Parameters needed to effect the change.
     * @param originator
     *            Originator of this change.
     * @return Result of the attempt.
     */
    public <T> EventPropertyChangeResult changeEventProperty(
            IHazardEventView event, EventPropertyChange<T> propertyChange,
            T parameters, IOriginator originator);

    /**
     * Determine whether or not the specified event may have something undone.
     * 
     * @param event
     *            Event to be checked.
     * @return <code>true</code> if undo is possible, <code>false</code>
     *         otherwise.
     */
    public boolean isUndoable(IHazardEventView event);

    /**
     * Determine whether or not the specified event may have something redone.
     * 
     * @param event
     *            Event to be checked.
     * @return <code>true</code> if redo is possible, <code>false</code>
     *         otherwise.
     */
    public boolean isRedoable(IHazardEventView event);

    /**
     * Undo the most recent undoable action for the specified event.
     * 
     * @param event
     *            Event upon which to operate.
     * @return Result of the attempt.
     */
    public EventPropertyChangeResult undo(IHazardEventView event);

    /**
     * Redo the most recent undone action for the specified event.
     * 
     * @param event
     *            Event upon which to operate.
     * @return Result of the attempt.
     */
    public EventPropertyChangeResult redo(IHazardEventView event);

    /**
     * Get the megawidget specifier manager for the specified event. Note that
     * this method must be implemented to return a cached manager if
     * appropriate, unlike the
     * {@link ISessionConfigurationManager#getMegawidgetSpecifiersForHazardEvent(IHazardEvent)}
     * method.
     * <p>
     * Invocation of this method has the side effect of potentially updating the
     * event's attributes to sync them with the values found specified in the
     * returned megawidget specifiers.
     * </p>
     * 
     * @param event
     *            Event for which to retrieve the manager.
     * @return Megawidget specifier manager, holding specifiers for the
     *         megawidgets as well as any side effects applier to be used with
     *         the megawidgets.
     */
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(
            IHazardEventView event);

    /**
     * Get the duration selector choices that are available for the specified
     * event, given the latter's status.
     * 
     * @param event
     *            Event for which to fetch the duration selector choices.
     * @return List of choices; each of these is of the form given by the
     *         description of the
     *         {@link gov.noaa.gsd.viz.megawidgets.validators.SingleTimeDeltaStringChoiceValidatorHelper}
     *         class. The list is pruned of any choices that are not currently
     *         available for the specified event if its end time cannot shrink
     *         or expand. If the specified event does not use a duration
     *         selector for its end time, an empty list is returned.
     */
    public List<String> getDurationChoices(IHazardEventView event);

    /**
     * Get the map of hazard attribute identifiers that trigger recommender
     * executions to the recommenders executed for the specified hazard event.
     * 
     * @param identifier
     *            Identifier of the hazard event for which to fetch the map.
     * @return Map, or <code>null</code> if the hazard event has no associated
     *         map.
     */
    public Map<String, String> getRecommendersForTriggerIdentifiers(
            String identifier);

    /**
     * Get a set indicating which hazard event identifiers are allowed to have
     * their end time "until further notice" mode toggled. The returned object
     * will be kept current by the instance of this class, so that it will
     * continue to be valid as long as the session event manager exists. At any
     * given instant after it is fetched via this method, it may be queried to
     * determine whether or not a specific hazard event within this session may
     * use "until further notice".
     * <p>
     * Note that the set is unmodifiable; attempts to modify it will result in
     * an {@link UnsupportedOperationException}.
     * 
     * @return Set of hazard event identifiers indicating which events may have
     *         their end time "until further notice" mode toggled.
     */
    public Set<String> getEventIdsAllowingUntilFurtherNotice();

    /**
     * Get a map of hazard event identifiers to their corresponding start time
     * editability limitations. Each hazard event being managed must have an
     * entry in this map. The returned object will be kept current by the
     * instance of this class, so that it will continue to be valid as long as
     * the session event manager exists. At any given instant after it is
     * fetched via this method, it may be queried to determine the start time
     * boundaries for a specific hazard event within this session.
     * <p>
     * Note that the map is unmodifiable; attempts to modify it will result in
     * an {@link UnsupportedOperationException}.
     * 
     * @return Map of hazard event identifiers to their corresponding start time
     *         editability limitations.
     */
    public Map<String, Range<Long>> getStartTimeBoundariesForEventIds();

    /**
     * Get a map of hazard event identifiers to their corresponding end time
     * editability limitations. Each hazard event being managed must have an
     * entry in this map. The returned object will be kept current by the
     * instance of this class, so that it will continue to be valid as long as
     * the session event manager exists. At any given instant after it is
     * fetched via this method, it may be queried to determine the end time
     * boundaries for a specific hazard event within this session.
     * <p>
     * Note that the map is unmodifiable; attempts to modify it will result in
     * an {@link UnsupportedOperationException}.
     * 
     * @return Map of hazard event identifiers to their corresponding end time
     *         editability limitations.
     */
    public Map<String, Range<Long>> getEndTimeBoundariesForEventIds();

    /**
     * Get a map of hazard event identifiers to their corresponding time
     * resolutions. Each hazard event being managed must have an entry in this
     * map. The returned object will be kept current by the instance of this
     * class, so that it will continue to be valid as long as the session event
     * manager exists. At any given instant after it is fetched via this method,
     * it may be queried to determine the time resolution boundaries for a
     * specific hazard event within this session.
     * <p>
     * Note that the map is unmodifiable; attempts to modify it will result in
     * an {@link UnsupportedOperationException}.
     * 
     * @return Map of hazard event identifiers to their corresponding time
     *         resolutions.
     */
    public Map<String, TimeResolution> getTimeResolutionsForEventIds();

    /**
     * Receive notification that a command was invoked within the user interface
     * that may require a metadata refresh or other reaction.
     * 
     * TODO: Remove the <code>mutableProperties</code> parameter once event
     * modifying scripts are removed.
     * 
     * @param event
     *            Event for which the command was invoked.
     * @param identifier
     *            Identifier of the command that was invoked.
     */
    public void eventCommandInvoked(IHazardEventView event, String identifier);

    /**
     * Checks all events for conflicts.
     * 
     * @return Map pairing events with maps. The latter in turn pair events that
     *         conflict with the enclosing map's event with the list of area
     *         names where the conflict is occurring.
     * @throws HazardEventServiceException
     *             If a problem occurs while attempting to get the conflicting
     *             hazard events.
     */
    public Map<IReadableHazardEvent, Map<IReadableHazardEvent, Collection<String>>> getAllConflictingEvents()
            throws HazardEventServiceException;

    /**
     * Determine which events and geometries, if any, if a specific event
     * conflicts spatially with an existing event or event(s).
     * 
     * @param event
     *            Event to test for conflicts.
     * @param startTime
     *            Start time of hazard event
     * @param endTime
     *            End time of hazard event
     * @param geometry
     *            Geometry of hazard event.
     * @param phenSigSubtype
     *            Type of the event, consisting of the phenomenon, optional
     *            significance, and optional sub-type.
     * @return Map pairing events which conflict spatially with the specified
     *         event with the list of area names where the conflict is
     *         occurring. This map will be empty if there are no conflicting
     *         hazards.
     * @throws HazardEventServiceException
     *             If a problem occurs while attempting to get the conflicting
     *             hazard events.
     */
    public Map<IReadableHazardEvent, Collection<String>> getConflictingEvents(
            IReadableHazardEvent event, Date startTime, Date endTime,
            Geometry geometry, String phenSigSubtype)
                    throws HazardEventServiceException;

    /**
     * Get a map of selected event identifiers to any events with which they
     * conflict. The returned object will be kept current by the instance of
     * this class, so that it will continue to be valid as long as the session
     * event manager exists. At any given instant after it is fetched via this
     * method, it may be queried to determine whether or not a specific selected
     * hazard event conflicts with others.
     * <p>
     * Note that the map is unmodifiable; attempts to modify it will result in
     * an {@link UnsupportedOperationException}.
     * 
     * @return Map of selected event identifiers to any events with which they
     *         conflict. The latter is an empty collection if there are no
     *         conflicting hazards.
     */
    public Map<String, Collection<IReadableHazardEvent>> getConflictingEventsForSelectedEvents();

    /**
     * Determine whether or not it is valid to change the specified event's
     * hazard type (includes phen, sig, and subtype).
     * 
     * @param event
     *            Event to be examined.
     * @return <code>true</code> if the event can have its type changed,
     *         <code>false</code> otherwise.
     */
    public boolean canEventTypeBeChanged(IReadableHazardEvent event);

    /**
     * Determine whether or not the specified event's area can be changed.
     * 
     * @param event
     *            View of the event to be examined.
     * @return <code>true</code> the event's area can be changed,
     *         <code>false</code> the event's area cannot be changed.
     */
    public boolean canEventAreaBeChanged(IReadableHazardEvent event);

    /**
     * Determine whether or not the event may be set to proposed status.
     * 
     * @param event
     *            Event to be checked.
     * @return <code>true</code> if the event may be proposed,
     *         <code>false</code> otherwise.
     */
    public boolean isProposedStateAllowed(IHazardEventView event);

    /**
     * Determine whether or not the specified event is in the database.
     * 
     * @param event
     *            Event to be checked.
     * @return <code>true</code> if the event has ever been saved to the
     *         database, <code>false</code> otherwise.
     */
    public boolean isEventInDatabase(IReadableHazardEvent event);

    /**
     * Makes visible the hazard (high resolution) representation of the selected
     * hazard geometries. If the user directly attempted this, and the lock on
     * the one or more of the hazard events cannot be acquired, notify the user.
     * 
     * @param originator
     */
    public void setHighResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator);

    /**
     * Builds, stores and makes visible the product (low resolution)
     * representation of the selected hazard geometries. This includes clipping
     * to the CWA, modifying the polygons to conform to the hazard areas and
     * simplifying the polygons to conform to the max 20 point rule. If the user
     * directly attempted this, and the lock on one or more of the hazard events
     * cannot be acquired, notify the user.
     * 
     * @param originator
     * @return true - this function successfully clipped the hazard geometries
     *         false - this function failed, probably because a geometry was
     *         outside of the forecast area (cwa or hsa), or one or more events'
     *         locks could not be acquired.
     */
    public boolean setLowResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator);

    /**
     * Makes visible the hazard (high resolution) representation of the selected
     * hazard geometries. If the user directly attempted this, and the lock on
     * the hazard event cannot be acquired, notify the user.
     * 
     * @param originator
     */
    public void setHighResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator);

    /**
     * Builds, stores and makes visible the product (low resolution)
     * representation of the current hazard geometry. If the user directly
     * attempted this, and the lock on the hazard event cannot be acquired,
     * notify the user.
     * 
     * @param originator
     * @return true - this function successfully clipped the hazard geometru
     *         false - this function failed, probably because the geometry was
     *         outside of the forecast area, or the event's lock could not be
     *         acquired.
     */
    public boolean setLowResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator);

    /**
     * Break the lock on the specified event, if any.
     * 
     * @param event
     *            Event that is to have its lock broken.
     */
    public void breakEventLock(IHazardEventView event);

    /**
     * Update the UGC information associated with the selected hazard events. It
     * is assumed that no locking is needed.
     */
    public void updateSelectedHazardUgcs();

    /**
     * Find the UGC enclosing the given location. If that UGC is included in the
     * currently selected event then remove it; if it is not included, add it.
     * If more or less than one event is selected, then do not make any change.
     * If the currently selected event is locked by another workstation, and the
     * user attempted this action, notify the user.
     * 
     * @param location
     *            Coordinate enclosed by a UGC
     * @param originator
     *            Originator of the change.
     */
    public void addOrRemoveEnclosingUgcs(Coordinate location,
            IOriginator originator);

    /**
     * @param event
     *            Event for which to build initial areas.
     * @return Initial hazard areas for the given hazardEvent
     */
    public Map<String, String> buildInitialHazardAreas(
            IReadableHazardEvent event);

    /**
     * Returns the geometry representing the current CWA.
     * 
     * @return County Warning Area geometry.
     */
    public Geometry getCwaGeometry();

    /**
     * Clears the geometry representing the current CWA.
     */
    public void clearCwaGeometry();

    /**
     * @param eventId
     *            of the event the user is currently pointing to.
     */
    public void setCurrentEvent(String eventId);

    /**
     * @param event
     *            Event the user is currently pointing to.
     */
    public void setCurrentEvent(IHazardEventView event);

    /**
     * 
     * @return Event the user is currently pointing to
     */
    public IHazardEventView getCurrentEvent();

    /**
     * @return true if the user is currently pointing to an event
     */
    public boolean isCurrentEvent();

    /**
     * Determine whether or not the specified event is historical or the current
     * version.
     * 
     * @param event
     *            Event for which to determine its historical status.
     * @return <code>true</code> if the event is historical, <code>false</code>
     *         otherwise.
     */
    public boolean isEventHistorical(IHazardEventView event);

    /**
     * Determine whether or not the specified event is currently checked.
     * 
     * @param event
     *            Event for which to determine its checked status.
     * @return <code>true</code> if the event is currently checked,
     *         <code>false</code> otherwise.
     */
    public boolean isEventChecked(IHazardEventView event);

    /**
     * Determine whether or not the specified event is currently modified.
     * 
     * @param event
     *            Event for which to determine its modified status.
     * @return <code>true</code> if the event is currently modified,
     *         <code>false</code> otherwise.
     */
    public boolean isEventModified(IHazardEventView event);

    /**
     * Set the checked status of the specified event.
     * 
     * @param event
     *            Event to have its checked status set.
     * @param checked
     *            Flag indicating whether or not the event is to be checked.
     */
    public void setEventChecked(IHazardEventView event, boolean checked,
            IOriginator originator);

    /**
     * Determine whether the specified hazard event may accept the specified
     * geometry as its new geometry. It is assumed that the geometry is valid,
     * i.e. {@link IAdvancedGeometry#isValid()} returns <code>true</code>, if
     * this method is told not to check geometry validity.
     * 
     * @param geometry
     *            Geometry to be used.
     * @param event
     *            Event to have its geometry changed.
     * @param checkGeometryValidity
     *            Flag indicating whether or not to check the geometry's
     *            validity itself.
     * @return True if the geometry of the given hazard event can be modified to
     *         the given geometry, false otherwise,.
     */
    public boolean isValidGeometryChange(IAdvancedGeometry geometry,
            IReadableHazardEvent event, boolean checkGeometryValidity);

    /**
     * Set the flag indicating whether or not newly user-created events should
     * be added to the current selection set.
     * 
     * @param addCreatedEventsToSelected
     *            New value.
     */
    public void setAddCreatedEventsToSelected(
            boolean addCreatedEventsToSelected);

    /**
     * Determine whether or not this manager is shut down.
     * 
     * @return <code>true</code> if the manager is shut down, <code>false</code>
     *         otherwise.
     * 
     * @deprecated Remove this method once garbage collection is sorted out, as
     *             it will no longer be needed at that point; see Redmine issue
     *             #21271.
     */
    @Deprecated
    public boolean isShutDown();

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();
}
