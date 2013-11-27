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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Base class for Text drawn in Hazard Services.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 2011              Bryon.Lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Nov 23, 2013   1462     Bryon.Lawrence      Set text to bold.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardServicesText extends Text implements IHazardServicesShape {

    private final HazardServicesDrawingAttributes drawingAttributes;

    private String id;

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
     * @param id
     *            The id associated with this drawable.
     */
    public HazardServicesText(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, Coordinate textCoord,
            Layer activeLayer, String id) {
        this.id = id;
        this.drawingAttributes = drawingAttributes;
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

        setStyle(FontStyle.BOLD);

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
     * @param id
     *            The id associated with this drawable.
     */
    public HazardServicesText(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, ArrayList<Coordinate> points,
            Layer activeLayer, String id) {
        this(drawingAttributes, pgenCategory, pgenType, points.get(0),
                activeLayer, id);
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
     * @param id
     *            The id associated with this drawable.
     */
    public HazardServicesText(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, Point locationPoint,
            Layer activeLayer, String id) {
        this(drawingAttributes, pgenCategory, pgenType, locationPoint
                .getCoordinate(), activeLayer, id);
    }

    @Override
    public HazardServicesDrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    @Override
    public String getID() {
        return id;
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
    public LineString getEditableVertices() {
        return null;
    }

    @Override
    public Geometry getGeometry() {
        return null;
    }

    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setIsEditable(boolean isEditable) {
        throw new UnsupportedOperationException("Text is never editable");
    }

}
