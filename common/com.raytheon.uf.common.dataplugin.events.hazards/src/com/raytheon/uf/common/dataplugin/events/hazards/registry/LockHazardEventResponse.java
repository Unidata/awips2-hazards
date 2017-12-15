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
package com.raytheon.uf.common.dataplugin.events.hazards.registry;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.dataplugin.events.locks.LockInfo;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Response used as a return from lock/unlock requests
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 26,2016  7623       Ben.Phillippe Initial creation
 * Dec 19, 2016 21504      Robert.Blum Added lockInfoMap.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "LockHazardEventResponse")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class LockHazardEventResponse extends HazardEventResponse {

    /** Status of the lock/unlock request */
    @DynamicSerializeElement
    @XmlElement
    private boolean success;

    @DynamicSerializeElement
    private Map<String, LockInfo> lockInfoMap = new HashMap<String, LockInfo>();

    /** Any additional details regarding the lock/unlock request */
    @DynamicSerializeElement
    @XmlElement
    private String message;

    public LockHazardEventResponse() {
        super();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the lockInfoMap
     */
    public Map<String, LockInfo> getLockInfoMap() {
        return lockInfoMap;
    }

    /**
     * @param lockInfoMap
     *            the lockInfoMap to set
     */
    public void setLockInfoMap(Map<String, LockInfo> lockInfoMap) {
        this.lockInfoMap = lockInfoMap;
    }

    /**
     * Adds or overrides an entry to the lockInfoMap for the given eventID.
     * 
     * @param eventID
     * @param info
     */
    public void addLockInfo(String eventID, LockInfo info) {
        lockInfoMap.put(eventID, info);
    }

}
