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

/**
 * A single class in which all recommender actions will go through.
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

public final class CAVERecommenderEngine extends
        AbstractRecommenderEngine<CAVERecommenderScriptManager> {

    public CAVERecommenderEngine() {
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
    protected PythonJobCoordinator<CAVERecommenderScriptManager> getCoordinator() {
        factory = new CAVERecommenderPythonFactory();
        return PythonJobCoordinator.newInstance(factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.recommenders.AbstractRecommenderEngine#
     * runExecuteRecommender(java.lang.String,
     * com.raytheon.uf.common.dataplugin.events.EventSet, java.util.Map,
     * java.util.Map,
     * com.raytheon.uf.common.python.concurrent.IPythonJobListener)
     */
    @Override
    public void runExecuteRecommender(String recommenderName,
            EventSet<IEvent> eventSet, Map<String, Serializable> spatialInfo,
            Map<String, Serializable> dialogInfo,
            IPythonJobListener<EventSet<IEvent>> listener) {
        VizPythonJob<EventSet<IEvent>> job = new VizPythonJob<>(
                recommenderName, listener);
        super.runExecuteRecommender(recommenderName, eventSet, spatialInfo,
                dialogInfo, job);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.recommenders.AbstractRecommenderEngine#
     * runEntireRecommender(java.lang.String,
     * com.raytheon.uf.common.python.concurrent.IPythonJobListener)
     */
    @Override
    public void runEntireRecommender(String recommenderName,
            IPythonJobListener<EventSet<IEvent>> listener) {
        VizPythonJob<EventSet<IEvent>> job = new VizPythonJob<>(
                recommenderName, listener);
        super.runEntireRecommender(recommenderName, job);
    }
}
