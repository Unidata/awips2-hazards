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

/**
 * Used to respond to retrieval requests for interoperability records associated
 * with specified criteria.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 2, 2014            bkowal     Initial creation
 *
 * </pre>
 *
 * @author bkowal
 * @version 1.0	
 */

@DynamicSerialize
public class RecordRetrieveResponse {

    @DynamicSerializeElement
    private List<IHazardsInteroperabilityRecord> records;
    
    /**
     * 
     */
    public RecordRetrieveResponse(List<IHazardsInteroperabilityRecord> records) {
        this.records = records;
    }

    /**
     * @return the records
     */
    public List<IHazardsInteroperabilityRecord> getRecords() {
        return records;
    }

}
