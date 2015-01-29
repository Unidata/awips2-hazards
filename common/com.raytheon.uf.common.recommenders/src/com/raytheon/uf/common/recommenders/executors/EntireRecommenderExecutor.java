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
package com.raytheon.uf.common.recommenders.executors;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;

/**
 * Executes an entire recommender, start to finish.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 05, 2013            mnash        Initial creation
 * Jan 29, 2015 3626       Chris.Golden Added EventSet to arguments for getting dialog
 *                                      info.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class EntireRecommenderExecutor<P extends AbstractRecommenderScriptManager>
        extends AbstractRecommenderExecutor<P, EventSet<IEvent>> {

    private final EventSet<IEvent> eventSet;

    /**
     * @param recommenderName
     */
    public EntireRecommenderExecutor(String recommenderName,
            EventSet<IEvent> eventSet) {
        super(recommenderName);
        this.eventSet = eventSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.concurrent.IPythonExecutor#execute(com.
     * raytheon.uf.common.python.PythonInterpreter)
     */
    @Override
    public EventSet<IEvent> execute(P script) {
        return script.executeEntireRecommender(recommenderName, eventSet);
    }
}
