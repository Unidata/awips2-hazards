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
package com.raytheon.uf.common.dataplugin.events.locks;

import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class holds data related to a Hazard Event lock.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 12, 2016 21504      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

@DynamicSerialize
public class LockInfo {

    /** Enumeration denoting status of the lock */
    public enum LockStatus {
        LOCKABLE, LOCKED_BY_ME, LOCKED_BY_OTHER
    };

    /**
     * The workstation from which the lock was created.
     */
    @DynamicSerializeElement
    private WsId workstation;

    /**
     * The status of the lock.
     */
    @DynamicSerializeElement
    private LockStatus lockStatus;

    public LockInfo() {

    }

    /**
     * @return the workstation
     */
    public WsId getWorkstation() {
        return workstation;
    }

    /**
     * @param workstation
     *            the workstation to set
     */
    public void setWorkstation(WsId workstation) {
        this.workstation = workstation;
    }

    /**
     * @return the lockStatus
     */
    public LockStatus getLockStatus() {
        return lockStatus;
    }

    /**
     * @param lockStatus
     *            the lockStatus to set
     */
    public void setLockStatus(LockStatus lockStatus) {
        this.lockStatus = lockStatus;
    }
}
