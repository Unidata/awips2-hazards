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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * An interoperability record. Created and/or checked by ingest whenever an
 * interoperability product is ingested. This data type will be mapped to
 * a particular hazard to answer the existence question.
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

@Entity
@Table(name = "hazards_interoperability")
@DynamicSerialize
public class HazardsInteroperability extends PersistableDataObject<String>
        implements IHazardsInteroperabilityRecord {

    private static final long serialVersionUID = -1120290501262059067L;

    @EmbeddedId
    @DynamicSerializeElement
    @JoinColumn
    private HazardsInteroperabilityPK key;

    @Column(name = HazardInteroperabilityConstants.INTEROPERABILITY_TYPE, nullable = false, length = 10)
    @DynamicSerializeElement
    @Enumerated(EnumType.STRING)
    private INTEROPERABILITY_TYPE interoperabilityType;
    
    @Column(name = HazardInteroperabilityConstants.CREATION_DATE, nullable = false)
    @DynamicSerializeElement
    private Date creationDate;

    public HazardsInteroperability() {
        this.key = new HazardsInteroperabilityPK();
    }

    public HazardsInteroperabilityPK getKey() {
        return key;
    }

    public void setKey(HazardsInteroperabilityPK key) {
        this.key = key;
    }

    @Override
    public String getHazardEventID() {
        return this.key.getHazardEventID();
    }


    /**
     * @return the interoperabilityType
     */
    public INTEROPERABILITY_TYPE getInteroperabilityType() {
        return interoperabilityType;
    }

    /**
     * @param interoperabilityType
     *            the interoperabilityType to set
     */
    public void setInteroperabilityType(
            INTEROPERABILITY_TYPE interoperabilityType) {
        this.interoperabilityType = interoperabilityType;
    }
    
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime
                * result
                + ((interoperabilityType == null) ? 0 : interoperabilityType
                        .hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    /* (non-Javadoc)
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
        HazardsInteroperability other = (HazardsInteroperability) obj;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (interoperabilityType != other.interoperabilityType)
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }
}