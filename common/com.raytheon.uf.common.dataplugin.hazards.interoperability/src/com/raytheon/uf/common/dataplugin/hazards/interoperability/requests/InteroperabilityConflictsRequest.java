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
package com.raytheon.uf.common.dataplugin.hazards.interoperability.requests;

import java.util.Date;

import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardRequest;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Request object used to determine if there are conlicts among hazard products
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
public class InteroperabilityConflictsRequest extends HazardRequest {

    /** The phenomenon and significance string */
    @DynamicSerializeElement
    private String phenSig;

    /** The site ID */
    @DynamicSerializeElement
    private String siteID;

    /** The start time of the request */
    @DynamicSerializeElement
    private Date startTime;

    /** The end time of the request */
    @DynamicSerializeElement
    private Date endTime;

    /**
     * Creates a new InteroperabilityConflictsRequest
     */
    public InteroperabilityConflictsRequest() {
        super();
    }

    /**
     * InteroperabilityConflictsRequest
     * 
     * @param practice
     *            Practice mode flag
     * @param phenSig
     *            The phensig
     * @param siteID
     *            The site ID
     * @param startTime
     *            The start time of the request
     * @param endTime
     *            The end time of the request
     */
    public InteroperabilityConflictsRequest(boolean practice, String phenSig,
            String siteID, Date startTime, Date endTime) {
        super(practice);
        this.phenSig = phenSig;
        this.siteID = siteID;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @return the phenSig
     */
    public String getPhenSig() {
        return phenSig;
    }

    /**
     * @param phenSig
     *            the phenSig to set
     */
    public void setPhenSig(String phenSig) {
        this.phenSig = phenSig;
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

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     *            the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

}
