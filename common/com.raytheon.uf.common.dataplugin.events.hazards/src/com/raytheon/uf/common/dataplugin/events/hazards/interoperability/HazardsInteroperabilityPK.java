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

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The key for the interoperability events.
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
public class HazardsInteroperabilityPK implements
        IHazardsInteroperabilityPrimaryKey {

    private static final long serialVersionUID = -3568262610203300949L;

    @Column(name = HazardConstants.SITE_ID, nullable = false, length = 4)
    @DynamicSerializeElement
    private String siteID;

    @Column(name = HazardInteroperabilityConstants.HAZARD_TYPE, nullable = false, length = 30)
    @DynamicSerializeElement
    private String hazardType;

    @Column(name = HazardInteroperabilityConstants.HAZARD_EVENT_ID, nullable = false, length = 100)
    @DynamicSerializeElement
    private String hazardEventID;

    @Column(name = HazardInteroperabilityConstants.ETN, nullable = false, length = 4)
    @DynamicSerializeElement
    private String etn;

    /**
     * 
     */
    public HazardsInteroperabilityPK() {
    }

    public HazardsInteroperabilityPK(String siteID, String hazardType,
            String hazardEventID, String etn) {
        this.siteID = siteID;
        this.hazardType = hazardType;
        this.hazardEventID = hazardEventID;
        this.etn = etn;
    }

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public String getHazardType() {
        return hazardType;
    }

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

    public String getEtn() {
        return etn;
    }

    public void setEtn(String etn) {
        this.etn = etn;
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
        result = prime * result + ((etn == null) ? 0 : etn.hashCode());
        result = prime * result
                + ((hazardEventID == null) ? 0 : hazardEventID.hashCode());
        result = prime * result
                + ((hazardType == null) ? 0 : hazardType.hashCode());
        result = prime * result + ((siteID == null) ? 0 : siteID.hashCode());
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
        HazardsInteroperabilityPK other = (HazardsInteroperabilityPK) obj;
        if (etn == null) {
            if (other.etn != null)
                return false;
        } else if (!etn.equals(other.etn))
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
        return true;
    }
}