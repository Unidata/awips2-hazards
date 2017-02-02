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

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Base class from which to derive classes used to handle input in
 * scaling drawing modes for the spatial display. Scaling drawing is drawing of
 * shapes that start with a single click and release (for example providing a
 * corner for a rectangle, or a center point for a circle) and then a second
 * single click and release (providing the diagonally opposite corner for a
 * rectangle, or a circumferential point for a circle, to continue the examples
 * given above). This drawing process inherently includes scaling of the shape
 * being drawn, since the two points placed define the size (and for some shapes
 * the width to height ratio) of the shape.
 * <p>
 * Subclasses must handle the creation of drawables based upon two points, the
 * initial point placed and a second point as specified at any given time.
 * </p>
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
public abstract class ScalingDrawingInputHandler extends DrawingInputHandler {

    // Private Variables

    /**
     * First coordinate placed during the drawing of the shape.
     */
    private Coordinate firstLocation;

    /**
     * Second coordinate placed during the drawing of the shape.
     */
    private Coordinate secondLocation;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public ScalingDrawingInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void reset() {
        super.reset();
        if (firstLocation != null) {
            firstLocation = null;
            secondLocation = null;
            hideGhost();
            getSpatialDisplay().issueRefresh();
        }
    }

    @Override
    public boolean handleMouseMove(int x, int y) {

        /*
         * Do nothing if there is no first point recorded yet.
         */
        if (firstLocation == null) {
            return false;
        }

        /*
         * Get the cursor location in lat-lon coordinates, which is to be used
         * as the second location. If it cannot be translated, or it is equal to
         * the first location, hide any ghost showing.
         */
        Coordinate location = getLocationFromPixel(x, y, 1, false);
        if ((location == null) || location.equals(firstLocation)) {
            hideGhost();
            return true;
        }

        /*
         * If the second location has not changed, do nothing more; otherwise,
         * use it.
         */
        if ((secondLocation != null) && secondLocation.equals(location)) {
            return true;
        }
        secondLocation = location;

        /*
         * Show the ghost drawable.
         */
        showGhost();
        getSpatialDisplay().issueRefresh();
        return true;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int button) {

        /*
         * Get the cursor location.
         */
        Coordinate location = getLocationFromPixel(x, y, button, true);
        if (location == null) {
            return false;
        }

        /*
         * If this is the first location being placed, remember it and do
         * nothing more.
         */
        if (firstLocation == null) {
            firstLocation = location;
            return true;
        }

        /*
         * If the second location is the same as the first, cancel the edit.
         */
        hideGhost();
        if (location.equals(firstLocation)) {
            firstLocation = secondLocation = null;
            getSpatialDisplay().handleUserDrawingActionComplete(null);
            return true;
        }

        /*
         * Create the geometry that is to be used as the new shape, and hand it
         * off to the spatial display.
         */
        IAdvancedGeometry geometry = createGeometryFromPoints(firstLocation,
                location);
        firstLocation = secondLocation = null;
        getSpatialDisplay().handleUserDrawingActionComplete(geometry);
        return true;
    }

    // Protected Methods

    /**
     * Get the geometry to be used as a ghost, given the specified second point.
     * 
     * @param secondPoint
     *            Point to be used as the second point in the drawable.
     * @return Drawable to be used as a ghost.
     */
    protected abstract IAdvancedGeometry createGeometryFromPoints(
            Coordinate firstPoint, Coordinate secondPoint);

    /**
     * Show a ghost drawable providing a visual indicator of the shape drawn so
     * far.
     */
    protected final void showGhost() {
        ArrayList<Coordinate> ghostPoints = Lists
                .newArrayList(AdvancedGeometryUtilities
                        .getJtsGeometry(
                                createGeometryFromPoints(firstLocation,
                                        secondLocation)).getCoordinates());
        AbstractDrawableComponent ghost = getDrawableFactory().create(
                DrawableType.LINE, getDrawingAttributes(), "Line",
                "LINE_SOLID", ghostPoints, null);
        ((Line) ghost).setLinePoints(ghostPoints);
        getSpatialDisplay().setGhostOfDrawableBeingEdited(ghost);
    }

    /**
     * Hide any visible ghost drawable.
     */
    protected final void hideGhost() {
        getSpatialDisplay().setGhostOfDrawableBeingEdited(null);
    }
}
