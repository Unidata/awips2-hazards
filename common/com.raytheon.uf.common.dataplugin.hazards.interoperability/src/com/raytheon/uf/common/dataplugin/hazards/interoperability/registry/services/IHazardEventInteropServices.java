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
package com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services;

import java.util.Date;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.annotations.FastInfoset;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.HazardConflictDict;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.HazardInteroperabilityResponse;

/**
 * 
 * Hazard Service endpoint interface for hazard interoperability web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IHazardEventInteropServices.NAMESPACE, name = "HazardEventInteropServices")
@SOAPBinding
public interface IHazardEventInteropServices {

    /** The path to this set of services */
    public static final String PATH = "/hazardEventInteropServices";

    /** The namespace for these services */
    public static final String NAMESPACE = "http://services.registry.hazards.interop.edex.uf.raytheon.com/";

    /** The service name */
    public static final String SERVICE_NAME = "HazardEventInteropSvc";

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
    public void store(
            @WebParam(name = "events") HazardInteroperabilityRecord... events)
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
    public void storeEventList(
            @WebParam(name = "events") List<HazardInteroperabilityRecord> events)
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
    public void delete(
            @WebParam(name = "events") HazardInteroperabilityRecord... events)
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
    public void deleteEventList(
            @WebParam(name = "events") List<HazardInteroperabilityRecord> events)
            throws HazardEventServiceException;

    /**
     * Deletes all hazard events from the registry
     * 
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteAll")
    public void deleteAll() throws HazardEventServiceException;

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
    public void update(
            @WebParam(name = "events") HazardInteroperabilityRecord... events)
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
    public void updateEventList(
            @WebParam(name = "events") List<HazardInteroperabilityRecord> events)
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
    public HazardInteroperabilityResponse retrieveByParams(
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
    public HazardInteroperabilityResponse retrieve(
            @WebParam(name = "request") HazardEventQueryRequest request)
            throws HazardEventServiceException;

    /**
     * Checks if there are conflicts with existing GFE grids
     * 
     * @param phenSig
     *            The phen/sig
     * @param siteID
     *            The site ID
     * @param startTime
     *            The start time
     * @param endTime
     *            The end time
     * @return True if conflicts exist, else false
     * @throws HazardEventServiceException
     */
    @WebMethod(operationName = "hasConflicts")
    public Boolean hasConflicts(@WebParam(name = "phenSig") String phenSig,
            @WebParam(name = "siteID") String siteID,
            @WebParam(name = "startTime") Date startTime,
            @WebParam(name = "endTime") Date endTime)
            throws HazardEventServiceException;

    /**
     * Retrieves the hazard conflict dictionary
     * 
     * @return The hazard conflict dictionary
     */
    @WebMethod(operationName = "retrieveHazardsConflictDict")
    public HazardConflictDict retrieveHazardsConflictDict();

    @WebMethod(operationName = "purgeInteropRecords")
    public void purgeInteropRecords() throws HazardEventServiceException;

    @WebMethod(operationName = "purgePracticeWarnings")
    public void purgePracticeWarnings() throws HazardEventServiceException;

    @WebMethod(operationName = "getActiveTable")
    public HazardInteroperabilityResponse getActiveTable(
            @WebParam(name = "siteID") String siteID)
            throws HazardEventServiceException;

    /**
     * Method used to test connectivity to this set of services
     * 
     * @return A ping response
     */
    @WebMethod(operationName = "ping")
    public String ping();
}
