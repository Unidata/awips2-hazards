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

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Description: Base class for changing hazard event VTEC records in the
 * registry.
 * <p>
 * Note that the more elegant solution of using a common base class with a
 * generic parameter for the object type in place of this,
 * {@link ChangeHazardEventRequest}, and
 * {@link ChangeGenericRegistryObjectRequest} is not possible due to JAXB
 * limitations.
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
public abstract class ChangeHazardEventVtecRequest extends HazardRequest {

    // Private Variables

    /**
     * VTEC records to be changed.
     */
    @DynamicSerializeElement
    private List<Object> vtecRecords;

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public ChangeHazardEventVtecRequest() {

        /*
         * No action.
         */
    }

    /**
     * Construct a standard instance.
     * 
     * @param vtecRecords
     *            VTEC records to be changed.
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public ChangeHazardEventVtecRequest(List<Object> vtecRecords,
            boolean practice) {
        super(practice);
        this.vtecRecords = vtecRecords;
    }

    // Public Methods

    /**
     * Get the VTEC records.
     * 
     * @return VTEC records.
     */
    public List<Object> getVtecList() {
        return vtecRecords;
    }

    /**
     * Set the VTEC records.
     * 
     * @param vtecRecords
     *            VTEC records.
     */
    public void setVtecList(List<Object> vtecRecords) {
        this.vtecRecords = vtecRecords;
    }
}
