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
package com.raytheon.uf.common.dataplugin.events.hazards.interoperability;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The primary key for the gfe interoperability events.
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

@Embeddable
@DynamicSerialize
public class HazardsInteroperabilityGFEPK implements
        IHazardsInteroperabilityPrimaryKey {

    private static final long serialVersionUID = -5649663414668926034L;

    @Column(name = HazardConstants.SITE_ID, nullable = false, length = 4)
    @DynamicSerializeElement
    private String siteID;

    @Column(name = HazardInteroperabilityConstants.HAZARD_TYPE, nullable = false, length = 30)
    @DynamicSerializeElement
    private String hazardType;

    @Column(name = HazardInteroperabilityConstants.HAZARD_EVENT_ID, nullable = false, length = 100)
    @DynamicSerializeElement
    private String hazardEventID;

    @Column(name = HazardInteroperabilityConstants.START_DATE, nullable = false)
    @DynamicSerializeElement
    private Date startDate;

    @Column(name = HazardInteroperabilityConstants.END_DATE, nullable = false)
    @DynamicSerializeElement
    private Date endDate;

    /**
     * 
     */
    public HazardsInteroperabilityGFEPK() {
    }

    public HazardsInteroperabilityGFEPK(String siteID, String hazardType,
            String hazardEventID, Date startDate, Date endDate) {
        this.siteID = siteID;
        this.hazardType = hazardType;
        this.hazardEventID = hazardEventID;
        this.startDate = startDate;
        this.endDate = endDate;
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
     * @return the hazardType
     */
    public String getHazardType() {
        return hazardType;
    }

    /**
     * @param hazardType
     *            the hazardType to set
     */
    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    /**
     * @return the hazardEventID
     */
    public String getHazardEventID() {
        return hazardEventID;
    }

    /**
     * @param hazardEventID
     *            the hazardEventID to set
     */
    public void setHazardEventID(String hazardEventID) {
        this.hazardEventID = hazardEventID;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate
     *            the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate
     *            the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result
                + ((hazardEventID == null) ? 0 : hazardEventID.hashCode());
        result = prime * result
                + ((hazardType == null) ? 0 : hazardType.hashCode());
        result = prime * result + ((siteID == null) ? 0 : siteID.hashCode());
        result = prime * result
                + ((startDate == null) ? 0 : startDate.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HazardsInteroperabilityGFEPK other = (HazardsInteroperabilityGFEPK) obj;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        if (hazardEventID == null) {
            if (other.hazardEventID != null)
                return false;
        } else if (!hazardEventID.equals(other.hazardEventID))
            return false;
        if (hazardType == null) {
            if (other.hazardType != null)
                return false;
        } else if (!hazardType.equals(other.hazardType))
            return false;
        if (siteID == null) {
            if (other.siteID != null)
                return false;
        } else if (!siteID.equals(other.siteID))
            return false;
        if (startDate == null) {
            if (other.startDate != null)
                return false;
        } else if (!startDate.equals(other.startDate))
            return false;
        return true;
    }
}