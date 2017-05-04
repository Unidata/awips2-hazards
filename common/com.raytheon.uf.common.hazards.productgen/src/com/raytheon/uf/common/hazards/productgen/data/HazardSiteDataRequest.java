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
package com.raytheon.uf.common.hazards.productgen.data;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Cave Request message to request server. This message requests that the
 * Request server exports this Site's Localization Data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2015 3473       Chris.Cody  Initial creation
 * Nov 23, 2015 3473       Robert.Blum Changed to only be used for exports.
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */

@DynamicSerialize
public class HazardSiteDataRequest implements IServerRequest {

    @DynamicSerializeElement
    private String siteId;

    @DynamicSerializeElement
    private Boolean practice;

    /**
     * Default constructor.
     */
    public HazardSiteDataRequest() {
    }

    /**
     * Constructor for Export Localization Request
     * 
     * @param siteId
     *            Site to Export to Central Registry Server
     */
    public HazardSiteDataRequest(String siteId, Boolean practice) {
        this.siteId = siteId;
        this.setPractice(practice);
    }

    /**
     * Get Export Site Id.
     * 
     * @return Export Site Id
     */
    public String getSiteId() {
        return this.siteId;
    }

    /**
     * Set Export Site Id.
     * 
     * @param siteId
     *            Export Site Id
     */
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    /**
     * Mode of Hazard Service Operation.
     * 
     * @return Hazard Services Mode
     */
    public Boolean getPractice() {
        return this.practice;
    }

    /**
     * Mode of Hazard Service Operation.
     * 
     * @param practice
     *            Hazard Services Mode
     */
    public void setPractice(Boolean practice) {
        if (practice == null) {
            practice = Boolean.TRUE;
        }
        this.practice = practice;
    }

}
