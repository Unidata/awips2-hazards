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

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventsModified extends OriginatedSessionNotification {

    private boolean isAllowingUntilFurtherNoticeSet = false;

    private boolean isLastChangedEventModified = false;

    public SessionEventsModified(boolean isAllowingUntilFurtherNoticeSet,
            boolean isLastChangedEventModified, IOriginator originator) {
        super(originator);
        this.isAllowingUntilFurtherNoticeSet = isAllowingUntilFurtherNoticeSet;
        this.isLastChangedEventModified = isLastChangedEventModified;
    }

    public SessionEventsModified(IOriginator originator) {
        super(originator);
        this.isAllowingUntilFurtherNoticeSet = false;
        this.isLastChangedEventModified = false;
    }

    public boolean getIsAllowingUntilFurtherNoticeSet() {
        return (isAllowingUntilFurtherNoticeSet);
    }

    public void setIsAllowingUntilFurtherNoticeSet(
            boolean isAllowingUntilFurtherNoticeSet) {
        this.isAllowingUntilFurtherNoticeSet = isAllowingUntilFurtherNoticeSet;
    }

    public boolean getIsLastChangedEventModified() {
        return (isLastChangedEventModified);
    }

    public void setIsLastChangedEventModified(boolean isLastChangedEventModified) {
        this.isLastChangedEventModified = isLastChangedEventModified;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }
}
