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

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;

/**
 * Description: A {@link IHazardAlertJob} for testing that runs synchronously so
 * as to avoid race conditions in unit tests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 26, 2013            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardAlertJobForTesting implements IHazardAlertJob {

    private final IHazardSessionAlertsManager alertsManager;

    private final IHazardAlert hazardAlert;

    public HazardAlertJobForTesting(IHazardSessionAlertsManager alertsManager,
            IHazardAlert hazardAlert) {
        this.alertsManager = alertsManager;
        this.hazardAlert = hazardAlert;
    }

    /**
     * For testing, we'll only check if the alert is scheduled if it is to run
     * in the future. We can't have a unit test wait for any delays
     */
    @Override
    public void schedule(long delay) {
        if (delay == 0) {
            alertsManager.activateAlert(this);
        }

    }

    @Override
    public boolean cancel() {
        /**
         * Nothing to do right now.
         */
        return true;
    }

    @Override
    public IHazardAlert getHazardAlert() {
        return hazardAlert;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
