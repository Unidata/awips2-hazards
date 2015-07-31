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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.raytheon.uf.common.dataplugin.events.IValidator;
import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.slotconverter.HazardAttributeSlotConverter;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.annotations.RegistryObjectVersion;
import com.raytheon.uf.common.registry.annotations.SlotAttribute;
import com.raytheon.uf.common.registry.annotations.SlotAttributeConverter;
import com.raytheon.uf.common.registry.ebxml.slots.DateSlotConverter;
import com.raytheon.uf.common.registry.ebxml.slots.GeometrySlotConverter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * The Hazard record class which at its most basic level contains information
 * that every hazard will contain
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 16, 2012            mnash       Initial creation
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 14, 2013 1472       bkowal      Remove ISerializableObject. Renamed hazard subtype to subType.
 * Dec     2013 2368       thansen   Added getHazardType
 * Apr 23, 2014 2925       Chris.Golden Augmented with additional methods to
 *                                      set the type components atomically, or
 *                                      the start and end time atomically.
 * Jun 30, 2014 3512       Chris.Golden Added addHazardAttributes() method.
 * Feb 22, 2015 6561       mpduff      Override getInsertTime
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Jul 31, 2015 7458      Robert.Blum   Added new userName and workstation fields.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@XmlRootElement(name = "hazard")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@RegistryObject({ HazardConstants.SITE_ID,
        HazardConstants.HAZARD_EVENT_IDENTIFIER, HazardConstants.UNIQUE_ID })
@RegistryObjectVersion(value = 1.0f)
public class HazardEvent implements IHazardEvent, IValidator {

    /**
     * The issuing site ID
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.SITE_ID)
    private String siteID;

    /**
     * The event ID
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_EVENT_IDENTIFIER)
    private String eventID;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.UNIQUE_ID)
    private String uniqueID = UUID.randomUUID().toString();

    /**
     * The status of the record at this point in time
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_EVENT_STATUS)
    private HazardStatus status;

    /**
     * Phenomenon that is being recorded
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.PHENOMENON)
    private String phenomenon;

    /**
     * Significance of the hazard
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.SIGNIFICANCE)
    private String significance;

    /**
     * subtype of the hazard
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_EVENT_SUB_TYPE)
    private String subType;

    /**
     * The start time of the hazard
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.HAZARD_EVENT_START_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private Date startTime;

    /**
     * The end time of the hazard
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.HAZARD_EVENT_END_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private Date endTime;

    /**
     * The time this hazard was created
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.CREATION_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private Date creationTime;

    /**
     * The mode of the hazard
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_MODE)
    private ProductClass hazardMode;

    /**
     * The geometry coverage of the hazard
     */
    @DynamicSerializeElement
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @XmlAttribute
    @SlotAttribute(HazardConstants.GEOMETRY)
    @SlotAttributeConverter(GeometrySlotConverter.class)
    private Geometry geometry;

    /**
     * The time this hazard was inserted into the repository
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.INSERT_TIME)
    private Date insertTime;

    /**
     * The user name of the person who created the hazard or the last person
     * that issue the hazard.
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.USER_NAME)
    private String userName;

    /**
     * The workstation of the person who created the hazard or the last person
     * that issue the hazard.
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.WORKSTATION)
    private String workStation;

    /**
     * Additional attributes of the hazard
     */
    @DynamicSerializeElement
    @XmlElement(name = "Attributes")
    @SlotAttribute("Attributes")
    @SlotAttributeConverter(HazardAttributeSlotConverter.class)
    private Set<HazardAttribute> attributes = new HashSet<HazardAttribute>();

    /**
     * Creates a new HazardEvent
     */
    public HazardEvent() {

    }

