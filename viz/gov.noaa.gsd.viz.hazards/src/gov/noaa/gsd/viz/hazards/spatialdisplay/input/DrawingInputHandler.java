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
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.DrawableAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.LineDrawableAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.PolygonDrawableAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.SymbolDrawableAttributes;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Event;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Base class from which to derive classes used to handle input in
 * drawing modes for the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation.
 * Aug 28, 2016   19537    Chris.Golden Changed to build drawables
 *                                      without a PGEN layer.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class DrawingInputHandler extends BaseInputHandler {

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

    /**
     * Shape type to be drawn.
     */
    private GeometryType shapeType;

    /**
     * Map of shape types to drawing attributes.
     */
    private final Map<GeometryType, DrawableAttributes> drawingAttributesForShapeTypes = new HashMap<>();

    /**
     * Drawable element factory, used to create linear and polygonal ghost
     * shapes while drawing.
     */
    private final DrawableElementFactory drawableFactory = new DrawableElementFactory();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public DrawingInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void reset() {
        getSpatialDisplay().setCursor(CursorTypes.DRAW_CURSOR);
        if (points.isEmpty() == false) {
            points.clear();
            hideGhost();
            getSpatialDisplay().issueRefresh();
        }
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        getSpatialDisplay().setCursor(CursorTypes.DRAW_CURSOR);
        return true;
    }

    /**
     * Set the shape type.
     * 
     * @param shapeType
     *            Shape type.
     */
    public void setShapeType(GeometryType shapeType) {
        this.shapeType = shapeType;
        if (drawingAttributesForShapeTypes.get(shapeType) == null) {
            DrawableAttributes drawingAttributes = null;
            if (this.shapeType == GeometryType.LINE) {
                drawingAttributes = new LineDrawableAttributes();
            } else if (this.shapeType == GeometryType.POLYGON) {
                drawingAttributes = new PolygonDrawableAttributes(false);
            } else {
                drawingAttributes = new SymbolDrawableAttributes();
            }
            drawingAttributesForShapeTypes.put(this.shapeType,
                    drawingAttributes);
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
     * Get the current shape type.
     * 
     * @return Curernt shape type.
     */
    protected final GeometryType getShapeType() {
        return shapeType;
    }

    /**
     * Get the drawing attributes for the current shape type.
     * 
     * @return Drawing attributes for the current shape type.
     */
    protected final DrawableAttributes getDrawingAttributes() {
        return drawingAttributesForShapeTypes.get(shapeType);
    }

    /**
     * Get the drawable element factory.
     * 
     * @return Drawable element factory.
     */
    protected final DrawableElementFactory getDrawableFactory() {
        return drawableFactory;
    }

    /**
     * If the specified button is the left button (or optionally the right
     * button as well), translate the specified pixel coordinates to world
     * coordinates.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @param button
     *            Number of the button pressed or released.
     * @param allowRightButton
     *            Flag indicating whether or not the right button is allowed as
     *            well as the left button. If <code>false</code>, only the left
     *            button is allowed.
     * @return World coordinates, or <code>null</code> if the button is not one
     *         of the allowed buttons, or if the pixel coordinates are not
     *         within the geographic extent.
     */
    protected final Coordinate getLocationFromPixel(int x, int y, int button,
            boolean allowRightButton) {

        /*
         * Ignore anything but left (and if appropriate, right) button events.
         */
        if ((button != 1) && ((allowRightButton == false) || (button != 3))) {
            return null;
        }

        /*
         * Get the world location.
         */
        return translatePixelToWorld(x, y);
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
