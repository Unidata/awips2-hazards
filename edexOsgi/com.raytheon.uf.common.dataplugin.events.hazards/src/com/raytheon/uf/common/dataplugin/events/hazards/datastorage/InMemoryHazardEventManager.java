/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.datastorage;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: In-memory implementation of {@link IHazardEventManager}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class InMemoryHazardEventManager extends HazardEventManager {

    private List<IHazardEvent> hazardEvents;

    public InMemoryHazardEventManager() {
        super(Mode.PRACTICE);
        hazardEvents = Lists.newArrayList();
    }

    @Override
    public IHazardEvent createEvent() {
        return new BaseHazardEvent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataplugin.events.datastorage.IEventManager#
     * createEvent(com.raytheon.uf.common.dataplugin.events.IEvent)
     */
    @Override
    public IHazardEvent createEvent(IHazardEvent event) {
        return new BaseHazardEvent(event);
    }

    @Override
    public Map<String, HazardHistoryList> getEventsByFilter(
            Map<String, List<Object>> filters) {
        Map<String, HazardHistoryList> result = Maps.newHashMap();
        for (IHazardEvent hazardEvent : hazardEvents) {
            if (filterCriteriaMet(hazardEvent, filters)) {
                HazardHistoryList hazardList = new HazardHistoryList();
                hazardList.add(hazardEvent);
                result.put(hazardEvent.getEventID(), hazardList);
            }

        }

        return result;
    }

    private boolean filterCriteriaMet(IHazardEvent hazardEvent,
            Map<String, List<Object>> filters) {
        boolean value = true;
        for (String filterName : filters.keySet()) {
            List<Object> list = filters.get(filterName);
            for (Object filterValue : list) {
                if (filterName.equals(HazardConstants.EVENTID)) {
                    if (hazardEvent.getEventID().equals(filterValue)) {
                        return true;
                    }
                    value = false;
                }

                if (filterName.equals(HazardConstants.SITEID)) {
                    if (list.size() > 1) {
                        if (hazardEvent.getSiteID().equals(filterValue)) {
                            return true;
                        }
                        value = false;
                    }
                }

                if (filterName.equals(HazardConstants.STATE)) {
                    if (hazardEvent.getState().equals(filterValue)) {
                        return true;
                    }
                    value = false;
                }

                if (filterName.equals(HazardConstants.SIGNIFICANCE)) {
                    if (hazardEvent.getSignificance().equals(filterValue)) {
                        return true;
                    }
                    value = false;
                }

                if (filterName.equals(HazardConstants.PHENOMENON)) {
                    if (hazardEvent.getPhenomenon().equals(filterValue)) {
                        return true;
                    }
                    value = false;
                }

                if (filterName.equals(HazardConstants.GEOMETRY)) {
                    if (hazardEvent.getGeometry().intersects(
                            (Geometry) filterValue)) {
                        return true;
                    }
                    value = false;
                }

                if (filterName.equals(HazardConstants.STARTTIME)) {
                    Date filterDate = (Date) filterValue;
                    Date eventDate = hazardEvent.getStartTime();
                    if (!eventDate.before(filterDate)) {
                        return true;
                    }
                    value = false;
                }

                if (filterName.equals(HazardConstants.ENDTIME)) {
                    Date filterDate = (Date) filterValue;
                    Date eventDate = hazardEvent.getEndTime();
                    if (!eventDate.after(filterDate)) {
                        return true;
                    }
                    value = false;
                }
            }
        }
        return value;
    }

    @Override
    public boolean storeEvents(List<IHazardEvent> events) {
        return this.hazardEvents.addAll(events);
    }

    @Override
    public boolean updateEvents(List<IHazardEvent> events) {
        for (IHazardEvent event : events) {
            _updateEvent(event);
        }
        return true;
    }

    private void _updateEvent(IHazardEvent event) {
        IHazardEvent matchedEvent = null;
        for (IHazardEvent hazardEvent : hazardEvents) {
            if (hazardEvent.getEventID().equals(event.getEventID())) {
                matchedEvent = hazardEvent;
                break;
            }
        }
        if (matchedEvent == null) {
            throw new IllegalArgumentException(
                    "Attempt to update non-existent event");
        }
        hazardEvents.remove(matchedEvent);
        hazardEvents.add(event);
    }

    @Override
    public boolean removeEvents(List<IHazardEvent> events) {
        for (IHazardEvent event : events) {
            if (hazardEvents.contains(event)) {
                hazardEvents.remove(event);
            }
        }
        return true;
    }

    // @Override
    public boolean removeAllEvents() {
        hazardEvents.clear();
        return true;
    }
}
