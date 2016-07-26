/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import java.util.LinkedHashMap;
import java.util.Map;

import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Encapsulation of a text position relative to a center point.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 4/11/12                 Bryon.Lawrence    Initial creation
 * Feb 03, 2015    3865    Chris.Cody        Check for valid Active Editor class
 * Feb 12, 2015 4959       Dan Schaffer      Modify MB3 add/remove UGCs to match Warngen
 * Mar 16, 2016 15676      Chris.Golden      Moved to more appropriate location.
 * Jun 23, 2016 19537      Chris.Golden      Revamped completely to allow flexible text
 *                                           positioning (arbitrary angle and offset).
 * Jul 25, 2016 19537      Chris.Golden      Moved to different package.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class TextPositioner {

    // Public Static Constants

    /**
     * Centered text.
     */
    public static final TextPositioner CENTERED = new TextPositioner();

    /**
     * Text above the centroid of the entity being labeled.
     */
    public static final TextPositioner ABOVE = new TextPositioner(90.0, 10);

    // Private Static Variables

    /**
     * Maximum number of instances of this class allowed to exist simultaneously
     * in the cache.
     */
    private static final int MAXIMUM_CACHE_SIZE = 16;

    // Private Static Variables

    /**
     * Cache holding text positioners, to allow reuse since these objects are
     * immutable.
     */
    private static final Map<CacheKey, TextPositioner> textPositionersForAnglesAndOffsets = new LinkedHashMap<CacheKey, TextPositioner>(
            MAXIMUM_CACHE_SIZE + 1, 1.0f, true) {

        // Private Static Constants

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        // Protected Methods

        @Override
        protected final boolean removeEldestEntry(
                Map.Entry<CacheKey, TextPositioner> eldest) {
            return (size() > MAXIMUM_CACHE_SIZE);
        }
    };

    // Private Static Classes

    /**
     * Key for the cache, encapsulating offset and direction.
     */
    private static class CacheKey {

        // Private Variables

        /**
         * Angle
         */
        private final double angle;

        /**
         * Offset.
         */
        private final double offset;

        /**
         * Hash code.
         */
        private final int hashCode;

        // Public Methods

        /**
         * Construct a standard instance.
         * 
         * @param angle
         *            Angle.
         * @apram offset Offset.
         */
        public CacheKey(double angle, double offset) {
            this.angle = angle;
            this.offset = offset;
            this.hashCode = Double.valueOf(angle).hashCode()
                    + Double.valueOf(offset).hashCode();
        }

        // Public Methods

        /**
         * Determine whether or not this instance equals the specified one.
         * 
         * @param other
         *            Other instance against which to check equality.
         * @return True if the other instance is equivalent to this one, false
         *         otherwise.
         */
        @Override
        public boolean equals(Object other) {
            if (other instanceof CacheKey == false) {
                return false;
            }
            CacheKey otherKey = (CacheKey) other;
            return ((angle == otherKey.angle) && (offset == otherKey.offset));
        }

        /**
         * Get the hash code.
         * 
         * @return Hash code.
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    // Private Variables

    /**
     * X offset of the label.
     */
    private final int xOffset;

    /**
     * Y offset of the label.
     */
    private final int yOffset;

    // Public Static Methods

    /**
     * Get an instance with the specified parameters.
     * 
     * @param angle
     *            Angle in degrees, with 0 being to the right, 90 being above,
     *            180 being to the left, and 270 being below.
     * @param offset
     *            Offset in pixels.
     * @return Text positioner.
     */
    public static TextPositioner getInstance(double angle, double offset) {
        CacheKey key = new CacheKey(angle, offset);
        TextPositioner textPositioner = textPositionersForAnglesAndOffsets
                .get(key);
        if (textPositioner == null) {
            textPositioner = new TextPositioner(angle, offset);
            textPositionersForAnglesAndOffsets.put(key, textPositioner);
        }
        return textPositioner;
    }

    // Private Constructors

    /**
     * Construct an instance that centers the text.
     */
    private TextPositioner() {
        this.xOffset = this.yOffset = 0;
    }

    /**
     * Construct a standard instance.
     * 
     * @param angle
     *            Angle in degrees, with 0 being to the right, 90 being above,
     *            180 being to the left, and 270 being below.
     * @param offset
     *            Offset in pixels.
     */
    private TextPositioner(double angle, double offset) {
        angle = Math.toRadians(angle);
        xOffset = (int) ((offset * Math.cos(angle)) + 0.5);
        yOffset = (int) -((offset * Math.sin(angle)) + 0.5);
    }

    // Public Methods

    /**
     * Get a label's position relative to a point on the display.
     * 
     * @param centerPoint
     *            Center point of the geometry being labeled.
     * @return The coordinate at which to place the text.
     */
    public Coordinate getLabelPosition(final Coordinate centerPoint) {
        Coordinate centerCoord = null;
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if ((editor != null) && (centerPoint != null)) {
            double[] centerXY = editor.translateInverseClick(centerPoint);
            centerXY[0] += xOffset;
            centerXY[1] += yOffset;
            centerCoord = editor.translateClick(centerXY[0], centerXY[1]);
        }

        /*
         * It is possible that this text position will not be in the view area.
         * This seems to be a problem when switching from the D2D to GFE
         * perspectives, and when handling multi-polygons.
         */
        if (centerCoord != null) {
            return centerCoord;
        }
        return centerPoint;
    }

    /**
     * Get a string representation.
     * 
     * @return String representation.
     */
    @Override
    public String toString() {
        return "[" + xOffset + ", " + yOffset + "]";
    }
}
