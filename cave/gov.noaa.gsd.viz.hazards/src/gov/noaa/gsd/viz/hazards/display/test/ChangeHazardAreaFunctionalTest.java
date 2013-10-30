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
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;

/**
 * Description: {@link FunctionalTest} of the simple hazard story.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ChangeHazardAreaFunctionalTest extends FunctionalTest {

    private enum Steps {
        START, ISSUE_FLASH_FLOOD_WATCH, PREVIEW_MODIFIED_EVENT
    }

    private Steps step;

    public ChangeHazardAreaFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        try {
            super.run();
            SpatialDisplayAction displayAction = new SpatialDisplayAction(
                    HazardConstants.NEW_EVENT_SHAPE);
            Dict toolParameters = buildEventArea();
            displayAction.setToolParameters(toolParameters);
            step = Steps.START;
            eventBus.post(displayAction);
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            String actionType = spatialDisplayAction.getActionType();

            if (actionType.equals(HazardConstants.NEW_EVENT_SHAPE)) {
                IHazardEvent selectedEvent = getEvent();

                Dict dict = buildEventTypeTypeSelection(selectedEvent,
                        AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);

                HazardDetailAction hazardDetailAction = new HazardDetailAction(
                        HazardConstants.UPDATE_EVENT_TYPE);
                hazardDetailAction.setJSONText(dict.toJSONString());
                eventBus.post(hazardDetailAction);
            } else {
                step = Steps.PREVIEW_MODIFIED_EVENT;
                previewEvent(eventBus);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            if (step == Steps.START) {
                step = Steps.ISSUE_FLASH_FLOOD_WATCH;
                issueEvent(eventBus);
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @SuppressWarnings({ "rawtypes" })
    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {
        try {
            switch (step) {

            case ISSUE_FLASH_FLOOD_WATCH:
                SpatialDisplayAction modifyAreaAction = new SpatialDisplayAction(
                        HazardConstants.MODIFY_EVENT_AREA);
                IHazardEvent event = getEvent();
                String eventID = event.getEventID();
                Dict modifiedEvent = buildEventArea();
                List shapes = modifiedEvent
                        .getDynamicallyTypedValue(HazardConstants.SHAPES);
                Dict floodEvent = (Dict) shapes.get(0);
                List<List<Double>> points = floodEvent
                        .getDynamicallyTypedValue(HazardConstants.POINTS);
                List<Double> onePoint = points.get(3);
                onePoint.set(1, 39.0);
                modifiedEvent.put(HazardConstants.EVENTID, eventID);
                modifyAreaAction.setModifyEventJSON(modifiedEvent
                        .toJSONString());
                eventBus.post(modifyAreaAction);
                break;

            case PREVIEW_MODIFIED_EVENT:

                Dict products = productsFromEditorView(mockProductEditorView);
                String legacy = products
                        .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
                assertTrue(legacy.contains(EXA_VTEC_STRING + "."
                        + FLASH_FLOOD_WATCH_PHEN_SIG));
                assertTrue(legacy.contains(CON_VTEC_STRING + "."
                        + FLASH_FLOOD_WATCH_PHEN_SIG));
                testSuccess();

                break;

            default:
                testError();
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    private IHazardEvent getEvent() {
        IHazardEvent selectedEvent = appBuilder.getSessionManager()
                .getEventManager().getSelectedEvents().iterator().next();
        return selectedEvent;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
