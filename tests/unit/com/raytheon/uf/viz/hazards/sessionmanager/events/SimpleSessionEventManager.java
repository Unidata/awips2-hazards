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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.AbstractSessionEventManager;
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

    private final List<IHazardEvent> events = new ArrayList<IHazardEvent>();

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
    public IHazardEvent addEvent(IHazardEvent event) {
        events.add(event);
        return event;
    }

    @Override
    public void removeEvent(IHazardEvent event) {
        events.remove(event);
    }

    @Override
    public Collection<IHazardEvent> getEvents() {
        return events;
    }

    @Override
    public boolean canChangeGeometry(IHazardEvent event) {
        return canChangeGeometry;
    }

    @Override
    public boolean canChangeTimeRange(IHazardEvent event) {
        return canChangeTimeRange;
    }

    @Override
    public boolean canChangeType(IHazardEvent event) {
        return canChangeType;
    }

    @Override
    public void sortEvents(Comparator<IHazardEvent> comparator) {
        Collections.sort(events, comparator);
    }

    public void reset() {
        events.clear();
    }

    @Override
    public IHazardEvent getLastModifiedSelectedEvent() {
        return null;
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
    public Collection<IHazardEvent> getEventsForCurrentSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endEvent(IHazardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void issueEvent(IHazardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void proposeEvent(IHazardEvent event) {
        throw new UnsupportedOperationException();
    }

}
