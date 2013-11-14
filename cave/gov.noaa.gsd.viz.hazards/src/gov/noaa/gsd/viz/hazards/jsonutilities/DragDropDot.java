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

import com.google.gson.annotations.SerializedName;

/**
 * 
 * Description: Convenience class for creating a drag/drop dot shape. When
 * serialized to a JSON string, it will be recognized by the Hazard Services
 * model and displayable in the Hazard Services spatial view with the attributes
 * defined by the SerializeName annotations below.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer       Description
 * ------------ ---------- -----------    --------------------------
 * Feb 2011                Bryon.Lawrence Initial creation
 * Mar 2013                Bryon.Lawrence Added documentation
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
final public class DragDropDot extends Shape {
    @SuppressWarnings("unused")
    private final int pointID;

    @SerializedName("fill color")
    private String fillColor;

    @SerializedName("border thick")
    private int borderThickness;

    private int radius;

    private String borderStyle;

    @SerializedName("border color")
    private String borderColor;

    @SuppressWarnings("unused")
    private final int[] centerPoint;

    /**
     * Build the dot.
     * 
     * @param label
     *            Dot's label, e.g. "Drag me to storm"
     * @param isVisible
     *            Whether or not the dot should be visible in the Spatial View
     * @param isSelected
     *            Whether or not the dot should be displayed as selected
     * @param include
     *            Whether or not to include the dot in set of hazards to display
     * @param fillColor
     *            The fill color of the dot (RGB)
     * @param borderThickness
     *            The thickness of the dot's border
     * @param borderStyle
     *            The style of the dot's border
     * @param borderColor
     *            The color of the dot's border
     * @param radius
     *            The radius of the dot
     * @param pointID
     *            Identifier of the dot used internally by hazard services
     *            software.
     * @param centerPoint
     *            The center point of the dot, i.e. where the dot is initially
     *            displayed.
     */
    public DragDropDot(String label, String isVisible, String isSelected,
            String include, String fillColor, int borderThickness,
            String borderStyle, String borderColor, int radius, int pointID,
            int centerPoint[]) {
        super(label, isVisible, isSelected, include);
        setShapeType(HAZARD_EVENT_SHAPE_TYPE_DOT);
        this.fillColor = fillColor;
        this.borderThickness = borderThickness;
        this.borderStyle = borderStyle;
        this.borderColor = borderColor;
        this.radius = radius;
        this.centerPoint = centerPoint;
        this.pointID = pointID;
    }

    /*
     * Getters and setters.
     */
    /**
     * @param color
     */
    public void setFillColor(String color) {
        this.fillColor = color;
    }

    /**
     * 
     * @param
     * @return An RGB string representing the color.
     */
    public String getFillColor() {
        return fillColor;
    }

    /**
     * 
     * @param thickness
     *            0 = thin, > 0 = progressively thicker.
     * @return
     */
    public void setBorderThickness(int thickness) {
        this.borderThickness = thickness;
    }

    /**
     * 
     * @param
     * @return The border thickness value
     */
    public int getBorderThickness() {
        return borderThickness;
    }

    /**
     * 
     * @param borderStyle
     *            Can be one of many different styles from NCEP's
     *            linePatterns.xml file.
     * 
     * @return
     */
    public void setBorderStyle(String borderStyle) {
        this.borderStyle = borderStyle;
    }

    /**
     * Returns the border style.
     */
    public String getBorderStyle() {
        return borderStyle;
    }

    /**
     * 
     * @param borderColor
     *            An RGB color string
     * @return
     */
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * 
     * @param
     * @return An RGB color string
     */
    public String getBorderColor() {
        return borderColor;
    }

    /**
     * 
     * @param radius
     *            The radius of this circle, basically controlling the dot's
     *            size.
     * @return
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * 
     * @param
     * @return The radius of the circle representing this dot.
     */
    public int getRadius() {
        return radius;
    }
}
