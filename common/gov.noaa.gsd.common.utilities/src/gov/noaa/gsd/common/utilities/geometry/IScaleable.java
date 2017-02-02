/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities.geometry;

/**
 * Description: Interface that must be implemented by any advanced geometry
 * classes that allow scaling.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 29, 2016   15928    Chris.Golden Initial creation.
 * Sep 29, 2016   15928    Chris.Golden Changed behavior to allow resizing
 *                                      to cause geometries to flip over the
 *                                      appropriate axis if the user crosses
 *                                      that axis while resizing.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IScaleable extends IAdvancedGeometry {

    /**
     * Get a copy of this geometry, scaled around the geometry's center point
     * (as provided by {@link #getCenterPoint()}) by the specified horizontal
     * and vertical multipliers. Note that if the geometry also implements
     * {@link IRotatable}, the scaling will be rotated as well. For example, if
     * rotation is 30 degrees, then the horizontal multiplier will be applied
     * along the axis running 30 degrees from the horizontal, and the vertical
     * multiplier along an axis running 120 degrees from the horizontal.
     * 
     * @param horizontalMultiplier
     *            Multiplier to be applied along the horizontal axis (before
     *            rotation); must be a non-zero number. A negative number will
     *            cause the geometry to be flipped along the axis perpendicular
     *            to the rescaling.
     * @param verticalMultiplier
     *            Multiplier to be applied along the vertical axis (before
     *            rotation); must be a non-zero number. A negative number will
     *            cause the geometry to be flipped along the axis perpendicular
     *            to the rescaling.
     * @return Copy of this geometry scaled as specified.
     */
    public <G extends IScaleable> G scaledCopyOf(double horizontalMultiplier,
            double verticalMultiplier);
}
