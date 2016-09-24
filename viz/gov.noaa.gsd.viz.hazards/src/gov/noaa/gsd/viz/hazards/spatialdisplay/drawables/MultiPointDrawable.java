/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.util.List;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Multi-point drawable shape base class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Aug 23, 2013  1264      Dan Schaffer  Initial creation.
 * Feb 09, 2015  6260      Dan Schaffer  Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden  Added code to support visual features.
 * Mar 26, 2016 15676      Chris.Golden  Added visual feature identifier.
 * Jul 25, 2016 19537      Chris.Golden  Renamed, and removed unneeded member
 *                                       data and methods. Also changed to
 *                                       be concrete (not abstract) class, as
 *                                       no subclasses for lines and polygons
 *                                       are needed.
 * Aug 22, 2016 19537      Chris.Golden  Removed unneeded layer constructor
 *                                       parameter. Also added toString()
 *                                       method.
 * Sep 12, 2016 15934      Chris.Golden  Changed to work with advanced
 *                                       geometries.
 * Sep 20, 2016 15934      Chris.Golden  Made into abstract base class for
 *                                       path and ellipse drawables. Also
 *                                       added methods to allow copying and
 *                                       translation (offsetting by deltas).
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public abstract class MultiPointDrawable<G extends IAdvancedGeometry> extends
        Line implements IDrawable<G> {

    // Private Static Constants

    /**
     * PGEN category for lines.
     */
    private static final String LINE = "Line";

    // Private Variables

    /**
     * Identifier of the entity that this shape represents in whole or in part.
     */
    private final IEntityIdentifier identifier;

    /**
     * Geometry of this drawable.
     */
    private G geometry;

    /**
     * Index of the sub-geometry this shape represents within the overall
     * geometry represented by this shape's {@link DECollection}, or
     * <code>-1</code> if this shape does not represent a sub-geometry (or if it
     * does, but it is the only sub-geometry within a collection).
     */
    private final int geometryIndex;

    /**
     * Flag indicating whether or not this shape is movable,
     */
    private boolean movable;

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
    public MultiPointDrawable(IEntityIdentifier identifier,
            DrawableAttributes attributes, G geometry) {
        this.identifier = identifier;
        this.geometryIndex = attributes.getGeometryIndex();
        this.geometry = geometry;
        update(attributes);
        setPgenCategory(LINE);
        setPgenType(attributes.getLineStyle().toString());
        setGeometry(geometry);
    }

    // Protected Constructors

    /**
     * Construct a copy instance. This is intended to be used by implementations
     * of {@link #copyOf()}.
     * 
     * @param other
     *            Drawable being copied.
     */
    @SuppressWarnings("unchecked")
    protected MultiPointDrawable(MultiPointDrawable<G> other) {
        this.identifier = other.identifier;
        this.geometryIndex = other.geometryIndex;
        this.movable = other.movable;
        update(other);
        setPgenCategory(LINE);
        setPgenType(other.pgenType);
        setGeometry((G) other.geometry.copyOf());
    }

    // Public Methods

    @Override
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public G getGeometry() {
        return geometry;
    }

    @Override
    public abstract boolean isEditable();

    @Override
    public abstract void setEditable(boolean editable);

    @Override
    public abstract boolean isResizable();

    @Override
    public abstract void setResizable(boolean resizable);

    @Override
    public abstract boolean isRotatable();

    @Override
    public abstract void setRotatable(boolean rotatable);

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

    // Protected Methods

    /**
     * Set the geometry to that specified.
     * 
     * @param geometry
     *            New geometry to be used.
     */
    protected void setGeometry(G geometry) {
        this.geometry = geometry;
        List<Coordinate> points = Lists.newArrayList(AdvancedGeometryUtilities
                .getJtsGeometry(geometry).getCoordinates());
        setLinePoints(points);
    }
}
