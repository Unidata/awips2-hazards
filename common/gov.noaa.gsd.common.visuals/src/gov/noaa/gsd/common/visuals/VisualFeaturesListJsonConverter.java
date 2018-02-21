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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.raytheon.uf.common.colormap.Color;

import gov.noaa.gsd.common.utilities.JtsJsonConversionModule;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;

/**
 * Description: Class providing methods allowing the conversion of
 * {@link VisualFeaturesList} instances to JSON and back.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 18, 2016   15676    Chris.Golden Initial creation.
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
 * Sep 12, 2016   15934    Chris.Golden Changed to work with advanced geometries
 *                                      now used by visual features.
 * Jan 17, 2018   33428    Chris.Golden Added support for flag that indicates
 *                                      whether or not visual feature is
 *                                      editable via geometry operations.
 * Feb 02, 2018   26712    Chris.Golden Added bufferColor, bufferThickness, and
 *                                      useForCentering properties to visual
 *                                      features.
 * Feb 21, 2018   46736    Chris.Golden Added persist flag to visual features.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisualFeaturesListJsonConverter {

    // Public Static Constants

    /**
     * Key for the identifier in a JSON object representing a visual feature.
     */
    public static final String KEY_IDENTIFIER = "identifier";

    /**
     * Key for the visibility constraints in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_VISIBILITY_CONSTRAINTS = "visibilityConstraints";

    /**
     * Key for the persist flag in a JSON object representing a visual feature.
     */
    public static final String KEY_PERSIST = "persist";

    /**
     * Key for the templates in a JSON object representing a visual feature.
     */
    public static final String KEY_TEMPLATES = "templates";

    /**
     * Key for the geometry in a JSON object representing a visual feature.
     */
    public static final String KEY_GEOMETRY = "geometry";

    /**
     * Key for the border color in a JSON object representing a visual feature.
     */
    public static final String KEY_BORDER_COLOR = "borderColor";

    /**
     * Key for the buffer color in a JSON object representing a visual feature.
     */
    public static final String KEY_BUFFER_COLOR = "bufferColor";

    /**
     * Key for the fill color in a JSON object representing a visual feature.
     */
    public static final String KEY_FILL_COLOR = "fillColor";

    /**
     * Key for the border thickness in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_BORDER_THICKNESS = "borderThickness";

    /**
     * Key for the buffer thickness in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_BUFFER_THICKNESS = "bufferThickness";

    /**
     * Key for the border style in a JSON object representing a visual feature.
     */
    public static final String KEY_BORDER_STYLE = "borderStyle";

    /**
     * Key for the fill style in a JSON object representing a visual feature.
     */
    public static final String KEY_FILL_STYLE = "fillStyle";

    /**
     * Key for the diameter in a JSON object representing a visual feature.
     */
    public static final String KEY_DIAMETER = "diameter";

    /**
     * Key for the symbol shape in a JSON object representing a visual feature.
     */
    public static final String KEY_SYMBOL_SHAPE = "symbolShape";

    /**
     * Key for the label in a JSON object representing a visual feature.
     */
    public static final String KEY_LABEL = "label";

    /**
     * Key for the text offset length in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_TEXT_OFFSET_LENGTH = "textOffsetLength";

    /**
     * Key for the text offset direction in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_TEXT_OFFSET_DIR = "textOffsetDirection";

    /**
     * Key for the text size in a JSON object representing a visual feature.
     */
    public static final String KEY_TEXT_SIZE = "textSize";

    /**
     * Key for the text color in a JSON object representing a visual feature.
     */
    public static final String KEY_TEXT_COLOR = "textColor";

    /**
     * Key for the drag capability in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_DRAGGABILITY = "dragCapability";

    /**
     * Key for the flag in a JSON object representing a visual feature that
     * indicates whether or not, if a visual feature's geometry is a collection
     * of multiple geometries, any point sub-geometries within that collection
     * are draggable.
     */
    public static final String KEY_MULTI_GEOMETRY_POINTS_DRAGGABLE = "multiGeometryPointsDraggable";

    /**
     * Key for the editable using geometry operations flag in a JSON object
     * representing a visual feature.
     */
    public static final String KEY_EDITABLE_USING_GEOMETRY_OPS = "editableUsingGeometryOps";

    /**
     * Key for the rotatable flag in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_ROTATABLE = "rotatable";

    /**
     * Key for the scaleable flag in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_SCALEABLE = "scaleable";

    /**
     * Key for the use for centering flag in a JSON object representing a visual
     * feature.
     */
    public static final String KEY_USE_FOR_CENTERING = "useForCentering";

    /**
     * Key for the topmost flag in a JSON object representing a visual feature.
     */
    public static final String KEY_TOPMOST = "topmost";

    /**
     * Key for the temporally variant property default value.
     */
    public static final String TEMPORALLY_VARIANT_KEY_DEFAULT = "default";

    /**
     * String that may be used in place of a color, double, or integer value
     * definition to indicate that a border, fill, or text color, border
     * thickness, diameter, text offset length, text offset direction, or text
     * size is to be the same as that of the type of the hazard associated with
     * the visual feature.
     */
    public static final String PROPERTY_VALUE_EVENT_TYPE = "eventType";

    /**
     * Pattern used to format and parse timestamps.
     */
    public static final String TIMESTAMP_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // Package-Private Static Constants

    /**
     * Type of a boolean.
     */
    static final TypeToken<?> TYPE_BOOLEAN = TypeToken.of(Boolean.class);

    /**
     * Type of an integer.
     */
    static final TypeToken<?> TYPE_INTEGER = TypeToken.of(Integer.class);

    /**
     * Type of a double.
     */
    static final TypeToken<?> TYPE_DOUBLE = TypeToken.of(Double.class);

    /**
     * Type of a string.
     */
    static final TypeToken<?> TYPE_STRING = TypeToken.of(String.class);

    /**
     * Type of a list of strings.
     */
    @SuppressWarnings("serial")
    static final TypeToken<?> TYPE_LIST_OF_STRINGS = new TypeToken<List<String>>() {
    };

    /**
     * Type of a color.
     */
    static final TypeToken<?> TYPE_COLOR = TypeToken.of(Color.class);

    /**
     * Type of a geometry.
     */
    static final TypeToken<?> TYPE_GEOMETRY = TypeToken
            .of(IAdvancedGeometry.class);

    /**
     * Type of a border style.
     */
    static final TypeToken<?> TYPE_BORDER_STYLE = TypeToken
            .of(BorderStyle.class);

    /**
     * Type of a fill style.
     */
    static final TypeToken<?> TYPE_FILL_STYLE = TypeToken.of(FillStyle.class);

    /**
     * Type of a symbol shape.
     */
    static final TypeToken<?> TYPE_SYMBOL_SHAPE = TypeToken
            .of(SymbolShape.class);

    /**
     * Type of a drag capability.
     */
    static final TypeToken<?> TYPE_DRAGGABILITY = TypeToken
            .of(DragCapability.class);

    /**
     * Immutable map pairing property names with the types of the corresponding
     * values.
     */
    static final ImmutableMap<String, TypeToken<?>> TYPES_FOR_PROPERTIES = ImmutableMap
            .<String, TypeToken<?>> builder()
            .put(KEY_TEMPLATES, TYPE_LIST_OF_STRINGS)
            .put(KEY_GEOMETRY, TYPE_GEOMETRY).put(KEY_BORDER_COLOR, TYPE_COLOR)
            .put(KEY_BUFFER_COLOR, TYPE_COLOR).put(KEY_FILL_COLOR, TYPE_COLOR)
            .put(KEY_BORDER_THICKNESS, TYPE_DOUBLE)
            .put(KEY_BUFFER_THICKNESS, TYPE_DOUBLE)
            .put(KEY_BORDER_STYLE, TYPE_BORDER_STYLE)
            .put(KEY_FILL_STYLE, TYPE_FILL_STYLE).put(KEY_DIAMETER, TYPE_DOUBLE)
            .put(KEY_SYMBOL_SHAPE, TYPE_SYMBOL_SHAPE)
            .put(KEY_LABEL, TYPE_STRING)
            .put(KEY_TEXT_OFFSET_LENGTH, TYPE_DOUBLE)
            .put(KEY_TEXT_OFFSET_DIR, TYPE_DOUBLE)
            .put(KEY_TEXT_SIZE, TYPE_INTEGER).put(KEY_TEXT_COLOR, TYPE_COLOR)
            .put(KEY_DRAGGABILITY, TYPE_DRAGGABILITY)
            .put(KEY_MULTI_GEOMETRY_POINTS_DRAGGABLE, TYPE_BOOLEAN)
            .put(KEY_EDITABLE_USING_GEOMETRY_OPS, TYPE_BOOLEAN)
            .put(KEY_ROTATABLE, TYPE_BOOLEAN).put(KEY_SCALEABLE, TYPE_BOOLEAN)
            .put(KEY_USE_FOR_CENTERING, TYPE_BOOLEAN)
            .put(KEY_TOPMOST, TYPE_BOOLEAN).build();

    /**
     * Object mapper used to convert visual feature lists to and from JSON.
     * (Note that this class <a href="http://wiki.fasterxml.com/JacksonFAQ">is
     * thread-safe</a> as long as it is configured here and not during use, and
     * thus is safe for multiple threads to use to serialize or deserialize
     * visual features simultaneously.)
     */
    static final ObjectMapper CONVERTER = new ObjectMapper();

    static {

        /*
         * Configure the converter to ignore unknown properties, and to include
         * in serialization all non-null members of an object.
         */
        CONVERTER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        CONVERTER.getSerializationConfig()
                .withSerializationInclusion(JsonInclude.Include.NON_NULL);

        /*
         * Configure the converter to serialize and deserialize objects expected
         * to be of type VisualFeaturesList using custom routines.
         */
        SimpleModule module = new SimpleModule("VisualFeaturesList",
                new Version(1, 0, 0, null, null, null));
        module.addSerializer(VisualFeaturesList.class,
                new JsonSerializer<VisualFeaturesList>() {

                    @Override
                    public void serialize(VisualFeaturesList value,
                            JsonGenerator jsonGenerator,
                            SerializerProvider provider) throws IOException,
                                    JsonProcessingException {
                        VisualFeaturesListJsonSerializer.serialize(value,
                                jsonGenerator, provider);
                    }
                });
        module.addDeserializer(VisualFeaturesList.class,
                new JsonDeserializer<VisualFeaturesList>() {

                    @Override
                    public VisualFeaturesList deserialize(JsonParser jsonParser,
                            DeserializationContext context) throws IOException,
                                    JsonProcessingException {
                        return VisualFeaturesListJsonDeserializer
                                .deserialize(jsonParser, context);
                    }
                });
        CONVERTER.registerModule(module);

        /*
         * Configure the converter to serialize and deserialize objects expected
         * to be of type Geometry using custom routines.
         */
        CONVERTER.registerModule(JtsJsonConversionModule.getInstance());
    };

    // Package-Private Static Variables

    /**
     * Date formatter used for parsing timestamp strings into dates. It is
     * thread-local because this class's static methods may be called
     * simultaneously by multiple threads, and according to the JDK
     * documentation, <code>SimpleDateFormat</code> is not thread-safe.
     */
    static final ThreadLocal<SimpleDateFormat> timestampFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(
                    VisualFeaturesListJsonConverter.TIMESTAMP_FORMAT_PATTERN);
        }
    };

    // Public Static Methods

    /**
     * Convert the specified JSON string into a visual features list.
     * 
     * @param json
     *            Text providing the JSON.
     * @return Visual features list.
     * @throws IOException
     *             If an error occurs when reading the nodes from the parser.
     * @throws JsonProcessingException
     *             If the JSON is malformed.
     */
    public static VisualFeaturesList fromJson(String json)
            throws IOException, JsonProcessingException {
        return CONVERTER.readValue(json, VisualFeaturesList.class);
    }

    /**
     * Convert the specified visual featuers list into a JSON string.
     * 
     * @param visualFeatures
     *            Visual features list.
     * @return JSON string.
     * @throws IOException
     *             If an error occurs when reading the nodes from the parser.
     * @throws JsonProcessingException
     *             If the JSON is malformed.
     */
    public static String toJson(VisualFeaturesList visualFeatures)
            throws IOException, JsonProcessingException {
        return CONVERTER.writeValueAsString(visualFeatures);
    }
}
