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
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Description: A convenience class for creating a line shape which can easily
 * be translated into a JSON string.
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
final public class Line extends Shape {
    private String color;

    private int thickness;

    List<double[]> points;

    public Line(String label, String isVisible, String isSelected,
            String include, String color, int thickness, Coordinate[] points) {
        super(label, isVisible, isSelected, include);
        setShapeType(HAZARD_EVENT_SHAPE_TYPE_LINE);
        this.color = color;
        this.thickness = thickness;

        this.points = Lists.newArrayList();

        for (Coordinate point : points) {
            double pointArray[] = new double[2];
            pointArray[0] = point.x;
            pointArray[1] = point.y;
            this.points.add(pointArray);
        }
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public int getThickness() {
        return thickness;
    }

    public void addPoints(double latitude, double longitude) {
        points.add(new double[] { latitude, longitude });
    }

}
