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
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * An {@link IHazardEvent} that has no storage annotations, rather, it can be
 * converted into any other {@link IHazardEvent}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 24, 2013            mnash       Initial creation
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * Dec    2013  2368       thansen   Added getHazardType
 * Apr 23, 2014 2925       Chris.Golden Augmented with additional methods to
 *                                      set the type components atomically, or
 *                                      the start and end time atomically.
 * Jun 30, 2014 3512       Chris.Golden Added addHazardAttributes() method.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class BaseHazardEvent implements IHazardEvent {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(BaseHazardEvent.class);

    private Date startTime;

    private Date endTime;

    private Geometry geometry;

    private String site;

    private String eventId;

    private HazardStatus hazardState;

    private String phenomenon;

    private String significance;

    private String subtype;

    private Date creationTime;

    private ProductClass hazardMode;

    private Map<String, Serializable> attributes;

    /**
     * 
     */
    public BaseHazardEvent() {
        attributes = new HashMap<String, Serializable>();
    }

    public BaseHazardEvent(IHazardEvent event) {
        this();
        setEventID(event.getEventID());
        setSiteID(event.getSiteID());
        setEndTime(event.getEndTime());
        setStartTime(event.getStartTime());
        setCreationTime(event.getCreationTime());
        setGeometry(event.getGeometry());
        setPhenomenon(event.getPhenomenon());
        setSignificance(event.getSignificance());
        setSubType(event.getSubType());
        setStatus(event.getStatus());
        setHazardMode(event.getHazardMode());
        if (event.getHazardAttributes() != null) {
            getHazardAttributes().putAll(event.getHazardAttributes());
        }
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Deprecated
    public void setGeometry(String geom) {
        WKTReader reader = new WKTReader();
        try {
            setGeometry(reader.read(geom));
        } catch (ParseException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to read in geometry text", e);
        }
    }

    @Override
    public String getSiteID() {
        return site;
    }

    @Override
    public void setSiteID(String site) {
        this.site = site;
    }

    @Override
    public String getEventID() {
        return eventId;
    }

    @Override
    public void setEventID(String uuid) {
        this.eventId = uuid;
    }

    @Override
    public HazardStatus getStatus() {
        return hazardState;
    }

    @Override
    public void setStatus(HazardStatus state) {
        this.hazardState = state;
    }

    @Deprecated
    public void setStatus(String state) {
        setStatus(HazardStatus.valueOf(String.valueOf(state).toUpperCase()));
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
        return subtype;
    }

    @Override
    public void setSubType(String subtype) {
        this.subtype = subtype;
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
    public Date getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public void setEndTime(Date date) {
        this.endTime = date;
    }

    @Override
    public void setStartTime(Date date) {
        this.startTime = date;
    }

    @Override
    public void setTimeRange(Date startTime, Date endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
    }

    @Override
    public void setGeometry(Geometry geom) {
        this.geometry = geom;
    }

    @Override
    public ProductClass getHazardMode() {
        return hazardMode;
    }

    @Override
    public void setHazardMode(ProductClass mode) {
        this.hazardMode = mode;
    }

    @Deprecated
    public void setHazardMode(String mode) {
        try {
            setHazardMode(HazardConstants.productClassFromAbbreviation(mode));
        } catch (IllegalArgumentException e) {
            try {
                setHazardMode(HazardConstants.productClassFromName(mode));
            } catch (IllegalArgumentException f) {
                // default to test
                setHazardMode(ProductClass.TEST);
            }
        }
    }

    @Override
    public Map<String, Serializable> getHazardAttributes() {
        return attributes;
    }

    @Override
    public void setHazardAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void addHazardAttribute(String key, Serializable value) {
        attributes.put(key, value);
    }

    @Override
    public void addHazardAttributes(Map<String, Serializable> attributes) {
        this.attributes.putAll(attributes);
    }

    @Override
    public void removeHazardAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public Serializable getHazardAttribute(String key) {
        return attributes.get(key);
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
                + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
        result = prime * result
                + ((geometry == null) ? 0 : geometry.hashCode());
        result = prime * result
                + ((hazardMode == null) ? 0 : hazardMode.hashCode());
        result = prime * result
                + ((hazardState == null) ? 0 : hazardState.hashCode());
        result = prime * result
                + ((creationTime == null) ? 0 : creationTime.hashCode());
        result = prime * result
                + ((phenomenon == null) ? 0 : phenomenon.hashCode());
        result = prime * result
                + ((significance == null) ? 0 : significance.hashCode());
        result = prime * result + ((site == null) ? 0 : site.hashCode());
        result = prime * result
                + ((startTime == null) ? 0 : startTime.hashCode());
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseHazardEvent other = (BaseHazardEvent) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (endTime == null) {
            if (other.endTime != null) {
                return false;
            }
        } else if (!endTime.equals(other.endTime)) {
            return false;
        }
        if (eventId == null) {
            if (other.eventId != null) {
                return false;
            }
        } else if (!eventId.equals(other.eventId)) {
            return false;
        }
        if (geometry == null) {
            if (other.geometry != null) {
                return false;
            }
        } else if (!geometry.equals(other.geometry)) {
            return false;
        }
        if (hazardMode != other.hazardMode) {
            return false;
        }
        if (hazardState != other.hazardState) {
            return false;
        }
        if (creationTime == null) {
            if (other.creationTime != null) {
                return false;
            }
        } else if (!creationTime.equals(other.creationTime)) {
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
        if (site == null) {
            if (other.site != null) {
                return false;
            }
        } else if (!site.equals(other.site)) {
            return false;
        }
        if (startTime == null) {
            if (other.startTime != null) {
                return false;
            }
        } else if (!startTime.equals(other.startTime)) {
            return false;
        }
        if (subtype == null) {
            if (other.subtype != null) {
                return false;
            }
        } else if (!subtype.equals(other.subtype)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
