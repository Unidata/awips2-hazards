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

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

/**
 * Description: The basic {@link INotificationHandler}
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
public class NotificationHandler implements INotificationHandler {

    private final HazardSessionAlertsManager hazardSessionAlertsManager;

    public NotificationHandler(
            HazardSessionAlertsManager hazardSessionAlertsManager) {
        this.hazardSessionAlertsManager = hazardSessionAlertsManager;
    }

    @Override
    public void start() {
        NotificationManagerJob.addObserver(HazardNotification.HAZARD_TOPIC,
                hazardSessionAlertsManager);
    }

    @Override
    public void stop() {
        NotificationManagerJob.removeObserver(HazardNotification.HAZARD_TOPIC,
                hazardSessionAlertsManager);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
