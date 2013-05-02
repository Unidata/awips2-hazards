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

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory;
import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.recommenders.executors.EntireRecommenderExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderDialogInfoExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderInventoryExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderMetadataExecutor;
import com.raytheon.uf.common.recommenders.executors.RecommenderSpatialInfoExecutor;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

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
 * Mar 6, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public abstract class AbstractRecommenderEngine<P extends AbstractRecommenderScriptManager> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractRecommenderEngine.class);

    protected PythonJobCoordinator<P> coordinator;

    protected AbstractPythonScriptFactory<P> factory;

    /**
     * 
     */
    public AbstractRecommenderEngine() {
        coordinator = getCoordinator();
    }

    /**
     * This method, as implemented by subclasses, needs to run the necessary
     * parts (all of them) of the recommender in correct order. This method
     * should be called by clients.
     * 
     * @param recommenderName
     */
    public void runEntireRecommender(String recommenderName,
            IPythonJobListener<List<IEvent>> listener) {
        IPythonExecutor<P, List<IEvent>> executor = new EntireRecommenderExecutor<P>(
                recommenderName);
        try {
            coordinator.submitAsyncJob(executor, listener);
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
     * @param spatialInfo
     * @param dialogInfo
     * @param listener
     */
    public void runExecuteRecommender(String recommenderName,
            EventSet<IEvent> eventSet, Map<String, String> spatialInfo,
            Map<String, String> dialogInfo,
            IPythonJobListener<List<IEvent>> listener) {
        IPythonExecutor<P, List<IEvent>> executor = new RecommenderExecutor<P>(
                recommenderName, eventSet, spatialInfo, dialogInfo);
        try {
            coordinator.submitAsyncJob(executor, listener);
        } catch (Exception e) {
            statusHandler
                    .handle(Priority.PROBLEM,
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
     * @return
     */
    public Map<String, String> getDialogInfo(String recommenderName) {
        IPythonExecutor<P, Map<String, String>> executor = new RecommenderDialogInfoExecutor<P>(
                recommenderName);
        try {
            return coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get dialog information", e);
        }
        return null;
    }

    /**
     * This method may do different things depending on the implementation.
     * Subclasses retrieve information about the spatial info, or possibly read
     * from a file if no spatial info should be present.
     * 
     * @param recommenderName
     * @return
     */
    public Map<String, String> getSpatialInfo(String recommenderName) {
        IPythonExecutor<P, Map<String, String>> executor = new RecommenderSpatialInfoExecutor<P>(
                recommenderName);
        try {
            return coordinator.submitSyncJob(executor);
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
    public Map<String, String> getScriptMetadata(String recommenderName) {
        IPythonExecutor<P, Map<String, String>> executor = new RecommenderMetadataExecutor<P>(
                recommenderName);
        try {
            return coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get script metadata", e);
        }
        return null;
    }

    public List<EventRecommender> getInventory() {
        IPythonExecutor<P, List<EventRecommender>> executor = new RecommenderInventoryExecutor<P>();
        try {
            return coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to get inventory", e);
        }
        return null;
    }

    protected abstract PythonJobCoordinator<P> getCoordinator();

}
