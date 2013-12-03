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
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;

/**
 * Console presenter, used to manage the console view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Aug 16, 2013    1325    daniel.s.schaffer@noaa.gov    Alerts integration
 * Aug 22, 2013    1936    Chris.Golden      Added console countdown timers.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
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
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ConsolePresenter(ISessionManager model, IConsoleView<?, ?> view,
            EventBus eventBus) {
        super(model, view, eventBus);
    }

    // Public Methods

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public final void modelChanged(EnumSet<HazardConstants.Element> changed) {
        if (changed.contains(HazardConstants.Element.CURRENT_TIME)) {
            getView().updateCurrentTime(timeManager.getCurrentTime());
        }
        if (changed.contains(HazardConstants.Element.SELECTED_TIME)) {
            getView().updateSelectedTime(timeManager.getSelectedTime());
        }
        if (changed.contains(HazardConstants.Element.SELECTED_TIME_RANGE)) {
            TimeRange range = getModel().getTimeManager()
                    .getSelectedTimeRange();
            String updateTimeRange = jsonConverter.toJson(new String[] {
                    jsonConverter.fromDate(range.getStart()),
                    jsonConverter.fromDate(range.getEnd()) });
            getView().updateSelectedTimeRange(updateTimeRange);
        }
        if (changed.contains(HazardConstants.Element.VISIBLE_TIME_DELTA)) {
            String timeDelta = Long.toString(getModel()
                    .getConfigurationManager().getSettings()
                    .getDefaultTimeDisplayDuration());
            getView().updateVisibleTimeDelta(timeDelta);
        }
        if (changed.contains(HazardConstants.Element.VISIBLE_TIME_RANGE)) {
            String earliestTime = jsonConverter.fromDate(timeManager
                    .getVisibleRange().getStart());
            String latestTime = jsonConverter.fromDate(timeManager
                    .getVisibleRange().getEnd());
            getView().updateVisibleTimeRange(earliestTime, latestTime);
        }
        if (changed.contains(HazardConstants.Element.SETTINGS)) {
            getView().setSettings(modelAdapter.getSettingsList());
        }
        if (changed.contains(HazardConstants.Element.DYNAMIC_SETTING)
                || changed.contains(HazardConstants.Element.EVENTS)) {
            updateHazardEvents();
        }
        if (changed.contains(HazardConstants.Element.SITE)) {
            view.updateTitle(configurationManager.getSiteID());
        }
    }

    @Override
    public void dispose() {
        getModel().unregisterForNotification(this);
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    public final void initialize(IConsoleView<?, ?> view) {
        getModel().registerForNotification(this);

        // Determine whether the time line navigation buttons should be in
        // the console toolbar, or below the console's table.
        boolean temporalControlsInToolBar = true;
        String jsonStartUpConfig = modelAdapter
                .getConfigItem(Utilities.START_UP_CONFIG);
        if (jsonStartUpConfig != null) {
            Dict startUpConfig = Dict.getInstance(jsonStartUpConfig);
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
        view.initialize(this, timeManager.getSelectedTime(), timeManager
                .getCurrentTime(), configurationManager.getSettings()
                .getDefaultTimeDisplayDuration(), modelAdapter
                .getComponentData(HazardServicesAppBuilder.TEMPORAL_ORIGINATOR,
                        "all"), modelAdapter.getSettingsList(), modelAdapter
                .getConfigItem(Utilities.FILTER_CONFIG), alertsManager
                .getActiveAlerts(), temporalControlsInToolBar);
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
        String componentDataJSON = modelAdapter.getComponentData(
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
            oldEventsMap = Maps.newHashMap();
            newEventsMap = Maps.newHashMap();
            for (int j = 0; j < newEventsList.size(); j++) {
                Dict event = oldEventsList.get(j);
                oldEventsMap.put((String) event
                        .get(HazardConstants.HAZARD_EVENT_IDENTIFIER), event);
                event = newEventsList.get(j);
                newEventsMap.put((String) event
                        .get(HazardConstants.HAZARD_EVENT_IDENTIFIER), event);
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

    @Subscribe
    public void alertsModified(HazardAlertsModified notification) {
        view.setActiveAlerts(notification.getActiveAlerts());
    }
}
