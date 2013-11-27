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
package com.raytheon.uf.viz.hazards.sessionmanager.impl;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.AllHazardsFilterStrategy;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.HazardEventExpirationAlertStrategy;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.HazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.SessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.impl.SessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.impl.SessionTimeManager;
import com.raytheon.uf.viz.recommenders.CAVERecommenderEngine;
import com.raytheon.uf.viz.recommenders.interactive.InteractiveRecommenderEngine;

/**
 * Implementation of ISessionManager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013 1257       bsteffen    Initial creation
 * Aug 01, 2013  1325      daniel.s.schaffer@noaa.gov     Added support for alerting
<<<<<<< Updated upstream
 * Nov 19, 2013 1463       blawrenc    Added state of automatic hazard conflict
 *                                     testing.
=======
 *
 * Nov 20, 2013   2159     daniel.s.schaffer@noaa.gov Now alerts interoperable with DRT
 *
>>>>>>> Stashed changes
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionManager implements ISessionManager {

    private final EventBus eventBus;

    private final ISessionEventManager eventManager;

    private final ISessionTimeManager timeManager;

    private final ISessionConfigurationManager configManager;

    private final ISessionProductManager productManager;

    private final AbstractRecommenderEngine<?> recommenderEngine;

    private final IHazardSessionAlertsManager alertsManager;

    /*
     * Flag indicating whether or not automatic hazard checking is running.
     */
    private boolean autoHazardChecking = false;

    public SessionManager(IPathManager pathManager,
            IHazardEventManager hazardEventManager) {
        // TODO switch the bus to async
        // bus = new AsyncEventBus(Executors.newSingleThreadExecutor());
        eventBus = new EventBus();
        SessionNotificationSender sender = new SessionNotificationSender(
                eventBus);
        timeManager = new SessionTimeManager(sender);
        configManager = new SessionConfigurationManager(pathManager, sender);
        eventManager = new SessionEventManager(timeManager, configManager,
                hazardEventManager, sender);
        productManager = new SessionProductManager(timeManager, configManager,
                eventManager, sender);
        alertsManager = new HazardSessionAlertsManager(sender, timeManager);
        alertsManager.addAlertGenerationStrategy(HazardNotification.class,
                new HazardEventExpirationAlertStrategy(alertsManager,
                        timeManager, configManager, hazardEventManager,
                        new AllHazardsFilterStrategy()));

        /**
         * TODO Where should a call be made to remove the NotificationJob
         * observer (done in the stop method)?
         */
        alertsManager.start();
        recommenderEngine = new CAVERecommenderEngine();
        recommenderEngine.injectEngine(new InteractiveRecommenderEngine());
        eventBus.register(timeManager);
        eventBus.register(configManager);
        eventBus.register(eventManager);
        eventBus.register(productManager);
        eventBus.register(recommenderEngine);
        eventBus.register(alertsManager);

    }

    @Override
    public ISessionEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public ISessionTimeManager getTimeManager() {
        return timeManager;
    }

    @Override
    public ISessionConfigurationManager getConfigurationManager() {
        return configManager;
    }

    @Override
    public ISessionProductManager getProductManager() {
        return productManager;
    }

    @Override
    public IHazardSessionAlertsManager getAlertsManager() {
        return alertsManager;
    }

    @Override
    public AbstractRecommenderEngine<?> getRecommenderEngine() {
        return recommenderEngine;
    }

    @Override
    public void registerForNotification(Object object) {
        eventBus.register(object);
    }

    @Override
    public void unregisterForNotification(Object object) {
        eventBus.unregister(object);
    }

    @Override
    public void shutdown() {

        eventManager.shutdown();

        timeManager.shutdown();

        configManager.shutdown();

        productManager.shutdown();

        alertsManager.shutdown();
        recommenderEngine.shutdownEngine();
    }

    @Override
    public void toggleAutoHazardChecking() {
        autoHazardChecking = !autoHazardChecking;

        /*
         * Force a refresh of the Hazard Services views. There is probably a
         * better way to do this.
         */
        eventManager.setSelectedEvents(eventManager.getSelectedEvents());
    }

    @Override
    public boolean isAutoHazardCheckingOn() {
        return autoHazardChecking;
    }

}
