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

import com.raytheon.uf.common.colormap.Color;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Puntal;

/**
 * Description: Spatial entity, instances of which provide arbitrary drawable,
 * and optionally editable, elements for a spatial display. The generic
 * parameter <code>I</code> provides the type of the entity's identifier.
 * <p>
 * Spatial entities may be created via the
 * {@link #build(SpatialEntity, Object, Geometry, Color, Color, double, BorderStyle, FillStyle, double, SymbolShape, String, double, double, double, double, int, Color, DragCapability, boolean, boolean, boolean, boolean)
 * build()} static method, or using one of the {@link VisualFeature} class's
 * <code>getStateAtTime()</code> static methods.
 * </p>
 * <p>
 * Spatial entities are designed to be immutable, and should be used as such.
 * This means that care must be taken to avoid changing underlying component
 * objects that may at some level be mutable, such as {@link Geometry} and
 * {@link Color}.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 10, 2016   15676    Chris.Golden Initial creation.
 * Jun 23, 2016   19537    Chris.Golden Added topmost and symbol shape properties.
 * Jul 25, 2016   19537    Chris.Golden Changed to provide a better build method;
 *                                      removed the modified flag; made immutable;
 *                                      added fill style property; and added flag
 *                                      that allows multi-geometry visual features
 *                                      to avoid allowing dragging of their point
 *                                      sub-geometries even if drag capability
 *                                      would otherwise allow it.
 * Aug 24, 2016   19537    Chris.Golden Fixed bug causing spatial entity to be
 *                                      needlessly recreated by build() method
 *                                      due to wrong return type for getTextSize().
 *                                      Also added toString() method.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SpatialEntity<I> {

    // Private Variables

    /**
     * Identifier of this entity.
     */
    private final I identifier;

    /**
     * Geometry.
     */
    private Geometry geometry;

    /**
     * Border color.
     */
    private Color borderColor;

    /**
     * Fill color.
     */
    private Color fillColor;

    /**
     * Border thickness in pixels.
     */
    private double borderThickness;

    /**
     * Border style.
     */
    private BorderStyle borderStyle;

    /**
     * Fill style.
     */
    private FillStyle fillStyle;

    /**
     * Diameter in pixels.
     */
    private double diameter;

    /**
     * Symbol shape.
     */
    private SymbolShape symbolShape;

    /**
     * Text label.
     */
    private String label;

    /**
     * Text offset length in pixels.
     */
    private double textOffsetLength;

    /**
     * Text offset direction in degrees (with 0 being to the right, 90 above,
     * and so on).
     */
    private double textOffsetDirection;

    /**
     * Text size in points.
     */
    private int textSize;

    /**
     * Text color.
     */
    private Color textColor;

    /**
     * Drag capability.
     */
    private DragCapability dragCapability;

    /**
     * Flag indicating whether or not, if {@link #geometry} is a collection of
     * multiple geometries, any {@link Puntal} sub-geometries within that
     * collection are draggable. If <code>false</code>, this overrides any
     * capabilities specified in {@link #dragCapability} for such points, but
     * has no effect on a <code>geometry</code> consisting of a single
     * <code>Puntal</code> object.
     */
    private boolean multiGeometryPointsDraggable;

    /**
     * Flag indicating whether or not the entity is rotatable.
     */
    private boolean rotatable;

    /**
     * Flag indicating whether or not the entity is scaleable.
     */
    private boolean scaleable;

    /**
     * Flag indicating whether or not the entity is topmost.
     */
    private boolean topmost;

    // Public Static Methods

    /**
     * Create a spatial entity to have the specified parameters.
     * 
     * @param spatialEntity
     *            Spatial entity which, if not <code>null</code>, is to be
     *            returned if it is equivalent to what a newly generated spatial
     *            entity with these parameters would be. If <code>null</code>, a
     *            new spatial entity will always be generated.
     * @param identifier
     *            Identifier of the spatial entity to be created, used if
     *            <code>spatialEntity</code> is specified as <code>null</code>
     *            when creating the new spatial entity. If
     *            <code>spatialEntity</code> is not <code>null</code>, this
     *            identifier must be the equivalent of that returned by the
     *            latter's {@link #getIdentifier()}.
     * @param geometry
     *            Geometry to be used.
     * @param borderColor
     *            Border color to be used.
     * @param fillColor
     *            Fill color to be used.
     * @param borderThickness
     *            Border thickness to be used.
     * @param borderStyle
     *            Border style to be used.
     * @param fillStyle
     *            Fill style to be used.
     * @param diameter
     *            Point diameter to be used, if the <code>geometry</code> is a
     *            point.
     * @param symbolShape
     *            Symbol shape to be used, if the <code>geometry</code> is a
     *            point.
     * @param label
     *            Label to be used.
     * @param singlePointTextOffsetLength
     *            Single-point text offset length, to be used if the
     *            <code>geometry</code> is a point.
     * @param singlePointTextOffsetDirection
     *            Single-point text offset direction, to be used if the
     *            <code>geometry</code> is a point.
     * @param multiPointTextOffsetLength
     *            Multi-point text offset length, to be used if the
     *            <code>geometry</code> is not a point.
     * @param multiPointTextOffsetDirection
     *            Multi-point text offset direction, to be used if the
     *            <code>geometry</code> is not a point.
     * @param textSize
     *            Text size to be used.
     * @param textColor
     *            Text color to be used.
     * @param dragCapability
     *            Drag capability to be used.
     * @param multiGeometryPointsDraggable
     *            Flag indicating whether or not, if <code>geometry</code> is a
     *            collection of multiple geometries, any {@link Puntal}
     *            sub-geometries within that collection are draggable. If
     *            <code>false</code>, this overrides any capabilities specified
     *            in <code>dragCapability</code> for such points, but has no
     *            effect on a <code>geometry</code> consisting of a single
     *            <code>Puntal</code> object.
     * @param rotatable
     *            Rotatable flag to be used.
     * @param scaleable
     *            Scaleable flag to be used.
     * @param topmost
     *            Topmost flag to be used.
     * @return Spatial entity with the specified parameters, either the one
     *         provided as an input parameter (if the latter was not
     *         <code>null</code> and was equivalent to the spatial entity that
     *         would have been generated with these parameters), or a newly
     *         created one.
     */
    public static <I> SpatialEntity<I> build(SpatialEntity<I> spatialEntity,
            I identifier, Geometry geometry, Color borderColor,
            Color fillColor, double borderThickness, BorderStyle borderStyle,
            FillStyle fillStyle, double diameter, SymbolShape symbolShape,
            String label, double singlePointTextOffsetLength,
            double singlePointTextOffsetDirection,
            double multiPointTextOffsetLength,
            double multiPointTextOffsetDirection, int textSize,
            Color textColor, DragCapability dragCapability,
            boolean multiGeometryPointsDraggable, boolean rotatable,
            boolean scaleable, boolean topmost) {

        /*
         * Remember the original, and create a new spatial entity if an original
         * was not provided.
         */
        SpatialEntity<I> original = spatialEntity;
        if (spatialEntity == null) {
            spatialEntity = new SpatialEntity<>(identifier);
        }

        /*
         * For each desired property value of the spatial entity, check to see
         * if the original has this value already, creating a new spatial entity
         * if not. If one has already been created in the course of this method,
         * it will simply be reused. Then set the property to the desired value.
         */
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getGeometry()), geometry);
        spatialEntity.setGeometry(geometry);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getBorderColor()),
                borderColor);
        spatialEntity.setBorderColor(borderColor);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getFillColor()), fillColor);
        spatialEntity.setFillColor(fillColor);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getBorderThickness()),
                borderThickness);
        spatialEntity.setBorderThickness(borderThickness);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getBorderStyle()),
                borderStyle);
        spatialEntity.setBorderStyle(borderStyle);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getFillStyle()), fillStyle);
        spatialEntity.setFillStyle(fillStyle);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getDiameter()), diameter);
        spatialEntity.setDiameter(diameter);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getSymbolShape()),
                symbolShape);
        spatialEntity.setSymbolShape(symbolShape);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getLabel()), label);
        spatialEntity.setLabel(label);
        boolean pointGeometry = (geometry.getGeometryN(0) instanceof Puntal);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getBorderColor()),
                borderColor);
        double textOffsetLength = (pointGeometry ? singlePointTextOffsetLength
                : multiPointTextOffsetLength);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getTextOffsetLength()),
                textOffsetLength);
        spatialEntity.setTextOffsetLength(textOffsetLength);
        double textOffsetDirection = (pointGeometry ? singlePointTextOffsetDirection
                : multiPointTextOffsetDirection);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getTextOffsetDirection()),
                textOffsetDirection);
        spatialEntity.setTextOffsetDirection(textOffsetDirection);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getTextSize()), textSize);
        spatialEntity.setTextSize(textSize);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getTextColor()), textColor);
        spatialEntity.setTextColor(textColor);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.getDragCapability()),
                dragCapability);
        spatialEntity.setDragCapability(dragCapability);
        spatialEntity = createIfNeeded(
                original,
                spatialEntity,
                (original == null ? null : original
                        .isMultiGeometryPointsDraggable()),
                multiGeometryPointsDraggable);
        spatialEntity
                .setMultiGeometryPointsDraggable(multiGeometryPointsDraggable);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.isRotatable()), rotatable);
        spatialEntity.setRotatable(rotatable);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.isScaleable()), scaleable);
        spatialEntity.setScaleable(scaleable);
        spatialEntity = createIfNeeded(original, spatialEntity,
                (original == null ? null : original.isTopmost()), topmost);
        spatialEntity.setTopmost(topmost);
        return spatialEntity;
    }

    // Private Static Methods

    /**
     * Create a spatial entity if needed based upon the given parameters. There
     * is such a need if:
     * <ul>
     * <li>the specified original and under-construction spatial entity
     * parameters are the same object; and</li>
     * <li>the specified first and second objects are not equivalent.</li>
     * </ul>
     * 
     * @param original
     *            Original spatial entity.
     * @param underConstruction
     *            Spatial entity currently under construction; this will be
     *            returned if either it does not reference the same object as
     *            <code>original</code>, or if it does, but <code>first</code>
     *            and <code>second</code> are equivalent.
     * @param first
     *            First object to be compared.
     * @param second
     *            Second object to be compared.
     * @return Spatial entity that was created, if needed, otherwise simply a
     *         reference to the provided under-construction spatial entity.
     */
    private static <I> SpatialEntity<I> createIfNeeded(
            SpatialEntity<I> original, SpatialEntity<I> underConstruction,
            Object first, Object second) {
        if ((original == underConstruction)
                && (Utils.equal(first, second) == false)) {
            underConstruction = new SpatialEntity<I>(original);
        }
        return underConstruction;
    }

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of this instance.
     */
    SpatialEntity(I identifier) {
        this.identifier = identifier;
    }

    // Private Constructors

    /**
     * Construct a copy instance.
     * 
     * @param other
     *            Spatial entity to be copied.
     */
    SpatialEntity(SpatialEntity<I> other) {
        identifier = other.identifier;
        geometry = other.geometry;
        borderColor = other.borderColor;
        fillColor = other.fillColor;
        borderThickness = other.borderThickness;
        borderStyle = other.borderStyle;
        fillStyle = other.fillStyle;
        diameter = other.diameter;
        symbolShape = other.symbolShape;
        label = other.label;
        textOffsetLength = other.textOffsetLength;
        textOffsetDirection = other.textOffsetDirection;
        textSize = other.textSize;
        textColor = other.textColor;
        dragCapability = other.dragCapability;
        multiGeometryPointsDraggable = other.multiGeometryPointsDraggable;
        rotatable = other.rotatable;
        scaleable = other.scaleable;
        topmost = other.topmost;
    }

    // Public Methods

    @Override
    public boolean equals(Object other) {
        if (other instanceof SpatialEntity == false) {
            return false;
        }
        SpatialEntity<?> otherEntity = (SpatialEntity<?>) other;
        return (Utils.equal(identifier, otherEntity.identifier)
                && Utils.equal(geometry, otherEntity.geometry)
                && Utils.equal(borderColor, otherEntity.borderColor)
                && Utils.equal(fillColor, otherEntity.fillColor)
                && (borderThickness == otherEntity.borderThickness)
                && Utils.equal(borderStyle, otherEntity.borderStyle)
                && Utils.equal(fillStyle, otherEntity.fillStyle)
                && (diameter == otherEntity.diameter)
                && Utils.equal(symbolShape, otherEntity.symbolShape)
                && Utils.equal(label, otherEntity.label)
                && (textOffsetLength == otherEntity.textOffsetLength)
                && (textOffsetDirection == otherEntity.textOffsetDirection)
                && (textSize == otherEntity.textSize)
                && Utils.equal(textColor, otherEntity.textColor)
                && Utils.equal(dragCapability, otherEntity.dragCapability)
                && (multiGeometryPointsDraggable == otherEntity.multiGeometryPointsDraggable)
                && (rotatable == otherEntity.rotatable)
                && (scaleable == otherEntity.scaleable) && (topmost == otherEntity.topmost));
    }

    @Override
    public int hashCode() {
        return (int) ((Utils.getHashCode(identifier)
                + Utils.getHashCode(geometry) + Utils.getHashCode(borderColor)
                + Utils.getHashCode(fillColor) + ((long) borderThickness)
                + Utils.getHashCode(borderStyle) + Utils.getHashCode(fillStyle)
                + ((long) diameter) + Utils.getHashCode(symbolShape)
                + Utils.getHashCode(label) + ((long) textOffsetLength)
                + ((long) textOffsetDirection) + (textSize)
                + Utils.getHashCode(textColor)
                + Utils.getHashCode(dragCapability)
                + (multiGeometryPointsDraggable ? 1L : 0L)
                + (rotatable ? 1L : 0L) + (scaleable ? 1L : 0L) + (topmost ? 1L
                    : 0L)) % Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public I getIdentifier() {
        return identifier;
    }

    /**
     * Get the geometry.
     * 
     * @return Geometry.
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Get the border color.
     * 
     * @return Border color.
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Get the fill color.
     * 
     * @return Fill color.
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Get the border thickness in pixels.
     * 
     * @return Border thickness
     */
    public double getBorderThickness() {
        return borderThickness;
    }

    /**
     * Get the border style.
     * 
     * @return Border style.
     */
    public BorderStyle getBorderStyle() {
        return borderStyle;
    }

    /**
     * Get the fill style.
     * 
     * @return Fill style.
     */
    public FillStyle getFillStyle() {
        return fillStyle;
    }

    /**
     * Get the diameter in pixels.
     * 
     * @return Diameter.
     */
    public double getDiameter() {
        return diameter;
    }

    /**
     * Get the symbol shape.
     * 
     * @return Symbol shape.
     */
    public SymbolShape getSymbolShape() {
        return symbolShape;
    }

    /**
     * Get the text label.
     * 
     * @return Text label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the text offset length in pixels.
     * 
     * @return Text offset length.
     */
    public double getTextOffsetLength() {
        return textOffsetLength;
    }

    /**
     * Get the text offset direction in degrees (with 0 being to the right, 90
     * above, and so on).
     * 
     * @return Text offset direction.
     */
    public double getTextOffsetDirection() {
        return textOffsetDirection;
    }

    /**
     * Get the text size in pixels.
     * 
     * @return Text size.
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Get the text color.
     * 
     * @return Text color.
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * Get the drag capability.
     * 
     * @return Drag capability.
     */
    public DragCapability getDragCapability() {
        return dragCapability;
    }

    /**
     * Determine whether, if {@link #getGeometry()} yields a collection of
     * multiple geometries, any {@link Puntal} sub-geometries within that
     * collection are draggable.
     * 
     * @return True if <code>Puntal</code> geometries within a multi-geometry
     *         collection returned by <code>getGeometry()</code> are draggable,
     *         false otherwise. If the latter, this overrides any capabilities
     *         specified in {@link #getDragCapability()} for such points, but
     *         has no effect on a geometry consisting of a single
     *         <code>Puntal</code> object.
     */
    public boolean isMultiGeometryPointsDraggable() {
        return multiGeometryPointsDraggable;
    }

    /**
     * Determine whether the entity is rotatable.
     * 
     * @return True if the entity is rotatable, otherwise false.
     */
    public boolean isRotatable() {
        return rotatable;
    }

    /**
     * Determine whether the entity is scaleable.
     * 
     * @return True if the entity is scaleable, otherwise false.
     */
    public boolean isScaleable() {
        return scaleable;
    }

    /**
     * Determine whether the entity is topmost.
     * 
     * @return True if the entity is topmost, otherwise false.
     */
    public boolean isTopmost() {
        return topmost;
    }

    // Private Methods

    /**
     * Set the geometry.
     * 
     * @param geometry
     *            New value.
     */
    private void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Set the border color.
     * 
     * @param borderColor
     *            New value.
     */
    private void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Set the fill color.
     * 
     * @param fillColor
     *            New value.
     */
    private void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Set the border thickness in pixels.
     * 
     * @param borderThickness
     *            New value.
     */
    private void setBorderThickness(double borderThickness) {
        this.borderThickness = borderThickness;
    }

    /**
     * Set the border style.
     * 
     * @param borderStyle
     *            New value.
     */
    private void setBorderStyle(BorderStyle borderStyle) {
        this.borderStyle = borderStyle;
    }

    /**
     * Set the fill style.
     * 
     * @param fillStyle
     *            New value.
     */
    private void setFillStyle(FillStyle fillStyle) {
        this.fillStyle = fillStyle;
    }

    /**
     * Set the diameter in pixels.
     * 
     * @param diameter
     *            New value.
     */
    private void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    /**
     * Set the symbol shape.
     * 
     * @param symbolShape
     *            New value.
     */
    private void setSymbolShape(SymbolShape symbolShape) {
        this.symbolShape = symbolShape;
    }

    /**
     * Set the text label.
     * 
     * @param label
     *            New value.
     */
    private void setLabel(String label) {
        this.label = label;
    }

    /**
     * Set the text offset length in pixels.
     * 
     * @param textOffsetLength
     *            New value.
     */
    private void setTextOffsetLength(double textOffsetLength) {
        this.textOffsetLength = textOffsetLength;
    }

    /**
     * Set the text offset direction in degrees (with 0 being to the right, 90
     * above, and so on).
     * 
     * @param textOffsetDirection
     *            New value.
     */
    private void setTextOffsetDirection(double textOffsetDirection) {
        this.textOffsetDirection = textOffsetDirection;
    }

    /**
     * Set the text size in points.
     * 
     * @param textSize
     *            New value.
     */
    private void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * Set the text color.
     * 
     * @param textColor
     *            New value.
     */
    private void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    /**
     * Set the drag capability.
     * 
     * @param dragCapability
     *            New value.
     */
    private void setDragCapability(DragCapability dragCapability) {
        this.dragCapability = dragCapability;
    }

    /**
     * Set the flag indicating whether, if {@link #getGeometry()} yields a
     * collection of multiple geometries, any {@link Puntal} sub-geometries
     * within that collection are draggable.
     * 
     * @param multiGeometryPointsDraggable
     *            Flag indicating whether or not <code>Puntal</code> geometries
     *            within a geometry collection are draggable. If
     *            <code>false</code>, this overrides any capabilities specified
     *            in {@link #getDragCapability()} for such points, but has no
     *            effect on a geometry consisting of a single
     *            <code>Puntal</code> object.
     */
    private void setMultiGeometryPointsDraggable(
            boolean multiGeometryPointsDraggable) {
        this.multiGeometryPointsDraggable = multiGeometryPointsDraggable;
    }

    /**
     * Set the flag indicating whether or not the feature is rotatable.
     * 
     * @param rotatable
     *            New value.
     */
    private void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    /**
     * Set the flag indicating whether or not the feature is scaleable.
     * 
     * @param scaleable
     *            New value.
     */
    private void setScaleable(boolean scaleable) {
        this.scaleable = scaleable;
    }

    /**
     * Set the flag indicating whether or not the feature is topmost.
     * 
     * @param topmost
     *            New value.
     */
    private void setTopmost(boolean topmost) {
        this.topmost = topmost;
    }
}