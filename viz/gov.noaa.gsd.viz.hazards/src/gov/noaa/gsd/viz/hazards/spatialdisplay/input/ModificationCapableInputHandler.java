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
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ManipulationPoint;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import java.awt.Color;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Base class for input handlers used to modify drawables (possibly
 * among other things). The generic parameter <code>M</code> provides the type
 * of the manipulation point that this input handler may utilize.
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
public class ModificationCapableInputHandler<M extends ManipulationPoint>
        extends NonDrawingInputHandler {

    // Private Static Constants

    /**
     * Colors for the ghost drawable.
     */
    Color[] GHOST_COLORS = new Color[] { Color.WHITE, Color.WHITE };

    // Private Variables

    /**
     * Ghost drawable.
     */
    private AbstractDrawableComponent ghostDrawable;

    /**
     * Manipulation point of a drawable over which the mouse is hovering; if
     * <code>null</code>, it is not over a manipulation point.
     */
    private M manipulationPointUnderMouse;

    /**
     * Coordinate of the previous cursor location in lat-lon coordinates.
     */
    private Coordinate previousLocation;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public ModificationCapableInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void reset() {
        super.reset();
        finalizeMouseHandling();
    }

    // Protected Methods

    /**
     * Get the ghost drawable.
     * 
     * @return Ghost drawable.
     */
    protected AbstractDrawableComponent getGhostDrawable() {
        return ghostDrawable;
    }

    /**
     * Clear the ghost drawable.
     */
    protected void clearGhostDrawable() {
        ghostDrawable = null;
    }

    /**
     * Create a ghost drawable of the specified drawable.
     * 
     * @param drawableBeingEdited
     *            Drawable that is being edited which needs a ghost.
     */
    protected void createGhostDrawable(
            AbstractDrawableComponent drawableBeingEdited) {
        ghostDrawable = ((IDrawable<?>) drawableBeingEdited).copyOf();
        ghostDrawable.setColors(GHOST_COLORS);
    }

    /**
     * Get the manipulation point under the mouse, if any.
     * 
     * @return Manipulation point under the mouse, or <code>null</code> if there
     *         is none.
     */
    protected M getManipulationPointUnderMouse() {
        return manipulationPointUnderMouse;
    }

    /**
     * Clear the manipulation point under the mouse.
     */
    protected void clearManipulationPointUnderMouse() {
        manipulationPointUnderMouse = null;
    }

    /**
     * Set the manipulation point under the mouse to that specified.
     * 
     * @param manipulationPoint
     *            Manipulation point under the mouse.
     */
    protected void setManipulationPointUnderMouse(M manipulationPoint) {
        manipulationPointUnderMouse = manipulationPoint;
    }

    /**
     * Get the location in latitude-longitude coordinates of the specified point
     * in pixel space, returning the last-calculated location if this
     * pixel-space coordinate does not translate to latitude-longitude space.
     * 
     * @param x
     *            X coordinate in pixel space.
     * @param y
     *            Y coordinate in pixel space.
     * @return Location in latitude-longitude coordinates, or <code>null</code>
     *         if no latitude-longitude location could be calculated, nor has
     *         one been calculated by this handler previously.
     */
    protected Coordinate getLocationFromPixels(int x, int y) {

        /*
         * Get the cursor location in lat-lon coordinates.
         */
        Coordinate location = translatePixelToWorld(x, y);

        /*
         * Check for a null coordinate. If it is null use the previous
         * coordinate. If not save it off as the previous.
         */
        if (location == null) {
            if (previousLocation != null) {
                location = previousLocation;
            } else {
                return null;
            }
        } else {
            previousLocation = location;
        }

        return location;
    }

    /**
     * Finish up whatever modification operation is in process.
     */
    protected void finalizeMouseHandling() {

        getSpatialDisplay().setGhostOfDrawableBeingEdited(null);
        clearGhostDrawable();

        getSpatialDisplay().setDrawableBeingEdited(null);

        clearManipulationPointUnderMouse();

        getSpatialDisplay().visualCuesNeedUpdatingAtNextRefresh();
        getSpatialDisplay().issueRefresh();
    }

}
