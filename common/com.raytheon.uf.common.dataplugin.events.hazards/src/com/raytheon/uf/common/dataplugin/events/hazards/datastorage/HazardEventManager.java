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
import java.util.Collections;
import java.util.Date;
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
 * Feb 16, 2017  29138    Chris.Golden  Revamped to allow for the querying of
 *                                      historical versions of events, or
 *                                      latest (non-historical) versions, or
 *                                      both. Also added a method to allow for
 *                                      querying the size of a history list,
 *                                      so that the whole history list does not
 *                                      have to be shipped back to the client.
 * Feb 27, 2017  29138    Chris.Golden  Added method to get latest hazard
 *                                      events by site ID.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public class HazardEventManager implements IHazardEventManager {

    // Public Enumerated Types

    /**
     * Possible modes that CAVE may be in.
     */
    public enum Mode {
        OPERATIONAL, PRACTICE
    }

    /*
     * What to include in the hazard events returned from a query.
     */
    public enum Include {
        HISTORICAL_AND_LATEST_EVENTS, HISTORICAL_EVENTS, LATEST_EVENTS, LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS
    }

    // Private Static Constants

    /**
     * Logger,
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventManager.class);

    // Private Variables

    /**
     * Data access services.
     */
    private final IHazardEventServices hazardDataAccess;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param mode
     *            Mode.
     */
    public HazardEventManager(Mode mode) {
        this.hazardDataAccess = HazardServicesClient
                .getHazardEventServices(mode);
    }

    // Public Methods

    @Override
    public HazardEvent createEvent() {
        return new HazardEvent();
    }

    @Override
    public HazardEvent createEvent(IHazardEvent event) {
        return new HazardEvent(event);
    }

    @Override
    public boolean storeEvents(HazardEvent... events) {
        return storeEvents(Arrays.asList(events));
    }

    @Override
    public boolean storeEvents(List<HazardEvent> events) {
        try {
            return checkResponse(hazardDataAccess.storeEventList(events));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateEvents(HazardEvent... events) {
        return updateEvents(Arrays.asList(events));
    }

    @Override
    public boolean updateEvents(List<HazardEvent> events) {
        try {
            return checkResponse(hazardDataAccess.updateEventList(events));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean removeEvents(HazardEvent... events) {
        return removeEvents(Arrays.asList(events));
    }

    @Override
    public boolean removeEvents(List<HazardEvent> events) {
        try {
            return checkResponse(hazardDataAccess.deleteEventList(events));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public Map<String, HazardHistoryList> queryHistory(
            HazardEventQueryRequest request) {
        Map<String, HazardHistoryList> events = Collections.emptyMap();
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
    public Map<String, HazardEvent> queryLatest(HazardEventQueryRequest request) {
        Map<String, HazardEvent> events = Collections.emptyMap();
        try {
            HazardEventResponse response = hazardDataAccess.retrieve(request);
            if (response.success()) {
                events = response.getLatestMap();
            } else {
                checkResponse(response);
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return events;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByFilter(
            Map<String, List<?>> filters, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest();
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        for (Entry<String, List<?>> entry : filters.entrySet()) {
            request.and(entry.getKey(), entry.getValue());
        }
        return queryHistory(request);
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryBySiteID(String site,
            boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(
                HazardConstants.SITE_ID, site);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        return queryHistory(request);
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByPhenomenon(
            String phenomenon, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(
                HazardConstants.PHENOMENON, phenomenon);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        return queryHistory(request);
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryBySignificance(
            String significance, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(
                HazardConstants.SIGNIFICANCE, significance);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        return queryHistory(request);
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByPhenSig(
            String phenomenon, String significance, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(
                HazardConstants.PHENOMENON, phenomenon).and(
                HazardConstants.SIGNIFICANCE, significance);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        return queryHistory(request);
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByGeometry(
            Geometry geometry, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest().and(
                HazardConstants.GEOMETRY,
                AdvancedGeometryUtilities.createGeometryWrapper(geometry, 0));
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        return queryHistory(request);
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByTime(Date startTime,
            Date endTime, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest().and(
                HazardConstants.HAZARD_EVENT_START_TIME, ">", startTime).and(
                HazardConstants.HAZARD_EVENT_END_TIME, "<", endTime);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        return queryHistory(request);
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByTimeRange(
            TimeRange range, boolean includeLatestVersion) {
        return getHistoryByTime(range.getStart(), range.getEnd(),
                includeLatestVersion);
    };

    @Override
    public HazardHistoryList getHistoryByEventID(String eventIdentifier,
            boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest().and(
                HazardConstants.HAZARD_EVENT_IDENTIFIER, eventIdentifier);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        return queryHistory(request).get(eventIdentifier);
    }

    @Override
    public int getHistorySizeByEventID(String eventIdentifier,
            boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest().and(
                HazardConstants.HAZARD_EVENT_IDENTIFIER, eventIdentifier);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        request.setSizeOnlyRequired(true);
        return queryHistorySize(request).get(eventIdentifier);
    }

    @Override
    public HazardEvent getLatestByEventID(String eventIdentifier,
            boolean includeHistoricalVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest().and(
                HazardConstants.HAZARD_EVENT_IDENTIFIER, eventIdentifier);
        request.setInclude(includeHistoricalVersion ? Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS
                : Include.LATEST_EVENTS);
        return queryLatest(request).get(eventIdentifier);
    }

    @Override
    public Map<String, HazardEvent> getLatestBySiteID(String site,
            boolean includeHistoricalVersions) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(
                HazardConstants.SITE_ID, site);
        request.setInclude(includeHistoricalVersions ? Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS
                : Include.LATEST_EVENTS);
        return queryLatest(request);
    }

    @Override
    public void storeEventSet(EventSet<HazardEvent> set) {
        Iterator<HazardEvent> eventIter = set.iterator();
        while (eventIter.hasNext()) {
            HazardEvent event = eventIter.next();
            storeEvents(event);
        }
    }

    @Override
    public Map<String, HazardHistoryList> getAllHistory(
            boolean includeLatestVersion) {
        return queryHistory(new HazardEventQueryRequest(
                includeLatestVersion ? Include.HISTORICAL_AND_LATEST_EVENTS
                        : Include.HISTORICAL_EVENTS));
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

    // Private Methods

    /**
     * Submit the specified query request and get a map of event identifiers to
     * the sizes of the history lists back.
     * 
     * @param request
     *            Request to be submitted.
     * @return Map of event identifiers to the sizes of their history lists.
     */
    private Map<String, Integer> queryHistorySize(
            HazardEventQueryRequest request) {
        Map<String, Integer> historySizesForEventIdentifiers = Collections
                .emptyMap();
        try {
            HazardEventResponse response = hazardDataAccess.retrieve(request);
            if (response.success()) {
                historySizesForEventIdentifiers = response.getHistorySizeMap();
            } else {
                checkResponse(response);
            }
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return historySizesForEventIdentifiers;
    }

    /**
     * Check the specified response from the web server and log any errors.
     * 
     * @param response
     *            Response to be checked.
     * @return <code>true</code> if the response indicates success,
     *         <code>false</code> otherwise.
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
