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
 * Description: Possible fill styles.
 * <p>
 * <strong>Note</strong>: The ordering of the values must not change, as the
 * ordinals are used in serialization and deserialization of
 * {@link VisualFeature} instances. Also, if more than 256 different values are
 * specified, serialization and deserialization as done by
 * {@link VisualFeaturesListBinarySerializer} and
 * {@link VisualFeaturesListBinaryDeserializer} will need to be changed to use
 * more than one byte.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2016   19537    Chris.Golden Initial creation.
 * Feb 13, 2017   28892    Chris.Golden Added comment about need to maintain
 *                                      ordering of values so that serialization
 *                                      and deserialization will work between
 *                                      versions of enclosing objects. Also
 *                                      added static method to allow value to
 *                                      be looked up by ordinal without using
 *                                      values() each time.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum FillStyle {

    // Values

    SOLID("solid"), HATCHED("hatched");

    // Private Static Constants

    /**
     * Map of descriptions to instances.
     */
    private static final Map<String, FillStyle> INSTANCES_FOR_DESCRIPTIONS = new LinkedHashMap<>(
            3, 1.0f);
    static {
        for (FillStyle value : FillStyle.values()) {
            INSTANCES_FOR_DESCRIPTIONS.put(value.description, value);
        }
    }

    /**
     * List of all possible values, indexed by their ordinals. This is cached
     * because {@link #values()} creates a new array each time it is called.
     */
    private static final List<FillStyle> ALL_VALUES = ImmutableList
            .copyOf(values());

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
    public static FillStyle getInstance(String description) {
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

    /**
     * Get the value with the specified ordinal. Invoking this method rather
     * than {@link #values()} is preferable because the latter creates a new
     * array each time it is called.
     * 
     * @param ordinal
     *            Ordinal for which to fetch the value.
     * @return Value.
     * @throws IllegalArgumentException
     *             If the ordinal value is not within range.
     */
    public static FillStyle getValueForOrdinal(int ordinal)
            throws IllegalArgumentException {
        if ((ordinal < 0) || (ordinal >= ALL_VALUES.size())) {
            throw new IllegalArgumentException("ordinal value " + ordinal
                    + " is out of range");
        }
        return ALL_VALUES.get(ordinal);
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param description
     *            Description.
     */
    private FillStyle(String description) {
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
