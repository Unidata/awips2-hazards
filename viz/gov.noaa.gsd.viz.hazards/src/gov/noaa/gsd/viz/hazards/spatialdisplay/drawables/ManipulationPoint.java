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
 * Description: Manipulation point, a base class for points specified in
 * latitude-longitude coordinates that, if dragged by the user, allow a
 * {@link IDrawable} to be manipulated in some fashion.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 28, 2016   15928    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class ManipulationPoint {

    // Private Variables

    /**
     * Drawable to which this point applies.
     */
    private final AbstractDrawableComponent drawable;

    /**
     * Location of the point.
     */
    private final Coordinate location;

    // Public Methods

    /**
     * Construct a standard instance.
     * 
     * @param drawable
     *            Drawable to which this point applies.
     * @param location
     *            Location of the point.
     */
    public ManipulationPoint(AbstractDrawableComponent drawable,
            Coordinate location) {
        this.drawable = drawable;
        this.location = location;
    }

    // Public Methods

    /**
     * Get the drawable to which this point applies.
     * 
     * @return Drawable to which this point applies.
     */
    public AbstractDrawableComponent getDrawable() {
        return drawable;
    }

    /**
     * Get the location.
     * 
     * @return Location.
     */
    public Coordinate getLocation() {
        return location;
    }

    /**
     * Get the mouse cursor to be used with this point.
     * 
     * @return Mouse cursor to be used with this point.
     */
    public abstract CursorType getCursor();
}
