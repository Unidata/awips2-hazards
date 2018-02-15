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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventRequestServices;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.TimeRange;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;

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
 * Aug 04, 2015   6895    Ben.Phillippe Finished HS data access refactor
 * Aug 20, 2015   6895    Ben.Phillippe Routing registry requests through
 *                                      request server
 * Jan 26, 2016   7623    Ben.Phillippe Implemented locking of HazardEvents
 * Mar 14, 2016  12145    mduff         Cleaned up error handling.
 * May 06, 2016  18202    Robert.Blum   Changes for operational mode.
 * Sep 14, 2016  15934    Chris.Golden  Changed to handle advanced geometries
 *                                      now used by hazard events in place of
 *                                      JTS geometries.
 * Dec 12, 2016  21504    Robert.Blum   Moved locking code to SessionLockManager.
 * Feb 16, 2017  29138    Chris.Golden  Revamped to allow for the querying of
 *                                      historical versions of events, or
 *                                      latest (non-historical) versions, or
 *                                      both. Also added a method to allow for
 *                                      querying the size of a history list,
 *                                      so that the whole history list does not
 *                                      have to be shipped back to the client.
 * Feb 27, 2017  29138    Chris.Golden  Added method to get latest hazard
 *                                      events by site ID.
 * Mar 16, 2017  29138    Chris.Golden  Added workaround code to differentiate
 *                                      historical versions of events from
 *                                      latest versions; this is needed until
 *                                      the latest version saving is fixed.
 * Apr 04, 2017  32732    Chris.Golden  Fixed temporary code used to prune
 *                                      event history lists of latest-version
 *                                      snapshots so that it does not return
 *                                      any empty history lists.
 * Apr 13, 2017  33142    Chris.Golden  Added ability to delete all events
 *                                      with a particular event identifier.
 * Dec 17, 2017  20739    Chris.Golden  Refactored away access to directly
 *                                      mutable session events.
 * Feb 23, 2018  28387    Chris.Golden  Added getLatestByPhenomenonsAndSiteID()
 *                                      method.
 * Jun 06, 2018  15561    Chris.Golden  Added practice flag for hazard event
 *                                      construction.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public class HazardEventManager implements IHazardEventManager {

    // Public Static Constants

    /**
     * Attribute of a hazard event which, if not null, indicates that the event
     * is a historical version.
     * 
     * @deprecated Should not be needed once saving an event as "latest" is
     *             available again.
     */
    @Deprecated
    public static final String HISTORICAL = "historical";

    // Public Enumerated Types

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
     * Current mode.
     */
    private final boolean practice;

    /**
     * Data access services.
     */
    private final IHazardEventServices hazardDataAccess;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public HazardEventManager(boolean practice) {
        this.practice = practice;
        this.hazardDataAccess = HazardEventRequestServices
                .getServices(practice);
    }

    // Public Methods

    @Override
    public HazardEvent createEvent(boolean practice) {
        return new HazardEvent(practice);
    }

    @Override
    public HazardEvent createEvent(IReadableHazardEvent event) {
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
            HazardEventQueryRequest request)
            throws HazardEventServiceException {
        Map<String, HazardHistoryList> events = Collections.emptyMap();
        HazardEventResponse response = hazardDataAccess.retrieve(request);
        if (response.success()) {
            events = response.getHistoryMap();
        } else {
            checkResponse(response);
        }

        /*
         * TODO: Remove this code once the HISTORICAL attribute is not being
         * used.
         * 
         * Iterate through the history lists returned, pruning out all latest
         * version (ahistorical) hazard events, or if the latest one is wanted
         * as well, all but the most recent "latest version". This is needed
         * because "latest" ones are currently being saved to the history list
         * if StartUpConfig options are set to force this. Remove any history
         * lists that end up being empty as a result.
         */
        boolean needLatest = (request
                .getInclude() != Include.HISTORICAL_EVENTS);
        Set<String> toBeRemoved = new HashSet<>();
        for (Map.Entry<String, HazardHistoryList> entry : events.entrySet()) {
            Iterator<HazardEvent> iterator = entry.getValue().iterator();
            Collections.reverse(entry.getValue());
            while (iterator.hasNext()) {
                HazardEvent event = iterator.next();
                if (event.getHazardAttribute(HISTORICAL) == null) {
                    if (needLatest) {
                        needLatest = false;
                    } else {
                        iterator.remove();
                    }
                } else {
                    event.removeHazardAttribute(HISTORICAL);
                }
            }
            Collections.reverse(entry.getValue());
            if (entry.getValue().isEmpty()) {
                toBeRemoved.add(entry.getKey());
            }
        }
        for (String eventIdentifier : toBeRemoved) {
            events.remove(eventIdentifier);
        }

        return events;
    }

    @Override
    public Map<String, HazardEvent> queryLatest(HazardEventQueryRequest request)
            throws HazardEventServiceException {
        Map<String, HazardEvent> events = Collections.emptyMap();
        HazardEventResponse response = hazardDataAccess.retrieve(request);
        if (response.success()) {
            events = response.getLatestMap();
        } else {
            checkResponse(response);
        }
        return events;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByFilter(
            Map<String, List<?>> filters, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        for (Entry<String, List<?>> entry : filters.entrySet()) {
            request.and(entry.getKey(), entry.getValue());
        }

        Map<String, HazardHistoryList> result;
        try {
            result = queryHistory(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR, "Error executing filter query",
                    e);
            result = Collections.emptyMap();
        }
        return result;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryBySiteID(String site,
            boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice,
                HazardConstants.SITE_ID, site);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }

        Map<String, HazardHistoryList> result;
        try {
            result = queryHistory(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event histories by site.", e);
            result = Collections.emptyMap();
        }
        return result;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByPhenomenon(
            String phenomenon, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice,
                HazardConstants.PHENOMENON, phenomenon);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }

        Map<String, HazardHistoryList> result;
        try {
            result = queryHistory(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event histories by phenomenon.", e);
            result = Collections.emptyMap();
        }
        return result;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryBySignificance(
            String significance, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice,
                HazardConstants.SIGNIFICANCE, significance);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }

        Map<String, HazardHistoryList> result;
        try {
            result = queryHistory(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event histories by significance.", e);
            result = Collections.emptyMap();
        }
        return result;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByPhenSig(String phenomenon,
            String significance, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice,
                HazardConstants.PHENOMENON, phenomenon)
                        .and(HazardConstants.SIGNIFICANCE, significance);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }

        Map<String, HazardHistoryList> result;
        try {
            result = queryHistory(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event histories by phensig.", e);
            result = Collections.emptyMap();
        }
        return result;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByGeometry(
            Geometry geometry, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice)
                .and(HazardConstants.GEOMETRY, AdvancedGeometryUtilities
                        .createGeometryWrapper(geometry, 0));
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }

        Map<String, HazardHistoryList> result;
        try {
            result = queryHistory(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event histories by geometry.", e);
            result = Collections.emptyMap();
        }
        return result;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByTime(Date startTime,
            Date endTime, boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice)
                .and(HazardConstants.HAZARD_EVENT_START_TIME, ">", startTime)
                .and(HazardConstants.HAZARD_EVENT_END_TIME, "<", endTime);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }

        Map<String, HazardHistoryList> result;
        try {
            result = queryHistory(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event histories by time.", e);
            result = Collections.emptyMap();
        }
        return result;
    }

    @Override
    public Map<String, HazardHistoryList> getHistoryByTimeRange(TimeRange range,
            boolean includeLatestVersion) {
        return getHistoryByTime(range.getStart(), range.getEnd(),
                includeLatestVersion);
    };

    @Override
    public HazardHistoryList getHistoryByEventID(String eventIdentifier,
            boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice)
                .and(HazardConstants.HAZARD_EVENT_IDENTIFIER, eventIdentifier);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }

        HazardHistoryList result;
        try {
            result = queryHistory(request).get(eventIdentifier);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event history by event ID.", e);
            result = new HazardHistoryList();
        }
        return result;
    }

    @Override
    public int getHistorySizeByEventID(String eventIdentifier,
            boolean includeLatestVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice)
                .and(HazardConstants.HAZARD_EVENT_IDENTIFIER, eventIdentifier);
        if (includeLatestVersion == false) {
            request.setInclude(Include.HISTORICAL_EVENTS);
        }
        request.setSizeOnlyRequired(true);
        try {
            return queryHistorySize(request).get(eventIdentifier);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting event history size by event ID.", e);
            return -1;
        }
    }

    @Override
    public HazardEvent getLatestByEventID(String eventIdentifier,
            boolean includeHistoricalVersion) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice)
                .and(HazardConstants.HAZARD_EVENT_IDENTIFIER, eventIdentifier);
        request.setInclude(includeHistoricalVersion
                ? Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS
                : Include.LATEST_EVENTS);

        try {
            return queryLatest(request).get(eventIdentifier);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting latest event by event ID.", e);
            return null;
        }
    }

    @Override
    public Map<String, HazardEvent> getLatestBySiteID(String site,
            boolean includeHistoricalVersions) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice,
                HazardConstants.SITE_ID, site);
        request.setInclude(includeHistoricalVersions
                ? Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS
                : Include.LATEST_EVENTS);

        try {
            return queryLatest(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting latest event by event ID.", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, HazardEvent> getLatestByPhenomenonsAndSiteID(
            Collection<String> phenomenons, String site,
            boolean includeHistoricalVersions) {
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice,
                HazardConstants.SITE_ID, site);
        if ((phenomenons != null) && (phenomenons.isEmpty() == false)) {
            request = request.and(HazardConstants.PHENOMENON, "in",
                    phenomenons);
        }
        request.setInclude(includeHistoricalVersions
                ? Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS
                : Include.LATEST_EVENTS);

        try {
            return queryLatest(request);
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting latest event by event ID.", e);
            return Collections.emptyMap();
        }
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
        try {
            return queryHistory(
                    new HazardEventQueryRequest(practice,
                            includeLatestVersion
                                    ? Include.HISTORICAL_AND_LATEST_EVENTS
                                    : Include.HISTORICAL_EVENTS));
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting all event histories.", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean removeAllCopiesOfEvent(String eventIdentifier) {
        try {
            return checkResponse(
                    hazardDataAccess.deleteAllWithIdentifier(eventIdentifier));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
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
     * @throws HazardEventServiceException
     *             If a problem occurs when attempting to query the history
     *             size.
     */
    private Map<String, Integer> queryHistorySize(
            HazardEventQueryRequest request)
            throws HazardEventServiceException {
        Map<String, Integer> historySizesForEventIdentifiers = Collections
                .emptyMap();

        /*
         * TODO: Remove this code once the HISTORICAL attribute is not being
         * used, and replace it with the commented-out code below it.
         * 
         * Iterate through the history lists returned, getting the size for
         * each. This is done instead of querying the sizes (the commented-out
         * code) because the latter would yield sizes of history lists if all
         * "latest" versions that might be found within said lists are included,
         * which they should not be. This will not be needed once "latest"
         * versions are no longer saved to history lists.
         */
        historySizesForEventIdentifiers = new HashMap<>();
        request.setSizeOnlyRequired(false);
        for (Map.Entry<String, HazardHistoryList> entry : queryHistory(request)
                .entrySet()) {
            historySizesForEventIdentifiers.put(entry.getKey(),
                    entry.getValue().size());
        }
        // HazardEventResponse response = hazardDataAccess.retrieve(request);
        // if (response.success()) {
        // historySizesForEventIdentifiers = response.getHistorySizeMap();
        // } else {
        // checkResponse(response);
        // }

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
