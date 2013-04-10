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

import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.List;

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
 * 
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
            String include, String color, int thickness, List<double[]> points) {
        super(label, isVisible, isSelected, include);
        setShapeType(Utilities.HAZARD_EVENT_SHAPE_TYPE_LINE);
        this.color = color;
        this.thickness = thickness;
        this.points = points;
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
