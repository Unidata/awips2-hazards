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

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Description: Notification of a change to an event's status with regard to
 * whether or not it may allow "until further notice" to be used as its end
 * time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 19, 2014  2925      Chris.Golden Initial creation.
 * May 12, 2014  2925      Chris.Golden Changed to be derived from the appropriate
 *                                      superclass.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionEventAllowUntilFurtherNoticeModified extends
        SessionEventModified implements ISessionNotification {

    public SessionEventAllowUntilFurtherNoticeModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            IHazardEvent event, IOriginator originator) {
        super(eventManager, event, originator);
    }
}
