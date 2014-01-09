/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.datastorage;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class InMemoryHazardEventManager extends HazardEventManager {

    private final List<IHazardEvent> hazardEvents;

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

    // very ugly, very brute force, but gets the job done
    private boolean filterCriteriaMet(IHazardEvent hazardEvent,
            Map<String, List<Object>> filters) {
        boolean value = true;
        for (String filterName : filters.keySet()) {
            List<Object> list = filters.get(filterName);
            boolean internalTruth = true;
            if (filterName.equals(HAZARD_EVENT_IDENTIFIER)) {
                for (Object filterValue : list) {
                    if (hazardEvent.getEventID().equals(filterValue)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
            }

            if (filterName.equals(SITE_ID)) {
                for (Object filterValue : list) {
                    if (hazardEvent.getSiteID().equals(filterValue)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
            }

            if (filterName.equals(HAZARD_EVENT_STATE)) {
                for (Object filterValue : list) {
                    if (hazardEvent.getState().equals(filterValue)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
            }

            if (filterName.equals(SIGNIFICANCE)) {
                for (Object filterValue : list) {
                    if (hazardEvent.getSignificance().equals(filterValue)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
            }

            if (filterName.equals(PHENOMENON)) {
                for (Object filterValue : list) {
                    if (hazardEvent.getPhenomenon().equals(filterValue)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
            }

            if (filterName.equals(GEOMETRY)) {
                for (Object filterValue : list) {
                    if (hazardEvent.getGeometry().intersects(
                            (Geometry) filterValue)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
            }

            if (filterName.equals(HAZARD_EVENT_START_TIME)) {
                for (Object filterValue : list) {
                    Date filterDate = (Date) filterValue;
                    Date eventDate = hazardEvent.getStartTime();
                    if (eventDate.after(filterDate)
                            || eventDate.equals(filterDate)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
            }

            if (filterName.equals(HAZARD_EVENT_END_TIME)) {
                for (Object filterValue : list) {
                    Date filterDate = (Date) filterValue;
                    Date eventDate = hazardEvent.getEndTime();
                    if (eventDate.before(filterDate)
                            || eventDate.equals(filterDate)) {
                        internalTruth = true;
                        break;
                    } else {
                        internalTruth = false;
                    }
                }
                if (value == false) {
                    return false;
                }
                value = internalTruth;
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

    @Override
    public boolean removeAllEvents() {
        hazardEvents.clear();
        return true;
    }
}
