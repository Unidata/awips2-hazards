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
 * Description: Enumeration of possible mouse drawing actions.
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
public enum HazardServicesMouseHandlers {
    MULTI_SELECTION, // Multi selection mouse handler
    SINGLE_SELECTION, // Single selection mouse handler
    EVENTBOX_DRAWING, // Polygon drawing action
    FREEHAND_DRAWING, // Freehand drawing action
    DRAG_DROP_DRAWING, // Drag me to storm drawing action
    DRAW_BY_AREA, // Draw by area drawing action
    SELECTION_RECTANGLE
    // Rectangle multi-selection tool.
}
