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
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.SessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.impl.SessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.impl.SessionTimeManager;
import com.raytheon.uf.viz.recommenders.CAVERecommenderEngine;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionManager implements ISessionManager {

    private final EventBus eventBus;

    private final SessionEventManager eventManager;

    private final SessionTimeManager timeManager;

    private final SessionConfigurationManager configManager;

    private final SessionProductManager productManager;

    private final AbstractRecommenderEngine<?> recommenderEngine;


    public SessionManager(IPathManager pathManager,
            IHazardEventManager hazardEventManager) {
        // TODO switch the bus to async
        // bus = new AsyncEventBus(Executors.newSingleThreadExecutor());
        eventBus = new EventBus();
        SessionNotificationSender sender = new SessionNotificationSender(
                eventBus);
        timeManager = new SessionTimeManager(sender);
        configManager = new SessionConfigurationManager(pathManager, sender);
        eventManager = new SessionEventManager(timeManager, configManager, hazardEventManager,
                sender);
        productManager = new SessionProductManager(timeManager, configManager, eventManager, sender);
        recommenderEngine = new CAVERecommenderEngine();
        eventBus.register(timeManager);
        eventBus.register(configManager);
        eventBus.register(eventManager);
        eventBus.register(productManager);
        eventBus.register(recommenderEngine);
    }

    @Override
    public SessionEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public SessionTimeManager getTimeManager() {
        return timeManager;
    }

    @Override
    public SessionConfigurationManager getConfigurationManager() {
        return configManager;
    }

    @Override
    public ISessionProductManager getProductManager() {
        return productManager;
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

}
