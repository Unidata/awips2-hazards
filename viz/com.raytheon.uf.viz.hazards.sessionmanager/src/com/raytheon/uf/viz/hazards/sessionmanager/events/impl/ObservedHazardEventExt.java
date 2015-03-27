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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IllegalEventModificationException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A hazard event which notifies the SessionEventManager whenever fields are
 * changed. Changes for #6898 removed all of the messaging components from what
 * SHOULD be a DATA ONLY object. HOWEVER, Python RELIES on the messaging
 * capabilities of ObservedHazardEvent to communicate changes from script
 * operations BACK into the java code. To mitigate this, an
 * ObservedHazardEventExt object has been created to WRAP the
 * ObservedHazardEvent object and restore communications with CAVE: Hazard
 * Services without re-implementing sending messages for every operation within
 * CAVE. Note that changes that originate from megawidgets will perform
 * symmetric changes to the object in the Python script. This will cause an
 * "echo" message to be sent when widget change "side effects" are processed.
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 10, 2015 6898       Chris.Cody  Create data object with messaging for python notification.
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */

public class ObservedHazardEventExt extends ObservedHazardEvent {

    private final ObservedHazardEvent observedHazardEvent;

    private final SessionEventManager eventManager;

    public ObservedHazardEventExt(IHazardEvent event,
            SessionEventManager eventManager) {
        if (event instanceof ObservedHazardEvent) {
            this.observedHazardEvent = (ObservedHazardEvent) event;
        } else {
            this.observedHazardEvent = new ObservedHazardEvent(event);
        }
        this.eventManager = eventManager;
    }

