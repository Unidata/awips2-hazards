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

import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;

/**
 * Description: Visual feature, instances of which provide arbitrary drawable,
 * and optionally editable, elements for a spatial display that vary over time.
 * Each visual feature may reference other visual features by identifier and use
 * the them as templates, providing attribute values that are not explicitly
 * defined within the primary visual feature. Temporal variance means that an
 * instance may only be visible within a particular time range, or may have
 * different visual characteristics or geometries at different times. To get a
 * concrete representation of a visual feature at a particular point in time,
 * the
 * {@link #getStateAtTime(SpatialEntity, Object, boolean, Date, Color, double, BorderStyle, double, String, double, double, double, double, int)}
 * method is used to generate a {@link SpatialEntity} if appropriate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 12, 2016   15676    Chris.Golden Initial creation.
 * Mar 26, 2016   15676    Chris.Golden Added copy constructor, and a method
 *                                      to set the geometry for whichever time
 *                                      range encompasses a given timestamp.
 * Apr 05, 2016   15676    Chris.Golden Added toString() method for debugging.
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
 * Jun 23, 2016   19537    Chris.Golden Added ability to use "as event" as a
 *                                      value for label text. Also added new
 *                                      topmost and symbol shape properties.
 * Jul 25, 2015   19537    Chris.Golden Changed spatial entity creation methods
 *                                      to work with immutable entities; added
 *                                      fill style property; and added flag that
 *                                      allows multi-geometry visual features
 *                                      to avoid allowing dragging of their
 *                                      point sub-geometries even if drag
 *                                      capability would otherwise allow it.
 * Sep 12, 2016   15934    Chris.Golden Changed to use advanced geometries instead
 *                                      of JTS geometries. Also removed custom
 *                                      serialization/deserialization methods, as
 *                                      they are unneeded now that advanced
 *                                      geometries are being used (since it was
 *                                      the JTS geometries' presence that had
 *                                      necessitated their inclusion previously).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = VisualFeatureSerializationAdapter.class)
public class VisualFeature implements Serializable {

    // Private Static Constants

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = 9083255877893928066L;

    // Package-Private Static Classes

    /**
     * Serializable color.
     */
    static class SerializableColor extends Color implements Serializable {

        // Private Static Constants

        /**
         * Serialization version UID.
         */
        private static final long serialVersionUID = 5319722295972192334L;

        // Public Constructors

        /**
         * Construct a default instance.
         */
        public SerializableColor() {
        }

        /**
         * Construct an instance that is opaque.
         * 
         * @param red
         *            Red component.
         * @param green
         *            Green component.
         * @param blue
         *            Blue component.
         */
        public SerializableColor(float red, float green, float blue) {
            super(red, green, blue);
        }

        /**
         * Construct an instance.
         * 
         * @param red
         *            Red component.
         * @param green
         *            Green component.
         * @param blue
         *            Blue component.
         * @param alpha
         *            Alpha comnponent.
         */
        public SerializableColor(float red, float green, float blue, float alpha) {
            super(red, green, blue, alpha);
        }

        /**
         * Construct an instance based upon an existing color.
         * 
         * @param color
         *            Color to be copied.
         */
        public SerializableColor(Color color) {
            super(color.getRed(), color.getGreen(), color.getBlue(), color
                    .getAlpha());
        }

        // Private Methods

        /**
         * Write out the object for serialization purposes. This is required
         * because apparently just subclassing a non-serializable class like
         * {@link Color} with private fields, and not providing custom
         * serialization and deserialization, causes all the fields to be set to
         * <code>0.0</code>.
         * 
         * @param stream
         *            Stream to which to write out the object.
         * @throws IOException
         *             If the object cannot be written out.
         */
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.writeFloat(getRed());
            stream.writeFloat(getGreen());
            stream.writeFloat(getBlue());
            stream.writeFloat(getAlpha());
        }

        /**
         * Read in the object for deserialization purposes. This is required
         * because apparently just subclassing a non-serializable class like
         * {@link Color} with private fields, and not providing custom
         * serialization and deserialization, causes all the fields to be set to
         * <code>0.0</code>.
         * 
         * @param stream
         *            Stream from which to read in the object.
         * @throws IOException
         *             If the object cannot be read in.
         * @throws ClassNotFoundException
         *             If the class of a serialized object cannot be found.
         */
        private void readObject(ObjectInputStream stream) throws IOException,
                ClassNotFoundException {
            setRed(stream.readFloat());
            setGreen(stream.readFloat());
            setBlue(stream.readFloat());
            setAlpha(stream.readFloat());
        }

    }

    // Private Classes

    /**
     * Property fetcher, each instance of which is used to fetch particular
     * property's value from a specified visual feature at a particular time.
     */
    private interface IPropertyFetcher<P> {

        /**
         * Get the value of the property for the specified visual feature at the
         * specified time.
         * 
         * @param visualFeature
         *            Visual feature to be queried.
         * @param time
         *            Time for which to query the property value.
         */
        P getPropertyValue(VisualFeature visualFeature, Date time);
    }

    // Public Static Constants

    /**
     * Special color indicating that the event type's color should be used.
     */
    public static final SerializableColor COLOR_OF_EVENT_TYPE = new SerializableColor(
            -1.0f, -1.0f, -1.0f);

    /**
     * Special double value indicating that a value appropriate to an event type
     * should be used for a property.
     */
    public static final Double DOUBLE_OF_EVENT_TYPE = -1.0;

    /**
     * Special integer value indicating that a value appropriate to an event
     * type should be used for a property.
     */
    public static final Integer INTEGER_OF_EVENT_TYPE = -1;

    /**
     * Special string value indicating that a value appropriate to an event type
     * should be used for a property. The value is irrelevant, and is simply
     * something that is extremely unlikely to ever be used as a label by a
     * visual feature.
     */
    public static final String STRING_OF_EVENT_TYPE = "!#@$@#$65!#%@%^5623542354^`DAQW%$%afd";

    /**
     * Default border color.
     */
    public static final SerializableColor DEFAULT_BORDER_COLOR = new SerializableColor(
            1.0f, 1.0f, 1.0f);

    /**
     * Default fill color.
     */
    public static final SerializableColor DEFAULT_FILL_COLOR = new SerializableColor(
            0.0f, 0.0f, 0.0f, 0.0f);

    /**
     * Default border thickness.
     */
    public static final double DEFAULT_BORDER_THICKNESS = 1.0;

    /**
     * Default border style.
     */
    public static final BorderStyle DEFAULT_BORDER_STYLE = BorderStyle.SOLID;

    /**
     * Default fill style.
     */
    public static final FillStyle DEFAULT_FILL_STYLE = FillStyle.SOLID;

    /**
     * Default diameter.
     */
    public static final double DEFAULT_DIAMETER = 5.0;

    /**
     * Default symbol shape.
     */
    public static final SymbolShape DEFAULT_SYMBOL_SHAPE = SymbolShape.CIRCLE;

    /**
     * Default text offset length.
     */
    public static final double DEFAULT_TEXT_OFFSET_LENGTH = 0.0;

    /**
     * Default text offset direction.
     */
    public static final double DEFAULT_TEXT_OFFSET_DIRECTION = 90.0;

    /**
     * Default text size.
     */
    public static final int DEFAULT_TEXT_SIZE = 10;

    /**
     * Default text color.
     */
    public static final SerializableColor DEFAULT_TEXT_COLOR = new SerializableColor(
            1.0f, 1.0f, 1.0f);

    /**
     * Default drag capability.
     */
    public static final DragCapability DEFAULT_DRAG_CAPABILITY = DragCapability.NONE;

    /**
     * Default draggable flag for point geometries that are part of collections
     * of multiple geometries.
     */
    public static final boolean DEFAULT_MULTI_GEOMETRY_POINTS_DRAGGABLE = true;

    /**
     * Default rotatable flag.
     */
    public static final boolean DEFAULT_ROTATABLE = false;

    /**
     * Default scaleable flag.
     */
    public static final boolean DEFAULT_SCALEABLE = false;

    /**
     * Default topmost flag.
     */
    public static final boolean DEFAULT_TOPMOST = false;

    // Private Static Constants

    /**
     * Geometry property fetcher.
     */
    private static final IPropertyFetcher<IAdvancedGeometry> GEOMETRY_FETCHER = new IPropertyFetcher<IAdvancedGeometry>() {

        @Override
        public IAdvancedGeometry getPropertyValue(VisualFeature visualFeature,
                Date time) {
            TemporallyVariantProperty<IAdvancedGeometry> property = visualFeature
                    .getGeometry();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Border color fetcher.
     */
    private static final IPropertyFetcher<SerializableColor> BORDER_COLOR_FETCHER = new IPropertyFetcher<SerializableColor>() {

        @Override
        public SerializableColor getPropertyValue(VisualFeature visualFeature,
                Date time) {
            TemporallyVariantProperty<SerializableColor> property = visualFeature
                    .getBorderColor();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Fill color fetcher.
     */
    private static final IPropertyFetcher<SerializableColor> FILL_COLOR_FETCHER = new IPropertyFetcher<SerializableColor>() {

        @Override
        public SerializableColor getPropertyValue(VisualFeature visualFeature,
                Date time) {
            TemporallyVariantProperty<SerializableColor> property = visualFeature
                    .getFillColor();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Border thickness fetcher.
     */
    private static final IPropertyFetcher<Double> BORDER_THICKNESS_FETCHER = new IPropertyFetcher<Double>() {

        @Override
        public Double getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Double> property = visualFeature
                    .getBorderThickness();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Border style fetcher.
     */
    private static final IPropertyFetcher<BorderStyle> BORDER_STYLE_FETCHER = new IPropertyFetcher<BorderStyle>() {

        @Override
        public BorderStyle getPropertyValue(VisualFeature visualFeature,
                Date time) {
            TemporallyVariantProperty<BorderStyle> property = visualFeature
                    .getBorderStyle();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Fill style fetcher.
     */
    private static final IPropertyFetcher<FillStyle> FILL_STYLE_FETCHER = new IPropertyFetcher<FillStyle>() {

        @Override
        public FillStyle getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<FillStyle> property = visualFeature
                    .getFillStyle();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Diameter fetcher.
     */
    private static final IPropertyFetcher<Double> DIAMETER_FETCHER = new IPropertyFetcher<Double>() {

        @Override
        public Double getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Double> property = visualFeature
                    .getDiameter();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Symbol shape fetcher.
     */
    private static final IPropertyFetcher<SymbolShape> SYMBOL_SHAPE_FETCHER = new IPropertyFetcher<SymbolShape>() {

        @Override
        public SymbolShape getPropertyValue(VisualFeature visualFeature,
                Date time) {
            TemporallyVariantProperty<SymbolShape> property = visualFeature
                    .getSymbolShape();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Text label fetcher.
     */
    private static final IPropertyFetcher<String> LABEL_FETCHER = new IPropertyFetcher<String>() {

        @Override
        public String getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<String> property = visualFeature
                    .getLabel();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Text offset length fetcher.
     */
    private static final IPropertyFetcher<Double> TEXT_OFFSET_LENGTH_FETCHER = new IPropertyFetcher<Double>() {

        @Override
        public Double getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Double> property = visualFeature
                    .getTextOffsetLength();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Text offset direction fetcher.
     */
    private static final IPropertyFetcher<Double> TEXT_OFFSET_DIRECTION_FETCHER = new IPropertyFetcher<Double>() {

        @Override
        public Double getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Double> property = visualFeature
                    .getTextOffsetDirection();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Text size fetcher.
     */
    private static final IPropertyFetcher<Integer> TEXT_SIZE_FETCHER = new IPropertyFetcher<Integer>() {

        @Override
        public Integer getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Integer> property = visualFeature
                    .getTextSize();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Text color fetcher.
     */
    private static final IPropertyFetcher<SerializableColor> TEXT_COLOR_FETCHER = new IPropertyFetcher<SerializableColor>() {

        @Override
        public SerializableColor getPropertyValue(VisualFeature visualFeature,
                Date time) {
            TemporallyVariantProperty<SerializableColor> property = visualFeature
                    .getTextColor();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Drag capability fetcher.
     */
    private static final IPropertyFetcher<DragCapability> DRAG_CAPABILITY_FETCHER = new IPropertyFetcher<DragCapability>() {

        @Override
        public DragCapability getPropertyValue(VisualFeature visualFeature,
                Date time) {
            TemporallyVariantProperty<DragCapability> property = visualFeature
                    .getDragCapability();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Rotatable flag fetcher.
     */
    private static final IPropertyFetcher<Boolean> ROTATABLE_FETCHER = new IPropertyFetcher<Boolean>() {

        @Override
        public Boolean getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Boolean> property = visualFeature
                    .getRotatable();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Draggable points within multi-geometry collection flag fetcher.
     */
    private static final IPropertyFetcher<Boolean> MULTI_GEOMETRY_POINTS_DRAGGABLE_FETCHER = new IPropertyFetcher<Boolean>() {

        @Override
        public Boolean getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Boolean> property = visualFeature
                    .getMultiGeometryPointsDraggable();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Scaleable flag fetcher.
     */
    private static final IPropertyFetcher<Boolean> SCALEABLE_FETCHER = new IPropertyFetcher<Boolean>() {

        @Override
        public Boolean getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Boolean> property = visualFeature
                    .getScaleable();
            return (property == null ? null : property.getProperty(time));
        }
    };

    /**
     * Topmost flag fetcher.
     */
    private static final IPropertyFetcher<Boolean> TOPMOST_FETCHER = new IPropertyFetcher<Boolean>() {

        @Override
        public Boolean getPropertyValue(VisualFeature visualFeature, Date time) {
            TemporallyVariantProperty<Boolean> property = visualFeature
                    .getTopmost();
            return (property == null ? null : property.getProperty(time));
        }
    };

    // Private Variables

    /**
     * Identifier of this feature.
     */
    private final String identifier;

    /**
     * Visibility constraints.
     */
    private VisibilityConstraints visibilityConstraints;

    /**
     * List of visual features to be treated as templates for this feature, in
     * the order in which they should be checked when querying for property
     * values. This list may be <code>null</code>.
     * <p>
     * <strong>Note</strong>: The visual features listed herein cannot cause
     * potential circular dependencies. Thus, none of them may be a reference to
     * this visual feature, nor may they be references to visual features that
     * via their <code>templates</code> lists, directly or indirectly reference
     * this visual feature. Thus, if feature A's list includes B, and feature
     * B's list includes C, and feature C's list includes A, that would
     * constitute a circular dependency.
     * </p>
     */
    private TemporallyVariantProperty<ImmutableList<VisualFeature>> templates;

    /**
     * Geometry; may be <code>null</code>.
     */
    private TemporallyVariantProperty<IAdvancedGeometry> geometry;

    /**
     * Border color; may be <code>null</code>.
     */
    private TemporallyVariantProperty<SerializableColor> borderColor;

    /**
     * Fill color; may be <code>null</code>.
     */
    private TemporallyVariantProperty<SerializableColor> fillColor;

    /**
     * Border thickness in pixels; may be <code>null</code>.
     */
    private TemporallyVariantProperty<Double> borderThickness;

    /**
     * Border style; may be <code>null</code>.
     */
    private TemporallyVariantProperty<BorderStyle> borderStyle;

    /**
     * Fill style; may be <code>null</code>.
     */
    private TemporallyVariantProperty<FillStyle> fillStyle;

    /**
     * Diameter in pixels; may be <code>null</code>.
     */
    private TemporallyVariantProperty<Double> diameter;

    /**
     * Symbol shape; may be <code>null</code>.
     */
    private TemporallyVariantProperty<SymbolShape> symbolShape;

    /**
     * Text label; may be <code>null</code>.
     */
    private TemporallyVariantProperty<String> label;

    /**
     * Text offset length in pixels; may be <code>null</code>.
     */
    private TemporallyVariantProperty<Double> textOffsetLength;

    /**
     * Text offset direction in degrees (with 0 being to the right, 90 above,
     * and so on); may be <code>null</code>.
     */
    private TemporallyVariantProperty<Double> textOffsetDirection;

    /**
     * Text size in points; may be <code>null</code>.
     */
    private TemporallyVariantProperty<Integer> textSize;

    /**
     * Text color; may be <code>null</code>.
     */
    private TemporallyVariantProperty<SerializableColor> textColor;

    /**
     * Drag capability; may be <code>null</code>.
     */
    private TemporallyVariantProperty<DragCapability> dragCapability;

    /**
     * Flag indicating whether or not the feature is rotatable; may be
     * <code>null</code>.
     */
    private TemporallyVariantProperty<Boolean> rotatable;

    /**
     * Flag indicating whether or not, if {@link #geometry} is a collection of
     * multiple geometries, any point sub-geometries within that collection are
     * draggable. If <code>false</code>, this overrides any capabilities
     * specified in {@link #dragCapability} for such points, but has no effect
     * on a <code>geometry</code> consisting of a single point. May be
     * <code>null</code>.
     */
    private TemporallyVariantProperty<Boolean> multiGeometryPointsDraggable;

    /**
     * Flag indicating whether or not the feature is scaleable; may be
     * <code>null</code>.
     */
    private TemporallyVariantProperty<Boolean> scaleable;

    /**
     * Flag indicating whether or not the feature is topmost; may be
     * <code>null</code>.
     */
    private TemporallyVariantProperty<Boolean> topmost;

    // Public Constructors

    /**
     * Construct a standard instance that is as copy of the specified visual
     * feature.
     * 
     * @param original
     *            Visual feature to be copied.
     */
    public VisualFeature(VisualFeature original) {

        /*
         * All immutable properties of the original feature are simply
         * referenced by this new object, since their immutability guarantees
         * that sharing the objects will not cause problems.
         */
        this.identifier = original.identifier;
        this.visibilityConstraints = original.visibilityConstraints;
        this.templates = original.templates;
        this.borderColor = original.borderColor;
        this.fillColor = original.fillColor;
        this.borderThickness = original.borderThickness;
        this.borderStyle = original.borderStyle;
        this.fillStyle = original.fillStyle;
        this.diameter = original.diameter;
        this.symbolShape = original.symbolShape;
        this.label = original.label;
        this.textOffsetLength = original.textOffsetLength;
        this.textOffsetDirection = original.textOffsetDirection;
        this.textSize = original.textSize;
        this.textColor = original.textColor;
        this.dragCapability = original.dragCapability;
        this.rotatable = original.rotatable;
        this.multiGeometryPointsDraggable = original.multiGeometryPointsDraggable;
        this.scaleable = original.scaleable;
        this.topmost = original.topmost;

        /*
         * Mutable properties must be copied down to the point where immutable
         * components are encountered. Geometries for all practical purposes
         * immutable.
         */
        this.geometry = new TemporallyVariantProperty<>(
                original.geometry.getDefaultProperty());
        for (Map.Entry<Range<Date>, IAdvancedGeometry> entry : original.geometry
                .getPropertiesForTimeRanges().entrySet()) {
            this.geometry.addPropertyForTimeRange(entry.getKey(),
                    entry.getValue());
        }
    }

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of this instance.
     */
    VisualFeature(String identifier) {
        this.identifier = identifier;
    }

    // Public Methods

    @Override
    public boolean equals(Object other) {
        if (other instanceof VisualFeature == false) {
            return false;
        }
        VisualFeature otherFeature = (VisualFeature) other;
        return (Utils.equal(identifier, otherFeature.identifier)
                && (visibilityConstraints == otherFeature.visibilityConstraints)
                && Utils.equal(templates, otherFeature.templates)
                && Utils.equal(geometry, otherFeature.geometry)
                && Utils.equal(borderColor, otherFeature.borderColor)
                && Utils.equal(fillColor, otherFeature.fillColor)
                && Utils.equal(borderThickness, otherFeature.borderThickness)
                && Utils.equal(borderStyle, otherFeature.borderStyle)
                && Utils.equal(fillStyle, otherFeature.fillStyle)
                && Utils.equal(diameter, otherFeature.diameter)
                && Utils.equal(symbolShape, otherFeature.symbolShape)
                && Utils.equal(label, otherFeature.label)
                && Utils.equal(textOffsetLength, otherFeature.textOffsetLength)
                && Utils.equal(textOffsetDirection,
                        otherFeature.textOffsetDirection)
                && Utils.equal(textSize, otherFeature.textSize)
                && Utils.equal(textColor, otherFeature.textColor)
                && Utils.equal(dragCapability, otherFeature.dragCapability)
                && Utils.equal(multiGeometryPointsDraggable,
                        otherFeature.multiGeometryPointsDraggable)
                && Utils.equal(rotatable, otherFeature.rotatable)
                && Utils.equal(scaleable, otherFeature.scaleable) && Utils
                    .equal(topmost, otherFeature.topmost));
    }

    @Override
    public int hashCode() {
        return (int) ((Utils.getHashCode(identifier)
                + Utils.getHashCode(visibilityConstraints)
                + Utils.getHashCode(templates) + Utils.getHashCode(geometry)
                + Utils.getHashCode(borderColor) + Utils.getHashCode(fillColor)
                + Utils.getHashCode(borderThickness)
                + Utils.getHashCode(borderStyle) + Utils.getHashCode(fillStyle)
                + Utils.getHashCode(diameter) + Utils.getHashCode(symbolShape)
                + Utils.getHashCode(label)
                + Utils.getHashCode(textOffsetLength)
                + Utils.getHashCode(textOffsetDirection)
                + Utils.getHashCode(textSize) + Utils.getHashCode(textColor)
                + Utils.getHashCode(dragCapability)
                + Utils.getHashCode(multiGeometryPointsDraggable)
                + Utils.getHashCode(rotatable) + Utils.getHashCode(scaleable) + Utils
                    .getHashCode(topmost)) % Integer.MAX_VALUE);
    }

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the visibility constraints.
     * 
     * @return Visibility constraints.
     */
    public VisibilityConstraints getVisibilityConstraints() {
        return visibilityConstraints;
    }

    /**
     * Get the geometry for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Geometry that applies for the specified time, or
     *         <code>null</code> if none applies.
     */
    public IAdvancedGeometry getGeometry(Date time) {
        return getValue(GEOMETRY_FETCHER, time, null);
    }

    /**
     * Get the border color for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Border color that applies for the specified time; may be
     *         {@link #COLOR_OF_EVENT_TYPE}, indicating that the border should
     *         be the color of the event type.
     */
    public Color getBorderColor(Date time) {
        return getValue(BORDER_COLOR_FETCHER, time, DEFAULT_BORDER_COLOR);
    }

    /**
     * Get the fill color for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Fill color that applies for the specified time; may be
     *         {@link #COLOR_OF_EVENT_TYPE}, indicating that the fill should be
     *         the color of the event type.
     */
    public Color getFillColor(Date time) {
        return getValue(FILL_COLOR_FETCHER, time, DEFAULT_FILL_COLOR);
    }

    /**
     * Get the border thickness in pixels for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Border thickness that applies for the specified time; may be
     *         {@link #DOUBLE_OF_EVENT_TYPE}, indicating that the event type's
     *         border thickness should be used.
     */
    public double getBorderThickness(Date time) {
        return getValue(BORDER_THICKNESS_FETCHER, time,
                DEFAULT_BORDER_THICKNESS);
    }

    /**
     * Get the border style for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Border style that applies for the specified time.
     */
    public BorderStyle getBorderStyle(Date time) {
        return getValue(BORDER_STYLE_FETCHER, time, DEFAULT_BORDER_STYLE);
    }

    /**
     * Get the fill style for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Fill style that applies for the specified time.
     */
    public FillStyle getFillStyle(Date time) {
        return getValue(FILL_STYLE_FETCHER, time, DEFAULT_FILL_STYLE);
    }

    /**
     * Get the diameter in pixels for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Diameter that applies for the specified time; may be
     *         {@link #DOUBLE_OF_EVENT_TYPE}, indicating that the event type's
     *         point diameter should be used.
     */
    public double getDiameter(Date time) {
        return getValue(DIAMETER_FETCHER, time, DEFAULT_DIAMETER);
    }

    /**
     * Get the symbol shape for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Symbol shape that applies for the specified time.
     */
    public SymbolShape getSymbolShape(Date time) {
        return getValue(SYMBOL_SHAPE_FETCHER, time, DEFAULT_SYMBOL_SHAPE);
    }

    /**
     * Get the text label for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Text label that applies for the specified time, if any; may be
     *         {@link #STRING_OF_EVENT_TYPE}, indicating that the label should
     *         be the same as what the hazard event would have.
     */
    public String getLabel(Date time) {
        return getValue(LABEL_FETCHER, time, null);
    }

    /**
     * Get the text offset length in pixels for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Text offset length that applies for the specified time; may be
     *         {@link #DOUBLE_OF_EVENT_TYPE}, indicating that the event type's
     *         text offset length be used.
     */
    public double getTextOffsetLength(Date time) {
        return getValue(TEXT_OFFSET_LENGTH_FETCHER, time,
                DEFAULT_TEXT_OFFSET_LENGTH);
    }

    /**
     * Get the text offset direction in degrees (with 0 being to the right, 90
     * above, and so on) for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Text offset direction that applies for the specified time; may be
     *         {@link #DOUBLE_OF_EVENT_TYPE}, indicating that the event type's
     *         text offset direction should be used.
     */
    public double getTextOffsetDirection(Date time) {
        return getValue(TEXT_OFFSET_DIRECTION_FETCHER, time,
                DEFAULT_TEXT_OFFSET_DIRECTION);
    }

    /**
     * Get the text size in pixels for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Text size that applies for the specified time; may be
     *         {@link #INTEGER_OF_EVENT_TYPE}, indicating that the event type's
     *         text size should be used.
     */
    public int getTextSize(Date time) {
        return getValue(TEXT_SIZE_FETCHER, time, DEFAULT_TEXT_SIZE);
    }

    /**
     * Get the text color for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Text color that applies for the specified time; may be
     *         {@link #COLOR_OF_EVENT_TYPE}, indicating that the text should be
     *         the color of the event type.
     */
    public Color getTextColor(Date time) {
        return getValue(TEXT_COLOR_FETCHER, time, DEFAULT_TEXT_COLOR);
    }

    /**
     * Get the drag capability for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return Drag capability that applies for the specified time.
     */
    public DragCapability getDragCapability(Date time) {
        return getValue(DRAG_CAPABILITY_FETCHER, time, DEFAULT_DRAG_CAPABILITY);
    }

    /**
     * Determine whether, if {@link #getGeometry(Date)} yields a collection of
     * multiple geometries, any point sub-geometries within that collection are
     * draggable for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return <code>true</code> if point sub-geometries within a multi-geometry
     *         collection returned by <code>getGeometry()</code> are draggable,
     *         <code>false</code> otherwise. If the latter, this overrides any
     *         capabilities specified in {@link #getDragCapability(Date)} for
     *         such points, but has no effect on a geometry consisting of a
     *         single point.
     */
    public boolean isMultiGeometryPointsDraggable(Date time) {
        return getValue(MULTI_GEOMETRY_POINTS_DRAGGABLE_FETCHER, time,
                DEFAULT_MULTI_GEOMETRY_POINTS_DRAGGABLE);
    }

    /**
     * Determine whether the feature is rotatable for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return True if the feature is rotatable at the specified time, otherwise
     *         false.
     */
    public boolean isRotatable(Date time) {
        return getValue(ROTATABLE_FETCHER, time, DEFAULT_ROTATABLE);
    }

    /**
     * Determine whether the feature is scaleable for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return True if the feature is scaleable at the specified time, otherwise
     *         false.
     */
    public boolean isScaleable(Date time) {
        return getValue(SCALEABLE_FETCHER, time, DEFAULT_SCALEABLE);
    }

    /**
     * Determine whether the feature is topmost for the specified time.
     * 
     * @param time
     *            Time for which to check.
     * @return True if the feature is topmost at the specified time, otherwise
     *         false.
     */
    public boolean isTopmost(Date time) {
        return getValue(TOPMOST_FETCHER, time, DEFAULT_TOPMOST);
    }

    /**
     * Get the state of this object as a spatial entity for the specified time,
     * with values found to be the appropriate <code>XXXX_OF_EVENT_TYPE</code>
     * constant for the parameter's type being replaced with the appropriate
     * specified hazard event visual property. For example, if the border color
     * is found to be {@link #COLOR_OF_EVENT_TYPE}, the color specified by
     * <code>hazardColor</code> is used.
     * <p>
     * A spatial entity may be provided when this method is invoked if the
     * caller wishes to reuse an existing object instead of building a new one.
     * If provided, the existing object is returned (assuming a spatial entity
     * is appropriate for the specified time) only if the entity that would have
     * been built would have been identical to the one provided.
     * <p>
     * 
     * @param spatialEntity
     *            Previously constructed spatial entity which, if sufficient to
     *            represent this visual feature at the specified time, is
     *            returned in lieu of building a new spatial entity. If
     *            <code>null</code>, a new spatial entity is built if the visual
     *            feature is visible at the specified time.
     * @param identifier
     *            Identifier of the spatial entity; if
     *            <code>spatialEntity</code> is not <code>null</code>, its
     *            {@link SpatialEntity#getIdentifier()} method must return a
     *            result equivalent to this object.
     * @param selected
     *            Flag indicating whether or not the item represented by this
     *            object is currently selected.
     * @param time
     *            Time for which to get the state of this object.
     * @param hazardColor
     *            Color of the hazard type associated with this visual feature;
     *            this is used if any of the visual feature's colors are found
     *            to be {@link #COLOR_OF_EVENT_TYPE}.
     * @param hazardBorderThickness
     *            Border thickness of the hazard type associated with this
     *            visual feature; this is used if the border thickness is found
     *            to be {@link #DOUBLE_OF_EVENT_TYPE}.
     * @param hazardBorderStyle
     *            Border style of the hazard type associated with this visual
     *            feature; this is used if the border style is found to be
     *            {@link BorderStyle#EVENT_TYPE}.
     * @param hazardPointDiameter
     *            Point diameter of the hazard type associated with this visual
     *            feature; this is used if the diameter is found to be
     *            {@link #DOUBLE_OF_EVENT_TYPE}.
     * @param hazardLabel
     *            Label of the hazard event associated with this visual feature;
     *            this is used if the label is found to be
     *            {@link #STRING_OF_EVENT_TYPE}.
     * @param hazardSinglePointTextOffsetLength
     *            Single-point text offset length of the hazard type associated
     *            with this visual feature; this is used if the text offset
     *            length is found to be {@link #DOUBLE_OF_EVENT_TYPE} and the
     *            geometry is a single point.
     * @param hazardSinglePointTextOffsetDirection
     *            Single-point text offset direction of the hazard type
     *            associated with this visual feature; this is used if the text
     *            offset direction is found to be {@link #DOUBLE_OF_EVENT_TYPE}
     *            and the geometry is a single point.
     * @param hazardMultiPointTextOffsetLength
     *            Multi-point text offset length of the hazard type associated
     *            with this visual feature; this is used if the text offset
     *            length is found to be {@link #DOUBLE_OF_EVENT_TYPE} and the
     *            geometry is comprised of multiple points.
     * @param hazardMultiPointTextOffsetDirection
     *            Multi-point text offset direction of the hazard type
     *            associated with this visual feature; this is used if the text
     *            offset direction is found to be {@link #DOUBLE_OF_EVENT_TYPE}
     *            and the geometry is comprised of multiple points.
     * @param hazardTextSize
     *            Text size of the hazard type associated with this visual
     *            feature; this is used if the text size is found to be
     *            {@link #INTEGER_OF_EVENT_TYPE}.
     * @return Spatial entity representing the state of this object at the
     *         specified time, or <code>null</code> if this object is not
     *         visible at that time. If a spatial entity is returned and the
     *         <code>spatialEntity</code> parameter was not <code>null</code>,
     *         the returned object may be the unaltered spatial entity so
     *         provided by the caller.
     */
    public <I> SpatialEntity<I> getStateAtTime(SpatialEntity<I> spatialEntity,
            I identifier, boolean selected, Date time, Color hazardColor,
            double hazardBorderThickness, BorderStyle hazardBorderStyle,
            double hazardPointDiameter, String hazardLabel,
            double hazardSinglePointTextOffsetLength,
            double hazardSinglePointTextOffsetDirection,
            double hazardMultiPointTextOffsetLength,
            double hazardMultiPointTextOffsetDirection, int hazardTextSize) {

        /*
         * If the visibility constraints do not allow this visual feature to be
         * visible, create nothing.
         */
        if ((visibilityConstraints == VisibilityConstraints.NEVER)
                || ((visibilityConstraints == VisibilityConstraints.UNSELECTED) && selected)
                || ((visibilityConstraints == VisibilityConstraints.SELECTED) && (selected == false))) {
            return null;
        }

        /*
         * If the geometry at this time is not visible create nothing.
         */
        IAdvancedGeometry geometry = getGeometry(time);
        if (geometry == null) {
            return null;
        }

        /*
         * Build a spatial entity, or reuse the one provided if its properties
         * match the ones specified by this visual feature at the given time.
         */
        double textOffsetLength = getTextOffsetLength(time);
        double textOffsetDirection = getTextOffsetDirection(time);
        return SpatialEntity.build(
                spatialEntity,
                identifier,
                geometry,
                getColor(getBorderColor(time), hazardColor),
                getColor(getFillColor(time), hazardColor),
                getDouble(getBorderThickness(time), hazardBorderThickness),
                getBorderStyle(getBorderStyle(time), hazardBorderStyle),
                getFillStyle(time),
                getDouble(getDiameter(time), hazardPointDiameter),
                getSymbolShape(time),
                getString(getLabel(time), hazardLabel),
                getDouble(textOffsetLength, hazardSinglePointTextOffsetLength),
                getDouble(textOffsetDirection,
                        hazardSinglePointTextOffsetDirection),
                getDouble(textOffsetLength, hazardMultiPointTextOffsetLength),
                getDouble(textOffsetDirection,
                        hazardMultiPointTextOffsetDirection),
                getInteger(getTextSize(time), hazardTextSize),
                getColor(getTextColor(time), hazardColor),
                getDragCapability(time), isMultiGeometryPointsDraggable(time),
                isRotatable(time), isScaleable(time), isTopmost(time));
    }

    /**
     * Get the state of this object as a spatial entity for the specified time,
     * with values found to be the appropriate <code>XXXX_OF_EVENT_TYPE</code>
     * constant for the parameter's type being replaced with the default value
     * for that parameter. For example, if the border color is found to be
     * {@link #COLOR_OF_EVENT_TYPE}, then the color specified by
     * {@link #DEFAULT_BORDER_COLOR} is used.
     * <p>
     * A spatial entity may be provided when this method is invoked if the
     * caller wishes to reuse an existing object instead of building a new one.
     * If provided, the existing object is returned (assuming a spatial entity
     * is appropriate for the specified time) only if the entity that would have
     * been built would have been identical to the one provided.
     * <p>
     * 
     * @param spatialEntity
     *            Previously constructed spatial entity which, if sufficient to
     *            represent this visual feature at the specified time, is
     *            returned in lieu of building a new spatial entity. If
     *            <code>null</code>, a new spatial entity is built if the visual
     *            feature is visible at the specified time.
     * @param identifier
     *            Identifier of the spatial entity; if
     *            <code>spatialEntity</code> is not <code>null</code>, its
     *            {@link SpatialEntity#getIdentifier()} method must return a
     *            result equivalent to this object.
     * @param time
     *            Time for which to get the state of this visual feature object.
     * @return Spatial entity representing the state of this object at the
     *         specified time, or <code>null</code> if this object is not
     *         visible at said time. If a spatial entity is returned and the
     *         <code>spatialEntity</code> parameter was not <code>null</code>,
     *         the returned object may be the unaltered spatial entity so
     *         provided by the caller.
     */
    public <I> SpatialEntity<I> getStateAtTime(SpatialEntity<I> spatialEntity,
            I identifier, Date time) {

        /*
         * If the geometry at this time is not visible create nothing.
         */
        IAdvancedGeometry geometry = getGeometry(time);
        if (geometry == null) {
            return null;
        }

        /*
         * Build a spatial entity, or reuse the one provided if its properties
         * match the ones specified by this visual feature at the given time.
         */
        double textOffsetLength = getDouble(getTextOffsetLength(time),
                DEFAULT_TEXT_OFFSET_LENGTH);
        double textOffsetDirection = getDouble(getTextOffsetDirection(time),
                DEFAULT_TEXT_OFFSET_DIRECTION);
        return SpatialEntity.build(spatialEntity, identifier, geometry,
                getColor(getBorderColor(time), DEFAULT_BORDER_COLOR),
                getColor(getFillColor(time), DEFAULT_FILL_COLOR),
                getDouble(getBorderThickness(time), DEFAULT_BORDER_THICKNESS),
                getBorderStyle(getBorderStyle(time), DEFAULT_BORDER_STYLE),
                getFillStyle(time),
                getDouble(getDiameter(time), DEFAULT_DIAMETER),
                getSymbolShape(time), getLabel(time), textOffsetLength,
                textOffsetDirection, textOffsetLength, textOffsetDirection,
                getInteger(getTextSize(time), DEFAULT_TEXT_SIZE),
                getColor(getTextColor(time), DEFAULT_TEXT_COLOR),
                getDragCapability(time), isMultiGeometryPointsDraggable(time),
                isRotatable(time), isScaleable(time), isTopmost(time));
    }

    /**
     * Set the geometry for the specified time. Note that this method only
     * succeeds in setting the geometry if this object has a geometry for a time
     * range encompassing the given time; if it does not, but a template from
     * which this object inherits geometry does, this method will fail.
     * 
     * @param time
     *            Time for which to set the geometry of this object.
     * @param geometry
     *            New geometry to be used.
     * @return True if the geometry was set successfully, false otherwise. The
     *         latter will occur if <code>time</code> does not fall within a
     *         time range for which a geometry has already been defined.
     */
    public boolean setGeometry(Date time, IAdvancedGeometry geometry) {
        return this.geometry.addPropertyForTimeRangeEncompassingTime(time,
                geometry);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(identifier);
        stringBuilder.append(" [selection-based visibility: ");
        stringBuilder.append(visibilityConstraints.getDescription());
        stringBuilder.append("] (");
        boolean never = false;
        if (geometry != null) {
            if (geometry.getDefaultProperty() != null) {
                stringBuilder.append("always visible");
            } else {
                Map<Range<Date>, IAdvancedGeometry> geometriesForTimeRanges = geometry
                        .getPropertiesForTimeRanges();
                if (geometriesForTimeRanges.isEmpty()) {
                    never = true;
                } else {
                    stringBuilder.append("visible ");
                    boolean first = true;
                    for (Range<Date> range : geometriesForTimeRanges.keySet()) {
                        if (first) {
                            first = false;
                        } else {
                            stringBuilder.append(", ");
                        }
                        stringBuilder.append(range.lowerEndpoint() + " to "
                                + range.upperEndpoint());
                    }
                }
            }
        } else {
            never = true;
        }
        if (never) {
            stringBuilder.append("never visible");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    // Package-Private Methods

    /**
     * Get the list of visual features to be treated as templates for this
     * feature, in the order in which they are to be checked when querying for
     * property values.
     * 
     * @return List of visual features; may be <code>null</code>.
     */
    TemporallyVariantProperty<ImmutableList<VisualFeature>> getTemplates() {
        return templates;
    }

    /**
     * Get the geometry.
     * 
     * @return Geometry; may be <code>null</code>.
     */
    TemporallyVariantProperty<IAdvancedGeometry> getGeometry() {
        return geometry;
    }

    /**
     * Get the border color.
     * 
     * @return Border color; may be <code>null</code>.
     */
    TemporallyVariantProperty<SerializableColor> getBorderColor() {
        return borderColor;
    }

    /**
     * Get the fill color.
     * 
     * @return Fill color; may be <code>null</code>.
     */
    TemporallyVariantProperty<SerializableColor> getFillColor() {
        return fillColor;
    }

    /**
     * Get the border thickness in pixels.
     * 
     * @return Border thickness; may be <code>null</code>.
     */
    TemporallyVariantProperty<Double> getBorderThickness() {
        return borderThickness;
    }

    /**
     * Get the border style.
     * 
     * @return Border style; may be <code>null</code>.
     */
    TemporallyVariantProperty<BorderStyle> getBorderStyle() {
        return borderStyle;
    }

    /**
     * Get the fill style.
     * 
     * @return Fill style; may be <code>null</code>.
     */
    TemporallyVariantProperty<FillStyle> getFillStyle() {
        return fillStyle;
    }

    /**
     * Get the diameter in pixels.
     * 
     * @return Diameter; may be <code>null</code>.
     */
    TemporallyVariantProperty<Double> getDiameter() {
        return diameter;
    }

    /**
     * Get the symbol shape.
     * 
     * @return Symbol shape; may be <code>null</code>.
     */
    TemporallyVariantProperty<SymbolShape> getSymbolShape() {
        return symbolShape;
    }

    /**
     * Get the text label.
     * 
     * @return Text label; may be <code>null</code>.
     */
    TemporallyVariantProperty<String> getLabel() {
        return label;
    }

    /**
     * Get the text offset length in pixels.
     * 
     * @return Text offset length; may be <code>null</code>.
     */
    TemporallyVariantProperty<Double> getTextOffsetLength() {
        return textOffsetLength;
    }

    /**
     * Get the text offset direction in degrees (with 0 being to the right, 90
     * above, and so on).
     * 
     * @return Text offset direction; may be <code>null</code>.
     */
    TemporallyVariantProperty<Double> getTextOffsetDirection() {
        return textOffsetDirection;
    }

    /**
     * Get the text size in points.
     * 
     * @return Text size; may be <code>null</code>.
     */
    TemporallyVariantProperty<Integer> getTextSize() {
        return textSize;
    }

    /**
     * Get the text color.
     * 
     * @return Text color; may be <code>null</code>.
     */
    TemporallyVariantProperty<SerializableColor> getTextColor() {
        return textColor;
    }

    /**
     * Get the drag capability.
     * 
     * @return Drag capability; may be <code>null</code>.
     */
    TemporallyVariantProperty<DragCapability> getDragCapability() {
        return dragCapability;
    }

    /**
     * Get the flag indicating whether, if {@link #getGeometry(Date)} yields a
     * collection of multiple geometries, any point sub-geometries within that
     * collection are draggable for the specified time.
     * 
     * @return Flag indicating whether or not point sub-geometries within a
     *         multi-geometry collection returned by <code>getGeometry()</code>
     *         are draggable. If the latter, this overrides any capabilities
     *         specified in {@link #getDragCapability(Date)} for such points,
     *         but has no effect on a geometry consisting of a single point. May
     *         be <code>null</code>.
     */
    TemporallyVariantProperty<Boolean> getMultiGeometryPointsDraggable() {
        return multiGeometryPointsDraggable;
    }

    /**
     * Get the flag indicating whether or not the feature is rotatable.
     * 
     * @return Flag indicating whether or not the feature is rotatable; may be
     *         <code>null</code>.
     */
    TemporallyVariantProperty<Boolean> getRotatable() {
        return rotatable;
    }

    /**
     * Get the flag indicating whether or not the feature is scaleable.
     * 
     * @return Flag indicating whether or not the feature is scaleable; may be
     *         <code>null</code>.
     */
    TemporallyVariantProperty<Boolean> getScaleable() {
        return scaleable;
    }

    /**
     * Get the flag indicating whether or not the feature is topmost.
     * 
     * @return Flag indicating whether or not the feature is topmost; may be
     *         <code>null</code>.
     */
    TemporallyVariantProperty<Boolean> getTopmost() {
        return topmost;
    }

    /**
     * Set the visibility constraints for this feature.
     */
    void setVisibilityConstraints(VisibilityConstraints visibilityConstraints) {
        this.visibilityConstraints = visibilityConstraints;
    }

    /**
     * Set the list of visual features to be treated as templates for this
     * feature, in the order in which they are to be checked first when querying
     * for property values.
     * 
     * @param templates
     *            New value; may be <code>null</code>.
     */
    void setTemplates(
            TemporallyVariantProperty<ImmutableList<VisualFeature>> templates) {
        this.templates = templates;
    }

    /**
     * Set the geometry.
     * 
     * @param geometry
     *            New value; may be <code>null</code>.
     */
    void setGeometry(TemporallyVariantProperty<IAdvancedGeometry> geometry) {
        this.geometry = geometry;
    }

    /**
     * Set the border color.
     * 
     * @param borderColor
     *            New value; may be <code>null</code>.
     */
    void setBorderColor(TemporallyVariantProperty<SerializableColor> borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Set the fill color.
     * 
     * @param fillColor
     *            New value; may be <code>null</code>.
     */
    void setFillColor(TemporallyVariantProperty<SerializableColor> fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Set the border thickness in pixels.
     * 
     * @param borderThickness
     *            New value; may be <code>null</code>.
     */
    void setBorderThickness(TemporallyVariantProperty<Double> borderThickness) {
        this.borderThickness = borderThickness;
    }

    /**
     * Set the border style.
     * 
     * @param borderStyle
     *            New value; may be <code>null</code>.
     */
    void setBorderStyle(TemporallyVariantProperty<BorderStyle> borderStyle) {
        this.borderStyle = borderStyle;
    }

    /**
     * Set the fill style.
     * 
     * @param fillStyle
     *            New value; may be <code>null</code>.
     */
    void setFillStyle(TemporallyVariantProperty<FillStyle> fillStyle) {
        this.fillStyle = fillStyle;
    }

    /**
     * Set the diameter in pixels.
     * 
     * @param diameter
     *            New value; may be <code>null</code>.
     */
    void setDiameter(TemporallyVariantProperty<Double> diameter) {
        this.diameter = diameter;
    }

    /**
     * Set the symbol shape.
     * 
     * @param symbolShape
     *            New value; may be <code>null</code>.
     */
    void setSymbolShape(TemporallyVariantProperty<SymbolShape> symbolShape) {
        this.symbolShape = symbolShape;
    }

    /**
     * Set the text label.
     * 
     * @param label
     *            New value; may be <code>null</code>.
     */
    void setLabel(TemporallyVariantProperty<String> label) {
        this.label = label;
    }

    /**
     * Set the text offset length in pixels.
     * 
     * @param textOffsetLength
     *            New value; may be <code>null</code>.
     */
    void setTextOffsetLength(TemporallyVariantProperty<Double> textOffsetLength) {
        this.textOffsetLength = textOffsetLength;
    }

    /**
     * Set the text offset direction in degrees (with 0 being to the right, 90
     * above, and so on).
     * 
     * @param textOffsetDirection
     *            New value; may be <code>null</code>.
     */
    void setTextOffsetDirection(
            TemporallyVariantProperty<Double> textOffsetDirection) {
        this.textOffsetDirection = textOffsetDirection;
    }

    /**
     * Set the text size in points.
     * 
     * @param textSize
     *            New value; may be <code>null</code>.
     */
    void setTextSize(TemporallyVariantProperty<Integer> textSize) {
        this.textSize = textSize;
    }

    /**
     * Set the text color.
     * 
     * @param textColor
     *            New value; may be <code>null</code>.
     */
    void setTextColor(TemporallyVariantProperty<SerializableColor> textColor) {
        this.textColor = textColor;
    }

    /**
     * Set the drag capability.
     * 
     * @param dragCapability
     *            New value; may be <code>null</code>.
     */
    void setDragCapability(
            TemporallyVariantProperty<DragCapability> dragCapability) {
        this.dragCapability = dragCapability;
    }

    /**
     * Set the flag indicating whether or not, if {@link #geometry} holds a
     * collection of multiple geometries, any point sub-geometries within that
     * collection are draggable. If <code>false</code>, this overrides any
     * capabilities specified in {@link #dragCapability} for such points, but
     * has no effect on a geometry consisting of a single point.
     * 
     * @param multiGeometryPointsDraggable
     *            New value; may be <code>null</code>.
     */
    void setMultiGeometryPointsDraggable(
            TemporallyVariantProperty<Boolean> multiGeometryPointsDraggable) {
        this.multiGeometryPointsDraggable = multiGeometryPointsDraggable;
    }

    /**
     * Set the flag indicating whether or not the feature is rotatable.
     * 
     * @param rotatable
     *            New value; may be <code>null</code>.
     */
    void setRotatable(TemporallyVariantProperty<Boolean> rotatable) {
        this.rotatable = rotatable;
    }

    /**
     * Set the flag indicating whether or not the feature is scaleable.
     * 
     * @param scaleable
     *            New value; may be <code>null</code>.
     */
    void setScaleable(TemporallyVariantProperty<Boolean> scaleable) {
        this.scaleable = scaleable;
    }

    /**
     * Set the flag indicating whether or not the feature is topmost.
     * 
     * @param topmost
     *            New value; may be <code>null</code>.
     */
    void setTopmost(TemporallyVariantProperty<Boolean> topmost) {
        this.topmost = topmost;
    }

    // Private Methods

    /**
     * Get a property value using the specified fetcher, checking first this
     * instance, then any templates. If the value is not found, use the
     * specified default.
     * 
     * @param propertyFetcher
     *            Property fetcher to be used.
     * @param time
     *            Time for which to get the property.
     * @param defaultProperty
     *            Default property.
     */
    private <P> P getValue(IPropertyFetcher<P> propertyFetcher, Date time,
            P defaultProperty) {

        /*
         * If the property is provided by this visual feature, use it.
         */
        P property = propertyFetcher.getPropertyValue(this, time);
        if (property != null) {
            return property;
        }

        /*
         * If no value was found for the property within this visual feature,
         * iterate through any templates provided, querying each in turn for the
         * property value, and using the first one that provides it.
         */
        if (templates != null) {
            List<VisualFeature> visualFeatures = templates.getProperty(time);
            if (visualFeatures != null) {
                for (VisualFeature visualFeature : visualFeatures) {
                    property = propertyFetcher.getPropertyValue(visualFeature,
                            time);
                    if (property != null) {
                        return property;
                    }
                }
            }
        }

        /*
         * If no property value was found anywhere, use the default value.
         */
        return defaultProperty;
    }

    /**
     * Get the specified integer value, unless said value is equal to
     * {@link #INTEGER_OF_EVENT_TYPE}, in which case get the specified hazard
     * type integer value.
     * 
     * @param value
     *            Value to be used, unless it is {@link #INTEGER_OF_EVENT_TYPE}.
     * @param hazardTypeValue
     *            Value to be used if <code>value</code> is
     *            {@link #INTEGER_OF_EVENT_TYPE}.
     * @return Value.
     */
    private int getInteger(Integer value, int hazardTypeValue) {
        return (value.equals(INTEGER_OF_EVENT_TYPE) ? hazardTypeValue : value);
    }

    /**
     * Get the specified double value, unless said value is equal to
     * {@link #DOUBLE_OF_EVENT_TYPE}, in which case get the specified hazard
     * type double value.
     * 
     * @param value
     *            Value to be used, unless it is {@link #DOUBLE_OF_EVENT_TYPE}.
     * @param hazardTypeValue
     *            Value to be used if <code>value</code> is
     *            {@link #DOUBLE_OF_EVENT_TYPE}.
     * @return Value.
     */
    private double getDouble(Double value, double hazardTypeValue) {
        return (value.equals(DOUBLE_OF_EVENT_TYPE) ? hazardTypeValue : value);
    }

    /**
     * Get the specified string, unless said value is equal to
     * {@link #STRING_OF_EVENT_TYPE}, in which case get the specified hazard
     * type string.
     * 
     * @param value
     *            Value to be used, unless it is {@link #STRING_OF_EVENT_TYPE}.
     * @param hazardTypeValue
     *            Value to be used if <code>value</code> is
     *            {@link #STRING_OF_EVENT_TYPE}.
     * @return Value.
     */
    private String getString(String value, String hazardTypeValue) {
        return (STRING_OF_EVENT_TYPE.equals(value) ? hazardTypeValue : value);
    }

    /**
     * Get the specified border style, unless said style is equal to
     * {@link BorderStyle#EVENT_TYPE}, in which case get the specified hazard
     * type border style.
     * 
     * @param value
     *            Border style to be used, unless it is
     *            {@link BorderStyle#EVENT_TYPE}.
     * @param hazardTypeValue
     *            Border style to be used if <code>value</code> is
     *            {@link BorderStyle#EVENT_TYPE}.
     * @return Border style.
     */
    private BorderStyle getBorderStyle(BorderStyle value,
            BorderStyle hazardTypeValue) {
        return (value == BorderStyle.EVENT_TYPE ? hazardTypeValue : value);
    }

    /**
     * Get the specified color, unless said color is equal to
     * {@link #COLOR_OF_EVENT_TYPE}, in which case get the specified hazard type
     * color.
     * 
     * @param color
     *            Color to be used, unless it is {@link #COLOR_OF_EVENT_TYPE}.
     * @param hazardTypeColor
     *            Color to be used if <code>color</code> is
     *            {@link #COLOR_OF_EVENT_TYPE}.
     * @return Color.
     */
    private Color getColor(Color color, Color hazardTypeColor) {
        return (color.equals(COLOR_OF_EVENT_TYPE) ? hazardTypeColor : color);
    }
}