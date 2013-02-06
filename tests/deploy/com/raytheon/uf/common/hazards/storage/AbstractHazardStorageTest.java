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

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.edex.ebxml.registry.RegistryManagerDeployTest;
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
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

@Ignore
public abstract class AbstractHazardStorageTest {

    private final String site = "koax";

    private final String phen = "FF";

    private final String sig = "W";

    private final HazardState state = HazardState.POTENTIAL;

    private final Date date = new Date();

    private final ProductClass clazz = ProductClass.OPERATIONAL;

    private final Coordinate coordinate = new Coordinate(10, 10);

    private final HazardEventManager manager = new HazardEventManager(getMode());

    @Before
    public void setUp() {
        RegistryManagerDeployTest.setDeployInstance();
    }

    private IHazardEvent storeEvent() {
        IHazardEvent createdEvent = manager.createEvent();
        createdEvent.setEndTime(date);
        createdEvent.setHazardMode(clazz);
        createdEvent.setIssueTime(date);
        createdEvent.setPhenomenon(phen);
        createdEvent.setSignificance(sig);
        createdEvent.setSite(site);
        createdEvent.setState(state);
        createdEvent.setStartTime(date);

        GeometryFactory factory = new GeometryFactory();
        Geometry geometry = factory.createPoint(coordinate);
        createdEvent.setGeometry(geometry);
        manager.storeEvent(createdEvent);
        return createdEvent;
    }

    /**
     * 
     */
    @Test
    public void testByEventId() {
        IHazardEvent createdEvent = storeEvent();
        HazardHistoryList list = manager
                .getByEventId(createdEvent.getEventId());
        assertThat(list, hasSize(1));
        assertEquals(createdEvent, list.get(0));
    }

    @Test
    public void testByGeometry() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager
                .getByGeometry(createdEvent.getGeometry());
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventId())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testBySite() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getBySite(site);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventId())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByPhenomenon() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getByPhenomenon(phen);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventId())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testBySignificance() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getBySignificance(sig);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventId())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByPhensig() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getByPhensig(phen, sig);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventId())) {
                assertEquals(list.get(eId).get(0), createdEvent);
            }
        }
    }

    @Test
    public void testByTime() {
        IHazardEvent createdEvent = storeEvent();
        Map<String, HazardHistoryList> list = manager.getByTime(date, date);
        for (String eId : list.keySet()) {
            if (list.get(eId).equals(createdEvent.getEventId())) {
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
                .getByEventId(createdEvent.getEventId());
        assertThat(list, hasSize(0));
    }

    @Test
    public void testUpdate() {
        String modPhen = "FW";
        IHazardEvent createdEvent = storeEvent();
        createdEvent.setPhenomenon(modPhen);
        boolean tf = manager.updateEvent(createdEvent);
        assertTrue(tf);
        HazardHistoryList list = manager
                .getByEventId(createdEvent.getEventId());
        assertThat(list, hasSize(1));
        assertEquals(list.get(0), createdEvent);
    }

    abstract Mode getMode();
}
