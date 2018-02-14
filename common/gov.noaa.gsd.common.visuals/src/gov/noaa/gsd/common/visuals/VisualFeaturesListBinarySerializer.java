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
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator.ByteOrder;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryBinaryTranslator;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature.SerializableColor;

/**
 * Description: Helper class for {@link VisualFeaturesList} providing methods to
 * serialize instances of {@link VisualFeaturesList} into arrays of bytes.
 * <p>
 * Note that the methods in this class are thread-safe.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 10, 2017   28892    Chris.Golden Initial creation.
 * Jan 17, 2018   33428    Chris.Golden Added support for flag that indicates
 *                                      whether or not visual feature is
 *                                      editable via geometry operations.
 * Feb 02, 2018   26712    Chris.Golden Added bufferColor, bufferThickness, and
 *                                      useForCentering properties to visual
 *                                      features.
 * Feb 13, 2018   20595    Chris.Golden Changed to serialize and deserialize
 *                                      RGBA color components as floats, which is
 *                                      what they are, instead of trying to use
 *                                      one byte per float, which led to problems.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class VisualFeaturesListBinarySerializer extends VisualFeaturesListSerializer {

    // Package-Private Static Methods

    /**
     * Serialize the specified visual features list as an array of bytes into
     * the specified output stream.
     * 
     * @param visualFeatures
     *            Visual features list to be serialized.
     * @param outputStream
     *            Output stream into which to serialize the visual features list
     *            as an array of bytes.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    static void serialize(VisualFeaturesList visualFeatures,
            OutputStream outputStream) throws IOException {

        /*
         * If there is no visual features list, add 0 as the length of the list
         * and do nothing more.
         */
        if (visualFeatures == null) {
            PrimitiveAndStringBinaryTranslator.writeInteger(0, outputStream,
                    ByteOrder.BIG_ENDIAN);
            return;
        }

        /*
         * Add the length of the list.
         */
        PrimitiveAndStringBinaryTranslator.writeInteger(visualFeatures.size(),
                outputStream, ByteOrder.BIG_ENDIAN);

        /*
         * Add the visual feature class serialization version number, so that
         * deserializers will know what version they are dealing with.
         */
        PrimitiveAndStringBinaryTranslator.writeShort(
                VisualFeature.getClassSerializationVersionNumber(),
                outputStream, ByteOrder.BIG_ENDIAN);

        /*
         * Iterate through the visual features, adding each in turn.
         */
        for (VisualFeature visualFeature : visualFeatures) {

            /*
             * Add the identifier and the visibility constraints.
             */
            PrimitiveAndStringBinaryTranslator.writeString(
                    visualFeature.getIdentifier(), outputStream,
                    ByteOrder.BIG_ENDIAN);
            PrimitiveAndStringBinaryTranslator.writeShort(
                    (short) visualFeature.getVisibilityConstraints().ordinal(),
                    outputStream, ByteOrder.BIG_ENDIAN);

            /*
             * Add the temporally variant properties. Note that all the
             * properties are serialized in a fairly straightforward way except
             * for the templates, for which a temporally variant property
             * holding the templates identifiers instead of the templates
             * themselves is serialized.
             */
            serializeProperty(
                    getIdentifiersOfVisualFeatureTemplates(visualFeature),
                    outputStream);
            serializeProperty(visualFeature.getGeometry(), outputStream);
            serializeProperty(visualFeature.getBorderColor(), outputStream);
            serializeProperty(visualFeature.getBufferColor(), outputStream);
            serializeProperty(visualFeature.getFillColor(), outputStream);
            serializeProperty(visualFeature.getBorderThickness(), outputStream);
            serializeProperty(visualFeature.getBufferThickness(), outputStream);
            serializeProperty(visualFeature.getBorderStyle(), outputStream);
            serializeProperty(visualFeature.getFillStyle(), outputStream);
            serializeProperty(visualFeature.getDiameter(), outputStream);
            serializeProperty(visualFeature.getSymbolShape(), outputStream);
            serializeProperty(visualFeature.getLabel(), outputStream);
            serializeProperty(visualFeature.getTextOffsetLength(),
                    outputStream);
            serializeProperty(visualFeature.getTextOffsetDirection(),
                    outputStream);
            serializeProperty(visualFeature.getTextSize(), outputStream);
            serializeProperty(visualFeature.getTextColor(), outputStream);
            serializeProperty(visualFeature.getDragCapability(), outputStream);
            serializeProperty(visualFeature.getMultiGeometryPointsDraggable(),
                    outputStream);
            serializeProperty(visualFeature.getEditableUsingGeometryOps(),
                    outputStream);
            serializeProperty(visualFeature.getRotatable(), outputStream);
            serializeProperty(visualFeature.getScaleable(), outputStream);
            serializeProperty(visualFeature.getUseForCentering(), outputStream);
            serializeProperty(visualFeature.getTopmost(), outputStream);
        }
    }

    /**
     * Serialize the specified temporally variant property to the specified
     * output stream.
     * 
     * @param property
     *            Temporally variant property; may be <code>null</code>.
     * @param outputStream
     *            Output stream to which to serialize the property.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    private static <P extends Serializable> void serializeProperty(
            TemporallyVariantProperty<P> property, OutputStream outputStream)
                    throws IOException {

        /*
         * Add a number indicating how many properties there are and of what
         * types (default versus range-specific). 0 indicates no properties; a
         * number greater than 0 indicates only range-specific properties, no
         * default property; and a number less than 0 indicates both
         * range-specific and default properties, with the absolute value of the
         * number being the number of range-specific properties plus 1.
         */
        if (property == null) {
            PrimitiveAndStringBinaryTranslator.writeShort((short) 0,
                    outputStream, ByteOrder.BIG_ENDIAN);
            return;
        }
        P defaultProperty = property.getDefaultProperty();
        Map<Range<Date>, P> propertiesForTimeRanges = property
                .getPropertiesForTimeRanges();
        PrimitiveAndStringBinaryTranslator.writeShort(
                (short) (defaultProperty == null
                        ? propertiesForTimeRanges.size()
                        : (propertiesForTimeRanges.size() + 1) * -1),
                outputStream, ByteOrder.BIG_ENDIAN);

        /*
         * Serialize the default property value if one exists.
         */
        if (defaultProperty != null) {
            serializePropertyValue(defaultProperty, outputStream);
        }

        /*
         * Iterate through the range-specific property values, serializing each
         * by first add the lower and upper boundaries as long integers, then
         * adding the property itself.
         */
        for (Map.Entry<Range<Date>, P> entry : propertiesForTimeRanges
                .entrySet()) {
            PrimitiveAndStringBinaryTranslator.writeLong(
                    entry.getKey().lowerEndpoint().getTime(), outputStream,
                    ByteOrder.BIG_ENDIAN);
            PrimitiveAndStringBinaryTranslator.writeLong(
                    entry.getKey().upperEndpoint().getTime(), outputStream,
                    ByteOrder.BIG_ENDIAN);
            serializePropertyValue(entry.getValue(), outputStream);
        }
    }

    /**
     * Serialize the specified property value to the specified output stream.
     * 
     * @param propertyValue
     *            Property value; must not be <code>null</code>.
     * @param outputStream
     *            Output stream to which to serialize the property.
     * @throws IOException
     *             If a problem occurs during serialization.
     */
    @SuppressWarnings("unchecked")
    private static <P extends Serializable> void serializePropertyValue(
            P propertyValue, OutputStream outputStream) throws IOException {

        /*
         * Serialize the property value differently depending upon what its type
         * is.
         */
        if (propertyValue instanceof Boolean) {
            PrimitiveAndStringBinaryTranslator
                    .writeBoolean((Boolean) propertyValue, outputStream);
        } else if (propertyValue instanceof Integer) {
            PrimitiveAndStringBinaryTranslator.writeInteger(
                    (Integer) propertyValue, outputStream,
                    ByteOrder.BIG_ENDIAN);
        } else if (propertyValue instanceof Double) {
            PrimitiveAndStringBinaryTranslator.writeDouble(
                    (Double) propertyValue, outputStream, ByteOrder.BIG_ENDIAN);
        } else if (propertyValue instanceof String) {
            PrimitiveAndStringBinaryTranslator.writeString(
                    (String) propertyValue, outputStream, ByteOrder.BIG_ENDIAN);
        } else if (propertyValue instanceof BorderStyle) {
            PrimitiveAndStringBinaryTranslator.writeByte(
                    (short) ((BorderStyle) propertyValue).ordinal(),
                    outputStream);
        } else if (propertyValue instanceof FillStyle) {
            PrimitiveAndStringBinaryTranslator.writeByte(
                    (short) ((FillStyle) propertyValue).ordinal(),
                    outputStream);
        } else if (propertyValue instanceof SymbolShape) {
            PrimitiveAndStringBinaryTranslator.writeByte(
                    (short) ((SymbolShape) propertyValue).ordinal(),
                    outputStream);
        } else if (propertyValue instanceof DragCapability) {
            PrimitiveAndStringBinaryTranslator.writeByte(
                    (short) ((DragCapability) propertyValue).ordinal(),
                    outputStream);
        } else if (propertyValue instanceof ImmutableList) {
            ImmutableList<String> list = (ImmutableList<String>) propertyValue;
            PrimitiveAndStringBinaryTranslator.writeShort((short) list.size(),
                    outputStream, ByteOrder.BIG_ENDIAN);
            for (String element : list) {
                PrimitiveAndStringBinaryTranslator.writeString(element,
                        outputStream, ByteOrder.BIG_ENDIAN);
            }
        } else if (propertyValue instanceof SerializableColor) {
            SerializableColor color = (SerializableColor) propertyValue;
            if (color.equals(VisualFeature.COLOR_OF_EVENT_TYPE)) {
                PrimitiveAndStringBinaryTranslator.writeBoolean(true,
                        outputStream);
            } else {
                PrimitiveAndStringBinaryTranslator.writeBoolean(false,
                        outputStream);
                PrimitiveAndStringBinaryTranslator.writeFloat(color.getRed(),
                        outputStream, ByteOrder.BIG_ENDIAN);
                PrimitiveAndStringBinaryTranslator.writeFloat(color.getGreen(),
                        outputStream, ByteOrder.BIG_ENDIAN);
                PrimitiveAndStringBinaryTranslator.writeFloat(color.getBlue(),
                        outputStream, ByteOrder.BIG_ENDIAN);
                PrimitiveAndStringBinaryTranslator.writeFloat(color.getAlpha(),
                        outputStream, ByteOrder.BIG_ENDIAN);
            }
        } else if (propertyValue instanceof IAdvancedGeometry) {
            AdvancedGeometryBinaryTranslator.serializeToBinaryStream(
                    (IAdvancedGeometry) propertyValue, outputStream);
        } else {

            /*
             * This should never occur, but it is here to avoid having new
             * properties added in the future cause silent serialization
             * failures because the code was not modified to handle a new type
             * of property.
             */
            throw new IllegalStateException(
                    "internal error: property of type \""
                            + propertyValue.getClass()
                            + "\" could not be serialized as it is of an unexpected type");
        }
    }
}
