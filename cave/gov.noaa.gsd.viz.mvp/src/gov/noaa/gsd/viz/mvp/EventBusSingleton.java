/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.mvp;

import com.google.common.eventbus.EventBus;

/**
 * Event bus singleton, used to create a single shared event bus for use by
 * presenters and other <code>IAction</code> generators to send events off to
 * listeners.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class EventBusSingleton {

    // Private Static Variables

    /*
     * A single instance of the eventBus
     */
    private static EventBus eventBus;

    // Public Static Methods

    /**
     * @return An instance of this eventBus singleton
     */
    static public EventBus getInstance() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }

    // Private Constructors

    /*
     * Private constructor prevents instantiation outside of this class.
     */
    private EventBusSingleton() {
        eventBus = new EventBus();
    }
}
