/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.jsonutilities;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Description: A convenience class for creating a polygon shape which can
 * easily be translated into a JSON string.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
final public class Polygon extends Shape {
    @SerializedName("fill color")
    private String fillColor;

    @SerializedName("border thick")
    private int borderThickness;

    private String borderStyle;

    @SerializedName("border color")
    private String borderColor;

    List<double[]> points;

    public Polygon(String label, String isVisible, String isSelected,
            String include, String fillColor, int borderThickness,
            String borderStyle, String borderColor, List<double[]> points) {
        super(label, isVisible, isSelected, include);
        setShapeType(HAZARD_EVENT_SHAPE_TYPE_POLYGON);
        this.fillColor = fillColor;
        this.borderThickness = borderThickness;
        this.borderStyle = borderStyle;
        this.borderColor = borderColor;
        this.points = points;
    }

    public Polygon(String label, String isVisible, String isSelected,
            String include, String fillColor, int borderThickness,
            String borderStyle, String borderColor, Coordinate[] points) {
        super(label, isVisible, isSelected, include);
        setShapeType(HAZARD_EVENT_SHAPE_TYPE_POLYGON);
        this.fillColor = fillColor;
        this.borderThickness = borderThickness;
        this.borderStyle = borderStyle;
        this.borderColor = borderColor;

        this.points = Lists.newArrayList();

        for (Coordinate point : points) {
            double pointArray[] = new double[2];
            pointArray[0] = point.x;
            pointArray[1] = point.y;
            this.points.add(pointArray);
        }
    }

    public void setFillColor(String color) {
        this.fillColor = color;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setBorderThickness(int thickness) {
        this.borderThickness = thickness;
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderStyle(String borderStyle) {
        this.borderStyle = borderStyle;
    }

    public String getBorderStyle() {
        return borderStyle;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public static void main(String args[]) {
        List<double[]> points = Lists.newArrayList();
        points.add(new double[] { 42.0, -120 });
        points.add(new double[] { 42.1, -120.1 });

        Polygon testPolygon = new Polygon("FF.W", "true", "true", "true",
                "255 255 255", 1, "DASHED", "255 255 255", points);
        Gson gson = new Gson();
        String json = gson.toJson(testPolygon);

        // Try going the other way...
        @SuppressWarnings("unused")
        Polygon obj = gson.fromJson(json, Polygon.class);

        System.exit(0);
    }

}
