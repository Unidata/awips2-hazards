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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;

/**
 * Description: Path drawable shape, used for both lines and polygons.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Sep 21, 2016   15934    Chris.Golden  Initial creation.
 * Sep 29, 2016   15928    Chris.Golden  Added use of manipulation points.
 * Jan 17, 2018   33428    Chris.Golden Changed to work with new version of
 *                                      {@link IDrawable}.
 * Feb 02, 2018   26712    Chris.Golden Changed to allow visual buffering of
 *                                      appropriate drawables.
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
            MultiPointDrawableAttributes attributes, GeometryWrapper geometry) {
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
        setVertexEditable(other.editable);
    }

    // Public Methods

    @Override
    public boolean isVertexEditable() {
        return editable;
    }

    @Override
    public void setVertexEditable(boolean editable) {
        this.editable = editable;
        updateManipulationPoints();
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
        setGeometry(new GeometryWrapper(
                AffineTransformation.translationInstance(x, y)
                        .transform(getGeometry().getGeometry()),
                getGeometry().getRotation()));
    }

    // Protected Methods

    @Override
    protected List<ManipulationPoint> getUpdatedManipulationPoints() {

        /*
         * Get the vertices to be made into vertex-movement manipulation points
         * if the path or polygon is editable. If it is a polygon and has holes
         * in it, get each interior ring's coordinate array and add it to the
         * list of arrays.
         */
        List<Coordinate[]> coordinates = null;
        int numCoordinates = 0;
        if (isVertexEditable()) {
            coordinates = AdvancedGeometryUtilities
                    .getCoordinates(getGeometry().getGeometry());
            for (Coordinate[] ringCoordinates : coordinates) {
                numCoordinates += ringCoordinates.length;
            }
        } else {
            coordinates = Collections.emptyList();
        }

        /*
         * Get the manipulation points needed for resizing or rotating, if any.
         */
        List<ManipulationPoint> rotationAndResizingPoints = (Utilities
                .getBoundingBoxManipulationPoints(this));

        /*
         * Create the list of manipulation points, sized to hold any editable
         * vertices and any rotation or resizing points.
         */
        List<ManipulationPoint> manipulationPoints = new ArrayList<>(
                numCoordinates + rotationAndResizingPoints.size());

        /*
         * Add any rotation and resizing points.
         */
        manipulationPoints.addAll(rotationAndResizingPoints);

        /*
         * Add the vertex-movement points, if any, the outer shell's points
         * first, then any hole's points. For each ring of points, skip the last
         * one if it is the same as the first one.
         */
        for (int j = 0; j < coordinates.size(); j++) {
            Coordinate[] ringCoordinates = coordinates.get(j);
            Coordinate firstCoordinate = null;
            for (int k = 0; k < ringCoordinates.length; k++) {
                Coordinate coordinate = ringCoordinates[k];
                if (firstCoordinate == null) {
                    firstCoordinate = coordinate;
                } else if (firstCoordinate.equals(coordinate)) {
                    continue;
                }
                manipulationPoints.add(
                        new VertexManipulationPoint(this, coordinate, j, k));
            }
        }

        return manipulationPoints;
    }
}
