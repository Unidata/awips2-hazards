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

import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.AREAL_FLOOD_WARNING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.AREAL_FLOOD_WARNING_FULLTYPE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.AREAL_FLOOD_WARNING_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.AREAL_FLOOD_WATCH;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.AREAL_FLOOD_WATCH_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.CAN_VTEC_STRING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.CON_VTEC_STRING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_STATEMENT_PRODUCT_ID;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_WARNING_PRODUCT_ID;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_WATCH_PRODUCT_ID;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.NEW_VTEC_STRING;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
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
 * Feb 07, 2013 2890       bkowal      Product Generation JSON refactor.
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

    public SimpleHazardStoryFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void runFirstStep() {
        try {
            this.step = Steps.CREATE_NEW_HAZARD_AREA;
            Coordinate[] coordinates = autoTestUtilities.buildEventArea(-96.0,
                    41.0);

            new HazardEventBuilder(appBuilder.getSessionManager())
                    .buildPolygonHazardEvent(coordinates);
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
    public void handleNewHazard(SessionEventAdded action) {

        try {

            switch (step) {

            case CREATE_NEW_HAZARD_AREA:
                this.step = Steps.ASSIGN_AREAL_FLOOD_WATCH;

                /*
                 * Retrieve the selected event.
                 */
                Collection<ObservedHazardEvent> selectedEvents = appBuilder
                        .getSessionManager().getEventManager()
                        .getSelectedEvents();

                assertTrue(selectedEvents.size() == 1);

                IHazardEvent selectedEvent = selectedEvents.iterator().next();

                assertTrue(selectedEvent.getEventID().length() > 0);

                Map<String, Serializable> eventTypeSelection = autoTestUtilities
                        .buildEventTypeSelection(selectedEvent,
                                AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);

                HazardDetailAction hazardDetailAction = new HazardDetailAction(
                        HazardDetailAction.ActionType.UPDATE_EVENT_TYPE);
                hazardDetailAction.setParameters(eventTypeSelection);
                eventBus.publishAsync(hazardDetailAction);
                break;

            default:
                break;
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
    @Handler(priority = -1)
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            switch (hazardDetailAction.getActionType()) {

            case UPDATE_EVENT_TYPE:

                Collection<ObservedHazardEvent> selectedEvents = appBuilder
                        .getSessionManager().getEventManager()
                        .getSelectedEvents();

                DictList hidContents = mockHazardDetailView.getContents();

                switch (step) {
                case ASSIGN_AREAL_FLOOD_WATCH:
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
                    break;

                case UPGRADE_TO_AREAL_FLOOD_WARNING:
                    /*
                     * There should be two events selected, the FA.A and the
                     * FA.W
                     */
                    assertTrue(selectedEvents.size() == 2);

                    /*
                     * Check the information passed to the mocked Hazard
                     * Information Dialog
                     */
                    assertTrue(hidContents.size() == 2);
                    step = Steps.PREVIEW_AREAL_FLOOD_WARNING;
                    autoTestUtilities.previewEvent();

                default:
                    break;

                }
                break;

            case PREVIEW:
                assertFalse(mockProductStagingView.isToBeIssued());
                break;

            default:
                break;

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
    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        try {

            if (step.equals(Steps.PREVIEW_AREAL_FLOOD_WATCH)) {
                checkArealFloodWatchPreview();
                step = Steps.ISSUE_AREAL_FLOOD_WATCH;
                autoTestUtilities.issueEvent();
            } else if (step.equals(Steps.ISSUE_AREAL_FLOOD_WATCH)) {
                checkArealFloodWatchIssue();
                step = Steps.UPGRADE_TO_AREAL_FLOOD_WARNING;

                /*
                 * Retrieve the selected event.
                 */
                Collection<ObservedHazardEvent> selectedEvents = appBuilder
                        .getSessionManager().getEventManager()
                        .getSelectedEvents();

                assertTrue(selectedEvents.size() == 1);

                IHazardEvent selectedEvent = selectedEvents.iterator().next();

                assertTrue(selectedEvent.getEventID().length() > 0);

                /*
                 * Build a map simulating a hazard type selection in the HID.
                 */
                Map<String, Serializable> hazardTypeSelection = new HashMap<>();
                hazardTypeSelection.put(
                        HazardConstants.HAZARD_EVENT_IDENTIFIER,
                        selectedEvent.getEventID());
                hazardTypeSelection.put(HazardConstants.HAZARD_EVENT_FULL_TYPE,
                        AREAL_FLOOD_WARNING_FULLTYPE);

                HazardDetailAction hazardDetailAction = new HazardDetailAction(
                        HazardDetailAction.ActionType.UPDATE_EVENT_TYPE);
                hazardDetailAction.setParameters(hazardTypeSelection);
                eventBus.publishAsync(hazardDetailAction);
            } else if (step == Steps.PREVIEW_AREAL_FLOOD_WARNING) {
                checkArealFloodWarningPreview();
                step = Steps.ISSUE_AREAL_FLOOD_WARNING;
                autoTestUtilities.issueEvent();
            } else if (step == Steps.ISSUE_AREAL_FLOOD_WARNING) {

                /*
                 * Fire off another preview action.
                 */
                checkArealFloodWarningIssue();
                step = Steps.PREVIEW_FOLLOW_UP_STATEMENT;
                autoTestUtilities.previewEvent();
            } else if (step == Steps.PREVIEW_FOLLOW_UP_STATEMENT) {
                checkFollowUpStatementPreview();
                step = Steps.ISSUE_FOLLOW_UP_STATEMENT;
                autoTestUtilities.issueEvent();
            } else if (step == Steps.ISSUE_FOLLOW_UP_STATEMENT) {

                /*
                 * End the selected event.
                 */
                checkFollowUpStatementIssue();
                step = Steps.PREVIEW_CANCELLATION_STATEMENT;
                SpatialDisplayAction spatialAction = new SpatialDisplayAction(
                        SpatialDisplayAction.ActionType.CONEXT_MENU_SELECTED,
                        0, HazardConstants.END_SELECTED_HAZARDS);
                eventBus.publishAsync(spatialAction);
            } else if (step == Steps.PREVIEW_CANCELLATION_STATEMENT) {
                checkCancellationStatementPreview();
                step = Steps.ISSUE_CANCELLATION_STATEMENT;
                autoTestUtilities.issueEvent();
            } else if (step == Steps.ISSUE_CANCELLATION_STATEMENT) {
                checkCancellationStatementIssue();
                testSuccess();
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
        List<GeneratedProductList> generatedProductListStorage = mockProductEditorView
                .getGeneratedProductsList();
        GeneratedProductList generatedProductList = generatedProductListStorage
                .get(0);
        assertEquals(generatedProductList.size(), 1);

        IGeneratedProduct generatedProduct0 = generatedProductList
                .get(NumberUtils.INTEGER_ZERO);
        assertEquals(generatedProduct0.getProductID(), FLOOD_WATCH_PRODUCT_ID);
        final String legacy0 = generatedProduct0.getEntries()
                .get(ProductConstants.ASCII_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        assertTrue(legacy0.contains(NEW_VTEC_STRING));
        assertTrue(legacy0.contains(AREAL_FLOOD_WATCH));
        final String xml0 = generatedProduct0.getEntries()
                .get(ProductConstants.XML_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        assertTrue(xml0.contains(AREAL_FLOOD_WATCH));
        final String cap0 = generatedProduct0.getEntries()
                .get(ProductConstants.CAP_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        assertTrue(cap0.contains(AREAL_FLOOD_WATCH));
    }

    /**
     * Tests the contents of the Flood Warning product during a Preview action.
     * 
     * @param
     * @return
     */
    private void checkArealFloodWarningPreview() {
        List<GeneratedProductList> generatedProductListStorage = mockProductEditorView
                .getGeneratedProductsList();
        assertEquals(generatedProductListStorage.size(), 2);
        for (GeneratedProductList generatedProductList : generatedProductListStorage) {
            for (IGeneratedProduct generatedProduct : generatedProductList) {

                String productID = generatedProduct.getProductID();

                if (productID.equals(FLOOD_WATCH_PRODUCT_ID)) {
                    final String legacy = generatedProduct.getEntries()
                            .get(ProductConstants.ASCII_PRODUCT_KEY)
                            .get(NumberUtils.INTEGER_ZERO).toString();
                    assertTrue(legacy.contains(CAN_VTEC_STRING));
                } else if (productID.equals(FLOOD_WARNING_PRODUCT_ID)) {
                    final String legacy = generatedProduct.getEntries()
                            .get(ProductConstants.ASCII_PRODUCT_KEY)
                            .get(NumberUtils.INTEGER_ZERO).toString();
                    assertTrue(legacy.contains(NEW_VTEC_STRING));
                    assertTrue(legacy.contains(AREAL_FLOOD_WARNING));
                    final String xml = generatedProduct.getEntries()
                            .get(ProductConstants.XML_PRODUCT_KEY)
                            .get(NumberUtils.INTEGER_ZERO).toString();
                    assertTrue(xml.contains(AREAL_FLOOD_WARNING));
                    final String cap = generatedProduct.getEntries()
                            .get(ProductConstants.CAP_PRODUCT_KEY)
                            .get(NumberUtils.INTEGER_ZERO).toString();
                    assertTrue(cap.contains(AREAL_FLOOD_WARNING));

                } else {
                    /*
                     * Unexpected product encountered.
                     */
                    testError();
                }
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
        List<GeneratedProductList> generatedProductListStorage = mockProductEditorView
                .getGeneratedProductsList();
        GeneratedProductList generatedProductList = generatedProductListStorage
                .get(0);
        assertTrue(generatedProductList.size() >= NumberUtils.INTEGER_ONE);

        Map<String, IGeneratedProduct> generatedProductMap = new HashMap<String, IGeneratedProduct>();
        for (IGeneratedProduct generatedProduct : generatedProductList) {
            generatedProductMap.put(generatedProduct.getProductID(),
                    generatedProduct);
        }

        assertTrue(generatedProductMap.containsKey(FLOOD_STATEMENT_PRODUCT_ID));
        IGeneratedProduct generatedProduct = generatedProductMap
                .get(FLOOD_STATEMENT_PRODUCT_ID);
        assertEquals(generatedProduct.getProductID(),
                FLOOD_STATEMENT_PRODUCT_ID);
        final String legacy = generatedProduct.getEntries()
                .get(ProductConstants.ASCII_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
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
        List<GeneratedProductList> generatedProductListStorage = mockProductEditorView
                .getGeneratedProductsList();
        GeneratedProductList generatedProductList = generatedProductListStorage
                .get(0);
        assertEquals(generatedProductList.size(), 1);

        IGeneratedProduct generatedProduct = generatedProductList
                .get(NumberUtils.INTEGER_ZERO);
        assertEquals(generatedProduct.getProductID(),
                FLOOD_STATEMENT_PRODUCT_ID);
        final String legacy = generatedProduct.getEntries()
                .get(ProductConstants.ASCII_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
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

        Map<String, String> hazardStateMap = new HashMap<>();

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

        Map<String, String> hazardStateMap = new HashMap<>();

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

        Map<String, String> hazardStateMap = new HashMap<>();

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
