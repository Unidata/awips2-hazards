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

import com.raytheon.uf.common.colormap.Color;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Spatial entity, instances of which provide arbitrary drawable,
 * and optionally editable, elements for a spatial display. Note that any change
 * to the various properties of an instance marks the entity as modified. The
 * modified flag may be checked and, if true, reset using the
 * {@link #checkAndResetModified()} method. The generic parameter <code>I</code>
 * provides the type of the entity's identifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 10, 2016   15676    Chris.Golden Initial creation.
 * Jun 23, 2016   19537    Chris.Golden Added topmost and symbol shape properties.
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

    /**
     * Flag indicating whether or not the entity has been modified since the
     * last call to {@link #checkAndResetModified()}.
     */
    private transient boolean modified;

    // Package Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of this instance.
     */
    SpatialEntity(I identifier) {
        this.identifier = identifier;
    }

    // Public Methods

    @Override
    public boolean equals(Object other) {
        if (other instanceof SpatialEntity == false) {
            return false;
        }
        SpatialEntity<?> otherEntity = (SpatialEntity<?>) other;
        return (compare(identifier, otherEntity.identifier)
                && compare(geometry, otherEntity.geometry)
                && compare(borderColor, otherEntity.borderColor)
                && compare(fillColor, otherEntity.fillColor)
                && (borderThickness == otherEntity.borderThickness)
                && compare(borderStyle, otherEntity.borderStyle)
                && (diameter == otherEntity.diameter)
                && compare(symbolShape, otherEntity.symbolShape)
                && compare(label, otherEntity.label)
                && (textOffsetLength == otherEntity.textOffsetLength)
                && (textOffsetDirection == otherEntity.textOffsetDirection)
                && (textSize == otherEntity.textSize)
                && compare(textColor, otherEntity.textColor)
                && compare(dragCapability, otherEntity.dragCapability)
                && (rotatable == otherEntity.rotatable)
                && (scaleable == otherEntity.scaleable) && (topmost == otherEntity.topmost));
    }

    @Override
    public int hashCode() {
        return (int) ((getHashCode(identifier) + getHashCode(geometry)
                + getHashCode(borderColor) + getHashCode(fillColor)
                + ((long) borderThickness) + getHashCode(borderStyle)
                + ((long) diameter) + getHashCode(symbolShape)
                + getHashCode(label) + ((long) textOffsetLength)
                + ((long) textOffsetDirection) + (textSize)
                + getHashCode(textColor) + getHashCode(dragCapability)
                + (rotatable ? 1L : 0L) + (scaleable ? 1L : 0L) + (topmost ? 1L
                    : 0L)) % Integer.MAX_VALUE);
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
    public double getTextSize() {
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

    /**
     * Determine whether or not this object has had its visual properties
     * modified since the last call to this method, or since creation if this
     * method has not yet been called on this instance, and reset the modified
     * flag if it is set to true.
     * 
     * @return True if this object has been modified.
     */
    public boolean checkAndResetModified() {
        boolean modified = this.modified;
        this.modified = false;
        return modified;
    }

    /**
     * Set the geometry.
     * 
     * @param geometry
     *            New value.
     */
    public void setGeometry(Geometry geometry) {
        if (compare(this.geometry, geometry) == false) {
            this.geometry = geometry;
            modified = true;
        }
    }

    /**
     * Set the border color.
     * 
     * @param borderColor
     *            New value.
     */
    public void setBorderColor(Color borderColor) {
        if (compare(this.borderColor, borderColor) == false) {
            this.borderColor = borderColor;
            modified = true;
        }
    }

    /**
     * Set the fill color.
     * 
     * @param fillColor
     *            New value.
     */
    public void setFillColor(Color fillColor) {
        if (compare(this.fillColor, fillColor) == false) {
            this.fillColor = fillColor;
            modified = true;
        }
    }

    /**
     * Set the border thickness in pixels.
     * 
     * @param borderThickness
     *            New value.
     */
    public void setBorderThickness(double borderThickness) {
        if (this.borderThickness != borderThickness) {
            this.borderThickness = borderThickness;
            modified = true;
        }
    }

    /**
     * Set the border style.
     * 
     * @param borderStyle
     *            New value.
     */
    public void setBorderStyle(BorderStyle borderStyle) {
        if (compare(this.borderStyle, borderStyle) == false) {
            this.borderStyle = borderStyle;
            modified = true;
        }
    }

    /**
     * Set the diameter in pixels.
     * 
     * @param diameter
     *            New value.
     */
    public void setDiameter(double diameter) {
        if (this.diameter != diameter) {
            this.diameter = diameter;
            modified = true;
        }
    }

    /**
     * Set the symbol shape.
     * 
     * @param symbolShape
     *            New value.
     */
    public void setSymbolShape(SymbolShape symbolShape) {
        if (compare(this.symbolShape, symbolShape) == false) {
            this.symbolShape = symbolShape;
            modified = true;
        }
    }

    /**
     * Set the text label.
     * 
     * @param label
     *            New value.
     */
    public void setLabel(String label) {
        if (compare(this.label, label) == false) {
            this.label = label;
            modified = true;
        }
    }

    /**
     * Set the text offset length in pixels.
     * 
     * @param textOffsetLength
     *            New value.
     */
    public void setTextOffsetLength(double textOffsetLength) {
        if (this.textOffsetLength != textOffsetLength) {
            this.textOffsetLength = textOffsetLength;
            modified = true;
        }
    }

    /**
     * Set the text offset direction in degrees (with 0 being to the right, 90
     * above, and so on).
     * 
     * @param textOffsetDirection
     *            New value.
     */
    public void setTextOffsetDirection(double textOffsetDirection) {
        if (this.textOffsetDirection != textOffsetDirection) {
            this.textOffsetDirection = textOffsetDirection;
            modified = true;
        }
    }

    /**
     * Set the text size in points.
     * 
     * @param textSize
     *            New value.
     */
    public void setTextSize(int textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            modified = true;
        }
    }

    /**
     * Set the text color.
     * 
     * @param textColor
     *            New value.
     */
    public void setTextColor(Color textColor) {
        if (compare(this.textColor, textColor) == false) {
            this.textColor = textColor;
            modified = true;
        }
    }

    /**
     * Set the drag capability.
     * 
     * @param dragCapability
     *            New value.
     */
    public void setDragCapability(DragCapability dragCapability) {
        if (compare(this.dragCapability, dragCapability) == false) {
            this.dragCapability = dragCapability;
            modified = true;
        }
    }

    /**
     * Set the flag indicating whether or not the feature is rotatable.
     * 
     * @param rotatable
     *            New value.
     */
    public void setRotatable(boolean rotatable) {
        if (this.rotatable != rotatable) {
            this.rotatable = rotatable;
            modified = true;
        }
    }

    /**
     * Set the flag indicating whether or not the feature is scaleable.
     * 
     * @param scaleable
     *            New value.
     */
    public void setScaleable(boolean scaleable) {
        if (this.scaleable != scaleable) {
            this.scaleable = scaleable;
            modified = true;
        }
    }

    /**
     * Set the flag indicating whether or not the feature is topmost.
     * 
     * @param topmost
     *            New value.
     */
    public void setTopmost(boolean topmost) {
        if (this.topmost != topmost) {
            this.topmost = topmost;
            modified = true;
        }
    }

    // Private Methods

    /**
     * Compare the specified objects to see if they are equivalent, or are both
     * <code>null</code>.
     * 
     * @param object1
     *            First object to be compared; may be <code>null</code>.
     * @param object2
     *            Second object to be compared; may be <code>null</code>.
     * @return True if the two objects are equivalent or are both
     *         <code>null</code>, false otherwise.
     */
    private boolean compare(Object object1, Object object2) {
        return (object1 == null ? object2 == null : object1.equals(object2));
    }

    /**
     * Get the hash code of the specified object.
     * 
     * @param object
     *            Object for which the hash code is to be generated, or
     *            <code>null</code>.
     * @return Hash code, or 0 if the object is <code>null</code>.
     */
    private long getHashCode(Object object) {
        return (object == null ? 0L : object.hashCode());
    }
}