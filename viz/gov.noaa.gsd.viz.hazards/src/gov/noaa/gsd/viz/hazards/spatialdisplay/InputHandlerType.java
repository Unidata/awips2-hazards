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
 * Jun 23, 2016  19537     Chris.Golden        Removed storm-track-specific code.
 * Sep 21, 2016  15934     Chris.Golden        Added ellipse drawing option.
 * Sep 29, 2016  15928     Chris.Golden        Added geometry editing options.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public enum InputHandlerType {
    SINGLE_SELECTION, RECTANGLE_MULTI_SELECTION, FREEHAND_MULTI_SELECTION, MOVE, VERTEX_MOVE, RESIZE, ROTATE, VERTEX_DRAWING, FREEHAND_DRAWING, ELLIPSE_DRAWING, DRAW_BY_AREA
}
