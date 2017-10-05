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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Description: Request object for deleting all generic registry objects.
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
public class DeleteAllGenericRegistryObjectsRequest extends HazardRequest {

    // Public Constructors

    /**
     * Construct a standard instance for operational mode.
     */
    public DeleteAllGenericRegistryObjectsRequest() {
    }

    /**
     * Construct a standard instance.
     * 
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public DeleteAllGenericRegistryObjectsRequest(boolean practice) {
        super(practice);
    }
}
