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

import java.util.List;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;

/**
 * Description: A {@link ISessionNotification} indicating that the state of
 * hazard alerts has changed and making available the active alerts.
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
public class HazardAlertsModified implements ISessionNotification {

    private final List<IHazardAlert> activeAlerts;

    public HazardAlertsModified(List<IHazardAlert> activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    public List<IHazardAlert> getActiveAlerts() {
        return activeAlerts;
    }

}
