/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.hazards.sessionmanager;

import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

/**
 * Primary interface for maintaining the state of everything during a session of
 * user interaction. Most functionality is delegated to other managers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013 1257       bsteffen    Initial creation
 * Aug 01, 2013  1325      daniel.s.schaffer@noaa.gov     Added support for alerting
 * Nov 19, 2013  1463      blawrenc    Added state of automatic hazard conflict testing.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionManager {

    /**
     * Get a manager for interacting with the events
     * 
     * @return
     */
    public ISessionEventManager getEventManager();

    /**
     * Get a manager for interacting with the times
     * 
     * @return
     */
    public ISessionTimeManager getTimeManager();

    /**
     * Get a manager for interacting with the configuration
     * 
     * @return
     */
    public ISessionConfigurationManager getConfigurationManager();

    /**
     * Get a manager for configuring, generating, and issueing products.
     * 
     * @return
     */
    public ISessionProductManager getProductManager();

    /**
     * Get a manager for handling alerting.
     * 
     * @return
     */
    public IHazardSessionAlertsManager getAlertsManager();

    /**
     * Get the recommender engine to use for running recommenders.
     * 
     * TODO this may be moved out of session manager or into a sub manager, this
     * is a pending design decision.
     * 
     * @return
     */
    public AbstractRecommenderEngine<?> getRecommenderEngine();

    /**
     * Register an object as to receive ISessionNotifiation events for this
     * session. The object passed in should use the EventBus @Subscribe
     * annotation to specify which methods are listening for
     * ISessionNotification or its sub classes.
     * 
     * @param object
     */
    public void registerForNotification(Object object);

    /**
     * UNregister for notifications.
     * 
     * @param object
     */
    public void unregisterForNotification(Object object);

    /**
     * 
     * Shutdown activities such as spawned {@link Job}s
     */
    public void shutdown();

    /**
     * Turns on/off automatic hazard checking.
     * 
     * @param
     * @return
     */
    void toggleAutoHazardChecking();

    /**
     * Returns the state of auto hazard checking.
     * 
     * @param
     * @return true - Automatic hazard checking is on. false - Automatic hazard
     *         checking is off.
     */
    boolean isAutoHazardCheckingOn();
}
