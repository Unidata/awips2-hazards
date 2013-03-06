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

import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Feb 5, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderExecutor<P extends AbstractRecommenderScriptManager>
        extends AbstractRecommenderExecutor<P, List<IEvent>> {

    private Map<String, String> spatialInfo;

    private Map<String, String> dialogInfo;

    private Set<IEvent> eventSet;

    /**
     * Pass in the dialog info and spatial info values. We will not need to get
     * them when running.
     */
    public RecommenderExecutor(String recommenderName, Set<IEvent> eventSet,
            Map<String, String> spatialInfo, Map<String, String> dialogInfo) {
        super(recommenderName);
        this.eventSet = eventSet;
        this.spatialInfo = spatialInfo;
        this.dialogInfo = dialogInfo;
    }

    @Override
    public List<IEvent> execute(P script) {
        return script.executeRecommender(recommenderName, eventSet, dialogInfo,
                spatialInfo);
    }
}
