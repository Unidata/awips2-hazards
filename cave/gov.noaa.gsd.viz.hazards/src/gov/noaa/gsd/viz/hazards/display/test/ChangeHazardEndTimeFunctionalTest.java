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

import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.EXT_VTEC_STRING;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import java.util.Date;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
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
 * Apr 29, 2014    2925   Chris.Golden Fixed to work with new HID event propagation.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ChangeHazardEndTimeFunctionalTest extends FunctionalTest {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

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

    @Override
    protected String getCurrentStep() {
        return step.toString();
    }

    private void stepCompleted() {
        statusHandler.debug("Completed step " + step);
    }

    private Date endTime = null;

    @Handler(priority = -1)
    public void sessionEventModifiedOccurred(SessionEventModified action) {
        if (step == Steps.START) {
            ObservedHazardEvent event = autoTestUtilities.getSelectedEvent();
            if (!"FF".equals(event.getPhenomenon())
                    || !"A".equals(event.getSignificance())) {
                return;
            }
            stepCompleted();
            step = Steps.ISSUE_FLASH_FLOOD_WATCH;
            autoTestUtilities.issueEvent();
        } else if (step == Steps.ISSUE_FLASH_FLOOD_WATCH) {
            if (autoTestUtilities.getSelectedEvent().getEndTime()
                    .equals(endTime) == false) {
                return;
            }
            stepCompleted();
            step = Steps.PREVIEW_MODIFIED_EVENT;
            autoTestUtilities.previewEvent();
        }
    }

    @Handler(priority = -1)
    public void sessionEventAddedOccurred(SessionEventAdded action) {
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
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        try {
            switch (step) {

            case ISSUE_FLASH_FLOOD_WATCH:
                IHazardEvent event = autoTestUtilities.getSelectedEvent();
                endTime = new Date(event.getEndTime().getTime()
                        + (30 * TimeUtil.MILLIS_PER_HOUR));
                event.setEndTime(endTime);
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
