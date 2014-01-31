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

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.deprecated.DeprecatedUtilities;
import gov.noaa.gsd.viz.hazards.jsonutilities.DeprecatedEvent;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Console;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;

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
            getView().setSettings(
                    configurationManager.getSettings().getSettingsID(),
                    configurationManager.getAvailableSettings());
        }
        if (changed.contains(HazardConstants.Element.CURRENT_SETTINGS)) {
            updateHazardEventsForSettingChange();
        }
        if (changed.contains(HazardConstants.Element.EVENTS)) {
            updateHazardEventsForEventChange();
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

        /**
         * TODO Can this be pushed into the super class? If so then all
         * subclasses would have to do that otherwise double registration will
         * occur.
         */
        eventBus.register(this);

        // Determine whether the time line navigation buttons should be in
        // the console toolbar, or below the console's table.
        boolean temporalControlsInToolBar = true;
        StartUpConfig startUpConfig = configurationManager.getStartUpConfig();
        if (startUpConfig != null) {
            Console console = startUpConfig.getConsole();
            if (console != null) {
                String timeLineNavigation = console.getTimeLineNavigation();
                if (timeLineNavigation != null) {
                    temporalControlsInToolBar = !(timeLineNavigation
                            .equals(HazardConstants.START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION_BELOW));
                }
            }
        }

        List<Dict> eventsAsDicts = adaptEventsForDisplay();

        // Initialize the view.
        view.initialize(this, timeManager.getSelectedTime(), timeManager
                .getCurrentTime(), configurationManager.getSettings()
                .getDefaultTimeDisplayDuration(), eventsAsDicts,
                configurationManager.getSettings(), configurationManager
                        .getAvailableSettings(), jsonConverter
                        .toJson(configurationManager.getFilterConfig()),
                alertsManager.getActiveAlerts(), temporalControlsInToolBar);
    }

    private List<Dict> adaptEventsForDisplay() {
        Collection<IHazardEvent> currentEvents = eventManager
                .getEventsForCurrentSettings();

        DeprecatedEvent[] jsonEvents = DeprecatedUtilities
                .eventsAsJSONEvents(currentEvents);
        DeprecatedUtilities.adaptJSONEvent(jsonEvents, currentEvents,
                configurationManager, timeManager);
        List<Dict> result = Lists.newArrayList();
        for (DeprecatedEvent event : jsonEvents) {
            result.add(Dict.getInstance(jsonConverter.toJson(event)));
        }
        return result;
    }

    // Private Methods

    private void updateHazardEventsForSettingChange() {
        List<Dict> eventsAsDicts = adaptEventsForDisplay();

        /**
         * Optimization. Only update the view of the hazard events if the
         * settings are changed by a different component. If it was by this
         * component, then the view current settings and the
         * configurationManager current settings will be identical so no need to
         * update the view. This optimization is particularly useful when you
         * sort the console rows!!!
         * 
         * TODO An optimization that was here needs to be resurrected. Need to
         * take another approach; probably by including the originator in the
         * {@link CurrentSettingsAction}. The code would skip call if the
         * originator is the Console.
         */

        getView().setHazardEvents(eventsAsDicts,
                configurationManager.getSettings());
    }

    private void updateHazardEventsForEventChange() {

        List<Dict> eventsAsDicts = adaptEventsForDisplay();

        // Get the hazard events as shown by the view, and compare them
        // to the hazard events in the model. If the number of events
        // and their identifiers are unchanged, then one or more events
        // have had their parameters changed; otherwise, the event set
        // as a whole has changed.
        List<Dict> oldEvents = getView().getHazardEvents();
        Map<String, Dict> oldEventsMap = null;
        Map<String, Dict> newEventsMap = null;
        boolean eventsListUnchanged = (eventsAsDicts.size() == oldEvents.size());
        if (eventsListUnchanged) {
            oldEventsMap = Maps.newHashMap();
            newEventsMap = Maps.newHashMap();
            for (int j = 0; j < eventsAsDicts.size(); j++) {
                Dict event = oldEvents.get(j);
                oldEventsMap.put((String) event
                        .get(HazardConstants.HAZARD_EVENT_IDENTIFIER), event);
                event = eventsAsDicts.get(j);
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
            getView().setHazardEvents(eventsAsDicts,
                    configurationManager.getSettings());
        }
    }

    @Subscribe
    public void alertsModified(HazardAlertsModified notification) {
        view.setActiveAlerts(notification.getActiveAlerts());
    }
}
