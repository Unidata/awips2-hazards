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
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Description: Input handler for using a freehand-drawn selection shape to
 * select drawables in the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Golden Initial creation (adapted from the old
 *                                      FreeHandMultiSelectionAction inner
 *                                      class).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class FreehandMultiSelectionInputHandler extends
        MultiSelectionInputHandler {

    // Private Variables

    /**
     * List of points making up the freehand shape.
     */
    private final List<Coordinate> points = new ArrayList<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public FreehandMultiSelectionInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay, SWT.CTRL);
    }

    // Protected Methods

    @Override
    protected boolean handlePotentialSelectionByShapeStart(Coordinate location) {
        if (points.isEmpty()) {
            points.add(location);
            return true;
        }
        return false;
    }

    @Override
    protected void handleSelectionByShapeNewPoint(Coordinate location) {
        if (location != null) {
            points.add(location);
        }
    }

    @Override
    protected void handleSelectionByShapeReset() {
        points.clear();
    }

    @Override
    protected boolean isSelectionShapeValid() {
        return (points.size() > 1);
    }

    @Override
    protected Coordinate[] getPointsOfSelectionShape() {
        return points.toArray(new Coordinate[points.size()]);
    }

    @Override
    protected Geometry getGeometryOfSelectionShape() {
        Utilities.closeCoordinatesIfNecessary(points);
        LinearRing linearRing = new GeometryFactory().createLinearRing(points
                .toArray(new Coordinate[points.size()]));
        return getGeometryFactory().createPolygon(linearRing, null);
    }
}
