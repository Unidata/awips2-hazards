/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static gov.noaa.gsd.viz.hazards.spatialdisplay.LineStyle.*;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;

import java.awt.Color;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * Description: Drawing attributes for a Hazard Services point.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2013     1264   Chris.Golden      Initial creation
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class PointDrawingAttributes extends HazardServicesDrawingAttributes {

    // Public Enumerated Types

    /**
     * Part of the point that a drawing attributes object is intended for.
     */
    public enum Element {
        INNER, OUTER
    };

    // Private Static Constants

    private static final double INNER_SIZE_SCALE = 5.5;

    private static final double OUTER_SIZE_SCALE = 8.5;

    private static final double OUTER_SELECTED_SIZE_SCALE = 10.5;

    private static final int SMOOTH_FACTOR = 0;

    // Private Variables

    private double sizeScale;

    private float lineWidth = 2.0f;

    private Color[] colors = new Color[] { Color.WHITE, Color.WHITE };

    private LineStyle lineStyle = LINE_SOLID;

    private Element element = Element.INNER;

    // Public Constructors

    /**
     * Construct a standard instance for an inner element.
     * 
     * @param parShell
     *            Parent shell.
     * @throws VizException
     *             If a viz exception occurs.
     */
    public PointDrawingAttributes(Shell parShell, ISessionManager sessionManager)
            throws VizException {
        this(parShell, sessionManager, Element.INNER);
    }

    /**
     * Construct a standard instance.
     * 
     * @param parShell
     *            Parent shell.
     * @param element
     *            Element of a point for which these attributes are intended.
     * @throws VizException
     *             If a viz exception occurs.
     */
    public PointDrawingAttributes(Shell parShell,
            ISessionManager sessionManager, Element element)
            throws VizException {
        super(parShell, sessionManager.getConfigurationManager());
        this.element = element;
    }

    // Public Methods

    @Override
    public void setAttrForDlg(IAttribute ia) {

        // No action.
    }

    @Override
    public int getSmoothFactor() {
        return SMOOTH_FACTOR;
    }

    @Override
    public float getLineWidth() {
        return lineWidth;
    }

    @Override
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public Boolean isClosedLine() {
        return true;
    }

    @Override
    public Color[] getColors() {
        return colors;
    }

    @Override
    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    @Override
    public Boolean isFilled() {
        return true;
    }

    @Override
    public FillPattern getFillPattern() {
        return FillPattern.FILL_PATTERN_5;
    }

    @Override
    public void setSOLIDLineStyle() {
        this.lineStyle = LINE_SOLID;
    }

    @Override
    public void setDASHEDLineStyle() {
        this.lineStyle = LINE_DASHED_4;
    }

    @Override
    public LineStyle getLineStyle() {
        return lineStyle;
    }

    @Override
    public double getSizeScale() {
        return sizeScale;
    }

    /**
     * Get the element of the point for which these attributes are intended.
     * 
     * @return Element of the point.
     */
    public Element getElement() {
        return element;
    }

    @Override
    public void setAttributes(int shapeNum, IHazardEvent hazardEvent) {
        super.setAttributes(shapeNum, hazardEvent);
        Boolean selected = (Boolean) hazardEvent
                .getHazardAttribute(ISessionEventManager.ATTR_SELECTED);

        if (element.equals(Element.INNER)) {
            sizeScale = INNER_SIZE_SCALE;
        } else if (selected) {
            sizeScale = OUTER_SELECTED_SIZE_SCALE;
        } else {
            sizeScale = OUTER_SIZE_SCALE;
        }

        if (element.equals(Element.OUTER)) {
            setColors(new Color[] { Color.WHITE, Color.WHITE });
        }

        if (element.equals(Element.INNER)) {
            /**
             * We want no label for the inner circle
             */
            setString(null);
        }

    }

    /**
     * TODO Handle MultiPoint
     */
    @Override
    public List<Coordinate> buildCoordinates(int shapeNum,
            IHazardEvent hazardEvent) {
        Boolean selected = (Boolean) hazardEvent
                .getHazardAttribute(ISessionEventManager.ATTR_SELECTED);
        int radius = 3;
        if (selected) {
            radius = 5;
        }
        Point point = (Point) hazardEvent.getGeometry();

        Coordinate centerCoordInPixels = worldToPixel(new Coordinate(
                point.getX(), point.getY()));

        Coordinate circumferenceCoordInPixels = new Coordinate(
                centerCoordInPixels.x - radius, centerCoordInPixels.y);

        Coordinate circumferenceCoordInWorld = pixelToWorld(circumferenceCoordInPixels);

        List<Coordinate> result = Lists.newArrayList();
        result.add(new Coordinate(point.getX(), point.getY(), 0));
        result.add(circumferenceCoordInWorld);
        return result;
    }
}
