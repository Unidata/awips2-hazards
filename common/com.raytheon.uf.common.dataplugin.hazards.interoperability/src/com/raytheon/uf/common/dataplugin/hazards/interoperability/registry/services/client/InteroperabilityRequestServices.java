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
package com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GetHazardActiveTableRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.HazardConflictDict;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.HazardInteroperabilityResponse;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.IHazardEventInteropServices;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.DeleteAllInteroperabilityRecordsRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.DeleteInteroperabilityRecordRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.InteroperabilityConflictsRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.InteroperabilityRecordQueryRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.PurgeInteroperabilityRecordsRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.PurgePracticeWarningsRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.StoreInteroperabilityRecordRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.UpdateInteroperabilityRecordRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;

/**
 * 
 * Suite of interoperability services that are routed through the request server
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * May 06, 2016 18202      Robert.Blum Changes for operational mode.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class InteroperabilityRequestServices implements
        IHazardEventInteropServices {

    /** The practice mode flag */
    private final boolean practice;

    /** The instance to use for practice mode */
    private static InteroperabilityRequestServices practiceClient = new InteroperabilityRequestServices(
            true);

    /** The instance to use for operational mode */
    private static InteroperabilityRequestServices operationalClient = new InteroperabilityRequestServices(
            false);

    /**
     * Creates a new InteroperabilityRequestServices
     * 
     * @param practice
     *            The practice mode flag
     */
    private InteroperabilityRequestServices(boolean practice) {
        this.practice = practice;
    }

    /**
     * Gets the InteroperabilityRequestServices applicable for the given mode
     * 
     * @param practice
     *            True for practice mode, false for operational mode
     * @return The InteroperabilityRequestServices instance for the specified
     *         mode
     */
    public static InteroperabilityRequestServices getServices(boolean practice) {
        if (practice) {
            return practiceClient;
        } else {
            return operationalClient;
        }
    }

    @Override
    public void store(HazardInteroperabilityRecord... events)
            throws HazardEventServiceException {
        storeEventList(Arrays.asList(events));
    }

    @Override
    public void storeEventList(List<HazardInteroperabilityRecord> events)
            throws HazardEventServiceException {
        routeRequest(new StoreInteroperabilityRecordRequest(events,
                this.practice));
    }

    @Override
    public void delete(HazardInteroperabilityRecord... events)
            throws HazardEventServiceException {
        deleteEventList(Arrays.asList(events));
    }

    @Override
    public void deleteEventList(List<HazardInteroperabilityRecord> events)
            throws HazardEventServiceException {
        routeRequest(new DeleteInteroperabilityRecordRequest(events,
                this.practice));
    }

    @Override
    public void deleteAll() throws HazardEventServiceException {
        routeRequest(new DeleteAllInteroperabilityRecordsRequest(this.practice));
    }

    @Override
    public void update(HazardInteroperabilityRecord... events)
            throws HazardEventServiceException {
        updateEventList(Arrays.asList(events));

    }

    @Override
    public void updateEventList(List<HazardInteroperabilityRecord> events)
            throws HazardEventServiceException {
        routeRequest(new UpdateInteroperabilityRecordRequest(events,
                this.practice));

    }

    @Override
    public HazardInteroperabilityResponse retrieveByParams(Object... params)
            throws HazardEventServiceException {
        InteroperabilityRecordQueryRequest request = new InteroperabilityRecordQueryRequest(
                practice);
        request.setPractice(this.practice);
        for (int i = 0; i < params.length; i++) {
            request.and((String) params[i], (String) params[i + 1],
                    params[i + 2]);
        }
        return routeRequest(request);
    }

    @Override
    public HazardInteroperabilityResponse retrieve(
            HazardEventQueryRequest request) throws HazardEventServiceException {
        return retrieveByParams(HazardEventServicesUtil
                .convertQueryToArray(request));
    }

    @Override
    public Boolean hasConflicts(String phenSig, String siteID, Date startTime,
            Date endTime) throws HazardEventServiceException {
        HazardInteroperabilityResponse response = routeRequest(new InteroperabilityConflictsRequest(
                this.practice, phenSig, siteID, startTime, endTime));
        return response.getPayload();
    }

    @Override
    public HazardConflictDict retrieveHazardsConflictDict() {
        return null;
    }

    @Override
    public void purgeInteropRecords() throws HazardEventServiceException {
        routeRequest(new PurgeInteroperabilityRecordsRequest(this.practice));
    }

    @Override
    public void purgePracticeWarnings() throws HazardEventServiceException {
        routeRequest(new PurgePracticeWarningsRequest(this.practice));

    }

    @Override
    public HazardInteroperabilityResponse getActiveTable(String siteID)
            throws HazardEventServiceException {
        return routeRequest(new GetHazardActiveTableRequest(this.practice,
                siteID));
    }

    @Override
    public String ping() {
        return null;
    }

    /**
     * Routes the request to the request server
     * 
     * @param request
     *            The request to send
     * @return The response from the request server
     */
    private HazardInteroperabilityResponse routeRequest(HazardRequest request)
            throws HazardEventServiceException {
        try {
            Object response = RequestRouter.route(request);
            if (response instanceof HazardInteroperabilityResponse) {
                return (HazardInteroperabilityResponse) response;
            } else {
                throw new HazardEventServiceException(
                        "Received incorrect response type. Expected instance of ["
                                + HazardInteroperabilityResponse.class
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
