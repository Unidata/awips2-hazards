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
package com.raytheon.uf.common.dataplugin.events.hazards.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Base class for lock/unlock requests
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 26, 2016  7623      Ben.Phillippe Initial creation.
 * Dec 19, 2016 21504      Robert.Blum   Added LockRequestType.
 * Apr 07, 2017 32734      mduff         Added ORPHAN_CHECK request type.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
public class LockRequest extends HazardRequest {

    public static enum LockRequestType {
        LOCK, UNLOCK, STATUS, BREAK, ORPHAN_CHECK;
    }

    /** The list of event IDs to be locked or unlocked */
    @DynamicSerializeElement
    protected List<String> eventIdList = new ArrayList<String>();

    /** The workstation ID of the workstation making the request */
    @DynamicSerializeElement
    protected WsId workstationId;

    @DynamicSerializeElement
    private LockRequestType type;

    /**
     * Creates a new LockRequest
     */
    public LockRequest() {
        this(true);
    }

    /**
     * Creates a new LockRequest in the given mode
     * 
     * @param practice
     *            The practice mode flag
     */
    public LockRequest(boolean practice) {
        super(practice);
    }

    public void addEventId(String eventId) {
        eventIdList.add(eventId);
    }

    public List<String> getEventIdList() {
        return eventIdList;
    }

    public void setEventIdList(List<String> eventIdList) {
        this.eventIdList = eventIdList;
    }

    public WsId getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(WsId workstationId) {
        this.workstationId = workstationId;
    }

    /**
     * @return the type
     */
    public LockRequestType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(LockRequestType type) {
        this.type = type;
    }
}
