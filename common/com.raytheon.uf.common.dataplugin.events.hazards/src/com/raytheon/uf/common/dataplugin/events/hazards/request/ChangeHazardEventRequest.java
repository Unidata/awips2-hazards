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

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Description: Base class for changing hazard events in the registry.
 * <p>
 * Note that the more elegant solution of using a common base class with a
 * generic parameter for the object type in place of this,
 * {@link ChangeGenericRegistryObjectRequest}, and
 * {@link ChangeHazardEventVtecRequest} is not possible due to JAXB limitations.
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
public abstract class ChangeHazardEventRequest extends HazardRequest {

    // Private Variables

    /**
     * Events to be changed.
     */
    @DynamicSerializeElement
    private List<HazardEvent> events;

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public ChangeHazardEventRequest() {

        /*
         * No action.
         */
    }

    // Public Methods

    /**
     * Construct a standard instance.
     * 
     * @param events
     *            Events to be changed.
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public ChangeHazardEventRequest(List<HazardEvent> events,
            boolean practice) {
        super(practice);
        this.events = events;
    }

    // Public Methods

    /**
     * Get the events.
     * 
     * @return Events.
     */
    public List<HazardEvent> getEvents() {
        return events;
    }

    /**
     * Set the events.
     * 
     * @param events
     *            Events.
     */
    public void setEvents(List<HazardEvent> events) {
        this.events = events;
    }
}
