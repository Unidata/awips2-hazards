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
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.JSONUtilities;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import java.util.List;

import com.google.common.eventbus.EventBus;
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
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AutoTestUtilities {

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

    /**
     * JSON for creating a new event area. This simulates the user drawing an
     * event on the Spatial Display.
     */
    private static String newEventJSON = "{\"eventID\":\"\",\"shapes\":[{\"fill color\":\"White\",\"border thick\":2.0,\"borderStyle\":\"SOLID\",\"border color\":\"White\",\"points\":[[-96.26542325417236,41.32490969692897],[-96.42627540209477,41.336399136066284],[-96.57563811087988,40.888311009710975],[-96.12754998452456,40.876821570573654],[-96.26542325417236,41.32490969692897]],\"isSelected\":\"true\",\"isVisible\":\"true\",\"include\":\"true\",\"label\":\"\",\"shapeType\":\"polygon\"}]}";

    public static Dict buildEventArea() {
        return JSONUtilities.createDictFromJSON(newEventJSON);
    }

    static Dict buildEventTypeTypeSelection(IHazardEvent selectedEvent,
            String fullType) {
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
    static void issueEvent(EventBus eventBus) {
        eventBus.post(new HazardDetailAction(HazardAction.ISSUE.getValue()));
    }

    static void previewEvent(EventBus eventBus) {
        eventBus.post(new HazardDetailAction(HazardAction.PREVIEW.getValue()));
    }

    @SuppressWarnings("unchecked")
    static Dict productsFromEditorView(ProductEditorViewForTesting editorView) {
        List<Dict> productsCollection = editorView
                .getGeneratedProductsDictList();
        Dict productCollection = productsCollection.get(0);
        Dict products = productCollection
                .getDynamicallyTypedValue(ProductConstants.PRODUCTS);
        return products;
    }

}
