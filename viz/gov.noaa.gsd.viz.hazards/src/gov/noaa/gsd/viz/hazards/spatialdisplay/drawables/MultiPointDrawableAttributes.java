/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import java.awt.Color;

/**
 * The PGEN drawing attributes associated with a multi-point shape drawn on the
 * Spatial Display in Hazard Services. All drawables in Hazard Services are
 * rendered using PGEN drawing classes.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Feb 02, 2018   26712    Chris.Golden  Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public abstract class MultiPointDrawableAttributes extends DrawableAttributes {

    // Private Variables

    /**
     * Width in pixels of the buffer to each side of the drawable's shape. If
     * <code>0</code>, no buffer is needed.
     */
    private float bufferWidth;

    /**
     * Color of the buffer to each side of the drawable's shape.
     */
    private Color bufferColor;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public MultiPointDrawableAttributes() {
    }

    // Public Methods

    /**
     * Get the width in pixels of the buffer drawn to each side of the shape's
     * outline.
     * 
     * @return Width in pixels of the buffer. If <code>0</code>, no buffer is
     *         needed.
     */
    public float getBufferWidth() {
        return bufferWidth;
    }

    /**
     * Set the width in pixels of the buffer drawn to each side of the shape's
     * outline.
     * 
     * @param bufferWidth
     *            Width in pixels of the buffer. If <code>0</code>, no buffer is
     *            needed.
     */
    public void setBufferWidth(float bufferWidth) {
        this.bufferWidth = bufferWidth;
    }

    /**
     * Get the color of the buffer drawn to each side of the shape's outline.
     * 
     * @return Color of the buffer.
     */
    public Color getBufferColor() {
        return bufferColor;
    }

    /**
     * Set the color of the buffer drawn to each side of the shape's outline.
     * 
     * @param bufferColor
     *            Color of the buffer.
     */
    public void setBufferColor(Color bufferColor) {
        this.bufferColor = bufferColor;
    }
}
