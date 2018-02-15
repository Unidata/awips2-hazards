/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.recommenders.executors;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * {@link AbstractRecommenderExecutor} to check the spatial information from the
 * recommender for completeness.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 17, 2017    3782    Chris.Golden  Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */

public class RecommenderCheckSpatialInfoExecutor<P extends AbstractRecommenderScriptManager>
        extends AbstractRecommenderExecutor<P, Boolean> {

    private final EventSet<IEvent> eventSet;

    private final VisualFeaturesList visualFeatures;

    public RecommenderCheckSpatialInfoExecutor(String recommenderName,
            EventSet<IEvent> eventSet, VisualFeaturesList visualFeatures) {
        super(recommenderName);
        this.eventSet = eventSet;
        this.visualFeatures = visualFeatures;
    }

    @Override
    public Boolean execute(P script) {
        return script.isVisualFeaturesCompleteSetOfSpatialInfo(recommenderName,
                eventSet, visualFeatures);
    }
}
