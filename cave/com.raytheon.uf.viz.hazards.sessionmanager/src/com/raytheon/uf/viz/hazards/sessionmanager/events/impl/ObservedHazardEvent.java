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
package com.raytheon.uf.viz.hazards.sessionmanager.events.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IllegalEventModificationException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStateModified;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A hazard event which notifies the SessionEventManager whenever fields are
 * changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 23, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class ObservedHazardEvent implements IHazardEvent {

    private final SessionEventManager eventManager;

    private final IHazardEvent delegate;

    public Date getStartTime() {
        return delegate.getStartTime();
    }

    public Date getEndTime() {
        return delegate.getEndTime();
    }

    public Geometry getGeometry() {
        return delegate.getGeometry();
    }

    public String getSiteID() {
        return delegate.getSiteID();
    }

    public String getEventID() {
        return delegate.getEventID();
    }

    public HazardState getState() {
        return delegate.getState();
    }

    public String getPhenomenon() {
        return delegate.getPhenomenon();
    }

    public String getSignificance() {
        return delegate.getSignificance();
    }

    public String getSubtype() {
        return delegate.getSubtype();
    }

    public Date getIssueTime() {
        return delegate.getIssueTime();
    }

    public ProductClass getHazardMode() {
        return delegate.getHazardMode();
    }

    public Serializable getHazardAttribute(String key) {
        return delegate.getHazardAttribute(key);
    }

    public ObservedHazardEvent(IHazardEvent event,
            SessionEventManager eventManager) {
        delegate = new BaseHazardEvent(event);
        Assert.isNotNull(eventManager);
        this.eventManager = eventManager;
    }

    private final boolean changed(Object newObj, Object oldObj) {
        if (newObj == null) {
            if (oldObj == null) {
                return false;
            }
        } else if (newObj.equals(oldObj)) {
            return false;
        }
        return true;
    }

    private final boolean changed(Geometry newObj, Geometry oldObj) {
        if (newObj == null) {
            if (oldObj == null) {
                return false;
            }
        } else if (newObj.equalsExact(oldObj)) {
            return false;
        } else if (newObj.equals(oldObj)) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Serializable> getHazardAttributes() {
        Map<String, Serializable> attr = delegate.getHazardAttributes();
        // Do not allow modification because listeners would need to be fired.
        attr = Collections.unmodifiableMap(delegate.getHazardAttributes());
        return attr;
    }

    @Override
    public void setSiteID(String site) {
        setSiteID(site, true);
    }

    @Override
    public void setEventID(String eventId) {
        setEventID(eventId, true);
    }

    @Override
    public void setState(HazardState state) {
        setState(state, true, true);
    }

    @Override
    public void setPhenomenon(String phenomenon) {
        setPhenomenon(phenomenon, true);
    }

    @Override
    public void setSignificance(String significance) {
        setSignificance(significance, true);
    }

    @Override
    public void setSubtype(String subtype) {
        setSubtype(subtype, true);
    }

    @Override
    public void setIssueTime(Date date) {
        setIssueTime(date, true);
    }

    @Override
    public void setEndTime(Date date) {
        setEndTime(date, true);
    }

    @Override
    public void setStartTime(Date date) {
        setStartTime(date, true);
    }

    @Override
    public void setGeometry(Geometry geom) {
        setGeometry(geom, true);
    }

    @Override
    public void setHazardMode(ProductClass mode) {
        setHazardMode(mode, true);
    }

    @Override
    public void setHazardAttributes(Map<String, Serializable> attributes) {
        setHazardAttributes(attributes, true);
    }

    @Override
    public void addHazardAttribute(String key, Serializable value) {
        addHazardAttribute(key, value, true);
    }

    @Override
    public void removeHazardAttribute(String key) {
        removeHazardAttribute(key, true);
    }

    protected void setSiteID(String site, boolean notify) {
        if (changed(getSiteID(), site)) {
            delegate.setSiteID(site);
            if (notify) {
                eventManager
                        .hazardEventModified(new SessionEventModified(
                                eventManager, this));
            }
        }
    }

    protected void setEventID(String eventId, boolean notify) {
        if (changed(getEventID(), eventId)) {
            delegate.setEventID(eventId);
            if (notify) {
                eventManager
                        .hazardEventModified(new SessionEventModified(
                                eventManager, this));
            }
        }
    }

    protected void setState(HazardState state, boolean notify, boolean persist) {
        if (changed(getState(), state)) {
            delegate.setState(state);
            if (notify) {
                eventManager.hazardEventStateModified(
                        new SessionEventStateModified(eventManager, this),
                        persist);
            }
        }
    }

    protected void setPhenomenon(String phenomenon, boolean notify) {
        if (changed(getPhenomenon(), phenomenon)) {
            if (eventManager.canChangeType(this)) {
                delegate.setPhenomenon(phenomenon);
                if (notify) {
                    eventManager
                            .hazardEventModified(new SessionEventModified(
                                    eventManager, this));
                }
            } else {
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    protected void setSignificance(String significance, boolean notify) {
        if (changed(getSignificance(), significance)) {
            if (eventManager.canChangeType(this)) {
                delegate.setSignificance(significance);
                if (notify) {
                    eventManager
                            .hazardEventModified(new SessionEventModified(
                                    eventManager, this));
                }
            } else {
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    protected void setSubtype(String subtype, boolean notify) {
        if (changed(getSubtype(), subtype)) {
            if (eventManager.canChangeType(this)) {
                delegate.setSubtype(subtype);
                if (notify) {
                    eventManager
                            .hazardEventModified(new SessionEventModified(
                                    eventManager, this));
                }
            } else {
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    protected void setIssueTime(Date date, boolean notify) {
        if (changed(getIssueTime(), date)) {
            delegate.setIssueTime(date);
            if (notify) {
                eventManager
                        .hazardEventModified(new SessionEventModified(
                                eventManager, this));
            }
        }
    }

    protected void setEndTime(Date date, boolean notify) {
        if (changed(getEndTime(), date)) {
            if (eventManager.canChangeTimeRange(this)) {
                delegate.setEndTime(date);
                if (notify) {
                    eventManager
                            .hazardEventModified(new SessionEventModified(
                                    eventManager, this));
                }
            } else {
                throw new IllegalEventModificationException("endTime");
            }
        }
    }

    protected void setStartTime(Date date, boolean notify) {
        if (changed(getStartTime(), date)) {
            if (eventManager.canChangeTimeRange(this)) {
                delegate.setStartTime(date);
                if (notify) {
                    eventManager
                            .hazardEventModified(new SessionEventModified(
                                    eventManager, this));
                }
            } else {
                throw new IllegalEventModificationException("startTime");
            }
        }
    }

    protected void setGeometry(Geometry geom, boolean notify) {
        if (changed(getGeometry(), geom)) {
            if (eventManager.canChangeGeometry(this)) {
                delegate.setGeometry(geom);
                if (notify) {
                    eventManager
                            .hazardEventModified(new SessionEventGeometryModified(
                                    eventManager, this));
                }
            } else {
                throw new IllegalEventModificationException("geometry");
            }
        }
    }

    protected void setHazardMode(ProductClass mode, boolean notify) {
        if (changed(getHazardMode(), mode)) {
            delegate.setHazardMode(mode);
            if (notify) {
                eventManager
                        .hazardEventModified(new SessionEventModified(
                                eventManager, this));
            }
        }
    }

    public void setHazardAttributes(Map<String, Serializable> attributes,
            boolean notify) {
        if (changed(getHazardAttributes(), attributes)) {
            delegate.setHazardAttributes(attributes);
            if (notify) {
                eventManager
                        .hazardEventModified(new SessionEventModified(
                                eventManager, this));
            }
        }
    }

    public void addHazardAttribute(String key, Serializable value,
            boolean notify) {
        if (changed(value, getHazardAttribute(key))) {
            delegate.removeHazardAttribute(key);
            delegate.addHazardAttribute(key, value);
            if (notify) {
                eventManager
                        .hazardEventAttributeModified(new SessionEventAttributeModified(
                                eventManager, this, key));
            }
        }
    }

    public void removeHazardAttribute(String key, boolean notify) {
        if (getHazardAttribute(key) != null) {
            delegate.removeHazardAttribute(key);
            if (notify) {
                eventManager
                        .hazardEventAttributeModified(new SessionEventAttributeModified(
                                eventManager, this, key));
            }
        }
    }

}
