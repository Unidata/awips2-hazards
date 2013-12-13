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
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.NewHazardAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

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
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class HazardConflictFunctionalTest extends FunctionalTest {

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    private static final double SECOND_EVENT_CENTER_Y = 41.0;

    private static final double SECOND_EVENT_CENTER_X = -96.0;

    /**
     * Steps defining this test.
     */
    private enum Steps {
        TOGGLE_ON_HAZARD_DETECTION, CREATE_FIRST_HAZARD_AREA, ASSIGN_AREAL_FLOOD_WATCH, CREATE_SECOND_HAZARD_AREA, ASSIGN_FLASH_FLOOD_WATCH, TOGGLE_OFF_HAZARD_DETECTION
    }

    /**
     * The current step being tested.
     */
    private Steps step;

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

            /*
             * Simulate the forecaster turning 'on' hazard conflict detection
             * via the Console view menu.
             */
            this.step = Steps.TOGGLE_ON_HAZARD_DETECTION;
            ConsoleAction consoleAction = new ConsoleAction(
                    HazardConstants.CHECK_CONFLICT_ACTION,
                    HazardConstants.AUTO_CHECK_CONFLICTS);
            eventBus.post(consoleAction);
        } catch (Exception e) {
            handleException(e);
        }

    }

    /**
     * Listens for tool actions occurring within Hazard Services. Calls the
     * appropriate tests based on the current test step.
     * 
     * @param action
     *            Contains the tool action sent over the Event Bus by Hazard
     *            Services
     * @return
     */
    @Subscribe
    public void consoleActionOccurred(final ConsoleAction action) {
        if (action.getId().equals(HazardConstants.AUTO_CHECK_CONFLICTS)) {

            if (this.step == Steps.TOGGLE_ON_HAZARD_DETECTION) {
                /*
                 * Make sure that auto hazard checking really is 'on'
                 */
                assertTrue(appBuilder.getSessionManager()
                        .isAutoHazardCheckingOn());

                /*
                 * Create the first hazard.
                 */
                this.step = Steps.CREATE_FIRST_HAZARD_AREA;
                autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                        FIRST_EVENT_CENTER_Y);
            } else {
                assertTrue(!appBuilder.getSessionManager()
                        .isAutoHazardCheckingOn());

                testSuccess();
            }
        }
    }

    /**
     * Listens for spatial display actions generated from within Hazard
     * Services. Performs the appropriate tests based on the current test step.
     * 
     * @param action
     * @return
     */
    @Subscribe
    public void handleNewHazard(NewHazardAction action) {

        try {

            if (this.step == Steps.CREATE_FIRST_HAZARD_AREA) {
                this.step = Steps.ASSIGN_AREAL_FLOOD_WATCH;

                /*
                 * Retrieve the selected event.
                 */
                Collection<IHazardEvent> selectedEvents = appBuilder
                        .getSessionManager().getEventManager()
                        .getSelectedEvents();

                assertTrue(selectedEvents.size() == 1);

                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);
            } else {
                /*
                 * Retrieve the selected event.
                 */
                this.step = Steps.ASSIGN_FLASH_FLOOD_WATCH;

                Collection<IHazardEvent> selectedEvents = appBuilder
                        .getSessionManager().getEventManager()
                        .getSelectedEvents();

                assertTrue(selectedEvents.size() == 1);

                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Listens for actions generated from the Hazard Information Dialog.
     * Performs the appropriate tests based on the current test step.
     * 
     * @param hazardDetailAction
     *            The action originating from the Hazard Information Dialog.
     * @return
     */
    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            String action = hazardDetailAction.getAction();

            if (action.equals(HazardConstants.UPDATE_EVENT_TYPE)) {

                if (step == Steps.ASSIGN_AREAL_FLOOD_WATCH) {

                    checkArealFloodWatch();

                    /*
                     * Create the second event.
                     */
                    this.step = Steps.CREATE_SECOND_HAZARD_AREA;
                    autoTestUtilities.createEvent(SECOND_EVENT_CENTER_X,
                            SECOND_EVENT_CENTER_Y);
                } else if (step == Steps.ASSIGN_FLASH_FLOOD_WATCH) {

                    checkHazardConflicts();

                    this.step = Steps.TOGGLE_OFF_HAZARD_DETECTION;
                    ConsoleAction consoleAction = new ConsoleAction(
                            HazardConstants.CHECK_CONFLICT_ACTION,
                            HazardConstants.AUTO_CHECK_CONFLICTS);
                    eventBus.post(consoleAction);
                }

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
        Collection<IHazardEvent> selectedEvents = appBuilder
                .getSessionManager().getEventManager().getSelectedEvents();

        assertTrue(selectedEvents.size() == 1);

        IHazardEvent selectedEvent = selectedEvents.iterator().next();

        firstEventID = selectedEvent.getEventID();

        assertTrue(selectedEvent.getPhenomenon().equals("FA"));
        assertTrue(selectedEvent.getSignificance().equals("A"));

        /*
         * Check the information passed to the mocked Hazard Information Dialog
         */
        DictList hidContents = mockHazardDetailView.getContents();
        assertTrue(hidContents.size() == 1);

        Dict hidContent = hidContents.getDynamicallyTypedValue(0);
        assertTrue(hidContent.getDynamicallyTypedValue(
                HazardConstants.HAZARD_EVENT_IDENTIFIER).equals(firstEventID));
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
        Collection<IHazardEvent> selectedEvents = appBuilder
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
         * Check the information passed to the mocked Hazard Information Dialog
         */
        DictList hidContents = mockHazardDetailView.getContents();
        assertTrue(hidContents.size() == 1);
        Map<String, Collection<IHazardEvent>> hazardConflictMap = mockHazardDetailView
                .getConflictMap();

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
