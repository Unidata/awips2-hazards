/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

/**
 * Source of edits to geometries involving drag-and-drop. "Source" means what
 * sort of drawable entity is beginning the edit.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 22, 2018   25765    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum DragAndDropGeometryEditSource {
    VERTEX("vertex"), BOUNDING_BOX("boundingBox");

    // Private Static Constants

    /**
     * Map of geometry edit sources in camel case to instances.
     */
    private static final Map<String, DragAndDropGeometryEditSource> GEOMETRY_EDIT_SOURCES_FROM_IDENTIFIERS;

    static {
        Map<String, DragAndDropGeometryEditSource> map = new HashMap<>(
                DragAndDropGeometryEditSource.values().length, 1.0f);
        for (DragAndDropGeometryEditSource value : DragAndDropGeometryEditSource
                .values()) {
            map.put(value.toString(), value);
        }
        GEOMETRY_EDIT_SOURCES_FROM_IDENTIFIERS = ImmutableMap.copyOf(map);
    };

    // Private Variables

    /**
     * Identifier.
     */
    private final String identifier;

    // Public Static Methods

    /**
     * Get the specified drag-and-drop geometry edit source in camel case. This
     * method is provided to allow this type to be instantiated from a
     * lower-case version of its name, for {@link ObjectMapper}-based JSON
     * deserialization purposes.
     * 
     * @param value
     *            Drag-and-drop geometry edit source to be fetched, but in camel
     *            case.
     * @return Drag-and-drop geometry edit source.
     */
    @JsonCreator
    public static DragAndDropGeometryEditSource fromString(String value) {
        return GEOMETRY_EDIT_SOURCES_FROM_IDENTIFIERS.get(value);
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     *
     * @param identifier
     *            Identifier.
     */
    private DragAndDropGeometryEditSource(String identifier) {
        this.identifier = identifier;
    }

    // Public Methods

    @Override
    @JsonValue
    public String toString() {
        return identifier;
    }
}
