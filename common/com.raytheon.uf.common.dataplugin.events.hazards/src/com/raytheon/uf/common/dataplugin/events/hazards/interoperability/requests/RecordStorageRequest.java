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
package com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests;

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Used to request the storage, update, or removal of the provided
 * interoperability records.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 1, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@DynamicSerialize
public class RecordStorageRequest implements IServerRequest {

    @DynamicSerialize
    public enum RequestType {
        STORE, UPDATE, DELETE
    }

    @DynamicSerializeElement
    private RequestType requestType;

    @DynamicSerializeElement
    private List<IHazardsInteroperabilityRecord> records;

    /**
     * 
     */
    public RecordStorageRequest(RequestType requestType,
            List<IHazardsInteroperabilityRecord> records) {
        this.requestType = requestType;
        this.records = records;
    }

    /**
     * @return the requestType
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * @param requestType
     *            the requestType to set
     */
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    /**
     * @return the records
     */
    public List<IHazardsInteroperabilityRecord> getRecords() {
        return records;
    }

    /**
     * @param records
     *            the records to set
     */
    public void setRecords(List<IHazardsInteroperabilityRecord> records) {
        this.records = records;
    }
}
