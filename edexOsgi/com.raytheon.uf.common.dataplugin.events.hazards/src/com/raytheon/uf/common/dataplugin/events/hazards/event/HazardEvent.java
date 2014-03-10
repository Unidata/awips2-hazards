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
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.raytheon.uf.common.dataplugin.events.IValidator;
import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.annotations.RegistryObjectVersion;
import com.raytheon.uf.common.registry.annotations.SlotAttribute;
import com.raytheon.uf.common.registry.annotations.SlotAttributeConverter;
import com.raytheon.uf.common.registry.ebxml.slots.DateSlotConverter;
import com.raytheon.uf.common.registry.ebxml.slots.GeometrySlotConverter;
import com.raytheon.uf.common.serialization.adapters.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
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

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEvent.class);

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.SITE_ID)
    private String siteID;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_EVENT_IDENTIFIER)
    private String eventID;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.UNIQUE_ID)
    private String uniqueID;

    /**
     * The state of the record at this point in time
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_EVENT_STATE)
    private HazardState state;

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

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.HAZARD_EVENT_START_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private Date startTime;

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.HAZARD_EVENT_END_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private Date endTime;

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.ISSUE_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private Date issueTime;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_MODE)
    private ProductClass hazardMode;

    @DynamicSerializeElement
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @XmlAttribute
    @SlotAttribute(HazardConstants.GEOMETRY)
    @SlotAttributeConverter(GeometrySlotConverter.class)
    private Geometry geometry;

    @DynamicSerializeElement
    @XmlElement
    private Set<HazardAttribute> hazardAttributesSerializable;

    @Transient
    private Map<String, Serializable> hazardAttributes;

    /**
     * It is not recommended to declare a {@link HazardEvent}. The better
     * approach is to use {@link IHazardEventManager} manager = new
     * {@link HazardEventManager}; {@link IHazardEventManager#createEvent()}
     */
    public HazardEvent() {
        uniqueID = UUID.randomUUID().toString();
        hazardAttributesSerializable = new HashSet<HazardAttribute>();
        hazardAttributes = new HashMap<String, Serializable>();
    }

    public HazardEvent(IHazardEvent event) {
        this();
        setSiteID(event.getSiteID());
        setEndTime(event.getEndTime());
        setEventID(event.getEventID());
        setStartTime(event.getStartTime());
        setIssueTime(event.getIssueTime());
        setGeometry(event.getGeometry());
        setPhenomenon(event.getPhenomenon());
        setSignificance(event.getSignificance());
        setSubType(event.getSubType());
        setState(event.getState());
        setHazardMode(event.getHazardMode());
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
     * @return the state
     */
    @Override
    public HazardState getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    @Override
    public void setState(HazardState state) {
        this.state = state;
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

    /**
     * @return the issueTime
     */
    @Override
    public Date getIssueTime() {
        return issueTime;
    }

    /**
     * @param issueTime
     *            the issueTime to set
     */
    @Override
    public void setIssueTime(Date issueTime) {
        this.issueTime = new Date(issueTime.getTime());
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
     * @return the hazardAttributesSerializable
     */
    public Set<HazardAttribute> getHazardAttributesSerializable() {
        return hazardAttributesSerializable;
    }

    /**
     * @param hazardAttributesSerializable
     *            the hazardAttributesSerializable to set
     */
    public void setHazardAttributesSerializable(
            Set<HazardAttribute> hazardAttributesSerializable) {
        this.hazardAttributesSerializable = hazardAttributesSerializable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * getHazardAttributes()
     */
    @Override
    public Map<String, Serializable> getHazardAttributes() {
        if (hazardAttributes == null
                || hazardAttributes.size() != hazardAttributesSerializable
                        .size()) {
            hazardAttributes = new HashMap<String, Serializable>();
            for (IHazardAttribute attr : hazardAttributesSerializable) {
                hazardAttributes.put(attr.getKey(), attr.getValue());
            }
        }
        return hazardAttributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * setHazardAttributes(java.util.List)
     */
    @Override
    public void setHazardAttributes(Map<String, Serializable> attributes) {
        if (hazardAttributesSerializable == null
                || hazardAttributesSerializable.size() != hazardAttributes
                        .size()) {
            hazardAttributesSerializable = new HashSet<HazardAttribute>();
            for (Entry<String, Serializable> entry : attributes.entrySet()) {
                hazardAttributesSerializable.add(new HazardAttribute(eventID,
                        entry.getKey(), entry.getValue()));
            }
        }
        this.hazardAttributes = attributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * addHazardAttribute(java.lang.String, java.io.Serializable)
     */
    @Override
    public void addHazardAttribute(String key, Serializable value) {
        HazardAttribute attr = new HazardAttribute(eventID, key, value);
        try {
            attr.isValid();
            hazardAttributes.put(key, value);
            hazardAttributesSerializable.add(attr);
        } catch (ValidationException e) {
            statusHandler.handle(Priority.ERROR, "Unable to validate "
                    + eventID, e);
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
        Iterator<HazardAttribute> attrIter = hazardAttributesSerializable
                .iterator();
        while (attrIter.hasNext()) {
            IHazardAttribute attr = attrIter.next();
            if (attr.getKey().equals(key)) {
                attrIter.remove();
                break;
            }
        }
        hazardAttributes.remove(key);
    }

    @Override
    public boolean isValid() throws ValidationException {
        // future validation here, read from the necessary file
        if (true) {
            return true;
        }
        for (Field field : getClass().getDeclaredFields()) {
            try {
                if (field.get(this) == null) {
                    throw new ValidationException("Unable to validate event.  "
                            + field.getName() + " + is missing or not set.");
                }
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Unable to validate event.  "
                        + field.getName() + " + is missing or not set.");
            } catch (IllegalAccessException e) {
                throw new ValidationException("Unable to validate event.  "
                        + field.getName() + " + is missing or not set.");
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventId : ").append(eventID).append("\n");
        builder.append("Site : ").append(siteID).append("\n");
        builder.append("Phensig : ").append(phenomenon).append(".")
                .append(significance).append("\n");
        builder.append("Issue Time : ").append(new Date(issueTime.getTime()))
                .append("\n");
        builder.append("Start Time : ").append(new Date(startTime.getTime()))
                .append("\n");
        builder.append("End Time : ").append(new Date(endTime.getTime()))
                .append("\n");
        if (hazardAttributesSerializable.isEmpty() == false) {
            builder.append("--Attributes--\n");
            for (IHazardAttribute attr : hazardAttributesSerializable) {
                builder.append(attr.getKey()).append(":")
                        .append(attr.getValue()).append("\n");
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
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
        result = prime * result
                + ((geometry == null) ? 0 : geometry.hashCode());
        result = prime
                * result
                + ((hazardAttributes == null) ? 0 : hazardAttributes.hashCode());
        result = prime
                * result
                + ((hazardAttributesSerializable == null) ? 0
                        : hazardAttributesSerializable.hashCode());
        result = prime * result
                + ((hazardMode == null) ? 0 : hazardMode.hashCode());
        result = prime * result
                + ((issueTime == null) ? 0 : issueTime.hashCode());
        result = prime * result
                + ((phenomenon == null) ? 0 : phenomenon.hashCode());
        result = prime * result
                + ((significance == null) ? 0 : significance.hashCode());
        result = prime * result + ((siteID == null) ? 0 : siteID.hashCode());
        result = prime * result
                + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
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
        if (hazardAttributesSerializable == null) {
            if (other.hazardAttributesSerializable != null) {
                return false;
            }
        } else if (!hazardAttributesSerializable
                .equals(other.hazardAttributesSerializable)) {
            return false;
        }
        if (hazardMode != other.hazardMode) {
            return false;
        }
        if (issueTime == null) {
            if (other.issueTime != null) {
                return false;
            }
        } else if (!issueTime.equals(other.issueTime)) {
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
        if (state != other.state) {
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
}
