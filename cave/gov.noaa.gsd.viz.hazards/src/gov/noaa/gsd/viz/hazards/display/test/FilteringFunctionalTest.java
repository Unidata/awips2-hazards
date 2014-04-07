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
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.List;
import java.util.Set;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
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
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class FilteringFunctionalTest extends FunctionalTest {

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

    @Handler(priority = -1)
    public void handleNewHazard(SessionEventAdded action) {

        try {
            switch (step) {
            case START:
                IHazardEvent event = autoTestUtilities.getSelectedEvent();

                autoTestUtilities.assignEventType(
                        AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE, event);
                break;

            default:
                break;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        switch (step) {
        case START:
            step = Steps.CHANGE_TO_TORNADO;
            autoTestUtilities.changeStaticSettings(CANNED_TORNADO_SETTING);
            break;

        default:
            break;

        }
    }

    @Handler(priority = -1)
    public void staticSettingsActionOccurred(
            final StaticSettingsAction settingsAction) {
        switch (step) {

        case CHANGE_TO_TORNADO:
            List<Dict> events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 0);
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
            step = Steps.CHANGE_BACK_CURRENT_SETTINGS;
            autoTestUtilities.changeCurrentSettings(savedCurrentSettings);
            break;

        case CHANGE_BACK_CURRENT_SETTINGS:
            events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 1);
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
        step = Steps.CHANGING_CURRENT_SETTTINGS;
        autoTestUtilities.changeCurrentSettings(settings);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
