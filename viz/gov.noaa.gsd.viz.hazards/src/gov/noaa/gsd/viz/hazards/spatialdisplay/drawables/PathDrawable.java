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

import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;

import com.vividsolutions.jts.geom.util.AffineTransformation;

/**
 * Description: Path drawable shape, used for both lines and polygons.
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
public class PathDrawable extends MultiPointDrawable<GeometryWrapper> {

    // Private Variables

    /**
     * Flag indicating whether or not this shape is editable,
     */
    private boolean editable;

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
    public PathDrawable(IEntityIdentifier identifier,
            DrawableAttributes attributes, GeometryWrapper geometry) {
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
    protected PathDrawable(PathDrawable other) {
        super(other);
        this.editable = other.editable;
    }

    // Public Methods

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public void setResizable(boolean resizable) {
        throw new UnsupportedOperationException(
                "paths/polygons cannot be resizable");
    }

    @Override
    public boolean isRotatable() {
        return false;
    }

    @Override
    public void setRotatable(boolean rotatable) {
        throw new UnsupportedOperationException(
                "paths/polygons cannot be resizable");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends IDrawable<?>> D copyOf() {
        return (D) new PathDrawable(this);
    }

    @Override
    public String toString() {
        return getIdentifier() + " (" + (isClosedLine() ? "polygon" : "line")
                + ")";
    }

    @Override
    public void offsetBy(double x, double y) {
        setGeometry(new GeometryWrapper(AffineTransformation
                .translationInstance(x, y).transform(
                        getGeometry().getGeometry()), getGeometry()
                .getRotation()));
    }
}
