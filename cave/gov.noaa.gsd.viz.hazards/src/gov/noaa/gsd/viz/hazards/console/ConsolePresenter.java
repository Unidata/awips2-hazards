/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Console presenter, used to manage the console view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsolePresenter extends
        HazardServicesPresenter<IConsoleView<?, ?>> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsolePresenter.class);

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Console view to be handled by this presenter.
     */
    public ConsolePresenter(IHazardServicesModel model, IConsoleView<?, ?> view) {
        super(model, view);
    }

    // Public Methods

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public final void modelChanged(EnumSet<IHazardServicesModel.Element> changed) {
        if (changed.contains(IHazardServicesModel.Element.CURRENT_TIME)) {
            getView().updateCurrentTime(getModel().getCurrentTime());
        }
        if (changed.contains(IHazardServicesModel.Element.SELECTED_TIME)) {
            getView().updateSelectedTime(getModel().getSelectedTime());
        }
        if (changed.contains(IHazardServicesModel.Element.SELECTED_TIME_RANGE)) {
            getView()
                    .updateSelectedTimeRange(getModel().getSelectedTimeRange());
        }
        if (changed.contains(IHazardServicesModel.Element.VISIBLE_TIME_DELTA)) {
            getView().updateVisibleTimeDelta(getModel().getTimeLineDuration());
        }
        if (changed.contains(IHazardServicesModel.Element.VISIBLE_TIME_RANGE)) {
            getView().updateVisibleTimeRange(
                    getModel().getTimeLineEarliestVisibleTime(),
                    getModel().getTimeLineLatestVisibleTime());
        }
        if (changed.contains(IHazardServicesModel.Element.SETTINGS)) {
            getView().setSettings(getModel().getSettingsList());
        }
        if (changed.contains(IHazardServicesModel.Element.DYNAMIC_SETTING)
                || changed.contains(IHazardServicesModel.Element.EVENTS)) {
            updateHazardEvents();
        }
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    protected final void initialize(IConsoleView<?, ?> view) {

        // Determine whether the time line navigation buttons should be in
        // the console toolbar, or below the console's table.
        boolean temporalControlsInToolBar = true;
        Dict startUpConfig = Dict.getInstance(getModel().getConfigItem(
                Utilities.START_UP_CONFIG));
        if (startUpConfig != null) {
            Dict consoleConfig = startUpConfig
                    .getDynamicallyTypedValue(Utilities.START_UP_CONFIG_CONSOLE);
            if (consoleConfig != null) {
                String temporalControlsPosition = consoleConfig
                        .getDynamicallyTypedValue(Utilities.START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION);
                if (temporalControlsPosition != null) {
                    temporalControlsInToolBar = !(temporalControlsPosition
                            .equals(Utilities.START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION_BELOW));
                }
            }
        }

        // Initialize the view.
        view.initialize(
                this,
                Long.parseLong(getModel().getSelectedTime()),
                Long.parseLong(getModel().getCurrentTime()),
                Long.parseLong(getModel().getTimeLineDuration()),
                getModel().getComponentData(
                        HazardServicesAppBuilder.TEMPORAL_ORIGINATOR, "all"),
                getModel().getSettingsList(),
                getModel().getConfigItem(Utilities.FILTER_CONFIG),
                temporalControlsInToolBar);
    }

    // Private Methods

    /**
     * Update the hazard events in the view as needed.
     */
    private void updateHazardEvents() {

        // Compare the dynamic setting being used by the view versus the
        // one in the model. If they are not the same, then a complete
        // refresh is in order as if the events have changed.
        Dict oldSetting = getView().getDynamicSetting();
        String componentDataJSON = getModel().getComponentData(
                HazardServicesAppBuilder.TEMPORAL_ORIGINATOR, "all");
        Dict componentData = Dict.getInstance(componentDataJSON);
        Dict newSetting = componentData
                .getDynamicallyTypedValue(Utilities.TEMPORAL_DISPLAY_DYNAMIC_SETTING);
        if (oldSetting.equals(newSetting) == false) {
            getView().setHazardEvents(componentDataJSON);
            return;
        }

        // Get the hazard events as shown by the view, and compare them
        // to the hazard events in the model. If the number of events
        // and their identifiers are unchanged, then one or more events
        // have had their parameters changed; otherwise, the event set
        // as a whole has changed.
        List<Dict> oldEventsList = getView().getHazardEvents();
        List<Dict> newEventsList = componentData
                .getDynamicallyTypedValue(Utilities.TEMPORAL_DISPLAY_EVENTS);
        Map<String, Dict> oldEventsMap = null;
        Map<String, Dict> newEventsMap = null;
        boolean eventsListUnchanged = (newEventsList.size() == oldEventsList
                .size());
        if (eventsListUnchanged) {
            oldEventsMap = new HashMap<String, Dict>();
            newEventsMap = new HashMap<String, Dict>();
            for (int j = 0; j < newEventsList.size(); j++) {
                Dict event = oldEventsList.get(j);
                oldEventsMap.put(
                        (String) event.get(Utilities.HAZARD_EVENT_IDENTIFIER),
                        event);
                event = newEventsList.get(j);
                newEventsMap.put(
                        (String) event.get(Utilities.HAZARD_EVENT_IDENTIFIER),
                        event);
            }
            if (!oldEventsMap.keySet().equals(newEventsMap.keySet())) {
                eventsListUnchanged = false;
            }
        }

        // If one or more events have had their parameters changed,
        // iterate through them and determine which have been changed,
        // and update the view accordingly; otherwise, update the view's
        // entire list.
        if (eventsListUnchanged) {
            for (String identifier : oldEventsMap.keySet()) {
                if (!oldEventsMap.get(identifier).equals(
                        newEventsMap.get(identifier))) {
                    getView().updateHazardEvent(
                            newEventsMap.get(identifier).toJSONString());
                }
            }
        } else {
            getView().setHazardEvents(componentDataJSON);
        }
    }
}
