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
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.annotations.FastInfoset;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;

/**
 * 
 * Hazard Service endpoint interface for hazard web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Aug 20, 2015 6895      Ben.Phillippe Routing registry requests through
 *                                      request server
 * Apr 13, 2017 33142     Chris.Golden  Added ability to delete all events
 *                                      with a particular event identifier.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IHazardEventServices.NAMESPACE, name = "HazardEventServices")
@SOAPBinding
public interface IHazardEventServices {

    /** The path to this set of services */
    public static final String PATH = "/hazardEventServices";

    /** The namespace for these services */
    public static final String NAMESPACE = "http://services.registry.hazards.edex.uf.raytheon.com/";

    /** The service name */
    public static final String SERVICE_NAME = "HazardEventSvc";

    /**
     * Stores Hazard Events to the registry
     * 
     * @param events
     *            The events to store
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "store")
    public HazardEventResponse store(
            @WebParam(name = "events") HazardEvent... events)
            throws HazardEventServiceException;

    /**
     * Stores Hazard Events to the registry
     * 
     * @param events
     *            The events to store
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "storeEventList")
    public HazardEventResponse storeEventList(
            @WebParam(name = "events") List<HazardEvent> events)
            throws HazardEventServiceException;

    /**
     * Deletes Hazard Events from the registry
     * 
     * @param events
     *            The events to delete
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "delete")
    public HazardEventResponse delete(
            @WebParam(name = "events") HazardEvent... events)
            throws HazardEventServiceException;

    /**
     * Deletes Hazard Events from the registry
     * 
     * @param events
     *            The events to delete
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteEventList")
    public HazardEventResponse deleteEventList(
            @WebParam(name = "events") List<HazardEvent> events)
            throws HazardEventServiceException;

    /**
     * Deletes all hazard events with the specified event identifier from the
     * registry
     * 
     * @param identifier
     *            Identifier of the events to be deleted.
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteAllWithIdentifier")
    public HazardEventResponse deleteAllWithIdentifier(
            @WebParam(name = "identifier") String identifier)
            throws HazardEventServiceException;

    /**
     * Deletes all hazard events from the registry
     * 
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteAll")
    public HazardEventResponse deleteAll() throws HazardEventServiceException;

    /**
     * Updates Hazard Events in the registry
     * 
     * @param events
     *            The events to update
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "update")
    public HazardEventResponse update(
            @WebParam(name = "events") HazardEvent... events)
            throws HazardEventServiceException;

    /**
     * Updates Hazard Events in the registry
     * 
     * @param events
     *            The events to update
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "updateEventList")
    public HazardEventResponse updateEventList(
            @WebParam(name = "events") List<HazardEvent> events)
            throws HazardEventServiceException;

    /**
     * Retrieves hazard events from the registry based on the given parameters.
     * The parameters are to be in batches of 3: Key, operand, value.
     * 
     * @param params
     *            The parameters to query for
     * @return The result of the query
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "retrieveByParams")
    public HazardEventResponse retrieveByParams(
            @WebParam(name = "params") Object... params)
            throws HazardEventServiceException;

    /**
     * Retrieves hazard events from the registry
     * 
     * @param request
     *            The request object encapsulating the query parameters
     * @return The result of the query
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "retrieve")
    public HazardEventResponse retrieve(
            @WebParam(name = "request") HazardEventQueryRequest request)
            throws HazardEventServiceException;

    /**
     * Request a new event ID for the given site
     * 
     * @param siteID
     *            The site ID
     * @return The new event ID
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "requestEventId")
    public String requestEventId(@WebParam(name = "siteID") String siteID)
            throws HazardEventServiceException;

    /**
     * Looks up the region for the given site
     * 
     * @param siteID
     *            The site ID
     * @return The region
     */
    @WebMethod(operationName = "lookupRegion")
    public String lookupRegion(@WebParam(name = "siteID") String siteID)
            throws HazardEventServiceException;;

    /**
     * Method used to test connectivity to this set of services
     * 
     * @return A ping response
     */
    @WebMethod(operationName = "ping")
    public String ping();

}