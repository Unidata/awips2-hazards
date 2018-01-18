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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Range;

import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator.ByteOrder;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryBinaryTranslator;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature.SerializableColor;

/**
 * Description: Helper class for {@link VisualFeaturesList} providing methods to
 * deserialize arrays of bytes into instances of {@link VisualFeaturesList}.
 * <p>
 * Note that the methods in this class are thread-safe, as thread-local storage
 * is used for recording information during the deserialization process. The
 * same thread must, of course, perform the entire deserialization process for a
 * given instance of <code>VisualFeaturesList</code>.
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class VisualFeaturesListBinaryDeserializer
        extends VisualFeaturesListDeserializer {

    // Package-Private Static Methods

    /**
     * Deserialize a visual features list from an array of bytes from the
     * specified input stream.
     * 
     * @param bytesInputStream
     *            Byte aray input stream from which to take the array of bytes
     *            to be deserialized into the visual features list.
     * @return Deserialized visual features list.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    @SuppressWarnings("unchecked")
    static VisualFeaturesList deserialize(ByteArrayInputStream bytesInputStream)
            throws IOException {

        /*
         * Read the length of the list, and create the list with that capacity,
         * or return nothing if the length is 0.
         */
        int length = PrimitiveAndStringBinaryTranslator
                .readInteger(bytesInputStream, ByteOrder.BIG_ENDIAN);
        if (length == 0) {
            return null;
        }
        VisualFeaturesList visualFeatures = new VisualFeaturesList(length);

        /*
         * Prepare for the deserialization.
         */
        prepareForDeserialization();

        /*
         * For now, simply read and discard the serialized version number, since
         * there is only one such version.
         */
        PrimitiveAndStringBinaryTranslator.readShort(bytesInputStream,
                ByteOrder.BIG_ENDIAN);

        /*
         * Iterate through the serialized visual features, reading in each one
         * and creating it.
         */
        for (int index = 0; index < length; index++) {

            /*
             * Read the identifier and ensure it is valid and unique, then
             * create the visual feature.
             */
            String identifier = PrimitiveAndStringBinaryTranslator
                    .readString(bytesInputStream, ByteOrder.BIG_ENDIAN);
            if (identifier.isEmpty()) {
                throw new IOException(
                        "expected valid unique string identifier for visual feature at index "
                                + index + " but got empty string");
            } else if (isVisualFeatureIdentifierUnique(identifier) == false) {
                throw new IOException("identifier \"" + identifier + "\""
                        + "for visual feature at index " + index
                        + " not unique");
            }
            VisualFeature visualFeature = new VisualFeature(identifier);

            /*
             * Read the visiblity constraints and assign them.
             */
            try {
                visualFeature.setVisibilityConstraints(VisibilityConstraints
                        .getValueForOrdinal(PrimitiveAndStringBinaryTranslator
                                .readShort(bytesInputStream,
                                        ByteOrder.BIG_ENDIAN)));
            } catch (IOException e) {
                throw new IOException("unknown visibility constraints", e);
            }

            /*
             * Read the temporally variant template identifiers property, and
             * associate the template identifiers with the visual feature so
             * that they may be converted to actual templates later while
             * checking for any circular or unresolved template dependencies.
             */
            TemporallyVariantProperty<? extends List<String>> templateIdentifiers = (TemporallyVariantProperty<? extends List<String>>) deserializeProperty(
                    ArrayList.class, bytesInputStream);
            if (templateIdentifiers != null) {
                recordTemplateIdentifiersForVisualFeature(visualFeature,
                        templateIdentifiers);
            }

            /*
             * Read in the other temporally variant properties, assigning each
             * to the visual feature as they are deserialized.
             */
            visualFeature.setGeometry(deserializeProperty(
                    IAdvancedGeometry.class, bytesInputStream));
            visualFeature.setBorderColor(deserializeProperty(
                    SerializableColor.class, bytesInputStream));
            visualFeature.setFillColor(deserializeProperty(
                    SerializableColor.class, bytesInputStream));
            visualFeature.setBorderThickness(
                    deserializeProperty(Double.class, bytesInputStream));
            visualFeature.setBorderStyle(
                    deserializeProperty(BorderStyle.class, bytesInputStream));
            visualFeature.setFillStyle(
                    deserializeProperty(FillStyle.class, bytesInputStream));
            visualFeature.setDiameter(
                    deserializeProperty(Double.class, bytesInputStream));
            visualFeature.setSymbolShape(
                    deserializeProperty(SymbolShape.class, bytesInputStream));
            visualFeature.setLabel(
                    deserializeProperty(String.class, bytesInputStream));
            visualFeature.setTextOffsetLength(
                    deserializeProperty(Double.class, bytesInputStream));
            visualFeature.setTextOffsetDirection(
                    deserializeProperty(Double.class, bytesInputStream));
            visualFeature.setTextSize(
                    deserializeProperty(Integer.class, bytesInputStream));
            visualFeature.setTextColor(deserializeProperty(
                    SerializableColor.class, bytesInputStream));
            visualFeature.setDragCapability(deserializeProperty(
                    DragCapability.class, bytesInputStream));
            visualFeature.setMultiGeometryPointsDraggable(
                    deserializeProperty(Boolean.class, bytesInputStream));
            visualFeature.setEditableUsingGeometryOps(
                    deserializeProperty(Boolean.class, bytesInputStream));
            visualFeature.setRotatable(
                    deserializeProperty(Boolean.class, bytesInputStream));
            visualFeature.setScaleable(
                    deserializeProperty(Boolean.class, bytesInputStream));
            visualFeature.setTopmost(
                    deserializeProperty(Boolean.class, bytesInputStream));

            visualFeatures.add(visualFeature);
        }

        /*
         * Ensure that the visual features' specified templates do not lead to
         * any circular or unresolved dependencies, and set each of their
         * templates up to include references to one another (since the
         * templates were specified in the serialized object as identifiers).
         */
        try {
            setTemplatesForVisualFeatures(visualFeatures);
        } catch (DependencyException e) {
            throw new IOException("bad template dependency", e);
        }

        return visualFeatures;
    }

    // Private Static Methods

    /**
     * Deserialize a temporally variant property of the specified type from the
     * specified input stream.
     * 
     * @param propertyClass
     *            Type of the temporally variant property to be deserialized.
     * @param bytesInputStream
     *            Byte array input stream from which to deserialize the
     *            temporally variant property.
     * @return Temporally variant property, or <code>null</code> if none was
     *         found.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    private static <P extends Serializable> TemporallyVariantProperty<P> deserializeProperty(
            Class<P> propertyClass, ByteArrayInputStream bytesInputStream)
                    throws IOException {

        /*
         * Get the number of variant properties that make up the temporally
         * variant property to be deserialized. 0 indicates no properties; a
         * number greater than 0 indicates only range-specific properties, no
         * default property; and a number less than 0 indicates both
         * range-specific and default properties, with the absolute value of the
         * number being the number of range-specific properties plus 1.
         */
        int propertyVariantCount = PrimitiveAndStringBinaryTranslator
                .readShort(bytesInputStream, ByteOrder.BIG_ENDIAN);
        if (propertyVariantCount == 0) {
            return null;
        }

        /*
         * If there is a default property, deserialize it and determine how many
         * range-specific properties are to be deserialized.
         */
        P defaultProperty = null;
        if (propertyVariantCount < 0) {
            defaultProperty = deserializePropertyValue(propertyClass,
                    bytesInputStream);
            propertyVariantCount = Math.abs(propertyVariantCount) - 1;
        }

        /*
         * Create the property.
         */
        TemporallyVariantProperty<P> property = new TemporallyVariantProperty<>(
                defaultProperty);

        /*
         * For each range-specific property value to be found, deserialize a
         * range made up of two long integers, and then deserialize the property
         * value itself, and associate the two within the property.
         */
        for (int index = 0; index < propertyVariantCount; index++) {
            Range<Date> timeRange = Range.closedOpen(
                    new Date(PrimitiveAndStringBinaryTranslator
                            .readLong(bytesInputStream, ByteOrder.BIG_ENDIAN)),
                    new Date(PrimitiveAndStringBinaryTranslator
                            .readLong(bytesInputStream, ByteOrder.BIG_ENDIAN)));
            property.addPropertyForTimeRange(timeRange,
                    deserializePropertyValue(propertyClass, bytesInputStream));
        }

        return property;
    }

    /**
     * Deserialize a property value of the specified type from the specified
     * input stream.
     * 
     * @param propertyClass
     *            Type of the property to be deserialized.
     * @param bytesInputStream
     *            Byte array input stream from which to deserialize the property
     *            value.
     * @return Property value.
     * @throws IOException
     *             If a problem occurs during deserialization.
     */
    @SuppressWarnings("unchecked")
    private static <P extends Serializable> P deserializePropertyValue(
            Class<P> propertyClass, ByteArrayInputStream bytesInputStream)
                    throws IOException {

        /*
         * Deserialize the property value differently depending upon what its
         * type is.
         */
        if (Boolean.class.isAssignableFrom(propertyClass)) {
            return (P) Boolean.valueOf(PrimitiveAndStringBinaryTranslator
                    .readBoolean(bytesInputStream));
        } else if (Integer.class.isAssignableFrom(propertyClass)) {
            return (P) Integer.valueOf(PrimitiveAndStringBinaryTranslator
                    .readInteger(bytesInputStream, ByteOrder.BIG_ENDIAN));
        } else if (Double.class.isAssignableFrom(propertyClass)) {
            return (P) Double.valueOf(PrimitiveAndStringBinaryTranslator
                    .readDouble(bytesInputStream, ByteOrder.BIG_ENDIAN));
        } else if (String.class.isAssignableFrom(propertyClass)) {
            return (P) PrimitiveAndStringBinaryTranslator
                    .readString(bytesInputStream, ByteOrder.BIG_ENDIAN);
        } else if (BorderStyle.class.isAssignableFrom(propertyClass)) {
            try {
                return (P) BorderStyle
                        .getValueForOrdinal(PrimitiveAndStringBinaryTranslator
                                .readByte(bytesInputStream));
            } catch (IllegalArgumentException e) {
                throw new IOException("unknown border style", e);
            }
        } else if (FillStyle.class.isAssignableFrom(propertyClass)) {
            try {
                return (P) FillStyle
                        .getValueForOrdinal(PrimitiveAndStringBinaryTranslator
                                .readByte(bytesInputStream));
            } catch (IllegalArgumentException e) {
                throw new IOException("unknown fill style", e);
            }
        } else if (SymbolShape.class.isAssignableFrom(propertyClass)) {
            try {
                return (P) SymbolShape
                        .getValueForOrdinal(PrimitiveAndStringBinaryTranslator
                                .readByte(bytesInputStream));
            } catch (IllegalArgumentException e) {
                throw new IOException("unknown symbol shape", e);
            }
        } else if (DragCapability.class.isAssignableFrom(propertyClass)) {
            try {
                return (P) DragCapability
                        .getValueForOrdinal(PrimitiveAndStringBinaryTranslator
                                .readByte(bytesInputStream));
            } catch (IllegalArgumentException e) {
                throw new IOException("unknown drag capability", e);
            }
        } else if (List.class.isAssignableFrom(propertyClass)) {
            int length = PrimitiveAndStringBinaryTranslator
                    .readShort(bytesInputStream, ByteOrder.BIG_ENDIAN);
            List<String> list = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                list.add(PrimitiveAndStringBinaryTranslator
                        .readString(bytesInputStream, ByteOrder.BIG_ENDIAN));
            }
            return (P) list;
        } else if (SerializableColor.class.isAssignableFrom(propertyClass)) {
            boolean eventType = PrimitiveAndStringBinaryTranslator
                    .readBoolean(bytesInputStream);
            if (eventType) {
                return (P) VisualFeature.COLOR_OF_EVENT_TYPE;
            }
            return (P) new SerializableColor(
                    translateColorComponentToFloat(
                            PrimitiveAndStringBinaryTranslator
                                    .readByte(bytesInputStream)),
                    translateColorComponentToFloat(
                            PrimitiveAndStringBinaryTranslator
                                    .readByte(bytesInputStream)),
                    translateColorComponentToFloat(
                            PrimitiveAndStringBinaryTranslator
                                    .readByte(bytesInputStream)),
                    translateColorComponentToFloat(
                            PrimitiveAndStringBinaryTranslator
                                    .readByte(bytesInputStream)));
        } else if (IAdvancedGeometry.class.isAssignableFrom(propertyClass)) {
            return (P) AdvancedGeometryBinaryTranslator
                    .deserializeFromBinaryStream(bytesInputStream);
        } else {

            /*
             * This should never occur, but it is here to avoid having new
             * properties added in the future cause silent deserialization
             * failures because the code was not modified to handle a new type
             * of property.
             */
            throw new IllegalStateException(
                    "internal error: property of type \"" + propertyClass
                            + "\" could not be deserialized as it is of an unexpected type");
        }
    }

    /**
     * Get a value between 0.0 and 1.0 inclusive from the specified color
     * component value, with the value of 0 returning 0.0, 255 returning 1.0,
     * and anything in between returning something between 0.0 and 1.0.
     * 
     * @param value
     *            Value to be translated; must be between 0 and 255 inclusive.
     *            Values above or below this range will be silently truncated to
     *            the range.
     * @return Value translated to a float between 0.0 and 1.0 inclusive.
     */
    private static float translateColorComponentToFloat(short value) {
        if (value < 0) {
            value = 0;
        } else if (value > 255) {
            value = 255;
        }
        return (value) / 255.0f;
    }
}
