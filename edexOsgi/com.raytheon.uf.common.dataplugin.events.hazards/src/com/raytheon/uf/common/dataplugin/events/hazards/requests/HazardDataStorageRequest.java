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
package com.raytheon.uf.common.dataplugin.events.hazards.requests;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request to modify the database, contains an enum to specify which operation
 * to do.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@DynamicSerialize
public class HazardDataStorageRequest implements IServerRequest {

    @DynamicSerialize
    public enum RequestType {
        STORE, UPDATE, DELETE;
    }

    @DynamicSerializeElement
    private IHazardEvent[] events;

    @DynamicSerializeElement
    private boolean practice;

    @DynamicSerializeElement
    private RequestType type;

    /**
     * @return the event
     */
    public IHazardEvent[] getEvents() {
        return events;
    }

    /**
     * @param event
     *            the event to set
     */
    public void setEvents(IHazardEvent... events) {
        this.events = events;
    }

    /**
     * @return the mode
     */
    public boolean isPractice() {
        return practice;
    }

    /**
     * @param mode
     *            the mode to set
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
    }

    /**
     * @return the type
     */
    public RequestType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(RequestType type) {
        this.type = type;
    }
}
