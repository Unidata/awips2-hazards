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

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * The PGEN drawing attributes associated with a circle drawn on the Spatial
 * Display in Hazard Services. All drawables in Hazard Services are rendered
 * using PGEN drawing classes.
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
public class CircleDrawingAttributes extends HazardServicesDrawingAttributes {

    public static double SIZE_SCALE = 7.5f;

    public static int SMOOTH_FACTOR = 0;

    private float lineWidth = 2.0f;

    private Color[] colors = new Color[] { Color.WHITE, Color.WHITE };

    private final boolean filled = true;

    private String lineStyle = "LINE_SOLID";

    private long pointID = 0;

    public CircleDrawingAttributes(Shell parShell) throws VizException {
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
     * Sets flag indicating whether or not this event drawable should be closed.
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
        this.lineStyle = "LINE_DASHED_4";
    }

    @Override
    public String getLineStyle() {
        return lineStyle;
    }

    @Override
    public double getSizeScale() {
        return SIZE_SCALE;
    }

    @Override
    public List<Coordinate> updateFromEventDict(Dict shape) {
        // Test for a pointID. There may or may not be one
        // depending on what this circle is part of...
        Number obj = shape.getDynamicallyTypedValue("pointID");

        if (obj != null) {
            pointID = obj.longValue();
        }

        List<Number> pointsArray = shape
                .getDynamicallyTypedValue("centerPoint");

        ArrayList<Coordinate> points = new ArrayList<Coordinate>();

        int radius = ((Number) shape.get("radius")).intValue();

        double lonCenter = pointsArray.get(0).doubleValue();
        double latCenter = pointsArray.get(1).doubleValue();

        double lonCircumference;
        double latCircumference;

        double[] centerCoordInPixels;

        AbstractEditor editor = (AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor();

        IDescriptor descriptor = editor.getActiveDisplayPane().getDescriptor();

        // If the center point is undefined, force it to be the
        // center of the display.
        if (lonCenter == -9999 || latCenter == -9999) {
            centerCoordInPixels = editor.getActiveDisplayPane()
                    .getRenderableDisplay().getExtent().getCenter();
            double[] centerCoordInWorld = descriptor
                    .pixelToWorld(centerCoordInPixels);

            lonCenter = centerCoordInWorld[0];
            latCenter = centerCoordInWorld[1];

        } else {
            // Compute the circumference point.
            centerCoordInPixels = descriptor.worldToPixel(new double[] {
                    lonCenter, latCenter });

        }

        // Compute the circumference point
        double[] circumferenceCoordInPixels = new double[2];
        circumferenceCoordInPixels[0] = centerCoordInPixels[0] - radius;
        circumferenceCoordInPixels[1] = centerCoordInPixels[1];

        double[] circumferenceCoordInWorld = descriptor
                .pixelToWorld(circumferenceCoordInPixels);
        lonCircumference = circumferenceCoordInWorld[0];
        latCircumference = circumferenceCoordInWorld[1];

        Coordinate coord = new Coordinate(lonCenter, latCenter, 0);
        points.add(coord);
        coord = new Coordinate(lonCircumference, latCircumference, 0);
        points.add(coord);

        Color[] colors = new Color[] { Color.BLACK, Color.BLACK };
        int borderThickness = ((Number) shape.get("border thick")).intValue();
        String fillcolor = (String) shape.get("fill color");
        String borderStyle = (String) shape.get("borderStyle");
        String borderColor = (String) shape.get("border color");
        String label = (String) shape.get("label");

        colors[0] = ToolLayer.convertRGBStringToColor(borderColor);
        colors[1] = ToolLayer.convertRGBStringToColor(fillcolor);

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

    @Override
    public void setPointID(long pointID) {
        this.pointID = pointID;
    }

    @Override
    public long getPointID() {
        return pointID;
    }
}
