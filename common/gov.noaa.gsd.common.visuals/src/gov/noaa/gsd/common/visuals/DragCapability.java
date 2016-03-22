/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Description: Possible drag capabilities: None, part (individual parts of the entity may be
 * dragged), whole (the entire entity may be dragged together), and all
 * (either a part of the entity or the entire entity may be dragged).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 10, 2016   15676    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */

/**
 * 
 */
public enum DragCapability {

    // Values

    NONE("none"), PART("part"), WHOLE("whole"), ALL("all");

    // Private Static Constants

    /**
     * Map of descriptions to instances.
     */
    private static final Map<String, DragCapability> INSTANCES_FOR_DESCRIPTIONS = new LinkedHashMap<>(
            4, 1.0f);
    static {
        for (DragCapability value : DragCapability.values()) {
            INSTANCES_FOR_DESCRIPTIONS.put(value.description, value);
        }
    }

    // Private Variables

    /**
     * String description.
     */
    private final String description;

    // Public Static Methods

    /**
     * Get the instance with the specified description.
     * 
     * @param description
     *            Description for which an instance is to be found.
     * @return Instance corresponding to the given description, or
     *         <code>null</code> if no such instance is found.
     */
    public static DragCapability getInstance(String description) {
        return INSTANCES_FOR_DESCRIPTIONS.get(description);
    }

    /**
     * Get the list of descriptions of all possible values.
     * 
     * @return List of descriptions of all possible values.
     */
    public static List<String> getDescriptions() {
        return ImmutableList.copyOf(INSTANCES_FOR_DESCRIPTIONS.keySet());
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param description
     *            Description.
     */
    private DragCapability(String description) {
        this.description = description;
    }

    // Public Methods

    /**
     * Get the description.
     * 
     * @return Description.
     */
    public String getDescription() {
        return description;
    }
}
