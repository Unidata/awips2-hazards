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
import com.raytheon.uf.common.registry.annotations.SlotAttribute;
import com.raytheon.uf.common.registry.annotations.SlotAttributeConverter;
import com.raytheon.uf.common.registry.ebxml.slots.DateSlotConverter;
import com.raytheon.uf.common.registry.ebxml.slots.GeometrySlotConverter;
import com.raytheon.uf.common.serialization.ISerializableObject;
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
 * Aug 16, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@XmlRootElement(name = "hazard")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@RegistryObject({ HazardConstants.SITE, HazardConstants.EVENTID,
        HazardConstants.ISSUETIME })
public class HazardEvent implements IHazardEvent, ISerializableObject,
        IValidator {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEvent.class);

    @DynamicSerialize
    private static class RegistryDate extends Date {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        public RegistryDate() {
            super();
        }

        public RegistryDate(Date date) {
            super(date.getTime());
        }

        @Override
        public String toString() {
            return String.valueOf(getTime());
        }

    }

    private static final long serialVersionUID = 1L;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.SITE)
    private String site;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.EVENTID)
    private String eventId;

    /**
     * The state of the record at this point in time
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.STATE)
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

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.STARTTIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private RegistryDate startTime;

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.ENDTIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private RegistryDate endTime;

    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.ISSUETIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private RegistryDate issueTime;

    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARDMODE)
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
        eventId = UUID.randomUUID().toString();
        hazardAttributesSerializable = new HashSet<HazardAttribute>();
        hazardAttributes = new HashMap<String, Serializable>();
    }

    /**
     * @return the site
     */
    public String getSite() {
        return site;
    }

    /**
     * @param site
     *            the site to set
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * @return the eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @param eventId
     *            the eventId to set This is here to support dynamic serialize.
     *            It is not recommended for use. An eventId gets set when the
     *            event gets created.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * @return the state
     */
    public HazardState getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(HazardState state) {
        this.state = state;
    }

    /**
     * @return the phenomenon
     */
    public String getPhenomenon() {
        return phenomenon;
    }

    /**
     * @param phenomenon
     *            the phenomenon to set
     */
    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    /**
     * @return the significance
     */
    public String getSignificance() {
        return significance;
    }

    /**
     * @param significance
     *            the significance to set
     */
    public void setSignificance(String significance) {
        this.significance = significance;
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
    public void setStartTime(Date startTime) {
        this.startTime = new RegistryDate(startTime);
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
    public void setEndTime(Date endTime) {
        this.endTime = new RegistryDate(endTime);
    }

    /**
     * @return the issueTime
     */
    public Date getIssueTime() {
        return issueTime;
    }

    /**
     * @param issueTime
     *            the issueTime to set
     */
    public void setIssueTime(Date issueTime) {
        this.issueTime = new RegistryDate(issueTime);
    }

    /**
     * @return the hazardType
     */
    public ProductClass getHazardMode() {
        return hazardMode;
    }

    /**
     * @param hazardType
     *            the hazardType to set
     */
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
        HazardAttribute attr = new HazardAttribute(eventId, key, value);
        try {
            attr.isValid();
            hazardAttributes.put(key, value);
            hazardAttributesSerializable.add(attr);
        } catch (ValidationException e) {
            statusHandler.handle(Priority.ERROR, "Unable to validate "
                    + eventId, e);
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
        return hazardAttributes.get(key);
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
        builder.append("EventId : ").append(eventId).append("\n");
        builder.append("Site : ").append(site).append("\n");
        builder.append("Phensig : ").append(phenomenon).append(".")
                .append(significance).append("\n");
        builder.append("Issue Time : ").append(new Date(issueTime.getTime()))
                .append("\n");
        builder.append("Start Time : ").append(new Date(startTime.getTime()))
                .append("\n");
        builder.append("End Time : ").append(new Date(endTime.getTime()))
                .append("\n");
        return builder.toString();
    }
}
