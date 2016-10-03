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

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorType;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Scale manipulation point, holding the location and type of a
 * point that, if dragged by the user, allows a drawable to be rescaled in a
 * manner appropriate to its type.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 29, 2016   15928    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ScaleManipulationPoint extends CenterRelativeManipulationPoint {

    // Public Enumerated Types

    /**
     * Direction of the point when viewed from the center of the drawable (if
     * rotation of said drawable is discounted, that is, before any rotation is
     * applied, so <code>EAST</code> means to the east of a drawable if the
     * drawable was not rotated, but if it is rotated 90 degrees, it will
     * actually be north).
     */
    public enum Direction {
        SOUTHEAST, EAST, NORTHEAST, NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH
    }

    // Private Variables

    /**
     * Direction of the manipulation point.
     */
    private final Direction direction;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param drawable
     *            Drawable to which this point applies.
     * @param location
     *            Location of the point.
     * @param center
     *            Center of the shape.
     * @param direction
     *            Direction of the manipulation point when viewed from the
     *            center of the drawable, before any rotation of said drawable
     *            is applied.
     */
    public ScaleManipulationPoint(AbstractDrawableComponent drawable,
            Coordinate location, Coordinate center, Direction direction) {
        super(drawable, location, center);
        this.direction = direction;
    }

    // Public Methods

    /**
     * Get the direction of the manipulation point.
     * 
     * @return Direction of the manipulation point when viewed from the center
     *         of the drawable, before any rotation of said drawable is applied.
     */
    public Direction getDirection() {
        return direction;
    }

    @Override
    public CursorType getCursor() {

        /*
         * The pixel space angle between the center and the point must be
         * calculated to determine the cursor to be used. First, get the angle
         * between the center and the manipulation point in lat-lon space.
         */
        Coordinate location = getLocation();
        Coordinate center = getCenter();
        double angleRadians = Math.atan2(location.y - center.y, location.x
                - center.x);

        /*
         * Calculate about 1/1000 of the total diagonal "distance" in lat-lons.
         * This will be used as the magnitude of the offset to either side from
         * the manipulation point along the angle calculated above to create a
         * line segment that, when translated to pixel coordinates, can provide
         * a pixel-space version of the angle.
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        IRenderableDisplay display = editor.getActiveDisplayPane()
                .getRenderableDisplay();
        IExtent extent = display.getExtent();
        IDescriptor descriptor = display.getDescriptor();
        double[] leftCorner = descriptor.pixelToWorld(new double[] {
                extent.getMinX(), extent.getMinY() });
        double[] rightCorner = descriptor.pixelToWorld(new double[] {
                extent.getMaxX(), extent.getMaxY() });

        /*
         * Calculate the two offset points. If either of them do not translate
         * into pixel space, use the original manipulation point in that one's
         * place.
         */
        double[] pixelPoint = editor.translateInverseClick(location);
        List<double[]> pixelPoints = new ArrayList<>(2);
        double lengthLatLons = Math.pow(
                Math.pow(leftCorner[0] - rightCorner[0], 2.0)
                        + Math.pow(leftCorner[1] - rightCorner[1], 2.0), 0.5) / 1000.0;
        for (double angleOffset = 0.0; angleOffset < Math.PI * 2.0; angleOffset += Math.PI) {
            double[] thisPixelPoint = editor
                    .translateInverseClick(new Coordinate(location.x
                            + (lengthLatLons * Math.cos(angleRadians
                                    + angleOffset)), location.y
                            + (lengthLatLons * Math.sin(angleRadians
                                    + angleOffset))));
            pixelPoints.add(thisPixelPoint == null ? pixelPoint
                    : thisPixelPoint);
        }

        /*
         * If the manipulation point in pixel space was not used in place of
         * both offset points, then calculate the angle along the line segment
         * in pixel space. Because pixel space flips the angle along the
         * vertical access, flip it back. Then ensure it is between 0 and Pi.
         */
        if (pixelPoints.get(0) != pixelPoints.get(1)) {
            angleRadians = Math.atan2(
                    pixelPoints.get(1)[1] - pixelPoints.get(0)[1],
                    pixelPoints.get(1)[0] - pixelPoints.get(0)[0]);
            angleRadians = (Math.PI - Math.abs(angleRadians))
                    * (angleRadians < 0.0 ? -1.0 : 1.0);
            if (angleRadians < 0.0) {
                angleRadians = (Math.PI * 2.0) + angleRadians;
            }
        }

        /*
         * Translate the angle into an appropriate cursor.
         */
        double angleDegrees = Math.toDegrees(angleRadians);
        return ((angleDegrees < 15.0) || (angleDegrees > 345.0) ? CursorType.SCALE_RIGHT
                : (angleDegrees <= 75.0 ? CursorType.SCALE_UP_AND_RIGHT
                        : (angleDegrees < 105.0 ? CursorType.SCALE_UP
                                : (angleDegrees <= 165.0 ? CursorType.SCALE_UP_AND_LEFT
                                        : (angleDegrees < 195.0 ? CursorType.SCALE_LEFT
                                                : (angleDegrees <= 255.0 ? CursorType.SCALE_DOWN_AND_LEFT
                                                        : (angleDegrees < 285.0 ? CursorType.SCALE_DOWN
                                                                : CursorType.SCALE_DOWN_AND_RIGHT)))))));
    }

    @Override
    public String toString() {
        return "scaling (" + direction.toString().toLowerCase() + ")";
    }
}
