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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import java.util.List;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;

/**
 * Description: Constants and utilities for {@link FunctionalTest}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 30, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * Nov  04, 2013   2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Added some utilities
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AutoTestUtilities {

    public static Double EVENT_BUILDER_OFFSET = 0.0025;

    static final String OAX = "OAX";

    static final String AREAL_FLOOD_WATCH_PHEN_SIG = "FA.A";

    static final String FLASH_FLOOD_WATCH_PHEN_SIG = "FF.A";

    static final String FLOOD_WATCH_PHEN_SIG = "FL.A";

    static final String FLOOD_WARNING_PHEN_SIG = "FL.W";

    static final String AREAL_FLOOD_WARNING_PHEN_SIG = "FA.W";

    static final String AREAL_FLOOD_WATCH = "AREAL FLOOD WATCH";

    static final String FLASH_FLOOD_WATCH = "FLASH FLOOD WATCH";

    static final String AREAL_FLOOD_WARNING = "AREAL FLOOD WARNING";

    static final String FLOOD_WARNING = "FLOOD WARNING";

    static final String FLOOD_WATCH = "FLOOD WATCH";

    static final String AREAL_FLOOD_WATCH_FULLTYPE = "FA.A ("
            + AREAL_FLOOD_WATCH + ")";

    static final String FLASH_FLOOD_WATCH_FULLTYPE = "FF.A ("
            + FLASH_FLOOD_WATCH + ")";

    static final String AREAL_FLOOD_WARNING_FULLTYPE = "FA.W ("
            + AREAL_FLOOD_WARNING + ")";

    static final String FLW_FULL_TEXT = FLOOD_WARNING_PHEN_SIG + " ("
            + FLOOD_WARNING + ")";

    static final String FFW_NON_CONVECTIVE_PHEN_SIG = "FF.W.NonConvective";

    static final String FFW_NON_CONVECTIVE_FULL_TEXT = FFW_NON_CONVECTIVE_PHEN_SIG
            + " (FLASH FLOOD WARNING)";

    static final String FLOOD_WATCH_PRODUCT_ID = "FFA";

    static final String FLOOD_WARNING_PRODUCT_ID = "FLW";

    static final String FLOOD_STATEMENT_PRODUCT_ID = "FLS";

    static final String NEW_VTEC_STRING = "NEW.K" + OAX;

    static final String CON_VTEC_STRING = "CON.K" + OAX;

    static final String CAN_VTEC_STRING = "CAN.K" + OAX;

    static final String EXA_VTEC_STRING = "EXA.K" + OAX;

    static final String EXT_VTEC_STRING = "EXT.K" + OAX;

    static final String HYDROLOGY = "Hydrology";

    static final String SEV2 = "sev2";

    static final String INCLUDE = "include";

    static final String LOW_CONFIDENCE_URGENCY_LEVEL = "Low Confidence (Potential Structure Failure)";

    static final String URGENCY_LEVEL = "urgencyLevel";

    static final String BRANCH_OAK_DAM = "Branch Oak Dam";

    static final String DAM_NAME = "damName";

    static final double FORECAST_CONFIDENCE_VALUE = 50.0;

    static final String SET_CONFIDENCE = "Set confidence:";

    static final String FORECAST_TYPE = "forecastType";

    static final String FORECAST_CONFIDENCE_PERCENTAGE = "forecastConfidencePercentage";

    private final HazardServicesAppBuilder appBuilder;

    private final EventBus eventBus;

    public AutoTestUtilities(HazardServicesAppBuilder appBuilder) {
        this.appBuilder = appBuilder;
        this.eventBus = appBuilder.getEventBus();
    }

    void createEvent(Double centerX, Double centerY) {
        SpatialDisplayAction displayAction = new SpatialDisplayAction(
                HazardConstants.NEW_EVENT_SHAPE);
        Dict toolParameters = buildEventArea(centerX, centerY);
        displayAction.setToolParameters(toolParameters);
        eventBus.post(displayAction);
    }

    void assignSelectedEventType(String eventType) {
        IHazardEvent selectedEvent = getSelectedEvent();
        assignEventType(eventType, selectedEvent);
    }

    void assignEventType(String eventType, IHazardEvent selectedEvent) {
        Dict dict = buildEventTypeSelection(selectedEvent, eventType);

        HazardDetailAction hazardDetailAction = new HazardDetailAction(
                HazardConstants.UPDATE_EVENT_TYPE);
        hazardDetailAction.setJSONText(dict.toJSONString());
        appBuilder.getEventBus().post(hazardDetailAction);
    }

    IHazardEvent getSelectedEvent() {
        IHazardEvent selectedEvent = appBuilder.getSessionManager()
                .getEventManager().getSelectedEvents().iterator().next();
        return selectedEvent;
    }

    Dict buildEventArea(Double centerX, Double centerY) {
        Dict result = new Dict();
        DictList shapes = new DictList();
        result.put(HazardConstants.SHAPES, shapes);
        Dict shape = new Dict();
        shapes.add(shape);
        shape.put(HazardConstants.IS_SELECTED_KEY, Boolean.TRUE.toString());
        shape.put(HazardConstants.IS_VISIBLE_KEY, Boolean.TRUE.toString());
        shape.put(HazardConstants.HAZARD_EVENT_SHAPE_TYPE,
                HazardConstants.HAZARD_EVENT_SHAPE_TYPE_POLYGON);
        DictList points = new DictList();
        shape.put(HazardConstants.POINTS, points);
        DictList point = buildPoint(centerX, centerY, -EVENT_BUILDER_OFFSET,
                -EVENT_BUILDER_OFFSET);
        points.add(point);
        point = buildPoint(centerX, centerY, EVENT_BUILDER_OFFSET,
                -EVENT_BUILDER_OFFSET);
        points.add(point);
        point = buildPoint(centerX, centerY, EVENT_BUILDER_OFFSET,
                EVENT_BUILDER_OFFSET);
        points.add(point);
        point = buildPoint(centerX, centerY, -EVENT_BUILDER_OFFSET,
                EVENT_BUILDER_OFFSET);
        points.add(point);
        point = buildPoint(centerX, centerY, -EVENT_BUILDER_OFFSET,
                -EVENT_BUILDER_OFFSET);
        points.add(point);
        return result;
    }

    private DictList buildPoint(Double centerX, Double centerY, Double xOffset,
            Double yOffset) {
        DictList point = new DictList();
        point.add(centerX + xOffset);
        point.add(centerY + yOffset);
        return point;
    }

    Dict buildEventTypeSelection(IHazardEvent selectedEvent, String fullType) {
        /*
         * Build the JSON simulating a hazard type selection in the HID.
         */
        Dict dict = new Dict();
        dict.put(HAZARD_EVENT_IDENTIFIER, selectedEvent.getEventID());
        dict.put(ISessionEventManager.ATTR_HAZARD_CATEGORY,
                AutoTestUtilities.HYDROLOGY);
        dict.put(HAZARD_EVENT_FULL_TYPE, fullType);
        return dict;
    }

    /**
     * Issues one or more products associated with hazard events. This version
     * tests the issue button on the Hazard Information Dialog.
     * 
     * @param
     * @return
     */
    void issueEvent() {
        eventBus.post(new HazardDetailAction(HazardAction.ISSUE.getValue()));
    }

    void previewEvent() {
        eventBus.post(new HazardDetailAction(HazardAction.PREVIEW.getValue()));
    }

    @SuppressWarnings("unchecked")
    Dict productsFromEditorView(ProductEditorViewForTesting editorView) {
        List<Dict> productsCollection = editorView
                .getGeneratedProductsDictList();
        Dict productCollection = productsCollection.get(0);
        Dict products = productCollection
                .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
        return products;
    }

    void runDamBreakRecommender() {
        Dict damBreakInfo = new Dict();
        damBreakInfo.put(DAM_NAME, BRANCH_OAK_DAM);
        damBreakInfo.put(URGENCY_LEVEL, LOW_CONFIDENCE_URGENCY_LEVEL);
        eventBus.post(new ToolAction(
                ToolAction.ToolActionEnum.RUN_TOOL_WITH_PARAMETERS,
                FunctionalTest.DAM_BREAK_FLOOD_RECOMMENDER, damBreakInfo));
    }

    void setAddToPendingMode(String mode) {
        SpatialDisplayAction action = new SpatialDisplayAction(
                HazardConstants.ADD_PENDING_TO_SELECTED, mode);
        eventBus.post(action);
    }

}
