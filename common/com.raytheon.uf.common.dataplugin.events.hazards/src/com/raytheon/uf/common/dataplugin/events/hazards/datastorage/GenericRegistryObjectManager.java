/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.datastorage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.GenericRegistryObjectResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IGenericRegistryObjectServices;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.GenericRegistryObjectRequestServices;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GenericRegistryObjectQueryRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Description: Manager of generic objects, allowing the latter to be persisted,
 * updated, deleted, and queried. All access to the registry/database for
 * registry objects will happen through here.
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
public class GenericRegistryObjectManager implements IGenericRegistryObjectManager {

    // Private Static Constants

    /**
     * Logger,
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GenericRegistryObjectManager.class);

    // Private Variables

    /**
     * Current mode.
     */
    private final boolean practice;

    /**
     * Data access services.
     */
    private final IGenericRegistryObjectServices genericObjectDataAccess;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public GenericRegistryObjectManager(boolean practice) {
        this.practice = practice;
        this.genericObjectDataAccess = GenericRegistryObjectRequestServices
                .getServices(practice);
    }

    // Public Methods

    @Override
    public Map<String, GenericRegistryObject> query(
            GenericRegistryObjectQueryRequest request)
                    throws HazardEventServiceException {
        Map<String, GenericRegistryObject> genericObjects = Collections
                .emptyMap();
        GenericRegistryObjectResponse response = genericObjectDataAccess
                .retrieve(request);
        if (response.success()) {
            genericObjects = response.getMap();
        } else {
            checkResponse(response);
        }
        return genericObjects;
    }

    @Override
    public Map<String, GenericRegistryObject> getAll() {
        try {
            return query(new GenericRegistryObjectQueryRequest(practice));
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error requesting all generic objects.", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean store(GenericRegistryObject... genericObjects) {
        return store(Arrays.asList(genericObjects));
    }

    @Override
    public boolean store(List<GenericRegistryObject> genericObjects) {
        try {
            return checkResponse(
                    genericObjectDataAccess.storeList(genericObjects));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean update(GenericRegistryObject... genericObjects) {
        return update(Arrays.asList(genericObjects));
    }

    @Override
    public boolean update(List<GenericRegistryObject> genericObjects) {
        try {
            return checkResponse(
                    genericObjectDataAccess.updateList(genericObjects));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean remove(GenericRegistryObject... genericObjects) {
        return remove(Arrays.asList(genericObjects));
    }

    @Override
    public boolean remove(List<GenericRegistryObject> genericObjects) {
        try {
            return checkResponse(
                    genericObjectDataAccess.deleteList(genericObjects));
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean removeAll() {
        try {
            return checkResponse(genericObjectDataAccess.deleteAll());
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return false;
    }

    // Private Methods

    /**
     * Check the specified response from the web server and log any errors.
     * 
     * @param response
     *            Response to be checked.
     * @return <code>true</code> if the response indicates success,
     *         <code>false</code> otherwise.
     */
    private boolean checkResponse(GenericRegistryObjectResponse response) {
        if (!response.success()) {
            for (Throwable t : response.getExceptions()) {
                statusHandler.error(
                        "Registry web service call encountered an error", t);
            }
        }
        return response.success();
    }
}
