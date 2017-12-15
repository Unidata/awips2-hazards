/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Modification of the visual features of an event.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 21, 2017   38072    Chris.Golden Initial creation.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 *
 * @author Chris.Golden
 */
public class EventVisualFeaturesModification implements IEventModification {

    // Private Variables

    /**
     * Identifiers of the modified visual features.
     */
    private final Set<String> visualFeatureIdentifiers;

    // Public Constructors

    /**
     * Construct an instance indicating that a single visual feature changed.
     * 
     * @param visualFeatureIdentifier
     *            Identifier of the visual feature that changed.
     */
    public EventVisualFeaturesModification(String visualFeatureIdentifier) {
        this.visualFeatureIdentifiers = ImmutableSet
                .of(visualFeatureIdentifier);
    }

    /**
     * Construct an instance indicating that multiple visual features changed.
     * 
     * @param visualFeatureIdentifiers
     *            Identifiers of the visual features that changed.
     */
    public EventVisualFeaturesModification(
            Set<String> visualFeatureIdentifiers) {
        this.visualFeatureIdentifiers = ImmutableSet
                .copyOf(visualFeatureIdentifiers);
    }

    // Public Methods

    /**
     * Get the identifiers of the visual features that have changed. Note that
     * the returned set is not modifiable.
     * 
     * @return Identifiers of the visual featuers that have changed.
     */
    public Set<String> getVisualFeatureIdentifiers() {
        return visualFeatureIdentifiers;
    }

    @Override
    public void apply(IHazardEventView sourceEvent, IHazardEvent targetEvent) {
        targetEvent.setVisualFeatures(sourceEvent.getVisualFeatures());
    }

    @Override
    public MergeResult<? extends IEventModification> merge(
            IEventModification original, IEventModification modified) {

        /*
         * If the new modification is of the same type as this one, merge the
         * two together by combining their visual feature identifier sets;
         * otherwise, the merge has failed.
         */
        if (modified instanceof EventVisualFeaturesModification) {
            HashSet<String> combinedVisualFeatureIdentifiers = new HashSet<>(
                    getVisualFeatureIdentifiers());
            combinedVisualFeatureIdentifiers
                    .addAll(((EventVisualFeaturesModification) modified)
                            .getVisualFeatureIdentifiers());
            return IMergeable.Helper.getSuccessSubjectCancellationResult(
                    new EventVisualFeaturesModification(
                            combinedVisualFeatureIdentifiers));
        } else {
            return IMergeable.Helper.getFailureResult();
        }
    }
}
