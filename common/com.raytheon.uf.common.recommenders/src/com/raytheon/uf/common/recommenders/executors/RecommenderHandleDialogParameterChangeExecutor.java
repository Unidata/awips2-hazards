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

import java.util.Collection;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * {@link AbstractRecommenderExecutor} to handle recommender dialog-related
 * parameter changes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 21, 2017    3782    Chris.Golden  Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */

public class RecommenderHandleDialogParameterChangeExecutor<P extends AbstractRecommenderScriptManager>
        extends
        AbstractRecommenderExecutor<P, MutablePropertiesAndVisualFeatures> {

    private final EventSet<IEvent> eventSet;

    private final Collection<String> triggeringDialogIdentifiers;

    private final Map<String, Map<String, Object>> mutableDialogProperties;

    private final Collection<String> triggeringVisualFeatureIdentifiers;

    private final VisualFeaturesList visualFeatures;

    private final boolean collecting;

    public RecommenderHandleDialogParameterChangeExecutor(
            String recommenderName, EventSet<IEvent> eventSet,
            Collection<String> triggeringDialogIdentifiers,
            Map<String, Map<String, Object>> mutableDialogProperties,
            Collection<String> triggeringVisualFeatureIdentifiers,
            VisualFeaturesList visualFeatures, boolean collecting) {
        super(recommenderName);
        this.eventSet = eventSet;
        this.triggeringDialogIdentifiers = triggeringDialogIdentifiers;
        this.mutableDialogProperties = mutableDialogProperties;
        this.triggeringVisualFeatureIdentifiers = triggeringVisualFeatureIdentifiers;
        this.visualFeatures = visualFeatures;
        this.collecting = collecting;
    }

    @Override
    public MutablePropertiesAndVisualFeatures execute(P script) {
        return script.handleDialogParameterChange(recommenderName, eventSet,
                triggeringDialogIdentifiers, mutableDialogProperties,
                triggeringVisualFeatureIdentifiers, visualFeatures, collecting);
    }
}
