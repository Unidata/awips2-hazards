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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification.NotificationType;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionHazardNotificationListener;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Tests for SessionHazardNotification
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionHazardNotificationListenerTest {

    private static final String TEST_EVENT_ID = "TestEventId";

    private static final String TEST_ATTR_KEY = "TestKey";

    private static final String TEST_ATTR_VAL1 = "TestVal1";

    private static final String TEST_ATTR_VAL2 = "TestVal1";

    private static final String TEST_PHEN1 = "TestPhen1";

    private static final String TEST_PHEN2 = "TestPhen2";

    private SimpleSessionEventManager eventManager = new SimpleSessionEventManager();

    private SessionHazardNotificationListener listener;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Before
    public void init() {
        eventManager = new SimpleSessionEventManager();
        listener = new SessionHazardNotificationListener(eventManager, false);
    }

    private IHazardEvent getDummyEvent() {
        IHazardEvent event = new BaseHazardEvent();
        event.setEventID(TEST_EVENT_ID);
        event.setPhenomenon(TEST_PHEN1);
        event.setGeometry(geometryFactory.createPoint(new Coordinate()));

        return event;
    }

    /**
     * When a new event is created it should be added to the event manager.
     */
    @Test
    public void testNewEvent() {
        eventManager.reset();

        listener.handleNotification(new HazardNotification(getDummyEvent(),
                NotificationType.STORE, Mode.PRACTICE));

        Assert.assertNotNull(eventManager.getEventById(TEST_EVENT_ID));
    }

    /**
     * When an event is updated the attributes should reflect the update.
     */
    @Test
    public void testUpdate() {
        eventManager.reset();
        eventManager.addEvent(getDummyEvent(), null);

        IHazardEvent event = getDummyEvent();
        event.setPhenomenon(TEST_PHEN2);

        listener.handleNotification(new HazardNotification(event,
                NotificationType.UPDATE, Mode.PRACTICE));

        Assert.assertEquals(eventManager.getEventById(TEST_EVENT_ID)
                .getPhenomenon(), TEST_PHEN2);
    }

    /**
     * When an existing event is stored again, it should behave like an update.
     */
    @Test
    public void testRestore() {
        eventManager.reset();

        IHazardEvent event = eventManager.addEvent(getDummyEvent(), null);
        event.addHazardAttribute(HAZARD_EVENT_SELECTED, true);

        listener.handleNotification(new HazardNotification(getDummyEvent(),
                NotificationType.STORE, Mode.PRACTICE));

        Assert.assertEquals(eventManager.getEventById(TEST_EVENT_ID)
                .getHazardAttribute(HAZARD_EVENT_SELECTED), Boolean.TRUE);
    }

    /**
     * When an event is deleted it should be removed from the event manager.
     */
    @Test
    public void testDelete() {
        eventManager.reset();
        eventManager.addEvent(getDummyEvent(), null);

        HazardNotification notification = new HazardNotification(
                getDummyEvent(), NotificationType.DELETE, Mode.PRACTICE);

        listener.handleNotification(notification);

        Assert.assertNull(eventManager.getEventById(TEST_EVENT_ID));
    }

    /**
     * Changes from notification should not affect whether an event is selected
     * or not.
     */
    @Test
    public void testPreserveSelection() {
        eventManager.reset();
        IHazardEvent event = eventManager.addEvent(getDummyEvent(), null);
        event.addHazardAttribute(HAZARD_EVENT_SELECTED, true);

        listener.handleNotification(new HazardNotification(getDummyEvent(),
                NotificationType.UPDATE, Mode.PRACTICE));

        Assert.assertEquals(eventManager.getEventById(TEST_EVENT_ID)
                .getHazardAttribute(HAZARD_EVENT_SELECTED), Boolean.TRUE);
    }

    /**
     * New event attributes should be copied to the event in the event manager
     */
    @Test
    public void testAddAttribute() {
        eventManager.reset();
        eventManager.addEvent(getDummyEvent(), null);

        IHazardEvent event = getDummyEvent();
        event.addHazardAttribute(TEST_ATTR_KEY, TEST_ATTR_VAL2);
        listener.handleNotification(new HazardNotification(event,
                NotificationType.UPDATE, Mode.PRACTICE));

        Assert.assertEquals(eventManager.getEventById(TEST_EVENT_ID)
                .getHazardAttribute(TEST_ATTR_KEY), TEST_ATTR_VAL2);
    }

    /**
     * Deleted event attributes should be removed from the event in the event
     * manager
     */
    @Test
    public void testRemoveAttribute() {
        eventManager.reset();
        IHazardEvent event = eventManager.addEvent(getDummyEvent(), null);
        event.addHazardAttribute(TEST_ATTR_KEY, TEST_ATTR_VAL1);

        listener.handleNotification(new HazardNotification(getDummyEvent(),
                NotificationType.UPDATE, Mode.PRACTICE));

        Assert.assertNull(eventManager.getEventById(TEST_EVENT_ID)
                .getHazardAttribute(TEST_ATTR_KEY));
    }

    /**
     * Changed event attributes should be removed from the event in the event
     * manager
     */
    @Test
    public void testChangeAttribute() {
        eventManager.reset();
        IHazardEvent event = eventManager.addEvent(getDummyEvent(), null);
        event.addHazardAttribute(TEST_ATTR_KEY, TEST_ATTR_VAL1);

        event = getDummyEvent();
        event.addHazardAttribute(TEST_ATTR_KEY, TEST_ATTR_VAL2);
        listener.handleNotification(new HazardNotification(event,
                NotificationType.UPDATE, Mode.PRACTICE));

        Assert.assertEquals(eventManager.getEventById(TEST_EVENT_ID)
                .getHazardAttribute(TEST_ATTR_KEY), TEST_ATTR_VAL2);
    }

}
