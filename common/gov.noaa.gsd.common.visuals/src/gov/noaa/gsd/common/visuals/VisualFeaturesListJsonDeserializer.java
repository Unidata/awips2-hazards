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

import gov.noaa.gsd.common.visuals.VisualFeature.SerializableColor;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Description: Helper class for {@link VisualFeaturesListJsonConverter}
 * providing methods to deserialize JSON strings into instances of
 * {@link VisualFeaturesList}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 19, 2016   15676    Chris.Golden Initial creation.
 * Apr 05, 2016   15676    Chris.Golden Changed to detect invalid geometries
 *                                      and to warn about time range boundaries
 *                                      that do not lie on minute boundaries.
 * May 05, 2016   15676    Chris.Golden Added ability to be serialized to
 *                                      support Thrift serialiation and
 *                                      deserialization. This in turn allows
 *                                      two H.S. instances sharing an edex
 *                                      to see each other's stored events.
 * Jun 10, 2016   19537    Chris.Golden Combined base and selected visual feature
 *                                      lists for each hazard event into one,
 *                                      replaced by visibility constraints
 *                                      based upon selection state to individual
 *                                      visual features.
 * Jun 23, 2016   19537    Chris.Golden Added support for using "as event" as a
 *                                      value for label text in visual features,
 *                                      as well as support for new topmost and
 *                                      symbol shape properties of visual features.
 * Jul 25, 2016   19537    Chris.Golden Added support for new fill style and
 *                                      allow-drag-of-points-in-multi-geometries
 *                                      flag.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class VisualFeaturesListJsonDeserializer {

    // Private Interfaces

    /**
     * Interface describing the methods that must be implemented by a property
     * deserializer, used to deserialize properties of type <code>P</code> for a
     * visual feature.
     */
    private interface IPropertyDeserializer<P> {

        /**
         * Deserialize a property value of the enclosing class's parameterized
         * type from the specified node.
         * 
         * @param node
         *            Node from which to deserialize the property value.
         * @param identifier
         *            Identifier of the visual feature to which this property
         *            belongs.
         * @return Deserialized property value.
         * @throws JsonParseException
         *             If the JSON is malformed.
         */
        P deserializeProperty(JsonNode node, String identifier)
                throws JsonParseException;
    }

    /**
     * Interface describing the methods that must be implemented by a property
     * assigner, used to assign property values of type <code>P</code> to a
     * visual feature.
     */
    private interface IPropertyAssigner<P extends Serializable> {

        /**
         * Assign the specified property value to the specified visual feature.
         * 
         * @param value
         *            Temporally variant property value to be assigned.
         * @param visualFeature
         *            Visual feature to which to assign the property value.
         */
        void assignPropertyValue(TemporallyVariantProperty<P> value,
                VisualFeature visualFeature);
    }

    // Private Static Classes

    /**
     * Dependency, an encapsulation of the relationship created by one visual
     * feature having a dependency upon another.
     */
    private static class Dependency {

        // Private Variables

        /**
         * Feature that depends upon the other.
         */
        private final VisualFeature depender;

        /**
         * Feature upon which the other depends.
         */
        private final VisualFeature dependee;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param depender
         *            Feature that depends upon the other.
         * @param dependee
         *            Feature upon which the other depends.
         */
        public Dependency(VisualFeature depender, VisualFeature dependee) {
            this.depender = depender;
            this.dependee = dependee;
        }

        // Public Methods

        @Override
        public boolean equals(Object other) {
            return ((other instanceof Dependency)
                    && (depender == ((Dependency) other).depender) && (dependee == ((Dependency) other).dependee));
        }

        @Override
        public int hashCode() {
            return (int) ((((long) depender.hashCode()) + ((long) dependee
                    .hashCode())) % Integer.MAX_VALUE);
        }

        /**
         * Translate the dependency to a descriptive string.
         * 
         * @return Descriptive string.
         */
        @Override
        public String toString() {
            return depender.getIdentifier() + " -> " + dependee.getIdentifier();
        }
    }

    // Private Static Constants

    /**
     * One of the keys always found a JSON object representing a color.
     */
    private static final String COLOR_KEY_RED = "red";

    /**
     * Key for optional alpha component of a JSON object representing a color.
     */
    private static final String COLOR_KEY_ALPHA = "alpha";

    /**
     * Epoch timestamp matching expression.
     */
    private static final Pattern EPOCH_TIMESTAMP_PATTERN = Pattern
            .compile("^[0-9]+$");

    /**
     * Splitter used to break up time range strings into two timestamps.
     */
    private static final Splitter TIME_RANGE_STRING_SPLITTER = Splitter.on(" ")
            .omitEmptyStrings().trimResults();

    /**
     * Immutable map pairing property names with the deserializers to be used to
     * acquire values of these properties from a JSON node.
     */
    private static final ImmutableMap<String, IPropertyDeserializer<?>> DESERIALIZERS_FOR_PROPERTIES = ImmutableMap
            .<String, IPropertyDeserializer<?>> builder()
            .put(VisualFeaturesListJsonConverter.KEY_TEMPLATES,
                    new IPropertyDeserializer<List<String>>() {

                        @Override
                        public List<String> deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeListOfStrings(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEMPLATES);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_GEOMETRY,
                    new IPropertyDeserializer<Geometry>() {

                        @Override
                        public Geometry deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeGeometry(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_GEOMETRY);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_COLOR,
                    new IPropertyDeserializer<SerializableColor>() {

                        @Override
                        public SerializableColor deserializeProperty(
                                JsonNode node, String identifier)
                                throws JsonParseException {
                            return deserializeColor(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_BORDER_COLOR);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_FILL_COLOR,
                    new IPropertyDeserializer<SerializableColor>() {

                        @Override
                        public SerializableColor deserializeProperty(
                                JsonNode node, String identifier)
                                throws JsonParseException {
                            return deserializeColor(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_FILL_COLOR);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_THICKNESS,
                    new IPropertyDeserializer<Double>() {

                        @Override
                        public Double deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeNonNegativeDouble(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_BORDER_THICKNESS,
                                    false);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_STYLE,
                    new IPropertyDeserializer<BorderStyle>() {

                        @Override
                        public BorderStyle deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeBorderStyle(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_BORDER_STYLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_FILL_STYLE,
                    new IPropertyDeserializer<FillStyle>() {

                        @Override
                        public FillStyle deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeFillStyle(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_FILL_STYLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DIAMETER,
                    new IPropertyDeserializer<Double>() {

                        @Override
                        public Double deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeNonNegativeDouble(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_DIAMETER,
                                    true);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SYMBOL_SHAPE,
                    new IPropertyDeserializer<SymbolShape>() {

                        @Override
                        public SymbolShape deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeSymbolShape(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_SYMBOL_SHAPE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_LABEL,
                    new IPropertyDeserializer<String>() {

                        @Override
                        public String deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeString(node, identifier,
                                    VisualFeaturesListJsonConverter.KEY_LABEL);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_LENGTH,
                    new IPropertyDeserializer<Double>() {

                        @Override
                        public Double deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeNonNegativeDouble(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_LENGTH,
                                    false);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_DIR,
                    new IPropertyDeserializer<Double>() {

                        @Override
                        public Double deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeNonNegativeDouble(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_DIR,
                                    false);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_SIZE,
                    new IPropertyDeserializer<Integer>() {

                        @Override
                        public Integer deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeNonNegativeInteger(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_SIZE,
                                    true);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_COLOR,
                    new IPropertyDeserializer<SerializableColor>() {

                        @Override
                        public SerializableColor deserializeProperty(
                                JsonNode node, String identifier)
                                throws JsonParseException {
                            return deserializeColor(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_COLOR);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DRAGGABILITY,
                    new IPropertyDeserializer<DragCapability>() {

                        @Override
                        public DragCapability deserializeProperty(
                                JsonNode node, String identifier)
                                throws JsonParseException {
                            return deserializeDragCapability(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_DRAGGABILITY);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_MULTI_GEOMETRY_POINTS_DRAGGABLE,
                    new IPropertyDeserializer<Boolean>() {

                        @Override
                        public Boolean deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeBoolean(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_MULTI_GEOMETRY_POINTS_DRAGGABLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_ROTATABLE,
                    new IPropertyDeserializer<Boolean>() {

                        @Override
                        public Boolean deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeBoolean(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_ROTATABLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SCALEABLE,
                    new IPropertyDeserializer<Boolean>() {

                        @Override
                        public Boolean deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeBoolean(
                                    node,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_SCALEABLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TOPMOST,
                    new IPropertyDeserializer<Boolean>() {

                        @Override
                        public Boolean deserializeProperty(JsonNode node,
                                String identifier) throws JsonParseException {
                            return deserializeBoolean(node, identifier,
                                    VisualFeaturesListJsonConverter.KEY_TOPMOST);
                        }
                    }).build();

    /**
     * Immutable map pairing property names with the assigners to be used to
     * assign values for these properties.
     */
    private static final ImmutableMap<String, IPropertyAssigner<?>> ASSIGNERS_FOR_PROPERTIES = ImmutableMap
            .<String, IPropertyAssigner<?>> builder()
            .put(VisualFeaturesListJsonConverter.KEY_TEMPLATES,
                    new IPropertyAssigner<ImmutableList<String>>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<ImmutableList<String>> value,
                                VisualFeature visualFeature) {

                            /*
                             * Template identifiers are not assigned directly to
                             * a visual feature; instead, they are associated
                             * with that feature so that all visual features'
                             * templates can be checked to ensure there are no
                             * circular or unresolved dependencies. Thus,
                             * compile all the templates that might be
                             * associated with this visual feature into a single
                             * set and remember it for later. Also remember the
                             * temporally variant property so that, assuming no
                             * problems are found when the dependencies are
                             * checked post-deserialization, a new temporally
                             * variant property may be constructed using
                             * references to the visual features instead of
                             * their identifiers.
                             */
                            String identifier = visualFeature.getIdentifier();
                            Set<String> referencedFeatures = new HashSet<>();
                            List<String> features = value.getDefaultProperty();
                            if (features != null) {
                                referencedFeatures.addAll(features);
                            }
                            for (List<String> otherFeatures : value
                                    .getPropertiesForTimeRanges().values()) {
                                referencedFeatures.addAll(otherFeatures);
                            }
                            referencedTemplatesForFeatures.get().put(
                                    identifier, referencedFeatures);
                            templatesForFeatures.get().put(identifier, value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_GEOMETRY,
                    new IPropertyAssigner<Geometry>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Geometry> value,
                                VisualFeature visualFeature) {
                            visualFeature.setGeometry(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_COLOR,
                    new IPropertyAssigner<SerializableColor>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<SerializableColor> value,
                                VisualFeature visualFeature) {
                            visualFeature.setBorderColor(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_FILL_COLOR,
                    new IPropertyAssigner<SerializableColor>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<SerializableColor> value,
                                VisualFeature visualFeature) {
                            visualFeature.setFillColor(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_THICKNESS,
                    new IPropertyAssigner<Double>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Double> value,
                                VisualFeature visualFeature) {
                            visualFeature.setBorderThickness(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_STYLE,
                    new IPropertyAssigner<BorderStyle>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<BorderStyle> value,
                                VisualFeature visualFeature) {
                            visualFeature.setBorderStyle(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_FILL_STYLE,
                    new IPropertyAssigner<FillStyle>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<FillStyle> value,
                                VisualFeature visualFeature) {
                            visualFeature.setFillStyle(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DIAMETER,
                    new IPropertyAssigner<Double>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Double> value,
                                VisualFeature visualFeature) {
                            visualFeature.setDiameter(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SYMBOL_SHAPE,
                    new IPropertyAssigner<SymbolShape>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<SymbolShape> value,
                                VisualFeature visualFeature) {
                            visualFeature.setSymbolShape(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_LABEL,
                    new IPropertyAssigner<String>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<String> value,
                                VisualFeature visualFeature) {
                            visualFeature.setLabel(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_LENGTH,
                    new IPropertyAssigner<Double>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Double> value,
                                VisualFeature visualFeature) {
                            visualFeature.setTextOffsetLength(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_DIR,
                    new IPropertyAssigner<Double>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Double> value,
                                VisualFeature visualFeature) {
                            visualFeature.setTextOffsetDirection(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_SIZE,
                    new IPropertyAssigner<Integer>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Integer> value,
                                VisualFeature visualFeature) {
                            visualFeature.setTextSize(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_COLOR,
                    new IPropertyAssigner<SerializableColor>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<SerializableColor> value,
                                VisualFeature visualFeature) {
                            visualFeature.setTextColor(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DRAGGABILITY,
                    new IPropertyAssigner<DragCapability>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<DragCapability> value,
                                VisualFeature visualFeature) {
                            visualFeature.setDragCapability(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_MULTI_GEOMETRY_POINTS_DRAGGABLE,
                    new IPropertyAssigner<Boolean>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Boolean> value,
                                VisualFeature visualFeature) {
                            visualFeature
                                    .setMultiGeometryPointsDraggable(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_ROTATABLE,
                    new IPropertyAssigner<Boolean>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Boolean> value,
                                VisualFeature visualFeature) {
                            visualFeature.setRotatable(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SCALEABLE,
                    new IPropertyAssigner<Boolean>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Boolean> value,
                                VisualFeature visualFeature) {
                            visualFeature.setScaleable(value);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TOPMOST,
                    new IPropertyAssigner<Boolean>() {

                        @Override
                        public void assignPropertyValue(
                                TemporallyVariantProperty<Boolean> value,
                                VisualFeature visualFeature) {
                            visualFeature.setTopmost(value);
                        }
                    }).build();

    // Private Static Variables

    /**
     * Well-Known Text reader, used for deserializing geometries. It is
     * thread-local because <code>WKTReader</code> is not explicitly declared to
     * be thread-safe. This class's static methods may be called simultaneously
     * by multiple threads, and each thread must be able to deserialize
     * geometries separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<WKTReader> wktReader = new ThreadLocal<WKTReader>() {

        @Override
        protected WKTReader initialValue() {
            return new WKTReader();
        }
    };

    /**
     * Set of visual feature identifiers that are being deserialized. This is
     * used during deserialization to allow checking for non-unique identifiers.
     * It is thread-local because multiple threads might attempt to use it at
     * once, and each thread must be able to track the references of visual
     * features to one another separately in order to avoid cross-thread
     * pollution.
     */
    private static final ThreadLocal<Set<String>> features = new ThreadLocal<Set<String>>() {

        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };

    /**
     * Mapping of visual feature identifiers to sets of any visual feature
     * identifiers that they are using as templates. This is used during
     * deserialization to track the sets of all features that are referenced by
     * features as templates, as this information must be analyzed
     * post-deserialization to ensure that there are no circular or unresolved
     * dependencies. It is thread-local because multiple threads might attempt
     * to use it at once, and each thread must be able to track the references
     * of visual features to one another separately in order to avoid
     * cross-thread pollution.
     */
    private static final ThreadLocal<Map<String, Set<String>>> referencedTemplatesForFeatures = new ThreadLocal<Map<String, Set<String>>>() {

        @Override
        protected Map<String, Set<String>> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * Mapping of visual feature identifiers to temporally variant properties
     * holding lists of visual feature identifiers that they are to use as
     * templates. This is used after deserialization to assign the templates
     * property of the visual features properly. It is thread-local because
     * multiple threads might attempt to use it at once, and each thread must be
     * able to track the references of visual features to one another separately
     * in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<Map<String, TemporallyVariantProperty<ImmutableList<String>>>> templatesForFeatures = new ThreadLocal<Map<String, TemporallyVariantProperty<ImmutableList<String>>>>() {

        @Override
        protected Map<String, TemporallyVariantProperty<ImmutableList<String>>> initialValue() {
            return new HashMap<>();
        }
    };

    // Package Static Methods

    /**
     * Deserialize the next node in the specified JSON parser as a visual
     * features list.
     * 
     * @param jsonParser
     *            JSON parser.
     * @param context
     *            Deserialization context.
     * @return Visual features list.
     * @throws IOException
     *             If an error occurs when reading the nodes from the parser.
     * @throws JsonProcessingException
     *             If the JSON is malformed.
     */
    static VisualFeaturesList deserialize(JsonParser jsonParser,
            DeserializationContext context) throws IOException,
            JsonProcessingException {

        /*
         * Clear the thread-local set of display features, map of display
         * features to their referenced templates, and map of display features
         * to all their referenced templates, for this thread. These will be
         * used for the deserialization to track which visual features are to be
         * used as templates by which, and to avoid duplicate identifiers.
         */
        features.get().clear();
        referencedTemplatesForFeatures.get().clear();
        templatesForFeatures.get().clear();

        /*
         * Ensure an array was supplied.
         */
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (node instanceof ArrayNode == false) {
            throw new JsonParseException(
                    "expected JSON array holding visual features but got \""
                            + node + "\"", JsonLocation.NA);
        }
        VisualFeaturesList list = new VisualFeaturesList();

        /*
         * Iterate through the array elements, deserializing each into a visual
         * feature and adding it to the list.
         */
        for (int j = 0; j < node.size(); j++) {
            list.add(deserializeVisualFeature(node.get(j), j));
        }

        /*
         * Ensure that the visual features' specified templates do not lead to
         * any circular or unresolved dependencies, and set each of their
         * templates up to include references to one another (since the
         * templates were provided in the JSON as identifiers).
         */
        setTemplatesForVisualFeatures(list);

        return list;
    }

    // Private Static Methods

    /**
     * Deserialize the specified node in as a visual feature, with no templates
     * set. The list of templates (if any) provided by the node is instead
     * recorded within the {@link #templatesForFeatures} for this thread, so
     * that it may be checked for circular or unresolved dependencies later.
     * 
     * @param node
     *            Node to be deserialized.
     * @param index
     *            Index of the visual feature to be deserialized in the list.
     * @return Visual feature.
     * @throws JsonProcessingException
     *             If the JSON is malformed.
     */
    @SuppressWarnings("unchecked")
    private static VisualFeature deserializeVisualFeature(JsonNode node,
            int index) throws JsonProcessingException {

        /*
         * Ensure an object was supplied.
         */
        if (node instanceof ObjectNode == false) {
            throw new JsonParseException(
                    "expected JSON object holding visual feature but got \""
                            + node + "\"", JsonLocation.NA);
        }
        ObjectNode objectNode = (ObjectNode) node;

        /*
         * Ensure an identifier was supplied, and that it is unique within the
         * list.
         */
        String identifier = null;
        node = objectNode.get(VisualFeaturesListJsonConverter.KEY_IDENTIFIER);
        if (node instanceof TextNode == false) {
            throw new JsonParseException("expected valid unique string "
                    + "identifier for visual feature at index " + index
                    + " but "
                    + (node == null ? "none was provided" : "got " + node),
                    JsonLocation.NA);
        }
        identifier = node.getTextValue();
        if (identifier.isEmpty()) {
            throw new JsonParseException("expected valid unique string "
                    + "identifier for visual feature at index " + index
                    + " but got empty string", JsonLocation.NA);
        } else if (features.get().contains(identifier)) {
            throw new JsonParseException("identifier \"" + identifier + "\""
                    + "for visual feature at index " + index + " not unique",
                    JsonLocation.NA);
        }
        features.get().add(identifier);

        /*
         * Create the visual feature.
         */
        VisualFeature visualFeature = new VisualFeature(identifier);

        /*
         * Ensure visibility constraints are supplied.
         */
        node = objectNode
                .get(VisualFeaturesListJsonConverter.KEY_VISIBILITY_CONSTRAINTS);
        VisibilityConstraints visibilityConstraints = null;
        if (node instanceof TextNode) {
            visibilityConstraints = VisibilityConstraints.getInstance(node
                    .getTextValue());
        } else if (node == null) {
            visibilityConstraints = VisibilityConstraints.ALWAYS;
        }
        if (visibilityConstraints == null) {
            throw createValueDeserializationException(
                    identifier,
                    VisualFeaturesListJsonConverter.KEY_VISIBILITY_CONSTRAINTS,
                    "one of: "
                            + Joiner.on(", ").join(
                                    VisibilityConstraints.getDescriptions()),
                    node, null);
        } else {
            visualFeature.setVisibilityConstraints(visibilityConstraints);
        }

        /*
         * Iterate through the properties of the object, processing each in
         * turn.
         */
        Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.getFields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String name = entry.getKey();
            node = entry.getValue();

            /*
             * Skip the property if it is the identifier, as that has already
             * been processed. Also skip any properties that have null values.
             */
            if (name.equals(VisualFeaturesListJsonConverter.KEY_IDENTIFIER)
                    || (node == null) || (node instanceof NullNode)) {
                continue;
            }

            /*
             * Get the type of the property; if none is found, then skip the
             * property, as it is an unknown one.
             */
            TypeToken<?> type = VisualFeaturesListJsonConverter.TYPES_FOR_PROPERTIES
                    .get(name);
            if (type == null) {
                continue;
            }

            /*
             * Deserialize the property value and assign it to the visual
             * feature according to its type.
             */
            IPropertyDeserializer<?> deserializer = DESERIALIZERS_FOR_PROPERTIES
                    .get(name);
            IPropertyAssigner<?> assigner = ASSIGNERS_FOR_PROPERTIES.get(name);
            if (type.equals(VisualFeaturesListJsonConverter.TYPE_BOOLEAN)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<Boolean>) deserializer,
                        (IPropertyAssigner<Boolean>) assigner);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_INTEGER)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<Integer>) deserializer,
                        (IPropertyAssigner<Integer>) assigner);
            } else if (type.equals(VisualFeaturesListJsonConverter.TYPE_DOUBLE)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<Double>) deserializer,
                        (IPropertyAssigner<Double>) assigner);
            } else if (type.equals(VisualFeaturesListJsonConverter.TYPE_STRING)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<String>) deserializer,
                        (IPropertyAssigner<String>) assigner);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_LIST_OF_STRINGS)) {
                deserializeAndAssignProperty(
                        node,
                        visualFeature,
                        name,
                        (IPropertyDeserializer<ImmutableList<String>>) deserializer,
                        (IPropertyAssigner<ImmutableList<String>>) assigner);
            } else if (type.equals(VisualFeaturesListJsonConverter.TYPE_COLOR)) {
                deserializeAndAssignProperty(
                        node,
                        visualFeature,
                        name,
                        (IPropertyDeserializer<SerializableColor>) deserializer,
                        (IPropertyAssigner<SerializableColor>) assigner);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_GEOMETRY)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<Geometry>) deserializer,
                        (IPropertyAssigner<Geometry>) assigner);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_BORDER_STYLE)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<BorderStyle>) deserializer,
                        (IPropertyAssigner<BorderStyle>) assigner);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_FILL_STYLE)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<FillStyle>) deserializer,
                        (IPropertyAssigner<FillStyle>) assigner);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_SYMBOL_SHAPE)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<SymbolShape>) deserializer,
                        (IPropertyAssigner<SymbolShape>) assigner);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_DRAGGABILITY)) {
                deserializeAndAssignProperty(node, visualFeature, name,
                        (IPropertyDeserializer<DragCapability>) deserializer,
                        (IPropertyAssigner<DragCapability>) assigner);
            } else {

                /*
                 * This should never occur, but it is here to avoid having new
                 * properties added in the future cause silent deserialization
                 * failures because the code was not modified to handle a new
                 * type of property.
                 */
                throw new IllegalStateException(
                        "internal error: property \""
                                + name
                                + "\" could not be deserialized as it is of an unexpected type");
            }
        }

        /*
         * Return the completed visual feature.
         */
        return visualFeature;
    }

    /**
     * Set the templates for the visual features within the specified list
     * according to the information compiled within the
     * {@link #referencedTemplatesForFeatures} and the
     * {@link #templatesForFeatures} maps, ensuring that the templates do not
     * lead to any circular or unresolved dependencies in the process.
     * 
     * @param visualFeatures
     *            List of visual features for which to check the templates for
     *            problematic dependencies, and to set up templates for assuming
     *            there are no such problems.
     * @throws JsonParseException
     *             If bad dependencies are found.
     */
    private static void setTemplatesForVisualFeatures(
            VisualFeaturesList visualFeatures) throws JsonParseException {

        /*
         * Compile a mapping of visual feature identifiers to the features
         * themselves.
         */
        Map<String, VisualFeature> visualFeaturesForIdentifiers = new HashMap<>(
                visualFeatures.size(), 1.0f);
        for (VisualFeature visualFeature : visualFeatures) {
            visualFeaturesForIdentifiers.put(visualFeature.getIdentifier(),
                    visualFeature);
        }

        /*
         * Create a set of checked dependencies, into which will be placed any
         * dependencies that have been checked during the dependency problem
         * checking. This will be done to avoid rechecking any dependencies that
         * have already been checked once.
         */
        Set<Dependency> checkedDependencies = new HashSet<>();

        /*
         * Iterate through the sets of all potential templates associated with
         * each visual feature, checking each of the resulting dependencies to
         * make sure that there are no circular or unresolved ones.
         */
        for (Map.Entry<String, Set<String>> entry : referencedTemplatesForFeatures
                .get().entrySet()) {

            /*
             * Iterate through the individual dependencies for this visual
             * feature, checking each in turn.
             */
            String identifier = entry.getKey();
            for (String otherIdentifier : entry.getValue()) {
                checkedDependencies.addAll(ensureDependencyIsLegal(identifier,
                        otherIdentifier, visualFeaturesForIdentifiers,
                        checkedDependencies, new LinkedHashSet<String>(
                                visualFeatures.size(), 1.0f), ""));
            }
        }

        /*
         * If execution has made it to this point, then no bad dependencies were
         * found. Thus, fill in the templates for the visual features.
         */
        for (Map.Entry<String, TemporallyVariantProperty<ImmutableList<String>>> entry : templatesForFeatures
                .get().entrySet()) {

            /*
             * Convert the temporally variant property holding lists of
             * identifiers to one holding lists of visual features, and assign
             * it.
             */
            TemporallyVariantProperty<ImmutableList<String>> templateIdentifiers = entry
                    .getValue();
            TemporallyVariantProperty<ImmutableList<VisualFeature>> templates = new TemporallyVariantProperty<>(
                    convertIdentifiersToVisualFeatures(
                            templateIdentifiers.getDefaultProperty(),
                            visualFeaturesForIdentifiers));
            for (Map.Entry<Range<Date>, ImmutableList<String>> subEntry : templateIdentifiers
                    .getPropertiesForTimeRanges().entrySet()) {
                templates.addPropertyForTimeRange(
                        subEntry.getKey(),
                        convertIdentifiersToVisualFeatures(subEntry.getValue(),
                                visualFeaturesForIdentifiers));
            }
            visualFeaturesForIdentifiers.get(entry.getKey()).setTemplates(
                    templates);
        }
    }

    /**
     * Ensure that the specified dependency is legal (that is, does not result
     * in any circular or unresolved dependencies).
     * 
     * @param depender
     *            Identifier of the visual feature that is dependent upon the
     *            other.
     * @param dependee
     *            Identifier of the visual feature that is depended upon by the
     *            depender.
     * @param visualFeaturesForIdentifiers
     *            Map of visual features' identifiers to the visual features
     *            themselves.
     * @param checkedDependencies
     *            Set of all dependencies that have already been checked and
     *            found to be legal.
     * @param dependencyPath
     *            Ordered set of identifiers of visual features, ordered by
     *            dependency relationships (element 0 depends upon 1, 1 upon 2,
     *            and so on).
     * @return Set of all dependencies that have been checked as a result of
     *         this method executing successfully.
     * @throws JsonParseException
     *             If a dependency is found to be circular or unresolved.
     */
    private static Set<Dependency> ensureDependencyIsLegal(String depender,
            String dependee,
            Map<String, VisualFeature> visualFeaturesForIdentifiers,
            Set<Dependency> checkedDependencies,
            LinkedHashSet<String> dependencyPath, String prefix)
            throws JsonParseException {

        /*
         * Ensure that the dependee exists; if not, an error has occurred.
         */
        VisualFeature otherFeature = visualFeaturesForIdentifiers.get(dependee);
        if (otherFeature == null) {
            throw createDependencyException(depender, dependee,
                    "missing visual feature with latter identifier");
        }

        /*
         * See if this dependency has already been checked, and if so, do
         * nothing more with it.
         */
        Dependency dependency = new Dependency(
                visualFeaturesForIdentifiers.get(depender), otherFeature);
        if (checkedDependencies.contains(dependency)) {
            return Collections.emptySet();
        }

        /*
         * Determine whether or not this dependency is part of a circular
         * dependency by checking the dependency path as it was until now to
         * determine whether the dependee is already in the path. If so, an
         * error has occurred.
         */
        if (dependencyPath.contains(dependee)) {
            throw createDependencyException(dependencyPath.iterator().next(),
                    dependee,
                    "encountered circular dependency: "
                            + Joiner.on(" -> ").join(dependencyPath) + " -> "
                            + dependee);
        }

        /*
         * Add the dependee to the dependency path so that it may be passed onto
         * recursive calls for checking any dependees of this dependee.
         */
        dependencyPath.add(dependee);

        /*
         * Create a set of just-checked dependencies and add the one just
         * checked to the set. This set will have all the other dependencies
         * checked by recursive calls to this method added to it as well.
         */
        Set<Dependency> justCheckedDependencies = new HashSet<>();
        justCheckedDependencies.add(dependency);

        /*
         * Iterate through any dependencies that the dependee has, handling each
         * by calling this method recursively, and add any dependencies that the
         * recursive calls check successfully to the set of just-checked ones.
         */
        Set<String> subdependees = referencedTemplatesForFeatures.get().get(
                dependee);
        if (subdependees != null) {
            for (String subdependee : subdependees) {
                justCheckedDependencies.addAll(ensureDependencyIsLegal(
                        dependee, subdependee, visualFeaturesForIdentifiers,
                        checkedDependencies,
                        new LinkedHashSet<>(dependencyPath), prefix + "    "));
            }
        }

        /*
         * Return the set of just-checked dependencies.
         */
        return justCheckedDependencies;
    }

    /**
     * Convert the specified list of visual feature identifiers to a list of the
     * corresponding visual features.
     * 
     * @param identifiers
     *            List of visual feature identifiers; may be <code>null</code>.
     * @param visualFeaturesForIdentifiers
     *            Map of visual feature identifiers to their corresponding
     *            visual features.
     * @return List of visual features, or <code>null</code> if the specified
     *         list was <code>null</code>.
     */
    private static ImmutableList<VisualFeature> convertIdentifiersToVisualFeatures(
            List<String> identifiers,
            Map<String, VisualFeature> visualFeaturesForIdentifiers) {
        List<VisualFeature> features = null;
        if (identifiers != null) {
            features = new ArrayList<>(identifiers.size());
            for (String identifier : identifiers) {
                features.add(visualFeaturesForIdentifiers.get(identifier));
            }
        }
        return ImmutableList.copyOf(features);
    }

    /**
     * Deserialize the specified node as a temporally variant property with the
     * type of its potential values given by <code>P</code>, and assign it to
     * the specified visual feature.
     * 
     * @param node
     *            Node to be deserialized.
     * @param visualFeature
     *            Visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property that is being deserialized.
     * @param deserializer
     *            Deserializer to be used to actually deserialize the potential
     *            property values.
     * @param assigner
     *            Assigner to be used to assign the resulting temporally variant
     *            property to the visual feature.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static <P extends Serializable> void deserializeAndAssignProperty(
            JsonNode node, VisualFeature visualFeature, String propertyName,
            IPropertyDeserializer<P> deserializer, IPropertyAssigner<P> assigner)
            throws JsonParseException {

        /*
         * If the node is an object representation and does not have any of the
         * properties of another serialized object type, then it is actually a
         * dictionary pairing time ranges (and/or the string "default") with
         * property values. Otherwise, it is a property value itself that
         * applies regardless of time range boundaries.
         */
        String identifier = visualFeature.getIdentifier();
        TemporallyVariantProperty<P> property = null;
        if ((node instanceof ObjectNode) && (node.has(COLOR_KEY_RED) == false)) {

            /*
             * Create the temporally variant property object with the default
             * found in the node, if one is found.
             */
            property = new TemporallyVariantProperty<>(
                    deserializePropertyValue(
                            node.get(VisualFeaturesListJsonConverter.TEMPORALLY_VARIANT_KEY_DEFAULT),
                            identifier, deserializer));

            /*
             * Iterate through the key-value pairings for the node, processing
             * each in turn as a time range paired with a property value.
             */
            Iterator<String> iterator = node.getFieldNames();
            while (iterator.hasNext()) {

                /*
                 * Skip the default key-value pairing, as that has been dealt
                 * with above.
                 */
                String key = iterator.next();
                if (key.equals(VisualFeaturesListJsonConverter.TEMPORALLY_VARIANT_KEY_DEFAULT)) {
                    continue;
                }

                /*
                 * Parse the key string into a time range, consisting of two
                 * boundaries separated by a space. Each boundary may be either
                 * of the form given by the TIMESTAMP_FORMAT_PATTERN constant,
                 * or else it must be a long integer providing epoch time in
                 * milliseconds.
                 */
                Range<Date> timeRange = null;
                Throwable cause = null;
                List<String> timestamps = TIME_RANGE_STRING_SPLITTER
                        .splitToList(key);

                /*
                 * Only attempt to parse the boundaries of the time range if
                 * there are two of them.
                 */
                if (timestamps.size() == 2) {

                    /*
                     * for each boundary, parse it as either a long integer, or
                     * else a date-time specified according to the format
                     * mentioned above. Failure to do this indicates a parse
                     * error has occurred.
                     */
                    List<Date> boundaries = new ArrayList<>(2);
                    for (String timestamp : timestamps) {
                        Date boundary = null;
                        if (EPOCH_TIMESTAMP_PATTERN.matcher(timestamp)
                                .matches()) {
                            boundary = new Date(Long.parseLong(timestamp));
                        } else {
                            try {
                                boundary = VisualFeaturesListJsonConverter.timestampFormat
                                        .get().parse(timestamp);
                            } catch (Exception e) {
                                cause = e;
                                break;
                            }
                        }
                        boundaries.add(boundary);
                    }

                    /*
                     * Create a time range if the lower boundary is less than or
                     * equal to the upper boundary.
                     */
                    if (boundaries.get(0).compareTo(boundaries.get(1)) <= 0) {
                        timeRange = Range.closed(boundaries.get(0),
                                boundaries.get(1));
                    }
                }

                /*
                 * Signal an error if no time range was created.
                 */
                if (timeRange == null) {
                    throw createTimeRangeDeserializationException(identifier,
                            propertyName, key, cause);
                }

                /*
                 * Log a warning if the time range boundaries are not rounded to
                 * minutes (i.e. they should be 18:36:00, not 18:36:23).
                 */
                if ((timeRange.lowerEndpoint().getTime() % 60000L != 0L)
                        || (timeRange.upperEndpoint().getTime() % 60000L != 0L)) {
                    System.err
                            .println("WARNING: visual feature \""
                                    + identifier
                                    + "\" has property \""
                                    + propertyName
                                    + "\" with range "
                                    + timeRange
                                    + ", at least one end of which is not on a minute boundary.");
                }

                /*
                 * Deserialize the value that goes with this time range, and if
                 * this results in a non-null value, add the time range and the
                 * value to the temporally variant property.
                 */
                P value = deserializePropertyValue(node.get(key), identifier,
                        deserializer);
                if (value != null) {
                    property.addPropertyForTimeRange(timeRange, value);
                }
            }
        } else {

            /*
             * Deserialize the value for this property, and if this results in
             * something non-null, create a temporally variant property object
             * with only a default value to be used as the property.
             */
            P value = deserializePropertyValue(node, identifier, deserializer);
            if (value != null) {
                property = new TemporallyVariantProperty<>(value);
            }
        }

        /*
         * If a temporally variant property object was created, assign it.
         */
        if (property != null) {
            assigner.assignPropertyValue(property, visualFeature);
        }
    }

    /**
     * Deserialize the specified node as a property of type <code>P</code>.
     * 
     * @param node
     *            Node to be deserialized; may be <code>null</code> or of type
     *            {@link NullNode}, in which case <code>null</code> will be
     *            returned.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param deserializer
     *            Deserializer to be used to perform the actual deserialization.
     * @return Deserialized value, or <code>null</code> if there was no value.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static <P> P deserializePropertyValue(JsonNode node,
            String identifier, IPropertyDeserializer<P> deserializer)
            throws JsonParseException {
        return ((node != null) && (node instanceof NullNode == false) ? deserializer
                .deserializeProperty(node, identifier) : null);
    }

    /**
     * Deserialize the specified node as a boolean value.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return Boolean value.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static Boolean deserializeBoolean(JsonNode node, String identifier,
            String propertyName) throws JsonParseException {
        try {
            return VisualFeaturesListJsonConverter.CONVERTER.readValue(node,
                    Boolean.class);
        } catch (IOException e) {
            throw createValueDeserializationException(identifier, propertyName,
                    "boolean value", node, e);
        }
    }

    /**
     * Deserialize the specified node as a non-negative integer.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @param positive
     *            Flag indicating whether or not the deserialized value must be
     *            positive. If false, it must merely be non-negative.
     * @return Non-negative integer value.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static Integer deserializeNonNegativeInteger(JsonNode node,
            String identifier, String propertyName, boolean positive)
            throws JsonParseException {

        /*
         * If the node is a string specifying the event type integer, use that.
         */
        if (node.isTextual()
                && VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE
                        .equals(node.getTextValue())) {
            return VisualFeature.INTEGER_OF_EVENT_TYPE;
        }

        /*
         * Expect the value as an integer.
         */
        int value = 0;
        Throwable throwable = null;
        try {
            value = VisualFeaturesListJsonConverter.CONVERTER.readValue(node,
                    Integer.class);
        } catch (IOException e) {
            throwable = e;
        }
        if ((throwable != null) || (value < 0) || (positive && (value == 0))) {
            throw createValueDeserializationException(
                    identifier,
                    propertyName,
                    (positive ? "positive" : "non-negative") + " integer value",
                    node, throwable);
        }
        return value;
    }

    /**
     * Deserialize the specified node as a non-negative double.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @param positive
     *            Flag indicating whether or not the deserialized value must be
     *            positive. If false, it must merely be non-negative.
     * @return Non-negative double value.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static Double deserializeNonNegativeDouble(JsonNode node,
            String identifier, String propertyName, boolean positive)
            throws JsonParseException {

        /*
         * If the node is a string specifying the event type double, use that.
         */
        if (node.isTextual()
                && VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE
                        .equals(node.getTextValue())) {
            return VisualFeature.DOUBLE_OF_EVENT_TYPE;
        }

        /*
         * Expect the value as a double.
         */
        double value = 0.0;
        Throwable throwable = null;
        try {
            value = VisualFeaturesListJsonConverter.CONVERTER.readValue(node,
                    Double.class);
        } catch (IOException e) {
            throwable = e;
        }
        if ((throwable != null) || (value < 0.0)
                || (positive && (value == 0.0))) {
            throw createValueDeserializationException(identifier, propertyName,
                    (positive ? "positive" : "non-negative")
                            + " floating-point value", node, throwable);
        }
        return value;
    }

    /**
     * Deserialize the specified node as a string.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return String.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static String deserializeString(JsonNode node, String identifier,
            String propertyName) throws JsonParseException {

        /*
         * If the node is a string specifying the event type string, use that.
         */
        if (node.isTextual()
                && VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE
                        .equals(node.getTextValue())) {
            return VisualFeature.STRING_OF_EVENT_TYPE;
        }

        /*
         * Expect the value as a string.
         */
        String value = node.getTextValue();
        return ((value != null) && (value.isEmpty() == false) ? value : null);
    }

    /**
     * Deserialize the specified node as a list of strings.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return List of strings.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static List<String> deserializeListOfStrings(JsonNode node,
            String identifier, String propertyName) throws JsonParseException {
        try {
            return VisualFeaturesListJsonConverter.CONVERTER.readValue(node,
                    new TypeReference<List<String>>() {
                    });
        } catch (IOException e) {
            throw createValueDeserializationException(identifier, propertyName,
                    "list of strings", node, e);
        }
    }

    /**
     * Deserialize the specified node as a color.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return Color.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static SerializableColor deserializeColor(JsonNode node,
            String identifier, String propertyName) throws JsonParseException {

        /*
         * If the node is a string specifying the event type color, use that.
         */
        if (node.isTextual()
                && VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE
                        .equals(node.getTextValue())) {
            return VisualFeature.COLOR_OF_EVENT_TYPE;
        }

        /*
         * Expect the color to be a dictionary providing red, green, blue, and
         * (optionally) alpha values.
         */
        try {

            /*
             * Let the converter do the heavy lifting, but since the
             * instantiation of a Color object via deserialization in this
             * manner always leads to an alpha value of 0 if no alpha entry is
             * included in the JSON object, set it to 1 if it was not explicitly
             * specified.
             */
            SerializableColor color = VisualFeaturesListJsonConverter.CONVERTER
                    .readValue(node, SerializableColor.class);
            if (node.has(COLOR_KEY_ALPHA) == false) {
                color.setAlpha(1.0f);
            }
            return color;
        } catch (IOException e) {
            throw createValueDeserializationException(identifier, propertyName,
                    "color dictionary", node, e);
        }
    }

    /**
     * Deserialize the specified node as a geometry.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return Geometry.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static Geometry deserializeGeometry(JsonNode node,
            String identifier, String propertyName) throws JsonParseException {
        Geometry geometry;
        try {
            geometry = wktReader.get().read(
                    VisualFeaturesListJsonConverter.CONVERTER.readValue(node,
                            String.class));
        } catch (IOException | ParseException e) {
            throw createValueDeserializationException(identifier, propertyName,
                    "geometry in Well-Known Text format", node, e);
        }
        if (geometry.isValid() == false) {
            throw createValueDeserializationException(identifier, propertyName,
                    "valid geometry", node, null);
        }
        return geometry;
    }

    /**
     * Deserialize the specified node as a border style.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return Border style.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static BorderStyle deserializeBorderStyle(JsonNode node,
            String identifier, String propertyName) throws JsonParseException {
        String description = node.getTextValue();
        BorderStyle style = BorderStyle.getInstance(description);
        if (style == null) {
            throw createValueDeserializationException(
                    identifier,
                    propertyName,
                    "one of: "
                            + Joiner.on(", ").join(
                                    BorderStyle.getDescriptions()), node, null);
        }
        return style;
    }

    /**
     * Deserialize the specified node as a fill style.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return Border style.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static FillStyle deserializeFillStyle(JsonNode node,
            String identifier, String propertyName) throws JsonParseException {
        String description = node.getTextValue();
        FillStyle style = FillStyle.getInstance(description);
        if (style == null) {
            throw createValueDeserializationException(
                    identifier,
                    propertyName,
                    "one of: "
                            + Joiner.on(", ").join(
                                    BorderStyle.getDescriptions()), node, null);
        }
        return style;
    }

    /**
     * Deserialize the specified node as a symbol shape.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return Symbol shape.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static SymbolShape deserializeSymbolShape(JsonNode node,
            String identifier, String propertyName) throws JsonParseException {
        String description = node.getTextValue();
        SymbolShape style = SymbolShape.getInstance(description);
        if (style == null) {
            throw createValueDeserializationException(
                    identifier,
                    propertyName,
                    "one of: "
                            + Joiner.on(", ").join(
                                    SymbolShape.getDescriptions()), node, null);
        }
        return style;
    }

    /**
     * Deserialize the specified node as a drag capability.
     * 
     * @param node
     *            Node to be deserialized.
     * @param identifier
     *            Identifier of the visual feature that is being deserialized.
     * @param propertyName
     *            Name of the property for which a value is being deserialized.
     * @return Drag capability.
     * @throws JsonParseException
     *             If an error occurs during deserialization.
     */
    private static DragCapability deserializeDragCapability(JsonNode node,
            String identifier, String propertyName) throws JsonParseException {
        String description = node.getTextValue();
        DragCapability draggability = DragCapability.getInstance(description);
        if (draggability == null) {
            throw createValueDeserializationException(
                    identifier,
                    propertyName,
                    "one of: "
                            + Joiner.on(", ").join(
                                    DragCapability.getDescriptions()), node,
                    null);
        }
        return draggability;
    }

    /**
     * Create a time range deserialization exception for the specified node with
     * the specified cause for the specified description and visual feature
     * identifier.
     * 
     * @param identifier
     *            Identifier of the visual feature that could not be
     *            deserialized.
     * @param propertyName
     *            Name of the property for which the time range could not be
     *            deserialized.
     * @param badTimeRange
     *            Bad time range string that could not be deserialized.
     * @param cause
     *            Exception that signaled the problem, if any.
     * @return Created exception.
     */
    private static JsonParseException createTimeRangeDeserializationException(
            String identifier, String propertyName, String badTimeRange,
            Throwable cause) {
        return new JsonParseException("visual feature \"" + identifier
                + "\": expected time range for property \"" + propertyName
                + "\" to be string holding two timestamps (lower and "
                + "upper boundaries, each of the form of either a "
                + "number giving epoch time in milliseconds, or else "
                + VisualFeaturesListJsonConverter.TIMESTAMP_FORMAT_PATTERN
                + ") separated by a space, but got \"" + badTimeRange + "\"",
                JsonLocation.NA, cause);
    }

    /**
     * Create a property value deserialization exception for the specified node
     * with the specified cause for the specified description and visual feature
     * identifier.
     * 
     * @param identifier
     *            Identifier of the visual feature that could not be
     *            deserialized.
     * @param propertyName
     *            Name of the property that could not be deserialized.
     * @param valueDescription
     *            Description of the value that was expected, such as
     *            "list of strings".
     * @param node
     *            Node that did not yield the correct property value.
     * @param cause
     *            Exception that signaled the problem, if any.
     * @return Created exception.
     */
    private static JsonParseException createValueDeserializationException(
            String identifier, String propertyName, String valueDescription,
            JsonNode node, Throwable cause) {
        return new JsonParseException("visual feature \"" + identifier
                + "\": expected value for property \"" + propertyName
                + "\" to be " + valueDescription + " but got \"" + node + "\"",
                JsonLocation.NA, cause);
    }

    /**
     * Create a template dependency exception for the specified visual feature
     * identifier and the template identifier with the specified description.
     * 
     * @param identifier
     *            Identifier of the visual feature that has a bad dependency.
     * @param otherIdentifier
     *            Identifier of the visual feature upon which the first one has
     *            a bad dependency.
     * @param description
     *            Description of the problem with the dependency.
     * @return Created exception.
     */
    private static JsonParseException createDependencyException(
            String identifier, String otherIdentifier, String description) {
        return new JsonParseException("visual feature \"" + identifier
                + "\": \"templates\" property includes bad dependency "
                + "upon visual feature \"" + otherIdentifier + "\": "
                + description, JsonLocation.NA);
    }
}
