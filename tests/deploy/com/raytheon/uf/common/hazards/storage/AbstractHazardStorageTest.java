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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
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
 * Jan 30, 2013            mnash       Initial creation
 * Oct 30, 2013 #1472      bkowal      Added a test for retrieving a large number of
 *                                     hazards by phensig.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 14, 2013 #1472      bkowal      Removed test for retrieving a large number of
 *                                     hazards. Updates for compatibility with the 
 *                                     Serialization changes. Test hazards will be
 *                                     purged after every test.
 * Apr 23, 2014 3357       bkowal      Temporarily disable a comparison broken by
 *                                     an unrelated change.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public abstract class AbstractHazardStorageTest {

    private final String site = "kxxx";

    private final String phen = "ZO"; // zombies!

    private final String sig = "P"; // apocalypse!

    private final HazardStatus state = HazardStatus.POTENTIAL;

    private final TimeUnit time = TimeUnit.DAYS;

    private final Date date = new Date(System.currentTimeMillis()
            - time.convert(365, TimeUnit.MILLISECONDS)); // go back a year from
                                                         // today, so we
                                                         // don't overlap

    private final ProductClass clazz = ProductClass.OPERATIONAL;

    private final Coordinate coordinate = new Coordinate(10, 10);

    private static final String THRIFT_STREAM_MAXSIZE = "200";

    private static final String EDEX_HOME = "/awips2/edex";

    public IHazardEventManager manager = new HazardEventManager(getMode());

    private List<HazardEvent> createdHazardEvents;

    @Before
    public void setUp() {
        DeployTestProperties.getInstance();
        System.setProperty("thrift.stream.maxsize", THRIFT_STREAM_MAXSIZE);
        System.setProperty("edex.home", EDEX_HOME);
        this.createdHazardEvents = new ArrayList<HazardEvent>();
    }

    public HazardEvent createNewEvent() {
        HazardEvent createdEvent = manager.createEvent();
        createdEvent.setEventID(UUID.randomUUID().toString());
        createdEvent.setEndTime(date);
        createdEvent.setHazardMode(clazz);
        createdEvent.setCreationTime(date);
        createdEvent.setPhenomenon(phen);
        createdEvent.setSignificance(sig);
        createdEvent.setSiteID(site);
        createdEvent.setStatus(state);
        createdEvent.setStartTime(date);
        createdEvent.setSubType("Biohazard");

        GeometryFactory factory = new GeometryFactory();
        Geometry geometry = factory.createPoint(coordinate);
        createdEvent.setGeometry(AdvancedGeometryUtilities
                .createGeometryWrapper(geometry, 0));
        return createdEvent;
    }

    private HazardEvent storeEvent() {
        HazardEvent createdEvent = createNewEvent();
        return this.storeEvent(createdEvent);
    }

    private HazardEvent storeEvent(HazardEvent hazardEvent) {
        boolean stored = manager.storeEvents(hazardEvent);
        assertTrue("Not able to store event", stored);
        this.createdHazardEvents.add(hazardEvent);
        return hazardEvent;
    }

    private boolean removeEvent(HazardEvent hazardEvent) {
        return manager.removeEvents(hazardEvent);
    }

    /**
     * 
     */
    @Test
    public void testByEventId() {
        IHazardEvent createdEvent = storeEvent();
        HazardHistoryList list = manager.getHistoryByEventID(
                createdEvent.getEventID(), true);
        assertThat(list, hasSize(1));
        /*
         * the persist time attribute currently breaks the step below. the
         * persist time attribute is added just before the hazard is stored.
         * however, it is not yet readily available in the hazard that is
         * returned from the storage step.
         */
        // assertEquals(list.get(0), createdEvent);
    }

    @Test
    public void testByGeometry() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager
                .getHistoryByGeometry(
                        AdvancedGeometryUtilities
                                .getJtsGeometryAsCollection(createdEvent
                                        .getGeometry()), true);
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
        Map<String, HazardHistoryList> list = manager.getHistoryBySiteID(site, true);
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
        Map<String, HazardHistoryList> list = manager.getHistoryByPhenomenon(phen,
                true);
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
        Map<String, HazardHistoryList> list = manager.getHistoryBySignificance(sig,
                true);
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
        Map<String, HazardHistoryList> list = manager.getHistoryByPhenSig(phen, sig,
                true);
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
        Map<String, HazardHistoryList> list = manager.getHistoryByTime(date, date,
                true);
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testRemove() {
        HazardEvent createdEvent = storeEvent();
        boolean tf = manager.removeEvents(createdEvent);
        // special case - remove event from the list of created events
        this.createdHazardEvents.remove(createdEvent);
        assertTrue(tf);
        HazardHistoryList list = manager.getHistoryByEventID(
                createdEvent.getEventID(), true);
        assertThat(list, hasSize(0));
    }

    @Test
    public void testUpdateTime() {
        HazardEvent createdEvent = storeEvent();
        Date newTime = new Date();
        createdEvent.setCreationTime(newTime);
        boolean tf = manager.updateEvents(createdEvent);
        assertTrue(tf);
        HazardHistoryList list = manager.getHistoryByEventID(
                createdEvent.getEventID(), true);
        assertThat(createdEvent.getEventID(), list, hasSize(1));
    }

    @Test
    public void testByMultipleSite() {
        IHazardEvent createdEvent = storeEvent();
        HazardQueryBuilder builder = new HazardQueryBuilder();
        // get by kxxx OR koax
        builder.addKey(HazardConstants.SITE_ID, site);
        builder.addKey(HazardConstants.SITE_ID, "koax");
        Map<String, HazardHistoryList> list = manager
                .getHistoryByFilter(builder.getQuery());
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
        builder.addKey(HazardConstants.HAZARD_EVENT_START_TIME,
                createdEvent.getStartTime());
        builder.addKey(HazardConstants.HAZARD_EVENT_START_TIME,
                createdEvent.getStartTime());
        Map<String, HazardHistoryList> list = manager
                .getHistoryByFilter(builder.getQuery());
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
        builder.addKey(HazardConstants.HAZARD_EVENT_END_TIME,
                createdEvent.getEndTime());
        builder.addKey(HazardConstants.HAZARD_EVENT_END_TIME,
                createdEvent.getEndTime());
        Map<String, HazardHistoryList> list = manager
                .getHistoryByFilter(builder.getQuery());
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
        Map<String, HazardHistoryList> list = manager
                .getHistoryByFilter(builder.getQuery());
        assertTrue("No events returned", list.isEmpty() == false);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventID())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Ignore
    @Test
    public void testByMultiplePhensig() {
        List<String> phensigs = new ArrayList<String>();
        HazardEvent event = storeEvent();

        event = createNewEvent();
        event.setPhenomenon("ZW");
        event.setSubType("Convective");
        this.storeEvent(event);

        event = createNewEvent();
        event.setPhenomenon("ZW");
        event.setSignificance("D");
        this.storeEvent(event);

        event = createNewEvent();
        event.setPhenomenon("TL");
        event.setSignificance("D");
        this.storeEvent(event);

        phensigs.add("TL.D");
        phensigs.add("ZW.P.Convective");
        Map<String, HazardHistoryList> list = manager
                .getByMultiplePhensigs(phensigs);
        // should get 2 back
        assertThat(list.keySet(), hasSize(2));
    }

    @After
    public void purgeTestHazardEvents() {
        for (HazardEvent hazardEvent : this.createdHazardEvents) {
            this.removeEvent(hazardEvent);
        }
        this.createdHazardEvents.clear();
    }

    abstract Mode getMode();
}