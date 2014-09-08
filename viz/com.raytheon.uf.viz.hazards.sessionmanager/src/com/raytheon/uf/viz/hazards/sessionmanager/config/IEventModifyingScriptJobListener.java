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

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Interface describing the methods that must be implemented in
 * order to listen for the result of an event modifying script execution.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 20, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IEventModifyingScriptJobListener {

    // Public Methods

    /**
     * Receive notification that an event modifying script completed
     * successfully.
     * 
     * @param identifier
     *            Identifier of the event modifying script that was run.
     * @param hazardEvent
     *            Hazard event returned by the event modifying script; if
     *            <code>null</code> none was returned, meaning no modification
     *            is required.
     */
    public void scriptExecutionComplete(String identifier,
            IHazardEvent hazardEvent);
}
