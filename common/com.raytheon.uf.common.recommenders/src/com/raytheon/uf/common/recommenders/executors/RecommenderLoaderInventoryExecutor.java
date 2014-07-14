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

import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;
import com.raytheon.uf.common.recommenders.EventRecommender;

/**
 * THIS EXECUTOR CURRENTLY ONLY EXISTS FOR TESTING PURPOSES. This executor
 * allows a user to specify a recommender that they would like to be loaded into
 * inventory before retrieving the inventory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 14, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RecommenderLoaderInventoryExecutor<P extends AbstractRecommenderScriptManager>
        extends RecommenderInventoryExecutor<P> {

    private final String recommenderName;

    public RecommenderLoaderInventoryExecutor(final String recommenderName) {
        this.recommenderName = recommenderName;
    }

    @Override
    public List<EventRecommender> execute(P script) {
        return script.getInventory(this.recommenderName);
    }
}