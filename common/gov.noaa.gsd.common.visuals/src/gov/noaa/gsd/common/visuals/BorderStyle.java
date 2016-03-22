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
 * Description: Possible border styles.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 10, 2016   15676    Chris.Golden Initial creation.
 * Mar 22, 2016   15676    Chris.Golden Added dotted border style.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum BorderStyle {

    // Values

    SOLID("solid"), DOTTED("dotted"), DASHED("dashed"), EVENT_TYPE("eventType");

    // Private Static Constants

    /**
     * Map of descriptions to instances.
     */
    private static final Map<String, BorderStyle> INSTANCES_FOR_DESCRIPTIONS = new LinkedHashMap<>(
            3, 1.0f);
    static {
        for (BorderStyle value : BorderStyle.values()) {
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
    public static BorderStyle getInstance(String description) {
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
    private BorderStyle(String description) {
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
