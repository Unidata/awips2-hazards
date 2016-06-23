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

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;

/**
 * {@link AbstractRecommenderExecutor} to get the spatial information from the
 * recommender.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 06, 2013            mnash     Initial creation
 * Jan 29, 2015 3626       Chris.Golden Added EventSet to arguments for getting dialog
 *                                      info.
 * Jun 23, 2016 19537      Chris.Golden Changed to use visual features for spatial info.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderSpatialInfoExecutor<P extends AbstractRecommenderScriptManager>
        extends AbstractRecommenderExecutor<P, VisualFeaturesList> {

    private final EventSet<IEvent> eventSet;

    public RecommenderSpatialInfoExecutor(String recommenderName,
            EventSet<IEvent> eventSet) {
        super(recommenderName);
        this.eventSet = eventSet;
    }

    @Override
    public VisualFeaturesList execute(P script) {
        return script.getVisualFeatures(recommenderName, eventSet);
    }
}
