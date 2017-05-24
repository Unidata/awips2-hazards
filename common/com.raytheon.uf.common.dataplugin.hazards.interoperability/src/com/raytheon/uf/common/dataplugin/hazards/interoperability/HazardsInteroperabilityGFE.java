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
package com.raytheon.uf.common.dataplugin.hazards.interoperability;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * An interoperability record type specific to GFE records. The records are
 * accessed and updated by the EDEX request instance. They are associated with a
 * particular GFE grid and a hazard. An additional record type is used for GFE
 * Hazard Events because the events can exist before a product is generated and
 * ingested and, therefore, before the hazard event has an associated etn.
 * 
 * 
 * The question is if this data type should be in this common, generic
 * dataplugin or in a GFE-specific hazards plugin. It is true that this data
 * type is primarily used for GFE interoperability; however, it has no direct
 * dependencies on GFE. And this data type exists specifically for
 * interoperability purposes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 01, 2014            bkowal      Initial creation
 * Oct 21, 2014   5051     mpduff      Change to support Hibernate upgrade.
 * Dec 08, 2014   2826     dgilling    Rename db table to better indicate this is for
 *                                     practice only.
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@Entity
@Table(name = "practice_hazards_interoperability_gfe")
@DynamicSerialize
public class HazardsInteroperabilityGFE extends PersistableDataObject<String>
        implements IHazardsInteroperabilityRecord {

    private static final long serialVersionUID = 3022206902787618108L;

    @EmbeddedId
    @JoinColumn
    @DynamicSerializeElement
    private HazardsInteroperabilityGFEPK key;

    @Column(name = HazardInteroperabilityConstants.PARM_ID, nullable = false, length = 150)
    @DynamicSerializeElement
    private String parmID;

    @Column(name = HazardInteroperabilityConstants.GEOMETRY, columnDefinition = "geometry", nullable = false)
    @Type(type = "org.hibernate.spatial.GeometryType")
    @DynamicSerializeElement
    private Geometry geometry;

    @Column(name = HazardInteroperabilityConstants.CREATION_DATE, nullable = false)
    @DynamicSerializeElement
    private Date creationDate;

    public HazardsInteroperabilityGFE() {
        this.key = new HazardsInteroperabilityGFEPK();
    }

    /**
     * @return the key
     */
    public HazardsInteroperabilityGFEPK getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(HazardsInteroperabilityGFEPK key) {
        this.key = key;
    }

    /**
     * @return the hazardEventID
     */
    @Override
    public String getHazardEventID() {
        return this.key.getHazardEventID();
    }

    /**
     * @return the parmID
     */
    public String getParmID() {
        return parmID;
    }

    /**
     * @param parmID
     *            the parmID to set
     */
    public void setParmID(String parmID) {
        this.parmID = parmID;
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * @param geometry
     *            the geometry to set
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * @return the creationDate
     */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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
        result = prime * result
                + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result
                + ((geometry == null) ? 0 : geometry.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((parmID == null) ? 0 : parmID.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        HazardsInteroperabilityGFE other = (HazardsInteroperabilityGFE) obj;
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (geometry == null) {
            if (other.geometry != null) {
                return false;
            }
        } else if (!geometry.equals(other.geometry)) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (parmID == null) {
            if (other.parmID != null) {
                return false;
            }
        } else if (!parmID.equals(other.parmID)) {
            return false;
        }
        return true;
    }
}