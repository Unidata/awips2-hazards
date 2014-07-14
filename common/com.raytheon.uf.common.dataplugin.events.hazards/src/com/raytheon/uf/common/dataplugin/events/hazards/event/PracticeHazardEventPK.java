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
package com.raytheon.uf.common.dataplugin.events.hazards.event;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * For more unique keys, since there are the possibility for multiple same
 * eventIds, make a primary key that uses the siteID, eventID, and issuingTime
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2012            mnash     Initial creation
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Apr 24, 2014 3539      bkowal    Set column lengths
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
@SequenceGenerator(name = "PRACTICE_HAZARD_GENERATOR", sequenceName = "practicehazard_seq", allocationSize = 1)
@XmlAccessorType(XmlAccessType.NONE)
public class PracticeHazardEventPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @DynamicSerializeElement
    @XmlElement
    @Column(name = HazardConstants.SITE_ID, length = 4)
    private String siteID;

    @DynamicSerializeElement
    @XmlElement
    @Column(name = HazardConstants.HAZARD_EVENT_IDENTIFIER, length = 100)
    private String eventID;

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRACTICE_HAZARD_GENERATOR")
    @DynamicSerializeElement
    @XmlElement
    @Column(name = HazardConstants.UNIQUE_ID, length = 100)
    private String uniqueID;

    /**
         * 
         */
    public PracticeHazardEventPK() {
        // unique id is always unique, allowing for different items to be added
        // to the table with different primary keys
        uniqueID = UUID.randomUUID().toString();
    }

    /**
     * @return the issuingSite
     */
    public String getSiteID() {
        return siteID;
    }

    /**
     * @param issuingSite
     *            the issuingSite to set
     */
    public void setSiteID(String issuingSite) {
        this.siteID = issuingSite;
    }

    /**
     * @return the eventID
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * @param eventID
     *            the eventID to set
     */
    public void setEventID(String eventId) {
        this.eventID = eventId;
    }

    /**
     * @return the uniqueID
     */
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * @param uniqueID
     *            the uniqueID to set
     */
    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
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
        result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
        result = prime * result + ((siteID == null) ? 0 : siteID.hashCode());
        result = prime * result
                + ((uniqueID == null) ? 0 : uniqueID.hashCode());
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
        PracticeHazardEventPK other = (PracticeHazardEventPK) obj;
        if (eventID == null) {
            if (other.eventID != null) {
                return false;
            }
        } else if (!eventID.equals(other.eventID)) {
            return false;
        }
        if (siteID == null) {
            if (other.siteID != null) {
                return false;
            }
        } else if (!siteID.equals(other.siteID)) {
            return false;
        }
        if (uniqueID == null) {
            if (other.uniqueID != null) {
                return false;
            }
        } else if (!uniqueID.equals(other.uniqueID)) {
            return false;
        }
        return true;
    }
}
