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
package com.raytheon.uf.edex.recommenders;

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.recommenders.executors.RecommenderExecutor;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Engine used by EDEX to create a new {@link EDEXRecommenderScriptManager}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 23, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class EDEXRecommenderEngine extends
        AbstractRecommenderEngine<EDEXRecommenderScriptManager> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EDEXRecommenderEngine.class);

    /**
     * Default constructor
     */
    public EDEXRecommenderEngine() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.recommenders.AbstractRecommenderEngine#getCoordinator
     * ()
     */
    @Override
    protected PythonJobCoordinator<EDEXRecommenderScriptManager> getCoordinator() {
        factory = new EDEXRecommenderPythonFactory();
        return PythonJobCoordinator.newInstance(factory);
    }

    public EventSet<IEvent> runRecommender(String recommenderName,
            EventSet<IEvent> eventSet, Map<String, Serializable> spatialInfo,
            Map<String, Serializable> dialogInfo) {
        IPythonExecutor<EDEXRecommenderScriptManager, EventSet<IEvent>> executor = new RecommenderExecutor<EDEXRecommenderScriptManager>(
                recommenderName, eventSet, spatialInfo, dialogInfo);
        try {
            return getCoordinator(recommenderName).submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler
                    .handle(Priority.PROBLEM,
                            "Unable to submit job to run execute method of recommender",
                            e);
        }
        return null;
    }
}
