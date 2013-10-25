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
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;

/**
 * Description: {@link FunctionalTest} of the mixed hazard story.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
class MixedHazardStoryFunctionalTest extends FunctionalTest {

    private static final String FLASH_FLOOD_WATCH = "FLASH FLOOD WATCH";

    private static final String OAX = "OAX";

    private static final String FLW = "FL.W (FLOOD WARNING)";

    private static final String FFW_NON_CONVECTIVE = "FF.W.NonConvective (FLASH FLOOD WARNING)";

    private static final String LOW_CONFIDENCE_URGENCY_LEVEL = "Low Confidence (Potential Structure Failure)";

    private static final String URGENCY_LEVEL = "urgencyLevel";

    private static final String BRANCH_OAK_DAM = "Branch Oak Dam";

    private static final String DAM_NAME = "damName";

    private static final double FORECAST_CONFIDENCE_VALUE = 50.0;

    private static final String SET_CONFIDENCE = "Set confidence:";

    private static final String FORECAST_TYPE = "forecastType";

    private static final String FORECAST_CONFIDENCE_PERCENTAGE = "forecastConfidencePercentage";

    private enum Steps {
        RUN_DAM_BREAK, RUN_FLOOD, SELECT_RECOMMENDED, FIRST_PREVIEW, FIRST_ISSUE, REMOVING_POTENTIAL_EVENTS, UPDATING_FIRST_EVENT, UPDATING_SECOND_EVENT, TEST_ENDED, SECOND_PREVIEW, SECOND_ISSUE, FIRST_UPGRADE, SECOND_UPGRADE
    }

    private Steps step;

    MixedHazardStoryFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        if (testsEnabled) {
            super.run();
            this.step = Steps.RUN_DAM_BREAK;
            eventBus.post(new ToolAction(ToolAction.ToolActionEnum.RUN_TOOL,
                    HazardConstants.DAM_BREAK_FLOOD_RECOMMENDER));
        }

    }

    @Subscribe
    public void toolActionOccurred(final ToolAction action) {
        List<Dict> hazards;
        switch (action.getAction()) {
        case RUN_TOOL:
            switch (step) {
            case RUN_DAM_BREAK:

                Dict damBreakInfo = new Dict();
                damBreakInfo.put(DAM_NAME, BRANCH_OAK_DAM);
                damBreakInfo.put(URGENCY_LEVEL, LOW_CONFIDENCE_URGENCY_LEVEL);
                eventBus.post(new ToolAction(
                        ToolAction.ToolActionEnum.RUN_TOOL_WITH_PARAMETERS,
                        HazardConstants.DAM_BREAK_FLOOD_RECOMMENDER,
                        damBreakInfo));
                break;
            case RUN_FLOOD:

                Dict riverFloodInfo = new Dict();
                riverFloodInfo.put(FORECAST_CONFIDENCE_PERCENTAGE,
                        FORECAST_CONFIDENCE_VALUE);
                riverFloodInfo.put(FORECAST_TYPE, SET_CONFIDENCE);
                eventBus.post(new ToolAction(
                        ToolAction.ToolActionEnum.RUN_TOOL_WITH_PARAMETERS,
                        HazardConstants.RIVER_FLOOD_RECOMMENDER, riverFloodInfo));
                break;
            default:
                testError();
                break;
            }
            break;

        case RUN_TOOL_WITH_PARAMETERS:
            break;

        case PRODUCTS_GENERATED:
            if (step.equals(Steps.FIRST_ISSUE)) {
                checkFirstIssue();
            }
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
                 * spatial display because the associated MVP View code includes
                 * the logic for retrieving hazards instead of being handed
                 * hazards.
                 */

                step = Steps.RUN_FLOOD;
                eventBus.post(new ToolAction(
                        ToolAction.ToolActionEnum.RUN_TOOL,
                        HazardConstants.RIVER_FLOOD_RECOMMENDER));
                break;
            case RUN_FLOOD:
                hazards = mockConsoleView.getHazardEvents();
                assertEquals(hazards.size(), 8);
                event = hazards.get(0);
                assertEquals(event.get(HazardConstants.TYPE), "FF.A");
                event = hazards.get(1);
                assertEquals(event.get(HazardConstants.TYPE), "FL.A");
                assertEquals(event.get(HazardConstants.STATE),
                        HazardConstants.HazardState.POTENTIAL.getValue());
                assertEquals(event.get(HazardConstants.COLOR), "191 221 217");

                String e0 = hazards.get(0).getDynamicallyTypedValue(
                        HazardConstants.EVENTID);
                String e1 = hazards.get(1).getDynamicallyTypedValue(
                        HazardConstants.EVENTID);

                /*
                 * TODO Add back e1
                 */
                String[] eventIDs = new String[] { e0 };
                step = Steps.SELECT_RECOMMENDED;
                SpatialDisplayAction displayAction = new SpatialDisplayAction(
                        HazardConstants.SELECTED_EVENTS_CHANGED, eventIDs);
                eventBus.post(displayAction);
                break;
            default:
                testError();
                break;
            }

            break;
        default:
            break;
        }
    }

    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {
        switch (step) {
        case SELECT_RECOMMENDED:
            checkConsoleSelections();
            step = Steps.FIRST_PREVIEW;
            eventBus.post(new HazardDetailAction(
                    HazardConstants.HazardAction.PREVIEW.getValue()));
            break;

        case REMOVING_POTENTIAL_EVENTS:
            List<Dict> hazards = mockConsoleView.getHazardEvents();

            /*
             * TODO Change to 2 when we issue one of the dam break events.
             */
            assertEquals(hazards.size(), 1);

            step = Steps.UPDATING_FIRST_EVENT;
            replaceEvent((Dict) mockHazardDetailView.getContents().get(0),
                    FFW_NON_CONVECTIVE);
            break;

        default:
            testError();

        }

    }

    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        String action = hazardDetailAction.getAction();
        if (action.equalsIgnoreCase(HazardConstants.HazardAction.PREVIEW
                .getValue())) {
            assertFalse(mockProductStagingView.isToBeIssued());

        } else if (action.equals(HazardConstants.UPDATE_EVENT_METADATA)) {
            if (step.equals(Steps.UPDATING_FIRST_EVENT)) {
                step = Steps.UPDATING_SECOND_EVENT;
                replaceEvent((Dict) mockHazardDetailView.getContents().get(1),
                        FLW);
            } else if (step.equals(Steps.UPDATING_SECOND_EVENT)) {
                step = Steps.SECOND_PREVIEW;
                eventBus.post(new HazardDetailAction(
                        HazardConstants.HazardAction.PREVIEW.getValue()));

            }
        }
    }

    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {

        if (step.equals(Steps.FIRST_PREVIEW)) {
            checkFirstPreview();
            step = Steps.FIRST_ISSUE;
            issueEvent();
        } else if (step.equals(Steps.FIRST_ISSUE)) {
            SpatialDisplayAction spatialAction = new SpatialDisplayAction(
                    HazardConstants.CONEXT_MENU_SELECTED, 0,
                    HazardConstants.CONTEXT_MENU_REMOVE_POTENTIAL_HAZARDS);
            step = Steps.REMOVING_POTENTIAL_EVENTS;
            eventBus.post(spatialAction);

        } else if (step.equals(Steps.SECOND_PREVIEW)) {
            step = Steps.SECOND_ISSUE;
            issueEvent();
        }

        else if (step.equals(Steps.SECOND_ISSUE)) {
            /*
             * Check FLW_FLS upgrade
             */
            step = Steps.FIRST_UPGRADE;
        } else if (step.equals(Steps.FIRST_UPGRADE)) {
            /*
             * Check FFA_UPGRADE product
             */
            step = Steps.SECOND_UPGRADE;
        } else if (step.equals(Steps.SECOND_UPGRADE)) {
            /*
             * Check FLW_FLS_UPGRADE product
             */
            step = Steps.TEST_ENDED;
            endTest();

        } else {
            testError();
        }

    }

    private void checkFirstIssue() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();

        int numIssued = 0;
        for (Dict hazard : hazards) {
            String stateAsString = hazard
                    .getDynamicallyTypedValue(HazardConstants.STATE);
            if (stateAsString.equals(HazardConstants.HazardState.ISSUED
                    .getValue())) {
                numIssued += 1;
            }

        }
        assertEquals(numIssued, 1);
    }

    private void checkFirstPreview() {
        Dict productInfo = mockProductEditorView.getProductInfo();
        List<?> generatedProducts = (ArrayList<?>) productInfo
                .get(HazardConstants.GENERATED_PRODUCTS);
        assertEquals(generatedProducts.size(), 1);
        Dict generatedProduct = (Dict) generatedProducts.get(0);
        String productId = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCT_ID);
        assertEquals(productId, "FFA");
        Dict products = generatedProduct
                .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
        String legacy = products
                .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
        assertTrue(legacy.contains("NEW.KOAX"));
        assertTrue(legacy.contains(FLASH_FLOOD_WATCH));
        String xml = products
                .getDynamicallyTypedValue(ProductConstants.XML_PRODUCT_KEY);
        assertTrue(xml.contains(FLASH_FLOOD_WATCH));
        String cap = products
                .getDynamicallyTypedValue(ProductConstants.CAP_PRODUCT_KEY);
        assertTrue(cap.contains(FLASH_FLOOD_WATCH));
    }

    private void issueEvent() {
        eventBus.post(new HazardDetailAction(HazardConstants.HazardAction.ISSUE
                .getValue()));
    }

    private void replaceEvent(Dict event, String eventType) {
        String eventID = event
                .getDynamicallyTypedValue(HazardConstants.EVENTID);
        Dict metadata = new Dict();
        metadata.put(HazardConstants.EVENTID, eventID);
        metadata.put(ISessionEventManager.ATTR_HAZARD_CATEGORY,
                HazardConstants.HYDROLOGY_SETTING);
        metadata.put(Utilities.HAZARD_EVENT_FULL_TYPE, eventType);

        eventBus.post(new HazardDetailAction(
                HazardConstants.UPDATE_EVENT_METADATA, metadata.toJSONString()));
    }

    private void checkConsoleSelections() {
        List<Dict> hazards = mockConsoleView.getHazardEvents();
        int i = 0;
        for (Dict hazard : hazards) {
            Boolean selected = (Boolean) hazard
                    .get(ISessionEventManager.ATTR_SELECTED);
            if (i == 0) {
                assertTrue(selected);
            } else {
                assertFalse(selected);
            }
            i += 1;

        }
    }

    private void checkDamBreakRecommendation(Dict event) {
        assertEquals(event.get(HazardConstants.TYPE), "FF.A");
        assertEquals(event.get(HazardConstants.SITEID), OAX);
        assertEquals(event.get(HazardConstants.CAUSE), "Dam Failure");
        assertEquals(event.get(HazardConstants.STATE),
                HazardConstants.HazardState.PENDING.getValue());

        assertEquals(asDouble(event.get(HazardConstants.ISSUETIME)),
                new Double(1.2971376E12));
        assertEquals(asDouble(event.get(HazardConstants.STARTTIME)),
                new Double(1.2971376E12));
        assertEquals(asDouble(event.get(HazardConstants.ENDTIME)), new Double(
                1.2971484E12));
        assertEquals(event.get(HazardConstants.COLOR), "144 224 209");
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
