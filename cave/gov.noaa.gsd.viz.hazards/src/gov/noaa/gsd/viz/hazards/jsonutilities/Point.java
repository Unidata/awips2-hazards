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
 * Description: A convenience class for creating a point shape which can easily
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
final public class Point extends Shape {

    private final List<double[]> points;

    private String color;

    private String pointID;

    public Point(String label, String isVisible, String isSelected,
            String include, String color, Coordinate point, String pointID) {
        super(label, isVisible, isSelected, include);
        setShapeType(HAZARD_EVENT_SHAPE_TYPE_POINT);
        this.color = color;
        this.points = Lists.newArrayList(new double[] { point.x, point.y });
        this.pointID = pointID;
    }

    public void setPoint(double point[]) {
        this.points.set(0, point);
    }

    public double[] getPoint() {
        return points.get(0);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public String getPointID() {
        return pointID;
    }

    public void setPointID(String pointID) {
        this.pointID = pointID;
    }
}
