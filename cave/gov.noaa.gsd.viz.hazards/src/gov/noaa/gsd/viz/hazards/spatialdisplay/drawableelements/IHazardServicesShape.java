/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import com.vividsolutions.jts.geom.Polygon;

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
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public interface IHazardServicesShape {

    /**
     * Sets the eventID associated with this hazard geometry.
     * 
     * @param eventID
     *            - the ID to associate with this hazard.
     * @return
     */
    public void setEventID(String eventID);

    /**
     * 
     * Retrieves the eventID associated with this hazard geometry.
     * 
     * @param
     * @return the ID associated with this hazard.
     */
    public String getEventID();

    /**
     * 
     * 
     * Returns flag indicating whether or not the vertices associated with this
     * shape can be edited.
     * 
     * @param
     * @return true - the vertices can be edited, false - they cannot be edited.
     */
    public boolean canVerticesBeEdited();

    /**
     * Returns the JTS polygon associated with this shape. All Hazard Services
     * geometries have an associated polygon. This allows the use of JTS tools.
     * 
     * @param
     * @return JTS polygon representing the selectable area of this shape.
     */
    public Polygon getPolygon();

}
