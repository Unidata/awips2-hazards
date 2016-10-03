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
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;

import org.eclipse.swt.SWT;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Input handler for using a rectangular selection shape to select
 * drawables in the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation (adapted from the old
 *                                      RectangleMultiSelectionAction inner
 *                                      class).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class RectangleMultiSelectionInputHandler extends
        MultiSelectionInputHandler {

    // Private Variables

    /**
     * Anchor point, the starting point of the rectangular shape.
     */
    private Coordinate anchorPoint;

    /**
     * Drag point, the point dragged to the diagonally opposite corner of the
     * <code>anchorPoint</code> to create the rectangular shape.
     */
    private Coordinate dragPoint;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public RectangleMultiSelectionInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay, SWT.SHIFT);
    }

    // Protected Methods

    @Override
    protected boolean handlePotentialSelectionByShapeStart(Coordinate location) {
        if (anchorPoint == null) {
            anchorPoint = location;
            return true;
        }
        return false;
    }

    @Override
    protected void handleSelectionByShapeNewPoint(Coordinate location) {
        if (location != null) {
            dragPoint = location;
        }
    }

    @Override
    protected void handleSelectionByShapeReset() {
        anchorPoint = null;
        dragPoint = null;
    }

    @Override
    protected boolean isSelectionShapeValid() {
        return ((anchorPoint != null) && (dragPoint != null));
    }

    @Override
    protected Coordinate[] getPointsOfSelectionShape() {
        return getGeometryOfSelectionShape().getCoordinates();
    }

    @Override
    protected Geometry getGeometryOfSelectionShape() {
        Envelope envelope = new Envelope(anchorPoint, dragPoint);
        return AdvancedGeometryUtilities.getGeometryFactory().toGeometry(
                envelope);
    }
}
