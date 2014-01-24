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

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;

/**
 * 
 * Description: Defines an abstract shape which can be extended to define
 * concrete shapes. Provides a convenient way to create shapes which can then be
 * serialized into JSON.
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
abstract public class Shape {
    /**
     * Indicates whether or not the shape is selected ("true" or "false")
     */
    private String isSelected;

    /**
     * Indicates whether or not the shape is visible ("true" or "false")
     */
    private String isVisible;

    /**
     * Indicates whether or not to include a shape in a hazard event. ("true" or
     * "false")
     */
    private String include;

    /**
     * Label associated with a shape
     */
    private String label;

    /**
     * The type of shape.
     */
    private GeometryType geometryType;

    /**
     * Initializes the shape member variables.
     * 
     * @param label
     *            The label associated with the shape
     * @param isVisible
     *            Is the shape visibile ("true", "false")
     * @param isSelected
     *            Is the shape selected ("true", "false")
     * @param include
     *            Is the shape included in the hazard event ("true", "false")
     */
    public Shape(String label, String isVisible, String isSelected,
            String include) {
        this.label = label;
        this.geometryType = getGeometryType();
        this.isVisible = isVisible;
        this.isSelected = isSelected;
        this.include = include;
    }

    /**
     * Set the shape's selected state.
     * 
     * @param isSelected
     *            "true" or "false"
     * @return
     */
    public void setSelected(String isSelected) {
        this.isSelected = isSelected;
    }

    /**
     * @return This shape's selected state ("true", "false")
     */
    public String isSelected() {
        return isSelected;
    }

    /**
     * Set this shape's visible state.
     * 
     * @param isVisible
     *            "true" or "false"
     * @return
     */
    public void setVisible(String isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * @return This shape's visible state ("true" or "false")
     */
    public String isVisible() {
        return isVisible;
    }

    /**
     * @param label
     *            The label to assign to this shape.
     * @return
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return This shape's label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param geometryType
     *            The type to assign to this shape
     * @return
     */
    public void setGeometryType(GeometryType geometryType) {
        this.geometryType = geometryType;
    }

    /**
     * @return The shape's type
     */
    public GeometryType getGeometryType() {
        return geometryType;
    }

    /**
     * @param include
     *            whether or not to include this shape in the event ("true" or
     *            "false")
     */
    public void setInclude(String include) {
        this.include = include;
    }

    /**
     * @return This shape's include state ("true" or "false")
     */
    public String getInclude() {
        return include;
    }
}
