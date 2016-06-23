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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Description: Helper class for {@link VisualFeaturesListJsonConverter}
 * providing methods to serialize instances of {@link VisualFeaturesList} into
 * JSON strings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 24, 2016   15676    Chris.Golden Initial creation.
 * Mar 29, 2016   15676    Chris.Golden Altered for proper serialization.
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class VisualFeaturesListJsonSerializer {

    // Private Interfaces

    /**
     * Interface describing the methods that must be implemented by a property
     * serializer, used to serialize properties of type <code>P</code> for a
     * visual feature.
     */
    private interface IPropertySerializer<P extends Serializable> {

        /**
         * Serialize a property value of the enclosing class's parameterized
         * type from the specified node.
         * 
         * @param value
         *            Property value to be serialized.
         * @param jsonGenerator
         *            JSON generator into which to serialize the property value.
         * @param identifier
         *            Identifier of the visual feature to which this property
         *            belongs.
         * @throws JsonGenerationException
         *             If the property cannot be serialized.
         */
        void serializeProperty(P value, JsonGenerator generator,
                String identifier) throws JsonGenerationException;
    }

    /**
     * Interface describing the methods that must be implemented by a property
     * fetcher, used to fetch property values of type <code>P</code> from a
     * visual feature.
     */
    private interface IPropertyFetcher<P extends Serializable> {

        /**
         * Fetch the value of the property appropriate to the implementation
         * from the specified visual feature.
         * 
         * @param visualFeature
         *            Visual feature from which to fetch the property value.
         * @return Property value, or <code>null</code> if there is none.
         */
        TemporallyVariantProperty<P> fetchPropertyValue(
                VisualFeature visualFeature);
    }

    // Private Static Constants

    /**
     * Immutable map pairing property names with the serializers to be used to
     * translate values of these properties to JSON.
     */
    private static final ImmutableMap<String, IPropertySerializer<?>> SERIALIZERS_FOR_PROPERTIES = ImmutableMap
            .<String, IPropertySerializer<?>> builder()
            .put(VisualFeaturesListJsonConverter.KEY_TEMPLATES,
                    new IPropertySerializer<ImmutableList<String>>() {

                        @Override
                        public void serializeProperty(
                                ImmutableList<String> value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeListOfStrings(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEMPLATES);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_GEOMETRY,
                    new IPropertySerializer<Geometry>() {

                        @Override
                        public void serializeProperty(Geometry value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeGeometry(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_GEOMETRY);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_COLOR,
                    new IPropertySerializer<SerializableColor>() {

                        @Override
                        public void serializeProperty(SerializableColor value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeColor(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_BORDER_COLOR);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_FILL_COLOR,
                    new IPropertySerializer<SerializableColor>() {

                        @Override
                        public void serializeProperty(SerializableColor value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeColor(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_FILL_COLOR);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_THICKNESS,
                    new IPropertySerializer<Double>() {

                        @Override
                        public void serializeProperty(Double value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeDouble(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_BORDER_THICKNESS);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_STYLE,
                    new IPropertySerializer<BorderStyle>() {

                        @Override
                        public void serializeProperty(BorderStyle value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeBorderStyle(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_BORDER_STYLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DIAMETER,
                    new IPropertySerializer<Double>() {

                        @Override
                        public void serializeProperty(Double value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeDouble(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_DIAMETER);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SYMBOL_SHAPE,
                    new IPropertySerializer<SymbolShape>() {

                        @Override
                        public void serializeProperty(SymbolShape value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeSymbolShape(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_SYMBOL_SHAPE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_LABEL,
                    new IPropertySerializer<String>() {

                        @Override
                        public void serializeProperty(String value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeString(value, generator, identifier,
                                    VisualFeaturesListJsonConverter.KEY_LABEL);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_LENGTH,
                    new IPropertySerializer<Double>() {

                        @Override
                        public void serializeProperty(Double value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeDouble(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_LENGTH);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_DIR,
                    new IPropertySerializer<Double>() {

                        @Override
                        public void serializeProperty(Double value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeDouble(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_DIR);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_SIZE,
                    new IPropertySerializer<Integer>() {

                        @Override
                        public void serializeProperty(Integer value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeInteger(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_SIZE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_COLOR,
                    new IPropertySerializer<SerializableColor>() {

                        @Override
                        public void serializeProperty(SerializableColor value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeColor(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_TEXT_COLOR);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DRAGGABILITY,
                    new IPropertySerializer<DragCapability>() {

                        @Override
                        public void serializeProperty(DragCapability value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeDragCapability(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_DRAGGABILITY);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_ROTATABLE,
                    new IPropertySerializer<Boolean>() {

                        @Override
                        public void serializeProperty(Boolean value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeBoolean(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_ROTATABLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SCALEABLE,
                    new IPropertySerializer<Boolean>() {

                        @Override
                        public void serializeProperty(Boolean value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeBoolean(
                                    value,
                                    generator,
                                    identifier,
                                    VisualFeaturesListJsonConverter.KEY_SCALEABLE);
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TOPMOST,
                    new IPropertySerializer<Boolean>() {

                        @Override
                        public void serializeProperty(Boolean value,
                                JsonGenerator generator, String identifier)
                                throws JsonGenerationException {
                            serializeBoolean(value, generator, identifier,
                                    VisualFeaturesListJsonConverter.KEY_TOPMOST);
                        }
                    }).build();

    /**
     * Immutable map pairing property names with the fetchers to be used to
     * fetch values for these properties.
     */
    private static final ImmutableMap<String, IPropertyFetcher<?>> FETCHERS_FOR_PROPERTIES = ImmutableMap
            .<String, IPropertyFetcher<?>> builder()
            .put(VisualFeaturesListJsonConverter.KEY_TEMPLATES,
                    new IPropertyFetcher<ImmutableList<String>>() {

                        @Override
                        public TemporallyVariantProperty<ImmutableList<String>> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            TemporallyVariantProperty<ImmutableList<VisualFeature>> features = visualFeature
                                    .getTemplates();
                            if (features == null) {
                                return null;
                            }
                            TemporallyVariantProperty<ImmutableList<String>> templates = new TemporallyVariantProperty<>(
                                    convertVisualFeaturesToIdentifiers(features
                                            .getDefaultProperty()));
                            for (Map.Entry<Range<Date>, ImmutableList<VisualFeature>> entry : features
                                    .getPropertiesForTimeRanges().entrySet()) {
                                templates.addPropertyForTimeRange(
                                        entry.getKey(),
                                        convertVisualFeaturesToIdentifiers(entry
                                                .getValue()));
                            }
                            return templates;
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_GEOMETRY,
                    new IPropertyFetcher<Geometry>() {

                        @Override
                        public TemporallyVariantProperty<Geometry> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getGeometry();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_COLOR,
                    new IPropertyFetcher<SerializableColor>() {

                        @Override
                        public TemporallyVariantProperty<SerializableColor> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getBorderColor();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_FILL_COLOR,
                    new IPropertyFetcher<SerializableColor>() {

                        @Override
                        public TemporallyVariantProperty<SerializableColor> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getFillColor();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_THICKNESS,
                    new IPropertyFetcher<Double>() {

                        @Override
                        public TemporallyVariantProperty<Double> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getBorderThickness();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_BORDER_STYLE,
                    new IPropertyFetcher<BorderStyle>() {

                        @Override
                        public TemporallyVariantProperty<BorderStyle> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getBorderStyle();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DIAMETER,
                    new IPropertyFetcher<Double>() {

                        @Override
                        public TemporallyVariantProperty<Double> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getDiameter();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SYMBOL_SHAPE,
                    new IPropertyFetcher<SymbolShape>() {

                        @Override
                        public TemporallyVariantProperty<SymbolShape> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getSymbolShape();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_LABEL,
                    new IPropertyFetcher<String>() {

                        @Override
                        public TemporallyVariantProperty<String> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getLabel();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_LENGTH,
                    new IPropertyFetcher<Double>() {

                        @Override
                        public TemporallyVariantProperty<Double> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getTextOffsetLength();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_OFFSET_DIR,
                    new IPropertyFetcher<Double>() {

                        @Override
                        public TemporallyVariantProperty<Double> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getTextOffsetDirection();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_SIZE,
                    new IPropertyFetcher<Integer>() {

                        @Override
                        public TemporallyVariantProperty<Integer> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getTextSize();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TEXT_COLOR,
                    new IPropertyFetcher<SerializableColor>() {

                        @Override
                        public TemporallyVariantProperty<SerializableColor> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getTextColor();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_DRAGGABILITY,
                    new IPropertyFetcher<DragCapability>() {

                        @Override
                        public TemporallyVariantProperty<DragCapability> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getDragCapability();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_ROTATABLE,
                    new IPropertyFetcher<Boolean>() {

                        @Override
                        public TemporallyVariantProperty<Boolean> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getRotatable();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_SCALEABLE,
                    new IPropertyFetcher<Boolean>() {

                        @Override
                        public TemporallyVariantProperty<Boolean> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getScaleable();
                        }
                    })
            .put(VisualFeaturesListJsonConverter.KEY_TOPMOST,
                    new IPropertyFetcher<Boolean>() {

                        @Override
                        public TemporallyVariantProperty<Boolean> fetchPropertyValue(
                                VisualFeature visualFeature) {
                            return visualFeature.getTopmost();
                        }
                    }).build();

    // Private Static Variables

    /**
     * Well-Known Text writer, used for serializing geometries. It is
     * thread-local because <code>WKTWriter</code> is not explicitly declared to
     * be thread-safe. This class's static methods may be called simultaneously
     * by multiple threads, and each thread must be able to serialize geometries
     * separately in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<WKTWriter> wktWriter = new ThreadLocal<WKTWriter>() {

        @Override
        protected WKTWriter initialValue() {
            return new WKTWriter();
        }
    };

    // Package Static Methods

    /**
     * Serialize the specified visual features list as JSON using the specified
     * generator.
     * 
     * @param visualFeatures
     *            Visual features list to be serialized.
     * @param jsonGenerator
     *            JSON generator.
     * @param provider
     *            Serializer provider.
     * @throws IOException
     *             If an error occurs when writing the nodes to the generator.
     * @throws JsonProcessingException
     *             If the JSON generation fails.
     */
    static void serialize(VisualFeaturesList visualFeatures,
            JsonGenerator jsonGenerator, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jsonGenerator.writeStartArray();
        for (VisualFeature visualFeature : visualFeatures) {
            serializeVisualFeature(visualFeature, jsonGenerator);
        }
        jsonGenerator.writeEndArray();
    }

    // Private Static Methods

    /**
     * Serialize the specified visual feature into JSON using the specified
     * generator.
     * 
     * @param visualFeature
     *            Visual feature to be serialized.
     * @param jsonGenerator
     *            JSON generator.
     * @throws IOException
     *             If an error occurs when writing the nodes to the generator.
     * @throws JsonProcessingException
     *             If the JSON generation fails.
     */
    @SuppressWarnings("unchecked")
    private static void serializeVisualFeature(VisualFeature visualFeature,
            JsonGenerator jsonGenerator) throws IOException,
            JsonProcessingException {

        /*
         * Start the creation of a JSON object to represent the visual feature.
         */
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField(
                VisualFeaturesListJsonConverter.KEY_IDENTIFIER,
                visualFeature.getIdentifier());

        jsonGenerator.writeStringField(
                VisualFeaturesListJsonConverter.KEY_VISIBILITY_CONSTRAINTS,
                visualFeature.getVisibilityConstraints().getDescription());

        for (Map.Entry<String, TypeToken<?>> entry : VisualFeaturesListJsonConverter.TYPES_FOR_PROPERTIES
                .entrySet()) {
            String name = entry.getKey();
            TypeToken<?> type = entry.getValue();
            IPropertyFetcher<?> fetcher = FETCHERS_FOR_PROPERTIES.get(name);
            IPropertySerializer<?> serializer = SERIALIZERS_FOR_PROPERTIES
                    .get(name);
            if (type.equals(VisualFeaturesListJsonConverter.TYPE_BOOLEAN)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<Boolean>) fetcher,
                        (IPropertySerializer<Boolean>) serializer);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_INTEGER)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<Integer>) fetcher,
                        (IPropertySerializer<Integer>) serializer);
            } else if (type.equals(VisualFeaturesListJsonConverter.TYPE_DOUBLE)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<Double>) fetcher,
                        (IPropertySerializer<Double>) serializer);
            } else if (type.equals(VisualFeaturesListJsonConverter.TYPE_STRING)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<String>) fetcher,
                        (IPropertySerializer<String>) serializer);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_LIST_OF_STRINGS)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<ImmutableList<String>>) fetcher,
                        (IPropertySerializer<ImmutableList<String>>) serializer);
            } else if (type.equals(VisualFeaturesListJsonConverter.TYPE_COLOR)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<SerializableColor>) fetcher,
                        (IPropertySerializer<SerializableColor>) serializer);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_GEOMETRY)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<Geometry>) fetcher,
                        (IPropertySerializer<Geometry>) serializer);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_BORDER_STYLE)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<BorderStyle>) fetcher,
                        (IPropertySerializer<BorderStyle>) serializer);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_SYMBOL_SHAPE)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<SymbolShape>) fetcher,
                        (IPropertySerializer<SymbolShape>) serializer);
            } else if (type
                    .equals(VisualFeaturesListJsonConverter.TYPE_DRAGGABILITY)) {
                fetchAndSerializeProperty(jsonGenerator, visualFeature, name,
                        (IPropertyFetcher<DragCapability>) fetcher,
                        (IPropertySerializer<DragCapability>) serializer);
            } else {

                /*
                 * This should never occur, but it is here to avoid having new
                 * properties added in the future cause silent serialization
                 * failures because the code was not modified to handle a new
                 * type of property.
                 */
                throw new IllegalStateException(
                        "internal error: property \""
                                + name
                                + "\" could not be serialized as it is of an unexpected type");
            }
        }

        /*
         * Close up and complete the JSON object.
         */
        jsonGenerator.writeEndObject();
    }

    /**
     * Convert the specified list of visual features to a list of the features'
     * identifiers.
     * 
     * @param features
     *            List of visual feature identifiers; may be <code>null</code>.
     * @return List of identifiers, or <code>null</code> if the specified list
     *         was <code>null</code>.
     */
    private static ImmutableList<String> convertVisualFeaturesToIdentifiers(
            List<VisualFeature> features) {
        List<String> identifiers = null;
        if (features != null) {
            identifiers = new ArrayList<>(features.size());
            for (VisualFeature feature : features) {
                identifiers.add(feature.getIdentifier());
            }
        }
        return ImmutableList.copyOf(identifiers);
    }

    /**
     * Fetch the specified property of the specified visual feature with the
     * type of its potential values given by <code>P</code> and serialize it
     * using the specified generator.
     * 
     * @param generator
     *            JSON generator used for the serialization.
     * @param visualFeature
     *            Visual feature for which a property is being serialized.
     * @param propertyName
     *            Name of the property that is being serialized.
     * @param fetcher
     *            Fetcher to be used to get the temporally variant property
     *            values that are to be serialized.
     * @param serializer
     *            Serializer to be used to actually serialize the fetched
     *            temporally variant property values.
     * @throws JsonGenerationException
     *             If an error occurs during deserialization.
     */
    private static <P extends Serializable> void fetchAndSerializeProperty(
            JsonGenerator generator, VisualFeature visualFeature,
            String propertyName, IPropertyFetcher<P> fetcher,
            IPropertySerializer<P> serializer) throws JsonGenerationException {

        /*
         * Get the temporally variant property value from the visual feature; if
         * none is returned, then this visual feature has no such property, and
         * nothing more needs to be done for this property name.
         */
        TemporallyVariantProperty<P> property = fetcher
                .fetchPropertyValue(visualFeature);
        if (property == null) {
            return;
        }

        /*
         * Serialize the property, since it is non-null.
         */
        String identifier = visualFeature.getIdentifier();
        try {

            /*
             * Put in the field name for the property.
             */
            generator.writeFieldName(propertyName);

            /*
             * Get the map of property values that actually depend upon where
             * the time falls within a range. If there are no such properties,
             * then serialize this property simply as the default value;
             * otherwise, serialize it as a JSON object with the keys being
             * string versions of the time ranges and the values being the
             * corresponding property values.
             */
            Map<Range<Date>, P> propertiesForTimeRanges = property
                    .getPropertiesForTimeRanges();
            if (propertiesForTimeRanges.isEmpty()) {

                /*
                 * Serialize the property value itself.
                 */
                serializer.serializeProperty(property.getDefaultProperty(),
                        generator, identifier);
            } else {

                /*
                 * Create a dictionary holding keys that are string versions of
                 * time ranges and/or the "default" entry, with corresponding
                 * values that are property values for those time ranges or for
                 * the default, respectively.
                 */
                generator.writeStartObject();
                P defaultProperty = property.getDefaultProperty();
                if (defaultProperty != null) {
                    generator
                            .writeFieldName(VisualFeaturesListJsonConverter.TEMPORALLY_VARIANT_KEY_DEFAULT);
                    serializer.serializeProperty(defaultProperty, generator,
                            identifier);
                }
                for (Map.Entry<Range<Date>, P> entry : propertiesForTimeRanges
                        .entrySet()) {
                    Range<Date> timeRange = entry.getKey();
                    generator.writeFieldName(timeRange.lowerEndpoint()
                            .getTime()
                            + " "
                            + timeRange.upperEndpoint().getTime());
                    serializer.serializeProperty(entry.getValue(), generator,
                            identifier);
                }
                generator.writeEndObject();
            }
        } catch (Exception e) {

            /*
             * This should never occur, but it is here to catch any unexpected
             * checked exceptions while writing a field name or the beginning or
             * end of a JSON object.
             */
            throw new IllegalStateException(
                    "internal error: property \""
                            + propertyName
                            + "\" could not be serialized due to an unexpected problem",
                    e);
        }
    }

    /**
     * Serialize the specified boolean.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeBoolean(Boolean value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            generator.writeBoolean(value);
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified integer.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeInteger(Integer value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            if (value.equals(VisualFeature.INTEGER_OF_EVENT_TYPE)) {
                generator
                        .writeString(VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE);
            } else {
                generator.writeNumber(value);
            }
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified double.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeDouble(Double value, JsonGenerator generator,
            String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            if (value.equals(VisualFeature.DOUBLE_OF_EVENT_TYPE)) {
                generator
                        .writeString(VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE);
            } else {
                generator.writeNumber(value);
            }
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified string.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeString(String value, JsonGenerator generator,
            String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            if (value.equals(VisualFeature.STRING_OF_EVENT_TYPE)) {
                generator
                        .writeString(VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE);
            } else {
                generator.writeString(value);
            }
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified list of strings.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeListOfStrings(List<String> value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            generator.writeStartArray();
            for (String string : value) {
                generator.writeString(string);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified color.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeColor(SerializableColor value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            if (value.equals(VisualFeature.COLOR_OF_EVENT_TYPE)) {
                generator
                        .writeString(VisualFeaturesListJsonConverter.PROPERTY_VALUE_EVENT_TYPE);
            } else {
                generator.writeObject(value);
            }
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified geometry.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeGeometry(Geometry value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            generator.writeString(wktWriter.get().write(value));
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified border style.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeBorderStyle(BorderStyle value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            generator.writeString(value.getDescription());
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified symbol shape.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeSymbolShape(SymbolShape value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            generator.writeString(value.getDescription());
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Serialize the specified drag capability.
     * 
     * @param value
     *            Item to be serialized.
     * @param generator
     *            JSON generator to be used for serialization.
     * @param identifier
     *            Identifier of the visual feature that is being serialized.
     * @param propertyName
     *            Name of the property for which a value is being serialized.
     * @throws JsonGenerationException
     *             If an error occurs during serialization.
     */
    private static void serializeDragCapability(DragCapability value,
            JsonGenerator generator, String identifier, String propertyName)
            throws JsonGenerationException {
        try {
            generator.writeString(value.getDescription());
        } catch (IOException e) {
            throw createValueSerializationException(value, identifier,
                    propertyName, e);
        }
    }

    /**
     * Create a property value serialization exception for the specified value
     * with the specified cause for the specified visual feature identifier.
     * 
     * @param identifier
     *            Identifier of the visual feature that could not be serialized.
     * @param propertyName
     *            Name of the property that could not be serialized.
     * @param cause
     *            Exception that signaled the problem, if any.
     * @return Created exception.
     */
    private static JsonGenerationException createValueSerializationException(
            Object value, String identifier, String propertyName,
            Throwable cause) {
        return new JsonGenerationException("visual feature \"" + identifier
                + "\": could not serialize \"" + value + "\" for property \""
                + propertyName + "\"", cause);
    }
}
