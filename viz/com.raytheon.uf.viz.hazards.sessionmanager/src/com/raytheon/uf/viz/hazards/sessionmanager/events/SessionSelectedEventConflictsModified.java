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

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Description: Notification of a change to the map of selected event
 * identifiers to the events with which they conflict.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 12, 2014    2925    Chris.Golden Initial creation.
 * Apr 10, 2015    6898    Chris.Cody   Refactored async messaging
 * May 20, 2015    7624    mduff        Changed notification hierarchy.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionSelectedEventConflictsModified extends SessionNotification {

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the event.
     */
    public SessionSelectedEventConflictsModified(IOriginator originator) {
        super(originator, false, false);
    }
}
