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

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GenericRegistryObjectQueryRequest;

/**
 * Description: Interface describing the methods that must be implemented by a
 * generic registry object manager, used to store and retrieve
 * {@link GenericRegistryObject} instances.
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
public interface IGenericRegistryObjectManager {

    /**
     * Execute the specified query of the registry for generic objects.
     * 
     * @param request
     *            Query request to be executed.
     * @return Map of generic object names to their objects.
     * @throws HazardEventServiceException
     *             If a problem occurs when attempting the query.
     */
    Map<String, GenericRegistryObject> query(
            GenericRegistryObjectQueryRequest request)
                    throws HazardEventServiceException;

    /**
     * Retrieve all generic objects.
     * <p>
     * <strong>Note</strong>: This should be used cautiously, as there may be a
     * large number of generic objects returned.
     * </p>
     * 
     * @return Map of generic object names to their objects.
     */
    Map<String, GenericRegistryObject> getAll();

    /**
     * Store the specified generic object(s).
     * 
     * @param genericObjects
     *            Generic object(s) to be stored.
     * @return <code>true</code> if the storage of the generic object(s) was
     *         successful, <code>false</code> otherwise.
     */
    public boolean store(GenericRegistryObject... genericObjects);

    /**
     * Store the specified generic objects.
     * 
     * @param genericObjects
     *            Generic objects to be stored.
     * @return <code>true</code> if the storage of the generic objects was
     *         successful, <code>false</code> otherwise.
     */
    public boolean store(List<GenericRegistryObject> genericObjects);

    /**
     * Update the specified generic object(s).
     * 
     * @param genericObjects
     *            Generic object(s) to be updated.
     * @return <code>true</code> if the update of the generic object(s) was
     *         successful, <code>false</code> otherwise.
     */
    public boolean update(GenericRegistryObject... genericObjects);

    /**
     * Update the specified generic objects.
     * 
     * @param genericObjects
     *            Generic objects to be updated.
     * @return <code>true</code> if the update of the generic objects was
     *         successful, <code>false</code> otherwise.
     */
    public boolean update(List<GenericRegistryObject> genericObjects);

    /**
     * Remove the specified generic object(s).
     *
     * @param genericObjects
     *            Generic object(s) to be removed.
     * @return <code>true</code> if the removal of the generic object(s) was
     *         successful, <code>false</code> otherwise.
     */
    public boolean remove(GenericRegistryObject... genericObjects);

    /**
     * Remove the specified generic objects.
     * 
     * @param genericObjects
     *            Generic objects to be removed.
     * @return <code>true</code> if the removal of the generic objects was
     *         successful, <code>false</code> otherwise.
     */
    public boolean remove(List<GenericRegistryObject> genericObjects);

    /**
     * Remove all generic objects.
     * 
     * @return <code>true</code> if the generic objects were removed,
     *         <code>false</code> otherwise.
     */
    boolean removeAll();
}
