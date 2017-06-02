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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;
import com.raytheon.uf.common.dataplugin.events.hazards.request.DeleteAllHazardEventsRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.DeleteAllHazardEventsWithIdRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.DeleteHazardEventRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GetRegistryInfoRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GetWfoRegionRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventIdRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.StoreHazardEventRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.UpdateHazardEventRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;

/**
 * 
 * Service implementation for the Hazard Services web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015  6895     Ben.Phillippe Refactored Hazard Service data access
 * Aug 04, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * Aug 13, 2015  8836     Chris.Cody    Changes for a configurable Event Id
 * Aug 20, 2015  6895     Ben.Phillippe Routing registry requests through request
 *                                      server
 * Oct 14, 2015 12494     Chris Golden  Reworked to allow hazard types to include
 *                                      only phenomenon (i.e. no significance) where
 *                                      appropriate.
 * Jan 20, 2016 14969     kbisanz       Improved exception message in retrieve()
 * May 03, 2016 18193     Ben.Phillippe Replication of Hazard VTEC Records.
 * May 06, 2016 18202     Robert.Blum   Changes for operational mode.
 * Feb 01, 2017 15556     Chris.Golden  Changed to always update insert time of
 *                                      events.
 * Feb 16, 2017 29138     Chris.Golden  Revamped to slim down the response to a
 *                                      query so that it does not carry extra
 *                                      serialized objects with it that are not
 *                                      needed.
 * Apr 13, 2017 33142     Chris.Golden  Added ability to delete all events with
 *                                      a particular event identifier.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventRequestServices implements IHazardEventServices {

    /** Practice mode status */
    private final boolean practice;

    /** The client used in practice mode */
    private static HazardEventRequestServices practiceClient = new HazardEventRequestServices(
            true);

    /** The client used in operational mode */
    private static HazardEventRequestServices operationalClient = new HazardEventRequestServices(
            false);

    /**
     * Creates a new HazardEventRequestServices in the given mode
     * 
     * @param practice
     *            True if in practice mode, false if in Operational mode
     */
    private HazardEventRequestServices(boolean practice) {
        this.practice = practice;
    }

    /**
     * Gets the instance for the given mode
     * 
     * @param practice
     *            True if in practice mode, false if in Operational mode
     * @return The HazardEventRequestServices instance for the given mode
     */
    public static HazardEventRequestServices getServices(boolean practice) {
        if (practice) {
            return practiceClient;
        } else {
            return operationalClient;
        }
    }

    @Override
    public HazardEventResponse store(HazardEvent... events)
            throws HazardEventServiceException {
        return storeEventList(Arrays.asList(events));
    }

    @Override
    public HazardEventResponse storeEventList(List<HazardEvent> events)
            throws HazardEventServiceException {
        StoreHazardEventRequest request = new StoreHazardEventRequest(events,
                this.practice);
        return routeRequest(request);
    }

    @Override
    public HazardEventResponse delete(HazardEvent... events)
            throws HazardEventServiceException {
        return deleteEventList(Arrays.asList(events));
    }

    @Override
    public HazardEventResponse deleteEventList(List<HazardEvent> events)
            throws HazardEventServiceException {
        DeleteHazardEventRequest request = new DeleteHazardEventRequest(events,
                this.practice);
        return routeRequest(request);
    }

    @Override
    public HazardEventResponse deleteAllWithIdentifier(String identifier)
            throws HazardEventServiceException {
        DeleteAllHazardEventsWithIdRequest request = new DeleteAllHazardEventsWithIdRequest(
                identifier, this.practice);
        return routeRequest(request);
    }

    @Override
    public HazardEventResponse deleteAll() throws HazardEventServiceException {
        return routeRequest(new DeleteAllHazardEventsRequest(this.practice));
    }

    @Override
    public HazardEventResponse update(HazardEvent... events)
            throws HazardEventServiceException {
        return updateEventList(Arrays.asList(events));
    }

    @Override
    public HazardEventResponse updateEventList(List<HazardEvent> events)
            throws HazardEventServiceException {
        UpdateHazardEventRequest request = new UpdateHazardEventRequest(events,
                this.practice);
        return routeRequest(request);
    }

    @Override
    public HazardEventResponse retrieveByParams(Object... params)
            throws HazardEventServiceException {
        if (params.length % 3 != 0) {
            throw new IllegalArgumentException(
                    "Parameters submitted to retrieve must of the form [key], [operand], [value]");
        }
        HazardEventQueryRequest request = new HazardEventQueryRequest(practice);
        for (int i = 0; i < params.length; i += 3) {
            request.and((String) params[i], (String) params[i + 1],
                    params[i + 2]);
        }
        return retrieve(request);
    }

    @Override
    public HazardEventResponse retrieve(HazardEventQueryRequest request)
            throws HazardEventServiceException {
        return routeRequest(request);
    }

    @Override
    public String requestEventId(String siteID)
            throws HazardEventServiceException {
        HazardEventIdRequest request = new HazardEventIdRequest(siteID,
                this.practice);
        String eventID = routeRequest(request).getPayload();
        return eventID;
    }

    @Override
    public String lookupRegion(String siteID)
            throws HazardEventServiceException {
        GetWfoRegionRequest request = new GetWfoRegionRequest(siteID,
                this.practice);
        String region = routeRequest(request).getPayload();
        return region;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getRegistryConnectionInfo()
            throws HazardEventServiceException {
        return (Map<String, String>) routeRequest(new GetRegistryInfoRequest())
                .getPayload();
    }

    /**
     * Routes the request to the request server
     * 
     * @param request
     *            The request to send
     * @return The response from the request server
     */
    private HazardEventResponse routeRequest(HazardRequest request)
            throws HazardEventServiceException {
        try {
            Object response = RequestRouter.route(request);
            if (response instanceof HazardEventResponse) {
                return (HazardEventResponse) response;
            } else {
                throw new HazardEventServiceException(
                        "Received incorrect response type. Expected instance of ["
                                + HazardEventResponse.class
                                + "] but instead received instance of ["
                                + response.getClass() + "] for request type ["
                                + request.getClass() + "]");
            }
        } catch (Exception e) {
            throw new HazardEventServiceException("Error routing request ["
                    + request.getClass() + "]", e);
        }
    }
}
