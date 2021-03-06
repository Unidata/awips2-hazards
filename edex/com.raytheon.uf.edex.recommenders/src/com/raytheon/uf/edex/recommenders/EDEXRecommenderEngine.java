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

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * Engine used by EDEX to create a new {@link EDEXRecommenderScriptManager}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 23, 2013            mnash        Initial creation.
 * Dec 16, 2015   14019    Robert.Blum  Updates for new PythonJobCoordinator API.
 * Mar 31, 2016    8837    Robert.Blum  Changes for Service Backup.
 * Jun 23, 2016   19537    Chris.Golden Changed to use visual features for
 *                                      spatial info gathering.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class EDEXRecommenderEngine
        extends AbstractRecommenderEngine<EDEXRecommenderScriptManager> {

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
     * @see com.raytheon.uf.common.recommenders.AbstractRecommenderEngine#
     * getCoordinator ()
     */
    @Override
    protected PythonJobCoordinator<EDEXRecommenderScriptManager> createCoordinator() {
        factory = new EDEXRecommenderPythonFactory(getSite());
        return new PythonJobCoordinator<>(NUM_RECOMMENDER_THREADS,
                RECOMMENDER_THREAD_POOL_NAME + " - " + site, factory);
    }

    public EventSet<IEvent> runRecommender(String recommenderName,
            EventSet<IEvent> eventSet, VisualFeaturesList visualFeatures,
            Map<String, Serializable> dialogInfo) {
        IPythonExecutor<EDEXRecommenderScriptManager, EventSet<IEvent>> executor = new RecommenderExecutor<EDEXRecommenderScriptManager>(
                recommenderName, eventSet, visualFeatures, dialogInfo);
        try {
            return getCoordinator().submitJob(executor).get();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to submit job to run execute method of recommender",
                    e);
        }
        return null;
    }
}
