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
package com.raytheon.uf.common.dataplugin.events.hazards.datastorage;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.query.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.TimeRange;
import com.vividsolutions.jts.geom.Geometry;

/**
 * All access to the registry/database for hazards will happen through here.
 * Contains methods to get, store, update, delete, and create new hazards. This
 * class should be the only class used to access the database for hazards.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 5, 2012            mnash         Initial creation
 * Nov 04, 2013   2182    Dan Schaffer  Started refactoring
 * May 29, 2015   6895    Ben.Phillippe Refactored Hazard Service data access
 * Sep 14, 2016  15934    Chris.Golden  Changed to handle advanced geometries
 *                                      now used by hazard events in place of
 *                                      JTS geometries.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public class HazardEventManager implements IHazardEventManager {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventManager.class);

    /** Data Access Services */
    private final IHazardEventServices hazardDataAccess;

    /** Enum denoting what mode CAVE is in */
    public static enum Mode {
        OPERATIONAL, PRACTICE
    }

    /**
     * Creates a new HazardEventManager for the given mode
     * 
     * @param mode
     *            The mode
     */
    public HazardEventManager(Mode mode) {
        this.hazardDataAccess = HazardServicesClient
                .getHazardEventServices(mode);
    }

    /**
     * Creates an event based on the mode.
     */
    @Override
    public IHazardEvent createEvent() {
        return new HazardEvent();
    }

    @Override
    public IHazardEvent createEvent(IHazardEvent event) {
        return new HazardEvent(event);
    }

    @Override
    public Map<String, HazardHistoryList> query(HazardEventQueryRequest request) {
        Map<String, HazardHistoryList> events = new HashMap<String, HazardHistoryList>();
        try {
            HazardEventResponse response = hazardDataAccess.retrieve(request);
            if (response.success()) {
                events = response.getHistoryMap();
            } else {
                checkResponse(response);
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return events;
    }

    @Override
    public Map<String, HazardHistoryList> getEventsByFilter(
            Map<String, List<Object>> filters) {
        HazardEventQueryRequest req = new HazardEventQueryRequest();
        for (Entry<String, List<Object>> entry : filters.entrySet()) {
            req.and(entry.getKey(), entry.getValue());
        }
        return query(req);
    }

    @Override
    public boolean storeEvent(IHazardEvent... event) {
        return storeEvents(Arrays.asList(event));
    }

    @Override
    public boolean storeEvents(List<IHazardEvent> events) {
        try {
            return checkResponse(hazardDataAccess
                    .storeEventList(makeHazardEventList(events)));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateEvent(IHazardEvent... event) {
        return updateEvents(Arrays.asList(event));
    }

    @Override
    public boolean updateEvents(List<IHazardEvent> events) {
        try {
            return checkResponse(hazardDataAccess
                    .updateEventList(makeHazardEventList(events)));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean removeEvent(IHazardEvent... event) {
        return removeEvents(Arrays.asList(event));
    }

    @Override
    public boolean removeEvents(List<IHazardEvent> events) {
        try {
            return checkResponse(hazardDataAccess
                    .deleteEventList(makeHazardEventList(events)));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public Map<String, HazardHistoryList> getBySiteID(String site) {
        return this.query(new HazardEventQueryRequest(HazardConstants.SITE_ID,
                site));
    }

    @Override
    public Map<String, HazardHistoryList> getByPhenomenon(String phenomenon) {
        return this.query(new HazardEventQueryRequest(
                HazardConstants.PHENOMENON, phenomenon));
    }

    @Override
    public Map<String, HazardHistoryList> getBySignificance(String significance) {
        return this.query(new HazardEventQueryRequest(
                HazardConstants.SIGNIFICANCE, significance));
    }

    @Override
    public Map<String, HazardHistoryList> getByPhensig(String phen, String sig) {
        return this.query(new HazardEventQueryRequest(
                HazardConstants.PHENOMENON, phen).and(
                HazardConstants.SIGNIFICANCE, sig));
    }

    @Override
    public HazardHistoryList getByEventID(String eventId) {
        return this.query(
                new HazardEventQueryRequest().and(
                        HazardConstants.HAZARD_EVENT_IDENTIFIER, eventId)).get(
                eventId);

    }

    @Override
    public Map<String, HazardHistoryList> getByGeometry(Geometry geometry) {
        return this.query(new HazardEventQueryRequest().and(
                HazardConstants.GEOMETRY,
                AdvancedGeometryUtilities.createGeometryWrapper(geometry, 0)));
    }

    @Override
    public Map<String, HazardHistoryList> getByTime(Date startTime, Date endTime) {
        return this.query(new HazardEventQueryRequest().and(
                HazardConstants.HAZARD_EVENT_START_TIME, ">", startTime).and(
                HazardConstants.HAZARD_EVENT_END_TIME, "<", endTime));
    }

    @Override
    public Map<String, HazardHistoryList> getByTimeRange(TimeRange range) {
        return getByTime(range.getStart(), range.getEnd());
    };

    @Override
    public void storeEventSet(EventSet<IHazardEvent> set) {
        Iterator<IHazardEvent> eventIter = set.iterator();
        while (eventIter.hasNext()) {
            IHazardEvent event = eventIter.next();
            storeEvent(event);
        }
    }

    @Override
    public Map<String, HazardHistoryList> getAll() {
        return query(new HazardEventQueryRequest());
    }

    @Override
    public boolean removeAllEvents() {
        try {
            return checkResponse(hazardDataAccess.deleteAll());
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    /**
     * Converts IHazardEvent objects to HazardEvent objects
     * 
     * @param events
     *            The list of IHazardEvent objects
     * @return The List of HazardEvent objects
     */
    @SuppressWarnings("unchecked")
    private List<HazardEvent> makeHazardEventList(List<IHazardEvent> events) {
        return (List<HazardEvent>) (List<?>) events;
    }

    /**
     * Checks the response from the web server and logs any errors
     * 
     * @param response
     *            The response to check
     * @return True if the response indicates success, else false
     */
    private boolean checkResponse(HazardEventResponse response) {
        if (!response.success()) {
            for (Throwable t : response.getExceptions()) {
                statusHandler.error(
                        "Registry web service call encountered an error", t);
            }
        }
        return response.success();
    }
}
