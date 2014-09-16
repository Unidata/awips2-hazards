/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config;

import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Encapsulation of a modified hazard event and any associated
 * JSON-encoded extra data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 16, 2014    4753    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ModifiedHazardEvent {

    // Private Variables

    /**
     * Hazard event that has been modified.
     */
    private final IHazardEvent hazardEvent;

    /**
     * Metadata megawidgets' mutable properties that have been modified.
     */
    private final Map<String, Map<String, Object>> mutableProperties;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param hazardEvent
     *            Hazard event that has been modified.
     * @param mutableProperties
     *            Metadata megawidgets' mutable properties that have been
     *            modified.
     */
    public ModifiedHazardEvent(IHazardEvent hazardEvent,
            Map<String, Map<String, Object>> mutableProperties) {
        this.hazardEvent = hazardEvent;
        this.mutableProperties = mutableProperties;
    }

    // Public Methods

    /**
     * Get the hazard event that was modified.
     * 
     * @return Hazard event that was modified.
     */
    public IHazardEvent getHazardEvent() {
        return hazardEvent;
    }

    /**
     * Get the metadata megawidgets' mutable properties that have been modified.
     * 
     * @return Metadata megawidgets' mutable properties that have been modified.
     */
    public Map<String, Map<String, Object>> getMutableProperties() {
        return mutableProperties;
    }
}
