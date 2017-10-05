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
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Description: Request for storing generic objects.
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
@DynamicSerialize
public class StoreGenericRegistryObjectRequest
        extends ChangeGenericRegistryObjectRequest {

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public StoreGenericRegistryObjectRequest() {

        /*
         * No action.
         */
    }

    /**
     * Construct a standard instance.
     * 
     * @param genericObjects
     *            Generic objects to store.
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public StoreGenericRegistryObjectRequest(
            List<GenericRegistryObject> genericObjects, boolean practice) {
        super(genericObjects, practice);
    }
}
