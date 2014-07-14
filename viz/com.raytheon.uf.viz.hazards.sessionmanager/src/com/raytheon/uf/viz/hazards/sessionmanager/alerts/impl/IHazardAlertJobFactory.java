/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl;

import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;

/**
 * Description: A {@link IHazardAlertJob} factory
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- -- --------- --------------------------
 * Jul 26, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public interface IHazardAlertJobFactory {

    /**
     * Create a {@link IHazardAlertJob} containing the given
     * {@link IHazardAlert} A @ link HazardSessionAlertsManager} is included so
     * it can be notified when this runs that it is time to activate the alert.
     */
    IHazardAlertJob createJob(HazardSessionAlertsManager alertsManager,
            IHazardAlert hazardAlert);

}
