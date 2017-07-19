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
package com.raytheon.uf.common.recommenders;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.python.concurrent.PythonInterpreterFactory;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.recommenders.executors.EntireRecommenderExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderDialogInfoExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderInventoryExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderLoaderInventoryExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderMetadataExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderSpatialInfoExecutor;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * An abstract class waiting for implementation to call the corresponding
 * interpreter class for recommender calls
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 06, 2013            mnash       Initial creation
 * Jul 12, 2013 1257       bsteffen    Convert recommender dialog info to use
 *                                     Serializeables for values instead of
 *                                     Strings.
 * Apr 14, 2014 3422       bkowal      Created a second getInventory method
 *                                     (primarily for testing purposes) that can
 *                                     can be used to ensure that a file has actually
 *                                     been loaded before the inventory is retrieved.
 * Aug 18, 2014 4243       Chris.Golden Changed getInventory(recommenderName) to only
 *                                      return a single recommender.
 * Jan 29, 2015 3626       Chris.Golden Added EventSet to arguments for getting dialog
 *                                      info.
 * Mar 31, 2016  8837      Robert.Blum  Changes for Service Backup.
 * Jun 23, 2016 19537      Chris.Golden Changed to use visual features for spatial info.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public abstract class AbstractRecommenderEngine<P extends AbstractRecommenderScriptManager> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractRecommenderEngine.class);

    protected static final String RECOMMENDER_THREAD_POOL_NAME = "Recommenders";

    protected static final int NUM_RECOMMENDER_THREADS = 1;

    protected PythonInterpreterFactory<P> factory;

    protected PythonJobCoordinator<P> coordinator;

    protected String site;

    public static final String DEFAULT_RECOMMENDER_JOB_COORDINATOR = "Recommenders - ";

    /**
     * 
     */
    public AbstractRecommenderEngine() {
    }

    /**
     * This method, as implemented by subclasses, needs to run the necessary
     * parts (all of them) of the recommender in correct order. This method
     * should be called by clients.
     * 
     * @param recommenderName
     */
    public void runEntireRecommender(String recommenderName,
            EventSet<IEvent> eventSet,
            IPythonJobListener<EventSet<IEvent>> listener) {
        IPythonExecutor<P, EventSet<IEvent>> executor = new EntireRecommenderExecutor<P>(
                recommenderName, eventSet);
        try {
            getCoordinator().submitJobWithCallback(executor, listener);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to run entire recommender", e);
        }
    }

    /**
     * This method, as implemented by subclasses, executes just the execute
     * method of a recommender. This method should be called by clients.
     * 
     * @param recommenderName
     * @param visualFeatures
     * @param dialogInfo
     * @param listener
     */
    public void runExecuteRecommender(String recommenderName,
            EventSet<IEvent> eventSet, VisualFeaturesList visualFeatures,
            Map<String, Serializable> dialogInfo,
            IPythonJobListener<EventSet<IEvent>> listener) {
        IPythonExecutor<P, EventSet<IEvent>> executor = new RecommenderExecutor<P>(
                recommenderName, eventSet, visualFeatures, dialogInfo);
        try {
            getCoordinator().submitJobWithCallback(executor, listener);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to run execute method of recommender",
                    e);
        }
    }

    /**
     * This method may do different things depending on the implementation.
     * Subclasses retrieve information about a possible dialog, or possibly read
     * from a file if no dialog should be present.
     * 
     * @param recommenderName
     *            Name of the recommender.
     * @param eventSet
     *            Event set providing context for this request.
     * @return Map holding the description of a dialog to be shown in order to
     *         get input from the user; if empty, no dialog is needed.
     */
    public Map<String, Serializable> getDialogInfo(String recommenderName,
            EventSet<IEvent> eventSet) {
        IPythonExecutor<P, Map<String, Serializable>> executor = new RecommenderDialogInfoExecutor<P>(
                recommenderName, eventSet);
        try {
            return getCoordinator().submitJob(executor).get();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get dialog information", e);
            return null;
        }
    }

    /**
     * This method may do different things depending on the implementation.
     * Subclasses create visual features based upon what the recommender
     * requests, or possibly read from a file.
     * 
     * @param recommenderName
     *            Name of the recommender.
     * @param eventSet
     *            Event set providing context for this request.
     * @return List of visual features to be used to get spatial input from the
     *         user; if empty, no spatial input is needed.
     */
    public VisualFeaturesList getSpatialInfo(String recommenderName,
            EventSet<IEvent> eventSet) {
        IPythonExecutor<P, VisualFeaturesList> executor = new RecommenderSpatialInfoExecutor<P>(
                recommenderName, eventSet);
        try {
            return getCoordinator().submitJob(executor).get();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get spatial information", e);
        }
        return null;
    }

    /**
     * This method may do different things depending on the implementation.
     * Subclasses retrieve script metadata from the file most likely.
     * 
     * @param recommenderName
     * @return
     */
    public Map<String, Serializable> getScriptMetadata(String recommenderName) {
        IPythonExecutor<P, Map<String, Serializable>> executor = new RecommenderMetadataExecutor<P>(
                recommenderName);
        try {
            return getCoordinator().submitJob(executor).get();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get script metadata", e);
        }
        return null;
    }

    public List<EventRecommender> getInventory() {
        IPythonExecutor<P, List<EventRecommender>> executor = new RecommenderInventoryExecutor<P>();
        try {
            return getCoordinator().submitJob(executor).get();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get inventory", e);
        }
        return null;
    }

    /**
     * Attempt to load the specified recommender and return it.
     * 
     * @param recommenderName
     *            The name of the recommender that should be or needs to be
     *            loaded before returning the inventory
     * @return The recommender that was loaded, or <code>null</code> if none was
     *         found.
     */
    public EventRecommender getInventory(String recommenderName) {
        IPythonExecutor<P, EventRecommender> executor = new RecommenderLoaderInventoryExecutor<P>(
                recommenderName);
        try {
            return getCoordinator().submitJob(executor).get();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get inventory", e);
        }
        return null;
    }

    /**
     * Allows multiple coordinators to be injected into another single engine
     * and thus ran all using a single command.
     * 
     * @param engines
     */
    public void injectEngine(AbstractRecommenderEngine<?>... engines) {
        for (AbstractRecommenderEngine<?> engine : engines) {
            // instantiates the Python Job Coordinator
            engine.getCoordinator();
        }
    }

    public PythonJobCoordinator<P> getCoordinator() {
        if (coordinator == null) {
            coordinator = createCoordinator();
        }
        return coordinator;
    }

    protected abstract PythonJobCoordinator<P> createCoordinator();

    /**
     * Shuts down the engine, and frees any threads that were originally
     * allocated. This should be called once we are done using the recommender
     * engine (not each time, but when the application that uses it has ended).
     */
    public void shutdownEngine() {
        if (coordinator != null) {
            coordinator.shutdown();
        }
    }

    /**
     * @return the site
     */
    public String getSite() {
        return site;
    }

    /**
     * Sets the site for the recommender engine.
     * 
     * @param site
     */
    public void setSite(String site) {
        this.site = site;
        shutdownEngine();
    }
}
