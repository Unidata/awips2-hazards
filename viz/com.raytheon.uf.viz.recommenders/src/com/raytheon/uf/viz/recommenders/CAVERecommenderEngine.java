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

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.viz.python.VizPythonJob;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * A single class in which all recommender actions will go through.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 06, 2013            mnash        Initial creation
 * Jan 29, 2015 3626       Chris.Golden Added EventSet to arguments for getting dialog
 *                                      info.
 * Mar 31, 2016  8837      Robert.Blum  Changes for Service Backup.
 * May 03, 2016 18376      Chris.Golden Changed to support reuse of Jep instance
 *                                      between H.S. sessions in the same CAVE session,
 *                                      since stopping and starting the Jep instances
 *                                      when the latter use numpy is dangerous.
 * Jun 23, 2016 19537      Chris.Golden Changed to use visual features for spatial
 *                                      info collection.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public final class CAVERecommenderEngine
        extends AbstractRecommenderEngine<CAVERecommenderScriptManager> {

    // Private Static Constants

    /**
     * Instance of recommender engine to be used for all Hazard Services
     * sessions. A single instance is shared, instead of starting up and
     * shutting down one instance per session, because of bad numpy-Jep
     * interactions as described
     * <a href="https://github.com/mrj0/jep/issues/28">here</a>. By keeping a
     * singleton around, Jep with numpy loaded is never shut down.
     */
    private static final CAVERecommenderEngine CAVE_RECOMMENDER_ENGINE = new CAVERecommenderEngine();

    // Public Methods

    /**
     * Get the singleton instance of this class.
     * 
     * @return Singleton instance of this class.
     */
    public static CAVERecommenderEngine getInstance() {
        return CAVE_RECOMMENDER_ENGINE;
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     */
    private CAVERecommenderEngine() {
        super();
    }

    // Public Methods

    @Override
    protected PythonJobCoordinator<CAVERecommenderScriptManager> createCoordinator() {
        factory = new CAVERecommenderPythonFactory(site);
        return new PythonJobCoordinator<>(NUM_RECOMMENDER_THREADS,
                RECOMMENDER_THREAD_POOL_NAME + " - " + site, factory);
    }

    @Override
    public void runExecuteRecommender(String recommenderName,
            EventSet<IEvent> eventSet, VisualFeaturesList visualFeatures,
            Map<String, Serializable> dialogInfo,
            IPythonJobListener<EventSet<IEvent>> listener) {
        VizPythonJob<EventSet<IEvent>> job = new VizPythonJob<>(recommenderName,
                listener);
        super.runExecuteRecommender(recommenderName, eventSet, visualFeatures,
                dialogInfo, job);
    }

    @Override
    public void runEntireRecommender(String recommenderName,
            EventSet<IEvent> eventSet,
            IPythonJobListener<EventSet<IEvent>> listener) {
        VizPythonJob<EventSet<IEvent>> job = new VizPythonJob<>(recommenderName,
                listener);
        super.runEntireRecommender(recommenderName, eventSet, job);
    }

    @Override
    public void shutdownEngine() {

        /*
         * Do nothing; since Jep and numpy do not play well together when a Jep
         * instance is shut down and then another one started that also uses
         * numpy, this instance needs to be kept around and functional in case
         * H.S. starts up again.
         */
    }
}
