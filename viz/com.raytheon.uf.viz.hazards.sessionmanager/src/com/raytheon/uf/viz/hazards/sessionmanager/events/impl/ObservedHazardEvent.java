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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ATTR_ISSUED;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IllegalEventModificationException;
import com.raytheon.uf.viz.hazards.sessionmanager.modifiable.IModifiable;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

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
 * Apr 09, 2014 2925       Chris.Golden Added toString() method, and augmented
 *                                      with additional methods to set the
 *                                      type components atomically, or the
 *                                      start and end time atomically, as well
 *                                      as use of new, more fine-grained
 *                                      notifications in response to event
 *                                      modification.
 * Jun 30, 2014 3512       Chris.Golden Added addHazardAttributes() methods.
 *                                      Also changed a few methods that were
 *                                      public that should have been protected
 *                                      like the other notify-taking methods.
 * Dec  1, 2014 4188       Dan Schaffer Now allowing hazards to be shrunk or expanded when appropriate.
 * Jan 22, 2015 4959       Dan Schaffer MB3 to add/remove UGCs to a hazard
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect warned area designation.
 * Feb 05, 2015 2331       Chris.Golden Removed check to see if start or end
 *                                      time can be modified; this is handled
 *                                      by the session event manager now, as
 *                                      the rules are significantly more
 *                                      complicated, depending upon hazard
 *                                      type, status, start and end time at
 *                                      last issuance, etc.
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Feb 22, 2015   6561     mpduff       Override get and get/setInsertTime
 * Mar 13, 2015 6090       Dan Schaffer Fixed goosenecks
 * Apr 10, 2015 6898       Chris.Cody   Removed all messaging from data object.
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

    private final GeometryFactory geometryFactory = new GeometryFactory();

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
    public HazardStatus getStatus() {
        return delegate.getStatus();
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
    public Date getCreationTime() {
        return delegate.getCreationTime();
    }

    @Override
    public ProductClass getHazardMode() {
        return delegate.getHazardMode();
    }

    @Override
    public Serializable getHazardAttribute(String key) {
        return delegate.getHazardAttribute(key);
    }

    protected ObservedHazardEvent() {
        delegate = null;
    }

    public ObservedHazardEvent(IHazardEvent event) {
        delegate = new BaseHazardEvent(event);
    }

    protected boolean changed(Object newObj, Object oldObj) {
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

    protected final boolean changed(Geometry newObj, Geometry oldObj) {
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
        if (changed(getSiteID(), site)) {
            delegate.setSiteID(site);
        }
    }

    @Override
    public void setEventID(String eventId) {
        if (changed(getEventID(), eventId)) {
            delegate.setEventID(eventId);
        }
    }

    @Override
    public void setStatus(HazardStatus status) {
        if (changed(getStatus(), status)) {
            delegate.setStatus(status);
        }
    }

    @Override
    public void setPhenomenon(String phenomenon) {
        if (changed(getPhenomenon(), phenomenon)) {
            if (canChangeType()) {
                delegate.setPhenomenon(phenomenon);
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("phenomenon");
            }
        }
    }

    @Override
    public void setSignificance(String significance) {
        if (changed(getSignificance(), significance)) {
            if (canChangeType()) {
                delegate.setSignificance(significance);
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("significance");
            }
        }
    }

    public void setSubType(String subtype) {
        if (changed(getSubType(), subtype)) {
            if (canChangeType()) {
                delegate.setSubType(subtype);
            } else {
                this.modified = false;
                throw new IllegalEventModificationException("subtype");
            }
        }
    }

    public void setHazardType(String phenomenon, String significance,
            String subtype) {
        /*
         * TODO Handle case when user sets hazard type back to empty. Should the
         * HID even allow that?
         */
        setPhenomenon(phenomenon);
        setSignificance(significance);
        setSubType(subtype);
    }

    public void setCreationTime(Date date) {
        if (getCreationTime() == null) {
            delegate.setCreationTime(date);
        }
    }

    public void setEndTime(Date date) {
        if (changed(getEndTime(), date)) {
            delegate.setEndTime(date);
        }
    }

    public void setStartTime(Date date) {
        if (changed(getStartTime(), date)) {
            delegate.setStartTime(date);
        }
    }

    public void setTimeRange(Date startTime, Date endTime) {
        if ((changed(getStartTime(), startTime) == true)
                || (changed(getEndTime(), endTime) == true)) {
            setStartTime(startTime);
            setEndTime(endTime);
        }
    }

    public void setGeometry(Geometry geom) {
        /*
         * Make sure that geometries are GeometryCollections throughout
         */
        if (!(geom instanceof GeometryCollection)) {
            geom = geometryFactory
                    .createGeometryCollection(new Geometry[] { geom });
        }
        if (changed(getGeometry(), geom)) {
            pushToStack("setGeometry", Geometry.class, getGeometry());
            delegate.setGeometry(geom);
        }
    }

    public void setHazardMode(ProductClass mode) {
        if (changed(getHazardMode(), mode)) {
            delegate.setHazardMode(mode);
        }
    }

    public void setHazardAttributes(Map<String, Serializable> attributes) {
        Set<String> changedKeys = getChangedAttributes(attributes, true);
        if (changedKeys.isEmpty() == false) {
            delegate.setHazardAttributes(attributes);
        }
    }

    /**
     * Get the set of attribute names that will be changed by modifying the
     * current hazard attributes using the specified attributes. If
     * <code>willReplace</code> is true, the modification is assumed to be that
     * the specified attributes map will replace the old map; otherwise, it is
     * assumed that the specified attributes will be added to the old map,
     * meaning that any attributes present in the old map but not the new will
     * retain their old values.
     * 
     * @param attributes
     *            Map of attributes with which to modify the old attributes.
     * @param willReplace
     *            Flag indicating whether or not the specified attributes will
     *            replace the old attributes; if false, they will simply be
     *            added on top of the old attributes, overwriting only those old
     *            ones that are found in the new attributes map as well.
     * @return Set of attribute names that will change as a result of the
     *         specified modification to the old attributes map.
     */
    protected Set<String> getChangedAttributes(
            Map<String, Serializable> attributes, boolean willReplace) {

        /*
         * Determine which attributes are present in the new map but not the old
         * (and, if this is being checked for a replacement map of attributes,
         * the old map but not the new).
         */
        Map<String, Serializable> oldAttributes = getHazardAttributes();
        Set<String> oldAttributeKeys = oldAttributes.keySet();
        Set<String> attributeKeys = attributes.keySet();
        Set<String> changedKeys = new HashSet<>(
                willReplace ? Sets.symmetricDifference(oldAttributeKeys,
                        attributeKeys) : Sets.difference(attributeKeys,
                        oldAttributeKeys));

        /*
         * For each attribute present in both maps, determine whether the values
         * in the two maps are the same, and if not, add that attribute to the
         * set of changed attributes.
         */
        for (String key : Sets.intersection(oldAttributeKeys, attributeKeys)) {
            Serializable oldValue = oldAttributes.get(key);
            Serializable newValue = attributes.get(key);
            if ((oldValue != newValue)
                    && ((oldValue == null) || (newValue == null) || (oldValue
                            .equals(newValue) == false))) {
                changedKeys.add(key);
            }
        }

        return changedKeys;
    }

    public void addHazardAttributes(Map<String, Serializable> attributes) {
        Set<String> changedKeys = getChangedAttributes(attributes, false);
        if (changedKeys.size() > 0) {
            Map<String, Serializable> modifiedAttributes = new HashMap<>();
            for (String changedKey : changedKeys) {
                modifiedAttributes.put(changedKey, attributes.get(changedKey));
            }
            delegate.addHazardAttributes(modifiedAttributes);
        }
    }

    public void addHazardAttribute(String key, Serializable value) {
        if (changed(value, getHazardAttribute(key))) {
            delegate.removeHazardAttribute(key);
            delegate.addHazardAttribute(key, value);
        }
    }

    public void removeHazardAttribute(String key) {
        if (getHazardAttribute(key) != null) {
            delegate.removeHazardAttribute(key);
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

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void setInsertTime(Date date) {
        // No-op

    }

    @Override
    public Date getInsertTime() {
        // No-op
        return null;
    }

    public boolean canChangeType() {
        if (hasEverBeenIssued()) {
            return false;
        }
        return true;
    }

    public boolean hasEverBeenIssued() {
        return Boolean.TRUE.equals(getHazardAttribute(ATTR_ISSUED));
    }

    /**
     * Tests if an event's area can be changed.
     * 
     * @return True - the event's area can be changed. False - the event's area
     *         cannot be changed.
     */
    public boolean canEventAreaBeChanged() {
        return (getStatus() != HazardStatus.ENDED);
    }

}
