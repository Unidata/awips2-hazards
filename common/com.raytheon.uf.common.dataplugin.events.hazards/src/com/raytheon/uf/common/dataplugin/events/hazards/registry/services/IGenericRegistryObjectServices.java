/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.cxf.annotations.FastInfoset;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.GenericRegistryObjectResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GenericRegistryObjectQueryRequest;

/**
 * 
 * Description: Hazard Service endpoint interface for generic object web
 * services.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 02, 2017   38506    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IGenericRegistryObjectServices.NAMESPACE, name = "GenericRegistryObjectServices")
@SOAPBinding
public interface IGenericRegistryObjectServices {

    // Public Static Constants

    /**
     * Path to this set of services.
     */
    public static final String PATH = "/genericRegistryObjectServices";

    /**
     * Namespace for these services.
     */
    public static final String NAMESPACE = "http://services.registry.hazards.edex.uf.raytheon.com/";

    /**
     * Service name.
     */
    public static final String SERVICE_NAME = "GenericRegistryObjectSvc";

    // Public Methods

    /**
     * Store the specified generic object(s).
     * 
     * @param genericObjects
     *            Generic object(s) to be stored.
     * @return Response object containing any errors encountered.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "store")
    public GenericRegistryObjectResponse store(
            @WebParam(name = "genericObjects") GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException;

    /**
     * Store the specified generic objects.
     * 
     * @param genericObjects
     *            Generic objects to be stored.
     * @return Response object containing any errors encountered.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "storeList")
    public GenericRegistryObjectResponse storeList(
            @WebParam(name = "genericObjects") List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException;

    /**
     * Update the specified generic object(s).
     * 
     * @param genericObjects
     *            Generic object(s) to be updated.
     * @return Response object containing any errors encountered.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "update")
    public GenericRegistryObjectResponse update(
            @WebParam(name = "genericObjects") GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException;

    /**
     * Update the specified generic objects.
     * 
     * @param genericObjects
     *            Generic objects to be updated.
     * @return Response object containing any errors encountered.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "updateList")
    public GenericRegistryObjectResponse updateList(
            @WebParam(name = "genericObjects") List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException;

    /**
     * Delete the specified generic object(s).
     * 
     * @param genericObjects
     *            Generic object(s) to be deleted.
     * @return Response object containing any errors encountered.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "delete")
    public GenericRegistryObjectResponse delete(
            @WebParam(name = "genericObjects") GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException;

    /**
     * Delete the specified generic objects.
     * 
     * @param genericObjects
     *            Generic objects to be deleted.
     * @return Response object containing any errors encountered.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "deleteList")
    public GenericRegistryObjectResponse deleteList(
            @WebParam(name = "genericObjects") List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException;

    /**
     * Delete all generic objects.
     * 
     * @return Response object containing any errors encountered.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "deleteAll")
    public GenericRegistryObjectResponse deleteAll()
            throws HazardEventServiceException;

    /**
     * Retrieve generic objects.
     * 
     * @param request
     *            Request object encapsulating the query parameters.
     * @return Result of the query.
     * @throws HazardEventServiceException
     *             If an unrecoverable error occurs.
     */
    @WebMethod(operationName = "retrieve")
    public GenericRegistryObjectResponse retrieve(
            @WebParam(name = "request") GenericRegistryObjectQueryRequest request)
                    throws HazardEventServiceException;
}