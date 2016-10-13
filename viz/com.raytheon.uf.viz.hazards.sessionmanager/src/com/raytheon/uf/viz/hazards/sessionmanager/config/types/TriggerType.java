/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

/**
 * Description: Enumeration of the types of triggers that may initiate the
 * execution of event-driven tools.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 27, 2016   18266    Chris.Golden  Initial creation.
 * Oct 05, 2016   22870    Chris.Golden  Added frame change as a trigger.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum TriggerType {
    TIME_INTERVAL, FRAME_CHANGE, DATA_LAYER_CHANGE;
}
