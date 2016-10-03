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

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.MovementManipulationPoint;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Input handler that allows the user to move a drawable.
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
public class MoveInputHandler extends
        ModificationInputHandler<MovementManipulationPoint> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public MoveInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Protected Methods

    @Override
    protected boolean isEditableViaHandler(AbstractDrawableComponent drawable) {
        return ((IDrawable<?>) drawable).isMovable();
    }

    @Override
    protected void modifyDrawableForManipulationPointMove(
            AbstractDrawableComponent drawable,
            MovementManipulationPoint manipulationPoint, Coordinate newLocation) {

        /*
         * Get the offset between the last position during the move of the click
         * point and the current one. Also remember the current position for
         * next time.
         */
        Coordinate lastMoveLocation = manipulationPoint.getLocation();
        double deltaX = newLocation.x - lastMoveLocation.x;
        double deltaY = newLocation.y - lastMoveLocation.y;
        lastMoveLocation.x = newLocation.x;
        lastMoveLocation.y = newLocation.y;

        /*
         * Offset the ghost drawable.
         */
        ((IDrawable<?>) drawable).offsetBy(deltaX, deltaY);
    }
}
