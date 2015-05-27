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

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * A Notification that will be sent out through the SessionManager to notify all
 * components that a new event has been added to the session.
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

public class SessionEventAdded extends SessionEventModified {

    public SessionEventAdded(IHazardEvent event,
            boolean notifyAllowUntilFurtherNoticeSet,
            boolean isLastChangedEventModified, IOriginator originator) {
        super(event, notifyAllowUntilFurtherNoticeSet,
                isLastChangedEventModified, originator);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
