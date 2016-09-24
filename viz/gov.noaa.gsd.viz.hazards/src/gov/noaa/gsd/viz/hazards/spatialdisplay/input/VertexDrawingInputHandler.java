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
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.SequencePosition;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Vertex drawing input handler, for drawing points, lines and
 * polygons with explicit point placement within the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation (adapted from the old
 *                                      VertexHazardDrawingAction inner class).
 * Sep 21, 2016   15934    Chris.Golden Changed to use new superclass.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VertexDrawingInputHandler extends IncrementalDrawingInputHandler {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public VertexDrawingInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void reset() {
        if (GeometryType.POINT.equals(getShapeType())
                && (getPoints().isEmpty() == false)) {
            getSpatialDisplay().handleUserEndSequenceOfPointShapeCreation();
        }
        super.reset();
    }

    @Override
    public boolean handleMouseMove(int x, int y) {

        /*
         * Do nothing if there are no points recorded as of yet, or if the
         * geometries being created (if any) are points.
         */
        if (getPoints().isEmpty() || (getShapeType() == GeometryType.POINT)) {
            return false;
        }

        /*
         * Get the cursor location.
         */
        Coordinate location = getLocationFromPixel(x, y, 1, false);
        if (location == null) {
            return false;
        }

        /*
         * Show the ghost drawable.
         */
        showGhost(location);
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
         * Add the point to the list of points, if not identical to the previous
         * one.
         */
        if (addPointIfNotIdenticalToPreviousPoint(location) == false) {
            return true;
        }

        /*
         * If individual point geometries are being created, create one now,
         * indicating whether it is the first one in a sequence, one of the
         * middle ones, or the last one, depending upon which mouse button was
         * released and how many points there are in the list. If a line or
         * polygon is being created and the right mouse button was released,
         * hide any ghost drawable and hand the list of points off to the
         * spatial display.
         */
        if (getShapeType() == GeometryType.POINT) {
            getSpatialDisplay().handleUserCreationOfPointShape(
                    location,
                    (button == 3 ? SequencePosition.LAST
                            : (getPoints().size() == 1 ? SequencePosition.FIRST
                                    : SequencePosition.INTERIOR)));
            if (button == 3) {
                getPoints().clear();
            }
        } else if (button == 3) {
            hideGhost();
            List<Coordinate> pointsCopy = new ArrayList<>(getPoints());
            getPoints().clear();
            getSpatialDisplay().handleUserMultiPointDrawingActionComplete(
                    getShapeType(), pointsCopy);
        }
        return true;
    }
}
