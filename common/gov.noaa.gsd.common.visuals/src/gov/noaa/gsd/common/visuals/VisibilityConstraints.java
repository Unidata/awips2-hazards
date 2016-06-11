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
 * Description: Visibility constraints, indicating what the state of the object
 * represented by a visual feature must be in order to have the latter be
 * visible. <code>NEVER</code> indicates the visual feature is never visible;
 * this may be used so that a visual feature may be kept but rendered invisible.
 * <code>UNSELECTED</code> indicates the visual feature should only be visible
 * if the object it represents is unselected, while <code>SELECTED</code>
 * indicates the opposite. <code>ALWAYS</code> means that regardless of
 * selection state of the represented object, the visual feature should be
 * visible.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 10, 2016   19537    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum VisibilityConstraints {

    // Values

    NEVER("never"), UNSELECTED("unselected"), SELECTED("selected"), ALWAYS(
            "always");

    // Private Static Constants

    /**
     * Map of descriptions to instances.
     */
    private static final Map<String, VisibilityConstraints> INSTANCES_FOR_DESCRIPTIONS = new LinkedHashMap<>(
            4, 1.0f);
    static {
        for (VisibilityConstraints value : VisibilityConstraints.values()) {
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
    public static VisibilityConstraints getInstance(String description) {
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
    private VisibilityConstraints(String description) {
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
