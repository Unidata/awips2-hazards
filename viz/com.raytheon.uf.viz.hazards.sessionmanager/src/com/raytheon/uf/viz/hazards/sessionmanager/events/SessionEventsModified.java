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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * A Notification that will be sent out through the SessionManager to notify all
 * components that the set of events in the session has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2013 1257       bsteffen    Initial creation
 * Apr 10, 2015 6898       Chris.Cody  Refactored async messaging
 * May 20, 2015 7624       mduff       Changed notification hierarchy.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventsModified extends SessionNotification {

    protected Set<String> eventIds = new HashSet<>();

    public SessionEventsModified(Set<String> eventIds,
            boolean isAllowingUntilFurtherNoticeSet,
            boolean isLastChangedEventModified, IOriginator originator) {
        super(originator, isAllowingUntilFurtherNoticeSet,
                isLastChangedEventModified);
        this.eventIds = eventIds;
    }

    public SessionEventsModified(IOriginator originator) {
        super(originator, false, false);
    }

    public Set<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(Set<String> eventIds) {
        this.eventIds = eventIds;
    }

    public void addEventId(String eventId) {
        eventIds.add(eventId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
