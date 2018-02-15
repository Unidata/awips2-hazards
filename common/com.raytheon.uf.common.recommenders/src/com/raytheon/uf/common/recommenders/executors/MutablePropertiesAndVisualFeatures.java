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

import java.util.Map;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

/**
 * Simple encapsulation of megawidget mutable properties paired with visual
 * features.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 21, 2018    3782    Chris.Golden  Initial creation.
 *
 * </pre>
 *
 * @author Chris.Golden
 */
public class MutablePropertiesAndVisualFeatures {

    // Private Variables

    /**
     * Mutable properties.
     */
    private final Map<String, Map<String, Object>> mutableProperties;

    /**
     * Visual features.
     */
    private final VisualFeaturesList visualFeatures;

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public MutablePropertiesAndVisualFeatures() {
        mutableProperties = null;
        visualFeatures = null;
    }

    /**
     * Construct a standard instance.
     * 
     * @param mutableProperties
     *            Mutable properties.
     * @param visualFeatures
     *            Visual features.
     */
    public MutablePropertiesAndVisualFeatures(
            Map<String, Map<String, Object>> mutableProperties,
            VisualFeaturesList visualFeatures) {
        this.mutableProperties = mutableProperties;
        this.visualFeatures = visualFeatures;
    }

    // Public Methods

    /**
     * Get the mutable properties, if any.
     * 
     * @return Mutable properties, or <code>null</code> if none were provided at
     *         creation time.
     */
    public Map<String, Map<String, Object>> getMutableProperties() {
        return mutableProperties;
    }

    /**
     * Get the visual features, if any.
     * 
     * @return Visual features, or <code>null</code> if none were provided at
     *         creation time.
     */
    public VisualFeaturesList getVisualFeatures() {
        return visualFeatures;
    }
}
