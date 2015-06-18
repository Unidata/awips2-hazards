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

import java.util.Date;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: An {@link IHazardAlert} based on expiration time of a
 * {@link IHazardEvent}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 09, 2013  1325          daniel.s.schaffer@noaa.gov      Initial creation
 * Jun 18, 2015  7307      Chris.Cody  Added Hazard End time for requested Time Remaining calculation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public interface IHazardEventExpirationAlert extends IHazardAlert {

    public Date getHazardExpiration();

    public void setHazardExpiration(Date hazardExpiration);

    public Date getHazardEnd();

    public void setHazardEnd(Date hazardEnd);

    public Long getMillisBeforeExpiration();

}
