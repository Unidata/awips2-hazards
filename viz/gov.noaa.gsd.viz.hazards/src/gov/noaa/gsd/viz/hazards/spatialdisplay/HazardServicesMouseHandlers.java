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
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Jun 24, 2015   6601     Chris.Cody          Change Create by Hazard Type display text
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public enum HazardServicesMouseHandlers {
    FREE_HAND_MULTI_SELECTION, // Free-hand multi selection mouse handler
    SINGLE_SELECTION, // Single selection mouse handler
    VERTEX_DRAWING, // Vertex (point/line-path/vertex-polygon) drawing action
    FREEHAND_DRAWING, // Freehand drawing action
    STORM_TOOL_DRAG_DOT_DRAWING, // "Drag to <Hazard Type> Location" drawing
                                 // action
    DRAW_BY_AREA, // Draw by area drawing action
    RECTANGLE_MULTI_SELECTION // Rectangle multi-selection mouse handler
}
