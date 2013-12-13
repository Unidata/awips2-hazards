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

import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.*;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.NewHazardAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: {@link FunctionalTest} of the simple hazard story.
 * 
 * This follows the Simple Hazard story with the following exception. Products
 * are issued by pressing the Hazard Information Dialog Issue button, not the
 * Product Editor Issue button.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * Oct 29, 2013 2166       blawrenc    Fleshed out this test.
 * Nov  04, 2013   2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class SimpleHazardStoryFunctionalTest extends FunctionalTest {

    /**
     * Steps defining this test. These follow the Simple Hazard Story.
     */
    private enum Steps {
        CREATE_NEW_HAZARD_AREA, ASSIGN_AREAL_FLOOD_WATCH, PREVIEW_AREAL_FLOOD_WATCH,

        ISSUE_AREAL_FLOOD_WATCH, UPGRADE_TO_AREAL_FLOOD_WARNING,

        PREVIEW_AREAL_FLOOD_WARNING, ISSUE_AREAL_FLOOD_WARNING,

        PREVIEW_FOLLOW_UP_STATEMENT, ISSUE_FOLLOW_UP_STATEMENT,

        PREVIEW_CANCELLATION_STATEMENT, ISSUE_CANCELLATION_STATEMENT
    }

    /**
     * The current step being tested.
     */
    private Steps step;

    /**
     * Keeps track of how many products have been generated. This ensures that
     * this test does not try to query the product editor contents until all
     * products have been created.
     */
    private static int numProducts = 0;

    public SimpleHazardStoryFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        this.step = Steps.CREATE_NEW_HAZARD_AREA;
        Coordinate[] coordinates = autoTestUtilities
                .buildEventArea(-96.0, 41.0);
        IHazardEvent hazardEvent = new HazardEventBuilder(
                appBuilder.getSessionManager())
                .buildPolygonHazardEvent(coordinates);
        NewHazardAction action = new NewHazardAction(hazardEvent);
        eventBus.post(action);
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
    public void toolActionOccurred(final ToolAction action) {

        try {
            switch (action.getAction()) {
            case PRODUCTS_GENERATED:
                if (step.equals(Steps.ISSUE_AREAL_FLOOD_WATCH)) {
                    checkArealFloodWatchIssue();
                } else if (step.equals(Steps.ISSUE_AREAL_FLOOD_WARNING)) {
                    checkArealFloodWarningIssue();
                } else if (step.equals(Steps.ISSUE_FOLLOW_UP_STATEMENT)) {
                    checkFollowUpStatementIssue();
                } else if (step.equals(Steps.ISSUE_CANCELLATION_STATEMENT)) {
                    checkCancellationStatementIssue();
                    testSuccess();
                }

                break;
            case RUN_TOOL:
                break;
            case RUN_TOOL_WITH_PARAMETERS:
                break;
            case TOOL_RECOMMENDATIONS:
                break;
            default:
                break;
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
    @Subscribe
    public void handleNewHazard(NewHazardAction action) {

        try {

            this.step = Steps.ASSIGN_AREAL_FLOOD_WATCH;

            /*
             * Retrieve the selected event.
             */
            Collection<IHazardEvent> selectedEvents = appBuilder
                    .getSessionManager().getEventManager().getSelectedEvents();

            assertTrue(selectedEvents.size() == 1);

            IHazardEvent selectedEvent = selectedEvents.iterator().next();

            assertTrue(selectedEvent.getEventID().length() > 0);

            Dict dict = autoTestUtilities
                    .buildEventTypeSelection(selectedEvent,
                            AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);

            HazardDetailAction hazardDetailAction = new HazardDetailAction(
                    HazardConstants.UPDATE_EVENT_TYPE);
            hazardDetailAction.setJSONText(dict.toJSONString());
            eventBus.post(hazardDetailAction);

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
                    /*
                     * Retrieve the selected event.
                     */
                    Collection<IHazardEvent> selectedEvents = appBuilder
                            .getSessionManager().getEventManager()
                            .getSelectedEvents();

                    assertTrue(selectedEvents.size() == 1);

                    IHazardEvent selectedEvent = selectedEvents.iterator()
                            .next();

                    String eventID = selectedEvent.getEventID();

                    assertTrue(selectedEvent.getPhenomenon().equals("FA"));
                    assertTrue(selectedEvent.getSignificance().equals("A"));

                    /*
                     * Check the information passed to the mocked Hazard
                     * Information Dialog
                     */
                    DictList hidContents = mockHazardDetailView.getContents();
                    assertTrue(hidContents.size() == 1);

                    Dict hidContent = hidContents.getDynamicallyTypedValue(0);
                    assertTrue(hidContent.getDynamicallyTypedValue(
                            HazardConstants.HAZARD_EVENT_IDENTIFIER).equals(
                            eventID));

                    /*
                     * Trigger a preview action
                     */
                    this.step = Steps.PREVIEW_AREAL_FLOOD_WATCH;
                    autoTestUtilities.previewEvent();
                } else if (step == Steps.UPGRADE_TO_AREAL_FLOOD_WARNING) {
                    /*
                     * Retrieve the selected event.
                     */
                    Collection<IHazardEvent> selectedEvents = appBuilder
                            .getSessionManager().getEventManager()
                            .getSelectedEvents();

                    /*
                     * There should be two events selected, the FA.A and the
                     * FA.W
                     */
                    assertTrue(selectedEvents.size() == 2);

                    /*
                     * Check the information passed to the mocked Hazard
                     * Information Dialog
                     */
                    DictList hidContents = mockHazardDetailView.getContents();
                    assertTrue(hidContents.size() == 2);
                    step = Steps.PREVIEW_AREAL_FLOOD_WARNING;
                    autoTestUtilities.previewEvent();

                }

            } else if (action.equals(HazardConstants.HazardAction.PREVIEW
                    .getValue())) {
                assertFalse(mockProductStagingView.isToBeIssued());

            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    /**
     * Listener to handle results from the Product Generation Framework. The
     * appropriate tests are performed based on the current test step.
     * 
     * @param generated
     *            The generated product information from the Product Generation
     *            Framework.
     * @return
     */
    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {
        try {
            if (step.equals(Steps.PREVIEW_AREAL_FLOOD_WATCH)) {
                checkArealFloodWatchPreview();
                step = Steps.ISSUE_AREAL_FLOOD_WATCH;
                autoTestUtilities.issueEvent();
            } else if (step.equals(Steps.ISSUE_AREAL_FLOOD_WATCH)) {
                step = Steps.UPGRADE_TO_AREAL_FLOOD_WARNING;

                /*
                 * Retrieve the selected event.
                 */
                Collection<IHazardEvent> selectedEvents = appBuilder
                        .getSessionManager().getEventManager()
                        .getSelectedEvents();

                assertTrue(selectedEvents.size() == 1);

                IHazardEvent selectedEvent = selectedEvents.iterator().next();

                assertTrue(selectedEvent.getEventID().length() > 0);

                /*
                 * Build the JSON simulating a hazard type selection in the HID.
                 */
                Dict dict = new Dict();
                dict.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                        selectedEvent.getEventID());
                dict.put(HazardConstants.HAZARD_EVENT_FULL_TYPE,
                        AREAL_FLOOD_WARNING_FULLTYPE);

                HazardDetailAction hazardDetailAction = new HazardDetailAction(
                        HazardConstants.UPDATE_EVENT_TYPE);
                hazardDetailAction.setJSONText(dict.toJSONString());
                eventBus.post(hazardDetailAction);
            } else if (step == Steps.PREVIEW_AREAL_FLOOD_WARNING) {
                /*
                 * Need to ignore the first pass through here. Do not query the
                 * produce editor until all products are generated, and they are
                 * generated one at a time.
                 */
                numProducts++;

                if (numProducts == 2) {
                    numProducts = 0;
                    checkArealFloodWarningPreview();
                    step = Steps.ISSUE_AREAL_FLOOD_WARNING;
                    autoTestUtilities.issueEvent();
                }
            } else if (step == Steps.ISSUE_AREAL_FLOOD_WARNING) {

                /*
                 * Fire off another preview action.
                 */
                step = Steps.PREVIEW_FOLLOW_UP_STATEMENT;
                autoTestUtilities.previewEvent();
            } else if (step == Steps.PREVIEW_FOLLOW_UP_STATEMENT) {
                /*
                 * Need to ignore the first pass through here. For some reason
                 * this is just the FLW product the first time through...
                 */
                numProducts++;

                if (numProducts == 2) {
                    numProducts = 0;
                    checkFollowUpStatementPreview();
                    step = Steps.ISSUE_FOLLOW_UP_STATEMENT;
                    autoTestUtilities.issueEvent();
                }
            } else if (step == Steps.ISSUE_FOLLOW_UP_STATEMENT) {

                /*
                 * End the selected event.
                 */
                step = Steps.PREVIEW_CANCELLATION_STATEMENT;
                SpatialDisplayAction spatialAction = new SpatialDisplayAction(
                        HazardConstants.CONEXT_MENU_SELECTED, 0,
                        HazardConstants.END_SELECTED_HAZARDS);
                eventBus.post(spatialAction);
            } else if (step == Steps.PREVIEW_CANCELLATION_STATEMENT) {
                checkCancellationStatementPreview();
                step = Steps.ISSUE_CANCELLATION_STATEMENT;
                autoTestUtilities.issueEvent();
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
     * Tests the contents of the Flood Watch product during a Preview action.
     * 
     * @param
     * @return
     */
    private void checkArealFloodWatchPreview() {
        Dict productInfo = mockProductEditorView.getProductInfo();
        List<?> generatedProducts = (ArrayList<?>) productInfo
                .get(HazardConstants.GENERATED_PRODUCTS);
        assertEquals(generatedProducts.size(), 1);
        Dict generatedProduct = (Dict) generatedProducts.get(0);
        String productId = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCT_ID);
        assertEquals(productId, FLOOD_WATCH_PRODUCT_ID);
        Dict products = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
        String legacy = products
                .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
        assertTrue(legacy.contains(NEW_VTEC_STRING));
        assertTrue(legacy.contains(AREAL_FLOOD_WATCH));
        String xml = products
                .getDynamicallyTypedValue(ProductConstants.XML_PRODUCT_KEY);
        assertTrue(xml.contains(AREAL_FLOOD_WATCH));
        String cap = products
                .getDynamicallyTypedValue(ProductConstants.CAP_PRODUCT_KEY);
        assertTrue(cap.contains(AREAL_FLOOD_WATCH));
    }

    /**
     * Tests the contents of the Flood Warning product during a Preview action.
     * 
     * @param
     * @return
     */
    private void checkArealFloodWarningPreview() {
        Dict productInfo = mockProductEditorView.getProductInfo();
        @SuppressWarnings("unchecked")
        List<Dict> generatedProducts = (List<Dict>) productInfo
                .get(HazardConstants.GENERATED_PRODUCTS);
        assertEquals(generatedProducts.size(), 2);

        for (Dict generatedProduct : generatedProducts) {

            String productId = generatedProduct
                    .getDynamicallyTypedValue(ProductConstants.PRODUCT_ID);

            if (productId.equals(FLOOD_WATCH_PRODUCT_ID)) {
                Dict products = generatedProduct
                        .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
                String legacy = products
                        .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
                assertTrue(legacy.contains(CAN_VTEC_STRING));
            } else if (productId.equals(FLOOD_WARNING_PRODUCT_ID)) {
                Dict products = generatedProduct
                        .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
                String legacy = products
                        .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
                assertTrue(legacy.contains(NEW_VTEC_STRING));
                assertTrue(legacy.contains(AREAL_FLOOD_WARNING));
                String xml = products
                        .getDynamicallyTypedValue(ProductConstants.XML_PRODUCT_KEY);
                assertTrue(xml.contains(AREAL_FLOOD_WARNING));
                String cap = products
                        .getDynamicallyTypedValue(ProductConstants.CAP_PRODUCT_KEY);
                assertTrue(cap.contains(AREAL_FLOOD_WARNING));

            } else {
                /*
                 * Unexpected product encountered.
                 */
                testError();
            }
        }
    }

    /**
     * Checks the contents of the Areal Flood Warning follow-up statement during
     * a preview operation.
     * 
     * @param
     * @return
     */
    private void checkFollowUpStatementPreview() {
        Dict productInfo = mockProductEditorView.getProductInfo();
        List<?> generatedProducts = (ArrayList<?>) productInfo
                .get(HazardConstants.GENERATED_PRODUCTS);
        Dict generatedProduct = (Dict) generatedProducts.get(0);
        String productId = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCT_ID);
        assertEquals(productId, FLOOD_STATEMENT_PRODUCT_ID);
        Dict products = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
        String legacy = products
                .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
        assertTrue(legacy.contains(CON_VTEC_STRING));
    }

    /**
     * Tests the contents of the Flood Warning cancellation statement during an
     * end selected hazards operation.
     * 
     * @param
     * @return
     */
    private void checkCancellationStatementPreview() {
        Dict productInfo = mockProductEditorView.getProductInfo();
        List<?> generatedProducts = (ArrayList<?>) productInfo
                .get(HazardConstants.GENERATED_PRODUCTS);
        Dict generatedProduct = (Dict) generatedProducts.get(0);
        String productId = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCT_ID);
        assertEquals(productId, FLOOD_STATEMENT_PRODUCT_ID);
        Dict products = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
        String legacy = products
                .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
        assertTrue(legacy.contains(CAN_VTEC_STRING));
    }

    /**
     * Tests the contents of the Console after an issuance of a Flood Watch.
     * 
     * @param
     * @return
     */
    private void checkArealFloodWatchIssue() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();

        /*
         * There should only be one hazard displayed in the Console.
         */
        assertEquals(hazards.size(), 1);

        Dict hazard = hazards.get(0);
        String stateAsString = hazard
                .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_STATE);
        assertTrue(stateAsString.equals(HazardConstants.HazardState.ISSUED
                .getValue()));
    }

    /**
     * Tests the contents of the console once the Flood Warning is issued.
     * 
     * @param
     * @return
     */
    private void checkArealFloodWarningIssue() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();

        assertEquals(hazards.size(), 2);

        Map<String, String> hazardStateMap = Maps.newHashMap();

        /*
         * There should be one issued (the FA.W) and there should be one ended
         * (the FA.A)
         */
        for (Dict hazard : hazards) {

            String hazardType = hazard
                    .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_TYPE);
            String stateAsString = hazard
                    .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_STATE);

            hazardStateMap.put(hazardType, stateAsString);

        }

        assertEquals(hazardStateMap.size(), 2);
        assertTrue(hazardStateMap.containsKey(AREAL_FLOOD_WATCH_PHEN_SIG));
        assertTrue(hazardStateMap.containsKey(AREAL_FLOOD_WARNING_PHEN_SIG));
        assertEquals(hazardStateMap.get(AREAL_FLOOD_WATCH_PHEN_SIG),
                HazardConstants.HazardState.ENDED.getValue());
        assertEquals(hazardStateMap.get(AREAL_FLOOD_WARNING_PHEN_SIG),
                HazardConstants.HazardState.PENDING.getValue());
    }

    /**
     * Tests the contents of the Console when after the Flood Warning has been
     * ended.
     * 
     * @param
     * @return
     */
    private void checkCancellationStatementIssue() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();

        assertEquals(hazards.size(), 2);

        Map<String, String> hazardStateMap = Maps.newHashMap();

        /*
         * There should be one issued (the FA.W) and there should be one ended
         * (the FA.A)
         */
        for (Dict hazard : hazards) {

            String hazardType = hazard
                    .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_TYPE);
            String stateAsString = hazard
                    .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_STATE);

            hazardStateMap.put(hazardType, stateAsString);

        }

        assertEquals(hazardStateMap.size(), 2);
        assertTrue(hazardStateMap.containsKey(AREAL_FLOOD_WATCH_PHEN_SIG));
        assertTrue(hazardStateMap.containsKey(AREAL_FLOOD_WARNING_PHEN_SIG));
        assertEquals(hazardStateMap.get(AREAL_FLOOD_WATCH_PHEN_SIG),
                HazardConstants.HazardState.ENDED.getValue());
        assertEquals(hazardStateMap.get(AREAL_FLOOD_WARNING_PHEN_SIG),
                HazardConstants.HazardState.ENDED.getValue());
    }

    /**
     * Tests the contents of the Console after the Flood Warning follow-up
     * statement is issued.
     * 
     * @param
     * @return
     */
    private void checkFollowUpStatementIssue() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();

        assertEquals(hazards.size(), 2);

        Map<String, String> hazardStateMap = Maps.newHashMap();

        /*
         * There should be one issued (the FA.W) and there should be one ended
         * (the FA.A)
         */
        for (Dict hazard : hazards) {

            String hazardType = hazard
                    .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_TYPE);
            String stateAsString = hazard
                    .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_STATE);

            hazardStateMap.put(hazardType, stateAsString);

        }

        assertEquals(hazardStateMap.size(), 2);
        assertTrue(hazardStateMap.containsKey(AREAL_FLOOD_WATCH_PHEN_SIG));
        assertTrue(hazardStateMap.containsKey(AREAL_FLOOD_WARNING_PHEN_SIG));
        assertEquals(hazardStateMap.get(AREAL_FLOOD_WATCH_PHEN_SIG),
                HazardConstants.HazardState.ENDED.getValue());
        assertEquals(hazardStateMap.get(AREAL_FLOOD_WARNING_PHEN_SIG),
                HazardConstants.HazardState.ISSUED.getValue());
    }

}
