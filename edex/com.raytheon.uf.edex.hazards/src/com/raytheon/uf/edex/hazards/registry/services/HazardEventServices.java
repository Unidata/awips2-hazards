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
package com.raytheon.uf.edex.hazards.registry.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import org.apache.cxf.annotations.FastInfoset;
import org.springframework.transaction.annotation.Transactional;

import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.query.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;
import com.raytheon.uf.common.localization.region.RegionLookup;
import com.raytheon.uf.common.registry.ebxml.FactoryRegistryHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterTask;
import com.raytheon.uf.edex.hazards.notification.HazardNotifier;
import com.raytheon.uf.edex.registry.ebxml.dao.RegistryObjectDao;
import com.raytheon.uf.edex.registry.ebxml.util.EbxmlObjectUtil;

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
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Aug 13, 2015 8836      Chris.Cody    Changes for a configurable Event Id
 * Oct 14, 2015 12494     Chris Golden  Reworked to allow hazard types to include
 *                                      only phenomenon (i.e. no significance) where
 *                                      appropriate.
 * Feb 01, 2017 15556     Chris.Golden  Changed to always update insert time of
 *                                      events.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IHazardEventServices.NAMESPACE, endpointInterface = "com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices", portName = "HazardEventServicesPort", serviceName = IHazardEventServices.SERVICE_NAME)
