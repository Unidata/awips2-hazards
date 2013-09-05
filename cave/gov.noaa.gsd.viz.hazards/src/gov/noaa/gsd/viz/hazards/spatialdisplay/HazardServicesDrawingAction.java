/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

/**
 * Description: Enumeration of hazard services drawing actions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public enum HazardServicesDrawingAction {
    ADD_NODE, // Add a point to the selected hazard
    DELETE_NODE, // Delete a point from the selected hazard
    MOVE_ELEMENT, // Move the entire selected hazard
    COPY_ELEMENT
    // Copy the hazard
}
