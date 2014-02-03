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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_FULL_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.TopologyException;

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

    static final String CAUSE = "cause";

    static final String OAX = "OAX";

    static final String AREAL_FLOOD_WATCH_PHEN = "FA";

    static final String AREAL_FLOOD_WATCH_PHEN_SIG = AREAL_FLOOD_WATCH_PHEN
            + ".A";

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
        try {
            hazardEventBuilder.buildPolygonHazardEvent(coordinates);
        } catch (InvalidGeometryException e) {
            throw new TopologyException(e.getMessage());
        }
    }

    void assignSelectedEventType(String eventType) {
        IHazardEvent selectedEvent = getSelectedEvent();
        assignEventType(eventType, selectedEvent);
    }

    void assignEventType(String eventType, IHazardEvent selectedEvent) {
        Dict dict = buildEventTypeSelection(selectedEvent, eventType);

        HazardDetailAction hazardDetailAction = new HazardDetailAction(
                HazardDetailAction.ActionType.UPDATE_EVENT_TYPE);
        hazardDetailAction.setJSONText(dict.toJSONString());
        appBuilder.getEventBus().post(hazardDetailAction);
    }

    IHazardEvent getSelectedEvent() {
        IHazardEvent selectedEvent = appBuilder.getSessionManager()
                .getEventManager().getSelectedEvents().iterator().next();
        return selectedEvent;
    }

    Settings buildEventFilterCriteria(Set<String> visibleTypes,
            Set<String> visibleStates, Set<String> visibleSites) {
        Settings result = new Settings();
        result.setVisibleTypes(visibleTypes);
        result.setVisibleStates(visibleStates);
        result.setVisibleSites(visibleSites);
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
        eventBus.post(new HazardDetailAction(
                HazardDetailAction.ActionType.ISSUE));
    }

    void previewEvent() {
        eventBus.post(new HazardDetailAction(
                HazardDetailAction.ActionType.PREVIEW));
    }

    @SuppressWarnings("unchecked")
    Dict productsFromEditorView(ProductEditorViewForTesting editorView) {
        GeneratedProductList generatedProducts = editorView
                .getGeneratedProductList();
        IGeneratedProduct generatedProduct = generatedProducts.get(0);
        Dict d = new Dict();
        String productID = generatedProduct.getProductID();
        d.put("productID", productID);
        Dict products = new Dict();
        for (String format : generatedProduct.getEntries().keySet()) {
            products.put(format,
                    generatedProduct.getEntries().get(format).get(0));
        }

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

    void setAddToPendingMode(SpatialDisplayAction.ActionIdentifier mode) {
        SpatialDisplayAction action = new SpatialDisplayAction(
                SpatialDisplayAction.ActionType.ADD_PENDING_TO_SELECTED, mode);
        eventBus.post(action);
    }

    void changeStaticSettings(String settingsID) {
        StaticSettingsAction action = new StaticSettingsAction(
                StaticSettingsAction.ActionType.SETTINGS_CHOSEN, settingsID);
        eventBus.post(action);
    }

    void changeCurrentSettings(Settings settings) {
        CurrentSettingsAction action = new CurrentSettingsAction(settings);
        eventBus.post(action);
    }

    void updateSelectedEventAttributes(Dict updatedEventAttributes) {
        IHazardEvent selectedEvent = getSelectedEvent();
        updatedEventAttributes.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                selectedEvent.getEventID());
        SpatialDisplayAction displayAction = new SpatialDisplayAction(
                SpatialDisplayAction.ActionType.UPDATE_EVENT_METADATA);
        displayAction.setToolParameters(updatedEventAttributes);
        eventBus.post(displayAction);
    }

    /*
     * This is a helper method to convert a GeneratedProductList to a
     * List<Dict>. This method will go away as part of the JSON refacctor.
     */
    @Deprecated
    public static List<Dict> createGeneratedProductsDictList(
            GeneratedProductList generatedProducts) {
        List<Dict> generatedProductsDictList = new ArrayList<Dict>();
        if (generatedProducts != null) {

            for (IGeneratedProduct generatedProduct : generatedProducts) {
                Dict d = new Dict();
                String productID = generatedProduct.getProductID();
                d.put("productID", productID);
                Dict val = new Dict();
                for (String format : generatedProduct.getEntries().keySet()) {
                    val.put(format, generatedProduct.getEntries().get(format)
                            .get(0));
                }

                d.put("products", val);
                generatedProductsDictList.add(d);
            }
        }

        return generatedProductsDictList;
    }

}
