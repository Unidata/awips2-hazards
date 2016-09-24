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
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Base class from which to derive classes used to handle input in
 * incremental drawing modes for the spatial display. Incremental drawing means
 * placing a series of points sequentially, building up one or more shapes
 * incrementally.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 21, 2016   15934    Chris.Golden Initial creation (refactored out of
 *                                      DrawingInputHandler).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class IncrementalDrawingInputHandler extends DrawingInputHandler {

    // Private Variables

    /**
     * List of world coordinates that are part of the shape being drawn.
     * <p>
     * Note that this is an <code>ArrayList</code> instead of a
     * <code>List</code> because PGEN's
     * {@link DrawableElementFactory#create(DrawableType, gov.noaa.nws.ncep.ui.pgen.display.IAttribute, String, String, ArrayList, AbstractDrawableComponent)}
     * method requires an <code>ArrayList</code>.
     */
    private final ArrayList<Coordinate> points = new ArrayList<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public IncrementalDrawingInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void reset() {
        super.reset();
        if (points.isEmpty() == false) {
            points.clear();
            hideGhost();
            getSpatialDisplay().issueRefresh();
        }
    }

    // Protected Methods

    /**
     * Get the points placed during the ongoing drawing operation.
     * 
     * @return Points placed during the ongoing drawing operation.
     */
    protected final ArrayList<Coordinate> getPoints() {
        return points;
    }

    /**
     * Show a ghost drawable providing a visual indicator of the shape drawn so
     * far.
     * 
     * @param location
     *            Point to add to the end of the list of points for the purposes
     *            of creating the ghost drawable; if <code>null</code>, no extra
     *            point is added.
     */
    protected final void showGhost(Coordinate location) {
        ArrayList<Coordinate> ghostPoints = new ArrayList<>(points);
        if (location != null) {
            ghostPoints.add(location);
        }
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

    /**
     * Add the specified point to the points list if it is not the same as the
     * previous point (if the list is not empty).
     * 
     * @param point
     *            New point to be added.
     * @return <code>true</code> if the point was added, <code>false</code> if
     *         it was found to be identical to the previous point and skipped.
     */
    protected final boolean addPointIfNotIdenticalToPreviousPoint(
            Coordinate point) {
        if (points.isEmpty()
                || (points.get(points.size() - 1).equals(point) == false)) {
            points.add(point);
            return true;
        }
        return false;
    }
}
