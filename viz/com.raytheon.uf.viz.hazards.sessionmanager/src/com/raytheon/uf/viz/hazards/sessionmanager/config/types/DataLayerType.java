/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;

/**
 * Description: Enumeration of the types of data layers which when experiencing
 * a data update may initiate the execution of event-driven tools.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 27, 2016   18266    Chris.Golden  Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum DataLayerType {

    // Values

    RADAR(ImmutableSet.copyOf(Sets.newHashSet("RadarCoverageResource",
            "RadarGraphicsResource", "RadarRadialResource", "RadarSRMResource",
            "RadarXYResource"))), SATELLITE(Collections.<String> emptySet()), MRMS(
            Collections.<String> emptySet()), OTHER(Collections
            .<String> emptySet());

    // Private Constants

    /**
     * Names of subclasses of {@link AbstractVizResource} that fall under this
     * type.
     */
    private final Set<String> classNames;

    // Public Static Methods

    /**
     * Given the specified resource, get the associated data layer type.
     * 
     * @param resource
     *            Resource for which to get the data layer type.
     * @return Data layer type; {@link #OTHER} will always be returned if the
     *         resource is not found to be one of the other types.
     */
    public static DataLayerType getDataLayerType(
            AbstractVizResource<?, ?> resource) {
        String name = resource.getClass().getSimpleName();
        for (DataLayerType type : DataLayerType.values()) {
            if (type.getClassNames().contains(name)) {
                return type;
            }
        }
        return OTHER;
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param classNames
     *            Names of classes that fall into this type of data layer.
     */
    private DataLayerType(Set<String> classNames) {
        this.classNames = classNames;
    }

    // Public Methods

    /**
     * Get the class names, each of which is a subclass of
     * {@link AbstractVizResource}, associated with this data layer type.
     * 
     * @return Class names associated with this data layer type.
     */
    public Set<String> getClassNames() {
        return classNames;
    }
}
