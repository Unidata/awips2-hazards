/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.event;

/**
 * Interface describing the methods that must be implemented by classes intended
 * to act as views of a hazard event. Views allow read-only access to their
 * associated hazard events. The latter may change during the lifetime of its
 * view, in which case the view will show updated values if it is queried for
 * properties that have changed.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 11, 2017   20739    Chris.Golden Initial creation.
 * </pre>
 *
 * @author golden
 */
public interface IHazardEventView extends IReadableHazardEvent {
}
