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
 * Nov 15, 2013  2182      daniel.s.schaffer@noaa.gov Initial creation.
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
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
            new ChangeHazardAreaFunctionalTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                ChangeHazardAreaFunctionalTest.class)) {
            new ProductStagingDialogTest(appBuilder).run();
        }

        else if (testCompleted.getTestClass().equals(
                ProductStagingDialogTest.class)) {
            new SampleFunctionalTest(appBuilder).run();
        }

        else {
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
