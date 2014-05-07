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
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.List;
import java.util.Set;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Sets;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;

/**
 * Description: {@link FunctionalTest} of event filtering.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov      Initial creation
 * Jan 10, 2014 2890       bkowal      Now subscribes to a notification indicating
 *                                     that all product generation is complete.
 * Apr 09, 2014    2925       Chris.Golden Fixed to work with new HID event propagation.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class FilteringFunctionalTest extends FunctionalTest {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private final Set<String> visibleTypes = Sets
            .newHashSet(AutoTestUtilities.FLASH_FLOOD_WATCH_PHEN_SIG);

    private final Set<String> visibleSites = Sets
            .newHashSet(AutoTestUtilities.OAX);

    private Settings savedCurrentSettings;

    private enum Steps {
        START, CHANGE_TO_TORNADO, BACK_TO_CANNED_FLOOD,

        CHANGING_CURRENT_SETTTINGS, CHANGE_BACK_CURRENT_SETTINGS
    }

    private Steps step;

    public FilteringFunctionalTest(HazardServicesAppBuilder appBuilder) {
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
    public void sessionEventModifiedOccurred(SessionEventModified action) {
        try {
            if (step == Steps.START) {
                ObservedHazardEvent event = autoTestUtilities
                        .getSelectedEvent();
                if (!"FF".equals(event.getPhenomenon())
                        || !"A".equals(event.getSignificance())) {
                    return;
                }
                stepCompleted();
                step = Steps.CHANGE_TO_TORNADO;
                autoTestUtilities.changeStaticSettings(CANNED_TORNADO_SETTING);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void staticSettingsActionOccurred(
            final StaticSettingsAction settingsAction) {
        switch (step) {

        case CHANGE_TO_TORNADO:
            List<Dict> events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 0);
            stepCompleted();
            step = Steps.BACK_TO_CANNED_FLOOD;
            autoTestUtilities.changeStaticSettings(CANNED_FLOOD_SETTING);
            break;

        case BACK_TO_CANNED_FLOOD:
            events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 1);
            autoTestUtilities.issueEvent();
            break;

        default:
            testError();
            break;

        }

    }

    @Handler(priority = -1)
    public void currentSettingsActionOccurred(
            final CurrentSettingsAction settingsAction) {
        List<Dict> events = mockConsoleView.getHazardEvents();
        switch (step) {

        case CHANGING_CURRENT_SETTTINGS:
            events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 0);
            stepCompleted();
            step = Steps.CHANGE_BACK_CURRENT_SETTINGS;
            autoTestUtilities.changeCurrentSettings(savedCurrentSettings);
            break;

        case CHANGE_BACK_CURRENT_SETTINGS:
            events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 1);
            stepCompleted();
            testSuccess();
            break;

        default:
            testError();
            break;

        }

    }

    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {

        Settings currentSettings = appBuilder.getCurrentSettings();
        savedCurrentSettings = new Settings(currentSettings);
        Set<String> visibleStates = Sets.newHashSet();
        Settings settings = autoTestUtilities.buildEventFilterCriteria(
                visibleTypes, visibleStates, visibleSites);
        stepCompleted();
        step = Steps.CHANGING_CURRENT_SETTTINGS;
        autoTestUtilities.changeCurrentSettings(settings);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
