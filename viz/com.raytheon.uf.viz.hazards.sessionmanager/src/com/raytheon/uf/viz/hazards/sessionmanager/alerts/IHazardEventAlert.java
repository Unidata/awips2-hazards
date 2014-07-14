/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: A {@link IHazardAlert} that is triggered when a
 * {@link IHazardEvent} expiration time approaches.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013   1325    daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public interface IHazardEventAlert extends IHazardAlert {

    /**
     * @return the event ID associated with this {@link IHazardEvent} with which
     *         this is associated.
     */
    String getEventID();

}
