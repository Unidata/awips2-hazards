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
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.*;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.NewHazardAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;

/**
 * Description: {@link FunctionalTest} of changing the end time of an event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ChangeHazardEndTimeFunctionalTest extends FunctionalTest {

    private enum Steps {
        START, ISSUE_FLASH_FLOOD_WATCH, PREVIEW_MODIFIED_EVENT
    }

    private Steps step;

    public ChangeHazardEndTimeFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        step = Steps.START;
        autoTestUtilities.createEvent(-96.0, 41.0);
    }

    @Subscribe
    public void handleNewHazard(NewHazardAction action) {
        try {
            autoTestUtilities
                    .assignSelectedEventType(AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            if (hazardDetailAction.getActionType().equals(
                    HazardDetailAction.ActionType.ISSUE)
                    || hazardDetailAction.getActionType().equals(
                            HazardDetailAction.ActionType.PREVIEW)) {
                return;
            }
            if (step == Steps.START) {
                step = Steps.ISSUE_FLASH_FLOOD_WATCH;
                autoTestUtilities.issueEvent();
            } else if (step == Steps.ISSUE_FLASH_FLOOD_WATCH) {
                step = Steps.PREVIEW_MODIFIED_EVENT;
                autoTestUtilities.previewEvent();
            }

        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {
        try {
            switch (step) {

            case ISSUE_FLASH_FLOOD_WATCH:
                IHazardEvent event = autoTestUtilities.getSelectedEvent();
                String eventID = event.getEventID();
                Long endTimeInMillis = event.getEndTime().getTime();
                endTimeInMillis += 30 * TimeUtil.MILLIS_PER_HOUR;
                Dict updatedMetadata = new Dict();
                updatedMetadata.put(HAZARD_EVENT_IDENTIFIER, eventID);
                updatedMetadata.put(HAZARD_EVENT_START_TIME, event
                        .getStartTime().getTime());
                updatedMetadata.put(HAZARD_EVENT_END_TIME, endTimeInMillis);
                HazardDetailAction action = new HazardDetailAction(
                        HazardDetailAction.ActionType.UPDATE_TIME_RANGE,
                        updatedMetadata.toJSONString());
                eventBus.post(action);
                break;

            case PREVIEW_MODIFIED_EVENT:

                Dict products = autoTestUtilities
                        .productsFromEditorView(mockProductEditorView);
                String legacy = products
                        .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
                assertTrue(legacy.contains(EXT_VTEC_STRING));

                /*
                 * TODO Uncomment this when Issue #2447 is resolved.
                 */
                // assertTrue(legacy.contains(CAN_VTEC_STRING));
                testSuccess();

                break;

            default:
                testError();
            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
