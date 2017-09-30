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

import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

/**
 * Notification that will be sent out to notify all components that batching of
 * notifications has been toggled on or off.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 27, 2017   38072    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionBatchNotificationsToggled
        extends OriginatedSessionNotification {

    // Private Variables

    /**
     * Flag indicating whether batching of notifications has been toggled on or
     * off.
     */
    private final boolean batching;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the change.
     */
    public SessionBatchNotificationsToggled(boolean batching) {
        super(Originator.OTHER);
        this.batching = batching;
    }

    // Public Methods

    /**
     * Determine whether batching was turned on or off.
     * 
     * @return <code>true</code> if batching was turned on, <code>false</code>
     *         otherwise.
     */
    public boolean isBatching() {
        return batching;
    }
}
