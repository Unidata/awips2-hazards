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
 * Request class used for storing Hazard Event VTEC records
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * 4/5/2016       16577    Ben.Phillippe Initial creation
 * 5/3/2016       18193    Ben.Phillippe Replication of Hazard VTEC Records
 * Oct 02, 2017   38506    Chris.Golden  Moved common elements to new superclass.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
public class StoreHazardEventVtecRequest extends ChangeHazardEventVtecRequest {

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public StoreHazardEventVtecRequest() {

        /*
         * No action.
         */
    }

    /**
     * Construct a standard instance.
     * 
     * @param vtecRecords
     *            VTEC records to store.
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public StoreHazardEventVtecRequest(List<Object> vtecRecords,
            boolean practice) {
        super(vtecRecords, practice);
    }
}
