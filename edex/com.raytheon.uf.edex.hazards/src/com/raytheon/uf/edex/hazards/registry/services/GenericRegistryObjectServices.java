/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.edex.hazards.registry.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.annotations.FastInfoset;
import org.springframework.transaction.annotation.Transactional;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.GenericRegistryObjectResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServicesUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IGenericRegistryObjectServices;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GenericRegistryObjectQueryRequest;
import com.raytheon.uf.common.registry.ebxml.FactoryRegistryHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.registry.ebxml.dao.RegistryObjectDao;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

/**
 * Description: Service implementation for the generic object web services.
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
@WebService(targetNamespace = IGenericRegistryObjectServices.NAMESPACE, endpointInterface = "com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IGenericRegistryObjectServices", portName = "GenericRegistryObjectServicesPort", serviceName = IGenericRegistryObjectServices.SERVICE_NAME)
@SOAPBinding
@Transactional
public class GenericRegistryObjectServices
        implements IGenericRegistryObjectServices {

    // Private Static Constants

    /**
     * Logger.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GenericRegistryObjectServices.class);

    // Private Variables

    /**
     * Data access object used for registry objects.
     */
    private RegistryObjectDao dao;

    /**
     * Registry handler used to manipulate registry objects.
     */
    private FactoryRegistryHandler registryHandler;

    /**
     * Flag indicating whether or not this is a practice set of generic objects.
     */
    private boolean practice;

    /**
     * Web service context.
     */
    @Resource
    private WebServiceContext webServiceContext;

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public GenericRegistryObjectServices() {

        /*
         * No action.
         */
    }

    @Override
    @WebMethod(operationName = "store")
    public GenericRegistryObjectResponse store(
            @WebParam(name = "genericObjects") GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException {
        return storeList(Arrays.asList(genericObjects));
    }

    @Override
    @WebMethod(operationName = "storeList")
    public GenericRegistryObjectResponse storeList(
            @WebParam(name = "genericObjects") List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException {
        statusHandler.info("Creating " + genericObjects.size()
                + " GenericRegistryObjects: ");
        String userName = webServiceContext.getUserPrincipal().getName();
        GenericRegistryObjectResponse response = new GenericRegistryObjectResponse();
        try {
            for (GenericRegistryObject genericObject : genericObjects) {
                genericObject.setPractice(practice);
                response.addExceptions(registryHandler
                        .storeOrReplaceObject(userName, genericObject)
                        .getErrors());
            }
        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error storing generic objects", e);
        }
        return checkResponse("STORE",
                "Created " + genericObjects.size() + " GenericRegistryObjects.",
                response);
    }

    @Override
    @WebMethod(operationName = "update")
    public GenericRegistryObjectResponse update(
            @WebParam(name = "genericObjects") GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException {
        return updateList(Arrays.asList(genericObjects));
    }

    @Override
    @WebMethod(operationName = "updateList")
    public GenericRegistryObjectResponse updateList(
            @WebParam(name = "genericObjects") List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException {
        statusHandler.info("Updating " + genericObjects.size()
                + " GenericRegistryObjects: ");
        String userName = webServiceContext.getUserPrincipal().getName();
        GenericRegistryObjectResponse response = new GenericRegistryObjectResponse();
        try {
            for (GenericRegistryObject genericObject : genericObjects) {
                genericObject.setPractice(practice);
                response.addExceptions(registryHandler
                        .storeOrReplaceObject(userName, genericObject)
                        .getErrors());
            }
        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error updating generic objects", e);
        }
        return checkResponse("UPDATE",
                "Updated " + genericObjects.size() + " GenericRegistryObjects.",
                response);
    }

    @Override
    @WebMethod(operationName = "delete")
    public GenericRegistryObjectResponse delete(
            @WebParam(name = "genericObjects") GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException {
        return deleteList(Arrays.asList(genericObjects));
    }

    @Override
    @WebMethod(operationName = "deleteList")
    public GenericRegistryObjectResponse deleteList(
            @WebParam(name = "genericObjects") List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException {
        statusHandler.info("Deleting " + genericObjects.size()
                + " GenericRegistryObjects.");
        String userName = webServiceContext.getUserPrincipal().getName();
        GenericRegistryObjectResponse response = new GenericRegistryObjectResponse();
        try {
            response.addExceptions(registryHandler
                    .removeObjects(userName, new ArrayList<>(genericObjects))
                    .getErrors());
        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error deleting generic objects", e);
        }
        return checkResponse("DELETE",
                "Deleted " + genericObjects.size() + " GenericRegistryObjects",
                response);
    }

    @Override
    @WebMethod(operationName = "deleteAll")
    public GenericRegistryObjectResponse deleteAll()
            throws HazardEventServiceException {
        statusHandler
                .info("Deleting all GenericRegistryObjects from the registry");
        GenericRegistryObjectResponse deleteAllResponse = new GenericRegistryObjectResponse();
        try {
            GenericRegistryObjectResponse retrieveResponse = retrieve(
                    new GenericRegistryObjectQueryRequest(practice));
            if (retrieveResponse.success()) {
                if (retrieveResponse.getGenericObjects().isEmpty()) {
                    deleteAllResponse.merge(retrieveResponse);
                } else {
                    String userName = webServiceContext.getUserPrincipal()
                            .getName();
                    deleteAllResponse
                            .addExceptions(registryHandler
                                    .removeObjects(userName,
                                            new ArrayList<>(retrieveResponse
                                                    .getGenericObjects()))
                            .getErrors());
                }
            } else {
                deleteAllResponse.merge(retrieveResponse);
            }

        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error deleting all generic objects", e);
        }
        return checkResponse("DELETE_ALL", "", deleteAllResponse);
    }

    @Override
    @WebMethod(operationName = "retrieve")
    public GenericRegistryObjectResponse retrieve(
            @WebParam(name = "request") GenericRegistryObjectQueryRequest request)
                    throws HazardEventServiceException {
        statusHandler.info(
                "Executing query for GenericRegistryObjects:\n " + request);
        GenericRegistryObjectResponse response = new GenericRegistryObjectResponse();
        try {
            String query = HazardEventServicesUtil.createAttributeQuery(
                    practice, GenericRegistryObject.class,
                    request.getQueryParams(),
                    request.getQueryParameterKeyGenerator());
            // Workaround to ensure unique results are returned
            List<Object> objects = dao.executeHQLQuery(query);
            Collection<RegistryObjectType> registryObjectTypes = new LinkedHashSet<>();
            for (Object object : objects) {
                registryObjectTypes.add((RegistryObjectType) object);
            }
            response.setGenericObjects(
                    HazardEventServicesUtil.getContentObjects(
                            registryObjectTypes, GenericRegistryObject.class));
        } catch (Throwable e) {
            throw new HazardEventServiceException(
                    "Error retrieving generic objects with request: " + request,
                    e);
        }
        return checkResponse("QUERY",
                "Retrieved " + response.getGenericObjects().size()
                        + " GenericRegistryObjects.",
                response);
    }

    /**
     * Get the data access object.
     * 
     * @return Data access object.
     */
    public RegistryObjectDao getDao() {
        return dao;
    }

    /**
     * Set the data access object.
     * 
     * @param dao
     *            Data access object.
     */
    public void setDao(RegistryObjectDao dao) {
        this.dao = dao;
    }

    /**
     * Set the registry handler.
     * 
     * @param registryHandler
     *            Registry handler.
     */
    public void setRegistryHandler(FactoryRegistryHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    /**
     * Set the flag indicating whether not practice mode is in effect.
     * 
     * @param practice
     *            Flag indicating whether not practice mode is in effect.
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
    }

    // Private Methods

    /**
     * Check the specified response and outputs the standard message.
     * 
     * @param operation
     *            Operation that was executed.
     * @param details
     *            Any additional details to add to the message.
     * @param response
     *            Response that was generated.
     * @return Response.
     */
    private GenericRegistryObjectResponse checkResponse(String operation,
            String details, GenericRegistryObjectResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response.success()) {
            builder.append("Successfully executed [");
            builder.append(operation);
            builder.append("] operation.\n\tDetails: ");
            builder.append(details);
            statusHandler.info(builder.toString());
        } else {
            builder.append("Failed to execute [");
            builder.append(operation);
            builder.append("] operation.\n\tDetails: ");
            builder.append(details);
            statusHandler.error(builder.toString());
        }
        return response;
    }

}
