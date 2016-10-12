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

import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.utilities.geometry.IRotatable;
import gov.noaa.gsd.common.utilities.geometry.IScaleable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.MultiPointDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ScaleManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ScaleManipulationPoint.Direction;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.util.AffineTransformation;

/**
 * Description: Input handler that allows the user to resize an editable
 * drawable.
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
public class ResizeInputHandler extends
        ModificationInputHandler<ScaleManipulationPoint> {

    // Private Variables

    /**
     * Affine transformation for rotating a point by the reverse of the
     * drawable's rotation so that its distance from the horizontal and/or
     * vertical lines running through the center of the drawable may be
     * determined.
     */
    private AffineTransformation unrotateTransformer;

    /**
     * Center point of the drawable.
     */
    private Coordinate centerPoint;

    /**
     * Distances in longitudinal and latitudinal units respectively, between the
     * last mouse location and the vertical and horizontal axes of the drawable
     * rotated by the drawable's rotation.
     */
    private Coordinate lastDistances;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public ResizeInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void initialize(AbstractDrawableComponent drawableBeingEdited,
            ScaleManipulationPoint manipulationPoint, Coordinate newLocation) {

        /*
         * Get the center point of the drawable, to be used to measure the mouse
         * cursor's distance from horizontal and vertical axes passing through
         * that point, and the transformer to be used to unrotate mouse cursor
         * locations so that their distance from said axes may be calculated
         * without worrying about the drawable's rotation, if any.
         */
        IAdvancedGeometry geometry = ((MultiPointDrawable<?>) drawableBeingEdited)
                .getGeometry();
        centerPoint = geometry.getCenterPoint();
        double rotation = (geometry instanceof IRotatable ? ((IRotatable) geometry)
                .getRotation() : 0.0);
        unrotateTransformer = (rotation == 0.0 ? new AffineTransformation()
                : AffineTransformation.rotationInstance(rotation * -1.0,
                        centerPoint.x, centerPoint.y));

        /*
         * Get the longitudinal and latitudinal distances between the starting
         * location and the horizontal and vertical axes.
         */
        lastDistances = getDistances(manipulationPoint.getLocation());

        /*
         * Let the superclass do the rest.
         */
        super.initialize(drawableBeingEdited, manipulationPoint, newLocation);
    }

    // Protected Methods

    @Override
    protected boolean isEditableViaHandler(AbstractDrawableComponent drawable) {
        return ((IDrawable<?>) drawable).isResizable();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void modifyDrawableForManipulationPointMove(
            AbstractDrawableComponent drawable,
            ScaleManipulationPoint manipulationPoint, Coordinate newLocation) {

        /*
         * Get the horizontal and vertical distances between the new location
         * and the axes running through the center point.
         */
        Coordinate distances = getDistances(newLocation);

        /*
         * If horizontal scaling is allowed by the manipulation point being
         * used, determine the horizontal multiplier; if vertical scaling is
         * allowed, determine the vertical multiplier.
         */
        double horizontalMultiplier = 1.0;
        double verticalMultiplier = 1.0;
        if ((manipulationPoint.getDirection() != Direction.NORTH)
                && (manipulationPoint.getDirection() != Direction.SOUTH)) {
            horizontalMultiplier = distances.x / lastDistances.x;
        }
        if ((manipulationPoint.getDirection() != Direction.EAST)
                && (manipulationPoint.getDirection() != Direction.WEST)) {
            verticalMultiplier = distances.y / lastDistances.y;
        }
        lastDistances = distances;

        /*
         * Scale the geometry by the multipliers determined.
         */
        MultiPointDrawable<IScaleable> multiPointDrawable = (MultiPointDrawable<IScaleable>) drawable;
        IScaleable newGeometry = multiPointDrawable.getGeometry().scaledCopyOf(
                horizontalMultiplier, verticalMultiplier);
        multiPointDrawable.setGeometry(newGeometry);
    }

    // Private Methods

    /**
     * Get the distances, in longitudes and latitudes respectively, between the
     * specified location and the vertical and horizontal axes of the drawable,
     * rotated by the drawable's rotation if any.
     * 
     * @param location
     *            Location for which to generate distances.
     * @return Coordinate holding the longitudinal and latitudinal distances,
     *         respectively.
     */
    private Coordinate getDistances(Coordinate location) {
        Coordinate unrotatedLocation = new Coordinate();
        unrotateTransformer.transform(location, unrotatedLocation);
        return new Coordinate(Math.abs(unrotatedLocation.x - centerPoint.x),
                Math.abs(unrotatedLocation.y - centerPoint.y));
    }
}
