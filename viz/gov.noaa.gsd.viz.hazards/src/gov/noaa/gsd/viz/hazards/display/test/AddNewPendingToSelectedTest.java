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

import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.AREAL_FLOOD_WATCH_PHEN;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.EVENT_BUILDER_OFFSET;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction.ToolActionEnum;
import gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.DamBreakUrgencyLevels;

import java.util.Collection;
import java.util.Iterator;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;

/**
 * Description: {@link FunctionalTest} of the "Add Pending to Selected"
 * functionality whereby when you create a new hazard, all other previous
 * selected events remain selected.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 15, 2013    2166       daniel.s.schaffer@noaa.gov      Initial creation
 * Jan 10, 2014    2890       bkowal      Now subscribes to a notification indicating
 *                                     that all product generation is complete.
 * Apr 09, 2014    2925       Chris.Golden Fixed to work with new HID event propagation.
 * May 18, 2014    2925       Chris.Golden More changes to get it to work with the new HID.
 *                                         Also changed to ensure that ongoing preview and
 *                                         ongoing issue flags are set to false at the end
 *                                         of each test, and moved the steps enum into the
 *                                         base class.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AddNewPendingToSelectedTest extends
        FunctionalTest<AddNewPendingToSelectedTest.Steps> {

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    protected enum Steps {
        START, EVENT0, EVENT1, PREPARE_TO_RUN_TOOL, RUN_TOOL, PREVIEW, TEAR_DOWN, TEST_ENDED
    }

    public AddNewPendingToSelectedTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    public void run() {
        try {
            super.run();

        } catch (Exception e) {
            handleException(e);
        }

    }

    @Override
    protected void runFirstStep() {
        step = Steps.START;
        autoTestUtilities
                .setAddToPendingMode(SpatialDisplayAction.ActionIdentifier.ON);
    }

    @Handler(priority = -1)
    public void sessionModifiedOccurred(SessionModified action) {
        if ((step == Steps.TEST_ENDED)
                && (appBuilder.getSessionManager().isIssueOngoing() == false)
                && (appBuilder.getSessionManager().isPreviewOngoing() == false)) {
            stepCompleted();
            testSuccess();
        }
    }

    @Handler(priority = -1)
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            switch (step) {
            case START:
                stepCompleted();
                step = Steps.EVENT0;
                autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X, 41.0);
                break;

            case TEAR_DOWN:
                stepCompleted();
                step = Steps.TEST_ENDED;

                /*
                 * Preview ongoing needs to be reset, since a preview was
                 * started but is being canceled.
                 */
                mockProductEditorView.invokeDismissButton();
                break;

            default:
                testError();
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void sessionEventModifiedOccurred(SessionEventModified action) {
        switch (step) {
        case EVENT0:

            /*
             * Do nothing if the event does not have a type yet.
             */
            Collection<ObservedHazardEvent> selectedEvents = appBuilder
                    .getSessionManager().getEventManager().getSelectedEvents();

            ObservedHazardEvent selectedEvent = selectedEvents.iterator()
                    .next();

            if (!"FA".equals(selectedEvent.getPhenomenon())
                    || !"A".equals(selectedEvent.getSignificance())) {
                return;
            }

            stepCompleted();
            step = Steps.EVENT1;
            autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                    FIRST_EVENT_CENTER_Y + 3 * EVENT_BUILDER_OFFSET);
            break;

        case PREPARE_TO_RUN_TOOL:
            Iterator<ObservedHazardEvent> iterator = appBuilder
                    .getSessionManager().getEventManager().getSelectedEvents()
                    .iterator();
            ObservedHazardEvent event = iterator.next();
            event = skipToJustCreatedEventIfNecessary(iterator, event);
            if (!"FF".equals(event.getPhenomenon())
                    || !"A".equals(event.getSignificance())) {
                return;
            }
            stepCompleted();
            step = Steps.RUN_TOOL;

            autoTestUtilities
                    .runDamBreakRecommender(DamBreakUrgencyLevels.LOW_CONFIDENCE_URGENCY_LEVEL);
            break;
        default:
            break;
        }
    }

    @Handler(priority = -1)
    public void sessionEventAddedOccurred(SessionEventAdded action) {
        switch (step) {
        case EVENT0:
            autoTestUtilities
                    .assignSelectedEventType(AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);

            break;

        case EVENT1:
            Iterator<ObservedHazardEvent> iterator = appBuilder
                    .getSessionManager().getEventManager().getSelectedEvents()
                    .iterator();

            ObservedHazardEvent event = iterator.next();
            event = skipToJustCreatedEventIfNecessary(iterator, event);

            String[] phenSigSubType = HazardEventUtilities
                    .getHazardPhenSigSubType(AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE);

            eventManager.setEventType(event, phenSigSubType[0],
                    phenSigSubType[1], phenSigSubType[2],
                    UIOriginator.HAZARD_INFORMATION_DIALOG);
            stepCompleted();
            step = Steps.PREPARE_TO_RUN_TOOL;
            break;

        default:
            break;

        }
    }

    @Handler(priority = -1)
    public void toolActionOccurred(final ToolAction action) {
        try {
            if ((step == Steps.RUN_TOOL)
                    && action.getActionType().equals(
                            ToolActionEnum.TOOL_RECOMMENDATIONS)) {
                stepCompleted();
                step = Steps.PREVIEW;
                autoTestUtilities.previewFromHID();
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        try {
            if (step == Steps.PREVIEW) {
                assertEquals(productGenerationComplete.getGeneratedProducts()
                        .size(), 1);
                EventSet<IEvent> eventSet = productGenerationComplete
                        .getGeneratedProducts().get(0).getEventSet();
                assertEquals(eventSet.size(), 3);
                stepCompleted();
                step = Steps.TEAR_DOWN;
                autoTestUtilities
                        .setAddToPendingMode(SpatialDisplayAction.ActionIdentifier.OFF);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private ObservedHazardEvent skipToJustCreatedEventIfNecessary(
            Iterator<ObservedHazardEvent> iterator, ObservedHazardEvent event) {
        if ((event.getPhenomenon() != null)
                && event.getPhenomenon().equals(AREAL_FLOOD_WATCH_PHEN)) {
            event = iterator.next();
        }
        return event;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
