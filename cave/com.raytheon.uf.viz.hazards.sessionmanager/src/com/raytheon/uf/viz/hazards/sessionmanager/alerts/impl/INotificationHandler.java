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

import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

/**
 * Description: Abstraction that allows unit tests to avoid having to start and
 * stop subscriptions in {@link NotificationManagerJob}
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
public interface INotificationHandler {

    /**
     * Handle any startup tasks
     */
    void start();

    /**
     * Handle any shutdown tasks
     */
    void stop();

}
