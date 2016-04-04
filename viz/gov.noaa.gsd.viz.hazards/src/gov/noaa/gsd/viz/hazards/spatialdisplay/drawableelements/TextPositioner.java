/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Positions a label relative to a Drawable's center point.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 4/11/12                 Bryon.Lawrence    Initial creation
 * Feb 03, 2015    3865    Chris.Cody        Check for valid Active Editor class
 * Feb 12, 2015 4959       Dan Schaffer      Modify MB3 add/remove UGCs to match Warngen
 * Mar 16, 2016 15676      Chris.Golden      Moved to more appropriate location.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public enum TextPositioner {
    CENTER(0, 0), TOP(0, -10), BOTTOM(0, 10), LEFT(-10, 0), RIGHT(10, 0);

    private final int xOffset;

    private final int yOffset;

    TextPositioner(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    /**
     * Returns the x-position of the text label point
     * 
     * @param x
     * @return
     */
    public double computeXpos(double x) {

        return x + xOffset;
    }

    /**
     * Returns the y-position of the text label point
     * 
     * @param y
     * @return
     */
    public double computeYpos(double y) {
        return y + yOffset;
    }

    /**
     * Builds a label's position relative to a point on the display.
     * 
     * @param centerPoint
     * @return The coordinate to place the text at. PGEN text objects are
     *         centered on this point.
     */
    public Coordinate getLabelPosition(final Coordinate centerPoint) {

        Coordinate centerCoord = null;
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);

        if (editor != null && centerPoint != null) {
            double[] centerXY = editor.translateInverseClick(centerPoint);
            centerXY[0] = computeXpos(centerXY[0]);
            centerXY[1] = computeYpos(centerXY[1]);
            centerCoord = editor.translateClick(centerXY[0], centerXY[1]);
        }

        /*
         * It is possible that this text position will not be in the view area.
         * This seems to be a problem when switching from the D2D to GFE
         * perspectives. Also when handling multi-polygons.
         */
        if (centerCoord != null) {
            return centerCoord;
        } else {
            return centerPoint;
        }

    }
}