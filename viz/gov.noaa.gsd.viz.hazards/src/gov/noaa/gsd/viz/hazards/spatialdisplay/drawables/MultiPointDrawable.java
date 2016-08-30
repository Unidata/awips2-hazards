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

import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Multi-point drawable shape base class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2013   1264      daniel.s.schaffer  Initial creation
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * Jul 25, 2016 19537      Chris.Golden        Renamed, and removed unneeded member
 *                                             data and methods. Also changed to
 *                                             be concrete (not abstract) class, as
 *                                             no subclasses for lines and polygons
 *                                             are needed.
 * Aug 22, 2016 19537      Chris.Golden        Removed unneeded layer constructor
 *                                             parameter. Also added toString()
 *                                             method.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class MultiPointDrawable extends Line implements IDrawable {

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
     * Flag indicating whether or not this shape is editable,
     */
    private boolean editable = true;

    /**
     * Flag indicating whether or not this shape is movable,
     */
    private boolean movable = true;

    /**
     * Geometry of this drawable.
     */
    private final Geometry geometry;

    /**
     * Index of the sub-geometry this shape represents within the overall
     * geometry represented by this shape's {@link DECollection}, or
     * <code>-1</code> if this shape does not represent a sub-geometry (or if it
     * does, but it is the only sub-geometry within a collection).
     */
    private final int geometryIndex;

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
            DrawableAttributes attributes, Geometry geometry) {
        this.identifier = identifier;
        this.geometryIndex = attributes.getGeometryIndex();
        this.geometry = geometry;
        List<Coordinate> points = Lists.newArrayList(geometry.getCoordinates());
        setLinePoints(points);
        update(attributes);
        setPgenCategory(LINE);
        setPgenType(attributes.getLineStyle().toString());
    }

    // Public Methods

    @Override
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
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

    /*
     * Overridden to ensure that colors' alpha components are copied as well, as
     * the superclass does not do this; it only copies the RGB components of the
     * colors.
     */
    @Override
    public DrawableElement copy() {
        Line copy = (Line) super.copy();
        Color[] colors = new Color[getColors().length];
        for (int j = 0; j < getColors().length; j++) {
            colors[j] = new Color(getColors()[j].getRed(),
                    getColors()[j].getGreen(), getColors()[j].getBlue(),
                    getColors()[j].getAlpha());
        }
        copy.setColors(colors);
        return copy;
    }

    @Override
    public String toString() {
        return getIdentifier() + " (" + (isClosedLine() ? "polygon" : "line")
                + ")";
    }
}
