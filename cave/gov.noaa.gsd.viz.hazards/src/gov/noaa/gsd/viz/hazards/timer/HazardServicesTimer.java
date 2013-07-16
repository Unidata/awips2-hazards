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

import java.util.Calendar;
import java.util.Date;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Hazard Services utility which notifies all observers after a set amount of
 * time has passed. This may be set up on a one-time or repetitive basis.
 * Clients can act on this notification as they wish.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 01, 2012            Bryon.Lawrence    Initial creation
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardServicesTimer extends Thread {
    private final long elapsedTime;

    private long timerStartTime;

    private long timerEndTime;

    private final boolean repeat;

    private final EventBus eventBus;

    /**
     * Creates a Timer which wakes up and notifies its clients after the
     * elapsedTime. This may be set up to be a one-time notification or a
     * repetitive notification.
     * 
     * @param elapsedTime
     *            The elapsed time after which to notify clients (ms)
     * @param repeat
     *            true - repeat, false - one time notification.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public HazardServicesTimer(long elapsedTime, boolean repeat,
            EventBus eventBus) {
        this.eventBus = eventBus;
        this.elapsedTime = elapsedTime;
        this.repeat = repeat;
    }

    @Override
    public void run() {
        try {
            do {
                this.timerStartTime = Calendar.getInstance().getTimeInMillis();
                Thread.sleep(elapsedTime);
                this.timerEndTime = Calendar.getInstance().getTimeInMillis();

                // Notify listeners...
                fireTimerEvent();

            } while (repeat);

        } catch (InterruptedException e) {
            // An Interrupt exception will be thrown when a client calls the
            // stopTimer method.
        }
    }

    /**
     * Stops the hazard services timer by interrupting it.
     */
    public void stopTimer() {
        this.interrupt();
    }

    private void fireTimerEvent() {
        long actualElapsedTime = this.timerEndTime - this.timerStartTime;
        Date caveTime = SimulatedTime.getSystemTime().getTime();
        final TimerAction timerAction = new TimerAction(this.elapsedTime,
                actualElapsedTime, caveTime);

        // Alert the VizApp thread that it has a job
        // to do when it gets a chance.
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                eventBus.post(timerAction);
            }
        });
    }
}
