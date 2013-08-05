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

import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;

/**
 * Description: A unit of runnable {@link IHazardAlertJob}s. This abstraction
 * enables bypassing of actual {@link Job}s during unit testing because their
 * asynchronous nature causes race conditions during the tests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 26, 2013  1325      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public interface IHazardAlertJob {

    /**
     * Schedule this job with the given delay.
     */
    void schedule(long delayInMillis);

    /**
     * Cancel this job
     */
    boolean cancel();

    /**
     * 
     * @return the composed {@link IHazardAlert}
     */
    IHazardAlert getHazardAlert();
}
