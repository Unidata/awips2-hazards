/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

/**
 * Description: Interface describing the methods that must be implemented by a
 * current time provider. Instances of the latter are used to provide some time
 * megawidgets with the current time as needed. This is done so as to allow the
 * current time to be something other than that tracked by the system clock.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2013    2545    Chris.Golden      Initial creation
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
