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
 * <p>
 * TODO: A subclass is needed that allows the use of {@link GetterRunnable}
 * methods, perhaps a <code>call(GetterRunnable)</code> method that returns an
 * object of generic type <code>R</code> from <code>GetterRunnable</code>. Note
 * that this will mean that the main worker thread will need to allow not just
 * scheduling of asynchronous runnables, but also blocking calls that wait for
 * the runnable to be executed and then return the result.
 * </p>
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
