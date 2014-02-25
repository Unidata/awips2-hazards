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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionEventManager {

    /**
     * The selected attribute will be available as a Boolean for all hazards in
     * the session to mark whether the user has selected them, it will not be
     * persisted.
     */
    public static final String ATTR_SELECTED = "selected";

    /**
     * The checked attribute will be available as a Boolean for all hazards in
     * the session to mark whether the user has checked them, it will not be
     * persisted.
     */
    public static final String ATTR_CHECKED = "checked";

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
     * Comparator can be used with sortEvents to send selected events to the
     * front of the list.
     */
    public static final Comparator<IHazardEvent> SEND_SELECTED_FRONT = new Comparator<IHazardEvent>() {

        @Override
        public int compare(IHazardEvent o1, IHazardEvent o2) {
            boolean s1 = Boolean.TRUE.equals(o1
                    .getHazardAttribute(ISessionEventManager.ATTR_SELECTED));
            boolean s2 = Boolean.TRUE.equals(o2
                    .getHazardAttribute(ISessionEventManager.ATTR_SELECTED));
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
    public static final Comparator<IHazardEvent> SEND_SELECTED_BACK = Collections
            .reverseOrder(SEND_SELECTED_FRONT);

    /**
     * Add a new event to the Session, for example the event might come from a
     * user geometry or from a recommender. The new event will automatically be
     * selected and checked.
     * 
     * @param event
     * @return
     */
    public IHazardEvent addEvent(IHazardEvent event);

    /**
     * Get the event with the given ID or null if there is no such event in the
     * session.
     * 
     * @param eventId
     * @return
     */
    public IHazardEvent getEventById(String eventId);

    /**
     * Get all events with the given state from the session. This will never
     * return null, if no states exist an empty collection is returned.
     * 
     * @param state
     * @return
     */
    public Collection<IHazardEvent> getEventsByState(HazardState state);

    /**
     * Remove an event from the session.
     * 
     * @param event
     */
    public void removeEvent(IHazardEvent event);

    /**
     * Get all events that are currently part of this session.
     * 
     * @return
     */
    public Collection<IHazardEvent> getEvents();

    /**
     * Get all events where the ATTR_SELECTED is True.
     * 
     * @return
     */
    public Collection<IHazardEvent> getSelectedEvents();

    /**
     * Return the selected event that was most recently modified.
     * 
     * @return
     */
    public IHazardEvent getLastModifiedSelectedEvent();

    /**
     * Set the selected events. Any currently selected events that are no in
     * selectedEvents will be deslected, all events in the selectedEvents set
     * will get ATTR_SELECTED set to True.
     * 
     * @param selectedEvents
     */
    public void setSelectedEvents(Collection<IHazardEvent> selectedEvents);

    /**
     * Get all events where the ATTR_CHECKED is True.
     * 
     * @return
     */
    public Collection<IHazardEvent> getCheckedEvents();

    /**
     * Tests whether it is valid to change a hazards geometry.
     * 
     * @param event
     * @return
     */
    public boolean canChangeGeometry(IHazardEvent event);

    /**
     * Tests whether it is valid to change a hazards start or end time
     * 
     * @param event
     * @return
     */
    public boolean canChangeTimeRange(IHazardEvent event);

    /**
     * Tests whether it is valid to change a hazard type(includes phen, sig, and
     * subtype).
     * 
     * @param event
     * @return
     */
    public boolean canChangeType(IHazardEvent event);

    /**
     * Sort the events using a comparator. This can be useful with
     * SEND_SELECTED_BACK or SEND_SELECTED_TO_FRONT
     * 
     * @param comparator
     */
    public void sortEvents(Comparator<IHazardEvent> comparator);

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();

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
     * Tests the currently selected hazards for conflicts with other hazards.
     * 
     * @param
     * @return A map of selected event ids and corresponding collections of
     *         events which conflict spatially with them. This list will be
     *         empty if the are no conflicting hazards.
     */
    Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents();

    public Collection<IHazardEvent> getEventsForCurrentSettings();

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
     * @param The
     *            event to end.
     * @return
     */
    public void endEvent(IHazardEvent event);

    /**
     * Sets the state of the event to ISSUED, persists it to the database and
     * notifies all listeners of this.
     * 
     * @param The
     *            event to issue.
     * @return
     */
    public void issueEvent(IHazardEvent event);

    /**
     * Sets the state of the event to PROPOSED, persists it to the database and
     * notifies all listeners of this.
     * 
     * @param The
     *            event to propose
     * @return
     */
    public void proposeEvent(IHazardEvent event);

    /**
     * @return id of the most recently selected event
     */
    String getLastSelectedEventID();

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
     * Tests if an event's area can be changed.
     * 
     * @param event
     *            The event to test
     * @return True - the event's area can be changed. False - the event's area
     *         cannot be changed.
     */
    public boolean canEventAreaBeChanged(IHazardEvent event);

    /**
     * Updates the UGC information associated with the selected hazard events.
     * 
     * @param
     * @return
     */
    public void updateSelectedHazardUGCs();

}
