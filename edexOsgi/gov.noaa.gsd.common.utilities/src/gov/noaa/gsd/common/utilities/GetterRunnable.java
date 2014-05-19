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
 * Description: Runnable that may hold a return value following the execution of
 * {@link #run()}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 10, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class GetterRunnable<R> implements Runnable {

    // Private Variables

    /**
     * Return value.
     */
    private volatile R result;

    // Public Methods

    /**
     * Get the return value.
     * 
     * @return Return value.
     */
    public final R getResult() {
        return result;
    }

    // Protected Methods

    /**
     * Set the return value.
     * 
     * @param result
     *            New return value.
     */
    protected final void setResult(R result) {
        this.result = result;
    }
}
