/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ScaleManipulationPoint.Direction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Description: Interface describing the methods that must be implemented by all
 * drawables used within the spatial display. The generic parameter
 * <code>G</code> provides the geometry type that is associated with
 * implementing subclasses.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2012            bryon.lawrence      Initial creation
 * Jul 18, 2013  1264      Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Feb 09, 2015  6260      Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * Jun 23, 2016 19537      Chris.Golden        Changed to use better identifiers.
 * Jul 25, 2016 19537      Chris.Golden        Renamed.
 * Sep 12, 2016 15934      Chris.Golden        Changed to work with advanced
 *                                             geometries.
 * Sep 20, 2016 15934      Chris.Golden        Changed to include methods required
 *                                             for curved geometries, as well as
 *                                             methods to allow copying and
 *                                             translation (offsetting by deltas).
 * Sep 29, 2016 15928      Chris.Golden        Added Utilities static class, as well
 *                                             as new methods to be implemented.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IDrawable<G extends IAdvancedGeometry> {

    // Public Static Classes

    /**
     * Utilities class, with methods that may be used to perform common
     * operations on implementors of the enclosing interface.
     */
    public static class Utilities {

        // Public Methods

        /**
         * Change the opacity of the specified drawable by the specified
         * multiplier.
         * 
         * @param drawable
         *            Drawable that will have its colors' opacities altered.
         * @param multiplier
         *            Number by which to multiply the drawable's colors'
         *            opacities.
         */
        public static void changeOpacity(DrawableElement drawable,
                double multiplier) {
            Color[] colors = drawable.getColors();
            Color[] newColors = new Color[colors.length];
            for (int j = 0; j < colors.length; j++) {
                newColors[j] = new Color(colors[j].getRed(),
                        colors[j].getGreen(), colors[j].getBlue(),
                        (int) (colors[j].getAlpha() * multiplier));
            }
            drawable.setColors(newColors);
        }

        /**
         * Given the specified geometry with the specified rotatability and
         * scaleability, get the manipulation points along the bounding box of
         * the geometry, if any. If the geometry is neither rotatable nor
         * scaleable, an empty list will be returned. If rotatable but not
         * scaleable, four corner points of the bounding box will be returned,
         * each for rotation. Otherwise, eight points (the four corner points,
         * and the four midpoints between adjacent pairs of corner points) will
         * be returned, with all points being for scaling except for the first
         * one, which will be for rotation if the geometry is rotatable and for
         * scaling if it is not.
         * 
         * @param drawable
         *            Drawable for which the manipulation points are desired.
         * @return Manipulation points.
         */
        public static List<ManipulationPoint> getBoundingBoxManipulationPoints(
                IDrawable<?> drawable) {

            /*
             * If neither rotatable or resizable, return an empty list.
             */
            if ((drawable.isRotatable() == false)
                    && (drawable.isResizable() == false)) {
                return Collections.emptyList();
            }

            /*
             * Get the corner points.
             */
            IAdvancedGeometry geometry = drawable.getGeometry();
            List<Coordinate> cornerPoints = AdvancedGeometryUtilities
                    .getBoundingBoxCornerPoints(geometry);

            /*
             * Get the center point of the bounding box.
             */
            Coordinate center = AdvancedGeometryUtilities
                    .getJtsGeometry(geometry).getEnvelopeInternal().centre();

            /*
             * Create the manipulation points based on the corner points.
             * Midpoints are needed between the corner points if resizable.
             */
            List<ManipulationPoint> manipulationPoints = new ArrayList<>(
                    cornerPoints.size() * (drawable.isResizable() ? 2 : 1));
            for (int j = 0; j < cornerPoints.size(); j++) {

                /*
                 * Create a manipulation point for this corner point. If only
                 * rotatable, or if scaleable as well but this is the first
                 * point, make it a rotation point; otherwise, make it a scale
                 * point.
                 */
                Coordinate thisPoint = cornerPoints.get(j);
                if (drawable.isRotatable()
                        && ((drawable.isResizable() == false) || (j == 0))) {
                    manipulationPoints.add(new RotationManipulationPoint(
                            (AbstractDrawableComponent) drawable, thisPoint,
                            center));
                } else {
                    manipulationPoints.add(new ScaleManipulationPoint(
                            (AbstractDrawableComponent) drawable, thisPoint,
                            center, (j == 0 ? Direction.SOUTHEAST
                                    : (j == 1 ? Direction.NORTHEAST
                                            : (j == 2 ? Direction.NORTHWEST
                                                    : Direction.SOUTHWEST)))));
                }

                /*
                 * If resizable, create a scale point at the midpoint between
                 * this corner point and the next.
                 */
                if (drawable.isResizable()) {
                    Coordinate nextPoint = cornerPoints.get((j + 1)
                            % cornerPoints.size());
                    manipulationPoints.add(new ScaleManipulationPoint(
                            (AbstractDrawableComponent) drawable,
                            new Coordinate((thisPoint.x + nextPoint.x) / 2.0,
                                    (thisPoint.y + nextPoint.y) / 2.0), center,
                            (j == 0 ? Direction.EAST
                                    : (j == 1 ? Direction.NORTH
                                            : (j == 2 ? Direction.WEST
                                                    : Direction.SOUTH)))));
                }
            }

            return manipulationPoints;
        }
    }

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public IEntityIdentifier getIdentifier();

    /**
     * Returns the geometry version of this shape.
     * 
     * @return Geometry representing this shape, or <code>null</code> if no
     *         geometry is associated with it.
     */
    public G getGeometry();

    /**
     * Get the index of the sub-geometry this shape represents within the
     * overall geometry represented by this shape's {@link DECollection}.
     * 
     * @return Index of the sub-geometry within the overall geometry, or
     *         <code>-1</code> if this shape does not represent a sub-geometry
     *         (or if it does, but it is the only sub-geometry within a
     *         collection).
     */
    public int getGeometryIndex();

    /**
     * Determine whether or not the shape is editable.
     * 
     * @return <code>true</code> if the user can edit this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isEditable();

    /**
     * Set the editable status of this shape.
     * 
     * @param editable
     *            Flag indicating whether or not this shape is editable.
     */
    public void setEditable(boolean editable);

    /**
     * Determine whether or not the shape is resizable.
     * 
     * @return <code>true</code> if the user can resize this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isResizable();

    /**
     * Set the resizable status of this shape.
     * 
     * @param editable
     *            Flag indicating whether or not this shape is resizable.
     */
    public void setResizable(boolean resizable);

    /**
     * Determine whether or not the shape is rotatable.
     * 
     * @return <code>true</code> if the user can rotate this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isRotatable();

    /**
     * Set the rotatable status of this shape.
     * 
     * @param rotatable
     *            Flag indicating whether or not this shape is rotatable.
     */
    public void setRotatable(boolean rotatable);

    /**
     * Determine whether or not the shape is editable.
     * 
     * @return <code>true</code> if the user can move this shape,
     *         <code>false</code> otherwise.
     */
    public boolean isMovable();

    /**
     * Set the movable status of this shape.
     * 
     * @param movable
     *            Flag indicating whether or not this shape is movable.
     */
    public void setMovable(boolean movable);

    /**
     * Get a copy of this shape in which the backing {@link IAdvancedGeometry}
     * is copied, not just a reference to the original drawable's geometry.
     * 
     * @return Copy of this shape.
     */
    public <D extends IDrawable<?>> D copyOf();

    /**
     * Get a copy of this shape similar to {@link #copyOf()}, but with visual
     * cues indicating that it is highlit.
     * 
     * @param active
     *            Flag indicating whether or not the highlit copy is for an
     *            active original. The copy will be more obviously highlit if
     *            this is <code>true</code>.
     * @return Copy of this shape.
     */
    public <D extends IDrawable<?>> D highlitCopyOf(boolean active);

    /**
     * Offset this drawable by the specified deltas. This offsets the backing
     * {@link IAdvancedGeometry} as well.
     * 
     * @param x
     *            Longitudinal offset.
     * @param y
     *            Latitudinal offset.
     */
    public void offsetBy(double x, double y);

    /**
     * Get the manipulation points for this drawable. These are the points that
     * may be manipulated by the user in some way.
     * 
     * @return Manipulation points for this drawable.
     */
    public List<ManipulationPoint> getManipulationPoints();
}
