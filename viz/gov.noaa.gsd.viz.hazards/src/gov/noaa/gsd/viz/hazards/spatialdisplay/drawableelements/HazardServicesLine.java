/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.util.List;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Base class for polygon Hazard-Geometries in Hazard Services.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardServicesLine extends HazardServicesShape {

    private final LineString geometry;

    /**
     * 
     * @param drawingAttributes
     *            Attributes associated with this drawable.
     * @param pgenCategory
     *            The PGEN category of this drawable. Not used by Hazard
     *            Services but required by PGEN.
     * @param pgenType
     *            The PGEN type of this drawable. Not used by Hazard Services
     *            but required by PGEN.
     * @param points
     *            The list points defining this drawable.
     * @param activeLayer
     *            The PGEN layer this will be drawn to.
     * @param id
     *            The id associated with this drawable.
     */
    public HazardServicesLine(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, List<Coordinate> points,
            Layer activeLayer, String id) {
        super(id, drawingAttributes);
        setLinePoints(points);
        update(drawingAttributes);
        setPgenCategory(pgenCategory);
        setPgenType(pgenType);
        setParent(activeLayer);

        GeometryFactory gf = new GeometryFactory();

        List<Coordinate> drawnPoints = Lists.newArrayList();

        for (Coordinate coord : points) {
            drawnPoints.add((Coordinate) coord.clone());
        }

        geometry = gf.createLineString(drawnPoints
                .toArray(new Coordinate[drawnPoints.size()]));
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public LineString getEditableVertices() {
        return geometry;
    }

}
