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

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Rotation manipulation point, holding the last location of a
 * point that, in being dragged by the user, is moving a drawable.
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
public class MovementManipulationPoint extends ManipulationPoint {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param drawable
     *            Drawable to which this point applies.
     * @param location
     *            Last location of the point being dragged by the user.
     */
    public MovementManipulationPoint(AbstractDrawableComponent drawable,
            Coordinate location) {
        super(drawable, location);
    }

    // Public Methods

    @Override
    public CursorType getCursor() {
        return CursorType.MOVE_SHAPE_CURSOR;
    }

    @Override
    public String toString() {
        return "movement";
    }
}
