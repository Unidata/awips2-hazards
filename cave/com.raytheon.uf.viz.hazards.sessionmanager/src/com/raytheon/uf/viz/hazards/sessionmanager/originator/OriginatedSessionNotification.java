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
package com.raytheon.uf.viz.hazards.sessionmanager.originator;

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;

/**
 * Contains the originator of what sent this out so that can be filtered upon.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 6, 2014            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class OriginatedSessionNotification implements ISessionNotification {

    private IOriginator originator;

    /**
     * 
     */
    public OriginatedSessionNotification(IOriginator originator) {
        this.originator = originator;
    }

    /**
     * @return the originator
     */
    public IOriginator getOriginator() {
        return originator;
    }

    /**
     * @param originator
     *            the originator to set
     */
    public void setOriginator(IOriginator originator) {
        this.originator = originator;
    }

}
