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

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
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

    public AutomatedTests() {
    }

    public void run(final HazardServicesAppBuilder appBuilder) {

        this.appBuilder = appBuilder;
        appBuilder.getEventBus().register(this);

    }

    @Subscribe
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        if (consoleAction.getAction().equals(
                HazardConstants.RUN_AUTOMATED_TESTS)) {
            new MixedHazardStoryFunctionalTest(appBuilder).run();
        }
    }

    @Subscribe
    public void testCompleted(final TestCompleted testCompleted) {
        if (testCompleted.getTestClass().equals(
                MixedHazardStoryFunctionalTest.class)) {
            new SimpleHazardStoryFunctionalTest(appBuilder).run();

        }

        else if (testCompleted.getTestClass().equals(
                SimpleHazardStoryFunctionalTest.class)) {
            new AddNewPendingToSelectedTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                AddNewPendingToSelectedTest.class)) {
            new ChangeHazardAreaFunctionalTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                ChangeHazardAreaFunctionalTest.class)) {
            new ChangeHazardEndTimeFunctionalTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                ChangeHazardEndTimeFunctionalTest.class)) {
            new ProductStagingDialogTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                ProductStagingDialogTest.class)) {
            new FilteringFunctionalTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                FilteringFunctionalTest.class)) {
            new ContextMenuFunctionalTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                ContextMenuFunctionalTest.class)) {
            new HazardConflictFunctionalTest(appBuilder).run();
        } else if (testCompleted.getTestClass().equals(
                HazardConflictFunctionalTest.class)) {
            new DamBreakFunctionalTest(appBuilder).run();
        } else {
            statusHandler.debug("All tests completed");
            appBuilder.getEventBus().post(
                    new ConsoleAction(HazardConstants.RESET_ACTION,
                            HazardConstants.RESET_EVENTS));
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
