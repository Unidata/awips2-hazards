/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

/**
 * Description: Interface describing the methods that must be implemented by a
 * current time provider, used to provide the application's current time (which
 * may be different from the system current time).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 16, 2013    2545    Chris.Golden Initial creation.
 * May 12, 2014    2925    Chris.Golden Moved to gov.noaa.gsd.common.utilities
 *                                      package.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ICurrentTimeProvider {

    // Public Methods

    /**
     * Get the current time as an epoch time in milliseconds.
     * 
     * @return Current time as an epoch time in milliseconds.
     */
    public long getCurrentTime();
}
