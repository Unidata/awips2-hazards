/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.utilities.geometry.Ellipse;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Ellipse drawable shape.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Sep 21, 2016   15934    Chris.Golden  Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EllipseDrawable extends MultiPointDrawable<Ellipse> {

    // Private Variables

    /**
     * Flag indicating whether or not this shape is resizable,
     */
    private boolean resizable;

    /**
     * Flag indicating whether or not this shape is rotatable,
     */
    private boolean rotatable;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier.
     * @param attributes
     *            Drawable attributes.
     * @param geometry
     *            Geometry.
     */
    public EllipseDrawable(IEntityIdentifier identifier,
            DrawableAttributes attributes, Ellipse geometry) {
        super(identifier, attributes, geometry);
    }

    // Protected Constructors

    /**
     * Construct a copy instance. This is intended to be used by implementations
     * of {@link #copyOf()}.
     * 
     * @param other
     *            Drawable to be copied.
     */
    protected EllipseDrawable(EllipseDrawable other) {
        super(other);
        this.resizable = other.resizable;
        this.rotatable = other.rotatable;
    }

    // Public Methods

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        throw new UnsupportedOperationException("ellipses cannot be editable");
    }

    @Override
    public boolean isResizable() {
        return resizable;
    }

    @Override
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    @Override
    public boolean isRotatable() {
        return rotatable;
    }

    @Override
    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    @Override
    public String toString() {
        return getIdentifier() + " (ellipse)";
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends IDrawable<?>> D copyOf() {
        return (D) new EllipseDrawable(this);
    }

    @Override
    public void offsetBy(double x, double y) {
        Coordinate centerPoint = getGeometry().getCenterPoint();
        setGeometry(new Ellipse(new Coordinate(centerPoint.x + x, centerPoint.y
                + y), getGeometry().getWidth(), getGeometry().getHeight(),
                getGeometry().getUnits(), getGeometry().getRotation()));
    }
}
