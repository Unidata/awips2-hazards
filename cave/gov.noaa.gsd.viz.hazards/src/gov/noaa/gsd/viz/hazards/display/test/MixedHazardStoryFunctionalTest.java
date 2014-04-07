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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_END;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_HAZARD_INFORMATION_DIALOG;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_REMOVE_POTENTIAL_HAZARDS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CREATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.END_SELECTED_HAZARDS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_COLOR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_FULL_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_STATE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HYDROLOGY_SETTING;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PROPOSE_SELECTED_HAZARDS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.REMOVE_POTENTIAL_HAZARDS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SITE_ID;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.CAN_VTEC_STRING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.CAUSE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.CON_VTEC_STRING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FFW_NON_CONVECTIVE_FULL_TEXT;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FFW_NON_CONVECTIVE_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLASH_FLOOD_WATCH;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLASH_FLOOD_WATCH_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_WARNING_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_WATCH;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_WATCH_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLOOD_WATCH_PRODUCT_ID;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLW_FULL_TEXT;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FORECAST_CONFIDENCE_PERCENTAGE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FORECAST_CONFIDENCE_VALUE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FORECAST_TYPE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.INCLUDE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.NEW_VTEC_STRING;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.OAX;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.SET_CONFIDENCE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.SEV2;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.DamBreakUrgencyLevels;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;

