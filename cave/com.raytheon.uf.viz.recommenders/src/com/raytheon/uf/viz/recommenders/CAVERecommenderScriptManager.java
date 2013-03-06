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
package com.raytheon.uf.viz.recommenders;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory;
import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;
import com.raytheon.uf.common.recommenders.EventRecommender;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.recommenders.executors.CAVEEntireRecommenderExecutor;
import com.raytheon.uf.viz.recommenders.executors.CAVERecommenderDialogInfoExecutor;
import com.raytheon.uf.viz.recommenders.executors.CAVERecommenderExecutor;
import com.raytheon.uf.viz.recommenders.executors.CAVERecommenderGetMetadataExecutor;
import com.raytheon.uf.viz.recommenders.executors.CAVERecommenderSpatialInfoExecutor;

/**
 * CAVE-side implementation of the {@link AbstractRecommenderScriptManager}. Has
 * methods to runRecommenders start to finish, or run them piece by piece if
 * desired.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 24, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class CAVERecommenderScriptManager extends
        AbstractRecommenderScriptManager<IEvent> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CAVERecommenderScriptManager.class);

    private AbstractPythonScriptFactory<CAVERecommenderScriptManager> factory;

    private PythonJobCoordinator<CAVERecommenderScriptManager> coordinator;

    /**
     * @throws JepException
     * 
     */
    public CAVERecommenderScriptManager() throws JepException {
        super(buildScriptPath(), buildPythonPath(),
                CAVERecommenderScriptManager.class.getClassLoader(),
                "Recommender");
        factory = new CAVERecommenderPythonFactory();
        coordinator = PythonJobCoordinator.newInstance(factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager#
     * runRecommender(java.lang.String)
     */
    @Override
    public void runEntireRecommender(final String recommenderName,
            IPythonJobListener<List<IEvent>> listener) {
        IPythonExecutor<CAVERecommenderScriptManager, List<IEvent>> executor = new CAVEEntireRecommenderExecutor(
                recommenderName);
        try {
            coordinator.submitAsyncJob(executor, listener);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to submit job to executor service", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager#
     * runExecuteRecommender(java.lang.String, java.util.Map, java.util.Map,
     * com.raytheon.uf.common.python.concurrent.IPythonJobListener)
     */
    @Override
    public void runExecuteRecommender(String recommenderName,
            Set<IEvent> eventSet, Map<String, String> spatialInfo,
            Map<String, String> dialogInfo,
            IPythonJobListener<List<IEvent>> listener) {
        IPythonExecutor<CAVERecommenderScriptManager, List<IEvent>> executor = new CAVERecommenderExecutor(
                recommenderName, eventSet, spatialInfo, dialogInfo);
        try {
            coordinator.submitAsyncJob(executor, listener);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to submit job to executor service", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager#
     * executeRecommender(java.lang.String)
     */
    @Override
    public List<IEvent> executeEntireRecommender(String recommenderName) {
        for (EventRecommender rec : inventory) {
            if (rec.getName().equals(recommenderName)) {
                statusHandler.handle(Priority.VERBOSE, "Running "
                        + recommenderName);
                try {
                    String recName = resolveCorrectName(recommenderName);
                    if (isInstantiated(recName) == false) {
                        instantiatePythonScript(recName);
                    }
                    Map<String, String> dialogValues = getDialogInfo(recName);
                    showDialog(dialogValues);
                    Map<String, String> spatialValues = getSpatialInfo(recName);
                    return executeRecommender(recommenderName, null,
                            dialogValues, spatialValues);
                } catch (JepException e) {
                    statusHandler.handle(Priority.ERROR,
                            "Unable to execute recommender", e);
                }
            }
        }
        return null;
    }

    @Override
    protected Map<String, String> getDialogInfo(String recName) {
        CAVERecommenderDialogInfoExecutor executor = new CAVERecommenderDialogInfoExecutor(
                recName);
        try {
            return coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to submit job to executor service", e);
        }
        return null;
    }

    @Override
    protected Map<String, String> getSpatialInfo(String recName) {
        CAVERecommenderSpatialInfoExecutor executor = new CAVERecommenderSpatialInfoExecutor(
                recName);
        try {
            return coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to submit job to executor service", e);
        }
        return null;
    }

    @Override
    public Map<String, String> getScriptMetadata(String recommenderName) {
        CAVERecommenderGetMetadataExecutor executor = new CAVERecommenderGetMetadataExecutor(
                recommenderName);
        try {
            return coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to submit job to executor service", e);
        }
        return null;
    }

    private void showDialog(Map<String, String> dialogValues) {
        // TODO, call megawidgets
        // MegawidgetSpecifierFactory factory = new
        // MegawidgetSpecifierFactory();
        // megawidgets need to be refactored into a separate plugin
    }
}
