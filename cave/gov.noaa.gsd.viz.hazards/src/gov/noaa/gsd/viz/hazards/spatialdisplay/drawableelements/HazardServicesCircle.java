/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.CircleDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import java.util.List;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Base class for circle drawables in Hazard Services.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 2011              Bryon.Lawrence    Initial creation
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardServicesCircle extends Line implements IHazardServicesShape {

    private String eventID = null;

    /**
     * This is an instance of JTS polygon matching this PGEN-based polygon. This
     * is used for JTS utilitis such as determining whether or not a click point
     * is within a polygon.
     */
    private Polygon polygon = null;

    public HazardServicesCircle() {
        super();
    }

    /**
     * 
     * @param drawingAttributes
     *            Attributes associated with this circle.
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
     * @param eventID
     *            The eventID associated with this drawable.
     */
    public HazardServicesCircle(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, List<Coordinate> points,
            Layer activeLayer, String eventID) {
        this();
        this.eventID = eventID;
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

        drawnPoints.add(drawnPoints.get(0));
        LinearRing ls = gf.createLinearRing(drawnPoints
                .toArray(new Coordinate[0]));
        polygon = gf.createPolygon(ls, null);
    }

    @Override
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    @Override
    public String getEventID() {
        return eventID;
    }

    public long getPointID() {
        CircleDrawingAttributes attributes = (CircleDrawingAttributes) getAttr();
        return attributes.getPointID();
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    @Override
    public Polygon getPolygon() {
        return polygon;
    }

    @Override
    public boolean canVerticesBeEdited() {
        return false;
    }

}
