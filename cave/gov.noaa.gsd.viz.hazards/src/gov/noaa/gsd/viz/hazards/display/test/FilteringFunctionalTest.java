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
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.SettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;

/**
 * Description: {@link FunctionalTest} of event filtering.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class FilteringFunctionalTest extends FunctionalTest {

    private final List<String> visibleTypes = Lists
            .newArrayList(AutoTestUtilities.FLASH_FLOOD_WATCH_PHEN_SIG);

    private final List<String> visibleSites = Lists
            .newArrayList(AutoTestUtilities.OAX);

    private Dict savedDynamicSettings;

    private enum Steps {
        START, BACK_TO_CANNED_FLOOD, CHANGING_DYNAMIC_SETTTINGS, CHANGE_BACK_DYNAMIC_SETTINGS
    }

    private Steps step;

    public FilteringFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        try {
            super.run();
            step = Steps.START;
            autoTestUtilities.createEvent(-96.0, 41.0);
        } catch (Exception e) {
            handleException(e);
        }

    }

    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            if (spatialDisplayAction.getActionType().equals(
                    HazardConstants.NEW_EVENT_SHAPE)) {
                IHazardEvent event = autoTestUtilities.getSelectedEvent();

                autoTestUtilities.assignEventType(
                        AutoTestUtilities.FLASH_FLOOD_WATCH_FULLTYPE, event);

            } else {
                testError();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Subscribe
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        switch (step) {
        case START:
            autoTestUtilities.changeStaticSettings(CANNED_TORNADO_SETTING);
            break;
        case BACK_TO_CANNED_FLOOD:
            break;

        default:
            break;

        }
    }

    @Subscribe
    public void settingsActionOccurred(final SettingsAction settingsAction) {
        switch (step) {

        case START:
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

        case CHANGING_DYNAMIC_SETTTINGS:
            events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 0);
            step = Steps.CHANGE_BACK_DYNAMIC_SETTINGS;
            autoTestUtilities.changeDynamicSettings(savedDynamicSettings);
            break;

        case CHANGE_BACK_DYNAMIC_SETTINGS:
            events = mockConsoleView.getHazardEvents();
            assertEquals(events.size(), 1);
            testSuccess();
            break;

        default:
            testError();
            break;

        }

    }

    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {

        List<String> visibleStates = Lists.newArrayList();
        savedDynamicSettings = Dict.getInstance(appBuilder.getSetting());
        Dict settings = autoTestUtilities.buildEventFilterCriteria(
                visibleTypes, visibleStates, visibleSites);
        step = Steps.CHANGING_DYNAMIC_SETTTINGS;
        autoTestUtilities.changeDynamicSettings(settings);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