    /**
     * Creates a copy of the given hazard event
     * 
     * @param event
     *            The hazard event to copy
     */
    public HazardEvent(IHazardEvent event) {
        this();
        setSiteID(event.getSiteID());
        setEndTime(event.getEndTime());
        setEventID(event.getEventID());
        setStartTime(event.getStartTime());
        setCreationTime(event.getCreationTime());
        setGeometry(event.getGeometry());
        setPhenomenon(event.getPhenomenon());
        setSignificance(event.getSignificance());
        setSubType(event.getSubType());
        setStatus(event.getStatus());
        setHazardMode(event.getHazardMode());
        setWorkStation(event.getWorkStation());
        setUserName(event.getUserName());
        if (event.getHazardAttributes() != null) {
            setHazardAttributes(new HashMap<String, Serializable>(
                    event.getHazardAttributes()));
        }
    }

    /**
     * @return the siteID
     */
    @Override
    public String getSiteID() {
        return siteID;
    }

    /**
     * @param siteID
     *            the siteID to set
     */
    @Override
    public void setSiteID(String site) {
        this.siteID = site;
    }

    /**
     * @return the eventID
     */
    @Override
    public String getEventID() {
        return eventID;
    }

    /**
     * @param eventID
     *            the eventID to set
     */
    @Override
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

    /**
     * @return the status
     */
    @Override
    public HazardStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    @Override
    public void setStatus(HazardStatus state) {
        this.status = state;
    }

    /**
     * @return the phenomenon
     */
    @Override
    public String getPhenomenon() {
        return phenomenon;
    }

    /**
     * @param phenomenon
     *            the phenomenon to set
     */
    @Override
    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    /**
     * @return the significance
     */
    @Override
    public String getSignificance() {
        return significance;
    }

    /**
     * @param significance
     *            the significance to set
     */
    @Override
    public void setSignificance(String significance) {
        this.significance = significance;
    }

    /**
     * @return subtype
     */
    @Override
    public String getSubType() {
        return subType;
    }

    /**
     * @param subtype
     */
    @Override
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * @return hazardType e.g. FA.A or FF.W.Convective
     */
    @Override
    public String getHazardType() {
        return HazardEventUtilities.getHazardType(this);
    }

    @Override
    public void setHazardType(String phenomenon, String significance,
            String subtype) {
        setPhenomenon(phenomenon);
        setSignificance(significance);
        setSubType(subtype);
    }

    /**
     * @return the startTime
     */
    @Override
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    @Override
    public void setStartTime(Date startTime) {
        this.startTime = new Date(startTime.getTime());
    }

    /**
     * @return the endTime
     */
    @Override
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     *            the endTime to set
     */
    @Override
    public void setEndTime(Date endTime) {
        this.endTime = new Date(endTime.getTime());
    }

    @Override
    public void setTimeRange(Date startTime, Date endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
    }

    /**
     * @return the creationTime
     */
    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime
     *            the creationTime to set
     */
    @Override
    public void setCreationTime(Date creationTime) {
        this.creationTime = new Date(creationTime.getTime());
    }

    /**
     * @return the hazardType
     */
    @Override
    public ProductClass getHazardMode() {
        return hazardMode;
    }

    /**
     * @param hazardType
     *            the hazardType to set
     */
    @Override
    public void setHazardMode(ProductClass hazardMode) {
        this.hazardMode = hazardMode;
    }

