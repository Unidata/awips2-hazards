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
 * Description: Interface describing the methods that must be implemented for a
 * class to be considered an asynchronous scheduler of {@link Runnable} objects.
 * Implementations allow arbitrary <code>Runnable</code>s to be scheduled to run
 * asynchronously.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 04, 2014   3319     Chris.Golden Initial creation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IRunnableAsynchronousScheduler {

    /**
     * Schedule the specified runnable to be executed asynchronously.
     * 
     * @param runnable
     *            Runnable to be scheduled.
     */
    public void schedule(Runnable runnable);
}
