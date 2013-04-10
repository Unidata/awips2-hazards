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

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Description: A convenience class for creating a circle shape.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 2012                Bryon.Lawrence    Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
final public class Circle extends Shape {

    /**
     * Circle center
     */
    private double point[];

    /**
     * Circle color
     */
    private String color;

    /**
     * Circle identifier
     */
    private String id;

    /**
     * Circle name
     */
    private String name;

    /**
     * Create a circle with the given attributes.
     * 
     * @param label
     *            The label of the circle
     * @param id
     *            The identifier of the circle
     * @param name
     *            The name of the circle.
     * @param isVisible
     *            whether or not the circle is visible
     * @param isSelected
     *            whether or not the circle is selected
     * @param include
     *            whether or not the circle should be included in a hazard event
     * @param color
     *            the color of the circle
     * @param point
     *            the center point of the circle
     */
    public Circle(String label, String id, String name, String isVisible,
            String isSelected, String include, String color, double point[]) {
        super(label, isVisible, isSelected, include);
        setShapeType(Utilities.HAZARD_EVENT_SHAPE_TYPE_CIRCLE);
        this.color = color;
        this.id = id;
        this.name = name;
        this.point = point;
    }

    /**
     * Create a circle with the given attributes
     * 
     * @param label
     *            the label to associate with the circle
     * @param id
     *            the id of the circle
     * @param name
     *            the name of the circle
     * @param isVisible
     *            whether or not the circle is visible
     * @param isSelected
     *            whether or not the circle is selected
     * @param include
     *            whether or not the circle is included in a hazard event
     * @param color
     *            the circle color
     * @param point
     *            the center point of the circle
     */
    public Circle(String label, String id, String name, String isVisible,
            String isSelected, String include, String color, Coordinate point) {
        super(label, isVisible, isSelected, include);
        setShapeType(Utilities.HAZARD_EVENT_SHAPE_TYPE_CIRCLE);
        this.color = color;
        this.id = id;
        this.name = name;

        this.point = new double[2];
        this.point[0] = point.x;
        this.point[1] = point.y;
    }

    /**
     * Set the center point
     * 
     * @param point
     *            contains the lat, lon of the center point
     * @return
     */
    public void setPoint(double point[]) {
        this.point = point;
    }

    /**
     * @return the center point of the circle as an array containing the lat,
     *         lon value.
     */
    public double[] getPoint() {
        return point;
    }

    /**
     * @param color
     *            the color to set the circle to.
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * 
     * @param
     * @return the color of the circle
     */
    public String getColor() {
        return color;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the circle name
     */
    public String getName() {
        return name;
    }

}
