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
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;

import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.input.InputAdapter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Description: Base class from which to derive input handlers for various
 * spatial display modes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden+
 * @version 1.0
 */
public abstract class BaseInputHandler extends InputAdapter {

    // Private Variables

    /**
     * Spatial display.
     */
    private final SpatialDisplay spatialDisplay;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public BaseInputHandler(SpatialDisplay spatialDisplay) {
        this.spatialDisplay = spatialDisplay;
    }

    // Public Methods

    /**
     * Reset the handler to starting state, in preparation for its use.
     * Subclasses should override this method to reinitialize member data.
     */
    public abstract void reset();

    // Protected Methods

    /**
     * Get the spatial display.
     * 
     * @return Spatial display.
     */
    protected final SpatialDisplay getSpatialDisplay() {
        return spatialDisplay;
    }

    /**
     * Translate the specified pixel coordinates to world coordinates.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return Translated coordinates, or <code>null</code> if the specified
     *         coordinates fall outside the geographic extent.
     */
    protected final Coordinate translatePixelToWorld(int x, int y) {
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        return editor.translateClick(x, y);
    }

    /**
     * Get the identifier for the specified drawable element.
     * 
     * @param drawable
     *            Drawable for which to fetch the identifier.
     * @return Identifier, or <code>null</code> if the specified drawable is not
     *         a {@link IDrawable}.
     */
    protected final IEntityIdentifier getIdentifierFromDrawable(
            AbstractDrawableComponent drawable) {
        if (drawable instanceof IDrawable) {
            return ((IDrawable) drawable).getIdentifier();
        }
        return null;
    }

    /**
     * Get the geometry for the specified drawable element.
     * 
     * @param drawable
     *            Drawable for which to fetch the geometry.
     * @return Geometry, or <code>null</code> if the specified drawable is not a
     *         {@link IDrawable}.
     */
    protected final Geometry getGeometryFromDrawable(
            AbstractDrawableComponent drawable) {
        if (drawable instanceof IDrawable) {
            return ((IDrawable) drawable).getGeometry();
        }
        return null;
    }
}