/**
 * Description: {@link FunctionalTest} of the mixed hazard story.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166      daniel.s.schaffer@noaa.gov      Initial creation
 * Oct 29, 2013 2166      daniel.s.schaffer@noaa.gov      Test in working order
 * Nov  04, 2013   2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Using new utility
 * Nov 20, 2013   2159     daniel.s.schaffer@noaa.gov Now alerts interoperable with DRT
 * Feb 07, 2014 2890       bkowal       Product Generation JSON refactor.
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
class MixedHazardStoryFunctionalTest extends FunctionalTest {

    private enum Steps {
        RUN_DAM_BREAK, RUN_FLOOD, SELECT_RECOMMENDED, SELECTION_PREVIEW,

        SELECTION_ISSUE, REMOVING_POTENTIAL_EVENTS,

        UPDATING_FIRST_EVENT, UPDATING_SECOND_EVENT,

        REPLACEMENT_PREVIEW_FIRST_PRODUCT, REPLACEMENT_PREVIEW_SECOND_PRODUCT, REPLACEMENT_PREVIEW_THIRD_PRODUCT,

        REPLACEMENT_ISSUE_FIRST_PRODUCT, REPLACEMENT_ISSUE_SECOND_PRODUCT, REPLACEMENT_ISSUE_THIRD_PRODUCT,

        CONTINUING_EVENTS,

        CONTINUED_PREVIEW_FIRST_PRODUCT, CONTINUED_PREVIEW_SECOND_PRODUCT,

        CONTINUED_ISSUE_FIRST_PRODUCT, CONTINUED_ISSUE_SECOND_PRODUCT,

        REMOVING_ENDED_EVENTS,

        ENDED_PREVIEW_FIRST_PRODUCT, ENDED_PREVIEW_SECOND_PRODUCT,

        ENDED_ISSUE_FIRST_PRODUCT, ENDED_ISSUE_SECOND_PRODUCT,

        TEST_ENDED
    }

    private Steps step;

    MixedHazardStoryFunctionalTest(HazardServicesAppBuilder appBuilder) {
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
        this.step = Steps.RUN_DAM_BREAK;
        eventBus.publishAsync(new ToolAction(
                ToolAction.ToolActionEnum.RUN_TOOL, DAM_BREAK_FLOOD_RECOMMENDER));
    }

    @Handler(priority = -1)
    public void toolActionOccurred(final ToolAction action) {
        try {
            List<Dict> hazards;
            switch (action.getActionType()) {
            case RUN_TOOL:
                switch (step) {
                case RUN_DAM_BREAK:

                    autoTestUtilities
                            .runDamBreakRecommender(DamBreakUrgencyLevels.LOW_CONFIDENCE_URGENCY_LEVEL);
                    break;

                case RUN_FLOOD:

                    Map<String, Serializable> riverFloodInfo = new HashMap<>();
                    riverFloodInfo.put(FORECAST_CONFIDENCE_PERCENTAGE,
                            FORECAST_CONFIDENCE_VALUE);
                    riverFloodInfo.put(FORECAST_TYPE, SET_CONFIDENCE);
                    eventBus.publishAsync(new ToolAction(
                            ToolAction.ToolActionEnum.RUN_TOOL_WITH_PARAMETERS,
                            RIVER_FLOOD_RECOMMENDER, riverFloodInfo));
                    break;
                default:
                    testError();
                    break;
                }
                break;

            case RUN_TOOL_WITH_PARAMETERS:
                break;

            case TOOL_RECOMMENDATIONS:
                switch (step) {
                case RUN_DAM_BREAK:
                    DictList hidContents;
                    hazards = mockConsoleView.getHazardEvents();
                    assertEquals(hazards.size(), 1);

                    Dict event = hazards.get(0);
                    checkDamBreakRecommendation(event);

                    hidContents = mockHazardDetailView.getContents();

                    assertEquals(hidContents.size(), 1);
                    Dict hidEvent = (Dict) hidContents.get(0);
                    checkDamBreakRecommendation(hidEvent);

                    /*
                     * Note that it is not possible to check the contents of the
                     * spatial display because the associated MVP View code
                     * includes the logic for retrieving hazards instead of
                     * being handed hazards.
                     */

                    step = Steps.RUN_FLOOD;
                    eventBus.publishAsync(new ToolAction(
                            ToolAction.ToolActionEnum.RUN_TOOL,
                            RIVER_FLOOD_RECOMMENDER));
                    break;
                case RUN_FLOOD:
                    hazards = mockConsoleView.getHazardEvents();
                    assertEquals(hazards.size(), 8);
                    event = hazards.get(0);
                    assertEquals(event.get(HAZARD_EVENT_TYPE),
                            FLASH_FLOOD_WATCH_PHEN_SIG);
                    event = hazards.get(1);
                    assertEquals(event.get(HAZARD_EVENT_TYPE),
                            FLOOD_WATCH_PHEN_SIG);
                    assertEquals(event.get(HAZARD_EVENT_STATE),
                            HazardState.POTENTIAL.getValue());
                    assertEquals(event.get(HAZARD_EVENT_COLOR), "191 221 216");

                    String e0 = hazards.get(0).getDynamicallyTypedValue(
                            HAZARD_EVENT_IDENTIFIER);
                    String e1 = hazards.get(1).getDynamicallyTypedValue(
                            HAZARD_EVENT_IDENTIFIER);

                    String[] eventIDs = new String[] { e0, e1 };
                    step = Steps.SELECT_RECOMMENDED;
                    SpatialDisplayAction displayAction = new SpatialDisplayAction(
                            SpatialDisplayAction.ActionType.SELECTED_EVENTS_CHANGED,
                            eventIDs);
                    eventBus.publishAsync(displayAction);
                    break;
                default:
                    testError();
                    break;
                }

                break;
            default:
                break;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {
        try {
            switch (step) {
            case SELECT_RECOMMENDED:
                checkConsoleSelections();
                checkHidFloodEventAddition();
                step = Steps.SELECTION_PREVIEW;
                autoTestUtilities.previewEvent();
                break;

            case REMOVING_POTENTIAL_EVENTS:
                List<Dict> hazards = mockConsoleView.getHazardEvents();

                assertEquals(hazards.size(), 2);

                step = Steps.UPDATING_FIRST_EVENT;
                replaceEvent((Dict) mockHazardDetailView.getContents().get(0),
                        FFW_NON_CONVECTIVE_FULL_TEXT);
                break;

            case REMOVING_ENDED_EVENTS:
                step = Steps.ENDED_PREVIEW_FIRST_PRODUCT;
                break;

            default:
                testError();

            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Handler(priority = -1)
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            switch (hazardDetailAction.getActionType()) {
            case PREVIEW:
                assertFalse(mockProductStagingView.isToBeIssued());
                break;

            case UPDATE_EVENT_TYPE:

                switch (step) {
                case UPDATING_FIRST_EVENT:
                    step = Steps.UPDATING_SECOND_EVENT;
                    replaceEvent(
                            (Dict) mockHazardDetailView.getContents().get(1),
                            FLW_FULL_TEXT);
                    break;

                case UPDATING_SECOND_EVENT:
                    checkReplacement();
                    step = Steps.REPLACEMENT_PREVIEW_FIRST_PRODUCT;
                    autoTestUtilities.previewEvent();
                    break;

                default:
                    testError();
                }
                break;

            case UPDATE_EVENT_METADATA:
                step = Steps.CONTINUED_PREVIEW_FIRST_PRODUCT;
                autoTestUtilities.previewEvent();
                break;

            default:
                throw new IllegalArgumentException("Unexpected action type "
                        + hazardDetailAction.getActionType());
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {

        try {
            Dict event;

            switch (step) {
            case SELECTION_PREVIEW:
                checkSelectionPreview();
                step = Steps.SELECTION_ISSUE;
                issueEvent();
                break;

            case SELECTION_ISSUE:
                checkFirstSelectionIssue();
                List<String> contextMenuEntries = toolLayer
                        .getContextMenuEntries();
                checkMenuContextMenu(contextMenuEntries,
                        CONTEXT_MENU_HAZARD_INFORMATION_DIALOG);
                assertTrue(contextMenuEntries
                        .contains(CONTEXT_MENU_HAZARD_INFORMATION_DIALOG));
                assertTrue(contextMenuEntries.contains(END_SELECTED_HAZARDS));
                assertTrue(contextMenuEntries
                        .contains(PROPOSE_SELECTED_HAZARDS));
                assertTrue(contextMenuEntries
                        .contains(REMOVE_POTENTIAL_HAZARDS));

                step = Steps.REMOVING_POTENTIAL_EVENTS;
                postContextMenuEvent(CONTEXT_MENU_REMOVE_POTENTIAL_HAZARDS);
                break;

            case REPLACEMENT_PREVIEW_FIRST_PRODUCT:
                checkReplacementPreview();
                step = Steps.REPLACEMENT_ISSUE_FIRST_PRODUCT;
                issueEvent();
                break;

            case REPLACEMENT_ISSUE_FIRST_PRODUCT:
                checkOriginalProductsEnded();
                checkReplacementEvents(HazardState.ISSUED.getValue());
                checkEndedEventsGoneFromHid();
                Map<String, Serializable> metadata = new HashMap<>();
                metadata.put(INCLUDE, SEV2);
                this.step = Steps.CONTINUING_EVENTS;
                event = getEventByType(FFW_NON_CONVECTIVE_PHEN_SIG);
                updateEvent(event, metadata);
                break;

            case CONTINUED_PREVIEW_FIRST_PRODUCT:
                checkContinuedPreview();
                step = Steps.CONTINUED_ISSUE_FIRST_PRODUCT;
                issueEvent();
                break;

            case CONTINUED_ISSUE_FIRST_PRODUCT:
                checkReplacementEvents(HazardState.ISSUED.getValue());
                step = Steps.REMOVING_ENDED_EVENTS;
                postContextMenuEvent(CONTEXT_MENU_END);
                break;

            case ENDED_PREVIEW_FIRST_PRODUCT:
                checkEndedPreview();
                step = Steps.ENDED_ISSUE_FIRST_PRODUCT;
                issueEvent();
                break;

            case ENDED_ISSUE_FIRST_PRODUCT:
                checkReplacementEvents(HazardState.ENDED.getValue());
                step = Steps.TEST_ENDED;
                testSuccess();
                break;

            default:
                testError();
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    private void checkMenuContextMenu(List<String> contextMenuEntries,
            String expected) {
        for (String entry : contextMenuEntries) {
            if (entry.equals(expected)) {
                return;
            }
        }
        assertFalse(true);
    }

    private void postContextMenuEvent(String choice) {
        SpatialDisplayAction spatialAction = new SpatialDisplayAction(
                SpatialDisplayAction.ActionType.CONEXT_MENU_SELECTED, 0, choice);
        eventBus.publishAsync(spatialAction);
    }

    private void checkReplacementEvents(String state) {
        Dict event;
        event = getEventByType(FLOOD_WARNING_PHEN_SIG);
        assertEquals(event.get(HAZARD_EVENT_STATE), state);
        event = getEventByType(FFW_NON_CONVECTIVE_PHEN_SIG);
        assertEquals(event.get(HAZARD_EVENT_STATE), state);
    }

    private void checkOriginalProductsEnded() {
        Dict event;
        event = getEventByType(FLOOD_WATCH_PHEN_SIG);
        assertEquals(event.get(HAZARD_EVENT_STATE),
                HazardState.ENDED.getValue());
        event = getEventByType(FLASH_FLOOD_WATCH_PHEN_SIG);
        assertEquals(event.get(HAZARD_EVENT_STATE),
                HazardState.ENDED.getValue());
    }

    private Dict getEventByType(String eventType) {
        List<Dict> events = mockConsoleView.getHazardEvents();
        for (Dict event : events) {
            String type = event.getDynamicallyTypedValue(HAZARD_EVENT_TYPE);
            if (type.equals(eventType)) {
                return event;
            }
        }
        testError();
        return null;
    }

    private void checkSelectionPreview() {
        List<GeneratedProductList> generatedProductListStorage = mockProductEditorView
                .getGeneratedProductsList();
        GeneratedProductList generatedProductList = generatedProductListStorage
                .get(0);
        assertEquals(generatedProductList.size(), 2);

        IGeneratedProduct generatedProduct0 = generatedProductList
                .get(NumberUtils.INTEGER_ZERO);
        assertEquals(generatedProduct0.getProductID(), FLOOD_WATCH_PRODUCT_ID);
        final String legacy0 = generatedProduct0.getEntries()
                .get(ProductConstants.ASCII_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        assertTrue(legacy0.contains(NEW_VTEC_STRING + "."
                + FLOOD_WATCH_PHEN_SIG));
        assertTrue(legacy0.contains(FLOOD_WATCH));
        final String xml0 = generatedProduct0.getEntries()
                .get(ProductConstants.XML_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        assertTrue(xml0.contains(FLOOD_WATCH));
        final String cap0 = generatedProduct0.getEntries()
                .get(ProductConstants.CAP_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        assertTrue(cap0.contains(FLOOD_WATCH));

        IGeneratedProduct generatedProduct1 = generatedProductList
                .get(NumberUtils.INTEGER_ONE);
        assertEquals(generatedProduct1.getProductID(), FLOOD_WATCH_PRODUCT_ID);
        final String legacy1 = generatedProduct1.getEntries()
                .get(ProductConstants.ASCII_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        assertTrue(legacy1.contains(NEW_VTEC_STRING + "."
                + FLASH_FLOOD_WATCH_PHEN_SIG));
        assertTrue(legacy1.contains(FLASH_FLOOD_WATCH));
    }

    private void checkFirstSelectionIssue() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();

        int numIssued = 0;
        for (Dict hazard : hazards) {
            String stateAsString = hazard
                    .getDynamicallyTypedValue(HAZARD_EVENT_STATE);
            if (stateAsString.equals(HazardState.ISSUED.getValue())) {
                numIssued += 1;
            }

        }
        assertEquals(numIssued, 2);

    }

    private void checkReplacementPreview() {
        Dict products = autoTestUtilities
                .productsFromEditorView(mockProductEditorView);
        String legacy = products
                .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
        assertTrue(legacy.contains("REPLACED"));
        assertTrue(legacy.contains("FLOOD WATCH"));

    }

    private void checkContinuedPreview() {
        Dict products = autoTestUtilities
                .productsFromEditorView(mockProductEditorView);
        String legacy = products
                .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
        assertTrue(legacy.contains(CON_VTEC_STRING));

    }

    private void checkEndedPreview() {
        Dict products = autoTestUtilities
                .productsFromEditorView(mockProductEditorView);
        String legacy = products
                .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
        assertTrue(legacy.contains(CAN_VTEC_STRING));

    }

    private void issueEvent() {
        ProductEditorAction action = new ProductEditorAction(HazardAction.ISSUE);
        action.setGeneratedProductsList(mockProductEditorView
                .getGeneratedProductsList());
        eventBus.publishAsync(action);
    }

    private void replaceEvent(Dict event, String eventType) {
        String eventID = event
                .getDynamicallyTypedValue(HAZARD_EVENT_IDENTIFIER);
        Map<String, Serializable> metadata = new HashMap<>();
        metadata.put(HAZARD_EVENT_IDENTIFIER, eventID);
        metadata.put(ISessionEventManager.ATTR_HAZARD_CATEGORY,
                HYDROLOGY_SETTING);
        metadata.put(HAZARD_EVENT_FULL_TYPE, eventType);

        eventBus.publishAsync(new HazardDetailAction(
                HazardDetailAction.ActionType.UPDATE_EVENT_TYPE, metadata));
    }

    private void updateEvent(Dict event, Map<String, Serializable> metadata) {
        Map<String, Serializable> allMetadata = new HashMap<>();
        String eventID = event
                .getDynamicallyTypedValue(HAZARD_EVENT_IDENTIFIER);
        allMetadata.put(HAZARD_EVENT_IDENTIFIER, eventID);
        for (String key : metadata.keySet()) {
            allMetadata.put(key, metadata.get(key));
        }

        eventBus.publishAsync(new HazardDetailAction(
                HazardDetailAction.ActionType.UPDATE_EVENT_METADATA,
                allMetadata));
    }

    private void checkConsoleSelections() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();
        int numSelected = 0;
        for (Dict hazard : hazards) {
            Boolean selected = (Boolean) hazard
                    .get(ISessionEventManager.ATTR_SELECTED);
            if (selected) {
                numSelected += 1;
            }

        }
        assertEquals(numSelected, 2);
    }

    private void checkDamBreakRecommendation(Dict event) {
        assertEquals(event.get(HAZARD_EVENT_TYPE), FLASH_FLOOD_WATCH_PHEN_SIG);
        assertEquals(event.get(SITE_ID), OAX);
        assertEquals(event.get(CAUSE), "Dam Failure");
        assertEquals(event.get(HAZARD_EVENT_STATE),
                HazardState.PENDING.getValue());

        assertEquals(asDouble(event.get(CREATION_TIME)), new Double(
                1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_START_TIME)), new Double(
                1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_END_TIME)), new Double(
                1.2971484E12));
        assertEquals(event.get(HAZARD_EVENT_COLOR), "142 224 209");
    }

    private void checkHidFloodEventAddition() {
        DictList hidContents = mockHazardDetailView.getContents();
        assertEquals(hidContents.size(), 2);
        Dict floodEvent = (Dict) hidContents.get(1);
        assertEquals(floodEvent.get(HAZARD_EVENT_TYPE), FLOOD_WATCH_PHEN_SIG);
    }

    private void checkReplacement() {
        List<Dict> consoleEvents = this.mockConsoleView.getHazardEvents();
        assertEquals(consoleEvents.size(), 4);
        Dict updatedDamBreakEvent = consoleEvents.get(2);
        assertEquals(updatedDamBreakEvent.get(HAZARD_EVENT_STATE),
                HazardState.PENDING.getValue());
        assertEquals(updatedDamBreakEvent.get(HAZARD_EVENT_TYPE),
                FFW_NON_CONVECTIVE_PHEN_SIG);

        Dict updatedRiverFloodEvent = consoleEvents.get(3);
        assertEquals(updatedRiverFloodEvent.get(HAZARD_EVENT_TYPE),
                FLOOD_WARNING_PHEN_SIG);

        DictList hidContents = mockHazardDetailView.getContents();
        assertEquals(hidContents.size(), 4);
        Dict event = (Dict) hidContents.get(2);
        String fullType = event
                .getDynamicallyTypedValue(HAZARD_EVENT_FULL_TYPE);
        assertTrue(fullType.contains(FFW_NON_CONVECTIVE_PHEN_SIG));

        event = (Dict) hidContents.get(3);
        fullType = event.getDynamicallyTypedValue(HAZARD_EVENT_FULL_TYPE);
        assertTrue(fullType.contains(FLOOD_WARNING_PHEN_SIG));
    }

    private void checkEndedEventsGoneFromHid() {
        DictList hidContents = mockHazardDetailView.getContents();
        assertEquals(hidContents.size(), 2);
        Dict floodEvent = (Dict) hidContents.get(1);
        assertEquals(floodEvent.get(HAZARD_EVENT_TYPE), FLOOD_WARNING_PHEN_SIG);
    }

    /**
     * Deal with the fact that sometimes the values are
     * ComparableLazilyParsedNumber and sometimes Doubles
     */
    private Double asDouble(Object object) {
        String doubleAsString = String.valueOf(object);
        Double result = Double.valueOf(doubleAsString);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
