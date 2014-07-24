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
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionModified;
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
 * May 18, 2014    2925   Chris.Golden More changes to get it to work with the new HID.
 *                                     Also changed to ensure that ongoing preview and
 *                                     ongoing issue flags are set to false at the end
 *                                     of each test, and moved the steps enum into the
 *                                     base class.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ChangeHazardEndTimeFunctionalTest extends
        FunctionalTest<ChangeHazardEndTimeFunctionalTest.Steps> {

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    protected enum Steps {
        START, ISSUE_FLASH_FLOOD_WATCH, READY_FOR_PREVIEW, PREVIEW_MODIFIED_EVENT, TEST_ENDED
    }

    public ChangeHazardEndTimeFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void runFirstStep() {
        step = Steps.START;
        autoTestUtilities.createEvent(-96.0, 41.0);
    }

    private Date endTime = null;

    boolean productEditorHasBeenUpdated = false;

    @Handler(priority = -1)
    public void sessionModifiedOccurred(final SessionModified action) {
        if ((step == Steps.PREVIEW_MODIFIED_EVENT)
                && (appBuilder.getSessionManager().isIssueOngoing() == false)
                && appBuilder.getSessionManager().isPreviewOngoing()) {
            productEditorHasBeenUpdated = true;
        } else if (step == Steps.TEST_ENDED
                && (appBuilder.getSessionManager().isIssueOngoing() == false)
                && (appBuilder.getSessionManager().isPreviewOngoing() == false)) {
            stepCompleted();
            testSuccess();
        }
    }

    private void handleCompletedIssuance() {
        IHazardEvent event = autoTestUtilities.getSelectedEvent();
        endTime = new Date(event.getEndTime().getTime()
                + (30 * TimeUtil.MILLIS_PER_HOUR));
        event.setEndTime(endTime);
        stepCompleted();
        step = Steps.READY_FOR_PREVIEW;
    }

    @Handler(priority = -1)
    public void sessionEventStateModifiedOccurred(
            final SessionEventStatusModified action) {
        try {
            switch (step) {

            case ISSUE_FLASH_FLOOD_WATCH:
                handleCompletedIssuance();
                break;

            default:

            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void handleHazardEventTimeRangeModification(
            SessionEventTimeRangeModified action) {

        try {
            switch (step) {

            case READY_FOR_PREVIEW:
                if (autoTestUtilities.getSelectedEvent().getEndTime()
                        .equals(endTime) == false) {
                    return;
                }
                stepCompleted();
                step = Steps.PREVIEW_MODIFIED_EVENT;
                autoTestUtilities.previewEvent();
                break;

            default:
                break;

            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void sessionEventTypeModifiedOccurred(SessionEventTypeModified action) {
        if (step == Steps.START) {
            ObservedHazardEvent event = autoTestUtilities.getSelectedEvent();
            if (!"FF".equals(event.getPhenomenon())
                    || !"A".equals(event.getSignificance())) {
                return;
            }
            stepCompleted();
            step = Steps.ISSUE_FLASH_FLOOD_WATCH;
            autoTestUtilities.issueEvent();
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

            case PREVIEW_MODIFIED_EVENT:
                if (productEditorHasBeenUpdated == false) {
                    return;
                }

                Dict products = autoTestUtilities
                        .productsFromEditorView(mockProductEditorView);
                String legacy = products
                        .getDynamicallyTypedValue(ProductConstants.ASCII_PRODUCT_KEY);
                assertTrue(legacy.contains(EXT_VTEC_STRING));

                /*
                 * TODO Uncomment this when Issue #2447 is resolved.
                 */
                // assertTrue(legacy.contains(CAN_VTEC_STRING));

                stepCompleted();
                step = Steps.TEST_ENDED;

                /*
                 * Preview ongoing needs to be reset, since a preview was
                 * started but is being canceled.
                 */
                mockProductEditorView.invokeDismissButton();
                break;

            case ISSUE_FLASH_FLOOD_WATCH:
                handleCompletedIssuance();
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
