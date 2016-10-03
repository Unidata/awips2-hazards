/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;

import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.util.AffineTransformation;

/**
 * Single-lat-lon-location symbol drawable.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 2011              Bryon.Lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * Jun 23, 2016 19537      Chris.Golden        Changed to use better identifiers.
 * Jul 25, 2016 19537      Chris.Golden        Renamed, and removed unneeded member
 *                                             data and methods.
 * Aug 22, 2016 19537      Chris.Golden        Removed unneeded layer constructor
 *                                             parameter. Also added toString()
 *                                             method.
 * Sep 12, 2016 15934      Chris.Golden        Changed to work with advanced
 *                                             geometries. Also removed code that
 *                                             was designed to make the drawable's
 *                                             geometry a polygon so that hit tests
 *                                             could be done using it; hit testing
 *                                             is the job of the spatial display, not
 *                                             the individual drawables, which merely
 *                                             supply their true geometries.
 * Sep 21, 2016 15934      Chris.Golden        Added support for resizable and
 *                                             rotatable flags. Also added methods to
 *                                             allow copying and translation
 *                                             (offsetting by deltas).
 * Sep 29, 2016 15928      Chris.Golden        Added offsetBy() method and use of
 *                                             manipulation points.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class SymbolDrawable extends Symbol implements
        IDrawable<GeometryWrapper> {

    // Private Variables

    /**
     * Identifier of the entity that this shape represents in whole or in part.
     */
    private final IEntityIdentifier identifier;

    /**
     * Geometry of this drawable.
     */
    private GeometryWrapper geometry;

    /**
     * Index of the sub-geometry this shape represents within the overall
     * geometry represented by this shape's {@link DECollection}, or
     * <code>-1</code> if this shape does not represent a sub-geometry (or if it
     * does, but it is the only sub-geometry within a collection).
     */
    private final int geometryIndex;

    /**
     * Flag indicating whether or not this shape is movable.
     */
    private boolean movable = false;

    // Public Constructors

    /**
     * Creates a symbol.
     * 
     * @param identifier
     *            Identifier of this symbol.
     * @param drawingAttributes
     *            Attributes controlling the appearance of this symbol.
     * @param pgenType
     *            The PGEN type of this symbol, indicating the symbol geometry.
     * @param geometry
     *            Point geometry for this symbol.
     */
    public SymbolDrawable(IEntityIdentifier identifier,
            DrawableAttributes drawingAttributes, String pgenType,
            GeometryWrapper geometry) {
        this.identifier = identifier;
        this.geometry = geometry;
        this.geometryIndex = drawingAttributes.getGeometryIndex();
        setLocation(AdvancedGeometryUtilities.getCentroid(geometry));
        setPgenCategory(pgenType);
        setPgenType(pgenType);
        this.setColors(drawingAttributes.getColors());
        this.setLineWidth(drawingAttributes.getLineWidth());
        this.setSizeScale(drawingAttributes.getSizeScale());
    }

    // Protected Constructors

    /**
     * Construct a copy instance. This is intended to be used by implementations
     * of {@link #copyOf()}.
     * 
     * @param other
     *            Drawable being copied.
     */
    protected SymbolDrawable(SymbolDrawable other) {
        this.identifier = other.identifier;
        this.geometry = other.geometry.copyOf();
        this.geometryIndex = other.geometryIndex;
        this.movable = other.movable;
        setLocation(AdvancedGeometryUtilities.getCentroid(geometry));
        setPgenCategory(other.pgenType);
        setPgenType(other.pgenType);
        update(other);
    }

    // Public Methods

    @Override
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public GeometryWrapper getGeometry() {
        return geometry;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        throw new UnsupportedOperationException("symbols cannot be editable");
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public void setResizable(boolean resizable) {
        throw new UnsupportedOperationException("symbols cannot be resizable");
    }

    @Override
    public boolean isRotatable() {
        return false;
    }

    @Override
    public void setRotatable(boolean rotatable) {
        throw new UnsupportedOperationException("symbols cannot be rotatable");
    }

    @Override
    public boolean isMovable() {
        return movable;
    }

    @Override
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    @Override
    public int getGeometryIndex() {
        return geometryIndex;
    }

    @Override
    public String toString() {
        return getIdentifier() + " (symbol = \"" + getPgenType() + "\")";
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends IDrawable<?>> D copyOf() {
        return (D) new SymbolDrawable(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D extends IDrawable<?>> D highlitCopyOf(boolean active) {
        SymbolDrawable drawable = copyOf();
        if (active == false) {
            Utilities.changeOpacity(drawable, 0.65);
        }
        drawable.setSizeScale(drawable.getSizeScale() * (active ? 1.2f : 1.5f));
        return (D) drawable;
    }

    @Override
    public void offsetBy(double x, double y) {
        this.geometry = new GeometryWrapper(AffineTransformation
                .translationInstance(x, y).transform(
                        getGeometry().getGeometry()), getGeometry()
                .getRotation());
        setLocation(AdvancedGeometryUtilities.getCentroid(geometry));
    }

    @Override
    public List<ManipulationPoint> getManipulationPoints() {
        return Collections.emptyList();
    }
}
