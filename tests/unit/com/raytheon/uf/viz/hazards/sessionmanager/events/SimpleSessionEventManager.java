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
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

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
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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

public class SimpleSessionEventManager implements
        ISessionEventManager<ObservedHazardEvent> {

    private final boolean canChangeType;

    private final List<ObservedHazardEvent> events = new ArrayList<ObservedHazardEvent>();

    public SimpleSessionEventManager() {
        this(true, true);
    }

    public SimpleSessionEventManager(boolean canChangeGeometry,
            boolean canChangeType) {
        this.canChangeType = canChangeType;
    }

    @Override
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(
            IHazardEvent hazardEvent) {
        return null;
    }

    @Override
    public List<String> getDurationChoices(IHazardEvent event) {
        return Collections.emptyList();
    }

    @Override
    public void eventCommandInvoked(ObservedHazardEvent event,
            String identifier,
            Map<String, Map<String, Object>> mutableProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEventCategory(ObservedHazardEvent event, String category,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setEventType(ObservedHazardEvent event, String phenomenon,
            String significance, String subType, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObservedHazardEvent addEvent(IHazardEvent event,
            IOriginator originator) {
        SessionEventManager eventManager = mock(SessionEventManager.class);
        ObservedHazardEvent ev = new ObservedHazardEvent(event, eventManager);
        when(eventManager.canChangeType(ev)).thenReturn(true);
        events.add(ev);
        return ev;
    }

    @Override
    public void removeEvent(ObservedHazardEvent event, IOriginator originator) {
        events.remove(event);
    }

    @Override
    public List<ObservedHazardEvent> getEvents() {
        return events;
    }

    @Override
    public boolean canChangeType(ObservedHazardEvent event) {
        return canChangeType;
    }

    @Override
    public void sortEvents(Comparator<ObservedHazardEvent> comparator,
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
    public Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> getAllConflictingEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getEventIdsAllowingUntilFurtherNotice() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<IHazardEvent, Collection<String>> getConflictingEvents(
            IHazardEvent event, Date startTime, Date endTime,
            Geometry geometry, String phenSigSubtype) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ObservedHazardEvent> getEventsForCurrentSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endEvent(ObservedHazardEvent event, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void issueEvent(ObservedHazardEvent event, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getEventIdsAllowingProposal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void proposeEvent(ObservedHazardEvent event, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canEventAreaBeChanged(ObservedHazardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSelectedHazardUGCs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObservedHazardEvent getEventById(String eventId) {
        for (ObservedHazardEvent event : events) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }

        }
        return null;
    }

    @Override
    public HazardHistoryList getEventHistoryById(String eventId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ObservedHazardEvent> getEventsByStatus(
            HazardStatus status, boolean includeUntyped) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ObservedHazardEvent> getCheckedEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeEvents(Collection<ObservedHazardEvent> events,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentEvent(String eventID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEventChecked(IHazardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEventChecked(IHazardEvent event, boolean checked,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObservedHazardEvent getCurrentEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentEvent(ObservedHazardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void proposeEvents(Collection<ObservedHazardEvent> events,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCurrentEvent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidGeometryChange(IAdvancedGeometry geometry,
            ObservedHazardEvent hazardEvent, boolean checkGeometryValidity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addOrRemoveEnclosingUGCs(Coordinate location,
            IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> buildInitialHazardAreas(IHazardEvent hazardEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateHazardAreas(IHazardEvent hazardEvent) {
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
    public boolean setEventTimeRange(ObservedHazardEvent event, Date startTime,
            Date endTime, IOriginator originator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setEventGeometry(ObservedHazardEvent event,
            IAdvancedGeometry geometry, IOriginator originator) {
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
    public void saveEvents(List<IHazardEvent> events, boolean addToHistory,
            boolean treatAsIssuance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAddCreatedEventsToSelected(boolean addCreatedEventsToSelected) {
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
    public void revertEventToLastSaved(String eventIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mergeHazardEvents(IHazardEvent newEvent,
            ObservedHazardEvent oldEvent, boolean forceMerge,
            boolean keepVisualFeatures, boolean persistOnStatusChange,
            boolean useModifiedValue, IOriginator originator) {
        throw new UnsupportedOperationException();
    }
}
