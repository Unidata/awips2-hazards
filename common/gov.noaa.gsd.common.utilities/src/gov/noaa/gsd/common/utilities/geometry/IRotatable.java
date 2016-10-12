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
 * classes that allow rotation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 31, 2016   15934    Chris.Golden Initial creation.
 * Sep 29, 2016   15928    Chris.Golden Added method to get a rotated copy.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IRotatable extends IAdvancedGeometry {

    /**
     * Get the rotation of the geometry in radians, with <code>0</code> being no
     * rotation, <code>Pi / 2</code> being a counterclockwise quarter turn, etc.
     * 
     * @return Rotation of the geometry in radians, within the range
     *         <code>[0, 2 * Pi)</code>.
     */
    public double getRotation();

    /**
     * Get a copy of this geometry, rotated by the specified angle delta around
     * the geometry's center point (as provided by {@link #getCenterPoint()}).
     * 
     * @param delta
     *            Angle delta in radians by which to rotate.
     * @return Copy of this geometry rotated as specified.
     */
    public <G extends IRotatable> G rotatedCopyOf(double delta);
}
