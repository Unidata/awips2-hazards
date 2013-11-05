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
package com.raytheon.uf.common.hazards.storage;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.util.DeployTestProperties;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Test the storage of an {@link IHazardEvent}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 30, 2013            mnash     Initial creation
 * Oct 30, 2013 #1472      bkowal    Added a test for retrieving a large number of
 *                                   hazards by phensig.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@Ignore
public abstract class AbstractHazardStorageTest {

    private final String site = "kxxx";

    private final String phen = "ZO"; // zombies!

    private final String sig = "P"; // apocalypse!

    private final HazardState state = HazardState.POTENTIAL;

    private final TimeUnit time = TimeUnit.DAYS;

    private final Date date = new Date(System.currentTimeMillis()
            - time.convert(365, TimeUnit.MILLISECONDS)); // go back a year from
                                                         // today, so we
                                                         // don't overlap

    private final ProductClass clazz = ProductClass.OPERATIONAL;

    private final Coordinate coordinate = new Coordinate(10, 10);

    public IHazardEventManager manager = new HazardEventManager(getMode());

    @Before
    public void setUp() {
        DeployTestProperties.getInstance();
    }

    public IHazardEvent createNewEvent() {
        IHazardEvent createdEvent = manager.createEvent();
        createdEvent.setEventID(UUID.randomUUID().toString());
        createdEvent.setEndTime(date);
        createdEvent.setHazardMode(clazz);
        createdEvent.setIssueTime(date);
        createdEvent.setPhenomenon(phen);
        createdEvent.setSignificance(sig);
        createdEvent.setSiteID(site);
        createdEvent.setState(state);
        createdEvent.setStartTime(date);
        createdEvent.setSubtype("Biohazard");

        GeometryFactory factory = new GeometryFactory();
        Geometry geometry = factory.createPoint(coordinate);
        createdEvent.setGeometry(geometry);
        return createdEvent;
    }

    private IHazardEvent storeEvent() {
        IHazardEvent createdEvent = createNewEvent();
        boolean stored = manager.storeEvent(createdEvent);
        assertTrue("Not able to store event", stored);
        return createdEvent;
    }

    /**
     * 
     */
    @Test
    public void testByEventId() {
        IHazardEvent createdEvent = storeEvent();
        HazardHistoryList list = manager
                .getByEventID(createdEvent.getEventID());
        assertThat(list, hasSize(1));
        assertEquals(createdEvent, list.get(0));
    }

    @Test
    public void testByGeometry() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager
                .getByGeometry(createdEvent.getGeometry());
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testBySite() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getBySiteID(site);
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByPhenomenon() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getByPhenomenon(phen);
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testBySignificance() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getBySignificance(sig);
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByPhensig() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getByPhensig(phen, sig);
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByTime() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getByTime(date, date);
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testRemove() {
        IHazardEvent createdEvent = storeEvent();
        boolean tf = manager.removeEvent(createdEvent);
        assertTrue(tf);
        HazardHistoryList list = manager
                .getByEventID(createdEvent.getEventID());
        assertThat(list, hasSize(0));
    }

    @Test
    public void testUpdateTime() {
        IHazardEvent createdEvent = storeEvent();
        Date newTime = new Date();
        createdEvent.setIssueTime(newTime);
        boolean tf = manager.updateEvent(createdEvent);
        assertTrue(tf);
        HazardHistoryList list = manager
                .getByEventID(createdEvent.getEventID());
        assertThat(createdEvent.getEventID(), list, hasSize(1));
    }

