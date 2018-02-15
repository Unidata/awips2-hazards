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
package com.raytheon.uf.common.dataplugin.events.datastorage;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.time.TimeRange;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Methods must be implemented to get, store, remove, and update, as well as
 * create an event. The parameter <code>T</code> indicates the type of event
 * expected by the {@link #createEvent(IEvent)} method; <code>U</code> indicates
 * the type of event to be created by the <code>createEvent()</code> methods, as
 * well as the types to be passed to the <code>storeXXXX()</code>,
 * <code>updateXXXX()</code>, and <code>removeXXXX()</code> methods (where
 * <code>U</code> is always a subclass of <code>T</code>); and <code>HL</code>
 * indicates the type of list of events to be returned by the
 * <code>getXXXX()</code> methods returning lists.
 * <p>
 * Since {@link IEvent} has a start and end time, as well as a geometry, getters
 * by those fields need to be implemented as well.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 05, 2012            mnash        Initial creation
 * Feb 16, 2017 29138      Chris.Golden Revamped to allow for the querying of
 *                                      historical versions of events, or
 *                                      latest (non-historical) versions, or
 *                                      both. Also changed to be capable of
 *                                      having different types of events for
 *                                      the events being copied during creation
 *                                      versus the events being manipulated.
 * Jun 06, 2018 15561     Chris.Golden  Added practice flag for event
 *                                      construction.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IEventManager<T extends IEvent, U extends T, HL extends List<U>> {

    /**
     * Create an event.
     * 
     * @param practice
     *            Flag indicating whether or not practice mode is enabled.
     * @return Created event.
     */
    public U createEvent(boolean practice);

    /**
     * Create a new event by copying the specified event.
     * 
     * @param event
     *            Event to be copied.
     * @return Created event.
     */
    public U createEvent(T event);

    /**
     * Store the specified event(s).
     * 
     * @param events
     *            Event(s) to be stored.
     * @return <code>true</code> if the event(s) was stored successfully,
     *         <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean storeEvents(U... events);

    /**
     * Store the specified events.
     * 
     * @param events
     *            Events to be stored.
     * @return <code>true</code> if the events were stored successfully,
     *         <code>false</code> otherwise.
     */
    public boolean storeEvents(List<U> events);

    /**
     * Update the specified event(s).
     * 
     * @param events
     *            Event(s) to be updated.
     * @return <code>true</code> if the event(s) was updated successfully,
     *         <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean updateEvents(U... events);

    /**
     * Update the specified events.
     * 
     * @param event
     *            Events to be updated.
     * @return <code>true</code> if the events were updated successfully,
     *         <code>false</code> otherwise.
     */
    public boolean updateEvents(List<U> events);

    /**
     * Remove the specified event(s).
     * 
     * @param events
     *            Event(s) to be removed.
     * @return <code>true</code> if the event(s) was removed successfully,
     *         <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean removeEvents(U... events);

    /**
     * Remove the specified events.
     * 
     * @param events
     *            Events to be removed.
     * @return <code>true</code> if the events were removed successfully,
     *         <code>false</code> otherwise.
     */
    public boolean removeEvents(List<U> events);

    /**
     * Retrieve the history lists of events based on a set of filters.
     * 
     * @param filter
     *            Map pairing event attribute names to the values that those
     *            attributes may have in order to pass through the filter.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    public Map<String, HL> getHistoryByFilter(Map<String, List<?>> filters,
            boolean includeLatestVersion);

    /**
     * Retrieve the history lists of all events that intersect the given
     * geometry.
     * 
     * @param geometry
     *            Geometry that must be intersected.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    public Map<String, HL> getHistoryByGeometry(Geometry geometry,
            boolean includeLatestVersion);

    /**
     * Retrieve the history lists of all events with time ranges lying within
     * the start and end times (exclusive).
     * 
     * @param startTime
     *            Start time; the events that filter through must have start
     *            times after this.
     * @param endTime
     *            End time; the events that filter through must have end times
     *            before this.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    public Map<String, HL> getHistoryByTime(Date startTime, Date endTime,
            boolean includeLatestVersion);

    /**
     * Retrieve the history lists of all events with time ranges lying within
     * the start and end times (exclusive).
     * 
     * @param timeRange
     *            Time range within which the events' time ranges must lie, that
     *            is, their start times must be after the lower bound of the
     *            time range, and their end times must be before the upper bound
     *            of the time range.
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of event identifiers to their history lists.
     */
    public Map<String, HL> getHistoryByTimeRange(TimeRange range,
            boolean includeLatestVersion);

    /**
     * Retrieve all the events.
     * <p>
     * <strong>Note</strong>: This should be used cautiously, as there may be a
     * large number of events returned.
     * </p>
     * 
     * @param includeLatestVersion
     *            Flag indicating whether or not non-historical latest versions
     *            should be included in the history lists (in each case as the
     *            last item).
     * @return Map of all event identifiers to their history lists.
     */
    public Map<String, HL> getAllHistory(boolean includeLatestVersion);
}
