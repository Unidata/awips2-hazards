/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;

import java.util.Collection;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

/**
 * Description: {@link FunctionalTest} of the hazard conflict detection.
 * 
 * Tests hazard conflict detection.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 14, 2013 2166       blawrenc    Created functional test for 
 *                                     hazard conflict detection.
 * Apr 23, 2014 3357       bkowal      No longer error on an unexpected state. Just continue
 *                                     without modifying any data or the state.
 * Apr 29, 2014 2925       Chris.Golden Fixed to work with new HID event propagation.
 * May 18, 2014 2925       Chris.Golden More changes to get it to work with the new HID.
 *                                      Also changed to ensure that ongoing preview and
 *                                      ongoing issue flags are set to false at the end
 *                                      of each test, and moved the steps enum into the
 *                                      base class.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class HazardConflictFunctionalTest extends
        FunctionalTest<HazardConflictFunctionalTest.Steps> {

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    private static final double SECOND_EVENT_CENTER_Y = 41.0;

    private static final double SECOND_EVENT_CENTER_X = -96.0;

    /**
     * Steps defining this test.
     */
    protected enum Steps {
        TOGGLE_ON_HAZARD_DETECTION, CREATE_FIRST_HAZARD_AREA, ASSIGN_AREAL_FLOOD_WATCH, CREATE_SECOND_HAZARD_AREA, ASSIGN_FLASH_FLOOD_WATCH, CHECK_CONFLICTS, TOGGLE_OFF_HAZARD_DETECTION
    }

    /**
     * The identifier of the first (FA.A) event created.
     */
    private String firstEventID;

    public HazardConflictFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        try {
            super.run();

        } catch (Exception e) {
            handleException(e);
        }

    }

    @Override
    protected void runFirstStep() {

        /*
         * Simulate the forecaster turning 'on' hazard conflict detection via
         * the Console view menu.
         */
        this.step = Steps.TOGGLE_ON_HAZARD_DETECTION;
        ConsoleAction consoleAction = new ConsoleAction(
                ConsoleAction.ActionType.CHANGE_MODE,
                ConsoleAction.AUTO_CHECK_CONFLICTS);
        eventBus.publishAsync(consoleAction);
    }

    /**
     * Listens for console actions occurring within Hazard Services. Calls the
     * appropriate tests based on the current test step.
     * 
     * @param action
     *            Contains the tool action sent over the Event Bus by Hazard
     *            Services
     * @return
     */
    @Handler(priority = -1)
    public void consoleActionOccurred(final ConsoleAction action) {
        try {
            if (action.getId().equals(ConsoleAction.AUTO_CHECK_CONFLICTS)) {

                if (this.step == Steps.TOGGLE_ON_HAZARD_DETECTION) {
                    /*
                     * Make sure that auto hazard checking really is 'on'
                     */
                    assertTrue(appBuilder.getSessionManager()
                            .isAutoHazardCheckingOn());

                    /*
                     * Create the first hazard.
                     */
                    stepCompleted();
                    this.step = Steps.CREATE_FIRST_HAZARD_AREA;
                    autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                            FIRST_EVENT_CENTER_Y);
                } else {
                    assertTrue(!appBuilder.getSessionManager()
                            .isAutoHazardCheckingOn());
                    stepCompleted();
                    testSuccess();
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Listens for spatial display actions generated from within Hazard
     * Services. Performs the appropriate tests based on the current test step.
     * 
     * @param action
     * @return
     */
    @Handler(priority = -1)
    public void sessionEventAddedOccurred(SessionEventAdded action) {

        try {
            switch (step) {
            case CREATE_FIRST_HAZARD_AREA:
                stepCompleted();
                this.step = Steps.ASSIGN_AREAL_FLOOD_WATCH;
                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);
                break;

            case CREATE_SECOND_HAZARD_AREA:
                stepCompleted();
                this.step = Steps.ASSIGN_FLASH_FLOOD_WATCH;
                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);
                break;

            default:
                // Do Nothing.
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void sessionEventModifiedOccurred(SessionEventModified action) {
        try {
            if (step == Steps.ASSIGN_AREAL_FLOOD_WATCH) {
                ObservedHazardEvent event = autoTestUtilities
                        .getSelectedEvent();
                if (!"FA".equals(event.getPhenomenon())
                        || !"A".equals(event.getSignificance())) {
                    return;
                }

                checkArealFloodWatch();

                /*
                 * Create the second event.
                 */
                stepCompleted();
                this.step = Steps.CREATE_SECOND_HAZARD_AREA;
                autoTestUtilities.createEvent(SECOND_EVENT_CENTER_X,
                        SECOND_EVENT_CENTER_Y);
            } else if (step == Steps.ASSIGN_FLASH_FLOOD_WATCH) {
                ObservedHazardEvent event = autoTestUtilities
                        .getSelectedEvent();
                if (!"FF".equals(event.getPhenomenon())
                        || !"A".equals(event.getSignificance())) {
                    return;
                }
                stepCompleted();
                this.step = Steps.CHECK_CONFLICTS;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void sessionSelectedEventConflictsModifiedOccurred(
            SessionSelectedEventConflictsModified event) {
        try {
            if (step == Steps.CHECK_CONFLICTS) {
                checkHazardConflicts();
                stepCompleted();
                this.step = Steps.TOGGLE_OFF_HAZARD_DETECTION;
                ConsoleAction consoleAction = new ConsoleAction(
                        ConsoleAction.ActionType.CHANGE_MODE,
                        ConsoleAction.AUTO_CHECK_CONFLICTS);
                eventBus.publishAsync(consoleAction);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Tests the contents of the Flood Watch hazard event and the information
     * sent to the Hazard Information dialog.
     * 
     * @param
     * @return
     */
    private void checkArealFloodWatch() {
        /*
         * Retrieve the selected event.
         */
        Collection<ObservedHazardEvent> selectedEvents = appBuilder
                .getSessionManager().getEventManager().getSelectedEvents();

        assertTrue(selectedEvents.size() == 1);

        IHazardEvent selectedEvent = selectedEvents.iterator().next();

        firstEventID = selectedEvent.getEventID();

        assertTrue(selectedEvent.getPhenomenon().equals("FA"));
        assertTrue(selectedEvent.getSignificance().equals("A"));
    }

    /**
     * Tests the hazard conflicts created by the generation of the hazard
     * events.
     * 
     * @param
     * @return
     */
    private void checkHazardConflicts() {
        /*
         * Retrieve the selected event.
         */
        Collection<ObservedHazardEvent> selectedEvents = appBuilder
                .getSessionManager().getEventManager().getSelectedEvents();

        /*
         * There one event selected, the FF.A
         */
        assertTrue(selectedEvents.size() == 1);

        /*
         * Retrieve the eventID of the selected event
         */
        String selectedEventID = selectedEvents.iterator().next().getEventID();

        /*
         * Check the conflict information.
         */
        Map<String, Collection<IHazardEvent>> hazardConflictMap = appBuilder
                .getSessionManager().getEventManager()
                .getConflictingEventsForSelectedEvents();

        assertTrue(hazardConflictMap.size() == 1);
        assertTrue(hazardConflictMap.containsKey(selectedEventID));

        Collection<IHazardEvent> conflictingHazards = hazardConflictMap
                .get(selectedEventID);
        assertTrue(conflictingHazards.size() == 1);
        String conflictingEventID = conflictingHazards.iterator().next()
                .getEventID();
        assertTrue(conflictingEventID.equals(firstEventID));

    }

}
