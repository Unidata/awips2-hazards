/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.datastorage;

import static org.junit.Assert.*;

import org.joda.time.DateTime



import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.time.TimeRange
import com.vividsolutions.jts.geom.Geometry

import gov.noaa.gsd.common.utilities.DateTimes
import gov.noaa.gsd.viz.hazards.HazardEventsBuilder
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

/**
 * Description: Tests of {@link InMemoryHazardEventManager}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 21, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
class InMemoryHazardEventManagerTest extends spock.lang.Specification {

    private IHazardEventManager hazardEventManager;
    private DateTimes dateTimes;

    def setup() {
        hazardEventManager = new InMemoryHazardEventManager();
        String eventsAsJson = Utilities.getCannedEventsAsJSON()
        HazardEventsBuilder hazardEventBuilder = new HazardEventsBuilder(eventsAsJson)
        hazardEventManager.storeEvents(hazardEventBuilder.getEvents())
        dateTimes = new DateTimes();
    }


    def "Basic" () {
        when: "Get all"
        Map<String, HazardHistoryList> allEvents = hazardEventManager.getAll()

        then: "Correct number of events"
        allEvents.size() == 16
    }

    def "Filter by time" () {
        when: "Time filter"
        DateTime dt = dateTimes.newDateTime(1297140900000)
        Date startDatetime = dt.toDate()

        dt = dateTimes.newDateTime(1297151700000)
        Date endDatetime = dt.toDate()
        Map<String, HazardHistoryList> filteredEvents =
                hazardEventManager.getByTime(startDatetime, endDatetime);
        IHazardEvent event3 = filteredEvents.get("3").get(0)

        then: "Get correct subset"
        filteredEvents.size() == 4
        event3.getEventID() == "3"

        when: "Time range"
        TimeRange timeRange = new TimeRange(startDatetime, endDatetime)
        filteredEvents = hazardEventManager.getByTimeRange(timeRange);

        then:
        filteredEvents.size() == 4
    }

    def "Filter by eventID" () {
        when: "Existing event"
        HazardHistoryList eventList = hazardEventManager.getByEventID("11")
        IHazardEvent event = eventList.get(0)

        then: "Correct event"
        eventList.size() == 1

        when: "Get a non-existent event"
        eventList = hazardEventManager.getByEventID("999")

        then:
        eventList.size() == 0
    }

    def "Filter by state" () {
        Map<String, HazardHistoryList> events = hazardEventManager.getBySiteID("RAH")

        expect:
        events.size() == 4
    }

    def "Filter by significance" () {
        Map<String, HazardHistoryList> events = hazardEventManager.getBySignificance("Y")

        expect:
        events.size() == 1
        events.get("10").size() == 1
    }

    def "Filter by phensig" () {
        Map<String, HazardHistoryList> events = hazardEventManager.getByPhensig("FL", "W")

        expect:
        events.size() == 1
        events.get("12").size() == 1
    }

    def "Filter by geometry" () {
        HazardHistoryList eventList = hazardEventManager.getByEventID("11")
        IHazardEvent event = eventList.get(0)
        Geometry geometry = event.getGeometry()
        Map<String, HazardHistoryList> events = hazardEventManager.getByGeometry(geometry)

        expect:
        events.size() == 2
        events.get("11").size() == 1
    }

    def "Remove, store, update" () {
        when: "Remove"
        HazardHistoryList eventList = hazardEventManager.getByEventID("11")
        IHazardEvent event = eventList.get(0)
        hazardEventManager.removeEvent(event)
        Map<String, HazardHistoryList> allEvents = hazardEventManager.getAll()

        then:
        allEvents.size() == 15
        allEvents.get("11") == null

        when: "Store"
        hazardEventManager.storeEvent(event)
        allEvents = hazardEventManager.getAll()

        then:
        allEvents.size() == 16
        allEvents.get("11") != null

        when: "Update"
        event.setSiteID("OMA")
        hazardEventManager.updateEvent(event)
        event = hazardEventManager.getByEventID("11").get(0)

        then:
        event.getSiteID() == "OMA"

        when: "Remove all"
        hazardEventManager.removeAllEvents()
        allEvents = hazardEventManager.getAll()

        then:
        allEvents.size() == 0

        when: "Update non-existing event"
        event = hazardEventManager.createEvent()
        event.setEventID("9999")
        boolean exceptionFound = false

        try {
            hazardEventManager.updateEvent(event)
        }
        catch (IllegalArgumentException e) {
            exceptionFound = true
        }

        then:
        exceptionFound
    }
}
