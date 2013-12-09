package com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import gov.noaa.gsd.common.utilities.Utils;

import org.joda.time.DateTime

import spock.lang.*

import com.google.common.eventbus.EventBus
import com.raytheon.uf.common.colormap.Color
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.InMemoryHazardEventManager
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent
import com.raytheon.uf.common.serialization.JAXBManager
import com.raytheon.uf.common.time.SimulatedTime
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertState
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationConsoleTimer
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationPopUpAlert
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationSpatialTimer
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardEventExpirationAlertConfigCriterion
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender
import com.raytheon.uf.viz.hazards.sessionmanager.impl.SessionNotificationSender
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager

/**
 *
 * Description: Tests {@link SessionAlertsManager}
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 12, 2013  1325     daniel.s.schaffer@noaa.gov    Initial creation
 * Nov 20, 2013  2159     daniel.s.schaffer@noaa.gov    Now interoperable with DRT
 * Nov 25, 2013  2336      Chris.Golden                 Altered to handle new
 *                                                      location of utility
 *                                                      classes.
 * Dec 02, 2013  1472      bkowal      Fix junit test
 * </pre>
 *
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
class SessionAlertsManagerTest extends spock.lang.Specification {

    IHazardEventManager hazardEventManager;
    ISessionTimeManager sessionTimeManager;
    HazardSessionAlertsManager alertsManager;
    ISessionConfigurationManager sessionConfigurationManager;
    EventBus eventBus;
    HazardEventForAlertsTesting event0

    static String EVENT_O = "event_0"
    static String EVENT_1 = "event_1"

    /**
     * Sets up this test.
     */
    def setup() {

        hazardEventManager = new InMemoryHazardEventManager()
        event0 = buildHazardEvent(EVENT_O,
                new DateTime(2013, 7, 25, 15, 0, 0, 0))

        hazardEventManager.storeEvent(event0)

        mockSessionTimeManager(new DateTime(2013, 7, 25, 14, 52, 0, 0))

        mockConfigurationManager()


        eventBus = new EventBus()
        ISessionNotificationSender sender = new SessionNotificationSender(
                eventBus);
        alertsManager = new HazardSessionAlertsManager(sender, sessionTimeManager)

        INotificationHandler notificationHandler = mock(INotificationHandler.class)

        alertsManager.setNotificationHandler(notificationHandler)
        alertsManager.setAlertJobFactory(new HazardAlertJobFactoryForTesting())
        alertsManager.addAlertGenerationStrategy(HazardNotification.class,
                new HazardEventExpirationAlertStrategy(alertsManager,
                sessionTimeManager, sessionConfigurationManager, hazardEventManager,
                new AllHazardsFilterStrategy()));

        alertsManager.start()
    }


    /**
     * Cleans up any resources used by this test.
     */
    def cleanup() {
        alertsManager.shutdown()
    }

    def "An event with superceded alerts is issued"() {

        when: "The hazard is issued"
        HazardEventForAlertsTesting hazardEvent = buildHazardEvent(EVENT_1,
                new DateTime(2013, 7, 25, 15, 0, 0, 0))
        hazardEvent.setPhenomenon("SV")
        hazardEvent.setSubType(null)

        HazardNotification hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        List<IHazardAlert> scheduledAlerts = alertsManager.getScheduledAlerts()

        then: "The proper alerts are active"
        activeAlerts.size() == 4
    }

    def "The proper alerts are created upon initialization"() {
        when: "get the active alerts"
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        HazardEventExpirationSpatialTimer alert = activeAlerts.get(0)

        then: "The proper alert is created"
        activeAlerts.size() == 1
        alert.getEventID() == EVENT_O
        alert.getState() == HazardAlertState.ACTIVE
        alert.getActivationTime() == new DateTime(2013, 7, 25, 14, 50, 0, 0).toDate()
        alert.getColor() == new Color(1, 1, 0);
        alert.isBold()
        !alert.isItalic()
        alert.isBlinking()
        alert.getHazardExpiration() == new DateTime(2013, 7, 25, 15, 0, 0, 0).toDate()
    }

    def "A hazard for which there is an alert is changed in an innocuous way"() {
        when: "The hazard is changed"
        event0.addHazardAttribute("foo", "bar")
        HazardNotification hazardNotification = new HazardNotification(event0, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        List<IHazardAlert> scheduledAlerts = alertsManager.getScheduledAlerts()

        then: "No scheduled or new active alerts"
        scheduledAlerts.size() == 0
        activeAlerts.size() == 1
    }

    def "The expirationTime for a hazard for which there are alerts is changed"() {
        when: "The expirationTime is changed"
        IHazardEvent modifiedEvent = buildHazardEvent(EVENT_O,
                new DateTime(2013, 7, 25, 16, 1, 0, 0))
        HazardNotification hazardNotification = new HazardNotification(modifiedEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        List<IHazardAlert> scheduledAlerts = alertsManager.getScheduledAlerts()

        then: "The active alert is canceled"
        activeAlerts.size() == 0

        and: "A new alert is scheduled"
        scheduledAlerts.size() == 1
    }

    def "A hazard event is issued that schedules an alert in the future"() {

        when: "The hazard is issued"

        HazardEventForAlertsTesting hazardEvent = buildHazardEvent(EVENT_1,
                new DateTime(2013, 7, 25, 16, 1, 0, 0))

        HazardNotification hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        List<IHazardAlert> scheduledAlerts = alertsManager.getScheduledAlerts()
        HazardEventExpirationSpatialTimer alert = scheduledAlerts.get(0)

        then: "The proper alert is scheduled but active alerts are unchanged"
        scheduledAlerts.size() == 1
        alert.getEventID() == EVENT_1

        activeAlerts.size() == 1

        when: "The hazard is canceled"
        hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.DELETE)
        alertsManager.handleNotification(hazardNotification)
        scheduledAlerts = alertsManager.getScheduledAlerts()

        then: "The scheduled alert is removed"
        scheduledAlerts.size() == 0
    }

    def "A hazard event is issued that schedules a console and spatial count-down timers and a pop-up alert"() {

        when: "The hazard is issued"

        HazardEventForAlertsTesting hazardEvent = buildHazardEvent(EVENT_1,
                new DateTime(2013, 7, 25, 16, 1, 0, 0))
        hazardEvent.setPhenomenon("SV")
        hazardEvent.setSubType(null)

        HazardNotification hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        List<IHazardAlert> scheduledAlerts = alertsManager.getScheduledAlerts()
        HazardEventExpirationSpatialTimer spatialAlert = scheduledAlerts.get(0)
        HazardEventExpirationConsoleTimer consoleAlert = scheduledAlerts.get(1)
        HazardEventExpirationPopUpAlert popupAlert = scheduledAlerts.get(2)

        then: "The proper alerts are scheduled"
        scheduledAlerts.size() == 4
        consoleAlert.getEventID() == EVENT_1
        spatialAlert.getEventID() == EVENT_1
        popupAlert.getEventID() == EVENT_1

        and: "The immediate alert is activated"
        activeAlerts.size() == 2
    }

    def "A hazard event is issued that triggers a spatial alert immediately"() {

        when: "The hazard is issued"

        HazardEventForAlertsTesting hazardEvent = buildHazardEvent(EVENT_1,
                new DateTime(2013, 7, 25, 15, 1, 0, 0))
        hazardEvent.setPhenomenon("FL")
        hazardEvent.setSubType(null)

        HazardNotification hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        HazardEventExpirationSpatialTimer alert = activeAlerts.get(1)

        then: "The proper alert is activated"
        activeAlerts.size() == 2
        alert.getEventID() == EVENT_1
        alert.getFontSize() == 12
    }

    def "A hazard event is issued that triggers a pop-up alert immediately and a 2nd one later"() {

        when: "The hazard is issued"

        HazardEventForAlertsTesting hazardEvent = buildHazardEvent(EVENT_1,
                new DateTime(2013, 7, 25, 15, 1, 0, 0))
        hazardEvent.setPhenomenon("FA")
        hazardEvent.setSubType(null)

        HazardNotification hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        HazardEventExpirationPopUpAlert alert = activeAlerts.get(1)
        List<IHazardAlert> scheduledAlerts = alertsManager.getScheduledAlerts()

        then: "The proper alerts are activated/scheduled"
        activeAlerts.size() == 2
        alert.getEventID() == EVENT_1
        scheduledAlerts.size() == 1
    }

    def "A hazard event is issued that does not initially result in an alert"() {

        when: "The hazard is issued"

        HazardEventForAlertsTesting hazardEvent = buildHazardEvent(EVENT_1,
                new DateTime(2013, 7, 25, 15, 0, 0, 0))
        hazardEvent.setPhenomenon("WS")

        HazardNotification hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()
        List<IHazardAlert> scheduledAlerts = alertsManager.getScheduledAlerts()

        then: "No scheduled or new active alerts"
        scheduledAlerts.size() == 0
        activeAlerts.size() == 1

        when: "The hazard is changed to one that causes an alert"
        hazardEvent = buildHazardEvent(EVENT_1,
                new DateTime(2013, 7, 25, 15, 0, 0, 0))
        hazardNotification = new HazardNotification(hazardEvent, HazardNotification.NotificationType.STORE)
        alertsManager.handleNotification(hazardNotification)
        activeAlerts = alertsManager.getActiveAlerts()

        then: "The alert is activated"
        activeAlerts.size() == 2
    }

    def "A hazard event corresponding to an active alert is ended"() {

        when: "The hazard is ended"

        HazardNotification hazardNotification = new HazardNotification(event0, HazardNotification.NotificationType.STORE)
        event0.setState(HazardState.ENDED);
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()

        then: "The alert is canceled"
        activeAlerts.size() == 0
    }

    def "A hazard event corresponding to an active alert is deleted"() {

        when: "The hazard is deleted"

        HazardNotification hazardNotification = new HazardNotification(event0, HazardNotification.NotificationType.DELETE)
        alertsManager.handleNotification(hazardNotification)
        List<IHazardAlert> activeAlerts = alertsManager.getActiveAlerts()

        then: "The alert is canceled"
        activeAlerts.size() == 0
    }

    def "Pop-up messages are correct"() {
        SimulatedTime systemTime = SimulatedTime.getSystemTime();
        DateTime currentTime = new DateTime(2013, 1, 1, 12, 0, 0, 0)
        systemTime.setFrozen(true)
        systemTime.setTime(currentTime.getMillis())
        HazardEventExpirationAlertConfigCriterion criterion = new HazardEventExpirationAlertConfigCriterion()
        HazardEventExpirationPopUpAlert alert = new HazardEventExpirationPopUpAlert(EVENT_1, criterion)
        alert.setSystemTime(systemTime)

        when: "1 day or more"
        alert.setHazardExpiration(new DateTime(2013, 1, 2, 12, 0, 0, 0).toDate())

        then: "Message is expressed in day ranges"
        alert.getText() == "A product generated by event event_1 will expire in 1 to 2 days"

        when: "One hour exactly"
        alert.setHazardExpiration(new DateTime(2013, 1, 1, 13, 0, 0, 0).toDate())

        then: "Message is expressed in hours and minutes"
        alert.getText().contains("in 1 hours and 0 minutes")

        when: "More than one hour"
        alert.setHazardExpiration(new DateTime(2013, 1, 1, 13, 1, 0, 0).toDate())

        then: "Message is expressed in hours and minutes"
        alert.getText().contains("in 1 hours and 1 minutes")

        when: "1 minute exactly"
        alert.setHazardExpiration(new DateTime(2013, 1, 1, 12, 1, 0, 0).toDate())

        then: "Message is expressed in minutes"
        alert.getText().contains("in 1 minutes")

        when: "More than one minute"
        alert.setHazardExpiration(new DateTime(2013, 1, 1, 12, 2, 0, 0).toDate())

        then: "Message is expressed in minutes"
        alert.getText().contains("in 2 minutes")

        when: "seconds left"
        alert.setHazardExpiration(new DateTime(2013, 1, 1, 12, 0, 59, 0).toDate())

        then: "Message is expressed in seconds"
        alert.getText().contains("in 59 seconds")
    }

    def "Activation time based on percent completion"() {
        when: "The criteria is 25% through event completion"
        HazardEventExpirationAlertFactory factory = new HazardEventExpirationAlertFactory(sessionTimeManager);
        HazardEventExpirationAlertConfigCriterion criterion = mock(HazardEventExpirationAlertConfigCriterion.class)
        when(criterion.getUnits()).thenReturn(HazardEventExpirationAlertConfigCriterion.Units.PERCENT)
        when(criterion.getExpirationTime()).thenReturn(25L)

        then: "The activationTime is 25% between the current time and the expiration time"
        Date activationTime = factory.computeActivationTime(criterion, event0)
        activationTime == new DateTime(2013, 7, 25, 14, 54, 0, 0).toDate()
    }



    private HazardEventForAlertsTesting buildHazardEvent(String eventID, DateTime dateTime) {
        HazardEventForAlertsTesting hazardEvent = new HazardEventForAlertsTesting()
        hazardEvent.setEventID(eventID)
        hazardEvent.setPhenomenon("FF")
        hazardEvent.setSignificance("W")
        hazardEvent.setSubType("NonConvective")
        hazardEvent.setState(HazardState.ISSUED)
        hazardEvent.addHazardAttribute(HazardConstants.EXPIRATIONTIME, dateTime.getMillis())
        return hazardEvent
    }


    private mockSessionTimeManager(DateTime dateTime) {
        sessionTimeManager = mock(ISessionTimeManager.class)
        when(sessionTimeManager.getCurrentTime()).thenReturn(dateTime.toDate())
    }

    private mockConfigurationManager() {
        String path = getClass().getResource("HazardAlertsConfig.xml").path;
        String xmlData = Utils.textFileAsString(path);
        JAXBManager jaxbManager = new JAXBManager(HazardAlertsConfig.class);
        HazardAlertsConfig config = jaxbManager.unmarshalFromXml(xmlData)
        sessionConfigurationManager = mock(ISessionConfigurationManager.class)

        when(sessionConfigurationManager.getAlertConfig()).thenReturn(config)
    }
}
