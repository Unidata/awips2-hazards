/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Description: Notification of a change to the last modified event in the list
 * of selected events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionLastChangedEventModified extends SessionEventsModified {

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param originator
     *            Originator of the event.
     */
    public SessionLastChangedEventModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            IOriginator originator) {
        super(eventManager, originator);
    }
}
