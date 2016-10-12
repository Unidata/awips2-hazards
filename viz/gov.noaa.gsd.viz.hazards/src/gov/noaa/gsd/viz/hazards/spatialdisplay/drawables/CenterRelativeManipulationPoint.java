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

import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Base class for manipulation points that allow for actions that
 * are relative to the center of the drawable being manipulated.
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
public abstract class CenterRelativeManipulationPoint extends ManipulationPoint {

    // Private Variables

    /**
     * Center of the shape.
     */
    private final Coordinate center;

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
    public CenterRelativeManipulationPoint(AbstractDrawableComponent drawable,
            Coordinate location, Coordinate center) {
        super(drawable, location);
        this.center = center;
    }

    // Public Methods

    /**
     * Get the center of the shape.
     * 
     * @return Center of the shape.
     */
    public Coordinate getCenter() {
        return center;
    }
}
