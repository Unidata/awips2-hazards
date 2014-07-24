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

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.test.FunctionalTest.StopTesting;
import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Description: Automated functional tests
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 08, 2013  2166      daniel.s.schaffer@noaa.gov
 * Oct 29, 2013 2166       daniel.s.schaffer@noaa.gov      Cleaned up handling of errors
 * Nov 14, 2013 1463       bryon.lawrence                  Added test for hazard conflict 
 *                                                         detection.
 * Nov 15, 2013  2182      daniel.s.schaffer@noaa.gov Initial creation.
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Added auto-tests
 * 
 *  
 * Nov 29, 2013 2380    daniel.s.schaffer@noaa.gov Added call of {@link FilteringFunctionalTest}
 * May 18, 2014    2925       Chris.Golden Changed to ensure that ongoing preview and
 *                                         ongoing issue flags are set to false at the
 *                                         end of each test.
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class AutomatedTests {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private HazardServicesAppBuilder appBuilder;

    /**
     * Functional test that is currently running. This must be referenced in
     * order to keep it from being garbage collected. When using the guava
     * <code>EventBus</code>, this was not required, because the
     * <code>EventBus</code> itself kept a strong reference to anything
     * registered with it; but by design, the {@link BoundedReceptionEventBus}
     * uses weak references, and thus if this reference is not kept, garbage
     * collection of the test occurs.
     */
    private FunctionalTest<? extends Enum<?>> functionalTest;

    public AutomatedTests() {
    }

    public void run(final HazardServicesAppBuilder appBuilder) {

        this.appBuilder = appBuilder;
        appBuilder.getEventBus().subscribe(this);
    }

    @Handler(priority = -1)
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        if (consoleAction.getActionType().equals(
                ConsoleAction.ActionType.RUN_AUTOMATED_TESTS)) {
            runTest(new MixedHazardStoryFunctionalTest(appBuilder));
        }
    }

    @Handler(priority = -1)
    public void testCompleted(final TestCompleted testCompleted) {
        if (testCompleted.getTestClass().equals(
                MixedHazardStoryFunctionalTest.class)) {
            runTest(new StormTrackFunctionalTest(appBuilder));

        }

        else if (testCompleted.getTestClass().equals(
                StormTrackFunctionalTest.class)) {
            runTest(new AddNewPendingToSelectedTest(appBuilder));
        }

        else if (testCompleted.getTestClass().equals(
                AddNewPendingToSelectedTest.class)) {
            runTest(new ChangeHazardAreaFunctionalTest(appBuilder));
        }

        else if (testCompleted.getTestClass().equals(
                ChangeHazardAreaFunctionalTest.class)) {
            runTest(new ChangeHazardEndTimeFunctionalTest(appBuilder));
        }

        else if (testCompleted.getTestClass().equals(
                ChangeHazardEndTimeFunctionalTest.class)) {
            runTest(new ProductStagingDialogTest(appBuilder));
        }

        else if (testCompleted.getTestClass().equals(
                ProductStagingDialogTest.class)) {
            runTest(new FilteringFunctionalTest(appBuilder));
        }

        else if (testCompleted.getTestClass().equals(
                FilteringFunctionalTest.class)) {
            runTest(new ContextMenuFunctionalTest(appBuilder));

        } else if (testCompleted.getTestClass().equals(
                ContextMenuFunctionalTest.class)) {
            runTest(new HazardConflictFunctionalTest(appBuilder));

        } else if (testCompleted.getTestClass().equals(
                HazardConflictFunctionalTest.class)) {
            runTest(new DamBreakFunctionalTest(appBuilder));

        } else if (testCompleted.getTestClass().equals(StopTesting.class)) {
            functionalTest = null;
            System.err.println("Stopping tests");
            resetEvents();
            appBuilder.getSessionManager().setPreviewOngoing(false);
            appBuilder.getSessionManager().setIssueOngoing(false);

        } else {
            functionalTest = null;
            System.out.println("All tests completed");

            /**
             * TODO If the HID is detached, in causes a an error in
             * {@link HazardDetailViewPart#configureScrolledCompositeToHoldPanel}
             * when it calls metadataContentPanel.layout(); Chris is working the
             * problem.
             */
            resetEvents();

        }

    }

    private void runTest(FunctionalTest<? extends Enum<?>> test) {
        functionalTest = test;
        functionalTest.run();
    }

    private void resetEvents() {
        appBuilder.getEventBus().publishAsync(
                new ConsoleAction(ConsoleAction.ActionType.RESET,
                        ConsoleAction.RESET_EVENTS));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
