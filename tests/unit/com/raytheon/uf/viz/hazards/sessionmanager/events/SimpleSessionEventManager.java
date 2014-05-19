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
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.AbstractSessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
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
 * 
 *  
 * Nov 29, 2013 2380    daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SimpleSessionEventManager extends AbstractSessionEventManager {

    private final boolean canChangeGeometry;

    private final boolean canChangeTimeRange;

    private final boolean canChangeType;

    private final List<ObservedHazardEvent> events = new ArrayList<ObservedHazardEvent>();

    public SimpleSessionEventManager() {
        this(true, true, true);
    }

    public SimpleSessionEventManager(boolean canChangeGeometry,
            boolean canChangeTimeRange, boolean canChangeType) {
        this.canChangeGeometry = canChangeGeometry;
        this.canChangeTimeRange = canChangeTimeRange;
        this.canChangeType = canChangeType;
    }

    @Override
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(
            ObservedHazardEvent hazardEvent) {
        return null;
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
    public void removeEvent(IHazardEvent event, IOriginator originator) {
        events.remove(event);
    }

    @Override
    public Collection<ObservedHazardEvent> getEvents() {
        return events;
    }

    @Override
    public boolean canChangeGeometry(ObservedHazardEvent event) {
        return canChangeGeometry;
    }

    @Override
    public boolean canChangeTimeRange(ObservedHazardEvent event) {
        return canChangeTimeRange;
    }

    @Override
    public boolean canChangeType(ObservedHazardEvent event) {
        return canChangeType;
    }

    @Override
    public void sortEvents(Comparator<ObservedHazardEvent> comparator) {
        Collections.sort(events, comparator);
    }

    public void reset() {
        events.clear();
    }

    @Override
    public ObservedHazardEvent getLastModifiedSelectedEvent() {
        return null;
    }

    @Override
    public void setLastModifiedSelectedEvent(ObservedHazardEvent event,
            IOriginator originator) {
        throw new UnsupportedOperationException();
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
    public Collection<ObservedHazardEvent> getEventsForCurrentSettings() {
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
    public boolean clipSelectedHazardGeometries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reduceSelectedHazardGeometries() {
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
}
