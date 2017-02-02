/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;

/**
 * Description: Interface describing the methods that must be implemented in
 * order to be a listener for notifications of changes to countdown timer
 * displays managed by an instance of {@link CountdownTimersDisplayManager}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 22, 2013    1936    Chris.Golden Initial creation.
 * Jan 24, 2017   15556    Chris.Golden Changed name to use AWIPS2 standard
 *                                      "I" prefix for interfaces.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ICountdownTimersDisplayListener {

    /**
     * Receive notification that at least one of the countdown timer displays
     * has changed.
     * 
     * @param manager
     *            Manager of the countdown timer displays that have changed.
     */
    void countdownTimerDisplaysChanged(
            CountdownTimersDisplayManager<?, ?> manager);

    /**
     * Receive notification that all the countdown timer displays have changed.
     * 
     * @param manager
     *            Manager of the countdown timer displays that have changed.
     */
    void allCountdownTimerDisplaysChanged(
            CountdownTimersDisplayManager<?, ?> manager);
}