    /**
     * @return the geometry
     */
    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * @param geometry
     *            the geometry to set
     */
    @Override
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * @return the hazardAttributes
     */
    @Override
    public Map<String, Serializable> getHazardAttributes() {
        Map<String, Serializable> attrs = new HashMap<String, Serializable>();

        for (HazardAttribute attribute : attributes) {
            attrs.put(attribute.getKey(),
                    (Serializable) attribute.getValueObject());
        }
        return attrs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * addHazardAttribute(java.lang.String, java.io.Serializable)
     */
    @Override
    public void addHazardAttribute(final String key, final Serializable value) {
        this.attributes.add(new HazardAttribute(eventID, key, value));

    }

    @Override
    public void addHazardAttributes(Map<String, Serializable> attributes) {
        for (Map.Entry<String, Serializable> entry : attributes.entrySet()) {
            addHazardAttribute(entry.getKey(), entry.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * getHazardAttribute(java.lang.String)
     */
    @Override
    public Serializable getHazardAttribute(String key) {
        return getHazardAttributes().get(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * removeHazardAttribute(java.lang.String)
     */
    @Override
    public void removeHazardAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public boolean isValid() throws ValidationException {
        // TODO: Determine validation strategy
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventId : ").append(eventID).append("\n");
        builder.append("Site : ").append(siteID).append("\n");
        builder.append("Phensig : ").append(phenomenon).append(".")
                .append(significance).append("\n");
        builder.append("Creation Time : ")
                .append(new Date(creationTime.getTime())).append("\n");
        builder.append("Start Time : ").append(new Date(startTime.getTime()))
                .append("\n");
        builder.append("End Time : ").append(new Date(endTime.getTime()))
                .append("\n");
        if (this.attributes.isEmpty() == false) {
            builder.append("--Attributes--\n");
            for (HazardAttribute attr : this.attributes) {
                builder.append(attr.toString());
            }
        }
        return builder.toString();
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
                + ((creationTime == null) ? 0 : creationTime.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
        result = prime * result
                + ((geometry == null) ? 0 : geometry.hashCode());
        result = prime * result
                + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result
                + ((hazardMode == null) ? 0 : hazardMode.hashCode());
        result = prime * result
                + ((insertTime == null) ? 0 : insertTime.hashCode());
        result = prime * result
                + ((phenomenon == null) ? 0 : phenomenon.hashCode());
        result = prime * result
                + ((significance == null) ? 0 : significance.hashCode());
        result = prime * result + ((siteID == null) ? 0 : siteID.hashCode());
        result = prime * result
                + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((subType == null) ? 0 : subType.hashCode());
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
        HazardEvent other = (HazardEvent) obj;
        if (creationTime == null) {
            if (other.creationTime != null) {
                return false;
            }
        } else if (!creationTime.equals(other.creationTime)) {
            return false;
        }
        if (endTime == null) {
            if (other.endTime != null) {
                return false;
            }
        } else if (!endTime.equals(other.endTime)) {
            return false;
        }
        if (eventID == null) {
            if (other.eventID != null) {
                return false;
            }
        } else if (!eventID.equals(other.eventID)) {
            return false;
        }
        if (geometry == null) {
            if (other.geometry != null) {
                return false;
            }
        } else if (!geometry.equals(other.geometry)) {
            return false;
        }
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (hazardMode != other.hazardMode) {
            return false;
        }
        if (insertTime == null) {
            if (other.insertTime != null) {
                return false;
            }
        } else if (!insertTime.equals(other.insertTime)) {
            return false;
        }
        if (phenomenon == null) {
            if (other.phenomenon != null) {
                return false;
            }
        } else if (!phenomenon.equals(other.phenomenon)) {
            return false;
        }
        if (significance == null) {
            if (other.significance != null) {
                return false;
            }
        } else if (!significance.equals(other.significance)) {
            return false;
        }
        if (siteID == null) {
            if (other.siteID != null) {
                return false;
            }
        } else if (!siteID.equals(other.siteID)) {
            return false;
        }
        if (startTime == null) {
            if (other.startTime != null) {
                return false;
            }
        } else if (!startTime.equals(other.startTime)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        if (subType == null) {
            if (other.subType != null) {
                return false;
            }
        } else if (!subType.equals(other.subType)) {
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

    @Override
    public void setInsertTime(Date date) {
        this.insertTime = date;

    }

    @Override
    public Date getInsertTime() {
        return this.insertTime;
    }

    @Override
    public void setHazardAttributes(Map<String, Serializable> attributes) {
        this.attributes.clear();
        for (Entry<String, Serializable> entry : attributes.entrySet()) {
            addHazardAttribute(entry.getKey(), entry.getValue());
        }
    }

    public Set<HazardAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(Set<HazardAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Geometry getProductGeometry() {
        return HazardEventUtilities.getProductGeometry(this);
    }

    @Override
    public void setProductGeometry(Geometry geom) {
        HazardEventUtilities.setProductGeometry(this, geom);
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setWorkStation(String workStation) {
        this.workStation = workStation;
    }

    @Override
    public String getWorkStation() {
        return workStation;
    }
}
