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
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction.ToolActionEnum;
import gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.DamBreakUrgencyLevels;

import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;

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
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AddNewPendingToSelectedTest extends FunctionalTest {

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    private enum Steps {
        START, EVENT0, EVENT1, RUN_TOOL, PREVIEW, TEAR_DOWN
    }

    private Steps step;

    public AddNewPendingToSelectedTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        try {
            super.run();

        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {

        step = Steps.START;
        autoTestUtilities
                .setAddToPendingMode(SpatialDisplayAction.ActionIdentifier.ON);

    }

    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            switch (step) {
            case START:
                step = Steps.EVENT0;
                autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X, 41.0);
                break;

            case TEAR_DOWN:
                testSuccess();
                break;

            default:
                testError();
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void handleNewHazardEvent(SessionEventAdded action) {
        switch (step) {
        case EVENT0:
            autoTestUtilities
                    .assignSelectedEventType(AutoTestUtilities.AREAL_FLOOD_WATCH_FULLTYPE);

            break;

        case EVENT1:
            Iterator<IHazardEvent> iterator = appBuilder.getSessionManager()
                    .getEventManager().getSelectedEvents().iterator();

            IHazardEvent event = iterator.next();
            event = skipToJustCreatedEventIfNecessary(iterator, event);

            autoTestUtilities.assignEventType(
                    AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE, event);
            step = Steps.RUN_TOOL;
            break;

        default:
            break;

        }
    }

    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        try {
            switch (step) {
            case EVENT0:
                step = Steps.EVENT1;
                autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                        FIRST_EVENT_CENTER_Y + 3 * EVENT_BUILDER_OFFSET);
                break;

            case RUN_TOOL:
                autoTestUtilities
                        .runDamBreakRecommender(DamBreakUrgencyLevels.LOW_CONFIDENCE_URGENCY_LEVEL);
                break;

            case PREVIEW:
                break;

            default:
                testError();

            }
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void toolActionOccurred(final ToolAction action) {
        try {
            if (action.getActionType().equals(
                    ToolActionEnum.TOOL_RECOMMENDATIONS)) {
                step = Steps.PREVIEW;
                autoTestUtilities.previewEvent();
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {
        EventSet<IEvent> eventSet = generated.getProducts().get(0)
                .getEventSet();
        assertEquals(eventSet.size(), 3);
        step = Steps.TEAR_DOWN;
        autoTestUtilities
                .setAddToPendingMode(SpatialDisplayAction.ActionIdentifier.OFF);

    }

    private IHazardEvent skipToJustCreatedEventIfNecessary(
            Iterator<IHazardEvent> iterator, IHazardEvent event) {
        if (event.getPhenomenon().equals(AREAL_FLOOD_WATCH_PHEN)) {
            event = iterator.next();
        }
        return event;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
