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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Object used to hold an interoperability record.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@Entity
@Table(name = "hazard_interoperability")
@DynamicSerialize
@XmlRootElement(name = "HazardInteroperabilityRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class HazardInteroperabilityRecord extends
        PersistableDataObject<Integer> {

    private static final long serialVersionUID = 4106108054791763991L;

    @Id
    @GeneratedValue
    @XmlElement
    private int id;

    @Column(nullable = false, length = 4)
    @XmlElement
    private String siteID;

    @Column(name = HazardInteroperabilityConstants.HAZARD_EVENT_ID, nullable = false, length = 100)
    @DynamicSerializeElement
    @XmlElement
    private String hazardEventID;

    @Column(name = HazardInteroperabilityConstants.ACTIVE_TABLE_ID, nullable = false, length = 100)
    @DynamicSerializeElement
    @XmlElement
    private int activeTableEventID;

    @Column(name = HazardInteroperabilityConstants.INTEROPERABILITY_TYPE, nullable = false, length = 10)
    @DynamicSerializeElement
    @Enumerated(EnumType.STRING)
    @XmlElement
    private INTEROPERABILITY_TYPE interoperabilityType;

    @Column(name = HazardInteroperabilityConstants.CREATION_DATE, nullable = false, columnDefinition = "timestamp without time zone default now()")
    @DynamicSerializeElement
    @XmlElement
    private Date creationDate;

    @Column(name = HazardInteroperabilityConstants.PHENOMENON, nullable = false, length = 2)
    @DynamicSerializeElement
    @XmlElement
    private String phen;

    @Column(name = HazardInteroperabilityConstants.SIGNIFICANCE, nullable = false, length = 1)
    @DynamicSerializeElement
    @XmlElement
    private String sig;

    @Column(name = HazardInteroperabilityConstants.START_DATE, nullable = false)
    @DynamicSerializeElement
    @XmlElement
    private Date startDate;

    @Column(name = HazardInteroperabilityConstants.END_DATE, nullable = false)
    @DynamicSerializeElement
    @XmlElement
    private Date endDate;

    @Column(name = HazardInteroperabilityConstants.GEOMETRY, columnDefinition = "geometry", nullable = false)
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @XmlAttribute
    @DynamicSerializeElement
    private Geometry geometry;

    @Column(name = HazardInteroperabilityConstants.PARM_ID, nullable = false, length = 150)
    @DynamicSerializeElement
    private String parmID;

    @Column(name = "practice")
    @DynamicSerializeElement
    @XmlElement
    private boolean practice;

    public HazardInteroperabilityRecord() {

    }

    public HazardInteroperabilityRecord(String siteID, String hazardEventID,
            int activeTableEventID, INTEROPERABILITY_TYPE interoperabilityType) {
        super();
        this.siteID = siteID;
        this.hazardEventID = hazardEventID;
        this.activeTableEventID = activeTableEventID;
        this.interoperabilityType = interoperabilityType;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
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
     * @return the activeTableEventID
     */
    public int getActiveTableEventID() {
        return activeTableEventID;
    }

    /**
     * @param activeTableEventID
     *            the activeTableEventID to set
     */
    public void setActiveTableEventID(int activeTableEventID) {
        this.activeTableEventID = activeTableEventID;
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

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the phen
     */
    public String getPhen() {
        return phen;
    }

    /**
     * @param phen
     *            the phen to set
     */
    public void setPhen(String phen) {
        this.phen = phen;
    }

    /**
     * @return the sig
     */
    public String getSig() {
        return sig;
    }

    /**
     * @param sig
     *            the sig to set
     */
    public void setSig(String sig) {
        this.sig = sig;
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
     * @return the practice
     */
    public boolean isPractice() {
        return practice;
    }

    /**
     * @param practice
     *            the practice to set
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
    }

}
