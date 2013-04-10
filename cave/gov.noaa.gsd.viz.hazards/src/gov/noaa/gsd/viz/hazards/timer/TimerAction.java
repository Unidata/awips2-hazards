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

import java.util.Date;

/**
 * Action object sent when a timer event is fired. This contains the actual
 * elapsed time since the timer was started, the actual elapsed time since the
 * timer was started, and the CaveTime when the timer event was fired.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 5/1/12                  Bryon.Lawrence    Initial creation
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class TimerAction {
    private final long elapsedTime;

    private final long actualElapsedTime;

    private final Date caveTime;

    public TimerAction(long elapsedTime, long actualElapsedTime, Date caveTime) {
        this.elapsedTime = elapsedTime;
        this.actualElapsedTime = actualElapsedTime;
        this.caveTime = caveTime;
    }

    /**
     * @return the elapsedTime
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * @return the actualElapsedTime
     */
    public long getActualElapsedTime() {
        return actualElapsedTime;
    }

    /**
     * @return the caveTime
     */
    public Date getCaveTime() {
        return (Date) caveTime.clone();
    }
}
