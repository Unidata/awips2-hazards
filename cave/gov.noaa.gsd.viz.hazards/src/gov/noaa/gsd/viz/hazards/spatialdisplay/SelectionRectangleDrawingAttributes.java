/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.exception.VizException;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * <Description> The PGEN drawing attributes associated with a polygon drawn on
 * the Spatial Display in Hazard Services. All drawables in Hazard Services are
 * rendered using PGEN drawing classes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class SelectionRectangleDrawingAttributes extends
        HazardServicesDrawingAttributes {

    public static int SMOOTH_FACTOR = 0;

    public static double SIZE_SCALE = 7.5;

    private float lineWidth = 2.0f;

    private Color[] colors = new Color[] { Color.WHITE, Color.WHITE };

    private final boolean filled = false;

    private String lineStyle = "LINE_SOLID";

    private boolean selected = false;

    public SelectionRectangleDrawingAttributes(Shell parShell)
            throws VizException {
        super(parShell);
    }

    @Override
    public void setAttrForDlg(IAttribute ia) {
    }

    /**
     * Returns the smoothing factor used to draw polygons.
     * 
     * @return int
     * 
     */
    @Override
    public int getSmoothFactor() {
        return SMOOTH_FACTOR;
    }

    /**
     * Sets the line width of the drawn event polygon border
     * 
     * @return float
     * 
     */
    @Override
    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Sets flag indicating whether or not this event drawable shoulf be closed.
     * 
     * @return Boolean
     */
    @Override
    public Boolean isClosedLine() {
        return true;
    }

    @Override
    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    /**
     * Returns a flag indicating whether or not to fill a polygon.
     * 
     * @return Boolean
     */
    @Override
    public Boolean isFilled() {
        return filled;
    }

    /**
     * Returns the fill pattern to use in drawing a event polgyon.
     * 
     * @return FillPattern
     * 
     * @see FillPattern
     */
    @Override
    public FillPattern getFillPattern() {
        return FillPattern.FILL_PATTERN_5;
    }

    public void setSOLIDLineStyle() {
        this.lineStyle = "LINE_SOLID";
    }

    public void setDASHEDLineStyle() {
        this.lineStyle = "LINE_DASHED_2";
    }

    @Override
    public String getLineStyle() {
        return lineStyle;
    }

    @Override
    public double getSizeScale() {
        return SIZE_SCALE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Coordinate> updateFromEventDict(Dict shape) {

        List<Object> pointsArray = (List<Object>) shape.get("points");

        ArrayList<Coordinate> points = new ArrayList<Coordinate>();

        for (int j = 0; j < pointsArray.size(); ++j) {
            List<Number> coords = (List<Number>) pointsArray.get(j);
            double lon = coords.get(0).doubleValue();
            double lat = coords.get(1).doubleValue();
            Coordinate coord = new Coordinate(lon, lat, 0);
            points.add(coord);
        }

        Color[] colors = new Color[] { Color.BLACK, Color.BLACK };
        int borderThickness = ((Number) shape.get("border thick")).intValue();
        String fillcolor = (String) shape.get("fill color");
        String borderStyle = (String) shape.get("borderStyle");
        String borderColor = (String) shape.get("border color");
        String label = (String) shape.get("label");

        colors[0] = ToolLayer.convertRGBStringToColor(borderColor);
        colors[1] = ToolLayer.convertRGBStringToColor(fillcolor);

        Boolean selected = ((Boolean) shape.get("isSelected"));

        if (selected != null) {
            setSelected(selected);
        }

        setLineWidth(borderThickness);

        if (label != null && label.length() > 0) {
            setString(new String[] { label });
        } else {
            setString(null);
        }

        switch (BorderStyles.valueOf(borderStyle)) {

        case SOLID:
            setSOLIDLineStyle();
            break;

        case DASHED:
            setDASHEDLineStyle();
            break;

        case NONE:
            // Nothing to do at the moment.
            break;
        }

        setColors(colors);

        return points;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