    public ObservedHazardEvent getInternalObservedHazardEvent() {
        return (this.observedHazardEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getStartTime()
     */
    @Override
    public Date getStartTime() {
        return this.observedHazardEvent.getStartTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getEndTime()
     */
    @Override
    public Date getEndTime() {
        return this.observedHazardEvent.getEndTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getGeometry()
     */
    @Override
    public Geometry getGeometry() {
        return this.observedHazardEvent.getGeometry();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getSiteID()
     */
    @Override
    public String getSiteID() {
        return this.observedHazardEvent.getSiteID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getEventID()
     */
    @Override
    public String getEventID() {
        return this.observedHazardEvent.getEventID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getStatus()
     */
    @Override
    public HazardStatus getStatus() {
        return this.observedHazardEvent.getStatus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getPhenomenon()
     */
    @Override
    public String getPhenomenon() {
        return this.observedHazardEvent.getPhenomenon();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getSignificance()
     */
    @Override
    public String getSignificance() {
        return this.observedHazardEvent.getSignificance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getSubType()
     */
    @Override
    public String getSubType() {
        return this.observedHazardEvent.getSubType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getHazardType()
     */
    @Override
    public String getHazardType() {
        return HazardEventUtilities.getHazardType(this.observedHazardEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getCreationTime()
     */
    @Override
    public Date getCreationTime() {
        return this.observedHazardEvent.getCreationTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getHazardMode()
     */
    @Override
    public ProductClass getHazardMode() {
        return this.observedHazardEvent.getHazardMode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getHazardAttribute
     * (String)
     */
    @Override
    public Serializable getHazardAttribute(String key) {
        return this.observedHazardEvent.getHazardAttribute(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#run()
     */
    @Override
    public Map<String, Serializable> getHazardAttributes() {
        return (this.observedHazardEvent.getHazardAttributes());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#run()
     */
    @Override
    public void setSiteID(String site) {
        setSiteID(site, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#run()
     */
    @Override
    public void setEventID(String eventId) {
        setEventID(eventId, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setStatus(HazardStatus
     * )
     */
    @Override
    public void setStatus(HazardStatus status) {
        setStatus(status, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setPhenomenon(
     * String)
     */
    @Override
    public void setPhenomenon(String phenomenon) {
        setPhenomenon(phenomenon, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setSignificance
     * (String)
     */
    @Override
    public void setSignificance(String significance) {
        setSignificance(significance, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setSubType(String)
     */
    @Override
    public void setSubType(String subtype) {
        setSubType(subtype, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#rsetHazardType
     * (String, String, String)
     */
    @Override
    public void setHazardType(String phenomenon, String significance,
            String subtype) {
        setHazardType(phenomenon, significance, subtype, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setCreationTime
     * (Date)
     */
    @Override
    public void setCreationTime(Date date) {
        setCreationTime(date, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setEndTime(Date)
     */
    @Override
    public void setEndTime(Date date) {
        setEndTime(date, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setStartTime(Date)
     */
    @Override
    public void setStartTime(Date date) {
        setStartTime(date, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setTimeRange(Date)
     */
    @Override
    public void setTimeRange(Date startTime, Date endTime) {
        setTimeRange(startTime, endTime, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setGeometry(Geometry
     * )
     */
    @Override
    public void setGeometry(Geometry geom) {
        setGeometry(geom, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setHazardMode(
     * ProductClass)
     */
    @Override
    public void setHazardMode(ProductClass mode) {
        setHazardMode(mode, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setHazardAttributes
     * (Map<String, Serializable>)
     */
    @Override
    public void setHazardAttributes(Map<String, Serializable> attributes) {
        setHazardAttributes(attributes, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#addHazardAttributes
     * (Map<String, Serializable>)
     */
    @Override
    public void addHazardAttributes(Map<String, Serializable> attributes) {
        addHazardAttributes(attributes, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#addHazardAttribute
     * (String, Serializable)
     */
    @Override
    public void addHazardAttribute(String key, Serializable value) {
        addHazardAttribute(key, value, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#removeHazardAttribute
     * (String)
     */
    @Override
    public void removeHazardAttribute(String key) {
        removeHazardAttribute(key, true, Originator.OTHER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#undo()
     */
    @Override
    public void undo() {
        this.observedHazardEvent.undo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#redo()
     */
    @Override
    public void redo() {
        this.observedHazardEvent.redo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#isUndoable()
     */
    @Override
    public Boolean isUndoable() {
        return (this.observedHazardEvent.isUndoable());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#isRedoable()
     */
    @Override
    public Boolean isRedoable() {
        return (this.observedHazardEvent.isRedoable());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#clearUndoRedo()
     */
    @Override
    public void clearUndoRedo() {
        this.observedHazardEvent.clearUndoRedo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#isModified()
     */
    @Override
    public boolean isModified() {
        return (this.observedHazardEvent.isModified());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setModified(boolean
     * )
     */
    @Override
    public void setModified(boolean modified) {
        this.observedHazardEvent.setModified(modified);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.events.impl#toString()
     */
    @Override
    public String toString() {
        return (this.observedHazardEvent.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#setInsertTime(
     * Date)
     */
    @Override
    public void setInsertTime(Date date) {
        // No-op
        this.observedHazardEvent.setInsertTime(date);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#getInsertTime()
     */
    @Override
    public Date getInsertTime() {
        // No-op
        return (this.observedHazardEvent.getInsertTime());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#canChangeType()
     */
    @Override
    public boolean canChangeType() {
        return (this.observedHazardEvent.canChangeType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#hasEverBeenIssued
     * ()
     */
    @Override
    public boolean hasEverBeenIssued() {
        return (this.observedHazardEvent.hasEverBeenIssued());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.impl#canEventAreaBeChanged
     * ()
     */
    @Override
    public boolean canEventAreaBeChanged() {
        return (this.observedHazardEvent.canEventAreaBeChanged());
    }

    // Methods containing notification: Call a method in SessionEventManager and
    // passing it a pre-filled message object.
    public void setSiteID(String site, boolean notify, IOriginator originator) {
        if (changed(getSiteID(), site)) {
            this.observedHazardEvent.setSiteID(site);
            notifyHazardEventModified(notify, originator);
        }
    }

    public void setEventID(String eventId, boolean notify,
            IOriginator originator) {
        if (changed(getEventID(), eventId)) {
            this.observedHazardEvent.setEventID(eventId);
            notifyHazardEventModified(notify, originator);
        }
    }

    public void setStatus(HazardStatus status, boolean notify,
            IOriginator originator) {
        if (changed(getStatus(), status)) {
            this.observedHazardEvent.setStatus(status);
            notifyHazardEventStatusModified(notify, originator, true);
        }
    }

    public void setPhenomenon(String phenomenon, boolean notify,
            IOriginator originator) {
        if (changed(getPhenomenon(), phenomenon)) {
            if (canChangeType()) {
                this.observedHazardEvent.setPhenomenon(phenomenon);
                notifyHazardEventModified(notify, originator);
            } else {
                this.observedHazardEvent.setModified(false);
                throw new IllegalEventModificationException("phenomenon");
            }
        }
    }

    public void setSignificance(String significance, boolean notify,
            IOriginator originator) {
        if (changed(getSignificance(), significance)) {
            if (canChangeType()) {
                this.observedHazardEvent.setSignificance(significance);
                notifyHazardEventModified(notify, originator);
            } else {
                this.observedHazardEvent.setModified(false);
                throw new IllegalEventModificationException("significance");
            }
        }
    }

    public void setSubType(String subtype, boolean notify,
            IOriginator originator) {
        if (changed(getSubType(), subtype)) {
            if (canChangeType()) {
                this.observedHazardEvent.setSubType(subtype);
                notifyHazardEventTypeModified(notify, originator);
            } else {
                this.observedHazardEvent.setModified(false);
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    public void setHazardType(String phenomenon, String significance,
            String subtype, boolean notify, IOriginator originator) {
        boolean changed = (changed(getPhenomenon(), phenomenon)
                || changed(getSignificance(), significance) || changed(
                getSubType(), subtype));

        if (changed == true) {
            setPhenomenon(phenomenon, false, originator);
            setSignificance(significance, false, originator);
            setSubType(subtype, false, originator);

            notifyUpdateHazardAreas(notify);
            notifyHazardEventTypeModified(notify, originator);
        }
    }

    public void setCreationTime(Date date, boolean notify,
            IOriginator originator) {
        if (getCreationTime() == null) {
            this.observedHazardEvent.setCreationTime(date);
            notifyHazardEventModified(notify, originator);
        }
    }

    public void setEndTime(Date date, boolean notify, IOriginator originator) {
        if (changed(getEndTime(), date)) {
            this.observedHazardEvent.setEndTime(date);
            notifyHazardEventModified(notify, originator);
        }
    }

    public void setStartTime(Date date, boolean notify, IOriginator originator) {
        if (changed(getStartTime(), date)) {
            this.observedHazardEvent.setStartTime(date);
            notifyHazardEventModified(notify, originator);
        }
    }

    public void setTimeRange(Date startTime, Date endTime, boolean notify,
            IOriginator originator) {
        if ((changed(getStartTime(), startTime) == true)
                || (changed(getEndTime(), endTime) == true)) {
            setStartTime(startTime);
            setEndTime(endTime);
        }
    }

    public void setGeometry(Geometry geom, boolean notify,
            IOriginator originator) {
        this.observedHazardEvent.setGeometry(geom);
        notifyHazardEventGeometryModified(notify, originator);
    }

    public void setHazardMode(ProductClass mode, boolean notify,
            IOriginator originator) {
        if (changed(getHazardMode(), mode)) {
            this.observedHazardEvent.setHazardMode(mode);
            notifyHazardEventModified(notify, originator);
        }
    }

    public void setHazardAttributes(Map<String, Serializable> attributes,
            boolean notify, IOriginator originator) {
        Set<String> changedKeys = this.observedHazardEvent
                .getChangedAttributes(attributes, true);
        if (changedKeys.isEmpty() == false) {
            this.observedHazardEvent.setHazardAttributes(attributes);
            notifyHazardEventAttributesModified(changedKeys, attributes,
                    notify, originator);
        }
    }

    public void addHazardAttributes(Map<String, Serializable> attributes,
            boolean notify, IOriginator originator) {
        Set<String> changedKeys = getChangedAttributes(attributes, false);
        if (changedKeys.size() > 0) {
            Map<String, Serializable> modifiedAttributes = new HashMap<>();
            for (String changedKey : changedKeys) {
                modifiedAttributes.put(changedKey, attributes.get(changedKey));
            }
            this.observedHazardEvent.addHazardAttributes(attributes);
            eventManager
                    .hazardEventModified(new SessionEventAttributesModified(
                            this.observedHazardEvent, modifiedAttributes,
                            originator));
        }
    }

    public void addHazardAttribute(String key, Serializable value,
            boolean notify, IOriginator originator) {
        if (changed(value, getHazardAttribute(key))) {
            this.observedHazardEvent.removeHazardAttribute(key);
            if ((notify == true) && (eventManager != null)) {
                eventManager
                        .hazardEventAttributesModified(new SessionEventAttributesModified(
                                this.observedHazardEvent, key, value,
                                originator));
            }
        }
    }

    public void removeHazardAttribute(String key, boolean notify,
            IOriginator originator) {
        if (getHazardAttribute(key) != null) {
            this.observedHazardEvent.removeHazardAttribute(key);
            if ((notify == true) && (eventManager != null)) {
                eventManager
                        .hazardEventAttributesModified(new SessionEventAttributesModified(
                                this.observedHazardEvent, key,
                                getHazardAttribute(key), originator));
            }
        }
    }

    private void notifyHazardEventStatusModified(boolean notify,
            IOriginator originator, boolean persist) {
        if ((notify) && (eventManager != null)) {
            eventManager
                    .hazardEventStatusModified(new SessionEventStatusModified(
                            this.observedHazardEvent, originator));
        }
    }

    private void notifyHazardEventModified(boolean notify,
            IOriginator originator) {
        if ((notify) && (eventManager != null)) {
            eventManager.hazardEventModified(new SessionEventModified(
                    this.observedHazardEvent, originator));
        }
    }

    private void notifyHazardEventTypeModified(boolean notify,
            IOriginator originator) {
        if ((notify) && (eventManager != null)) {
            eventManager.hazardEventModified(new SessionEventTypeModified(
                    this.observedHazardEvent, originator));
        }
    }

    private void notifyHazardEventAttributesModified(Set<String> changedKeys,
            Map<String, Serializable> attributes, boolean notify,
            IOriginator originator) {
        if ((notify) && (eventManager != null)) {
            Map<String, Serializable> modifiedAttributes = new HashMap<>();
            for (String changedKey : changedKeys) {
                modifiedAttributes.put(changedKey, attributes.get(changedKey));
            }
            eventManager
                    .hazardEventModified(new SessionEventAttributesModified(
                            this, modifiedAttributes, originator));
        }
    }

    private void notifyUpdateHazardAreas(boolean notify) {
        if ((notify) && (eventManager != null)) {
            eventManager.updateHazardAreas(this.observedHazardEvent);
        }
    }

    private void notifyHazardEventGeometryModified(boolean notify,
            IOriginator originator) {
        if ((notify) && (eventManager != null)) {
            eventManager.hazardEventModified(new SessionEventGeometryModified(
                    this.observedHazardEvent, originator));
        }
    }

}
