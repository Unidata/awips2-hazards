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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.events.IValidator;
import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.adapters.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Behaves the same as a real hazard event, only with annotations to store
 * separately so we don't accidentally store practice events with operational
 * events
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 1, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@Entity
@Table(name = "practice_hazards")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class PracticeHazardEvent extends PersistableDataObject implements
        IHazardEvent, ISerializableObject, IValidator {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PracticeHazardEvent.class);

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @DynamicSerializeElement
    @XmlElement
    @JoinColumn
    private PracticeHazardEventPK key;

    /**
     * The state of the record at this point in time
     */
    @Column
    @DynamicSerializeElement
    @XmlElement
    @Enumerated(EnumType.STRING)
    private HazardState state;

    /**
     * Phenomenon that is being recorded
     */
    @Column
    @DynamicSerializeElement
    @XmlElement
    private String phenomenon;

    /**
     * Significance of the hazard
     */
    @Column
    @DynamicSerializeElement
    @XmlElement
    private String significance;

    /**
     * Subtype of the hazard
     */
    @Column
    @DynamicSerializeElement
    @XmlElement
    private String subtype;

    @DynamicSerializeElement
    @XmlElement
    @Column
    private Date issueTime;

    @Column
    @DynamicSerializeElement
    @XmlElement
    private Date startTime;

    @Column
    @DynamicSerializeElement
    @XmlElement
    private Date endTime;

    @Column
    @DynamicSerializeElement
    @XmlElement
    @Enumerated(EnumType.STRING)
    private ProductClass hazardMode;

    @Column(name = "geometry", columnDefinition = "geometry")
    @Type(type = "com.raytheon.edex.db.objects.hibernate.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    @XmlElement
    private Geometry geometry;

    @DynamicSerializeElement
    @XmlElement
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private Set<PracticeHazardAttribute> hazardAttrsSerializable;

    @Transient
    private Map<String, Serializable> hazardAttributes;

    /**
     * Do not call this constructor directly. Rather, use
     * {@link HazardEventManager} and {@link HazardEventManager#createEvent()}
     */
    public PracticeHazardEvent() {
        // set the eventid here so that it can't be set anywhere else
        key = new PracticeHazardEventPK();
        hazardAttrsSerializable = new HashSet<PracticeHazardAttribute>();
        hazardAttributes = new HashMap<String, Serializable>();
    }

    public PracticeHazardEvent(IHazardEvent event) {
        this();
        setSiteID(event.getSiteID());
        setEndTime(event.getEndTime());
        setStartTime(event.getStartTime());
        setIssueTime(event.getIssueTime());
        setGeometry(event.getGeometry());
        setPhenomenon(event.getPhenomenon());
        setSignificance(event.getSignificance());
        setSubtype(event.getSubtype());
        setState(event.getState());
        setHazardMode(event.getHazardMode());
        if (event.getHazardAttributes() != null) {
            setHazardAttributes(event.getHazardAttributes());
        }
    }

    /**
     * @return the key
     */
    public PracticeHazardEventPK getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(PracticeHazardEventPK key) {
        this.key = key;
    }

    /**
     * @return the site
     */
    @Override
    public String getSiteID() {
        return key.getSiteID();
    }

    /**
     * @param site
     *            the site to set
     */
    @Override
    public void setSiteID(String site) {
        key.setSiteID(site);
    }

    /**
     * @return the eventId
     */
    @Override
    public String getEventID() {
        return key.getEventID();
    }

    /**
     * This is not to be used. This is set on construction of the object.
     */
    @Override
    public void setEventID(String eventId) {
        key.setEventID(eventId);
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
     * @return the subtype
     */
    @Override
    public String getSubtype() {
        return subtype;
    }

    /**
     * @param subtype
     *            the subtype to set
     */
    @Override
    public void setSubtype(String subtype) {
        this.subtype = subtype;
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
        this.startTime = startTime;
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
        this.endTime = endTime;
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
        this.issueTime = issueTime;
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
     * @return the hazardAttrsSerializable
     * 
     *         This method should NOT be called outside of serialization
     */
    public Set<PracticeHazardAttribute> getHazardAttrsSerializable() {
        return hazardAttrsSerializable;
    }

    /**
     * @param hazardAttrsSerializable
     *            the hazardAttrsSerializable to set
     * 
     *            This method should NOT be called outside of serialization
     */
    public void setHazardAttrsSerializable(
            Set<PracticeHazardAttribute> hazardAttrsSerializable) {
        this.hazardAttrsSerializable = hazardAttrsSerializable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * getHazardAttributes()
     */
    @Override
    public Map<String, Serializable> getHazardAttributes() {
        if (hazardAttributes == null) {
            hazardAttributes = new HashMap<String, Serializable>();
            for (IHazardAttribute attr : hazardAttrsSerializable) {
                hazardAttributes.put(attr.getKey(), attr.getValue());
            }
        }
        return hazardAttributes;
    }

    /**
     * @param hazardAttributes
     *            the hazardAttributes to set
     */
    public void setHazardAttributes(Map<String, Serializable> hazardAttributes) {
        Set<PracticeHazardAttribute> attrs = new HashSet<PracticeHazardAttribute>();
        for (Entry<String, Serializable> entry : hazardAttributes.entrySet()) {
            attrs.add(new PracticeHazardAttribute(key, entry.getKey(), entry
                    .getValue()));
        }
        this.hazardAttrsSerializable = attrs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * addHazardAttribute(java.lang.String, java.io.Serializable)
     */
    @Override
    public void addHazardAttribute(String mapKey, Serializable value) {
        PracticeHazardAttribute attr = new PracticeHazardAttribute(key, mapKey,
                value);
        try {
            attr.isValid();
            hazardAttributes.put(mapKey, value);
            hazardAttrsSerializable.add(attr);
        } catch (ValidationException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to validate " + mapKey, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent#
     * removeHazardAttribute(java.lang.String)
     */
    @Override
    public void removeHazardAttribute(String key) {
        for (Iterator<PracticeHazardAttribute> attrIter = hazardAttrsSerializable
                .iterator(); attrIter.hasNext();) {
            IHazardAttribute attr = attrIter.next();
            if (attr.getKey().equals(key)) {
                attrIter.remove();
                break;
            }
        }
        hazardAttributes.remove(key);
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
     * @see com.raytheon.uf.common.dataplugin.events.IValidator#isValid()
     */
    @Override
    public boolean isValid() throws ValidationException {
        // TODO remove for later validation
        if (true) {
            return true;
        }
        // check the validity of the PracticeHazardEvent, meaning, is everything
        // pertinent set (all the fields need to be set)
        for (Field field : getClass().getDeclaredFields()) {
            try {
                if (field.get(this) == null) {
                    throw new ValidationException("Unable to validate event.  "
                            + field.getName() + " is missing or not set.");
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
        builder.append("EventId : ").append(key.getEventID()).append("\n");
        builder.append("Site : ").append(getSiteID()).append("\n");
        builder.append("Phensig : ").append(phenomenon).append(".")
                .append(significance).append("\n");
        builder.append("Issue Time : ")
                .append(new Date(getIssueTime().getTime())).append("\n");
        builder.append("Start Time : ").append(new Date(startTime.getTime()))
                .append("\n");
        builder.append("End Time : ").append(new Date(endTime.getTime()))
                .append("\n");
        if (hazardAttrsSerializable.isEmpty() == false) {
            builder.append("--Attributes--\n");
            for (IHazardAttribute attr : hazardAttrsSerializable) {
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
        result = prime * result
                + ((geometry == null) ? 0 : geometry.hashCode());
        result = prime
                * result
                + ((hazardAttributes == null) ? 0 : hazardAttributes.hashCode());
        result = prime
                * result
                + ((hazardAttrsSerializable == null) ? 0
                        : hazardAttrsSerializable.hashCode());
        result = prime * result
                + ((hazardMode == null) ? 0 : hazardMode.hashCode());
        result = prime * result
                + ((issueTime == null) ? 0 : issueTime.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result
                + ((phenomenon == null) ? 0 : phenomenon.hashCode());
        result = prime * result
                + ((significance == null) ? 0 : significance.hashCode());
        result = prime * result
                + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
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
        PracticeHazardEvent other = (PracticeHazardEvent) obj;
        if (endTime == null) {
            if (other.endTime != null)
                return false;
        } else if (!endTime.equals(other.endTime))
            return false;
        if (geometry == null) {
            if (other.geometry != null)
                return false;
        } else if (!geometry.equals(other.geometry))
            return false;
        if (hazardAttrsSerializable == null) {
            if (other.hazardAttrsSerializable != null)
                return false;
        } else if (!hazardAttrsSerializable
                .equals(other.hazardAttrsSerializable))
            return false;
        if (hazardMode != other.hazardMode)
            return false;
        if (issueTime == null) {
            if (other.issueTime != null)
                return false;
        } else if (!issueTime.equals(other.issueTime))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (phenomenon == null) {
            if (other.phenomenon != null)
                return false;
        } else if (!phenomenon.equals(other.phenomenon))
            return false;
        if (significance == null) {
            if (other.significance != null)
                return false;
        } else if (!significance.equals(other.significance))
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else if (!startTime.equals(other.startTime))
            return false;
        if (state != other.state)
            return false;
        if (subtype == null) {
            if (other.subtype != null)
                return false;
        } else if (!subtype.equals(other.subtype))
            return false;
        return true;
    }
}
