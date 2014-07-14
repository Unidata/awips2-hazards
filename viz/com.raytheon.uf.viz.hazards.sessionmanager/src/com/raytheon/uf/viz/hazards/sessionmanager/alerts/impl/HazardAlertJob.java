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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;

/**
 * Description: A {@link Job} for hazard alerts
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
public class HazardAlertJob extends Job implements IHazardAlertJob {

    private final IHazardSessionAlertsManager alertsManager;

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardAlertJob.class);

    private final IHazardAlert hazardAlert;

    public HazardAlertJob(IHazardSessionAlertsManager alertsManager,
            IHazardAlert hazardAlert) {
        super("Hazard Alert Job");
        this.alertsManager = alertsManager;
        this.hazardAlert = hazardAlert;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        statusHandler.debug("activating alert, activating time is "
                + hazardAlert.getActivationTime());
        alertsManager.activateAlert(this);
        return Status.OK_STATUS;
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
