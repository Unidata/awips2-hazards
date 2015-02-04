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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CREATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_COLOR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_STATUS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SITE_ID;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.CAUSE;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FFW_NON_CONVECTIVE_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.FLASH_FLOOD_WATCH_PHEN_SIG;
import static gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.OAX;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.test.AutoTestUtilities.DamBreakUrgencyLevels;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;

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
 * Apr 09, 2014    2925       Chris.Golden Fixed to work with new HID event propagation.
 * May 18, 2014    2925       Chris.Golden More changes to get it to work with the new HID.
 *                                         Also changed to ensure that ongoing preview and
 *                                         ongoing issue flags are set to false at the end
 *                                         of each test, and moved the steps enum into the
 *                                         base class.
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
class DamBreakFunctionalTest extends
        FunctionalTest<DamBreakFunctionalTest.Steps> {

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    protected enum Steps {
        RUN_DAM_BREAK_LOW_CONFIDENCE, RECEIVE_DAM_BREAK_LOW_CONFIDENCE_EVENTS, RUN_DAM_BREAK_HIGH_CONFIDENCE, RECEIVE_DAM_BREAK_HIGH_CONFIDENCE_EVENTS, RUN_DAM_BREAK_DAM_FAILED, RECEIVE_DAM_BREAK_DAM_FAILED_EVENTS, TEST_ENDED
    }

    DamBreakFunctionalTest(HazardServicesAppBuilder appBuilder) {
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

        /*
         * Create a new hazard area.
         */
        this.step = Steps.RUN_DAM_BREAK_LOW_CONFIDENCE;
        eventBus.publishAsync(new ToolAction(
                ToolAction.RecommenderActionEnum.RUN_RECOMENDER, settings
                        .getTool(DAM_BREAK_FLOOD_RECOMMENDER), "tbd"));
    }

    @Handler(priority = -1)
    public void sessionEventAddedOccurred(final SessionEventAdded action) {
        try {
            List<Dict> hazards;
            switch (step) {
            case RECEIVE_DAM_BREAK_LOW_CONFIDENCE_EVENTS:
                hazards = mockConsoleView.getHazardEvents();
                assertEquals(hazards.size(), 1);

                Dict event = hazards.get(0);
                checkDamBreakRecommendationLowConfidence(event);

                stepCompleted();
                step = Steps.RUN_DAM_BREAK_HIGH_CONFIDENCE;
                eventBus.publishAsync(new ToolAction(
                        ToolAction.RecommenderActionEnum.RUN_RECOMENDER,
                        settings.getTool(DAM_BREAK_FLOOD_RECOMMENDER), "tbd"));
                break;

            case RECEIVE_DAM_BREAK_HIGH_CONFIDENCE_EVENTS:
                hazards = mockConsoleView.getHazardEvents();
                assertEquals(hazards.size(), 2);

                event = hazards.get(1);
                checkDamBreakRecommendationHighConfidence(event);

                stepCompleted();
                step = Steps.RUN_DAM_BREAK_DAM_FAILED;
                eventBus.publishAsync(new ToolAction(
                        ToolAction.RecommenderActionEnum.RUN_RECOMENDER,
                        settings.getTool(DAM_BREAK_FLOOD_RECOMMENDER), "tbd"));

                break;

            case RECEIVE_DAM_BREAK_DAM_FAILED_EVENTS:
                hazards = mockConsoleView.getHazardEvents();
                assertEquals(hazards.size(), 3);

                event = hazards.get(2);
                checkDamBreakRecommendationDamFailed(event);

                stepCompleted();
                step = Steps.TEST_ENDED;
                testSuccess();
                break;

            default:
                testError();
                break;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void toolActionOccurred(final ToolAction action) {
        try {
            switch (action.getRecommenderActionType()) {
            case RUN_RECOMENDER:
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

            case RECOMMENDATIONS:
                switch (step) {
                case RUN_DAM_BREAK_LOW_CONFIDENCE:
                    stepCompleted();
                    step = Steps.RECEIVE_DAM_BREAK_LOW_CONFIDENCE_EVENTS;
                    break;

                case RUN_DAM_BREAK_HIGH_CONFIDENCE:
                    stepCompleted();
                    step = Steps.RECEIVE_DAM_BREAK_HIGH_CONFIDENCE_EVENTS;
                    break;

                case RUN_DAM_BREAK_DAM_FAILED:
                    stepCompleted();
                    step = Steps.RECEIVE_DAM_BREAK_DAM_FAILED_EVENTS;
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
        assertEquals(event.get(HAZARD_EVENT_STATUS),
                HazardStatus.PENDING.getValue());

        assertEquals(asDouble(event.get(CREATION_TIME)), new Double(
                1.2971376E12));
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
        assertEquals(event.get(HAZARD_EVENT_STATUS),
                HazardStatus.PENDING.getValue());

        assertEquals(asDouble(event.get(CREATION_TIME)), new Double(
                1.2971376E12));
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
        assertEquals(event.get(HAZARD_EVENT_STATUS),
                HazardStatus.PENDING.getValue());

        assertEquals(asDouble(event.get(CREATION_TIME)), new Double(
                1.2971376E12));
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
