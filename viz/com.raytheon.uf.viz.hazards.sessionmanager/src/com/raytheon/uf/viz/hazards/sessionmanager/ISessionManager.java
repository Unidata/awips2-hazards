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

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;

import java.util.List;

import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;

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
 * May 20, 2013  1257      bsteffen    Initial creation
 * Aug 01, 2013  1325      daniel.s.schaffer@noaa.gov     Added support for alerting
 * Nov 19, 2013  1463      blawrenc    Added state of automatic hazard conflict testing.
 * Nov 23, 2013  1462      blawrenc    Added state of hatched area drawing
 * Oct 08, 2014  4042      Chris.Golden Added generate method (moved from message handler).
 * Dec 05, 2014  4124      Chris.Golden Changed to work with parameterized config manager.
 * Sep 14, 2015  3473      Chris.Cody   Implement Hazard Services Import/Export through
 *                                      Central Registry server.
 * Nov 10, 2015 12762      Chris.Golden Added code to implement and use new recommender
 *                                      manager.
 * Mar 03, 2016 14004      Chris.Golden Changed to pass recommender identifier to the
 *                                      method handling recommender results.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionManager<E extends IHazardEvent, S extends ISettings>
        extends IUndoRedoable {

    /**
     * Get a manager for interacting with the events
     * 
     * @return
     */
    public ISessionEventManager<E> getEventManager();

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
    public ISessionConfigurationManager<S> getConfigurationManager();

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
     * Get a manager for handling recommenders.
     * 
     * @return
     */
    public ISessionRecommenderManager getRecommenderManager();

    /**
     * Get the temporal frame context provider.
     * 
     * @return Temporal frame context provider.
     */
    public IFrameContextProvider getFrameContextProvider();

    /**
     * Register an object as to receive ISessionNotifiation events for this
     * session. The object passed in should use the
     * {@link BoundedReceptionEventBus} <code>{@literal @}Handler</code>
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

    /**
     * Resets any practice events and VTEC information
     */
    void reset();

    /**
     * Turns on/off the display of the hatched areas associated with hazards.
     */
    public void toggleHatchedAreaDisplay();

    /**
     * Returns the state of the display of hatched areas.
     * 
     * @return true - hatched areas are displayed. false - hatched areas are not
     *         displayed.
     */
    public boolean areHatchedAreasDisplayed();

    /**
     * Handle the result of a recommender run.
     * 
     * @param recommenderIdentifier
     *            Identifier of the recommender that ran.
     * @param events
     *            Set of events that were created or modified by the
     *            recommender.
     */
    public void handleRecommenderResult(String recommenderIdentifier,
            EventSet<IEvent> events);

    /**
     * 
     * @return True from the time the forecaster hits PREVIEW until they confirm
     *         an issue, Cancel the Product Staging Dialog or Dismiss the
     *         Product Editor
     */
    public boolean isPreviewOngoing();

    /**
     * @param isOngoing
     */
    public void setPreviewOngoing(boolean isOngoing);

    /**
     * 
     * @return True from the time the forecaster confirms they want to issue
     *         until the product issuance is complete
     */
    public boolean isIssueOngoing();

    /**
     * @param isOngoing
     */
    public void setIssueOngoing(boolean isOngoing);

    /**
     * Run the specified tool.
     * 
     * @param type
     *            Type of the tool to be run.
     * @param identifier
     *            Identifier of the tool to be run.
     * @param context
     *            Context for the tool, if any.
     */
    public void runTool(ToolType type, String identifier,
            RecommenderExecutionContext context);

    /**
     * Generate products based upon the currently selected events.
     * <p>
     * 
     * @param issue
     *            Flag indicating whether or not the generation is occurring as
     *            a result of an issue command. If false, then a preview command
     *            initiated the generation.
     */
    public void generate(boolean issue);

    /**
     * Export Hazard Services Site Configuration Files
     * 
     * Push the Hazard Services Configuration Files for this Site
     * 
     * @siteId Site Identifier to Export
     */
    public void exportApplicationSiteData(String siteId);

    /**
     * Import Hazard Services Site Configuration Files
     * 
     * Pull the Hazard Services Configuration Files for a Backup Site
     * 
     * @param backupSiteIdList
     *            List of Site Id values that will be imported as backup
     *            localization site data
     */
    public void importApplicationBackupSiteData(List<String> backupSiteIdList);

}
