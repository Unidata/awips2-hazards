/**
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAttributesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventCreationTimeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventGeometryModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventIdentifierModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventIssuanceCountModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventOriginModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventStatusModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTimeRangeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTypeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventVisualFeaturesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IllegalEventModificationException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;

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
 * Dec  1, 2014 4188       Dan Schaffer Now allowing hazards to be shrunk or
 *                                      expanded when appropriate.
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
 * Feb 22, 2015 6561       mpduff       Override get and get/setInsertTime
 * Mar 13, 2015 6090       Dan Schaffer Fixed goosenecks
 * Jul 31, 2015 7458       Robert.Blum  Added new userName and workstation methods.
 * Aug 03, 2015 8836       Chris.Cody   Changes for a configurable Event Id.
 * Nov 10, 2015 12762      Chris.Golden Fixed potential bug causing notifications to
 *                                      be sent out in a case where they are not
 *                                      desired.
 * Feb 08, 2016 9650       Chris.Golden Altered changed() method to check properly
 *                                      for geometry collection differences (for
 *                                      18-Hazard-Services Redmine issue #9650).
 * Feb 08, 2016 10207      Chris.Golden Altered general changed() method to remove
 *                                      bugs (for 18-Hazard-Services Redmine issue
 *                                      #10207).
 * Feb 10, 2016 15561      Chris.Golden Removed bug caused by trying to copy
 *                                      attribute map when at least one of the
 *                                      attribute values is null.
 * Mar 03, 2016 14004      Chris.Golden Added missing setGeometry() method.
 * Mar 06, 2016 15676      Chris.Golden Added visual features.
 * Mar 26, 2016 15676      Chris.Golden Added more methods to get and set
 *                                      individual visual features.
 * Apr 28, 2016 18267      Chris.Golden Added missing method for replacing
 *                                      all hazard attributes, due to a
 *                                      specified originator.
 * May 02, 2016 18235      Chris.Golden Added source field.
 * Jun 10, 2016 19537      Chris.Golden Combined base and selected visual feature
 *                                      lists for each hazard event into one,
 *                                      replaced by visibility constraints
 *                                      based upon selection state to individual
 *                                      visual features.
 * Jun 23, 2016 19537      Chris.Golden Changed to use new constructor for visual
 *                                      feature change notification.
 * Sep 12, 2016 15934      Chris.Golden Changed to work with advanced geometries
 *                                      now used by hazard events.
 * Nov 01, 2016  6470      Robert.Blum  Clearing redo stack when new modifications
 *                                      are  made to the geometry to make it
 *                                      function more like typical undo/redo
 *                                      functionality.
 * Nov 02, 2016 26024      Chris.Golden Adjusted attribute changing to provide
 *                                      the old attribute values in notifications
 *                                      that result. Also made the protected
 *                                      methods for setting properties return
 *                                      a boolean value indicating whether the
 *                                      set happened or not (returning false if
 *                                      no change occurred).
 * Dec 19, 2016 21504      Robert.Blum  Changed from user name and workstation to
 *                                      instances of WsId.
 * Feb 01, 2017 15556      Chris.Golden Added "visible in history list" flag, and
 *                                      record of insert time. Also fixed setting
 *                                      of status to not persist the hazard event
 *                                      if the status is reverting from "ending"
 *                                      to something other than "ended".
 * Feb 16, 2017 29138      Chris.Golden Removed notion of visibility in history
 *                                      list (since all events in history list
 *                                      are now visible). Also added public
 *                                      method to allow setting of status with
 *                                      persistence optional.
 * Mar 16, 2017 15528      Chris.Golden Fixed modification flag to be set properly
 *                                      when appropriate event attributes or
 *                                      fields change.
 * Mar 28, 2017 32487      Chris.Golden Added use of SessionEventOriginModified
 *                                      notification when the site identifier,
 *                                      workstation, or user name are modified.
 * Mar 30, 2017 15528      Chris.Golden Changed to have the modified setter and
 *                                      getter part of IHazardEvent, not
 *                                      IModifiable (which has been removed).
 * May 24, 2017 15561      Chris.Golden Added getPhensig() method.
 * Jun 05, 2017 15561      Chris.Golden Added code to notify the session event
 *                                      manager when an undo or redo caused the
 *                                      geometry to change.
 * Jun 21, 2017 18375      Chris.Golden Added new flag that prevents the modified
 *                                      flag from changing.
 * Sep 27, 2017 38072      Chris.Golden Changed to use new SessionEventModified
 *                                      notification.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly
 *                                      mutable session events for all but the
 *                                      event manager. Instances of this class
 *                                      are no longer exposed to others besides
 *                                      those in the same package.
 * Jan 26, 2018 33428      Chris.Golden Added issuance count.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ObservedHazardEvent implements IHazardEvent, IUndoRedoable {

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

    /**
     * Flag for indicating whether or not an event has been modified. In this
     * case, this indicates whether or not the event has been modified since it
     * was last persisted.
     */
    private volatile boolean modified = false;

    /**
     * Flag for indicating whether or not the {@link #modified} flag is not
     * allowed to change.
     */
    private volatile boolean modifiedNotAllowedToChange = false;

    @Override
    public Date getStartTime() {
        return delegate.getStartTime();
    }

    @Override
    public Date getEndTime() {
        return delegate.getEndTime();
    }

    @Override
    public IAdvancedGeometry getGeometry() {
        return delegate.getGeometry();
    }

    @Override
    public Geometry getFlattenedGeometry() {
        return delegate.getFlattenedGeometry();
    }

    @Override
    public Geometry getProductGeometry() {
        return delegate.getProductGeometry();
    }

    @Override
    public VisualFeature getVisualFeature(String identifier) {
        return delegate.getVisualFeature(identifier);
    }

    @Override
    public VisualFeaturesList getVisualFeatures() {
        return delegate.getVisualFeatures();
    }

    @Override
    public String getSiteID() {
        return delegate.getSiteID();
    }

    @Override
    public String getEventID() {
        return delegate.getEventID();
    }

    /**
     * Return a filtered Event Id String
     * 
     * @see com.raytheon.uf.common.dataplugin.events.hazards.event.
     *      HazardServicesEventIdUtil
     * 
     * @return the eventID using filtering from HazardServicesEventIdUtil
     */
    @Override
    public String getDisplayEventID() {
        return (HazardServicesEventIdUtil.getDisplayId(getEventID()));
    }

    @Override
    public HazardStatus getStatus() {
        return delegate.getStatus();
    }

    @Override
    public int getIssuanceCount() {
        return delegate.getIssuanceCount();
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
    public String getPhensig() {
        return HazardEventUtilities.getHazardPhenSig(this);
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
    public Source getSource() {
        return delegate.getSource();
    }

    @Override
    public WsId getWsId() {
        return delegate.getWsId();
    }

    @Override
    public Serializable getHazardAttribute(String key) {
        return delegate.getHazardAttribute(key);
    }

    protected ObservedHazardEvent() {
        eventManager = null;
        delegate = null;
    }

    public ObservedHazardEvent(IReadableHazardEvent event,
            SessionEventManager eventManager) {
        delegate = new BaseHazardEvent(event);
        Assert.isNotNull(eventManager);
        this.eventManager = eventManager;
    }

    private final boolean changed(Object newObj, Object oldObj) {
        if ((newObj == null) && (oldObj == null)) {
            return false;
        } else if (((newObj == null) && (oldObj != null))
                || (newObj != null) && (oldObj == null)) {
            return true;
        } else if ((newObj instanceof Object[])
                && (oldObj instanceof Object[])) {
            return (Arrays.deepEquals((Object[]) newObj,
                    (Object[]) oldObj) == false);
        }
        return (newObj.equals(oldObj) == false);
    }

    @Override
    public Map<String, Serializable> getHazardAttributes() {
        Map<String, Serializable> attr = delegate.getHazardAttributes();

        /*
         * Do not allow modification because listeners would need to be fired.
         * Before creating the immutable map, filter out any entries from the
         * original map that have null values, as this is not allowed for
         * immutable maps.
         */
        Map<String, Serializable> map = new HashMap<>(
                delegate.getHazardAttributes());
        map.values().removeAll(Collections.singleton(null));
        attr = ImmutableMap.copyOf(map);
        return attr;
    }

    @Override
    public void setSiteID(String site) {
        setSiteID(site, true, Originator.OTHER);
    }

    @Override
    public void setEventID(String eventId) {
        setEventID(eventId, true, Originator.OTHER);
    }

    @Override
    public void setStatus(HazardStatus status) {
        if (changed(getStatus(), status)) {
            setStatus(status, true, true, Originator.OTHER);
        }
    }

    @Override
    public void setIssuanceCount(int count) {
        setIssuanceCount(count, true, Originator.OTHER);
    }

    @Override
    public void setPhenomenon(String phenomenon) {
        setPhenomenon(phenomenon, true, Originator.OTHER);
    }

    @Override
    public void setSignificance(String significance) {
        setSignificance(significance, true, Originator.OTHER);
    }

    @Override
    public void setSubType(String subtype) {
        setSubType(subtype, true, Originator.OTHER);
    }

    @Override
    public void setHazardType(String phenomenon, String significance,
            String subtype) {
        setHazardType(phenomenon, significance, subtype, true,
                Originator.OTHER);
    }

    @Override
    public void setCreationTime(Date date) {
        setCreationTime(date, true, Originator.OTHER);
    }

    @Override
    public void setEndTime(Date date) {
        setEndTime(date, true, Originator.OTHER);
    }

    @Override
    public void setStartTime(Date date) {
        setStartTime(date, true, Originator.OTHER);
    }

    @Override
    public void setTimeRange(Date startTime, Date endTime) {
        setTimeRange(startTime, endTime, true, Originator.OTHER);
    }

    @Override
    public void setGeometry(IAdvancedGeometry geometry) {
        setGeometry(geometry, true, Originator.OTHER);
    }

    @Override
    public void setProductGeometry(Geometry geometry) {
        geometry = AdvancedGeometryUtilities
                .getJtsGeometryAsCollection(geometry);
        delegate.setProductGeometry(geometry);
    }

    @Override
    public boolean setVisualFeature(VisualFeature visualFeature) {
        return setVisualFeature(visualFeature, true, Originator.OTHER);
    }

    @Override
    public void setVisualFeatures(VisualFeaturesList visualFeatures) {
        setVisualFeatures(visualFeatures, true, Originator.OTHER);
    }

    @Override
    public void setHazardMode(ProductClass mode) {
        setHazardMode(mode, true, Originator.OTHER);
    }

    @Override
    public void setSource(Source source) {
        setSource(source, true, Originator.OTHER);
    }

    @Override
    public void setWsId(WsId wsId) {
        setWsId(wsId, true, Originator.OTHER);
    }

    @Override
    public void setHazardAttributes(Map<String, Serializable> attributes) {
        setHazardAttributes(attributes, true, Originator.OTHER);
    }

    @Override
    public void addHazardAttribute(String key, Serializable value) {
        addHazardAttribute(key, value, true, Originator.OTHER);
    }

    @Override
    public void addHazardAttributes(Map<String, Serializable> attributes) {
        addHazardAttributes(attributes, true, Originator.OTHER);
    }

    @Override
    public void removeHazardAttribute(String key) {
        removeHazardAttribute(key, true, Originator.OTHER);
    }

    public void setSiteID(String siteID, IOriginator originator) {
        setSiteID(siteID, true, originator);
    }

    public void setEventID(String eventID, IOriginator originator) {
        setEventID(eventID, true, originator);
    }

    public void setStatus(HazardStatus status, IOriginator originator) {
        setStatus(status, true, true, originator);
    }

    public void setStatus(HazardStatus status, boolean persist,
            IOriginator originator) {
        setStatus(status, true, persist, originator);
    }

    public void setIssuanceCount(int count, IOriginator originator) {
        setIssuanceCount(count, true, originator);
    }

    public void setPhenomenon(String phenomenon, IOriginator originator) {
        setPhenomenon(phenomenon, true, originator);
    }

    public void setSignificance(String significance, IOriginator originator) {
        setSignificance(significance, true, originator);
    }

    public void setSubType(String subType, IOriginator originator) {
        setSubType(subType, true, originator);
    }

    public void setHazardType(String phenomenon, String significance,
            String subtype, IOriginator originator) {
        setHazardType(phenomenon, significance, subtype, true, originator);
    }

    public void setCreationTime(Date creationTime, IOriginator originator) {
        setCreationTime(creationTime, true, originator);
    }

    public void setEndTime(Date endTime, IOriginator originator) {
        setEndTime(endTime, true, originator);
    }

    public void setStartTime(Date startTime, IOriginator originator) {
        setStartTime(startTime, true, originator);
    }

    public void setTimeRange(Date startTime, Date endTime,
            IOriginator originator) {
        setTimeRange(startTime, endTime, true, originator);
    }

    public void setGeometry(IAdvancedGeometry geometry,
            IOriginator originator) {
        setGeometry(geometry, true, originator);
    }

    public boolean setVisualFeature(VisualFeature visualFeature,
            IOriginator originator) {
        return setVisualFeature(visualFeature, true, originator);
    }

    public void setVisualFeatures(VisualFeaturesList visualFeatures,
            IOriginator originator) {
        setVisualFeatures(visualFeatures, true, originator);
    }

    public void setHazardMode(ProductClass productClass,
            IOriginator originator) {
        setHazardMode(productClass, true, originator);
    }

    public void setSource(Source source, IOriginator originator) {
        setSource(source, true, originator);
    }

    public void setWsId(WsId wsId, IOriginator originator) {
        setWsId(wsId, true, originator);
    }

    public void setHazardAttributes(Map<String, Serializable> attributes,
            IOriginator originator) {
        setHazardAttributes(attributes, true, originator);
    }

    public void addHazardAttribute(String key, Serializable value,
            IOriginator originator) {
        addHazardAttribute(key, value, true, originator);
    }

    public void addHazardAttributes(Map<String, Serializable> attributes,
            IOriginator originator) {
        addHazardAttributes(attributes, true, originator);
    }

    public void removeHazardAttribute(String key, IOriginator originator) {
        removeHazardAttribute(key, true, originator);
    }

    private void handleModification() {
        setModified(true);
    }

    protected boolean setSiteID(String site, boolean notify,
            IOriginator originator) {
        if (changed(getSiteID(), site)) {
            delegate.setSiteID(site);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventOriginModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    protected boolean setEventID(String eventId, boolean notify,
            IOriginator originator) {
        if (changed(getEventID(), eventId)) {
            delegate.setEventID(eventId);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, eventView,
                            new EventIdentifierModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    protected boolean setStatus(HazardStatus status, boolean notify,
            boolean persist, IOriginator originator) {
        if (changed(getStatus(), status)) {

            /*
             * No need to persist the change if changing from "ending" to
             * something other than "ended", as this is a revert.
             */
            if ((getStatus() == HazardStatus.ENDING)
                    && (status != HazardStatus.ENDED)) {
                persist = false;
            }
            delegate.setStatus(status);
            if (notify) {

                /*
                 * Notify the event manager, but do not treat this as something
                 * that should set the modified flag, since changes in status
                 * should not be considered modifications, and in fact may
                 * result in the modified flag being reset to false depending
                 * upon what status is now being used.
                 */
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventStatusModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventStatusModification(), originator),
                            persist);
                }
            }
            return true;
        }
        return false;
    }

    protected boolean setIssuanceCount(int count, boolean notify,
            IOriginator originator) {
        if (getIssuanceCount() != count) {
            delegate.setIssuanceCount(count);
            if (notify) {

                /*
                 * Notify the event manager, but do not treat this as something
                 * that should set the modified flag, since issuances should not
                 * be considered modifications, and in fact may result in the
                 * modified flag being reset to false.
                 */
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, eventView,
                            new EventIssuanceCountModification(), originator));
                }
            }
            return true;
        }
        return false;
    }

    protected boolean setPhenomenon(String phenomenon, boolean notify,
            IOriginator originator) {
        if (changed(getPhenomenon(), phenomenon)) {
            if (eventManager.canEventTypeBeChanged(this)) {
                delegate.setPhenomenon(phenomenon);
                if (notify) {
                    IHazardEventView eventView = eventManager
                            .getViewForSessionEvent(this);
                    if (eventView != null) {
                        eventManager.hazardEventModified(
                                new SessionEventModified(eventManager,
                                        eventView, new EventTypeModification(),
                                        originator));
                    }
                    handleModification();
                }
            } else {
                throw new IllegalEventModificationException("phenomenon");
            }
            return true;
        }
        return false;
    }

    protected boolean setSignificance(String significance, boolean notify,
            IOriginator originator) {
        if (changed(getSignificance(), significance)) {
            if (eventManager.canEventTypeBeChanged(this)) {
                delegate.setSignificance(significance);
                if (notify) {
                    IHazardEventView eventView = eventManager
                            .getViewForSessionEvent(this);
                    if (eventView != null) {
                        eventManager.hazardEventModified(
                                new SessionEventModified(eventManager,
                                        eventView, new EventTypeModification(),
                                        originator));
                    }
                    handleModification();
                }
            } else {
                throw new IllegalEventModificationException("significance");
            }
            return true;
        }
        return false;
    }

    protected boolean setSubType(String subtype, boolean notify,
            IOriginator originator) {

        if (changed(getSubType(), subtype)) {
            if (eventManager.canEventTypeBeChanged(this)) {
                delegate.setSubType(subtype);
                if (notify) {
                    IHazardEventView eventView = eventManager
                            .getViewForSessionEvent(this);
                    if (eventView != null) {
                        eventManager.hazardEventModified(
                                new SessionEventModified(eventManager,
                                        eventView, new EventTypeModification(),
                                        originator));
                    }
                    handleModification();
                }
            } else {
                throw new IllegalEventModificationException("subtype");
            }
            return true;
        }
        return false;
    }

    protected boolean setHazardType(String phenomenon, String significance,
            String subtype, boolean notify, IOriginator originator) {

        /*
         * TODO Handle case when user sets hazard type back to empty. Should the
         * HID even allow that?
         */
        boolean changed = (changed(getPhenomenon(), phenomenon)
                || changed(getSignificance(), significance)
                || changed(getSubType(), subtype));
        notify &= changed;
        setPhenomenon(phenomenon, false, originator);
        setSignificance(significance, false, originator);
        setSubType(subtype, false, originator);

        IHazardEventView eventView = eventManager.getViewForSessionEvent(this);
        if (eventView != null) {
            eventManager.updateHazardAreas(eventView);
        }
        if (notify) {
            if (eventView != null) {
                eventManager.hazardEventModified(
                        new SessionEventModified(eventManager, eventView,
                                new EventTypeModification(), originator));
            }
            handleModification();
        }
        return changed;
    }

    protected boolean setCreationTime(Date date, boolean notify,
            IOriginator originator) {

        if (getCreationTime() == null) {
            delegate.setCreationTime(date);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, eventView,
                            new EventCreationTimeModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    protected boolean setEndTime(Date date, boolean notify,
            IOriginator originator) {

        if (changed(getEndTime(), date)) {
            delegate.setEndTime(date);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, eventView,
                            new EventTimeRangeModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    protected boolean setStartTime(Date date, boolean notify,
            IOriginator originator) {
        if (changed(getStartTime(), date)) {
            delegate.setStartTime(date);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, eventView,
                            new EventTimeRangeModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    protected boolean setTimeRange(Date startTime, Date endTime, boolean notify,
            IOriginator originator) {
        boolean changed = (changed(getStartTime(), startTime)
                || changed(getEndTime(), endTime));
        notify &= changed;
        setStartTime(startTime, false, originator);
        setEndTime(endTime, false, originator);
        if (notify) {
            IHazardEventView eventView = eventManager
                    .getViewForSessionEvent(this);
            if (eventView != null) {
                eventManager.hazardEventModified(
                        new SessionEventModified(eventManager, eventView,
                                new EventTimeRangeModification(), originator));
            }
            handleModification();
        }
        return changed;
    }

    /**
     * Set the geometry to be that specified.
     * 
     * @param geometry
     *            New geometry.
     * @param notify
     *            Flag indicating whether or not to send out a notification of
     *            any change that results.
     * @param originator
     *            Originator of the change.
     * @return <code>true</code> if this resulted in a change,
     *         <code>false</code> otherwise.
     */
    protected boolean setGeometry(IAdvancedGeometry geometry, boolean notify,
            IOriginator originator) {
        if (changed(getGeometry(), geometry)) {
            pushToStack("setGeometry", IAdvancedGeometry.class, getGeometry());
            delegate.setGeometry(geometry);

            /*
             * Since the new modification not from undo/redo, clear the redo
             * stack.
             */
            if ((undoInProgress == false) && (redoInProgress == false)) {
                redoStack.clear();
            }

            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(new SessionEventModified(
                            eventManager, eventView,
                            new EventGeometryModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    /**
     * Replace the visual feature with the same identifier as the specified
     * visual feature with the latter.
     * 
     * @param visualFeature
     *            New visual feature.
     * @param notify
     *            Flag indicating whether or not to send out a notification of
     *            any change that results.
     * @param originator
     *            Originator of the change.
     * @return <code>true</code> if the new visual feature replaced the old one,
     *         <code>false</code> if no visual feature with the given identifier
     *         was found.
     */
    public boolean setVisualFeature(VisualFeature visualFeature, boolean notify,
            IOriginator originator) {
        if (changed(visualFeature,
                getVisualFeature(visualFeature.getIdentifier()))) {
            boolean result = delegate.setVisualFeature(visualFeature);
            if (result && notify) {

                /*
                 * Notify the event manager, but do not treat this as something
                 * that should set the modified flag, since visual features are
                 * merely visual representations of the hazard event; if they
                 * change and nothing else does, that should not be considered a
                 * substantive modification to the hazard event.
                 */
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventVisualFeaturesModification(
                                            visualFeature.getIdentifier()),
                                    originator));
                }
            }
            return result;
        }
        return false;
    }

    /**
     * Set the visual features to those specified.
     * 
     * @param visualFeatures
     *            New visual features.
     * @param notify
     *            Flag indicating whether or not to send out a notification of
     *            any change that results.
     * @param originator
     *            Originator of the change.
     * @return Set of identifiers of visual features that changed as a result of
     *         this invocation, or <code>null</code> if none changed.
     */
    protected Set<String> setVisualFeatures(VisualFeaturesList visualFeatures,
            boolean notify, IOriginator originator) {
        VisualFeaturesList oldVisualFeatures = getVisualFeatures();
        if (listChanged(oldVisualFeatures, visualFeatures)) {
            Set<String> changedIdentifiers = getIdentifiersOfChanged(
                    oldVisualFeatures, visualFeatures);
            delegate.setVisualFeatures(visualFeatures);
            if (notify) {

                /*
                 * Notify the event manager, but do not treat this as something
                 * that should set the modified flag, since visual features are
                 * merely visual representations of the hazard event; if they
                 * change and nothing else does, that should not be considered a
                 * substantive modification to the hazard event.
                 */
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager
                            .hazardEventModified(
                                    new SessionEventModified(eventManager,
                                            eventView,
                                            new EventVisualFeaturesModification(
                                                    changedIdentifiers),
                                            originator));
                }
            }
            return changedIdentifiers;
        }
        return null;
    }

    /**
     * Determine whether or not replacing one list with the other results in a
     * change. If one is <code>null</code> and the other empty, that is
     * considered no change.
     * 
     * @param oldList
     *            Old list.
     * @param newList
     *            New list.
     * @return True if the two lists are functionally equivalent, false
     *         otherwise.
     */
    private boolean listChanged(List<?> oldList, List<?> newList) {
        if (oldList != newList) {
            if (((oldList == null) && (newList != null) && newList.isEmpty())
                    || ((oldList != null) && (newList == null)
                            && oldList.isEmpty())) {
                return false;
            }
            return changed(oldList, newList);
        }
        return false;
    }

    /**
     * Get the identifiers of any visual features that changed between the
     * specified old and new lists.
     * 
     * @param oldVisualFeatures
     *            Old visual features list.
     * @param newVisualFeatures
     *            New visual features list.
     * @return Set of identifiers of those visual features that changed.
     */
    private Set<String> getIdentifiersOfChanged(
            VisualFeaturesList oldVisualFeatures,
            VisualFeaturesList newVisualFeatures) {

        /*
         * Build a set of identifiers of visual features that have changed
         * between the two visual features lists. If the old or new visual
         * features list is null, then all the ones in the other list are
         * considered changed (they've either been added or deleted), so list
         * them. Otherwise, iterate through the lists, comparing each in turn;
         * z-order matters, so even if the lists are identical in content but
         * not in ordering, any order changes mean that both visual features at
         * such an index are considered changed.
         */
        Set<String> changedIdentifiers = new HashSet<>();
        if ((oldVisualFeatures == null) || (newVisualFeatures == null)) {
            VisualFeaturesList list = (oldVisualFeatures == null
                    ? newVisualFeatures : oldVisualFeatures);
            for (VisualFeature visualFeature : list) {
                changedIdentifiers.add(visualFeature.getIdentifier());
            }
        } else {
            VisualFeaturesList shorterList = (oldVisualFeatures
                    .size() > newVisualFeatures.size() ? newVisualFeatures
                            : oldVisualFeatures);
            VisualFeaturesList longerList = (shorterList == oldVisualFeatures
                    ? newVisualFeatures : oldVisualFeatures);
            int index = 0;
            while (index < shorterList.size()) {
                if (shorterList.get(index)
                        .equals(longerList.get(index)) == false) {
                    changedIdentifiers
                            .add(shorterList.get(index).getIdentifier());
                    changedIdentifiers
                            .add(longerList.get(index).getIdentifier());
                }
                index++;
            }
            while (index < longerList.size()) {
                changedIdentifiers.add(longerList.get(index).getIdentifier());
                index++;
            }
        }
        return changedIdentifiers;
    }

    protected boolean setHazardMode(ProductClass mode, boolean notify,
            IOriginator originator) {
        if (changed(getHazardMode(), mode)) {
            delegate.setHazardMode(mode);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventOriginModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    protected boolean setSource(Source source, boolean notify,
            IOriginator originator) {
        if (changed(getSource(), source)) {
            delegate.setSource(source);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventOriginModification(), originator));
                }
                handleModification();
            }
            return true;
        }
        return false;
    }

    protected boolean setWsId(WsId wsId, boolean notify,
            IOriginator originator) {
        if (changed(getWsId(), wsId)) {
            delegate.setWsId(wsId);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventOriginModification(), originator));
                }
            }
            return true;
        }
        return false;
    }

    private void setHazardAttributes(Map<String, Serializable> attributes,
            boolean notify, IOriginator originator) {

        Set<String> changedKeys = getChangedAttributes(attributes, true);
        if (changedKeys.isEmpty() == false) {
            Map<String, Serializable> originalAttributes = null;
            if (notify) {
                originalAttributes = new HashMap<>();
                for (String changedKey : changedKeys) {
                    originalAttributes.put(changedKey,
                            delegate.getHazardAttribute(changedKey));

                }
            }
            delegate.setHazardAttributes(attributes);
            if (notify) {
                Map<String, Serializable> modifiedAttributes = new HashMap<>();
                for (String changedKey : changedKeys) {
                    modifiedAttributes.put(changedKey,
                            attributes.get(changedKey));

                }
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager.hazardEventAttributeModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventAttributesModification(
                                            modifiedAttributes,
                                            originalAttributes),
                                    originator));
                }
                if (Sets.intersection(changedKeys,
                        eventManager.getHazardAttributesAffectingModifyFlag(
                                delegate.getEventID()))
                        .isEmpty() == false) {
                    handleModification();
                }
            }
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
    private Set<String> getChangedAttributes(
            Map<String, Serializable> attributes, boolean willReplace) {

        /*
         * Determine which attributes are present in the new map but not the old
         * (and, if this is being checked for a replacement map of attributes,
         * the old map but not the new).
         */
        Map<String, Serializable> oldAttributes = getHazardAttributes();
        Set<String> oldAttributeKeys = oldAttributes.keySet();
        Set<String> attributeKeys = attributes.keySet();
        Set<String> changedKeys = new HashSet<>(willReplace
                ? Sets.symmetricDifference(oldAttributeKeys, attributeKeys)
                : Sets.difference(attributeKeys, oldAttributeKeys));

        /*
         * For each attribute present in both maps, determine whether the values
         * in the two maps are the same, and if not, add that attribute to the
         * set of changed attributes.
         */
        for (String key : Sets.intersection(oldAttributeKeys, attributeKeys)) {
            Serializable oldValue = oldAttributes.get(key);
            Serializable newValue = attributes.get(key);
            if ((oldValue != newValue)
                    && ((oldValue == null) || (newValue == null)
                            || (oldValue.equals(newValue) == false))) {
                changedKeys.add(key);
            }
        }

        return changedKeys;
    }

    protected boolean addHazardAttributes(Map<String, Serializable> attributes,
            boolean notify, IOriginator originator) {

        Set<String> changedKeys = getChangedAttributes(attributes, false);
        if (changedKeys.size() > 0) {
            Map<String, Serializable> originalAttributes = null;
            IHazardEventView eventView = null;
            if (notify) {
                eventView = eventManager.getViewForSessionEvent(this);
                if (eventView != null) {
                    originalAttributes = new HashMap<>();
                    for (String changedKey : changedKeys) {
                        originalAttributes.put(changedKey,
                                delegate.getHazardAttribute(changedKey));

                    }
                }
            }
            Map<String, Serializable> modifiedAttributes = new HashMap<>();
            for (String changedKey : changedKeys) {
                modifiedAttributes.put(changedKey, attributes.get(changedKey));
            }
            delegate.addHazardAttributes(modifiedAttributes);
            if (notify) {
                if (eventView != null) {
                    eventManager.hazardEventAttributeModified(
                            new SessionEventModified(eventManager, eventView,
                                    new EventAttributesModification(
                                            modifiedAttributes,
                                            originalAttributes),
                                    originator));
                }
                if (Sets.intersection(changedKeys,
                        eventManager.getHazardAttributesAffectingModifyFlag(
                                delegate.getEventID()))
                        .isEmpty() == false) {
                    handleModification();
                }
            }
            return true;
        }
        return false;
    }

    protected boolean addHazardAttribute(String key, Serializable value,
            boolean notify, IOriginator originator) {

        if (changed(value, getHazardAttribute(key))) {
            Serializable oldValue = (notify ? delegate.getHazardAttribute(key)
                    : null);
            delegate.removeHazardAttribute(key);
            delegate.addHazardAttribute(key, value);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager
                            .hazardEventAttributeModified(
                                    new SessionEventModified(eventManager,
                                            eventView,
                                            new EventAttributesModification(key,
                                                    value, oldValue),
                                            originator));
                }
                if (eventManager.getHazardAttributesAffectingModifyFlag(
                        delegate.getEventID()).contains(key)) {
                    handleModification();
                }
            }
            return true;
        }
        return false;
    }

    protected boolean removeHazardAttribute(String key, boolean notify,
            IOriginator originator) {

        Serializable oldValue = delegate.getHazardAttribute(key);
        if (oldValue != null) {
            delegate.removeHazardAttribute(key);
            if (notify) {
                IHazardEventView eventView = eventManager
                        .getViewForSessionEvent(this);
                if (eventView != null) {
                    eventManager
                            .hazardEventAttributeModified(
                                    new SessionEventModified(eventManager,
                                            eventView,
                                            new EventAttributesModification(key,
                                                    null, oldValue),
                                            originator));
                }
                if (eventManager.getHazardAttributesAffectingModifyFlag(
                        delegate.getEventID()).contains(key)) {
                    handleModification();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean undo() {
        boolean success = false;
        if (isUndoable()) {

            Pair<Method, Object> pair = undoStack.pop();
            Method method = pair.getFirst();
            Object value = pair.getSecond();

            try {
                undoInProgress = true;
                IAdvancedGeometry oldGeometry = delegate.getGeometry();
                method.invoke(this, value);
                if (Utils.equal(oldGeometry, delegate.getGeometry()) == false) {
                    eventManager.handleEventGeometryChangeFromUndoOrRedo(this);
                }
                success = true;
            } catch (Exception e) {
                statusHandler.error(
                        "Error invoking undo method for event " + getEventID(),
                        e);
            }

            undoInProgress = false;
        }
        return success;
    }

    @Override
    public boolean redo() {
        boolean success = false;
        if (isRedoable()) {
            Pair<Method, Object> pair = redoStack.pop();
            Method method = pair.getFirst();
            Object value = pair.getSecond();

            try {
                redoInProgress = true;
                IAdvancedGeometry oldGeometry = delegate.getGeometry();
                method.invoke(this, value);
                if (Utils.equal(oldGeometry, delegate.getGeometry()) == false) {
                    eventManager.handleEventGeometryChangeFromUndoOrRedo(this);
                }
                success = true;
            } catch (Exception e) {
                statusHandler.error(
                        "Error invoking redo method for event " + getEventID(),
                        e);
            }

            redoInProgress = false;
        }
        return success;
    }

    @Override
    public boolean isUndoable() {
        return !undoStack.isEmpty();
    }

    @Override
    public boolean isRedoable() {
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
                Method method = getClass().getMethod(methodName, className);
                Pair<Method, Object> methodValuePair = new Pair<Method, Object>(
                        method, value);
                undoStack.push(methodValuePair);
            } else {
                Method method = getClass().getMethod(methodName, className);
                Pair<Method, Object> methodValuePair = new Pair<Method, Object>(
                        method, value);
                redoStack.push(methodValuePair);

            }
        } catch (Exception e) {
            statusHandler.error(
                    "Error updating undo/redo stack for event " + getEventID(),
                    e);
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

    /**
     * Set the event's "modified" flag to indicate whether or not it has been
     * modified since the last time it was issued.
     * 
     * @param modified
     *            Flag indicating whether or not the event has been modified
     *            since the last time it was issued.
     */
    @Override
    public void setModified(boolean modified) {
        if ((modifiedNotAllowedToChange == false)
                && (this.modified != modified)) {
            this.modified = modified;
            eventManager.hazardEventModifiedFlagChanged(this);
        }
    }

    /**
     * Determine whether or not the modified flag is not allowed to change.
     * 
     * @return <code>true</code> if the modified flag is not allowed to change,
     *         <code>false</code> otherwise.
     */
    boolean isModifiedNotAllowedToChange() {
        return modifiedNotAllowedToChange;
    }

    /**
     * Set the flag indicating whether or not the modified flag is not allowed
     * to change.
     * 
     * @param notAllowedToChange
     *            Flag indicating whether or not the modified flag is not
     *            allowed to change.
     */
    void setModifiedNotAllowedToChange(boolean notAllowedToChange) {
        this.modifiedNotAllowedToChange = notAllowedToChange;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void setInsertTime(Date date) {
        delegate.setInsertTime(date);

    }

    @Override
    public Date getInsertTime() {
        return delegate.getInsertTime();
    }
}
