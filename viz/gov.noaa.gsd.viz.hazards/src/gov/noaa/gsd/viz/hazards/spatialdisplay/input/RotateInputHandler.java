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

import gov.noaa.gsd.common.utilities.geometry.IRotatable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.MultiPointDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.RotationManipulationPoint;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Input handler that allows the user to rotate an editable
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
public class RotateInputHandler extends
        ModificationInputHandler<RotationManipulationPoint> {

    // Private Variables

    /**
     * Angle in radians between the center and the last location of the
     * manipulation point.
     */
    private double lastAngle;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public RotateInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void initialize(AbstractDrawableComponent drawableBeingEdited,
            RotationManipulationPoint manipulationPoint, Coordinate newLocation) {

        /*
         * Get the angle between the center and the manipulation point in
         * lat-lon space.
         */
        Coordinate location = manipulationPoint.getLocation();
        Coordinate center = manipulationPoint.getCenter();
        lastAngle = getAngleBetweenPoints(center, location);

        /*
         * Let the superclass do the rest.
         */
        super.initialize(drawableBeingEdited, manipulationPoint, newLocation);
    }

    // Protected Methods

    @Override
    protected boolean isEditableViaHandler(AbstractDrawableComponent drawable) {
        return ((IDrawable<?>) drawable).isRotatable();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void modifyDrawableForManipulationPointMove(
            AbstractDrawableComponent drawable,
            RotationManipulationPoint manipulationPoint, Coordinate newLocation) {

        /*
         * Get the angle between the center and the latest location, and then
         * from this determine the delta between it and the previously
         * determined angle. Then save the new angle for the next time this
         * method is called.
         */
        double angle = getAngleBetweenPoints(manipulationPoint.getCenter(),
                newLocation);
        double delta = angle - lastAngle;
        lastAngle = angle;

        /*
         * Rotate the geometry by the delta determined.
         */
        MultiPointDrawable<IRotatable> multiPointDrawable = (MultiPointDrawable<IRotatable>) drawable;
        IRotatable newGeometry = multiPointDrawable.getGeometry()
                .rotatedCopyOf(delta);
        multiPointDrawable.setGeometry(newGeometry);
    }

    // Private Methods

    /**
     * Get the angle in radians between the two points within the range
     * <code>[0, 2 * Pi)</code>.
     * 
     * @param firstLocation
     *            First location.
     * @param secondLocation
     *            Second location.
     * @return Angle in radians within the range <code>[0, 2 * Pi)</code>.
     */
    private double getAngleBetweenPoints(Coordinate firstLocation,
            Coordinate secondLocation) {
        return (Math.atan2(secondLocation.y - firstLocation.y, secondLocation.x
                - firstLocation.x) + (2.0 * Math.PI))
                % (2.0 * Math.PI);
    }
}
