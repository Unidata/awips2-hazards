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

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Freehand drawing input handler, for drawing freehand lines and
 * polygons within the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation (adapted from the old
 *                                      FreeHandHazardDrawingAction inner class).
 * Sep 21, 2016   15934    Chris.Golden Changed to use new superclass.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class FreehandDrawingInputHandler extends IncrementalDrawingInputHandler {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public FreehandDrawingInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public boolean handleMouseDown(int x, int y, int button) {
        return addLocationToPoints(x, y, button, false);
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {
        return addLocationToPoints(x, y, button, true);
    }

    @Override
    public boolean handleMouseUp(int x, int y, int button) {
        if (addLocationToPoints(x, y, button, false) == false) {
            return false;
        }

        /*
         * Hand the list of points off to the spatial display.
         */
        List<Coordinate> points = getPoints();
        List<Coordinate> pointsCopy = new ArrayList<Coordinate>(points);
        points.clear();
        getSpatialDisplay().handleUserMultiPointDrawingActionComplete(
                getShapeType(), new ArrayList<Coordinate>(pointsCopy));
        return true;
    }

    // Private Methods

    /**
     * If the specified button is the left one, add the specified mouse location
     * to the list of points, and display a ghost drawable if this results in
     * more than one point being in said list.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @param button
     *            Number of the button pressed or released.
     * @param showGhost
     *            Flag indicating whether or not to display a ghost element
     *            showing the shape being created.
     * @return <code>true</code> if the point was added, <code>false</code>
     *         otherwise.
     */
    private boolean addLocationToPoints(int x, int y, int button,
            boolean showGhost) {

        /*
         * Get the world location.
         */
        Coordinate location = getLocationFromPixel(x, y, button, false);
        if (location == null) {
            return false;
        }

        /*
         * Add the location to the list of points, and if it is more than a
         * single point and a ghost drawable should be shown, create the latter
         * for the spatial display to display. Otherwise, remove any ghost
         * drawable.
         */
        addPointIfNotIdenticalToPreviousPoint(location);
        if ((getPoints().size() > 1) && showGhost) {
            showGhost(null);
        } else {
            hideGhost();
        }
        getSpatialDisplay().issueRefresh();
        return true;
    }
}
