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
import com.raytheon.uf.common.dataplugin.events.hazards.registry.geometryadapters.GeometryAdapter;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.slotconverter.HazardAttributeSlotConverter;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.message.adapter.WsIdAdapter;
import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.annotations.RegistryObjectVersion;
import com.raytheon.uf.common.registry.annotations.SlotAttribute;
import com.raytheon.uf.common.registry.annotations.SlotAttributeConverter;
import com.raytheon.uf.common.registry.ebxml.slots.DateSlotConverter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryAdapter;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.common.visuals.VisualFeaturesListAdapter;

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
 * Aug 03, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * Mar 01, 2016 15676      Chris.Golden Added visual features to hazard event.
 * Mar 26, 2016 15676      Chris.Golden Added more methods to get and set
 *                                      individual visual features.
 * May 02, 2016 18235      Chris.Golden Added source field.
 * May 10, 2016 18515      Chris.Golden Fixed bug that caused duplicate-key hazard
 *                                      attributes to be kept around, so that a
 *                                      particular key (e.g. "cta") might have
 *                                      two or more entries in the attributes
 *                                      set, each with a different value. This
 *                                      same bug also caused attributes that were
 *                                      supposedly removed from the event to not
 *                                      actually be removed.
 * Jun 03, 2016 19403      mduff        Removed some slots.
 * Jun 08, 2016 18899     Ben.Phillippe Changed geometry fields to be xml elements
 *                                      instead of  attributes to avoid the jaxb
 *                                      attribute size limit
 * Jun 10, 2016 19537      Chris.Golden Combined base and selected visual feature
 *                                      lists for each hazard event into one,
 *                                      replaced by visibility constraints
 *                                      based upon selection state to individual
 *                                      visual features.
 * Jun 23, 2016 19537      Chris.Golden Changed to use new visual feature list
 *                                      method for visual feature replacement.
 * Sep 12, 2016 15934      Chris.Golden Changed hazard events to use advanced
 *                                      geometries instead of JTS geometries.
 * Sep 21, 2016 15934      Chris.Golden Changed to work with new version of
 *                                      AdvancedGeometryUtilities.
 * Dec 19, 2016 21504      Robert.Blum  Updates for hazard locking.
 * Feb 01, 2017 15556      Chris.Golden Added visible-in-history-list flag.
 * Feb 13, 2017 28892      Chris.Golden Removed slot converter for visual features
 *                                      list, as visual features should not be
 *                                      put in slots.
 * Feb 16, 2017 29138      Chris.Golden Removed the visible-in-history-list flag
 *                                      since use of the history list is being
 *                                      reduced with advent of ability to save
 *                                      a "latest version" to the database that
 *                                      is not part of the history list. Also
 *                                      added ability to configure instances to
 *                                      indicate they are "latest version" ones,
 *                                      that is, not to be in the history list.
 * Mar 30, 2017 15528      Chris.Golden Added modified flag as part of basic
 *                                      hazard event, since this flag must be
 *                                      persisted as part of the hazard event.
 * May 24, 2017 15561      Chris.Golden Added getPhensig() method.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly
 *                                      mutable session events.
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
     * Value for the {@link HazardEvent#uniqueID} indicating that this is the
     * latest version of the hazard event that has been persisted (not part of
     * the history list).
     */
    public static final String LATEST_VERSION = "latest";

    /**
     * Flag indicating whether or not the hazard event is in a modified state.
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.MODIFIED)
    private boolean modified;

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
    private Date creationTime;

    /**
     * The mode of the hazard
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_MODE)
    private ProductClass hazardMode;

    /**
     * The source of the hazard
     */
    @DynamicSerializeElement
    @XmlAttribute
    @SlotAttribute(HazardConstants.HAZARD_SOURCE)
    private Source source;

    /**
     * The flattened geometry coverage of the hazard
     */
    @DynamicSerializeElement
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @XmlElement
    private Geometry flattenedGeometry;

    /**
     * The geometry coverage of the hazard.
     */
    @DynamicSerializeElement
    @XmlJavaTypeAdapter(value = AdvancedGeometryAdapter.class)
    @XmlElement
    private IAdvancedGeometry geometry;

    /**
     * Visual features list of the hazard.
     * <p>
     * No slot attribute or slot converter is provided because there is no
     * reason why visual features should have their own slots.
     * </p>
     */
    @DynamicSerializeElement
    @XmlJavaTypeAdapter(value = VisualFeaturesListAdapter.class)
    @XmlElement
    private VisualFeaturesList visualFeatures;

    /**
     * The time this hazard was inserted into the repository.
     */
    @DynamicSerializeElement
    @XmlElement
    @SlotAttribute(HazardConstants.INSERT_TIME)
    @SlotAttributeConverter(DateSlotConverter.class)
    private Date insertTime;

    /**
     * The workstation id of the person who created the hazard or the last
     * person that issue the hazard.
     */
    @DynamicSerializeElement
    @XmlElement
    @XmlJavaTypeAdapter(value = WsIdAdapter.class)
    private WsId wsId;

    /**
     * Additional attributes of the hazard.
     */
    @DynamicSerializeElement
    @XmlElement(name = "Attributes")
    @SlotAttribute("Attributes")
    @SlotAttributeConverter(HazardAttributeSlotConverter.class)
    private Set<HazardAttribute> attributes = new HashSet<HazardAttribute>();

    /**
     * Construct a standard instance.
     */
    public HazardEvent() {

    }

    /**
     * Construct a copy instance.
     * 
     * @param event
     *            Hazard event to be copied.
     */
    public HazardEvent(IReadableHazardEvent event) {
        this();
        setModified(event.isModified());
        setEventID(event.getEventID());
        setSiteID(event.getSiteID());
        setEndTime(event.getEndTime());
        setStartTime(event.getStartTime());
        setCreationTime(event.getCreationTime());
        setInsertTime(event.getInsertTime());
        setGeometry(event.getGeometry());
        setVisualFeatures(event.getVisualFeatures());
        setPhenomenon(event.getPhenomenon());
        setSignificance(event.getSignificance());
        setSubType(event.getSubType());
        setStatus(event.getStatus());
        setHazardMode(event.getHazardMode());
        setSource(event.getSource());
        setWsId(event.getWsId());
        if (event.getHazardAttributes() != null) {
            setHazardAttributes(new HashMap<>(event.getHazardAttributes()));
        }
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public String getSiteID() {
        return siteID;
    }

    @Override
    public void setSiteID(String site) {
        this.siteID = site;
    }

    @Override
    public String getEventID() {
        return eventID;
    }

    @Override
    public void setEventID(String eventId) {
        this.eventID = eventId;
    }

    @Override
    public String getDisplayEventID() {
        return (HazardServicesEventIdUtil.getDisplayId(getEventID()));
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public boolean isLatestVersion() {
        return uniqueID.equals(LATEST_VERSION);
    }

    public void setLatestVersion() {
        uniqueID = LATEST_VERSION;
    }

    @Override
    public HazardStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(HazardStatus state) {
        this.status = state;
    }

    @Override
    public String getPhenomenon() {
        return phenomenon;
    }

    @Override
    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    @Override
    public String getSignificance() {
        return significance;
    }

    @Override
    public void setSignificance(String significance) {
        this.significance = significance;
    }

    @Override
    public String getSubType() {
        return subType;
    }

    @Override
    public void setSubType(String subType) {
        this.subType = subType;
    }

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

    @Override
    public String getPhensig() {
        return HazardEventUtilities.getHazardPhenSig(this);
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = new Date(startTime.getTime());
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = new Date(endTime.getTime());
    }

    @Override
    public void setTimeRange(Date startTime, Date endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
    }

    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(Date creationTime) {
        this.creationTime = new Date(creationTime.getTime());
    }

    @Override
    public ProductClass getHazardMode() {
        return hazardMode;
    }

    @Override
    public void setHazardMode(ProductClass hazardMode) {
        this.hazardMode = hazardMode;
    }

    @Override
    public Source getSource() {
        return source;
    }

    @Override
    public void setSource(Source source) {
        this.source = source;
    }

    @Override
    public Geometry getFlattenedGeometry() {
        return flattenedGeometry;
    }

    @Override
    public IAdvancedGeometry getGeometry() {
        return geometry;
    }

    /**
     * Set the flattened geometry to that specified. Needed for Thrift
     * deserialization.
     * 
     * @param flattenedGeometry
     *            Flattened geometry to be used.
     */
    public void setFlattenedGeometry(Geometry flattenedGeometry) {
        this.flattenedGeometry = flattenedGeometry;
    }

    @Override
    public void setGeometry(IAdvancedGeometry geometry) {
        this.geometry = geometry;
        this.flattenedGeometry = AdvancedGeometryUtilities
                .getJtsGeometryAsCollection(geometry);
    }

    @Override
    public VisualFeature getVisualFeature(String identifier) {
        return (visualFeatures == null ? null
                : visualFeatures.getByIdentifier(identifier));
    }

    @Override
    public VisualFeaturesList getVisualFeatures() {
        return visualFeatures;
    }

    @Override
    public boolean setVisualFeature(VisualFeature visualFeature) {
        if (visualFeatures == null) {
            return false;
        }
        return visualFeatures.replace(visualFeature);
    }

    @Override
    public void setVisualFeatures(VisualFeaturesList visualFeatures) {
        this.visualFeatures = visualFeatures;
    }

    @Override
    public Map<String, Serializable> getHazardAttributes() {
        Map<String, Serializable> attrs = new HashMap<>();

        for (HazardAttribute attribute : attributes) {
            attrs.put(attribute.getKey(),
                    (Serializable) attribute.getValueObject());
        }
        return attrs;
    }

    @Override
    public void addHazardAttribute(final String key, final Serializable value) {

        /*
         * Remove the old hazard attribute first, since given that the
         * attributes are a set of HazardAttribute objects, and HazardAttribute
         * does not implement equals() and hashCode() to only consider the key
         * of the object, adding one here will probably cause two hazard
         * attributes with the same key but different values to be left in the
         * set otherwise.
         */
        removeHazardAttribute(key);
        this.attributes.add(new HazardAttribute(eventID, key, value));

    }

    @Override
    public void addHazardAttributes(Map<String, Serializable> attributes) {
        for (Map.Entry<String, Serializable> entry : attributes.entrySet()) {
            addHazardAttribute(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Serializable getHazardAttribute(String key) {
        return getHazardAttributes().get(key);
    }

    @Override
    public void removeHazardAttribute(String key) {
        HazardAttribute attributeToRemove = null;
        for (HazardAttribute attribute : attributes) {
            if (attribute.getKey().equals(key)) {
                attributeToRemove = attribute;
                break;
            }
        }
        if (attributeToRemove != null) {
            attributes.remove(attributeToRemove);
        }
    }

    @Override
    public boolean isValid() throws ValidationException {
        /*
         * TODO: Determine validation strategy.
         */
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (modified ? 1 : 0);
        result = prime * result
                + ((creationTime == null) ? 0 : creationTime.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
        result = prime * result
                + ((geometry == null) ? 0 : geometry.hashCode());
        result = prime * result
                + ((visualFeatures == null) ? 0 : visualFeatures.hashCode());
        result = prime * result
                + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result
                + ((hazardMode == null) ? 0 : hazardMode.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
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
        if (uniqueID == null) {
            if (other.uniqueID != null) {
                return false;
            }
        } else if (!uniqueID.equals(other.uniqueID)) {
            return false;
        }
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
        if (visualFeatures == null) {
            if (other.visualFeatures != null) {
                return false;
            }
        } else if (!visualFeatures.equals(other.visualFeatures)) {
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
        if (source != other.source) {
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
        return (modified == other.modified);
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
    public WsId getWsId() {
        return wsId;
    }

    @Override
    public void setWsId(WsId wsId) {
        this.wsId = wsId;
    }
}
