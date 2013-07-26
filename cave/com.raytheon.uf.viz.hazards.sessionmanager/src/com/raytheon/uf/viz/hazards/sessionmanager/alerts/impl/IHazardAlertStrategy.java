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

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;

/**
 * Description: A strategy for deciding what alerts are needed, constructing
 * them and determining when they should be scheduled or canceled.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013   1325     daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public interface IHazardAlertStrategy {

    /**
     * Used to create any {@link IHazardAlert}s needed when Hazard Services
     * starts.
     */
    void initializeAlerts();

    /**
     * Update existing alerts due to an external event such as the issuance of
     * cancellation of a {@link IHazardEvent}
     */
    void updateAlerts(Object notification);

    /**
     * The {@link IHazardSessionAlertsManager} is required to call back to
     * schedule or cancel {@link IHazardAlert}s
     */
    IHazardSessionAlertsManager getAlertsManager();

}
