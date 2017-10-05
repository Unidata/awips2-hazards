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

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * 
 * Request for storing hazard events
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Aug 20, 2015    6895    Ben.Phillippe Routing registry requests through request server
 * Oct 02, 2017   38506    Chris.Golden  Moved common elements to new superclass.
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
public class StoreHazardEventRequest extends ChangeHazardEventRequest {

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public StoreHazardEventRequest() {

        /*
         * No action.
         */
    }

    /**
     * Construct a standard instance.
     * 
     * @param events
     *            Events to store.
     * @param practice
     *            Flag indicating whether or not practice mode is in effect.
     */
    public StoreHazardEventRequest(List<HazardEvent> events, boolean practice) {
        super(events, practice);
    }
}
