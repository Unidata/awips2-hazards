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

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Manages all events in a session.
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
 *  
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
     * @param originator
     * @return
     */
    public E addEvent(IHazardEvent event, IOriginator originator);

    /**
     * Get the event with the given ID or null if there is no such event in the
     * session.
     * 
     * @param eventId
     * @return
     */
    public E getEventById(String eventId);

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
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(E event);

    /**
     * Receive notification that a command was invoked within the user interface
     * that requires a script to be run in response.
     * 
     * @param event
     *            Hazard event for which to run the script.
     * @param identifier
     *            Identifier of the command that was invoked.
     */
    public void scriptCommandInvoked(E event, String identifier);

    /**
     * Get all events with the given state from the session. This will never
     * return null, if no states exist an empty collection is returned.
     * 
     * @param state
     * @return
     */
    public Collection<E> getEventsByStatus(HazardStatus state);

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
     * Get all events that are currently part of this session.
     * 
     * @return
     */
    public Collection<E> getEvents();

    /**
     * 
     * @return the selected events
     */
    public List<E> getSelectedEvents();

    /**
     * Return the selected event that was most recently modified.
     * 
     * @return
     */
    public E getLastModifiedSelectedEvent();

    /**
     * Set the selected event that was most recently modified.
     * 
     * @param event
     *            New last-modified event.
     * @param originator
     *            Originator of the change.
     */
    public void setLastModifiedSelectedEvent(E event, IOriginator originator);

    /**
     * Set the selected events. Any currently selected events that are no in
     * selectedEvents will be deslected, all events in the selectedEvents set
     * will get ATTR_SELECTED set to True.
     * 
     * @param selectedEvents
     * @param originator
     */
    public void setSelectedEvents(Collection<E> selectedEvents,
            IOriginator originator);

    /**
     * 
     * @return the checked events
     */
    public Collection<E> getCheckedEvents();

    public Collection<E> getEventsForCurrentSettings();

    /**
     * Tests whether it is valid to change a hazards geometry.
     * 
     * @param event
     * @return
     */
    public boolean canChangeGeometry(E event);

    /**
     * Tests whether it is valid to change a hazards start or end time
     * 
     * @param event
     * @return
     */
    public boolean canChangeTimeRange(E event);

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
     */
    public void sortEvents(Comparator<E> comparator);

    /**
     * Checks all events for conflicts.
     * 
     * @param
     * @return A map of events with a map of any conflicting events and lists of
     *         corresponding conflicting geometries.
     */
    public Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> getAllConflictingEvents();

    /**
     * Tests if a specific event conflicts spatially with an existing event or
     * event(s).
     * 
     * @param event
     *            Event to test for conflicts
     * @param startTime
     *            - modified start time of hazard event
     * @param endTime
     *            - modified end time of hazard event
     * @param geometry
     *            - modified geometry of hazard event.
     * @param phenSigSubtype
     *            Contains phenomena, significance and an optional sub-type.
     * 
     * @return A map of events which conflict spatially with an existing event
     *         or events. Each event in the map will have a list of area names
     *         where the conflict is occurring. This map will be empty if there
     *         are no conflicting hazards.
     */
    public Map<IHazardEvent, Collection<String>> getConflictingEvents(
            IHazardEvent event, Date startTime, Date endTime,
            Geometry geometry, String phenSigSubtype);

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
     * Clips the selected hazard geometries to the cwa or hsa boundaries as
     * specified in the hazard type definition in HazardTypes.py.
     * 
     * @param
     * @return true - this function successfully clipped the hazard geometries
     *         false - this function failed, probably because a geometry was
     *         outside of the forecast area (cwa or hsa).
     */
    boolean clipSelectedHazardGeometries();

    /**
     * If a point limit is specified in hazard types, then the number of points
     * in the geometry are reduced to match that limit.
     * 
     * @param
     * @return
     */
    void reduceSelectedHazardGeometries();

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
     * Indicates the user it not currently point to any event
     */
    public void noCurrentEvent();

    /**
     * @return true if the user is currently pointing to an event
     */
    public boolean isCurrentEvent();

    /**
     * @return true if the event is selected
     */
    public boolean isSelected(E event);

}
