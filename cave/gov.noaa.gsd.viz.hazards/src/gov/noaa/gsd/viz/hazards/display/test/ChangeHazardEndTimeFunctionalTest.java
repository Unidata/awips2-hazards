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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.EXT_VTEC_STRING;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;

/**
 * Description: {@link FunctionalTest} of changing the end time of an event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Initial creation
 * Jan 10, 2014 2890       bkowal      Now subscribes to a notification indicating
 *                                     that all product generation is complete.
 * Apr 23, 2014 3357       bkowal      No longer error on an unexpected state. Just continue
 *                                     without modifying any data or the state.
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

    @Override
    protected void runFirstStep() {
        step = Steps.START;
        autoTestUtilities.createEvent(-96.0, 41.0);
    }

    @Handler(priority = -1)
    public void handleNewHazard(SessionEventAdded action) {
        try {
            switch (step) {
            case START:
                autoTestUtilities
                        .assignSelectedEventType(AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);

                break;

            default:
                break;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
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

    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        try {
            switch (step) {

            case ISSUE_FLASH_FLOOD_WATCH:
                IHazardEvent event = autoTestUtilities.getSelectedEvent();
                String eventID = event.getEventID();
                Long endTimeInMillis = event.getEndTime().getTime();
                endTimeInMillis += 30 * TimeUtil.MILLIS_PER_HOUR;
                Map<String, Serializable> updatedMetadata = new HashMap<>();
                updatedMetadata.put(HAZARD_EVENT_IDENTIFIER, eventID);
                updatedMetadata.put(HAZARD_EVENT_START_TIME, event
                        .getStartTime().getTime());
                updatedMetadata.put(HAZARD_EVENT_END_TIME, endTimeInMillis);
                HazardDetailAction action = new HazardDetailAction(
                        HazardDetailAction.ActionType.UPDATE_TIME_RANGE,
                        updatedMetadata);
                eventBus.publishAsync(action);
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
                // Do Nothing.
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
