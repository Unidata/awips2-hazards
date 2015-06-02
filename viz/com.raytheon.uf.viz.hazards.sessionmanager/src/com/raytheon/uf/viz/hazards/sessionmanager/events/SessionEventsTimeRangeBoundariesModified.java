/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.util.Collections;
import java.util.Set;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Notification that will be sent out through the SessionManager to notify all
 * components that the boundaries constraining the range of allowable values for
 * the start and/or end time of an event in the session have changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 02, 2015    2331    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionEventsTimeRangeBoundariesModified extends
        SessionEventsModified implements ISessionNotification {

    private final Set<String> events;

    public SessionEventsTimeRangeBoundariesModified(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            Set<String> events, IOriginator originator) {
        super(eventManager, originator);
        this.events = Collections.unmodifiableSet(events);
    }

    public final Set<String> getChangedEvents() {
        return events;
    }
}
