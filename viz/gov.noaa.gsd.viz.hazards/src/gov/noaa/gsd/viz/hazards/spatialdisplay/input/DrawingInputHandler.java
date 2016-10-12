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
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay.CursorType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.DrawableAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.LineDrawableAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.PolygonDrawableAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.SymbolDrawableAttributes;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;

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
 * Aug 28, 2016   19537    Chris.Golden Changed to build drawables without a
 *                                      PGEN layer.
 * Sep 21, 2016   15934    Chris.Golden Extracted incremental-drawing elements
 *                                      to put in new interim subclass
 *                                      IncrementalDrawingInputHandler, thus
 *                                      allowing this class to be the super-
 *                                      class of the new ScalingDrawingInput-
 *                                      Handler.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class DrawingInputHandler extends BaseInputHandler {

    // Private Variables

    /**
     * Shape type to be drawn.
     */
    private GeometryType shapeType;

    /**
     * Map of shape types to drawing attributes.
     */
    private final Map<GeometryType, DrawableAttributes> drawingAttributesForShapeTypes = new HashMap<>();

    /**
     * Drawable element factory, used to create ghost shapes while drawing.
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
        getSpatialDisplay().setCursor(CursorType.DRAW_CURSOR);
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        getSpatialDisplay().setCursor(CursorType.DRAW_CURSOR);
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
}
