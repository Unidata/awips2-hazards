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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Range;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.AbstractHazardServicesEventIdUtil.IdDisplayType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

/**
 * Simplified event manager that just stores all events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013 1257       bsteffen    Initial creation
 * Nov 14, 2013 1463       blawrenc    Added stubbed out methods
 *                                     for handling conflict
 *                                     detection.
 * Nov 29, 2013 2380    daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * Aug 20, 2014 4243       Chris.Golden Added implementation of
 *                                      new method to run scripts.
 * Sep 16, 2014 4753       Chris.Golden Added mutable properties to event scripts.
 * Dec  1, 2014 4188       Dan Schaffer Now allowing hazards to be shrunk or expanded when appropriate.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Jan 08, 2015 5700       Chris.Golden Changed to generalize the meaning of a command invocation
 *                                      for a particular event, since it no longer only means
 *                                      that an event-modifying script is to be executed.
 * Jan  7, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect hazard area designation.
 * Feb 03, 2015 2331       Chris.Golden Changed to support allowable boundaries for event start
 *                                      and end times.
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Mar 13, 2015 6090       Dan Schaffer Relaxed geometry validity check.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SimpleSessionEventManager implements ISessionEventManager {

    private final boolean canChangeType;

    private final List<ObservedHazardEvent> events = new ArrayList<>();

    public SimpleSessionEventManager() {
        this(true, true);
    }

    public SimpleSessionEventManager(boolean canChangeGeometry,
            boolean canChangeType) {
        this.canChangeType = canChangeType;
    }

    @Override
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(
            IHazardEventView hazardEvent) {
        return null;
    }

    @Override
    public List<String> getDurationChoices(IHazardEventView event) {
        return Collections.emptyList();
    }

    @Override
    public void eventCommandInvoked(IHazardEventView event, String identifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventPropertyChangeResult changeEventProperty(
            IHazardEventView event, EventPropertyChange<T> propertyChange,
            T parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventPropertyChangeResult changeEventProperty(
            IHazardEventView event, EventPropertyChange<T> propertyChange,
            T parameters, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IHazardEventView addEvent(IReadableHazardEvent event,
            IOriginator originator) {
        SessionEventManager eventManager = mock(SessionEventManager.class);
        ObservedHazardEvent ev = new ObservedHazardEvent(event, eventManager);
        when(eventManager.canEventTypeBeChanged(ev)).thenReturn(true);
        events.add(ev);
        return new HazardEventView(ev);
    }

    @Override
    public void removeEvent(IHazardEventView event, boolean confirm,
            IOriginator originator) {
        for (ObservedHazardEvent sessionEvent : events) {
            if (event.getEventID().equals(sessionEvent.getEventID())) {
                events.remove(sessionEvent);
                return;
            }
        }
    }

    @Override
    public List<IHazardEventView> getEvents() {
        List<IHazardEventView> eventViews = new ArrayList<>(events.size());
        for (ObservedHazardEvent event : events) {
            eventViews.add(new HazardEventView(event));
        }
        return eventViews;
    }

    @Override
    public boolean canEventTypeBeChanged(IReadableHazardEvent event) {
        return canChangeType;
    }

    @Override
    public void sortEvents(Comparator<IReadableHazardEvent> comparator,
            IOriginator originator) {
        Collections.sort(events, comparator);
    }

    public void reset() {
        events.clear();
    }

    @Override
    public void shutdown() {
        /**
         * Nothing to do right now.
         */
    }

    @Override
    public Map<IReadableHazardEvent, Map<IReadableHazardEvent, Collection<String>>> getAllConflictingEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getEventIdsAllowingUntilFurtherNotice() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<IReadableHazardEvent, Collection<String>> getConflictingEvents(
            IReadableHazardEvent event, Date startTime, Date endTime,
            Geometry geometry, String phenSigSubtype) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Collection<IReadableHazardEvent>> getConflictingEventsForSelectedEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IHazardEventView> getEventsForCurrentSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endEvent(IHazardEventView event, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void issueEvent(IHazardEventView event, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getSelectedEventIdsAllowingProposal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventPropertyChangeResult proposeEvent(IHazardEventView event,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canEventAreaBeChanged(IReadableHazardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSelectedHazardUgcs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IHazardEventView getEventById(String eventId) {
        for (ObservedHazardEvent event : events) {
            if (event.getEventID().equals(eventId)) {
                return new HazardEventView(event);
            }

        }
        return null;
    }

    @Override
    public List<IHazardEventView> getEventHistoryById(String eventId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<IHazardEventView> getEventsByStatus(HazardStatus status,
            boolean includeUntyped) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IHazardEventView> getCheckedEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeEvents(Collection<? extends IHazardEventView> events,
            boolean confirm, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentEvent(String eventID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEventChecked(IHazardEventView event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEventChecked(IHazardEventView event, boolean checked,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IHazardEventView getCurrentEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentEvent(IHazardEventView event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, EventPropertyChangeResult> proposeEvents(
            Collection<? extends IHazardEventView> events,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCurrentEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidGeometryChange(IAdvancedGeometry geometry,
            IReadableHazardEvent hazardEvent, boolean checkGeometryValidity,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addOrRemoveEnclosingUgcs(Coordinate location,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> buildInitialHazardAreas(
            IReadableHazardEvent hazardEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Range<Long>> getStartTimeBoundariesForEventIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Range<Long>> getEndTimeBoundariesForEventIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHighResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setLowResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHighResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setLowResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, EventPropertyChangeResult> saveEvents(
            List<? extends IHazardEventView> events, boolean addToHistory,
            boolean keepLocked, boolean treatAsIssuance,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAddCreatedEventsToSelected(
            boolean addCreatedEventsToSelected) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean isShutDown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, TimeResolution> getTimeResolutionsForEventIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHistoricalVersionCountForEvent(String eventIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventPropertyChangeResult revertEventToLastSaved(
            String eventIdentifier, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventPropertyChangeResult mergeHazardEvents(
            IReadableHazardEvent newEvent, IHazardEventView oldEvent,
            boolean forceMerge, boolean persistOnStatusChange,
            boolean fromDatabase, boolean useModifiedValue,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry getCwaGeometry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearCwaGeometry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyEvents(List<? extends IHazardEventView> events) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getRecommendersForTriggerIdentifiers(
            String eventIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void breakEventLock(IHazardEventView event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPotentialEventsToPending(
            Collection<? extends IHazardEventView> events) {
        // TODO Auto-generated method stub
    }

    @Override
    public EventPropertyChangeResult initiateEventEndingProcess(
            IHazardEventView event, IOriginator originator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean initiateSelectedEventsEndingProcess(IOriginator originator) {
        return false;
    }

    @Override
    public EventPropertyChangeResult revertEventEndingProcess(
            IHazardEventView event, IOriginator originator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUndoable(IHazardEventView event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRedoable(IHazardEventView event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public EventPropertyChangeResult undo(IHazardEventView event) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventPropertyChangeResult redo(IHazardEventView event) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEventHistorical(IHazardEventView event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEventModified(IHazardEventView event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isProposedStateAllowed(IHazardEventView event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetEvents(IOriginator originator) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isEventInDatabase(IReadableHazardEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager#
     * setIdDisplayType(com.raytheon.uf.common.dataplugin.events.hazards.event.
     * HazardServicesEventIdUtil.IdDisplayType)
     */
    @Override
    public void setIdDisplayType(IdDisplayType displayType) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager#
     * isHazardous(com.raytheon.uf.common.dataplugin.events.hazards.event.
     * IReadableHazardEvent)
     */
    @Override
    public boolean isHazardous(IReadableHazardEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager#
     * getFilteredEvents()
     */
    @Override
    public Set<IHazardEventView> getFilteredEvents() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager#
     * changeEventProperty(java.lang.String,
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager.
     * EventPropertyChange, java.lang.Object)
     */
    @Override
    public <T> EventPropertyChangeResult changeEventProperty(
            String eventIdentifier, EventPropertyChange<T> propertyChange,
            T parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager#
     * changeEventProperty(java.lang.String,
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager.
     * EventPropertyChange, java.lang.Object,
     * com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator)
     */
    @Override
    public <T> EventPropertyChangeResult changeEventProperty(
            String eventIdentifier, EventPropertyChange<T> propertyChange,
            T parameters, IOriginator originator) {
        // TODO Auto-generated method stub
        return null;
    }
}
