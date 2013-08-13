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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;

/**
 * {@link AbstractRecommenderExecutor} to run the recommender, just the execute
 * method.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 05, 2013            mnash       Initial creation
 * Jul 12, 2013 1257       bsteffen    Convert recommender dialog info to use
 *                                     Serializeables for values instead of
 *                                     Strings.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderExecutor<P extends AbstractRecommenderScriptManager>
        extends AbstractRecommenderExecutor<P, List<IEvent>> {

    private Map<String, Serializable> spatialInfo;

    private Map<String, Serializable> dialogInfo;

    private EventSet<IEvent> eventSet;

    /**
     * Pass in the dialog info and spatial info values. We will not need to get
     * them when running.
     */
    public RecommenderExecutor(String recommenderName,
            EventSet<IEvent> eventSet, Map<String, Serializable> spatialInfo,
            Map<String, Serializable> dialogInfo) {
        super(recommenderName);
        this.eventSet = eventSet;
        this.spatialInfo = spatialInfo;
        this.dialogInfo = dialogInfo;
    }

    @Override
    public List<IEvent> execute(P script) {
        List<IEvent> events = script.executeRecommender(recommenderName,
                eventSet, dialogInfo, spatialInfo);
        return events;
    }
}
