/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Description: Interface for all IHIS drawables.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 03, 2012            bryon.lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IHazardServicesShape {

    /**
     * Get the drawable attributes.
     * 
     * @return Drawable attributes.
     */
    public HazardServicesDrawingAttributes getDrawingAttributes();

    /**
     * Sets the eventID associated with this hazard geometry.
     * 
     * @param id
     *            - the id to associate with this hazard.
     * @return
     */
    public void setID(String id);

    /**
     * 
     * Retrieves the id associated with this hazard geometry.
     * 
     * @return the ID associated with this hazard.
     */
    public String getID();

    /**
     * Returns the JTS geometry version of this shape. All Hazard Services
     * shapes have an associated JTS geometries. This allows the use of JTS
     * tools.
     * 
     * @return JTS geometry representing this shape.
     */
    public Geometry getGeometry();

    /**
     * @return True if the user can edit this shape.
     */
    public boolean isEditable();

    /**
     * @return True if the user can move this shape.
     */
    public boolean isMovable();

    /**
     * Set the editable status of this shape.
     * 
     * @param isEditable
     *            true if this shape is editable.
     */
    public void setIsEditable(boolean isEditable);

    /**
     * Set the movable status of this shape.
     * 
     * @param isMovable
     *            true if this shape is movable.
     */
    public void setMovable(boolean isMovable);

}
