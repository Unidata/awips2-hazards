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

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * Parent class for all session events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 18, 2015   7624     mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class SessionNotification extends OriginatedSessionNotification {
    public SessionNotification(IOriginator originator,
            boolean isAllowingUntilFurtherNoticeSet,
            boolean isLastChangedEventModified) {
        super(originator);
        this.isAllowingUntilFurtherNoticeSet = isAllowingUntilFurtherNoticeSet;
        this.isLastChangedEventModified = isLastChangedEventModified;
    }

    protected boolean isAllowingUntilFurtherNoticeSet = false;

    protected boolean isLastChangedEventModified = false;

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
}
