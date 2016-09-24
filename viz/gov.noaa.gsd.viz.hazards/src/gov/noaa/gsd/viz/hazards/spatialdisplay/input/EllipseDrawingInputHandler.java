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

import gov.noaa.gsd.common.utilities.geometry.Ellipse;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.utilities.geometry.LinearUnit;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Ellipse drawing input handler, for drawing ellipses within the
 * spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 21, 2016   15934    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EllipseDrawingInputHandler extends ScalingDrawingInputHandler {

    // Private Static Constants

    /**
     * Linear unit to be used for width and height of the generated ellipse.
     */
    private static final LinearUnit LINEAR_UNIT = LinearUnit.NAUTICAL_MILES;

    // Private Variables

    /**
     * Coordinate used for horizontal radius calculation; allocated and kept for
     * the life of this handler to avoid constant reallocation only.
     */
    private final Coordinate horizontalRadiusLocation = new Coordinate();

    /**
     * Coordinate used for vertical radius calculation; allocated and kept for
     * the life of this handler to avoid constant reallocation only.
     */
    private final Coordinate verticalRadiusLocation = new Coordinate();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public EllipseDrawingInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Protected Methods

    @Override
    protected IAdvancedGeometry createGeometryFromPoints(Coordinate firstPoint,
            Coordinate secondPoint) {

        /*
         * Calculate the horizontal radius by determining the distance between
         * the first point (which is the center) and a point at the same
         * latitude, but with the longitude of the second point.
         */
        horizontalRadiusLocation.x = secondPoint.x;
        horizontalRadiusLocation.y = firstPoint.y;
        double horizontalRadius = LINEAR_UNIT.getDistanceBetween(firstPoint,
                horizontalRadiusLocation);

        /*
         * Calculate the vertical radius by determining the distance between the
         * first point (which is the center) and a point at the same longitude,
         * but with the latitude of the second point.
         */
        verticalRadiusLocation.x = firstPoint.x;
        verticalRadiusLocation.y = secondPoint.y;
        double verticalRadius = LINEAR_UNIT.getDistanceBetween(firstPoint,
                verticalRadiusLocation);

        /*
         * Create an ellipse with the first point as the center, the width and
         * height calculated using the radii found above, and no rotation.
         */
        return new Ellipse(firstPoint, horizontalRadius * 2.0,
                verticalRadius * 2.0, LINEAR_UNIT, 0);
    }
}
