/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.TextPositioner;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Base class for Text drawn in Hazard Services.
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
public class HazardServicesText extends Text implements IHazardServicesShape {
    private String eventID = null;

    public HazardServicesText() {
        super();
    }

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
     * @param textCoord
     *            The coordinate to plot the text at.
     * @param points
     *            The list points defining this drawable.
     * @param activeLayer
     *            The PGEN layer this will be drawn to.
     * @param eventID
     *            The eventID associated with this drawable.
     */
    public HazardServicesText(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, Coordinate textCoord,
            Layer activeLayer, String eventID) {
        this();
        this.eventID = eventID;
        update(drawingAttributes);
        setPgenCategory(pgenCategory);
        setPgenType(pgenType);
        setParent(activeLayer);
        setText(new String[] { pgenCategory });
        setColors(drawingAttributes.getColors());

        // Allow the TextPositioner to adjust the label's location
        // relative to the centroid of the hazard area.
        TextPositioner textPositioner = drawingAttributes.getTextPosition();
        Coordinate labelCoord = textPositioner.getLabelPosition(textCoord);
        setLocation(labelCoord);

    }

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
     * @param eventID
     *            The eventID associated with this drawable.
     */
    public HazardServicesText(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, ArrayList<Coordinate> points,
            Layer activeLayer, String eventID) {
        this(drawingAttributes, pgenCategory, pgenType, points.get(0),
                activeLayer, eventID);
    }

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
     * @param locationPoint
     *            The coordinate to plot the text at.
     * @param points
     *            The list points defining this drawable.
     * @param activeLayer
     *            The PGEN layer this will be drawn to.
     * @param eventID
     *            The eventID associated with this drawable.
     */
    public HazardServicesText(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, Point locationPoint,
            Layer activeLayer, String eventID) {
        this(drawingAttributes, pgenCategory, pgenType, locationPoint
                .getCoordinate(), activeLayer, eventID);
    }

    @Override
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    @Override
    public String getEventID() {
        return eventID;
    }

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.NORMAL;
    }

    @Override
    public Boolean maskText() {
        return false;
    }

    @Override
    public float getFontSize() {
        return 15;
    }

    @Override
    public boolean canVerticesBeEdited() {
        return false;
    }

    @Override
    public Polygon getPolygon() {
        return null;
    }

}
