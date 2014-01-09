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

import java.util.Collection;
import java.util.Iterator;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationOpFailedException;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.AllHazardsFilterStrategy;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.HazardEventExpirationAlertStrategy;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.HazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.SessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.impl.SessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.impl.SessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
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
 * Nov 19, 2013 1463       blawrenc    Added state of automatic hazard conflict
 *                                     testing.
 * 
 * Nov 20, 2013   2159     daniel.s.schaffer@noaa.gov Now alerts interoperable with DRT
 * 
 * Nov 23, 2013 1462       blawrenc    Added state of hatch area drawing.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionManager implements ISessionManager {

    /**
     * Files in localization to be removed when the events are reset from the
     * Console. These are VTEC-related. If VTEC information is allowed to
     * persist after events are deleted, VTEC processing could be compromised
     * for future events. These are assumed to be CAVE_STATIC files.
     * 
     * TODO This need to be eliminated when we go to a database solution.
     */
    private static final String[] filesToDeleteOnReset = {
            "gfe/userPython/testVtecRecords_local.json",
            "gfe/userPython/vtecRecords.json",
            "gfe/userPython/vtecRecords.lock" };

    /**
     * Logging mechanism.
     */
    private final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(this.getClass());

    private final EventBus eventBus;

    private final ISessionEventManager eventManager;

    private final ISessionTimeManager timeManager;

    private final ISessionConfigurationManager configManager;

    private final ISessionProductManager productManager;

    private final AbstractRecommenderEngine<?> recommenderEngine;

    private final IHazardSessionAlertsManager alertsManager;

    private final IHazardEventManager hazardManager;

    /*
     * Flag indicating whether or not automatic hazard checking is running.
     */
    private boolean autoHazardChecking = false;

    /*
     * Flag indicating whether or not hazard hatch areas are displayed.
     */
    private boolean hatchAreaDisplay = false;

    /*
     * Messenger for displaying questions and warnings to the user and
     * retrieving answers. This allows the viz side (App Builder) to be
     * responsible for these dialogs, but gives the session manager and other
     * managers access to them without creating a dependency on the
     * gov.noaa.gsd.viz.hazards plugin. Since all parts of Hazard Services can
     * use the same code for creating these dialogs, it makes it easier for them
     * to be stubbed for testing.
     */

    public SessionManager(IPathManager pathManager,
            IHazardEventManager hazardEventManager, IMessenger messenger) {
        // TODO switch the bus to async
        // bus = new AsyncEventBus(Executors.newSingleThreadExecutor());
        eventBus = new EventBus();
        SessionNotificationSender sender = new SessionNotificationSender(
                eventBus);
        timeManager = new SessionTimeManager(sender);
        configManager = new SessionConfigurationManager(pathManager, sender);
        eventManager = new SessionEventManager(timeManager, configManager,
                hazardEventManager, sender, messenger);
        productManager = new SessionProductManager(timeManager, configManager,
                eventManager, sender, messenger);
        alertsManager = new HazardSessionAlertsManager(sender, timeManager);
        alertsManager.addAlertGenerationStrategy(HazardNotification.class,
                new HazardEventExpirationAlertStrategy(alertsManager,
                        timeManager, configManager, hazardEventManager,
                        new AllHazardsFilterStrategy()));
        hazardManager = hazardEventManager;

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

    @Override
    public void toggleHatchedAreaDisplay() {
        hatchAreaDisplay = !hatchAreaDisplay;

        ISessionEventManager eventManager = getEventManager();

        /*
         * Force a refresh of the Hazard Services views. There is probably a
         * better way to do this.
         */
        eventManager.setSelectedEvents(eventManager.getSelectedEvents());

    }

    @Override
    public boolean areHatchedAreasDisplayed() {
        return hatchAreaDisplay;
    }

    @Override
    public void reset() {

        for (IHazardEvent event : eventManager.getEvents()) {
            eventManager.removeEvent(event);
        }

        hazardManager.removeAllEvents();

        /*
         * Reset the VTEC information in the VTEC files. This needs to be done.
         * Otherwise, it is difficult to test against the job sheets and
         * functional tests when the forecaster selects Reset->Events but old
         * VTEC information remains in the VTEC files. This solution will change
         * once VTEC is stored in the database.
         */
        IPathManager pathManager = PathManagerFactory.getPathManager();
        LocalizationContext localizationContext = pathManager.getContext(
                LocalizationContext.LocalizationType.CAVE_STATIC,
                LocalizationContext.LocalizationLevel.USER);

        for (String fileToDelete : filesToDeleteOnReset) {
            LocalizationFile localizationFile = pathManager
                    .getLocalizationFile(localizationContext, fileToDelete);

            if (localizationFile.exists()) {
                try {
                    localizationFile.delete();
                } catch (LocalizationOpFailedException e) {
                    statusHandler.error("Error while reseting.", e);
                }
            }
        }
    }

    @Override
    public void undo() {
        Collection<IHazardEvent> events = eventManager.getSelectedEvents();

        if (events.size() == 1) {
            Iterator<IHazardEvent> eventIter = events.iterator();
            ObservedHazardEvent obsEvent = (ObservedHazardEvent) eventIter
                    .next();
            obsEvent.undo();
        }

    }

    @Override
    public void redo() {
        Collection<IHazardEvent> events = eventManager.getSelectedEvents();

        /*
         * Limited to single selected hazard events.
         */
        if (events.size() == 1) {
            Iterator<IHazardEvent> eventIter = events.iterator();
            ObservedHazardEvent obsEvent = (ObservedHazardEvent) eventIter
                    .next();
            obsEvent.redo();
        }

    }

    @Override
    public Boolean isUndoable() {
        Collection<IHazardEvent> hazardEvents = eventManager
                .getSelectedEvents();

        /*
         * Limited to single selected hazard events.
         */
        if (hazardEvents.size() == 1) {
            Iterator<IHazardEvent> iterator = hazardEvents.iterator();
            return ((IUndoRedoable) iterator.next()).isUndoable();
        }

        return false;
    }

    @Override
    public Boolean isRedoable() {

        Collection<IHazardEvent> hazardEvents = eventManager
                .getSelectedEvents();

        /*
         * Limit to single selection.
         */
        if (hazardEvents.size() == 1) {
            Iterator<IHazardEvent> iterator = hazardEvents.iterator();
            return ((IUndoRedoable) iterator.next()).isRedoable();
        }

        return false;
    }

    @Override
    public void handleRecommenderResult(EventSet<IEvent> eventList) {
        for (IEvent event : eventList) {
            if (event instanceof IHazardEvent) {
                IHazardEvent hevent = (IHazardEvent) event;
                hevent = eventManager.addEvent(hevent);
            }
        }
    }

    @Override
    public void clearUndoRedo() {
        throw new UnsupportedOperationException();
    }

}
