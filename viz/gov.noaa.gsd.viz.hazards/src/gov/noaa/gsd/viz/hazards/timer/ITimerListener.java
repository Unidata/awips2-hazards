/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.timer;

/**
 * 
 * A class must implement this interface if it wants to be notified by the
 * hazard services timer of time changes.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 5/1/2012                Bryon.Lawrence    Initial creation
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public interface ITimerListener {
    /**
     * This method is called when the hazard services timer ticks. It is up to
     * the client to decide what to do when notified.
     * 
     * @param timerAction
     *            Contains basic CAVE and elapsed time information.
     */
    public void timerActionOccurred(TimerAction timerAction);
}
