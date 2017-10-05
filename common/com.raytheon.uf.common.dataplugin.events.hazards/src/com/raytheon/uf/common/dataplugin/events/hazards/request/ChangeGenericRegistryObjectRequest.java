/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.request;

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.event.GenericRegistryObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Description: Base class for changing generic objects from the registry.
 * <p>
 * Note that the more elegant solution of using a common base class with a
 * generic parameter for the object type in place of this,
 * {@link ChangeHazardEventRequest}, and {@link ChangeHazardEventVtecRequest} is
 * not possible due to JAXB limitations.
 * </p>
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
public abstract class ChangeGenericRegistryObjectRequest extends HazardRequest {

    // Private Variables

    /**
     * Objects to be changed.
     */
    @DynamicSerializeElement
    private List<GenericRegistryObject> genericObjects;

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public ChangeGenericRegistryObjectRequest() {

        /*
         * No action.
         */
    }

    // Public Methods

    /**
     * Construct a standard instance.
     * 
     * @param objects
     *            Objects to be changed.
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public ChangeGenericRegistryObjectRequest(
            List<GenericRegistryObject> genericObjects, boolean practice) {
        super(practice);
        this.genericObjects = genericObjects;
    }

    /**
     * Get the generic objects.
     * 
     * @return Generic objects.
     */
    public List<GenericRegistryObject> getGenericObjects() {
        return genericObjects;
    }

    /**
     * Set the generic objects.
     * 
     * @param genericObjects
     *            Generic objects.
     */
    public void setGenericObjects(List<GenericRegistryObject> genericObjects) {
        this.genericObjects = genericObjects;
    }
}
