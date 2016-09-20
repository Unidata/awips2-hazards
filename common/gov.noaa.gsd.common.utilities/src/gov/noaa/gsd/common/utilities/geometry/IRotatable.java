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
 * classes that track their rotation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 31, 2016   15934    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IRotatable extends IAdvancedGeometry {

    /**
     * Get the rotation of the geometry in degrees, with <code>0</code> being no
     * rotation, <code>90</code> being a counterclockwise quarter turn, etc.
     * 
     * @return Rotation of the geometry in degrees.
     */
    public double getRotation();
}
