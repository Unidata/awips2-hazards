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

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;

/**
 * Interface describing the methods that must be implemented by classes that are
 * to represent hazard events existing within the context of a Hazard Services
 * client session.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 8, 2018    15561    Chris.Golden Initial creation.
 *
 * </pre>
 *
 * @author golden
 */
public interface ISessionHazardEvent extends IHazardEvent {

    /**
     * Get the hazard status as it was prior to expiration. This will return
     * whatever status the hazard event had prior to be being expired, if it has
     * been expired; if it is not expired, it will return the same value as does
     * {@link #getStatus()}. It will never return {@link HazardStatus#ELAPSING})
     * or {@link HazardStatus#ELAPSED}, as these are expiration statuses.
     * 
     * @return Hazard status prior to expiration, if it is being, or is already,
     *         expired, or the current hazard status if it is not expiring or
     *         expired.
     */
    public HazardStatus getPreExpiredStatus();
}
