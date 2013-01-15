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
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * For more unique keys, since there are the possibility for multiple same
 * eventIds, make a primary key that uses the site, eventId, and issuingTime
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 9, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class PracticeHazardEventPK implements ISerializableObject, Serializable {

    private static final long serialVersionUID = 1L;

    @DynamicSerializeElement
    @XmlElement
    @Column(name = HazardConstants.SITE)
    private String site;

    @DynamicSerializeElement
    @XmlElement
    @Column
    private String eventId;

    @DynamicSerializeElement
    @XmlElement
    @Column
    private Date timeIssued;

    /**
         * 
         */
    public PracticeHazardEventPK() {
        eventId = UUID.randomUUID().toString();
    }

    /**
     * @return the issuingSite
     */
    public String getSite() {
        return site;
    }

    /**
     * @param issuingSite
     *            the issuingSite to set
     */
    public void setSite(String issuingSite) {
        this.site = issuingSite;
    }

    /**
     * @return the eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @param eventId
     *            the eventId to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * @return the timeIssued
     */
    public Date getTimeIssued() {
        return timeIssued;
    }

    /**
     * @param timeIssued
     *            the timeIssued to set
     */
    public void setTimeIssued(Date timeIssued) {
        this.timeIssued = timeIssued;
    }
}
