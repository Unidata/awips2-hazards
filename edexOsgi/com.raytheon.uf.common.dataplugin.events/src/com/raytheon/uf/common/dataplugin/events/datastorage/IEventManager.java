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
 * create an event
 * 
 * Since IEvent has a start and end time, as well as a geometry, getters by
 * those fields need to be implemented as well
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 5, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public interface IEventManager<T extends IEvent, HL extends List<T>> {

    /**
     * Create an event and return it to the user
     * 
     * @return
     */
    public T createEvent();

    /**
     * Copies the past in event information and returns a new event out
     * 
     * @param event
     * @return
     */
    public T createEvent(T event);

    /**
     * Retrieve events based on a set of filters.
     * 
     * @param filter
     * @return a list with the eventid being the key
     */
    public Map<String, HL> getEventsByFilter(Map<String, Object> filters);

    /**
     * Store a single event
     * 
     * @param event
     * @return
     */
    public boolean storeEvent(T... event);

    /**
     * Store multiple events
     * 
     * @param events
     * @return
     */
    public boolean storeEvents(List<T> events);

    /**
     * Update a single event
     * 
     * @param event
     * @return
     */
    public boolean updateEvent(T... event);

    /**
     * Update multiple events
     * 
     * @param events
     * @return
     */
    public boolean updateEvents(List<T> events);

    /**
     * Remove a single event
     * 
     * @param event
     * @return
     */
    public boolean removeEvent(T... event);

    /**
     * Remove multiple events
     * 
     * @param events
     * @return
     */
    public boolean removeEvents(List<T> events);

    /**
     * Retrieve an event based on the location, to be more precise, return ANY
     * hazard that intersects the given geometry
     * 
     * @return
     */
    public Map<String, HL> getByGeometry(Geometry geometry);

    /**
     * Retrieve any event that is between the start and end date
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public Map<String, HL> getByTime(Date startTime, Date endTime);

    /**
     * Takes a {@link TimeRange} instead of two dates and retrieves all hazards
     * within that time. Just a convenience method.
     * 
     * @param range
     * @return
     */
    public Map<String, HL> getByTimeRange(TimeRange range);

    /**
     * Retrieves all the events. This should be used under caution as there may
     * be a high number of events.
     * 
     * @return
     */
    public Map<String, HL> getAll();

}