@SOAPBinding
@Transactional
public class HazardEventServices implements IHazardEventServices {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventServices.class);

    /** The lock id for operational cluster locks */
    private static final String OPERATIONAL_LOCK_NAME = "Operational Hazard Services Event Id";

    /** The lock id for the practice cluster locks */
    private static final String PRACTICE_LOCK_NAME = "Practice Hazard Services Event Id";

    /** Data Access Object used for registry objects */
    private RegistryObjectDao dao;

    /** Registry handler used to manipulate registry objects */
    private FactoryRegistryHandler registryHandler;

    /** Denotes if this is a practice set of services */
    private boolean practice;

    /** The mode (PRACTICE, OPERATIONAL) derived from the practice boolean */
    private Mode mode;

    /** Hazard notifier for sending event notifications */
    private HazardNotifier hazardNotifier;

    /** Web service context */
    @Resource
    private WebServiceContext wsContext;

    /**
     * Creates an empty HazardEventServices
     */
    public HazardEventServices() {

    }

    @Override
    @WebMethod(operationName = "store")
    public HazardEventResponse store(
            @WebParam(name = "events") HazardEvent... events)
            throws HazardEventServiceException {
        return storeEventList(Arrays.asList(events));
    }

    @Override
    @WebMethod(operationName = "storeEventList")
    public HazardEventResponse storeEventList(
            @WebParam(name = "events") List<HazardEvent> events)
            throws HazardEventServiceException {
        statusHandler.info("Creating " + events.size() + " HazardEvents: ");
        String userName = wsContext.getUserPrincipal().getName();
        HazardEventResponse response = new HazardEventResponse();
        try {
            validateEvents(events);
            for (HazardEvent event : events) {
                String phensig = HazardEventUtilities.getHazardPhenSig(event);
                if (event.getSubType() != null && !event.getSubType().isEmpty()) {
                    phensig += "." + event.getSubType();
                }
                event.addHazardAttribute(HazardConstants.PHEN_SIG, phensig);
                event.addHazardAttribute("practice", practice);
                event.setInsertTime(new Date());
                response.addExceptions(registryHandler.storeOrReplaceObject(
                        userName, event).getErrors());
                hazardNotifier.notify(event, NotificationType.STORE, mode);
            }
        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Storing Events", e);
        }
        return checkResponse("STORE", "Created " + events.size()
                + " HazardEvents.", response);
    }

    @Override
    @WebMethod(operationName = "delete")
    public HazardEventResponse delete(
            @WebParam(name = "events") HazardEvent... events)
            throws HazardEventServiceException {
        return deleteEventList(Arrays.asList(events));
    }

    @Override
    @WebMethod(operationName = "deleteEventList")
    public HazardEventResponse deleteEventList(
            @WebParam(name = "events") List<HazardEvent> events)
            throws HazardEventServiceException {
        statusHandler.info("Deleting " + events.size() + " HazardEvents.");
        String userName = wsContext.getUserPrincipal().getName();
        HazardEventResponse response = new HazardEventResponse();
        try {
            validateEvents(events);
            response.addExceptions(registryHandler.removeObjects(userName,
                    new ArrayList<HazardEvent>(events)).getErrors());
            for (HazardEvent event : events) {
                hazardNotifier.notify(event, NotificationType.DELETE, mode);
            }
        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Deleting Events", e);
        }
        return checkResponse("DELETE", "Deleted " + events.size()
                + " HazardEvents", response);
    }

    @Override
    @WebMethod(operationName = "deleteAll")
    public HazardEventResponse deleteAll() throws HazardEventServiceException {
        statusHandler.info("Deleting all HazardEvents from the Registry");
        HazardEventResponse deleteAllResponse = new HazardEventResponse();
        try {
            HazardEventResponse retrieveResponse = retrieve(new HazardEventQueryRequest());

            if (retrieveResponse.success()) {
                if (retrieveResponse.getEvents().isEmpty()) {
                    deleteAllResponse.merge(retrieveResponse);
                } else {
                    HazardEventResponse deleteResponse = deleteEventList(retrieveResponse
                            .getEvents());
                    if (!deleteResponse.success()) {
                        deleteAllResponse.merge(deleteResponse);
                    }
                }
            } else {
                deleteAllResponse.merge(retrieveResponse);
            }

        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Deleting Events", e);
        }
        return checkResponse("DELETE_ALL", "", deleteAllResponse);
    }

    @Override
    @WebMethod(operationName = "update")
    public HazardEventResponse update(
            @WebParam(name = "events") HazardEvent... events)
            throws HazardEventServiceException {
        return updateEventList(Arrays.asList(events));
    }

    @Override
    @WebMethod(operationName = "updateEventList")
    public HazardEventResponse updateEventList(
            @WebParam(name = "events") List<HazardEvent> events)
            throws HazardEventServiceException {
        statusHandler.info("Updating " + events.size() + " HazardEvents: ");
        String userName = wsContext.getUserPrincipal().getName();
        HazardEventResponse response = new HazardEventResponse();
        try {
            validateEvents(events);
            for (HazardEvent event : events) {

                String phensig = HazardEventUtilities.getHazardPhenSig(event);
                if (event.getSubType() != null && !event.getSubType().isEmpty()) {
                    phensig += "." + event.getSubType();
                }
                event.addHazardAttribute(HazardConstants.PHEN_SIG, phensig);
                event.addHazardAttribute("practice", practice);
                event.setInsertTime(new Date());
                response.addExceptions(registryHandler.storeOrReplaceObject(
                        userName, event).getErrors());
                hazardNotifier.notify(event, NotificationType.UPDATE, mode);
            }
        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Updating Events", e);
        }
        return checkResponse("UPDATE", "Updated " + events.size()
                + " HazardEvents.", response);
    }

    @Override
    @WebMethod(operationName = "retrieveByParams")
    public HazardEventResponse retrieveByParams(
            @WebParam(name = "params") Object... params)
            throws HazardEventServiceException {
        HazardEventQueryRequest request = null;
        if (params.length % 3 != 0) {
            throw new IllegalArgumentException(
                    "Incorrect number of arguments submitted to retrieve");
        } else {
            request = convertArrayToQuery(params);
        }
        return retrieve(request);

    }

    @Override
    @WebMethod(operationName = "retrieve")
    public HazardEventResponse retrieve(
            @WebParam(name = "request") HazardEventQueryRequest request)
            throws HazardEventServiceException {
        statusHandler.info("Executing Query for HazardEvents:\n " + request);
        HazardEventResponse response = new HazardEventResponse();
        try {
            String query = HazardEventServicesUtil.createAttributeQuery(
                    practice, request.getQueryParams());
            List<RegistryObjectType> result = dao.executeHQLQuery(query);
            response.setEvents(HazardEventServicesUtil.getHazardEvents(result));
        } catch (Throwable e) {
            throw new HazardEventServiceException("Error Deleting Events", e);
        }
        return checkResponse("QUERY", "Retrieved "
                + response.getEvents().size() + " HazardEvents.", response);
    }

    @Override
    @WebMethod(operationName = "requestEventId")
    public String requestEventId(@WebParam(name = "siteID") String siteID)
            throws HazardEventServiceException {
        statusHandler.info("Requesting Event ID for Site [" + siteID + "] in ["
                + (practice ? "Practice] Mode" : "Operational] Mode"));
        // have different numbering depending on practice/operational hazards
        String lockName = practice ? PRACTICE_LOCK_NAME : OPERATIONAL_LOCK_NAME;
        ClusterTask task = ClusterLockUtils.lookupLock(lockName, siteID);
        task = ClusterLockUtils.lock(lockName, siteID, task.getExtraInfo(), 15,
                true);
        Integer eventId = 0;
        if (task.getExtraInfo() == null || task.getExtraInfo().isEmpty()) {

            List<HazardEvent> events = retrieve(
                    new HazardEventQueryRequest(HazardConstants.SITE_ID, siteID))
                    .getEvents();

            if (events.isEmpty()) {
                // starting at 1 if none exists in the database
                eventId = 1;
            } else {
                // we don't find the site in the cluster_task table, but we have
                // some in the hazards table, we want to make sure we start from
                // that value
                // Hazard Event Id values are a STRING in the form:
                // HZ-SSS-YYYY-000000
                // Take the LAST segment of the id and parse that
                // value into an Integer.
                // (this value should also be incremented.)
                Integer highestValue = Integer.valueOf(0);
                for (HazardEvent event : events) {
                    String currentEventIdString = event.getEventID();
                    String serialId = null;
                    int lastDashIdx = currentEventIdString.lastIndexOf("-");
                    if (lastDashIdx > 0) {
                        serialId = currentEventIdString
                                .substring(lastDashIdx + 1);
                    } else {
                        // Possibly an OLD Id value?
                        serialId = currentEventIdString;
                    }
                    try {
                        Integer curSerialIdInt = Integer.parseInt(serialId);
                        if (curSerialIdInt.intValue() > highestValue.intValue()) {
                            highestValue = curSerialIdInt;
                        }
                    } catch (NumberFormatException nfe) {
                        this.statusHandler
                                .info("Unknown stored Hazard Event Id "
                                        + currentEventIdString
                                        + " unable to parse serial ID.");
                    }
                }
                eventId = highestValue + 1;
            }
        } else {
            eventId = Integer.parseInt(task.getExtraInfo()) + 1;
        }
        String serialEventIdString = String.valueOf(eventId);
        ClusterLockUtils.updateExtraInfo(lockName, siteID, serialEventIdString);
        ClusterLockUtils.unlock(task, false);
        statusHandler.info("Returning Event ID of [" + serialEventIdString
                + "] for Site [" + siteID + "] in ["
                + (practice ? "Practice] Mode" : "Operational] Mode"));
        return (serialEventIdString);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.registry.
     * IHazardEventServices#lookupRegion(java.lang.String, java.lang.String)
     */
    @Override
    @WebMethod(operationName = "lookupRegion")
    public String lookupRegion(@WebParam(name = "siteID") String siteID) {
        return RegionLookup.getWfoRegion(siteID);
    }

    @Override
    @WebMethod(operationName = "ping")
    public String ping() {
        statusHandler.info("Received Ping from "
                + EbxmlObjectUtil.getClientHost(wsContext));
        return "OK";
    }

    /**
     * Validates events
     * 
     * @param events
     *            The events to validate
     * @throws ValidationException
     *             If validation errors occur
     */
    private void validateEvents(List<HazardEvent> events)
            throws ValidationException {
        for (HazardEvent event : events) {
            event.isValid();
        }
    }

    /**
     * Checks the response and outputs the standard message
     * 
     * @param operation
     *            The operation executed
     * @param details
     *            Any additional details to add to the message
     * @param response
     *            The response object
     * @return The REsponse object
     */
    private HazardEventResponse checkResponse(String operation, String details,
            final HazardEventResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response.success()) {
            builder.append("Successfully executed [");
            builder.append(operation);
            builder.append("] operation.\n\tDetails: ");
            builder.append(details);
            statusHandler.info(builder.toString());
        }
        return response;
    }

    /**
     * Converts a query submitted as an array into a query request object
     * 
     * @param params
     *            The parameters to convert
     * @return
     */
    private HazardEventQueryRequest convertArrayToQuery(Object[] params) {
        HazardEventQueryRequest request = new HazardEventQueryRequest();
        for (int i = 0; i < params.length; i += 3) {
            if ((params[i] instanceof String)
                    && (params[i + 1] instanceof String)) {
                request.and((String) params[i], (String) params[i + 1],
                        params[i + 2]);
            } else {
                throw new IllegalArgumentException(
                        "Incorrect parameter types received by retrieve query");
            }
        }
        return request;
    }

    /**
     * @return the dao
     */
    public RegistryObjectDao getDao() {
        return dao;
    }

    /**
     * @param dao
     *            the dao to set
     */
    public void setDao(RegistryObjectDao dao) {
        this.dao = dao;
    }

    /**
     * @param registryHandler
     *            the registryHandler to set
     */
    public void setRegistryHandler(FactoryRegistryHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    /**
     * @param practice
     *            the practice to set
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
        this.mode = this.practice ? Mode.PRACTICE : Mode.OPERATIONAL;
    }

    /**
     * @param hazardNotifier
     *            the hazardNotifier to set
     */
    public void setHazardNotifier(HazardNotifier hazardNotifier) {
        this.hazardNotifier = hazardNotifier;
    }

}
