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

import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Range;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
 * Oct 19, 2016 21873      Chris.Golden Added time resolution tracking for individual events.
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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionEventManager<E extends IHazardEvent> {

    /**
     * The issued attribute will be available as a Boolean for all hazards in
     * the session to mark whether the event has been previously issued, it will
     * not be persisted.
     */
    public static final String ATTR_ISSUED = "issued";

    /**
     * The hazard category attribute will be available as a String for any new
     * hazards without a phenSig. After a phenSig has been assigned hazard
     * category should be looked up from the configuration manager. This
     * attribute will not be persisted.
     */
    public static final String ATTR_HAZARD_CATEGORY = "hazardCategory";

    /**
     * Add a new event to the Session, for example the event might come from a
     * user geometry or from a recommender. The new event will automatically be
     * selected and checked.
     * 
     * @param event
     *            Nascent event.
     * @param originator
     * @return Event that was added. This will generally not be the same object
     *         as the passed-in <code>event</code>, since addition requires that
     *         a specific subclass of {@link IHazardEvent} is created.
     * @throws HazardEventServiceException
     *             If a problem occurs while attempting to add the hazard event.
     */
    public E addEvent(IHazardEvent event, IOriginator originator)
            throws HazardEventServiceException;

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
     * @param keepVisualFeatures
     *            If <code>true</code>, then if the new event has no visual
     *            features, the old event's visual features will be kept. If
     *            <code>false</code>, the new event's visual features list will
     *            always be used in place of the old one's.
     * @param persistOnStatusChange
     *            Flag indicating whether or not the event should be saved to
     *            the database (persisted) if its status is being changed as a
     *            result of this merge.
     * @param useModifiedValue
     *            Flag indicating whether or not the new event's modified flag
     *            value should be used for the event it is being merged into.
     * @param originator
     *            Originator of this action.
     */
    public void mergeHazardEvents(IHazardEvent newEvent, E oldEvent,
            boolean forceMerge, boolean keepVisualFeatures,
            boolean persistOnStatusChange, boolean useModifiedValue,
            IOriginator originator);

    /**
     * Get the event with the given ID or null if there is no such event in the
     * session.
     * 
     * @param eventId
     * @return
     */
    public E getEventById(String eventId);

    /**
     * Get the history list for the event with the given ID or null if there is
     * no such event in the session.
     * 
     * @param eventId
     * @return
     */
    public HazardHistoryList getEventHistoryById(String eventId);

    /**
     * Get the number of historical versions (that is, the size of the history
     * list) that exist for the specified event.
     * 
     * @param eventIdentifier
     *            Identifier of the event for which the number of historical
     *            versions is to be fetched.
     * @return Number of historical versions.
     */
    public int getHistoricalVersionCountForEvent(String eventIdentifier);

    /**
     * Set the specified event to have the specified category. As a side effect,
     * the event is changed to have no type.
     * 
     * @param event
     *            Event to be modified.
     * @param category
     *            Category for the event.
     * @param originator
     *            Originator of this change.
     */
    public void setEventCategory(E event, String category,
            IOriginator originator);

    /**
     * Set the specified event to have the specified type. If the former cannot
     * change its type, a new event will be created as a result.
     * 
     * @param event
     *            Event to be modified.
     * @param phenomenon
     *            Phenomenon, or <code>null</code> if the event is to have no
     *            type.
     * @param significance
     *            Phenomenon, or <code>null</code> if the event is to have no
     *            type.
     * @param subType
     *            Phenomenon, or <code>null</code> if the event is to have no
     *            subtype.
     * @param originator
     *            Originator of this change.
     * @return True if the event type was set, or false if the attempt resulted
     *         in the creation of a new event with the new type, and the
     *         original event has not had its type changed.
     */
    public boolean setEventType(E event, String phenomenon,
            String significance, String subType, IOriginator originator);

    /**
     * Set the specified event's time range.
     * 
     * @param event
     *            Event to be modified.
     * @param startTime
     *            New start time.
     * @param endTime
     *            New end time.
     * @param originator
     *            Originator of this change.
     * @return True if the new time range is now in use, false if it was
     *         rejected because one or both values fell outside their allowed
     *         boundaries.
     */
    public boolean setEventTimeRange(E event, Date startTime, Date endTime,
            IOriginator originator);

    /**
     * Set the specified event's geometry. It is assumed that the specified
     * geometry is valid, that is, that {@link Geometry#isValid()} would return
     * <code>true</code>.
     * 
     * @param event
     *            Event to be modified.
     * @param geometry
     *            New geometry.
     * @param originator
     *            Originator of this change.
     * @return True if the new geometry is now in use, false if it was rejected.
     */
    public boolean setEventGeometry(E event, IAdvancedGeometry geometry,
            IOriginator originator);

    /**
     * Get the megawidget specifier manager for the specified event. Note that
     * this method must be implemented to return a cached manager if
     * appropriate, unlike the
     * {@link ISessionConfigurationManager#getMegawidgetSpecifiersForHazardEvent(IHazardEvent)}
     * method.
     * 
     * @param event
     *            Hazard event for which to retrieve the manager.
     * @return Megawidget specifier manager, holding specifiers for the
     *         megawidgets as well as any side effects applier to be used with
     *         the megawidgets.
     */
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(IHazardEvent event);

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
    public List<String> getDurationChoices(IHazardEvent event);

    /**
     * Receive notification that a command was invoked within the user interface
     * that requires a metadata refresh or a script to be run in response.
     * 
     * @param event
     *            Hazard event for which to run the script or refresh the
     *            metadata.
     * @param identifier
     *            Identifier of the command that was invoked.
     * @param mutableProperties
     *            Mutable properties to be passed to the script, if one is run.
     */
    public void eventCommandInvoked(E event, String identifier,
            Map<String, Map<String, Object>> mutableProperties);

    /**
     * Get all events with the given status from the session. This will never
     * return null, if no states exist an empty collection is returned.
     * 
     * @param status
     *            Status of the hazards to be fetched.
     * @param includeUntyped
     *            Flag indicating whether or not to include untyped hazard
     *            events (those without types).
     * @return Events with the specified status, including untyped events if
     *         appropriate.
     */
    public Collection<E> getEventsByStatus(HazardStatus status,
            boolean includeUntyped);

    /**
     * Remove an event from the session.
     * 
     * @param event
     * @param originator
     */
    public void removeEvent(E event, IOriginator originator);

    /**
     * Remove events from the session.
     * 
     * @param events
     * @param originator
     */
    public void removeEvents(Collection<E> events, IOriginator originator);

    /**
     * Get all events that are currently being managed by this session. This may
     * include events that are not currently visible due to filtering based upon
     * the current settings; see {@link #getEventsForCurrentSettings()} if only
     * the filtered list is desired.
     * 
     * @return List of all events that are currently being managed by this
     *         session.
     */
    public List<E> getEvents();

    /**
     * Get the events that are currently being managed by this session, filtered
     * by the current settings. For an unfiltered list, use the
     * {@link #getEvents()} method.
     * 
     * @return List of events that are currently being managed by this session,
     *         filtered by the current settings.
     */
    public List<E> getEventsForCurrentSettings();

    /**
     * 
     * @return the checked events
     */
    public List<E> getCheckedEvents();

    /**
     * Tests whether it is valid to change a hazard type(includes phen, sig, and
     * subtype).
     * 
     * @param event
     * @return
     */
    public boolean canChangeType(E event);

    /**
     * Tests if an event's area can be changed.
     * 
     * @param event
     *            The event to test
     * @return True - the event's area can be changed. False - the event's area
     *         cannot be changed.
     */
    public boolean canEventAreaBeChanged(E event);

    /**
     * Sort the events using a comparator. This can be useful with
     * SEND_SELECTED_BACK or SEND_SELECTED_TO_FRONT
     * 
     * @param comparator
     * @param originator
     */
    public void sortEvents(Comparator<E> comparator, IOriginator originator);

    /**
     * Checks all events for conflicts.
     * 
     * @return Map pairing events with maps. The latter in turn pair identifiers
     *         (of events that conflict with the enclosing map's event) with the
     *         list of area names where the conflict is occurring.
     * @throws HazardEventServiceException
     *             If a problem occurs while attempting to get the conflicting
     *             hazard events.
     */
    public Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> getAllConflictingEvents()
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
    public Map<IHazardEvent, Collection<String>> getConflictingEvents(
            IHazardEvent event, Date startTime, Date endTime,
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
    public Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents();

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
     * Sets the state of the event to ENDED, persists it to the database and
     * notifies all listeners of this state change.
     * 
     * @param event
     * @param originator
     */
    public void endEvent(E event, IOriginator originator);

    /**
     * Sets the state of the event to ISSUED, persists it to the database and
     * notifies all listeners of this.
     * 
     * @param event
     * @param originator
     */
    public void issueEvent(E event, IOriginator originator);

    /**
     * Get a set indicating which hazard event identifiers are allowed to have
     * their status changed to "proposed".
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
    public Set<String> getEventIdsAllowingProposal();

    /**
     * Sets the state of the event to PROPOSED, persists it to the database and
     * notifies all listeners of this.
     * 
     * @param event
     * @param originator
     */
    public void proposeEvent(E event, IOriginator originator);

    /**
     * Sets the state of the events to PROPOSED, persists them to the database
     * and notifies all listeners of this.
     * 
     * @param events
     * @param originator
     */
    public void proposeEvents(Collection<E> events, IOriginator originator);

    /**
     * Makes visible the hazard (high resolution) representation of the selected
     * hazard geometries.
     * 
     * @param originator
     */
    public void setHighResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator);

    /**
     * Builds, stores and makes visible the product (low resolution)
     * representation of the selected hazard geometries. This includes clipping
     * to the CWA, modifying the polygons to conform to the hazard areas and
     * simplifying the polygons to conform to the max 20 point rule.
     * 
     * @param originator
     * @return true - this function successfully clipped the hazard geometries
     *         false - this function failed, probably because a geometry was
     *         outside of the forecast area (cwa or hsa).
     */
    public boolean setLowResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator);

    /**
     * Makes visible the hazard (high resolution) representation of the selected
     * hazard geometries.
     * 
     * @param originator
     */
    public void setHighResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator);

    /**
     * Builds, stores and makes visible the product (low resolution)
     * representation of the current hazard geometry.
     * 
     * @param originator
     * @return true - this function successfully clipped the hazard geometru
     *         false - this function failed, probably because the geometry was
     *         outside of the forecast area.
     */
    public boolean setLowResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator);

    /**
     * Updates the UGC information associated with the selected hazard events.
     * 
     * @param
     * @return
     */
    public void updateSelectedHazardUGCs();

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();

    /**
     * @param eventId
     *            of the event the user is currently pointing to.
     */
    public void setCurrentEvent(String eventId);

    /**
     * @param the
     *            event the user is currently pointing to.
     */
    public void setCurrentEvent(E event);

    /**
     * 
     * @return event the user is currently pointing to
     */
    public E getCurrentEvent();

    /**
     * @return true if the user is currently pointing to an event
     */
    public boolean isCurrentEvent();

    /**
     * Determine whether or not the specified event is currently checked.
     * 
     * @param event
     *            Event for which to determine its checked status.
     * @return <code>true</code> if the event is currently checked,
     *         <code>false</code> otherwise.
     */
    public boolean isEventChecked(IHazardEvent event);

    /**
     * Set the checked status of the specified event.
     * 
     * @param event
     *            Event to have its checked status set.
     * @param checked
     *            Flag indicating whether or not the event is to be checked.
     */
    public void setEventChecked(IHazardEvent event, boolean checked,
            IOriginator originator);

    /**
     * Determine whether the specified hazard event may accept the specified
     * geometry as its new geometry. It is assumed that the geometry is valid,
     * i.e. {@link IAdvancedGeometry#isValid()} returns <code>true</code>, if
     * this method is told not to check geometry validity.
     * 
     * @param geometry
     *            Geometry to be used.
     * @param hazardEvent
     *            Hazard event to have its geometry changed.
     * @param checkGeometryValidity
     *            Flag indicating whether or not to check the geometry's
     *            validity itself.
     * @return True if the geometry of the given hazard event can be modified to
     *         the given geometry, false otherwise,.
     */
    public boolean isValidGeometryChange(IAdvancedGeometry geometry,
            E hazardEvent, boolean checkGeometryValidity);

    /**
     * Find the UGC enclosing the given location. If that UGC is included in the
     * currently selected event then remove it; if it is not included, add it.
     * If more or less than one event is selected, then do not make any change.
     * 
     * @param location
     *            Coordinate enclosed by a UGC
     * @param originator
     *            Originator of the change.
     */
    public void addOrRemoveEnclosingUGCs(Coordinate location,
            IOriginator originator);

    /**
     * @param hazardEvent
     * @return the initial hazardAreas for the given hazardEvent
     */
    public Map<String, String> buildInitialHazardAreas(IHazardEvent hazardEvent);

    /**
     * Update the hazard areas.
     * 
     * @param hazardEvent
     */
    public void updateHazardAreas(IHazardEvent hazardEvent);

    /**
     * Save the specified events to the database.
     * 
     * @param events
     *            Events to be saved.
     * @param addToHistory
     *            Flag indicating whether or not the snapshots of the events
     *            created to be saved to the database should be part of their
     *            respective events' history lists.
     * @param treatAsIssuance
     *            Flag indicating whether or not the save is part of the
     *            issuance of the hazard events.
     */
    public void saveEvents(List<IHazardEvent> events, boolean addToHistory,
            boolean treatAsIssuance);

    /**
     * Copy the specified hazard events, creating a copy with pending status and
     * no type for each.
     * 
     * @param events
     *            Events to be copied.
     */
    public void copyEvents(List<IHazardEvent> events);

    /**
     * Revert the event with the specified identifier to the most recently saved
     * version of that event, if any.
     * 
     * @param eventIdentifier
     *            Identifier of the event to be reverted.
     */
    public void revertEventToLastSaved(String eventIdentifier);

    /**
     * Set the flag indicating whether or not newly user-created events should
     * be added to the current selection set.
     * 
     * @param addCreatedEventsToSelected
     *            New value.
     */
    public void setAddCreatedEventsToSelected(boolean addCreatedEventsToSelected);

    /**
     * Returns the geometry representing the current CWA.
     * 
     * @return County Warning Area geometry.
     */
    public Geometry getCwaGeometry();

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
}
