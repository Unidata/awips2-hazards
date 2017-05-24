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

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Request for retrieving a new hazard event ID
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
public class HazardEventIdRequest extends HazardRequest {

    /** The site ID to retrieve the new hazard event ID */
    @DynamicSerializeElement
    private String siteID;

    /**
     * Creates a new HazardEventIdRequest
     */
    public HazardEventIdRequest() {

    }

    /**
     * Creates a new HazardEventIdRequest
     * 
     * @param siteID
     *            The site id to get the hazard event ID for
     * @param practice
     *            THe practice mode flag
     */
    public HazardEventIdRequest(String siteID, boolean practice) {
        super(practice);
        this.siteID = siteID;
    }

    /**
     * @return the siteID
     */
    public String getSiteID() {
        return siteID;
    }

    /**
     * @param siteID
     *            the siteID to set
     */
    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

}
