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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;

import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IllegalEventModificationException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.modifiable.IModifiable;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
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
 * Aug 06, 2013 1265       blawrenc    Updated to support undo/redo
 * Aug 22, 2013 1921       blawrenc    Added a deep array equality test to the
 *                                     changed (Object, Object) method.
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
 * Nov 29, 2013 2378       blawrenc    Added a mechanism for 
 *                                     keeping track of when an hazard
 *                                     event is modified. This is in place
 *                                     of resetting the state to PENDING when
 *                                     a modification occurs.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class ObservedHazardEvent implements IHazardEvent, IUndoRedoable,
        IModifiable {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ObservedHazardEvent.class);

    private final SessionEventManager eventManager;

    private final IHazardEvent delegate;

    /**
     * Undo stack. Supports undo operations on this hazard event.
     */
    private final Stack<Pair<Method, Object>> undoStack = new Stack<Pair<Method, Object>>();

    /**
     * Redo stack. Support redo operations on this hazard event.
     */
    private final Stack<Pair<Method, Object>> redoStack = new Stack<Pair<Method, Object>>();

    /**
     * Flag indicating whether or not an undo operation is in progress.
     */
    private volatile Boolean undoInProgress = false;

    /**
     * Flag indicating whether or not a redo operation is in progress.
     */
    private volatile Boolean redoInProgress = false;

    /*
     * Flag for indicating whether or not an event has been modified. In this
     * case, this indicates whether or not the event has been modified since it
     * was last persisted.
     */
    private volatile Boolean modified = false;

    @Override
    public Date getStartTime() {
        return delegate.getStartTime();
    }

    @Override
    public Date getEndTime() {
        return delegate.getEndTime();
    }

    @Override
    public Geometry getGeometry() {
        return delegate.getGeometry();
    }

    @Override
    public String getSiteID() {
        return delegate.getSiteID();
    }

    @Override
    public String getEventID() {
        return delegate.getEventID();
    }

    @Override
    public HazardState getState() {
        return delegate.getState();
    }

    @Override
    public String getPhenomenon() {
        return delegate.getPhenomenon();
    }

    @Override
    public String getSignificance() {
        return delegate.getSignificance();
    }

    @Override
    public String getSubType() {
        return delegate.getSubType();
    }

    @Override
    public String getHazardType() {
        return HazardEventUtilities.getHazardType(this);
    }

    @Override
    public Date getIssueTime() {
        return delegate.getIssueTime();
    }

    @Override
    public ProductClass getHazardMode() {
        return delegate.getHazardMode();
    }

    @Override
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
        } else if ((newObj instanceof Object[]) && (oldObj instanceof Object[])) {

            return !Arrays.deepEquals((Object[]) newObj, (Object[]) oldObj);

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
        }
        return true;
    }

    @Override
    public Map<String, Serializable> getHazardAttributes() {
        Map<String, Serializable> attr = delegate.getHazardAttributes();
        // Do not allow modification because listeners would need to be fired.
        attr = ImmutableMap.copyOf(delegate.getHazardAttributes());
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
        if (changed(getState(), state)) {
            setState(state, true, true);
        }
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
    public void setSubType(String subtype) {
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
                eventManager.hazardEventModified(new SessionEventModified(
                        eventManager, this));
            }
        }
    }

    protected void setEventID(String eventId, boolean notify) {
        if (changed(getEventID(), eventId)) {
            delegate.setEventID(eventId);
            if (notify) {
                eventManager.hazardEventModified(new SessionEventModified(
                        eventManager, this));
            }
        }
    }

    protected void setState(HazardState state, boolean notify, boolean persist) {

        delegate.setState(state);

        if (notify) {
            eventManager.hazardEventStateModified(
                    new SessionEventStateModified(eventManager, this), persist);
        }
    }

    protected void setPhenomenon(String phenomenon, boolean notify) {
        if (changed(getPhenomenon(), phenomenon)) {
            if (eventManager.canChangeType(this)) {
                delegate.setPhenomenon(phenomenon);
                if (notify) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, this));
                }
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    protected void setSignificance(String significance, boolean notify) {
        if (changed(getSignificance(), significance)) {
            if (eventManager.canChangeType(this)) {
                delegate.setSignificance(significance);
                if (notify) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, this));
                }
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    protected void setSubtype(String subtype, boolean notify) {
        if (changed(getSubType(), subtype)) {
            if (eventManager.canChangeType(this)) {
                delegate.setSubType(subtype);
                if (notify) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, this));
                }
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    protected void setIssueTime(Date date, boolean notify) {
        if (changed(getIssueTime(), date)) {
            delegate.setIssueTime(date);
            if (notify) {
                eventManager.hazardEventModified(new SessionEventModified(
                        eventManager, this));
            }
        }
    }

    protected void setEndTime(Date date, boolean notify) {
        if (changed(getEndTime(), date)) {
            if (eventManager.canChangeTimeRange(this)) {
                delegate.setEndTime(date);
                if (notify) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, this));
                }
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("endTime");
            }
        }
    }

    protected void setStartTime(Date date, boolean notify) {
        if (changed(getStartTime(), date)) {
            if (eventManager.canChangeTimeRange(this)) {
                delegate.setStartTime(date);
                if (notify) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, this));
                }
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("startTime");
            }
        }
    }

    protected void setGeometry(Geometry geom, boolean notify) {
        if (changed(getGeometry(), geom)) {
            if (eventManager.canChangeGeometry(this)) {
                pushToStack("setGeometry", Geometry.class, getGeometry());
                delegate.setGeometry(geom);
                if (notify) {
                    eventManager
                            .hazardEventModified(new SessionEventGeometryModified(
                                    eventManager, this));
                }
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("geometry");
            }
        }
    }

    protected void setHazardMode(ProductClass mode, boolean notify) {
        if (changed(getHazardMode(), mode)) {
            delegate.setHazardMode(mode);
            if (notify) {
                eventManager.hazardEventModified(new SessionEventModified(
                        eventManager, this));
            }
        }
    }

    public void setHazardAttributes(Map<String, Serializable> attributes,
            boolean notify) {
        if (changed(getHazardAttributes(), attributes)) {
            delegate.setHazardAttributes(attributes);
            if (notify) {
                eventManager.hazardEventModified(new SessionEventModified(
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

    @Override
    public void undo() {
        if (isUndoable()) {

            Pair<Method, Object> pair = undoStack.pop();
            Method method = pair.getFirst();
            Object value = pair.getSecond();

            try {
                undoInProgress = true;
                method.invoke(this, value);
            } catch (Exception e) {
                statusHandler.error("Error invoking undo method for event "
                        + getEventID(), e);
            }

            undoInProgress = false;
        }
    }

    @Override
    public void redo() {
        if (isRedoable()) {
            Pair<Method, Object> pair = redoStack.pop();
            Method method = pair.getFirst();
            Object value = pair.getSecond();

            try {
                redoInProgress = true;
                method.invoke(this, value);
            } catch (Exception e) {
                statusHandler.error("Error invoking redo method for event "
                        + getEventID(), e);
            }

            redoInProgress = false;
        }
    }

    @Override
    public Boolean isUndoable() {
        return !undoStack.isEmpty();
    }

    @Override
    public Boolean isRedoable() {
        return !redoStack.isEmpty();
    }

    /**
     * Method for handling pushes to the undo and redo stack. Each element on
     * these stacks contain the 'setter' method to call as well as the value to
     * pass to it. When items are popped off of the undo or redo stack, the
     * method is invoked with the stored value. This allows other portions of
     * the ObservedHazardEvent state to be eventually undone/redone.
     * 
     * @param methodName
     *            The name of the calling method which wants to push an item to
     *            either the undo or redo stack.
     * @param className
     *            The name of the class which contains the method.
     * @param value
     *            The value to be pushed to either the undo or redo stack.
     * @return
     */
    private void pushToStack(final String methodName, final Class<?> className,
            Object value) {

        try {
            if ((!undoInProgress && !redoInProgress) || redoInProgress) {
                Method method = this.getClass()
                        .getMethod(methodName, className);
                Pair<Method, Object> methodValuePair = new Pair<Method, Object>(
                        method, value);
                undoStack.push(methodValuePair);
            } else {
                Method method = this.getClass()
                        .getMethod(methodName, className);
                Pair<Method, Object> methodValuePair = new Pair<Method, Object>(
                        method, value);
                redoStack.push(methodValuePair);

            }
        } catch (Exception e) {
            statusHandler.error("Error updating undo/redo stack for event "
                    + getEventID(), e);
        }

    }

    @Override
    public void clearUndoRedo() {
        undoStack.clear();
        redoStack.clear();
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

}
