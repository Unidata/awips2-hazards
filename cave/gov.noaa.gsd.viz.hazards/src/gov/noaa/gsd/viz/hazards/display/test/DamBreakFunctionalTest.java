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
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.DamBreakUrgencyLevels;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;

/**
 * Description: {@link FunctionalTest} of the Dam Break Flood recommender. This
 * tests to ensure the correct hazard events are created based on the
 * forecaster-selected urgency level in the Dam Break Tool Dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 12, 2013 2560       blawrenc    Initial creation
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
class DamBreakFunctionalTest extends FunctionalTest {

    private enum Steps {
        RUN_DAM_BREAK_LOW_CONFIDENCE, RUN_DAM_BREAK_HIGH_CONFIDENCE, RUN_DAM_BREAK_DAM_FAILED, TEST_ENDED
    }

    private Steps step;

    DamBreakFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        try {
            super.run();
            this.step = Steps.RUN_DAM_BREAK_LOW_CONFIDENCE;
            eventBus.post(new ToolAction(ToolAction.ToolActionEnum.RUN_TOOL,
                    DAM_BREAK_FLOOD_RECOMMENDER));
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void toolActionOccurred(final ToolAction action) {
        try {
            List<Dict> hazards;
            switch (action.getActionType()) {
            case RUN_TOOL:
                switch (step) {
                case RUN_DAM_BREAK_LOW_CONFIDENCE:

                    autoTestUtilities
                            .runDamBreakRecommender(DamBreakUrgencyLevels.LOW_CONFIDENCE_URGENCY_LEVEL);
                    break;

                case RUN_DAM_BREAK_HIGH_CONFIDENCE:

                    autoTestUtilities
                            .runDamBreakRecommender(DamBreakUrgencyLevels.HIGH_CONFIDENCE_URGENCY_LEVEL);
                    break;

                case RUN_DAM_BREAK_DAM_FAILED:

                    autoTestUtilities
                            .runDamBreakRecommender(DamBreakUrgencyLevels.DAM_FAILED_URGENCY_LEVEL);
                    break;

                default:
                    testError();
                    break;
                }
                break;

            case TOOL_RECOMMENDATIONS:
                switch (step) {
                case RUN_DAM_BREAK_LOW_CONFIDENCE:
                    DictList hidContents;
                    hazards = mockConsoleView.getHazardEvents();
                    assertEquals(hazards.size(), 1);

                    Dict event = hazards.get(0);
                    checkDamBreakRecommendationLowConfidence(event);

                    hidContents = mockHazardDetailView.getContents();

                    assertEquals(hidContents.size(), 1);
                    Dict hidEvent = (Dict) hidContents.get(0);
                    checkDamBreakRecommendationLowConfidence(hidEvent);

                    step = Steps.RUN_DAM_BREAK_HIGH_CONFIDENCE;
                    eventBus.post(new ToolAction(
                            ToolAction.ToolActionEnum.RUN_TOOL,
                            DAM_BREAK_FLOOD_RECOMMENDER));
                    break;

                case RUN_DAM_BREAK_HIGH_CONFIDENCE:
                    hazards = mockConsoleView.getHazardEvents();
                    assertEquals(hazards.size(), 2);

                    event = hazards.get(1);
                    checkDamBreakRecommendationHighConfidence(event);

                    hidContents = mockHazardDetailView.getContents();

                    assertEquals(hidContents.size(), 1);
                    hidEvent = (Dict) hidContents.get(0);
                    checkDamBreakRecommendationHighConfidence(hidEvent);

                    step = Steps.RUN_DAM_BREAK_DAM_FAILED;
                    eventBus.post(new ToolAction(
                            ToolAction.ToolActionEnum.RUN_TOOL,
                            DAM_BREAK_FLOOD_RECOMMENDER));

                    break;

                case RUN_DAM_BREAK_DAM_FAILED:
                    hazards = mockConsoleView.getHazardEvents();
                    assertEquals(hazards.size(), 3);

                    event = hazards.get(2);
                    checkDamBreakRecommendationDamFailed(event);

                    hidContents = mockHazardDetailView.getContents();

                    assertEquals(hidContents.size(), 1);
                    hidEvent = (Dict) hidContents.get(0);
                    checkDamBreakRecommendationDamFailed(hidEvent);

                    step = Steps.TEST_ENDED;
                    testSuccess();
                    break;

                default:
                    testError();
                    break;
                }

                break;
            default:
                break;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void checkDamBreakRecommendationLowConfidence(Dict event) {
        assertEquals(event.get(HAZARD_EVENT_TYPE), FLASH_FLOOD_WATCH_PHEN_SIG);
        assertEquals(event.get(SITE_ID), OAX);
        assertEquals(event.get(CAUSE), "Dam Failure");
        assertEquals(event.get(HAZARD_EVENT_STATE),
                HazardState.PENDING.getValue());

        assertEquals(asDouble(event.get(ISSUE_TIME)), new Double(1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_START_TIME)), new Double(
                1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_END_TIME)), new Double(
                1.2971484E12));
        assertEquals(event.get(HAZARD_EVENT_COLOR), "142 224 209");
    }

    private void checkDamBreakRecommendationHighConfidence(Dict event) {
        assertEquals(event.get(HAZARD_EVENT_TYPE), FFW_NON_CONVECTIVE_PHEN_SIG);
        assertEquals(event.get(SITE_ID), OAX);
        assertEquals(event.get(CAUSE), "Dam Failure");
        assertEquals(event.get(HAZARD_EVENT_STATE),
                HazardState.PENDING.getValue());

        assertEquals(asDouble(event.get(ISSUE_TIME)), new Double(1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_START_TIME)), new Double(
                1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_END_TIME)), new Double(
                1.2971484E12));
        assertEquals(event.get(HAZARD_EVENT_COLOR), "255 255 255");
    }

    private void checkDamBreakRecommendationDamFailed(Dict event) {
        assertEquals(event.get(HAZARD_EVENT_TYPE), FFW_NON_CONVECTIVE_PHEN_SIG);
        assertEquals(event.get(SITE_ID), OAX);
        assertEquals(event.get(CAUSE), "Dam Failure");
        assertEquals(event.get(HAZARD_EVENT_STATE),
                HazardState.PENDING.getValue());

        assertEquals(asDouble(event.get(ISSUE_TIME)), new Double(1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_START_TIME)), new Double(
                1.2971376E12));
        assertEquals(asDouble(event.get(HAZARD_EVENT_END_TIME)), new Double(
                1.2971484E12));
        assertEquals(event.get(HAZARD_EVENT_COLOR), "255 255 255");
    }

    /**
     * Deal with the fact that sometimes the values are
     * ComparableLazilyParsedNumber and sometimes Doubles
     */
    private Double asDouble(Object object) {
        String doubleAsString = String.valueOf(object);
        Double result = Double.valueOf(doubleAsString);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
