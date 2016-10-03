/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.input;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.PathDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.VertexManipulationPoint;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Input handler that allows the user to move the vertex of an
 * editable drawable.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 29, 2016   15928    Chris.Golden Initial creation (majority of code
 *                                      refactored out of the
 *                                      SelectionAndModificationInputHandler).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VertexMoveInputHandler extends
        ModificationInputHandler<VertexManipulationPoint> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public VertexMoveInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Protected Methods

    @Override
    protected boolean isEditableViaHandler(AbstractDrawableComponent drawable) {
        return ((IDrawable<?>) drawable).isEditable();
    }

    @Override
    protected void modifyDrawableForManipulationPointMove(
            AbstractDrawableComponent drawable,
            VertexManipulationPoint manipulationPoint, Coordinate newLocation) {

        /*
         * Get the coordinates of the part of the geometry of the drawable in
         * which the vertex is found. For a polygon, this may be the exterior
         * shell, or one of the interior rings (if it has holes). For other
         * geometries, only one group of coordinates is possible.
         */
        int partIndex = manipulationPoint.getLinearRingIndex();
        PathDrawable pathDrawable = (PathDrawable) drawable;
        Geometry geometry = pathDrawable.getGeometry().getGeometry();
        List<Coordinate[]> overallCoordinates = AdvancedGeometryUtilities
                .getCoordinates(geometry);
        Coordinate[] coordinates = overallCoordinates.get(partIndex);

        /*
         * Replace the previous coordinate with the new one. If this is a
         * polygon, the last point must be the same as the first, so ensure that
         * this is the case if the first point is the one being moved. (The last
         * point is never the one being moved for polygons.)
         */
        int vertexIndex = manipulationPoint.getVertexIndex();
        coordinates[vertexIndex] = newLocation;
        if ((vertexIndex == 0) && getSpatialDisplay().isPolygon(drawable)) {
            coordinates[coordinates.length - 1] = (Coordinate) newLocation
                    .clone();
        }

        /*
         * Create a geometry using the list of coordinate arrays, which contains
         * the array that was just modified, and tell the drawable to use it.
         */
        GeometryWrapper modifiedGeometry = (GeometryWrapper) getSpatialDisplay()
                .buildModifiedGeometryForDrawable(pathDrawable,
                        overallCoordinates);
        pathDrawable.setGeometry(modifiedGeometry);
    }
}
