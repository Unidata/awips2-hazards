/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client;

import java.util.Arrays;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.GenericRegistryObjectResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IGenericRegistryObjectServices;
import com.raytheon.uf.common.dataplugin.events.hazards.request.DeleteAllGenericRegistryObjectsRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.DeleteGenericRegistryObjectRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GenericRegistryObjectQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.StoreGenericRegistryObjectRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.UpdateGenericRegistryObjectRequest;
import com.raytheon.uf.common.serialization.comm.RequestRouter;

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
public class GenericRegistryObjectRequestServices
        implements IGenericRegistryObjectServices {

    // Private Static Variables

    /**
     * Client used in practice mode.
     */
    private static GenericRegistryObjectRequestServices practiceClient = new GenericRegistryObjectRequestServices(
            true);

    /**
     * Client used in operational mode.
     */
    private static GenericRegistryObjectRequestServices operationalClient = new GenericRegistryObjectRequestServices(
            false);

    // Private Variables

    /**
     * Flag indicating whether or not practice mode is in effect.
     */
    private final boolean practice;

    // Public Static Methods

    /**
     * Get the instance for the specified mode.
     * 
     * @param practice
     *            Flag indicating whether or not the instance for practice mode
     *            is desired.
     * @return Instance for the given mode.
     */
    public static GenericRegistryObjectRequestServices getServices(
            boolean practice) {
        if (practice) {
            return practiceClient;
        } else {
            return operationalClient;
        }
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    private GenericRegistryObjectRequestServices(boolean practice) {
        this.practice = practice;
    }

    // Public Methods

    @Override
    public GenericRegistryObjectResponse store(
            GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException {
        return storeList(Arrays.asList(genericObjects));
    }

    @Override
    public GenericRegistryObjectResponse storeList(
            List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException {
        StoreGenericRegistryObjectRequest request = new StoreGenericRegistryObjectRequest(
                genericObjects, this.practice);
        return routeRequest(request);
    }

    @Override
    public GenericRegistryObjectResponse update(
            GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException {
        return updateList(Arrays.asList(genericObjects));
    }

    @Override
    public GenericRegistryObjectResponse updateList(
            List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException {
        UpdateGenericRegistryObjectRequest request = new UpdateGenericRegistryObjectRequest(
                genericObjects, this.practice);
        return routeRequest(request);
    }

    @Override
    public GenericRegistryObjectResponse delete(
            GenericRegistryObject... genericObjects)
                    throws HazardEventServiceException {
        return deleteList(Arrays.asList(genericObjects));
    }

    @Override
    public GenericRegistryObjectResponse deleteList(
            List<GenericRegistryObject> genericObjects)
                    throws HazardEventServiceException {
        DeleteGenericRegistryObjectRequest request = new DeleteGenericRegistryObjectRequest(
                genericObjects, this.practice);
        return routeRequest(request);
    }

    @Override
    public GenericRegistryObjectResponse deleteAll()
            throws HazardEventServiceException {
        return routeRequest(
                new DeleteAllGenericRegistryObjectsRequest(this.practice));
    }

    @Override
    public GenericRegistryObjectResponse retrieve(
            GenericRegistryObjectQueryRequest request)
                    throws HazardEventServiceException {
        return routeRequest(request);
    }

    // Private Methods

    /**
     * Route the specified request to the request server.
     * 
     * @param request
     *            Request to be routed.
     * @return Response from the request server.
     */
    private GenericRegistryObjectResponse routeRequest(HazardRequest request)
            throws HazardEventServiceException {
        try {
            Object response = RequestRouter.route(request);
            if (response instanceof GenericRegistryObjectResponse) {
                return (GenericRegistryObjectResponse) response;
            } else {
                throw new HazardEventServiceException(
                        "Received incorrect response type. Expected instance of ["
                                + GenericRegistryObjectResponse.class
                                + "] but instead received instance of ["
                                + response.getClass() + "] for request type ["
                                + request.getClass() + "]");
            }
        } catch (Exception e) {
            throw new HazardEventServiceException(
                    "Error routing request [" + request.getClass() + "]", e);
        }
    }
}
