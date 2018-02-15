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

import java.util.List;

import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.undoable.IUndoRedoable;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardSessionAlertsManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.geomaps.GeoMapUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.locks.ISessionLockManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;

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
 * Nov 23, 2015  3473      Robert.Blum  Removed importApplicationBackupSiteData.
 * Mar 03, 2016 14004      Chris.Golden Changed to pass recommender identifier to the
 *                                      method handling recommender results.
 * Mar 04, 2016 15933      Chris.Golden Added ability to run multiple recommenders in
 *                                      sequence in response to a time interval trigger,
 *                                      instead of just one recommender.
 * Apr 06, 2016  8837      Robert.Blum  Added setupEventIdDisplay().
 * Jun 23, 2016 19537      Chris.Golden Added use of spatial context provider.
 * Jul 27, 2016 19924      Chris.Golden Added use of display resource context provider.
 * Dec 12, 2016 21504      Robert.Blum  Added method to retrieve the new lock manager.
 * Dec 14, 2016 22119      Kevin.Bisanz Add flags to export config, ProductText, and
 *                                      ProductData individually.
 * Jan 04, 2017 26746      Chris.Golden Made geo map utilities a component of this class.
 * Jan 12, 2017 28034      mduff        Added getSiteId convenience method.
 * Feb 01, 2017 15556      Chris.Golden Added selection manager.
 * Feb 16, 2017 28708      mduff        Removed setupEventIdDisplay.
 * Feb 21, 2017 29138      Chris.Golden Added method to get runnable asynchronous
 *                                      scheduler.
 * Apr 13, 2017 33142      Chris.Golden Added tracking of events that have been removed
 *                                      since last recommender execution commencement, so
 *                                      that when events are returned by recommenders, any
 *                                      that have been removed while the recommender was
 *                                      executing can be ignored.
 * May 31, 2017 34684      Chris.Golden Moved recommender-specific methods to the session
 *                                      recommender manager where they belong.
 * Sep 27, 2017 38072      Chris.Golden Removed getter for the event bus, as it should not
 *                                      be publicly accessible; and added methods to start
 *                                      and stop batching of messages.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable session
 *                                      events.
 * Feb 13, 2018 44514      Chris.Golden Removed event-modifying script code, as such
 *                                      scripts are not to be used.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionManager<S extends ISettings> extends IUndoRedoable {

    /**
     * Get the runnable asynchronous scheduler used to enqueue tasks on the
     * session manager thread.
     * 
     * @return Runnable asynchronous scheduler.
     */
    public IRunnableAsynchronousScheduler getRunnableAsynchronousScheduler();

    /**
     * Get a manager for interacting with the events
     * 
     * @return
     */
    public ISessionEventManager getEventManager();

    /**
     * Get a manager for interacting with the selection set.
     * 
     * @return
     */
    public ISessionSelectionManager getSelectionManager();

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
     * Get a manager for interacting with the locks.
     * 
     * @return
     */
    public ISessionLockManager getLockManager();

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
     * Get the spatial context provider.
     * 
     * @return Spatial context provider.
     */
    public ISpatialContextProvider getSpatialContextProvider();

    /**
     * Get the display resource context provider.
     * 
     * #return Display resource context provider.
     */
    public IDisplayResourceContextProvider getDisplayResourceContextProvider();

    /**
     * Get the temporal frame context provider.
     * 
     * @return Temporal frame context provider.
     */
    public IFrameContextProvider getFrameContextProvider();

    /**
     * Get the geographic map utilities.
     * 
     * @return Geographic map utilities.
     */
    public GeoMapUtilities getGeoMapUtilities();

    /**
     * 
     * Shutdown activities such as spawned {@link Job}s
     */
    public void shutdown();

    /**
     * Mark the beginning of a batch of changes to the session.
     * <p>
     * <strong>Note</strong>: For every invocation of this method, an invocation
     * of {@link #finishBatchedChanges()} must be made as well. At any point at
     * which this method has been invoked more than the latter method, any
     * notifications normally sent out asynchronously about the changes are
     * held, and not released until the number of invocations of the two methods
     * is equal again.
     * </p>
     */
    public void startBatchedChanges();

    /**
     * Finish accumulating a batch of changes to the session, sending out any
     * asynchronous notifications that have accumulated since the batch started
     * if this method has been invoked an equal number of times as has
     * {@link #startBatchedChanges()}.
     */
    public void finishBatchedChanges();

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
     * Run the specified tools.
     * 
     * @param type
     *            Type of the tools to be run.
     * @param identifiers
     *            Identifiers of the tool to be run, ordered in the sequence
     *            they are to be executed.
     * @param context
     *            Context for the execution, if any.
     */
    public void runTools(ToolType type, List<String> identifiers,
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
     * @param siteId
     *            Site Identifier to Export
     * @param exportConfig
     *            Flag to export config info
     * @param exportProductText
     *            Flag to export ProductText info
     * @param exportProductData
     *            Flag to export ProductData info
     */
    public void exportApplicationSiteData(String siteId, boolean exportConfig,
            boolean exportProductText, boolean exportProductData);

    /**
     * Receive notification that a command was invoked within the user interface
     * that may require a metadata refresh, a recommender execution, or some
     * other response.
     * 
     * TODO: Remove the <code>mutableProperties</code> parameter once event
     * modifying scripts are removed.
     * 
     * @param eventView
     *            View for the hazard event for which the command was invoked.
     * @param identifier
     *            Identifier of the command that was invoked.
     */
    public void eventCommandInvoked(IHazardEventView eventView,
            String identifier);

    /**
     * Get the site id for the session.
     * 
     * @return The Site ID
     */
    String getSiteId();
}
