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

import com.google.gson.Gson;

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
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
final public class Point extends Shape {

    private double point[];

    private String color;

    public Point(String label, String isVisible, String isSelected,
            String include, String color, double point[]) {
        super(label, isVisible, isSelected, include);
        setShapeType(Utilities.HAZARD_EVENT_SHAPE_TYPE_POINT);
        this.color = color;
        this.point = point;
    }

    public void setPoint(double point[]) {
        this.point = point;
    }

    public double[] getPoint() {
        return point;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public static void main(String args[]) {
        Point testPoint = new Point("FF.W", "true", "true", "true",
                "255 255 255", new double[] { 42.0, -120.0 });
        Gson gson = new Gson();
        String json = gson.toJson(testPoint);

        System.out.println("JSON string: " + json);

        // Try going the other way...
        @SuppressWarnings("unused")
        Point obj = gson.fromJson(json, Point.class);

        System.exit(0);
    }
}
