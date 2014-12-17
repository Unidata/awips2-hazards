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
 * Apr 01, 2014            bkowal       Initial creation
 * Dec 18, 2014  #2826     dgilling     Change fields used in interoperability.
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

    @Column(name = HazardInteroperabilityConstants.PHENOMENON, nullable = false, length = 2)
    @DynamicSerializeElement
    private String phen;

    @Column(name = HazardInteroperabilityConstants.SIGNIFICANCE, nullable = false, length = 1)
    @DynamicSerializeElement
    private String sig;

    @Column(name = HazardInteroperabilityConstants.HAZARD_EVENT_ID, nullable = false, length = 100)
    @DynamicSerializeElement
    private String hazardEventID;

    @Column(name = HazardInteroperabilityConstants.ETN, nullable = false, length = 4)
    @DynamicSerializeElement
    private String etn;

    public HazardsInteroperabilityPK() {
        // for dynamic serialize only
    }

    public HazardsInteroperabilityPK(String siteID, String phenomenon,
            String significance, String hazardEventID, String etn) {
        this.siteID = siteID;
        this.phen = phenomenon;
        this.sig = significance;
        this.hazardEventID = hazardEventID;
        this.etn = etn;
    }

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public String getPhen() {
        return phen;
    }

    public void setPhen(String phenomenon) {
        this.phen = phenomenon;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String significance) {
        this.sig = significance;
    }

    public String getHazardEventID() {
        return hazardEventID;
    }

    public void setHazardEventID(String hazardEventID) {
        this.hazardEventID = hazardEventID;
    }

    public String getEtn() {
        return etn;
    }

    public void setEtn(String etn) {
        this.etn = etn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((etn == null) ? 0 : etn.hashCode());
        result = prime * result
                + ((hazardEventID == null) ? 0 : hazardEventID.hashCode());
        result = prime * result
                + ((phen == null) ? 0 : phen.hashCode());
        result = prime * result
                + ((sig == null) ? 0 : sig.hashCode());
        result = prime * result + ((siteID == null) ? 0 : siteID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HazardsInteroperabilityPK other = (HazardsInteroperabilityPK) obj;
        if (etn == null) {
            if (other.etn != null) {
                return false;
            }
        } else if (!etn.equals(other.etn)) {
            return false;
        }
        if (hazardEventID == null) {
            if (other.hazardEventID != null) {
                return false;
            }
        } else if (!hazardEventID.equals(other.hazardEventID)) {
            return false;
        }
        if (phen == null) {
            if (other.phen != null) {
                return false;
            }
        } else if (!phen.equals(other.phen)) {
            return false;
        }
        if (sig == null) {
            if (other.sig != null) {
                return false;
            }
        } else if (!sig.equals(other.sig)) {
            return false;
        }
        if (siteID == null) {
            if (other.siteID != null) {
                return false;
            }
        } else if (!siteID.equals(other.siteID)) {
            return false;
        }
        return true;
    }
}