    @Test
    public void testByMultipleSite() {
        IHazardEvent createdEvent = storeEvent();
        HazardQueryBuilder builder = new HazardQueryBuilder();
        // get by kxxx OR koax
        builder.addKey(HazardConstants.SITEID, site);
        builder.addKey(HazardConstants.SITEID, "koax");
        Map<String, HazardHistoryList> list = manager.getEventsByFilter(builder
                .getQuery());
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByMultipleStartTime() {
        IHazardEvent createdEvent = storeEvent();
        HazardQueryBuilder builder = new HazardQueryBuilder();
        builder.addKey(HazardConstants.STARTTIME, createdEvent.getStartTime());
        builder.addKey(HazardConstants.STARTTIME, createdEvent.getStartTime());
        Map<String, HazardHistoryList> list = manager.getEventsByFilter(builder
                .getQuery());
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByMultipleEndTime() {
        IHazardEvent createdEvent = storeEvent();
        HazardQueryBuilder builder = new HazardQueryBuilder();
        builder.addKey(HazardConstants.ENDTIME, createdEvent.getEndTime());
        builder.addKey(HazardConstants.ENDTIME, createdEvent.getEndTime());
        Map<String, HazardHistoryList> list = manager.getEventsByFilter(builder
                .getQuery());
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByMultipleGeometry() {
        IHazardEvent createdEvent = storeEvent();
        HazardQueryBuilder builder = new HazardQueryBuilder();
        GeometryFactory factory = new GeometryFactory();
        builder.addKey(HazardConstants.GEOMETRY, createdEvent.getGeometry());
        // add a bogus point
        builder.addKey(HazardConstants.GEOMETRY,
                factory.createPoint(new Coordinate(1, 1)));
        Map<String, HazardHistoryList> list = manager.getEventsByFilter(builder
                .getQuery());
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByMultiplePhensig() {
        List<String> phensigs = new ArrayList<String>();
        IHazardEvent event = storeEvent();

        event = createNewEvent();
        event.setPhenomenon("ZW");
        event.setSubtype("Convective");
        manager.storeEvent(event);

        event = createNewEvent();
        event.setPhenomenon("ZW");
        event.setSignificance("D");
        manager.storeEvent(event);

        event = createNewEvent();
        event.setPhenomenon("TL");
        event.setSignificance("D");
        manager.storeEvent(event);

        phensigs.add("TL.D");
        phensigs.add("ZW.P.Convective");
        Map<String, HazardHistoryList> list = manager
                .getByMultiplePhensigs(phensigs);
        // should get 2 back
        assertThat(list.keySet(), hasSize(2));
        for (String eid : list.keySet()) {
            for (IHazardEvent ev : list.get(eid)) {
                manager.removeEvent(ev);
            }
        }
    }

    @Test
    public void testByMultiplePhensigLargeRetrieval() {
        List<String> phensigs = new ArrayList<String>();
        IHazardEvent event = storeEvent();

        // create 50 of each type of event.
        for (int i = 0; i < 50; i++) {
            event = createNewEvent();
            event.setPhenomenon("FA");
            event.setSubtype("Convective");
            manager.storeEvent(event);
        }

        for (int i = 0; i < 50; i++) {
            event = createNewEvent();
            event.setPhenomenon("TO");
            event.setSignificance("D");
            manager.storeEvent(event);
        }

        for (int i = 0; i < 50; i++) {
            event = createNewEvent();
            event.setPhenomenon("FF");
            event.setSignificance("D");
            manager.storeEvent(event);
        }

        phensigs.add("FF.D");
        phensigs.add("FA.P.Convective");
        final long startTime = System.currentTimeMillis();
        Map<String, HazardHistoryList> list = manager
                .getByMultiplePhensigs(phensigs);
        final long endTime = System.currentTimeMillis();
        final long totalTime = endTime - startTime;
        
        // ensure that the total time is < 1 second = 0 seconds
        assertThat(totalTime, lessThanOrEqualTo(DateUtils.MILLIS_PER_SECOND));
        // verify that we have received the expected number of records
        assertThat(list.keySet(), hasSize(100));
        for (String eid : list.keySet()) {
            for (IHazardEvent ev : list.get(eid)) {
                manager.removeEvent(ev);
            }
        }
    }

    abstract Mode getMode();
}
