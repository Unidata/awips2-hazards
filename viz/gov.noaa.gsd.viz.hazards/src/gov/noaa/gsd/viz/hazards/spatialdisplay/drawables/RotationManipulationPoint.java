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
 * Description: Rotation manipulation point, holding the location of a point
 * that, if dragged by the user, allows a drawable to be rotated.
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
public class RotationManipulationPoint extends CenterRelativeManipulationPoint {

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
     */
    public RotationManipulationPoint(AbstractDrawableComponent drawable,
            Coordinate location, Coordinate center) {
        super(drawable, location, center);
    }

    // Public Methods

    @Override
    public CursorType getCursor() {
        return CursorType.ROTATE_CURSOR;
    }

    @Override
    public String toString() {
        return "rotation";
    }
}
