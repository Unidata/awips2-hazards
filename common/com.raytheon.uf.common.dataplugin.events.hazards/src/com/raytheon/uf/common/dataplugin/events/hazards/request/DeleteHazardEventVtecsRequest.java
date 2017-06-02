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

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Request object for deleting hazard event vtecs from the registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2016 20037      Robert.Blum Initial Creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
@DynamicSerialize
public class DeleteHazardEventVtecsRequest extends HazardRequest {

    /** List of Vtecs to be deleted from the registry */
    @DynamicSerializeElement
    private List<Object> vtecRecords;

    /**
     * Creates a new DeleteHazardEventVtecsRequest
     */
    public DeleteHazardEventVtecsRequest() {

    }

    /**
     * Creates a new DeleteHazardEventVtecsRequest
     * 
     * @param vtecRecords
     *            List of vtecRecords to delete
     * @param practice
     *            Practice mode flag
     */
    public DeleteHazardEventVtecsRequest(List<Object> vtecRecords,
            boolean practice) {
        super(practice);
        this.vtecRecords = vtecRecords;
    }

    /**
     * @return the vtecRecords
     */
    public List<Object> getVtecRecords() {
        return vtecRecords;
    }

    /**
     * @param vtecRecords
     *            the vtecRecords to set
     */
    public void setVtecRecords(List<Object> vtecRecords) {
        this.vtecRecords = vtecRecords;
    }

}
