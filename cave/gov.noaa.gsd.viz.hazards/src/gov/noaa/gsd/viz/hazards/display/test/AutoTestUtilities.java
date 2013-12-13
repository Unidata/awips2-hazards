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
import gov.noaa.gsd.viz.hazards.display.action.NewHazardAction;
import gov.noaa.gsd.viz.hazards.display.action.SettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.vividsolutions.jts.geom.Coordinate;

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
 *  
 * Nov 29, 2013 2380    daniel.s.schaffer@noaa.gov Added code for test of settings-based filtering
 * 
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

    static final String HIGH_CONFIDENCE_URGENCY_LEVEL = "High Confidence (Structure Failure Imminent)";

    static final String DAM_HAS_FAILED_URGENCY_LEVEL = "Structure has Failed!!";

    static public enum DamBreakUrgencyLevels {

        LOW_CONFIDENCE_URGENCY_LEVEL(
                "Low Confidence (Potential Structure Failure)"), HIGH_CONFIDENCE_URGENCY_LEVEL(
                "High Confidence (Structure Failure Imminent)"), DAM_FAILED_URGENCY_LEVEL(
                "Structure has Failed!!");

        private DamBreakUrgencyLevels(final String text) {
            this.text = text;
        }

        private final String text;

        @Override
        public String toString() {
            return text;
        }

    }

    static final String URGENCY_LEVEL = "urgencyLevel";

    static final String BRANCH_OAK_DAM = "Branch Oak Dam";

    static final String DAM_NAME = "damName";

    static final double FORECAST_CONFIDENCE_VALUE = 50.0;

    static final String SET_CONFIDENCE = "Set confidence:";

    static final String FORECAST_TYPE = "forecastType";

    static final String FORECAST_CONFIDENCE_PERCENTAGE = "forecastConfidencePercentage";

    private final HazardServicesAppBuilder appBuilder;

    private final EventBus eventBus;

    private final HazardEventBuilder hazardEventBuilder;

    public AutoTestUtilities(HazardServicesAppBuilder appBuilder) {
        this.appBuilder = appBuilder;
        this.eventBus = appBuilder.getEventBus();
        this.hazardEventBuilder = new HazardEventBuilder(
                appBuilder.getSessionManager());
    }

    void createEvent(Double centerX, Double centerY) {
        Coordinate[] coordinates = buildEventArea(centerX, centerY);
        IHazardEvent hazardEvent = hazardEventBuilder
                .buildPolygonHazardEvent(coordinates);
        NewHazardAction action = new NewHazardAction(hazardEvent);
        eventBus.post(action);
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

    Dict buildEventFilterCriteria(List<String> visibleTypes,
            List<String> visibleStates, List<String> visibleSites) {
        Dict result = new Dict();
        DictList visibleTypesContainer = new DictList();
        result.put(SETTING_HAZARD_TYPES, visibleTypesContainer);
        visibleTypesContainer.addAll(visibleTypes);

        DictList visibleStatesContainer = new DictList();
        result.put(SETTING_HAZARD_STATES, visibleStatesContainer);
        visibleStatesContainer.addAll(visibleStates);

        DictList visibleSitesContainer = new DictList();
        result.put(SETTING_HAZARD_SITES, visibleSitesContainer);
        visibleSitesContainer.addAll(visibleSites);

        return result;
    }

    Coordinate[] buildEventArea(Double centerX, Double centerY) {
        List<Coordinate> result = Lists.newArrayList();

        result.add(buildPoint(centerX, centerY, -EVENT_BUILDER_OFFSET,
                -EVENT_BUILDER_OFFSET));
        result.add(buildPoint(centerX, centerY, EVENT_BUILDER_OFFSET,
                -EVENT_BUILDER_OFFSET));
        result.add(buildPoint(centerX, centerY, EVENT_BUILDER_OFFSET,
                EVENT_BUILDER_OFFSET));
        result.add(buildPoint(centerX, centerY, -EVENT_BUILDER_OFFSET,
                EVENT_BUILDER_OFFSET));
        result.add(buildPoint(centerX, centerY, -EVENT_BUILDER_OFFSET,
                -EVENT_BUILDER_OFFSET));
        return result.toArray(new Coordinate[result.size()]);
    }

    private Coordinate buildPoint(Double centerX, Double centerY,
            Double xOffset, Double yOffset) {
        return new Coordinate(centerX + xOffset, centerY + yOffset);
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

    void runDamBreakRecommender(DamBreakUrgencyLevels urgencyLevel) {
        Dict damBreakInfo = new Dict();
        damBreakInfo.put(DAM_NAME, BRANCH_OAK_DAM);
        damBreakInfo.put(URGENCY_LEVEL, urgencyLevel.toString());
        eventBus.post(new ToolAction(
                ToolAction.ToolActionEnum.RUN_TOOL_WITH_PARAMETERS,
                FunctionalTest.DAM_BREAK_FLOOD_RECOMMENDER, damBreakInfo));
    }

    void setAddToPendingMode(String mode) {
        SpatialDisplayAction action = new SpatialDisplayAction(
                HazardConstants.ADD_PENDING_TO_SELECTED, mode);
        eventBus.post(action);
    }

    void changeStaticSettings(String settingsID) {
        SettingsAction action = new SettingsAction(SETTING_CHOSEN, settingsID);
        eventBus.post(action);
    }

    void changeDynamicSettings(Dict settings) {
        SettingsAction action = new SettingsAction(DYNAMIC_SETTING_CHANGED,
                settings.toJSONString());
        eventBus.post(action);
    }

    void updateSelectedEventAttributes(Dict updatedEventAttributes) {
        IHazardEvent selectedEvent = getSelectedEvent();
        updatedEventAttributes.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                selectedEvent.getEventID());
        SpatialDisplayAction displayAction = new SpatialDisplayAction(
                HazardConstants.UPDATE_EVENT_METADATA);
        displayAction.setToolParameters(updatedEventAttributes);
        eventBus.post(displayAction);
    }

}
