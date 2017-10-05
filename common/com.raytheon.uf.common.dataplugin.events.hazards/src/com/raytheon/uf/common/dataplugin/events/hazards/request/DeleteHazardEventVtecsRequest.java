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

/**
 * 
 * Request object for deleting hazard event vtecs from the registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 24, 2016   20037    Robert.Blum  Initial creation.
 * Oct 02, 2017   38506    Chris.Golden Moved common elements to new superclass.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
@DynamicSerialize
public class DeleteHazardEventVtecsRequest
        extends ChangeHazardEventVtecRequest {

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public DeleteHazardEventVtecsRequest() {

        /*
         * No action.
         */
    }

    /**
     * Construct a standard instance.
     * 
     * @param vtecRecords
     *            VTEC records to delete.
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public DeleteHazardEventVtecsRequest(List<Object> vtecRecords,
            boolean practice) {
        super(vtecRecords, practice);
    }
}
