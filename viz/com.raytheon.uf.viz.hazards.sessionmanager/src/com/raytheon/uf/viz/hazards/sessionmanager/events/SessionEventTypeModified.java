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

import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * A Notification that will be sent out through the SessionManager to notify all
 * components that the type (phenomenon, significance, and/or subtype) of an
 * event in the session has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014 2925       Chris.Golden Initial creation.
 * Apr 10, 2015    6898    Chris.Cody   Refactored async messaging
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionEventTypeModified extends SessionEventModified {

    public SessionEventTypeModified(ObservedHazardEvent event,
            IOriginator originator) {
        super(event, originator);
    }

    public String getPhenomenon() {
        return getEvent().getPhenomenon();
    }

    public String getSignificance() {
        return getEvent().getSignificance();
    }

    public String getSubType() {
        return getEvent().getSubType();
    }
}
