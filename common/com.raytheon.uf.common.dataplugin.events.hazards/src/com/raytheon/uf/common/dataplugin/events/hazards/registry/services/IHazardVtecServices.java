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

import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.HazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventVtecResponse;

/**
 * 
 * Hazard Service VTEC record endpoint interface for hazard web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 3, 2016  18193    Ben.Phillippe Initial Creation
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IHazardVtecServices.NAMESPACE, name = "HazardVtecServices")
@SOAPBinding
public interface IHazardVtecServices {

    /** The path to this set of services */
    public static final String PATH = "/hazardVtecServices";

    /** The namespace for these services */
    public static final String NAMESPACE = "http://services.registry.hazards.edex.uf.raytheon.com/";

    /** The service name */
    public static final String SERVICE_NAME = "HazardVtecSvc";

    /**
     * Stores Hazard Event VTEC records to the registry
     * 
     * @param vtec
     *            The vtec records to store
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "storeVtec")
    public HazardEventVtecResponse storeVtec(@WebParam(name = "vtec")
    HazardEventVtec... vtec) throws HazardEventServiceException;

    /**
     * Stores Hazard Event VTEC records to the registry
     * 
     * @param vtec
     *            The VTEC records to store
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "storeVtecList")
    public HazardEventVtecResponse storeVtecList(@WebParam(name = "vtec")
    List<HazardEventVtec> events) throws HazardEventServiceException;

    /**
     * Deletes Hazard Event VTEC records from the registry
     * 
     * @param vtec
     *            The VTEC records to delete
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteVtec")
    public HazardEventVtecResponse deleteVtec(@WebParam(name = "vtec")
    HazardEventVtec... vtec) throws HazardEventServiceException;

    /**
     * Deletes Hazard Event VTEC records from the registry
     * 
     * @param vtec
     *            The VTEC records to delete
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteVtecList")
    public HazardEventVtecResponse deleteVtecList(@WebParam(name = "vtec")
    List<HazardEventVtec> vtec) throws HazardEventServiceException;

    /**
     * Deletes VTEC records using a registry query
     * 
     * @param request
     *            The query to execute
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteVtecByQuery")
    public HazardEventVtecResponse deleteVtecByQuery(
            @WebParam(name = "request")
            HazardEventQueryRequest request) throws HazardEventServiceException;

    /**
     * Deletes all hazard event VTEC records from the registry
     * 
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "deleteAllVtec")
    public HazardEventVtecResponse deleteAllVtec()
            throws HazardEventServiceException;

    /**
     * Updates Hazard Event VTEC records in the registry
     * 
     * @param vtec
     *            The events to update
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "updateVtec")
    public HazardEventVtecResponse updateVtec(@WebParam(name = "vtec")
    HazardEventVtec... vtec) throws HazardEventServiceException;

    /**
     * Updates Hazard Event VTEC records in the registry
     * 
     * @param vtec
     *            The VTEC records to update
     * @return A response object containing any errors encountered
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "updateVtecList")
    public HazardEventVtecResponse updateVtecList(@WebParam(name = "vtec")
    List<HazardEventVtec> vtec) throws HazardEventServiceException;

    /**
     * Retrieves VTEC records from the registry based on the given parameters.
     * The parameters are to be in batches of 3: Key, operand, value.
     * 
     * @param params
     *            The parameters to query for
     * @return The result of the query
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "retrieveVtecByParams")
    public HazardEventVtecResponse retrieveVtecByParams(
            @WebParam(name = "params")
            Object... params) throws HazardEventServiceException;

    /**
     * Retrieves hazard event VTEC records from the registry
     * 
     * @param request
     *            The request object encapsulating the query parameters
     * @return The result of the query
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs
     */
    @WebMethod(operationName = "retrieveVtec")
    public HazardEventVtecResponse retrieveVtec(@WebParam(name = "request")
    HazardEventQueryRequest request) throws HazardEventServiceException;

    @WebMethod(operationName = "getHazardVtecTable")
    public HazardEventVtecResponse getHazardVtecTable(
            @WebParam(name = "officeID")
            String officeID) throws HazardEventServiceException;
}
